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
import java.sql.Types;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Definition;
import kg.alex.spt.domain.EmployeeWork;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.ui.EmployeeDefinitionView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author eldiyar
 */
public class DbEmployeeWork extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbEmployeeWork.class);

    public DbEmployeeWork() throws Exception {
        super();
    }

    public int exec_insert(EmployeeWork ew) throws SQLException {
        String sql = "INSERT INTO hr_employee_work (employee_id,hr_own_id,hr_work_place_id,main_position,extra_position,year,working_status_id) "
                + "VALUES(?,?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ew.getEmployee_id());
        stat.setInt(2, ew.getOwn_id());
        stat.setInt(3, ew.getWork_place_id());
        stat.setString(4, ew.getMain_position());
        if (ew.getExtra_position() != null) {
            stat.setString(5, ew.getExtra_position());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        stat.setString(6, ew.getYear());
        stat.setInt(7, ew.getWorking_status_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeWork ew) throws SQLException {
        String sql = "update hr_employee_work set "
                + "hr_work_place_id=?, main_position=?, extra_position=?, year=?, working_status_id = ? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ew.getWork_place_id());
        stat.setString(2, ew.getMain_position());
        if (ew.getExtra_position() != null) {
            stat.setString(3, ew.getExtra_position());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        stat.setString(4, ew.getYear());
        stat.setInt(5, ew.getWorking_status_id());
        stat.setInt(6, ew.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(final MyVaadinUI myUI, int employee_id, int own_id, IndexedContainer c,
            EmployeeDefinitionView edv) throws SQLException {
        final SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT ew.id, ew.hr_work_place_id, ew.main_position, ew.extra_position, ew.year, ew.working_status_id FROM hr_employee_work as ew "
                + "where ew.employee_id = ? and ew.hr_own_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, own_id);
        ResultSet result = stat.executeQuery();
        final IndexedContainer container = edv.prepareWorkContainer(c);
        while (result.next()) {
            String id = result.getString("ew.id");
            Item item = container.addItem(id);
            item.getItemProperty(sysSettings.button).setValue(
                    edv.createButton(myUI.getMessage(SptMessages.DeleteButton), id, sysSettings.dbEmployeeWork));
            item.getItemProperty(myUI.getMessage(SptMessages.MainPosition)).setValue(
                    edv.createTextfield(result.getString("ew.main_position"),
                            myUI.getMessage(SptMessages.MainPosition),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 150, true), true));
            item.getItemProperty(myUI.getMessage(SptMessages.ExtraPositions)).setValue(
                    edv.createTextfield(result.getString("ew.extra_position"),
                            myUI.getMessage(SptMessages.ExtraPositions),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), false));
            ComboBoxMax cb = edv.createCombobox(0, myUI.getMessage(SptMessages.WorkingStatus), null, true);
            try {
                DbDefinition dbd = new DbDefinition();
                dbd.connect();
                cb.setContainerDataSource(dbd.exec_for_select_general_working_statuses(myUI));
                dbd.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setValue(result.getInt("ew.working_status_id"));
            item.getItemProperty(myUI.getMessage(SptMessages.WorkingStatus)).setValue(cb);
            final ComboBoxMax cb2 = edv.createCombobox(result.getInt("ew.hr_work_place_id"),
                    myUI.getMessage(SptMessages.WorkPlace), sysSettings.dbWork_placeTable, true);
            cb2.setNewItemsAllowed(true);
            cb2.setNewItemHandler(new AbstractSelect.NewItemHandler() {
                @Override
                public void addNewItem(String newItemCaption) {
                    try {
                        DbDefinition dbd = new DbDefinition();
                        dbd.connect();
                        int id = dbd.exec_insert(new Definition(0, newItemCaption), sysSettings.dbWork_placeTable, false);
                        dbd.close();
                        if (id != 0) {
                            Item item = ((IndexedContainer) cb2.getContainerDataSource()).addItem(id);
                            if (item != null) {
                                item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(newItemCaption);
                                cb2.setValue(id);
                                Iterator iter = container.getItemIds().iterator();
                                while (iter.hasNext()) {
                                    Object next = iter.next();
                                    if (((ComboBox) container.getContainerProperty(next,
                                            myUI.getMessage(SptMessages.WorkPlace)).getValue()).getValue() == null) {
                                        item = ((IndexedContainer) ((ComboBox) container.getContainerProperty(next,
                                                myUI.getMessage(SptMessages.WorkPlace)).getValue()).getContainerDataSource()).addItem(id);
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
            item.getItemProperty(myUI.getMessage(SptMessages.WorkPlace)).setValue(cb2);
            item.getItemProperty(myUI.getMessage(SptMessages.Year)).setValue(
                    edv.createTextfield(result.getString("ew.year"),
                            myUI.getMessage(SptMessages.Year),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 10, true), true));
            item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }
}
