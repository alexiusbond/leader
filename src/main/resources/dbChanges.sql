ALTER TABLE `spt`.`education_language` 
ADD COLUMN `order_num` INT NOT NULL DEFAULT '1' AFTER `name`;
UPDATE `spt`.`education_language` SET `order_num` = '500' WHERE (`id` = '2');
ALTER TABLE `spt`.`class_number` 
ADD COLUMN `order_num` INT NOT NULL DEFAULT '0' AFTER `name`;
UPDATE `spt`.`class_number` SET `order_num` = '0' WHERE (`id` = '2');
UPDATE `spt`.`class_number` SET `order_num` = '1' WHERE (`id` = '3');
UPDATE `spt`.`class_number` SET `order_num` = '2' WHERE (`id` = '4');
UPDATE `spt`.`class_number` SET `order_num` = '3' WHERE (`id` = '5');
UPDATE `spt`.`class_number` SET `order_num` = '4' WHERE (`id` = '6');
UPDATE `spt`.`class_number` SET `order_num` = '5' WHERE (`id` = '7');
UPDATE `spt`.`class_number` SET `order_num` = '0' WHERE (`id` = '8');
UPDATE `spt`.`class_number` SET `order_num` = '1' WHERE (`id` = '9');
UPDATE `spt`.`class_number` SET `order_num` = '2' WHERE (`id` = '10');
UPDATE `spt`.`class_number` SET `order_num` = '3' WHERE (`id` = '11');
UPDATE `spt`.`class_number` SET `order_num` = '4' WHERE (`id` = '12');
UPDATE `spt`.`class_number` SET `order_num` = '5' WHERE (`id` = '13');
