-- auto-generated definition
create table od_record
(
    id          int auto_increment comment '主键'
        primary key,
    gid         varchar(64)  not null comment 'aria2文件下载记录唯一标识',
    file_name   varchar(128) null comment '文件保存名称',
    target_id   int          not null comment '网盘目录ID',
    space_id    int          not null comment '所属空间id',
    create_time timestamp    null comment '创建时间',
    update_time timestamp    null comment '修改时间',
    uri_type    varchar(64)  null comment 'uri类型',
    msg         varchar(128) null comment '描述信息'
)
    comment '离线下载记录';

