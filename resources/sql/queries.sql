
-- :snip user-fields
id, fname, lname

-- :name list-users :?
-- :doc Get all the users
select * from users;

-- :name get-user-by-id :? :1
-- :doc Get a single user by ID
select :snip:user-fields from users
where id = :id;

-- :name create-user :i!
insert into users (fname, lname, email, age)
values (:fname, :lname, :email, :age)
returning id;

-- :name find-users :?
select * from users
where fname = :name
and city = :city
and year_birth = :year-birth
limit 10


-- :name find-users :?
select * from users
where fname = :name
--~ (when (:city params) "and city = :city")
--~ (when (:year-birth params) "and year_birth = :year-birth")
limit 10


-- :name get-user-by-id :? :1
-- :doc Get a single user by ID
select
/*~ (if (:user-fields params) */
:snip:user-fields
/*~*/
*
/*~ ) ~*/
from users
where id = :id;


-- :name find-users2 :?
select * from users u
/*~ (when (:with-photo? params) */
join photos p on p.user_id = u.id
/*~ ) ~*/



-- :snip user-fields
/*~ (if (:root? params) */
*
/*~*/
id, fname, lname
/*~ ) ~*/


-- :name get-user-by-id :? :1
select :snip:user-fields
from users where id = :id;
