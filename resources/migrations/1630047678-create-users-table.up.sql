
create type user_status as enum ('active', 'pending', 'blocked');

--;;

create table users(
  id serial primary key,
  name text not null,
  status user_status not null,
  email text
);

--;;

create index idx_users_name on users(name);
