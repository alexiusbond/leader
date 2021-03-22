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

import java.sql.*;

import com.vaadin.shared.ui.datefield.Resolution;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.EmployeeChildren;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.EmployeeDefinitionView;

public class DbEmployeeChildren extends BaseDb {

    public DbEmployeeChildren() throws Exception {
        super();
    }

    public int exec_insert(EmployeeChildren ech) throws SQLException {
        String sql = "INSERT INTO hr_employee_children (employee_id,fullname,"
                + "date_of_birth,institution,hr_education_status_id,hr_health_status_id) "
                + "VALUES(?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ech.getEmployee_id());
        stat.setString(2, ech.getFullname());
        stat.setDate(3, new Date(ech.getDate_of_birth().getTime()));
        if (!ech.getInstitution().equals("") && ech.getInstitution() != null) {
            stat.setString(4, ech.getInstitution());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        if (ech.getEducation_status_id() != 0) {
            stat.setInt(5, ech.getEducation_status_id());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        stat.setInt(6, ech.getHealth_status_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeChildren ech) throws SQLException {
        String sql = "update hr_employee_children set "
                + "fullname=?, date_of_birth=?, institution=?, hr_education_status_id=?, hr_health_status_id=? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, ech.getFullname());
        stat.setDate(2, new Date(ech.getDate_of_birth().getTime()));
        if (!ech.getInstitution().equals("") && ech.getInstitution() != null) {
            stat.setString(3, ech.getInstitution());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (ech.getEducation_status_id() != 0) {
            stat.setInt(4, ech.getEducation_status_id());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        stat.setInt(5, ech.getHealth_status_id());
        stat.setInt(6, ech.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT ech.id, ech.fullname, ech.date_of_birth, ech.institution, " +
                "ech.hr_education_status_id, ech.hr_health_status_id " +
                "FROM hr_employee_children as ech where ech.employee_id = ? ;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareChildrenContainer();
        while (result.next()) {
            String id = result.getString("ech.id");
            Item item = container.addItem(id);
            item.getItemProperty(SystemSettings.button).setValue(
                    edv.createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbEmployeeChildren, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(SptMessages.FullName)).setValue(
                    edv.createTextfield(result.getString("ech.fullname"),
                            myUI.getMessage(SptMessages.FullName),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true));
            item.getItemProperty(myUI.getMessage(SptMessages.DateOfBirth)).setValue(
                    edv.createDateField(result.getDate("ech.date_of_birth"),
                            myUI.getMessage(SptMessages.DateOfBirth), null,
                            true, SystemSettings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(SptMessages.Institution)).setValue(
                    edv.createTextfield(result.getString("ech.institution"),
                            myUI.getMessage(SptMessages.Institution),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 300, true), false));
            item.getItemProperty(myUI.getMessage(SptMessages.EducationStatus)).setValue(
                    edv.createCombobox(result.getInt("ech.hr_education_status_id"),
                            myUI.getMessage(SptMessages.EducationStatus), SystemSettings.dbHrEducationStatus, false));
            item.getItemProperty(myUI.getMessage(SptMessages.HealthStatus)).setValue(
                    edv.createCombobox(result.getInt("ech.hr_health_status_id"),
                            myUI.getMessage(SptMessages.HealthStatus), SystemSettings.dbHealthStatus, true));
            item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }
}
