package cool.muyucloud.croparia.dynamics.api.network;

@SuppressWarnings("unused")
public interface NetworkAccess<T> {
    long request(T resource, long amount);

    long accept(T resource, long amount);

    boolean isAccessibleFrom(NetworkAddress address);
}
