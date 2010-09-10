CREATE TABLE `dim_example_users` (
  `user_tk` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL DEFAULT 0,
  `user_name` varchar(255) NOT NULL DEFAULT 'unknown',
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `date_from` datetime DEFAULT NULL,
  `date_to` datetime DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `current` char(1) DEFAULT NULL,
  PRIMARY KEY (`user_tk`),
  UNIQUE KEY `dim_users_user_id_version` (`user_id`,`version`)
)