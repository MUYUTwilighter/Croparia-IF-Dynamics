package cool.muyucloud.croparia.dynamics.api.elenet;

import cool.muyucloud.croparia.dynamics.api.repo.FuelUnit;
import cool.muyucloud.croparia.dynamics.api.typetoken.Type;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import cool.muyucloud.croparia.dynamics.util.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings({"unused"})
public class ElenetHubImpl<F> implements ElenetHub {
    private static final int FUEL = 4;

    @NotNull
    private transient final FuelUnit<F> fuelUnit;
    @NotNull
    private transient final Provider<Float> fuelEffect;
    @NotNull
    private ElenetAddress address;
    private final Map<TypeToken<?>, Elenet<?>> elenets = new HashMap<>();
    @NotNull
    private final Map<TypeToken<?>, Collection<ElenetAddress>> resonatedHubs = new HashMap<>();
    @NotNull
    private final Map<TypeToken<?>, Collection<ElenetAddress>> resonatedPeers = new HashMap<>();
    private short coverage;
    private boolean removed = false;

    public ElenetHubImpl(@NotNull FuelUnit<F> fuelUnit, @NotNull Provider<Float> fuelEffect, @NotNull ElenetAddress address) {
        this.fuelUnit = fuelUnit;
        this.fuelEffect = fuelEffect;
        this.address = address;
    }

    public void tick() {
        if (this.isIdle()) return;
        this.fuelUnit.burn(this.calcFuel());
    }

    public void setCoverage(short coverage) {
        short oldCov = this.coverage;
        short oldRange = this.getRange();
        this.coverage = coverage;
        this.onCoverageChange(oldCov);
        this.onRangeChange(oldRange);
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public long calcFuel() {
        return (long) Math.max(FUEL * fuelEffect.get(), 1);
    }

    @Override
    public short getCoverage() {
        return this.coverage;
    }

    @Override
    public boolean isIdle() {
        return !removed && fuelUnit.isEnoughFor(this.calcFuel());
    }

    @Override
    public <T extends Type> Optional<Collection<ElenetAddress>> resonatedHubsOfType(TypeToken<T> type) {
        if (this.isTypeValid(type)) return Optional.of(this.resonatedHubs.computeIfAbsent(type, k -> new HashSet<>()));
        else return Optional.empty();
    }

    @Override
    public <T extends Type> Optional<Collection<ElenetAddress>> resonatedPeersOfType(TypeToken<T> type) {
        if (this.isTypeValid(type)) return Optional.of(this.resonatedPeers.computeIfAbsent(type, k -> new HashSet<>()));
        else return Optional.empty();
    }

    @Override
    public Map<TypeToken<?>, Elenet<?>> getElenets() {
        return elenets;
    }

    @Override
    public boolean isAccessibleFrom(@NotNull ElenetAddress address) {
        return !removed && this.address.equals(address);
    }

    @Override
    public @NotNull ElenetAddress getAddress() {
        return address;
    }

    public void setAddress(@NotNull ElenetAddress address) {
        this.address = address;
    }

    @Override
    public Set<TypeToken<?>> getTypes() {
        return Set.of();
    }
}
