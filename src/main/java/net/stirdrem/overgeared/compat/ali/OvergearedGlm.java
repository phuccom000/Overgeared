package net.stirdrem.overgeared.compat.ali;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.stirdrem.overgeared.loot.AddItemModifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OvergearedGlm {
    public static Optional<IPageLootModifier> getAddItemModifier(IServerUtils utils, AddItemModifier modifier) {
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, modifier,
                Arrays.asList(modifier.getConditions()),
                (page, conditions) -> List.of(new IOperation.AddOperation((stack) -> true,
                        addedItem(utils, conditions, modifier.getItem().getDefaultInstance(), 1)))));
    }

    private static ItemNode addedItem(IServerUtils utils, List<LootItemCondition> conditions, ItemStack item, int count) {
        RangeValue countValue = new RangeValue(count);
        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, conditions, 1f);
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance,
                new EnchantedRanges(countValue), Collections.emptyList(), conditions);

        return new ItemNode(1f, countValue, item, tooltip.build(), Collections.emptyList(), conditions);
    }
}