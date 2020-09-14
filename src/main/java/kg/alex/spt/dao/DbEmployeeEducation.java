/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Definition;
import kg.alex.spt.domain.EmployeeEducation;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.EmployeeDefinitionView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author eldiyar
 */
public class DbEmployeeEducation extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbEmployeeEducation.class);

    public DbEmployeeEducation() throws Exception {
        super();
    }

    public int exec_insert(EmployeeEducation ed) throws SQLException {
        String sql = "INSERT INTO hr_employee_education (employee_id,hr_university_id,hr_own_id,faculty,department,year) "
                + "VALUES(?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ed.getEmployee_id());
        stat.setInt(2, ed.getUniversity_id());
        stat.setInt(3, ed.getOwn_id());
        stat.setString(4, ed.getFaculty());
        stat.setString(5, ed.getDepartment());
        stat.setString(6, ed.getYear());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeEducation ed) throws SQLException {
        String sql = "update hr_employee_education set "
                + "hr_university_id=?, faculty=?, department=?, year=? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ed.getUniversity_id());
        stat.setString(2, ed.getFaculty());
        stat.setString(3, ed.getDepartment());
        stat.setString(4, ed.getYear());
        stat.setInt(5, ed.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(final MyVaadinUI myUI, int employee_id, int own_id, IndexedContainer c,
            EmployeeDefinitionView edv) throws SQLException {
        final SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT ed.id, ed.hr_university_id, ed.faculty, ed.department, ed.year FROM hr_employee_education as ed "
                + "where ed.employee_id = ? and ed.hr_own_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, own_id);
        ResultSet result = stat.executeQuery();
        final IndexedContainer container = edv.prepareEducationContainer(c);
        while (result.next()) {
            String id = result.getString("ed.id");
            Item item = container.addItem(id);
            item.getItemProperty(sysSettings.button).setValue(
                    edv.createButton(myUI.getMessage(SptMessages.DeleteButton), id, sysSettings.dbEmployeeEducation));
            item.getItemProperty(myUI.getMessage(SptMessages.Faculty)).setValue(
                    edv.createTextfield(result.getString("ed.faculty"),
                            myUI.getMessage(SptMessages.Faculty),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true));
            item.getItemProperty(myUI.getMessage(SptMessages.Department)).setValue(
                    edv.createTextfield(result.getString("ed.department"),
                            myUI.getMessage(SptMessages.Department),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true));
            item.getItemProperty(myUI.getMessage(SptMessages.Year)).setValue(
                    edv.createTextfield(result.getString("ed.year"),
                            myUI.getMessage(SptMessages.Year),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 10, true), true));
            final ComboBox cb = edv.createCombobox(result.getInt("ed.hr_university_id"),
                    myUI.getMessage(SptMessages.University), sysSettings.dbUniversityTable, true);
            cb.setNewItemsAllowed(true);
            cb.setNewItemHandler(new AbstractSelect.NewItemHandler() {
                @Override
                public void addNewItem(String newItemCaption) {
                    try {
                        DbDefinition dbd = new DbDefinition();
                        dbd.connect();
                        int id = dbd.exec_insert(new Definition(0, newItemCaption), sysSettings.dbUniversityTable, false);
                        dbd.close();
                        if (id != 0) {
                            Item item = ((IndexedContainer) cb.getContainerDataSource()).addItem(id);
                            if (item != null) {
                                item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(newItemCaption);
                                cb.setValue(id);
                                Iterator iter = container.getItemIds().iterator();
                                while (iter.hasNext()) {
                                    Object next = iter.next();
                                    if (((ComboBox) container.getContainerProperty(next,
                                            myUI.getMessage(SptMessages.University)).getValue()).getValue() == null) {
                                        item = ((IndexedContainer) ((ComboBox) container.getContainerProperty(next,
                                                myUI.getMessage(SptMessages.University)).getValue()).getContainerDataSource()).addItem(id);
                                        item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(newItemCaption);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                }
            });
            item.getItemProperty(myUI.getMessage(SptMessages.University)).setValue(cb);
            item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }
}
