ALTER TABLE `spt`.`student_contract`
    ADD COLUMN `creation_date` DATE NULL DEFAULT NULL AFTER `contract_number`;
update student_contract set creation_date = date(modification_date);
ALTER TABLE `spt`.`student_contract`
    CHANGE COLUMN `creation_date` `creation_date` DATE NOT NULL ;
