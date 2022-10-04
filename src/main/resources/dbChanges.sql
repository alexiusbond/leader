ALTER TABLE `spt`.`hr_employee_contacts`
    ADD COLUMN `passport_given` VARCHAR(45) NULL DEFAULT NULL AFTER `passport`,
ADD COLUMN `passport_date` DATE NULL DEFAULT NULL AFTER `passport_given`,
CHANGE COLUMN `passport` `passport` VARCHAR(20) CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NULL DEFAULT NULL AFTER `email`;
ALTER TABLE `spt`.`hr_employee_contacts`
    CHANGE COLUMN `passport_given` `passport_given` VARCHAR(45) NULL DEFAULT NULL AFTER `passport`,
    CHANGE COLUMN `passport_date` `passport_date` DATE NULL DEFAULT NULL AFTER `passport_given`;
CREATE TABLE `hr_contract_type` (
                                    `id` int NOT NULL AUTO_INCREMENT,
                                    `name` varchar(150) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
INSERT INTO `spt`.`hr_contract_type` (`id`, `name`) VALUES ('1', 'Трудовой договор с техническим персоналом');
INSERT INTO `spt`.`hr_contract_type` (`id`, `name`) VALUES ('2', 'Трудовой договор с административным персоналом');
INSERT INTO `spt`.`hr_contract_type` (`id`, `name`) VALUES ('3', 'Трудовой договор с педагогическим персоналом');
INSERT INTO `spt`.`hr_contract_type` (`id`, `name`) VALUES ('4', 'Договор на оказание образовательных и организационных услуг');
INSERT INTO `spt`.`hr_contract_type` (`id`, `name`) VALUES ('5', 'Договор на оказание услуг с персоналом');
CREATE TABLE `hr_employee_contract` (
                                        `id` int NOT NULL AUTO_INCREMENT,
                                        `employee_id` int NOT NULL,
                                        `contract_type_id` int NOT NULL,
                                        `salary` DOUBLE(12,2) NOT NULL,
  `from_date` DATE  NOT NULL,
  `till_date` DATE  NOT NULL,
  `year_id` int DEFAULT NULL,
  `hr_position_id` int DEFAULT NULL,
  `probationary_period` int DEFAULT NULL,
  `salary_date` DATE DEFAULT NULL,
  `working_days` int DEFAULT NULL,
  `working_hours` int DEFAULT NULL,
  `patent` varchar(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `patent_date` DATE DEFAULT NULL,
  `equipment` varchar(350) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_hr_employee_contract_employee_idx` (`employee_id`),
  KEY `fk_hr_employee_contract_type_idx` (`contract_type_id`),
  KEY `fk_hr_employee_contract_year_idx` (`year_id`),
  KEY `fk_hr_employee_contract_hr_position_idx` (`hr_position_id`),
  CONSTRAINT `fk_hr_employee_contract_employee1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_hr_employee_contract_type1` FOREIGN KEY (`contract_type_id`) REFERENCES `hr_contract_type` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_hr_employee_contract_year1` FOREIGN KEY (`year_id`) REFERENCES `year` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_hr_employee_contract_hr_position1` FOREIGN KEY (`hr_position_id`) REFERENCES `hr_position` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
UPDATE `spt`.`permissions` SET `permissions` = 'показ в меню,добавление,изменение,удаление,вкладка разрешений,вкладка образовательной деятельности,вкладка приказов,вкладка контактной информации,вкладка профессиональной информации,вкладка образовательной деятельности,вкладка достижений,вкладка информации о семье,вкладка доп. информации,вкладка документов,вкладка договоров,вкладка поиска,видимость контракта,организация уроков,назначение кураторства,видимость всех сотрудников' WHERE (`java_class_name` = 'EmployeeDefinitionView');
UPDATE `spt`.`user_permission` SET `permissions` = 'EmployeeDefinitionView:показ в меню,добавление,изменение,удаление,вкладка разрешений,вкладка образовательной деятельности,вкладка приказов,вкладка контактной информации,вкладка профессиональной информации,вкладка образовательной деятельности,вкладка достижений,вкладка информации о семье,вкладка доп. информации,вкладка документов,вкладка договоров,вкладка поиска,видимость контракта,организация уроков,назначение кураторства,видимость всех сотрудников' WHERE (`role_name` = 'admin') and (`permissions` = 'EmployeeDefinitionView:показ в меню,добавление,изменение,удаление,вкладка разрешений,вкладка образовательной деятельности,вкладка приказов,вкладка контактной информации,вкладка профессиональной информации,вкладка образовательной деятельности,вкладка достижений,вкладка информации о семье,вкладка доп. информации,вкладка документов,вкладка поиска,видимость контракта,организация уроков,назначение кураторства,видимость всех сотрудников');
UPDATE `spt`.`user_permission` SET `permissions` = 'EmployeeDefinitionView:показ в меню,добавление,изменение,удаление,вкладка разрешений,вкладка образовательной деятельности,вкладка приказов,вкладка контактной информации,вкладка профессиональной информации,вкладка образовательной деятельности,вкладка достижений,вкладка информации о семье,вкладка доп. информации,вкладка документов,вкладка договоров,вкладка поиска,видимость контракта,организация уроков,назначение кураторства,видимость всех сотрудников' WHERE (`role_name` = 'hr') and (`permissions` = 'EmployeeDefinitionView:показ в меню,добавление,изменение,удаление,вкладка разрешений,вкладка образовательной деятельности,вкладка приказов,вкладка контактной информации,вкладка профессиональной информации,вкладка образовательной деятельности,вкладка достижений,вкладка информации о семье,вкладка доп. информации,вкладка документов,вкладка поиска,видимость контракта,организация уроков,назначение кураторства,видимость всех сотрудников');
ALTER TABLE `spt`.`hr_employee_contract` ADD COLUMN `creation_date` DATE NOT NULL AFTER `equipment`;

