package net.stirdrem.overgeared.event;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.Overgeared;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Overgeared.MOD_ID);

    public static void register(IEventBus bus) {
        ATTRIBUTES.register(bus);
    }
}



