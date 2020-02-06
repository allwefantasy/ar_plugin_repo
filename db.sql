DROP TABLE IF EXISTS `plugin_store_item`;

CREATE TABLE `plugin_store_item` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(256) DEFAULT NULL,
  `path` text,
  `version` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table plugin_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `plugin_user`;

CREATE TABLE `plugin_user` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `plugin_id` int(11) DEFAULT NULL,
  `relate_type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;