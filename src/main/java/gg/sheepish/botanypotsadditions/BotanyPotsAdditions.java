package gg.sheepish.botanypotsadditions;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import gg.sheepish.botanypotsadditions.registry.ModBlockEntityTypes;
import gg.sheepish.botanypotsadditions.registry.ModBlocks;
import gg.sheepish.botanypotsadditions.registry.ModCreativeTabs;
import gg.sheepish.botanypotsadditions.registry.ModMenuTypes;
import gg.sheepish.botanypotsadditions.registry.ModParticleTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(BotanyPotsAdditions.MODID)
public class BotanyPotsAdditions {
    public static final String MODID = "botanypotsadditions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BotanyPotsAdditions(IEventBus modEventBus, net.neoforged.fml.ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,
                gg.sheepish.botanypotsadditions.config.ModConfig.SPEC);
        ModBlocks.register(modEventBus);
        ModBlockEntityTypes.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModParticleTypes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        LOGGER.info("BotanyPotsAdditions initialized");
    }
}
