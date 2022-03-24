package com.vmware.acmecatalog.response;

import com.vmware.acmecatalog.model.Product;

import java.util.List;

public class GetProductsResponse {
    private List<Product> data;

    public GetProductsResponse(List<Product> data) {
        this.data = data;
    }

    public List<Product> getData() {
        return data;
    }

    public void setData(List<Product> data) {
        this.data = data;
    }
}
