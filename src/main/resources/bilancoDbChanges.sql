ALTER TABLE `spt`.`acc_category` 
DROP FOREIGN KEY `fk_acc_cat_activity_status10`,
DROP FOREIGN KEY `fk_acc_cat_type1`;
ALTER TABLE `spt`.`acc_category` 
CHANGE COLUMN `acc_type_id` `acc_type_id` INT(2) NOT NULL ,
CHANGE COLUMN `activity_status_id` `activity_status_id` INT(2) NOT NULL ;
ALTER TABLE `spt`.`acc_category` 
ADD CONSTRAINT `fk_acc_cat_activity_status10`
  FOREIGN KEY (`activity_status_id`)
  REFERENCES `spt`.`activity_status` (`id`),
ADD CONSTRAINT `fk_acc_cat_type1`
  FOREIGN KEY (`acc_type_id`)
  REFERENCES `spt`.`acc_type` (`id`);

UPDATE `spt`.`acc_type` SET `name`='Приход' WHERE `id`='1';
UPDATE `spt`.`acc_type` SET `name`='Расход' WHERE `id`='2';
INSERT INTO `spt`.`acc_type` (`id`, `name`, `sign`) VALUES ('3', 'Возвращаемые активы', '1');
INSERT INTO `spt`.`acc_type` (`id`, `name`, `sign`) VALUES ('4', 'Краткосрочная задолженность', '-1');

INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'ShortTermDebtsDefinitionView:меню,добавление,изменение,удаление');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'ReturnableAssetsDefinitionView:меню,добавление,изменение,удаление');

INSERT INTO `spt`.`acc_invoice_type` (`id`, `name`) VALUES ('3', 'debt');
INSERT INTO `spt`.`acc_invoice_type` (`id`, `name`) VALUES ('4', 'assert');
ALTER TABLE `spt`.`acc_accruals` 
RENAME TO  `spt`.`acc_transfers` ;

INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('ShortTermDebtsView', 'Краткосрочные задолженности', 'меню,добавление,изменение,удаление,копирование');
INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('ReturnableAssetsView', 'Возвращаемые активы', 'меню,добавление,изменение,удаление,копирование');

INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'ShortTermDebtsView:меню,добавление,изменение,удаление,копирование');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'ReturnableAssetsView:меню,добавление,изменение,удаление,копирование');

DROP VIEW view_accurals;
CREATE
    ALGORITHM = UNDEFINED
    DEFINER = root`@`localhost
    SQL SECURITY DEFINER
VIEW spt.view_accurals AS
    SELECT
        inv.school_id AS school_id,
        acr.acc_category_id AS acc_category_id,
        IFNULL(SUM(IF((`acr`.`acc_currency_id` <> 2),
                    (`acr`.`amount` / acr.`currency_rate`),
                    acr.`amount`)),
                0.0) AS amount_usd,
        IFNULL(SUM(IF((`acr`.`acc_currency_id` <> 1),
                    (`acr`.`amount` * acr.`currency_rate`),
                    acr.`amount`)),
                0.0) AS amount_som,
        MAX(`inv`.`id`) AS invoice_id
    FROM
        (`spt`.`acc_transfers` acr
        LEFT JOIN spt.acc_invoice inv ON (((`inv`.`id` = acr.`invoice_id`)
            AND (`inv`.`acc_invoice_type_id` = 1))))
    GROUP BY inv.school_id , acr.acc_category_id;

ALTER TABLE `spt`.`acc_invoice`
    ADD COLUMN `is_confirmed` TINYINT(1) NOT NULL DEFAULT '0' AFTER `acc_invoice_type_id`;
UPDATE `spt`.`user_permission` SET `permissions` = 'ReturnableAssetsView:меню,добавление,изменение,удаление,копирование,контроль подтверждений' WHERE (`role_name` = 'admin') and (`permissions` = 'ReturnableAssetsView:меню,добавление,изменение,удаление,копирование');
UPDATE `spt`.`user_permission` SET `permissions` = 'ShortTermDebtsView:меню,добавление,изменение,удаление,копирование,контроль подтверждений' WHERE (`role_name` = 'admin') and (`permissions` = 'ShortTermDebtsView:меню,добавление,изменение,удаление,копирование');
