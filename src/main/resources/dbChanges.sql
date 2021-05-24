USE `spt`;
DROP procedure IF EXISTS `process_statuses`;

DELIMITER $$
USE `spt`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `process_statuses`(y_id int)
BEGIN
DECLARE done BOOLEAN DEFAULT 0;
   DECLARE school_id INT;
   
   DECLARE school_ids CURSOR
   FOR
   SELECT id from school order by id asc;
   
   DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done=1;
   
   OPEN school_ids;
   
   REPEAT
      
      FETCH school_ids INTO school_id;
      
UPDATE student AS st
        INNER JOIN
    (SELECT 
        MAX(so.id) AS max_id, so.student_id AS student_id
    FROM
        student_orders AS so
    LEFT JOIN student AS st1 ON so.student_id = st1.id
    WHERE
        so.year_id = y_id
            AND st1.school_id = school_id
            AND so.is_valid = 1
    GROUP BY so.student_id) AS it ON st.id = it.student_id 
SET 
    st.class_name_id = (SELECT 
            so2.to_class_name_id
        FROM
            student_orders AS so2
        WHERE
            so2.id = it.max_id),
    st.education_status_id = (SELECT 
            so3.to_education_status_id
        FROM
            student_orders AS so3
        WHERE
            so3.id = it.max_id);
   
   UNTIL done END REPEAT;
   
   CLOSE school_ids;
END$$

DELIMITER ;

USE `spt`;
DROP procedure IF EXISTS `processyears`;

DELIMITER $$
USE `spt`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `processyears`()
BEGIN
DECLARE done BOOLEAN DEFAULT 0;
   DECLARE year_id INT;
   
   DECLARE year_ids CURSOR
   FOR
   SELECT id from year order by id asc;
   
   DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done=1;
   
   OPEN year_ids;
   
   REPEAT
      
      FETCH year_ids INTO year_id;
      
CALL `processorders`(year_id);
CALL `process_statuses`(year_id);
   
   UNTIL done END REPEAT;
   
   CLOSE year_ids;
   

END$$

DELIMITER ;

