UPDATE `spt`.`permissions` SET `permissions` = 'меню,отчет по месяцам,отчет по датам,общий отчет по бухгалтерии,выписка по текущему счету,отчет по выплатам,балансовый отчет' WHERE (`java_class_name` = 'AccountingReportsView') and (`permissions` = 'меню,отчет по месяцам,отчет по датам,общий отчет по бухгалтерии,выписка по текущему счету,отчет по выплатам');


update user_permission set permissions = 'AccountingReportsView:отчет по датам,отчет по месяцам,school_accounting_report,общий отчет по бухгалтерии,меню,выписка по текущему счету,отчет по выплатам,балансовый отчет'
where permissions like 'AccountingReportsView:%';
UPDATE `spt`.`acc_invoice_type` SET `name` = 'debt' WHERE (`id` = '4');
UPDATE `spt`.`acc_invoice_type` SET `name` = 'assert' WHERE (`id` = '3');
