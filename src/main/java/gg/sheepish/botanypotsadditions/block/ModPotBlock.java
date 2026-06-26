package gg.sheepish.botanypotsadditions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ModPotBlock extends Block {
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

    private static final VoxelShape X_DIVIDER_SHAPE = box(7.5D, 1.0D, 3.125D, 8.5D, 7.9375D, 12.875D);
    private static final VoxelShape Z_DIVIDER_SHAPE = box(3.125D, 1.0D, 7.5D, 12.875D, 7.9375D, 8.5D);

    private final VoxelShape shape;

    public ModPotBlock(BlockBehaviour.Properties properties, boolean greenhouse, boolean hopper, boolean doubled, boolean quadrupled) {
        super(properties);

        VoxelShape baseShape = hopper ? HOPPER_POT_SHAPE : POT_SHAPE;

        if (doubled || quadrupled) {
            baseShape = Shapes.or(baseShape, X_DIVIDER_SHAPE);
        }

        if (quadrupled) {
            baseShape = Shapes.or(baseShape, Z_DIVIDER_SHAPE);
        }

        this.shape = greenhouse ? Shapes.or(baseShape, GLASS_LID_SHAPE) : baseShape;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }
}
