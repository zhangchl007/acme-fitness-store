package com.vmware.acmecatalog.response;

import com.vmware.acmecatalog.Request.ProductRequest;

import java.util.List;

public class GetProductsResponse {

    private List<ProductRequest> data;

    public GetProductsResponse(List<ProductRequest> data) {
        this.data = data;
    }

    public List<ProductRequest> getData() {
        return data;
    }

    public void setData(List<ProductRequest> data) {
        this.data = data;
    }
}
