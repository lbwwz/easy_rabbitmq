package com.lbwwz.easyrabbitmq.exception;

/**
 * @author lbwwz
 */
public class PublishOrConsumeException extends RuntimeException {
    private static final long serialVersionUID = 2165722396774751844L;


    public PublishOrConsumeException () {
        super();
    }


    public PublishOrConsumeException (String s) {
        super (s);
    }
}
