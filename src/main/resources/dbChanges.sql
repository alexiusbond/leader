ALTER TABLE `spt`.`education_language` 
ADD COLUMN `order_num` INT NOT NULL DEFAULT '1' AFTER `name`;
UPDATE `spt`.`education_language` SET `order_num` = '500' WHERE (`id` = '2');
ALTER TABLE `spt`.`class_number` 
ADD COLUMN `order_num` INT NOT NULL DEFAULT '0' AFTER `name`;
