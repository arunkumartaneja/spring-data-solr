package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.repository.Highlight;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;

import com.example.demo.model.Book;

public interface BookRepository extends SolrCrudRepository<Book, String> {

    @Query("name:*?0* OR genre_t:*?0*")
    public List<Book> findAll(String query);

    @Highlight(prefix = "<strong>", postfix = "</strong>", fields = {"name"})
    HighlightPage<Book> findByNameLike(String name, Pageable page);
}
