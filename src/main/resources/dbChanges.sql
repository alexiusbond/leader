ALTER TABLE `spt`.`education_language` 
ADD COLUMN `order_number_max` INT NULL AFTER `order_number_min`,
CHANGE COLUMN `order_num` `order_number_min` INT NOT NULL DEFAULT '1' ;
ALTER TABLE `spt`.`education_language` 
RENAME TO  `spt`.`class_type` ;
ALTER TABLE `spt`.`class_name` 
DROP FOREIGN KEY `fk_class_name_langl1`;
ALTER TABLE `spt`.`class_name` 
CHANGE COLUMN `education_language_id` `class_type_id` INT NOT NULL DEFAULT '1' ;
ALTER TABLE `spt`.`class_name` 
ADD CONSTRAINT `fk_class_name_langl1`
  FOREIGN KEY (`class_type_id`)
  REFERENCES `spt`.`class_type` (`id`)
  ON DELETE RESTRICT;
UPDATE `spt`.`class_type` SET `name` = 'Кыргызский класс' WHERE (`id` = '1');
UPDATE `spt`.`class_type` SET `name` = 'Русский класс' WHERE (`id` = '2');
UPDATE `spt`.`class_type` SET `name` = 'Английский класс' WHERE (`id` = '3');
UPDATE `spt`.`class_type` SET `order_number_max` = '399' WHERE (`id` = '1');
UPDATE `spt`.`class_type` SET `order_number_max` = '999' WHERE (`id` = '3');
UPDATE `spt`.`class_type` SET `order_number_max` = '599' WHERE (`id` = '2');
UPDATE `spt`.`class_type` SET `order_number_max` = '799' WHERE (`id` = '4');
UPDATE `spt`.`class_type` SET `order_number_max` = '999' WHERE (`id` = '5');
