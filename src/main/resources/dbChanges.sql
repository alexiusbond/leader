USE `spt`;
CREATE
OR REPLACE ALGORITHM = UNDEFINED
    DEFINER = `root`@`localhost`
    SQL SECURITY DEFINER
VIEW `view_total_transactions` AS
SELECT
    date(tr.date_time) as creation_date,
    `tr`.`school_id` AS `school_id`,
    `tr`.`acc_category_id` AS `acc_category_id`,
    IFNULL(SUM(IF((`tr`.`acc_currency_id` <> 2),
    (`tr`.`amount` / `tr`.`currency_rate`),
    `tr`.`amount`)),
    0.0) AS `amount_usd`,
    IFNULL(SUM(IF((`tr`.`acc_currency_id` <> 1),
    (`tr`.`amount` * `tr`.`currency_rate`),
    `tr`.`amount`)),
    0.0) AS `amount_som`
FROM
    `acc_transactions` `tr`
GROUP BY `tr`.`school_id` , `tr`.`acc_category_id`, date(tr.date_time);

USE `spt`;
CREATE
OR REPLACE ALGORITHM = UNDEFINED
    DEFINER = `root`@`localhost`
    SQL SECURITY DEFINER
VIEW `view_accurals` AS
SELECT
    date(`inv`.`creation_date`) AS `creation_date`,
    `inv`.`school_id` AS `school_id`,
    `acr`.`acc_category_id` AS `acc_category_id`,
    IFNULL(SUM(IF((`acr`.`acc_currency_id` <> 2),
    (`acr`.`amount` / `acr`.`currency_rate`),
    `acr`.`amount`)),
    0.0) AS `amount_usd`,
    IFNULL(SUM(IF((`acr`.`acc_currency_id` <> 1),
    (`acr`.`amount` * `acr`.`currency_rate`),
    `acr`.`amount`)),
    0.0) AS `amount_som`,
    MAX(`inv`.`id`) AS `invoice_id`
FROM
    (`acc_transfers` `acr`
    LEFT JOIN `acc_invoice` `inv` ON (((`inv`.`id` = `acr`.`invoice_id`)
    AND (`inv`.`acc_invoice_type_id` = 1))))
GROUP BY `inv`.`school_id` , `acr`.`acc_category_id`, date(`inv`.`creation_date`);
