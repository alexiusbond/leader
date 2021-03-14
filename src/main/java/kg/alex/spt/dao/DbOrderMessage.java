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
import kg.alex.spt.domain.OrderMessage;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;

import java.sql.*;

public class DbOrderMessage extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbOrderMessage.class);

    public DbOrderMessage() throws Exception {
        super();
    }

    public int exec_insert(OrderMessage d) throws SQLException {
        String sql = "INSERT INTO discount_order_messages (message, order_number, " +
                "order_title, order_content, student_id, creation_date, employee_id) "
                + "VALUES(?,?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (d.getMessage() != null) {
            stat.setString(1, d.getMessage());
        } else {
            stat.setNull(1, Types.VARCHAR);
        }
        stat.setString(2, d.getOrder_number());
        stat.setString(3, d.getTitle());
        stat.setString(4, d.getContent());
        stat.setInt(5, d.getStudent_id());
        stat.setDate(6, new Date(d.getDate().getTime()));
        stat.setInt(7, d.getEmployee_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public void execSQL(MyVaadinUI myUi, int employee_id, FilterTable t) throws SQLException {
        String sql = "SELECT em.id, concat(e.name, ' ', e.surname) as employee, " +
                "concat(st.name, ' ', st.surname) as student, " +
                "om.creation_date, om.order_number, mst.id, mst.name FROM employee_message AS em " +
                "left join employee as e on e.id = em.employee_id " +
                "left join message_status as mst on mst.id = em.message_status_id " +
                "left join order_messages as om on om.id = em.order_messages_id " +
                "left join student as st on st.id = om.student_id ";
        if (employee_id != 0) {
            sql += " WHERE em.employee_id = ?";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);

        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Employee), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.NumberAndDate), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Student), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Status), String.class, null);
        container.addContainerProperty(SystemSettings.status_id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("em.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Date)).setValue(SystemSettings.dtmf.format(
                    result.getTimestamp("om.creation_date")));
            item.getItemProperty(myUi.getMessage(SptMessages.Employee)).setValue(result.getString("employee"));
            item.getItemProperty(myUi.getMessage(SptMessages.Student)).setValue(result.getString("student"));
            item.getItemProperty(myUi.getMessage(SptMessages.NumberAndDate)).setValue(result.getString("om.order_number"));
            item.getItemProperty(myUi.getMessage(SptMessages.Status)).setValue(result.getString("mst.name"));
            item.getItemProperty(SystemSettings.status_id).setValue(result.getInt("mst.id"));
        }
        t.setContainerDataSource(container);
    }
}
