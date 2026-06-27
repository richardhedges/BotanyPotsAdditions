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

public class ModRecipeProvider implements DataProvider {
    private static final String BOTANY_POTS = "botanypots:";
    private final PackOutput output;

    public ModRecipeProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (PotBlockEntry entry : ModBlocks.potBlocks()) {
            if (entry.variant().isGreenhouse()) {
                futures.add(save(cache, entry.id(), greenhouseRecipe(entry, customOrBaseSource(entry))));

                if (!entry.variant().isDouble() && !entry.variant().isQuadruple() && !entry.form().id().equals("waxed_botany_pot")) {
                    futures.add(save(cache, entry.id() + "_from_materials", directGreenhouseRecipe(entry)));
                }
            }
            else if (entry.variant().isSprinkler()) {
                futures.add(save(cache, entry.id(), sprinklerRecipe(entry)));
            }
            else if (entry.variant().isDouble() || entry.variant().isQuadruple()) {
                futures.add(save(cache, entry.id(), celledRecipe(entry)));
            }

            if (entry.form().isHopper()) {
                futures.add(save(cache, entry.id() + "_from_hopper", hopperUpgradeRecipe(entry)));
            }
            else if (entry.form().id().equals("waxed_botany_pot")) {
                futures.add(save(cache, entry.id() + "_from_honeycomb", waxedRecipe(entry)));
            }
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "BotanyPotsAdditions recipes";
    }

    private CompletableFuture<?> save(CachedOutput cache, String name, JsonObject recipe) {
        return DataProvider.saveStable(cache, recipe, dataPath("recipe/" + name + ".json"));
    }

    private Path dataPath(String path) {
        return output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(BotanyPotsAdditions.MODID)
                .resolve(path);
    }

    private static JsonObject greenhouseRecipe(PotBlockEntry entry, String sourcePot) {
        return shaped(entry, "botanypotsadditions:greenhouse_pot", greenhousePattern(entry), key(
                itemKey("G", "minecraft:glass"),
                itemKey("P", sourcePot)));
    }

    private static JsonObject directGreenhouseRecipe(PotBlockEntry entry) {
        List<JsonObject> keys = new ArrayList<>();
        keys.add(itemKey("G", "minecraft:glass"));
        keys.add(itemKey("M", materialItem(entry)));
        keys.add(itemKey("P", "minecraft:flower_pot"));

        if (entry.form().isHopper()) {
            keys.add(itemKey("H", "minecraft:hopper"));
            return shaped(entry, "botanypotsadditions:greenhouse_pot", List.of("GHG", "MPM", " M "), key(keys));
        }

        return shaped(entry, "botanypotsadditions:greenhouse_pot", List.of("GGG", "MPM", " M "), key(keys));
    }

    private static JsonObject sprinklerRecipe(PotBlockEntry entry) {
        return shaped(entry, "botanypotsadditions:sprinkler_pot", List.of(" I ", "IGI", " P "), key(
                tagKey("I", "c:ingots/iron"),
                itemKey("G", "minecraft:gold_ingot"),
                itemKey("P", greenhousePot(entry))));
    }

    private static JsonObject celledRecipe(PotBlockEntry entry) {
        List<String> pattern = entry.variant().isQuadruple() ? List.of("PP", "PP") : List.of("PP");
        return shaped(entry, "botanypotsadditions:celled_pot", pattern, key(itemKey("P", basePot(entry))));
    }

    private static JsonObject hopperUpgradeRecipe(PotBlockEntry entry) {
        return shapeless(entry, "botanypotsadditions:hopper_pot", List.of(
                itemIngredient("minecraft:hopper"),
                itemIngredient(nonHopperPot(entry))));
    }

    private static JsonObject waxedRecipe(PotBlockEntry entry) {
        return shapeless(entry, "botanypotsadditions:waxed_pot", List.of(
                itemIngredient("minecraft:honeycomb"),
                itemIngredient(nonWaxedPot(entry))));
    }

    private static JsonObject shaped(PotBlockEntry entry, String group, List<String> pattern, JsonObject key) {
        JsonObject recipe = baseRecipe(entry);
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        recipe.addProperty("group", group);

        JsonArray patternArray = new JsonArray();
        pattern.forEach(patternArray::add);
        recipe.add("pattern", patternArray);
        recipe.add("key", key);
        recipe.add("result", result(entry));

        return recipe;
    }

    private static JsonObject shapeless(PotBlockEntry entry, String group, List<JsonObject> ingredients) {
        JsonObject recipe = baseRecipe(entry);
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "misc");
        recipe.addProperty("group", group);

        JsonArray ingredientArray = new JsonArray();
        ingredients.forEach(ingredientArray::add);
        recipe.add("ingredients", ingredientArray);
        recipe.add("result", result(entry));

        return recipe;
    }

    private static JsonObject baseRecipe(PotBlockEntry entry) {
        JsonObject recipe = new JsonObject();
        JsonArray conditions = new JsonArray();
        JsonObject condition = new JsonObject();

        condition.addProperty("type", "botanypots:config");
        condition.addProperty("property", recipeConfig(entry));
        conditions.add(condition);
        recipe.add("bookshelf:load_conditions", conditions);

        return recipe;
    }

    private static String recipeConfig(PotBlockEntry entry) {
        if (entry.form().isHopper()) {
            return "can_craft_hopper_pots";
        }

        if (entry.form().id().equals("waxed_botany_pot")) {
            return "can_wax_pots";
        }

        return "can_craft_basic_pots";
    }

    private static JsonObject result(PotBlockEntry entry) {
        JsonObject result = new JsonObject();
        result.addProperty("id", BotanyPotsAdditions.MODID + ":" + entry.id());
        result.addProperty("count", 1);
        return result;
    }

    private static List<String> greenhousePattern(PotBlockEntry entry) {
        return entry.form().isHopper() ? List.of("G G", " P ") : List.of("GGG", " P ");
    }

    private static String customOrBaseSource(PotBlockEntry entry) {
        if (!entry.variant().isDouble() && !entry.variant().isQuadruple()) {
            return basePot(entry);
        }

        String variant = entry.variant().isQuadruple() ? "quadruple" : "double";
        return customPot(entry, variant, entry.form().id());
    }

    private static String greenhousePot(PotBlockEntry entry) {
        return customPot(entry, entry.variant().id().replace("sprinkler", "greenhouse"), entry.form().id());
    }

    private static String nonHopperPot(PotBlockEntry entry) {
        return customPot(entry, entry.variant().id(), "botany_pot");
    }

    private static String nonWaxedPot(PotBlockEntry entry) {
        return customPot(entry, entry.variant().id(), "botany_pot");
    }

    private static String customPot(PotBlockEntry entry, String variant, String form) {
        return BotanyPotsAdditions.MODID + ":" + entry.style().id() + "_" + variant + "_" + form;
    }

    private static String basePot(PotBlockEntry entry) {
        return BOTANY_POTS + entry.style().id() + "_" + entry.form().id();
    }

    private static String materialItem(PotBlockEntry entry) {
        return "minecraft:" + entry.style().id();
    }

    private static JsonObject key(List<JsonObject> entries) {
        JsonObject key = new JsonObject();
        entries.forEach(entry -> key.add(entry.remove("symbol").getAsString(), entry));
        return key;
    }

    private static JsonObject key(JsonObject... entries) {
        return key(List.of(entries));
    }

    private static JsonObject itemKey(String symbol, String item) {
        JsonObject key = itemIngredient(item);
        key.addProperty("symbol", symbol);
        return key;
    }

    private static JsonObject tagKey(String symbol, String tag) {
        JsonObject key = new JsonObject();
        key.addProperty("symbol", symbol);
        key.addProperty("tag", tag);
        return key;
    }

    private static JsonObject itemIngredient(String item) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", item);
        return ingredient;
    }
}
