-- auto-generated definition
create table journal_list_files
(
    id          int auto_increment comment '主键'
        primary key,
    journal_id  int          null comment '日记id',
    file_md5    varchar(64)  null comment '文件md5值',
    file_name   varchar(256) null comment '文件名',
    create_time timestamp    null comment '创建日期',
    update_time timestamp    null comment '修改日期',
    media_type  varchar(128)  null comment '多媒体类型',
    constraint journal_list_files_journal_list_id_fk
        foreign key (journal_id) references journal_list (id)
)
    comment '日记关联的文件' charset = utf8mb3;

