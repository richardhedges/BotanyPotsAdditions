# BotanyPotsAdditions

Build a compact crop farm with greenhouse, sprinkler, double and quadruple upgrades for Botany Pots. Available for **Minecraft 1.21.1 on NeoForge**, with configurable balance settings introduced in **1.0.1**.

## Choose your pots

- **Double and quadruple pots:** two or four seed slots sharing one soil slot, with a visible divided layout.
- **Greenhouse pots:** glass-covered pots with a default 50% yield bonus. Growth speed can also be increased in the config.
- **Sprinkler pots:** FE and water drive accelerated growth. By default, they run 2–5 growth operations per game tick depending on stored charge, with the same yield bonus as greenhouse pots.
- **Combined upgrades:** double and quadruple layouts are available for greenhouse and sprinkler pots.
- **Hopper and waxed forms:** automate harvesting and item collection with hopper pots, or use waxed forms in your builds.
- **Matching materials:** variants cover Botany Pots' terracotta, concrete, glazed terracotta, brick, stone and other styles.

Sprinklers accept water from above and FE through their horizontal sides. The default capacity is **4,000 mB water** and **10,000 FE**. Each successful growth operation costs **1 mB and 10 FE**, shared by the pot's cells; at least **100 FE** must be stored to operate. Empty resources pause growth. Custom menus show seed slots, resource levels and growth estimates.

## Configuration

After starting a world, edit `serverconfig/botanypotsadditions-server.toml` inside its world folder. In singleplayer this is usually `saves/<world>/serverconfig/`; on a dedicated server it is `<world>/serverconfig/`. Stop the world/server before editing and restart afterwards. The server sends its settings to connecting clients.

For modpacks, put a copy in `defaultconfigs/` to seed new worlds. Existing worlds keep their own configuration.

Default values:

```toml
[double_pots]
yieldMultiplier = 2.0

[quad_pots]
yieldMultiplier = 4.0

[greenhouse_pots]
growthMultiplier = 1.0
yieldMultiplier = 1.5

[sprinkler_pots]
yieldMultiplier = 1.5
minGrowthMultiplier = 2
growthMultiplier = 5
waterCapacity = 4000
waterPerOperation = 1
energyCapacity = 10000
energyPerOperation = 10
energyMaxReceive = 1000
minimumEnergyToGrow = 100
```

### Tuning guidance

- Double/quad yield settings describe a fully planted pot with ordinary crop yield. The value is divided across its cells: the defaults give each planted cell normal output. Empty cells contribute nothing.
- Greenhouse or sprinkler yield combines with the cell tier. For example, a fully planted default double greenhouse has an expected 3× ordinary harvest output. Fractional yield uses random extra loot rolls; individual harvests vary.
- Yield uses Botany Pots' recipe yield scaling. Soil, tools and crop recipes can change the final result. A zero yield setting removes the ordinary base yield, but external yield bonuses can still contribute.
- Greenhouse growth uses Botany Pots' additive growth modifier system: `2.0` supplies +100% base growth speed, alongside soil and tool bonuses. Sprinklers have their own speed settings and do not inherit the greenhouse growth value.
- Sprinkler `growthMultiplier` is the maximum number of growth operations per game tick, while `minGrowthMultiplier` is the minimum. Set both to `4` for a fixed 4× operation rate. If the maximum is below the minimum, the minimum is used for both. Resource availability may reduce the actual rate.
- Costs are **per successful growth operation**, not per game tick or harvest. Set `waterPerOperation = 0` for no water requirement, or `energyPerOperation = 0` for no FE requirement. With FE use disabled, sprinklers use the maximum speed and ignore the minimum charge.
- `energyMaxReceive` limits each FE insertion call. Costs or minimum charge above capacity prevent operation. Reducing a capacity discards stored resources above the new limit when pots load.
- Yield settings accept 0–100; greenhouse growth accepts 1–100; sprinkler growth limits accept whole numbers 1–100. Capacities and input limits are positive integers; costs and minimum charge can also be zero.

Settings apply across materials and pot forms. Cell count, recipes, slots and connection sides remain defined by the pot type. The current sprinkler family uses FE and water together; there is no separate powered-sprinkler family.

## Compatibility and requirements

- Minecraft **1.21.1**
- NeoForge **21.1.229 or newer** for Minecraft 1.21.1
- Botany Pots **1.21.1-21.1.42 or newer**, plus its required dependencies

Optional Mystical Agriculture integration adds a Botany Pots recipe for Sculk Seeds when the relevant content is present. Mystical Agriculture is not required for the core mod.
