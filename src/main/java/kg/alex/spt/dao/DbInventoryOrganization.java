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
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Definition;
import kg.alex.spt.domain.InventoryOrganization;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.InventoryOrganizationView;
import kg.alex.spt.utils.ComboBoxMax;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Iterator;

public class DbInventoryOrganization extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbInventoryOrganization.class);

    public DbInventoryOrganization() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, InventoryOrganizationView v)
            throws SQLException {

        String sql = "SELECT t.id, t.quantity, t.price, t.inventory_category_id, t.title_id, "
                + "t.brand_id, t.remain, t.code, "
                + "t.purchase_date, t.life_time  "
                + "FROM dm_inventory_organization as t where t.invoice_id = ? order by t.id;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = v.prepareInventoriesContainer();
        double totalAmount = 0.0;
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
            final ComboBoxMax cb = v.createCombobox(result.getInt("t.brand_id"), myUi.getMessage(SptMessages.Brand),
                    SystemSettings.dbInventoryBrandTable, true, true);
            cb.setNewItemsAllowed(true);
            cb.setNewItemHandler(new AbstractSelect.NewItemHandler() {
                @Override
                public void addNewItem(String newItemCaption) {
                    try {
                        DbDefinition dbd = new DbDefinition();
                        dbd.connect();
                        int id = dbd.exec_insert(new Definition(0, newItemCaption), SystemSettings.dbInventoryBrandTable, false);
                        dbd.close();
                        if (id != 0) {
                            Iterator iter = container.getItemIds().iterator();
                            while (iter.hasNext()) {
                                Object next = iter.next();
                                Item item = ((IndexedContainer) ((ComboBox) container.getContainerProperty(next,
                                        myUi.getMessage(SptMessages.Brand)).getValue()).getContainerDataSource()).addItem(id);
                                item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(newItemCaption);
                                cb.setValue(id);
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                }
            });
            item.getItemProperty(myUi.getMessage(SptMessages.Brand)).setValue(cb);
            final ComboBoxMax cb2 = v.createCombobox(result.getInt("t.title_id"), myUi.getMessage(SptMessages.Title),
                    SystemSettings.dbInventoryTitleTable, true, true);
            cb2.setNewItemsAllowed(true);
            cb2.setNewItemHandler(new AbstractSelect.NewItemHandler() {
                @Override
                public void addNewItem(String newItemCaption) {
                    try {
                        DbDefinition dbd = new DbDefinition();
                        dbd.connect();
                        int id = dbd.exec_insert(new Definition(0, newItemCaption), SystemSettings.dbInventoryTitleTable, false);
                        dbd.close();
                        if (id != 0) {
                            Iterator iter = container.getItemIds().iterator();
                            while (iter.hasNext()) {
                                Object next = iter.next();
                                Item item = ((IndexedContainer) ((ComboBox) container.getContainerProperty(next,
                                        myUi.getMessage(SptMessages.Title)).getValue()).getContainerDataSource()).addItem(id);
                                item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(newItemCaption);
                                cb2.setValue(id);
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                }
            });
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(cb2);
            item.getItemProperty(myUi.getMessage(SptMessages.PurchaseYear)).setValue(
                    v.createDateFiled(result.getDate("t.purchase_date"),
                            myUi.getMessage(SptMessages.PurchaseYear), null, Resolution.YEAR, SystemSettings.yearPattern));
            item.getItemProperty(myUi.getMessage(SptMessages.LifeTime)).setValue(
                    v.createTextfieldWithProperty(
                            result.getInt("t.life_time"), myUi.getMessage(SptMessages.LifeTime),
                            new IntegerRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), 1, null),
                            new ObjectProperty<Integer>(0), SystemSettings.getStringToIntegerConverter(), true));
            int minVal = 1;
            if (result.getInt("t.remain") < result.getInt("t.quantity")) {
                minVal = result.getInt("t.quantity") - result.getInt("t.remain");
            }
            TextField tf = v.createTextfieldWithProperty(
                    result.getInt("t.quantity"), myUi.getMessage(SptMessages.Quantity),
                    new IntegerRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), minVal, null),
                    new ObjectProperty<Integer>(0), SystemSettings.getStringToIntegerConverter(), true);
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(SptMessages.Quantity));
            item.getItemProperty(myUi.getMessage(SptMessages.Quantity)).setValue(tf);
            item.getItemProperty(SystemSettings.quantity_id).setValue(result.getInt("t.quantity"));
            tf = v.createTextfieldWithProperty(
                    result.getDouble("t.price"), myUi.getMessage(SptMessages.Price),
                    new DoubleRangeValidator(myUi.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                    new ObjectProperty<Double>(0.0), SystemSettings.getStringToDoubleConverter(), true);
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(SptMessages.Price));
            item.getItemProperty(myUi.getMessage(SptMessages.Price)).setValue(tf);
            item.getItemProperty(myUi.getMessage(SptMessages.Amount)).setValue(
                    result.getInt("t.quantity") * result.getDouble("t.price"));
            item.getItemProperty(myUi.getMessage(SptMessages.Remain)).setValue(result.getInt("t.remain"));
            item.getItemProperty(myUi.getMessage(SptMessages.Code)).setValue(result.getString("t.code"));
            item.getItemProperty(SystemSettings.crud_status).setValue(myUi.getMessage(SptMessages.Update));
            totalAmount += result.getInt("t.quantity") * result.getDouble("t.price");
            totalQuantity += result.getInt("t.quantity");
        }
        v.setInventoriesFooter(totalAmount, totalQuantity);
        return container;
    }

    public int exec_delete(int invoice_id) throws SQLException {
        String sql = "DELETE FROM dm_inventory_organization WHERE invoice_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        return stat.executeUpdate();
    }

    public int exec_insert(InventoryOrganization inventoryOrganization) throws SQLException {
        String sql = "INSERT INTO dm_inventory_organization (invoice_id,inventory_category_id, "
                + "title_id,brand_id,quantity,price,remain,code,purchase_date,life_time," +
                "creation_date) VALUES(?,?,?,?,?,?,?,?,?,?,NOW());";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryOrganization.getInvoice_id());
        stat.setInt(2, inventoryOrganization.getInventory_category_id());
        stat.setInt(3, inventoryOrganization.getTitle_id());
        stat.setInt(4, inventoryOrganization.getBrand_id());
        stat.setInt(5, inventoryOrganization.getQuantity());
        stat.setDouble(6, inventoryOrganization.getPrice());
        stat.setInt(7, inventoryOrganization.getQuantity());
        stat.setString(8, inventoryOrganization.getCode());
        stat.setDate(9, new Date(inventoryOrganization.getPurchase_date().getTime()));
        stat.setInt(10, inventoryOrganization.getLifeTime());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(InventoryOrganization inventoryOrganization) throws SQLException {
        String sql = "update dm_inventory_organization set "
                + "inventory_category_id = ?, title_id = ?, brand_id = ?, remain = remain - quantity + ?, "
                + "quantity = ?, price = ?, code = ?, purchase_date = ?, life_time = ? "
                + "WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryOrganization.getInventory_category_id());
        stat.setInt(2, inventoryOrganization.getTitle_id());
        stat.setInt(3, inventoryOrganization.getBrand_id());
        stat.setInt(4, inventoryOrganization.getQuantity());
        stat.setInt(5, inventoryOrganization.getQuantity());
        stat.setDouble(6, inventoryOrganization.getPrice());
        stat.setString(7, inventoryOrganization.getCode());
        stat.setDate(8, new Date(inventoryOrganization.getPurchase_date().getTime()));
        stat.setInt(9, inventoryOrganization.getLifeTime());
        stat.setInt(10, inventoryOrganization.getId());
        return stat.executeUpdate();
    }

    public int exec_update_remain(int id, int quantity) throws SQLException {
        String sql = "update dm_inventory_organization set remain = remain + ? WHERE id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, quantity);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }

    public int exec_delete(InventoryOrganization inventoryOrganization) throws SQLException {
        String sql = "DELETE FROM dm_inventory_organization WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryOrganization.getId());
        return stat.executeUpdate();
    }
}
