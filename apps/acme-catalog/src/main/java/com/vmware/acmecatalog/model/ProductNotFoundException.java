package com.vmware.acmecatalog.model;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String id) {
        super("Could not find product " + id);
    }
}
