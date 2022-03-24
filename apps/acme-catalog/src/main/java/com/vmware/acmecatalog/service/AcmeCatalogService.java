package com.vmware.acmecatalog.service;

import com.vmware.acmecatalog.model.Product;
import com.vmware.acmecatalog.model.ProductNotFoundException;
import com.vmware.acmecatalog.repository.AcmeCatalogRepository;
import com.vmware.acmecatalog.response.CreateProductResponse;
import com.vmware.acmecatalog.response.GetProductResponse;
import com.vmware.acmecatalog.response.GetProductsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AcmeCatalogService implements IAcmeCatalogService {

    private AcmeCatalogRepository acmeCatalogRepository;

    public AcmeCatalogService(AcmeCatalogRepository acmeCatalogRepository) {
        this.acmeCatalogRepository = acmeCatalogRepository;
    }

    @Override
    public GetProductsResponse getProducts() {
        return new GetProductsResponse(acmeCatalogRepository.findAll());
    }

    @Override
    public GetProductResponse getProduct(String id) {
        var productFound = acmeCatalogRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return new GetProductResponse(productFound, HttpStatus.OK.value());
    }

    @Override
    public CreateProductResponse createProduct(Product newProduct) {
        var productSaved = acmeCatalogRepository.save(newProduct);
        String message;
        Integer httpStatus;

        if (productSaved.getId() != null) {
            message = "Product created successfully!";
            httpStatus = HttpStatus.CREATED.value();

        } else {
            message = "product creation unsuccessfully!";
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();
            productSaved = newProduct;
        }
        return new CreateProductResponse(message, productSaved, httpStatus);
    }
}
