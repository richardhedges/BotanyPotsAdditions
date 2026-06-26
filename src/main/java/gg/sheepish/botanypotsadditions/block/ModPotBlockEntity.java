package gg.sheepish.botanypotsadditions.block;

import net.darkhax.botanypots.common.impl.Helpers;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ModPotBlockEntity extends BotanyPotBlockEntity {
    public static final int BASE_SEED_SLOT = 1;
    private static final int VANILLA_SLOT_COUNT = 15;
    private static final int FIRST_OUTPUT_SLOT = 3;
    private static final int LAST_OUTPUT_SLOT = 14;

    private final int cellCount;
    private final int containerSize;

    public ModPotBlockEntity(BlockPos pos, BlockState state, int cellCount) {
        super(BotanyPotBlockEntity.TYPE, pos, state);
        this.cellCount = Math.max(1, cellCount);
        this.containerSize = VANILLA_SLOT_COUNT + Math.max(0, this.cellCount - 1);

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
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) slot);
                stack.save(registries, itemTag);
                items.add(itemTag);
            }
        }

        tag.put("Items", items);
        return tag;
    }

    public static void tickCelledPot(Level level, BlockPos pos, BlockState state, ModPotBlockEntity pot) {
        float growthBefore = pot.growthTime.getTicks();
        BotanyPotBlockEntity.tickPot(level, pos, state, pot);

        if (!level.isClientSide && pot.isHopper() && growthBefore > pot.growthTime.getTicks()) {
            pot.harvestExtraCells(level);
            pot.markUpdated();
        } else if (!level.isClientSide && pot.isHopper() && pot.getSeedItem(0).isEmpty()) {
            pot.tickExtraCellsWithoutBaseSeed(level);
        }
    }

    private void tickExtraCellsWithoutBaseSeed(Level level) {
        int requiredGrowthTicks = getRequiredExtraCellGrowthTicks(level);

        if (requiredGrowthTicks <= 0) {
            return;
        }

        growthTime.tickUp(level);

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
}
