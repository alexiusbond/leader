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
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Stock;
import kg.alex.spt.i18n.SptMessages;

public class DbStock extends BaseDb {

    public DbStock() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id) throws SQLException {


        String sql = "SELECT t.id, t.name, t.school_id, t.activity_status_id, ac.name FROM dp_stock as t "
                + "left join activity_status as ac on ac.id=t.activity_status_id "
                + "where t.school_id = ? order by t.name;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Status), String.class, null);
        container.addContainerProperty(SystemSettings.school_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.status_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("t.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(SystemSettings.school_id).setValue(
                    result.getInt("t.school_id"));
            item.getItemProperty(SystemSettings.status_id).setValue(
                    result.getInt("t.activity_status_id"));
            item.getItemProperty(SystemSettings.id).setValue(result.getInt("t.id"));
        }
        return container;
    }

    public int exec_insert(Stock def) throws SQLException {
        String sql = "INSERT IGNORE INTO dp_stock (name,school_id,activity_status_id) "
                + "VALUES(?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, def.getName());
        stat.setInt(2, def.getSchool_id());
        stat.setInt(3, def.getStatus_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Stock cl) throws SQLException {
        String sql = "UPDATE dp_stock SET name=?, school_id=?,activity_status_id=? "
                + "WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, cl.getName());
        stat.setInt(2, cl.getSchool_id());
        stat.setInt(3, cl.getStatus_id());
        stat.setInt(4, cl.getId());
        int status = stat.executeUpdate();
        return status;
    }

    public IndexedContainer execStock_sel(MyVaadinUI myUi, int scl_id) throws SQLException {
        String sql = "select t.id, t.name from dp_stock as t "
                + "where t.school_id = ? order by t.name;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("t.name"));
        }
        return container;
    }

}
