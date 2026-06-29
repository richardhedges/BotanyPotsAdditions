package gg.sheepish.botanypotsadditions.client;

import java.util.ArrayList;
import java.util.List;

import gg.sheepish.botanypotsadditions.menu.CelledPotMenu;
import gg.sheepish.botanypotsadditions.BotanyPotsAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class CelledPotScreen extends AbstractContainerScreen<CelledPotMenu> {
    private static final ResourceLocation EMPTY_SLOT_SEED = ResourceLocation.fromNamespaceAndPath("botanypots", "textures/item/empty_slot_seed.png");
    private static final ResourceLocation EMPTY_SLOT_SOIL = ResourceLocation.fromNamespaceAndPath("botanypots", "textures/item/empty_slot_soil.png");
    private static final ResourceLocation EMPTY_SLOT_HOE = ResourceLocation.withDefaultNamespace("textures/item/empty_slot_hoe.png");

    private final ResourceLocation backgroundTexture;

    public CelledPotScreen(CelledPotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.backgroundTexture = ResourceLocation.fromNamespaceAndPath(BotanyPotsAdditions.MODID, "textures/gui/container/" + textureName(menu));
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(backgroundTexture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderSlotPlaceholders(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
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

    private static String textureName(CelledPotMenu menu) {
        String cells = menu.cellCount() == 2 ? "double" : "quadruple";
        String form = menu.isHopper() ? "_hopper_botany_pot_gui.png" : "_botany_pot_gui.png";
        return cells + form;
    }

    private void renderSlotPlaceholders(GuiGraphics guiGraphics) {
        for (int cell = 0; cell < menu.cellCount(); cell++) {
            if (menu.isSeedSlotEmpty(cell)) {
                guiGraphics.blit(EMPTY_SLOT_SEED, leftPos + menu.seedX(cell), topPos + menu.seedY(cell), 0, 0, 16, 16, 16, 16);
            }
        }

        if (menu.isSoilSlotEmpty()) {
            guiGraphics.blit(EMPTY_SLOT_SOIL, leftPos + menu.soilX(), topPos + menu.soilY(), 0, 0, 16, 16, 16, 16);
        }

        if (menu.isHopper() && menu.isToolSlotEmpty()) {
            guiGraphics.blit(EMPTY_SLOT_HOE, leftPos + menu.toolX(), topPos + menu.toolY(), 0, 0, 16, 16, 16, 16);
        }
    }

    private static String formatGrowthTime(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
