-- =========================================================
-- 云记录 · 建表脚本（MySQL 9.5 · utf8mb4）
-- 10 张表 + mood 种子数据
-- 通用约定：主键 BIGINT AUTO_INCREMENT；created_at/updated_at 由 MyBatis-Plus 自动填充
-- =========================================================
SET NAMES utf8mb4;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `email`       VARCHAR(128) NOT NULL COMMENT '登录邮箱',
  `password`    VARCHAR(128) NOT NULL COMMENT 'BCrypt 密文',
  `nickname`    VARCHAR(64)  NOT NULL COMMENT '昵称',
  `avatar_url`  VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  `bio`         VARCHAR(255) DEFAULT NULL COMMENT '签名/简介',
  `created_at`  DATETIME     NOT NULL COMMENT '注册时间',
  `updated_at`  DATETIME     NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- 2. 记录卡
CREATE TABLE IF NOT EXISTS `record_card` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`    BIGINT      NOT NULL COMMENT '所属用户',
  `content`    TEXT        NOT NULL COMMENT '正文（最终展示内容）',
  `mood_id`    INT         DEFAULT NULL COMMENT '心情（关联 mood）',
  `style_key`  VARCHAR(64) DEFAULT NULL COMMENT '卡片风格（warm-sunny 等）',
  `ai_type`    VARCHAR(16) DEFAULT NULL COMMENT '原创 / AI润色 / AI扩写',
  `deleted`    TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` DATETIME    NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME    NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_record_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='记录卡';

-- 3. 卡片图片
CREATE TABLE IF NOT EXISTS `card_image` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `card_id`    BIGINT       NOT NULL COMMENT '所属记录卡',
  `url`        VARCHAR(255) NOT NULL COMMENT '图片地址',
  `sort_order` INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME     NOT NULL COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_image_card` (`card_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='记录卡图片';

-- 4. 心情字典（种子数据见下方）
CREATE TABLE IF NOT EXISTS `mood` (
  `id`           INT         NOT NULL COMMENT '主键',
  `name`         VARCHAR(32) NOT NULL COMMENT '心情名',
  `emoji`        VARCHAR(16) NOT NULL DEFAULT '' COMMENT '图标（本项目去 emoji，留空）',
  `weather_icon` VARCHAR(16) DEFAULT NULL COMMENT '心情点样式（md-sunny 等）',
  `sort_order`   INT         NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='心情字典';

-- 5. 理想卡
CREATE TABLE IF NOT EXISTS `ideal` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '所属用户',
  `title`       VARCHAR(128) NOT NULL COMMENT '理想标题',
  `description` TEXT         DEFAULT NULL COMMENT '理想描述',
  `visibility`  VARCHAR(16)  NOT NULL DEFAULT 'PRIVATE' COMMENT 'PRIVATE / PUBLIC',
  `status`      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DONE',
  `target_date` DATE         DEFAULT NULL COMMENT '目标日期',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at`  DATETIME     NOT NULL COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ideal_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='理想卡';

-- 6. 理想任务（AI 拆解 / 手动）
CREATE TABLE IF NOT EXISTS `ideal_task` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ideal_id`     BIGINT       NOT NULL COMMENT '所属理想',
  `content`      VARCHAR(255) NOT NULL COMMENT '任务内容',
  `is_completed` TINYINT      NOT NULL DEFAULT 0 COMMENT '是否完成',
  `sort_order`   INT          NOT NULL DEFAULT 0 COMMENT '顺序',
  `created_at`   DATETIME     NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_ideal` (`ideal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='理想任务';

-- 7. 每日打卡
CREATE TABLE IF NOT EXISTS `check_in` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ideal_id`   BIGINT   NOT NULL COMMENT '所属理想',
  `user_id`    BIGINT   NOT NULL COMMENT '冗余用户',
  `check_date` DATE     NOT NULL COMMENT '打卡日期',
  `status`     TINYINT  NOT NULL DEFAULT 1 COMMENT '1 完成 / 0 未完成',
  `created_at` DATETIME NOT NULL COMMENT '打卡时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_checkin_ideal_date` (`ideal_id`, `check_date`),
  KEY `idx_checkin_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日打卡';

-- 8. 时间胶囊
CREATE TABLE IF NOT EXISTS `time_capsule` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '所属用户',
  `title`       VARCHAR(128) DEFAULT NULL COMMENT '标题',
  `content`     TEXT         NOT NULL COMMENT '信件内容',
  `open_date`   DATE         NOT NULL COMMENT '开启日期',
  `is_opened`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已开启',
  `remind_sent` TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已发提醒',
  `created_at`  DATETIME     NOT NULL COMMENT '写入时间',
  PRIMARY KEY (`id`),
  KEY `idx_capsule_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='时间胶囊';

-- 9. AI 调用次数
CREATE TABLE IF NOT EXISTS `ai_usage_log` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`    BIGINT      NOT NULL COMMENT '用户',
  `scene`      VARCHAR(32) NOT NULL COMMENT 'EXPAND / STYLE / IDEAL_SPLIT',
  `created_at` DATETIME    NOT NULL COMMENT '调用时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_user_scene` (`user_id`, `scene`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 调用次数';

-- 10. 邮箱验证码
CREATE TABLE IF NOT EXISTS `email_code` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `email`      VARCHAR(128) NOT NULL COMMENT '邮箱',
  `code`       VARCHAR(8)   NOT NULL COMMENT '6 位验证码',
  `purpose`    VARCHAR(16)  NOT NULL DEFAULT 'REGISTER' COMMENT 'REGISTER / RESET',
  `expires_at` DATETIME     NOT NULL COMMENT '过期时间',
  `created_at` DATETIME     NOT NULL COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_emailcode_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='邮箱验证码';

-- 心情种子数据（去 emoji，心情点样式与原型一致）
INSERT IGNORE INTO `mood` (`id`, `name`, `emoji`, `weather_icon`, `sort_order`) VALUES
  (1, '开心',   '', 'md-sunny', 1),
  (2, '平静',   '', 'md-calm',  2),
  (3, '小确幸', '', 'md-joy',   3),
  (4, '有点累', '', 'md-moon',  4),
  (5, 'emo',    '', 'md-rain',  5),
  (6, '焦虑',   '', 'md-storm', 6);
