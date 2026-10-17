package net.stirdrem.overgeared.config;

import java.util.List;

public interface ServerConfigCommon {

    boolean ENABLE_MOD_TOOLTIPS();

    int MAX_ANVIL_DISTANCE();

    int STONE_ANVIL_MAX_USES();

    boolean ENABLE_STONE_TO_ANVIL();

    boolean ENABLE_ANVIL_TO_SMITHING();

    boolean ENABLE_BLUEPRINT_FORGING();

    boolean ENABLE_TIERED_HAMMERS();

    boolean ENABLE_TIER_A();

    boolean ENABLE_TIER_B();

    int HEATED_ITEM_COOLDOWN_TICKS();

    boolean ENABLE_DRAGON_BREATH_RECIPE();

    boolean TIPPING_TOGGLE();

    boolean UPGRADE_ARROW_POTION_TOGGLE();

    int MAX_POTION_TIPPING_USE();

    boolean ENABLE_MINIGAME();

    boolean INGREDIENTS_DEFINE_MAX_QUALITY();

    double MASTER_QUALITY_CHANCE();

    double MASTER_FROM_INGREDIENT_CHANCE();

    double PERFECT_QUALITY_SCORE();

    double EXPERT_QUALITY_SCORE();

    double WELL_QUALITY_SCORE();

    double BASE_DURABILITY_MULTIPLIER();

    List<? extends String> BASE_DURABILITY_BLACKLIST();

    boolean GRINDING_RESTORE_DURABILITY();

    List<? extends String> GRINDING_BLACKLIST();

    double DURABILITY_REDUCE_PER_GRIND();

    double DAMAGE_RESTORE_PER_GRIND();

    double FAIL_ON_WELL_QUALITY_CHANCE();

    double FAIL_ON_EXPERT_QUALITY_CHANCE();

    List<? extends String> AVAILABLE_TOOL_TYPES();

    List<? extends String> HIDDEN_TOOL_TYPES();

    int EXPERT_MAX_USE();

    int WELL_MAX_USE();

    int POOR_MAX_USE();

    double MASTER_DURABILITY_BONUS();

    double PERFECT_DURABILITY_BONUS();

    double EXPERT_DURABILITY_BONUS();

    double WELL_DURABILITY_BONUS();

    double POOR_DURABILITY_BONUS();

    double MASTER_MINING_SPEED_BONUS();

    double PERFECT_MINING_SPEED_BONUS();

    double EXPERT_MINING_SPEED_BONUS();

    double WELL_MINING_SPEED_BONUS();

    double POOR_MINING_SPEED_BONUS();

    boolean GET_ROCK_USING_FLINT();

    double ROCK_DROPPING_CHANCE();

    double FLINT_BREAKING_CHANCE();

    boolean ENABLE_FLETCHING_RECIPES();

    boolean ENABLE_LOOT_QUALITY();

    int QUALITY_WEIGHT_POOR();

    int QUALITY_WEIGHT_WELL();

    int QUALITY_WEIGHT_EXPERT();

    int QUALITY_WEIGHT_PERFECT();

    int QUALITY_WEIGHT_MASTER();

    List<? extends List<?>> CASTING_TOOL_TYPES();

    List<? extends String> MATERIAL_TYPES();

    List<? extends List<?>> MATERIAL_SETTING();

    boolean ENABLE_CASTING();

    int DEFAULT_ZONE_STARTING_SIZE();

    double DEFAULT_ZONE_SHRINK_FACTOR();

    int DEFAULT_MIN_PERFECT_ZONE();

    double DEFAULT_ARROW_SPEED();

    double DEFAULT_ARROW_SPEED_INCREASE();

    double DEFAULT_MAX_ARROW_SPEED();

    int POOR_ZONE_STARTING_SIZE();

    int POOR_MIN_PERFECT_ZONE();

    double POOR_ZONE_SHRINK_FACTOR();

    double POOR_ARROW_SPEED();

    double POOR_ARROW_SPEED_INCREASE();

    double POOR_MAX_ARROW_SPEED();

    int WELL_ZONE_STARTING_SIZE();

    double WELL_ZONE_SHRINK_FACTOR();

    int WELL_MIN_PERFECT_ZONE();

    double WELL_ARROW_SPEED();

    double WELL_ARROW_SPEED_INCREASE();

    double WELL_MAX_ARROW_SPEED();

    int EXPERT_ZONE_STARTING_SIZE();

    double EXPERT_ZONE_SHRINK_FACTOR();

    int EXPERT_MIN_PERFECT_ZONE();

    double EXPERT_ARROW_SPEED();

    double EXPERT_ARROW_SPEED_INCREASE();

    double EXPERT_MAX_ARROW_SPEED();

    int PERFECT_ZONE_STARTING_SIZE();

    double PERFECT_ZONE_SHRINK_FACTOR();

    int PERFECT_MIN_PERFECT_ZONE();

    double PERFECT_ARROW_SPEED();

    double PERFECT_ARROW_SPEED_INCREASE();

    double PERFECT_MAX_ARROW_SPEED();

    int MASTER_ZONE_STARTING_SIZE();

    double MASTER_ZONE_SHRINK_FACTOR();

    int MASTER_MIN_PERFECT_ZONE();

    double MASTER_ARROW_SPEED();

    double MASTER_ARROW_SPEED_INCREASE();

    double MASTER_MAX_ARROW_SPEED();

    int FIRED_CAST_DURABILITY();

    boolean EXPERT_ABOVE_INCREASE_BLUEPRINT();

    boolean PLAYER_AUTHOR_TOOLTIPS();

    boolean ENABLE_STONE_ANVIL_BREAKING();

    boolean ENABLE_CREATIVE_TAB_ITEMS();

    boolean ENABLE_QUALITY_BREAK_SYSTEM();

    List<? extends String> QUALITY_BREAK_BLACKLIST();

    double BREAK_CHANCE_POOR();

    double BREAK_CHANCE_WELL();

    double BREAK_CHANCE_EXPERT();

    double BREAK_CHANCE_PERFECT();

    double BREAK_CHANCE_MASTER();
}