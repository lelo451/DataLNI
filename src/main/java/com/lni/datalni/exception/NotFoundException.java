package com.lni.datalni.exception;

/** Thrown when an entity referenced by id does not exist. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String entity, Object id) {
        super(entity + " not found: " + id);
    }
}
