create sequence received_records_id_seq;

create table received_records(

id bigint primary key,

frequency DOUBLE PRECISION not null,
signal_level DOUBLE PRECISION not null,
record_id BIGINT not null

);

alter table received_records
add foreign key (record_id) references records (id) on delete cascade;