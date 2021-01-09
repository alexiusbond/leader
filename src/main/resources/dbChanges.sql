
DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `update_stud_statuses`(y_id int)
BEGIN
update student as st inner join (select max(so.id) as max_id,  so.student_id as student_id 
from student_orders as so  left join student as st1 on so.student_id = st1.id 
where  so.year_id = y_id and so.is_valid=1 group by so.student_id)  as it on st.id = it.student_id set st.class_name_id = 
(select  so2.to_class_name_id from student_orders as so2 where so2.id = it.max_id),  st.education_status_id = 
(select so3.to_education_status_id  from student_orders as so3 where so3.id = it.max_id);
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `processorders`(y_id int)
BEGIN
DECLARE done BOOLEAN DEFAULT 0;
   DECLARE stud_id INT;
   -- Declare the cursor
   DECLARE student_ids CURSOR
   FOR
   SELECT DISTINCT(student_id) as student_id
FROM student_orders
WHERE year_id = y_id AND (orders_id = 1 OR orders_id = 2) AND is_valid = 1;
   -- Declare continue handler
   DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done=1;
   -- Open the cursor
   OPEN student_ids;
   -- Loop through all rows
   REPEAT
      -- Get order number
      FETCH student_ids INTO stud_id;
      -- Insert order and total into ordertotals
DELETE FROM student_orders 
WHERE
    student_id = stud_id
    AND year_id > y_id
    AND is_valid = 1;
   -- End of loop
   UNTIL done END REPEAT;
   -- Close the cursor
   CLOSE student_ids;
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `processyears`()
BEGIN
DECLARE done BOOLEAN DEFAULT 0;
   DECLARE year_id INT;
   -- Declare the cursor
   DECLARE year_ids CURSOR
   FOR
   SELECT id from year order by id asc;
   -- Declare continue handler
   DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done=1;
   -- Open the cursor
   OPEN year_ids;
   -- Loop through all rows
   REPEAT
      -- Get order number
      FETCH year_ids INTO year_id;
      -- Insert order and total into ordertotals
CALL `processorders`(year_id);
CALL `update_stud_statuses`(year_id);
   -- End of loop
   UNTIL done END REPEAT;
   -- Close the cursor
   CLOSE year_ids;
END$$
DELIMITER ;


