CREATE TABLE `source_example_users` (
  `user_id` int(11) NOT NULL DEFAULT 0,
  `user_name` varchar(255) NOT NULL DEFAULT 'unknown',
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
)