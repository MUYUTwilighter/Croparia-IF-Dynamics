package cool.muyucloud.croparia.dynamics.api;

@SuppressWarnings("unused")
public enum RepoFlag {
    CONSUME(true, false),
    ACCEPT(true, true),
    DUAL(true, true);

    private final boolean consumable;
    private final boolean acceptable;

    RepoFlag(boolean consumable, boolean acceptable) {
        this.consumable = consumable;
        this.acceptable = acceptable;
    }

    public boolean isConsumable() {
        return consumable;
    }

    public boolean isAcceptable() {
        return acceptable;
    }
}
