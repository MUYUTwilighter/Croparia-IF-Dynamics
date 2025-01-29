package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

/**
 * Unified fluid interface for {@link net.minecraft.world.level.block.entity.BlockEntity}.<br>
 * It is used to connect your customized {@link FluidRepo} to the fluid API from fabric / forge.<br>
 *
 * You need to implement {@link FluidRepoProvider} in your {@code BlockEntity}.<br>
 * If you want to make it work for fabric, use {@link FluidRepoProvider#register(FluidRepoProvider)}
 * to register your {@code BlockEntity}.
 * */
public class FluidAgent implements FluidRepo {
    /**
     * Create a fluid agent from your customized {@link FluidRepo}. <br>
     * You should only create {@link FluidAgent} from this method,
     * and the implemented {@link FluidAgent} from fabric / forge module is returned.<br>
     * <b>DO NOT EXTEND INSTANTIATE THIS CLASS UNLESS YOU KNOW WHAT YOU ARE DOING</b>
     *
     * @param repo the fluid repo
     * @return the fluid agent
     * */
    @ExpectPlatform
    public static FluidAgent of(Supplier<FluidRepo> repo) {
        throw new AssertionError("Not implemented");
    }

    private final FluidRepo repo;

    protected FluidAgent(Supplier<FluidRepo> repo) {
        this.repo = repo.get();
    }

    public FluidRepo get() {
        return this.repo;
    }

    @Override
    public int size() {
        return this.get().size();
    }

    @Override
    public boolean isEmpty(int i) {
        return this.get().isEmpty(i);
    }

    @Override
    public boolean canConsume(int i, Fluid fluid, long amount) {
        return this.get().canConsume(i, fluid, amount);
    }

    @Override
    public boolean canConsume(Fluid fluid, long amount) {
        return this.get().canConsume(fluid, amount);
    }

    @Override
    public boolean canAccept(int i, Fluid fluid, long amount) {
        return this.get().canAccept(i, fluid, amount);
    }

    @Override
    public boolean canAccept(Fluid fluid, long amount) {
        return this.get().canAccept(fluid, amount);
    }

    @Override
    public long consume(int i, Fluid fluid, long amount) {
        return this.get().consume(i, fluid, amount);
    }

    @Override
    public long consume(Fluid fluid, long amount) {
        return this.get().consume(fluid, amount);
    }

    @Override
    public long accept(int i, Fluid fluid, long amount) {
        return this.get().accept(i, fluid, amount);
    }

    @Override
    public long accept(Fluid fluid, long amount) {
        return this.get().accept(fluid, amount);
    }

    @Override
    public long spaceFor(int i, Fluid fluid) {
        return this.get().spaceFor(i, fluid);
    }

    @Override
    public long spaceFor(Fluid fluid) {
        return this.get().spaceFor(fluid);
    }

    @Override
    public long capacityFor(int i, Fluid fluid) {
        return this.get().capacityFor(i, fluid);
    }

    @Override
    public long capacityFor(Fluid fluid) {
        return this.get().capacityFor(fluid);
    }

    @Override
    public long amountFor(int i, Fluid fluid) {
        return this.get().amountFor(i, fluid);
    }

    @Override
    public long amountFor(Fluid fluid) {
        return this.get().amountFor(fluid);
    }

    @Override
    public Fluid fluidFor(int i) {
        return this.get().fluidFor(i);
    }

    @Override
    public void load(CompoundTag tag) {
        this.get().load(tag);
    }

    @Override
    public void save(CompoundTag tag) {
        this.get().save(tag);
    }
}
