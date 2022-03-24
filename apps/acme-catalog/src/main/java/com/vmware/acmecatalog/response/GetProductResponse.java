package com.vmware.acmecatalog.response;

import com.vmware.acmecatalog.model.Product;

public class GetProductResponse {

    private Product data;
    private Integer status;

    public GetProductResponse(Product data, Integer status) {
        this.data = data;
        this.status = status;
    }

    public Product getData() {
        return data;
    }

    public void setData(Product data) {
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
