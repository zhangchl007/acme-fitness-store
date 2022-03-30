package com.vmware.acmecatalog.response;

import com.vmware.acmecatalog.Request.ProductRequest;

public class GetProductResponse {

    private ProductRequest data;
    private Integer status;

    public GetProductResponse(ProductRequest data, Integer status) {
        this.data = data;
        this.status = status;
    }

    public ProductRequest getData() {
        return data;
    }

    public void setData(ProductRequest data) {
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
