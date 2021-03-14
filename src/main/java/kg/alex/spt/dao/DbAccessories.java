/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Accessories;
import kg.alex.spt.i18n.SptMessages;

public class DbAccessories extends BaseDb {

    public ArrayList<Accessories> q = null;

    public DbAccessories() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi) throws SQLException {
        

        String sql = "SELECT ac.id, ac.name, ac.activity_status_id, a.name, "
                + "ac.accessories_category_id, c.name FROM accessories as ac "
                + "left join activity_status as a on a.id = ac.activity_status_id "
                + "left join accessories_category as c on c.id = ac.accessories_category_id ;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Category), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Status), String.class, null);
        container.addContainerProperty(SystemSettings.status_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.category_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("ac.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Status)).setValue(
                    result.getString("a.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Category)).setValue(
                    result.getString("c.name"));
            item.getItemProperty(SystemSettings.status_id).setValue(
                    result.getInt("ac.activity_status_id"));
            item.getItemProperty(SystemSettings.category_id).setValue(
                    result.getInt("ac.accessories_category_id"));
            item.getItemProperty(SystemSettings.id).setValue(result.getInt("ac.id"));
        }
        return container;
    }

    public int exec_insert(Accessories acc) throws SQLException {
        String sql = "INSERT IGNORE INTO accessories (name,activity_status_id,accessories_category_id) "
                + "VALUES(?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, acc.getName());
        stat.setInt(2, acc.getStatus_id());
        stat.setInt(3, acc.getCategory_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Accessories acc) throws SQLException {
        String sql = "UPDATE accessories SET name=?, activity_status_id=?, "
                + "accessories_category_id=? WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, acc.getName());
        stat.setInt(2, acc.getStatus_id());
        stat.setInt(3, acc.getCategory_id());
        stat.setInt(4, acc.getId());
        int status = stat.executeUpdate();
        return status;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, int cat_id) throws SQLException {
        String sql = "select t.id, t.name from accessories as t "
                + "where activity_status_id = 2 and accessories_category_id = ? "
                + "order by t.id desc;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cat_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getString("t.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("t.name"));
        }
        return container;
    }
}
