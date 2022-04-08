package com.vmware.acme.catalog;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AcmeCatalogController {

	private final AcmeCatalogService acmeCatalogService;

	public AcmeCatalogController(AcmeCatalogService acmeCatalogService) {
		this.acmeCatalogService = acmeCatalogService;
	}

	@GetMapping("/products")
	public GetProductsResponse getProducts() {
		return new GetProductsResponse(acmeCatalogService.getProducts().stream()
														 .map(ProductResponse::new)
														 .collect(Collectors.toList()));
	}

	@GetMapping("/products/{id}")
	public GetProductResponse getProduct(@PathVariable String id) {
		return new GetProductResponse(new ProductResponse(acmeCatalogService.getProduct(id)), HttpStatus.OK.value());
	}
}
