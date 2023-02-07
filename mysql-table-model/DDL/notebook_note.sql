-- auto-generated definition
create table notebook_note
(
    id             int auto_increment
        primary key,
    create_time    timestamp   null comment '创建时间',
    update_time    timestamp   null comment '修改时间',
    space_id       int         not null comment '所属空间id',
    text           longtext    not null comment 'markdown内容',
    html           longtext    null comment 'markdown生成的html预览',
    keyword        varchar(64) null comment '关键字,截取markdown前部分',
    tag            int         null comment '标签',
    create_user_id int         null comment '创建用户id',
    constraint notebook_note_id_uindex
        unique (id)
)
    comment '备忘录-笔记';

create index notebook_note_space_id_index
    on notebook_note (space_id);

