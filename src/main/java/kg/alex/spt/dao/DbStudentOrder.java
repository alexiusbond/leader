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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.StudentOrder;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.OutOfList;
import kg.alex.spt.ui.IssueOrderView;

/**
 *
 * @author eldiiar
 */
public class DbStudentOrder extends BaseDb {

    public DbStudentOrder() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int student_id, IssueOrderView iv)
            throws SQLException {
        

        String sql = "SELECT so.id, o.name, from_edu.name, from_edu.id, to_edu.name, y.name, "
                + "concat(from_class_number.name,' - ', from_class_name.name) as from_class, "
                + "concat(to_class_number.name,' - ', to_class_name.name) as to_class, "
                + "from_class_name.id, to_class_name.id, o.id, so.student_id, so.reason "
                + "FROM student_orders as so "
                + "left join orders as o on o.id=so.orders_id "
                + "left join class_name as from_class_name on from_class_name.id=so.from_class_name_id "
                + "left join class_number as from_class_number "
                + "on from_class_number.id=from_class_name.class_number_id "
                + "left join class_name as to_class_name on to_class_name.id=so.to_class_name_id "
                + "left join class_number as to_class_number "
                + "on to_class_number.id=to_class_name.class_number_id "
                + "left join education_status as from_edu on from_edu.id=so.from_education_status_id "
                + "left join education_status as to_edu on to_edu.id=so.to_education_status_id "
                + "left join year as y on y.id=so.year_id "
                + "where so.student_id = ? and so.year_id=? and so.is_valid=1 "
                + "and so.orders_id<4 order by so.id DESC;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, student_id);
        stat.setInt(2, myUi.getUser().getCurrent_year().getId());
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(SystemSettings.button, Button.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.OrderType), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.FromClass), String.class, null);
        container.addContainerProperty(SystemSettings.from_class_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.to_class_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.student_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.ToClass), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.FromEducationStatus),
                String.class, null);
        container.addContainerProperty(SystemSettings.order_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.from_education_status_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.ToEducationStatus),
                String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Reasons), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Year), String.class, null);
        boolean is_delete_added = false;
        while (result.next()) {
            Item item = container.addItem(result.getInt("so.id"));
            if (!is_delete_added) {
                item.getItemProperty(SystemSettings.button).setValue(iv.createButton(
                        myUi.getMessage(SptMessages.DeleteButton),
                        result.getString("so.id"), FontAwesome.MINUS));
                is_delete_added = true;
            }
            item.getItemProperty(myUi.getMessage(SptMessages.OrderType)).setValue(
                    result.getString("o.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.FromClass)).setValue(
                    result.getString("from_class"));
            item.getItemProperty(SystemSettings.from_class_id).setValue(
                    result.getInt("from_class_name.id"));
            item.getItemProperty(SystemSettings.to_class_id).setValue(
                    result.getInt("to_class_name.id"));
            item.getItemProperty(SystemSettings.order_id).setValue(
                    result.getInt("o.id"));
            item.getItemProperty(SystemSettings.student_id).setValue(
                    result.getInt("so.student_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.ToClass)).setValue(
                    result.getString("to_class"));
            item.getItemProperty(myUi.getMessage(SptMessages.FromEducationStatus)).setValue(
                    result.getString("from_edu.name"));
            item.getItemProperty(SystemSettings.from_education_status_id).setValue(
                    result.getInt("from_edu.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.ToEducationStatus)).setValue(
                    result.getString("to_edu.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Reasons)).setValue(
                    result.getString("so.reason"));
            item.getItemProperty(myUi.getMessage(SptMessages.Year)).setValue(
                    result.getString("y.name"));
        }
        return container;
    }

    public int exec_order_id(StudentOrder so) throws SQLException {
        String sql = "SELECT id from student_orders "
                + "where orders_id=3 and year_id=? and student_id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, so.getYear_id());
        stat.setInt(2, so.getStudent_id());
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getInt("id");
        }
        return 0;
    }

    public int exec_insert(StudentOrder so) throws SQLException {
        String sql = "INSERT INTO student_orders (student_id, orders_id,"
                + "year_id, from_class_name_id, to_class_name_id, from_education_status_id, "
                + "to_education_status_id, modification_date, employee_id, reason) "
                + "VALUES(?,?,?,?,?,?,?,NOW(),?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, so.getStudent_id());
        stat.setInt(2, so.getOrder_id());
        stat.setInt(3, so.getYear_id());
        stat.setInt(4, so.getFrom_class_id());
        if (so.getTo_class_id() != 0) {
            stat.setInt(5, so.getTo_class_id());
        } else {
            stat.setNull(5, Types.INTEGER);
        }
        stat.setInt(6, so.getFrom_education_status_id());
        stat.setInt(7, so.getTo_education_status_id());
        stat.setInt(8, so.getEmployee_id());
        if (so.getReasons() != null && !so.getReasons().equals("")) {
            stat.setString(9, so.getReasons());
        } else {
            stat.setNull(9, Types.VARCHAR);
        }
        return stat.executeUpdate();
    }

    public int exec_update_existed_to_class(StudentOrder so, int id) throws SQLException {
        String sql = "UPDATE student_orders set to_class_name_id=?, "
                + "modification_date=NOW(), employee_id=? "
                + "where id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (so.getTo_class_id() != 0) {
            stat.setInt(1, so.getTo_class_id());
        } else {
            stat.setNull(1, Types.INTEGER);
        }
        stat.setInt(2, so.getEmployee_id());
        stat.setInt(3, id);
        return stat.executeUpdate();
    }

    public int exec_update_from_class(StudentOrder so, int old_class_name_id,
            int order_id) throws SQLException {
        String sql = "UPDATE student_orders set from_class_name_id=? "
                + "where from_class_name_id=? and year_id=? and student_id=? and id>=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (so.getTo_class_id() != 0) {
            stat.setInt(1, so.getTo_class_id());
        } else {
            stat.setNull(1, Types.INTEGER);
        }
        stat.setInt(2, old_class_name_id);
        stat.setInt(3, so.getYear_id());
        stat.setInt(4, so.getStudent_id());
        stat.setInt(5, order_id);
        return stat.executeUpdate();
    }

    public int exec_update_from_class(StudentOrder so, int old_class_name_id) throws SQLException {
        String sql = "UPDATE student_orders set from_class_name_id=? "
                + "where from_class_name_id=? and year_id=? and student_id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (so.getTo_class_id() != 0) {
            stat.setInt(1, so.getTo_class_id());
        } else {
            stat.setNull(1, Types.INTEGER);
        }
        stat.setInt(2, old_class_name_id);
        stat.setInt(3, so.getYear_id() + 1);
        stat.setInt(4, so.getStudent_id());
        return stat.executeUpdate();
    }

    public int exec_update_to_class(StudentOrder so, int old_class_name_id,
            int order_id) throws SQLException {
        String sql = "UPDATE student_orders set to_class_name_id=? "
                + "where to_class_name_id=? and year_id=? and student_id=? and id>=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (so.getTo_class_id() != 0) {
            stat.setInt(1, so.getTo_class_id());
        } else {
            stat.setNull(1, Types.INTEGER);
        }
        stat.setInt(2, old_class_name_id);
        stat.setInt(3, so.getYear_id());
        stat.setInt(4, so.getStudent_id());
        stat.setInt(5, order_id);
        return stat.executeUpdate();
    }

    public int exec_update_to_class(StudentOrder so, int old_class_name_id) throws SQLException {
        String sql = "UPDATE student_orders set to_class_name_id=? "
                + "where to_class_name_id=? and year_id=? and student_id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (so.getTo_class_id() != 0) {
            stat.setInt(1, so.getTo_class_id());
        } else {
            stat.setNull(1, Types.INTEGER);
        }
        stat.setInt(2, old_class_name_id);
        stat.setInt(3, so.getYear_id() + 1);
        stat.setInt(4, so.getStudent_id());
        return stat.executeUpdate();
    }

    public int exec_update_future_orders(int year_id, int is_valid, int student_id) throws SQLException {
        String sql = "UPDATE student_orders set is_valid=? "
                + "where student_id=? and year_id>?";
        PreparedStatement stat = dbCon.prepareStatement(sql);

        stat.setInt(1, is_valid);
        stat.setInt(2, student_id);
        stat.setInt(3, year_id);
        return stat.executeUpdate();
    }

    public int insertNewStOrder(int id, int class_id, int year_id, int emp_id) throws SQLException {
        String sql = "insert into student_orders (student_id, orders_id, from_class_name_id, "
                + "to_class_name_id, from_education_status_id, to_education_status_id, "
                + "year_id, modification_date,employee_id) "
                + "value(?, 4, 200, ?, 1, 1, ?, NOW(), ?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        stat.setInt(2, class_id);
        stat.setInt(3, year_id);
        stat.setInt(4, emp_id);
        return stat.executeUpdate();
    }

    public int insertNewStCtrOrder(int id, int class_id, int stat_id, int year_id,
            int emp_id) throws SQLException {
        String sql = "insert ignore into student_orders (student_id, orders_id, from_class_name_id, "
                + "to_class_name_id, from_education_status_id, to_education_status_id, "
                + "year_id, modification_date,employee_id) "
                + "value(?, 5, ?, ?, ?, 2, ?, NOW(), ?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        stat.setInt(2, class_id);
        stat.setInt(3, class_id);
        stat.setInt(4, stat_id);
        stat.setInt(5, year_id);
        stat.setInt(6, emp_id);
        return stat.executeUpdate();
    }

    public int insertPre_regChange_year(int next_year_id, int school_id, int emp_id)
            throws SQLException {
        String sql = "insert ignore into student_orders (student_id, orders_id, from_class_name_id, "
                + "to_class_name_id, from_education_status_id, to_education_status_id, "
                + "year_id, reason, modification_date, employee_id) "
                + "select st.id, 4, st.class_name_id, st.class_name_id, 2, 1, ?, "
                + "NULL, now(), ? from student as st "
                + "left join ("
                + "select so.student_id as stud_id from student_orders as so "
                + "where so.year_id=? group by so.student_id) as so_temp on so_temp.stud_id=st.id "
                + "where st.education_status_id = 2 and st.entering_year_id <= ? "
                + "and st.school_id = ? and so_temp.stud_id IS NULL;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, next_year_id);
        stat.setInt(2, emp_id);
        stat.setInt(3, next_year_id);
        stat.setInt(4, next_year_id);
        stat.setInt(5, school_id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_outOf(MyVaadinUI myUI, String year_ids, String from_class_ids,
            String reasons, int school_id, OutOfList ol)
            throws SQLException {
        if (reasons != null) {
            reasons = reasons.replace(", ", "|");
        }
        String sql = "SELECT st.id, st.login, st.name, st.surname, so.reason, y.name, CONCAT(from_cl.name, ' - ', from_cln.name) AS from_class, "
                + "    CONCAT(to_cl.name, ' - ', to_cln.name) AS to_class, date(so.modification_date), sc.debt, sc.contr_with_disc, sc.net_payments "
                + "FROM student_orders AS so  "
                + "LEFT JOIN student AS st ON st.id = so.student_id "
                + "LEFT JOIN year AS y ON y.id = so.year_id "
                + "LEFT JOIN class_name AS from_cln ON from_cln.id = so.from_class_name_id "
                + "LEFT JOIN class_number AS from_cl ON from_cl.id = from_cln.class_number_id "
                + "LEFT JOIN class_name AS to_cln ON to_cln.id = so.to_class_name_id "
                + "LEFT JOIN class_number AS to_cl ON to_cl.id = to_cln.class_number_id "
                + "LEFT JOIN student_contract AS sc ON sc.student_id = st.id AND sc.year_id = so.year_id "
                + "WHERE so.year_id IN (" + year_ids + ") "
                + "	AND so.from_class_name_id IN (" + from_class_ids + ") ";
        if (reasons != null) {
            sql += "    and (so.reason REGEXP '" + reasons + "') ";
        }
        sql += "    AND so.is_valid = 1 "
                + "    AND so.orders_id = 1 "
                + "    AND st.school_id = ? "
                + "order by so.year_id, from_cl.id, from_cln.id, st.name, st.surname;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, "%" + reasons + "%");
        stat.setInt(1, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Id), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Firstname), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Surname), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Year), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Reasons), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.FromClass), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.ToClass), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Net), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Paid), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Left), Double.class, 0.0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("st.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Id)).setValue(
                    result.getString("st.login"));
            item.getItemProperty(myUI.getMessage(SptMessages.Firstname)).setValue(
                    result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Surname)).setValue(
                    result.getString("st.surname"));
            item.getItemProperty(myUI.getMessage(SptMessages.Year)).setValue(
                    result.getString("y.name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Reasons)).setValue(
                    result.getString("so.reason"));
            item.getItemProperty(myUI.getMessage(SptMessages.FromClass)).setValue(
                    result.getString("from_class"));
            item.getItemProperty(myUI.getMessage(SptMessages.ToClass)).setValue(
                    result.getString("to_class"));
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                    result.getString("date(so.modification_date)"));
            ol.nets += result.getDouble("sc.debt") + result.getDouble("sc.contr_with_disc");
            ol.paids += result.getDouble("sc.net_payments");
            item.getItemProperty(myUI.getMessage(SptMessages.Net)).setValue(
                    result.getDouble("sc.debt") + result.getDouble("sc.contr_with_disc"));
            item.getItemProperty(myUI.getMessage(SptMessages.Paid)).setValue(
                    result.getDouble("sc.net_payments"));
            item.getItemProperty(myUI.getMessage(SptMessages.Left)).setValue(
                    (result.getDouble("sc.debt") + result.getDouble("sc.contr_with_disc")
                    - (result.getDouble("sc.net_payments"))));
        }
        return container;
    }
}
