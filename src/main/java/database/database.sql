CREATE SCHEMA traffic_limits;
USE traffic_limits;
CREATE TABLE limits_per_hour
(
    id             int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    limit_name     varchar(45)     NOT NULL,
    limit_value    long,
    effective_date datetime
);
Insert Into limits_per_hour
VALUES (default, 'min', 1024, now()),
       (default, 'max', 1073741824, now());