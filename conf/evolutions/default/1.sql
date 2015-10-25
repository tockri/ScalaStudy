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

# --- !Downs

DROP TABLE "member";

DROP SEQUENCE "member_id_seq";

DROP TABLE "team";

DROP SEQUENCE "team_id_seq";