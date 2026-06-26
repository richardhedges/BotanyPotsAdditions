package gg.sheepish.botanypotsadditions.registry;

import gg.sheepish.botanypotsadditions.BotanyPotsAdditions;
import gg.sheepish.botanypotsadditions.menu.CelledPotMenu;
import gg.sheepish.botanypotsadditions.menu.SprinklerPotMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, BotanyPotsAdditions.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<CelledPotMenu>> CELLED_POT = MENU_TYPES.register(
            "celled_pot",
            () -> IMenuTypeExtension.create(CelledPotMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<SprinklerPotMenu>> SPRINKLER_POT = MENU_TYPES.register(
            "sprinkler_pot",
            () -> IMenuTypeExtension.create(SprinklerPotMenu::fromNetwork));

    private ModMenuTypes() {
    }

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
