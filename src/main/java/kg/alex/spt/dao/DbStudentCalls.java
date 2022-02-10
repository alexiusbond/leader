/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.students.CallsReport;
import kg.alex.spt.ui.StudentDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DbStudentCalls extends BaseDb {

    public DbStudentCalls() throws Exception {
        super();
    }

    public int exec_insert(int st_id, int year_id, int emp_id, String note)
            throws SQLException {
        String sql = "INSERT INTO student_calls (student_id,year_id,employee_id,"
                + "note, modification_date) VALUES(?,?,?,?,NOW());";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        stat.setInt(2, year_id);
        stat.setInt(3, emp_id);
        stat.setString(4, note);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_St_Calls(MyVaadinUI myUI, int stud_id, int year_id,
            StudentDefinitionView dw) throws SQLException {
        

        String sql = "SELECT sc.id, sc.note, concat(e.name, ' ', e.surname) as fullname, sc.modification_date FROM student_calls as sc "
                + "left join employee as e on sc.employee_id = e.id "
                + "where sc.student_id = ? and sc.year_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = dw.prepareCallsContainer();
        while (result.next()) {
            String id = result.getString("sc.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    dw.createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                            Settings.dbStudentCalls, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                    Settings.df.format(result.getDate("sc.modification_date")));
            item.getItemProperty(myUI.getMessage(SptMessages.WhoCalled)).setValue(
                    result.getString("fullname"));
            item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                    dw.createTextfieldNote(result.getString("sc.note"),
                            myUI.getMessage(SptMessages.Note), id));
            item.getItemProperty(Settings.crud_status)
                    .setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }

    public int exec_delete(String id) throws SQLException {
        String sql = "DELETE from student_calls where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, id);
        return stat.executeUpdate();
    }

    public int exec_update(String note, int id) throws SQLException {
        String sql = "Update student_calls set note=? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, note);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_getCallsReport(MyVaadinUI myUI, Date from,
            Date till, int year_id, String class_ids, String edu_statuses_ids,
            CallsReport cr) throws SQLException {
        

        String sql = "SELECT sc.id, st.name, st.surname, DATE(sc.modification_date) AS date, "
                + "CONCAT(cnu.name, ' - ', cna.name) AS class_name, sc.note, concat(e.name, ' ', e.surname) as fullname "
                + "FROM student_calls AS sc LEFT JOIN student AS st ON sc.student_id = st.id "
                + "LEFT JOIN employee AS e ON sc.employee_id = e.id LEFT JOIN "
                + "(SELECT MAX(so.id) AS oid, so.student_id AS stud_id FROM student_orders AS so "
                + "WHERE so.year_id = ? AND so.is_valid = 1 GROUP BY so.student_id) "
                + "AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN class_name AS cna ON cna.id = CASE "
                + "WHEN stud_o.to_class_name_id IS NULL THEN st.class_name_id "
                + "ELSE stud_o.to_class_name_id END "
                + "LEFT JOIN class_number AS cnu ON cna.class_number_id = cnu.id "
                + "LEFT JOIN education_status AS edu ON edu.id = "
                + "CASE WHEN stud_o.to_education_status_id IS NULL "
                + "THEN st.education_status_id ELSE stud_o.to_education_status_id END "
                + "WHERE cna.id IN (" + class_ids + ") AND DATE(sc.modification_date) >= ? "
                + "AND DATE(sc.modification_date) <= ? AND sc.year_id = ? "
                + "AND edu.id IN (" + edu_statuses_ids + ") "
                + "ORDER BY cnu.id, cna.id, st.name, st.surname, sc.modification_date;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setDate(2, new java.sql.Date(from.getTime()));
        stat.setDate(3, new java.sql.Date(till.getTime()));
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.WhoCalled), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("sc.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.FirstName)).setValue(
                    result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.LastName)).setValue(
                    result.getString("st.surname"));
            item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                    result.getString("class_name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                    result.getString("sc.note"));
            item.getItemProperty(myUI.getMessage(SptMessages.WhoCalled)).setValue(
                    result.getString("fullname"));
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                    Settings.df.format((result.getDate("date"))));
            cr.total++;
        }
        return container;
    }

    public String exec_getLastCall(int st_id) throws SQLException {
        String sql = "SELECT CONCAT(DATE(modification_date), IF((note IS NOT NULL AND note != ''), CONCAT(' (', note, ')'), '')) as last_call "
                + "FROM student_calls "
                + "WHERE student_id = ? order by id desc limit 1;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        ResultSet result = stat.executeQuery();
        String c = new String();
        while (result.next()) {
            c = result.getString("last_call");
        }
        return c;

    }
}
