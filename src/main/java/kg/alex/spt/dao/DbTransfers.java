/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.domain.SchoolAccounting;
import kg.alex.spt.domain.Transfer;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.TransfersView;
import kg.alex.spt.utils.FormattedTreeTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

public class DbTransfers extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbTransfers.class);

    public DbTransfers() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, int school_id,
                                    int acc_invoice_type_id, TransfersView v) throws SQLException {
        Subject currentUser = SecurityUtils.getSubject();
        

        String sql = "SELECT t.id, t.amount, t.acc_category_id, t.acc_currency_id, t.currency_rate, t.note "
                + "FROM acc_transfers as t where t.invoice_id = ? order by t.id;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = v.prepareTransfersContainer();
        double total = 0;
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    v.createButton(myUi.getMessage(SptMessages.DeleteButton), id, Settings.dbTransfers));
            ComboBox cb = v.createCombobox(0, myUi.getMessage(SptMessages.Category), null,
                    true, acc_invoice_type_id == 1);
            try {
                DbAccCategory dbCon = new DbAccCategory();
                dbCon.connect();
                cb.setContainerDataSource(dbCon.exec_for_select(myUi, v.getAcc_category_type_id(), school_id,
                        result.getInt("t.acc_category_id"), acc_invoice_type_id != 1));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setItemCaptionPropertyId(myUi.getMessage(SptMessages.FullName));
            cb.setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Category)).setValue(cb);
            item.getItemProperty(Settings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            cb = v.createCombobox(result.getInt("t.acc_currency_id"), myUi.getMessage(SptMessages.Currency), Settings.dbAcc_currency, true, false);
            cb.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(SptMessages.Currency)).setValue(cb);
            item.getItemProperty(Settings.acc_currency_id).setValue(result.getInt("t.acc_currency_id"));
            TextField tf = v.createTextfieldWithProperty(
                    result.getDouble("t.amount"), myUi.getMessage(SptMessages.Amount),
                    new DoubleRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), null, null),
                    new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(), true);
            tf.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(SptMessages.Amount)).setValue(tf);
            tf = v.createTextfieldWithProperty(
                    result.getDouble("t.currency_rate"), myUi.getMessage(SptMessages.Rate),
                    new DoubleRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                    new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(),
                    currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate));
            tf.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(SptMessages.Rate)).setValue(tf);
            item.getItemProperty(myUi.getMessage(SptMessages.Note)).setValue(v.createTextfield(
                    result.getString("t.note"), id, new StringLengthValidator(myUi.getMessage(SptMessages.NotifWrongValue),
                            null, 250, acc_invoice_type_id == 1), acc_invoice_type_id != 1));
            item.getItemProperty(Settings.crud_status).setValue(myUi.getMessage(SptMessages.Update));
            if (result.getInt("t.acc_currency_id") == 1) {
                total += result.getDouble("t.amount") / result.getDouble("t.currency_rate");
            } else {
                total += result.getDouble("t.amount");
            }
        }
        v.setTransfersFooter(total);
        return container;
    }

    public void exec_report_by_date(MyVaadinUI myUI, int type_id, int school_id, Date from_date, Date till_date,
                                    FormattedTreeTable t, String selectedCategoryIds) throws SQLException {
        String sql = "SELECT ac.id, concat(ifnull(concat(ac.parent_code,'.',ac.code), ac.code), ' - ', ac.name) as category, " +
                "acu.name, t.acc_currency_id, t.currency_rate, t.amount, t.note, concat(e.name, ' ', e.surname) as fullname " +
                "FROM acc_transfers as t " +
                "left join acc_invoice as inv on inv.id = t.invoice_id " +
                "left join acc_category as ac on ac.id = t.acc_category_id " +
                "left join acc_currency as acu on acu.id = t.acc_currency_id " +
                "left join employee as e on e.id = inv.employee_id " +
                "where inv.school_id = ? and date(inv.creation_date) >= ? and date(inv.creation_date) <= ? " +
                "and inv.acc_invoice_type_id = ? and t.acc_category_id in ( " +
                selectedCategoryIds + ") and inv.is_confirmed = 1 " +
                "order by inv.creation_date asc, ac.id;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setDate(2, new java.sql.Date(from_date.getTime()));
        stat.setDate(3, new java.sql.Date(till_date.getTime()));
        stat.setInt(4, type_id);
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Currency), String.class, "USD");
        container.addContainerProperty(myUI.getMessage(SptMessages.Rate), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.FullName), String.class, null);
        t.setContainerDataSource(container);

        int id = 1;
        while (result.next()) {
            Item item;
            if (container.getItem(result.getString("ac.id")) == null) {
                item = container.addItem(result.getString("ac.id"));
                item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(result.getString("category"));
                item.getItemProperty(myUI.getMessage(SptMessages.Currency)).setValue("USD");
                if (result.getInt("t.acc_currency_id") == 1) {
                    if (result.getDouble("t.currency_rate") != 0) {
                        container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                                result.getDouble("t.amount") / result.getDouble("t.currency_rate"));
                    }
                } else {
                    container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                            result.getDouble("t.amount"));
                }
                container.setChildrenAllowed(result.getString("ac.id"), true);
                t.setCollapsed(result.getString("ac.id"), false);
            } else {
                if (result.getInt("t.acc_currency_id") == 1) {
                    if (result.getDouble("t.currency_rate") != 0) {
                        container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                                (Double) container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(SptMessages.Amount)).getValue()
                                        + (result.getDouble("t.amount") / result.getDouble("t.currency_rate")));
                    }
                } else {
                    container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                            (Double) container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(SptMessages.Amount)).getValue()
                                    + result.getDouble("t.amount"));
                }
            }
            item = container.addItem(Integer.toString(id));
            container.setChildrenAllowed(Integer.toString(id), false);
            container.setParent(Integer.toString(id), result.getString("ac.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Currency)).setValue(result.getString("acu.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(result.getDouble("t.currency_rate"));
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(result.getDouble("t.amount"));
            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(result.getString("t.note"));
            item.getItemProperty(myUI.getMessage(SptMessages.FullName)).setValue(result.getString("fullname"));
            id++;
        }
    }

    public SchoolAccounting exec_get_ttls(int scl_id, Date from, Date till, String cat_ids) throws SQLException {
        String sql = "SELECT "
                + "SUM(IF(cat.acc_type_id = 3 AND DATE(inv.creation_date) >= ? AND DATE(inv.creation_date) <= ?, "
                + "if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)) , 0.0)) AS assersTtl, "
                + "SUM(IF(cat.acc_type_id = 4 AND DATE(inv.creation_date) >= ? AND DATE(inv.creation_date) <= ?, "
                + "if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)), 0.0)) AS debtsTtl, "
                + "SUM(IF(DATE(inv.creation_date) < ?, IF(cat.acc_type_id = 3, if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)), "
                + "-(if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)))), 0.0)) AS prev_balance "
                + "FROM acc_transfers AS tr LEFT JOIN acc_category AS cat ON cat.id = tr.acc_category_id "
                + "LEFT JOIN acc_invoice AS inv ON inv.id = tr.invoice_id "
                + "WHERE inv.school_id = ? and inv.is_confirmed = 1 ";
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
            acc.setTotal_income(result.getDouble("assersTtl"));
            acc.setTotal_outcome(result.getDouble("debtsTtl"));
            acc.setPrevious_balance(result.getDouble("prev_balance"));
        }
        return acc;
    }

    public int exec_insert(Transfer acr) throws SQLException {
        String sql = "INSERT INTO acc_transfers (invoice_id,acc_category_id,"
                + "acc_currency_id,amount,currency_rate,note) VALUES(?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acr.getInvoice_id());
        stat.setInt(2, acr.getAcc_category_id());
        stat.setInt(3, acr.getCurrency_id());
        stat.setDouble(4, acr.getAmount());
        stat.setDouble(5, acr.getRate());
        if (acr.getNote() != null && !acr.getNote().equals("")) {
            stat.setString(6, acr.getNote());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Transfer acr) throws SQLException {
        String sql = "update acc_transfers set "
                + "acc_category_id = ?, acc_currency_id = ?, amount = ?, currency_rate = ?, note = ? "
                + "WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acr.getAcc_category_id());
        stat.setInt(2, acr.getCurrency_id());
        stat.setDouble(3, acr.getAmount());
        stat.setDouble(4, acr.getRate());
        if (acr.getNote() != null && !acr.getNote().equals("")) {
            stat.setString(5, acr.getNote());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        stat.setInt(6, acr.getId());
        return stat.executeUpdate();
    }
}
