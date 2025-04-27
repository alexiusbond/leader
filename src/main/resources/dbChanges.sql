INSERT INTO permissions (java_class_name, caption, permissions)
VALUES ('CashBoxExpensesAccordion', 'Касса - расходы',
        'показ в меню,добавление,изменение,удаление,изменение старых записей');

INSERT INTO permissions (java_class_name, caption, permissions)
VALUES ('CashBoxIncomesAccordion', 'Касса - приходы',
        'показ в меню,добавление,изменение,удаление,изменение старых записей');

INSERT INTO user_permission (role_name, permissions)
VALUES ('admin',
        'CashBoxExpensesAccordion:показ в меню,добавление,изменение,удаление,изменение старых записей');

INSERT INTO user_permission (role_name, permissions)
VALUES ('admin', 'CashBoxIncomesAccordion:показ в меню,добавление,изменение,удаление,изменение старых записей');

UPDATE user_permission t
SET t.permissions = 'CashBoxView:изменение курса доллара'
WHERE t.role_name LIKE 'admin' ESCAPE '#' AND t.permissions LIKE
                                              'TransactionsView:показ в меню,изменение курса доллара,изменение старых записей' ESCAPE
                                              '#';

UPDATE permissions t
SET t.permissions = 'показ в меню,добавление,изменение,удаление,распечатка,изменение старых записей'
WHERE t.java_class_name LIKE 'PaymentsTab' ESCAPE '#' AND t.caption LIKE 'Ученики - Вкладка оплаты' ESCAPE '#' AND
                                                                                                           t.permissions LIKE
                                                                                                           'показ в меню,добавление,изменение,удаление,распечатка' ESCAPE
                                                                                                           '#';

DELETE
FROM permissions
WHERE java_class_name LIKE 'TransactionsView' ESCAPE
      '#' AND caption LIKE 'Касса' ESCAPE '#' AND permissions LIKE 'показ в меню' ESCAPE '#';

