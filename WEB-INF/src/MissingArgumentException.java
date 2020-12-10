public class MissingArgumentException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public MissingArgumentException() {
		super();
	}

	public MissingArgumentException(String message) {
		super(message);
	}
}