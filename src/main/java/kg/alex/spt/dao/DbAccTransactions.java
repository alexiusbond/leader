/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;

import java.sql.Connection;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.AccTransaction;
import kg.alex.spt.domain.SchoolAccounting;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.AccountingSchoolsReport;
import kg.alex.spt.ui.PayoutsView;
import kg.alex.spt.ui.TransactionsView;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.FormattedTreeTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.tepi.filtertable.FilterTreeTable;

public class DbAccTransactions extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbAccTransactions.class);

    public DbAccTransactions() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, int school_id, PayoutsView pav)
            throws SQLException {
        Subject currentUser = SecurityUtils.getSubject();
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT t.id, t.amount, t.acc_category_id, t.acc_currency_id, t.currency_rate, t.note "
                + "FROM acc_transactions as t where t.acc_invoice_id = ? order by t.id;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = pav.preparePayoutsContainer();
        double total = 0;
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(sysSettings.button).setValue(
                    pav.createButton(myUi.getMessage(SptMessages.DeleteButton), id, sysSettings.dbAcc_transactions));
            ComboBoxMax cb = pav.createCombobox(0, myUi.getMessage(SptMessages.Category), null, true, true);
            try {
                DbAccCategory dbCon = new DbAccCategory();
                dbCon.connect();
                cb.setContainerDataSource(dbCon.exec_for_select(myUi, 2, school_id, result.getInt("t.acc_category_id"), false));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setItemCaptionPropertyId(myUi.getMessage(SptMessages.FullName));
            cb.setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Category)).setValue(cb);
            item.getItemProperty(sysSettings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            cb = pav.createCombobox(result.getInt("t.acc_currency_id"), myUi.getMessage(SptMessages.Currency),
                    sysSettings.dbAcc_currency, true, false);
            cb.addValueChangeListener(pav);
            item.getItemProperty(myUi.getMessage(SptMessages.Currency)).setValue(cb);
            item.getItemProperty(sysSettings.acc_currency_id).setValue(result.getInt("t.acc_currency_id"));
            TextField tf = pav.createTextfieldWithProperty(
                    result.getDouble("t.amount"), myUi.getMessage(SptMessages.Amount),
                    new DoubleRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                    new ObjectProperty<Double>(0.0), sysSettings.getStringToDoubleConverter(), true);
            tf.addValueChangeListener(pav);
            item.getItemProperty(myUi.getMessage(SptMessages.Amount)).setValue(tf);
            tf = pav.createTextfieldWithProperty(
                    result.getDouble("t.currency_rate"), myUi.getMessage(SptMessages.Rate),
                    new DoubleRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                    new ObjectProperty<Double>(0.0), sysSettings.getStringToDoubleConverter(),
                    currentUser.isPermitted(sysSettings.cnTransactionsView + ":" + sysSettings.prmChangeCurrencyRate));
            tf.addValueChangeListener(pav);
            item.getItemProperty(myUi.getMessage(SptMessages.Rate)).setValue(tf);
            item.getItemProperty(myUi.getMessage(SptMessages.Note)).setValue(pav.createTextfield(
                    result.getString("t.note"), id, new StringLengthValidator(myUi.getMessage(SptMessages.NotifWrongValue), null, 250, true), true));
            item.getItemProperty(sysSettings.crud_status).setValue(myUi.getMessage(SptMessages.Update));
            if (result.getInt("t.acc_currency_id") == 1) {
                total += result.getDouble("t.amount") / result.getDouble("t.currency_rate");
            } else {
                total += result.getDouble("t.amount");
            }
        }
        pav.setPayoutsFooter(total);
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int incOrOut, int school_id,
                                    TransactionsView dw, Date from, Date till) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        Subject currentUser = SecurityUtils.getSubject();
        String sql = "SELECT t.id, t.date_time, t.acc_category_id, t.acc_currency_id, t.order_number, "
                + "t.currency_rate, t.amount, t.note, if(t.student_payments_id is not null or t.dp_invoice_id is not null "
                + "or t.acc_invoice_id is not null,true, false) as isDisabled, "
                + "if(DATE(t.modification_date) > DATE(DATE_SUB(NOW(), INTERVAL 5 DAY)), true, false) as isNotOld, "
                + "t.from_to_employee_id, concat(e.surname, ' ', e.name) as fullname "
                + "FROM acc_transactions as t "
                + "left join acc_category as ac on t.acc_category_id = ac.id "
                + "left join employee as e on t.employee_id = e.id "
                + "where ac.acc_type_id = ? and t.school_id = ? AND DATE(t.date_time) >= ? and DATE(t.date_time) <= ? order by t.date_time desc;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, incOrOut);
        stat.setInt(2, school_id);
        stat.setDate(3, new java.sql.Date(from.getTime()));
        stat.setDate(4, new java.sql.Date(till.getTime()));
        ResultSet result = stat.executeQuery();
        IndexedContainer container;

        if (incOrOut == 1) {
            container = dw.prepareIncomesContainer();
        } else {
            container = dw.prepareExpensesContainer();
        }
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            boolean isDisabled = result.getBoolean("isDisabled");
            if (!isDisabled) {
                isDisabled = !(currentUser.isPermitted(sysSettings.cnTransactionsView + ":" + sysSettings.prmChangeOlder5Days) || result.getBoolean("isNotOld"));
            }
            String tableName = myUI.getMessage(SptMessages.Outcomes);
            if (incOrOut == 1) {
                tableName = myUI.getMessage(SptMessages.Incomes);
                item.getItemProperty(sysSettings.button).setValue(dw.createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                        myUI.getMessage(SptMessages.Incomes), isDisabled, FontAwesome.MINUS_SQUARE));
                item.getItemProperty(myUI.getMessage(SptMessages.Category)).setValue(dw.createComboboxCategory(result.getInt("t.acc_category_id"),
                        myUI.getMessage(SptMessages.Category), id, 1, isDisabled, tableName));
            } else {
                HorizontalLayout hl = new HorizontalLayout();
                hl.setSpacing(true);
                hl.addComponent(dw.createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                        myUI.getMessage(SptMessages.Outcomes), isDisabled, FontAwesome.MINUS_SQUARE));
                hl.addComponent(dw.createButton(myUI.getMessage(SptMessages.Print), id,
                        myUI.getMessage(SptMessages.Print), false, FontAwesome.FILE_PDF_O));
                item.getItemProperty(sysSettings.button).setValue(hl);
                item.getItemProperty(myUI.getMessage(SptMessages.Category)).setValue(dw.createComboboxCategory(result.getInt("t.acc_category_id"),
                        myUI.getMessage(SptMessages.Category), id, 2, isDisabled, tableName));
                ComboBoxMax cb = dw.createCombobox(0, myUI.getMessage(SptMessages.ToEmployee), id, sysSettings.dbEmployee, false, false);
                try {
                    DbEmployee dbCon = new DbEmployee();
                    dbCon.connect();
                    cb.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool_id(), 0));
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                cb.removeValueChangeListener(dw);
                cb.setValue(result.getInt("t.from_to_employee_id"));
                cb.addValueChangeListener(dw);
                cb.setNullSelectionAllowed(true);
                cb.setEnabled(!isDisabled);
                item.getItemProperty(myUI.getMessage(SptMessages.ToEmployee)).setValue(cb);
                item.getItemProperty(sysSettings.order_number).setValue(result.getInt("t.order_number"));
                item.getItemProperty(sysSettings.from_employee_id).setValue(result.getString("fullname"));
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                    dw.createDateField(result.getTimestamp("t.date_time"), myUI.getMessage(SptMessages.Date), id, isDisabled, tableName));
            item.getItemProperty(myUI.getMessage(SptMessages.Currency)).setValue(dw.createCombobox(result.getInt("t.acc_currency_id"),
                    myUI.getMessage(SptMessages.Currency), id, sysSettings.dbAcc_currency, isDisabled, true));
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(dw.createTextfieldDouble(result.getDouble("t.amount"),
                    myUI.getMessage(SptMessages.Amount), id, false, isDisabled, tableName));
            item.getItemProperty(sysSettings.old_amount).setValue(result.getDouble("t.amount"));
            item.getItemProperty(sysSettings.old_rate).setValue(result.getDouble("t.currency_rate"));
            item.getItemProperty(sysSettings.old_currency).setValue(result.getInt("t.acc_currency_id"));
            item.getItemProperty(sysSettings.old_date).setValue(result.getDate("t.date_time"));
            item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(dw.createTextfieldNote(result.getString("t.note"),
                    myUI.getMessage(SptMessages.Note), id, isDisabled));
            if (!isDisabled && !currentUser.isPermitted(sysSettings.cnTransactionsView + ":" + sysSettings.prmChangeCurrencyRate)) {
                isDisabled = true;
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(dw.createTextfieldDouble(result.getDouble("currency_rate"),
                    myUI.getMessage(SptMessages.Rate), id, true, isDisabled, null));

        }
        return container;
    }

    public int exec_insert(AccTransaction t, Connection conn) throws SQLException {
        String sql = "INSERT INTO acc_transactions "
                + "(date_time, amount, acc_currency_id, currency_rate, note, "
                + "acc_category_id, employee_id, school_id, modification_date, dp_invoice_id, student_payments_id, "
                + "from_to_employee_id, acc_invoice_id) "
                + "VALUES(?,?,?,?,?,?,?,?,NOW(),?,?,?,?);";
        PreparedStatement stat = conn.prepareStatement(sql);
        stat.setTimestamp(1, new java.sql.Timestamp(t.getDate().getTime()));
        stat.setDouble(2, t.getAmount());
        if (t.getCurrency_id() != 0) {
            stat.setInt(3, t.getCurrency_id());
        } else {
            stat.setNull(3, Types.INTEGER);
        }
        stat.setDouble(4, t.getCurrency_rate());
        stat.setString(5, t.getNote());
        stat.setInt(6, t.getCategory_id());
        stat.setInt(7, t.getEmployee_id());
        stat.setInt(8, t.getSchool_id());
        if (t.getDp_invoice_id() != 0) {
            stat.setInt(9, t.getDp_invoice_id());
        } else {
            stat.setNull(9, Types.INTEGER);
        }
        if (t.getStudent_payments_id() != 0) {
            stat.setInt(10, t.getStudent_payments_id());
        } else {
            stat.setNull(10, Types.INTEGER);
        }
        if (t.getFrom_to_employee_id() != 0) {
            stat.setInt(11, t.getFrom_to_employee_id());
        } else {
            stat.setNull(11, Types.INTEGER);
        }
        if (t.getAcc_invoice_id() != 0) {
            stat.setInt(12, t.getAcc_invoice_id());
        } else {
            stat.setNull(12, Types.INTEGER);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId(conn);
        } else {
            return 0;
        }
    }

    public int exec_update(AccTransaction t) throws SQLException {
        String sql = "Update acc_transactions set date_time=?, "
                + "amount=?, acc_currency_id=?, currency_rate=?, note=?, acc_category_id=?, "
                + "employee_id=?, school_id=?, modification_date=NOW(), from_to_employee_id = ? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setTimestamp(1, new java.sql.Timestamp(t.getDate().getTime()));
        stat.setDouble(2, t.getAmount());
        if (t.getCurrency_id() != 0) {
            stat.setInt(3, t.getCurrency_id());
        } else {
            stat.setNull(3, Types.INTEGER);
        }
        stat.setDouble(4, t.getCurrency_rate());
        stat.setString(5, t.getNote());
        stat.setInt(6, t.getCategory_id());
        stat.setInt(7, t.getEmployee_id());
        stat.setInt(8, t.getSchool_id());
        if (t.getFrom_to_employee_id() != 0) {
            stat.setInt(9, t.getFrom_to_employee_id());
        } else {
            stat.setNull(9, Types.INTEGER);
        }
        stat.setString(10, t.getId());
        return stat.executeUpdate();
    }

    public int exec_update(AccTransaction t, String by_column_name, int by_column_value, Connection conn) throws SQLException {

        String sql = "update acc_transactions set date_time=?, "
                + "amount=?, acc_currency_id=?, currency_rate=?, note=?, acc_category_id=?, "
                + "employee_id=?, modification_date=NOW() WHERE " + by_column_name + "=?;";
        PreparedStatement stat = conn.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(t.getDate().getTime()));
        stat.setDouble(2, t.getAmount());
        if (t.getCurrency_id() != 0) {
            stat.setInt(3, t.getCurrency_id());
        } else {
            stat.setNull(3, Types.INTEGER);
        }
        stat.setDouble(4, t.getCurrency_rate());
        stat.setString(5, t.getNote());
        stat.setInt(6, t.getCategory_id());
        stat.setInt(7, t.getEmployee_id());
        stat.setInt(8, by_column_value);
        return stat.executeUpdate();
    }

    public int exec_delete(String by_column_name, String by_column_value, Connection conn) throws SQLException {
        String sql = "DELETE FROM acc_transactions WHERE " + by_column_name + " = ?";
        PreparedStatement stat = conn.prepareStatement(sql);
        stat.setString(1, by_column_value);
        return stat.executeUpdate();
    }

    public int exec_delete_by_st_id(int st_id) throws SQLException {
        String sql = "delete act from acc_transactions as act "
                + "left join student_payments as sp on sp.id = act.student_payments_id "
                + "where sp.student_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        return stat.executeUpdate();
    }

    public AccTransaction exec_allow_delete_by_st_id(int st_id, int school_id) throws SQLException {
        String sql = "select tr.date_time, tr.amount, sp.payment_category_id from acc_transactions as tr "
                + "left join student_payments as sp on sp.id = tr.student_payments_id "
                + "where sp.student_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            if (result.getInt("sp.payment_category_id") != 3) {
                AccTransaction accTr = exec_low_balance(dbCon, school_id, result.getDate("tr.date_time"), result.getDouble("tr.amount"), 0.0, 1);
                if (accTr != null) {
                    return accTr;
                }
            }
        }
        return null;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUI, int type_id, int school_id, int id) throws SQLException {
        String sql = "select ac.id, concat(ifnull(concat(ac.parent_code,'.',ac.code), ac.code), ' - ', ac.name) as name "
                + "from acc_category as ac where ac.acc_type_id=? and (ac.activity_status_id = 2 or ac.id = ?) "
                + "and ac.parent_id is not null and (ac.school_id is null or ac.school_id=?) "
                + "and ac.parent_id not in (select acc_category_id from dp_product_category) "
                + "order by ifnull(concat(ac.parent_code,'.',ac.code), ac.code) asc;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, type_id);
        stat.setInt(2, id);
        stat.setInt(3, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Name), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("ac.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(
                    result.getString("name"));
        }
        return container;
    }

    public void execSQL_by_months(MyVaadinUI myUI, int type_id, int school_id,
                                  FilterTreeTable categoriesTable, Calendar from, Calendar till, FormattedTreeTable t)
            throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        Set<Integer> selectedIds = sysSettings.getChild_ids((HierarchicalContainer) categoriesTable.getContainerDataSource(), (Set<?>) categoriesTable.getValue());
        String sql = "SELECT cat.id, cat.parent_id, CONCAT(ifnull(concat(cat.parent_code,'.',cat.code), cat.code), ' - ', cat.name) AS name, "
                + "sum(if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2))) as amount, DATE(tr.date_time) AS dt "
                + "FROM acc_category AS cat "
                + "LEFT JOIN acc_transactions AS tr ON tr.acc_category_id = cat.id "
                + "WHERE cat.id IN ("
                + sysSettings.convertCollectionToStr(selectedIds)
                + ") AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ? AND cat.acc_type_id = ? "
                + "AND cat.activity_status_id = 2 AND tr.school_id = ? "
                + "GROUP BY cat.id, YEAR(tr.date_time), MONTH(tr.date_time) ORDER BY ifnull(concat(cat.parent_code,'.',cat.code), cat.code);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(from.getTime().getTime()));
        stat.setDate(2, new java.sql.Date(till.getTime().getTime()));
        stat.setInt(3, type_id);
        stat.setInt(4, school_id);
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Name), String.class, null);
        Calendar current = Calendar.getInstance();
        current.setTime(from.getTime());
        while (current.before(till)) {
            container.addContainerProperty(sysSettings.ymdf.format(current.getTime()), Double.class, 0.0);
            current.add(Calendar.MONTH, 1);
        }
        container.addContainerProperty(myUI.getMessage(SptMessages.Total), Double.class, 0.0);
        t.setContainerDataSource(container);
        current.setTime(from.getTime());
        while (current.before(till)) {
            t.setColumnFooter(sysSettings.ymdf.format(current.getTime()), "0.00");
            current.add(Calendar.MONTH, 1);
        }
        t.setColumnFooter(myUI.getMessage(SptMessages.Total), "0.00");
        Iterator categories_iter = categoriesTable.getContainerDataSource().getItemIds().iterator();
        while (categories_iter.hasNext()) {
            Object catNext = categories_iter.next();
            if (selectedIds.contains(catNext)) {
                Item item = container.addItem((Integer) catNext);
                item.getItemProperty(myUI.getMessage(SptMessages.Name))
                        .setValue(categoriesTable.getContainerProperty(catNext, myUI.getMessage(SptMessages.Name)).getValue().toString());
                container.setChildrenAllowed(catNext, false);
                Object parent = ((HierarchicalContainer) categoriesTable.getContainerDataSource())
                        .getParent(catNext);
                if (parent != null) {
                    container.setParent(catNext, parent);
                }
                if (((HierarchicalContainer) categoriesTable.getContainerDataSource())
                        .getChildren(catNext) != null) {
                    container.setChildrenAllowed(catNext, true);
                    t.setCollapsed(catNext, false);
                }
            }
        }
        while (result.next()) {
            Item item = container.getItem(result.getInt("cat.id"));
            String month = sysSettings.ymdf.format(result.getDate("dt"));
            item.getItemProperty(month).setValue(result.getDouble("amount"));
            try {
                t.setColumnFooter(myUI.getMessage(SptMessages.Total), sysSettings.dFormat.format(Double.parseDouble(t.getColumnFooter(myUI.getMessage(SptMessages.Total))) + result.getDouble("amount")));
                t.setColumnFooter(month, sysSettings.dFormat.format(Double.parseDouble(t.getColumnFooter(month))
                        + result.getDouble("amount")));
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Total)).setValue((Double) item.getItemProperty(myUI.getMessage(SptMessages.Total)).getValue()
                    + result.getDouble("amount"));
            Integer parent_id = (Integer) container.getParent(result.getInt("cat.id"));
            while (parent_id != null) {
                item = container.getItem(parent_id);
                item.getItemProperty(month).setValue(
                        (Double) item.getItemProperty(month).getValue()
                                + result.getDouble("amount"));
                item.getItemProperty(myUI.getMessage(SptMessages.Total)).setValue((Double) item.getItemProperty(myUI.getMessage(SptMessages.Total)).getValue()
                        + result.getDouble("amount"));
                parent_id = (Integer) container.getParent(parent_id);
            }
        }
    }

    public void execSQL_by_months(MyVaadinUI myUI, int type_id, FilterTable schoolsTable,
                                  FilterTreeTable categoriesTable, Calendar from, Calendar till, FormattedTreeTable t)
            throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        Set<Integer> selectedCategoryIds = sysSettings.getChild_ids((HierarchicalContainer) categoriesTable.getContainerDataSource(), (Set<?>) categoriesTable.getValue());
        Set<Integer> selectedSchoolIds = new HashSet<>();
        selectedSchoolIds.addAll((Set<Integer>) schoolsTable.getValue());
        String sql = "SELECT cat.id, cat.parent_id, CONCAT(ifnull(concat(cat.parent_code,'.',cat.code), cat.code), ' - ', cat.name) AS name, "
                + "sum(if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2))) as amount, DATE(tr.date_time) AS dt, tr.school_id  "
                + "FROM acc_category AS cat "
                + "LEFT JOIN acc_transactions AS tr ON tr.acc_category_id = cat.id "
                + "WHERE cat.id IN ("
                + sysSettings.convertCollectionToStr(selectedCategoryIds)
                + ") AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ? AND cat.acc_type_id = ? "
                + "AND cat.activity_status_id = 2 AND tr.school_id IN ("
                + sysSettings.convertCollectionToStr(selectedSchoolIds)
                + ") GROUP BY cat.id, YEAR(tr.date_time), MONTH(tr.date_time), tr.school_id ORDER BY ifnull(concat(cat.parent_code,'.',cat.code), cat.code);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(from.getTime().getTime()));
        stat.setDate(2, new java.sql.Date(till.getTime().getTime()));
        stat.setInt(3, type_id);
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Name), String.class, null);
        Calendar current = Calendar.getInstance();
        current.setTime(from.getTime());
        Set<Integer> selectedSchools = (Set<Integer>) schoolsTable.getValue();
        Iterator schools_iter = null;
        while (current.before(till)) {
            schools_iter = (schoolsTable.getContainerDataSource().getItemIds()).iterator();
            while (schools_iter.hasNext()) {
                Object nextSchool = schools_iter.next();
                if (selectedSchools.contains((Integer) nextSchool)) {
                    container.addContainerProperty(schoolsTable.getContainerProperty(nextSchool, myUI.getMessage(SptMessages.Name)).getValue() + " - "
                            + sysSettings.ymdf.format(current.getTime()), Double.class, 0.0);
                }
            }
            current.add(Calendar.MONTH, 1);
        }
        schools_iter = ((Set<?>) schoolsTable.getValue()).iterator();
        while (schools_iter.hasNext()) {
            Object nextSchool = schools_iter.next();
            if (selectedSchools.contains((Integer) nextSchool)) {
                container.addContainerProperty(schoolsTable.getContainerProperty(nextSchool, myUI.getMessage(SptMessages.Name)).getValue() + " - "
                        + myUI.getMessage(SptMessages.Total), Double.class, 0.0);
            }
        }
        container.addContainerProperty(myUI.getMessage(SptMessages.Total), Double.class, 0.0);
        t.setContainerDataSource(container);

        current.setTime(from.getTime());
        while (current.before(till)) {
            schools_iter = ((Set<?>) schoolsTable.getValue()).iterator();
            while (schools_iter.hasNext()) {
                Object nextSchool = schools_iter.next();
                t.setColumnFooter(schoolsTable.getContainerProperty(nextSchool, myUI.getMessage(SptMessages.Name)).getValue() + " - "
                        + sysSettings.ymdf.format(current.getTime()), "0.00");
                t.setColumnFooter(schoolsTable.getContainerProperty(nextSchool, myUI.getMessage(SptMessages.Name)).getValue() + " - " + myUI.getMessage(SptMessages.Total), "0.00");
            }
            current.add(Calendar.MONTH, 1);
        }
        t.setColumnFooter(myUI.getMessage(SptMessages.Total), "0.00");
        Iterator itr = categoriesTable.getContainerDataSource().getItemIds().iterator();
        while (itr.hasNext()) {
            Object catNext = itr.next();
            if (selectedCategoryIds.contains(catNext)) {
                Item item = container.addItem((Integer) catNext);
                item.getItemProperty(myUI.getMessage(SptMessages.Name))
                        .setValue(categoriesTable.getContainerProperty(catNext, myUI.getMessage(SptMessages.Name)).getValue().toString());
                container.setChildrenAllowed(catNext, false);
                Object parent = ((HierarchicalContainer) categoriesTable.getContainerDataSource()).getParent(catNext);
                if (parent != null) {
                    container.setParent(catNext, parent);
                }
                if (((HierarchicalContainer) categoriesTable.getContainerDataSource()).getChildren(catNext) != null) {
                    container.setChildrenAllowed(catNext, true);
                    t.setCollapsed(catNext, false);
                }
            }
        }
        while (result.next()) {
            Item item = container.getItem(result.getInt("cat.id"));
            Object month = sysSettings.ymdf.format(result.getDate("dt"));
            Object school = schoolsTable.getContainerProperty(result.getInt("tr.school_id"), myUI.getMessage(SptMessages.Name)).getValue();
            item.getItemProperty(school + " - " + month).setValue(result.getDouble("amount"));
            try {
                t.setColumnFooter(myUI.getMessage(SptMessages.Total),
                        sysSettings.dFormat.format(Double.parseDouble(t.getColumnFooter(myUI.getMessage(SptMessages.Total)))
                                + result.getDouble("amount")));
                t.setColumnFooter(school + " - " + myUI.getMessage(SptMessages.Total),
                        sysSettings.dFormat.format(Double.parseDouble(t.getColumnFooter(school + " - " + myUI.getMessage(SptMessages.Total)))
                                + result.getDouble("amount")));
                t.setColumnFooter(school + " - " + month,
                        sysSettings.dFormat.format(
                                Double.parseDouble(t.getColumnFooter(school + " - " + month))
                                        + result.getDouble("amount")));
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            item.getItemProperty(school + " - " + myUI.getMessage(SptMessages.Total)).setValue((Double) item.getItemProperty(school + " - " + myUI.getMessage(SptMessages.Total)).getValue()
                    + result.getDouble("amount"));
            item.getItemProperty(myUI.getMessage(SptMessages.Total)).setValue((Double) item.getItemProperty(myUI.getMessage(SptMessages.Total)).getValue()
                    + result.getDouble("amount"));
            Integer parent_id = (Integer) container.getParent(result.getInt("cat.id"));
            while (parent_id != null) {
                item = container.getItem(parent_id);
                item.getItemProperty(school + " - " + month).setValue(
                        (Double) item.getItemProperty(school + " - " + month).getValue()
                                + result.getDouble("amount"));
                item.getItemProperty(school + " - " + myUI.getMessage(SptMessages.Total)).setValue((Double) item.getItemProperty(school + " - " + myUI.getMessage(SptMessages.Total)).getValue()
                        + result.getDouble("amount"));
                item.getItemProperty(myUI.getMessage(SptMessages.Total)).setValue((Double) item.getItemProperty(myUI.getMessage(SptMessages.Total)).getValue()
                        + result.getDouble("amount"));
                parent_id = (Integer) container.getParent(parent_id);
            }
        }
    }

    public SchoolAccounting exec_get_ttls(int scl_id, Date from, Date till, String cat_ids) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT "
                + "MAX(IF(cat.acc_type_id = 2, DATE(tr.date_time), null)) as max_exp, "
                + "MAX(IF(cat.acc_type_id = 1, DATE(tr.date_time), null)) as max_inc, "
                + "SUM(IF(cat.acc_type_id = 1 AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ?, "
                + "if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)) , 0.0)) AS incTtl, "
                + "SUM(IF(cat.acc_type_id = 2 AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ?, "
                + "if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)), 0.0)) AS expTtl, "
                + "SUM(IF(DATE(tr.date_time) < ?, IF(cat.acc_type_id = 1, if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)), "
                + "-(if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)))), 0.0)) AS prev_balance "
                + "FROM acc_transactions AS tr LEFT JOIN acc_category AS cat ON cat.id = tr.acc_category_id WHERE tr.school_id = ? ";
        if (cat_ids != null) {
            sql += "and tr.acc_category_id in (" + cat_ids + ")";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(from.getTime()));
        stat.setDate(2, new java.sql.Date(till.getTime()));
        stat.setDate(3, new java.sql.Date(from.getTime()));
        stat.setDate(4, new java.sql.Date(till.getTime()));
        stat.setDate(5, new java.sql.Date(from.getTime()));
        stat.setInt(6, scl_id);
        ResultSet result = stat.executeQuery();
        SchoolAccounting acc = new SchoolAccounting();
        while (result.next()) {
            acc.setTotal_income(result.getDouble("incTtl"));
            acc.setTotal_outcome(result.getDouble("expTtl"));
            acc.setPrevious_balance(result.getDouble("prev_balance"));
            if (result.getDate("max_inc") != null) {
                acc.setLast_income_date(sysSettings.df.format(result.getDate("max_inc")));
            }
            if (result.getDate("max_exp") != null) {
                acc.setLast_outcome_date(sysSettings.df.format(result.getDate("max_exp")));
            }
        }
        return acc;
    }

    public void exec_schools_accounting(MyVaadinUI myUI, String school_ids, Date from_date, Date till_date,
                                        AccountingSchoolsReport sar) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT sch.id, sch.name, "
                + "MAX(IF(cat.acc_type_id = 2, DATE(tr.date_time), null)) as max_exp, "
                + "MAX(IF(cat.acc_type_id = 1, DATE(tr.date_time), null)) as max_inc, "
                + "SUM(IF(cat.acc_type_id = 1 AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ?, "
                + "if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)) , 0.0)) AS incTtl, "
                + "SUM(IF(cat.acc_type_id = 2 AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ?, "
                + "if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)), 0.0)) AS expTtl, "
                + "SUM(IF(DATE(tr.date_time) < ?, IF(cat.acc_type_id = 1, if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)), "
                + "-(if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)))), 0.0)) AS prev_balance "
                + "FROM acc_transactions AS tr "
                + "LEFT JOIN acc_category AS cat ON cat.id = tr.acc_category_id "
                + "LEFT JOIN school AS sch ON sch.id = tr.school_id "
                + "WHERE sch.id IN (" + school_ids + ") GROUP BY tr.school_id;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(from_date.getTime()));
        stat.setDate(2, new java.sql.Date(till_date.getTime()));
        stat.setDate(3, new java.sql.Date(from_date.getTime()));
        stat.setDate(4, new java.sql.Date(till_date.getTime()));
        stat.setDate(5, new java.sql.Date(from_date.getTime()));
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        Calendar c = Calendar.getInstance();
        c.setTime(from_date);
        c.add(Calendar.DAY_OF_MONTH, -1);
        container.addContainerProperty(myUI.getMessage(SptMessages.School), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.IncomesTotal), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastIncomeDate), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.ExpensesTotal), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastExpenseDate), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Balance) + " (" + sysSettings.df.format(c.getTime()) + ")", Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Transactions), Double.class, 0.0);
        double ttlInc = 0;
        double ttlExp = 0;
        while (result.next()) {
            Item item = container.addItem(result.getInt("sch.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.School)).setValue(
                    result.getString("sch.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.IncomesTotal)).setValue(
                    result.getDouble("incTtl"));
            ttlInc += result.getDouble("incTtl");
            item.getItemProperty(myUI.getMessage(SptMessages.ExpensesTotal)).setValue(
                    result.getDouble("expTtl"));
            ttlExp += result.getDouble("expTtl");
            if (result.getDate("max_inc") != null) {
                item.getItemProperty(myUI.getMessage(SptMessages.LastIncomeDate)).setValue(
                        sysSettings.df.format(result.getDate("max_inc")));
            }
            if (result.getDate("max_exp") != null) {
                item.getItemProperty(myUI.getMessage(SptMessages.LastExpenseDate)).setValue(
                        sysSettings.df.format(result.getDate("max_exp")));
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Transactions)).setValue(
                    result.getDouble("incTtl") + result.getDouble("prev_balance") - result.getDouble("expTtl"));
            item.getItemProperty(myUI.getMessage(SptMessages.Balance) + " (" + sysSettings.df.format(c.getTime()) + ")").setValue(
                    result.getDouble("prev_balance"));
        }
        sar.dataTable.setContainerDataSource(container);

        sar.dataTable.setColumnFooter(myUI.getMessage(SptMessages.School),
                myUI.getMessage(SptMessages.Total));
        sar.dataTable.setColumnFooter(myUI.getMessage(SptMessages.IncomesTotal),
                sysSettings.round(ttlInc, 2) + "");
        sar.dataTable.setColumnFooter(myUI.getMessage(SptMessages.ExpensesTotal),
                sysSettings.round(ttlExp, 2) + "");
        sar.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total),
                sysSettings.round((ttlInc - ttlExp), 2) + "");
    }

    public double exec_salary_balance(int school_id, int acc_category_id, Date till) throws SQLException {

        String sql = "SELECT "
                + "(SELECT IFNULL(SUM(IF(acr.acc_currency_id != 2, acr.amount / acr.currency_rate, acr.amount)), 0.0) AS amount "
                + "FROM acc_transfers AS acr "
                + "LEFT JOIN acc_invoice AS inv ON acr.invoice_id = inv.id "
                + "WHERE acr.acc_category_id = ? AND date(inv.creation_date) < ? AND inv.acc_invoice_type_id = 1 and inv.school_id = ?) - "
                + "(SELECT IFNULL(SUM(IF(tr.acc_currency_id != 2, tr.amount / tr.currency_rate, tr.amount)), 0.0) AS amount "
                + "FROM acc_transactions AS tr WHERE tr.acc_category_id = ? AND date(tr.date_time) < ? and tr.school_id = ?) as balance;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acc_category_id);
        stat.setDate(2, new java.sql.Date(till.getTime()));
        stat.setInt(3, school_id);
        stat.setInt(4, acc_category_id);
        stat.setDate(5, new java.sql.Date(till.getTime()));
        stat.setInt(6, school_id);
        ResultSet result = stat.executeQuery();

        while (result.next()) {
            return result.getDouble("balance");
        }
        return 0.0;
    }

    public void exec_current_account_state(MyVaadinUI myUI, int acc_category_id, Date from, Date till, Table t,
                                           int currency_id, int school_id) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sign = "/";
        if (currency_id == 1) {
            sign = "*";
        }
        String sql = "SELECT t.creation_date, t.amount, t.rate, t.note, t.type FROM "
                + "(SELECT inv.creation_date AS creation_date, "
                + "IF(acr.acc_currency_id != ?, acr.amount " + sign + " acr.currency_rate, acr.amount) AS amount, "
                + "acr.currency_rate as rate, acr.note as note, ? as type FROM acc_transfers AS acr "
                + "LEFT JOIN acc_invoice AS inv ON acr.invoice_id = inv.id "
                + "WHERE acr.acc_category_id = ? AND (date(inv.creation_date) BETWEEN ? AND ?) "
                + "AND inv.school_id = ? AND inv.acc_invoice_type_id = 1 "
                + "UNION ALL "
                + "SELECT tr.date_time AS creation_date, "
                + "IF(tr.acc_currency_id != ?, tr.amount " + sign + " tr.currency_rate, tr.amount) AS amount, "
                + "tr.currency_rate as rate, tr.note as note, ? as type FROM acc_transactions AS tr "
                + "WHERE tr.acc_category_id = ? AND (date(tr.date_time) BETWEEN ? AND ?) AND tr.school_id = ? ) t "
                + "ORDER BY creation_date";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, currency_id);
        stat.setString(2, myUI.getMessage(SptMessages.Accrual));
        stat.setInt(3, acc_category_id);
        stat.setDate(4, new java.sql.Date(from.getTime()));
        stat.setDate(5, new java.sql.Date(till.getTime()));
        stat.setInt(6, school_id);
        stat.setInt(7, currency_id);
        stat.setString(8, myUI.getMessage(SptMessages.Payout));
        stat.setInt(9, acc_category_id);
        stat.setDate(10, new java.sql.Date(from.getTime()));
        stat.setDate(11, new java.sql.Date(till.getTime()));
        stat.setInt(12, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Type), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Rate), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Accrual), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Payout), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Balance), String.class, "0.00");
        int i = 0;
        double balance = exec_salary_balance(school_id, acc_category_id, from), totalPayouts = 0.0, totalAccruals = 0.0;
        while (result.next()) {
            Item item = container.addItem(++i);
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(sysSettings.df.format(result.getDate("t.creation_date")));
            item.getItemProperty(myUI.getMessage(SptMessages.Type)).setValue(result.getString("t.type"));
            item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(result.getString("t.note"));
            item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(result.getDouble("t.rate"));
            if (result.getString("t.type").equals(myUI.getMessage(SptMessages.Payout))) {
                item.getItemProperty(myUI.getMessage(SptMessages.Payout)).setValue(result.getDouble("t.amount"));
                balance -= result.getDouble("t.amount");
                totalPayouts += result.getDouble("t.amount");
            } else {
                item.getItemProperty(myUI.getMessage(SptMessages.Accrual)).setValue(result.getDouble("t.amount"));
                balance += result.getDouble("t.amount");
                totalAccruals += result.getDouble("t.amount");
            }
            if (balance < 0) {
                item.getItemProperty(myUI.getMessage(SptMessages.Balance)).setValue((sysSettings.dFormat.format(balance * -1))
                        + " (" + myUI.getMessage(SptMessages.Payout).charAt(0) + ")");
            } else {
                item.getItemProperty(myUI.getMessage(SptMessages.Balance)).setValue(sysSettings.dFormat.format(balance)
                        + " (" + myUI.getMessage(SptMessages.Accrual).charAt(0) + ")");
            }
        }
        t.setColumnFooter(myUI.getMessage(SptMessages.Accrual), sysSettings.dFormat.format(totalAccruals));
        t.setColumnFooter(myUI.getMessage(SptMessages.Payout), sysSettings.dFormat.format(totalPayouts));
        t.setContainerDataSource(container);
    }

    public void exec_account_remains(MyVaadinUI myUI, FilterTreeTable categoriesTable, int currency_id, int school_id,
                                     FormattedTreeTable t) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        Set<Integer> selectedCategoryIds = sysSettings.getChild_ids(
                (HierarchicalContainer) categoriesTable.getContainerDataSource(),
                (Set<?>) categoriesTable.getValue());
        String sql = " SELECT cat.id, CONCAT(IFNULL(CONCAT(cat.parent_code, '.', cat.code), cat.code), ' - ', cat.name) AS name, "
                + "IFNULL(acr.amount_som, 0.0) - IFNULL(tr.amount_som, 0.0) AS remain_som, "
                + "IFNULL(acr.amount_usd, 0.0) - IFNULL(tr.amount_usd, 0.0) AS remain_usd, "
                + "IFNULL(IF((aacr.acc_currency_id <> 2), (aacr.amount / aacr.currency_rate), aacr.amount), 0.0) AS salary_usd, "
                + "IFNULL(IF((aacr.acc_currency_id <> 1), (aacr.amount * aacr.currency_rate), aacr.amount), 0.0) AS salary_som "
                + "FROM acc_category AS cat "
                + "LEFT JOIN view_accurals AS acr ON acr.acc_category_id = cat.id AND acr.school_id = ? "
                + "LEFT JOIN view_total_transactions AS tr ON tr.acc_category_id = cat.id AND tr.school_id = ? "
                + "LEFT JOIN acc_transfers AS aacr ON aacr.invoice_id = acr.invoice_id AND aacr.acc_category_id = cat.id "
                + "WHERE cat.id IN (" + sysSettings.convertCollectionToStr(selectedCategoryIds) + ")";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, school_id);
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Name), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Remain), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Salary), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Ratio), Double.class, null);
        t.setContainerDataSource(container);
        double total_remains = 0.0, total_salaries = 0.0;
        Iterator itr = categoriesTable.getContainerDataSource().getItemIds().iterator();
        while (itr.hasNext()) {
            Object catNext = itr.next();
            if (selectedCategoryIds.contains(catNext)) {
                Item item = container.addItem((Integer) catNext);
                item.getItemProperty(myUI.getMessage(SptMessages.Name))
                        .setValue(categoriesTable.getContainerProperty(catNext, myUI.getMessage(SptMessages.Name)).getValue().toString());
                container.setChildrenAllowed(catNext, false);
                Object parent = ((HierarchicalContainer) categoriesTable.getContainerDataSource()).getParent(catNext);
                if (parent != null) {
                    container.setParent(catNext, parent);
                }
                if (((HierarchicalContainer) categoriesTable.getContainerDataSource()).getChildren(catNext) != null) {
                    container.setChildrenAllowed(catNext, true);
                    t.setCollapsed(catNext, false);
                }
            }
        }
        while (result.next()) {
            Item item = container.getItem(result.getInt("cat.id"));
            if (currency_id == 1) {
                item.getItemProperty(myUI.getMessage(SptMessages.Remain)).setValue(result.getDouble("remain_som"));
                item.getItemProperty(myUI.getMessage(SptMessages.Salary)).setValue(result.getDouble("salary_som"));
                total_remains += result.getDouble("remain_som");
                total_salaries += result.getDouble("salary_som");
                if (result.getDouble("salary_som") != 0.0) {
                    item.getItemProperty(myUI.getMessage(SptMessages.Ratio)).setValue(result.getDouble("remain_som") / result.getDouble("salary_som"));
                }
                Integer parent_id = (Integer) container.getParent(result.getInt("cat.id"));
                while (parent_id != null) {
                    item = container.getItem(parent_id);
                    item.getItemProperty(myUI.getMessage(SptMessages.Remain)).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(SptMessages.Remain)).getValue() + result.getDouble("remain_som"));
                    item.getItemProperty(myUI.getMessage(SptMessages.Salary)).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(SptMessages.Salary)).getValue() + result.getDouble("salary_som"));
                    if ((Double) item.getItemProperty(myUI.getMessage(SptMessages.Salary)).getValue() != 0.0) {
                        item.getItemProperty(myUI.getMessage(SptMessages.Ratio)).setValue(
                                (Double) item.getItemProperty(myUI.getMessage(SptMessages.Remain)).getValue()
                                        / (Double) item.getItemProperty(myUI.getMessage(SptMessages.Salary)).getValue());
                    }
                    parent_id = (Integer) container.getParent(parent_id);
                }
            } else {
                item.getItemProperty(myUI.getMessage(SptMessages.Remain)).setValue(result.getDouble("remain_usd"));
                item.getItemProperty(myUI.getMessage(SptMessages.Salary)).setValue(result.getDouble("salary_usd"));
                total_remains += result.getDouble("remain_usd");
                total_salaries += result.getDouble("salary_usd");
                if (result.getDouble("salary_usd") != 0.0) {
                    item.getItemProperty(myUI.getMessage(SptMessages.Ratio)).setValue(result.getDouble("remain_usd") / result.getDouble("salary_usd"));
                }
                Integer parent_id = (Integer) container.getParent(result.getInt("cat.id"));
                while (parent_id != null) {
                    item = container.getItem(parent_id);
                    item.getItemProperty(myUI.getMessage(SptMessages.Remain)).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(SptMessages.Remain)).getValue() + result.getDouble("remain_usd"));
                    item.getItemProperty(myUI.getMessage(SptMessages.Salary)).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(SptMessages.Salary)).getValue() + result.getDouble("salary_usd"));
                    if ((Double) item.getItemProperty(myUI.getMessage(SptMessages.Salary)).getValue() != 0.0) {
                        item.getItemProperty(myUI.getMessage(SptMessages.Ratio)).setValue(
                                (Double) item.getItemProperty(myUI.getMessage(SptMessages.Remain)).getValue()
                                        / (Double) item.getItemProperty(myUI.getMessage(SptMessages.Salary)).getValue());
                    }
                    parent_id = (Integer) container.getParent(parent_id);
                }
            }
        }
        t.setColumnFooter(myUI.getMessage(SptMessages.Remain), sysSettings.dFormat.format(total_remains));
        t.setColumnFooter(myUI.getMessage(SptMessages.Salary), sysSettings.dFormat.format(total_salaries));
        t.setColumnFooter(myUI.getMessage(SptMessages.Ratio), sysSettings.dFormat.format(total_remains / total_salaries));
    }

    public IndexedContainer exec_report_by_date(MyVaadinUI myUI, int type_id, int school_id, Date from_date, Date till_date,
                                                FilterTreeTable categoriesTable) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        Set<Integer> selectedIds = new HashSet<Integer>();
        selectedIds.addAll((Set<Integer>) categoriesTable.getValue());
        Iterator iter = ((Set<Integer>) categoriesTable.getValue()).iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            if (categoriesTable.getChildren(next) != null) {
                selectedIds.addAll(new HashSet<Integer>(
                        (Collection<Integer>) categoriesTable.getChildren(next)));
            }
        }
        String sql = "SELECT t.id, date(t.date_time), concat(ifnull(concat(ac.parent_code,'.',ac.code), ac.code), ' - ', ac.name) as category, "
                + "acu.name, t.currency_rate,t.amount,t.note, concat(e.name, ' ', e.surname) as fullname "
                + "FROM acc_transactions as t  "
                + "left join acc_category as ac on ac.id = t.acc_category_id  "
                + "left join acc_currency as acu on acu.id = t.acc_currency_id  "
                + "left join employee as e on e.id = t.employee_id "
                + "where t.school_id = ? and date(t.date_time) >= ? and date(t.date_time) <=? "
                + "and ac.acc_type_id = ? and t.acc_category_id in (" + sysSettings.convertCollectionToStr(selectedIds) + ") "
                + "order by t.date_time asc;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setDate(2, new java.sql.Date(from_date.getTime()));
        stat.setDate(3, new java.sql.Date(till_date.getTime()));
        stat.setInt(4, type_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Name), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Currency), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Rate), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.FullName), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(sysSettings.df.format(
                    result.getDate("date(t.date_time)")));
            item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(
                    result.getString("category"));
            item.getItemProperty(myUI.getMessage(SptMessages.Currency)).setValue(
                    result.getString("acu.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(
                    result.getDouble("t.currency_rate"));
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                    result.getDouble("t.amount"));
            item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                    result.getString("t.note"));
            item.getItemProperty(myUI.getMessage(SptMessages.FullName)).setValue(
                    result.getString("fullname"));
        }
        return container;
    }

    public void execSQL_Plan_Payments(MyVaadinUI myUI, int year_id, String edu_statuses_ids,
                                      int school_id, Date acad_year_start_date, Date acad_year_end_date, Table t) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT months.name AS month_name, months.id, i_temp.amn AS inst, p_temp.amn AS payments, "
                + "in_temp.amn AS income, out_temp.amn AS outcome FROM months LEFT JOIN (SELECT SUM(inst.amount) AS amn, "
                + "MONTH(inst.date_of_payment) AS mnth FROM student_installement_plan AS inst "
                + "LEFT JOIN student AS st ON st.id = inst.student_id "
                + "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 GROUP BY so.student_id) "
                + "AS o_temp ON st.id = o_temp.stud_id LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS edu ON edu.id = CASE WHEN stud_o.to_education_status_id IS NULL "
                + "THEN st.education_status_id ELSE stud_o.to_education_status_id END "
                + "WHERE inst.year_id = ? AND st.school_id = ? AND edu.id IN (" + edu_statuses_ids + ") "
                + "GROUP BY MONTH(inst.date_of_payment)) AS i_temp ON i_temp.mnth = months.id "
                + "LEFT JOIN (SELECT SUM(IF(pay.payment_category_id = 3, - pay.amount, pay.amount)) AS amn, "
                + "MONTH(pay.modification_date) AS mnth FROM student_payments AS pay "
                + "LEFT JOIN student AS st ON st.id = pay.student_id LEFT JOIN "
                + "(SELECT MAX(so.id) AS oid, so.student_id AS stud_id FROM student_orders AS so "
                + "WHERE so.year_id = ? AND so.is_valid = 1 GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS edu ON edu.id = CASE WHEN stud_o.to_education_status_id IS NULL "
                + "THEN st.education_status_id ELSE stud_o.to_education_status_id END WHERE pay.year_id = ? "
                + "AND st.school_id = ? AND pay.payment_category_id IN (1, 2, 3) AND edu.id IN (" + edu_statuses_ids + ") "
                + "GROUP BY MONTH(pay.modification_date)) AS p_temp ON p_temp.mnth = months.id "
                + "LEFT JOIN (SELECT SUM(if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2))) AS amn, "
                + "MONTH(tr.date_time) AS mnth FROM acc_transactions AS tr "
                + "WHERE tr.school_id = ? AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ? "
                + "AND tr.acc_category_id IN (SELECT acc_category_id FROM payment_category WHERE id IN (1, 2)) "
                + "GROUP BY MONTH(date_time)) AS in_temp ON in_temp.mnth = months.id LEFT JOIN "
                + "(SELECT SUM(if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2))) AS amn, "
                + "MONTH(tr.date_time) AS mnth FROM acc_transactions AS tr LEFT JOIN acc_category AS c ON c.id = tr.acc_category_id "
                + "WHERE tr.school_id = ? AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ? "
                + "AND c.acc_type_id = 2 GROUP BY MONTH(date_time)) AS out_temp ON out_temp.mnth = months.id ORDER BY months.order_num;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, school_id);
        stat.setInt(4, year_id);
        stat.setInt(5, year_id);
        stat.setInt(6, school_id);
        stat.setInt(7, school_id);
        stat.setDate(8, new java.sql.Date(acad_year_start_date.getTime()));
        stat.setDate(9, new java.sql.Date(acad_year_end_date.getTime()));
        stat.setInt(10, school_id);
        stat.setDate(11, new java.sql.Date(acad_year_start_date.getTime()));
        stat.setDate(12, new java.sql.Date(acad_year_end_date.getTime()));
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Month), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.InstallmentPlan), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Payments), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Debt), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Incomes), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Outcomes), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Difference), Double.class, 0.0);
        t.setContainerDataSource(container);
        double totalInst = 0.0, totalPayments = 0.0, totalDebt = 0.0, totalIncomes = 0.0, totalOutcomes = 0.0, totalTransactions = 0.0;

        while (result.next()) {
            Item item = container.addItem(result.getInt("months.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Month)).setValue(
                    result.getString("month_name"));
            item.getItemProperty(myUI.getMessage(SptMessages.InstallmentPlan)).setValue(
                    result.getDouble("inst"));
            totalInst += result.getDouble("inst");
            item.getItemProperty(myUI.getMessage(SptMessages.Payments)).setValue(
                    result.getDouble("payments"));
            totalPayments += result.getDouble("payments");
            item.getItemProperty(myUI.getMessage(SptMessages.Debt)).setValue(
                    result.getDouble("inst") - result.getDouble("payments"));
            totalDebt += (result.getDouble("inst") - result.getDouble("payments"));
            item.getItemProperty(myUI.getMessage(SptMessages.Incomes)).setValue(
                    result.getDouble("income"));
            totalIncomes += result.getDouble("income");
            item.getItemProperty(myUI.getMessage(SptMessages.Outcomes)).setValue(
                    result.getDouble("outcome"));
            totalOutcomes += result.getDouble("outcome");
            item.getItemProperty(myUI.getMessage(SptMessages.Difference)).setValue(
                    result.getDouble("income") - result.getDouble("outcome"));
            totalTransactions += (result.getDouble("income") - result.getDouble("outcome"));
        }
        t.setColumnFooter(myUI.getMessage(SptMessages.InstallmentPlan), sysSettings.dFormat.format(totalInst));
        t.setColumnFooter(myUI.getMessage(SptMessages.Payments), sysSettings.dFormat.format(totalPayments));
        t.setColumnFooter(myUI.getMessage(SptMessages.Debt), sysSettings.dFormat.format(totalDebt));
        t.setColumnFooter(myUI.getMessage(SptMessages.Incomes), sysSettings.dFormat.format(totalIncomes));
        t.setColumnFooter(myUI.getMessage(SptMessages.Outcomes), sysSettings.dFormat.format(totalOutcomes));
        t.setColumnFooter(myUI.getMessage(SptMessages.Difference), sysSettings.dFormat.format(totalTransactions));
    }

    public double getBalances(int school_id, Date date, boolean isDateIncluded) throws SQLException {

        String sql = "SELECT SUM(IF(cat.acc_type_id = 1, tr.amount, - tr.amount)) AS amount FROM acc_transactions AS tr "
                + "LEFT JOIN acc_category AS cat ON cat.id = tr.acc_category_id where tr.school_id = ? and DATE(tr.date_time) ";
        if (isDateIncluded) {
            sql += "<=";
        } else {
            sql += "<";
        }
        sql += "?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setDate(2, new java.sql.Date(date.getTime()));
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getDouble("amount");
        }
        return 0.0;
    }

    public AccTransaction exec_low_balance(Connection conn, int school_id, Date date, double old_amount,
                                           double new_amount, int inOut) throws SQLException {

        Date d = exec_nearestDate(conn, school_id, date);
        if (d == null) {
            return null;
        }
        String sql = "SELECT balances_table.date_time, round(balances_table.amount,2) as amount, round(balances_table.balance,2) as balance FROM "
                + "(SELECT gr_transactions.date_time AS date_time, gr_transactions.amount AS amount, "
                + "(@runtot:=gr_transactions.amount + @runtot) AS balance FROM "
                + "(SELECT DATE(tr.date_time) AS date_time, SUM(IF(cat.acc_type_id = 1, "
                + "if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)), "
                + "-(if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2))))) AS amount FROM "
                + "acc_transactions AS tr LEFT JOIN acc_category AS cat ON cat.id = tr.acc_category_id  where tr.school_id = ? "
                + "GROUP BY DATE(tr.date_time) ORDER BY DATE(tr.date_time)) AS gr_transactions, "
                + "(SELECT @runtot:=0) c) AS balances_table WHERE balances_table.date_time >= ? "
                + "and round(balances_table.balance,2) + ? + ? < 0;";
        PreparedStatement stat = conn.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setDate(2, new java.sql.Date(d.getTime()));
        if (inOut == 1) {
            stat.setDouble(3, -old_amount);
            stat.setDouble(4, new_amount);
        } else {
            stat.setDouble(3, old_amount);
            stat.setDouble(4, -new_amount);
        }
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            AccTransaction tr = new AccTransaction();
            tr.setDate(result.getDate("balances_table.date_time"));
            if (inOut == 1) {
                tr.setLimit(old_amount - result.getDouble("balance"));
                tr.setOverlimit((result.getDouble("balance") - old_amount + new_amount) * -1);
            } else {
                tr.setLimit(result.getDouble("balance") + old_amount);
                tr.setOverlimit((result.getDouble("balance") + old_amount - new_amount) * -1);
            }
            return tr;
        }
        return null;
    }

    public Date exec_nearestDate(Connection conn, int school_id, Date date) throws SQLException {

        String sql = "SELECT balances_table.date_time, balances_table.amount, balances_table.balance FROM "
                + "(SELECT gr_transactions.date_time AS date_time, gr_transactions.amount AS amount, "
                + "(@runtot:=gr_transactions.amount + @runtot) AS balance FROM "
                + "(SELECT DATE(tr.date_time) AS date_time, SUM(IF(cat.acc_type_id = 1, tr.amount, - tr.amount)) AS amount FROM "
                + "acc_transactions AS tr LEFT JOIN acc_category AS cat ON cat.id = tr.acc_category_id  where tr.school_id = ? "
                + "GROUP BY DATE(tr.date_time) ORDER BY DATE(tr.date_time)) AS gr_transactions, (SELECT @runtot:=0) c) AS balances_table "
                + "WHERE balances_table.date_time <= ?;";
        PreparedStatement stat = conn.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setDate(2, new java.sql.Date(date.getTime()));
        ResultSet result = stat.executeQuery();
        while (result.last()) {
            return result.getDate("balances_table.date_time");
        }
        return null;
    }

    public int getMaxOrderNum(int school_id) throws SQLException {
        int maxValue = 0;
        String sql = "select (ifnull(max(tr.order_number),0)+1) as max_plus1 "
                + "from acc_transactions as tr where tr.school_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            maxValue = (result.getInt("max_plus1"));
        } else {
            maxValue = 1;
        }
        return maxValue;
    }

    public int exec_update_order_number(String id, int school_id) throws SQLException {
        int order_number = getMaxOrderNum(school_id);
        String sql = "update acc_transactions set order_number=? WHERE id=? and order_number IS NULL;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, order_number);
        stat.setString(2, id);
        if (stat.executeUpdate() != 0) {
            return order_number;
        } else {
            return 0;
        }
    }
}
