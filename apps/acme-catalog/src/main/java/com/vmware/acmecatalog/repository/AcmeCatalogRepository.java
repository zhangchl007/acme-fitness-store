package com.vmware.acmecatalog.repository;

import com.vmware.acmecatalog.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AcmeCatalogRepository extends MongoRepository<Product, String> {
}
