package com.vmware.acmecatalog.response;

import com.vmware.acmecatalog.Request.ProductRequest;

public class CreateProductResponse {

    private String message;
    private ProductRequest resourceId;
    private Integer status;

    public CreateProductResponse(String message, ProductRequest resourceId, Integer status) {
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

    public ProductRequest getResourceId() {
        return resourceId;
    }

    public void setResourceId(ProductRequest resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
