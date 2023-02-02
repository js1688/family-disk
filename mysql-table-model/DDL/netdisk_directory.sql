-- auto-generated definition
create table netdisk_directory
(
    id          int auto_increment
        primary key,
    type        varchar(32)  not null comment '类型',
    file_md5    varchar(64)  null comment '文件md5值',
    pid         int          null comment '目录的上级id',
    space_id    int          null comment '所属空间id',
    create_time timestamp    null comment '创建时间',
    update_time timestamp    null comment '修改时间',
    name        varchar(256) null comment '目录名称',
    media_type  varchar(32)  null comment '文件多媒体类型',
    constraint netdisk_directory_name_space_id_pid_uindex
        unique (name, space_id, pid)
)
    comment '网盘文件目录' charset = utf8mb3;

create index netdisk_directory_type_index
    on netdisk_directory (type);

