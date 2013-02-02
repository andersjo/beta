/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta.options;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class OptionException extends RuntimeException {

	static final long serialVersionUID = 3133593772339064589L;

	public OptionException() {
		super();
	}

	public OptionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public OptionException(final String message) {
		super(message);
	}

	public OptionException(final Throwable cause) {
		super(cause);
	}
}
