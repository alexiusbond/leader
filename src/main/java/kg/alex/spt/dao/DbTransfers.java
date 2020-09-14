/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.TextField;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Transfer;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.TransfersView;
import kg.alex.spt.utils.ComboBoxMax;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class DbTransfers extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbTransfers.class);

    public DbTransfers() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, int school_id, TransfersView v)
            throws SQLException {
        Subject currentUser = SecurityUtils.getSubject();
        SystemSettings sysSettings = new SystemSettings();
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
            item.getItemProperty(sysSettings.button).setValue(
                    v.createButton(myUi.getMessage(SptMessages.DeleteButton), id, sysSettings.dbTransfers));
            ComboBoxMax cb = v.createCombobox(0, myUi.getMessage(SptMessages.Category), null, true, true);
            try {
                DbAccCategory dbCon = new DbAccCategory();
                dbCon.connect();
                cb.setContainerDataSource(dbCon.exec_for_select(myUi, v.getAcc_category_id(), school_id, result.getInt("t.acc_category_id")));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setItemCaptionPropertyId(myUi.getMessage(SptMessages.FullName));
            cb.setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Category)).setValue(cb);
            item.getItemProperty(sysSettings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            cb = v.createCombobox(result.getInt("t.acc_currency_id"), myUi.getMessage(SptMessages.Currency), sysSettings.dbAcc_currency, true, false);
            cb.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(SptMessages.Currency)).setValue(cb);
            item.getItemProperty(sysSettings.acc_currency_id).setValue(result.getInt("t.acc_currency_id"));
            TextField tf = v.createTextfieldWithProperty(
                    result.getDouble("t.amount"), myUi.getMessage(SptMessages.Amount),
                    new DoubleRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), null, null),
                    new ObjectProperty<Double>(0.0), sysSettings.getStringToDoubleConverter(), true);
            tf.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(SptMessages.Amount)).setValue(tf);
            tf = v.createTextfieldWithProperty(
                    result.getDouble("t.currency_rate"), myUi.getMessage(SptMessages.Rate),
                    new DoubleRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                    new ObjectProperty<Double>(0.0), sysSettings.getStringToDoubleConverter(),
                    currentUser.isPermitted(sysSettings.cnTransactionsView + ":" + sysSettings.prmChangeCurrencyRate));
            tf.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(SptMessages.Rate)).setValue(tf);
            item.getItemProperty(myUi.getMessage(SptMessages.Note)).setValue(v.createTextfield(
                    result.getString("t.note"), id, new StringLengthValidator(myUi.getMessage(SptMessages.NotifWrongValue), null, 250, true), false));
            item.getItemProperty(sysSettings.crud_status).setValue(myUi.getMessage(SptMessages.Update));
            if (result.getInt("t.acc_currency_id") == 1) {
                total += result.getDouble("t.amount") / result.getDouble("t.currency_rate");
            } else {
                total += result.getDouble("t.amount");
            }
        }
        v.setTransfersFooter(total);
        return container;
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
