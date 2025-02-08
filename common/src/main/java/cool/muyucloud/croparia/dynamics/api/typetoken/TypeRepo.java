package cool.muyucloud.croparia.dynamics.api.typetoken;

import java.util.function.Consumer;

public interface TypeRepo {
    void forEachType(Consumer<TypeToken<?>> consumer);

    boolean isTypeValid(TypeToken<?> type);
}
