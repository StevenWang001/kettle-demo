
drop table if exists job cascade;
create table job (
id serial primary key,
name varchar(64) unique,
description varchar(256),
interval varchar(64),
js_code varchar(1024),
source_url varchar(256),
source_type varchar(32)
);

drop table if exists biz_data_mapping cascade;
create table biz_data_mapping (
id serial primary key,
job_id int references job(id),
table_name varchar(64),
real_table_name varchar(64) unique
);

drop table if exists biz_data_mapping_fields cascade;
create table biz_data_mapping_fields (
id serial primary key,
mapping_id int references biz_data_mapping(id),
name varchar(64),
type varchar(32),
length int,
path varchar(64)
);


drop table if exists indicator cascade;
create table indicator (
id serial primary key,
job_id int references job(id),
name varchar(64),
description varchar(256),
sql varchar(256),
real_sql varchar(256),
interval varchar(64),
table_name varchar(64),
real_table_name varchar(64) unique
);

drop table if exists indicator_fields cascade;
create table indicator_fields (
id serial primary key,
indicator_id int references indicator(id),
name varchar(64),
type varchar(32),
length int
);
