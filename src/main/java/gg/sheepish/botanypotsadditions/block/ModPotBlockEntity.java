package gg.sheepish.botanypotsadditions.block;

import net.darkhax.botanypots.common.impl.Helpers;
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
    public static final int ENERGY_CAPACITY = 10_000;
    public static final int WATER_CAPACITY = 4_000;
    public static final int ENERGY_PER_GROWTH_TICK = 5;
    public static final int WATER_PER_GROWTH_TICK = 1;
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
        this.energyStorage = new SprinklerEnergyStorage(ENERGY_CAPACITY, 1_000);
        this.waterTank = new SprinklerFluidTank(WATER_CAPACITY);

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
        }

        if (tag.contains("SprinklerWater", Tag.TAG_COMPOUND)) {
            waterTank.readFromNBT(registries, tag.getCompound("SprinklerWater"));
        }
    }

    public static void tickModPot(Level level, BlockPos pos, BlockState state, ModPotBlockEntity pot) {
        float growthBefore = pot.growthTime.getTicks();
        boolean canGrow = pot.canSpendSprinklerResources();

        if (!pot.sprinkler || canGrow) {
            BotanyPotBlockEntity.tickPot(level, pos, state, pot);
        }

        if (!level.isClientSide && pot.sprinkler && growthBefore < pot.growthTime.getTicks()) {
            pot.spendSprinklerResources();
        }

        if (!level.isClientSide && pot.isHopper() && growthBefore > pot.growthTime.getTicks()) {
            pot.harvestExtraCells(level);
            pot.markUpdated();
        } else if (!level.isClientSide && pot.isHopper() && pot.getSeedItem(0).isEmpty()) {
            pot.tickExtraCellsWithoutBaseSeed(level);
        }
    }

    private void tickExtraCellsWithoutBaseSeed(Level level) {
        int requiredGrowthTicks = getRequiredExtraCellGrowthTicks(level);

        if (requiredGrowthTicks <= 0 || !canSpendSprinklerResources()) {
            return;
        }

        growthTime.tickUp(level);
        spendSprinklerResources();

        if (growthTime.getTicks() >= requiredGrowthTicks) {
            harvestExtraCells(level);
            growthTime.reset();
            markUpdated();
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

    private void harvestExtraCells(Level level) {
        Soil soil = getOrInvalidateSoil();

        for (int cell = 1; cell < cellCount; cell++) {
            CellBotanyPotContext context = new CellBotanyPotContext(this, cell, null, null);
            Crop crop = context.getCrop();

            if (crop == null || !crop.canHarvest(context, level)) {
                continue;
            }

            int rolls = Helpers.getLootRolls(context, level, crop, soil);

            for (int roll = 0; roll < rolls; roll++) {
                crop.onHarvest(context, level, this::addHarvestOutput);
            }
        }
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
        return !sprinkler || (energyStorage.getEnergyStored() >= ENERGY_PER_GROWTH_TICK && waterTank.getFluidAmount() >= WATER_PER_GROWTH_TICK);
    }

    private void spendSprinklerResources() {
        if (!sprinkler || level == null || level.isClientSide) {
            return;
        }

        energyStorage.consumeEnergy(ENERGY_PER_GROWTH_TICK);
        waterTank.drain(WATER_PER_GROWTH_TICK, IFluidHandler.FluidAction.EXECUTE);
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
