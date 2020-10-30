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

public class DbProductCategories extends BaseDb {

    public DbProductCategories() throws Exception {
        super();
    }

    public IndexedContainer execSQL_cont(MyVaadinUI myUI) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT id, name, acc_category_id "
                + "FROM dp_product_category order by id;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Name), String.class, 0);
        container.addContainerProperty(sysSettings.acc_category_id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(result.getString("name"));
            item.getItemProperty(sysSettings.acc_category_id).setValue(result.getInt("acc_category_id"));
        }
        return container;
    }

    public int exec_update(int cat_id, int id) throws SQLException {
        String sql = "update dp_product_category set acc_category_id = ? where id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cat_id);
        stat.setInt(2, id);
        int status = stat.executeUpdate();
        return status;
    }
}
