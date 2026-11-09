package net.stirdrem.overgeared.fabric.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.fabric.RegistryAliases;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Resolves renamed registry entries (see {@link RegistryAliases}) so worlds saved with an old id still load.
 * Only runs when the normal lookup found nothing.
 */
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryAliasMixin<T> {
    @Shadow
    public abstract ResourceKey<? extends Registry<T>> key();

    @Shadow
    public abstract T get(ResourceLocation id);

    @Shadow
    public abstract Optional<Holder.Reference<T>> getHolder(ResourceKey<T> key);

    @Inject(method = "get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;", at = @At("RETURN"), cancellable = true)
    private void overgeared$aliasGet(ResourceLocation id, CallbackInfoReturnable<T> cir) {
        if (cir.getReturnValue() == null) {
            ResourceLocation alias = RegistryAliases.get(key(), id);
            if (alias != null) cir.setReturnValue(get(alias));
        }
    }

    @Inject(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", at = @At("RETURN"), cancellable = true)
    private void overgeared$aliasGetByKey(ResourceKey<T> key, CallbackInfoReturnable<T> cir) {
        if (cir.getReturnValue() == null) {
            ResourceLocation alias = RegistryAliases.get(key(), key.location());
            if (alias != null) cir.setReturnValue(get(alias));
        }
    }

    @Inject(method = "getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void overgeared$aliasHolder(ResourceKey<T> key, CallbackInfoReturnable<Optional<Holder.Reference<T>>> cir) {
        if (cir.getReturnValue().isEmpty()) {
            ResourceLocation alias = RegistryAliases.get(key(), key.location());
            if (alias != null) cir.setReturnValue(getHolder(ResourceKey.create(key(), alias)));
        }
    }

    @Inject(method = "getHolder(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void overgeared$aliasHolderById(ResourceLocation id, CallbackInfoReturnable<Optional<Holder.Reference<T>>> cir) {
        if (cir.getReturnValue().isEmpty()) {
            ResourceLocation alias = RegistryAliases.get(key(), id);
            if (alias != null) cir.setReturnValue(getHolder(ResourceKey.create(key(), alias)));
        }
    }

    @Inject(method = "containsKey(Lnet/minecraft/resources/ResourceLocation;)Z", at = @At("RETURN"), cancellable = true)
    private void overgeared$aliasContains(ResourceLocation id, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && RegistryAliases.get(key(), id) != null) {
            cir.setReturnValue(true);
        }
    }
}
