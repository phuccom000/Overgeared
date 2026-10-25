package net.stirdrem.overgeared.compat.ali;

import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.server.LootCount;
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
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, modifier, Arrays.asList(modifier.getConditions()),
                (page, conditions) -> List.of(new IOperation.AddOperation((stack) -> true,
                        addedItem(utils, conditions, modifier.getItem().getDefaultInstance(), modifier.getCount())))));
    }

    private static ItemNode addedItem(IServerUtils utils, List<LootItemCondition> conditions, ItemStack item, int count) {
        NumberExpr countExpr = NumberExpr.constant(count);
        var chance = NodeUtils.getChance(utils, conditions, 1f);
        var tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance,
                LootCount.of(countExpr), NodeUtils.getCountLimit(item), Collections.emptyList(), conditions);

        return new ItemNode(1f, countExpr, item, tooltip.build(), Collections.emptyList(), conditions);
    }
}