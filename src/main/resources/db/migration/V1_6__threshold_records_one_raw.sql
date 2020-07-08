create sequence threshold_records_one_raw_id_seq;

create table threshold_records_one_raw(

id bigint primary key,

frequency TEXT not null,
signal_level TEXT not null,
record_id BIGINT not null

);

alter table threshold_records_one_raw
add foreign key (record_id) references records (id) on delete cascade;