-- =====================================
-- Rentigo 完整建表及触发器脚本（MySQL 8+）
-- =====================================

-- -- 1. 创建数据库并切换
-- CREATE DATABASE IF NOT EXISTS rentigo
--   DEFAULT CHARACTER SET utf8mb4
--   COLLATE utf8mb4_unicode_ci;
-- USE rentigo;

-- 2. 角色表 & 数据
CREATE TABLE IF NOT EXISTS role (
  id   TINYINT        PRIMARY KEY,
  name VARCHAR(32)    NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO role(id,name) VALUES
  (1,'USER'),
  (2,'OPERATOR'),
  (3,'ADMIN');

-- 3. 用户表
CREATE TABLE IF NOT EXISTS user (
  id             BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  username       VARCHAR(64)       NOT NULL UNIQUE,
  email          VARCHAR(128)      NOT NULL UNIQUE,
  password_hash  CHAR(60)          NOT NULL,
  status         TINYINT           NOT NULL DEFAULT 0,
  phone          VARCHAR(32)       NULL COMMENT '手机号码',
  real_name      VARCHAR(64)       NULL COMMENT '真实姓名',
  id_card        VARCHAR(32)       NULL COMMENT '身份证号',
  created_at     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,
  reserved_field1 VARCHAR(255)     NULL COMMENT '预留字段1',
  reserved_field2 VARCHAR(255)     NULL COMMENT '预留字段2',
  reserved_field3 DECIMAL(10,2)    NULL COMMENT '预留数值字段'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_user_email    ON user(email);
CREATE INDEX idx_user_username ON user(username);

-- 4. 用户-角色映射
CREATE TABLE IF NOT EXISTS user_role (
  user_id BIGINT UNSIGNED NOT NULL,
  role_id TINYINT         NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id)
    REFERENCES user(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id)
    REFERENCES role(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 车辆类型表 & 数据
CREATE TABLE IF NOT EXISTS vehicle_type (
  id             TINYINT         PRIMARY KEY,
  type_name      VARCHAR(32)     NOT NULL UNIQUE,
  deposit_amount DECIMAL(10,2)   NOT NULL DEFAULT 0.00 COMMENT '押金标准金额',
  created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  reserved_field1 VARCHAR(255)   NULL COMMENT '预留字段1',
  reserved_field2 VARCHAR(255)   NULL COMMENT '预留字段2',
  reserved_field3 DECIMAL(10,2)  NULL COMMENT '预留数值字段'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO vehicle_type(id, type_name, deposit_amount) VALUES
  (1,  'Sedan',        500.00),
  (2,  'SUV',          800.00),
  (3,  'Truck',        1000.00),
  (4,  'MPV',          700.00),
  (5,  'Hatchback',    400.00),
  (6,  'Crossover',    600.00),
  (7,  'Coupe',        600.00),
  (8,  'Convertible',  800.00),
  (9,  'Station Wagon', 550.00),
  (10, 'Pickup Truck', 900.00),
  (11, 'Van',          750.00);

-- 6. 服务中心地点表
CREATE TABLE IF NOT EXISTS location (
  id            SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  city          VARCHAR(64)       NOT NULL,
  center_name   VARCHAR(64)       NOT NULL,
  address       VARCHAR(255)      NOT NULL,
  lng           DECIMAL(10,7),
  lat           DECIMAL(10,7),
  contact_phone VARCHAR(32)       NULL COMMENT '联系电话',
  business_hours VARCHAR(100)     NULL COMMENT '营业时间',
  created_at    DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  reserved_field1 VARCHAR(255)    NULL COMMENT '预留字段1',
  reserved_field2 VARCHAR(255)    NULL COMMENT '预留字段2'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 车辆表
CREATE TABLE IF NOT EXISTS vehicle (
  id               BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  model            VARCHAR(64)       NOT NULL,
  vehicle_type_id  TINYINT           NOT NULL,
  location_id      SMALLINT UNSIGNED NOT NULL,
  color            VARCHAR(32),
  daily_price      DECIMAL(10,2)     NOT NULL,
  status           TINYINT           NOT NULL DEFAULT 0 COMMENT '0=AVAILABLE,1=RENTED,2=MAINTAIN',
  license_plate    VARCHAR(32)       UNIQUE,
  created_at       DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,
  reserved_field1  VARCHAR(255)      NULL COMMENT '预留字段1',
  reserved_field2  VARCHAR(255)      NULL COMMENT '预留字段2',
  reserved_field3  DECIMAL(10,2)     NULL COMMENT '预留数值字段',
  CONSTRAINT fk_vehicle_type FOREIGN KEY (vehicle_type_id)
    REFERENCES vehicle_type(id),
  CONSTRAINT fk_vehicle_loc  FOREIGN KEY (location_id)
    REFERENCES location(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_vehicle_loc_status ON vehicle(location_id, status);
CREATE INDEX idx_vehicle_type_deposit ON vehicle_type(deposit_amount);

-- 索引优化
CREATE INDEX idx_payment_type_status ON payment(payment_type, status);
CREATE INDEX idx_payment_user ON payment(user_id);

-- 8. 租赁表（含已支付态 & 乐观锁 & 并发锁定 & 押金管理）
CREATE TABLE IF NOT EXISTS rentals (
  id                   BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id              BIGINT UNSIGNED NOT NULL,
  vehicle_id           BIGINT UNSIGNED NOT NULL,
  start_time           DATETIME          NOT NULL,
  end_time             DATETIME          NOT NULL,
  actual_return_time   DATETIME          NULL,
  status               TINYINT           NOT NULL DEFAULT 0
                        COMMENT '0=PENDING_PAYMENT,1=PAID,2=ACTIVE,3=FINISHED,4=CANCELLED',
  total_amount         DECIMAL(10,2)     NOT NULL,
  deposit_amount       DECIMAL(10,2)     NOT NULL DEFAULT 0.00 COMMENT '本次租赁押金金额',
  deposit_status       TINYINT           NOT NULL DEFAULT 0 COMMENT '押金状态: 0=未收取,1=已收取,2=已退还,3=已没收,4=部分没收',
  deposit_paid_at      DATETIME          NULL COMMENT '押金支付时间',
  deposit_returned_at  DATETIME          NULL COMMENT '押金退还时间',
  overtime_amount      DECIMAL(10,2)     NOT NULL DEFAULT 0.00 COMMENT '超时费用',
  version              INT               NOT NULL DEFAULT 0,
  created_at           DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,
  reserved_field1      VARCHAR(255)      NULL COMMENT '预留字段1',
  reserved_field2      VARCHAR(255)      NULL COMMENT '预留字段2',
  reserved_field3      DECIMAL(10,2)     NULL COMMENT '预留数值字段',
  reserved_field4      DATETIME          NULL COMMENT '预留时间字段',
  CONSTRAINT fk_rental_user    FOREIGN KEY (user_id)
    REFERENCES user(id),
  CONSTRAINT fk_rental_vehicle FOREIGN KEY (vehicle_id)
    REFERENCES vehicle(id),
  CONSTRAINT chk_deposit_status CHECK (deposit_status IN (0,1,2,3,4))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_rental_user          ON rentals(user_id, status);
CREATE INDEX idx_rental_vehicle_stat  ON rentals(vehicle_id, status);
CREATE INDEX idx_rentals_deposit_status ON rentals(deposit_status);
CREATE INDEX idx_rentals_start_end_time ON rentals(start_time, end_time);

-- 9. 支付表
CREATE TABLE IF NOT EXISTS payment (
  id                BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  rental_id         BIGINT UNSIGNED NOT NULL,
  user_id           BIGINT UNSIGNED NOT NULL COMMENT '支付用户ID',
  amount            DECIMAL(10,2)      NOT NULL,
  payment_type      TINYINT            NOT NULL DEFAULT 0 COMMENT '支付类型: 0=租金,1=押金,2=超时费用',
  status            TINYINT            NOT NULL DEFAULT 0 COMMENT '0=INIT,1=PAID,2=FAILED,3=REFUNDED',
  description       VARCHAR(255)       NULL COMMENT '支付描述',
  transaction_id    VARCHAR(100)       NULL COMMENT '交易ID',
  payment_method    VARCHAR(50)        NULL COMMENT '支付方式',
  created_at        DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  reserved_field1   VARCHAR(255)       NULL COMMENT '预留字段1',
  reserved_field2   DECIMAL(10,2)      NULL COMMENT '预留数值字段',
  CONSTRAINT fk_payment_rental FOREIGN KEY (rental_id)
    REFERENCES rentals(id),
  CONSTRAINT fk_payment_user FOREIGN KEY (user_id)
    REFERENCES user(id),
  CONSTRAINT chk_payment_type CHECK (payment_type IN (0,1,2)),
  CONSTRAINT chk_payment_status CHECK (status IN (0,1,2,3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 10. 触发器：并发锁定 & 乐观锁 & 状态机
-- =============================================
DELIMITER $$

-- 10.1 BEFORE INSERT: 锁定车辆行 & 计算总金额 & 押金
CREATE TRIGGER trg_rental_before_insert
BEFORE INSERT ON rentals
FOR EACH ROW
BEGIN
  DECLARE days INT;
  DECLARE vehicle_type_id_val TINYINT;
  DECLARE deposit_val DECIMAL(10,2);
  
  -- 锁定车辆行并获取相关信息
  SELECT v.daily_price, v.vehicle_type_id 
  INTO @daily_price, vehicle_type_id_val
  FROM vehicle v
  WHERE v.id = NEW.vehicle_id
  FOR UPDATE;
  
  -- 获取车型对应的押金标准
  SELECT vt.deposit_amount 
  INTO deposit_val
  FROM vehicle_type vt 
  WHERE vt.id = vehicle_type_id_val;
  
  -- 计算租期天数
  SET days = TIMESTAMPDIFF(DAY, NEW.start_time, NEW.end_time);
  IF days < 1 THEN
    SET days = 1;
  END IF;
  
  -- 设置总金额和押金金额
  SET NEW.total_amount = @daily_price * days;
  SET NEW.deposit_amount = deposit_val;
END$$

-- 10.2 BEFORE UPDATE: 乐观锁 version 自增
CREATE TRIGGER trg_rental_before_update
BEFORE UPDATE ON rentals
FOR EACH ROW
BEGIN
  SET NEW.version = OLD.version + 1;
END$$

-- 10.3 AFTER UPDATE: 状态迁移时同库行锁 & 车辆状态同步
CREATE TRIGGER trg_rental_after_update
AFTER UPDATE ON rentals
FOR EACH ROW
BEGIN
  -- 已支付→激活（提车）时锁车
  IF OLD.status < 2 AND NEW.status = 2 THEN
    UPDATE vehicle
      SET status = 1
      WHERE id = NEW.vehicle_id;
  END IF;
  -- 激活→完成/取消 时释放车辆
  IF OLD.status = 2 AND NEW.status IN (3,4) THEN
    UPDATE vehicle
      SET status = 0
      WHERE id = NEW.vehicle_id;
  END IF;
END$$

DELIMITER ;

-- =======================================
-- 11. 示例数据插入（可选）
-- =======================================

-- 示例地点数据
INSERT IGNORE INTO location (id, city, center_name, address, lng, lat, contact_phone, business_hours) VALUES
(1, 'Beijing', 'Beijing Central', '123 Wangfujing Street', 116.4074, 39.9042, '400-123-4567', '09:00-18:00'),
(2, 'Shanghai', 'Shanghai Pudong', '456 Lujiazui Road', 121.4737, 31.2304, '400-234-5678', '09:00-18:00'),
(3, 'Guangzhou', 'Guangzhou Tianhe', '789 Zhujiang Road', 113.2644, 23.1291, '400-345-6789', '09:00-18:00');

-- 数据库初始化完成标识
SELECT 'Rentigo database initialization completed successfully' as status;