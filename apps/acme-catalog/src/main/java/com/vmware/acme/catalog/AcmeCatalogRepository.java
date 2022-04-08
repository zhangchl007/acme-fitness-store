package com.vmware.acme.catalog;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcmeCatalogRepository extends CrudRepository<Product, String> {

}
