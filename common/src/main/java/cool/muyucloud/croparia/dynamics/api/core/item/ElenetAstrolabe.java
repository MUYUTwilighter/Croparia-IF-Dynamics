package cool.muyucloud.croparia.dynamics.api.core.item;

import cool.muyucloud.croparia.dynamics.api.resource.ResourceType;
import cool.muyucloud.croparia.dynamics.api.resource.TypeToken;
import cool.muyucloud.croparia.dynamics.api.resource.TypeTokenAccess;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class ElenetAstrolabe<T extends ResourceType> extends Item implements TypeTokenAccess {
    @NotNull
    private final TypeToken<T> type;

    public ElenetAstrolabe(@NotNull TypeToken<T> type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public @NotNull TypeToken<?> getType() {
        return type;
    }
}
