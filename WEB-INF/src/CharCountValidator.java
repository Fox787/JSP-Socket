import java.lang.reflect.*;
import java.lang.annotation.Annotation;

public class CharCountValidator extends Validator {

	private static CharCount charCount;

	public void applyRule(Annotation annotation, Object fieldValue, Class<?> fieldType) throws Exception {
		charCount = (CharCount) annotation;

		/**
		 * developer/coder throws for potential annotating errors
		 */
		if (!fieldType.equals(String.class))
			throw new ValidationException(" is not of type String; Annotation @CharCount can only be applied to fields of type String.");

		if (charCount.min() < 0 || charCount.max() < 0)
			throw new ValidationException(" Annotation @CharCount parameters cannot be less than zero.");

		if (charCount.min() == 0 && charCount.max() == 0)
			throw new ValidationException(" Annotation @CharCount parameters min() and max() cannot both be zero (their default values). At least one of these parameters has to set to an integer greater than zero.");

		String fValue = fieldValue.toString();
		int fieldLength = fValue.length();

		boolean valid = true;
		String message = "";

		/**
		 * user relevant error messages
		 */
		if (charCount.min() > 0 && charCount.max() > 0) {
			if (fieldLength < charCount.min() || fieldLength > charCount.max()) {
				valid = false;
				if (charCount.min() == charCount.max())
					message = " character count (length) has to be exactly "  + charCount.min();
				else {
					message = " character count (length) has to be between "  + charCount.min() + " and " + charCount.max();
					message += " inclusive. Field character count is " + fieldLength + ", containing \"" + fValue + "\".";
				}
			}
		}

		if (charCount.max() == 0) {
			if (fieldLength < charCount.min()) {
				valid = false;
				message = " character count (length) has to be a minimum of "  + charCount.min();
				message += ". Field character count is " + fieldLength + ", containing \"" + fValue + "\".";
			}
		}

		if (charCount.min() == 0) {
			if (fieldLength > charCount.max()) {
				valid = false;
				message = " character count (length) has to be a maximum of "  + charCount.max();
				message += ". Field character count is " + fieldLength + ", containing \"" + fValue + "\".";
			}
		}
		
		if (!valid) throw new ValidationException(message);
	}
}