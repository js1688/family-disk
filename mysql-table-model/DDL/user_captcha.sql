-- auto-generated definition
create table user_captcha
(
    id                  int auto_increment comment '主键'
        primary key,
    email               varchar(64) null comment '邮箱号',
    captcha             varchar(6)  null comment '验证码',
    captcha_expire_time int         null comment '验证码到期时间戳(10位)',
    create_time         timestamp   null comment '创建时间',
    update_time         timestamp   null comment '修改时间'
)
    comment '用户验证码存储' charset = utf8mb3;

create index user_captcha_email_captcha_captcha_expire_time_index
    on user_captcha (email, captcha, captcha_expire_time);

create index user_captcha_email_captcha_expire_time_index
    on user_captcha (email, captcha_expire_time);

create index user_captcha_email_index
    on user_captcha (email);

