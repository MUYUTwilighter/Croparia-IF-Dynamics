package cool.muyucloud.croparia.dynamics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Suggest the access level of the method
 * */
@SuppressWarnings("unused")
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface SuggestAccess {
    String value() default "protected";
}
