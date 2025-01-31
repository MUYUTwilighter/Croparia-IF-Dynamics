package cool.muyucloud.croparia.dynamics.api.repo.item.forge;

import cool.muyucloud.croparia.dynamics.api.repo.item.ItemRepoProvider;
import cool.muyucloud.croparia.dynamics.api.repo.item.PlatformItemAgent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Optional;

public class ItemRepoProviderImpl {
    static void register(ItemRepoProvider provider) {
    }

    static Optional<PlatformItemAgent> find(Level world, BlockPos pos, Direction direction) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be == null) {
            return Optional.empty();
        }
        return be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).map(ForgeItemAgent::of);
    }
}
