package com.vmware.acmecatalog.controller;

import com.vmware.acmecatalog.model.Product;
import com.vmware.acmecatalog.response.CreateProductResponse;
import com.vmware.acmecatalog.response.GetProductResponse;
import com.vmware.acmecatalog.response.GetProductsResponse;
import com.vmware.acmecatalog.service.AcmeCatalogService;
import org.springframework.web.bind.annotation.*;

@RestController
public class AcmeCatalogController {

    private AcmeCatalogService acmeCatalogService;

    public AcmeCatalogController(AcmeCatalogService acmeCatalogService) {
        this.acmeCatalogService = acmeCatalogService;
    }

    @GetMapping("/products")
    public GetProductsResponse getProducts() {
        return acmeCatalogService.getProducts();
    }

    @GetMapping("/products/{id}")
    public GetProductResponse getProduct(@PathVariable String id) {
        return acmeCatalogService.getProduct(id);
    }

    @PostMapping("/products")
    public CreateProductResponse createProduct(@RequestBody Product newProduct) {
        return acmeCatalogService.createProduct(newProduct);
    }
}
