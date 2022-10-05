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

import java.sql.*;

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

    public int exec_update(EmployeeContract ec, int id) throws SQLException {
        String sql = "update hr_employee_contract set " +
                "year_id = ?, probationary_period = ?, working_days = ?, working_hours = ?, " +
                "salary_day = ?, patent_date = ?, patent = ?, equipment = ? WHERE id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (ec.getYearId() != 0) {
            stat.setInt(1, ec.getYearId());
        } else {
            stat.setNull(1, Types.INTEGER);
        }
        if (ec.getProbationaryPeriod() != 0) {
            stat.setInt(2, ec.getProbationaryPeriod());
        } else {
            stat.setNull(2, Types.INTEGER);
        }
        if (ec.getWorkingDays() != 0) {
            stat.setInt(3, ec.getWorkingDays());
        } else {
            stat.setNull(3, Types.INTEGER);
        }
        if (ec.getWorkingHours() != 0) {
            stat.setInt(4, ec.getWorkingHours());
        } else {
            stat.setNull(4, Types.INTEGER);
        }
        if (ec.getSalaryDay() != 0) {
            stat.setInt(5, ec.getSalaryDay());
        } else {
            stat.setNull(5, Types.INTEGER);
        }
        if (ec.getPatentDate() != null) {
            stat.setDate(6, new Date(ec.getPatentDate().getTime()));
        } else {
            stat.setNull(6, Types.DATE);
        }
        if (ec.getPatent() != null) {
            stat.setString(7, ec.getPatent());
        } else {
            stat.setNull(7, Types.VARCHAR);
        }
        if (ec.getEquipment() != null) {
            stat.setString(8, ec.getEquipment());
        } else {
            stat.setNull(8, Types.VARCHAR);
        }
        stat.setInt(9, id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT ec.id, ec.contract_type_id, ec.salary, ec.from_date, ec.till_date, ec.creation_date, " +
                "ec.year_id, ec.probationary_period, ec.salary_day, ec.working_days, ec.working_hours, ec.patent, " +
                "ec.patent_date, ec.equipment, y.name FROM hr_employee_contract as ec " +
                "left join year as y on y.id = ec.year_id where ec.employee_id = ?";
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
            item.getItemProperty(myUI.getMessage(SptMessages.Start)).setValue(edv.createDateField(result.getDate("ec.from_date"),
                    myUI.getMessage(SptMessages.Start), null, true, Settings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(SptMessages.CreationDate)).setValue(edv.createDateField(result.getDate("ec.creation_date"),
                    myUI.getMessage(SptMessages.CreationDate), null, true, Settings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(SptMessages.End)).setValue(edv.createDateField(result.getDate("ec.till_date"),
                    myUI.getMessage(SptMessages.End), null, true, Settings.datePattern, Resolution.DAY));
            if (result.getInt("ec.probationary_period") != 0) {
                item.getItemProperty(myUI.getMessage(SptMessages.ProbationaryPeriod)).setValue(
                        result.getInt("ec.probationary_period"));
            }
            if (result.getInt("ec.year_id") != 0) {
                item.getItemProperty(myUI.getMessage(SptMessages.AcademicYear)).setValue(
                        result.getInt("ec.year_id"));
                item.getItemProperty(myUI.getMessage(SptMessages.Year)).setValue(
                        result.getString("y.name"));
            }
            if (result.getInt("ec.salary_day") != 0) {
                item.getItemProperty(myUI.getMessage(SptMessages.SalaryDay)).setValue(
                        result.getInt("ec.salary_day"));
            }
            if (result.getInt("ec.working_days") != 0) {
                item.getItemProperty(myUI.getMessage(SptMessages.WorkingDays)).setValue(
                        result.getInt("ec.working_days"));
            }
            if (result.getInt("ec.working_hours") != 0) {
                item.getItemProperty(myUI.getMessage(SptMessages.WorkingHours)).setValue(
                        result.getInt("ec.working_hours"));
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Patent)).setValue(
                    result.getString("ec.patent"));
            item.getItemProperty(myUI.getMessage(SptMessages.Equipment)).setValue(
                    result.getString("ec.equipment"));
            item.getItemProperty(myUI.getMessage(SptMessages.PatentDate)).setValue(
                    result.getDate("ec.patent_date"));
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }
}
