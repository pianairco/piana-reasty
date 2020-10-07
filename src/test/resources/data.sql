DROP TABLE IF EXISTS users;

create sequence IF NOT EXISTS reasty_seq;

CREATE TABLE temp_val (
  val bigint,
  orders int
);

insert into temp_val (val, orders) values(reasty_seq.nextval, 1);
insert into temp_val (val, orders) values(reasty_seq.nextval, 2);

CREATE TABLE users (
  id bigint default reasty_seq.nextval primary key,
  first_name varchar(30),
  last_name varchar(30) NOT NULL,
  age number(3),
  amount int,
  is_verified number(1)
);

INSERT INTO users (id, first_name, last_name, age, amount, is_verified) values
    ((select val from temp_val where orders = 1), 'amin', 'rahmati', 16, 1350200, 1),
    ((select val from temp_val where orders = 2), 'basir', 'ahmadi', 20, 100600, 0);

CREATE TABLE users_role (
    id bigint default reasty_seq.nextval primary key,
    users_id bigint,
    title varchar(128),
    orders int,
    CONSTRAINT FK_USERS_ROLE_TO_USERS FOREIGN KEY ( users_id ) REFERENCES users( id )
);

insert into users_role (id, users_id,  title, orders) values
    (reasty_seq.nextval, (select val from temp_val where orders = 1), 'admin', 1),
    (reasty_seq.nextval, (select val from temp_val where orders = 2), 'customer', 2);