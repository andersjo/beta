/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

	abstract public String name();

	abstract public String argument() default "";

	abstract public String usage();

	abstract public boolean required() default false;
}
