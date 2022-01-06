INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('ImportBranchesFromExcelView', 'Импорт уроков из Excel', 'показ в меню,импорт,удаление');

INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'ImportBranchesFromExcelView:показ в меню,импорт,удаление');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('hr', 'ImportBranchesFromExcelView:показ в меню,импорт,удаление');
