UPDATE `spt`.`acc_category` SET `name` = 'Kısa Vadeli Borclar' WHERE (`id` = '8969');
DELETE FROM `spt`.`acc_category` WHERE (`id` = '9114');
UPDATE `spt`.`acc_category` SET `name` = 'KISA VADELI BORCLAR', `code` = '336' WHERE (`id` = '8969');
insert into acc_category (name, parent_code, code, parent_id, acc_type_id, activity_status_id, note, school_id, employee_id, modified_employee_id)
select concat('KISA VADELI BORCLAR - ',name_ru), '336', code, 8969, 5, 2, null, id, null, 1 from school;
