ALTER TABLE `spt`.`acc_invoice` 
ADD COLUMN `note2` VARCHAR(250) NULL DEFAULT NULL AFTER `is_confirmed`;
