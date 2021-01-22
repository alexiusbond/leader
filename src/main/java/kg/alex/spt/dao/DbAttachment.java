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
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.EmployeeDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public IndexedContainer execSQL(MyVaadinUI myUi, int employee_id, EmployeeDefinitionView edv) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT a.id, a.name, a.unique_name, 'Сертификат' as type FROM hr_employee_certificate AS ec " +
                "LEFT JOIN hr_attachments a ON a.id = ec.attachment_id WHERE ec.employee_id = ? " +
                "UNION " +
                "SELECT a.id, a.name, a.unique_name, 'Экзамен' as type FROM hr_employee_exam AS ex " +
                "LEFT JOIN hr_attachments a ON a.id = ex.attachment_id WHERE ex.employee_id = ? " +
                "UNION " +
                "SELECT a.id, a.name, a.unique_name, 'Образование' as type FROM hr_employee_education AS ed " +
                "LEFT JOIN hr_attachments a ON a.id = ed.attachment_id " +
                "WHERE ed.employee_id = ? AND hr_own_id = 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, employee_id);
        stat.setInt(3, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Name), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Type), String.class, null);
        container.addContainerProperty(sysSettings.button, Button.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("id"));

            item.getItemProperty(myUi.getMessage(SptMessages.Name)).setValue(result.getString("name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Type)).setValue(result.getString("type"));
            Attachment a = new Attachment();
            a.setId(result.getInt("id"));
            a.setUnique_name(result.getString("unique_name"));
            a.setName(result.getString("name"));
            com.vaadin.ui.Button b = edv.createButton(myUi.getMessage(SptMessages.DownLoad), a.getId() + "",
                    sysSettings.download_button, FontAwesome.DOWNLOAD);
            b.setStyleName(ValoTheme.BUTTON_SMALL);
            b.setData(a);
            item.getItemProperty(sysSettings.button).setValue(b);

        }
        return container;
    }
}
