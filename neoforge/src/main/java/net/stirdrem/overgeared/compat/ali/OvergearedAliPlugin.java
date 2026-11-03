package net.stirdrem.overgeared.compat.ali;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.loot.AddItemModifier;

@AliEntrypoint
public class OvergearedAliPlugin implements IGlobalLootModifierPlugin {
    @Override
    public String getModId() {
        return OvergearedMod.MOD_ID;
    }

    @Override
    public void registerGlobalLootModifier(IRegistry registry) {
        registry.registerGlobalLootModifier(AddItemModifier.class, OvergearedGlm::getAddItemModifier);
    }
}