
#
createuser -U postgres -s bugtest
createdb -U postgres -O bugtest bugtest

psql -U bugtest bugtest -c "create schema _default;"
psql -U bugtest bugtest -c "create sequence _default.hibernate_sequence;"
psql -U bugtest bugtest -c "create table _default.test (id bigint not null, name text not null, description text not null, date_created timestamp without time zone not null, primary key (id))"


