package net.stirdrem.overgeared.fabric.mixin;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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
    public abstract @Nullable T getValue(@Nullable Identifier id);

    @Shadow
    public abstract Optional<Holder.Reference<T>> get(ResourceKey<T> key);

    @Inject(method = "getValue(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;", at = @At("RETURN"), cancellable = true)
    private void overgeared$aliasGetValue(@Nullable Identifier id, CallbackInfoReturnable<T> cir) {
        if (id != null && cir.getReturnValue() == null) {
            Identifier alias = RegistryAliases.get(key(), id);
            if (alias != null) cir.setReturnValue(getValue(alias));
        }
    }

    @Inject(method = "getValue(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", at = @At("RETURN"), cancellable = true)
    private void overgeared$aliasGetValueByKey(@Nullable ResourceKey<T> key, CallbackInfoReturnable<T> cir) {
        if (key != null && cir.getReturnValue() == null) {
            Identifier alias = RegistryAliases.get(key(), key.identifier());
            if (alias != null) cir.setReturnValue(getValue(alias));
        }
    }

    @Inject(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void overgeared$aliasHolder(ResourceKey<T> key, CallbackInfoReturnable<Optional<Holder.Reference<T>>> cir) {
        if (cir.getReturnValue().isEmpty()) {
            Identifier alias = RegistryAliases.get(key(), key.identifier());
            if (alias != null) cir.setReturnValue(get(ResourceKey.create(key(), alias)));
        }
    }

    @Inject(method = "get(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void overgeared$aliasHolderById(Identifier id, CallbackInfoReturnable<Optional<Holder.Reference<T>>> cir) {
        if (cir.getReturnValue().isEmpty()) {
            Identifier alias = RegistryAliases.get(key(), id);
            if (alias != null) cir.setReturnValue(get(ResourceKey.create(key(), alias)));
        }
    }

    @Inject(method = "containsKey(Lnet/minecraft/resources/Identifier;)Z", at = @At("RETURN"), cancellable = true)
    private void overgeared$aliasContains(Identifier id, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && RegistryAliases.get(key(), id) != null) {
            cir.setReturnValue(true);
        }
    }
}
