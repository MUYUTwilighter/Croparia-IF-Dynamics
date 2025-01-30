package cool.muyucloud.croparia.dynamics.api.repo;

import java.lang.annotation.Inherited;

@Inherited
@SuppressWarnings("unused")
public @interface RepeatableUnreliable {
    Unreliable[] value();
}
