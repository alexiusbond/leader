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
import kg.alex.spt.Settings;
import kg.alex.spt.domain.ClassName;
import kg.alex.spt.i18n.SptMessages;

public class DbClassName extends BaseDb {

    public DbClassName() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id) throws SQLException {

        String sql = "SELECT cn.id, cn.name, cn.school_id, sc.name_ru, cnum.id, cnum.name, "
                + "cn.activity_status_id, ac.name FROM class_name as cn "
                + "left join class_number as cnum on cnum.id = cn.class_number_id "
                + "left join school as sc on sc.id = cn.school_id "
                + "left join activity_status as ac on ac.id=cn.activity_status_id "
                + "where sc.id = ? "
                + "order by cn.class_number_id and cn.name;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Number), String.class, 0);
        container.addContainerProperty(Settings.number_id, Integer.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Status), String.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        container.addContainerProperty(Settings.school_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.School), String.class, null);
        container.addContainerProperty(Settings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("cn.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Number)).setValue(
                    result.getString("cnum.name"));
            item.getItemProperty(Settings.number_id).setValue(
                    result.getInt("cnum.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("cn.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(Settings.status_id).setValue(
                    result.getInt("cn.activity_status_id"));
            item.getItemProperty(Settings.school_id).setValue(
                    result.getInt("cn.school_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.School)).setValue(
                    result.getString("sc.name_ru"));
            item.getItemProperty(Settings.id).setValue(result.getInt("cn.id"));
        }
        return container;
    }

    public int exec_insert(ClassName def) throws SQLException {
        String sql = "INSERT IGNORE INTO class_name (name,school_id,"
                + "class_number_id,activity_status_id) "
                + "VALUES(?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, def.getName());
        stat.setInt(2, def.getSchool_id());
        stat.setInt(3, def.getClass_number_id());
        stat.setInt(4, def.getStatus_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(ClassName cl) throws SQLException {
        String sql = "UPDATE class_name SET name=?, school_id=?,class_number_id=?,"
                + "activity_status_id=? "
                + "WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, cl.getName());
        stat.setInt(2, cl.getSchool_id());
        stat.setInt(3, cl.getClass_number_id());
        stat.setInt(4, cl.getStatus_id());
        stat.setInt(5, cl.getId());
        int status = stat.executeUpdate();
        return status;
    }

    public IndexedContainer execClass_sel(MyVaadinUI myUi, int scl_id) throws SQLException {
        String sql = "select cn.id, concat(cnu.name,' - ',cn.name) as cl_name "
                + "from class_name as cn "
                + "left join class_number as cnu on cn.class_number_id = cnu.id "
                + "where cn.school_id = ? "
                + "order by cnu.name, cn.name;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("cn.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("cl_name"));
        }
        return container;
    }

    public IndexedContainer execClass_for_import(MyVaadinUI myUi, int scl_id) throws SQLException {
        

        String sql = "select cn.id, concat(cnu.name,' - ',cn.name) as cl_name "
                + "from class_name as cn "
                + "left join class_number as cnu on cn.class_number_id = cnu.id "
                + "where cn.school_id = ? order by cnu.name, cn.name;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(Settings.id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getString("cl_name"));
            item.getItemProperty(Settings.id).setValue(
                    result.getInt("cn.id"));
        }
        return container;
    }
}
