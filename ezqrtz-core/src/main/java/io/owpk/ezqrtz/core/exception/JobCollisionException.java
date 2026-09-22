package io.owpk.ezqrtz.core.exception;

public final class JobCollisionException extends CztSchedulingException {

    public JobCollisionException(String msg) {
        super(msg);
    }
}
