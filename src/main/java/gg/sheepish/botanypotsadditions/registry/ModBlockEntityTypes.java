package gg.sheepish.botanypotsadditions.registry;

import java.util.function.Supplier;

import gg.sheepish.botanypotsadditions.BotanyPotsAdditions;
import gg.sheepish.botanypotsadditions.block.ModPotBlock;
import gg.sheepish.botanypotsadditions.block.ModPotBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntityTypes {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BotanyPotsAdditions.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModPotBlockEntity>> CELLED_POT = BLOCK_ENTITY_TYPES.register(
            "celled_pot",
            () -> BlockEntityType.Builder.of(ModPotBlockEntity::new, customPotBlocks()).build(null));
    public static final Supplier<BlockEntityType<BotanyPotBlockEntity>> CELLED_POT_AS_BOTANY = ModBlockEntityTypes::celledPotAsBotany;

    private ModBlockEntityTypes() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(ModBlockEntityTypes::addBlocksToBotanyPotBlockEntity);
        modEventBus.addListener(ModBlockEntityTypes::registerCapabilities);
    }

    private static net.minecraft.world.level.block.Block[] customPotBlocks() {
        return ModBlocks.potBlocks().stream()
                .map(entry -> entry.block().get())
                .filter(block -> block instanceof ModPotBlock pot && (pot.isCelled() || pot.isSprinkler()))
                .toArray(net.minecraft.world.level.block.Block[]::new);
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityType<BotanyPotBlockEntity> celledPotAsBotany() {
        return (BlockEntityType<BotanyPotBlockEntity>) (BlockEntityType<?>) CELLED_POT.get();
    }

    private static void addBlocksToBotanyPotBlockEntity(BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                BotanyPotBlockEntity.TYPE.get(),
                ModBlocks.potBlocks().stream()
                        .map(entry -> entry.block().get())
                        .toArray(net.minecraft.world.level.block.Block[]::new));
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                CELLED_POT.get(),
                (pot, side) -> pot.isSprinkler() ? pot.energyStorage(side) : null);
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CELLED_POT.get(),
                (pot, side) -> pot.isSprinkler() ? pot.waterTank(side) : null);
    }
}
