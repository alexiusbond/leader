CREATE TABLE `dm_floor` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `floor_name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('-2');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('-1');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('0');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('1');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('2');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('3');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('4');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('5');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('6');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('7');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('8');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('9');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('10');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('11');
INSERT INTO `spt`.`dm_floor` (`name`) VALUES ('12');
CREATE TABLE `dm_block` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `school_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dm_block_unq` (`school_id`,`name`),
  KEY `fk_dm_block_school1_idx` (`school_id`),
  CONSTRAINT `fk_dm_block_school1` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `dm_room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `description` varchar(150) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `block_id` int NOT NULL,
  `floor_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_dm_room_block1_idx` (`block_id`),
  KEY `fk_dm_room_floor1_idx` (`floor_id`),
  CONSTRAINT `fk_dm_room_block1` FOREIGN KEY (`block_id`) REFERENCES `dm_block` (`id`),
  CONSTRAINT `fk_dm_room_floor1` FOREIGN KEY (`floor_id`) REFERENCES `dm_floor` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `dm_inventory_category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dm_inventory_category_unq` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('MASA');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('SANDALYE');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('DOLAP');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('SEHPA');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('ELEKTRONIK');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('RANZA');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('KOLTUK');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('TARAYICI');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('LAPTOP');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('AKSESUAR');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('MUTFAK MALZEMELERI');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('LABORATUVAR MALZEMELERI');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('CELIK KASA');
INSERT INTO `spt`.`dm_inventory_category` (`name`) VALUES ('CEKMECE');

CREATE TABLE `dm_invoice` (
  `id` int NOT NULL AUTO_INCREMENT,
  `invoice_number` int NOT NULL,
  `creation_date` datetime NOT NULL,
  `note` varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `room_id` int NOT NULL,
  `activity_status_id` int NOT NULL,
  `school_id` int NOT NULL,
  `employee_id` int NOT NULL,
  `modification_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `invoice_unq` (`invoice_number`,`school_id`,`activity_status_id`),
  KEY `index_room3` (`room_id`),
  KEY `index_activity_status7` (`activity_status_id`),
  KEY `index8` (`school_id`),
  KEY `index9` (`employee_id`),
  CONSTRAINT `fk_dm_invoice_1` FOREIGN KEY (`room_id`) REFERENCES `dm_room` (`id`),
  CONSTRAINT `fk_dm_invoice_5` FOREIGN KEY (`activity_status_id`) REFERENCES `activity_status` (`id`),
  CONSTRAINT `fk_dm_invoice_6` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `fk_dm_invoice_7` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `dm_title` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dm_title_unq` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `dm_brand` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(120) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dm_brand_unq` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

LOCK TABLES `dm_brand` WRITE;
/*!40000 ALTER TABLE `dm_brand` DISABLE KEYS */;
INSERT INTO `dm_brand` VALUES (43,'ABAT'),(1,'ACER'),(2,'AIDATA'),(49,'AK BATA'),(3,'ALARKO'),(4,'ALASKA'),(5,'APPLE'),(6,'ARCELIK'),(7,'ASUS'),(52,'ATEC'),(40,'ATLANTIK'),(37,'BALLU'),(8,'BEKO'),(32,'BESGEN'),(44,'BIRUSA'),(50,'BOLIAN'),(9,'BOSCH'),(45,'BOSS'),(30,'CALISKAN CELIK'),(10,'CANON'),(11,'CASPER'),(46,'CBFLA'),(12,'DELL'),(13,'EPSON'),(14,'EZCOOL'),(33,'GLOKE VIEW'),(53,'HAMMER'),(15,'HP'),(47,'IDEA'),(38,'IRAN'),(16,'KAREL'),(17,'KODAK'),(18,'KUMTEL'),(19,'LAXON'),(20,'LENEVO'),(21,'LEXMARK'),(41,'LG'),(22,'LUXEL'),(34,'MOBELLO'),(48,'NANSEN'),(39,'OSTEN'),(35,'OZEL SIRKET'),(23,'PACKARD DELL'),(24,'PHILIPS'),(25,'REMTA'),(26,'SAMSUNG'),(27,'SANTANA'),(28,'SONY'),(42,'TEFAL'),(36,'TURK MALI'),(51,'URAGAN'),(29,'VESTEL'),(31,'XIAOMI');
/*!40000 ALTER TABLE `dm_brand` ENABLE KEYS */;
UNLOCK TABLES;


LOCK TABLES `dm_title` WRITE;
/*!40000 ALTER TABLE `dm_title` DISABLE KEYS */;
INSERT INTO `dm_title` VALUES (1,'1 GÖZLÜ EVRAK DOLABI'),(364,'10L'),(2,'2 ÇEKMECELİ DOLAPLI'),(324,'2 COZLU KASA'),(366,'2 GOZLU ASKILIK DOLABI'),(3,'2 GÖZLÜ EVRAK DOLABI'),(351,'2 GOZLU FIRIN'),(362,'2 KISHILIK'),(321,'2 KISILIK KOLTUK'),(4,'21 PARÇA MUHTELİF EBATLARDA STORE PERDE'),(5,'3 ÇEKMECELİ KESON'),(6,'3 ÇEKMECELİ TEKERLEKLİ KESON'),(325,'3 COZLU KAPAKLI DOSYA DOLABI'),(7,'3 GÖZLÜ DOSYA DOLABI'),(8,'3 GÖZLÜ EVRAK DOLABI'),(307,'3 GOZLU KAPAKLI DOSYA DOLABI'),(368,'4 COZLU KITAP DOLABI'),(293,'4 GOZLU ASKILIK DOLABI'),(9,'4 GÖZLÜ DOSYA DOLABI'),(10,'4 KAPAKLI DOLAP'),(11,'4 RAFLI KİTAPLIK'),(356,'58CM 1 KISHILIK'),(360,'58СМ 1 KISHILIK'),(358,'58СМ 2 KISHILIK'),(369,'6 GOZLU ELBISE DOLABI'),(341,'6 GOZLU ELEKTRIKLI OCAK'),(361,'65CM 1 KISHILIK'),(357,'65СМ 1 KISHILIK'),(319,'65СМ 2 KISHILIK'),(320,'66СМ 1 KISHILIK'),(359,'70CM 1 KISHILIK'),(333,'70СМ 1 KISHILIK'),(332,'70СМ 2 KISHILIK'),(316,'70СМ 6 KISHILIK'),(12,'8 GÖZLÜ DOSYA DOLABI'),(327,'8 GÖZLÜ DOSYA OGRENCI DOLABI'),(315,'80СМ 1 KISHILIK'),(13,'A 002 MITSUBISHI KLİMA'),(14,'ACER MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(15,'ACER PROJEKSİYON'),(16,'AÇIK RAFLI KAPAKSIZ DOLAP'),(17,'ADET KİTAPLIK'),(18,'AHŞAP DOLAP'),(19,'AİDATA AS-4211 SPECTRA BİLGİSAYAR+MONİTÖR'),(20,'AİRCONDİTİONER PENCERE TİPİ KLİMA'),(21,'ALARKO GENERAL KLİMA'),(22,'ALASKA SEBİL'),(328,'ALFABE PANO'),(23,'ALTUS BUZDOLABI'),(24,'ARÇELİK 51 EKRAN TELEVİZYON'),(25,'ARÇELİK AA SPLİT KLİMA'),(26,'ARÇELİK BUZDOLABI'),(27,'ARÇELİK ELEKTRİKLİ SÜPÜRGE'),(28,'ARÇELİK FIRIN'),(29,'ARÇELİK KLİMA'),(30,'ARÇELİK MİKRODALGA FIRIN'),(31,'ARÇELİK MİNİ BUZDOLABI'),(32,'ARÇELİK MÜZİK SETİ'),(33,'ARÇELİK PLUS S SPLİT KLİMA'),(34,'ARÇELİK SU SEBİLİ'),(35,'ARÇELİK VANTİLATÖR'),(36,'ARS 6300 ELETRİK SÜPÜRGESİ'),(37,'ASKILIK вешалка'),(38,'ASPİRATÖR'),(294,'ASSISTANT MASASI'),(39,'ASUS BİLGİSAYAR+MONİTÖR'),(40,'ASUS DİZÜSTÜ BİLGİSAYAR'),(41,'ASUS LAPTOP'),(42,'ASUS LG MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(43,'ASUS NOTEBOOK'),(367,'AYAKKABI RAFI'),(44,'AYAKLI ASKILIK'),(299,'AYNA'),(45,'BEKO 85*54,5*60 BUZDOLABI'),(46,'BEKO BULAŞIK MAKİNASI'),(47,'BEKO BUZDOLABI'),(48,'BEKO MİNİ BUZDOLABI'),(49,'BEKO PRENSES 170 BULAŞIK MAKİNASI'),(50,'BEKO TELEVİZYON'),(51,'BİLGİSAYAR KESON'),(52,'BİLGİSAYAR MASASI'),(53,'BOSCH KLİMA'),(54,'BOSCH-KTG 144 ONE MİNİ BUZDOLABI'),(55,'BROSS BİLGİSAYAR+MONİTÖR'),(56,'BROTHER - FAX 236 FAX MAKİNASI'),(57,'BROTHER FAKS'),(59,'BÜRO KOLTUĞU'),(60,'BÜRO MASASI'),(61,'BÜYÜK BOY ÇALIŞMA MASASI'),(62,'BÜYÜK BOY TOPLANTI MASASI 220*100'),(58,'BUZDOLABI'),(349,'BUZDOLAP'),(78,'C-BOX BİLGİSAYAR+MONİTÖR'),(79,'ÇALIŞMA KOLTUĞU'),(80,'ÇALIŞMA MASASI'),(81,'ÇALIŞMA MASASI 150*75'),(296,'CAM GERB. BAYRAK'),(63,'CAM KAPAKLI DOSYA DOLABI'),(64,'CAM KAPAKLI KÜTÜPHANE DOLABI'),(65,'CAM SEPRA PERDE'),(66,'CAMLI KAPAKLI ARŞİV DOLABI'),(67,'CAMLI KAPAKLI DOSYA DOLABI'),(68,'CAMLI KİTAPLIK'),(69,'CANON D 1100 FOTOĞRAF MAKİNASI'),(70,'CANON LİDE 60 TARAYICI'),(71,'CANON MX395 YAZICI'),(72,'CANON POWERSHAT A2300 GÜMÜŞ FOTOGRAF MAKİNASI'),(73,'CANON PROJEKSİYON CİHAZI'),(74,'CANON TARAYICI'),(75,'CASPER BİLGİSAYAR+MONİTÖR'),(76,'CASPER LAPTOP'),(77,'CASPER NİRVANA MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(82,'ÇAY MAKİNASI'),(83,'ÇEKMECELİ ÇALIŞMA MASASI'),(84,'ÇEKMECELİ DOLAP'),(308,'ÇEKMECELİ KESON'),(85,'ÇEKMECELİ SEHPA'),(86,'ÇELİK DİZAYN SÜRGÜLÜ ASPİRATÖR'),(87,'ÇELİK KASA'),(291,'COP KOVASI'),(88,'DAKTİLO'),(89,'DARK ENİGMA BİLGİSAYAR+MONİTÖR'),(90,'DAVLUMBAZ'),(91,'DELL INSPİRON 6400 DİZÜSTÜ BİLGİSAYAR'),(92,'DELL MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(93,'DERİLİ OFİS KOLTUĞU'),(94,'DOLAP'),(95,'DOLAPLI ÇEKMECE'),(96,'DOLAPLI KİTAPLIK'),(363,'DOSYA DOLABI'),(97,'DOSYA DOLAP'),(318,'ELBISE ASKI'),(98,'ELBİSE ASKILIK'),(99,'ELECTROLUX KLİMA'),(100,'ELEKTRİK SÜPÜRGESİ'),(365,'ELEKTRIKI ISITICI'),(101,'ELEKTRİKLİ ISITICI'),(374,'ELEKTRIKLİ OCAK'),(102,'ELEKTRİKLİ SOBA'),(373,'ELEKTRİKLİ SÜPÜRGE'),(103,'EPSON MARKA PROJEKSİYON'),(104,'EPSON PROJEKSİYON CİHAZI'),(105,'ETAJER'),(106,'EVEREST BİLGİSAYAR KASASI'),(107,'EVEREST PROJEKSİYON PERDESİ'),(108,'EZCOOL MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(109,'FAN'),(110,'FAX CİHAZI PANASONIC KX-FP205'),(111,'FİESTA TEZGAH ÜSTÜ OCAK'),(112,'FOTOKOPİ ÇEKMECELİ DOLAP'),(113,'FOTOKOPİ MAKİNASI STANDI'),(114,'FRİSBY MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(347,'FRITOZ'),(115,'FUJITSU ASY-24 RB KLİMA'),(116,'FUJİTSU MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(117,'GARDEN TİME PLASTİK SANDALYE'),(118,'GENERAL KLİMA'),(331,'GERB'),(336,'GERB. BAYRAK'),(119,'GIGABEYTE BİLGİSAYAR+MONİTÖR'),(371,'HALI'),(120,'HARDDİSK VERBATIM 500 GB HDD'),(122,'HP 1005 YAZICI'),(123,'HP 1010 LASERJET YAZICI'),(124,'HP 1010 W LASER JET'),(125,'HP 1020 YAZICI'),(126,'HP 1102 LASERJET YAZICI'),(127,'HP 1410 YAZICI'),(128,'HP 1600 COLOR LASER YAZICI'),(129,'HP 640 C YAZICI'),(130,'HP DESKJET YAZICI+TARAYICI'),(131,'HP ELİTE MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(132,'HP LASERJET 1018 YAZUCI'),(133,'HP LASERJET 1020'),(134,'HP LASERJET 2700'),(135,'HP LASERJET M127-M128 YAZICI(FAX-FOTOKOPİ-TARAYICI'),(136,'HP LASERJET P1102 YAZICI'),(137,'HP LASERJET YAZUCI-TARAYICI'),(138,'HP PAVİLİON DİZÜSTÜ BİLGİSAYAR'),(139,'HP YAZICI'),(140,'İKİLİ BEKLEME KOLTUĞU'),(141,'İKİLİ DERİ KOLTUK'),(142,'İKİLİ KOLTUK'),(143,'İKİLİ SEHPALI DERİ BEKLEME SANDALYESİ'),(144,'İMALAT RAFLI DOLAP (KÜTÜPHANE)'),(145,'JALUZİ MASA'),(338,'KANAPE'),(323,'KAPAKLI DOSYA DOLABI'),(146,'KAREL MS38 SANTRAL'),(147,'KAREL SANTRAL'),(148,'KAREL TELEFON'),(149,'KESON'),(150,'KİTAPLIK'),(343,'KIYMA MAKINASI'),(151,'KODAK C 533 FOTOĞRAF MAKİNASI'),(152,'KODAK DİJİTAL FOTOKOPİ MAKİNASI'),(153,'KODAK EASYSHARE C533 FOTOĞRAK MAKİNASI'),(154,'KOLÇAKLI DİNLEYİCİ SANDELYESİ'),(155,'KOLÇAKLI SANDALYE'),(156,'KOLÇAKSIZ SİYAH SANDALYE'),(376,'KOLONKA MUZIK'),(157,'KOLSUZ SİYAH KOLTUK'),(158,'KOLTUK'),(159,'KOLTUK TEKLİ (DERİ-KREM)'),(160,'KOLTUK YATARLI KREM RENGİ'),(375,'KOMUZ'),(161,'KONSOL'),(162,'KONUŞMACI MASASI'),(163,'KONUŞMACI SANDALYESİ'),(164,'KROM DERİLİ MİSAFİR KOLTUĞU'),(166,'KÜÇÜK 2 GÖZLÜ EVRAK DOLABI'),(167,'KÜÇÜK BOY ÇALIŞMA MASASI'),(168,'KÜÇÜK ÇALIŞMA MASASI'),(169,'KÜÇÜK DOLAP'),(165,'KUMTEL OCAK'),(170,'KÜTÜPHANE DOLABI'),(171,'KYOCERA FS-6025 NETWORK YAZICI FOTOKOPİ'),(172,'KYOCERA KM-2050 FOTOKOPİ MAKİNASI'),(173,'KYOCERA YAZICI'),(174,'LAXON TD79 TELSİZ TELEFON'),(175,'LAXON TD91 TELSİZ TELEFON'),(176,'LENOVO DİZÜSTÜ BİLGİSAYAR'),(177,'LENOVO MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(178,'LEXMARK MX310DN FAKSLI ÇOK FONKSİYONLU YAZICI'),(179,'LİBRA 260 LIK TOPLANTI MASASI'),(180,'LUXELL ISITICI'),(181,'MAGNUM ISITICI'),(182,'MAKAM DOLABI'),(183,'MAKAM KOLTUĞU'),(184,'MAKAM MASASI'),(185,'MAKAM ÜÇ ÇEKMECELİ KOMİDİN'),(186,'MASA'),(187,'MASA SANDALYE TAKIMI'),(188,'MASA ÜNİTESİ'),(189,'MASA VANTİLATÖRÜ'),(190,'MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(191,'MFC -7320 YAZICI-FAKS-TARAYICI'),(352,'MIKRODALGA FIRIN'),(309,'MINI BUZDOLAP'),(298,'MISAFIR KANAPE'),(193,'MİSAFİR KOLTUĞU'),(194,'MİSAFİR SANDALYESİ'),(192,'MITSUBISHI KLİMA'),(195,'MOTOROLA TELSİZ TELEFON'),(334,'MUDUR KOLTUGU'),(196,'MUTFAK DOLABI'),(337,'MUTFAK MASASI'),(197,'MUTFAK OCAĞI'),(353,'MUTFAK SANDALYE'),(198,'NATUREL 3 LÜ OCAK'),(199,'NETAŞ TELEFON'),(312,'NOTEBOOK'),(335,'OFIS MOBILYA: DOLAP. SEHBA. MASA. 2KOLTUK\n 1 KANAPE'),(200,'OFİS SANDALYESİ'),(304,'OGRETMEN KOLTUGU'),(306,'OGRETMEN MASASI'),(201,'ORTA BOY TOPLANTI MASASI 150*75'),(372,'OSMANLI KOLTUK'),(202,'ÖZEL İMALAT SEKRETER KOMİDİN'),(203,'ÖZEL İMALAT TABLA'),(204,'PANASONIC FAKS MAKİNASI'),(205,'PANASONIC KX-FT908TK FAKS MAKİNASI'),(206,'PANASONIC TELSİZ TELEFON'),(311,'PANO'),(301,'PANO CAM'),(295,'PANO TAHTA'),(339,'PASLANMAZ CELIK MASASI'),(340,'PASLANMAZ CELIK RAFLAR'),(344,'PASLANMAZ DAGITIM TABLO SETI'),(354,'PASLANMAZ DONDURUCU'),(346,'PASLANMAZ EKMEK MASASI'),(342,'PASLANMAZ ELEKTRIKLI KIZARTMA TAVASI 80L'),(345,'PASLANMAZ ET MASASI'),(348,'PASLANMAZ TENCERE 50L'),(207,'PERDE'),(208,'PERSONEL MASASI (TAŞIYICI KESON)'),(209,'PHILIPS CD 150 TELSİZ TELEFON'),(210,'PHILIPS IMPACT EXCEL 1700 WATT ELEKTRİKLİ SÜPÜRGE'),(211,'PHILIPS LCD TELEVİZYON'),(212,'PHILIPS MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(213,'PHILIPS TELEFON'),(214,'PHILIPS TELSİZ TELEFON'),(215,'PLASTİK KOLLU KAHVERENGİ SANDALYE'),(216,'PLASTİK MASA'),(217,'PORTMANTO'),(218,'PREMİER ELEKTRİKLİ TEKLİ OCAK'),(219,'PRINCESS'),(220,'PROJEKSİYON CİHAZI VE PERDESİ'),(221,'PROJEKSİYON PERDESİ'),(222,'PROJEKSİYON PERDESİ 180*180'),(314,'PROJEKSİYON+AKILLI TAHTA'),(223,'QUADRO MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(303,'RADYATOR'),(300,'RAF KIRGIZSTAN'),(224,'RAKS MASA VANTİLATÖRÜ'),(225,'REMTA SANAYİ TİPİ ÇAY MAKİNASI'),(326,'SAAT'),(226,'SAMSUNG 1640 LAZER YAZICU'),(227,'SAMSUNG FOTOKOPİ-SCANNER -FAX MAKİNASI'),(228,'SAMSUNG LCD TV'),(229,'SAMSUNG MASAÜSTÜ BİLGİSAYAR+MONİTÖR'),(230,'SAMSUNG SCX - 3405FW FAX-YAZICI'),(231,'SAMSUNG SCX 4623 FN FAX-TARAYICI'),(232,'SAMSUNG SCX-4623F YAZICI'),(233,'SAMSUNG SCX-4623FN YAZICI'),(234,'SANDALYE'),(370,'SANDALYE MUTFAK'),(235,'SANTANA ETAJER 100 LÜK'),(236,'SANTANA ETAJER 80*50*75'),(237,'SANTANA MASA 180*90*75'),(238,'SANTANA SEHPA 70*50*40'),(239,'SANYO PDG-DS420E PROJEKSİYON ALETİ-PERDESİ'),(297,'SATRANC MASASI'),(255,'ŞEF KOLTUĞU'),(240,'SEHPA'),(241,'SEHPA 110*68'),(242,'SEHPA 120*60'),(243,'SEHPA 50*60'),(244,'SEHPA 55*55'),(245,'SEHPA 60*37'),(246,'SEKRETER KOLTUĞU'),(247,'SEKRETER MASASI'),(248,'SETÜSTÜ OCAK'),(249,'SİEMENS TELSİZ TELEFON'),(330,'SINIF PANO'),(250,'SİYAH CAMLI KÜTÜPHANE DOLABI'),(355,'SPLİT KLİMA'),(251,'STOR PERDE'),(292,'SU SEBILI'),(252,'SUGA-EDA ŞEF KOLTUĞU'),(253,'SUPER POWER BİLGİSAYAR+MONİTÖR'),(254,'SÜRGÜLÜ CAM KAPAKLI KÜTÜPHANE DOLABI'),(273,'T-TEC KAMERA'),(256,'TABURE'),(305,'TARAYICI'),(329,'TEBESIR TAHTASI'),(257,'TEK KİŞİLİK KOLTUK'),(258,'TEK ŞERİTLİ ISITICI'),(121,'TEKERLEKLİ SANDALYE'),(259,'TEKLİ BEKLEME KOLTUĞU'),(260,'TEKLİ DERİ KOLTUK'),(261,'TEKLİ DERİ SANDALYE'),(262,'TEKLİ KOLTUK'),(263,'TEKLİ MİSAFİR KOLTUK'),(264,'TEKLİ OCAK'),(265,'TELEFON'),(266,'TELEFON MİNTON'),(302,'TELEVIZYON'),(267,'TELSİZ TELEFON'),(350,'TERAZI'),(310,'TIBBI KANAPE'),(268,'TOPLANTI MASASI'),(269,'TOPLANTI MASASI 5 PARÇA'),(270,'TOPLANTI MASASI OVAL'),(271,'TOPLANTI OTURMA BİRİMLERİ'),(272,'TOPLANTI SALONU SANDALYE'),(317,'TUBA KANAPE'),(275,'ÜÇLÜ DERİLİ BEKLEME SANDALYESİ'),(276,'ÜSTÜ CAM KAPAKLI ALTI EVRAK DOLABI'),(274,'UZUN SERGİ MASASI 50*200'),(277,'VANTİLATÖR'),(278,'VENTO BİLGİSAYAR+MONİTÖR'),(279,'VESTEL BİOPLUS 18000 KLİMA'),(280,'VESTEL BULAŞIK MAKİNASI'),(281,'VESTEL KLİMA'),(282,'VESTEL MÜZİK SETİ'),(283,'VESTEL RT 426 B CLASS TEK KAPILI BUZDOLABI'),(284,'VESTEL SPLİT KLİMA'),(285,'VESTEL SX-12 12.000 BTU BİO KLİMA'),(286,'VESTEL SX-18 18.000 BTU BİO KLİMA'),(287,'VESTİYER'),(288,'YATARLI SEKRETER KOLTUK'),(322,'YAZICI'),(289,'YUVARLAK ÇALIŞMA MASASI'),(290,'YUVARLAK MASA');
/*!40000 ALTER TABLE `dm_title` ENABLE KEYS */;
UNLOCK TABLES;


CREATE TABLE `dm_inventory_movements` (
  `id` int NOT NULL AUTO_INCREMENT,
  `invoice_id` int NOT NULL,
  `inventory_category_id` int NOT NULL,
  `title_id` int NOT NULL,
  `brand_id` int NOT NULL,
  `amount` int NOT NULL,
  `remain` int NOT NULL,
  `price` double(10,2) NOT NULL,
  `currency_rate` double(8,2) NOT NULL,
  `purchase_date` date NOT NULL,
  `shelf_life` int NOT NULL,
  `note` varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `inventory_movements_id` int DEFAULT NULL,
  `order_number` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `index3` (`invoice_id`),
  KEY `index4` (`inventory_category_id`),
  KEY `index5` (`brand_id`),
  KEY `index7` (`title_id`),
  KEY `index6` (`inventory_movements_id`),
  CONSTRAINT `fk_dm_inventory_movements_1` FOREIGN KEY (`invoice_id`) REFERENCES `dm_invoice` (`id`),
  CONSTRAINT `fk_dm_inventory_movements_2` FOREIGN KEY (`inventory_category_id`) REFERENCES `dm_inventory_category` (`id`),
  CONSTRAINT `fk_dm_inventory_movements_3` FOREIGN KEY (`brand_id`) REFERENCES `dm_brand` (`id`),
  CONSTRAINT `fk_dm_inventory_movements_4` FOREIGN KEY (`inventory_movements_id`) REFERENCES `dm_inventory_movements` (`id`),
  CONSTRAINT `fk_dm_inventory_movements_5` FOREIGN KEY (`title_id`) REFERENCES `dm_title` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('InventoryOrganizationView', 'Организация инвентаря', 'показ в меню,добавление,изменение,удаление');
INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('InventoryLiquidationView', 'Ликвидация инвентаря', 'показ в меню,добавление,изменение,удаление');
INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('InventoryReportsView', 'Отчеты по инвентаризации', 'показ в меню');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'InventoryOrganizationView:показ в меню,добавление,изменение,удаление');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'InventoryLiquidationView:показ в меню,добавление,изменение,удаление');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'InventoryReportsView:показ в меню,добавление,изменение,удаление');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'InventoryDefinitionView:показ в меню,добавление,изменение,удаление');
INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('BlockDefinitionView', 'Определение блоков', 'показ в меню,добавление,изменение,удаление');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'BlockDefinitionView:показ в меню,добавление,изменение,удаление');

ALTER TABLE `spt`.`dm_block` 
DROP FOREIGN KEY `fk_dm_block_school1`;
ALTER TABLE `spt`.`dm_block` 
ADD COLUMN `activity_status_id` INT NOT NULL DEFAULT '2' AFTER `school_id`,
CHANGE COLUMN `school_id` `school_id` INT NOT NULL ,
ADD INDEX `fk_dm_block_status1_idx` (`activity_status_id` ASC);
;
ALTER TABLE `spt`.`dm_block` 
ADD CONSTRAINT `fk_dm_block_school1`
  FOREIGN KEY (`school_id`)
  REFERENCES `spt`.`school` (`id`),
ADD CONSTRAINT `fk_dm_status_school1`
  FOREIGN KEY (`activity_status_id`)
  REFERENCES `spt`.`activity_status` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION;

INSERT INTO `spt`.`permissions` (`java_class_name`, `caption`, `permissions`) VALUES ('RoomDefinitionView', 'Определение помещений', 'показ в меню,добавление,изменение,удаление');
INSERT INTO `spt`.`user_permission` (`role_name`, `permissions`) VALUES ('admin', 'RoomDefinitionView:показ в меню,добавление,изменение,удаление');
ALTER TABLE `spt`.`dm_room` 
ADD COLUMN `activity_status_id` INT NOT NULL DEFAULT '2' AFTER `floor_id`,
ADD INDEX `fk_dm_room_status1_idx` (`activity_status_id` ASC);
;
ALTER TABLE `spt`.`dm_room` 
ADD CONSTRAINT `fk_dm_room_status1`
  FOREIGN KEY (`activity_status_id`)
  REFERENCES `spt`.`activity_status` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION;
UPDATE spt.user_permission SET permissions = REPLACE(permissions, 'ContractDefintion', 'ContractDefinition')  where permissions like '%Defintion%';
ALTER TABLE `spt`.`dm_block` 
CHANGE COLUMN `name` `name` VARCHAR(50) CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NOT NULL ;
ALTER TABLE `spt`.`dm_inventory_movements` 
ADD COLUMN `code` INT NOT NULL AFTER `brand_id`;
ALTER TABLE `spt`.`dm_inventory_movements` 
CHANGE COLUMN `code` `code` VARCHAR(50) NOT NULL ;
INSERT INTO `spt`.`dm_brand` (`id`, `name`) VALUES ('54', 'OTHER');
ALTER TABLE `spt`.`dm_inventory_movements` 
ADD INDEX `index8` (`code` ASC);

ALTER TABLE `spt`.`dm_inventory_movements` 
DROP FOREIGN KEY `fk_dm_inventory_movements_4`;
ALTER TABLE `spt`.`dm_inventory_movements` 
DROP COLUMN `order_number`,
DROP COLUMN `inventory_movements_id`,
DROP COLUMN `currency_rate`,
ADD UNIQUE INDEX `unq_code` (`invoice_id` ASC, `code` ASC),
DROP INDEX `index6` ;
ALTER TABLE `spt`.`dm_inventory_movements` ALTER INDEX `index3`;

ALTER TABLE `spt`.`dm_inventory_movements` 
CHANGE COLUMN `shelf_life` `life_time` INT NOT NULL ;
ALTER TABLE `spt`.`dm_inventory_movements` 
DROP COLUMN `note`;
ALTER TABLE `spt`.`dm_inventory_movements` 
CHANGE COLUMN `amount` `quantity` INT NOT NULL ;
DROP VIEW IF EXISTS `spt`.`view_remains` ;
USE `spt`;
CREATE 
     OR REPLACE ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `view_stock_remains` AS
    SELECT 
        `mv`.`acc_category_id` AS `acc_category_id`,
        `mv`.`dp_measurement_id` AS `dp_measurement_id`,
        `inv`.`to_stock_id` AS `stock_id`,
        SUM(`mv`.`remain`) AS `remain`
    FROM
        (`dp_stock_movements` `mv`
        LEFT JOIN `dp_invoice` `inv` ON ((`inv`.`id` = `mv`.`invoice_id`)))
    WHERE
        (`inv`.`service_type_id` = 1)
    GROUP BY `inv`.`to_stock_id` , `mv`.`acc_category_id` , `mv`.`dp_measurement_id`;
ALTER TABLE `spt`.`dm_inventory_movements` 
ADD COLUMN `inventory_movement_id` INT NULL DEFAULT NULL AFTER `life_time`,
DROP INDEX `index8` ,
ADD INDEX `index8` (`inventory_movement_id` ASC);
;
ALTER TABLE `spt`.`dm_inventory_movements` 
ADD CONSTRAINT `fk_dm_inventory_movements_6`
  FOREIGN KEY (`inventory_movement_id`)
  REFERENCES `spt`.`dm_inventory_movements` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION;
ALTER TABLE `spt`.`dm_inventory_movements` 
ADD COLUMN `creation_date` DATE NOT NULL DEFAULT '2021-01-01' AFTER `inventory_movement_id`;

ALTER TABLE `spt`.`dm_inventory_movements` 
DROP FOREIGN KEY `fk_dm_inventory_movements_6`;
ALTER TABLE `spt`.`dm_inventory_movements` 
DROP COLUMN `inventory_movement_id`,
DROP INDEX `index8` ;
ALTER TABLE `spt`.`dm_inventory_movements` 
RENAME TO  `spt`.`dm_inventory_organization` ;

CREATE TABLE `dm_inventory_liquidation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `invoice_id` int NOT NULL,
  `quantity` int NOT NULL,
  `creation_date` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `index3` (`invoice_id`),
  CONSTRAINT `fk_dm_inventory_liquidation_1` FOREIGN KEY (`invoice_id`) REFERENCES `dm_invoice` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `spt`.`dm_inventory_liquidation` 
ADD COLUMN `inventory_id` INT NOT NULL AFTER `creation_date`,
ADD INDEX `index4` (`inventory_id` ASC);
;
ALTER TABLE `spt`.`dm_inventory_liquidation` 
ADD CONSTRAINT `fk_dm_inventory_liquidation_2`
  FOREIGN KEY (`inventory_id`)
  REFERENCES `spt`.`dm_inventory_organization` (`id`)
  ON DELETE RESTRICT
  ON UPDATE NO ACTION;

CREATE 
     OR REPLACE ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `view_inventory_remains` AS
    SELECT 
    io.id AS inventory_id,
    io.quantity - IFNULL(SUM(il.quantity), 0) AS remain
FROM
    dm_inventory_organization AS io
        LEFT JOIN
    dm_inventory_liquidation il ON il.inventory_id = io.id
GROUP BY io.id;
ALTER TABLE `spt`.`dm_inventory_organization` 
DROP COLUMN `remain`;
