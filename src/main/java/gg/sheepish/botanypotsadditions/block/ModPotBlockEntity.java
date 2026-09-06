package gg.sheepish.botanypotsadditions.block;

import net.darkhax.botanypots.common.impl.Helpers;
import gg.sheepish.botanypotsadditions.config.ModConfig;
import gg.sheepish.botanypotsadditions.registry.ModParticleTypes;
import gg.sheepish.botanypotsadditions.registry.ModBlockEntityTypes;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class ModPotBlockEntity extends BotanyPotBlockEntity {
    public static final int BASE_SEED_SLOT = 1;
    private static final int SPRINKLER_PARTICLES_PER_TICK = 8;
    private static final int VANILLA_SLOT_COUNT = 15;
    private static final int FIRST_OUTPUT_SLOT = 3;
    private static final int LAST_OUTPUT_SLOT = 14;

    private final int cellCount;
    private final int containerSize;
    private final boolean sprinkler;
    private final SprinklerEnergyStorage energyStorage;
    private final SprinklerFluidTank waterTank;

    public ModPotBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, cellCountForState(state), isSprinklerState(state));
    }

    public ModPotBlockEntity(BlockPos pos, BlockState state, int cellCount) {
        this(pos, state, cellCount, isSprinklerState(state));
    }

    public ModPotBlockEntity(BlockPos pos, BlockState state, int cellCount, boolean sprinkler) {
        super(ModBlockEntityTypes.CELLED_POT_AS_BOTANY, pos, state);
        this.cellCount = Math.max(1, cellCount);
        this.containerSize = VANILLA_SLOT_COUNT + Math.max(0, this.cellCount - 1);
        this.sprinkler = sprinkler;
        this.energyStorage = new SprinklerEnergyStorage(ModConfig.ENERGY_CAPACITY.get(), ModConfig.ENERGY_INPUT.get());
        this.waterTank = new SprinklerFluidTank(ModConfig.WATER_CAPACITY.get());

        if (this.containerSize > VANILLA_SLOT_COUNT) {
            setItems(NonNullList.withSize(this.containerSize, ItemStack.EMPTY));
        }
    }

    @Override
    public int getContainerSize() {
        return containerSize;
    }

    public int cellCount() {
        return cellCount;
    }

    public boolean isSprinkler() {
        return sprinkler;
    }

    public IEnergyStorage energyStorage(Direction side) {
        return sprinkler && side != Direction.UP && side != Direction.DOWN ? energyStorage : null;
    }

    public IFluidHandler waterTank(Direction side) {
        return sprinkler && side == Direction.UP ? waterTank : null;
    }

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public int getWaterStored() {
        return waterTank.getFluidAmount();
    }

    public int getMaxWaterStored() {
        return waterTank.getCapacity();
    }

    public boolean hasSprinklerGrowthResources() {
        return canSpendSprinklerResources();
    }

    public static int getSprinklerGrowthTicks(int energyStored, int waterStored) {
        return SprinklerGrowth.operations(energyStored, waterStored, ModConfig.ENERGY_CAPACITY.get(),
                ModConfig.energyRequired(), ModConfig.ENERGY_PER_OPERATION.get(), ModConfig.WATER_PER_OPERATION.get(),
                ModConfig.MIN_GROWTH.get(), ModConfig.MAX_GROWTH.get());
    }

    public int seedSlotForCell(int cell) {
        if (cell <= 0) {
            return BASE_SEED_SLOT;
        }

        return VANILLA_SLOT_COUNT + cell - 1;
    }

    public ItemStack getSeedItem(int cell) {
        int slot = seedSlotForCell(cell);
        return slot >= 0 && slot < getContainerSize() ? getItem(slot) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);

        if (isExtraSeedSlot(slot)) {
            markUpdated();
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        ListTag items = tag.getList("Items", 10);

        for (int cell = 1; cell < cellCount; cell++) {
            int slot = seedSlotForCell(cell);
            ItemStack stack = getItem(slot);

            if (!stack.isEmpty()) {
                CompoundTag itemTag = (CompoundTag) stack.save(registries, new CompoundTag());
                itemTag.putByte("Slot", (byte) slot);
                items.add(itemTag);
            }
        }

        tag.put("Items", items);
        saveSprinklerResources(tag, registries);
        return tag;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveSprinklerResources(tag, registries);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("SprinklerEnergy", Tag.TAG_INT)) {
            energyStorage.deserializeNBT(registries, tag.get("SprinklerEnergy"));
            energyStorage.clampStored();
        }

        if (tag.contains("SprinklerWater", Tag.TAG_COMPOUND)) {
            waterTank.readFromNBT(registries, tag.getCompound("SprinklerWater"));
            if (waterTank.getFluidAmount() > waterTank.getCapacity()) {
                waterTank.getFluid().setAmount(waterTank.getCapacity());
            }
        }
    }

    public static void tickModPot(Level level, BlockPos pos, BlockState state, ModPotBlockEntity pot) {
        if (level.isClientSide && pot.sprinkler && pot.hasSprinklerGrowthResources()) {
            spawnSprinklerParticles(level, pos);
        }

        float growthBefore = pot.growthTime.getTicks();
        boolean harvestedBaseSeed = false;

        if (!pot.sprinkler) {
            BotanyPotBlockEntity.tickPot(level, pos, state, pot);
        } else {
            int growthTicks = pot.getSprinklerGrowthTicks();

            for (int tick = 0; tick < growthTicks && pot.canSpendSprinklerResources(); tick++) {
                float tickGrowthBefore = pot.growthTime.getTicks();
                BotanyPotBlockEntity.tickPot(level, pos, state, pot);
                float tickGrowthAfter = pot.growthTime.getTicks();

                if (tickGrowthAfter > tickGrowthBefore) {
                    if (!level.isClientSide) {
                        pot.spendSprinklerResources();
                    }
                } else {
                    harvestedBaseSeed = tickGrowthBefore > tickGrowthAfter;
                    break;
                }
            }
        }

        if (!level.isClientSide && pot.isHopper() && (harvestedBaseSeed || growthBefore > pot.growthTime.getTicks())) {
            pot.harvestExtraCells(level);
            pot.markUpdated();
        } else if (!level.isClientSide && pot.isHopper() && pot.getSeedItem(0).isEmpty()) {
            pot.tickExtraCellsWithoutBaseSeed(level);
        }
    }

    private void tickExtraCellsWithoutBaseSeed(Level level) {
        int requiredGrowthTicks = getRequiredExtraCellGrowthTicks(level);

        int growthTicks = getSprinklerGrowthTicks();

        if (requiredGrowthTicks <= 0 || growthTicks <= 0) {
            return;
        }

        for (int tick = 0; tick < growthTicks && canSpendSprinklerResources(); tick++) {
            growthTime.tickUp(level);
            spendSprinklerResources();

            if (growthTime.getTicks() >= requiredGrowthTicks) {
                harvestExtraCells(level);
                growthTime.reset();
                markUpdated();
                break;
            }
        }
    }

    private int getRequiredExtraCellGrowthTicks(Level level) {
        int requiredGrowthTicks = -1;

        for (int cell = 1; cell < cellCount; cell++) {
            CellBotanyPotContext context = new CellBotanyPotContext(this, cell, null, null);
            Crop crop = context.getCrop();

            if (crop != null && crop.isGrowthSustained(context, level)) {
                int cellGrowthTicks = context.getRequiredGrowthTicks();

                if (cellGrowthTicks > 0) {
                    requiredGrowthTicks = requiredGrowthTicks < 0 ? cellGrowthTicks : Math.min(requiredGrowthTicks, cellGrowthTicks);
                }
            }
        }

        return requiredGrowthTicks;
    }

    private static void spawnSprinklerParticles(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();

        for (int particle = 0; particle < SPRINKLER_PARTICLES_PER_TICK; particle++) {
            double angle = random.nextDouble() * Math.PI * 2D;
            double targetRadius = Math.sqrt(random.nextDouble());
            double targetX = Math.cos(angle) * targetRadius * 0.15D;
            double targetZ = Math.sin(angle) * targetRadius * 0.15D;
            double travelTicks = 20D + random.nextDouble() * 5D;
            double xSpeed = targetX / travelTicks;
            double zSpeed = targetZ / travelTicks;
            double ySpeed = -0.028D - random.nextDouble() * 0.006D;

            level.addParticle(
                    ModParticleTypes.SPRINKLER_WATER.get(),
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.004D,
                    pos.getY() + 0.94D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.004D,
                    xSpeed,
                    ySpeed,
                    zSpeed);
        }
    }

    private void harvestExtraCells(Level level) {
        harvestExtraCells(level, this::addHarvestOutput);
    }

    public void harvestExtraCells(Level level, java.util.function.Consumer<ItemStack> output) {
        Soil soil = getOrInvalidateSoil();

        for (int cell = 1; cell < cellCount; cell++) {
            CellBotanyPotContext context = new CellBotanyPotContext(this, cell, null, null);
            Crop crop = context.getCrop();

            if (crop == null || !crop.canHarvest(context, level)) {
                continue;
            }

            int rolls = getLootRolls(context, level, crop, soil);

            for (int roll = 0; roll < rolls; roll++) {
                crop.onHarvest(context, level, output);
            }
        }
    }

    private int getLootRolls(CellBotanyPotContext context, Level level, Crop crop, Soil soil) {
        // Helpers only adds block modifiers for its own BlockEntityContext record.
        float yield = Helpers.getTotalYield(context, level, crop, soil);
        if (getBlockState().getBlock() instanceof ModPotBlock block) {
            yield += crop.getYieldScale(context, level) * block.getYieldModifier(context, level, crop, soil);
        }
        return Helpers.determineRollCount(yield, level.getRandom());
    }

    private void addHarvestOutput(ItemStack harvestedStack) {
        if (harvestedStack.isEmpty()) {
            return;
        }

        ItemStack remaining = harvestedStack.copy();

        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT && !remaining.isEmpty(); slot++) {
            ItemStack existing = getItem(slot);

            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, remaining)) {
                int space = Math.min(existing.getMaxStackSize(), getMaxStackSize()) - existing.getCount();
                int moved = Math.min(space, remaining.getCount());

                if (moved > 0) {
                    existing.grow(moved);
                    remaining.shrink(moved);
                }
            }
        }

        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT && !remaining.isEmpty(); slot++) {
            if (getItem(slot).isEmpty()) {
                int moved = Math.min(remaining.getMaxStackSize(), remaining.getCount());
                setItem(slot, remaining.copyWithCount(moved));
                remaining.shrink(moved);
            }
        }
    }

    private boolean isExtraSeedSlot(int slot) {
        return slot >= VANILLA_SLOT_COUNT && slot < getContainerSize();
    }

    private boolean canSpendSprinklerResources() {
        return !sprinkler || (energyStorage.getEnergyStored() >= ModConfig.energyRequired() && waterTank.getFluidAmount() >= ModConfig.WATER_PER_OPERATION.get());
    }

    private int getSprinklerGrowthTicks() {
        return sprinkler ? getSprinklerGrowthTicks(energyStorage.getEnergyStored(), waterTank.getFluidAmount()) : 1;
    }

    private void spendSprinklerResources() {
        if (!sprinkler || level == null || level.isClientSide) {
            return;
        }

        energyStorage.consumeEnergy(ModConfig.ENERGY_PER_OPERATION.get());
        waterTank.drain(ModConfig.WATER_PER_OPERATION.get(), IFluidHandler.FluidAction.EXECUTE);
    }

    private void saveSprinklerResources(CompoundTag tag, HolderLookup.Provider registries) {
        if (!sprinkler) {
            return;
        }

        tag.put("SprinklerEnergy", energyStorage.serializeNBT(registries));
        tag.put("SprinklerWater", waterTank.writeToNBT(registries, new CompoundTag()));
    }

    private static int cellCountForState(BlockState state) {
        return state.getBlock() instanceof ModPotBlock pot ? pot.cellCount() : 1;
    }

    private static boolean isSprinklerState(BlockState state) {
        return state.getBlock() instanceof ModPotBlock pot && pot.isSprinkler();
    }

    private void onResourceContentsChanged() {
        setChanged();

        if (level != null && !level.isClientSide) {
            markUpdated();
        }
    }

    private class SprinklerEnergyStorage extends EnergyStorage {
        SprinklerEnergyStorage(int capacity, int maxReceive) {
            super(capacity, maxReceive, 0);
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = super.receiveEnergy(toReceive, simulate);

            if (received > 0 && !simulate) {
                onResourceContentsChanged();
            }

            return received;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            int extracted = super.extractEnergy(toExtract, simulate);

            if (extracted > 0 && !simulate) {
                onResourceContentsChanged();
            }

            return extracted;
        }

        void clampStored() {
            energy = Math.clamp(energy, 0, capacity);
        }

        void consumeEnergy(int amount) {
            if (amount <= 0 || energy <= 0) {
                return;
            }

            int consumed = Math.min(amount, energy);
            energy -= consumed;
            onResourceContentsChanged();
        }
    }

    private class SprinklerFluidTank extends FluidTank {
        SprinklerFluidTank(int capacity) {
            super(capacity, stack -> !stack.isEmpty() && stack.getFluid().is(Tags.Fluids.WATER));
        }

        @Override
        protected void onContentsChanged() {
            onResourceContentsChanged();
        }
    }
}
