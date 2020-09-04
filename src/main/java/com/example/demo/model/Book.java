package com.example.demo.model;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

@SolrDocument(collection = "demo")
public class Book {

    @Id
    @Field
    private String id;

    @Field
    private String name;

    @Field
    private Boolean inStock;

    @Indexed(name = "genre_s")
    private String genre;

    public Book() {

    }

    public Book(String id, String name, Boolean inStock, String genre) {
        this.id = id;
        this.name = name;
        this.inStock = inStock;
        this.genre = genre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

}
