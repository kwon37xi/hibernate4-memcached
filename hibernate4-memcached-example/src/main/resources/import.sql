INSERT INTO books (book_id,description,edition,published_at,title) VALUES (null,'This book is about hibernate4memcached implementation.',1, '2014-06-07','JPA 2 Hibernate4memcached');
INSERT INTO books (book_id,description,edition,published_at,title) VALUES (null,'JPA 2를 배워보자.',3,'2006-05-10','JPA 2.1 쉽게 배우기');

INSERT INTO authors (author_id,country,name) VALUES (null,'대한민국','손권남');
INSERT INTO authors (author_id,country,name) VALUES (null,'어느나라','누군가');
INSERT INTO authors (author_id,country,name) VALUES (null,'TOBEDELETED','난지워질꺼야');

INSERT INTO book_authors(book_id, author_id) VALUES(1, 1);
INSERT INTO book_authors(book_id, author_id) VALUES(2, 1);
INSERT INTO book_authors(book_id, author_id) VALUES(2, 2);

INSERT INTO people(person_id, name, birthdate) VALUES (null, 'KwonNam Son', '1977-04-29');