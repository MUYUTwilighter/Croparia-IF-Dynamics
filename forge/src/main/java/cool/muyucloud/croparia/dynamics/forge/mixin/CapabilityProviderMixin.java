package cool.muyucloud.croparia.dynamics.forge.mixin;


import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidAgent;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepoProvider;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
@Mixin(CapabilityProvider.class)
public abstract class CapabilityProviderMixin {
    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true, remap = false)
    public void onGetCapability(@NotNull Capability<Object> cap, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<Object>> cir) {
        if (this instanceof FluidRepoProvider provider && Objects.equals(cap, ForgeCapabilities.FLUID_HANDLER)) {
            FluidAgent agent = provider.fluidAgent(side);
            if (agent == null) {
                cir.setReturnValue(LazyOptional.empty());
            } else {
                cir.setReturnValue(LazyOptional.of(() -> agent));
            }
            cir.cancel();
        }
    }
}
