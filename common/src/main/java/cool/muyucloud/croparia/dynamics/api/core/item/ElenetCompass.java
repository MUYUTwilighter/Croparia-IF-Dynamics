package cool.muyucloud.croparia.dynamics.api.core.item;

import cool.muyucloud.croparia.dynamics.api.resource.ResourceType;
import cool.muyucloud.croparia.dynamics.api.resource.TypeToken;
import cool.muyucloud.croparia.dynamics.api.resource.TypeTokenAccess;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class ElenetCompass<T extends ResourceType> extends Item implements TypeTokenAccess {
    @NotNull
    private final TypeToken<T> type;

    public ElenetCompass(@NotNull TypeToken<T> type) {
        super(new Item.Properties());
        this.type = type;
    }

    @Override
    public @NotNull TypeToken<?> getType() {
        return type;
    }
}
