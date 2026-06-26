package gg.sheepish.botanypotsadditions.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import gg.sheepish.botanypotsadditions.BotanyPotsAdditions;
import gg.sheepish.botanypotsadditions.block.ModPotBlock;
import net.darkhax.botanypots.common.impl.block.BotanyPotBlock;
import net.darkhax.botanypots.common.impl.block.PotType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BotanyPotsAdditions.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BotanyPotsAdditions.MODID);
    private static final List<PotBlockEntry> POT_BLOCKS = new ArrayList<>();
    private static final List<DeferredItem<BlockItem>> BLOCK_ITEMS = new ArrayList<>();

    // Mirrors Botany Pots 1.21.1 style ids so our variants line up with their material/color families.
    private static final List<PotStyle> POT_STYLES = List.of(
            new PotStyle("terracotta"),
            new PotStyle("black_concrete"),
            new PotStyle("black_glazed_terracotta"),
            new PotStyle("black_terracotta"),
            new PotStyle("blue_concrete"),
            new PotStyle("blue_glazed_terracotta"),
            new PotStyle("blue_terracotta"),
            new PotStyle("bricks"),
            new PotStyle("brown_concrete"),
            new PotStyle("brown_glazed_terracotta"),
            new PotStyle("brown_terracotta"),
            new PotStyle("cyan_concrete"),
            new PotStyle("cyan_glazed_terracotta"),
            new PotStyle("cyan_terracotta"),
            new PotStyle("deepslate_bricks"),
            new PotStyle("end_stone_bricks"),
            new PotStyle("gray_concrete"),
            new PotStyle("gray_glazed_terracotta"),
            new PotStyle("gray_terracotta"),
            new PotStyle("green_concrete"),
            new PotStyle("green_glazed_terracotta"),
            new PotStyle("green_terracotta"),
            new PotStyle("light_blue_concrete"),
            new PotStyle("light_blue_glazed_terracotta"),
            new PotStyle("light_blue_terracotta"),
            new PotStyle("light_gray_concrete"),
            new PotStyle("light_gray_glazed_terracotta"),
            new PotStyle("light_gray_terracotta"),
            new PotStyle("lime_concrete"),
            new PotStyle("lime_glazed_terracotta"),
            new PotStyle("lime_terracotta"),
            new PotStyle("magenta_concrete"),
            new PotStyle("magenta_glazed_terracotta"),
            new PotStyle("magenta_terracotta"),
            new PotStyle("mossy_stone_bricks"),
            new PotStyle("mud_bricks"),
            new PotStyle("nether_bricks"),
            new PotStyle("orange_concrete"),
            new PotStyle("orange_glazed_terracotta"),
            new PotStyle("orange_terracotta"),
            new PotStyle("pink_concrete"),
            new PotStyle("pink_glazed_terracotta"),
            new PotStyle("pink_terracotta"),
            new PotStyle("polished_blackstone_bricks"),
            new PotStyle("prismarine_bricks"),
            new PotStyle("purple_concrete"),
            new PotStyle("purple_glazed_terracotta"),
            new PotStyle("purple_terracotta"),
            new PotStyle("quartz_bricks"),
            new PotStyle("red_concrete"),
            new PotStyle("red_glazed_terracotta"),
            new PotStyle("red_nether_bricks"),
            new PotStyle("red_terracotta"),
            new PotStyle("stone_bricks"),
            new PotStyle("tuff_bricks"),
            new PotStyle("white_concrete"),
            new PotStyle("white_glazed_terracotta"),
            new PotStyle("white_terracotta"),
            new PotStyle("yellow_concrete"),
            new PotStyle("yellow_glazed_terracotta"),
            new PotStyle("yellow_terracotta"));

    private static final List<PotVariant> POT_VARIANTS = List.of(
            new PotVariant("greenhouse"),
            new PotVariant("sprinkler"),
            new PotVariant("double"),
            new PotVariant("quadruple"),
            new PotVariant("double_greenhouse"),
            new PotVariant("quadruple_greenhouse"),
            new PotVariant("double_sprinkler"),
            new PotVariant("quadruple_sprinkler"));

    private static final List<PotForm> POT_FORMS = List.of(
            new PotForm("botany_pot"),
            new PotForm("hopper_botany_pot"),
            new PotForm("waxed_botany_pot"));

    static {
        POT_STYLES.forEach(style -> POT_VARIANTS.forEach(variant -> POT_FORMS.forEach(form -> registerPot(style, variant, form))));
    }

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    public static List<PotBlockEntry> potBlocks() {
        return Collections.unmodifiableList(POT_BLOCKS);
    }

    public static List<DeferredItem<BlockItem>> blockItems() {
        return Collections.unmodifiableList(BLOCK_ITEMS);
    }

    public static Supplier<BlockItem> creativeTabIcon() {
        return BLOCK_ITEMS.getFirst();
    }

    private static void registerPot(PotStyle style, PotVariant variant, PotForm form) {
        String name = style.id() + "_" + variant.id() + "_" + form.id();
        DeferredBlock<Block> block = BLOCKS.register(name, () -> new ModPotBlock(potProperties(), form.potType(), variant.hasGlassLid(), variant.isGreenhouse(), variant.isDouble(), variant.isQuadruple()));
        DeferredItem<BlockItem> item = registerBlockItem(name, block);

        POT_BLOCKS.add(new PotBlockEntry(style, variant, form, block, item));
    }

    private static DeferredItem<BlockItem> registerBlockItem(String name, Supplier<? extends Block> block) {
        DeferredItem<BlockItem> item = ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        BLOCK_ITEMS.add(item);
        return item;
    }

    private static BlockBehaviour.Properties potProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_BROWN)
                .strength(1.25F, 4.2F)
                .sound(SoundType.DECORATED_POT)
                .noOcclusion()
                .lightLevel(BotanyPotBlock.LIGHT_LEVEL);
    }

    public record PotStyle(String id) {
        public boolean isDefault() {
            return id.equals("terracotta");
        }
    }

    public record PotVariant(String id) {
        public boolean isDouble() {
            return id.startsWith("double");
        }

        public boolean isQuadruple() {
            return id.startsWith("quadruple");
        }

        public boolean isGreenhouse() {
            return id.contains("greenhouse");
        }

        public boolean isSprinkler() {
            return id.contains("sprinkler");
        }

        public boolean hasGlassLid() {
            return isGreenhouse() || isSprinkler();
        }
    }

    public record PotForm(String id) {
        public boolean isHopper() {
            return id.equals("hopper_botany_pot");
        }

        public PotType potType() {
            if (isHopper()) {
                return PotType.HOPPER;
            }

            if (id.equals("waxed_botany_pot")) {
                return PotType.WAXED;
            }

            return PotType.BASIC;
        }
    }

    public record PotBlockEntry(PotStyle style, PotVariant variant, PotForm form, DeferredBlock<Block> block, DeferredItem<BlockItem> item) {
        public String id() {
            return style.id() + "_" + variant.id() + "_" + form.id();
        }
    }
}
