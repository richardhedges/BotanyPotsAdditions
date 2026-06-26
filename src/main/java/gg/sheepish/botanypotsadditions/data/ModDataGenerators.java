package gg.sheepish.botanypotsadditions.data;

import gg.sheepish.botanypotsadditions.BotanyPotsAdditions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = BotanyPotsAdditions.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModDataGenerators {
    private ModDataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeClient()) {
            event.addProvider(new ModModelProvider(event.getGenerator().getPackOutput()));
            event.addProvider(new ModLanguageProvider(event.getGenerator().getPackOutput()));
        }
    }
}
