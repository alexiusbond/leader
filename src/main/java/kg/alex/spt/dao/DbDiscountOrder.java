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
import kg.alex.spt.domain.Attachment;
import kg.alex.spt.domain.DiscountOrder;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.EmployeeDefinitionView;

import java.sql.*;

public class DbDiscountOrder extends BaseDb {

    public DbDiscountOrder() throws Exception {
        super();
    }

    public int exec_insert(DiscountOrder d) throws SQLException {
        String sql = "INSERT INTO discount_order_messages (message, order_number, " +
                "order_title, order_content, student_id, " +
                "to_employee_id, message_status_id, creation_date, employee_id) "
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
        stat.setInt(5, d.getStudent_id());
        stat.setInt(6, d.getTo_employee_id());
        stat.setInt(7, d.getMessage_status_id());
        stat.setDate(8, new Date(d.getDate().getTime()));
        stat.setInt(9, d.getEmployee_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int employee_id, EmployeeDefinitionView edv) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT a.id, a.name, a.unique_name, concat(c.name, ' - ', ec.given_by) as details, 'Сертификат' as type " +
                "FROM hr_employee_certificate AS ec " +
                "LEFT JOIN hr_certificate c ON c.id = ec.certificate_id " +
                "LEFT JOIN hr_attachments a ON a.id = ec.attachment_id WHERE ec.employee_id = ? " +
                "UNION " +
                "SELECT a.id, a.name, a.unique_name, e.name as details, 'Экзамен' as type " +
                "FROM hr_employee_exam AS ex " +
                "LEFT JOIN hr_exam e ON e.id = ex.hr_exam_id " +
                "LEFT JOIN hr_attachments a ON a.id = ex.attachment_id WHERE ex.employee_id = ? " +
                "UNION " +
                "SELECT a.id, a.name, a.unique_name, concat(u.name, ' - ', el.name) as details, 'Образование' as type " +
                "FROM hr_employee_education AS ed " +
                "LEFT JOIN hr_university u ON u.id = ed.hr_university_id " +
                "LEFT JOIN hr_education_level el ON el.id = ed.education_level_id " +
                "LEFT JOIN hr_attachments a ON a.id = ed.attachment_id " +
                "WHERE ed.employee_id = ? AND hr_own_id = 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, employee_id);
        stat.setInt(3, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Name), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Details), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Type), String.class, null);
        container.addContainerProperty(sysSettings.button, Button.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("id"));

            item.getItemProperty(myUi.getMessage(SptMessages.Name)).setValue(result.getString("name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Type)).setValue(result.getString("type"));
            item.getItemProperty(myUi.getMessage(SptMessages.Details)).setValue(result.getString("details"));
            Attachment a = new Attachment();
            a.setId(result.getInt("id"));
            a.setUnique_name(result.getString("unique_name"));
            a.setName(result.getString("name"));
            Button b = edv.createButton(myUi.getMessage(SptMessages.DownLoad), a.getId() + "",
                    sysSettings.download_button, FontAwesome.DOWNLOAD);
            b.setStyleName(ValoTheme.BUTTON_SMALL);
            b.setData(a);
            item.getItemProperty(sysSettings.button).setValue(b);
        }
        return container;
    }
}
