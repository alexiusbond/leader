USE `spt`;
DROP function IF EXISTS `transliterate_func`;

DELIMITER $$
USE `spt`$$
CREATE FUNCTION `transliterate_func` (original VARCHAR(512)) 
RETURNS varchar(512) CHARSET utf8 READS SQL DATA
DETERMINISTIC
BEGIN
DECLARE translit VARCHAR(512) DEFAULT '';
DECLARE len INT(3) DEFAULT 0;
DECLARE pos INT(3) DEFAULT 1;
DECLARE letter CHAR(4);
 
SET original = TRIM(LOWER(original));
SET len = CHAR_LENGTH(original);
 
WHILE (pos <= len) DO
SET letter = SUBSTRING(original, pos, 1);
 
CASE TRUE
 
WHEN letter IN('á','à','â','ä','å','ā','ą','ă','а','а') THEN SET letter = 'a';
WHEN letter IN('č','ć','ç','ć') THEN SET letter = 'c';
WHEN letter IN('ď','đ','д','д') THEN SET letter = 'd';
WHEN letter IN('é','ě','ë','è','ê','ē','ę','е','е') THEN SET letter = 'e';
WHEN letter IN('ģ','ğ') THEN SET letter = 'g';
WHEN letter IN('í','î','ï','ī','î','и','і') THEN SET letter = 'i';
WHEN letter IN('ķ') THEN SET letter = 'k';
WHEN letter IN('ľ','ĺ','ļ','ł') THEN SET letter = 'l';
WHEN letter IN('ň','ņ','ń','ñ') THEN SET letter = 'n';
WHEN letter IN('ó','ö','ø','õ','ô','ő','ơ','о','о') THEN SET letter = 'o';
WHEN letter IN('ŕ','ř','р','р') THEN SET letter = 'r';
WHEN letter IN('š','ś','ș','ş','с','с') THEN SET letter = 's';
WHEN letter IN('ť','ț') THEN SET letter = 't';
WHEN letter IN('ú','ů','ü','ù','û','ū','ű','ư') THEN SET letter = 'u';
WHEN letter IN('ý','у','у') THEN SET letter = 'u';
WHEN letter IN('ž','ź','ż') THEN SET letter = 'z';
 
WHEN letter = 'б' THEN SET letter = 'b';
WHEN letter = 'в' THEN SET letter = 'v';
WHEN letter = 'г' THEN SET letter = 'g';
WHEN letter = 'д' THEN SET letter = 'd';
WHEN letter = 'ж' THEN SET letter = 'zh';
WHEN letter = 'з' THEN SET letter = 'z';
WHEN letter = 'и' THEN SET letter = 'i';
WHEN letter = 'й' THEN SET letter = 'i';
WHEN letter = 'к' THEN SET letter = 'k';
WHEN letter = 'л' THEN SET letter = 'l';
WHEN letter = 'м' THEN SET letter = 'm';
WHEN letter = 'н' THEN SET letter = 'n';
WHEN letter = 'п' THEN SET letter = 'p';
WHEN letter = 'т' THEN SET letter = 't';
WHEN letter = 'ф' THEN SET letter = 'f';
WHEN letter = 'х' THEN SET letter = 'ch';
WHEN letter = 'ц' THEN SET letter = 'c';
WHEN letter = 'ч' THEN SET letter = 'ch';
WHEN letter = 'ш' THEN SET letter = 'sh';
WHEN letter = 'щ' THEN SET letter = 'shch';
WHEN letter = 'ъ' THEN SET letter = '';
WHEN letter = 'ы' THEN SET letter = 'y';
WHEN letter = 'э' THEN SET letter = 'e';
WHEN letter = 'ю' THEN SET letter = 'ju';
WHEN letter = 'я' THEN SET letter = 'ja';
 
WHEN letter IN ('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','x','y','z')
OR letter REGEXP '^[0-9]+$'
THEN SET letter = letter;

ELSE
SET letter = '-';
 
END CASE;
 
SET translit = CONCAT(translit, letter);
SET pos = pos + 1;
END WHILE;
 
WHILE (translit REGEXP '\-{2,}') DO
SET translit = REPLACE(translit, '--', '-');
END WHILE;
 
RETURN TRIM(BOTH '-' FROM translit);
END$$

DELIMITER ;

