CREATE TABLE `acc_balance_settings` (
                                        `id` int NOT NULL AUTO_INCREMENT,
                                        `year_preferences` tinyint(1) NOT NULL DEFAULT '0',
                                        `prefix` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                                        `text_field_preferences` tinyint(1) NOT NULL DEFAULT '0',
                                        `postfix` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
                                        `acc_cactegory_id` int NOT NULL,
                                        `activity_status_id` int NOT NULL DEFAULT '2',
                                        PRIMARY KEY (`id`),
                                        KEY `ind_acc_balance_settings_category_id` (`acc_cactegory_id`),
                                        KEY `ind_acc_balance_settings_activity_status_id` (`activity_status_id`),
                                        CONSTRAINT `fk_acc_balance_settings_category_idx` FOREIGN KEY (`acc_cactegory_id`) REFERENCES `acc_category` (`id`) ON DELETE RESTRICT,
                                        CONSTRAINT `fk_acc_balance_settings_status_idx` FOREIGN KEY (`activity_status_id`) REFERENCES `activity_status` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', 'Kasa', '0', '6044', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', 'Banka (Optima)', '0', '6044', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', 'Banka (Rsk)', '0', '6044', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `postfix`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', 'Banka (', '1', ')', '6044', '2');

INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('1', 'Aktif öğrenci Alacakları', '0', '6045', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('1', 'Not confirmed öğrenci Alacakları', '0', '6045', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('-1', 'Dönem öğrenci Alacakları', '0', '6045', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('-1', 'Not confirmed öğrenci Alacakları', '0', '6045', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', 'Geçmiş Dönem Ayrılan ve Mezun öğrenci Alacakları', '0', '6045', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', 'Ort kurs alacakları', '0', '6045', '2');

INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', 'Borç verilen sahıs veya firmalardan alacaklar', '0', '6046', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', '1-', '1', '6046', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', '2-', '1', '6046', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', '3-', '1', '6046', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', 'Kantin kira alacakları', '0', '6046', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', 'Muavenet Alacakları', '0', '6046', '2');
INSERT INTO `spt`.`acc_balance_settings` (`year_preferences`, `prefix`, `text_field_preferences`, `acc_cactegory_id`, `activity_status_id`) VALUES ('0', 'Personel avanslardan alacaklar', '0', '6046', '2');

INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Binalar', '6048');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Demirbaş', '6048');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Araçlar', '6048');

INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('798.01 - Geçmiş Dönem Kıyafet Borçları', '6049');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('798.02 - Geçmiş Dönem Yatırım Borçları', '6049');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('798.03 - Geçmiş Dönem Eğitim Borçları', '6049');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('798.04 - Geçmiş Dönem Ayrılan Personel Borçları', '6049');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('798.05 - Geçmiş Dönem Yemekhane Borçları', '6049');

INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('TT personel maas borcları', '6050');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Sozlesmeli personel borcları', '6050');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Yerel personel borcları', '6050');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Yabancı personel borcları', '6050');

INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Toptancıya borclar', '6051');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Manava borclar', '6051');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Kasaba borclar', '6051');

INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Vergi', '6052');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Sigorta', '6052');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Elektrik', '6052');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Su /', '6052');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Isitma', '6052');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Guvenlik', '6052');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Kira', '6052');

INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Yatırım Giderleri', '6053');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `text_field_preferences`, `acc_cactegory_id`) VALUES ('1-', '1', '6053');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `text_field_preferences`, `acc_cactegory_id`) VALUES ('2-', '1', '6053');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Demirbaş Giderleri', '6053');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `text_field_preferences`, `acc_cactegory_id`) VALUES ('1-', '1', '6053');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `text_field_preferences`, `acc_cactegory_id`) VALUES ('2-', '1', '6053');

INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Emanet Alinan borclar', '6054');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Diğer Yardım ve Takviyeler', '6054');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Yayın evi kitap borcları', '6054');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `acc_cactegory_id`) VALUES ('Muavenet borcları', '6054');
INSERT INTO `spt`.`acc_balance_settings` (`prefix`, `text_field_preferences`, `postfix`, `acc_cactegory_id`) VALUES ('Tahmini kalan aylar borcları', '1', 'Ay', '6054');

ALTER TABLE `spt`.`acc_invoice`
    ADD COLUMN `is_old_version` TINYINT(1) NOT NULL DEFAULT '1' AFTER `note2`;
ALTER TABLE `spt`.`acc_transfers`
    ADD COLUMN `acc_balance_settings_id` INT NULL DEFAULT NULL AFTER `note`;
ALTER TABLE `spt`.`acc_transfers`
    ADD INDEX `ind_acc_transfers_balance` (`acc_balance_settings_id` ASC) VISIBLE;
ALTER TABLE `spt`.`acc_transfers`
    ADD CONSTRAINT `fk_acc_transfers_balance_id`
        FOREIGN KEY (`acc_balance_settings_id`)
            REFERENCES `spt`.`acc_balance_settings` (`id`)
            ON DELETE RESTRICT
            ON UPDATE NO ACTION;
UPDATE `spt`.`permissions` SET `java_class_name` = 'BalanceAccountsView' WHERE (`java_class_name` = 'ReturnableAssetsView');
DELETE FROM `spt`.`permissions` WHERE (`java_class_name` = 'ShortTermDebtsView');
delete from user_permission where permissions like 'ReturnableAssetsView%' and role_name != 'admin';
update user_permission set permissions = 'BalanceAccountsView:добавление,изменение,удаление,показ в меню' where permissions like 'ShortTermDebtsView%' and role_name != 'admin';
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'BalanceAccountsView:показ в меню,добавление,изменение,удаление,контроль подтверждений');
UPDATE `spt`.`acc_invoice_type` SET `name` = 'returnable asset' WHERE (`id` = '3');
UPDATE `spt`.`acc_invoice_type` SET `name` = 'short term debt' WHERE (`id` = '4');
ALTER TABLE `spt`.`acc_invoice`
    CHANGE COLUMN `is_old_version` `is_old_version` TINYINT(1) NOT NULL DEFAULT '0' ;
INSERT INTO `spt`.`acc_invoice_type` (`id`, `name`) VALUES ('5', 'assets and debts');
ALTER TABLE `spt`.`acc_invoice`
DROP COLUMN `is_old_version`;
