create sequence received_records_one_raw_id_seq;

create table received_records_one_raw(

id bigint primary key,

frequency varchar(2250) not null,
signal_level varchar(4500) not null,

record_id bigint not null

);

alter table received_records_one_raw
add foreign key (record_id) references records (id) on delete cascade;