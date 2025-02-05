package cool.muyucloud.croparia.dynamics.api.elenet;

import cool.muyucloud.croparia.dynamics.annotation.SuggestAccess;
import cool.muyucloud.croparia.dynamics.api.typetoken.Type;
import cool.muyucloud.croparia.dynamics.api.typetoken.TypeToken;

import java.util.Optional;

@SuppressWarnings("unused")
public interface ElenetPeer extends ElenetAccess {
    /**
     * Get the hub that this peer is resonated with the given type.<br>
     * When implementing, you need to check the availability of the resonance via {@link #isResonanceValid(TypeToken)}.
     * If not valid, return {@link Optional#empty()}.
     */
    <T extends Type> Optional<ElenetHub> resonatedHub(TypeToken<T> type);

    /**
     * Resonate this peer with the given hub, and isolate it from its original hub if type collides.
     */
    <T extends Type> void resonateWith(ElenetHub hub, TypeToken<T> type);

    /**
     * Force isolate this peer from its original hub of the given type.
     */
    <T extends Type> void isolateOfType(TypeToken<T> token);

    /**
     * Consume the specified amount of resource from this peer to the elenet.<br>
     * You should check {@link #canBeServed(TypeToken)} before you invoke this method.<br>
     */
    @SuggestAccess
    <T extends Type> long consume(T resource, long amount);

    /**
     * Accept the specified amount of resource to this peer from the elenet.<br>
     * You should check {@link #canBeServed(TypeToken)} before you invoke this method.<br>
     */
    @SuggestAccess
    <T extends Type> long accept(T resource, long amount);

    default void onRemove() {
        for (TypeToken<?> type : this.getTypes()) {
            this.isolateOfType(type);
            this.resonatedHub(type).ifPresent(hub -> hub.isolate(this, type));
        }
    }

    default <T extends Type> long tryConsume(T resource, long amount) {
        return this.canBeServed(resource.getType()) ? this.consume(resource, amount) : 0;
    }

    default <T extends Type> long tryAccept(T resource, long amount) {
        return this.canBeServed(resource.getType()) ? this.accept(resource, amount) : 0;
    }

    default boolean canBeServed(TypeToken<?> type) {
        return this.resonatedHub(type).map(hub -> hub.canServe(this, type)).orElse(false);
    }

    default boolean isResonanceValid(TypeToken<?> type) {
        return this.resonatedHub(type).map(hub -> hub.canResonate(this)).orElse(false);
    }
}
