/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.TreeTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.AccCategory;
import kg.alex.spt.i18n.SptMessages;
import org.tepi.filtertable.FilterTreeTable;

public class DbAccCategory extends BaseDb {

    public DbAccCategory() throws Exception {
        super();
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUI, int type) throws SQLException {
        String sql = "SELECT c.id, c.name, ifnull(concat(c.parent_code,'.',c.code), c.code) as code "
                + "from acc_category as c where c.acc_type_id = ? and c.school_id is null "
                + "order by ifnull(concat(c.parent_code,'.',c.code),c.code);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, type);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.FullName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Code), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(
                    result.getString("c.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.FullName)).setValue(
                    result.getString("code") + " - " + result.getString("c.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Code)).setValue(
                    result.getString("code"));
        }
        return container;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUI, int type, int school_id, int id, boolean withParents) throws SQLException {
        

        String sql = "SELECT c.id, c.name, ifnull(concat(c.parent_code,'.',c.code), c.code) as code, sc.acc_currency_id, "
                + "c.employee_id from acc_category as c "
                + "left join acc_category as cp on cp.id = c.parent_id "
                + "left join hr_salary_category as sc on sc.acc_category_id = cp.parent_id "
                + "where c.acc_type_id = ? and (c.school_id is null or c.school_id = ?) "
                + "and (c.activity_status_id = 2 or c.id = ?) ";
        if (!withParents) {
            sql += "and c.parent_id is not null ";
        }
        sql += "order by ifnull(concat(c.parent_code,'.',c.code),c.code);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, type);
        stat.setInt(2, school_id);
        stat.setInt(3, id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.FullName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Code), String.class, null);
        container.addContainerProperty(SystemSettings.acc_currency_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.employee_id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(result.getString("c.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.FullName)).setValue(
                    result.getString("code") + " - " + result.getString("c.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Code)).setValue(result.getString("code"));
            item.getItemProperty(SystemSettings.acc_currency_id).setValue(result.getInt("sc.acc_currency_id"));
            item.getItemProperty(SystemSettings.employee_id).setValue(result.getInt("c.employee_id"));
        }
        return container;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUI, String parent_ids) throws SQLException {
        String sql = "SELECT c.id, c.name, ifnull(concat(c.parent_code,'.',c.code),c.code) as code from acc_category as c "
                + "where c.parent_id in (" + parent_ids + ") order by ifnull(concat(c.parent_code,'.',c.code),c.code);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.FullName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Code), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(
                    result.getString("c.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.FullName)).setValue(
                    result.getString("code") + " - " + result.getString("c.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Code)).setValue(
                    result.getString("code"));
        }
        return container;
    }

    public void execSQL(MyVaadinUI myUI, int type, TreeTable t) throws SQLException {
        

        String sql = "SELECT c.id, c.name, ifnull(concat(c.parent_code,'.',c.code),c.code) as code, c.parent_id, s.name, c.activity_status_id, "
                + "c2.name, c.note FROM acc_category as c "
                + "left join activity_status as s on c.activity_status_id = s.id "
                + "left join acc_category as c2 on c.parent_id = c2.id where c.acc_type_id = ? and c.school_id is null "
                + "group by c.id "
                + "order by ifnull(concat(c.parent_code,'.',c.code),c.code), c.activity_status_id;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, type);
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Title), String.class, 0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Code), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Parent), String.class, null);
        container.addContainerProperty(SystemSettings.parent_id, Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Status), String.class, null);
        container.addContainerProperty(SystemSettings.status_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.id, Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Note), String.class, null);
        t.setContainerDataSource(container);
        while (result.next()) {
            Item item = container.addItem(result.getInt("c.id"));
            container.setChildrenAllowed(result.getInt("c.id"), false);
            container.setChildrenAllowed(result.getInt("c.parent_id"), true);
            t.setCollapsed(result.getInt("c.parent_id"), false);
            if (result.getInt("c.parent_id") != 0) {
                container.setParent(result.getInt("c.id"), result.getInt("c.parent_id"));
            }
            item.getItemProperty(SystemSettings.id).setValue(result.getInt("c.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(
                    result.getString("c.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Code)).setValue(
                    result.getString("code"));
            item.getItemProperty(myUI.getMessage(SptMessages.Parent)).setValue(
                    result.getString("c2.name"));
            item.getItemProperty(SystemSettings.parent_id).setValue(
                    result.getInt("c.parent_id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Status)).setValue(
                    result.getString("s.name"));
            item.getItemProperty(SystemSettings.status_id).setValue(
                    result.getInt("c.activity_status_id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                    result.getString("c.note"));
        }
    }

    public void execSQL_for_select_as_tree(MyVaadinUI myUI, int type, FilterTreeTable t,
                                           String school_ids, boolean onlyActive)
            throws SQLException {
        String sql = "SELECT c.id, concat(ifnull(concat(c.parent_code,'.',c.code),c.code), ' - ', c.name) as name, "
                + "c.parent_id FROM acc_category as c where c.acc_type_id = ? ";
        if (onlyActive) {
            sql += "and c.activity_status_id = 2 ";
        }
        sql += "and (c.school_id IS NULL or c.school_id in (" + school_ids + ")) "
                + "and (c.parent_id not in (select acc_category_id from dp_product_category) or c.parent_id is NULL) "
                + "group by c.id order by c.parent_code, CAST(code AS UNSIGNED);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, type);
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Title), String.class, null);
        t.setContainerDataSource(container);
        while (result.next()) {
            Item item = container.addItem(result.getInt("c.id"));
            container.setChildrenAllowed(result.getInt("c.id"), false);
            container.setChildrenAllowed(result.getInt("c.parent_id"), true);
            t.setCollapsed(result.getInt("c.parent_id"), false);
            if (result.getInt("c.parent_id") != 0) {
                container.setParent(result.getInt("c.id"), result.getInt("c.parent_id"));
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(
                    result.getString("name"));
        }
    }

    public void execSQL_for_select_as_tree(MyVaadinUI myUI, FilterTreeTable t, String parent_ids)
            throws SQLException {
        String sql = "SELECT c.id, concat(ifnull(concat(c.parent_code,'.',c.code),c.code), ' - ', c.name) as name, c.parent_id from acc_category as c "
                + "where c.parent_id in (" + parent_ids + ") or c.id in (" + parent_ids + ") order by c.parent_code, CAST(code AS UNSIGNED);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Title), String.class, null);
        t.setContainerDataSource(container);
        while (result.next()) {
            Item item = container.addItem(result.getInt("c.id"));
            container.setChildrenAllowed(result.getInt("c.id"), false);
            container.setChildrenAllowed(result.getInt("c.parent_id"), true);
            t.setCollapsed(result.getInt("c.parent_id"), false);
            if (result.getInt("c.parent_id") != 0) {
                container.setParent(result.getInt("c.id"), result.getInt("c.parent_id"));
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(result.getString("name"));
        }
    }

    public int exec_insert(AccCategory ac) throws SQLException {
        String sql = "INSERT IGNORE INTO acc_category (name, code, parent_id, acc_type_id, "
                + "activity_status_id, note, parent_code,school_id,employee_id) values(?,?,?,?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, ac.getName());
        stat.setString(2, ac.getCode());
        if (ac.getParent_id() != 0) {
            stat.setInt(3, ac.getParent_id());
        } else {
            stat.setNull(3, Types.INTEGER);
        }
        stat.setInt(4, ac.getType_id());
        stat.setInt(5, ac.getStatus_id());
        if (ac.getNote() != null && !ac.getNote().equals("")) {
            stat.setString(6, ac.getNote());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        if (ac.getParent_code() != null) {
            stat.setString(7, ac.getParent_code());
        } else {
            stat.setNull(7, Types.VARCHAR);
        }
        if (ac.getSchool_id() != 0) {
            stat.setInt(8, ac.getSchool_id());
        } else {
            stat.setNull(8, Types.INTEGER);
        }
        if (ac.getEmployee_id() != 0) {
            stat.setInt(9, ac.getEmployee_id());
        } else {
            stat.setNull(9, Types.INTEGER);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(AccCategory ac) throws SQLException {
        String sql = "update acc_category set name=?, code=?, parent_id=?, "
                + "activity_status_id=?, note=?, parent_code=? "
                + "where id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, ac.getName());
        stat.setString(2, ac.getCode());
        if (ac.getParent_id() != 0) {
            stat.setInt(3, ac.getParent_id());
        } else {
            stat.setNull(3, Types.INTEGER);
        }
        stat.setInt(4, ac.getStatus_id());
        stat.setString(5, ac.getNote());
        if (ac.getParent_code() != null) {
            stat.setString(6, ac.getParent_code());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        stat.setInt(7, ac.getId());
        int status = stat.executeUpdate();
        return status;
    }

    private int exec_update_parent_code(int id, String code) throws SQLException {
        String sql = "update acc_category set parent_code=? where id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, code);
        stat.setInt(2, id);
        int status = stat.executeUpdate();
        return status;
    }

    public int exec_update_activity_status(int id, int activity_status_id, String namePostfix) throws SQLException {
        String sql = "update acc_category set activity_status_id = ?, "
                + "name = if(? = 2, replace(name, ?, ''), concat(name, ?)) where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, activity_status_id);
        stat.setInt(2, activity_status_id);
        stat.setString(3, namePostfix);
        stat.setString(4, namePostfix);
        stat.setInt(5, id);
        int status = stat.executeUpdate();
        return status;
    }

    public int exec_id(int parent_id, int school_id) throws SQLException {
        String sql = "SELECT c.id from acc_category as c where c.parent_id = ? and school_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, parent_id);
        stat.setInt(2, school_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getInt("c.id");
        }
        return 0;
    }

    public AccCategory exec_sql(int employee_id, int from_school_id, int to_school_id) throws SQLException {
        String sql = "select ac.name, ac.code, concat(ac3.parent_code, '.', ac3.code) as parent_code, ac3.id "
                + "from acc_category ac "
                + "left join acc_category ac2 on ac.parent_id = ac2.id "
                + "left join acc_category ac3 on ac2.parent_code = ac3.parent_code and ac3.school_id = ? "
                + "where ac.school_id = ? and ac.employee_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, to_school_id);
        stat.setInt(2, from_school_id);
        stat.setInt(3, employee_id);
        ResultSet result = stat.executeQuery();
        AccCategory ac = new AccCategory();

        while (result.next()) {
            ac.setParent_code(result.getString("parent_code"));
            ac.setParent_id(result.getInt("ac3.id"));
            ac.setName(result.getString("ac.name"));
            ac.setCode(result.getString("ac.code"));
            ac.setType_id(2);
            ac.setStatus_id(2);
            ac.setEmployee_id(employee_id);
            ac.setSchool_id(to_school_id);
        }
        return ac;
    }

    public int exec_update_code(int id, String code, String name) throws SQLException {
        String sql = "update acc_category set code= ?, name = ? where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, code);
        stat.setString(2, name);
        stat.setInt(3, id);
        int status = stat.executeUpdate();
        return status;
    }

    public int exec_update_all_parent_codes(int parent_id, String parent_code, boolean isRecursevely) throws
            SQLException {
        int status = 0;
        String sql = "SELECT c.id, c.code from acc_category as c where c.parent_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, parent_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            exec_update_parent_code(result.getInt("c.id"), parent_code);
            if (isRecursevely) {
                exec_update_all_parent_codes(result.getInt("c.id"), result.getString("c.code"), true);
            }
        }
        return status;
    }
}
