package com.neural;

public class InvalidTypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1891980723467189820L;

	public InvalidTypeException(String msg) {
		super(msg);
	}
	
	public InvalidTypeException(Throwable e) {
		super(e);
	}
}
