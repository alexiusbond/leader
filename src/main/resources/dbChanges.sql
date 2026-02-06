ALTER TABLE `sky`.`student`
    ADD COLUMN `inn` VARCHAR(20) NULL AFTER `photo`;

ALTER TABLE `sky`.`student`
DROP COLUMN `middle_name`;
