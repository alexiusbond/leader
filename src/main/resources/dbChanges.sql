ALTER TABLE `spt`.`acc_category` 
ADD COLUMN `modified_employee_id` INT NOT NULL DEFAULT '1' AFTER `employee_id`;
ALTER TABLE `spt`.`acc_category` 
ADD INDEX `fk_acc_category_mod_empl_2_idx` (`modified_employee_id` ASC) VISIBLE;
ALTER TABLE `spt`.`acc_category` 
ADD CONSTRAINT `fk_acc_category_mod_empl_id`
  FOREIGN KEY (`modified_employee_id`)
  REFERENCES `spt`.`employee` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION;

DROP TRIGGER IF EXISTS `spt`.`u_acc_category`;

DELIMITER $$
USE `spt`$$
CREATE DEFINER=`root`@`localhost` TRIGGER `u_acc_category` AFTER UPDATE ON `acc_category` FOR EACH ROW BEGIN
if (new.parent_id in (select ac.id from hr_salary_category as sc left join acc_category as ac on ac.parent_id = sc.acc_category_id) and new.parent_id != old.parent_id) then
INSERT INTO data_log (row_id, table_name, column_name, action, old_field, new_field, 
            datetime, employee_id)
values(new.id,'acc_category','contract_type','update', 
(select concat('name: ', old.name, ', contract type: ', sc.name) from acc_category as ac left join hr_salary_category as sc on ac.parent_id = sc.acc_category_id where ac.id = old.parent_id limit 1), 
(select concat('name: ', new.name, ', contract type: ', sc.name) from acc_category as ac left join hr_salary_category as sc on ac.parent_id = sc.acc_category_id where ac.id = new.parent_id  limit 1),
            now(), new.modified_employee_id);
        end if;
END$$
DELIMITER ;
