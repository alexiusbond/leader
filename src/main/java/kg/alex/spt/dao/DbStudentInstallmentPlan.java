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
import kg.alex.spt.domain.StudentInstallmentPlan;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.students.ClassInstPlanReport;
import kg.alex.spt.reports.students.InstallmentPlanPaymentsReport;
import kg.alex.spt.ui.StudentDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DbStudentInstallmentPlan extends BaseDb {

    public DbStudentInstallmentPlan() throws Exception {
        super();
    }

    public IndexedContainer execSQL_St_InstPLan(MyVaadinUI myUI, int stud_id, int year_id,
                                                StudentDefinitionView dw) throws SQLException {


        String sql = "SELECT ip.id, ip.amount, ip.date_of_payment, ip.is_visible "
                + "FROM student_installement_plan as ip "
                + "where ip.student_id = ? and ip.year_id = ? "
                + "order by ip.is_visible, ip.date_of_payment;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = dw.prepareInstPlanContainer();
        while (result.next()) {
            String id = result.getString("ip.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    dw.createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                            Settings.dbStudentInstallment, FontAwesome.MINUS_SQUARE));
            java.util.Date date = result.getDate("ip.date_of_payment");
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                    dw.createDateField(date, myUI.getMessage(SptMessages.Date), id, false, true));
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                    dw.createTextFieldDouble(result.getDouble("ip.amount"), 2, myUI.getMessage(SptMessages.Amount), id));
            item.getItemProperty(Settings.status_id)
                    .setValue(result.getInt("ip.is_visible"));
        }
        return container;
    }

    public int exec_insert(StudentInstallmentPlan ip) throws SQLException {
        String sql = "INSERT INTO student_installement_plan (student_id, year_id, "
                + "amount, date_of_payment, is_visible) "
                + "VALUES(?,?,?,?,1);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ip.getStudent_id());
        stat.setInt(2, ip.getYear_id());
        stat.setDouble(3, ip.getAmount());
        stat.setDate(4, new java.sql.Date(ip.getDate_of_payment().getTime()));
        return stat.executeUpdate();
    }

    public int exec_insert_notVisible(int st_id, int year_id, double amount)
            throws SQLException {
        String sql = "INSERT INTO student_installement_plan (student_id, year_id, "
                + "amount, date_of_payment, is_visible) "
                + "VALUES(?,?,?,NOW(),0);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        stat.setInt(2, year_id);
        stat.setDouble(3, amount);
        return stat.executeUpdate();
    }

    public int exec_delete(int stud_id, int year_id) throws SQLException {
        String sql = "DELETE FROM student_installement_plan WHERE student_id=? "
                + "and year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_InstPLan(MyVaadinUI myUI, int stud_id, int year_id,
                                             InstallmentPlanPaymentsReport ip)
            throws SQLException {


        String sql = "SELECT ip.id, ip.amount, ip.date_of_payment "
                + "FROM student_installement_plan as ip "
                + "where ip.student_id = ? and ip.year_id = ? "
                + "order by ip.is_visible, ip.date_of_payment;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Amount), Double.class, 0.0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("ip.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                    Settings.df.format((result.getDate("ip.date_of_payment"))));
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                    result.getDouble("ip.amount"));
            ip.total_inst += result.getDouble("ip.amount");
        }
        return container;
    }

    public IndexedContainer execSQL_InstPlanByClass(MyVaadinUI myUI, Date from,
                                                    Date till, int year_id, String class_ids, String edu_statuses_ids,
                                                    ClassInstPlanReport cip) throws SQLException {


        String sql = "SELECT ip.id, ip.date_of_payment, CONCAT(cnu.name, ' - ', cna.name) AS class_name, "
                + "st.name, st.surname, ip.amount, concat(sr.phone,' (',sr.fullname,')') as phone FROM student_installement_plan AS ip "
                + "LEFT JOIN student AS st ON st.id = ip.student_id "
                + "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN class_name AS cna ON cna.id = CASE "
                + "WHEN stud_o.to_class_name_id IS NULL THEN st.class_name_id "
                + "ELSE stud_o.to_class_name_id END "
                + "LEFT JOIN class_number AS cnu ON cna.class_number_id = cnu.id "
                + "LEFT JOIN education_status AS edu ON edu.id = "
                + "CASE WHEN stud_o.to_education_status_id IS NULL "
                + "THEN st.education_status_id ELSE stud_o.to_education_status_id END "
                + "left join student_relatives as sr on st.id = sr.student_id and sr.is_main = 1 "
                + "WHERE cna.id IN (" + class_ids + ") AND DATE(ip.date_of_payment) >= ? "
                + "AND DATE(ip.date_of_payment) <= ? AND ip.year_id = ? "
                + "AND edu.id IN (" + edu_statuses_ids + ") "
                + "ORDER BY cnu.id, cna.id, st.name, st.surname, ip.is_visible, ip.date_of_payment;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setDate(2, new java.sql.Date(from.getTime()));
        stat.setDate(3, new java.sql.Date(till.getTime()));
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Phone), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("ip.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.FirstName)).setValue(result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.LastName)).setValue(result.getString("st.surname"));
            item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(result.getString("class_name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(result.getDouble("ip.amount"));
            item.getItemProperty(myUI.getMessage(SptMessages.Phone)).setValue(result.getString("phone"));
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(Settings.df.format((result.getDate("ip.date_of_payment"))));
            cip.total += result.getDouble("ip.amount");
        }
        return container;
    }

    public String execGetWeeklyPlan(String students, int scl_id, int year_id)
            throws SQLException {
        String sql = "SELECT count(distinct st.id) as st, "
                + "ifnull(sum(ip.amount),0.00) as week_plan "
                + "FROM student_installement_plan as ip "
                + "left join student as st on st.id = ip.student_id "
                + "where st.school_id = ? and ip.year_id = ? and "
                + "yearweek(ip.date_of_payment,1) = YEARWEEK(CURDATE(),1);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        String s = null;
        while (result.next()) {
            s = result.getString("st") + " " + students + " / " + Settings.dFormat2.format(result.getDouble("week_plan"));
        }
        return s;
    }

    public String execGetMonthlyPlan(String students, int scl_id, int year_id)
            throws SQLException {

        String sql = "SELECT count(distinct st.id) as st, "
                + "ifnull(sum(ip.amount),0.00) as month_plan "
                + "FROM student_installement_plan as ip "
                + "left join student as st on st.id = ip.student_id "
                + "where st.school_id = ? and ip.year_id = ? and "
                + "MONTH(ip.date_of_payment) = MONTH(CURRENT_DATE());";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        String s = null;
        while (result.next()) {
            s = result.getString("st") + " " + students + " / " + Settings.dFormat2.format(result.getDouble("month_plan"));
        }
        return s;
    }
}
