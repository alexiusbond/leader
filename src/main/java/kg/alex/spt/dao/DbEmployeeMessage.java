/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.EmployeeMessage;
import kg.alex.spt.domain.OrderMessage;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.pdf.OrderPdf;
import kg.alex.spt.ui.SendOrderView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbEmployeeMessage extends BaseDb {
    static final Logger logger = LogManager.getLogger(DbEmployeeMessage.class);

    public DbEmployeeMessage() throws Exception {
        super();
    }

    public int exec_insert(EmployeeMessage em) throws SQLException {
        String sql = "INSERT INTO employee_message (order_messages_id, employee_id, " +
                "message_status_id, modification_date) "
                + "VALUES(?,?,?,NOW());";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, em.getOrder_message_id());
        stat.setInt(2, em.getEmployee_id());
        stat.setInt(3, em.getMessage_status_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(int order_messages_id, int employee_id, int status_id) throws SQLException {
        String sql = "update employee_message set message_status_id = ?, " +
                "modification_date = NOW() where order_messages_id = ? and employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, status_id);
        stat.setInt(2, order_messages_id);
        stat.setInt(3, employee_id);
        int status = stat.executeUpdate();
        return status;
    }

    public void execSQL(MyVaadinUI myUi, int employee_id, FilterTable t) throws SQLException {
        String sql = "SELECT om.id, e.id, concat(st.name, ' ', st.surname) as student, " +
                "om.creation_date, om.order_number, om.message, om.order_content, om.order_title, " +
                "mst.id, mst.name FROM employee_message AS em " +
                "left join employee as e on e.id = em.employee_id " +
                "left join message_status as mst on mst.id = em.message_status_id " +
                "left join order_messages as om on om.id = em.order_messages_id " +
                "left join student as st on st.id = om.student_id " +
                "WHERE em.employee_id = ? order by mst.id desc, om.creation_date desc";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.OrderNumber), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Message), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Student), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Status), String.class, null);
        container.addContainerProperty(SystemSettings.button, Button.class, null);
        container.addContainerProperty(SystemSettings.status_id, Integer.class, 0);
        int unread = 0;
        while (result.next()) {
            Item item = container.addItem(result.getInt("om.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Date)).setValue(SystemSettings.df.format(
                    result.getDate("om.creation_date")));
            item.getItemProperty(myUi.getMessage(SptMessages.Student)).setValue(result.getString("student"));
            item.getItemProperty(myUi.getMessage(SptMessages.Message)).setValue(result.getString("om.message"));
            item.getItemProperty(myUi.getMessage(SptMessages.OrderNumber)).setValue(result.getString("om.order_number"));
            item.getItemProperty(myUi.getMessage(SptMessages.Status)).setValue(result.getString("mst.name"));
            item.getItemProperty(SystemSettings.status_id).setValue(result.getInt("mst.id"));
            OrderMessage orderMessage = new OrderMessage();
            orderMessage.setId(result.getInt("om.id"));
            orderMessage.setEmployee_id(result.getInt("e.id"));
            orderMessage.setTitle(result.getString("om.order_title"));
            orderMessage.setContent(result.getString("om.order_content"));
            orderMessage.setOrder_number(result.getString("om.order_number"));
            orderMessage.setDate(result.getDate("om.creation_date"));
            Button btn = new Button();
            btn.setDescription(myUi.getMessage(SptMessages.ViewDocument));
            btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btn.addStyleName(ValoTheme.BUTTON_TINY);
            btn.setIcon(FontAwesome.FILE_PDF_O);
            btn.setData(orderMessage);
            btn.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    try {
                        DbEmployeeMessage dbCon = new DbEmployeeMessage();
                        dbCon.connect();
                        OrderMessage om = (OrderMessage) clickEvent.getButton().getData();
                        dbCon.exec_update(om.getId(), om.getEmployee_id(), 1);
                        dbCon.close();
                        t.getContainerProperty(om.getId(), SystemSettings.status_id).setValue(1);
                        t.getContainerProperty(om.getId(), myUi.getMessage(SptMessages.Status))
                                .setValue(myUi.getMessage(SptMessages.UnRead));
                        int unread = Integer.parseInt(t.getColumnFooter(SystemSettings.status_id));
                        if (unread > 0) {
                            t.setColumnFooter(SystemSettings.status_id, (--unread) + "");
                            t.setColumnFooter(myUi.getMessage(SptMessages.Status),
                                    myUi.getMessage(SptMessages.UnRead) + ": " + unread);
                        }
                        myUi.repaintMessagesButton();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    new OrderPdf(myUi, (OrderMessage) clickEvent.getButton().getData());
                }
            });
            item.getItemProperty(SystemSettings.button).setValue(btn);
            if (result.getInt("mst.id") == 2) {
                unread++;
            }
        }
        t.setContainerDataSource(container);
        t.setColumnFooter(SystemSettings.status_id, unread + "");
        t.setColumnFooter(myUi.getMessage(SptMessages.Status),
                myUi.getMessage(SptMessages.UnRead) + ": " + unread);
        t.setColumnFooter(myUi.getMessage(SptMessages.Message),
                myUi.getMessage(SptMessages.Total) + ": " + container.size());
    }

    public boolean isUnread(int employee_id) throws SQLException {
        String sql = "SELECT count(*) as val FROM employee_message " +
                "where employee_id = ? and message_status_id = 2;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            if (result.getInt("val") > 0) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
