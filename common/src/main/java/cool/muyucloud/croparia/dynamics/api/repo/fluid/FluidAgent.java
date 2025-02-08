package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import cool.muyucloud.croparia.dynamics.api.repo.Repo;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.function.Supplier;

/**
 * Unified fluid interface for {@link net.minecraft.world.level.block.entity.BlockEntity}.<br>
 * It is used to connect your customized {@link Repo<FluidSpec>} to the fluid API from fabric / forge.<br>
 * <p>
 * You need to implement {@link Repo<FluidSpec>Provider} in your {@code BlockEntity}.<br>
 * If you want to make it work for fabric, use {@link Repo<FluidSpec>Provider#register(Repo<FluidSpec>Provider)}
 * to register your {@code BlockEntity}.
 * <p>
 * Use {@link #of(Supplier)} to create a {@link FluidAgent}. <br>
 * <b>DO NOT INSTANTIATE OR EXTEND THIS CLASS UNLESS YOU KNOW WHAT YOU ARE DOING</b>
 * </p>
 */
public abstract class FluidAgent implements Repo<FluidSpec> {
    /**
     * Create a fluid agent from your customized {@link Repo<FluidSpec>}. <br>
     * You should only create {@link FluidAgent} from this method,
     * and the implemented {@link FluidAgent} from fabric / forge module is returned.<br>
     *
     * @param repo the fluid repo
     * @return the fluid agent
     */
    @ExpectPlatform
    public static FluidAgent of(Supplier<Repo<FluidSpec>> repo) {
        throw new AssertionError("Not implemented");
    }

    private final Repo<FluidSpec> repo;

    protected FluidAgent(Supplier<Repo<FluidSpec>> repo) {
        this.repo = repo.get();
    }

    @Override
    public TypeToken<FluidSpec> getType() {
        return FluidSpec.TYPE;
    }

    public Repo<FluidSpec> get() {
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
    public FluidSpec resourceFor(int i) {
        return this.get().resourceFor(i);
    }

    @Override
    public long simConsume(FluidSpec fluid, long amount) {
        return this.get().simConsume(fluid, amount);
    }

    @Override
    public long simConsume(int i, FluidSpec resource, long amount) {
        return this.get().simConsume(i, resource, amount);
    }

    @Override
    public long consume(int i, FluidSpec resource, long amount) {
        return this.get().consume(i, resource, amount);
    }

    @Override
    public long consume(FluidSpec resource, long amount) {
        return this.get().consume(resource, amount);
    }

    @Override
    public long simAccept(FluidSpec resource, long amount) {
        return this.get().simAccept(resource, amount);
    }

    @Override
    public long simAccept(int i, FluidSpec resource, long amount) {
        return this.get().simAccept(i, resource, amount);
    }

    @Override
    public long accept(int i, FluidSpec fluid, long amount) {
        return this.get().accept(i, fluid, amount);
    }

    @Override
    public long accept(FluidSpec fluid, long amount) {
        return this.get().accept(fluid, amount);
    }

    @Override
    public long capacityFor(int i, FluidSpec fluid) {
        return this.get().capacityFor(i, fluid);
    }

    @Override
    public long capacityFor(FluidSpec fluid) {
        return this.get().capacityFor(fluid);
    }

    @Override
    public long amountFor(int i, FluidSpec fluid) {
        return this.get().amountFor(i, fluid);
    }

    @Override
    public long amountFor(FluidSpec fluid) {
        return this.get().amountFor(fluid);
    }
}
