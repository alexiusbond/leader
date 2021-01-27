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
import kg.alex.spt.domain.School;
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.i18n.SptMessages;

public class DbSchool extends BaseDb {

    public DbSchool() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT s.id, s.code, s.name, s.name_ru, s.year_id,y.name, "
                + "s.activity_status_id, ac.name, s.director_fullname, s.city, s.adress, "
                + "s.inn, s.bank, s.bank_account, s.phone, s.photo, s.school_type_id FROM school as s "
                + "left join year as y on y.id = s.year_id "
                + "left join activity_status as ac on ac.id = s.activity_status_id "
                + "order by CAST(s.code AS UNSIGNED)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Code), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Name), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.NameRu), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Status), String.class, null);
        container.addContainerProperty(sysSettings.status_id, Integer.class, 0);
        container.addContainerProperty(sysSettings.year_id, Integer.class, 0);
        container.addContainerProperty(sysSettings.school_type_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.Year), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.DirectorFullName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.City), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Address), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.INN), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Bank), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.BankAccount), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Phone), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Logo), String.class, null);
        container.addContainerProperty(sysSettings.id, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Code)).setValue(
                    result.getString("s.code"));
            item.getItemProperty(myUi.getMessage(SptMessages.Name)).setValue(
                    result.getString("s.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.NameRu)).setValue(
                    result.getString("s.name_ru"));
            item.getItemProperty(myUi.getMessage(SptMessages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(sysSettings.status_id).setValue(
                    result.getInt("s.activity_status_id"));
            item.getItemProperty(sysSettings.year_id).setValue(
                    result.getInt("s.year_id"));
            item.getItemProperty(sysSettings.school_type_id).setValue(
                    result.getInt("s.school_type_id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Year)).setValue(
                    result.getString("y.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.DirectorFullName)).setValue(
                    result.getString("s.director_fullname"));
            item.getItemProperty(myUi.getMessage(SptMessages.City)).setValue(
                    result.getString("s.city"));
            item.getItemProperty(myUi.getMessage(SptMessages.Address)).setValue(
                    result.getString("s.adress"));
            item.getItemProperty(myUi.getMessage(SptMessages.INN)).setValue(
                    result.getString("s.inn"));
            item.getItemProperty(myUi.getMessage(SptMessages.Bank)).setValue(
                    result.getString("s.bank"));
            item.getItemProperty(myUi.getMessage(SptMessages.BankAccount)).setValue(
                    result.getString("s.bank_account"));
            item.getItemProperty(myUi.getMessage(SptMessages.Phone)).setValue(
                    result.getString("s.phone"));
            item.getItemProperty(myUi.getMessage(SptMessages.Logo)).setValue(
                    result.getString("s.photo"));
            item.getItemProperty(sysSettings.id).setValue(result.getInt("s.id"));
        }
        return container;
    }

    public int exec_update(School scl) throws SQLException {
        String sql = "UPDATE school SET code=?,name=?,name_ru=?,"
                + "activity_status_id=?,director_fullname=?,adress=?,"
                + "inn=?,bank=?,bank_account=?,phone=?,photo=?,city=?,school_type_id=? WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, scl.getCode());
        stat.setString(2, scl.getName());
        if (scl.getName_ru() != null && !scl.getName_ru().equals("")) {
            stat.setString(3, scl.getName_ru());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        stat.setInt(4, scl.getStatus_id());
        if (scl.getDirector_f_name() != null && !scl.getDirector_f_name().equals("")) {
            stat.setString(5, scl.getDirector_f_name());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        if (scl.getAddress() != null && !scl.getAddress().equals("")) {
            stat.setString(6, scl.getAddress());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        if (scl.getInn() != null && !scl.getInn().equals("")) {
            stat.setString(7, scl.getInn());
        } else {
            stat.setNull(7, Types.VARCHAR);
        }
        if (scl.getBank() != null && !scl.getBank().equals("")) {
            stat.setString(8, scl.getBank());
        } else {
            stat.setNull(8, Types.VARCHAR);
        }
        if (scl.getBank_account() != null && !scl.getBank_account().equals("")) {
            stat.setString(9, scl.getBank_account());
        } else {
            stat.setNull(9, Types.VARCHAR);
        }
        if (scl.getPhone() != null && !scl.getPhone().equals("")) {
            stat.setString(10, scl.getPhone());
        } else {
            stat.setNull(10, Types.VARCHAR);
        }
        if (scl.getPhoto() != null && !scl.getPhoto().equals("")) {
            stat.setString(11, scl.getPhoto());
        } else {
            stat.setNull(11, Types.VARCHAR);
        }
        if (scl.getCity() != null && !scl.getCity().equals("")) {
            stat.setString(12, scl.getCity());
        } else {
            stat.setNull(12, Types.VARCHAR);
        }
        stat.setInt(13, scl.getSchool_type_id());
        stat.setInt(14, scl.getId());

        int status = stat.executeUpdate();
        return status;
    }

    public int exec_insert(School scl) throws SQLException {
        String sql = "INSERT IGNORE INTO school (code,name,name_ru,"
                + "year_id,activity_status_id,director_fullname,city,adress,inn,bank,"
                + "bank_account,phone,photo,school_type_id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, scl.getCode());
        stat.setString(2, scl.getName());
        if (scl.getName_ru() != null && !scl.getName_ru().equals("")) {
            stat.setString(3, scl.getName_ru());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        stat.setInt(4, scl.getYear_id());
        stat.setInt(5, scl.getStatus_id());
        if (scl.getDirector_f_name() != null && !scl.getDirector_f_name().equals("")) {
            stat.setString(6, scl.getDirector_f_name());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        if (scl.getCity() != null && !scl.getCity().equals("")) {
            stat.setString(7, scl.getCity());
        } else {
            stat.setNull(7, Types.VARCHAR);
        }
        if (scl.getAddress() != null && !scl.getAddress().equals("")) {
            stat.setString(8, scl.getAddress());
        } else {
            stat.setNull(8, Types.VARCHAR);
        }
        if (scl.getInn() != null && !scl.getInn().equals("")) {
            stat.setString(9, scl.getInn());
        } else {
            stat.setNull(9, Types.VARCHAR);
        }
        if (scl.getBank() != null && !scl.getBank().equals("")) {
            stat.setString(10, scl.getBank());
        } else {
            stat.setNull(10, Types.VARCHAR);
        }
        if (scl.getBank_account() != null && !scl.getBank_account().equals("")) {
            stat.setString(11, scl.getBank_account());
        } else {
            stat.setNull(11, Types.VARCHAR);
        }
        if (scl.getPhone() != null && !scl.getPhone().equals("")) {
            stat.setString(12, scl.getPhone());
        } else {
            stat.setNull(12, Types.VARCHAR);
        }
        if (scl.getPhoto() != null && scl.getPhoto().equals("")) {
            stat.setString(13, scl.getPhoto());
        } else {
            stat.setNull(13, Types.VARCHAR);
        }
        stat.setInt(14, scl.getSchool_type_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public School execSchool(int scl_id) throws SQLException {
        School scl = new School();
        String sql = "SELECT s.id, s.code, s.name, s.name_ru, s.year_id,y.name, "
                + "s.activity_status_id, ac.name, s.director_fullname, s.city, s.adress, "
                + "s.inn, s.bank, s.bank_account, s.phone, s.photo, s.school_type_id FROM school as s "
                + "left join year as y on y.id = s.year_id "
                + "left join activity_status as ac on ac.id = s.activity_status_id "
                + "where s.id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            scl.setAddress(result.getString("s.adress"));
            scl.setBank(result.getString("s.bank"));
            scl.setBank_account(result.getString("s.bank_account"));
            scl.setCode(result.getString("s.code"));
            scl.setDirector_f_name(result.getString("s.director_fullname"));
            scl.setCity(result.getString("s.city"));
            scl.setInn(result.getString("s.inn"));
            scl.setName(result.getString("s.name"));
            scl.setName_ru(result.getString("s.name_ru"));
            scl.setPhone(result.getString("s.phone"));
            scl.setStatus_id(result.getInt("s.activity_status_id"));
            scl.setSchool_type_id(result.getInt("s.school_type_id"));
            scl.setPhoto(result.getString("s.photo"));
        }
        return scl;
    }

    public int execUpdateYear(School scl) throws SQLException {
        String sql = "update school set year_id = ? where id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl.getYear_id());
        stat.setInt(2, scl.getId());
        int st = stat.executeUpdate();
        return st;
    }

    public IndexedContainer execSchoolSel(MyVaadinUI myUI, int except_id) throws SQLException {
        String sql = "SELECT s.id, concat(s.code, ' - ', s.name) as name, s.year_id, s.photo, s.code from school as s " +
                "where s.id!=? order by CAST(s.code AS UNSIGNED)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, except_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        SystemSettings sysSettings = new SystemSettings();
        container.addContainerProperty(myUI.getMessage(SptMessages.Name), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Code), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Logo), String.class, null);
        container.addContainerProperty(sysSettings.year_id, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(result.getString("name"));
            item.getItemProperty(myUI.getMessage(SptMessages.Code)).setValue(result.getString("s.code"));
            item.getItemProperty(myUI.getMessage(SptMessages.Logo)).setValue(result.getString("s.photo"));
            item.getItemProperty(sysSettings.year_id).setValue(result.getInt("s.year_id"));
        }
        return container;
    }

    public String execGet_logo(String st_login) throws SQLException {
        String sql = "SELECT sc.photo FROM school as sc "
                + "left join student as st on st.school_id = sc.id "
                + "where st.login = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, st_login);
        ResultSet result = stat.executeQuery();
        String logo = null;
        while (result.next()) {
            logo = result.getString("sc.photo");
        }
        return logo;
    }

    public StudInfoPdf execGetSchoolPdf(int scl_id) throws SQLException {
        StudInfoPdf st = new StudInfoPdf();
        String sql = "SELECT sc.id, sc.name, sc.adress, sc.phone, sc.director_fullname, "
                + "concat(e.surname, ' ', e.name) as fullname FROM school as sc "
                + "LEFT JOIN hr_employee_order AS eo ON sc.id = eo.school_id AND eo.to_date IS NULL "
                + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id "
                + "LEFT JOIN hr_position AS hrp ON eo.hr_position_id = hrp.id "
                + "LEFT JOIN position AS p ON hrp.id = p.hr_position_id "
                + "LEFT JOIN employee AS e ON eo.employee_id = e.id "
                + "where sc.id = ? AND ord.working_status_id = 2 and p.id = 2 limit 1;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            st.setScl_name_ru(result.getString("sc.name"));
            st.setScl_address(result.getString("sc.adress"));
            st.setScl_phone(result.getString("sc.phone"));
            st.setScl_accountent_fullname(result.getString("fullname"));
            st.setScl_dir_f_name(result.getString("sc.director_fullname"));
        }
        return st;
    }

    public int execGetCurrentDbSchoolYear(int scl_id) throws SQLException {
        String sql = "SELECT year_id from school where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        int i = 0;
        while (result.next()) {
            i = result.getInt("year_id");
        }
        return i;
    }
}
