package gg.sheepish.botanypotsadditions.block;

import gg.sheepish.botanypotsadditions.menu.CelledPotMenu;
import gg.sheepish.botanypotsadditions.registry.ModBlockEntityTypes;
import net.darkhax.botanypots.common.api.context.BotanyPotContext;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.darkhax.botanypots.common.impl.block.BotanyPotBlock;
import net.darkhax.botanypots.common.impl.block.PotType;
import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ModPotBlock extends BotanyPotBlock {
    private static final float GREENHOUSE_GROW_TIME_MULTIPLIER = 0.75F;

    private static final VoxelShape POT_SHAPE = Shapes.or(
            box(2.0D, 0.0D, 2.0D, 14.0D, 1.0D, 14.0D),
            box(2.0D, 1.0D, 2.0D, 3.0D, 8.0D, 14.0D),
            box(13.0D, 1.0D, 2.0D, 14.0D, 8.0D, 14.0D),
            box(3.0D, 1.0D, 2.0D, 13.0D, 8.0D, 3.0D),
            box(3.0D, 1.0D, 13.0D, 13.0D, 8.0D, 14.0D));

    private static final VoxelShape HOPPER_POT_SHAPE = Shapes.or(
            box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D),
            POT_SHAPE);

    private static final VoxelShape GLASS_LID_SHAPE = Shapes.or(
            box(2.0D, 15.0D, 2.0D, 14.0D, 16.0D, 14.0D),
            box(2.0D, 8.0D, 2.0D, 3.0D, 15.0D, 14.0D),
            box(13.0D, 8.0D, 2.0D, 14.0D, 15.0D, 14.0D),
            box(3.0D, 8.0D, 2.0D, 13.0D, 15.0D, 3.0D),
            box(3.0D, 8.0D, 13.0D, 13.0D, 15.0D, 14.0D));

    private static final VoxelShape CELLED_GLASS_LID_SHAPE = Shapes.or(
            box(2.0D, 15.0D, 2.0D, 14.0D, 16.0D, 14.0D),
            box(2.0D, 8.125D, 2.0D, 3.0D, 15.0D, 14.0D),
            box(13.0D, 8.125D, 2.0D, 14.0D, 15.0D, 14.0D),
            box(3.0D, 8.125D, 2.0D, 13.0D, 15.0D, 3.0D),
            box(3.0D, 8.125D, 13.0D, 13.0D, 15.0D, 14.0D));

    private static final VoxelShape X_DIVIDER_SHAPE = box(7.5D, 1.0D, 3.0D, 8.5D, 8.0625D, 13.0D);
    private static final VoxelShape Z_DIVIDER_SHAPE = box(3.0D, 1.0D, 7.5D, 13.0D, 8.0625D, 8.5D);

    private final VoxelShape shape;
    private final VoxelShape rotatedShape;
    private final boolean greenhouse;
    private final int cellCount;

    public ModPotBlock(BlockBehaviour.Properties properties, PotType type, boolean glassLid, boolean greenhouse, boolean doubled, boolean quadrupled) {
        super(properties, type);
        this.greenhouse = greenhouse;
        this.cellCount = quadrupled ? 4 : doubled ? 2 : 1;

        VoxelShape baseShape = type == PotType.HOPPER ? HOPPER_POT_SHAPE : POT_SHAPE;
        VoxelShape unrotatedShape = baseShape;
        VoxelShape rotatedShape = baseShape;

        if (doubled || quadrupled) {
            unrotatedShape = Shapes.or(unrotatedShape, X_DIVIDER_SHAPE);
            rotatedShape = Shapes.or(rotatedShape, quadrupled ? X_DIVIDER_SHAPE : Z_DIVIDER_SHAPE);
        }

        if (quadrupled) {
            unrotatedShape = Shapes.or(unrotatedShape, Z_DIVIDER_SHAPE);
            rotatedShape = Shapes.or(rotatedShape, Z_DIVIDER_SHAPE);
        }

        VoxelShape glassShape = isCelled() ? CELLED_GLASS_LID_SHAPE : GLASS_LID_SHAPE;
        this.shape = glassLid ? Shapes.or(unrotatedShape, glassShape) : unrotatedShape;
        this.rotatedShape = glassLid ? Shapes.or(rotatedShape, glassShape) : rotatedShape;
    }

    @Override
    public float getGrowthModifier(BotanyPotContext context, Level level, Crop crop, Soil soil) {
        return greenhouse
                ? growthSpeedModifierForTimeMultiplier(GREENHOUSE_GROW_TIME_MULTIPLIER)
                : super.getGrowthModifier(context, level, crop, soil);
    }

    private static float growthSpeedModifierForTimeMultiplier(float timeMultiplier) {
        return (1F / timeMultiplier) - 1F;
    }

    public int cellCount() {
        return cellCount;
    }

    public boolean isCelled() {
        return cellCount > 1;
    }

    @Override
    public void openMenu(BlockState state, Level level, BlockPos pos, Player player) {
        if (!isCelled()) {
            super.openMenu(state, level, pos, player);
            return;
        }

        MenuProvider menuProvider = getCelledMenuProvider(level, pos);
        if (menuProvider != null && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(menuProvider, buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeVarInt(cellCount);
                buffer.writeBoolean(type == PotType.HOPPER);
            });
        } else {
            super.openMenu(state, level, pos, player);
        }
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (!isCelled()) {
            return super.getMenuProvider(state, level, pos);
        }

        MenuProvider menuProvider = getCelledMenuProvider(level, pos);
        return menuProvider != null ? menuProvider : super.getMenuProvider(state, level, pos);
    }

    private MenuProvider getCelledMenuProvider(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ModPotBlockEntity pot = blockEntity instanceof ModPotBlockEntity celledPot
                ? celledPot
                : migrateCelledPot(level, pos, level.getBlockState(pos), blockEntity);

        if (pot == null) {
            return null;
        }

        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return pot.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player menuPlayer) {
                return new CelledPotMenu(containerId, playerInventory, pot, cellCount, type == PotType.HOPPER, pos);
            }
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isCelled() ? new ModPotBlockEntity(pos, state, cellCount) : super.newBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!isCelled()) {
            return super.getTicker(level, state, blockEntityType);
        }

        if (blockEntityType != ModBlockEntityTypes.CELLED_POT.get() && blockEntityType != BotanyPotBlockEntity.TYPE.get()) {
            return null;
        }

        return (tickerLevel, pos, tickerState, blockEntity) -> {
            if (blockEntity instanceof ModPotBlockEntity pot) {
                ModPotBlockEntity.tickCelledPot(tickerLevel, pos, tickerState, pot);
            } else if (blockEntity instanceof BotanyPotBlockEntity pot) {
                ModPotBlockEntity migratedPot = migrateCelledPot(tickerLevel, pos, tickerState, pot);
                if (migratedPot != null) {
                    ModPotBlockEntity.tickCelledPot(tickerLevel, pos, tickerState, migratedPot);
                }
            }
        };
    }

    private ModPotBlockEntity migrateCelledPot(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!isCelled() || !(blockEntity instanceof BotanyPotBlockEntity oldPot)) {
            return null;
        }

        ModPotBlockEntity newPot = new ModPotBlockEntity(pos, state, cellCount);
        int slotsToCopy = Math.min(oldPot.getContainerSize(), newPot.getContainerSize());

        for (int slot = 0; slot < slotsToCopy; slot++) {
            newPot.setItem(slot, oldPot.getItem(slot).copy());
        }

        level.setBlockEntity(newPot);
        newPot.setChanged();
        return newPot;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    private VoxelShape shapeForState(BlockState state) {
        if (cellCount != 2 || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return shape;
        }

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return facing == Direction.EAST || facing == Direction.WEST ? rotatedShape : shape;
    }
}
