/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Exam;
import kg.alex.spt.i18n.SptMessages;

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


    public IndexedContainer execSQL(MyVaadinUI myUi) throws SQLException {

        String sql = "SELECT e.id, e.name, e.validity, "
                + "e.activity_status_id, ac.name FROM hr_exam as e "
                + "left join activity_status as ac on ac.id = e.activity_status_id "
                + "order by e.id desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.ValidityMonths), Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Status), String.class, null);
        container.addContainerProperty(SystemSettings.status_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("e.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(
                    result.getString("e.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.ValidityMonths)).setValue(
                    result.getInt("e.validity"));
            item.getItemProperty(myUi.getMessage(SptMessages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(SystemSettings.status_id).setValue(
                    result.getInt("e.activity_status_id"));
            item.getItemProperty(SystemSettings.id).setValue(result.getInt("e.id"));
        }
        return container;
    }

    public int exec_insert(Exam exam) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_exam (name,validity,activity_status_id) "
                + "VALUES(?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, exam.getName());
        stat.setInt(2, exam.getValidity());
        stat.setInt(3, exam.getStatus_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Exam exam) throws SQLException {
        String sql = "UPDATE hr_exam SET name = ?, validity = ?, activity_status_id = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, exam.getName());
        stat.setInt(2, exam.getValidity());
        stat.setInt(3, exam.getStatus_id());
        stat.setInt(4, exam.getId());
        int status = stat.executeUpdate();
        return status;
    }

}
