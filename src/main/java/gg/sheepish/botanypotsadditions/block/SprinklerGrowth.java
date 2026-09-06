package gg.sheepish.botanypotsadditions.block;

/** Resource budgeting shared by server ticking and client growth estimates. */
public final class SprinklerGrowth {
    public static int operations(int energy, int water, int capacity, int energyRequired,
            int energyCost, int waterCost, int minimum, int maximum) {
        if (energy < energyRequired || water < waterCost) {
            return 0;
        }
        int upper = Math.max(minimum, maximum);
        long stored = Math.clamp(energy, 0, capacity);
        int scaled = energyCost == 0 ? upper : minimum + (int) (stored * (upper - minimum) / capacity);
        int energyLimit = energyCost == 0 ? upper : energy / energyCost;
        int waterLimit = waterCost == 0 ? upper : water / waterCost;
        return Math.min(scaled, Math.min(energyLimit, waterLimit));
    }

    private SprinklerGrowth() {}
}
