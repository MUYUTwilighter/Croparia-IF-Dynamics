package cool.muyucloud.croparia.dynamics.api.elenet;

import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public interface ElenetNode extends ElenetAccess {
    default <T> void refreshNetwork(TypeToken<T> token) {
        if (this.getElenets().get(token) == null) {
            this.findRegisteredNode(token).ifPresentOrElse(node -> {
                if (!node.resonateNode(this.getAddress(), this, token)) {
                    throw new IllegalStateException("Failed to resonate node %s with %s".formatted(this.getAddress(), node.getAddress()));
                }
            }, () -> this.initNetwork(token));
        }
    }

    default <T> Optional<ElenetNode> findRegisteredNode(TypeToken<T> token) {
        @NotNull ElenetAddress address = this.getAddress();
        int range = this.getRange(token);
        BlockPos lower = address.pos().offset(-range, -range, -range);
        BlockPos upper = address.pos().offset(range, range, range);
        for (int x = lower.getX(); x < upper.getX(); ++x) {
            for (int y = lower.getY(); y < upper.getY(); ++y) {
                for (int z = lower.getZ(); z < upper.getZ(); ++z) {
                    try (Level world = address.world()) {
                        BlockEntity be = world.getBlockEntity(new BlockPos(x, y, z));
                        if (be instanceof ElenetNode n && n.isTypeValid(token)) {
                            return Optional.of(n);
                        }
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
        }
        return Optional.empty();
    }

    <T> void initNetwork(TypeToken<T> token);


    Map<TypeToken<?>, Elenet<?>> getElenets();

    <T> void setNetwork(Elenet<T> elenet);

    <T> int getRange(TypeToken<T> token);

    @SuppressWarnings("unchecked")
    default <T> Optional<Elenet<T>> getElenet(TypeToken<T> token) {
        Elenet<?> elenet = this.getElenets().get(token);
        if (elenet.getType() == token) {
            return Optional.of((Elenet<T>) elenet);
        } else {
            return Optional.empty();
        }
    }

    <T> Map<ElenetAddress, ElenetNode> resonatedNodesOfType(TypeToken<T> token);

    <T> Map<ElenetAddress, ElenetPeer> resonatedPeersOfType(TypeToken<T> token);

    <T> boolean resonatePeer(ElenetAddress address, ElenetPeer peer, TypeToken<T> token);

    boolean isolatePeer(ElenetAddress address);

    <T> boolean resonateNode(ElenetAddress address, ElenetNode node, TypeToken<T> token);

    boolean isolateNode(ElenetAddress address);
}
