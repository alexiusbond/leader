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
