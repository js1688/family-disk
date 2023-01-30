-- auto-generated definition
create table journal_list
(
    id             int auto_increment comment '主键'
        primary key,
    title          varchar(64) null comment '日记标题',
    body           longtext    null comment '记录内容',
    happen_time    varchar(16) null comment '日记发生日期(yyyy-MM-dd)',
    create_time    timestamp   null comment '创建日期',
    update_time    timestamp   null comment '修改日期',
    create_user_id int         null comment '创建人ID',
    space_id       int         null comment '所属空间id'
)
    comment '日记列表' charset = utf8mb3;

create index journal_list_space_id_index
    on journal_list (space_id);

