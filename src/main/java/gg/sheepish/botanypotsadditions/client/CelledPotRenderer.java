package gg.sheepish.botanypotsadditions.client;

import com.mojang.blaze3d.vertex.PoseStack;

import gg.sheepish.botanypotsadditions.block.CellBotanyPotContext;
import gg.sheepish.botanypotsadditions.block.ModPotBlock;
import gg.sheepish.botanypotsadditions.block.ModPotBlockEntity;
import net.darkhax.botanypots.common.api.data.display.render.DisplayRenderer;
import net.darkhax.botanypots.common.api.data.display.math.AxisAlignedRotation;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.darkhax.botanypots.common.impl.block.BotanyPotRenderer;
import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class CelledPotRenderer implements BlockEntityRenderer<BotanyPotBlockEntity> {
    private static final float SOIL_HEIGHT_SCALE = 0.6375F;
    private static final float DOUBLE_SOIL_WIDTH = 8F / 16F;
    private static final float DOUBLE_SOIL_DEPTH = 16F / 16F;
    private static final float DOUBLE_CROP_SCALE = 0.6F;
    private static final float QUADRUPLE_SOIL_SIZE = 8F / 16F;
    private static final float QUADRUPLE_CROP_SCALE = 0.6F;
    private static final float CELL_LOW_CENTER = 5.25F / 16F;
    private static final float CELL_HIGH_CENTER = 10.75F / 16F;

    private final BlockEntityRendererProvider.Context renderContext;
    private final BotanyPotRenderer fallback;

    public CelledPotRenderer(BlockEntityRendererProvider.Context renderContext) {
        this.renderContext = renderContext;
        this.fallback = new BotanyPotRenderer(renderContext);
    }

    @Override
    public void render(BotanyPotBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!(blockEntity instanceof ModPotBlockEntity pot)
                || !(pot.getBlockState().getBlock() instanceof ModPotBlock block)
                || !block.isCelled()) {
            fallback.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }

        Level level = pot.getLevel();
        if (level == null) {
            return;
        }

        for (int cell = 0; cell < pot.cellCount(); cell++) {
            renderCell(pot, cell, partialTick, poseStack, bufferSource, packedLight, packedOverlay, level);
        }
    }

    @Override
    public int getViewDistance() {
        return fallback.getViewDistance();
    }

    private void renderCell(ModPotBlockEntity pot, int cell, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Level level) {
        CellBotanyPotContext context = new CellBotanyPotContext(pot, cell, null, null);
        Crop crop = context.getCrop();
        Soil soil = context.getSoil();
        BlockPos pos = pot.getBlockPos();
        int requiredGrowthTicks = context.getRequiredGrowthTicks();
        float growth = requiredGrowthTicks > 0
                ? Mth.clamp(pot.growthTime.getTicks(), 0F, (float) requiredGrowthTicks) / requiredGrowthTicks
                : 0F;

        float[] offset = cellOffset(pot, cell);

        if (soil != null) {
            var display = soil.getDisplay(context, level);
            poseStack.pushPose();
            transformToCell(poseStack, pot, offset, soilWidth(pot), SOIL_HEIGHT_SCALE, soilDepth(pot));
            if (display != null) {
                DisplayRenderer.renderState(renderContext, display, poseStack, level, pos, partialTick, bufferSource, packedLight, packedOverlay, pot, growth, 1F, 0F);
            }
            poseStack.popPose();
        }

        if (crop != null && crop.isGrowthSustained(context, level)) {
            float scale = 0.4F + Mth.clamp(0.6F * growth, 0F, 0.6F);
            float yOffset = 0.3984375F;

            for (var display : crop.getDisplayState(context, level)) {
                poseStack.pushPose();
                transformCropToCell(poseStack, pot, offset);
                yOffset = DisplayRenderer.renderState(renderContext, display, poseStack, level, pos, partialTick, bufferSource, packedLight, packedOverlay, pot, growth, scale * cropScale(pot), yOffset);
                poseStack.popPose();
            }
        }
    }

    private static void transformToCell(PoseStack poseStack, ModPotBlockEntity pot, float[] cellCenter, float xScale, float yScale, float zScale) {
        BotanyPotRenderer.applyRotation(rotationForFacing(pot), poseStack);
        poseStack.translate(cellCenter[0], 0F, cellCenter[1]);
        poseStack.scale(xScale, yScale, zScale);
        poseStack.translate(-0.5F, 0F, -0.5F);
    }

    private static void transformCropToCell(PoseStack poseStack, ModPotBlockEntity pot, float[] cellCenter) {
        BotanyPotRenderer.applyRotation(rotationForFacing(pot), poseStack);
        poseStack.translate(cellCenter[0] - 0.5F, 0F, cellCenter[1] - 0.5F);
    }

    private static AxisAlignedRotation rotationForFacing(ModPotBlockEntity pot) {
        Direction facing = pot.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        return switch (facing) {
            case EAST -> AxisAlignedRotation.Y_90;
            case SOUTH -> AxisAlignedRotation.Y_180;
            case WEST -> AxisAlignedRotation.Y_270;
            default -> AxisAlignedRotation.Y_0;
        };
    }

    private static float soilWidth(ModPotBlockEntity pot) {
        return pot.cellCount() == 2 ? DOUBLE_SOIL_WIDTH : QUADRUPLE_SOIL_SIZE;
    }

    private static float soilDepth(ModPotBlockEntity pot) {
        return pot.cellCount() == 2 ? DOUBLE_SOIL_DEPTH : QUADRUPLE_SOIL_SIZE;
    }

    private static float cropScale(ModPotBlockEntity pot) {
        return pot.cellCount() == 2 ? DOUBLE_CROP_SCALE : QUADRUPLE_CROP_SCALE;
    }

    private static float[] cellOffset(ModPotBlockEntity pot, int cell) {
        return pot.cellCount() == 2
                ? new float[][] {{CELL_LOW_CENTER, 0.5F}, {CELL_HIGH_CENTER, 0.5F}}[cell]
                : new float[][] {{CELL_LOW_CENTER, CELL_LOW_CENTER}, {CELL_HIGH_CENTER, CELL_LOW_CENTER}, {CELL_LOW_CENTER, CELL_HIGH_CENTER}, {CELL_HIGH_CENTER, CELL_HIGH_CENTER}}[cell];
    }
}
