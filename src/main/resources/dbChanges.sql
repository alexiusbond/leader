ALTER TABLE `spt`.`employee`
    ADD COLUMN `priority` TINYINT(1) NOT NULL DEFAULT ''1'' AFTER `modification_date`;
UPDATE `spt`.`employee` SET `priority` = '2' WHERE (`id` = '47') and (`login` = '100036') and (`nationality_id` = '176') and (`gender_id` = '1') and (`hr_martial_status_id` = '2') and (`employee_id` = '47');
@