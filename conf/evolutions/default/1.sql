# --- !Ups

CREATE SEQUENCE member_id_seq;

CREATE TABLE member
(
  id bigint NOT NULL DEFAULT nextval('member_id_seq'),
  name text NOT NULL DEFAULT ''::text,
  bizteam_id bigint NOT NULL,
  created_at timestamp  NOT NULL,
  constraint member_pkey primary key (id)
);


CREATE SEQUENCE bizteam_id_seq;

CREATE TABLE bizteam
(
  id bigint NOT NULL DEFAULT nextval('bizteam_id_seq'),
  name text NOT NULL DEFAULT ''::text,
  created_at timestamp  NOT NULL,
  constraint bizteam_pkey primary key (id)
);

INSERT INTO bizteam (name, created_at) VALUES
('Developing', NOW()),
('Sales', NOW());

INSERT INTO member (name, bizteam_id, created_at) VALUES
('Alice', 2, NOW()),
('Bob', 1, NOW());

# --- !Downs

DROP TABLE member;

DROP SEQUENCE member_id_seq;

DROP TABLE bizteam;

DROP SEQUENCE bizteam_id_seq;