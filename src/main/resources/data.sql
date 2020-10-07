DROP TABLE IF EXISTS users;

create sequence IF NOT EXISTS vavishka_seq;

select vavishka_seq.nextval from dual;
select vavishka_seq.nextval from dual;

CREATE TABLE IF NOT EXISTS users (
  id bigint default vavishka_seq.nextval primary key,
  user_id char(30),
  email varchar(256) NOT NULL,
  mobile char(11),
  password char(128),
  email_verified number(1),
  name varchar(64),
  picture_url varchar(256),
  locale char(2),
  family_name varchar(64),
  given_name varchar(64)
);

delete from users where id in(1, 2);

INSERT INTO users (id, user_id, email, email_verified, password) values
(1, 'admin', 'admin', 1, '$2a$10$kcXK1Vjmy79dMr.T7j5AJuWAlrGTqKWu/dk7kPFYESJGHqdCdO4.K'),
(2, 'seo', 'seo', 1, '$2a$10$kcXK1Vjmy79dMr.T7j5AJuWAlrGTqKWu/dk7kPFYESJGHqdCdO4.K');

CREATE TABLE IF NOT EXISTS header (
    id bigint default vavishka_seq.nextval primary key,
    path varchar(128),
    orders number(1)
);

CREATE TABLE IF NOT EXISTS samples (
  id bigint default vavishka_seq.nextval primary key,
  title char(128),
  description varchar(256) NOT NULL,
  image_src varchar(256)
);

CREATE TABLE IF NOT EXISTS samples_session (
    id bigint default vavishka_seq.nextval primary key,
    samples_id bigint,
    title char(128),
    description varchar(256),
    detail varchar(4096) ,
    icon_src varchar(256),
    orders int,
    CONSTRAINT FK_SESSION_TO_SAMPLES FOREIGN KEY ( samples_id ) REFERENCES samples( id )
);

CREATE TABLE IF NOT EXISTS samples_session_image (
    id bigint default vavishka_seq.nextval primary key,
    samples_session_id bigint,
    orders int,
    image_src varchar(256),
    CONSTRAINT FK_SESSION_IMAGE_TO_SAMPLES_SESSION FOREIGN KEY ( samples_session_id ) REFERENCES samples_session( id )
);

CREATE TABLE IF NOT EXISTS images (
    id bigint default vavishka_seq.nextval primary key,
    image_src char(70),
    image_type char(10),
    image_data binary(100000)
);
