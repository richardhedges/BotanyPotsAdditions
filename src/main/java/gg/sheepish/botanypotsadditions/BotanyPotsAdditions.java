package gg.sheepish.botanypotsadditions;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import gg.sheepish.botanypotsadditions.registry.ModBlockEntityTypes;
import gg.sheepish.botanypotsadditions.registry.ModBlocks;
import gg.sheepish.botanypotsadditions.registry.ModCreativeTabs;
import gg.sheepish.botanypotsadditions.registry.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(BotanyPotsAdditions.MODID)
public class BotanyPotsAdditions {
    public static final String MODID = "botanypotsadditions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BotanyPotsAdditions(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModBlockEntityTypes.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        LOGGER.info("BotanyPotsAdditions initialized");
    }
}
