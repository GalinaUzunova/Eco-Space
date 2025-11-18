package org.ecospace.exception;

import java.util.UUID;

public class ProductNotFound extends RuntimeException{
    public ProductNotFound(String message) {
        super(message);
    }
}
