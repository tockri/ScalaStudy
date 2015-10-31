# --- !Ups

CREATE SEQUENCE "member_id_seq";

CREATE TABLE "member"
(
  id bigint NOT NULL DEFAULT nextval('member_id_seq') PRIMARY KEY,
  name text NOT NULL DEFAULT ''::text,
  team_id bigint NOT NULL,
  created_at timestamp  NOT NULL
);


CREATE SEQUENCE "team_id_seq";

CREATE TABLE "team"
(
  id bigint NOT NULL DEFAULT nextval('team_id_seq') PRIMARY KEY,
  name text NOT NULL DEFAULT ''::text,
  created_at timestamp  NOT NULL
);

INSERT INTO "team" (name, created_at) VALUES
('Developing', NOW()),
('Sales', NOW());

INSERT INTO "member" (name, team_id, created_at) VALUES
('Alice', 2, NOW()),
('Bob', 1, NOW());

# --- !Downs

DROP TABLE "member";

DROP SEQUENCE "member_id_seq";

DROP TABLE "team";

DROP SEQUENCE "team_id_seq";