/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbExam extends BaseDb {
    
    public DbExam() throws Exception {
        super();
    }
    
    public int exec_main_exam() throws SQLException {
        String sql = "select t.id from hr_exam as t where t.is_main=1;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getInt("t.id");
        }
        return 0;
    }

    public int exec_update(int id) throws SQLException {
        String sql = "update hr_exam set is_main = 0;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.executeUpdate();
        sql = "update hr_exam set is_main = 1 where id = ?;";
        stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        int status = stat.executeUpdate();
        return status;
    }
}
