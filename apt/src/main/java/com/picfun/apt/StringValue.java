package com.picfun.apt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Secret
 * @since 2019/10/9
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface StringValue {

    String value();

}
