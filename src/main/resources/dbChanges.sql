UPDATE `spt`.`permissions` SET `permissions` = 'показ в меню,отчет по количеству преподаваемых часов,общий отчет по кадрам' WHERE (`java_class_name` = 'HRReportsView');

UPDATE `spt`.`user_permission` SET `permissions` = 'HRReportsView:показ в меню,отчет по количеству преподаваемых часов,общий отчет по кадрам' WHERE (`role_name` = 'hr') and (`permissions` = 'HRReportsView:показ в меню,отчет по количеству преподаваемых часов');
UPDATE `spt`.`user_permission` SET `permissions` = 'HRReportsView:показ в меню,отчет по количеству преподаваемых часов,общий отчет по кадрам' WHERE (`role_name` = 'admin') and (`permissions` = 'HRReportsView:показ в меню,отчет по количеству преподаваемых часов');

UPDATE `spt`.`gender` SET `name` = 'Мужской' WHERE (`id` = '1');
UPDATE `spt`.`gender` SET `name` = 'Женский' WHERE (`id` = '2');
delete ignore from nationality;
update employee set nationality_id = 127 where nationality_id = 128;
update employee set nationality_id = 93 where nationality_id = 187;
UPDATE `spt`.`nationality` SET `name` = 'Афганец' WHERE (`id` = '1');
UPDATE `spt`.`nationality` SET `name` = 'Австралиец' WHERE (`id` = '9');
UPDATE `spt`.`nationality` SET `name` = 'Азербайжанец' WHERE (`id` = '11');
UPDATE `spt`.`nationality` SET `name` = 'Канадец' WHERE (`id` = '31');
UPDATE `spt`.`nationality` SET `name` = 'Эфиоп' WHERE (`id` = '58');
UPDATE `spt`.`nationality` SET `name` = 'Немец' WHERE (`id` = '65');
UPDATE `spt`.`nationality` SET `name` = 'Индус' WHERE (`id` = '77');
UPDATE `spt`.`nationality` SET `name` = 'Индонезиец' WHERE (`id` = '78');
UPDATE `spt`.`nationality` SET `name` = 'Японец' WHERE (`id` = '85');
UPDATE `spt`.`nationality` SET `name` = 'Казах' WHERE (`id` = '87');
UPDATE `spt`.`nationality` SET `name` = 'Кениец' WHERE (`id` = '88');
UPDATE `spt`.`nationality` SET `name` = 'Кыргыз' WHERE (`id` = '93');
UPDATE `spt`.`nationality` SET `name` = 'Литовец' WHERE (`id` = '101');
UPDATE `spt`.`nationality` SET `name` = 'Монгол' WHERE (`id` = '117');
UPDATE `spt`.`nationality` SET `name` = 'Нигериец' WHERE (`id` = '127');
DELETE FROM `spt`.`nationality` WHERE (`id` = '128');
UPDATE `spt`.`nationality` SET `name` = 'Пакистанец' WHERE (`id` = '131');
UPDATE `spt`.`nationality` SET `name` = 'Филипинец' WHERE (`id` = '137');
UPDATE `spt`.`nationality` SET `name` = 'Португалец' WHERE (`id` = '139');
UPDATE `spt`.`nationality` SET `name` = 'Русский' WHERE (`id` = '142');
UPDATE `spt`.`nationality` SET `name` = 'Серб' WHERE (`id` = '151');
UPDATE `spt`.`nationality` SET `name` = 'Южноафриканец' WHERE (`id` = '159');
UPDATE `spt`.`nationality` SET `name` = 'Таджик' WHERE (`id` = '169');
UPDATE `spt`.`nationality` SET `name` = 'Таец' WHERE (`id` = '171');
UPDATE `spt`.`nationality` SET `name` = 'Турок' WHERE (`id` = '176');
UPDATE `spt`.`nationality` SET `name` = 'Туркмен' WHERE (`id` = '177');
UPDATE `spt`.`nationality` SET `name` = 'Украинец' WHERE (`id` = '180');
UPDATE `spt`.`nationality` SET `name` = 'Британец' WHERE (`id` = '182');
UPDATE `spt`.`nationality` SET `name` = 'Американец' WHERE (`id` = '183');
UPDATE `spt`.`nationality` SET `name` = 'Узбек' WHERE (`id` = '185');
UPDATE `spt`.`nationality` SET `name` = 'Замбиец' WHERE (`id` = '191');
DELETE FROM `spt`.`nationality` WHERE (`id` = '187');
UPDATE `spt`.`nationality` SET `name` = 'Калмык' WHERE (`id` = '193');
UPDATE `spt`.`nationality` SET `name` = 'Уйгур' WHERE (`id` = '194');
UPDATE `spt`.`nationality` SET `name` = 'Дунганин' WHERE (`id` = '195');
UPDATE `spt`.`nationality` SET `name` = 'Татарин' WHERE (`id` = '196');
UPDATE `spt`.`nationality` SET `name` = 'Чеченец' WHERE (`id` = '197');
UPDATE `spt`.`nationality` SET `name` = 'Карачаец' WHERE (`id` = '198');
UPDATE `spt`.`nationality` SET `name` = 'Лезгин' WHERE (`id` = '199');
UPDATE `spt`.`nationality` SET `name` = 'Курд' WHERE (`id` = '200');
UPDATE `spt`.`nationality` SET `name` = 'Багамец' WHERE (`id` = '12');
UPDATE `spt`.`nationality` SET `name` = 'Ганец' WHERE (`id` = '66');
UPDATE `spt`.`nationality` SET `name` = 'Иракиец' WHERE (`id` = '80');
UPDATE `spt`.`nationality` SET `name` = 'Араб' WHERE (`id` = '149');
UPDATE `spt`.`nationality` SET `name` = 'Зимбабвец' WHERE (`id` = '192');
INSERT INTO `spt`.`acc_category` (`name`, `parent_code`, `code`, `parent_id`, `acc_type_id`, `activity_status_id`, `school_id`, `employee_id`) VALUES ('SADIRBAYEV URMAT', '791.02.14', '104122', '3229', '2', '2', '5', '4012');

delete ignore FROM spt.hr_country;

UPDATE `spt`.`hr_country` SET `name` = 'Афганистан' WHERE (`id` = '397');
UPDATE `spt`.`hr_country` SET `name` = 'Босния и Герцеговина' WHERE (`id` = '423');
UPDATE `spt`.`hr_country` SET `name` = 'Канада' WHERE (`id` = '434');
UPDATE `spt`.`hr_country` SET `name` = 'Китай' WHERE (`id` = '440');
UPDATE `spt`.`hr_country` SET `name` = 'Финляндия' WHERE (`id` = '468');
UPDATE `spt`.`hr_country` SET `name` = 'Германия' WHERE (`id` = '476');
UPDATE `spt`.`hr_country` SET `name` = 'Гана' WHERE (`id` = '477');
UPDATE `spt`.`hr_country` SET `name` = 'Индонезия' WHERE (`id` = '496');
UPDATE `spt`.`hr_country` SET `name` = 'Ирак' WHERE (`id` = '498');
UPDATE `spt`.`hr_country` SET `name` = 'Казакстан' WHERE (`id` = '505');
UPDATE `spt`.`hr_country` SET `name` = 'Кыргызстан' WHERE (`id` = '510');
UPDATE `spt`.`hr_country` SET `name` = 'Монголия' WHERE (`id` = '537');
UPDATE `spt`.`hr_country` SET `name` = 'Нигерия' WHERE (`id` = '551');
UPDATE `spt`.`hr_country` SET `name` = 'Филиппины' WHERE (`id` = '563');
UPDATE `spt`.`hr_country` SET `name` = 'Португалия' WHERE (`id` = '566');
UPDATE `spt`.`hr_country` SET `name` = 'Россия' WHERE (`id` = '571');
UPDATE `spt`.`hr_country` SET `name` = 'Южная Африка' WHERE (`id` = '591');
UPDATE `spt`.`hr_country` SET `name` = 'Таджикистан' WHERE (`id` = '602');
UPDATE `spt`.`hr_country` SET `name` = 'Турция' WHERE (`id` = '611');
UPDATE `spt`.`hr_country` SET `name` = 'Туркменистан' WHERE (`id` = '612');
UPDATE `spt`.`hr_country` SET `name` = 'Великобритания' WHERE (`id` = '617');
UPDATE `spt`.`hr_country` SET `name` = 'США' WHERE (`id` = '618');
UPDATE `spt`.`hr_country` SET `name` = 'Узбекистан' WHERE (`id` = '621');
UPDATE `spt`.`hr_country` SET `name` = 'Венесуэлла' WHERE (`id` = '623');
UPDATE `spt`.`hr_country` SET `name` = 'Замбия' WHERE (`id` = '630');
ALTER TABLE `spt`.`employee` 
ADD COLUMN `hr_country_id` INT NOT NULL DEFAULT '510' AFTER `can_advisor`;
ALTER TABLE `spt`.`employee` 
ADD INDEX `fk_employee_country_idx` (`hr_country_id` ASC) VISIBLE;
ALTER TABLE `spt`.`employee` 
ADD CONSTRAINT `fk_employee_country1`
  FOREIGN KEY (`hr_country_id`)
  REFERENCES `spt`.`hr_country` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION;
update ignore employee e set e.hr_country_id = (select hr_country_id from hr_employee_contacts where employee_id = e.id);
ALTER TABLE `spt`.`hr_employee_contacts` 
DROP FOREIGN KEY `fk_hr_employee_contacts_hr_country1`;
ALTER TABLE `spt`.`hr_employee_contacts` 
DROP COLUMN `hr_country_id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`, `employee_id`),
DROP INDEX `fk_hr_employee_contacts_hr_country_idx` ;
UPDATE `spt`.`school` SET `school_type_id` = '7' WHERE (`id` = '16') and (`year_id` = '6') and (`activity_status_id` = '2') and (`school_type_id` = '1');

delete ignore FROM spt.hr_certificate;
delete ignore FROM spt.hr_university;
delete ignore FROM spt.hr_work_place;

ALTER TABLE `spt`.`hr_employee_exam` 
CHANGE COLUMN `score` `score` VARCHAR(10) NOT NULL ;
update hr_employee_branch set hr_branch_id = 18 where hr_branch_id = 29;
DELETE FROM `spt`.`hr_branch` WHERE (`id` = '29') and (`activity_status_id` = '2');
update hr_employee_branch set hr_branch_id = 19 where hr_branch_id = 30;
DELETE FROM `spt`.`hr_branch` WHERE (`id` = '30') and (`activity_status_id` = '2');

INSERT INTO `spt`.`hr_employee_order` (`hr_orders_id`, `employee_id`, `school_id`, `hr_position_id`, `from_date`, `modification_date`, `m_employee_id`, `can_not_delete`) VALUES ('6', '1537', '19', '12', '2020-09-18', '2020-09-23 12:16:33', '29', '0');
UPDATE `spt`.`hr_employee_order` SET `effected_by_id` = '6836' WHERE (`id` = '1480') and (`hr_orders_id` = '1') and (`employee_id` = '1537') and (`school_id` = '19') and (`hr_position_id` = '12') and (`m_employee_id` = '29');
INSERT INTO `spt`.`hr_employee_order` (`hr_orders_id`, `employee_id`, `school_id`, `hr_position_id`, `from_date`, `modification_date`, `m_employee_id`, `can_not_delete`) VALUES ('6', '1468', '19', '3', '2020-09-23', '2020-09-23 12:10:26', '29', '0');
UPDATE `spt`.`hr_employee_order` SET `effected_by_id` = '6837' WHERE (`id` = '1411') and (`hr_orders_id` = '1') and (`employee_id` = '1468') and (`school_id` = '19') and (`hr_position_id` = '3') and (`m_employee_id` = '29');
INSERT INTO `spt`.`hr_employee_order` (`hr_orders_id`, `employee_id`, `school_id`, `hr_position_id`, `from_date`, `modification_date`, `m_employee_id`, `can_not_delete`) VALUES ('6', '2017', '28', '14', '2020-09-18', '2020-09-18 12:16:07', '1', '0');
UPDATE `spt`.`hr_employee_order` SET `effected_by_id` = '6838' WHERE (`id` = '1960') and (`hr_orders_id` = '1') and (`employee_id` = '2017') and (`school_id` = '28') and (`hr_position_id` = '14') and (`m_employee_id` = '1');
INSERT INTO `spt`.`hr_employee_order` (`hr_orders_id`, `employee_id`, `school_id`, `hr_position_id`, `from_date`, `modification_date`, `m_employee_id`, `can_not_delete`) VALUES ('1', '153', '1', '3', '2021-01-01', '2021-01-01', '1', '1');
UPDATE `spt`.`hr_employee_order` SET `school_id` = '15' WHERE (`id` = '6839') and (`hr_orders_id` = '1') and (`employee_id` = '153') and (`school_id` = '1') and (`hr_position_id` = '3') and (`m_employee_id` = '1');
INSERT INTO `spt`.`hr_employee_order` (`hr_orders_id`, `employee_id`, `school_id`, `hr_position_id`, `from_date`, `modification_date`, `m_employee_id`, `can_not_delete`) VALUES ('6', '1010', '12', '10', '2020-09-25', '2020-09-25 15:33:55', '1035', '0');
UPDATE `spt`.`hr_employee_order` SET `effected_by_id` = '6840' WHERE (`id` = '953') and (`hr_orders_id` = '1') and (`employee_id` = '1010') and (`school_id` = '12') and (`hr_position_id` = '10') and (`m_employee_id` = '1035');
INSERT INTO `spt`.`hr_employee_order` (`hr_orders_id`, `employee_id`, `school_id`, `hr_position_id`, `from_date`, `modification_date`, `m_employee_id`, `can_not_delete`) VALUES ('6', '1370', '17', '10', '2020-09-18', '2020-11-25 16:21:22', '2947', '0');
UPDATE `spt`.`hr_employee_order` SET `effected_by_id` = '6841' WHERE (`id` = '1313') and (`hr_orders_id` = '1') and (`employee_id` = '1370') and (`school_id` = '17') and (`hr_position_id` = '10') and (`m_employee_id` = '2947');
