UPDATE `spt`.`permissions` SET `permissions` = 'показ в меню,отчет по количеству преподаваемых часов,общий отчет по кадрам' WHERE (`java_class_name` = 'HRReportsView');

UPDATE `spt`.`user_permission` SET `permissions` = 'HRReportsView:показ в меню,отчет по количеству преподаваемых часов,общий отчет по кадрам' WHERE (`role_name` = 'hr') and (`permissions` = 'HRReportsView:показ в меню,отчет по количеству преподаваемых часов');
UPDATE `spt`.`user_permission` SET `permissions` = 'HRReportsView:показ в меню,отчет по количеству преподаваемых часов,общий отчет по кадрам' WHERE (`role_name` = 'admin') and (`permissions` = 'HRReportsView:показ в меню,отчет по количеству преподаваемых часов');

SELECT 
    e.id,
    e.login,
    e.name,
    e.surname,
    e.middle_name,
    e.date_of_birth,
    e.can_advisor,
    g.name,
    n.name,
    m.name,
    ws.name,
    p.name,
    GROUP_CONCAT(DISTINCT p2.name
        ORDER BY eo2.id ASC
        SEPARATOR ', ') AS extra_positions,
    GROUP_CONCAT(DISTINCT IF(eb.hr_importance_id = 1,
            br.name,
            NULL)
        ORDER BY eb.id ASC
        SEPARATOR ', ') AS main_branch,
    GROUP_CONCAT(DISTINCT IF(eb.hr_importance_id = 2,
            br.name,
            NULL)
        ORDER BY eb.id ASC
        SEPARATOR ', ') AS extra_branches,
    ebh.hours,
    ebh.extra,
    sch.name_ru
FROM
    employee AS e
        LEFT JOIN
    gender AS g ON g.id = e.gender_id
        LEFT JOIN
    nationality AS n ON n.id = e.nationality_id
        LEFT JOIN
    hr_martial_status AS m ON m.id = e.hr_martial_status_id
        LEFT JOIN
    hr_employee_branch AS eb ON eb.employee_id = e.id
        LEFT JOIN
    hr_branch AS br ON br.id = eb.hr_branch_id
        LEFT JOIN
    hr_employee_order AS eo ON eo.id = (SELECT 
            MAX(id)
        FROM
            hr_employee_order
        WHERE
            employee_id = e.id AND to_date IS NULL
                AND school_id IN (2,3))
        LEFT JOIN
    hr_position AS p ON p.id = eo.hr_position_id
        LEFT JOIN
    position AS pos ON p.id = pos.hr_position_id
        LEFT JOIN
    hr_employee_order AS eo2 ON eo2.employee_id = e.id
        AND eo2.hr_orders_id = 2
        AND (eo2.to_date IS NULL
        OR eo2.to_date >= NOW())
        LEFT JOIN
    hr_position AS p2 ON p2.id = eo2.hr_position_id
        LEFT JOIN
    hr_orders AS ord ON ord.id = eo.hr_orders_id
        LEFT JOIN
    working_status AS ws ON ord.working_status_id = ws.id
        LEFT JOIN
    (SELECT 
        SUM(hours) AS hours,
            SUM(extra_hours) AS extra,
            employee_id AS e_id,
            year_id AS y_id,
            school_id AS sch_id
    FROM
        hr_employee_branch_hours
    GROUP BY employee_id , year_id , school_id) AS ebh ON ebh.e_id = e.id AND ebh.y_id = 6
        AND ebh.sch_id = eo.school_id
        LEFT JOIN
    school AS sch ON sch.id = eo.school_id
WHERE
    eo.school_id IN (2,3)
        AND ord.working_status_id IS NOT NULL
        AND ws.id IN (1 , 2, 3, 4, 5)
GROUP BY e.id
ORDER BY sch.id , e.id DESC