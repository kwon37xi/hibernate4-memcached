# hibernate4-memcached : Hibernate 4 Memcached Second Level Cache Implementation

## Requirements

* Java 6 or higher
* Hibernate 4.2.10 or higher

## Features
* current memcached-adapter support
    * spymemcached
* Current supported Concurrency Strategy
    * nonstrict-read-write
* Region(Namespace) support : Memcached does not have region concept. but this library supports it.
  Refer to [Memcached Namespace](https://code.google.com/p/memcached/wiki/NewProgrammingTricks#Namespacing)
* Many type of memcached adapters : You can add your own memcached adapter. I may add xmemcached, couchbase adapters.
