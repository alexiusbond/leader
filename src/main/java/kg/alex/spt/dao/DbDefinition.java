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
import java.util.Date;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Definition;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMultiselectMax;

/**
 * @author alex
 */
public class DbDefinition extends BaseDb {

    public DbDefinition() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, String dbTableName, boolean withActivityStatus, boolean itemIdAsName) throws SQLException {

        String sql = "select t.id, t.name ";
        if (withActivityStatus) {
            sql += ", st.id, st.name ";
        }
        sql += "from ";
        sql += dbTableName + " as t ";
        if (withActivityStatus) {
            sql += " left join activity_status as st on st.id=t.activity_status_id ";
        }
        sql += "order by t.id desc;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(SystemSettings.id, Integer.class, null);
        if (withActivityStatus) {
            container.addContainerProperty(myUi.getMessage(SptMessages.Status), String.class, null);
            container.addContainerProperty(SystemSettings.activity_status_id, Integer.class, null);
        }

        while (result.next()) {
            Item item;
            if (itemIdAsName) {
                item = container.addItem(result.getString("t.name"));
            } else {
                item = container.addItem(result.getInt("t.id"));
            }
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("t.name"));
            item.getItemProperty(SystemSettings.id).setValue(result.getInt("t.id"));
            if (withActivityStatus) {
                item.getItemProperty(myUi.getMessage(SptMessages.Status)).setValue(
                        result.getString("st.name"));
                item.getItemProperty(SystemSettings.activity_status_id).setValue(result.getInt("st.id"));
            }
        }
        return container;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, String dbTableName, boolean isDesc) throws SQLException {
        String sql = "select t.id, t.name from " + dbTableName + " as t order by t.id ";
        if (isDesc) {
            sql += "desc";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
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

    public IndexedContainer exec_for_select(MyVaadinUI myUi, String dbTableName,
                                            int school_id, boolean isDesc) throws SQLException {
        String sql = "select t.id, t.name from " + dbTableName + " as t where t.school_id = ? "
                + "and t.activity_status_id = 2 order by t.id";
        if (isDesc) {
            sql += " desc";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
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

    public IndexedContainer exec_for_select_general_working_statuses(MyVaadinUI myUi) throws SQLException {
        String sql = "select t.id, t.name from working_status as t where t.is_general = 1 order by t.id;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
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

    public IndexedContainer exec_order_for_sel(MyVaadinUI myUi) throws SQLException {

        String sql = "select o.id, o.name, o.education_status_id, es.name from orders as o "
                + "left join education_status as es on o.education_status_id=es.id "
                + "where o.id<4;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.EducationStatus), String.class, null);
        container.addContainerProperty(SystemSettings.education_status_id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("o.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("o.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.EducationStatus)).setValue(
                    result.getString("es.name"));
            item.getItemProperty(SystemSettings.education_status_id).setValue(
                    result.getInt("o.education_status_id"));
        }
        return container;
    }

    public IndexedContainer exec_positions_for_select(
            MyVaadinUI myUi, boolean withAdmin, boolean withHR) throws SQLException {

        String sql = "select t.id, t.name, t.default_permissions, p.id from hr_position as t "
                + "left join position as p on p.hr_position_id=t.id "
                + "where t.activity_status_id=2 ";
        if (!withAdmin) {
            sql += "and t.id!=5 ";
        }
        if (!withHR && !withAdmin) {
            sql += "and t.id!=25 ";
        }
        sql += "order by t.id desc;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(SystemSettings.position_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.Permissions), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("t.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Permissions)).setValue(
                    result.getString("t.default_permissions"));
            item.getItemProperty(SystemSettings.position_id).setValue(
                    result.getInt("p.id"));
        }
        return container;
    }

    public IndexedContainer exec_years_with_dates(MyVaadinUI myUi) throws SQLException {
        String sql = "select t.id, t.name, t.start_date, t.end_date from year as t order by t.id desc;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.StartDate), Date.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.TillDate), Date.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("t.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.StartDate)).setValue(
                    result.getDate("t.start_date"));
            item.getItemProperty(myUi.getMessage(SptMessages.TillDate)).setValue(
                    result.getDate("t.end_date"));
        }
        return container;
    }

    public IndexedContainer exec_years_for_select(MyVaadinUI myUi, int current_year_id) throws SQLException {
        String sql = "select t.id, t.name from year as t where t.id between ? and ? order by t.id desc;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, current_year_id - 1);
        stat.setInt(2, current_year_id + 1);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(result.getString("t.name"));
        }
        return container;
    }

    public int exec_insert(Definition def, String dbTableName, boolean withActivityStatus) throws SQLException {
        String sql = "INSERT IGNORE INTO " + dbTableName + " (name ";
        if (withActivityStatus) {
            sql += ", activity_status_id";
        }
        sql += ") VALUES(?";
        if (withActivityStatus) {
            sql += ",?";
        }
        sql += ");";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, def.getName());
        if (withActivityStatus) {
            stat.setInt(2, def.getActivity_status_id());
        }

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Definition def, String dbTableName, boolean withActivityStatus) throws SQLException {
        String sql = "UPDATE " + dbTableName + " SET name=? ";
        if (withActivityStatus) {
            sql += " ,activity_status_id=? ";
        }
        sql += "WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, def.getName());
        if (withActivityStatus) {
            stat.setInt(2, def.getActivity_status_id());
            stat.setInt(3, def.getId());
        } else {
            stat.setInt(2, def.getId());
        }
        int status = stat.executeUpdate();
        return status;
    }

    public int exec_update_emp_id(int id, int emp_id, String dbTableName) throws SQLException {
        String sql = "UPDATE " + dbTableName + " SET employee_id=? "
                + "WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, emp_id);
        stat.setInt(2, id);
        int status = stat.executeUpdate();
        return status;
    }

    public int exec_delete(int id, String dbTableName) throws SQLException {
        String sql = "DELETE FROM " + dbTableName + " WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        return stat.executeUpdate();
    }

    public int exec_delete_not_referenced(String dbTableName) throws SQLException {
        String sql = "DELETE IGNORE FROM " + dbTableName;
        PreparedStatement stat = dbCon.prepareStatement(sql);
        return stat.executeUpdate();
    }

    public int exec_delete(String id, String dbTableName) throws SQLException {
        String sql = "DELETE FROM " + dbTableName + " WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, id);
        return stat.executeUpdate();
    }

    public int exec_delete(String id, String dbTableName, String dbColumnName) throws SQLException {
        String sql = "DELETE FROM " + dbTableName + " WHERE " + dbColumnName + "=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, id);
        return stat.executeUpdate();
    }

    public IndexedContainer execPermissionSQL(MyVaadinUI myUi) throws SQLException {
        String sql = "SELECT p.java_class_name, p.caption, p.permissions FROM permissions as p " +
                "order by p.caption";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.ClassCaption), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Value), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Functions), ComboBoxMultiselectMax.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getString("p.java_class_name"));
            item.getItemProperty(myUi.getMessage(SptMessages.ClassName)).setValue(
                    result.getString("p.java_class_name"));
            item.getItemProperty(myUi.getMessage(SptMessages.ClassCaption)).setValue(
                    result.getString("p.caption"));
            item.getItemProperty(myUi.getMessage(SptMessages.Value)).setValue(
                    result.getString("p.permissions"));

        }
        return container;
    }

    public IndexedContainer execSQL_statuses_with_count(MyVaadinUI myUi, String table, boolean withTotal) throws SQLException {

        String sql = "SELECT id, name FROM " + table;
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(SystemSettings.id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(SystemSettings.count, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("id"));
            item.getItemProperty(SystemSettings.id).setValue(
                    result.getInt("id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("name"));
        }
        if (withTotal) {
            Item item = container.addItem(6);
            item.getItemProperty(SystemSettings.id).setValue(6);
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue("total");
        }

        return container;
    }


    public IndexedContainer execSQL_yes_no(MyVaadinUI myUi) {

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);

        Item item = container.addItem(1);
        item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(myUi.getMessage(SptMessages.Yes));
        item = container.addItem(0);
        item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(myUi.getMessage(SptMessages.No));

        return container;
    }

    public int execSQL_login() throws SQLException {
        String sql = "SELECT l.login FROM logins AS l LEFT JOIN employee AS e ON l.login = e.login WHERE e.login IS NULL LIMIT 1;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getInt("l.login");
        }
        return 0;
    }
}
