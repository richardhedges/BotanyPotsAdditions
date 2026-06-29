package gg.sheepish.botanypotsadditions.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gg.sheepish.botanypotsadditions.BotanyPotsAdditions;
import gg.sheepish.botanypotsadditions.registry.ModBlocks;
import gg.sheepish.botanypotsadditions.registry.ModBlocks.PotBlockEntry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class ModLootProvider implements DataProvider {
    private final PackOutput output;

    public ModLootProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (PotBlockEntry entry : ModBlocks.potBlocks()) {
            futures.add(DataProvider.saveStable(cache, selfDrop(entry), dataPath("loot_table/blocks/" + entry.id() + ".json")));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "BotanyPotsAdditions block loot";
    }

    private Path dataPath(String path) {
        return output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(BotanyPotsAdditions.MODID)
                .resolve(path);
    }

    private static JsonObject selfDrop(PotBlockEntry entry) {
        JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", pools(entry));
        table.addProperty("random_sequence", BotanyPotsAdditions.MODID + ":" + entry.id());
        return table;
    }

    private static JsonArray pools(PotBlockEntry entry) {
        JsonArray pools = new JsonArray();
        JsonObject pool = new JsonObject();

        JsonArray conditions = new JsonArray();
        JsonObject survivesExplosion = new JsonObject();
        survivesExplosion.addProperty("condition", "minecraft:survives_explosion");
        conditions.add(survivesExplosion);
        pool.add("conditions", conditions);

        JsonArray entries = new JsonArray();
        JsonObject item = new JsonObject();
        item.addProperty("type", "minecraft:item");
        item.addProperty("name", BotanyPotsAdditions.MODID + ":" + entry.id());
        entries.add(item);
        pool.add("entries", entries);

        pool.addProperty("rolls", 1.0F);
        pools.add(pool);
        return pools;
    }
}
