package com.moneytransfer.exception;

public class CustomException extends Exception{
	
	public CustomException(String msg) {
		super(msg);
	}

	
	public CustomException(String msg, Throwable cause) {
		super( msg,  cause);
	}
	
	
	
}
