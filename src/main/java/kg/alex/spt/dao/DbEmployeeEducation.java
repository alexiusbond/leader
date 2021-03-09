/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;

import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Attachment;
import kg.alex.spt.domain.Definition;
import kg.alex.spt.domain.EmployeeEducation;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.EmployeeDefinitionView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DbEmployeeEducation extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbEmployeeEducation.class);

    public DbEmployeeEducation() throws Exception {
        super();
    }

    public int exec_insert(EmployeeEducation ed) throws SQLException {
        String sql = "INSERT INTO hr_employee_education "
                + "(employee_id, hr_university_id, hr_own_id, department, start_date, "
                + "end_date, country_id, education_level_id,attachment_id) "
                + "VALUES(?,?,?,?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ed.getEmployee_id());
        stat.setInt(2, ed.getUniversity_id());
        stat.setInt(3, ed.getOwn_id());
        stat.setString(4, ed.getDepartment());
        stat.setString(5, SystemSettings.mysql_only_year.format(ed.getStart()));
        stat.setString(6, SystemSettings.mysql_only_year.format(ed.getEnd()));
        stat.setInt(7, ed.getCountry_id());
        stat.setInt(8, ed.getEducation_level_id());
        if (ed.getAttachment_id() != 0) {
            stat.setInt(9, ed.getAttachment_id());
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

    public int exec_update(EmployeeEducation ed) throws SQLException {
        String sql = "update hr_employee_education set "
                + "hr_university_id=?, department=?, start_date=?, end_date=?, country_id=?, education_level_id=?, attachment_id=? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ed.getUniversity_id());
        stat.setString(2, ed.getDepartment());
        stat.setString(3, SystemSettings.mysql_only_year.format(ed.getStart()));
        stat.setString(4, SystemSettings.mysql_only_year.format(ed.getEnd()));
        stat.setInt(5, ed.getCountry_id());
        stat.setInt(6, ed.getEducation_level_id());
        if (ed.getAttachment_id() != 0) {
            stat.setInt(7, ed.getAttachment_id());
        } else {
            stat.setNull(7, Types.INTEGER);
        }
        stat.setInt(8, ed.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(final MyVaadinUI myUI, int employee_id, int own_id, IndexedContainer c,
                                    EmployeeDefinitionView edv) throws SQLException {
        final SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT ed.id, ed.hr_university_id, ed.department, ed.start_date, ed.end_date, ed.country_id, "
                + "ed.education_level_id, a.id, a.name, a.extension, a.unique_name FROM hr_employee_education as ed "
                + "left join hr_attachments as a on a.id = ed.attachment_id "
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
                    edv.createButton(myUI.getMessage(SptMessages.DeleteButton), id, sysSettings.dbEmployeeEducation, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(SptMessages.Department)).setValue(
                    edv.createTextfield(result.getString("ed.department"),
                            myUI.getMessage(SptMessages.Department),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 250, true), true));
            item.getItemProperty(myUI.getMessage(SptMessages.Start)).setValue(
                    edv.createDateField(result.getDate("ed.start_date"),
                            myUI.getMessage(SptMessages.Start), null, true,
                            SystemSettings.yearPattern, Resolution.YEAR));
            item.getItemProperty(myUI.getMessage(SptMessages.End)).setValue(
                    edv.createDateField(result.getDate("ed.end_date"),
                            myUI.getMessage(SptMessages.End), null, true,
                            SystemSettings.yearPattern, Resolution.YEAR));
            item.getItemProperty(myUI.getMessage(SptMessages.Country)).setValue(
                    edv.createCombobox(result.getInt("ed.country_id"),
                            myUI.getMessage(SptMessages.Country), sysSettings.dbCountry, true));
            item.getItemProperty(myUI.getMessage(SptMessages.EduLevel)).setValue(
                    edv.createCombobox(result.getInt("ed.education_level_id"),
                            myUI.getMessage(SptMessages.EduLevel), sysSettings.dbEduLevel, true));
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

            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);

            if (result.getInt("a.id") != 0) {
                Attachment a = new Attachment();
                a.setId(result.getInt("a.id"));
                a.setUnique_name(result.getString("a.unique_name"));
                a.setExtension(result.getString("a.extension"));
                a.setName(result.getString("a.name"));
                Button b = edv.createButton(myUI.getMessage(SptMessages.DownLoad), id, sysSettings.download_button, FontAwesome.DOWNLOAD);
                b.setStyleName(ValoTheme.BUTTON_SMALL);
                b.setData(a);
                hl.addComponent(b);

                Upload upload = edv.createUpload("", false);
                upload.setId(id);
                upload.setData(container);
                hl.addComponent(upload);
                item.getItemProperty(myUI.getMessage(SptMessages.Document)).setValue(hl);
            }
            item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }
}
