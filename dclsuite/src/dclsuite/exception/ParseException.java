package dclsuite.exception;

public class ParseException extends Exception {
	private final Exception triggeredException;
	private final String body;
	private final int lineNumber;
	
	public ParseException(Exception triggeredException, String body, int lineNumber) {
		this.triggeredException = triggeredException;
		this.body = body;
		this.lineNumber = lineNumber;
	}
	
	public String getBody() {
		return this.body;
	}

	public int getLineNumber() {
		return this.lineNumber;
	}

	public Exception getTriggeredException() {
		return this.triggeredException;
	}
	
}
