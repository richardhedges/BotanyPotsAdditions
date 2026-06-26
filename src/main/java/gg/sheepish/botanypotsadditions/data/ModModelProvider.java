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

public class ModModelProvider implements DataProvider {
    private final PackOutput output;

    public ModModelProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (PotBlockEntry entry : ModBlocks.potBlocks()) {
            futures.add(DataProvider.saveStable(cache, blockModel(entry), resourcePath("models/block/" + entry.id() + ".json")));
            futures.add(DataProvider.saveStable(cache, blockState(entry), resourcePath("blockstates/" + entry.id() + ".json")));
            futures.add(DataProvider.saveStable(cache, itemModel(entry), resourcePath("models/item/" + entry.id() + ".json")));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "BotanyPotsAdditions block and item models";
    }

    private Path resourcePath(String path) {
        return output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(BotanyPotsAdditions.MODID)
                .resolve(path);
    }

    private static JsonObject blockModel(PotBlockEntry entry) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "block/block");
        model.addProperty("render_type", entry.variant().hasGlassLid() ? "minecraft:translucent" : "minecraft:cutout");

        JsonObject textures = new JsonObject();
        textures.addProperty("material", materialTexture(entry));
        textures.addProperty("material_top", materialTopTexture(entry));
        textures.addProperty("glass", "minecraft:block/glass");
        textures.addProperty("hopper_outside", "minecraft:block/hopper_outside");
        textures.addProperty("hopper_inside", "minecraft:block/hopper_inside");
        textures.addProperty("hopper_top", "minecraft:block/hopper_top");
        textures.addProperty("particle", "#material");
        model.add("textures", textures);
        model.add("elements", elements(entry));

        return model;
    }

    private static JsonObject blockState(PotBlockEntry entry) {
        JsonObject blockState = new JsonObject();
        JsonObject variants = new JsonObject();

        addFacingVariant(variants, entry, "north", 0);
        addFacingVariant(variants, entry, "east", 90);
        addFacingVariant(variants, entry, "south", 180);
        addFacingVariant(variants, entry, "west", 270);

        blockState.add("variants", variants);

        return blockState;
    }

    private static void addFacingVariant(JsonObject variants, PotBlockEntry entry, String facing, int yRotation) {
        JsonObject variant = new JsonObject();

        variant.addProperty("model", BotanyPotsAdditions.MODID + ":block/" + entry.id());

        if (yRotation != 0) {
            variant.addProperty("y", yRotation);
        }

        variants.add("facing=" + facing, variant);
    }

    private static JsonObject itemModel(PotBlockEntry entry) {
        JsonObject itemModel = new JsonObject();
        itemModel.addProperty("parent", BotanyPotsAdditions.MODID + ":block/" + entry.id());
        return itemModel;
    }

    private static String materialTexture(PotBlockEntry entry) {
        return "minecraft:block/" + entry.style().id();
    }

    private static String materialTopTexture(PotBlockEntry entry) {
        return "botanypots:block/" + entry.style().id() + "_pot_top";
    }

    private static JsonArray elements(PotBlockEntry entry) {
        JsonArray elements = new JsonArray();
        addPot(elements, entry.form().isHopper());

        if (entry.form().isHopper()) {
            addHopper(elements);
        }

        if (entry.variant().isDouble() || entry.variant().isQuadruple()) {
            addXDivider(elements);
        }

        if (entry.variant().isQuadruple()) {
            addZDivider(elements);
        }

        if (entry.variant().hasGlassLid()) {
            addGlassLid(elements, entry.variant().isDouble() || entry.variant().isQuadruple());
        }

        if (entry.variant().isSprinkler()) {
            addSprinkler(elements);
        }

        return elements;
    }

    private static void addPot(JsonArray elements, boolean hopper) {
        JsonObject outer = element("outer", 2, 0, 2, 14, 8, 14);
        faces(outer,
                face("north", 12, 0, 0, 8, "#material", null),
                face("east", 0, 0, 12, 8, "#material", null),
                face("south", 12, 0, 0, 8, "#material", null),
                face("west", 0, 0, 12, 8, "#material", null),
                face("up", 0, 0, 12, 12, "#material_top", null),
                face("down", 0, 0, 12, 12, "#material", hopper ? null : "down"));
        elements.add(outer);

        JsonObject inner = element("inner", 13, 8.1, 13, 3, 1, 3);
        faces(inner,
                face("north", 11, 7, 1, 0, "#material", null),
                face("east", 1, 7, 11, 0, "#material", null),
                face("south", 11, 7, 1, 0, "#material", null),
                face("west", 1, 7, 11, 0, "#material", null),
                face("up", 3, 3, 13, 13, "#material", null));
        elements.add(inner);
    }

    private static void addHopper(JsonArray elements) {
        JsonObject hopper = element("hopper", 1.75, -0.25, 1.75, 14.25, 4.25, 14.25);
        faces(hopper,
                face("north", 14, 3, 2, 7, "#hopper_outside", null),
                face("east", 2, 3, 14, 7, "#hopper_outside", null),
                face("south", 14, 3, 2, 7, "#hopper_outside", null),
                face("west", 2, 3, 14, 7, "#hopper_outside", null),
                face("up", 2, 2, 14, 14, "#hopper_top", null),
                face("down", 2, 2, 14, 14, "#hopper_inside", "down"));
        elements.add(hopper);
    }

    private static void addGlassLid(JsonArray elements, boolean celled) {
        double bottom = celled ? 8.125 : 8;
        JsonObject outer = element("glass_lid_outer", 2, bottom, 2, 14, 16, 14);
        faces(outer,
                face("north", 0, 0, 16, 16, "#glass", null),
                face("east", 0, 0, 16, 16, "#glass", null),
                face("south", 0, 0, 16, 16, "#glass", null),
                face("west", 0, 0, 16, 16, "#glass", null),
                face("up", 0, 0, 16, 16, "#glass", null));
        elements.add(outer);

        JsonObject inner = element("glass_lid_inner", 3, 15, 3, 13, bottom, 13);
        faces(inner,
                face("north", 0, 0, 16, 16, "#glass", null),
                face("east", 0, 0, 16, 16, "#glass", null),
                face("south", 0, 0, 16, 16, "#glass", null),
                face("west", 0, 0, 16, 16, "#glass", null));
        elements.add(inner);
    }

    private static void addSprinkler(JsonArray elements) {
        JsonObject body = element("sprinkler_body", 6.5, 14, 6.5, 9.5, 15, 9.5);
        faces(body,
                face("north", 0, 0, 3, 1, "#hopper_outside", null),
                face("east", 0, 0, 3, 1, "#hopper_outside", null),
                face("south", 0, 0, 3, 1, "#hopper_outside", null),
                face("west", 0, 0, 3, 1, "#hopper_outside", null),
                face("up", 0, 0, 3, 3, "#hopper_outside", null),
                face("down", 0, 0, 3, 3, "#hopper_outside", null));
        elements.add(body);

        JsonObject nozzle = element("sprinkler_nozzle", 7.25, 13.25, 7.25, 8.75, 14, 8.75);
        faces(nozzle,
                face("north", 0, 0, 1.5, 0.75, "#hopper_outside", null),
                face("east", 0, 0, 1.5, 0.75, "#hopper_outside", null),
                face("south", 0, 0, 1.5, 0.75, "#hopper_outside", null),
                face("west", 0, 0, 1.5, 0.75, "#hopper_outside", null),
                face("up", 0, 0, 1.5, 1.5, "#hopper_outside", null),
                face("down", 0, 0, 1.5, 1.5, "#hopper_outside", null));
        elements.add(nozzle);
    }

    private static void addXDivider(JsonArray elements) {
        JsonObject divider = element("cell_divider_x", 7.5, 1, 3, 8.5, 8.0625, 13);
        faces(divider,
                face("north", 0, 0, 1, 7, "#material", null),
                face("east", 0, 0, 10, 7, "#material", null),
                face("south", 0, 0, 1, 7, "#material", null),
                face("west", 0, 0, 10, 7, "#material", null),
                face("up", 0, 0, 1, 10, "#material_top", null));
        elements.add(divider);
    }

    private static void addZDivider(JsonArray elements) {
        JsonObject divider = element("cell_divider_z", 3, 1, 7.5, 13, 8.0625, 8.5);
        faces(divider,
                face("north", 0, 0, 10, 7, "#material", null),
                face("east", 0, 0, 1, 7, "#material", null),
                face("south", 0, 0, 10, 7, "#material", null),
                face("west", 0, 0, 1, 7, "#material", null),
                face("up", 0, 0, 10, 1, "#material_top", null));
        elements.add(divider);
    }

    private static JsonObject element(String name, double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
        JsonObject element = new JsonObject();
        element.addProperty("name", name);
        element.add("from", array(fromX, fromY, fromZ));
        element.add("to", array(toX, toY, toZ));
        return element;
    }

    private static void faces(JsonObject element, JsonObject... faces) {
        JsonObject facesObject = new JsonObject();
        for (JsonObject face : faces) {
            String direction = face.remove("direction").getAsString();
            facesObject.add(direction, face);
        }
        element.add("faces", facesObject);
    }

    private static JsonObject face(String direction, double u1, double v1, double u2, double v2, String texture, String cullface) {
        JsonObject face = new JsonObject();
        face.addProperty("direction", direction);
        face.add("uv", array(u1, v1, u2, v2));
        face.addProperty("texture", texture);

        if (cullface != null) {
            face.addProperty("cullface", cullface);
        }

        return face;
    }

    private static JsonArray array(double... values) {
        JsonArray array = new JsonArray();
        for (double value : values) {
            array.add(value);
        }
        return array;
    }
}
