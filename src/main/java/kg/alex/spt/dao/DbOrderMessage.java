/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.HorizontalLayout;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.domain.EmployeeMessage;
import kg.alex.spt.domain.OrderMessage;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.SendOrderView;
import org.tepi.filtertable.FilterTable;

import java.sql.*;

public class DbOrderMessage extends BaseDb {

    public DbOrderMessage() throws Exception {
        super();
    }

    public int exec_insert(OrderMessage d) throws SQLException {
        String sql = "INSERT INTO order_messages (message, order_number, " +
                "order_title, order_content, student_id, creation_date, employee_id, discount, student) "
                + "VALUES(?,?,?,?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (d.getMessage() != null) {
            stat.setString(1, d.getMessage());
        } else {
            stat.setNull(1, Types.VARCHAR);
        }
        stat.setString(2, d.getOrder_number());
        stat.setString(3, d.getTitle());
        stat.setString(4, d.getContent());
        if (d.getStudent_id() != 0) {
            stat.setInt(5, d.getStudent_id());
        } else {
            stat.setNull(5, Types.INTEGER);
        }
        stat.setDate(6, new Date(d.getDate().getTime()));
        stat.setInt(7, d.getEmployee_id());
        stat.setDouble(8, d.getDiscount());
        if (d.getStudent() != null) {
            stat.setString(9, d.getStudent());
        } else {
            stat.setNull(9, Types.VARCHAR);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int employee_id, FilterTable t, SendOrderView view) throws SQLException {
        String sql = "SELECT em.id, concat(e.name, ' ', e.surname) as employee, " +
                "concat(st.name, ' ', st.surname) as student, om.id, om.discount, om.student as student_info, " +
                "om.creation_date, om.order_number, om.message, om.order_content, om.order_title, om.student_id, " +
                "mst.id, mst.name FROM employee_message AS em " +
                "left join employee as e on e.id = em.employee_id " +
                "left join message_status as mst on mst.id = em.message_status_id " +
                "left join order_messages as om on om.id = em.order_messages_id " +
                "left join student as st on st.id = om.student_id ";
        if (employee_id != 0) {
            sql += " WHERE om.employee_id = ? ";
        }
        sql += "order by om.creation_date desc, CAST(substring(order_number, 9, 15) AS SIGNED) desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (employee_id != 0) {
            stat.setInt(1, employee_id);
        }
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Employee), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.OrderNumber), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Message), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Student), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Discount), Integer.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Status), String.class, null);
        container.addContainerProperty(Settings.button, HorizontalLayout.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        int unread = 0;
        while (result.next()) {
            Item item = container.addItem(result.getInt("em.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Date)).setValue(Settings.df.format(
                    result.getDate("om.creation_date")));
            item.getItemProperty(myUi.getMessage(SptMessages.Employee)).setValue(result.getString("employee"));
            if (result.getInt("om.student_id") == 0) {
                item.getItemProperty(myUi.getMessage(SptMessages.Student)).setValue(result.getString("student_info"));
            } else {
                item.getItemProperty(myUi.getMessage(SptMessages.Student)).setValue(result.getString("student"));
            }
            item.getItemProperty(myUi.getMessage(SptMessages.Discount)).setValue(result.getInt("discount"));
            item.getItemProperty(myUi.getMessage(SptMessages.Message)).setValue(result.getString("om.message"));
            item.getItemProperty(myUi.getMessage(SptMessages.Title)).setValue(result.getString("om.order_title"));
            item.getItemProperty(myUi.getMessage(SptMessages.OrderNumber)).setValue(result.getString("om.order_number"));
            item.getItemProperty(myUi.getMessage(SptMessages.Status)).setValue(result.getString("mst.name"));
            item.getItemProperty(Settings.status_id).setValue(result.getInt("mst.id"));
            EmployeeMessage employeeMessage = new EmployeeMessage();
            employeeMessage.setId(result.getInt("em.id"));
            employeeMessage.setOrder_message_id(result.getInt("om.id"));
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            hl.addComponent(view.createButton(myUi.getMessage(SptMessages.DeleteButton),
                    Settings.actDelete, FontAwesome.BAN, employeeMessage));
            OrderMessage orderMessage = new OrderMessage();
            orderMessage.setTitle(result.getString("om.order_title"));
            orderMessage.setContent(result.getString("om.order_content"));
            orderMessage.setOrder_number(result.getString("om.order_number"));
            orderMessage.setDate(result.getDate("om.creation_date"));
            hl.addComponent(view.createButton(myUi.getMessage(SptMessages.ViewDocument),
                    Settings.actPdf, FontAwesome.FILE_PDF_O, orderMessage));
            item.getItemProperty(Settings.button).setValue(hl);
            if (result.getInt("mst.id") == 2) {
                unread++;
            }
        }
        if (t != null) {
            t.setContainerDataSource(container);
            t.setColumnAlignment(myUi.getMessage(SptMessages.Discount), CustomTable.Align.RIGHT);
            t.setColumnFooter(myUi.getMessage(SptMessages.Title),
                    myUi.getMessage(SptMessages.UnRead) + ": " + unread);
            t.setColumnFooter(myUi.getMessage(SptMessages.Status),
                    myUi.getMessage(SptMessages.Total) + ": " + container.size());
        }
        return container;
    }
}
