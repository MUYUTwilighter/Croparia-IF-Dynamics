package cool.muyucloud.croparia.dynamics.api.fabric;

import cool.muyucloud.croparia.dynamics.api.FluidComposite;
import cool.muyucloud.croparia.dynamics.api.FluidRepo;
import cool.muyucloud.croparia.dynamics.api.FluidUnit;
import cool.muyucloud.croparia.dynamics.api.RepoFlag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FluidRepoImpl {
    public static FluidComposite composite(boolean divide, @NotNull FluidRepo... children) {
        return new FluidCompositeImpl(divide, children);
    }

    @SafeVarargs
    public static FluidUnit unit(long capacity, RepoFlag flag, @NotNull Predicate<Fluid>... predicates) {
        return new FluidUnitImpl(capacity, flag, predicates);
    }
}
