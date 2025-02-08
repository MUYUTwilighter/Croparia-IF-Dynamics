package cool.muyucloud.croparia.dynamics.api.resource;

import java.util.function.Consumer;

public interface TypeRepo {
    void forEachType(Consumer<TypeToken<?>> consumer);

    boolean isTypeValid(TypeToken<?> type);
}
