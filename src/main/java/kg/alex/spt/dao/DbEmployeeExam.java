/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.EmployeeExam;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.EmployeeDefinitionView;

public class DbEmployeeExam extends BaseDb {

    public DbEmployeeExam() throws Exception {
        super();
    }

    public int exec_insert(EmployeeExam ex) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "INSERT INTO hr_employee_exam (employee_id,hr_exam_id,score,date_of_issue) "
                + "VALUES(?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ex.getEmployee_id());
        stat.setInt(2, ex.getExam_id());
        stat.setDouble(3, ex.getScore());
        stat.setString(4, sysSettings.mysql_df.format(ex.getDate_of_issue()));
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeExam ex) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "update hr_employee_exam set "
                + "hr_exam_id=?, score=?, date_of_issue=? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ex.getExam_id());
        stat.setDouble(2, ex.getScore());
        stat.setString(3, sysSettings.mysql_df.format(ex.getDate_of_issue()));
        stat.setInt(4, ex.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
            EmployeeDefinitionView edv) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT ex.id, ex.hr_exam_id, ex.score, ex.date_of_issue FROM hr_employee_exam as ex "
                + "where ex.employee_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareExamContainer();
        while (result.next()) {
            String id = result.getString("ex.id");
            Item item = container.addItem(id);
            item.getItemProperty(sysSettings.button).setValue(
                    edv.createButton(myUI.getMessage(SptMessages.DeleteButton), id, sysSettings.dbEmployeeExams));
            item.getItemProperty(myUI.getMessage(SptMessages.Exam)).setValue(
                    edv.createCombobox(result.getInt("ex.hr_exam_id"),
                            myUI.getMessage(SptMessages.Exam), sysSettings.dbExamTable, true));
            item.getItemProperty(myUI.getMessage(SptMessages.Score)).setValue(
                    edv.createTextfieldWithProperty(result.getDouble("ex.score"), myUI.getMessage(SptMessages.Score),
                            new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                            new ObjectProperty<Double>(0.0), sysSettings.getStringToDoubleConverter()));
            item.getItemProperty(myUI.getMessage(SptMessages.IssueDate)).setValue(
                    edv.createDateField(result.getDate("ex.date_of_issue"),
                            myUI.getMessage(SptMessages.IssueDate), true));
            item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }
}
