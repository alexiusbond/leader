/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import kg.alex.spt.domain.EmployeeExtraInfo;

public class DbEmployeeExtraInfo extends BaseDb {

    public DbEmployeeExtraInfo() throws Exception {
        super();
    }

    public int exec_insert(EmployeeExtraInfo eei) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_extra_info (employee_id,hr_health_status_id,"
                + "hobbies,fobbies,health_notes,info) VALUES(?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, eei.getEmployee_id());
        stat.setInt(2, eei.getHealth_status_id());
        if (!eei.getHobbies().equals("") && eei.getHobbies() != null) {
            stat.setString(3, eei.getHobbies());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (!eei.getFobbies().equals("") && eei.getFobbies() != null) {
            stat.setString(4, eei.getFobbies());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        if (!eei.getHealth_notes().equals("") && eei.getHealth_notes() != null) {
            stat.setString(5, eei.getHealth_notes());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        if (!eei.getShort_notes().equals("") && eei.getShort_notes() != null) {
            stat.setString(6, eei.getShort_notes());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeExtraInfo eei) throws SQLException {
        String sql = "update hr_employee_extra_info set "
                + "hr_health_status_id=?, hobbies=?, fobbies=?, health_notes=?, info=? WHERE employee_id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, eei.getHealth_status_id());
        if (!eei.getHobbies().equals("") && eei.getHobbies() != null) {
            stat.setString(2, eei.getHobbies());
        } else {
            stat.setNull(2, Types.VARCHAR);
        }
        if (!eei.getFobbies().equals("") && eei.getFobbies() != null) {
            stat.setString(3, eei.getFobbies());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (!eei.getHealth_notes().equals("") && eei.getHealth_notes() != null) {
            stat.setString(4, eei.getHealth_notes());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        if (!eei.getShort_notes().equals("") && eei.getShort_notes() != null) {
            stat.setString(5, eei.getShort_notes());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        stat.setInt(6, eei.getEmployee_id());
        return stat.executeUpdate();
    }

    public EmployeeExtraInfo execSQL(int employee_id) throws SQLException {
        String sql = "SELECT * FROM hr_employee_extra_info AS eei LEFT JOIN hr_health_status AS hs ON hs.id = eei.hr_health_status_id WHERE eei.employee_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        EmployeeExtraInfo eei = null;
        while (result.next()) {
            eei = new EmployeeExtraInfo();
            eei.setHobbies(result.getString("eei.hobbies"));
            eei.setFobbies(result.getString("eei.fobbies"));
            eei.setShort_notes(result.getString("eei.info"));
            eei.setHealth_notes(result.getString("eei.health_notes"));
            eei.setHealth_status_id(result.getInt("hs.id"));
            break;
        }
        return eei;
    }
}
