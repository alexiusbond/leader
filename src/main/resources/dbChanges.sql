UPDATE spt.user_permission t
SET t.permissions = 'ShowAllSchools:меню'
WHERE t.role_name LIKE 'sapat#_secretary' ESCAPE '#' AND t.permissions LIKE 'ChangeSchool:изменение' ESCAPE '#';

UPDATE spt.user_permission t
SET t.permissions = 'ShowAllSchools:меню'
WHERE t.role_name LIKE 'admin' ESCAPE '#' AND t.permissions LIKE 'ChangeSchool:изменение' ESCAPE '#';

UPDATE spt.user_permission t
SET t.permissions = 'ShowAllSchools:меню'
WHERE t.role_name LIKE 'hr' ESCAPE '#' AND t.permissions LIKE 'ChangeSchool:изменение' ESCAPE '#';

create table if not exists spt.employee_hide_school
(
    employee_id integer     not null,
    school_id integer     not null
)
    collate = utf8_unicode_ci;

alter table employee_hide_school
    add constraint employee_hide_school_employee_id_fk
        foreign key (employee_id) references employee (id);

alter table employee_hide_school
    add constraint employee_hide_school_school_id_fk
        foreign key (school_id) references school (id);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 26);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 28);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 33);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 31);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 24);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 34);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 13);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 14);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 15);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 17);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 18);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 19);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 32);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 20);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 23);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 1);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 16);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 21);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 22);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 25);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 29);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 3);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 2);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 4);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 5);

INSERT INTO spt.employee_hide_school (employee_id, school_id)
VALUES (3762, 6);

UPDATE spt.user_permission t
SET t.permissions = 'ShowAllSchools:показ в меню'
WHERE t.role_name LIKE 'sapat#_secretary' ESCAPE '#' AND t.permissions LIKE 'ShowAllSchools:меню' ESCAPE '#';

UPDATE spt.user_permission t
SET t.permissions = 'ShowAllSchools:показ в меню'
WHERE t.role_name LIKE 'hr' ESCAPE '#' AND t.permissions LIKE 'ShowAllSchools:меню' ESCAPE '#';

UPDATE spt.user_permission t
SET t.permissions = 'ShowAllSchools:показ в меню'
WHERE t.role_name LIKE 'admin' ESCAPE '#' AND t.permissions LIKE 'ShowAllSchools:меню' ESCAPE '#';

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 3762;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 61;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 5282;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 59;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 57;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 62;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 3176;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 151;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 58;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 5625;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 4853;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 5956;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 4851;

UPDATE spt.employee t
SET t.password = '63532e55dc1def037134b21ebfcbefe93fb205942af82a4f5f3884a3e07a8c93'
WHERE t.id = 5624;

