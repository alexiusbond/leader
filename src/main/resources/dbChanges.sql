DELETE FROM `spt`.`permissions` WHERE (`java_class_name` = 'MessagesView') and (`permissions` = 'меню,добавление,изменение,удаление');
delete FROM spt.user_permission where permissions like '%MessagesView%';
ALTER TABLE `spt`.`discount_order_messages` 
CHANGE COLUMN `message` `message` VARCHAR(350) CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NULL DEFAULT NULL ;
