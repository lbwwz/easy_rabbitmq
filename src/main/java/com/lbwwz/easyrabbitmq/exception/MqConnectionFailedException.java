package com.lbwwz.easyrabbitmq.exception;

import java.io.IOException; /**
 * @author lbwwz
 */
public class MqConnectionFailedException  extends RuntimeException {

    private static final long serialVersionUID = 52066437697086987L;

    public MqConnectionFailedException () {
        super();
    }


    public MqConnectionFailedException (String s) {
        super (s);
    }

    public MqConnectionFailedException(String s, Exception e) {super(s,e);}
}
