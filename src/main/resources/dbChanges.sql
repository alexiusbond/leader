ALTER TABLE `spt`.`order_messages`
    ADD COLUMN `discount` INT(3) NOT NULL DEFAULT '0' AFTER `employee_id`;
ALTER TABLE `spt`.`order_messages`
    CHANGE COLUMN `discount` `discount` DOUBLE(5,2) NOT NULL DEFAULT '0.0' ;
UPDATE `spt`.`order_messages` SET `discount` = '20' WHERE (`id` = '3');
UPDATE `spt`.`order_messages` SET `discount` = '30' WHERE (`id` = '4');
UPDATE `spt`.`order_messages` SET `discount` = '50' WHERE (`id` = '5');
UPDATE `spt`.`order_messages` SET `discount` = '50' WHERE (`id` = '6');
UPDATE `spt`.`order_messages` SET `discount` = '20' WHERE (`id` = '7');
UPDATE `spt`.`order_messages` SET `discount` = '20' WHERE (`id` = '8');
UPDATE `spt`.`order_messages` SET `discount` = '40' WHERE (`id` = '9');
UPDATE `spt`.`order_messages` SET `discount` = '30' WHERE (`id` = '10');
UPDATE `spt`.`order_messages` SET `discount` = '25' WHERE (`id` = '11');
UPDATE `spt`.`order_messages` SET `discount` = '50' WHERE (`id` = '12');
UPDATE `spt`.`order_messages` SET `discount` = '40' WHERE (`id` = '14');
