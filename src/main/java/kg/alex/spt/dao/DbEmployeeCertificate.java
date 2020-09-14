/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.EmployeeCertificate;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.EmployeeDefinitionView;

/**
 *
 * @author eldiyar
 */
public class DbEmployeeCertificate extends BaseDb {

    public DbEmployeeCertificate() throws Exception {
        super();
    }

    public int exec_insert(EmployeeCertificate ec) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "INSERT INTO hr_employee_certificate (employee_id,name,given_by,date_of_issue) "
                + "VALUES(?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ec.getEmployee_id());
        stat.setString(2, ec.getName());
        stat.setString(3, ec.getGiven_by());
        stat.setString(4, sysSettings.mysql_df.format(ec.getDate_of_issue()));
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeCertificate ec) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "update hr_employee_certificate set "
                + "name=?, given_by=?, date_of_issue=? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, ec.getName());
        stat.setString(2, ec.getGiven_by());
        stat.setString(3, sysSettings.mysql_df.format(ec.getDate_of_issue()));
        stat.setInt(4, ec.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
            EmployeeDefinitionView edv) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT ec.id, ec.name, ec.given_by, ec.date_of_issue FROM hr_employee_certificate as ec "
                + "where ec.employee_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareCertificateContainer();
        while (result.next()) {
            String id = result.getString("ec.id");
            Item item = container.addItem(id);
            item.getItemProperty(sysSettings.button).setValue(
                    edv.createButton(myUI.getMessage(SptMessages.DeleteButton), id, sysSettings.dbEmployeeCertificate));
            item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(
                    edv.createTextfield(result.getString("ec.name"),
                            myUI.getMessage(SptMessages.Name),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true));
            item.getItemProperty(myUI.getMessage(SptMessages.GivenBy)).setValue(
                    edv.createTextfield(result.getString("ec.given_by"),
                            myUI.getMessage(SptMessages.GivenBy),
                            new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true));
            item.getItemProperty(myUI.getMessage(SptMessages.IssueDate)).setValue(
                    edv.createDateField(result.getDate("ec.date_of_issue"),
                            myUI.getMessage(SptMessages.IssueDate), true));
            item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }
}
