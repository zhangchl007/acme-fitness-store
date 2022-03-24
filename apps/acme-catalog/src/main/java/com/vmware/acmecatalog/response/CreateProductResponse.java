package com.vmware.acmecatalog.response;

import com.vmware.acmecatalog.model.Product;

public class CreateProductResponse {

    private String message;
    private Product resourceId;
    private Integer status;

    public CreateProductResponse(String message, Product resourceId, Integer status) {
        this.message = message;
        this.resourceId = resourceId;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Product getResourceId() {
        return resourceId;
    }

    public void setResourceId(Product resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
