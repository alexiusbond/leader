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
import kg.alex.spt.domain.EmployeeLanguage;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.EmployeeDefinitionView;

/**
 *
 * @author eldiyar
 */
public class DbEmployeeLanguage extends BaseDb {

    public DbEmployeeLanguage() throws Exception {
        super();
    }

    public int exec_insert(EmployeeLanguage el) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_language (employee_id,hr_language_id,hr_language_level_id) "
                + "VALUES(?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, el.getEmployee_id());
        stat.setInt(2, el.getLanguage_id());
        stat.setInt(3, el.getLevel_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeLanguage el) throws SQLException {
        String sql = "update hr_employee_language set "
                + "hr_language_id=?, hr_language_level_id=? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, el.getLanguage_id());
        stat.setInt(2, el.getLevel_id());
        stat.setInt(3, el.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
            EmployeeDefinitionView edv) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT el.id, el.hr_language_id, el.hr_language_level_id FROM hr_employee_language as el "
                + "where el.employee_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareLanguageContainer();
        while (result.next()) {
            String id = result.getString("el.id");
            Item item = container.addItem(id);
            item.getItemProperty(sysSettings.button).setValue(
                    edv.createButton(myUI.getMessage(SptMessages.DeleteButton), id, sysSettings.dbEmployeeLanguage));
            item.getItemProperty(myUI.getMessage(SptMessages.Language)).setValue(
                    edv.createCombobox(result.getInt("el.hr_language_id"),
                            myUI.getMessage(SptMessages.Language), sysSettings.dbLanguageTable, true));
            item.getItemProperty(myUI.getMessage(SptMessages.Level)).setValue(
                    edv.createCombobox(result.getInt("el.hr_language_level_id"),
                            myUI.getMessage(SptMessages.Level), sysSettings.dbLanguageLevelTable, true));
            item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }
}
