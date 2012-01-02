## Prerequisites
[DBQuery](https://github.com/Minecarts/DBQuery) and [DBConnector](https://github.com/Minecarts/DBConnector) configured with a MySQL server and database with the following tables:

```sql
CREATE TABLE `portal_blocks` ( 
  `portal_id` int(11) NOT NULL, 
  `world` char(32) NOT NULL, 
  `x` int(11) NOT NULL, 
  `y` int(11) NOT NULL, 
  `z` int(11) NOT NULL, 
  PRIMARY KEY (`portal_id`,`world`,`x`,`y`,`z`) 
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
```

```sql
CREATE TABLE `portals` ( 
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT, 
  `dest_world` char(32) DEFAULT NULL, 
  `dest_x` double(7,3) DEFAULT NULL, 
  `dest_y` double(7,3) DEFAULT NULL, 
  `dest_z` double(7,3) DEFAULT NULL, 
  `dest_pitch` float DEFAULT NULL, 
  `dest_yaw` float DEFAULT NULL, 
  `REMOVE_dest_vel` float DEFAULT '0', 
  `dest_vel_x` float DEFAULT '0', 
  `dest_vel_y` float DEFAULT '0', 
  `dest_vel_z` float DEFAULT '0', 
  `type` enum('NETHER','HOME','GENERIC','SKYLAND','END') NOT NULL DEFAULT 'NETHER', 
  `activation` enum('INSTANT','DELAYED') NOT NULL DEFAULT 'INSTANT', 
  `flags` set('CLEAR_INVENTORY','REQUIRE_EMPTY_INVENTORY','MESSAGE','SUBSCRIBER','MODE_CREATIVE','MODE_SURVIVAL','NO_SHARED_PORTALING') DEFAULT NULL, 
  `message` varchar(255) DEFAULT NULL, 
  `note` varchar(50) DEFAULT NULL, 
  PRIMARY KEY (`id`), 
  KEY `lookup` (`dest_world`,`dest_x`,`dest_y`,`dest_z`), 
  KEY `type` (`type`) 
) ENGINE=MyISAM AUTO_INCREMENT=685 DEFAULT CHARSET=utf8;
```

```sql
CREATE TABLE `portal_history` ( 
  `player` char(32) NOT NULL, 
  `world` char(32) NOT NULL, 
  `x` double(7,3) NOT NULL, 
  `y` double(7,3) NOT NULL, 
  `z` double(7,3) NOT NULL, 
  `portal_id` int(11) DEFAULT NULL, 
  `timestamp` datetime DEFAULT NULL, 
  `dest_world` char(32) NOT NULL DEFAULT '', 
  `dest_x` double(7,3) NOT NULL DEFAULT '0.000', 
  `dest_y` double(7,3) NOT NULL DEFAULT '0.000', 
  `dest_z` double(7,3) NOT NULL DEFAULT '0.000', 
  PRIMARY KEY (`player`,`world`) 
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
```

## Usage
* `/portal create` creates a new portal in the database and starts an editing session for this portal. Place portal blocks to add them to the portal.
* `/portal edit [ID#]` starts a portal editing session where `ID#` is the portal ID from the database and console debug messages.
* `/portal exit` sets the portal desintation to exactly your current x, y, z, pitch, and yaw.
* `/portal done` ends a portal editing session.