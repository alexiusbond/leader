/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.domain.ContractTotal;
import kg.alex.spt.domain.StudentContract;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.students.ClassListReport;
import kg.alex.spt.reports.students.DebtReport;
import kg.alex.spt.reports.students.DiscountsReport;
import kg.alex.spt.reports.students.YearMonthReport;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author alex
 */
public class DbStudentContract extends BaseDb {

    public DbStudentContract() throws Exception {
        super();
    }

    public int exec_insert_st_contract(MyVaadinUI myUi, StudentContract c) throws SQLException {
        String sql = "INSERT INTO student_contract (student_id, year_id, contract_id, debt, employee_id, " +
                "modification_date, activity_status_id, contr_with_disc, contract_number) "
                + "VALUES(?,?,?,?,?,NOW(),?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, c.getStudent_id());
        stat.setInt(2, c.getYear_id());
        stat.setInt(3, c.getContract_id());
        stat.setDouble(4, c.getDebt());
        stat.setInt(5, c.getEmployee_id());
        stat.setInt(6, c.getStatus_id());
        stat.setDouble(7, c.getContr_with_disc());
        stat.setInt(8, exec_next_contract_number(myUi.getUser().getSchool_id(),
                myUi.getUser().getCurrent_year().getId()));
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update_st_contract(StudentContract c)
            throws SQLException {
        String sql = "UPDATE student_contract SET contract_id=?,debt=?,employee_id=?,"
                + "modification_date=NOW(),activity_status_id=?,contr_with_disc=? "
                + "where student_id=? and year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, c.getContract_id());
        stat.setDouble(2, c.getDebt());
        stat.setInt(3, c.getEmployee_id());
        stat.setInt(4, c.getStatus_id());
        stat.setDouble(5, c.getContr_with_disc());
        stat.setInt(6, c.getStudent_id());
        stat.setInt(7, c.getYear_id());
        return stat.executeUpdate();
    }

    public int exec_delete(int stud_id, int year_id) throws SQLException {
        String sql = "DELETE FROM student_contract WHERE student_id=? and year_id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        return stat.executeUpdate();
    }

    public StudentContract exec_recount_contract(int stud_id, int year_id) throws SQLException {
        String sql = "SELECT sc.contract_id, sc.debt, c.amount, sc.contr_with_disc, vc.details, vc.amount, "
                + "sum(ip.amount) as plan_debt FROM student_contract as sc "
                + "left join contract as c on c.id = sc.contract_id "
                + "LEFT JOIN view_corrections as vc on vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "LEFT JOIN student_installement_plan as ip on sc.student_id = ip.student_id "
                + "and sc.year_id = ip.year_id AND ((ip.is_visible=1 and ip.date_of_payment <= now()) or ip.is_visible=0) "
                + "where sc.student_id = ? and sc.year_id = ? ;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        StudentContract c = new StudentContract();
        if (result.next()) {
            c.setContract_id(result.getInt("sc.contract_id"));
            c.setDebt(result.getDouble("sc.debt"));
            c.setAmount(result.getDouble("c.amount"));
            c.setContr_with_disc(result.getDouble("sc.contr_with_disc"));
            c.setPlan_debt(result.getDouble("plan_debt"));
            c.setCorrection(result.getDouble("vc.amount"));
            c.setCorrectionDetails(result.getString("vc.details"));
        }
        return c;
    }

    public int exec_update_status(int student_id, int activity_status, int employee_id)
            throws SQLException {
        String sql = "UPDATE student_contract SET activity_status_id=?,"
                + "employee_id=?, modification_date=NOW() WHERE student_id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, activity_status);
        stat.setInt(2, employee_id);
        stat.setInt(3, student_id);
        return stat.executeUpdate();
    }

    public int exec_update_status_by_id(int student_id, int activity_status, int employee_id)
            throws SQLException {
        String sql = "UPDATE student_contract SET activity_status_id=?,"
                + "employee_id=?, modification_date=NOW() WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, activity_status);
        stat.setInt(2, employee_id);
        stat.setInt(3, exec_last_by_student_id(student_id));
        return stat.executeUpdate();
    }

    public int exec_last_by_student_id(int student_id) throws SQLException {
        int id = 0;
        String sql = "SELECT max(id) from student_contract where student_id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, student_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            if (!result.wasNull()) {
                id = result.getInt("max(id)");
            }
        }
        return id;
    }

    public int execSQL_get_st_contract(int st_id, int year_id) throws SQLException {
        String sql = "SELECT sc.contract_id FROM student_contract as sc "
                + "where sc.student_id = ? and sc.year_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        int contr_id = 0;
        if (result.next()) {
            contr_id = result.getInt("sc.contract_id");
        }
        return contr_id;
    }

    public Double exec_get_debt(int st_id, int year_id) throws SQLException {
        double debt = 0.0;
        String sql = "SELECT IFNULL((SELECT SUM(sc.contr_with_disc) FROM student_contract AS sc "
                + "WHERE sc.student_id = ? and sc.year_id < ?),0.0) - "
                + "IFNULL((SELECT SUM(if(sp.payment_category_id!=3,sp.amount,0)) - "
                + "sum(if(sp.payment_category_id=3,sp.amount,0)) "
                + "FROM student_payments AS sp "
                + "WHERE sp.student_id = ? and sp.year_id < ?), 0.0) + "
                + "IFNULL((SELECT SUM(vc.amount) FROM view_corrections AS vc "
                + "WHERE vc.student_id = ? and vc.year_id < ?), 0.0) AS debt";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        stat.setInt(2, year_id);
        stat.setInt(3, st_id);
        stat.setInt(4, year_id);
        stat.setInt(5, st_id);
        stat.setInt(6, year_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            if (!result.wasNull()) {
                debt = result.getDouble("debt");
            }
        }
        return debt;
    }

    public int execUpdateNetPayments(double ttl_pay, int stud_id, int year_id)
            throws SQLException {
        String sql = "update student_contract set net_payments = ? "
                + "where student_id = ? and year_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDouble(1, ttl_pay);
        stat.setInt(2, stud_id);
        stat.setInt(3, year_id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_ClassList(MyVaadinUI myUI, String class_ids, int year_id,
                                              String edu_statuses_ids, ClassListReport clr) throws SQLException {
        clr.activeStudents = 0;
        clr.discountedStudents = 0;
        clr.prevYearDebts = 0;
        clr.nets = 0;
        clr.paid_amounts = 0;
        clr.contracts = 0;
        clr.discounts = 0;
        clr.corrections = 0;
        clr.debts = 0;
        clr.overPays = 0;
        String sql = "SELECT st.id, st.login, st.name, st.surname, edu.name, c.amount, sc.debt, vc.amount, vc.full_details, "
                + "sc.contr_with_disc, sc.net_payments, edu.id, sr.fullname, sr.phone, "
                + "rel.name, GROUP_CONCAT(DISTINCT "
                + "CASE d.discount_type_id WHEN 1 THEN CONCAT(d.name, ' - ', d.amount, '%') "
                + "WHEN 2 THEN CONCAT(d.name, ' - ', d.amount, '$') "
                + "WHEN 3 THEN CONCAT(d.name, ' - ', sd.free_entry_amount, '%') "
                + "ELSE CONCAT(d.name, ' - ', sd.free_entry_amount, '$') END "
                + "ORDER BY sd.id SEPARATOR '; ') AS disc, "
                + "CONCAT(cl.name, ' - ', cln.name) AS class "
                + "FROM student AS st LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS edu ON edu.id = CASE "
                + "WHEN stud_o.to_education_status_id IS NULL THEN st.education_status_id "
                + "ELSE stud_o.to_education_status_id END "
                + "LEFT JOIN class_name AS cln ON cln.id = CASE "
                + "WHEN stud_o.to_class_name_id IS NULL THEN st.class_name_id "
                + "ELSE stud_o.to_class_name_id END "
                + "LEFT JOIN class_number AS cl ON cl.id = cln.class_number_id "
                + "LEFT JOIN student_contract AS sc ON sc.student_id = st.id AND sc.year_id = ? "
                + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "LEFT JOIN contract AS c ON c.id = sc.contract_id "
                + "LEFT JOIN student_discount AS sd ON sd.student_id = st.id AND sd.year_id = ? "
                + "LEFT JOIN discount AS d ON d.id = sd.discount_id "
                + "LEFT JOIN student_relatives AS sr ON st.id = sr.student_id AND sr.is_main=1 "
                + "LEFT JOIN relatives AS rel ON sr.relatives_id = rel.id "
                + "WHERE cln.id IN (" + class_ids + ") and st.entering_year_id <= ? "
                + "AND edu.id IN (" + edu_statuses_ids + ") "
                + "GROUP BY st.id ORDER BY cl.id, cln.id, st.name, st.surname;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Id), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.EducationStatus), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Contract), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.DiscountType), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Discount), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.CorrectionType), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Correction), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.PreviousYearDebt), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Net), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Paid), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Debt), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.OverPay), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Relative), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.FullName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Phone), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("st.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.EducationStatus)).setValue(
                    result.getString("edu.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Id)).setValue(
                    result.getString("st.login"));
            item.getItemProperty(myUI.getMessage(SptMessages.FirstName)).setValue(
                    result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.LastName)).setValue(
                    result.getString("st.surname"));
            item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                    result.getString("class"));
            if (result.getString("rel.name") != null) {
                item.getItemProperty(myUI.getMessage(SptMessages.Relative)).setValue(
                        result.getString("rel.name"));
                item.getItemProperty(myUI.getMessage(SptMessages.FullName)).setValue(
                        result.getString("sr.fullname"));
                item.getItemProperty(myUI.getMessage(SptMessages.Phone)).setValue(
                        result.getString("sr.phone"));
            }
            if (result.getDouble("c.amount") != 0.0) {
                item.getItemProperty(myUI.getMessage(SptMessages.Contract)).setValue(result.getDouble("c.amount"));
                clr.contracts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Contract)).getValue();
                if (result.getString("disc") != null) {
                    item.getItemProperty(myUI.getMessage(SptMessages.DiscountType)).setValue(result.getString("disc"));
                    item.getItemProperty(myUI.getMessage(SptMessages.Discount)).setValue(
                            result.getDouble("c.amount") - result.getDouble("sc.contr_with_disc"));
                    clr.discounts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Discount)).getValue();
                    clr.discountedStudents++;
                } else {
                    item.getItemProperty(myUI.getMessage(SptMessages.Discount)).setValue(0.0);
                }
                item.getItemProperty(myUI.getMessage(SptMessages.CorrectionType)).setValue(result.getString("vc.full_details"));
                item.getItemProperty(myUI.getMessage(SptMessages.Correction)).setValue(result.getDouble("vc.amount"));
                clr.corrections += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Correction)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.PreviousYearDebt)).setValue(result.getDouble("sc.debt"));
                clr.prevYearDebts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.PreviousYearDebt)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Net)).setValue(
                        result.getDouble("sc.contr_with_disc") + result.getDouble("sc.debt") + result.getDouble("vc.amount"));
                clr.nets += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Paid)).setValue(result.getDouble("sc.net_payments"));
                clr.paid_amounts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue();
                double debt = (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue()
                        - result.getDouble("sc.net_payments");
                if (debt >= 0) {
                    item.getItemProperty(myUI.getMessage(SptMessages.Debt)).setValue(debt);
                    item.getItemProperty(myUI.getMessage(SptMessages.OverPay)).setValue(0.0);
                } else {
                    item.getItemProperty(myUI.getMessage(SptMessages.Debt)).setValue(0.0);
                    item.getItemProperty(myUI.getMessage(SptMessages.OverPay)).setValue(-1 * debt);
                }
                clr.debts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Debt)).getValue();
                clr.overPays += (Double) item.getItemProperty(myUI.getMessage(SptMessages.OverPay)).getValue();
            }
            if (result.getInt("edu.id") == 2) {
                clr.activeStudents++;
            }
        }
        return container;
    }

    public IndexedContainer execSQL_Discounts(MyVaadinUI myUI, String class_ids,
                                              String discounts_ids, int year_id,
                                              String edu_statuses_ids, DiscountsReport dr) throws SQLException {
        dr.activeStudents = 0;
        dr.discountedStudents = 0;
        dr.debts = 0;
        dr.nets = 0;
        dr.paid_amounts = 0;
        dr.contracts = 0;
        dr.discounts = 0;
        dr.corrections = 0;
        dr.lefts = 0;
        String sql = "SELECT st.id, st.login, st.name, st.surname, edu.name, c.amount, sc.debt, vc.amount, vc.full_details, "
                + "sc.contr_with_disc, sc.net_payments, edu.id, GROUP_CONCAT(DISTINCT "
                + "CASE d.discount_type_id WHEN 1 THEN CONCAT(d.name, ' - ', d.amount, '%') "
                + "WHEN 2 THEN CONCAT(d.name, ' - ', d.amount, '$') "
                + "WHEN 3 THEN CONCAT(d.name, ' - ', sd.free_entry_amount, '%') "
                + "ELSE CONCAT(d.name, ' - ', sd.free_entry_amount, '$') END "
                + "ORDER BY sd.id SEPARATOR '; ') AS disc, "
                + "CONCAT(cl.name, ' - ', cln.name) AS class "
                + "FROM student AS st LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS edu ON edu.id = CASE "
                + "WHEN stud_o.to_education_status_id IS NULL THEN st.education_status_id "
                + "ELSE stud_o.to_education_status_id END "
                + "LEFT JOIN class_name AS cln ON cln.id = CASE "
                + "WHEN stud_o.to_class_name_id IS NULL THEN st.class_name_id "
                + "ELSE stud_o.to_class_name_id END "
                + "LEFT JOIN class_number AS cl ON cl.id = cln.class_number_id "
                + "LEFT JOIN student_contract AS sc ON sc.student_id = st.id AND sc.year_id = ? "
                + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "LEFT JOIN contract AS c ON c.id = sc.contract_id "
                + "LEFT JOIN student_discount AS sd ON sd.student_id = st.id AND sd.year_id = ? "
                + "LEFT JOIN discount AS d ON d.id = sd.discount_id "
                + "WHERE cln.id IN (" + class_ids + ") and st.entering_year_id <= ? "
                + "AND edu.id IN (" + edu_statuses_ids + ") "
                + "AND d.id IN (" + discounts_ids + ") "
                + "GROUP BY st.id ORDER BY disc, cl.id, cln.id, st.name, st.surname;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Id), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.EducationStatus), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Contract), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.DiscountType), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Discount), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.CorrectionType), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Correction), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.PreviousYearDebt), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Net), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Paid), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Left), Double.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("st.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.EducationStatus)).setValue(
                    result.getString("edu.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Id)).setValue(
                    result.getString("st.login"));
            item.getItemProperty(myUI.getMessage(SptMessages.FirstName)).setValue(
                    result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.LastName)).setValue(
                    result.getString("st.surname"));
            item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                    result.getString("class"));
            if (result.getDouble("c.amount") != 0.0) {
                item.getItemProperty(myUI.getMessage(SptMessages.Contract)).setValue(
                        result.getDouble("c.amount"));
                dr.contracts += (Double) item.getItemProperty(
                        myUI.getMessage(SptMessages.Contract)).getValue();
                if (result.getString("disc") != null) {
                    item.getItemProperty(myUI.getMessage(SptMessages.DiscountType)).setValue(
                            result.getString("disc"));
                    item.getItemProperty(myUI.getMessage(SptMessages.Discount)).setValue(
                            result.getDouble("c.amount") - result.getDouble("sc.contr_with_disc"));
                    dr.discounts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Discount)).getValue();
                    dr.discountedStudents++;
                } else {
                    item.getItemProperty(myUI.getMessage(SptMessages.Discount)).setValue(0.0);
                }
                item.getItemProperty(myUI.getMessage(SptMessages.CorrectionType)).setValue(
                        result.getString("vc.full_details"));
                item.getItemProperty(myUI.getMessage(SptMessages.Correction)).setValue(
                        result.getDouble("vc.amount"));
                dr.corrections += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Correction)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.PreviousYearDebt)).setValue(
                        result.getDouble("sc.debt"));
                dr.debts += (Double) item.getItemProperty(
                        myUI.getMessage(SptMessages.PreviousYearDebt)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Net)).setValue(
                        result.getDouble("sc.contr_with_disc") + result.getDouble("sc.debt") + result.getDouble("vc.amount"));
                dr.nets += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Paid)).setValue(
                        result.getDouble("sc.net_payments"));
                dr.paid_amounts += (Double) item.getItemProperty(
                        myUI.getMessage(SptMessages.Paid)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Left)).setValue(
                        (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue()
                                - result.getDouble("sc.net_payments"));
                dr.lefts += (Double) item.getItemProperty(
                        myUI.getMessage(SptMessages.Left)).getValue();
            }
            if (result.getInt("edu.id") == 2) {
                dr.activeStudents++;
            }
        }
        return container;
    }

    public void execSQL_Yearly_by_classes(MyVaadinUI myUI, String school_ids,
                                          String edu_statuses_ids, int year_id, YearMonthReport ymr) throws SQLException {

        String sql = "SELECT sch.id, sch.name_ru, sch.code, CONCAT(cln.name, ' - ', cl.name) AS class, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), c.amount, 0)) AS contr, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), "
                + "(c.amount - sc.contr_with_disc), 0)) AS disc, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), vc.amount, 0)) AS corrections, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), "
                + "sc.contr_with_disc, 0)) AS contr_with_disc, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), sc.debt, 0)) AS debts, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), sc.net_payments, 0)) "
                + "AS payments, COUNT(st.t_st_id) AS stud_num, "
                + "COUNT(IF(st.t_edu_id = 2, 1, NULL)) AS active FROM class_name AS cl "
                + "LEFT JOIN class_number AS cln ON cln.id = cl.class_number_id "
                + "LEFT JOIN school AS sch ON cl.school_id = sch.id "
                + "LEFT JOIN (SELECT cl.id AS t_class_id, es.id AS t_edu_id, stud.id AS t_st_id "
                + "FROM student AS stud "
                + "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON stud.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS es ON es.id = CASE "
                + "WHEN stud_o.to_education_status_id IS NULL THEN stud.education_status_id "
                + "ELSE stud_o.to_education_status_id END "
                + "LEFT JOIN class_name AS cl ON cl.id = CASE "
                + "WHEN stud_o.to_class_name_id IS NULL THEN stud.class_name_id "
                + "ELSE stud_o.to_class_name_id END WHERE stud.school_id IN (" + school_ids + ") "
                + "AND stud.entering_year_id <= ?) AS st ON cl.id = st.t_class_id "
                + "LEFT JOIN student_contract AS sc ON sc.student_id = st.t_st_id AND sc.year_id = ? "
                + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "LEFT JOIN contract AS c ON sc.contract_id = c.id "
                + "WHERE cl.school_id IN (" + school_ids + ") GROUP BY cl.id "
                + "ORDER BY CAST(sch.code AS UNSIGNED), cln.id, cl.id;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        ResultSet result = stat.executeQuery();
        int school_id = 0;
        Table t = null;
        int i = 0;
        ymr.clearLayout();
        while (result.next()) {
            if (school_id != result.getInt("sch.id")) {
                if (t != null) {
                    t.setColumnFooter(myUI.getMessage(SptMessages.Total_Active),
                            ymr.totalStudents + "/" + ymr.totalActive);
                    t.setColumnFooter(myUI.getMessage(SptMessages.Contract),
                            Settings.dFormat.format(ymr.contracts));
                    t.setColumnFooter(myUI.getMessage(SptMessages.Discount),
                            Settings.dFormat.format(ymr.discounts));
                    if (ymr.contracts != 0) {
                        t.setColumnFooter(myUI.getMessage(SptMessages.DiscountPercentage),
                                Settings.dFormat.format((100 * ymr.discounts) / ymr.contracts));
                    }
                    t.setColumnFooter(myUI.getMessage(SptMessages.Correction),
                            Settings.dFormat.format(ymr.corrections));
                    t.setColumnFooter(myUI.getMessage(SptMessages.PreviousYearDebt),
                            Settings.dFormat.format(ymr.debts));
                    t.setColumnFooter(myUI.getMessage(SptMessages.Net),
                            Settings.dFormat.format(ymr.nets));
                    t.setColumnFooter(myUI.getMessage(SptMessages.Paid),
                            Settings.dFormat.format(ymr.paid_amounts));
                    t.setColumnFooter(myUI.getMessage(SptMessages.Left),
                            Settings.dFormat.format(ymr.lefts));
                    if (ymr.nets != 0.0) {
                        t.setColumnFooter(Settings.percentage, Settings.dFormat.format(ymr.paid_amounts * 100 / ymr.nets));
                    }
                    ymr.totalStudents = 0;
                    ymr.totalActive = 0;
                    ymr.contracts = 0.0;
                    ymr.discounts = 0.0;
                    ymr.debts = 0.0;
                    ymr.corrections = 0.0;
                    ymr.nets = 0.0;
                    ymr.paid_amounts = 0.0;
                    ymr.lefts = 0.0;
                }
                t = ymr.createTable(result.getString("sch.code")
                        + " - " + result.getString("sch.name_ru"));
                ymr.rightLay.addComponent(t);
                school_id = result.getInt("sch.id");
            }
            if (t != null) {
                Item item = t.getContainerDataSource().addItem(i++);
                item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                        result.getString("class"));
                item.getItemProperty(myUI.getMessage(SptMessages.Total_Active)).setValue(
                        result.getInt("stud_num") + "/" + result.getInt("active"));
                ymr.totalStudents += result.getInt("stud_num");
                ymr.totalActive += result.getInt("active");
                item.getItemProperty(myUI.getMessage(SptMessages.Contract)).setValue(
                        result.getDouble("contr"));
                ymr.contracts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Contract)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Discount)).setValue(
                        result.getDouble("disc"));
                if (result.getDouble("contr") != 0) {
                    item.getItemProperty(myUI.getMessage(SptMessages.DiscountPercentage)).setValue(
                            (100 * result.getDouble("disc")) / result.getDouble("contr"));
                }
                ymr.discounts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Discount)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Correction)).setValue(
                        result.getDouble("corrections"));
                ymr.corrections += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Correction)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.PreviousYearDebt)).setValue(
                        result.getDouble("debts"));
                ymr.debts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.PreviousYearDebt)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Net)).setValue(
                        result.getDouble("contr_with_disc") + result.getDouble("debts") + result.getDouble("corrections"));
                ymr.nets += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Paid)).setValue(
                        result.getDouble("payments"));
                ymr.paid_amounts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Left)).setValue(
                        (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue()
                                - result.getDouble("payments"));
                ymr.lefts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Left)).getValue();
                if ((Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue() != 0.0) {
                    item.getItemProperty(Settings.percentage).setValue((Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue() * 100
                            / (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue());
                }
            }
        }
        if (t != null) {
            t.setColumnFooter(myUI.getMessage(SptMessages.Total_Active),
                    ymr.totalStudents + "/" + ymr.totalActive);
            t.setColumnFooter(myUI.getMessage(SptMessages.Contract),
                    Settings.dFormat.format(ymr.contracts));
            t.setColumnFooter(myUI.getMessage(SptMessages.Discount),
                    Settings.dFormat.format(ymr.discounts));
            if (ymr.contracts != 0) {
                t.setColumnFooter(myUI.getMessage(SptMessages.DiscountPercentage),
                        Settings.dFormat.format((100 * ymr.discounts) / ymr.contracts));
            }
            t.setColumnFooter(myUI.getMessage(SptMessages.PreviousYearDebt),
                    Settings.dFormat.format(ymr.debts));
            t.setColumnFooter(myUI.getMessage(SptMessages.Correction),
                    Settings.dFormat.format(ymr.corrections));
            t.setColumnFooter(myUI.getMessage(SptMessages.Net),
                    Settings.dFormat.format(ymr.nets));
            t.setColumnFooter(myUI.getMessage(SptMessages.Paid),
                    Settings.dFormat.format(ymr.paid_amounts));
            t.setColumnFooter(myUI.getMessage(SptMessages.Left),
                    Settings.dFormat.format(ymr.lefts));
            if (ymr.nets != 0.0) {
                t.setColumnFooter(Settings.percentage, Settings.dFormat.format(
                        ymr.paid_amounts * 100 / ymr.nets));
            }
            ymr.totalStudents = 0;
            ymr.totalActive = 0;
            ymr.contracts = 0.0;
            ymr.discounts = 0.0;
            ymr.debts = 0.0;
            ymr.corrections = 0.0;
            ymr.nets = 0.0;
            ymr.paid_amounts = 0.0;
            ymr.lefts = 0.0;
        }
        if (ymr.rightLay.getComponentCount() != 0) {
            execSQL_Yearly_by_class_numbers(myUI, school_ids, edu_statuses_ids, year_id, ymr);
        }
    }

    public void execSQL_Monthly_by_classes(MyVaadinUI myUI, String school_ids,
                                           String edu_statuses_ids, int year_id, YearMonthReport ymr) throws SQLException {


        String sql = "SELECT months.name, months.id, s_temp.id, s_temp.name_ru, s_temp.code, i_temp.amn, p_temp.amn FROM months "
                + "CROSS JOIN school AS s_temp LEFT JOIN "
                + "(SELECT sch.id AS s_id, sch.name_ru AS s_name, SUM(inst.amount) AS amn, MONTH(inst.date_of_payment) AS mnth "
                + "FROM student_installement_plan AS inst LEFT JOIN student AS st ON st.id = inst.student_id "
                + "LEFT JOIN school AS sch ON sch.id = st.school_id LEFT JOIN (SELECT MAX(so.id) AS oid, "
                + "so.student_id AS stud_id FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS edu ON edu.id = CASE "
                + "WHEN stud_o.to_education_status_id IS NULL THEN st.education_status_id "
                + "ELSE stud_o.to_education_status_id END WHERE inst.year_id = ? "
                + "AND edu.id IN (" + edu_statuses_ids + ") GROUP BY sch.id , MONTH(inst.date_of_payment)) AS i_temp "
                + "ON i_temp.mnth = months.id AND s_temp.id = i_temp.s_id "
                + "LEFT JOIN (SELECT sch.id AS s_id, SUM(IF(pay.payment_category_id = 3, - pay.amount, pay.amount)) AS amn, "
                + "MONTH(pay.modification_date) AS mnth FROM student_payments AS pay "
                + "LEFT JOIN student AS st ON st.id = pay.student_id LEFT JOIN school AS sch ON sch.id = st.school_id "
                + "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id FROM student_orders AS so "
                + "WHERE so.year_id = ? AND so.is_valid = 1 GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid LEFT JOIN education_status AS edu "
                + "ON edu.id = CASE WHEN stud_o.to_education_status_id IS NULL THEN st.education_status_id "
                + "ELSE stud_o.to_education_status_id END WHERE pay.year_id = ? AND edu.id IN (" + edu_statuses_ids + ") "
                + "GROUP BY sch.id , MONTH(pay.modification_date)) AS p_temp ON p_temp.mnth = months.id "
                + "AND s_temp.id = p_temp.s_id WHERE s_temp.id IN (" + school_ids + ") ORDER BY  CAST(s_temp.code AS UNSIGNED), months.order_num;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        int school_id = 0;
        Table t = null;
        int i = 0;
        ymr.clearLayout();
        while (result.next()) {
            if (school_id != result.getInt("s_temp.id")) {
                if (t != null) {
                    t.setColumnFooter(myUI.getMessage(SptMessages.InstPlanDebt),
                            Settings.dFormat.format(ymr.inst_plans));
                    t.setColumnFooter(myUI.getMessage(SptMessages.Paid),
                            Settings.dFormat.format(ymr.paid_amounts));
                    t.setColumnFooter(myUI.getMessage(SptMessages.Left),
                            Settings.dFormat.format(ymr.lefts));
                    if (ymr.inst_plans != 0.0) {
                        t.setColumnFooter(Settings.percentage, Settings.dFormat.format(
                                ymr.paid_amounts * 100 / ymr.inst_plans));
                    }
                    ymr.inst_plans = 0.0;
                    ymr.paid_amounts = 0.0;
                    ymr.lefts = 0.0;
                }
                t = ymr.createTable(result.getString("s_temp.code") + " - " + result.getString("s_temp.name_ru"));
                ymr.rightLay.addComponent(t);
                school_id = result.getInt("s_temp.id");
            }
            if (t != null) {
                Item item = t.getContainerDataSource().addItem(i++);
                item.getItemProperty(myUI.getMessage(SptMessages.Month)).setValue(
                        result.getString("months.name"));
                item.getItemProperty(myUI.getMessage(SptMessages.InstPlanDebt)).setValue(
                        result.getDouble("i_temp.amn"));
                ymr.inst_plans += (Double) item.getItemProperty(
                        myUI.getMessage(SptMessages.InstPlanDebt)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Paid)).setValue(
                        result.getDouble("p_temp.amn"));
                ymr.paid_amounts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Left)).setValue(
                        (Double) item.getItemProperty(myUI.getMessage(SptMessages.InstPlanDebt)).getValue()
                                - (Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue());
                ymr.lefts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Left)).getValue();
                if ((Double) item.getItemProperty(myUI.getMessage(SptMessages.InstPlanDebt)).getValue() != 0.0) {
                    item.getItemProperty(Settings.percentage).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue() * 100.0
                                    / (Double) item.getItemProperty(myUI.getMessage(SptMessages.InstPlanDebt)).getValue());
                }
            }
        }
        if (t != null) {
            t.setColumnFooter(myUI.getMessage(SptMessages.InstPlanDebt),
                    Settings.dFormat.format(ymr.inst_plans));
            t.setColumnFooter(myUI.getMessage(SptMessages.Paid),
                    Settings.dFormat.format(ymr.paid_amounts));
            t.setColumnFooter(myUI.getMessage(SptMessages.Left),
                    Settings.dFormat.format(ymr.lefts));
            if (ymr.inst_plans != 0.0) {
                t.setColumnFooter(Settings.percentage, Settings.dFormat.format(
                        ymr.paid_amounts * 100 / ymr.inst_plans));
            }
            ymr.inst_plans = 0.0;
            ymr.paid_amounts = 0.0;
            ymr.lefts = 0.0;
        }
        if (ymr.rightLay.getComponentCount() != 0) {
            execSQL_Monthly(myUI, school_ids, edu_statuses_ids, year_id, ymr);
        }
    }

    public void execSQL_Yearly_by_class_numbers(MyVaadinUI myUI, String school_ids,
                                                String edu_statuses_ids, int year_id, YearMonthReport ymr) throws SQLException {

        String sql = "SELECT cln.name AS class, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), c.amount, 0)) AS contr, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), "
                + "(c.amount - sc.contr_with_disc), 0)) AS disc, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), "
                + "sc.contr_with_disc, 0)) AS contr_with_disc, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), vc.amount, 0)) AS corrections, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), sc.debt, 0)) AS debts, "
                + "SUM(IF(st.t_edu_id IN (" + edu_statuses_ids + "), sc.net_payments,0)) "
                + "AS payments, COUNT(st.t_st_id) AS stud_num, "
                + "COUNT(IF(st.t_edu_id = 2, 1, NULL)) AS active FROM class_name AS cl "
                + "LEFT JOIN class_number AS cln ON cln.id = cl.class_number_id "
                + "LEFT JOIN (SELECT cl.id AS t_class_id, es.id AS t_edu_id, stud.id AS t_st_id "
                + "FROM student AS stud "
                + "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON stud.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS es ON es.id = CASE "
                + "WHEN stud_o.to_education_status_id IS NULL THEN stud.education_status_id "
                + "ELSE stud_o.to_education_status_id END "
                + "LEFT JOIN class_name AS cl ON cl.id = CASE "
                + "WHEN stud_o.to_class_name_id IS NULL THEN stud.class_name_id "
                + "ELSE stud_o.to_class_name_id END WHERE stud.school_id IN (" + school_ids + ") "
                + "AND stud.entering_year_id <= ?) AS st ON cl.id = st.t_class_id "
                + "LEFT JOIN student_contract AS sc ON sc.student_id = st.t_st_id AND sc.year_id = ? "
                + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "LEFT JOIN contract AS c ON sc.contract_id = c.id "
                + "WHERE cl.school_id IN (" + school_ids + ") GROUP BY cln.id "
                + "ORDER BY cln.id;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        ResultSet result = stat.executeQuery();
        Table t;
        int i = 0;
        t = ymr.createTable(myUI.getMessage(SptMessages.Total));
        ymr.rightLay.addComponent(t);
        while (result.next()) {
            if (t != null) {
                Item item = t.getContainerDataSource().addItem(i++);
                item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                        result.getString("class"));
                item.getItemProperty(myUI.getMessage(SptMessages.Total_Active)).setValue(
                        result.getInt("stud_num") + "/" + result.getInt("active"));
                ymr.totalStudents += result.getInt("stud_num");
                ymr.totalActive += result.getInt("active");
                item.getItemProperty(myUI.getMessage(SptMessages.Contract)).setValue(
                        result.getDouble("contr"));
                ymr.contracts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Contract)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Correction)).setValue(
                        result.getDouble("corrections"));
                ymr.corrections += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Correction)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Discount)).setValue(
                        result.getDouble("disc"));
                if (result.getDouble("contr") != 0) {
                    item.getItemProperty(myUI.getMessage(SptMessages.DiscountPercentage)).setValue(
                            (100 * result.getDouble("disc")) / result.getDouble("contr"));
                }
                ymr.discounts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Discount)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.PreviousYearDebt)).setValue(
                        result.getDouble("debts"));
                ymr.debts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.PreviousYearDebt)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Net)).setValue(
                        result.getDouble("contr_with_disc") + result.getDouble("debts") + result.getDouble("corrections"));
                ymr.nets += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Paid)).setValue(
                        result.getDouble("payments"));
                ymr.paid_amounts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Left)).setValue(
                        (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue()
                                - result.getDouble("payments"));
                ymr.lefts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Left)).getValue();
                if ((Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue() != 0.0) {
                    item.getItemProperty(Settings.percentage).setValue((Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue() * 100
                            / (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue());
                }
            }
        }
        if (t != null) {
            t.setColumnFooter(myUI.getMessage(SptMessages.Total_Active),
                    ymr.totalStudents + "/" + ymr.totalActive);
            t.setColumnFooter(myUI.getMessage(SptMessages.Contract),
                    Settings.dFormat.format(ymr.contracts));
            t.setColumnFooter(myUI.getMessage(SptMessages.Correction),
                    Settings.dFormat.format(ymr.corrections));
            t.setColumnFooter(myUI.getMessage(SptMessages.Discount),
                    Settings.dFormat.format(ymr.discounts));
            if (ymr.contracts != 0) {
                t.setColumnFooter(myUI.getMessage(SptMessages.DiscountPercentage),
                        Settings.dFormat.format((100 * ymr.discounts) / ymr.contracts));
            }
            t.setColumnFooter(myUI.getMessage(SptMessages.PreviousYearDebt),
                    Settings.dFormat.format(ymr.debts));
            t.setColumnFooter(myUI.getMessage(SptMessages.Net),
                    Settings.dFormat.format(ymr.nets));
            t.setColumnFooter(myUI.getMessage(SptMessages.Paid),
                    Settings.dFormat.format(ymr.paid_amounts));
            t.setColumnFooter(myUI.getMessage(SptMessages.Left),
                    Settings.dFormat.format(ymr.lefts));
            if (ymr.nets != 0.0) {
                t.setColumnFooter(Settings.percentage, Settings.dFormat.format(
                        ymr.paid_amounts * 100 / ymr.nets));
            }
            ymr.totalStudents = 0;
            ymr.totalActive = 0;
            ymr.contracts = 0.0;
            ymr.corrections = 0.0;
            ymr.discounts = 0.0;
            ymr.debts = 0.0;
            ymr.nets = 0.0;
            ymr.paid_amounts = 0.0;
            ymr.lefts = 0.0;
        }
    }

    public void execSQL_Summary_report(MyVaadinUI myUI, String school_ids,
                                       String edu_statuses_ids, int year_id, YearMonthReport ymr) throws SQLException {

        String sql = "SELECT sch.name_ru, sch.code, "
                + "SUM(IF(es.id IN (" + edu_statuses_ids + "), c.amount, 0)) AS contr, "
                + "SUM(IF(es.id IN (" + edu_statuses_ids + "), "
                + "(c.amount - sc.contr_with_disc), 0)) AS disc, "
                + "SUM(IF(es.id IN (" + edu_statuses_ids + "), sc.contr_with_disc, 0)) "
                + "AS contr_with_disc, "
                + "SUM(IF(es.id IN (" + edu_statuses_ids + "), vc.amount, 0)) AS corrections, "
                + "SUM(IF(es.id IN (" + edu_statuses_ids + "), sc.debt,0)) AS debts, "
                + "SUM(IF(es.id IN (" + edu_statuses_ids + "), sc.net_payments,0)) "
                + "AS payments, COUNT(st.id) AS stud_num, "
                + "COUNT(IF(es.id = 2, 1, NULL)) AS active FROM school AS sch "
                + "LEFT JOIN student AS st ON sch.id = st.school_id "
                + "LEFT JOIN "
                + "(SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? and so.is_valid=1 "
                + "GROUP BY so.student_id) "
                + "AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS es ON es.id = stud_o.to_education_status_id "
                + "LEFT JOIN student_contract AS sc ON sc.student_id = st.id AND sc.year_id = ? "
                + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "LEFT JOIN contract AS c ON sc.contract_id = c.id "
                + "WHERE st.entering_year_id <= ? "
                + "OR st.entering_year_id IS NULL GROUP BY sch.id "
                + "HAVING sch.id in (" + school_ids + ") ORDER BY CAST(sch.code AS UNSIGNED);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        ResultSet result = stat.executeQuery();
        Table t;
        int i = 0;
        ymr.clearLayout();
        t = ymr.createTable(myUI.getMessage(SptMessages.Total));
        ymr.rightLay.addComponent(t);
        while (result.next()) {
            if (t != null) {
                Item item = t.getContainerDataSource().addItem(i++);
                item.getItemProperty(myUI.getMessage(SptMessages.School)).setValue(
                        result.getString("sch.name_ru"));
                item.getItemProperty(myUI.getMessage(SptMessages.Total_Active)).setValue(
                        result.getInt("stud_num") + "/" + result.getInt("active"));
                ymr.totalStudents += result.getInt("stud_num");
                ymr.totalActive += result.getInt("active");
                item.getItemProperty(myUI.getMessage(SptMessages.Contract)).setValue(
                        result.getDouble("contr"));
                ymr.contracts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Contract)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Correction)).setValue(
                        result.getDouble("corrections"));
                ymr.corrections += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Correction)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Discount)).setValue(
                        result.getDouble("disc"));
                if (result.getDouble("contr") != 0) {
                    item.getItemProperty(myUI.getMessage(SptMessages.DiscountPercentage)).setValue(
                            result.getDouble("disc") * 100 / result.getDouble("contr"));
                }
                ymr.discounts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Discount)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.PreviousYearDebt)).setValue(
                        result.getDouble("debts"));
                ymr.debts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.PreviousYearDebt)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Net)).setValue(
                        result.getDouble("contr_with_disc") + result.getDouble("debts"));
                ymr.nets += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Paid)).setValue(
                        result.getDouble("payments"));
                ymr.paid_amounts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Left)).setValue(
                        (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue()
                                - result.getDouble("payments"));
                ymr.lefts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Left)).getValue();
                if ((Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue() != 0.0) {
                    item.getItemProperty(Settings.percentage).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue() * 100
                                    / (Double) item.getItemProperty(myUI.getMessage(SptMessages.Net)).getValue());
                }
            }
        }
        if (t != null) {
            t.setColumnFooter(myUI.getMessage(SptMessages.Total_Active),
                    ymr.totalStudents + "/" + ymr.totalActive);
            t.setColumnFooter(myUI.getMessage(SptMessages.Contract),
                    Settings.dFormat.format(ymr.contracts));
            t.setColumnFooter(myUI.getMessage(SptMessages.Correction),
                    Settings.dFormat.format(ymr.corrections));
            t.setColumnFooter(myUI.getMessage(SptMessages.Discount),
                    Settings.dFormat.format(ymr.discounts));
            if (ymr.contracts != 0) {
                t.setColumnFooter(myUI.getMessage(SptMessages.DiscountPercentage),
                        Settings.dFormat.format(ymr.discounts * 100 / ymr.contracts));
            }
            t.setColumnFooter(myUI.getMessage(SptMessages.PreviousYearDebt),
                    Settings.dFormat.format(ymr.debts));
            t.setColumnFooter(myUI.getMessage(SptMessages.Net),
                    Settings.dFormat.format(ymr.nets));
            t.setColumnFooter(myUI.getMessage(SptMessages.Paid),
                    Settings.dFormat.format(ymr.paid_amounts));
            t.setColumnFooter(myUI.getMessage(SptMessages.Left),
                    Settings.dFormat.format(ymr.lefts));
            if (ymr.nets != 0.0) {
                t.setColumnFooter(Settings.percentage, Settings.dFormat.format(
                        ymr.paid_amounts * 100 / ymr.nets));
            }
            ymr.totalStudents = 0;
            ymr.totalActive = 0;
            ymr.contracts = 0.0;
            ymr.corrections = 0.0;
            ymr.discounts = 0.0;
            ymr.debts = 0.0;
            ymr.nets = 0.0;
            ymr.paid_amounts = 0.0;
            ymr.lefts = 0.0;
        }
    }

    public void execSQL_Monthly(MyVaadinUI myUI, String school_ids, String edu_statuses_ids,
                                int year_id, YearMonthReport ymr) throws SQLException {

        String sql = "select months.name, months.id, i_temp.amn, p_temp.amn FROM months "
                + "left join ("
                + "select sum(inst.amount) as amn, month(inst.date_of_payment) as mnth "
                + "from student_installement_plan as inst "
                + "left join student as st on st.id=inst.student_id "
                + "left join school as sch on sch.id=st.school_id "
                + "LEFT JOIN "
                + "(SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS edu ON edu.id = "
                + "CASE WHEN stud_o.to_education_status_id IS NULL "
                + "THEN st.education_status_id ELSE stud_o.to_education_status_id END "
                + "where inst.year_id=? and sch.id in (" + school_ids + ") "
                + "AND edu.id IN (" + edu_statuses_ids + ") "
                + "group by month(inst.date_of_payment)) as i_temp on i_temp.mnth=months.id "
                + "left join ("
                + "select sum(if(pay.payment_category_id=3,-pay.amount,pay.amount)) as amn, "
                + "month(pay.modification_date) as mnth from student_payments as pay "
                + "left join student as st on st.id=pay.student_id "
                + "left join school as sch on sch.id=st.school_id "
                + "LEFT JOIN "
                + "(SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS edu ON edu.id = "
                + "CASE WHEN stud_o.to_education_status_id IS NULL "
                + "THEN st.education_status_id ELSE stud_o.to_education_status_id END "
                + "where pay.year_id=? and sch.id in (" + school_ids + ") "
                + "AND edu.id IN (" + edu_statuses_ids + ") "
                + "group by month(pay.modification_date)) as p_temp on p_temp.mnth=months.id "
                + "order by months.order_num;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        Table t;
        int i = 0;
        t = ymr.createTable(myUI.getMessage(SptMessages.Total));
        ymr.rightLay.addComponent(t);
        while (result.next()) {
            if (t != null) {
                Item item = t.getContainerDataSource().addItem(i++);
                item.getItemProperty(myUI.getMessage(SptMessages.Month)).setValue(
                        result.getString("months.name"));
                item.getItemProperty(myUI.getMessage(SptMessages.InstPlanDebt)).setValue(
                        result.getDouble("i_temp.amn"));
                ymr.inst_plans += (Double) item.getItemProperty(
                        myUI.getMessage(SptMessages.InstPlanDebt)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Paid)).setValue(
                        result.getDouble("p_temp.amn"));
                ymr.paid_amounts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue();
                item.getItemProperty(myUI.getMessage(SptMessages.Left)).setValue(
                        (Double) item.getItemProperty(myUI.getMessage(SptMessages.InstPlanDebt)).getValue()
                                - (Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue());
                ymr.lefts += (Double) item.getItemProperty(myUI.getMessage(SptMessages.Left)).getValue();
                if ((Double) item.getItemProperty(myUI.getMessage(SptMessages.InstPlanDebt)).getValue() != 0.0) {
                    item.getItemProperty(Settings.percentage).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(SptMessages.Paid)).getValue() * 100
                                    / (Double) item.getItemProperty(myUI.getMessage(SptMessages.InstPlanDebt)).getValue());
                }
            }
        }
        if (t != null) {
            t.setColumnFooter(myUI.getMessage(SptMessages.InstPlanDebt),
                    Settings.dFormat.format(ymr.nets));
            t.setColumnFooter(myUI.getMessage(SptMessages.Paid),
                    Settings.dFormat.format(ymr.paid_amounts));
            t.setColumnFooter(myUI.getMessage(SptMessages.Left),
                    Settings.dFormat.format(ymr.lefts));
            t.setColumnFooter(myUI.getMessage(SptMessages.InstPlanDebt),
                    Settings.dFormat.format(ymr.inst_plans));
            if (ymr.inst_plans != 0.0) {
                t.setColumnFooter(Settings.percentage, Settings.dFormat.format(
                        ymr.paid_amounts * 100 / ymr.inst_plans));
            }
            ymr.inst_plans = 0.0;
            ymr.paid_amounts = 0.0;
            ymr.lefts = 0.0;
        }
    }

    public int exec_contr_count(int id) throws SQLException {
        String sql = "SELECT count(contract_id) FROM student_contract "
                + "where contract_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        int i = 0;
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            i = result.getInt("count(contract_id)");
        }
        return i;
    }

    public ContractTotal execSQLTotals(int scl_id, int year_id)
            throws SQLException {
        String sql = "SELECT sum(c.amount) as contract, sum(sc.debt) as debt, "
                + "(sum(c.amount)-sum(sc.contr_with_disc)) as disc, sum(vc.amount) as correction, "
                + "(sum(sc.net_payments)) as payment "
                + "FROM student_contract as sc "
                + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "left join student as st on st.id = sc.student_id "
                + "left join contract as c on sc.contract_id = c.id "
                + "where st.school_id = ? and sc.year_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        ContractTotal ct = new ContractTotal();
        while (result.next()) {
            ct.setTtl_contract(result.getDouble("contract"));
            ct.setTtl_debt(result.getDouble("debt"));
            ct.setTtl_disc(result.getDouble("disc"));
            ct.setTtl_correction(result.getDouble("correction"));
            ct.setTtl_payments(result.getDouble("payment"));
            ct.setTtl_left(result.getDouble("debt") + result.getDouble("contract")
                    - result.getDouble("disc") - result.getDouble("payment")
                    + result.getDouble("correction"));
        }
        return ct;
    }

    public IndexedContainer execSQL_DebtsByClass(MyVaadinUI myUI, Date from,
                                                 Date till, int year_id, String class_ids, String edu_statuses_ids,
                                                 DebtReport dr) throws SQLException {
        String sql = "SELECT st.id, CONCAT(cnu.name, ' - ', cna.name) AS class_name, "
                + "st.name, st.surname, ip_temp.amount AS inst_plan, sp_temp.amount AS paid "
                + "FROM student AS st LEFT JOIN "
                + "(SELECT ip.student_id AS stud_id, SUM(ip.amount) AS amount "
                + "FROM student_installement_plan AS ip "
                + "WHERE DATE(ip.date_of_payment) >=? AND DATE(ip.date_of_payment) <=? "
                + "AND ip.year_id = ? GROUP BY ip.student_id) AS ip_temp "
                + "ON st.id = ip_temp.stud_id "
                + "LEFT JOIN "
                + "(SELECT sp.student_id AS stud_id, "
                + "(SUM(IF(sp.payment_category_id != 3, sp.amount, 0)) - "
                + "SUM(IF(sp.payment_category_id = 3, sp.amount, 0))) AS amount "
                + "FROM student_payments AS sp WHERE DATE(sp.modification_date) >=? "
                + "AND DATE(sp.modification_date) <=? AND sp.year_id = ? "
                + "GROUP BY sp.student_id) AS sp_temp "
                + "ON st.id = sp_temp.stud_id "
                + "LEFT JOIN "
                + "(SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? "
                + "AND so.is_valid = 1 GROUP BY so.student_id) AS o_temp "
                + "ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN class_name AS cna ON cna.id = CASE "
                + "WHEN stud_o.to_class_name_id IS NULL "
                + "THEN st.class_name_id ELSE stud_o.to_class_name_id END "
                + "LEFT JOIN class_number AS cnu ON cna.class_number_id = cnu.id "
                + "LEFT JOIN education_status AS edu ON edu.id = CASE "
                + "WHEN stud_o.to_education_status_id IS NULL "
                + "THEN st.education_status_id ELSE stud_o.to_education_status_id END "
                + "WHERE cna.id IN (" + class_ids + ") AND edu.id IN (" + edu_statuses_ids + ") "
                + "and st.entering_year_id <= ? "
                + "ORDER BY cnu.id, cna.id, st.name , st.surname;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(from.getTime()));
        stat.setDate(2, new java.sql.Date(till.getTime()));
        stat.setInt(3, year_id);
        stat.setDate(4, new java.sql.Date(from.getTime()));
        stat.setDate(5, new java.sql.Date(till.getTime()));
        stat.setInt(6, year_id);
        stat.setInt(7, year_id);
        stat.setInt(8, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.InstallmentPlan), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Paid), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Debt), Double.class, 0.0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("st.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.FirstName)).setValue(
                    result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.LastName)).setValue(
                    result.getString("st.surname"));
            item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                    result.getString("class_name"));
            item.getItemProperty(myUI.getMessage(SptMessages.InstallmentPlan)).setValue(
                    result.getDouble("inst_plan"));
            item.getItemProperty(myUI.getMessage(SptMessages.Paid)).setValue(
                    result.getDouble("paid"));
            item.getItemProperty(myUI.getMessage(SptMessages.Debt)).setValue(
                    result.getDouble("inst_plan") - result.getDouble("paid"));
            dr.inst_total += result.getDouble("inst_plan");
            dr.paid_total += result.getDouble("paid");
            dr.debt_total += result.getDouble("inst_plan") - result.getDouble("paid");
        }
        return container;
    }

    public ContractTotal execSQL_totalsByScl(MyVaadinUI myUI, int year_id, String edu_statuses_ids,
                                             int school_id) throws SQLException {
        String sql = "SELECT count(st.id) as ttl_students, "
                + "SUM(c.amount) AS contract, SUM(sc.debt) AS debt, "
                + "(SUM(c.amount) - SUM(sc.contr_with_disc)) AS disc,  "
                + "(SUM(sc.contr_with_disc) + SUM(sc.debt) + IFNULL(SUM(vc.amount), 0.0)) AS net, "
                + "SUM(vc.amount) AS correction, SUM(sc.net_payments) AS payment "
                + "FROM student AS st "
                + "LEFT JOIN student_contract AS sc ON st.id = sc.student_id and sc.year_id = ? "
                + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "LEFT JOIN contract AS c ON sc.contract_id = c.id "
                + "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so "
                + "WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS edu ON edu.id = CASE "
                + "WHEN stud_o.to_education_status_id IS NULL THEN st.education_status_id "
                + "ELSE stud_o.to_education_status_id "
                + "END "
                + "WHERE st.school_id = ? AND edu.id IN (" + edu_statuses_ids + ") and st.entering_year_id <= ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, school_id);
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Total), Double.class, 0.0);
        ContractTotal ct = new ContractTotal();
        while (result.next()) {
            ct.setTtl_students(result.getInt("ttl_students"));
            ct.setTtl_contract(result.getDouble("contract"));
            ct.setTtl_debt(result.getDouble("debt"));
            ct.setTtl_disc(result.getDouble("disc"));
            ct.setTtl_correction(result.getDouble("correction"));
            ct.setTtl_payments(result.getDouble("payment"));
            ct.setTtl_net(result.getDouble("net"));
            ct.setTtl_left(result.getDouble("net") - result.getDouble("payment"));
        }
        return ct;
    }

    public int exec_next_contract_number(int school_id, int year_id) throws SQLException {
        String sql = "SELECT (ifnull(max(tr.contract_number), 0) + 1) as num FROM student_contract tr "
                + "LEFT JOIN student st ON st.id = tr.student_id where st.school_id = ? and tr.year_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("num");
        }
        return 0;
    }
}
