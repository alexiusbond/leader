CREATE TABLE `initial_passwords` (
  `login` int NOT NULL,
  `password` varchar(10) NOT NULL,
  PRIMARY KEY (`login`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

insert into initial_passwords (login, password)
SELECT login, LEFT(MD5(RAND()), 8) from employee;

update employee e set e.password = (select SHA2(t.password, 256) password from initial_passwords t where t.login = e.login) where e.id in (
3592,3591,3589,2359,2358,2357,2349,2343,2337,2336,2335,2334,2333,2329,2326,2322,2321,2317,2312,2304,607,606,605,604,603,602,601,600,599,595,594,593,592,591,590,589,588,587,586,585,584,583,581,579,577,576,575,574,573,571,570,568,565,561,558,557,556,555,554,553,551,550,42,41,6);