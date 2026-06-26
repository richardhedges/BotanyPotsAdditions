package gg.sheepish.botanypotsadditions.client;

import java.util.ArrayList;
import java.util.List;

import gg.sheepish.botanypotsadditions.BotanyPotsAdditions;
import gg.sheepish.botanypotsadditions.menu.SprinklerPotMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SprinklerPotScreen extends AbstractContainerScreen<SprinklerPotMenu> {
    private static final int BAR_WIDTH = 6;
    private static final int BAR_HEIGHT = 52;
    private static final int ENERGY_BAR_X = 154;
    private static final int WATER_BAR_X = 163;
    private static final int BAR_Y = 17;

    private final ResourceLocation backgroundTexture;

    public SprinklerPotScreen(SprinklerPotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.backgroundTexture = backgroundTexture(menu);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(backgroundTexture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderResourceBar(guiGraphics, ENERGY_BAR_X, BAR_Y, menu.energyStored(), menu.maxEnergyStored(), 0xFF2F80ED, 0xFF1D2B3A);
        renderResourceBar(guiGraphics, WATER_BAR_X, BAR_Y, menu.waterStored(), menu.maxWaterStored(), 0xFF2DCEEF, 0xFF173746);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderResourceTooltip(guiGraphics, mouseX, mouseY);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> tooltip = new ArrayList<>(super.getTooltipFromContainerItem(stack));
        int requiredGrowthTicks = menu.getRequiredGrowthTicks(hoveredSlot);

        if (requiredGrowthTicks > 0) {
            tooltip.add(Component.literal("Growth Time: " + formatGrowthTime(requiredGrowthTicks)).withStyle(ChatFormatting.GRAY));
        }

        return tooltip;
    }

    private void renderResourceBar(GuiGraphics guiGraphics, int x, int y, int stored, int capacity, int fillColor, int backgroundColor) {
        int left = leftPos + x;
        int top = topPos + y;
        guiGraphics.fill(left, top, left + BAR_WIDTH, top + BAR_HEIGHT, backgroundColor);

        if (capacity > 0 && stored > 0) {
            int fillHeight = Math.max(1, stored * BAR_HEIGHT / capacity);
            guiGraphics.fill(left + 1, top + BAR_HEIGHT - fillHeight + 1, left + BAR_WIDTH - 1, top + BAR_HEIGHT - 1, fillColor);
        }
    }

    private void renderResourceTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovering(ENERGY_BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.literal(menu.energyStored() + " / " + menu.maxEnergyStored() + " FE").withStyle(ChatFormatting.BLUE), mouseX, mouseY);
        } else if (isHovering(WATER_BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.literal(menu.waterStored() + " / " + menu.maxWaterStored() + " mB Water").withStyle(ChatFormatting.AQUA), mouseX, mouseY);
        }
    }

    private static ResourceLocation backgroundTexture(SprinklerPotMenu menu) {
        if (menu.cellCount() == 1) {
            String form = menu.isHopper() ? "hopper_botany_pot_gui.png" : "botany_pot_gui.png";
            return ResourceLocation.fromNamespaceAndPath("botanypots", "textures/gui/container/" + form);
        }

        String cells = menu.cellCount() == 2 ? "double" : "quadruple";
        String form = menu.isHopper() ? "_hopper_botany_pot_gui.png" : "_botany_pot_gui.png";
        return ResourceLocation.fromNamespaceAndPath(BotanyPotsAdditions.MODID, "textures/gui/container/" + cells + form);
    }

    private static String formatGrowthTime(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
