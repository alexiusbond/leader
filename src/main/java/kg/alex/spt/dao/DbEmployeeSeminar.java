/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.vaadin.shared.ui.datefield.Resolution;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.EmployeeSeminar;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.EmployeeDefinitionView;

/**
 * @author eldiyar
 */
public class DbEmployeeSeminar extends BaseDb {

    public DbEmployeeSeminar() throws Exception {
        super();
    }

    public int exec_insert(EmployeeSeminar es) throws SQLException {
        String sql = "INSERT INTO hr_employee_seminar (employee_id,name,subject,note,date_of_issue) "
                + "VALUES(?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, es.getEmployee_id());
        stat.setString(2, es.getName());
        stat.setString(3, es.getSubject());
        stat.setString(4, es.getNote());
        stat.setDate(5, new Date(es.getDate_of_issue().getTime()));
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeSeminar es) throws SQLException {
        String sql = "update hr_employee_seminar set "
                + "name=?, subject=?, note=?, date_of_issue=? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, es.getName());
        stat.setString(2, es.getSubject());
        stat.setString(3, es.getNote());
        stat.setDate(4, new Date(es.getDate_of_issue().getTime()));
        stat.setInt(5, es.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT es.id, es.name, es.subject, es.note, es.date_of_issue FROM hr_employee_seminar as es "
                + "where es.employee_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareSeminarContainer();
        while (result.next()) {
            String id = result.getString("es.id");
            Item item = container.addItem(id);
            item.getItemProperty(sysSettings.button).setValue(
                    edv.createButton(myUI.getMessage(SptMessages.DeleteButton), id, sysSettings.dbEmployeeSeminar));
            item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(
                    edv.createTextfield(result.getString("es.name"),
                            myUI.getMessage(SptMessages.Name),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true));
            item.getItemProperty(myUI.getMessage(SptMessages.Subject)).setValue(
                    edv.createTextfield(result.getString("es.subject"),
                            myUI.getMessage(SptMessages.Subject),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true));
            item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                    edv.createTextfield(result.getString("es.note"),
                            myUI.getMessage(SptMessages.Note),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), false));
            item.getItemProperty(myUI.getMessage(SptMessages.IssueDate)).setValue(
                    edv.createDateField(result.getDate("es.date_of_issue"),
                            myUI.getMessage(SptMessages.IssueDate), null,
                            true, sysSettings.datePattern, Resolution.DAY));
            item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }
}
