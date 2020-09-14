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
import java.util.ArrayList;
import java.util.Date;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.StockMovement;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.ui.StockIncomeView;
import kg.alex.spt.ui.StockOutcomeView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DbStockMovements extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbStockMovements.class);

    public DbStockMovements() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, StockIncomeView v)
            throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT t.id, t.amount, t.remain, t.price, t.acc_category_id, t.dp_measurement_id, t.note, t.currency_rate, rmn.remain "
                + "FROM dp_stock_movements as t "
                + "left join dp_invoice as inv on inv.id = t.invoice_id "
                + "left join view_remains as rmn on rmn.acc_category_id = t.acc_category_id and rmn.stock_id = inv.to_stock_id and rmn.dp_measurement_id = t.dp_measurement_id "
                + "where t.invoice_id = ? order by t.id;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = v.prepareMovementsContainer();
        v.prepareOriginalContainer();
        double total = 0;
        while (result.next()) {
            boolean is_modifyable = true;
            if (result.getDouble("t.amount") != result.getDouble("t.remain")) {
                is_modifyable = false;
            }
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(sysSettings.button).setValue(
                    v.createButton(myUi.getMessage(SptMessages.DeleteButton), id, sysSettings.dbStockMovement, is_modifyable));
            ComboBoxMax cb = v.createCombobox(0, myUi.getMessage(SptMessages.Product), null, true, is_modifyable);
            try {
                DbAccCategory dbCon = new DbAccCategory();
                dbCon.connect();
                cb.setContainerDataSource(dbCon.exec_for_select(myUi,
                        v.getProductCategorySelect().getContainerProperty(v.getProductCategorySelect().getValue(),
                                sysSettings.acc_category_id).getValue().toString()));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setItemCaptionPropertyId(myUi.getMessage(SptMessages.FullName));
            cb.setValue(result.getInt("t.acc_category_id"));
            cb.setId(id);
            cb.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(SptMessages.Product)).setValue(cb);
            item.getItemProperty(sysSettings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Note)).setValue(v.createTextfield(
                    result.getString("t.note"), myUi.getMessage(SptMessages.Note),
                    new StringLengthValidator(myUi.getMessage(SptMessages.NotifWrongValue), null, 250, false), true));
            cb = v.createCombobox(result.getInt("t.dp_measurement_id"), myUi.getMessage(SptMessages.Measurement),
                    sysSettings.dbMeasurement, true, is_modifyable);
            cb.setId(id);
            cb.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(SptMessages.Measurement)).setValue(cb);
            item.getItemProperty(sysSettings.measurement_id).setValue(result.getInt("t.dp_measurement_id"));
            double minVal = 0.1;
            if (result.getDouble("t.remain") < result.getDouble("t.amount")) {
                minVal = result.getDouble("t.amount") - result.getDouble("t.remain");
            }
            TextField tf = v.createTextfieldWithProperty(
                    result.getDouble("t.amount"), myUi.getMessage(SptMessages.Quantity),
                    new DoubleRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), minVal, null),
                    new ObjectProperty<Double>(0.0), sysSettings.getStringToDoubleConverter(), (result.getDouble("t.remain") > 0));
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(SptMessages.Quantity));
            item.getItemProperty(myUi.getMessage(SptMessages.Quantity)).setValue(tf);
            item.getItemProperty(sysSettings.quantity_id).setValue(result.getDouble("t.amount"));
            tf = v.createTextfieldWithProperty(
                    result.getDouble("t.price"), myUi.getMessage(SptMessages.Price),
                    new DoubleRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                    new ObjectProperty<Double>(0.0), sysSettings.getStringToDoubleConverter(), is_modifyable);
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(SptMessages.Price));
            item.getItemProperty(myUi.getMessage(SptMessages.Price)).setValue(tf);
            item.getItemProperty(myUi.getMessage(SptMessages.Rate)).setValue(result.getDouble("t.currency_rate"));
            item.getItemProperty(myUi.getMessage(SptMessages.Amount)).setValue(
                    result.getDouble("t.amount") * result.getDouble("t.price"));
            item.getItemProperty(myUi.getMessage(SptMessages.Remain)).setValue(result.getDouble("rmn.remain"));
            item.getItemProperty(sysSettings.crud_status).setValue(myUi.getMessage(SptMessages.Update));
            item.getItemProperty(sysSettings.is_modifiable).setValue(is_modifyable);
            total += result.getDouble("t.amount") * result.getDouble("t.price");

            item = v.getOriginalCont().addItem(id);
            item.getItemProperty(sysSettings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(sysSettings.measurement_id).setValue(result.getInt("t.dp_measurement_id"));
            item.getItemProperty(sysSettings.quantity_id).setValue(result.getDouble("t.amount"));
        }
        v.setMovementsFooter(total);
        return container;
    }

    public ArrayList<StockMovement> execSQL(Integer invoice_id, Integer acc_category_id, Integer measurement_id) throws SQLException {
        ArrayList<StockMovement> list = new ArrayList<>();
        String sql = "SELECT t.id, t.amount, t.dp_stock_movements_id FROM dp_stock_movements as t "
                + "left join dp_invoice as inv on inv.id = t.invoice_id where t.invoice_id = ? ";
        if (acc_category_id != null) {
            sql += "and t.acc_category_id = " + acc_category_id + " ";
        }
        if (measurement_id != null) {
            sql += "and t.dp_measurement_id = " + measurement_id + " ";
        }
        sql += "order by t.id;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            StockMovement sm = new StockMovement();
            sm.setId(result.getInt("t.id"));
            sm.setStock_movement_id(result.getInt("t.dp_stock_movements_id"));
            sm.setQuantity(result.getDouble("t.amount"));
            list.add(sm);
        }
        return list;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, StockOutcomeView v) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT t.id, sum(t.amount) as amount, sum(t.amount * t.price) as total, "
                + "t.acc_category_id, t.dp_measurement_id, t.note, "
                + "sum(t.currency_rate*t.amount) as currency_rate, rmn.remain, "
                + "t.dp_stock_movements_id FROM dp_stock_movements as t "
                + "left join dp_invoice as inv on inv.id = t.invoice_id "
                + "left join view_remains as rmn "
                + "on rmn.acc_category_id = t.acc_category_id and rmn.stock_id = inv.from_stock_id and rmn.dp_measurement_id = t.dp_measurement_id "
                + "where t.invoice_id = ? group by t.order_number order by t.id";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = v.prepareMovementsContainer();
        v.prepareOriginalContainer();
        double total = 0;
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(sysSettings.button).setValue(
                    v.createButton(myUi.getMessage(SptMessages.DeleteButton), id, sysSettings.dbStockMovement));
            ComboBoxMax cb = v.createCombobox(0, myUi.getMessage(SptMessages.Product), null, true);
            try {
                DbAccCategory dbCon = new DbAccCategory();
                dbCon.connect();
                cb.setContainerDataSource(dbCon.exec_for_select(myUi,
                        v.getProductCategorySelect().getContainerProperty(v.getProductCategorySelect().getValue(),
                                sysSettings.acc_category_id).getValue().toString()));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setItemCaptionPropertyId(myUi.getMessage(SptMessages.FullName));
            cb.setValue(result.getInt("t.acc_category_id"));
            cb.setId(id);
            cb.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(SptMessages.Product)).setValue(cb);
            item.getItemProperty(sysSettings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Note)).setValue(v.createTextfield(
                    result.getString("t.note"), myUi.getMessage(SptMessages.Note),
                    new StringLengthValidator(myUi.getMessage(SptMessages.NotifWrongValue), null, 250, false), true));
            cb = v.createCombobox(result.getInt("t.dp_measurement_id"), myUi.getMessage(SptMessages.Measurement), sysSettings.dbMeasurement, true);
            cb.setId(id);
            cb.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(SptMessages.Measurement)).setValue(cb);
            item.getItemProperty(sysSettings.measurement_id).setValue(result.getInt("t.dp_measurement_id"));
            TextField tf = v.createTextfieldWithProperty(
                    result.getDouble("amount"), myUi.getMessage(SptMessages.Quantity),
                    new DoubleRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                    new ObjectProperty<Double>(0.0), sysSettings.getStringToDoubleConverter());
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(SptMessages.Quantity));
            item.getItemProperty(myUi.getMessage(SptMessages.Quantity)).setValue(tf);
            item.getItemProperty(sysSettings.quantity_id).setValue(result.getDouble("amount"));
            item.getItemProperty(myUi.getMessage(SptMessages.Price)).setValue(result.getDouble("total") / result.getDouble("amount"));
            item.getItemProperty(myUi.getMessage(SptMessages.Rate)).setValue(result.getDouble("currency_rate") / result.getDouble("amount"));
            item.getItemProperty(myUi.getMessage(SptMessages.Amount)).setValue(result.getDouble("total"));
            item.getItemProperty(myUi.getMessage(SptMessages.Remain)).setValue(result.getDouble("rmn.remain"));
            item.getItemProperty(sysSettings.crud_status).setValue(myUi.getMessage(SptMessages.Update));
            total += result.getDouble("total");

            item = v.getOriginalCont().addItem(id);
            item.getItemProperty(sysSettings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(sysSettings.measurement_id).setValue(result.getInt("t.dp_measurement_id"));
            item.getItemProperty(sysSettings.quantity_id).setValue(result.getDouble("amount"));
        }
        v.setMovementsFooter(total);
        return container;
    }

    public int exec_delete(int invoice_id) throws SQLException {
        String sql = "DELETE FROM dp_stock_movements WHERE invoice_id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        return stat.executeUpdate();
    }

    public int exec_insert(StockMovement smv) throws SQLException {
        String sql = "INSERT INTO dp_stock_movements (invoice_id,acc_category_id,"
                + "dp_measurement_id,amount,price,note,currency_rate,remain,dp_stock_movements_id,order_number) VALUES(?,?,?,?,?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, smv.getInvoice_id());
        stat.setInt(2, smv.getAcc_category_id());
        stat.setInt(3, smv.getMeasurement_id());
        stat.setDouble(4, smv.getQuantity());
        stat.setDouble(5, smv.getPrice());
        if (smv.getNote() != null) {
            stat.setString(6, smv.getNote());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        stat.setDouble(7, smv.getRate());
        stat.setDouble(8, smv.getQuantity());
        if (smv.getStock_movement_id() != 0) {
            stat.setInt(9, smv.getStock_movement_id());
            exec_update_remain(smv.getStock_movement_id(), -smv.getQuantity());
        } else {
            stat.setNull(9, Types.VARCHAR);
        }
        stat.setInt(10, smv.getOrder_number());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(StockMovement smv) throws SQLException {
        String sql = "update dp_stock_movements set "
                + "acc_category_id = ?,dp_measurement_id = ?, remain = remain - amount + ?, amount = ?,price = ?, note = ?, dp_stock_movements_id = ? "
                + "WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, smv.getAcc_category_id());
        stat.setInt(2, smv.getMeasurement_id());
        stat.setDouble(3, smv.getQuantity());
        stat.setDouble(4, smv.getQuantity());
        stat.setDouble(5, smv.getPrice());
        stat.setString(6, smv.getNote());
        if (smv.getStock_movement_id() != 0) {
            stat.setInt(7, smv.getStock_movement_id());
        } else {
            stat.setNull(7, Types.VARCHAR);
        }
        stat.setInt(8, smv.getId());
        return stat.executeUpdate();
    }

    public int exec_update_remain(int id, double amount) throws SQLException {
        String sql = "update dp_stock_movements set remain = remain + ? WHERE id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDouble(1, amount);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }

    public double execSQL_remain(int stock_movement_id) throws SQLException {
        String sql = "SELECT remain as rmn FROM dp_stock_movements where id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stock_movement_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getDouble("rmn");
        }
        return 0.0;
    }

    public double execSQL_remain(int acc_category_id, int measurement_id, int stock_id) throws SQLException {
        String sql = "SELECT remain FROM view_remains where acc_category_id = ? and dp_measurement_id = ? and stock_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acc_category_id);
        stat.setDouble(2, measurement_id);
        stat.setInt(3, stock_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getDouble("remain");
        }
        return 0.0;
    }

    public double execSQL_remain(int acc_category_id, int measurement_id, int stock_id, Date date) throws SQLException {
        String sql = "SELECT SUM(mv.remain) AS remain FROM dp_stock_movements mv "
                + "LEFT JOIN dp_invoice inv ON inv.id = mv.invoice_id "
                + "WHERE inv.service_type_id = 1 AND DATE(inv.creation_date) <= ? AND mv.acc_category_id = ? "
                + "AND mv.dp_measurement_id = ? AND inv.to_stock_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(date.getTime()));
        stat.setInt(2, acc_category_id);
        stat.setDouble(3, measurement_id);
        stat.setInt(4, stock_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getDouble("remain");
        }
        return 0.0;
    }

    public boolean execSQL_allowNewInsert(int acc_category_id, int measurement_id, int stock_id, Date date) throws SQLException {
        String sql = "SELECT if(mv.remain<mv.amount, 0, 1) AS isOk FROM dp_stock_movements mv "
                + "LEFT JOIN dp_invoice inv ON inv.id = mv.invoice_id "
                + "WHERE inv.service_type_id = 1 AND DATE(inv.creation_date) > ? AND mv.acc_category_id = ? "
                + "AND mv.dp_measurement_id = ? AND inv.to_stock_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(date.getTime()));
        stat.setInt(2, acc_category_id);
        stat.setDouble(3, measurement_id);
        stat.setInt(4, stock_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            if (result.getInt("isOk") == 0) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<StockMovement> execSQL_remains(int acc_category_id, int measurement_id, int stock_id) throws SQLException {
        String sql = "SELECT sm.id, remain, price, currency_rate, sm.acc_category_id, dp_measurement_id "
                + "FROM dp_stock_movements AS sm LEFT JOIN dp_invoice AS inv ON inv.id = sm.invoice_id "
                + "WHERE sm.remain > 0 AND inv.service_type_id = 1 AND sm.acc_category_id = ? AND sm.dp_measurement_id = ? "
                + "AND inv.to_stock_id = ? ORDER BY inv.creation_date;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acc_category_id);
        stat.setInt(2, measurement_id);
        stat.setInt(3, stock_id);
        ResultSet result = stat.executeQuery();
        ArrayList<StockMovement> list = new ArrayList<>();
        while (result.next()) {
            StockMovement stMov = new StockMovement();
            stMov.setPrice(result.getDouble("price"));
            stMov.setRate(result.getDouble("currency_rate"));
            stMov.setRemain(result.getDouble("remain"));
            stMov.setId(result.getInt("sm.id"));
            list.add(stMov);
        }
        return list;
    }

    public int exec_delete(StockMovement sm) throws SQLException {
        exec_update_remain(sm.getStock_movement_id(), sm.getQuantity());
        String sql = "DELETE FROM dp_stock_movements WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sm.getId());
        return stat.executeUpdate();
    }
}
