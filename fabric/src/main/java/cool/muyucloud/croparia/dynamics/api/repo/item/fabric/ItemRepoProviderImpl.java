package cool.muyucloud.croparia.dynamics.api.repo.item.fabric;

import cool.muyucloud.croparia.dynamics.api.repo.item.ItemAgent;
import cool.muyucloud.croparia.dynamics.api.repo.item.ItemRepoProvider;
import cool.muyucloud.croparia.dynamics.api.repo.item.PlatformItemAgent;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ItemRepoProviderImpl {
    static void register(ItemRepoProvider provider) {
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> {
            ItemAgent agent = provider.itemAgent(direction);
            if (agent == null) return null;
            if (agent instanceof ItemAgentImpl itemAgent) return itemAgent;
            throw new AssertionError("Unknown item agent: " + agent);
        });
    }

    static Optional<PlatformItemAgent> find(Level world, BlockPos pos, Direction direction) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(world, pos, direction);
        if (storage == null) return Optional.empty();
        return Optional.of(FabricItemAgent.of(storage));
    }
}
