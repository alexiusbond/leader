/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import kg.alex.spt.domain.StudentContractCorrection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DbStudentContractCorrection extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbStudentContractCorrection.class);

    public DbStudentContractCorrection() throws Exception {
        super();
    }
/*
    public IndexedContainer execSQL(MyVaadinUI myUI, int client_id, MyViewUsable v) throws SQLException {
        Subject currentUser = SecurityUtils.getSubject();
        String sql = "SELECT t.id, t.registration_date, t.correction_type_id, t.amount, t.note, t.client_contract_id, " +
                "CONCAT(e.surname, ' ', e.name) AS employee FROM client_contract_correction AS t " +
                "LEFT JOIN client_contract AS cc ON t.client_contract_id = cc.id " +
                "LEFT JOIN employee AS e ON t.employee_id = e.id WHERE cc.client_id = ? " +
                "ORDER BY cc.id, t.registration_date DESC";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, client_id);
        ResultSet result = stat.executeQuery();

        IndexedContainer container = v.prepareContainer(myUI.getMessage(SptMessages.Correction));
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(SystemSettings.button).setValue(
                    v.createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbStudentContractCorrection, FontAwesome.TRASH_O,
                            currentUser.isPermitted(SystemSettings.correctionsTab + ":" + SystemSettings.prmDelete)));
            ComboBoxMax cb = v.createCombobox(0, null, myUI.getMessage(SptMessages.CorrectionType),
                    SystemSettings.dbCorrection_type, null, true, null, null,
                    currentUser.isPermitted(SystemSettings.correctionsTab + ":" + SystemSettings.prmModify));
            try {
                DbDefinition dbCon = new DbDefinition();
                dbCon.connect();
                cb.setContainerDataSource(dbCon.exec_correction_types(myUI));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setValue(result.getInt("t.correction_type_id"));
            item.getItemProperty(myUI.getMessage(SptMessages.CorrectionType)).setValue(cb);
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                    SystemSettings.df.format(result.getTimestamp("t.registration_date")));
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                    v.createTextField(result.getDouble("t.amount"), myUI.getMessage(SptMessages.Amount), null,
                            new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 1.0, null),
                            true, null, currentUser.isPermitted(SystemSettings.correctionsTab + ":" + SystemSettings.prmModify),
                            null, null, new ObjectProperty<Double>(0.0),
                            SystemSettings.getStringToDoubleConverter()));
            item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                    v.createTextField(result.getString("t.note"), myUI.getMessage(SptMessages.Note),
                            null, new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue),
                                    null, 250, true), true, null,
                            currentUser.isPermitted(SystemSettings.correctionsTab + ":" + SystemSettings.prmModify),
                            null, null, null, null));
            item.getItemProperty(myUI.getMessage(SptMessages.Employee)).setValue(result.getString("employee"));
            item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }*/

    public int exec_insert(StudentContractCorrection cc) throws SQLException {
        String sql = "INSERT INTO client_contract_correction "
                + "(client_contract_id, correction_type_id, amount, registration_date, "
                + "employee_id, modification_date, note) "
                + "VALUES(?,?,?,?,?,NOW(),?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cc.getStudent_contract_id());
        stat.setInt(2, cc.getCorrection_type_id());
        stat.setDouble(3, cc.getAmount());
        stat.setDate(4, new java.sql.Date(cc.getRegistration_date().getTime()));
        stat.setInt(5, cc.getEmployee_id());
        if (cc.getNote() != null && !cc.getNote().equals("")) {
            stat.setString(6, cc.getNote());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(StudentContractCorrection cc) throws SQLException {
        String sql = "update client_contract_correction set client_contract_id = ?, "
                + "correction_type_id = ?, amount = ?, "
                + "registration_date = ?, note = ? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cc.getStudent_contract_id());
        stat.setInt(2, cc.getCorrection_type_id());
        stat.setDouble(3, cc.getAmount());
        stat.setDate(4, new java.sql.Date(cc.getRegistration_date().getTime()));
        if (cc.getNote() != null && !cc.getNote().equals("")) {
            stat.setString(5, cc.getNote());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        stat.setString(6, cc.getId());
        stat.executeUpdate();
        return stat.getUpdateCount();
    }
}
