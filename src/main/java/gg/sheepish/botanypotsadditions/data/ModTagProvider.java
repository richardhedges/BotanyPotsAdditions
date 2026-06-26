package gg.sheepish.botanypotsadditions.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gg.sheepish.botanypotsadditions.BotanyPotsAdditions;
import gg.sheepish.botanypotsadditions.registry.ModBlocks;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class ModTagProvider implements DataProvider {
    private final PackOutput output;

    public ModTagProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(DataProvider.saveStable(cache, botanyPotsTag(), dataPath("botanypots/tags/block/botany_pots.json")));
        futures.add(DataProvider.saveStable(cache, botanyPotsTag(), dataPath("botanypots/tags/item/botany_pots.json")));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "BotanyPotsAdditions tags";
    }

    private Path dataPath(String path) {
        return output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(path);
    }

    private static JsonObject botanyPotsTag() {
        JsonObject tag = new JsonObject();
        JsonArray values = new JsonArray();

        tag.addProperty("replace", false);
        ModBlocks.potBlocks().forEach(entry -> values.add(BotanyPotsAdditions.MODID + ":" + entry.id()));
        tag.add("values", values);

        return tag;
    }
}
