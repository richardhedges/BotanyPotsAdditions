package gg.sheepish.botanypotsadditions.registry;

import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

public final class ModBlockEntityTypes {
    private ModBlockEntityTypes() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModBlockEntityTypes::addBlocksToBotanyPotBlockEntity);
    }

    private static void addBlocksToBotanyPotBlockEntity(BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                BotanyPotBlockEntity.TYPE.get(),
                ModBlocks.potBlocks().stream()
                        .map(entry -> entry.block().get())
                        .toArray(net.minecraft.world.level.block.Block[]::new));
    }
}
