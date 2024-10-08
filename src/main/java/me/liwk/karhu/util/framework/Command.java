package me.liwk.karhu.util.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
   String name() default "karhu";

   String[] aliases() default {};

   String permission() default "karhu.staff";

   String description() default "";

   String usage() default "";

   boolean inGameOnly() default true;
}
