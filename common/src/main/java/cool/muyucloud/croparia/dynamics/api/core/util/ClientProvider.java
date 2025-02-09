package cool.muyucloud.croparia.dynamics.api.core.util;

import cool.muyucloud.croparia.dynamics.api.elenet.ElenetAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

@SuppressWarnings("unused")
public class ClientProvider {
    public static Minecraft get() {
        return Minecraft.getInstance();
    }

    public static Optional<BlockEntity> getBlockEntity(ElenetAddress address) {
        try (Level world = get().level) {
            if (world != null && world.dimension().location() == address.worldId()) {
                return Optional.ofNullable(world.getBlockEntity(address.pos()));
            }
        } catch (Throwable ignored) {
        }
        return Optional.empty();
    }
}
