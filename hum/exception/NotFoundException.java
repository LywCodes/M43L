package ita.exception;

import ita.enumeration.EntityType;

public class NotFoundException extends RuntimeException {

    public NotFoundException(EntityType entityType, String key, String value) {
        super(String.format("%s with %s %s not found", entityType.getValue(), key, value));
    }

}
