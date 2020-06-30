package com.example.demo.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.ContentHandler;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

@RestController
@RequestMapping("/book/")
public class DemoController {

	@Autowired
	private BookRepository bookRepository;

	@Value("${spring.data.solr.host}")
	String solrURL;

	private HttpSolrClient client;
	private long start = System.currentTimeMillis();
	private AutoDetectParser autoParser;
	private int totalTika = 0;
	private int totalSql = 0;

	private Collection docList = new ArrayList();

	@GetMapping
	public String hello() {
		return "hello";
	}

	@GetMapping("add")
	public Book add() {
		return bookRepository.save(getBook());
	}

	@GetMapping("index-csv")
	public String indexCsv() {

		try {

			HttpSolrClient client = new HttpSolrClient.Builder(solrURL).build();
			client.setParser(new XMLResponseParser());
			 autoParser = new AutoDetectParser();

//	        SqlTikaExample idxer = new SqlTikaExample("http://localhost:8983/solr");

			doTikaDocuments(new File("E:/solr/demo/books.csv"));
//	        idxer.doSqlDocuments();

			endIndexing();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "indexCsv";
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

	// Recursively traverse the filesystem, parsing everything found.
	private void doTikaDocuments(File root) throws IOException, SolrServerException {

		// Simple loop for recursively indexing all the files
		// in the root directory passed in.
		for (File file : root.listFiles()) {
			if (file.isDirectory()) {
				doTikaDocuments(file);
				continue;
			}
			// Get ready to parse the file.
			ContentHandler textHandler = new BodyContentHandler();
			Metadata metadata = new Metadata();
			ParseContext context = new ParseContext();
			// Tim Allison noted the following, thanks Tim!
			// If you want Tika to parse embedded files (attachments within your .doc or any
			// other embedded
			// files), you need to send in the autodetectparser in the parsecontext:
			 context.set(Parser.class, autoParser);

			InputStream input = new FileInputStream(file);

			// Try parsing the file. Note we haven't checked at all to
			// see whether this file is a good candidate.
			try {
				autoParser.parse(input, textHandler, metadata, context);
			} catch (Exception e) {
				// Needs better logging of what went wrong in order to
				// track down "bad" documents.
				System.out.println(String.format("File %s failed", file.getCanonicalPath()));
				e.printStackTrace();
				continue;
			}
			// Just to show how much meta-data and what form it's in.
			dumpMetadata(file.getCanonicalPath(), metadata);

			// Index just a couple of the meta-data fields.
			SolrInputDocument doc = new SolrInputDocument();

			doc.addField("id", file.getCanonicalPath());

			// Crude way to get known meta-data fields.
			// Also possible to write a simple loop to examine all the
			// metadata returned and selectively index it and/or
			// just get a list of them.
			// One can also use the Lucidworks field mapping to
			// accomplish much the same thing.
			String author = metadata.get("Author");

			if (author != null) {
				doc.addField("author", author);
			}

			doc.addField("text", textHandler.toString());

			docList.add(doc);
			++totalTika;

			// Completely arbitrary, just batch up more than one document
			// for throughput!
			if (docList.size() >= 1000) {
				// Commit within 5 minutes.
				UpdateResponse resp = client.add(docList, 300000);
				if (resp.getStatus() != 0) {
					System.out.println("Some horrible error has occurred, status is: " + resp.getStatus());
				}
				docList.clear();
			}
		}
	}

	// Just to show all the metadata that's available.
	private void dumpMetadata(String fileName, Metadata metadata) {
		System.out.println("Dumping metadata for file: " + fileName);
		for (String name : metadata.names()) {
			System.out.println(name + ":" + metadata.get(name));
		}
		System.out.println("nn");
	}

	// Just a convenient place to wrap things up.
	private void endIndexing() throws IOException, SolrServerException {
		if (docList.size() > 0) { // Are there any documents left over?
			client.add(docList, 300000); // Commit within 5 minutes
		}
		client.commit(); // Only needs to be done at the end,
		// commitWithin should do the rest.
		// Could even be omitted
		// assuming commitWithin was specified.
		long endTime = System.currentTimeMillis();
		System.out.println("Total Time Taken: " + (endTime - start) + " milliseconds to index " + totalSql
				+ " SQL rows and " + totalTika + " documents");
	}

}
