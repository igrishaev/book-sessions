
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


SELECT
  a.id                     as "author/id",
  a.name                   as "author/name",
  json_agg(row_to_json(p)) as "author/posts"
FROM authors a
JOIN posts p ON p.author_id = a.id
GROUP BY a.id;


SELECT
  a.id                     as "author/id",
  a.name                   as "author/name",
  json_agg(row_to_json(p)) as "author/posts",



FROM authors a
JOIN posts p ON p.author_id = a.id
LEFT JOIN comments c ON c.post_id = p.id
GROUP BY a.id;



SELECT
  p.id        as "post/id",
  p.title     as "post/title",
  p.author_id as "post/author-id",
  json_agg(row_to_json(c)) FILTER (WHERE c IS NOT NULL) as "post/comments"
FROM posts p
LEFT JOIN comments c ON c.post_id = p.id
GROUP BY p.id;


 post/id |         post/title          | post/author-id |                                                       post/comments
---------+-----------------------------+----------------+----------------------------------------------------------------------------------------------------------------------------
      10 | Introduction to Python      |              1 | [{"id":100,"text":"Thanks for sharing this!","post_id":10}, {"id":200,"text":"Nice reading, it was useful.","post_id":10}]
      20 | Thoughts on LISP            |              1 |
      30 | Is mining still profitable? |              2 | [{"id":300,"text":"TL;DR: you must learn lisp","post_id":30}]
      40 | Mining on Raspberry Pi      |              2 |


SELECT
  a.id                         as "author/id",
  a.name                       as "author/name",
  json_agg(row_to_json(posts)) as "author/posts"
FROM
  authors a,
  (SELECT
    p.id        as "post/id",
    p.title     as "post/title",
    p.author_id as "post/author-id",
    json_agg(row_to_json(c)) FILTER (WHERE c IS NOT NULL) as "post/comments"
  FROM posts p
  LEFT JOIN comments c ON c.post_id = p.id
  GROUP BY p.id
) AS posts
WHERE a.id = posts."post/author-id"
GROUP BY a.id;



CREATE TABLE goods (
  id serial primary key,
  title text not null
);

CREATE TABLE good_attrs (
  id serial primary key,
  good_id integer not null references goods(id),
  attr text not null,
  val jsonb
);



INSERT INTO goods (id, title)
VALUES (1, 'iPhone 99x'),
       (2, 'Galaxy 33.plus'),
       (3, 'G. Orwell 1984');

INSERT INTO good_attrs (good_id, attr, val)
VALUES (1, 'phone.display.diag', '145'),
       (1, 'phone.wifi.support', 'true'),
       (3, 'book.pages', '215'),
       (3, 'book.genre', '"dystopia"');


SELECT
  g.id,
  g.title,
  a.attrs
FROM
  goods g
LEFT JOIN (
  SELECT
    ga.good_id,
    jsonb_object_agg(ga.attr, ga.val) as attrs
  FROM good_attrs ga
    GROUP BY ga.good_id
) a ON a.good_id = g.id;


 id |     title      |                          attrs
----+----------------+---------------------------------------------------------
  1 | iPhone 99x     | {"phone.display.diag": 145, "phone.wifi.support": true}
  2 | Galaxy 33.plus |
  3 | G. Orwell 1984 | {"book.genre": "dystopia", "book.pages": 215}


SELECT
  ga.good_id,
  jsonb_object_agg(ga.attr, ga.val) as attrs
FROM good_attrs ga
  GROUP BY ga.good_id;



select 1 as id, 'foo' as name
UNION
select 2 as id, 'bar' as name;

 id | name
----+------
  1 | foo
  2 | bar


CREATE TABLE admins (
  id serial,
  full_name text NOT NULL,
  email text
);

INSERT INTO admins (full_name, email)
VALUES ('Petr Smirnov', 'petr@test.com'),
       ('Oleg Ivanov', 'oleg@test.com');

SELECT
  u.id AS id,
  'user' AS type,
  row_to_json(u) AS entity
FROM
  users u
UNION ALL
SELECT
  a.id AS id,
  'admin' AS type,
  row_to_json(a) AS entity
FROM
  admins a;


 id | type  |                                                                           entity
----+-------+------------------------------------------------------------------------------------------------------------------------------------------------------------
  1 | user  | {"id":1,"fname":"Ivan","lname":"Petrov","email":"test@test.com","age":30,"city":null,"year_birth":null,"created_at":"2021-08-10T07:36:03.934029+00:00"}
  3 | user  | {"id":3,"fname":"Huan","lname":null,"email":null,"age":null,"city":null,"year_birth":null,"created_at":"2021-08-10T07:36:03.934029+00:00"}
  1 | admin | {"id":1,"full_name":"Petr Smirnov","email":"petr@test.com"}
  2 | admin | {"id":2,"full_name":"Oleg Ivanov","email":"oleg@test.com"}


SELECT
  u.id AS id,
  'user' AS type,
  to_jsonb(u) AS entity
FROM
  users u
UNION
SELECT
  a.id AS id,
  'admin' AS type,
  to_jsonb(a) AS entity
FROM
  admins a;


SELECT
  u.id AS id,
  'user' AS type,
  row_to_json(u) AS entity
FROM
  users u
UNION
SELECT
  a.id AS id,
  'admin' AS type,
  row_to_json(a) AS entity
FROM
  admins a;


create table migrations (
  migration_id text primary key,
  created_at timestamp,
  description text
);


1630047678-create-users-table.up.sql
1630047678-create-users-table.down.sql

1630048005-create-profiles-table.up.sql
1630048005-create-profiles-table.down.sql


create table requests(
  id serial primary key,
  ip text not null,
  country_code text null
);

insert into requests (ip)
values
('24.198.249.0'),
('187.33.237.120');

SELECT * FROM requests;

 id |       ip       | country_code
----+----------------+--------------
  1 | 24.198.249.0   |
  2 | 187.33.237.120 |



 id |       ip       | country_code
----+----------------+--------------
  1 | 24.198.249.0   | US
  2 | 187.33.237.120 | BR
