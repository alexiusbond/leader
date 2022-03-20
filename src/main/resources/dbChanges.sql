CREATE TABLE `education_language` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
INSERT INTO `spt`.`education_language` (`id`, `name`) VALUES ('1', 'Кыргызский');
INSERT INTO `spt`.`education_language` (`id`, `name`) VALUES ('2', 'Русский');
ALTER TABLE `spt`.`class_name` 
DROP COLUMN `code`,
ADD COLUMN `education_language_id` INT NOT NULL DEFAULT '1' AFTER `activity_status_id`;
ALTER TABLE `spt`.`class_name` 
ADD INDEX `fk_class_name_lang1_idx` (`education_language_id` ASC);
;
ALTER TABLE `spt`.`class_name` 
ADD CONSTRAINT `fk_class_name_langl1`
  FOREIGN KEY (`education_language_id`)
  REFERENCES `spt`.`education_language` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION;
