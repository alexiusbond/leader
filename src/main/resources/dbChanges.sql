update spt.user_permission set permissions = concat(permissions,',отчет по долгам и погашениям') where permissions like 'ReportsView:%' and role_name not in ('bank', 'hr');

INSERT INTO `spt`.`acc_type` (`id`, `name`, `sign`) VALUES ('5', 'Приход или расход', '0');
INSERT INTO `spt`.`acc_category` (`name`, `code`, `acc_type_id`, `activity_status_id`, `modified_employee_id`) VALUES ('TEST BOTH PARENT', '5577', '5', '2', '1');
ALTER TABLE `spt`.`acc_transactions`
    ADD COLUMN `acc_type_id` INT NOT NULL DEFAULT '1' AFTER `acc_invoice_id`;
ALTER TABLE `spt`.`acc_transactions`
    ADD INDEX `fk_acc_transactions_type_idx` (`acc_type_id` ASC) VISIBLE;
ALTER TABLE `spt`.`acc_transactions`
DROP FOREIGN KEY `fk_acc_category12`,
DROP FOREIGN KEY `fk_acc_transactions_1`,
DROP FOREIGN KEY `fk_acc_transactions_2`,
DROP FOREIGN KEY `fk_acc_transactions_3`,
DROP FOREIGN KEY `fk_acc_transactions_4`,
DROP FOREIGN KEY `fk_acc_transactions_5`,
DROP FOREIGN KEY `fk_empl12`,
DROP FOREIGN KEY `fk_school_id`;
ALTER TABLE `spt`.`acc_transactions`
;
ALTER TABLE `spt`.`acc_transactions` RENAME INDEX `fk_acc_transactions_1` TO `fk_acc_transactions_currency`;
ALTER TABLE `spt`.`acc_transactions` ALTER INDEX `fk_acc_transactions_currency` VISIBLE;
ALTER TABLE `spt`.`acc_transactions` RENAME INDEX `fk_acc_transactions_5` TO `fk_acc_transactions_acc_invoice`;
ALTER TABLE `spt`.`acc_transactions` ALTER INDEX `fk_acc_transactions_acc_invoice` VISIBLE;
ALTER TABLE `spt`.`acc_transactions`
    ADD CONSTRAINT `fk_transactions_acc_category`
        FOREIGN KEY (`acc_category_id`)
            REFERENCES `spt`.`acc_category` (`id`)
            ON DELETE RESTRICT,
ADD CONSTRAINT `fk_acc_transactions_currency`
  FOREIGN KEY (`acc_currency_id`)
  REFERENCES `spt`.`acc_currency` (`id`),
ADD CONSTRAINT `fk_acc_transactions_student_payments`
  FOREIGN KEY (`student_payments_id`)
  REFERENCES `spt`.`student_payments` (`id`),
ADD CONSTRAINT `fk_acc_transactions_dp_invoice`
  FOREIGN KEY (`dp_invoice_id`)
  REFERENCES `spt`.`dp_invoice` (`id`),
ADD CONSTRAINT `fk_acc_transactions_from_to_employee`
  FOREIGN KEY (`from_to_employee_id`)
  REFERENCES `spt`.`employee` (`id`),
ADD CONSTRAINT `fk_acc_transactions_acc_invoice`
  FOREIGN KEY (`acc_invoice_id`)
  REFERENCES `spt`.`acc_invoice` (`id`)
  ON DELETE RESTRICT,
ADD CONSTRAINT `fk_acc_transactions_employee`
  FOREIGN KEY (`employee_id`)
  REFERENCES `spt`.`employee` (`id`),
ADD CONSTRAINT `fk_acc_transactions_school_id`
  FOREIGN KEY (`school_id`)
  REFERENCES `spt`.`school` (`id`),
ADD CONSTRAINT `fk_acc_transactions_type`
  FOREIGN KEY (`acc_type_id`)
  REFERENCES `spt`.`acc_type` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION;
update acc_transactions t set t.acc_type_id = (select acc_type_id from acc_category where id = t.acc_category_id);
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'IncomesExpensesDefinitionView:показ в меню,добавление,изменение,удаление');
INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('IncomesExpensesDefinitionView', 'Определение двусторонних категорий', 'показ в меню,добавление,изменение,удаление');
ALTER TABLE `spt`.`student_relatives`
    CHANGE COLUMN `adress` `address` TEXT CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NULL DEFAULT NULL ;
ALTER TABLE `spt`.`school`
    CHANGE COLUMN `adress` `address` TEXT CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NULL DEFAULT NULL ;
