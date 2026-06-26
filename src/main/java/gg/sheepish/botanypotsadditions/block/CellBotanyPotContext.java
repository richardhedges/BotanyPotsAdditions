package gg.sheepish.botanypotsadditions.block;

import net.darkhax.botanypots.common.api.context.BotanyPotContext;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

public record CellBotanyPotContext(ModPotBlockEntity pot, int cell, Player player, InteractionHand hand) implements BotanyPotContext {
    @Override
    public ItemStack getItem(int slot) {
        return slot == ModPotBlockEntity.BASE_SEED_SLOT ? getSeedItem() : pot.getItem(slot);
    }

    @Override
    public int size() {
        return pot.getContainerSize();
    }

    @Override
    public ItemStack getSoilItem() {
        return pot.getSoilItem();
    }

    @Override
    public ItemStack getSeedItem() {
        return pot.getSeedItem(cell);
    }

    @Override
    public ItemStack getHarvestItem() {
        return pot.getHarvestItem();
    }

    @Override
    public LootParams createLootParams(BlockState state) {
        return pot.getRecipeContext().createLootParams(state);
    }

    @Override
    public void runFunction(ResourceLocation function) {
        pot.runFunction(function);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public ItemStack getInteractionItem() {
        return player != null && hand != null ? player.getItemInHand(hand) : ItemStack.EMPTY;
    }

    @Override
    public int getRequiredGrowthTicks() {
        Level level = pot.getLevel();
        Crop crop = getCrop();

        return level != null && crop != null
                ? net.darkhax.botanypots.common.impl.Helpers.getRequiredGrowthTicks(this, level, crop, getSoil())
                : -1;
    }

    @Override
    public boolean isServerThread() {
        Level level = pot.getLevel();
        return level != null && !level.isClientSide;
    }

    @Override
    public Crop getCrop() {
        Level level = pot.getLevel();
        ItemStack seed = getSeedItem();

        if (level == null || seed.isEmpty()) {
            return null;
        }

        var cache = Crop.CACHE.apply(level);
        var recipe = cache != null ? cache.lookup(seed, this, level) : null;
        return recipe != null ? recipe.value() : null;
    }

    @Override
    public Soil getSoil() {
        return pot.getOrInvalidateSoil();
    }
}
