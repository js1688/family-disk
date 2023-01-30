-- auto-generated definition
create table user_space_rel
(
    id             int auto_increment
        primary key,
    create_user_id int         not null comment '空间的创建用户id',
    space_id       int         not null comment '空间id',
    user_id        int         not null comment '用户id',
    create_time    timestamp   null comment '创建时间',
    update_time    timestamp   null comment '修改时间',
    role           varchar(32) null comment '空间权限',
    constraint user_space_rel_user_info_null_fk
        foreign key (user_id) references user_info (id),
    constraint user_space_rel_user_space_null_fk
        foreign key (space_id) references user_space (id)
)
    comment '用户与空间关联关系' charset = utf8mb3;

create index user_space_rel_create_user_id_index
    on user_space_rel (create_user_id);

create index user_space_rel_space_id_index
    on user_space_rel (space_id);

create index user_space_rel_user_id_index
    on user_space_rel (user_id);

