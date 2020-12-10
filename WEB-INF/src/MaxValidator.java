import java.lang.reflect.*;
import java.lang.annotation.Annotation;

public class MaxValidator extends Validator {

	private static Max max;

	public void applyRule(Annotation annotation, Object fieldValue, Class<?> fieldType) throws Exception {
		/**
		 * developer/coder throws for potential annotating errors
		 */
		if (!isReflectedAsNumber(fieldType))
			throw new ValidationException(" is not a primitive number type or a subclass of Number. Annotation @Max cannot be applied to non-number types.");

		max = (Max) annotation;
		
		double fValue = Double.parseDouble(fieldValue.toString());

		boolean valid = max.inclusive() ? fValue <= max.value(): fValue < max.value();

		if (!valid) {
			String message = " must be greater than ";
			String orEquals = max.inclusive() ? "(or equals to) " : ""; 
			message +=  orEquals + max.value() + ".";
			throw new ValidationException(message);
		}	
	}
}