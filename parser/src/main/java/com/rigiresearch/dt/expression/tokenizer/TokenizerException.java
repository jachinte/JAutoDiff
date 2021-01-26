package com.rigiresearch.dt.expression.tokenizer;

/**
 * An error thrown during the expression tokenization.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @version $Id$
 * @since 0.1.0
 */
public final class TokenizerException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 3217235941152996517L;

	/**
	 * Empty constructor.
	 */
	public TokenizerException() {
		super();
	}

	/**
	 * Default constructor.
	 * @param message The error message
	 */
	public TokenizerException(final String message) {
		super(message);
	}

}
