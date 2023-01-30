-- auto-generated definition
create table user_info
(
    id          int auto_increment comment '主键'
        primary key,
    email       varchar(64)                  null comment '用户邮箱',
    name        varchar(32)                  null comment '用户名称',
    password    varchar(32)                  not null comment '密码',
    create_time timestamp                    null comment '创建时间',
    update_time timestamp                    null comment '修改时间',
    role        varchar(32) default 'COMMON' not null comment '账户角色',
    constraint user_info_pk
        unique (email)
)
    comment '用户信息' charset = utf8mb3;

