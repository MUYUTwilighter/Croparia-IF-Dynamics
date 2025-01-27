package cool.muyucloud.croparia.dynamics.forge.mixin;


import cool.muyucloud.croparia.dynamics.api.forge.FluidCompositeImpl;
import cool.muyucloud.croparia.dynamics.api.forge.FluidUnitImpl;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepo;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepoProvider;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnstableApiUsage")
@Mixin(CapabilityProvider.class)
public abstract class CapabilityProviderMixin {
    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true, remap = false)
    public void onGetCapability(@NotNull Capability<Object> cap, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<Object>> cir) {
        if (this instanceof FluidRepoProvider provider) {
            cir.setReturnValue(LazyOptional.of(() -> {
                FluidRepo repo = provider.get(
                    provider.getLevel(), provider.getBlockPos(),
                    provider.getLevel().getBlockState(provider.getBlockPos()), null, side
                );
                if (repo instanceof FluidCompositeImpl composite) {
                    return composite;
                } else if (repo instanceof FluidUnitImpl unit) {
                    return unit;
                } else {
                    throw new IllegalArgumentException("Unsupported fluid repo: " + repo + ", please extend either FluidCompositeImpl or FluidUnitImpl");
                }
            }));
        }
    }
}
