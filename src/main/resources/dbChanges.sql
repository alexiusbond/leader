ALTER TABLE `student_discount`     ADD COLUMN `creation_date` DATE NULL DEFAULT NULL;
update student_discount set creation_date = date(modification_date);
ALTER TABLE  `student_discount`     CHANGE COLUMN `creation_date` `creation_date` DATE NOT NULL;

ALTER TABLE `student_correction`     ADD COLUMN `creation_date` DATE NULL DEFAULT NULL;
update student_correction set creation_date = date(modification_date);
ALTER TABLE  `student_correction`     CHANGE COLUMN `creation_date` `creation_date` DATE NOT NULL;

ALTER TABLE `spt`.`payment_category`
    CHANGE COLUMN `name` `name` VARCHAR(50) CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NOT NULL ;
