package cool.muyucloud.croparia.dynamics.api.elenet;

import cool.muyucloud.croparia.dynamics.api.typetoken.TypeRepo;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface ElenetAccess extends TypeRepo {
    boolean isAccessibleFrom(@NotNull ElenetAddress address);

    @NotNull
    ElenetAddress getAddress();
}
