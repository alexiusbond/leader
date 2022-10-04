/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.domain.EmployeeContract;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.EmployeeDefinitionView;
import org.vaadin.hene.popupbutton.PopupButton;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbEmployeeContract extends BaseDb {

    public DbEmployeeContract() throws Exception {
        super();
    }

    public int exec_insert(EmployeeContract ec) throws SQLException {
        String sql = "INSERT INTO hr_employee_contract (employee_id, contract_type_id, salary, from_date, " +
                "till_date, creation_date) VALUES(?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ec.getEmployee_id());
        stat.setInt(2, ec.getContract_type_id());
        stat.setDouble(3, ec.getSalary());
        stat.setDate(4, new Date(ec.getFromDate().getTime()));
        stat.setDate(5, new Date(ec.getTillDate().getTime()));
        stat.setDate(6, new Date(ec.getCreationDate().getTime()));
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeContract ec) throws SQLException {
        String sql = "update hr_employee_contract set "
                + "contract_type_id = ?, salary = ?, from_date = ?, till_date = ?, creation_date = ? WHERE id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ec.getContract_type_id());
        stat.setDouble(2, ec.getSalary());
        stat.setDate(3, new Date(ec.getFromDate().getTime()));
        stat.setDate(4, new Date(ec.getTillDate().getTime()));
        stat.setDate(5, new Date(ec.getCreationDate().getTime()));
        stat.setInt(6, ec.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {


        String sql = "SELECT ec.id, ec.contract_type_id, ec.salary, ec.from_date, ec.till_date, ec.creation_date "
                + "FROM hr_employee_contract as ec where ec.employee_id = ? ;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareContractsContainer();
        while (result.next()) {
            String id = result.getString("ec.id");
            Item item = container.addItem(id);
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            hl.addComponent(edv.createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                    null, FontAwesome.MINUS_SQUARE));
            PopupButton btn = new PopupButton();
            btn.setDescription(myUI.getMessage(SptMessages.Print));
            btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btn.addStyleName(ValoTheme.BUTTON_TINY);
            btn.setIcon(FontAwesome.PRINT);
            btn.setData(id);
            btn.addClickListener(edv);
            btn.setContent(edv.getContractExtraInfoLay());
            hl.addComponent(btn);
            item.getItemProperty(Settings.button).setValue(hl);
            item.getItemProperty(myUI.getMessage(SptMessages.AgreementType)).setValue(
                    edv.createCombobox(result.getInt("ec.contract_type_id"),
                            myUI.getMessage(SptMessages.AgreementType), Settings.dbContractType, true));
            item.getItemProperty(myUI.getMessage(SptMessages.SalaryAmount)).setValue(
                    edv.createTextFieldWithProperty(result.getDouble("ec.salary"),
                            myUI.getMessage(SptMessages.SalaryAmount),
                            new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue),
                                    0.01, null), new ObjectProperty<>(0.0),
                            Settings.getStringToDoubleConverter(2)));
            item.getItemProperty(myUI.getMessage(SptMessages.Start)).setValue(
                    edv.createDateField(result.getDate("ec.from_date"),
                            myUI.getMessage(SptMessages.Start), null, true,
                            Settings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(SptMessages.CreationDate)).setValue(
                    edv.createDateField(result.getDate("ec.creation_date"),
                            myUI.getMessage(SptMessages.CreationDate), null, true,
                            Settings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(SptMessages.End)).setValue(
                    edv.createDateField(result.getDate("ec.till_date"),
                            myUI.getMessage(SptMessages.End), null, true,
                            Settings.datePattern, Resolution.DAY));
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }
}
