/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.domain.EducationStatus;
import kg.alex.spt.domain.Student;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.students.StatusesReport;
import kg.alex.spt.ui.CallsView;
import kg.alex.spt.ui.IssueOrderView;
import kg.alex.spt.ui.StudentDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class DbStudent extends BaseDb {

    public DbStudent() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id, StudentDefinitionView sdv, String edu_sts)
            throws SQLException {


        if (edu_sts.equals("")) {
            edu_sts = "-1";
        }
        String sql = "SELECT s.id, s.login, s.name, s.surname, s.middle_name, s.entering_year_id, "
                + "s.date_of_birth, s.photo, s.gender_id, s.education_status_id, es.name, y.name, "
                + "s.class_name_id, concat(cnu.name,' - ',cn.name) as cl_name, sr.fullname, sr.phone, rel.name "
                + "FROM student as s "
                + "LEFT JOIN student_relatives AS sr ON s.id = sr.student_id AND sr.is_main = 1 "
                + "LEFT JOIN relatives AS rel ON sr.relatives_id = rel.id "
                + "left join education_status as es on s.education_status_id = es.id "
                + "left join class_name as cn on s.class_name_id = cn.id "
                + "left join class_number as cnu on cn.class_number_id = cnu.id "
                + "left join year as y on s.entering_year_id = y.id "
                + "WHERE s.school_id = ? and s.entering_year_id<=? and s.education_status_id in (" + edu_sts + ") "
                + "GROUP BY s.id ORDER BY s.education_status_id, s.name, s.surname;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, myUi.getUser().getCurrent_year().getId());
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Id), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.FirstName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.LastName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Relative), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Phone), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.MiddleName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.DateOfBirth), Date.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Photo), String.class, null);
        container.addContainerProperty(Settings.gender_id, Integer.class, 0);
        container.addContainerProperty(Settings.education_status_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.EducationStatus), String.class, null);
        container.addContainerProperty(Settings.class_name_id, Integer.class, 0);
        container.addContainerProperty(Settings.entering_year_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.EnteringYear), String.class, null);
        int total = 0;
        sdv.eduStatCont.getContainerProperty(1, Settings.count).setValue(0);
        sdv.eduStatCont.getContainerProperty(2, Settings.count).setValue(0);
        sdv.eduStatCont.getContainerProperty(3, Settings.count).setValue(0);
        sdv.eduStatCont.getContainerProperty(4, Settings.count).setValue(0);
        sdv.eduStatCont.getContainerProperty(5, Settings.count).setValue(0);
        sdv.eduStatCont.getContainerProperty(6, Settings.count).setValue(0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Id)).setValue(
                    result.getString("s.login"));
            item.getItemProperty(myUi.getMessage(SptMessages.FirstName)).setValue(
                    result.getString("s.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.LastName)).setValue(
                    result.getString("s.surname"));
            if (result.getString("rel.name") != null) {
                item.getItemProperty(myUi.getMessage(SptMessages.Relative)).setValue(
                        result.getString("rel.name") + " - " + result.getString("sr.fullname"));
                item.getItemProperty(myUi.getMessage(SptMessages.Phone)).setValue(
                        result.getString("sr.phone"));
            }
            item.getItemProperty(myUi.getMessage(SptMessages.MiddleName)).setValue(
                    result.getString("s.middle_name"));
            item.getItemProperty(myUi.getMessage(SptMessages.DateOfBirth)).setValue(
                    result.getDate("s.date_of_birth"));
            item.getItemProperty(myUi.getMessage(SptMessages.Photo)).setValue(result.getString("s.photo"));
            item.getItemProperty(Settings.gender_id).setValue(result.getInt("s.gender_id"));
            item.getItemProperty(Settings.education_status_id).setValue(result.getInt("s.education_status_id"));
            sdv.eduStatCont.getContainerProperty(result.getInt("s.education_status_id"), Settings.count)
                    .setValue(((Integer) sdv.eduStatCont.getContainerProperty(result.getInt("s.education_status_id"), Settings.count)
                            .getValue()) + 1);
            item.getItemProperty(myUi.getMessage(SptMessages.EducationStatus)).setValue(
                    result.getString("es.name"));
            item.getItemProperty(Settings.class_name_id).setValue(
                    result.getInt("s.class_name_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.ClassName)).setValue(
                    result.getString("cl_name"));
            item.getItemProperty(myUi.getMessage(SptMessages.EnteringYear)).setValue(
                    result.getString("y.name"));
            item.getItemProperty(Settings.entering_year_id).setValue(
                    result.getInt("s.entering_year_id"));
            total++;
        }
        sdv.eduStatCont.getContainerProperty(6, Settings.count).setValue(total);
        return container;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, int scl_id, String edu_sts)
            throws SQLException {

        String sql = "SELECT s.id, s.name, s.surname, cnu.name, concat(cnu.name,' - ',cn.name) as cl_name "
                + "FROM student as s "
                + "left join class_name as cn on s.class_name_id = cn.id "
                + "left join class_number as cnu on cn.class_number_id = cnu.id "
                + "WHERE s.school_id = ? and s.education_status_id in (" + edu_sts + ") "
                + "ORDER BY cnu.id, cn.id, s.name, s.surname;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.FullName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.ClassNumber), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.FullName)).setValue(
                    result.getString("s.name")
                            + " " + result.getString("s.surname"));
            item.getItemProperty(myUi.getMessage(SptMessages.ClassNumber)).setValue(
                    result.getString("cnu.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("s.name") + " " +
                            result.getString("s.surname") + " - " +
                            result.getString("cl_name"));
        }
        return container;
    }

    public IndexedContainer execSQL_for_import(MyVaadinUI myUi, int scl_id) throws SQLException {
        String sql = "SELECT s.login, s.name, s.surname, s.middle_name, s.date_of_birth, g.name, "
                + "concat(cnu.name,' - ',cn.name) as cl_name, sr.fullname, sr.passport, sr.work_place, "
                + "sr.phone, sr.address, r.name FROM student as s "
                + "left join gender as g on s.gender_id = g.id "
                + "left join class_name as cn on s.class_name_id = cn.id "
                + "left join class_number as cnu on cn.class_number_id = cnu.id "
                + "left join student_relatives as sr on s.id = sr.student_id and sr.is_main=1 "
                + "left join relatives as r on r.id = sr.relatives_id "
                + "WHERE s.school_id = ? and s.entering_year_id<=? ORDER BY s.id DESC;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, myUi.getUser().getCurrent_year().getId());
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Id), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.FirstName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.LastName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.MiddleName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Gender), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.DateOfBirth), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Relative), String.class, null);
        container.addContainerProperty(
                myUi.getMessage(SptMessages.FullName) + " ("
                        + myUi.getMessage(SptMessages.Relative) + ")", String.class, null);
        container.addContainerProperty(
                myUi.getMessage(SptMessages.Passport) + " ("
                        + myUi.getMessage(SptMessages.Relative) + ")", String.class, null);
        container.addContainerProperty(
                myUi.getMessage(SptMessages.WorkPlace) + " ("
                        + myUi.getMessage(SptMessages.Relative) + ")", String.class, null);
        container.addContainerProperty(
                myUi.getMessage(SptMessages.Phone) + " ("
                        + myUi.getMessage(SptMessages.Relative) + ")", String.class, null);
        container.addContainerProperty(
                myUi.getMessage(SptMessages.Address) + " ("
                        + myUi.getMessage(SptMessages.Relative) + ")", String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getString("s.login"));
            item.getItemProperty(myUi.getMessage(SptMessages.Id)).setValue(
                    result.getString("s.login"));
            item.getItemProperty(myUi.getMessage(SptMessages.FirstName)).setValue(
                    result.getString("s.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.LastName)).setValue(
                    result.getString("s.surname"));
            item.getItemProperty(myUi.getMessage(SptMessages.MiddleName)).setValue(
                    result.getString("s.middle_name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Gender)).setValue(
                    result.getString("g.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.DateOfBirth)).setValue(
                    Settings.df.format(result.getDate("s.date_of_birth")));
            item.getItemProperty(myUi.getMessage(SptMessages.ClassName)).setValue(
                    result.getString("cl_name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Relative)).setValue(
                    result.getString("r.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.FullName) + " ("
                    + myUi.getMessage(SptMessages.Relative) + ")").setValue(
                    result.getString("sr.fullname"));
            item.getItemProperty(myUi.getMessage(SptMessages.Passport) + " ("
                    + myUi.getMessage(SptMessages.Relative) + ")").setValue(
                    result.getString("sr.passport"));
            item.getItemProperty(myUi.getMessage(SptMessages.WorkPlace) + " ("
                    + myUi.getMessage(SptMessages.Relative) + ")").setValue(
                    result.getString("sr.work_place"));
            item.getItemProperty(myUi.getMessage(SptMessages.Phone) + " ("
                    + myUi.getMessage(SptMessages.Relative) + ")").setValue(
                    result.getString("sr.phone"));
            item.getItemProperty(myUi.getMessage(SptMessages.Address) + " ("
                    + myUi.getMessage(SptMessages.Relative) + ")").setValue(
                    result.getString("sr.address"));
        }
        return container;
    }

    public IndexedContainer execSQL_for_orders(MyVaadinUI myUi, int scl_id,
                                               IssueOrderView iv) throws SQLException {


        String sql = "SELECT s.id, s.login, s.name, s.surname, es.name, s.entering_year_id, "
                + "concat(cnu.name,' - ',cn.name) as cl_name, s.class_name_id, "
                + "s.education_status_id FROM student as s "
                + "left join education_status as es on s.education_status_id = es.id "
                + "left join class_name as cn on s.class_name_id = cn.id "
                + "left join class_number as cnu on cn.class_number_id = cnu.id "
                + "WHERE s.school_id = ? and s.entering_year_id<=? "
                + "ORDER BY s.education_status_id, s.name, s.surname;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, myUi.getUser().getCurrent_year().getId());
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(Settings.button, Button.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Id), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.FirstName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.LastName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.EducationStatus), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(Settings.class_id, Integer.class, 0);
        container.addContainerProperty(Settings.education_status_id, Integer.class, 0);
        container.addContainerProperty(Settings.entering_year_id, Integer.class, 0);
        container.addContainerProperty(Settings.id, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(Settings.button).setValue(
                    iv.createButton(myUi.getMessage(SptMessages.Details),
                            result.getString("s.id"), FontAwesome.INFO));
            item.getItemProperty(myUi.getMessage(SptMessages.Id)).setValue(
                    result.getString("s.login"));
            item.getItemProperty(myUi.getMessage(SptMessages.FirstName)).setValue(
                    result.getString("s.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.LastName)).setValue(
                    result.getString("s.surname"));
            item.getItemProperty(myUi.getMessage(SptMessages.EducationStatus)).setValue(
                    result.getString("es.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.ClassName)).setValue(
                    result.getString("cl_name"));
            item.getItemProperty(Settings.class_id).setValue(
                    result.getInt("s.class_name_id"));
            item.getItemProperty(Settings.education_status_id).setValue(
                    result.getInt("s.education_status_id"));
            item.getItemProperty(Settings.entering_year_id).setValue(
                    result.getInt("s.entering_year_id"));
        }
        return container;
    }

    public int exec_insert(Student s) throws SQLException {
        String sql = "INSERT ignore INTO student (login, password, name, "
                + "surname, middle_name, date_of_birth, photo, school_id, gender_id, "
                + "education_status_id,entering_year_id,class_name_id,employee_id, "
                + "modification_date) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,NOW());";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, s.getLogin());
        stat.setString(2, s.getPassword());
        stat.setString(3, s.getName());
        stat.setString(4, s.getSurname());
        stat.setString(5, s.getMiddle_name());
        stat.setDate(6, new java.sql.Date(s.getBirth_date().getTime()));
        stat.setString(7, s.getPhoto());
        stat.setInt(8, s.getSchool_id());
        stat.setInt(9, s.getGender_id());
        stat.setInt(10, s.getEdu_status_id());
        stat.setInt(11, s.getEntering_year_id());
        stat.setInt(12, s.getClass_name_id());
        stat.setInt(13, s.getEmployee_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Student s) throws SQLException {
        String sql = "UPDATE student SET login=?, name=?, surname=?, "
                + "middle_name=?, date_of_birth=?, photo=?, gender_id=?, "
                + "education_status_id=?, class_name_id=?,employee_id=?,"
                + "modification_date=NOW() "
                + "WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, s.getLogin());
        stat.setString(2, s.getName());
        stat.setString(3, s.getSurname());
        stat.setString(4, s.getMiddle_name());
        stat.setDate(5, new java.sql.Date(s.getBirth_date().getTime()));
        stat.setString(6, s.getPhoto());
        stat.setInt(7, s.getGender_id());
        stat.setInt(8, s.getEdu_status_id());
        stat.setInt(9, s.getClass_name_id());
        stat.setInt(10, s.getEmployee_id());
        stat.setInt(11, s.getId());
        return stat.executeUpdate();
    }

    public int exec_updateLogin(int student_id, String login) throws SQLException {
        String sql = "UPDATE student SET login = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, login);
        stat.setInt(2, student_id);
        return stat.executeUpdate();
    }

    public int exec_update(int student_id, int education_status_id, int class_id,
                           int employee_id) throws SQLException {
        String sql = "UPDATE student SET education_status_id=?, class_name_id=?, employee_id=?,"
                + "modification_date=NOW() WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, education_status_id);
        stat.setInt(2, class_id);
        stat.setInt(3, employee_id);
        stat.setInt(4, student_id);
        return stat.executeUpdate();
    }

    public int exec_update(int student_id, int education_status_id,
                           int employee_id) throws SQLException {
        String sql = "UPDATE student SET education_status_id=?, employee_id=?,"
                + "modification_date=NOW() WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, education_status_id);
        stat.setInt(2, employee_id);
        stat.setInt(3, student_id);
        return stat.executeUpdate();
    }

    public IndexedContainer execStud_sel(MyVaadinUI myUi, int cl_id, int year_id)
            throws SQLException {
        String sql = "SELECT st.id, st.name, st.surname, st.middle_name "
                + "FROM student AS st "
                + "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN class_name AS cln ON cln.id = "
                + "CASE WHEN stud_o.to_class_name_id IS NULL THEN st.class_name_id "
                + "ELSE stud_o.to_class_name_id END "
                + "WHERE cln.id = ?  and st.entering_year_id <= ? ORDER BY st.name, st.surname;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, cl_id);
        stat.setInt(3, year_id);

        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("st.id"));
            String fullname;
            fullname = result.getString("st.name") + " " + result.getString("st.surname");
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(fullname);
        }
        return container;
    }

    public StudentInfoPdf execStudInfo_pdf(int student_id, int year_id)
            throws SQLException {
        String sql = "SELECT st.login, st.photo, st.name, st.surname, sc.name_ru, sc.address, sc.phone, sc.director_fullname, "
                + "CONCAT(cnu.name, ' - ', cna.name) AS class_name, concat(e.name, ' ', e.surname) as fullname FROM student AS st "
                + "LEFT JOIN (SELECT so.to_class_name_id AS tcl, so.student_id as stid "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.student_id = ? AND so.is_valid = 1 "
                + "ORDER BY id DESC LIMIT 1) AS o_temp on o_temp.stid=st.id LEFT JOIN school AS sc ON sc.id = st.school_id "
                + "LEFT JOIN class_name AS cna ON cna.id = CASE WHEN o_temp.tcl IS NULL THEN st.class_name_id ELSE o_temp.tcl END "
                + "LEFT JOIN class_number AS cnu ON cna.class_number_id = cnu.id "
                + "LEFT JOIN hr_employee_order AS eo ON st.school_id = eo.school_id and eo.to_date IS NULL "
                + "left join hr_orders as ord on ord.id=eo.hr_orders_id LEFT JOIN employee AS e ON eo.employee_id = e.id "
                + "LEFT JOIN hr_position AS hrp ON eo.hr_position_id = hrp.id "
                + "LEFT JOIN position AS p ON hrp.id = p.hr_position_id "
                + "WHERE st.id = ? AND p.id = 2 AND ord.working_status_id=2 LIMIT 1;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, student_id);
        stat.setInt(3, student_id);
        ResultSet result = stat.executeQuery();
        StudentInfoPdf st = new StudentInfoPdf();
        while (result.next()) {
            st.setStud_login(result.getString("st.login"));
            st.setStud_photo(result.getString("st.photo"));
            st.setStud_name(result.getString("st.name"));
            st.setStud_surname(result.getString("st.surname"));
            st.setStud_class_name(result.getString("class_name"));
            st.setScl_name_ru(result.getString("sc.name_ru"));
            st.setScl_address(result.getString("sc.address"));
            st.setScl_phone(result.getString("sc.phone"));
            st.setScl_accountant_full_name(result.getString("fullname"));
            st.setScl_dir_f_name(result.getString("sc.director_fullname"));
        }
        return st;
    }

    public int exec_delete(int id) throws SQLException {
        String sql = "delete sc, sd, ip, sp, sr, sa, so, sca from student as st "
                + "left join student_payments as sp on sp.student_id = st.id "
                + "left join student_contract as sc on sc.student_id = st.id "
                + "left join student_discount as sd on sd.student_id = st.id "
                + "left join student_installement_plan as ip on ip.student_id = st.id "
                + "left join student_relatives as sr on sr.student_id = st.id "
                + "left join student_accessories as sa on sa.student_id = st.id "
                + "left join student_orders as so on so.student_id = st.id "
                + "left join student_calls as sca on sca.student_id = st.id "
                + "where st.id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQLCalls(MyVaadinUI myUi, int year_id, String class_ids, CallsView cv) throws SQLException {

        String sql = "select st.id, st.login, st.name, st.surname,concat(cnu.name, ' - ' , cna.name) as class_name, "
                + "concat(sr.phone,' (',sr.fullname,')') "
                + "as is_main, MAX(IF(ip.is_visible = 1, ip.date_of_payment, NULL)) AS plan_debt_date, "
                + "ifnull((sum(ip.amount) - sc.net_payments),0.0) as plan_debt, "
                + "sc.contr_with_disc + sc.debt + ifnull(vc.amount, 0.0) - sc.net_payments as remain, "
                + "(SELECT concat(FORMAT(sp.amount, 2), ' (', date(sp.modification_date),')') FROM student_payments sp "
                + "where sp.student_id = st.id and sp.year_id = ? and sp.payment_category_id != 3 order by sp.id desc limit 1) as last_payment, "
                + "(SELECT CONCAT(DATE_FORMAT(modification_date, '%d-%m-%Y'), IF((note IS NOT NULL AND note != ''), CONCAT(' (', note, ')'), '')) "
                + "FROM student_calls as sc WHERE student_id = st.id order by sc.id desc limit 1) AS last_call "
                + "from student as st "
                + "left join student_relatives as sr on st.id = sr.student_id "
                + "left join student_contract as sc on st.id = sc.student_id "
                + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "left join student_installement_plan as ip on st.id = ip.student_id "
                + "and sc.year_id = ip.year_id AND ip.date_of_payment <= NOW() "
                + "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id FROM student_orders AS so "
                + "WHERE so.year_id = ? AND so.is_valid = 1 GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS edu ON edu.id = "
                + "CASE WHEN stud_o.to_education_status_id IS NULL THEN st.education_status_id "
                + "ELSE stud_o.to_education_status_id END "
                + "LEFT JOIN class_name AS cna ON cna.id = CASE "
                + "WHEN stud_o.to_class_name_id IS NULL THEN st.class_name_id "
                + "ELSE stud_o.to_class_name_id END "
                + "left join class_number as cnu on cnu.id = cna.class_number_id "
                + "where sr.is_main = 1 and sc.year_id = ? "
                + "and cna.id in(" + class_ids + ") group by st.id having plan_debt > 0 "
                + "order by class_name, st.name, st.surname;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = cv.prepareContainer();
        while (result.next()) {
            Item item = container.addItem(result.getInt("st.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Id)).setValue(result.getString("st.login"));
            item.getItemProperty(myUi.getMessage(SptMessages.FirstName)).setValue(result.getString("st.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.LastName)).setValue(result.getString("st.surname"));
            item.getItemProperty(myUi.getMessage(SptMessages.ClassName)).setValue(
                    result.getString("class_name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Phone)).setValue(
                    result.getString("is_main"));
            if (result.getDate("plan_debt_date") != null) {
                item.getItemProperty(myUi.getMessage(SptMessages.PlanDebtDate)).setValue(
                        Settings.df.format(result.getDate("plan_debt_date")));
            }
            item.getItemProperty(myUi.getMessage(SptMessages.InstPlanDebt)).setValue(
                    result.getDouble("plan_debt"));
            item.getItemProperty(myUi.getMessage(SptMessages.Remain)).setValue(result.getDouble("remain"));
            cv.total += result.getDouble("plan_debt");
            if (result.getString("last_call") != null) {
                item.getItemProperty(myUi.getMessage(SptMessages.LastCall)).setValue(result.getString("last_call"));
            }
            if (result.getString("last_payment") != null) {
                item.getItemProperty(myUi.getMessage(SptMessages.LastPayment)).setValue(result.getString("last_payment"));
            }
            item.getItemProperty(myUi.getMessage(SptMessages.Note)).setValue(
                    cv.createTextField(result.getInt("st.id")));
            item.getItemProperty(Settings.button).setValue(cv.createButton(result.getInt("st.id")));
        }
        return container;
    }

    public int exec_ChangeYear(int year_id, int school_id) throws SQLException {
        String sql = "update student as st inner join (select max(so.id) as max_id, "
                + "so.student_id as student_id from student_orders as so "
                + "left join student as st1 on so.student_id = st1.id where "
                + "so.year_id = ? and st1.school_id = ? and so.is_valid=1 group by so.student_id) "
                + "as it on st.id = it.student_id set st.class_name_id = (select "
                + "so2.to_class_name_id from student_orders as so2 where so2.id = it.max_id), "
                + "st.education_status_id = (select so3.to_education_status_id "
                + "from student_orders as so3 where so3.id = it.max_id);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, school_id);
        return stat.executeUpdate();

    }

    public EducationStatus execEduCount(int scl_id, int year_id)
            throws SQLException {
        String sql = "SELECT count(*) as ttl, count(if(st.education_status_id = 1, "
                + "st.education_status_id, null)) as prereg, "
                + "count(if(st.education_status_id = 2, st.education_status_id, null)) as active, "
                + "count(if(st.education_status_id = 3, st.education_status_id, null)) as notcon, "
                + "count(if(st.education_status_id = 4, st.education_status_id, null)) as outof, "
                + "count(if(st.education_status_id = 5, st.education_status_id, null)) as graduated "
                + "FROM student as st where st.school_id = ? and st.entering_year_id <= ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        EducationStatus e = new EducationStatus();
        while (result.next()) {
            e.setPre_registered(result.getString("prereg"));
            e.setActive(result.getString("active"));
            e.setNot_confirmed(result.getString("notcon"));
            e.setOutOf(result.getString("outof"));
            e.setGraduated(result.getString("graduated"));
            e.setTotal(result.getString("ttl"));
        }
        return e;
    }

    public void execSQL_Statuses_by_classes(MyVaadinUI myUI, int year_id,
                                            StatusesReport sr) throws SQLException {


        StringBuilder sql = new StringBuilder("SELECT sch.id, sch.name_ru, COUNT(IF(st.entering_year_id<="
                + year_id + " AND cna.class_number_id IN ("
                + Settings.convertCollectionToStr(((Set<?>) sr.classTable.getValue())) + ") "
                + "AND edu.id IN ("
                + Settings.convertCollectionToStr(((Set<?>) sr.statusMS.getValue())) + "),1,NULL)) AS quantity");
        Iterator<?> class_iter = ((Set<?>) sr.classTable.getValue()).iterator();
        Iterator<?> status_iter;
        while (class_iter.hasNext()) {
            Object nextClass = class_iter.next();
            status_iter = ((Set<?>) sr.statusMS.getValue()).iterator();
            while (status_iter.hasNext()) {
                Object nextStatus = status_iter.next();
                sql.append(", COUNT(IF(st.entering_year_id<=").append(year_id).append(" and cna.class_number_id = ").append(nextClass).append(" and edu.id= ").append(nextStatus).append(", 1, NULL)) AS quantity").append(nextClass).append("_").append(nextStatus).append(" ");
            }
        }
        sql.append(" FROM school as sch " + "LEFT JOIN student AS st on st.school_id=sch.id " + "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id " + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 " + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id " + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid " + "LEFT JOIN class_name AS cna ON cna.id = CASE " + "WHEN stud_o.to_class_name_id IS NULL THEN st.class_name_id " + "ELSE stud_o.to_class_name_id END " + "LEFT JOIN education_status AS edu ON edu.id = CASE " + "WHEN stud_o.to_education_status_id IS NULL THEN st.education_status_id " + "ELSE stud_o.to_education_status_id END " + "WHERE sch.id IN (").append(Settings.convertCollectionToStr(((Set<?>) sr.schoolsTable.getValue()))).append(") ").append("GROUP BY sch.id;");
        PreparedStatement stat = dbCon.prepareStatement(sql.toString());
        stat.setInt(1, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.School), String.class, null);
        class_iter = ((Set<?>) sr.classTable.getValue()).iterator();
        while (class_iter.hasNext()) {
            Object nextClass = class_iter.next();
            status_iter = ((Set<?>) sr.statusMS.getValue()).iterator();
            while (status_iter.hasNext()) {
                Object nextStatus = status_iter.next();
                {
                    container.addContainerProperty(sr.classTable.getContainerProperty(
                                    nextClass, myUI.getMessage(SptMessages.Title)).getValue() + " "
                                    + myUI.getMessage(SptMessages.ClassName) + " "
                                    + sr.statusMS.getContainerProperty(
                                    nextStatus, myUI.getMessage(SptMessages.Title)).getValue(),
                            Integer.class, 0);
                }
            }
        }
        container.addContainerProperty(myUI.getMessage(SptMessages.Total), Integer.class, 0);
        sr.dataTable.setContainerDataSource(container);
        sr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.School),
                myUI.getMessage(SptMessages.Total));
        int counter = 0;
        while (result.next()) {
            Item item = container.addItem(result.getInt("sch.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.School)).setValue(
                    result.getString("sch.name_ru"));
            class_iter = ((Set<?>) sr.classTable.getValue()).iterator();
            String footerVal;
            while (class_iter.hasNext()) {
                Object nextClass = class_iter.next();
                status_iter = ((Set<?>) sr.statusMS.getValue()).iterator();
                while (status_iter.hasNext()) {
                    Object nextStatus = status_iter.next();
                    item.getItemProperty(sr.classTable.getContainerProperty(
                            nextClass, myUI.getMessage(SptMessages.Title)).getValue() + " "
                            + myUI.getMessage(SptMessages.ClassName) + " "
                            + sr.statusMS.getContainerProperty(
                            nextStatus, myUI.getMessage(SptMessages.Title)).getValue()).setValue(
                            result.getInt("quantity" + nextClass + "_" + nextStatus));
                    footerVal = sr.dataTable.getColumnFooter(sr.classTable.getContainerProperty(
                            nextClass, myUI.getMessage(SptMessages.Title)).getValue() + " "
                            + myUI.getMessage(SptMessages.ClassName) + " "
                            + sr.statusMS.getContainerProperty(
                            nextStatus, myUI.getMessage(SptMessages.Title)).getValue());
                    if (counter != 0) {
                        sr.dataTable.setColumnFooter(sr.classTable.getContainerProperty(
                                        nextClass, myUI.getMessage(SptMessages.Title)).getValue() + " "
                                        + myUI.getMessage(SptMessages.ClassName) + " "
                                        + sr.statusMS.getContainerProperty(
                                        nextStatus, myUI.getMessage(SptMessages.Title)).getValue(),
                                (Integer.parseInt(footerVal)
                                        + result.getInt("quantity" + nextClass + "_" + nextStatus)) + "");
                    } else {
                        sr.dataTable.setColumnFooter(sr.classTable.getContainerProperty(
                                        nextClass, myUI.getMessage(SptMessages.Title)).getValue() + " "
                                        + myUI.getMessage(SptMessages.ClassName) + " "
                                        + sr.statusMS.getContainerProperty(
                                        nextStatus, myUI.getMessage(SptMessages.Title)).getValue(),
                                result.getInt("quantity" + nextClass + "_" + nextStatus) + "");
                    }
                }
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Total)).setValue(
                    result.getInt("quantity"));
            footerVal = sr.dataTable.getColumnFooter(
                    myUI.getMessage(SptMessages.Total));
            if (counter != 0) {
                sr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total),
                        (Integer.parseInt(footerVal)
                                + result.getInt("quantity")) + "");
            } else {
                sr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total),
                        result.getInt("quantity") + "");
            }
            counter++;
        }
    }

    public int execSQL_login(MyVaadinUI myUi, int year_id, int school_id, int class_type_id, int order_num,
                             int min, int max, String school_level) throws SQLException {
        String sql = "SELECT IFNULL(MAX(CAST(RIGHT(st.login, 3) AS UNSIGNED)), ?) + ? AS num " +
                "FROM student AS st " +
                "LEFT JOIN class_name AS cn ON cn.id = st.class_name_id " +
                "LEFT JOIN class_number AS cnu ON cnu.id = cn.class_number_id " +
                "WHERE st.entering_year_id = ? AND st.school_id = ? AND cn.class_type_id = ? " +
                "AND LENGTH(st.login) = 8 AND CAST(RIGHT(st.login, 3) AS UNSIGNED) BETWEEN ? AND ? ";
        if (school_level != null && school_level.equals(myUi.getMessage(SptMessages.PrimaryCode))) {
            sql += "AND cnu.name < 7";
        } else if (school_level != null && school_level.equals(myUi.getMessage(SptMessages.SecondaryCode))) {
            sql += "AND cnu.name >= 7";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, min);
        stat.setInt(2, order_num);
        stat.setInt(3, year_id);
        stat.setInt(4, school_id);
        stat.setInt(5, class_type_id);
        stat.setInt(6, min);
        stat.setInt(7, max - 1);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("num");
        }
        return 0;
    }

    public boolean isLoginExists(String login) throws SQLException {
        String sql = "SELECT st.login FROM student AS st where st.login = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, login);
        ResultSet result = stat.executeQuery();
        return result.next();
    }
}
