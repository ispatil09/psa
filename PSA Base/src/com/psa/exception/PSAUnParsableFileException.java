package com.psa.exception;

/**
 * 
 * @author SONY
 *Superclass of all exceptions thrown when failed in parsing.
 */
public class PSAUnParsableFileException extends PSAException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PSAUnParsableFileException(String msg) {
		super(msg);
	}

}
