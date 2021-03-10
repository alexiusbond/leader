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
import kg.alex.spt.i18n.SptMessages;

public class DbRelative extends BaseDb {

    public DbRelative() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int stud_id) throws SQLException {
        

        String sql = "SELECT sr.fullname, sr.work_place, sr.phone, "
                + "sr.adress, sr.passport, sr.relatives_id, sr.is_main "
                + "FROM student_relatives as sr where sr.student_id = ? "
                + "and (sr.relatives_id = 1 or sr.relatives_id = 2) "
                + "group by sr.relatives_id;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.FullName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.WorkPlace), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Phone), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Address), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Passport), String.class, null);
        container.addContainerProperty(SystemSettings.is_main, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("sr.relatives_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.FullName)).setValue(
                    result.getString("sr.fullname"));
            item.getItemProperty(myUi.getMessage(SptMessages.WorkPlace)).setValue(
                    result.getString("sr.work_place"));
            item.getItemProperty(myUi.getMessage(SptMessages.Phone)).setValue(
                    result.getString("sr.phone"));
            item.getItemProperty(myUi.getMessage(SptMessages.Address)).setValue(
                    result.getString("sr.adress"));
            item.getItemProperty(myUi.getMessage(SptMessages.Passport)).setValue(
                    result.getString("sr.passport"));
            item.getItemProperty(myUi.getMessage(SptMessages.Phone)).setValue(
                    result.getString("sr.phone"));
            item.getItemProperty(SystemSettings.is_main).setValue(
                    result.getInt("sr.is_main"));
        }
        return container;
    }

    public IndexedContainer execSQLSilkRoad(MyVaadinUI myUi, int stud_id) throws SQLException {
        String sql = "SELECT id, fullname, adress, phone FROM student_relatives "
                + "where student_id = ? and is_main = 1;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.FullName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Address), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Phone), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("id"));
            item.getItemProperty(myUi.getMessage(SptMessages.FullName)).setValue(
                    result.getString("fullname"));
            item.getItemProperty(myUi.getMessage(SptMessages.Address)).setValue(
                    result.getString("adress"));
            item.getItemProperty(myUi.getMessage(SptMessages.Phone)).setValue(
                    result.getString("phone"));
        }
        return container;
    }
}
