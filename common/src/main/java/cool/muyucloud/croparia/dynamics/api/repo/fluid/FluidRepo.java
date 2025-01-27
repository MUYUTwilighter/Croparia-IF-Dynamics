package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import cool.muyucloud.croparia.dynamics.api.RepoFlag;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Predicate;

public interface FluidRepo {
    @ExpectPlatform
    static FluidComposite composite(boolean divide, @NotNull FluidRepo... children) {
        throw new AssertionError("Not implemented");
    }

    @SafeVarargs
    @ExpectPlatform
    static FluidUnit unit(long capacity, RepoFlag flag, @NotNull Predicate<Fluid>... predicates) {
        throw new AssertionError("Not implemented");
    }

    @ExpectPlatform
    static void register(FluidRepoProvider registration) {
        throw new AssertionError("Not implemented");
    }

    boolean isEmpty();

    boolean canConsume(Fluid fluid, long amount);

    boolean canAccept(Fluid fluid, long amount);

    long consume(Fluid fluid, long amount);

    long accept(Fluid fluid, long amount);

    long spaceFor(Fluid fluid);

    long capacityFor(Fluid fluid);

    long amountFor(Fluid fluid);

    Iterator<FluidUnit> units();
}
