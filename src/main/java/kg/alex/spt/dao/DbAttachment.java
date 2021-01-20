/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import kg.alex.spt.domain.Attachment;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbAttachment extends BaseDb {

    public DbAttachment() throws Exception {
        super();
    }

    public int exec_insert(Attachment a) throws SQLException {
        String sql = "INSERT INTO hr_attachments (name,extension,unique_name) "
                + "VALUES(?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, a.getName());
        stat.setString(2, a.getExtension());
        stat.setString(3, a.getUnique_name());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }
}
