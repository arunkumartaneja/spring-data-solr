# spring-data-solr



# Solr introduction

Apache Solr is an enterprise-capable, open source search platform based on the Apache Lucene search library. The Solr search engine is one of the most widely deployed 
search platforms worldwide.

It's major features include full-text search, hit highlighting, faceted search, real-time indexing, dynamic clustering, database integration, NoSQL features and rich document
(e.g., Word, PDF) handling

Solr runs as a standalone full-text search server. It uses the Lucene Java search library at its core for full-text indexing and search, and has REST-like HTTP/XML and JSON APIs
that make it usable from most popular programming languages

# Solr vs RDBMS

Solr is not meant to be a replacement for your RDBMS. Rather, Solr should be used to develop the search service aspect of your application by storing only enough information 
to efficiently query your data source and providing enough information to the calling component to query your RDBMS for additional information. The data stored in the 
underlying Lucene index is essentially a fully searchable view of your data that resides as a decoupled component in your system.


[search-engine-solr-vs-relational-database](https://dzone.com/articles/search-engine-solr-vs-relational-database)

[solr-and-rdbms-the-basics-of-designing-your-application-for-the-best-of-both](https://lucidworks.com/post/solr-and-rdbms-the-basics-of-designing-your-application-for-the-best-of-both)
