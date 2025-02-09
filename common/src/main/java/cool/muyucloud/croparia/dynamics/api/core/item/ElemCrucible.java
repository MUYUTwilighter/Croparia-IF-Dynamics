package cool.muyucloud.croparia.dynamics.api.core.item;

import net.minecraft.world.item.Item;

public class ElemCrucible extends Item {
    private final int tier;

    public ElemCrucible(int tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return this.tier;
    }
}
