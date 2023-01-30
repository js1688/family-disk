-- auto-generated definition
create table email_send_record
(
    id             int auto_increment comment '主键'
        primary key,
    subject        varchar(64) null comment '邮件主题',
    recipient      varchar(64) null comment '目的地邮箱号',
    msg_body       longtext    null comment '邮件内容',
    next_send_time int         null comment '下一次允许发送的时间戳',
    subject_code   varchar(32) null comment '主题编码',
    create_time    timestamp   null comment '创建时间',
    update_time    timestamp   null comment '修改时间'
)
    comment '邮件发送记录' charset = utf8mb3;

create index email_send_record_next_send_time_index
    on email_send_record (next_send_time);

create index email_send_record_recipient_index
    on email_send_record (recipient);

create index email_send_record_subject_code_index
    on email_send_record (subject_code);

