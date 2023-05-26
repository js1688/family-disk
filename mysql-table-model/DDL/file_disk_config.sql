-- auto-generated definition
create table file_disk_config
(
    id          int auto_increment
        primary key,
    type        varchar(32) null comment '磁盘类型(LOCAL,HDFS,NAS)',
    max_size    int         null comment '磁盘总大小(GB)',
    path        varchar(64) null comment '磁盘地址',
    create_time timestamp   null comment '创建时间',
    update_time timestamp   null comment '修改时间',
    usable_size int         null comment '磁盘可用(GB)',
    bakPath     varchar(64) null comment '本地磁盘备份路径'
)
    comment '可存储文件的磁盘配置' charset = utf8mb3;

create index file_disk_config_type_path_index
    on file_disk_config (type, path);

