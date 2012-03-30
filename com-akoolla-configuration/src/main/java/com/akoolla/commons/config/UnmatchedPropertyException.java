package com.akoolla.commons.config;

/**
 * UnmatchedPropertyException.
 * 
 * @author tiffir
 * @version $Id: UnmatchedPropertyException.java 111 2011-03-14 11:22:01Z tiffir $
 */
public class UnmatchedPropertyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnmatchedPropertyException() {
        super();

    }

    public UnmatchedPropertyException(String message, Throwable cause) {
        super(message, cause);

    }

    public UnmatchedPropertyException(String message) {
        super(message);

    }

    public UnmatchedPropertyException(Throwable cause) {
        super(cause);

    }
}
