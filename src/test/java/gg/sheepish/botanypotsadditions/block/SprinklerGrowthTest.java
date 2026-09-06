package gg.sheepish.botanypotsadditions.block;

/** Runs without Minecraft bootstrapping; covers resource budgets and integer boundaries. */
public final class SprinklerGrowthTest {
    @org.junit.Test
    public void resourceBudgets() {
        expect(0, defaults(99, 4000), "minimum charge");
        expect(0, defaults(10000, 0), "empty water");
        expect(2, defaults(100, 4000), "low charge default");
        expect(3, defaults(5000, 4000), "half charge default");
        expect(5, defaults(10000, 4000), "full charge default");
        expect(1, defaults(10000, 1), "water-limited operations");
        expect(8, SprinklerGrowth.operations(100000, 10000, 100000, 100, 100, 100, 8, 8), "custom fixed speed");
        expect(0, SprinklerGrowth.operations(99, 10000, 100000, 100, 100, 100, 8, 8), "cannot afford full operation");
        expect(8, SprinklerGrowth.operations(0, 0, 100000, 0, 0, 0, 2, 8), "both costs disabled");
        expect(2, SprinklerGrowth.operations(0, 200, 100000, 0, 0, 100, 2, 8), "water-only pot");
        expect(8, SprinklerGrowth.operations(100000, 0, 100000, 100, 100, 0, 8, 8), "energy-only pot");
        expect(100, SprinklerGrowth.operations(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 1, 1, 1, 1, 100), "large capacity overflow");
        expect(8, SprinklerGrowth.operations(100, 100, 100, 1, 1, 1, 8, 2), "reversed speed limits");
        System.out.println("13 sprinkler resource regression checks passed.");
    }

    private static int defaults(int energy, int water) {
        return SprinklerGrowth.operations(energy, water, 10000, 100, 10, 1, 2, 5);
    }

    private static void expect(int expected, int actual, String description) {
        if (expected != actual) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
