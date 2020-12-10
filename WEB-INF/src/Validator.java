import java.lang.reflect.*;
import java.lang.annotation.Annotation;
import java.util.*;

public abstract class Validator {
	public abstract void applyRule(
		Annotation annotation, 
		Object fieldValue, 
		Class<?> fieldType
	) throws Exception;

	public static final String APPLY_RULE_METHOD_NAME = "applyRule";

	private static Map<String, Object> validatorMap = new HashMap<String, Object>();

	// NOTE: this method is static; it could have been placed in any other Class.
	//		 we've put it in this abstract class because it is related to the overall
	//		 validation use case that includes this "Validator" abstract class.
	public static void validate(Object model) throws Exception {
        if (model == null)
            throw new ValidationException(
            	String.join(" ", "Model [", model.getClass().getName(), "] cannot be null")
            );

		List<Field> fields = getInheritedDeclaredFields(model);

		for(Field field: fields) {
			Annotation[] annotations = field.getDeclaredAnnotations();
			
			for(Annotation annotation: annotations) {
				String annotationName = annotation.annotationType().getName();
				String abstractValidatorClassName = Validator.class.getSimpleName();
				/** 
				 *	some convention over configuration
				 *	convention: 
				 * 		if your annotation is named "Min" and we know this abstract class is
				 * 		named "Validator", then your validator rule will have to be implemented 
				 *		in a file named "MinValidator" ("Min" + "Validator" joined/concatenated)
				 *
				 *	Why are we doing this?
				 *		if we have an annotation "Min", based on that annotation, this Framework
				 *		can then find the matching validator rule in a file named "MinValidator".
				 *		This static "validate" method uses Java Reflection to:
				 *			- the current forEach iteration's annotation (find the name in String)
				 *			- this abstract class name (in String)
				 *			- concat them together, annotation name first
				 *			- looks for a class matching the concatenated String
				 *				> this class should extend abstract class "Validator"
				 *			- create an instance of that class 
				 *				> (through it's constructor, retieved using Java Reflection)
				 *			- calls its "applyRule" method (with appropriate arguments)
				 *				> we know the instance of the class has the "applyRule" method
				 *				> because this class extends abstract class "Validator"
				 *				> and this abstract class "Validator" has an abstract "applyRule" method
				 */
				String validatorClassName = String.join("", annotationName, abstractValidatorClassName);

				Class<?> validatorClass;
				try {
					 validatorClass = Class.forName(validatorClassName);
				} catch (Exception exp) {
					throw new ValidationException(
						"Cannot find Validator subclass " + validatorClassName +
						".class to validate targetted field \"" + String.join(".", field.getDeclaringClass().getName(), field.getName()) + 
						"\" annotated with @" + annotationName + ". Was such a subclass defined?"
						);
				}

				if (validatorMap.get(validatorClassName) == null)
					validatorMap.put(validatorClassName, validatorClass.getConstructor().newInstance(new Object[0]));
				Object validatorInstance = validatorMap.get(validatorClassName);
				field.setAccessible(true); // to access private field
				
				Method method = validatorClass.getMethod(
					APPLY_RULE_METHOD_NAME, 
					Annotation.class, 
					Object.class, 
					Class.class
				);

				try {
					method.invoke(validatorInstance, annotation, field.get(model), field.getType());
				} catch (InvocationTargetException ite) {
					if (ite.getCause() instanceof ValidationException)
						throw new ValidationException(
							"Field [ " + 
							String.join(".", field.getDeclaringClass().getName(), field.getName()) + 
							" ]" + ite.getCause().getMessage()
						);
				}
			}
		}
	}

	/**
	 * finding fields from model Class, as well as fields from its superclass hierarchy
	 */
	public static List<Field> getInheritedDeclaredFields(Object model) throws Exception {
		List<Field> fields = new ArrayList<Field>();
		
		Class<?> modelClass = model.getClass();
		while (modelClass != null) {
			fields.addAll(Arrays.asList(modelClass.getDeclaredFields()));
			modelClass = modelClass.getSuperclass();
		}

		return fields;
	}

	private final static Set<Class<?>> NUMBER_REFLECTED_PRIMITIVES;
	static {
	    Set<Class<?>> s = new HashSet<>();
	    s.add(byte.class);
	    s.add(short.class);
	    s.add(int.class);
	    s.add(long.class);
	    s.add(float.class);
	    s.add(double.class);
	    NUMBER_REFLECTED_PRIMITIVES = s;
	}

	public static boolean isReflectedAsNumber(Class<?> type) {
	    return Number.class.isAssignableFrom(type) || NUMBER_REFLECTED_PRIMITIVES.contains(type);
	}	
}