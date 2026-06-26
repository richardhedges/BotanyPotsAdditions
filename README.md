
# BotanyPotsAdditions

A NeoForge mod for Minecraft 1.21.1 that expands the Botany Pots mod with new pot types, upgrades, and higher-capacity variants.

Developer: BackSheep

## Planned Features

### Greenhouse Pots

Greenhouse pots are upgraded Botany Pots with a glass dome over the top. They will come in both normal and hopper variants, and will increase seed growth speed compared to standard Botany Pots.

### Sprinkler Pots

Sprinkler pots build on the greenhouse-style progression with an additional growth speed increase. They will also come in normal and hopper variants, and require RF to operate.

### Celled Pots

Celled pots add storage-drawer-style capacity tiers to Botany Pots. Instead of one crop space, celled variants can support multiple seeds of the same type while sharing one farmland block.

Supported cell sizes:

- Single: 1 seed and 1 farmland
- Double: 2 seeds of the same type and 1 farmland
- Quadruple: 4 seeds of the same type and 1 farmland

Celled variants are planned for:

- Standard Botany Pots
- Hopper Botany Pots
- Greenhouse Botany Pots
- Greenhouse Hopper Botany Pots
- Sprinkler Botany Pots
- Sprinkler Hopper Botany Pots

Double and quadruple pots will use split models so each crop cell is visually represented as halves or quarters.

## Development

Build the mod with:

```sh
./gradlew build
```

Run a client development instance with:

```sh
./gradlew runClient
```
