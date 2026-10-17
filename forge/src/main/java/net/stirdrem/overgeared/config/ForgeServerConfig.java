package net.stirdrem.overgeared.config;

import java.util.List;

public class ForgeServerConfig implements ServerConfigCommon {

    @Override
    public boolean ENABLE_MOD_TOOLTIPS() {
        return ServerConfig.ENABLE_MOD_TOOLTIPS.get();
    }

    @Override
    public int MAX_ANVIL_DISTANCE() {
        return ServerConfig.MAX_ANVIL_DISTANCE.get();
    }

    @Override
    public int STONE_ANVIL_MAX_USES() {
        return ServerConfig.STONE_ANVIL_MAX_USES.get();
    }

    @Override
    public boolean ENABLE_STONE_TO_ANVIL() {
        return ServerConfig.ENABLE_STONE_TO_ANVIL.get();
    }

    @Override
    public boolean ENABLE_ANVIL_TO_SMITHING() {
        return ServerConfig.ENABLE_ANVIL_TO_SMITHING.get();
    }

    @Override
    public boolean ENABLE_BLUEPRINT_FORGING() {
        return ServerConfig.ENABLE_BLUEPRINT_FORGING.get();
    }

    @Override
    public boolean ENABLE_TIERED_HAMMERS() {
        return ServerConfig.ENABLE_TIERED_HAMMERS.get();
    }

    @Override
    public boolean ENABLE_TIER_A() {
        return ServerConfig.ENABLE_TIER_A.get();
    }

    @Override
    public boolean ENABLE_TIER_B() {
        return ServerConfig.ENABLE_TIER_B.get();
    }

    @Override
    public int HEATED_ITEM_COOLDOWN_TICKS() {
        return ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();
    }

    @Override
    public boolean ENABLE_DRAGON_BREATH_RECIPE() {
        return ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get();
    }

    @Override
    public boolean TIPPING_TOGGLE() {
        return ServerConfig.TIPPING_TOGGLE.get();
    }

    @Override
    public boolean UPGRADE_ARROW_POTION_TOGGLE() {
        return ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get();
    }

    @Override
    public int MAX_POTION_TIPPING_USE() {
        return ServerConfig.MAX_POTION_TIPPING_USE.get();
    }

    @Override
    public boolean ENABLE_MINIGAME() {
        return ServerConfig.ENABLE_MINIGAME.get();
    }

    @Override
    public boolean INGREDIENTS_DEFINE_MAX_QUALITY() {
        return ServerConfig.INGREDIENTS_DEFINE_MAX_QUALITY.get();
    }

    @Override
    public double MASTER_QUALITY_CHANCE() {
        return ServerConfig.MASTER_QUALITY_CHANCE.get();
    }

    @Override
    public double MASTER_FROM_INGREDIENT_CHANCE() {
        return ServerConfig.MASTER_FROM_INGREDIENT_CHANCE.get();
    }

    @Override
    public double PERFECT_QUALITY_SCORE() {
        return ServerConfig.PERFECT_QUALITY_SCORE.get();
    }

    @Override
    public double EXPERT_QUALITY_SCORE() {
        return ServerConfig.EXPERT_QUALITY_SCORE.get();
    }

    @Override
    public double WELL_QUALITY_SCORE() {
        return ServerConfig.WELL_QUALITY_SCORE.get();
    }

    @Override
    public double BASE_DURABILITY_MULTIPLIER() {
        return ServerConfig.BASE_DURABILITY_MULTIPLIER.get();
    }

    @Override
    public List<? extends String> BASE_DURABILITY_BLACKLIST() {
        return ServerConfig.BASE_DURABILITY_BLACKLIST.get();
    }

    @Override
    public boolean GRINDING_RESTORE_DURABILITY() {
        return ServerConfig.GRINDING_RESTORE_DURABILITY.get();
    }

    @Override
    public List<? extends String> GRINDING_BLACKLIST() {
        return ServerConfig.GRINDING_BLACKLIST.get();
    }

    @Override
    public double DURABILITY_REDUCE_PER_GRIND() {
        return ServerConfig.DURABILITY_REDUCE_PER_GRIND.get();
    }

    @Override
    public double DAMAGE_RESTORE_PER_GRIND() {
        return ServerConfig.DAMAGE_RESTORE_PER_GRIND.get();
    }

    @Override
    public double FAIL_ON_WELL_QUALITY_CHANCE() {
        return ServerConfig.FAIL_ON_WELL_QUALITY_CHANCE.get();
    }

    @Override
    public double FAIL_ON_EXPERT_QUALITY_CHANCE() {
        return ServerConfig.FAIL_ON_EXPERT_QUALITY_CHANCE.get();
    }

    @Override
    public List<? extends String> AVAILABLE_TOOL_TYPES() {
        return ServerConfig.AVAILABLE_TOOL_TYPES.get();
    }

    @Override
    public List<? extends String> HIDDEN_TOOL_TYPES() {
        return ServerConfig.HIDDEN_TOOL_TYPES.get();
    }

    @Override
    public int EXPERT_MAX_USE() {
        return ServerConfig.EXPERT_MAX_USE.get();
    }

    @Override
    public int WELL_MAX_USE() {
        return ServerConfig.WELL_MAX_USE.get();
    }

    @Override
    public int POOR_MAX_USE() {
        return ServerConfig.POOR_MAX_USE.get();
    }

    @Override
    public double MASTER_DURABILITY_BONUS() {
        return ServerConfig.MASTER_DURABILITY_BONUS.get();
    }

    @Override
    public double PERFECT_DURABILITY_BONUS() {
        return ServerConfig.PERFECT_DURABILITY_BONUS.get();
    }

    @Override
    public double EXPERT_DURABILITY_BONUS() {
        return ServerConfig.EXPERT_DURABILITY_BONUS.get();
    }

    @Override
    public double WELL_DURABILITY_BONUS() {
        return ServerConfig.WELL_DURABILITY_BONUS.get();
    }

    @Override
    public double POOR_DURABILITY_BONUS() {
        return ServerConfig.POOR_DURABILITY_BONUS.get();
    }

    @Override
    public double MASTER_MINING_SPEED_BONUS() {
        return ServerConfig.MASTER_MINING_SPEED_BONUS.get();
    }

    @Override
    public double PERFECT_MINING_SPEED_BONUS() {
        return ServerConfig.PERFECT_MINING_SPEED_BONUS.get();
    }

    @Override
    public double EXPERT_MINING_SPEED_BONUS() {
        return ServerConfig.EXPERT_MINING_SPEED_BONUS.get();
    }

    @Override
    public double WELL_MINING_SPEED_BONUS() {
        return ServerConfig.WELL_MINING_SPEED_BONUS.get();
    }

    @Override
    public double POOR_MINING_SPEED_BONUS() {
        return ServerConfig.POOR_MINING_SPEED_BONUS.get();
    }

    @Override
    public boolean GET_ROCK_USING_FLINT() {
        return ServerConfig.GET_ROCK_USING_FLINT.get();
    }

    @Override
    public double ROCK_DROPPING_CHANCE() {
        return ServerConfig.ROCK_DROPPING_CHANCE.get();
    }

    @Override
    public double FLINT_BREAKING_CHANCE() {
        return ServerConfig.FLINT_BREAKING_CHANCE.get();
    }

    @Override
    public boolean ENABLE_FLETCHING_RECIPES() {
        return ServerConfig.ENABLE_FLETCHING_RECIPES.get();
    }

    @Override
    public boolean ENABLE_LOOT_QUALITY() {
        return ServerConfig.ENABLE_LOOT_QUALITY.get();
    }

    @Override
    public int QUALITY_WEIGHT_POOR() {
        return ServerConfig.QUALITY_WEIGHT_POOR.get();
    }

    @Override
    public int QUALITY_WEIGHT_WELL() {
        return ServerConfig.QUALITY_WEIGHT_WELL.get();
    }

    @Override
    public int QUALITY_WEIGHT_EXPERT() {
        return ServerConfig.QUALITY_WEIGHT_EXPERT.get();
    }

    @Override
    public int QUALITY_WEIGHT_PERFECT() {
        return ServerConfig.QUALITY_WEIGHT_PERFECT.get();
    }

    @Override
    public int QUALITY_WEIGHT_MASTER() {
        return ServerConfig.QUALITY_WEIGHT_MASTER.get();
    }

    @Override
    public List<? extends List<?>> CASTING_TOOL_TYPES() {
        return ServerConfig.CASTING_TOOL_TYPES.get();
    }

    @Override
    public List<? extends String> MATERIAL_TYPES() {
        return ServerConfig.MATERIAL_TYPES.get();
    }

    @Override
    public List<? extends List<?>> MATERIAL_SETTING() {
        return ServerConfig.MATERIAL_SETTING.get();
    }

    @Override
    public boolean ENABLE_CASTING() {
        return ServerConfig.ENABLE_CASTING.get();
    }

    @Override
    public int DEFAULT_ZONE_STARTING_SIZE() {
        return ServerConfig.DEFAULT_ZONE_STARTING_SIZE.get();
    }

    @Override
    public double DEFAULT_ZONE_SHRINK_FACTOR() {
        return ServerConfig.DEFAULT_ZONE_SHRINK_FACTOR.get();
    }

    @Override
    public int DEFAULT_MIN_PERFECT_ZONE() {
        return ServerConfig.DEFAULT_MIN_PERFECT_ZONE.get();
    }

    @Override
    public double DEFAULT_ARROW_SPEED() {
        return ServerConfig.DEFAULT_ARROW_SPEED.get();
    }

    @Override
    public double DEFAULT_ARROW_SPEED_INCREASE() {
        return ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get();
    }

    @Override
    public double DEFAULT_MAX_ARROW_SPEED() {
        return ServerConfig.DEFAULT_MAX_ARROW_SPEED.get();
    }

    @Override
    public int POOR_ZONE_STARTING_SIZE() {
        return ServerConfig.POOR_ZONE_STARTING_SIZE.get();
    }

    @Override
    public int POOR_MIN_PERFECT_ZONE() {
        return ServerConfig.POOR_MIN_PERFECT_ZONE.get();
    }

    @Override
    public double POOR_ZONE_SHRINK_FACTOR() {
        return ServerConfig.POOR_ZONE_SHRINK_FACTOR.get();
    }

    @Override
    public double POOR_ARROW_SPEED() {
        return ServerConfig.POOR_ARROW_SPEED.get();
    }

    @Override
    public double POOR_ARROW_SPEED_INCREASE() {
        return ServerConfig.POOR_ARROW_SPEED_INCREASE.get();
    }

    @Override
    public double POOR_MAX_ARROW_SPEED() {
        return ServerConfig.POOR_MAX_ARROW_SPEED.get();
    }

    @Override
    public int WELL_ZONE_STARTING_SIZE() {
        return ServerConfig.WELL_ZONE_STARTING_SIZE.get();
    }

    @Override
    public double WELL_ZONE_SHRINK_FACTOR() {
        return ServerConfig.WELL_ZONE_SHRINK_FACTOR.get();
    }

    @Override
    public int WELL_MIN_PERFECT_ZONE() {
        return ServerConfig.WELL_MIN_PERFECT_ZONE.get();
    }

    @Override
    public double WELL_ARROW_SPEED() {
        return ServerConfig.WELL_ARROW_SPEED.get();
    }

    @Override
    public double WELL_ARROW_SPEED_INCREASE() {
        return ServerConfig.WELL_ARROW_SPEED_INCREASE.get();
    }

    @Override
    public double WELL_MAX_ARROW_SPEED() {
        return ServerConfig.WELL_MAX_ARROW_SPEED.get();
    }

    @Override
    public int EXPERT_ZONE_STARTING_SIZE() {
        return ServerConfig.EXPERT_ZONE_STARTING_SIZE.get();
    }

    @Override
    public double EXPERT_ZONE_SHRINK_FACTOR() {
        return ServerConfig.EXPERT_ZONE_SHRINK_FACTOR.get();
    }

    @Override
    public int EXPERT_MIN_PERFECT_ZONE() {
        return ServerConfig.EXPERT_MIN_PERFECT_ZONE.get();
    }

    @Override
    public double EXPERT_ARROW_SPEED() {
        return ServerConfig.EXPERT_ARROW_SPEED.get();
    }

    @Override
    public double EXPERT_ARROW_SPEED_INCREASE() {
        return ServerConfig.EXPERT_ARROW_SPEED_INCREASE.get();
    }

    @Override
    public double EXPERT_MAX_ARROW_SPEED() {
        return ServerConfig.EXPERT_MAX_ARROW_SPEED.get();
    }

    @Override
    public int PERFECT_ZONE_STARTING_SIZE() {
        return ServerConfig.PERFECT_ZONE_STARTING_SIZE.get();
    }

    @Override
    public double PERFECT_ZONE_SHRINK_FACTOR() {
        return ServerConfig.PERFECT_ZONE_SHRINK_FACTOR.get();
    }

    @Override
    public int PERFECT_MIN_PERFECT_ZONE() {
        return ServerConfig.PERFECT_MIN_PERFECT_ZONE.get();
    }

    @Override
    public double PERFECT_ARROW_SPEED() {
        return ServerConfig.PERFECT_ARROW_SPEED.get();
    }

    @Override
    public double PERFECT_ARROW_SPEED_INCREASE() {
        return ServerConfig.PERFECT_ARROW_SPEED_INCREASE.get();
    }

    @Override
    public double PERFECT_MAX_ARROW_SPEED() {
        return ServerConfig.PERFECT_MAX_ARROW_SPEED.get();
    }

    @Override
    public int MASTER_ZONE_STARTING_SIZE() {
        return ServerConfig.MASTER_ZONE_STARTING_SIZE.get();
    }

    @Override
    public double MASTER_ZONE_SHRINK_FACTOR() {
        return ServerConfig.MASTER_ZONE_SHRINK_FACTOR.get();
    }

    @Override
    public int MASTER_MIN_PERFECT_ZONE() {
        return ServerConfig.MASTER_MIN_PERFECT_ZONE.get();
    }

    @Override
    public double MASTER_ARROW_SPEED() {
        return ServerConfig.MASTER_ARROW_SPEED.get();
    }

    @Override
    public double MASTER_ARROW_SPEED_INCREASE() {
        return ServerConfig.MASTER_ARROW_SPEED_INCREASE.get();
    }

    @Override
    public double MASTER_MAX_ARROW_SPEED() {
        return ServerConfig.MASTER_MAX_ARROW_SPEED.get();
    }

    @Override
    public int FIRED_CAST_DURABILITY() {
        return ServerConfig.FIRED_CAST_DURABILITY.get();
    }

    @Override
    public boolean EXPERT_ABOVE_INCREASE_BLUEPRINT() {
        return ServerConfig.EXPERT_ABOVE_INCREASE_BLUEPRINT.get();
    }

    @Override
    public boolean PLAYER_AUTHOR_TOOLTIPS() {
        return ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get();
    }

    @Override
    public boolean ENABLE_STONE_ANVIL_BREAKING() {
        return ServerConfig.ENABLE_STONE_ANVIL_BREAKING.get();
    }

    @Override
    public boolean ENABLE_CREATIVE_TAB_ITEMS() {
        return ServerConfig.ENABLE_CREATIVE_TAB_ITEMS.get();
    }

    @Override
    public boolean ENABLE_QUALITY_BREAK_SYSTEM() {
        return ServerConfig.ENABLE_QUALITY_BREAK_SYSTEM.get();
    }

    @Override
    public List<? extends String> QUALITY_BREAK_BLACKLIST() {
        return ServerConfig.QUALITY_BREAK_BLACKLIST.get();
    }

    @Override
    public double BREAK_CHANCE_POOR() {
        return ServerConfig.BREAK_CHANCE_POOR.get();
    }

    @Override
    public double BREAK_CHANCE_WELL() {
        return ServerConfig.BREAK_CHANCE_WELL.get();
    }

    @Override
    public double BREAK_CHANCE_EXPERT() {
        return ServerConfig.BREAK_CHANCE_EXPERT.get();
    }

    @Override
    public double BREAK_CHANCE_PERFECT() {
        return ServerConfig.BREAK_CHANCE_PERFECT.get();
    }

    @Override
    public double BREAK_CHANCE_MASTER() {
        return ServerConfig.BREAK_CHANCE_MASTER.get();
    }
}