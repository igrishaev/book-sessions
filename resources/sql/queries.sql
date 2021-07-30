
-- :name list-users :?
-- :doc Get all the users
select * from users;

-- :name get-user-by-id :? :1
-- :doc Get a single user by ID
select * from users
where id = :id;
