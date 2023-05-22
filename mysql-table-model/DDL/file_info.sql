-- auto-generated definition
create table file_info
(
    id             int auto_increment comment '主键'
        primary key,
    disk_id        int           not null comment '所属磁盘id',
    name           varchar(256)  null comment '文件名称',
    type           varchar(16)   null comment '文件类型',
    create_time    timestamp     null comment '创建时间',
    update_time    timestamp     null comment '修改时间',
    create_user_id int           null comment '创建用户id',
    space_id       int           null comment '所属空间id',
    size           bigint        null comment '文件大小(B)',
    source         varchar(32)   not null comment '文件来源(记事本,云盘,日记)',
    file_md5       varchar(64)   null comment '文件MD5值',
    mark_delete    int default 0 not null comment '标记删除(1是,0否)',
    delete_time    int           null comment '执行删除时间(10位长度)',
    media_type     varchar(128)  null comment '文件多媒体类型',
    `before`       int default 0 not null comment '是否是上传之前,意味着这个文件还未上传成功(1是0否)',
    constraint file_info_file_md5_space_id_source_uindex
        unique (file_md5, space_id, source),
    constraint file_info_user_info_null_fk
        foreign key (create_user_id) references user_info (id),
    constraint file_info_user_space_null_fk
        foreign key (space_id) references user_space (id)
)
    comment '文件信息' charset = utf8mb3;

create index file_info_mark_delete_delete_time_index
    on file_info (mark_delete, delete_time);

create index file_info_source_index
    on file_info (source);

