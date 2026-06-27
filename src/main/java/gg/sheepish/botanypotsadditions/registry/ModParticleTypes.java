package gg.sheepish.botanypotsadditions.registry;

import gg.sheepish.botanypotsadditions.BotanyPotsAdditions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticleTypes {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, BotanyPotsAdditions.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPRINKLER_WATER = PARTICLE_TYPES.register(
            "sprinkler_water",
            () -> new SimpleParticleType(false));

    private ModParticleTypes() {
    }

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }
}
