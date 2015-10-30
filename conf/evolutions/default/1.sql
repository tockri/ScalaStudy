# --- !Ups

CREATE SEQUENCE "member_id_seq";

CREATE TABLE "member"
(
  id bigint PRIMARY KEY DEFAULT nextval('member_id_seq'),
  name text NOT NULL DEFAULT ''::text,
  team_id bigint NOT NULL,
  created_at timestamp with time zone NOT NULL
);


CREATE SEQUENCE "team_id_seq";

CREATE TABLE "team"
(
  id bigint PRIMARY KEY DEFAULT nextval('team_id_seq'),
  name text NOT NULL DEFAULT ''::text,
  created_at timestamp with time zone NOT NULL
);

INSERT INTO team (id, name, created_at) VALUES
(1, 'Developing', NOW()),
(2, 'Sales', NOW());

INSERT INTO member (id, name, team_id, created_at) VALUES
(1, 'Alice', 2, NOW()),
(2, 'Bob', 1, NOW());

# --- !Downs

DROP TABLE "member";

DROP SEQUENCE "member_id_seq";

DROP TABLE "team";

DROP SEQUENCE "team_id_seq";