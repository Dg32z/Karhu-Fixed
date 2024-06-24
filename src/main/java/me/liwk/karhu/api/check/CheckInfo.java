/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.api.check;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value=RetentionPolicy.RUNTIME)
public @interface CheckInfo {
    public String name();

    public String desc() default "";

    public Category category();

    public boolean silent() default false;

    public boolean experimental();

    public SubCategory subCategory();

    public String credits() default "";

    public boolean subCheck() default false;
}

