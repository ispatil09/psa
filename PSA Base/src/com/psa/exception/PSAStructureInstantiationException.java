package com.psa.exception;

public class PSAStructureInstantiationException extends PSAException {

	/**
	 * Structure failed to instantiate at entity level.
	 * Example : 1. Trying to name node with a duplicate number.
	 * 			 2. Trying to add beam with two non existing nodes.
	 */
	private static final long serialVersionUID = -8797323574686263148L;

	public PSAStructureInstantiationException(String msg) {
		
		super(msg);
	}

}
