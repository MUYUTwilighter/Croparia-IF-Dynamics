package cool.muyucloud.croparia.dynamics.api;

import cool.muyucloud.croparia.dynamics.TagUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Predicate;

public class PredicateFluidTag implements Predicate<Fluid> {
    private final TagKey<Fluid> tag;

    public PredicateFluidTag(ResourceLocation id) {
        this.tag = TagKey.create(BuiltInRegistries.FLUID.key(), id);
    }

    public PredicateFluidTag(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(Fluid fluid) {
        return TagUtil.isIn(tag, fluid);
    }

    public TagKey<Fluid> getTag() {
        return tag;
    }
}