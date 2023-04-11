USE `spt`;
CREATE 
     OR REPLACE ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `view_student_last_class_status` AS
    SELECT 
        `t`.`student_id` AS `student_id`,
        `t`.`class_name` AS `class_name`,
        `t`.`class_number` AS `class_number`,
        `t`.`class_name_id` AS `class_name_id`,
        `t`.`class_number_id` AS `class_number_id`,
        `t`.`education_status` AS `education_status`,
        `t`.`education_status_id` AS `education_status_id`
    FROM
        `view_student_class_status` `t`
    WHERE
        (`t`.`year_id` = (SELECT 
                MAX(`so`.`year_id`)
            FROM
                `student_orders` `so`
            WHERE
                (`so`.`student_id` = `t`.`student_id`) and so.is_valid=1));
