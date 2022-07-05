ALTER TABLE `spt`.`order_messages`
    ADD COLUMN `year_id` INT NULL DEFAULT NULL AFTER `student`,
ADD INDEX `fk_discount_order_messages_year_idx` (`year_id` ASC) VISIBLE;

ALTER TABLE `spt`.`order_messages`
    ADD CONSTRAINT `fk_discount_order_messages_year`
        FOREIGN KEY (`year_id`)
            REFERENCES `spt`.`year` (`id`)
            ON DELETE RESTRICT
            ON UPDATE NO ACTION;
update spt.order_messages set year_id = 8 where order_content like '%2022-2023%';
update spt.order_messages set year_id = 7 where order_content like '%2021-2022%';
update spt.order_messages set year_id = 6 where order_content like '%2020-2021%';
update spt.order_messages set year_id = 5 where order_content like '%2019-2020%';
UPDATE `spt`.`order_messages` SET `year_id` = '5' WHERE (`id` = '991');
ALTER TABLE `spt`.`order_messages`
DROP FOREIGN KEY `fk_discount_order_messages_year`;
ALTER TABLE `spt`.`order_messages`
    CHANGE COLUMN `year_id` `year_id` INT NOT NULL ;
ALTER TABLE `spt`.`order_messages`
    ADD CONSTRAINT `fk_discount_order_messages_year`
        FOREIGN KEY (`year_id`)
            REFERENCES `spt`.`year` (`id`)
            ON DELETE RESTRICT;
