create sequence record_id_seq;

create table records(

id bigint primary key,
time_stamp DOUBLE PRECISION not null,
seq_number INTEGER not null,
threshold DOUBLE PRECISION not null

);