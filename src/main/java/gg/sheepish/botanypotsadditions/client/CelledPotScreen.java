package gg.sheepish.botanypotsadditions.client;

import gg.sheepish.botanypotsadditions.menu.CelledPotMenu;
import gg.sheepish.botanypotsadditions.BotanyPotsAdditions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CelledPotScreen extends AbstractContainerScreen<CelledPotMenu> {
    private final ResourceLocation backgroundTexture;

    public CelledPotScreen(CelledPotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.backgroundTexture = ResourceLocation.fromNamespaceAndPath(BotanyPotsAdditions.MODID, "textures/gui/container/" + textureName(menu));
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(backgroundTexture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
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

    private static String textureName(CelledPotMenu menu) {
        String cells = menu.cellCount() == 2 ? "double" : "quadruple";
        String form = menu.isHopper() ? "_hopper_botany_pot_gui.png" : "_botany_pot_gui.png";
        return cells + form;
    }
}
