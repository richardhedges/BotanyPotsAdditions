package gg.sheepish.botanypotsadditions.menu;

import java.util.function.IntSupplier;
import java.util.function.Predicate;

import gg.sheepish.botanypotsadditions.block.CellBotanyPotContext;
import gg.sheepish.botanypotsadditions.block.ModPotBlockEntity;
import gg.sheepish.botanypotsadditions.registry.ModMenuTypes;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.darkhax.botanypots.common.impl.block.menu.BotanyPotMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SprinklerPotMenu extends AbstractContainerMenu {
    public static final int GROWTH_PAUSED = -2;

    private static final int TOOL_SLOT = 2;
    private static final int STORAGE_START = 3;
    private static final int STORAGE_END_EXCLUSIVE = 15;
    private static final int SINGLE_HOPPER_OUTPUT_X = 80;
    private static final int SINGLE_HOPPER_INPUT_X = 44;
    private static final int SINGLE_HOPPER_TOOL_X = 18;
    private static final int SINGLE_HOPPER_TOOL_Y = 35;
    private static final int HOPPER_OUTPUT_X = 86;
    private static final int BASIC_INPUT_X = 80;
    private static final int HOPPER_INPUT_X = 35;
    private static final int HOPPER_TOOL_X = 9;
    private static final int HOPPER_TOOL_Y = 48;

    private final Level level;
    private final Inventory playerInventory;
    private final Container potContainer;
    private final BlockPos pos;
    private final int cellCount;
    private final boolean hopper;
    private int energyStored;
    private int maxEnergyStored;
    private int waterStored;
    private int maxWaterStored;

    public static SprinklerPotMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int cellCount = buffer.readVarInt();
        boolean hopper = buffer.readBoolean();
        int size = STORAGE_END_EXCLUSIVE + Math.max(0, cellCount - 1);

        return new SprinklerPotMenu(containerId, playerInventory, new SimpleContainer(size), cellCount, hopper, pos);
    }

    public SprinklerPotMenu(int containerId, Inventory playerInventory, Container potContainer, int cellCount, boolean hopper, BlockPos pos) {
        super(ModMenuTypes.SPRINKLER_POT.get(), containerId);
        this.level = playerInventory.player.level();
        this.playerInventory = playerInventory;
        this.potContainer = potContainer;
        this.pos = pos;
        this.cellCount = Math.max(1, cellCount);
        this.hopper = hopper;

        addPotSlots();
        addPlayerInventorySlots();
        addResourceDataSlots();
    }

    private void addPotSlots() {
        addSlot(new SingleItemSlot(potContainer, 0, inputX(), 48, this::isSoil));

        for (int cell = 0; cell < cellCount; cell++) {
            final int seedCell = cell;
            addSlot(new SeedSlot(potContainer, seedSlotForCell(seedCell), seedX(seedCell), seedY(seedCell), seedCell, stack -> isSeed(seedCell, stack)));
        }

        if (hopper) {
            addSlot(new ValidatingSlot(potContainer, TOOL_SLOT, toolX(), toolY(), stack -> stack.is(BotanyPotMenu.HARVEST_ITEM)));

            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 4; column++) {
                    int slot = STORAGE_START + column + row * 4;
                    addSlot(new OutputOnlySlot(potContainer, slot, outputX() + column * 18, 17 + row * 18));
                }
            }
        }
    }

    private void addPlayerInventorySlots() {
        int inventoryTop = 84;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, inventoryTop + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, inventoryTop + 58));
        }
    }

    private void addResourceDataSlots() {
        addDataSlot(resourceDataSlot(() -> sprinklerPot() != null ? sprinklerPot().getEnergyStored() : 0, value -> energyStored = value));
        addDataSlot(resourceDataSlot(() -> sprinklerPot() != null ? sprinklerPot().getMaxEnergyStored() : ModPotBlockEntity.ENERGY_CAPACITY, value -> maxEnergyStored = value));
        addDataSlot(resourceDataSlot(() -> sprinklerPot() != null ? sprinklerPot().getWaterStored() : 0, value -> waterStored = value));
        addDataSlot(resourceDataSlot(() -> sprinklerPot() != null ? sprinklerPot().getMaxWaterStored() : ModPotBlockEntity.WATER_CAPACITY, value -> maxWaterStored = value));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int potSlotCount = hopper ? cellCount + 14 : cellCount + 1;

        if (index < potSlotCount) {
            if (!moveItemStackTo(stack, potSlotCount, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (isSoil(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(BotanyPotMenu.HARVEST_ITEM) && hopper) {
            if (!moveItemStackTo(stack, cellCount + 1, cellCount + 2, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            int seedStart = 1;
            int seedEnd = 1 + cellCount;
            if (!moveItemStackTo(stack, seedStart, seedEnd, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return potContainer.stillValid(player);
    }

    public int cellCount() {
        return cellCount;
    }

    public boolean isHopper() {
        return hopper;
    }

    public int energyStored() {
        return energyStored;
    }

    public int maxEnergyStored() {
        return maxEnergyStored;
    }

    public int waterStored() {
        return waterStored;
    }

    public int maxWaterStored() {
        return maxWaterStored;
    }

    public int seedX(int cell) {
        if (hopper) {
            if (cellCount == 1) {
                return SINGLE_HOPPER_INPUT_X;
            }

            return cellCount == 2
                    ? new int[] {9, 27}[cell]
                    : new int[] {9, 27, 45, 63}[cell];
        }

        if (cellCount == 1) {
            return 80;
        }

        return cellCount == 2
                ? new int[] {71, 89}[cell]
                : new int[] {53, 71, 89, 107}[cell];
    }

    public int seedY(int cell) {
        return 22;
    }

    public int soilX() {
        return inputX();
    }

    public int soilY() {
        return 48;
    }

    public int toolX() {
        return cellCount == 1 ? SINGLE_HOPPER_TOOL_X : HOPPER_TOOL_X;
    }

    public int toolY() {
        return cellCount == 1 ? SINGLE_HOPPER_TOOL_Y : HOPPER_TOOL_Y;
    }

    public int getRequiredGrowthTicks(Slot slot) {
        int cell = seedCellForSlot(slot);
        CellBotanyPotContext context = cell >= 0 ? contextForCell(cell, slot.getItem()) : null;
        int requiredGrowthTicks = context != null ? context.getRequiredGrowthTicks() : -1;

        if (requiredGrowthTicks <= 0) {
            return -1;
        }

        int sprinklerGrowthTicks = ModPotBlockEntity.getSprinklerGrowthTicks(energyStored, waterStored);
        return sprinklerGrowthTicks > 0 ? Math.max(1, (requiredGrowthTicks + sprinklerGrowthTicks - 1) / sprinklerGrowthTicks) : GROWTH_PAUSED;
    }

    private int inputX() {
        if (hopper && cellCount == 1) {
            return SINGLE_HOPPER_INPUT_X;
        }

        return hopper ? HOPPER_INPUT_X : BASIC_INPUT_X;
    }

    private int outputX() {
        return cellCount == 1 ? SINGLE_HOPPER_OUTPUT_X : HOPPER_OUTPUT_X;
    }

    private int seedSlotForCell(int cell) {
        return cell == 0 ? ModPotBlockEntity.BASE_SEED_SLOT : STORAGE_END_EXCLUSIVE + cell - 1;
    }

    private int seedCellForSlot(Slot slot) {
        return slot instanceof SeedSlot seedSlot ? seedSlot.cell() : -1;
    }

    private boolean isSoil(ItemStack stack) {
        CellBotanyPotContext context = contextForCell(0);
        var cache = Soil.CACHE.apply(level);
        return context == null || (cache != null && cache.lookup(stack, context, level) != null);
    }

    private boolean isSeed(int cell, ItemStack stack) {
        CellBotanyPotContext context = contextForCell(cell);
        var cache = Crop.CACHE.apply(level);
        return context == null || (cache != null && cache.lookup(stack, context, level) != null);
    }

    private CellBotanyPotContext contextForCell(int cell) {
        return contextForCell(cell, ItemStack.EMPTY);
    }

    private CellBotanyPotContext contextForCell(int cell, ItemStack seedOverride) {
        ModPotBlockEntity pot = sprinklerPot();
        return pot != null ? new CellBotanyPotContext(pot, cell, playerInventory.player, null, seedOverride) : null;
    }

    private ModPotBlockEntity sprinklerPot() {
        return level.getBlockEntity(pos) instanceof ModPotBlockEntity pot ? pot : null;
    }

    private static DataSlot resourceDataSlot(IntSupplier getter, java.util.function.IntConsumer setter) {
        return new DataSlot() {
            @Override
            public int get() {
                return getter.getAsInt();
            }

            @Override
            public void set(int value) {
                setter.accept(value);
            }
        };
    }

    private static class ValidatingSlot extends Slot {
        private final Predicate<ItemStack> validator;

        ValidatingSlot(Container container, int slot, int x, int y, Predicate<ItemStack> validator) {
            super(container, slot, x, y);
            this.validator = validator;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return validator.test(stack);
        }
    }

    private static class OutputOnlySlot extends Slot {
        OutputOnlySlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private static class SingleItemSlot extends ValidatingSlot {
        SingleItemSlot(Container container, int slot, int x, int y, Predicate<ItemStack> validator) {
            super(container, slot, x, y, validator);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }

    private static class SeedSlot extends SingleItemSlot {
        private final int cell;

        SeedSlot(Container container, int slot, int x, int y, int cell, Predicate<ItemStack> validator) {
            super(container, slot, x, y, validator);
            this.cell = cell;
        }

        int cell() {
            return cell;
        }
    }
}
