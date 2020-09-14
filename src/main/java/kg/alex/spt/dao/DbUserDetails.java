/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kg.alex.spt.domain.Definition;
import kg.alex.spt.domain.UserDetails;

/**
 *
 * @author alex
 */
public class DbUserDetails extends BaseDb {

    public DbUserDetails() throws Exception {
        super();
    }

    public UserDetails execSQLUserInfo(String login) throws SQLException {
        String sql = "select e.id, ord.working_status_id, e.login, concat(e.surname, ' ', e.name) as fullname, "
                + "eo.school_id, sch.name, sch.school_type_id, sch.photo, sch.code, y.id, y.name, sch.transactions_start_date "
                + "from employee as e "
                + "left join hr_employee_order as eo on eo.employee_id=e.id and eo.to_date IS NULL "
                + "left join hr_orders as ord on ord.id=eo.hr_orders_id "
                + "left join school as sch on eo.school_id=sch.id "
                + "left join year as y on sch.year_id=y.id "
                + "where e.login=? and ord.working_status_id IS NOT NULL";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, login);
        ResultSet result = stat.executeQuery();
        UserDetails user = new UserDetails();
        while (result.next()) {
            user.setId(result.getInt("e.id"));
            user.setLogin(result.getString("login"));
            user.setFullname(result.getString("fullname"));
            user.setWorking_status_id(result.getInt("ord.working_status_id"));
            user.setSchool_id(result.getInt("eo.school_id"));
            user.setSchool_type_id(result.getInt("sch.school_type_id"));
            user.setSchool_name(result.getString("sch.name"));
            user.setSchool_code(result.getString("sch.code"));
            user.setSchool_logo(result.getString("sch.photo"));
            user.setCurrent_year(new Definition(result.getInt("y.id"), result.getString("y.name")));
            user.setTransactions_start_date(result.getDate("sch.transactions_start_date"));
        }
        return user;
    }

    public String execSQL_pass(String u_id) throws SQLException {
        String sql = "select  u.password from employee as u where u.login = ?;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, u_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getString("u.password");
        }
        return null;
    }

    public int editPass(String u_name, String pass) throws SQLException {
        String sql = "update employee set password=? where login=?;";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, pass);
        stat.setString(2, u_name);
        return stat.executeUpdate();
    }
}
