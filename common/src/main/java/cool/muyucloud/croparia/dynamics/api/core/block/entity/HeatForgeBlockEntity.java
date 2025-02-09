package cool.muyucloud.croparia.dynamics.api.core.block.entity;

import cool.muyucloud.croparia.dynamics.api.core.recipe.type.EfrType;
import cool.muyucloud.croparia.dynamics.api.repo.BurningFuel;
import cool.muyucloud.croparia.dynamics.api.repo.FuelRepo;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemUnit;
import cool.muyucloud.croparia.dynamics.api.resource.type.Heat;
import dev.architectury.registry.fuel.FuelRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class HeatForgeBlockEntity extends ElemForgeBlockEntity<Heat> {
    private final ItemUnit fuelSource = new ItemUnit(item -> FuelRegistry.get(item.toStack()) > 0, 64);
    private final BurningFuel burningFuel = new BurningFuel();

    public HeatForgeBlockEntity(Supplier<BlockEntityType<?>> beType, BlockPos pos, BlockState state, EfrType recipeType, int maxLevel) {
        super(beType, pos, state, recipeType, maxLevel);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.fuelSource.load(nbt.getCompound("fuel_source"));
        this.burningFuel.load(nbt.getCompound("burning_fuel"));
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        CompoundTag fuelSourceTag = new CompoundTag();
        compoundTag.put("fuel_source", fuelSourceTag);
        this.fuelSource.save(fuelSourceTag);
        CompoundTag burningFuelTag = new CompoundTag();
        compoundTag.put("burning_fuel", burningFuelTag);
        this.burningFuel.save(burningFuelTag);
    }

    @Override
    public void tick(MinecraftServer server) {
        super.tick(server);
        if (burningFuel.isEmpty() && this.getRecipeProcessor().isReady()) {
            Item source = this.fuelSource.getResource().getItem();
            long amount = this.fuelSource.consume(1);
            if (amount > 0) {
                this.burningFuel.refuel(source);
            }
        } else {
            burningFuel.burn(1);
        }
    }

    @Override
    public FuelRepo<Heat> getFuelUnit() {
        return burningFuel;
    }
}
