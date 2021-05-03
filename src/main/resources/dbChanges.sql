ALTER TABLE `spt`.`order_messages` 
DROP FOREIGN KEY `fk_discount_order_messages_student101`;
ALTER TABLE `spt`.`order_messages` 
ADD COLUMN `student` VARCHAR(200) NULL DEFAULT NULL AFTER `discount`,
CHANGE COLUMN `student_id` `student_id` INT NULL DEFAULT NULL ;
ALTER TABLE `spt`.`order_messages` 
ADD CONSTRAINT `fk_discount_order_messages_student101`
  FOREIGN KEY (`student_id`)
  REFERENCES `spt`.`student` (`id`);
