package cool.muyucloud.croparia.dynamics.api.repo.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class FluidSpec {
    private Fluid fluid;
    private CompoundTag nbt;

    public FluidSpec(@NotNull Fluid fluid, @Nullable CompoundTag nbt) {
        this.fluid = fluid;
        this.nbt = nbt;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    public void setFluid(Fluid fluid) {
        this.fluid = fluid;
    }

    public void setNbt(CompoundTag nbt) {
        this.nbt = nbt;
    }
}
