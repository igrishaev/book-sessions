
CREATE TYPE user_status AS ENUM ('active', 'pending', 'blocked');

--;;

CREATE TABLE users(
  id serial primary key,
  name text not null,
  status user_status not null,
  email text
);

--;;

CREATE INDEX idx_users_name ON users(name);
