

create table users(
    id   serial primary key,
    name text not null
);

create table profiles(
    id      serial primary key,
    user_id integer not null references users(id),
    avatar  text
);
