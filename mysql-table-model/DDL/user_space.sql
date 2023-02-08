-- auto-generated definition
create table user_space
(
    id             int auto_increment comment '主键'
        primary key,
    max_size       int         null comment '最大空间大小(MB)',
    code           varchar(64) null comment '空间编码',
    use_size       int         null comment '已使用空间大小(MB)',
    title          varchar(64) not null comment '空间主题',
    create_user_id int         null comment '空间创建用户id',
    create_time    timestamp   null comment '创建时间',
    update_time    timestamp   null comment '修改时间',
    constraint user_space_create_user_id_uindex
        unique (create_user_id),
    constraint user_space_pk
        unique (code)
)
    comment '用户空间' charset = utf8mb3;

