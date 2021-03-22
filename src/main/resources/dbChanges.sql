DELETE FROM `spt`.`permissions` WHERE (`java_class_name` = 'MessagesView') and (`permissions` = 'меню,добавление,изменение,удаление');
delete FROM spt.user_permission where permissions like '%MessagesView%';
ALTER TABLE `spt`.`discount_order_messages` 
CHANGE COLUMN `message` `message` VARCHAR(350) CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NULL DEFAULT NULL ;
ALTER TABLE `spt`.`discount_order_messages` 
DROP FOREIGN KEY `fk_discount_order_messages_status101`,
DROP FOREIGN KEY `fk_discount_order_messages_employee10`;
ALTER TABLE `spt`.`discount_order_messages` 
DROP COLUMN `message_status_id`,
DROP COLUMN `to_employee_id`,
DROP INDEX `fk_discount_order_messages_status1_idx` ,
DROP INDEX `fk_discount_order_messages_employee1_idx` ;
ALTER TABLE `spt`.`discount_order_messages` 
RENAME TO  `spt`.`order_messages` ;
DROP TABLE `spt`.`student_returns`;
CREATE TABLE `employee_message` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_messages_id` int NOT NULL,
  `employee_id` int NOT NULL,
  `message_status_id` int NOT NULL,
  `modification_date` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `spt`.`employee_message` 
ADD INDEX `index2` (`order_messages_id` ASC) INVISIBLE,
ADD INDEX `index3` (`employee_id` ASC) INVISIBLE,
ADD INDEX `index4` (`message_status_id` ASC) INVISIBLE,
ADD UNIQUE INDEX `unq_employee_message_emp_mes` (`order_messages_id` ASC, `employee_id` ASC) VISIBLE;

ALTER TABLE `spt`.`employee_message` 
ADD CONSTRAINT `fkemployee_message_employee`
  FOREIGN KEY (`employee_id`)
  REFERENCES `spt`.`employee` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION,
ADD CONSTRAINT `fkemployee_message_status`
  FOREIGN KEY (`message_status_id`)
  REFERENCES `spt`.`message_status` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION,
ADD CONSTRAINT `fkemployee_message_order_message`
  FOREIGN KEY (`order_messages_id`)
  REFERENCES `spt`.`order_messages` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION;

INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'SendDiscountOrder:меню,добавление,удаление');
UPDATE `spt`.`user_permission` SET `permissions` = 'SendOrder:меню,добавление,удаление' WHERE (`role_name` = 'sapat_secretary') and (`permissions` = 'SendDiscountOrder:меню,добавление,удаление');
UPDATE `spt`.`user_permission` SET `permissions` = 'SendOrder:меню,добавление,удаление' WHERE (`role_name` = 'admin') and (`permissions` = 'SendDiscountOrder:меню,добавление,удаление');
UPDATE `spt`.`user_permission` SET `permissions` = 'SendOrderView:меню,добавление,удаление' WHERE (`role_name` = 'sapat_secretary') and (`permissions` = 'SendOrder:меню,добавление,удаление');
UPDATE `spt`.`user_permission` SET `permissions` = 'SendOrderView:меню,добавление,удаление' WHERE (`role_name` = 'admin') and (`permissions` = 'SendOrder:меню,добавление,удаление');
