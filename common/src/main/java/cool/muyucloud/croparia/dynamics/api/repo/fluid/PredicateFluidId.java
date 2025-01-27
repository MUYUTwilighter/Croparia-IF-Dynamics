package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Predicate;

public class PredicateFluidId implements Predicate<Fluid> {
    private final ResourceLocation id;
    private final Fluid resource;

    public PredicateFluidId(ResourceLocation id) {
        this.id = id;
        this.resource = BuiltInRegistries.FLUID.get(id);
    }

    @Override
    public boolean test(Fluid resource) {
        return this.resource == resource;
    }

    public ResourceLocation getId() {
        return id;
    }

    public Fluid getResource() {
        return resource;
    }
}