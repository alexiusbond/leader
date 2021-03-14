/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import kg.alex.spt.domain.EmployeeMessage;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbEmployeeMessage extends BaseDb {

    public DbEmployeeMessage() throws Exception {
        super();
    }

    public int exec_insert(EmployeeMessage em) throws SQLException {
        String sql = "INSERT INTO employee_message (order_messages_id, employee_id, " +
                "message_status_id, modification_date) "
                + "VALUES(?,?,?,NOW());";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, em.getOrder_message_id());
        stat.setInt(2, em.getOrder_message_id());
        stat.setInt(3, em.getOrder_message_id());
        return stat.executeUpdate();
    }

    public int exec_update(EmployeeMessage em) throws SQLException {
        String sql = "update employee_message set message_status_id = ?, " +
                "modification_date = NOW() where order_message_id = ? " +
                "and employee_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, em.getMessage_status_id());
        stat.setInt(2, em.getOrder_message_id());
        stat.setInt(3, em.getEmployee_id());
        int status = stat.executeUpdate();
        return status;
    }
}
