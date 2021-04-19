ALTER TABLE `spt`.`permissions`
DROP INDEX `unq_ind` ;
ALTER TABLE `spt`.`permissions`
    CHANGE COLUMN `permissions` `permissions` VARCHAR(500) CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NOT NULL ;
ALTER TABLE `spt`.`user_permission`
    CHANGE COLUMN `permissions` `permissions` VARCHAR(500) CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NOT NULL ;

UPDATE `spt`.`permissions` SET `caption` = 'Ученики' WHERE (`java_class_name` = 'StudentDefinitionView');
UPDATE `spt`.`permissions` SET `permissions` = 'меню,добавление,изменение,удаление,информация о контракте,информация о задолженностях' WHERE (`java_class_name` = 'StudentDefinitionView');

INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('FamilyTab', 'Ученики - Вкладка информация о семье', 'меню');
INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('ContractTab', 'Ученики - Вкладка контракт', 'меню');
INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('PaymentsTab', 'Ученики - Вкладка оплаты', 'меню,добавление,изменение,удаление,распечатка');
INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('DiscountsTable', 'Ученики - Таблица скидок', 'меню,добавление,изменение,удаление');
INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('GiveAccessoriesTab', 'Ученики - Вкладка выдачи материалов', 'меню');
INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('TakeAccessoriesTab', 'Ученики - Вкладка приема материалов', 'меню');
INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('CallsTab', 'Ученики - Вкладка звонков', 'меню');
UPDATE  `spt`.`permissions` set `permissions` = REPLACE(`permissions`,'меню','показ в меню');
DELETE  from `spt`.`user_permission` WHERE (`role_name` != 'admin') and (`permissions` like 'StudentDefinitionView%');
UPDATE `spt`.`user_permission` SET `permissions` = 'StudentDefinitionView:меню,добавление,изменение,удаление,информация о контракте,информация о задолженностях' WHERE (`role_name` = 'admin') and (`permissions` like 'StudentDefinitionView%');
UPDATE IGNORE `spt`.`user_permission` set `permissions` = REPLACE(`permissions`,'меню','показ в меню');

INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'PaymentsTab:показ в меню,добавление,изменение,удаление,распечатка');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'DiscountsTable:показ в меню,добавление,изменение,удаление');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'FamilyTab:показ в меню');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'ContractTab:показ в меню');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'GiveAccessoriesTab:показ в меню');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'TakeAccessoriesTab:показ в меню');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'CallsTab:показ в меню');