import java.lang.reflect.*;
import java.lang.annotation.Annotation;

public class MinValidator extends Validator {

	private static Min min;

	public void applyRule(Annotation annotation, Object fieldValue, Class<?> fieldType) throws Exception {
		/**
		 * developer/coder throws for potential annotating errors
		 */
		if (!isReflectedAsNumber(fieldType))
			throw new ValidationException(" is not a primitive number type or a subclass of Number. Annotation @Min cannot be applied to non-number types.");

		min = (Min) annotation;
		
		double fValue = Double.parseDouble(fieldValue.toString());

		boolean valid = min.inclusive() ? fValue >= min.value(): fValue > min.value();

		if (!valid) {
			String message = " must be greater than ";
			String orEquals = min.inclusive() ? "(or equals to) " : ""; 
			message +=  orEquals + min.value() + ".";
			throw new ValidationException(message);
		}	
	}
}