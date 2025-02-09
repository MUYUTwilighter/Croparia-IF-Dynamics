package cool.muyucloud.croparia.dynamics.api.resource.type;

import cool.muyucloud.croparia.CropariaIf;
import cool.muyucloud.croparia.dynamics.api.resource.ResourceType;
import cool.muyucloud.croparia.dynamics.api.resource.TypeToken;

public class Heat implements ResourceType {
    public static final Heat EMPTY = new Heat();
    public static final TypeToken<Heat> TYPE = TypeToken.registerOrThrow(CropariaIf.of("heat"), EMPTY);

    @Override
    public TypeToken<?> getType() {
        return TYPE;
    }
}
