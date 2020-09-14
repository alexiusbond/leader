/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kg.alex.spt.domain.EmployeeContact;

/**
 *
 * @author eldiyar
 */
public class DbEmployeeContact extends BaseDb {

    public DbEmployeeContact() throws Exception {
        super();
    }

    public int exec_insert(EmployeeContact ec) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_contacts (employee_id,hr_country_id,"
                + "birth_place,passport,address,email) VALUES(?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ec.getEmployee_id());
        stat.setInt(2, ec.getCitizenship_id());
        stat.setString(3, ec.getBirth_place());
        stat.setString(4, ec.getPassport());
        stat.setString(5, ec.getAddress());
        stat.setString(6, ec.getEmail());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeContact ec) throws SQLException {
        String sql = "update hr_employee_contacts set "
                + "hr_country_id=?, birth_place=?, passport=?, address=?, email=? WHERE employee_id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ec.getCitizenship_id());
        stat.setString(2, ec.getBirth_place());
        stat.setString(3, ec.getPassport());
        stat.setString(4, ec.getAddress());
        stat.setString(5, ec.getEmail());
        stat.setInt(6, ec.getEmployee_id());
        return stat.executeUpdate();
    }

    public EmployeeContact execSQL(int employee_id) throws SQLException {
        String sql = "SELECT * FROM hr_employee_contacts AS ec LEFT JOIN hr_country AS c ON c.id = ec.hr_country_id WHERE ec.employee_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        EmployeeContact ec = null;
        while (result.next()) {
            ec = new EmployeeContact();
            ec.setAddress(result.getString("ec.address"));
            ec.setBirth_place(result.getString("ec.birth_place"));
            ec.setPassport(result.getString("ec.passport"));
            ec.setEmail(result.getString("ec.email"));
            ec.setCitizenship_id(result.getInt("c.id"));
            break;
        }
        return ec;
    }
}
