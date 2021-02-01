/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Definition;
import kg.alex.spt.domain.EmployeeWork;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.EmployeeDefinitionView;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Iterator;

public class DbEmployeeWork extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbEmployeeWork.class);

    public DbEmployeeWork() throws Exception {
        super();
    }

    public int exec_insert(EmployeeWork ew) throws SQLException {
        String sql = "INSERT INTO hr_employee_work (employee_id, hr_own_id, hr_work_place_id, position_id, "
                + "start_date, end_date, working_status_id, is_sapat) "
                + "VALUES(?,?,?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ew.getEmployee_id());
        stat.setInt(2, ew.getOwn_id());
        stat.setInt(3, ew.getWork_place_id());
        stat.setInt(4, ew.getMain_position_id());
        stat.setDate(5, new Date(ew.getStart().getTime()));
        if (ew.getEnd() != null) {
            stat.setDate(6, new Date(ew.getEnd().getTime()));
        } else {
            stat.setNull(6, Types.DATE);
        }
        stat.setInt(7, ew.getWorking_status_id());
        stat.setBoolean(8, ew.isSapat());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public void exec_insert_extra_position(int employee_work_id, int position_id) throws SQLException {
        String sql = "INSERT INTO hr_employee_work_extra_positions (hr_employee_work_id, position_id) VALUES(?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_work_id);
        stat.setInt(2, position_id);
        stat.executeUpdate();
    }

    public int exec_update(EmployeeWork ew) throws SQLException {
        String sql = "update hr_employee_work set "
                + "hr_work_place_id=?, position_id=?, start_date=?, end_date=?, "
                + "working_status_id = ?, is_sapat = ? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ew.getWork_place_id());
        stat.setInt(2, ew.getMain_position_id());
        stat.setDate(3, new Date(ew.getStart().getTime()));
        if (ew.getEnd() != null) {
            stat.setDate(4, new Date(ew.getEnd().getTime()));
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        stat.setInt(5, ew.getWorking_status_id());
        stat.setBoolean(6, ew.isSapat());
        stat.setInt(7, ew.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(final MyVaadinUI myUI, int employee_id, int own_id, IndexedContainer c,
                                    EmployeeDefinitionView edv) throws SQLException {
        final SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT ew.id, ew.hr_work_place_id, ew.position_id, " +
                "group_concat(ep.position_id separator ',') as extra_positions, " +
                "ew.start_date, ew.end_date, ew.working_status_id, ew.is_sapat FROM hr_employee_work as ew " +
                "left join hr_employee_work_extra_positions as ep on ep.hr_employee_work_id = ew.id " +
                "where ew.employee_id = ? and ew.hr_own_id = ? group by ew.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, own_id);
        ResultSet result = stat.executeQuery();
        final IndexedContainer container = edv.prepareWorkContainer(c);
        while (result.next()) {
            String id = result.getString("ew.id");
            Item item = container.addItem(id);
            item.getItemProperty(sysSettings.button).setValue(
                    edv.createButton(myUI.getMessage(SptMessages.DeleteButton), id, sysSettings.dbEmployeeWork, FontAwesome.MINUS_SQUARE));
            ComboBoxMax cb = edv.createCombobox(0, myUI.getMessage(SptMessages.MainPosition),
                    null, true);
            item.getItemProperty(myUI.getMessage(SptMessages.MainPosition)).setValue(cb);
            ComboBoxMultiselectMax cb3 = edv.createComboboxMulti(
                    myUI.getMessage(SptMessages.ExtraPositions), false);
            try {
                DbDefinition dbDef = new DbDefinition();
                dbDef.connect();
                cb3.setContainerDataSource(
                        dbDef.exec_positions_for_select(myUI, false, true));
                cb.setContainerDataSource(
                        dbDef.exec_positions_for_select(myUI, false, true));
                dbDef.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setValue(result.getInt("ew.position_id"));
            if (result.getString("extra_positions") != null) {
                cb3.setValue(sysSettings.convertToSet(result.getString("extra_positions")));
            }
            item.getItemProperty(myUI.getMessage(SptMessages.ExtraPositions)).setValue(cb3);
            cb = edv.createCombobox(0, myUI.getMessage(SptMessages.WorkingStatus), null, true);
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
            item.getItemProperty(myUI.getMessage(SptMessages.Start)).setValue(
                    edv.createDateField(result.getDate("ew.start_date"),
                            myUI.getMessage(SptMessages.Start), null, true, sysSettings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(SptMessages.End)).setValue(
                    edv.createDateField(result.getDate("ew.end_date"),
                            myUI.getMessage(SptMessages.End), null, false, sysSettings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(SptMessages.Sapat)).setValue(
                    edv.createCheckBox(result.getBoolean("ew.is_sapat"), myUI.getMessage(SptMessages.Sapat)));
            item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }
}
