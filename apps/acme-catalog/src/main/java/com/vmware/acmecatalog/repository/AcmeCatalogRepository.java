package com.vmware.acmecatalog.repository;

import com.vmware.acmecatalog.model.Product;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcmeCatalogRepository extends CrudRepository<Product, String> {
}
