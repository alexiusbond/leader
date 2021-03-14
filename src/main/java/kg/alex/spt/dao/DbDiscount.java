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
import kg.alex.spt.domain.Discount;
import kg.alex.spt.i18n.SptMessages;

/**
 *
 * @author eldiiar
 */
public class DbDiscount extends BaseDb {

    public DbDiscount() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi)
            throws SQLException {
        

        String sql = "SELECT d.id, d.name, d.amount, d.activity_status_id, dt.name,"
                + "d.discount_type_id, ac.name, d.year_id, y.name FROM spt.discount as d "
                + "left join discount_type as dt on dt.id=d.discount_type_id "
                + "left join year as y on y.id=d.year_id "
                + "left join activity_status as ac on ac.id=d.activity_status_id "
                + "order by y.id DESC, d.id DESC;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Value), Double.class, 0.0);
        container.addContainerProperty(SystemSettings.discount_type_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.DiscountType), String.class, null);
        container.addContainerProperty(SystemSettings.year_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.Year), String.class, null);
        container.addContainerProperty(SystemSettings.status_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.Status), String.class, null);
        container.addContainerProperty(SystemSettings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("d.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("d.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Value)).setValue(
                    result.getDouble("d.amount"));
            item.getItemProperty(SystemSettings.discount_type_id).setValue(
                    result.getInt("d.discount_type_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.DiscountType)).setValue(
                    result.getString("dt.name"));
            item.getItemProperty(SystemSettings.year_id).setValue(
                    result.getInt("d.year_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Year)).setValue(
                    result.getString("y.name"));
            item.getItemProperty(SystemSettings.status_id).setValue(
                    result.getInt("d.activity_status_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(SystemSettings.id).setValue(result.getInt("d.id"));
        }
        return container;
    }

    public int exec_insert(Discount d) throws SQLException {
        String sql = "INSERT IGNORE INTO discount (name,amount,"
                + "activity_status_id,discount_type_id,year_id) "
                + "VALUES(?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, d.getName());
        stat.setDouble(2, d.getAmount());
        stat.setInt(3, d.getStatus_id());
        stat.setInt(4, d.getDisc_type_id());
        stat.setInt(5, d.getYear_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Discount d) throws SQLException {
        String sql = "UPDATE discount SET name=?, amount=?,activity_status_id=?,"
                + "discount_type_id=? WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, d.getName());
        stat.setDouble(2, d.getAmount());
        stat.setInt(3, d.getStatus_id());
        stat.setInt(4, d.getDisc_type_id());
        stat.setInt(5, d.getId());
        int status = stat.executeUpdate();
        return status;
    }

    public IndexedContainer execSQL_for_year_sel(MyVaadinUI myUi, int cur_year) throws SQLException {
        String sql = "SELECT  distinct(d.year_id), y.name "
                + "FROM discount as d left join year as y on y.id = d.year_id "
                + "where d.year_id!=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cur_year);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("d.year_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("y.name"));
        }
        return container;
    }

    public int exec_copy(int curr_year, int selected_year) throws SQLException {
        String sql = "insert ignore into discount (name, amount, "
                + "discount_type_id, year_id, activity_status_id) select name, "
                + "amount,discount_type_id, ? as year_id, activity_status_id "
                + "from discount where year_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, curr_year);
        stat.setInt(2, selected_year);
        int st = stat.executeUpdate();
        return st;
    }

    public IndexedContainer exec_disc_select(MyVaadinUI myUi, int year_id) throws SQLException {
        

        String sql = "select t.id, t.name, t.amount, t.discount_type_id "
                + "from discount as t "
                + "where t.year_id = ? and t.activity_status_id = 2 "
                + "order by t.name, t.amount;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Amount), Double.class, 0.0);
        container.addContainerProperty(SystemSettings.discount_type_id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            if (result.getInt("t.discount_type_id") == 3) {
                item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                        result.getString("t.name") + " - (max " + result.getString("t.amount") + "%)");
            } else if (result.getInt("t.discount_type_id") == 4) {
                item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                        result.getString("t.name") + " - (max " + result.getString("t.amount") + "$)");
            } else if (result.getInt("t.discount_type_id") == 1) {
                item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                        result.getString("t.name") + " - " + result.getString("t.amount") + "%");
            } else if (result.getInt("t.discount_type_id") == 2) {
                item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                        result.getString("t.name") + " - " + result.getString("t.amount") + "$");
            }
            item.getItemProperty(myUi.getMessage(SptMessages.Amount)).setValue(
                    result.getDouble("t.amount"));
            item.getItemProperty(SystemSettings.discount_type_id).setValue(
                    result.getInt("t.discount_type_id"));
        }
        return container;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, int year_id, int dis_id) 
            throws SQLException {
        String sql = "SELECT d.id, d.name, d.amount, d.discount_type_id "
                + "FROM discount as d " 
                + "where d.year_id = ? and (d.activity_status_id = 2 or d.id = ?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, dis_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUi.getMessage(SptMessages.DiscountType), Integer.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("d.id"));
            if (result.getInt("d.discount_type_id") == 3) {
                item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                        result.getString("d.name") + " - (max " + result.getString("d.amount") + "%)");
            } else if (result.getInt("d.discount_type_id") == 4) {
                item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                        result.getString("d.name") + " - (max " + result.getString("d.amount") + "$)");
            } else if (result.getInt("d.discount_type_id") == 1) {
                item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                        result.getString("d.name") + " - " + result.getString("d.amount") + "%");
            } else if (result.getInt("d.discount_type_id") == 2) {
                item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                        result.getString("d.name") + " - " + result.getString("d.amount") + "$");
            }
            item.getItemProperty(myUi.getMessage(SptMessages.Amount)).setValue(
                    result.getDouble("d.amount"));
            item.getItemProperty(myUi.getMessage(SptMessages.DiscountType)).setValue(
                    result.getInt("d.discount_type_id"));
        }
        return container;
    }
}