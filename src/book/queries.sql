
CREATE TABLE categories (
  id serial primary key,
  title text not null
);

CREATE TABLE goods (
  id serial primary key,
  title text not null,
  category_id integer references categories(id)
);

INSERT INTO categories (id, title) VALUES (10, 'Gadgets'), (20, 'Books');

INSERT INTO goods (id, title, category_id)
VALUES (1, 'iPhone 99x', 10),
       (2, 'Galaxy 33.plus', 10),
       (3, 'G. Orwell 1984', 20);


SELECT *
FROM categories c
JOIN goods g ON g.category_id = c.id;


 id |  title  | id |     title      | category_id
----+---------+----+----------------+-------------
 10 | Gadgets |  1 | iPhone 99x     |          10
 10 | Gadgets |  2 | Galaxy 33.plus |          10
 20 | Books   |  3 | G. Orwell 1984 |          20



create table authors (
  id serial primary key,
  name text not null
);

create table posts (
  id serial primary key,
  title text not null,
  author_id integer references authors(id)
);

create table comments (
  id serial primary key,
  text text not null,
  post_id integer not null references posts(id)
);

INSERT INTO authors (id, name) VALUES
  (1, 'Ivan Petrov'),
  (2, 'Ivan Rublev');

INSERT INTO posts (id, author_id, title) VALUES
  (10, 1, 'Introduction to Python'),
  (20, 1, 'Thoughts on LISP'),
  (30, 2, 'Is mining still profitable?'),
  (40, 2, 'Mining on Raspberry Pi');






INSERT INTO comments (id, post_id, text) VALUES
  (100, 10, 'Thanks for sharing this!'),
  (200, 10, 'Nice reading, it was useful.'),
  (300, 30, 'TL;DR: you must learn lisp');


SELECT *
FROM authors a
JOIN posts p ON p.author_id = a.id;


 id |    name     | id |            title            | author_id
----+-------------+----+-----------------------------+-----------
  1 | Ivan Petrov | 10 | Introduction to Python      |         1
  1 | Ivan Petrov | 20 | Thoughts on LISP            |         1
  2 | Ivan Rublev | 30 | Is mining still profitable? |         2
  2 | Ivan Rublev | 40 | Mining on Raspberry Pi      |         2


Ivan Petrov
- Introduction to Python
- Thoughts on LISP

Ivan Rublev
- Is mining still profitable?
- Mining on Raspberry Pi


SELECT * FROM authors WHERE id = ANY('{1,2,3,4,5}');

SELECT
  a.id        as "author/id",
  a.name      as "author/name",
  p.id        as "post/id",
  p.title     as "post/title",
  p.author_id as "post/author-id"
FROM authors a
JOIN posts p ON p.author_id = a.id;



SELECT
  a.id        as "author/id",
  a.name      as "author/name",
  p.id        as "post/id",
  p.title     as "post/title",
  p.author_id as "post/author-id",
  c.id        as "comment/id",
  c.text      as "comment/text"
FROM authors a
JOIN posts p ON p.author_id = a.id
LEFT JOIN comments c ON c.post_id = p.id;


 author/id | author/name | post/id |         post/title          | post/author-id | comment/id |         comment/text
-----------+-------------+---------+-----------------------------+----------------+------------+------------------------------
         1 | Ivan Petrov |      10 | Introduction to Python      |              1 |        100 | Thanks for sharing this!
         1 | Ivan Petrov |      10 | Introduction to Python      |              1 |        200 | Nice reading, it was useful.
         2 | Ivan Rublev |      30 | Is mining still profitable? |              2 |        300 | TL;DR: you must learn lisp
         1 | Ivan Petrov |      20 | Thoughts on LISP            |              1 |            |
         2 | Ivan Rublev |      40 | Mining on Raspberry Pi      |              2 |            |
