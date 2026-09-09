package bot.script;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Identifies a {@link Script} implementation inside a script jar. Read without
 * initializing the class (the loader uses {@code Class.forName(name, false, loader)}). */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScriptManifest {
    String name();
    String version() default "1.0";
    String description() default "";
    String author() default "";
}
