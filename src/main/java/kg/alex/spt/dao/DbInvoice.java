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
import kg.alex.spt.domain.Invoice;
import kg.alex.spt.i18n.SptMessages;

public class DbInvoice extends BaseDb {

    public DbInvoice() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id, int invoice_type_id) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT t.id, LPAD(t.invoice_number, 7, 0) as inv_num, t.creation_date, "
                + "sum(if(acr.acc_currency_id != 2, acr.amount/acr.currency_rate, acr.amount)) as amount, t.note "
                + "FROM acc_invoice AS t "
                + "LEFT JOIN acc_transfers AS acr ON acr.invoice_id = t.id "
                + "WHERE t.school_id = ? and acc_invoice_type_id = ? group by t.id ORDER BY t.invoice_number DESC;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, invoice_type_id);
        System.out.println(stat.toString());
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.InvoiceNumber), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUi.getMessage(SptMessages.Note), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.InvoiceNumber)).setValue(result.getString("inv_num"));
            item.getItemProperty(myUi.getMessage(SptMessages.Amount)).setValue(result.getDouble("amount"));
            item.getItemProperty(myUi.getMessage(SptMessages.Date)).setValue(sysSettings.dtmf.format(result.getTimestamp("t.creation_date")));
            item.getItemProperty(myUi.getMessage(SptMessages.Note)).setValue(result.getString("t.note"));
        }
        return container;
    }

    public String execSQL_invoice_number(int id) throws SQLException {
        String sql = "SELECT LPAD(invoice_number, 7, 0) as inv_num FROM acc_invoice WHERE id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getString("inv_num");
        }
        return null;
    }

    public int execSQL_max_invoice_number(int school_id, int acc_invoice_type_id) throws SQLException {
        String sql = "SELECT max(invoice_number) as inv_num FROM acc_invoice WHERE school_id = ? and acc_invoice_type_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, acc_invoice_type_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getInt("inv_num");
        }
        return 0;
    }

    public int exec_insert(Invoice inv) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "INSERT IGNORE INTO acc_invoice (invoice_number,creation_date,note,school_id,employee_id,"
                + "modification_date,acc_invoice_type_id) "
                + "VALUES(?,?,?,?,?,NOW(),?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inv.getInvoice_number());
        stat.setString(2, sysSettings.mysql_dtsf.format(inv.getCreation_date()));
        if (inv.getNote() != null) {
            stat.setString(3, inv.getNote());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        stat.setInt(4, inv.getSchool_id());
        stat.setInt(5, inv.getEmployee_id());
        stat.setInt(6, inv.getAcc_invoice_type_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Invoice inv) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "UPDATE acc_invoice SET creation_date = ?,"
                + "note = ?,employee_id = ? WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, sysSettings.mysql_dtsf.format(inv.getCreation_date()));
        if (inv.getNote() != null) {
            stat.setString(2, inv.getNote());
        } else {
            stat.setNull(2, Types.VARCHAR);
        }
        stat.setInt(3, inv.getEmployee_id());
        stat.setInt(4, inv.getId());
        int status = stat.executeUpdate();
        return status;
    }
}
