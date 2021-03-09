/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Messages;
import kg.alex.spt.i18n.SptMessages;

public class DbMessage extends BaseDb {

    public DbMessage() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT m.id, m.subject, m.message, m.modification_date, concat(e.name, ' ', e.surname) as fullname, sch.name_ru, "
                + "m.photo, m.feedback, e.id FROM messages as m "
                + "left join employee as e on e.id = m.employee_id "
                + "left join hr_employee_order as eo on eo.employee_id=e.id and eo.to_date IS NULL "
                + "left join hr_orders as ord on ord.id=eo.hr_orders_id "
                + "left join school as sch on eo.school_id=sch.id "
                + "where ord.working_status_id IS NOT NULL "
                + "order by m.modification_date DESC;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Subject), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Message), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Feedback), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Employee), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.School), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Photo), String.class, null);
        container.addContainerProperty(sysSettings.id, Integer.class, 0);
        container.addContainerProperty(sysSettings.employee_id, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("m.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Date)).setValue(
                    sysSettings.df.format(result.getDate("m.modification_date")));
            item.getItemProperty(myUi.getMessage(SptMessages.Subject)).setValue(
                    result.getString("m.subject"));
            item.getItemProperty(myUi.getMessage(SptMessages.Message)).setValue(
                    result.getString("m.message"));
            item.getItemProperty(myUi.getMessage(SptMessages.Feedback)).setValue(
                    result.getString("m.feedback"));
            item.getItemProperty(myUi.getMessage(SptMessages.Employee)).setValue(
                    result.getString("fullname"));
            item.getItemProperty(myUi.getMessage(SptMessages.School)).setValue(
                    result.getString("sch.name_ru"));
            item.getItemProperty(myUi.getMessage(SptMessages.Photo)).setValue(
                    result.getString("m.photo"));
            item.getItemProperty(sysSettings.id).setValue(result.getInt("m.id"));
            item.getItemProperty(sysSettings.employee_id).setValue(result.getInt("e.id"));
        }
        return container;
    }

    public int exec_update(Messages msg) throws SQLException {
        String sql = "UPDATE messages SET subject=?,message=?,photo=?,"
                + "feedback=? WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, msg.getSubject());
        stat.setString(2, msg.getMessage());
        if (msg.getPhoto() != null && !msg.getPhoto().equals("")) {
            stat.setString(3, msg.getPhoto());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (msg.getFeedback() != null && !msg.getFeedback().equals("")) {
            stat.setString(4, msg.getFeedback());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        stat.setInt(5, msg.getId());

        int status = stat.executeUpdate();
        return status;
    }

    public int exec_insert(Messages msg) throws SQLException {
        String sql = "INSERT IGNORE INTO messages (subject,message,photo,"
                + "employee_id,modification_date,feedback) VALUES(?,?,?,?,NOW(),?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, msg.getSubject());
        stat.setString(2, msg.getMessage());
        if (msg.getPhoto() != null && !msg.getPhoto().equals("")) {
            stat.setString(3, msg.getPhoto());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        stat.setInt(4, msg.getEmployee_id());
        if (msg.getFeedback() != null && !msg.getFeedback().equals("")) {
            stat.setString(5, msg.getFeedback());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }
}
