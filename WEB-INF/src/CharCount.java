import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**	Define annotation interface CharCount.
 *	If both params gets defaults, Validator should through error.
 *	Only applies to String; If not String, Validator should through error.
 */

@Retention(RetentionPolicy.RUNTIME)
// Process this annotation at runtime

@Target(ElementType.FIELD)
// This is an annotation on a field (attribute)

public @interface CharCount {
	public int min() default 0;
	public int max() default 0;
}