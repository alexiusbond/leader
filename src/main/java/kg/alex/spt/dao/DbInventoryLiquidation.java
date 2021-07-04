/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.ui.TextField;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.InventoryLiquidation;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.InventoryLiquidationView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbInventoryLiquidation extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbInventoryLiquidation.class);

    public DbInventoryLiquidation() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, InventoryLiquidationView v)
            throws SQLException {

        String sql = "SELECT t.id, t.quantity, t.price, t.inventory_category_id, t.title_id, "
                + "t.brand_id, t.remain, t.code, "
                + "t.purchase_date, t.life_time  "
                + "FROM dm_inventory_liquidation as t where t.invoice_id = ? order by t.id;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = v.prepareInventoriesContainer();
        int totalQuantity = 0;
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(SystemSettings.button).setValue(
                    v.createButton(myUi.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbInventoryOrganization, true));
            item.getItemProperty(myUi.getMessage(SptMessages.Category)).setValue(
                    v.createCombobox(result.getInt("t.inventory_category_id"),
                            myUi.getMessage(SptMessages.Category),
                            SystemSettings.dbInventoryCategoryTable, true, true));
            TextField tf = v.createTextfieldWithProperty(
                    result.getInt("t.quantity"), myUi.getMessage(SptMessages.Quantity),
                    new IntegerRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue),
                            result.getInt("t.remain"), null),
                    new ObjectProperty<Integer>(0), SystemSettings.getStringToIntegerConverter(), true);
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(SptMessages.Quantity));
            item.getItemProperty(myUi.getMessage(SptMessages.Quantity)).setValue(tf);
            item.getItemProperty(SystemSettings.quantity_id).setValue(result.getInt("t.quantity"));

            item.getItemProperty(myUi.getMessage(SptMessages.Remain)).setValue(result.getInt("t.remain"));
            item.getItemProperty(myUi.getMessage(SptMessages.Code)).setValue(result.getString("t.code"));
            item.getItemProperty(SystemSettings.crud_status).setValue(myUi.getMessage(SptMessages.Update));
            totalQuantity += result.getInt("t.quantity");
        }
        v.setInventoriesFooter(totalQuantity);
        return container;
    }

    public int exec_delete(int invoice_id) throws SQLException {
        String sql = "DELETE FROM dm_inventory_liquidation WHERE invoice_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        return stat.executeUpdate();
    }

    public int exec_insert(InventoryLiquidation inventoryLiquidation) throws SQLException {
        String sql = "INSERT INTO dm_inventory_liquidation (invoice_id,quantity," +
                "inventory_id,creation_date) VALUES(?,?,?,NOW());";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryLiquidation.getInvoice_id());
        stat.setInt(2, inventoryLiquidation.getQuantity());
        stat.setInt(3, inventoryLiquidation.getInventory_id());
        updateRemain(inventoryLiquidation.getInventory_id(), -inventoryLiquidation.getQuantity());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(InventoryLiquidation inventoryLiquidation) throws SQLException {
        String sql = "update dm_inventory_liquidation set "
                + "quantity = ?, inventory_id = ? WHERE id= ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryLiquidation.getQuantity());
        stat.setInt(2, inventoryLiquidation.getInventory_id());
        stat.setInt(3, inventoryLiquidation.getId());
        updateRemain(inventoryLiquidation.getInventory_id(), -inventoryLiquidation.getQuantity());
        return stat.executeUpdate();
    }

    public int exec_delete(InventoryLiquidation inventoryLiquidation) throws SQLException {
        updateRemain(inventoryLiquidation.getInventory_id(), inventoryLiquidation.getQuantity());
        String sql = "DELETE FROM dm_inventory_liquidation WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryLiquidation.getId());
        return stat.executeUpdate();
    }

    private void updateRemain(int inventory_id, int quantity) {
        try {
            DbInventoryOrganization dbCon = new DbInventoryOrganization();
            dbCon.connect();
            dbCon.close();
            dbCon.exec_update_remain(inventory_id, quantity);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
