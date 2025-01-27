package cool.muyucloud.croparia.dynamics.api.repo.fluid.fabric;

import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidComposite;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepoProvider;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidRepo;
import cool.muyucloud.croparia.dynamics.api.repo.fluid.FluidUnit;
import cool.muyucloud.croparia.dynamics.api.RepoFlag;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public class FluidRepoImpl {
    public static FluidComposite composite(boolean divide, @NotNull FluidRepo... children) {
        return new FluidCompositeImpl(divide, children);
    }

    @SafeVarargs
    public static FluidUnit unit(long capacity, RepoFlag flag, @NotNull Predicate<Fluid>... predicates) {
        return new FluidUnitImpl(capacity, flag, predicates);
    }

    public static void register(FluidRepoProvider registration) {
        FluidStorage.SIDED.registerFallback((world, pos, state, blockEntity, context) -> {
            FluidRepo repo = registration.get(world, pos, state, blockEntity, context);
            if (repo instanceof FluidCompositeImpl composite) {
                return composite;
            } else if (repo instanceof FluidUnitImpl unit) {
                return unit;
            }
            throw new NotImplementedException("Unsupported fluid repo: " + repo + ", please extend either FluidCompositeImpl or FluidUnitImpl");
        });
    }
}
