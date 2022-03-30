package com.vmware.acmecatalog.service;

import com.vmware.acmecatalog.Request.ProductRequest;
import com.vmware.acmecatalog.model.Product;
import com.vmware.acmecatalog.model.ProductNotFoundException;
import com.vmware.acmecatalog.repository.AcmeCatalogRepository;
import com.vmware.acmecatalog.response.CreateProductResponse;
import com.vmware.acmecatalog.response.GetProductResponse;
import com.vmware.acmecatalog.response.GetProductsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AcmeCatalogService implements IAcmeCatalogService {

    private final AcmeCatalogRepository acmeCatalogRepository;

    public AcmeCatalogService(AcmeCatalogRepository acmeCatalogRepository) {
        this.acmeCatalogRepository = acmeCatalogRepository;
    }

    @Override
    public GetProductsResponse getProducts() {
        var result = (List<Product>) acmeCatalogRepository.findAll();

        List<ProductRequest> productsRequest = new ArrayList<>();

        for (Product product : result) {
            productsRequest.add(ProductRequest.fromProductToProductRequest(product));
        }

        return new GetProductsResponse(productsRequest);
    }

    @Override
    public GetProductResponse getProduct(String id) {
        var productFound = acmeCatalogRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ProductNotFoundException(id));

        return new GetProductResponse(ProductRequest.fromProductToProductRequest(productFound), HttpStatus.OK.value());
    }

    @Override
    public CreateProductResponse createProduct(ProductRequest product) {

        var newProduct = Product.fromProductRequestToProduct(product);

        var productSaved = acmeCatalogRepository.save(newProduct);
        String message;
        int httpStatus;
        ProductRequest productResponse;

        if (productSaved.getId() != null) {
            message = "Product created successfully!";
            httpStatus = HttpStatus.CREATED.value();
            productResponse = ProductRequest.fromProductToProductRequest(productSaved);

        } else {
            message = "product creation unsuccessfully!";
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();
            productResponse = ProductRequest.fromProductToProductRequest(newProduct);
        }
        return new CreateProductResponse(message, productResponse, httpStatus);
    }
}
