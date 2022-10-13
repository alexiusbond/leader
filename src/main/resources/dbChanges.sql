ALTER TABLE `spt`.`hr_employee_contacts`
    ADD COLUMN `inn` VARCHAR(20) NULL DEFAULT NULL AFTER `passport_date`;
