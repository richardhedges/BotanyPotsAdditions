package gg.sheepish.botanypotsadditions.data;

import java.util.Locale;

import gg.sheepish.botanypotsadditions.BotanyPotsAdditions;
import gg.sheepish.botanypotsadditions.registry.ModBlocks;
import gg.sheepish.botanypotsadditions.registry.ModBlocks.PotBlockEntry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, BotanyPotsAdditions.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.botanypotsadditions", "Botany Pots Additions");
        ModBlocks.potBlocks().forEach(entry -> addBlock(entry.block(), blockName(entry)));
    }

    private static String blockName(PotBlockEntry entry) {
        if (entry.style().isDefault()) {
            return titleCase(entry.variant().id() + "_" + entry.form().id());
        }

        return titleCase(entry.style().id() + "_" + entry.variant().id() + "_" + entry.form().id());
    }

    private static String titleCase(String id) {
        StringBuilder name = new StringBuilder();

        for (String part : id.split("_")) {
            if (!name.isEmpty()) {
                name.append(' ');
            }

            name.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
            name.append(part.substring(1));
        }

        return name.toString();
    }
}
