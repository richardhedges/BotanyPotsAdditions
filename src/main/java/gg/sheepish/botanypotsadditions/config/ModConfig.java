package gg.sheepish.botanypotsadditions.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** World-specific gameplay settings, synchronized to connecting clients by NeoForge. */
public final class ModConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue DOUBLE_YIELD;
    public static final ModConfigSpec.DoubleValue QUAD_YIELD;
    public static final ModConfigSpec.DoubleValue GREENHOUSE_GROWTH;
    public static final ModConfigSpec.DoubleValue GREENHOUSE_YIELD;
    public static final ModConfigSpec.DoubleValue SPRINKLER_YIELD;
    public static final ModConfigSpec.IntValue MIN_GROWTH;
    public static final ModConfigSpec.IntValue MAX_GROWTH;
    public static final ModConfigSpec.IntValue WATER_CAPACITY;
    public static final ModConfigSpec.IntValue WATER_PER_OPERATION;
    public static final ModConfigSpec.IntValue ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue ENERGY_PER_OPERATION;
    public static final ModConfigSpec.IntValue ENERGY_INPUT;
    public static final ModConfigSpec.IntValue MIN_ENERGY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Gameplay settings. Restart the world/server after editing.",
                "Yield multipliers describe standard crops; soil, tools and crop recipes also affect output.",
                "Double/quad yield is divided across the seed slots: empty cells produce nothing.").push("double_pots");
        DOUBLE_YIELD = multiplier(builder, "yieldMultiplier", 2.0, 0.0);
        builder.pop().push("quad_pots");
        QUAD_YIELD = multiplier(builder, "yieldMultiplier", 4.0, 0.0);
        builder.pop().push("greenhouse_pots");
        builder.comment("Growth speed relative to an ordinary pot; stacks additively with Botany Pots soil/tool bonuses.");
        GREENHOUSE_GROWTH = multiplier(builder, "growthMultiplier", 1.0, 1.0);
        GREENHOUSE_YIELD = multiplier(builder, "yieldMultiplier", 1.5, 0.0);
        builder.pop().push("sprinkler_pots");
        SPRINKLER_YIELD = multiplier(builder, "yieldMultiplier", 1.5, 0.0);
        builder.comment("Growth operations per game tick scale with stored energy between these limits.",
                "Set both limits to the same number for a fixed speed. Maximum is raised to minimum if lower.");
        MIN_GROWTH = integer(builder, "minGrowthMultiplier", 2, 1, 100);
        MAX_GROWTH = integer(builder, "growthMultiplier", 5, 1, 100);
        builder.comment("Water storage in mB (1000 mB = one bucket).");
        WATER_CAPACITY = integer(builder, "waterCapacity", 4000, 1, Integer.MAX_VALUE);
        builder.comment("mB consumed per successful growth operation, shared by all cells. Zero disables water use.");
        WATER_PER_OPERATION = integer(builder, "waterPerOperation", 1, 0, Integer.MAX_VALUE);
        builder.comment("Energy storage in FE.");
        ENERGY_CAPACITY = integer(builder, "energyCapacity", 10000, 1, Integer.MAX_VALUE);
        builder.comment("FE consumed per successful growth operation, shared by all cells. Zero disables energy use.");
        ENERGY_PER_OPERATION = integer(builder, "energyPerOperation", 10, 0, Integer.MAX_VALUE);
        builder.comment("Maximum FE accepted per capability receive call.");
        ENERGY_INPUT = integer(builder, "energyMaxReceive", 1000, 1, Integer.MAX_VALUE);
        builder.comment("Minimum stored FE to operate; ignored when energyPerOperation is zero.",
                "The pot must also afford a full operation. Costs/thresholds above capacity pause growth.");
        MIN_ENERGY = integer(builder, "minimumEnergyToGrow", 100, 0, Integer.MAX_VALUE);
        builder.pop();
        SPEC = builder.build();
    }

    private static ModConfigSpec.DoubleValue multiplier(ModConfigSpec.Builder builder, String name, double value, double min) {
        return builder.worldRestart().defineInRange(name, value, min, 100.0);
    }

    private static ModConfigSpec.IntValue integer(ModConfigSpec.Builder builder, String name, int value, int min, int max) {
        return builder.worldRestart().defineInRange(name, value, min, max);
    }

    public static int energyRequired() {
        return ENERGY_PER_OPERATION.get() == 0 ? 0 : Math.max(MIN_ENERGY.get(), ENERGY_PER_OPERATION.get());
    }

    private ModConfig() {}
}
