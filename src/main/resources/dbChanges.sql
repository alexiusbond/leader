--
-- Table structure for table `correction_type`
--

DROP TABLE IF EXISTS `correction_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `correction_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `type` varchar(1) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '-',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `student_contract_correction`
--

DROP TABLE IF EXISTS `student_contract_correction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_contract_correction` (
  `id` int NOT NULL AUTO_INCREMENT,
  `amount` double(8,2) NOT NULL,
  `note` varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `student_contract_id` int NOT NULL,
  `correction_type_id` int NOT NULL,
  `registration_date` datetime NOT NULL,
  `modification_date` datetime NOT NULL,
  `employee_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index5` (`student_contract_id`,`correction_type_id`),
  KEY `fk_student_contract_correction_student_contract1_idx` (`student_contract_id`),
  KEY `fk_student_contract_correction_employee1_idx` (`employee_id`),
  KEY `fk_student_contract_correction_correction_type1_idx` (`correction_type_id`),
  CONSTRAINT `fk_student_contract_correction_student_contract1` FOREIGN KEY (`student_contract_id`) REFERENCES `student_contract` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_student_contract_correction_correction_type1` FOREIGN KEY (`correction_type_id`) REFERENCES `correction_type` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_student_contract_correction_employee1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Final view structure for view `view_corrections`
--

/*!50001 DROP VIEW IF EXISTS `view_corrections`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `view_corrections` AS SELECT 
    GROUP_CONCAT(DISTINCT '(',
        `amr_t`.`type`,
        ') ',
        `amr_t`.`name`
        ORDER BY `amr_t`.`id` ASC
        SEPARATOR ', ') AS `names`,
    GROUP_CONCAT(DISTINCT `scc`.`note`
        ORDER BY `scc`.`id` ASC
        SEPARATOR ', ') AS `notes`,
    `scc`.`student_contract_id` AS `student_contract_id`,
    SUM(IF((`amr_t`.`type` = '+'),
        `scc`.`amount`,
        -(`scc`.`amount`))) AS `amount`
FROM
    (`student_contract_correction` `scc`
    LEFT JOIN `correction_type` `amr_t` ON ((`scc`.`correction_type_id` = `amr_t`.`id`)))
GROUP BY `scc`.`student_contract_id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('CorrectionsTable', 'Ученики - Таблица корректировок', 'показ в меню,добавление,изменение,удаление');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'CorrectionsTable:показ в меню,добавление,изменение,удаление');


ALTER TABLE `spt`.`student_contract_correction` 
RENAME TO  `spt`.`student_correction` ;
ALTER TABLE `spt`.`student_correction` 
DROP FOREIGN KEY `fk_student_contract_correction_student_contract1`;
ALTER TABLE `spt`.`student_correction` 
DROP COLUMN `registration_date`,
ADD COLUMN `year_id` INT NOT NULL AFTER `student_id`,
CHANGE COLUMN `student_contract_id` `student_id` INT NOT NULL ;
ALTER TABLE `spt`.`student_correction` 
ADD CONSTRAINT `fk_student_contract_correction_student_contract1`
  FOREIGN KEY (`student_id`)
  REFERENCES `spt`.`student_contract` (`id`)
  ON DELETE RESTRICT;
ALTER TABLE `spt`.`student_correction` 
ADD INDEX `fk_student_contract_correction_year1_idx` (`year_id` ASC);
ALTER TABLE `spt`.`student_correction` RENAME INDEX `fk_student_contract_correction_student_contract1_idx` TO `fk_student_contract_correction_student1_idx`;
ALTER TABLE `spt`.`student_correction` ALTER INDEX `fk_student_contract_correction_student1_idx`;
ALTER TABLE `spt`.`student_correction` 
DROP FOREIGN KEY `fk_student_contract_correction_student_contract1`;
ALTER TABLE `spt`.`student_correction` 
DROP INDEX `index5` ;
ALTER TABLE `spt`.`student_correction` 
ADD CONSTRAINT `fk_student_contract_correction_student1`
  FOREIGN KEY (`student_id`)
  REFERENCES `spt`.`student` (`id`)
  ON DELETE RESTRICT;
ALTER TABLE `spt`.`student_correction` 
ADD CONSTRAINT `fk_student_contract_correction_year1`
  FOREIGN KEY (`year_id`)
  REFERENCES `spt`.`year` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION;

USE `spt`;
CREATE 
     OR REPLACE ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `view_corrections` AS
    SELECT 
    GROUP_CONCAT(DISTINCT '(',
        `amr_t`.`type`,
        ') ',
        `amr_t`.`name`
        ORDER BY `amr_t`.`id` ASC
        SEPARATOR ', ') AS `names`,
    GROUP_CONCAT(DISTINCT `scc`.`note`
        ORDER BY `scc`.`id` ASC
        SEPARATOR ', ') AS `notes`,
    `scc`.`student_id` AS `student_id`,
    `scc`.`year_id` AS `year_id`,
    SUM(IF((`amr_t`.`type` = '+'),
        `scc`.`amount`,
        -(`scc`.`amount`))) AS `amount`
FROM
    (`student_correction` `scc`
    LEFT JOIN `correction_type` `amr_t` ON ((`scc`.`correction_type_id` = `amr_t`.`id`)))
GROUP BY `scc`.`student_id` , `scc`.`year_id`;
INSERT INTO `spt`.`correction_type` (`id`, `name`, `type`) VALUES ('1', 'Отчисление', '-');
INSERT INTO `spt`.`correction_type` (`id`, `name`, `type`) VALUES ('2', 'Позднее начало', '-');
INSERT INTO `spt`.`correction_type` (`id`, `name`, `type`) VALUES ('3', 'Доплата за общежите', '+');
INSERT INTO `spt`.`correction_type` (`id`, `name`, `type`) VALUES ('4', 'Доплата за питание', '+');
INSERT INTO `spt`.`correction_type` (`id`, `name`, `type`) VALUES ('5', 'Доплата за школьную форму', '+');
ALTER TABLE `spt`.`student_correction` 
ADD UNIQUE INDEX `unq_student_correction_idx` (`student_id` ASC, `year_id` ASC, `correction_type_id` ASC) VISIBLE;
ALTER TABLE `spt`.`student_correction` ALTER INDEX `fk_student_contract_correction_year1_idx` INVISIBLE;
USE `spt`;
CREATE 
     OR REPLACE ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `view_corrections` AS
     SELECT 
        GROUP_CONCAT(DISTINCT '(',
            `amr_t`.`type`,
            ') ',
            `amr_t`.`name`
            ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `names`,
            GROUP_CONCAT(DISTINCT 
            `amr_t`.`type`,
            `scc`.`amount`, '$'
            ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `amounts`,
        GROUP_CONCAT(DISTINCT `scc`.`note`
            ORDER BY `scc`.`id` ASC
            SEPARATOR ', ') AS `notes`,
        `scc`.`student_id` AS `student_id`,
        `scc`.`year_id` AS `year_id`,
        SUM(IF((`amr_t`.`type` = '+'),
            `scc`.`amount`,
            -(`scc`.`amount`))) AS `amount`
    FROM
        (`student_correction` `scc`
        LEFT JOIN `correction_type` `amr_t` ON ((`scc`.`correction_type_id` = `amr_t`.`id`)))
    GROUP BY `scc`.`student_id` , `scc`.`year_id`;

USE `spt`;
CREATE 
     OR REPLACE ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `view_corrections` AS
    SELECT 
        GROUP_CONCAT(DISTINCT '(',
            `amr_t`.`type`,
            ') ',
            `amr_t`.`name`
            ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `names`,
        GROUP_CONCAT(DISTINCT `amr_t`.`type`, `scc`.`amount`, '$'
            ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `details`,
        GROUP_CONCAT(DISTINCT `scc`.`note`
            ORDER BY `scc`.`id` ASC
            SEPARATOR ', ') AS `notes`,
        `scc`.`student_id` AS `student_id`,
        `scc`.`year_id` AS `year_id`,
        SUM(IF((`amr_t`.`type` = '+'),
            `scc`.`amount`,
            -(`scc`.`amount`))) AS `amount`
    FROM
        (`student_correction` `scc`
        LEFT JOIN `correction_type` `amr_t` ON ((`scc`.`correction_type_id` = `amr_t`.`id`)))
    GROUP BY `scc`.`student_id` , `scc`.`year_id`;
USE `spt`;
CREATE 
     OR REPLACE ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `view_corrections` AS
    SELECT 
        GROUP_CONCAT(DISTINCT '(',
            `amr_t`.`type`,
            ') ',
            `amr_t`.`name`
            ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `names`,
        GROUP_CONCAT(DISTINCT `amr_t`.`type`, `scc`.`amount`, ' $'
            ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `details`,
        GROUP_CONCAT(DISTINCT `scc`.`note`
            ORDER BY `scc`.`id` ASC
            SEPARATOR ', ') AS `notes`,
        `scc`.`student_id` AS `student_id`,
        `scc`.`year_id` AS `year_id`,
        SUM(IF((`amr_t`.`type` = '+'),
            `scc`.`amount`,
            -(`scc`.`amount`))) AS `amount`
    FROM
        (`student_correction` `scc`
        LEFT JOIN `correction_type` `amr_t` ON ((`scc`.`correction_type_id` = `amr_t`.`id`)))
    GROUP BY `scc`.`student_id` , `scc`.`year_id`;

USE `spt`;
CREATE 
     OR REPLACE ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `view_corrections` AS
    SELECT 
        GROUP_CONCAT(DISTINCT '(',
            `amr_t`.`type`,
            ') ',
            `amr_t`.`name`
            ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `names`,
        GROUP_CONCAT(DISTINCT '(',
            `amr_t`.`type`,
            ') ',
            `amr_t`.`name`, ' ', `scc`.`amount`, ' $'
            ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `full_details`,
        GROUP_CONCAT(DISTINCT `amr_t`.`type`, `scc`.`amount`, ' $'
            ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `details`,
        GROUP_CONCAT(DISTINCT `scc`.`note`
            ORDER BY `scc`.`id` ASC
            SEPARATOR ', ') AS `notes`,
        `scc`.`student_id` AS `student_id`,
        `scc`.`year_id` AS `year_id`,
        SUM(IF((`amr_t`.`type` = '+'),
            `scc`.`amount`,
            -(`scc`.`amount`))) AS `amount`
    FROM
        (`student_correction` `scc`
        LEFT JOIN `correction_type` `amr_t` ON ((`scc`.`correction_type_id` = `amr_t`.`id`)))
    GROUP BY `scc`.`student_id` , `scc`.`year_id`;
