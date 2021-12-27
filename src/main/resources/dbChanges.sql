CREATE TABLE `hr_employee_completeness` (
  `id` int NOT NULL AUTO_INCREMENT,
  `employee_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index4_unq` (`employee_id`),
  KEY `fk_hr_employee_completeness_employee_idx` (`employee_id`),
  CONSTRAINT `fk_hr_employee_completeness_employee1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `spt`.`hr_employee_completeness` 
ADD COLUMN `phones` TINYINT(1) NULL DEFAULT NULL AFTER `employee_id`,
ADD COLUMN `branches` TINYINT(1) NULL DEFAULT NULL AFTER `phones`,
ADD COLUMN `education` TINYINT(1) NULL DEFAULT NULL AFTER `branches`,
ADD COLUMN `work_places` TINYINT(1) NULL DEFAULT NULL AFTER `education`,
ADD COLUMN `exams` TINYINT(1) NULL DEFAULT NULL AFTER `work_places`,
ADD COLUMN `seminars` TINYINT(1) NULL DEFAULT NULL AFTER `exams`,
ADD COLUMN `certificates` TINYINT(1) NULL DEFAULT NULL AFTER `seminars`,
ADD COLUMN `languages` TINYINT(1) NULL DEFAULT NULL AFTER `certificates`,
ADD COLUMN `spouse_education` TINYINT(1) NULL DEFAULT NULL AFTER `languages`,
ADD COLUMN `spouse_work_places` TINYINT(1) NULL DEFAULT NULL AFTER `spouse_education`,
ADD COLUMN `children` TINYINT(1) NULL DEFAULT NULL AFTER `spouse_work_places`;

INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('CV_Window', 'Резюме сотрудника', 'информация о кураторстве,информация о контракте');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('hr', 'CV_Window:информация о кураторстве,информация о контракте');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'CV_Window:информация о кураторстве,информация о контракте');
insert into hr_employee_completeness (employee_id) select e.id from employee as e;

UPDATE hr_employee_completeness as c set c.phones = (select count(id) from hr_employee_phone_number where employee_id = c.employee_id);
UPDATE hr_employee_completeness as c set c.phones = null where c.phones = 0;
UPDATE hr_employee_completeness as c set c.phones = 1 where c.phones is not null;

UPDATE hr_employee_completeness as c set c.branches = (select count(id) from hr_employee_branch where employee_id = c.employee_id);
UPDATE hr_employee_completeness as c set c.branches = null where c.branches = 0;
UPDATE hr_employee_completeness as c set c.branches = 1 where c.branches is not null;

UPDATE hr_employee_completeness as c set c.education = (select count(id) from hr_employee_education where employee_id = c.employee_id and hr_own_id = 1);
UPDATE hr_employee_completeness as c set c.education = null where c.education = 0;
UPDATE hr_employee_completeness as c set c.education = 1 where c.education is not null;

UPDATE hr_employee_completeness as c set c.work_places = (select count(id) from hr_employee_work where employee_id = c.employee_id and hr_own_id = 1);
UPDATE hr_employee_completeness as c set c.work_places = null where c.work_places = 0;
UPDATE hr_employee_completeness as c set c.work_places = 1 where c.work_places is not null;

UPDATE hr_employee_completeness as c set c.exams = (select count(id) from hr_employee_exam where employee_id = c.employee_id);
UPDATE hr_employee_completeness as c set c.exams = null where c.exams = 0;
UPDATE hr_employee_completeness as c set c.exams = 1 where c.exams is not null;

UPDATE hr_employee_completeness as c set c.seminars = (select count(id) from hr_employee_seminar where employee_id = c.employee_id);
UPDATE hr_employee_completeness as c set c.seminars = null where c.seminars = 0;
UPDATE hr_employee_completeness as c set c.seminars = 1 where c.seminars is not null;

UPDATE hr_employee_completeness as c set c.certificates = (select count(id) from hr_employee_certificate where employee_id = c.employee_id);
UPDATE hr_employee_completeness as c set c.certificates = null where c.certificates = 0;
UPDATE hr_employee_completeness as c set c.certificates = 1 where c.certificates is not null;

UPDATE hr_employee_completeness as c set c.languages = (select count(id) from hr_employee_language where employee_id = c.employee_id);
UPDATE hr_employee_completeness as c set c.languages = null where c.languages = 0;
UPDATE hr_employee_completeness as c set c.languages = 1 where c.languages is not null;

UPDATE hr_employee_completeness as c set c.spouse_education = (select count(id) from hr_employee_education where employee_id = c.employee_id and hr_own_id = 2);
UPDATE hr_employee_completeness as c set c.spouse_education = null where c.spouse_education = 0;
UPDATE hr_employee_completeness as c set c.spouse_education = 1 where c.spouse_education is not null;

UPDATE hr_employee_completeness as c set c.spouse_work_places = (select count(id) from hr_employee_work where employee_id = c.employee_id and hr_own_id = 2);
UPDATE hr_employee_completeness as c set c.spouse_work_places = null where c.spouse_work_places = 0;
UPDATE hr_employee_completeness as c set c.spouse_work_places = 1 where c.spouse_work_places is not null;

UPDATE hr_employee_completeness as c set c.children = (select count(id) from hr_employee_children where employee_id = c.employee_id);
UPDATE hr_employee_completeness as c set c.children = null where c.children = 0;
UPDATE hr_employee_completeness as c set c.children = 1 where c.children is not null;
