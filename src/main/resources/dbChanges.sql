ALTER TABLE `spt`.`school` 
ADD COLUMN `lessons_start_date` DATE NULL DEFAULT NULL AFTER `transactions_start_date`,
ADD COLUMN `lessons_end_date` DATE NULL DEFAULT NULL AFTER `lessons_start_date`;
