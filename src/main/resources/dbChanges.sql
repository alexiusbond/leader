ALTER TABLE `spt`.`year` 
ADD COLUMN `installment_date_limit` DECIMAL(15) NOT NULL DEFAULT '1680220800000' AFTER `is_last`;
UPDATE `spt`.`year` SET `installment_date_limit` = '1711843200000' WHERE (`id` = '9');
