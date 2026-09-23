package io.owpk.ezqrtz.core.exception;

public final class JobCollisionException extends EzSchedulingException {

    public JobCollisionException(String msg) {
        super(msg);
    }
}
