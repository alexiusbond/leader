/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.StudentPayment;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.students.ClassPaymentsReport;
import kg.alex.spt.reports.students.InstallmentPlanPaymentsReport;
import kg.alex.spt.ui.StudentDefinitionView;
import kg.alex.spt.utils.ComboBoxMax;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class DbStudentPayment extends BaseDb {

    public DbStudentPayment() throws Exception {
        super();
    }

    public StudentPayment exec_recount_payment(int stud_id, int year_id) throws SQLException {
        String sql = "SELECT sum(if (sp.payment_category_id!=3,sp.amount,0.0)) - "
                + "sum(if (sp.payment_category_id=3,sp.amount,0.0)) as ttl_payment, "
                + "sum(if(sp.payment_category_id = 1, sp.amount, 0.0)) as init_payment "
                + "FROM student_payments as sp where sp.student_id = ? and year_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        StudentPayment sp = new StudentPayment();
        if (result.next()) {
            sp.setTtl_pay(result.getDouble("ttl_payment"));
            sp.setInit_pay(result.getDouble("init_payment"));
        }
        return sp;
    }

    public IndexedContainer execSQL_St_Payments(MyVaadinUI myUI, int stud_id, int year_id,
                                                StudentDefinitionView dw) throws SQLException {

        Subject currentUser = SecurityUtils.getSubject();
        String sql = "SELECT sp.id, sp.amount, sp.dollar_rate, sp.payment_type_id, sp.payment_category_id, "
                + "sp.who_paid, sp.note, sp.modification_date, "
                + "if(sp.modification_date <= DATE_SUB(NOW(), INTERVAL 24 HOUR),true, false) as isDisabled "
                + "FROM student_payments as sp "
                + "where sp.student_id=? and sp.year_id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = dw.preparePaymentsContainer();
        while (result.next()) {
            boolean isDisabled = false;
            if (!currentUser.isPermitted(SystemSettings.cnTransactionsView + ":" + SystemSettings.prmChangeOldTransactions)) {
                isDisabled = result.getBoolean("isDisabled");
            }
            String id = result.getString("sp.id");
            Item item = container.addItem(id);
            Button btn = dw.createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                    SystemSettings.dbStudentPayments, FontAwesome.MINUS_SQUARE);
            btn.setEnabled(!isDisabled);
            if (!currentUser.isPermitted(SystemSettings.paymentsTab + ":" + SystemSettings.actDelete)) {
                btn.setEnabled(false);
            }
            item.getItemProperty(SystemSettings.button).setValue(btn);
            item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
            if (!currentUser.isPermitted(SystemSettings.paymentsTab + ":" + SystemSettings.actModify)) {
                isDisabled = true;
            }
            ComboBoxMax cb = dw.createComboboxPayment(result.getInt("sp.payment_category_id"),
                    myUI.getMessage(SptMessages.PaymentCategoryType), id, true, false);
            cb.setId(myUI.getMessage(SptMessages.Payments));
            cb.setEnabled(!isDisabled);
            item.getItemProperty(myUI.getMessage(SptMessages.PaymentCategoryType)).setValue(cb);
            item.getItemProperty(myUI.getMessage(SptMessages.PaymentType)).setValue(
                    dw.createCombobox(result.getInt("sp.payment_type_id"), myUI.getMessage(SptMessages.PaymentType), id,
                            SystemSettings.dbPaymentType, false, false, false, isDisabled));
            TextField tf = dw.createTextfieldDouble(result.getDouble("sp.amount"), myUI.getMessage(SptMessages.Amount), id);
            tf.setId(myUI.getMessage(SptMessages.Payments));
            tf.setEnabled(!isDisabled);
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(tf);
            tf = dw.createTextfieldDouble(result.getDouble("sp.dollar_rate"), myUI.getMessage(SptMessages.Rate), id);
            tf.setEnabled(!isDisabled);
            item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(tf);
            tf = dw.createTextfield(result.getString("sp.who_paid"), myUI.getMessage(SptMessages.WhoPaid), id, false, false);
            tf.setEnabled(!isDisabled);
            item.getItemProperty(myUI.getMessage(SptMessages.WhoPaid)).setValue(tf);
            DateField df = dw.createDateField(result.getTimestamp("sp.modification_date"),
                    myUI.getMessage(SptMessages.Date), id, false, false);
            df.setId(myUI.getMessage(SptMessages.Payments));
            df.setEnabled(!isDisabled);
            if (currentUser.isPermitted(SystemSettings.cnTransactionsView + ":" + SystemSettings.prmChangeOldTransactions)) {
                df.setRangeStart(myUI.getUser().getTransactions_start_date());
            } else if (!isDisabled) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, -1441);
                df.setRangeStart(calendar.getTime());
                df.addValidator(new DateRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue),
                        df.getRangeStart(), df.getRangeEnd(), Resolution.MINUTE));
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(df);
            tf = dw.createTextfieldNote(result.getString("sp.note"), myUI.getMessage(SptMessages.Note), id);
            tf.setEnabled(!isDisabled);
            item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(tf);
            Button b = dw.createButton(myUI.getMessage(SptMessages.Print), id,
                    myUI.getMessage(SptMessages.Invoice), FontAwesome.PRINT);
            b.setEnabled(currentUser.isPermitted(SystemSettings.paymentsTab
                    + ":" + SystemSettings.actPrint));
            item.getItemProperty(myUI.getMessage(SptMessages.Print)).setValue(b);
            item.getItemProperty(SystemSettings.old_amount).setValue(result.getDouble("sp.amount"));
            item.getItemProperty(SystemSettings.old_date).setValue(result.getDate("sp.modification_date"));
            item.getItemProperty(SystemSettings.old_category).setValue(result.getInt("sp.payment_category_id"));
        }
        return container;
    }

    public int exec_update(StudentPayment sp) throws SQLException {
        String sql = "update student_payments set year_id=?, "
                + "amount=?, payment_type_id=?, payment_category_id=?, employee_id=?, "
                + "who_paid=?, note=?, modification_date=?, dollar_rate=? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sp.getYear_id());
        stat.setDouble(2, sp.getAmount());
        stat.setInt(3, sp.getPayment_type_id());
        stat.setInt(4, sp.getPayment_cat_type_id());
        stat.setInt(5, sp.getEmplooyee_id());
        stat.setString(6, sp.getWho_paid());
        stat.setString(7, sp.getNote());
        stat.setTimestamp(8, new java.sql.Timestamp(sp.getModification_date().getTime()));
        stat.setDouble(9, sp.getRate());
        stat.setInt(10, sp.getId());
        return stat.executeUpdate();
    }

    public int exec_insert(StudentPayment sp, int order_num) throws SQLException {
        String sql = "INSERT INTO student_payments (student_id, year_id, "
                + "amount, payment_type_id, payment_category_id, employee_id, "
                + "who_paid, order_number, note, modification_date, dollar_rate) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sp.getStudent_id());
        stat.setInt(2, sp.getYear_id());
        stat.setDouble(3, sp.getAmount());
        stat.setInt(4, sp.getPayment_type_id());
        stat.setInt(5, sp.getPayment_cat_type_id());
        stat.setInt(6, sp.getEmplooyee_id());
        stat.setString(7, sp.getWho_paid());
        stat.setInt(8, order_num);
        stat.setString(9, sp.getNote());
        stat.setTimestamp(10, new java.sql.Timestamp(sp.getModification_date().getTime()));
        stat.setDouble(11, sp.getRate());
        stat.executeUpdate();
        return getLastInsertedId();
    }

    public StudentPayment exec_get_init_payment(int st_id, int year_id) throws SQLException {
        StudentPayment sp = null;
        String sql = "SELECT sp.id, sp.amount, sp.modification_date, sp.dollar_rate, sp.student_id, sp.year_id, sp.payment_type_id," +
                "sp.payment_category_id, sp.who_paid, sp.note FROM student_payments as sp "
                + "where sp.student_id = ? and sp.year_id = ? and sp.payment_category_id = 1;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            sp = new StudentPayment();
            sp.setId(result.getInt("sp.id"));
            sp.setAmount(result.getDouble("sp.amount"));
            sp.setModification_date(result.getTimestamp("sp.modification_date"));
            sp.setRate(result.getDouble("sp.dollar_rate"));
            sp.setStudent_id(result.getInt("sp.student_id"));
            sp.setYear_id(result.getInt("sp.year_id"));
            sp.setPayment_type_id(result.getInt("sp.payment_type_id"));
            sp.setPayment_cat_type_id(result.getInt("sp.payment_category_id"));
            sp.setWho_paid(result.getString("sp.who_paid"));
            sp.setNote(result.getString("sp.note"));
        }
        return sp;
    }

    public int exec_delete(String id) throws SQLException {
        String sql = "DELETE FROM student_payments WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, id);
        return stat.executeUpdate();
    }

    public int getMaxOrderNum(int id) throws SQLException {
        int maxValue = 0;
        String sql = "select (max(sp.order_number)+1) as max_plus1 "
                + "from student_payments as sp "
                + "left join student as s on s.id = sp.student_id where "
                + "s.school_id = (SELECT school_id FROM student where id=?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            maxValue = (result.getInt("max_plus1"));
        } else {
            maxValue = 1;
        }
        return maxValue;
    }

    public String getOrderNum(String id) throws SQLException {
        String orderNum = "";
        String sql = "select order_number "
                + "from student_payments "
                + "where id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            orderNum = (result.getString("order_number"));
        } else {
            orderNum = "1";
        }
        return orderNum;
    }

    public IndexedContainer execSQL_Payment(MyVaadinUI myUI, int stud_id, int year_id,
                                            InstallmentPlanPaymentsReport ip) throws SQLException {


        String sql = "SELECT sp.id, sp.amount, sp.who_paid, sp.modification_date, "
                + "pc.id, pc.name FROM student_payments as sp "
                + "left join payment_category as pc on sp.payment_category_id = pc.id "
                + "where sp.student_id=? and sp.year_id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.WhoPaid), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.PaymentCategoryType), String.class, null);
        container.addContainerProperty(SystemSettings.payment_category_id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("sp.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                    SystemSettings.df.format((result.getDate("sp.modification_date"))));
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                    result.getDouble("sp.amount"));
            item.getItemProperty(myUI.getMessage(SptMessages.WhoPaid)).setValue(
                    result.getString("sp.who_paid"));
            item.getItemProperty(myUI.getMessage(SptMessages.PaymentCategoryType)).setValue(
                    result.getString("pc.name"));
            item.getItemProperty(SystemSettings.payment_category_id).setValue(
                    result.getInt("pc.id"));
            if (result.getInt("pc.id") != 3) {
                ip.total_pay += result.getDouble("sp.amount");
            } else {
                ip.total_pay -= result.getDouble("sp.amount");
            }
        }
        return container;
    }

    public IndexedContainer execSQL_PaymentsByClass(MyVaadinUI myUI, Date from,
                                                    Date till, int year_id, String class_ids, String edu_statuses_ids,
                                                    ClassPaymentsReport cpr) throws SQLException {


        String sql = "SELECT sp.id, sp.modification_date, CONCAT(cnu.name, ' - ', cna.name) AS class_name, "
                + "st.name, st.surname, sp.amount, pc.name, sp.who_paid, sp.payment_category_id "
                + "FROM student_payments AS sp "
                + "LEFT JOIN student AS st ON st.id = sp.student_id "
                + "LEFT JOIN "
                + "(SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN class_name AS cna ON cna.id = "
                + "CASE WHEN stud_o.to_class_name_id IS NULL "
                + "THEN st.class_name_id ELSE stud_o.to_class_name_id END "
                + "LEFT JOIN class_number AS cnu ON cna.class_number_id = cnu.id "
                + "LEFT JOIN education_status AS edu ON edu.id = "
                + "CASE WHEN stud_o.to_education_status_id IS NULL "
                + "THEN st.education_status_id ELSE stud_o.to_education_status_id END "
                + "LEFT JOIN payment_category AS pc ON sp.payment_category_id = pc.id "
                + "WHERE cna.id IN (" + class_ids + ") "
                + "AND DATE(sp.modification_date) >= ? AND DATE(sp.modification_date) <= ? "
                + "AND sp.year_id = ? AND edu.id IN (" + edu_statuses_ids + ") "
                + "ORDER BY cnu.id, cna.id, st.name, st.surname, sp.modification_date;";
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
        container.addContainerProperty(myUI.getMessage(SptMessages.PaymentCategoryType), String.class, null);
        container.addContainerProperty(SystemSettings.payment_category_id, Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(SptMessages.WhoPaid), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("sp.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.FirstName)).setValue(
                    result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.LastName)).setValue(
                    result.getString("st.surname"));
            item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                    result.getString("class_name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                    result.getDouble("sp.amount"));
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                    SystemSettings.df.format((result.getDate("sp.modification_date"))));
            item.getItemProperty(myUI.getMessage(SptMessages.PaymentCategoryType)).setValue(
                    result.getString("pc.name"));
            item.getItemProperty(SystemSettings.payment_category_id).setValue(
                    result.getInt("sp.payment_category_id"));
            item.getItemProperty(myUI.getMessage(SptMessages.WhoPaid)).setValue(
                    result.getString("sp.who_paid"));
            if (result.getInt("sp.payment_category_id") != 3) {
                cpr.total += result.getDouble("sp.amount");
            } else {
                cpr.total -= result.getDouble("sp.amount");
            }
        }
        return container;
    }

    public double exec_get_difference(int st_id, int year_id) throws SQLException {
        double ip = 0;
        String sql = "SELECT ifnull(sum(if(sp.payment_category_id != 3,sp.amount, 0.0)) - "
                + "sum(if(sp.payment_category_id = 3,sp.amount, 0.0)),0.0)  as total "
                + "FROM student_payments as sp where sp.student_id = ? and sp.year_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            ip = (result.getDouble("total"));
        }
        return ip;
    }

    public String execGetWeeklyPaid(String students, int scl_id, int year_id)
            throws SQLException {
        String sql = "SELECT count(distinct st.id) as st, "
                + "ifnull(sum(if(sp.payment_category_id = 3, -sp.amount, sp.amount)),0.00) as week_paid FROM student_payments as sp "
                + "left join student as st on st.id = sp.student_id "
                + "where st.school_id = ? and sp.year_id = ? and "
                + "yearweek(date(sp.modification_date), 1) = YEARWEEK(curdate(),1);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        String s = null;
        while (result.next()) {
            s = result.getString("st") + " " + students + " / " + SystemSettings.dFormat.format(result.getDouble("week_paid"));
        }
        return s;
    }

    public String execGetMonthlyPaid(String students, int scl_id, int year_id)
            throws SQLException {

        String sql = "SELECT count(distinct st.id) as st, "
                + "ifnull(sum(if(sp.payment_category_id = 3, -sp.amount, sp.amount)),0.00) as month_paid FROM student_payments as sp "
                + "left join student as st on st.id = sp.student_id "
                + "where st.school_id = ? and sp.year_id = ? and "
                + "MONTH(sp.modification_date) = MONTH(CURRENT_DATE());";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        String s = null;
        while (result.next()) {
            s = result.getString("st") + " " + students + " / " + SystemSettings.dFormat.format(result.getDouble("month_paid"));
        }
        return s;
    }

    public int exec_update_emp_id(int emp_id, String id) throws SQLException {
        String sql = "UPDATE student_payments SET employee_id=? "
                + "WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, emp_id);
        stat.setString(2, id);
        int status = stat.executeUpdate();
        return status;
    }
}
