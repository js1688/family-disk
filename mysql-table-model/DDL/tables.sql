-- MySQL dump 10.13  Distrib 8.0.26, for macos11 (x86_64)
--
-- Host: 49.232.164.11    Database: family
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `email_send_record`
--

DROP TABLE IF EXISTS `email_send_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_send_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject` varchar(64) DEFAULT NULL COMMENT '邮件主题',
  `recipient` varchar(64) DEFAULT NULL COMMENT '目的地邮箱号',
  `msg_body` longtext COMMENT '邮件内容',
  `next_send_time` int DEFAULT NULL COMMENT '下一次允许发送的时间戳',
  `subject_code` varchar(32) DEFAULT NULL COMMENT '主题编码',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `email_send_record_recipient_index` (`recipient`),
  KEY `email_send_record_subject_code_index` (`subject_code`),
  KEY `email_send_record_next_send_time_index` (`next_send_time`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb3 COMMENT='邮件发送记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `file_disk_config`
--

DROP TABLE IF EXISTS `file_disk_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_disk_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(32) DEFAULT NULL COMMENT '磁盘类型(LOCAL,NAS)',
  `max_size` int DEFAULT NULL COMMENT '磁盘总大小(GB)',
  `path` varchar(64) DEFAULT NULL COMMENT '磁盘地址',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  `usable_size` int DEFAULT NULL COMMENT '磁盘可用(GB)',
  PRIMARY KEY (`id`),
  KEY `file_disk_config_type_path_index` (`type`,`path`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3 COMMENT='可存储文件的磁盘配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `file_info`
--

DROP TABLE IF EXISTS `file_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `disk_id` int NOT NULL COMMENT '所属磁盘id',
  `name` varchar(256) DEFAULT NULL COMMENT '文件名称',
  `type` varchar(16) DEFAULT NULL COMMENT '文件类型',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  `create_user_id` int DEFAULT NULL COMMENT '创建用户id',
  `space_id` int DEFAULT NULL COMMENT '所属空间id',
  `size` bigint DEFAULT NULL COMMENT '文件大小(B)',
  `source` varchar(32) NOT NULL COMMENT '文件来源(记事本,云盘,日记)',
  `file_md5` varchar(64) DEFAULT NULL COMMENT '文件MD5值',
  `mark_delete` int NOT NULL DEFAULT '0' COMMENT '标记删除(1是,0否)',
  `delete_time` int DEFAULT NULL COMMENT '执行删除时间(10位长度)',
  `media_type` varchar(128) DEFAULT NULL COMMENT '文件多媒体类型',
  `before` int NOT NULL DEFAULT '0' COMMENT '是否是上传之前,意味着这个文件还未上传成功(1是0否)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `file_info_file_md5_space_id_source_uindex` (`file_md5`,`space_id`,`source`),
  KEY `file_info_user_info_null_fk` (`create_user_id`),
  KEY `file_info_user_space_null_fk` (`space_id`),
  KEY `file_info_source_index` (`source`),
  KEY `file_info_mark_delete_delete_time_index` (`mark_delete`,`delete_time`),
  CONSTRAINT `file_info_user_info_null_fk` FOREIGN KEY (`create_user_id`) REFERENCES `user_info` (`id`),
  CONSTRAINT `file_info_user_space_null_fk` FOREIGN KEY (`space_id`) REFERENCES `user_space` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2678 DEFAULT CHARSET=utf8mb3 COMMENT='文件信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `journal_list`
--

DROP TABLE IF EXISTS `journal_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal_list` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(64) DEFAULT NULL COMMENT '日记标题',
  `body` longtext COMMENT '记录内容',
  `happen_time` varchar(16) DEFAULT NULL COMMENT '日记发生日期(yyyy-MM-dd)',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建日期',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改日期',
  `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
  `space_id` int DEFAULT NULL COMMENT '所属空间id',
  PRIMARY KEY (`id`),
  KEY `journal_list_space_id_index` (`space_id`)
) ENGINE=InnoDB AUTO_INCREMENT=129 DEFAULT CHARSET=utf8mb3 COMMENT='日记列表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `journal_list_files`
--

DROP TABLE IF EXISTS `journal_list_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal_list_files` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `journal_id` int DEFAULT NULL COMMENT '日记id',
  `file_md5` varchar(64) DEFAULT NULL COMMENT '文件md5值',
  `file_name` varchar(256) DEFAULT NULL COMMENT '文件名',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建日期',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改日期',
  `media_type` varchar(128) DEFAULT NULL COMMENT '多媒体类型',
  PRIMARY KEY (`id`),
  KEY `journal_list_files_journal_list_id_fk` (`journal_id`),
  CONSTRAINT `journal_list_files_journal_list_id_fk` FOREIGN KEY (`journal_id`) REFERENCES `journal_list` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=254 DEFAULT CHARSET=utf8mb3 COMMENT='日记关联的文件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `netdisk_directory`
--

DROP TABLE IF EXISTS `netdisk_directory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `netdisk_directory` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(32) NOT NULL COMMENT '类型',
  `file_md5` varchar(64) DEFAULT NULL COMMENT '文件md5值',
  `pid` int DEFAULT NULL COMMENT '目录的上级id',
  `space_id` int DEFAULT NULL COMMENT '所属空间id',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  `name` varchar(256) DEFAULT NULL COMMENT '目录名称',
  `media_type` varchar(128) DEFAULT NULL COMMENT '文件多媒体类型',
  PRIMARY KEY (`id`),
  UNIQUE KEY `netdisk_directory_name_space_id_pid_uindex` (`name`,`space_id`,`pid`),
  KEY `netdisk_directory_type_index` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=2377 DEFAULT CHARSET=utf8mb3 COMMENT='网盘文件目录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notebook_note`
--

DROP TABLE IF EXISTS `notebook_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notebook_note` (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  `space_id` int NOT NULL COMMENT '所属空间id',
  `text` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'markdown内容',
  `html` longtext COLLATE utf8mb4_unicode_ci COMMENT 'markdown生成的html预览',
  `keyword` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关键字,截取markdown前部分',
  `tag` int DEFAULT NULL COMMENT '标签',
  `create_user_id` int DEFAULT NULL COMMENT '创建用户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `notebook_note_id_uindex` (`id`),
  KEY `notebook_note_space_id_index` (`space_id`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备忘录-笔记';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `od_record`
--

DROP TABLE IF EXISTS `od_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `od_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gid` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'aria2文件下载记录唯一标识',
  `file_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件保存名称',
  `target_id` int NOT NULL COMMENT '网盘目录ID',
  `space_id` int NOT NULL COMMENT '所属空间id',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  `uri_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'uri类型',
  `msg` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='离线下载记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `share_link`
--

DROP TABLE IF EXISTS `share_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `share_link` (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  `space_id` int DEFAULT NULL COMMENT '空间id',
  `uuid` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '链接uuid',
  `password` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '链接密码,空则不需要密码',
  `body_type` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容类型',
  `invalid_time` int DEFAULT NULL COMMENT '失效时间戳10位',
  `body_id` int DEFAULT NULL COMMENT '链接指向内容id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `share_link_id_uindex` (`id`),
  UNIQUE KEY `share_link_uuid_uindex` (`uuid`),
  KEY `share_link_invalid_time_index` (`invalid_time`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享链接';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_captcha`
--

DROP TABLE IF EXISTS `user_captcha`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_captcha` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱号',
  `captcha` varchar(6) DEFAULT NULL COMMENT '验证码',
  `captcha_expire_time` int DEFAULT NULL COMMENT '验证码到期时间戳(10位)',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `user_captcha_email_captcha_captcha_expire_time_index` (`email`,`captcha`,`captcha_expire_time`),
  KEY `user_captcha_email_captcha_expire_time_index` (`email`,`captcha_expire_time`),
  KEY `user_captcha_email_index` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb3 COMMENT='用户验证码存储';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_info`
--

DROP TABLE IF EXISTS `user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `email` varchar(64) DEFAULT NULL COMMENT '用户邮箱',
  `name` varchar(32) DEFAULT NULL COMMENT '用户名称',
  `password` varchar(32) NOT NULL COMMENT '密码',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  `role` varchar(32) NOT NULL DEFAULT 'COMMON' COMMENT '账户角色',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_info_pk` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb3 COMMENT='用户信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_space`
--

DROP TABLE IF EXISTS `user_space`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_space` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `max_size` int DEFAULT NULL COMMENT '最大空间大小(MB)',
  `code` varchar(64) DEFAULT NULL COMMENT '空间编码',
  `use_size` int DEFAULT NULL COMMENT '已使用空间大小(MB)',
  `title` varchar(64) NOT NULL COMMENT '空间主题',
  `create_user_id` int DEFAULT NULL COMMENT '空间创建用户id',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_space_pk` (`code`),
  UNIQUE KEY `user_space_create_user_id_uindex` (`create_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3 COMMENT='用户空间';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_space_rel`
--

DROP TABLE IF EXISTS `user_space_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_space_rel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_user_id` int NOT NULL COMMENT '空间的创建用户id',
  `space_id` int NOT NULL COMMENT '空间id',
  `user_id` int NOT NULL COMMENT '用户id',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  `role` varchar(32) DEFAULT NULL COMMENT '空间权限',
  `state` varchar(16) DEFAULT 'USE' COMMENT '关系状态',
  `title` varchar(64) NOT NULL COMMENT '空间主题',
  PRIMARY KEY (`id`),
  KEY `user_space_rel_create_user_id_index` (`create_user_id`),
  KEY `user_space_rel_space_id_index` (`space_id`),
  KEY `user_space_rel_user_id_index` (`user_id`),
  CONSTRAINT `user_space_rel_user_info_null_fk` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`),
  CONSTRAINT `user_space_rel_user_space_null_fk` FOREIGN KEY (`space_id`) REFERENCES `user_space` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb3 COMMENT='用户与空间关联关系';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-09-20 17:37:31
