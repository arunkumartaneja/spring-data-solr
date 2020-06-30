package com.example.demo.controller;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

@RestController
@RequestMapping("/book/")
public class DemoController {

	@Autowired
	private BookRepository bookRepository;

	@GetMapping
	public String hello() {
		return "hello";
	}

	@GetMapping("add")
	public Book add() {
		return bookRepository.save(getBook());
	}

	@GetMapping("search/{query}")
	public List<Book> search(@PathVariable String query) {
		return bookRepository.findAll(query);
	}

	@GetMapping("search-name/{query}")
	public HighlightPage<Book> searchByName(@PathVariable String query) {
		Pageable paging = PageRequest.of(0, 10);
		return bookRepository.findByNameLike(query, paging);
	}

	private Book getBook() {
		Random random = new Random();
		String id = random.ints(97, 123).limit(5)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
		Book book = new Book(id, "name-" + id, true, "genre-" + id);

		return book;
	}

}
