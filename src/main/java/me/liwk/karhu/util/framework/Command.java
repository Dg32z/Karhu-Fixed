/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.util.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Command {
    public String name() default "karhu";

    public String[] aliases() default {};

    public String permission() default "karhu.staff";

    public String description() default "";

    public String usage() default "";

    public boolean inGameOnly() default true;
}

