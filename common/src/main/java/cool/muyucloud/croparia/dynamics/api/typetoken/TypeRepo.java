package cool.muyucloud.croparia.dynamics.api.typetoken;

import java.util.Set;

public interface TypeRepo {
    Set<TypeToken<?>> getTypes();

    default boolean isTypeValid(TypeToken<?> type) {
        return this.getTypes().contains(type);
    }
}
