package com.vmware.acmecatalog.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.vmware.acmecatalog.Request.ProductRequest;
import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "catalog")
public class Product {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(columnDefinition = "varchar(36)")
    private String id;

    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String name;
    private String shortDescription;
    private String description;
    private Double price;
    private String tags;

    public static Product fromProductRequestToProduct(ProductRequest productRequest) {

        Product product = new Product();
        product.setPrice(productRequest.getPrice());
        product.setDescription(productRequest.getDescription());
        product.setName(productRequest.getName());
        product.setImageUrl1(productRequest.getImageUrl1());
        product.setImageUrl2(productRequest.getImageUrl2());
        product.setImageUrl3(productRequest.getImageUrl3());
        product.setShortDescription(productRequest.getShortDescription());

        var tags = "";

        if (productRequest.getTags() != null && !productRequest.getTags().isEmpty()) {
            for (int i = 0; i < productRequest.getTags().size(); i++) {
                tags = tags + productRequest.getTags().get(i);
                if (i < productRequest.getTags().size() - 1) {
                    tags = tags + ",";
                }
            }
        }
        product.setTags(tags);
        return product;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl1() {
        return imageUrl1;
    }

    public void setImageUrl1(String imageUrl1) {
        this.imageUrl1 = imageUrl1;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }

    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }

    public String getImageUrl3() {
        return imageUrl3;
    }

    public void setImageUrl3(String imageUrl3) {
        this.imageUrl3 = imageUrl3;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
