/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kg.alex.spt.domain.StudentInfoPdf;

public class DbStudentInfoPdf extends BaseDb {

    public DbStudentInfoPdf() throws Exception {
        super();
    }

    public StudentInfoPdf execSQL(int stud_id) throws SQLException {
        StudentInfoPdf sti = new StudentInfoPdf();
        String sql = "SELECT s.id, s.login, s.surname, s.name, s.middle_name, scl.city, scl.name_ru, scl.name_kg, scl.name_en, s.gender_id, "
                + "scl.director_fullname, scl.adress, scl.inn, scl.bank, scl.bank_account, "
                + "scl.school_type_id, scl.phone, sr.fullname, sr.phone, sr.passport, sr.adress, r.name_ru, r.name_ru_dec, "
                + "y.period, y.period_kg, y.name, "
                + "concat(cnum.name, ' - ',cn.name) as class_name "
                + "FROM student as s "
                + "left join school as scl on scl.id = s.school_id "
                + "left join student_relatives as sr on sr.student_id = s.id "
                + "left join relatives as r on r.id = sr.relatives_id "
                + "left join class_name as cn on cn.id = s.class_name_id "
                + "left join class_number as cnum on cn.class_number_id = cnum.id "
                + "left join year as y on scl.year_id = y.id "
                + "where s.id = ? and sr.is_main = 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            sti.setStud_id(result.getInt("s.id"));
            sti.setStud_login(result.getString("s.login"));
            sti.setStud_name(result.getString("s.name"));
            sti.setStud_surname(result.getString("s.surname"));
            if (result.getString("s.middle_name") == null
                    || result.getString("s.middle_name").equals("")) {
                sti.setStud_middle_name("");
            } else {
                sti.setStud_middle_name(result.getString("s.middle_name"));
            }
            if (result.getInt("s.gender_id") == 2) {
                sti.setIsStudentFeminitive(true);
            }
            sti.setScl_city(result.getString("scl.city"));
            sti.setScl_name_ru(result.getString("scl.name_ru"));
            sti.setScl_name_kg(result.getString("scl.name_kg"));
            sti.setScl_dir_f_name(result.getString("scl.director_fullname"));
            sti.setScl_address(result.getString("scl.adress"));
            sti.setScl_inn(result.getString("scl.inn"));
            sti.setScl_bank(result.getString("scl.bank"));
            sti.setScl_bank_account(result.getString("scl.bank_account"));
            sti.setScl_phone(result.getString("scl.phone"));
            sti.setScl_contr_type(result.getInt("scl.school_type_id"));
            sti.setScl_year_name(result.getString("y.name"));
            sti.setRel_fullname(result.getString("sr.fullname"));
            sti.setRel_phone(result.getString("sr.phone"));
            sti.setRel_address(result.getString("sr.adress"));
            sti.setRel_passport(result.getString("sr.passport"));
            sti.setRel_name(result.getString("r.name_ru"));
            sti.setRel_name_dec(result.getString("r.name_ru_dec"));
            sti.setPeriod(result.getString("y.period"));
            sti.setPeriod_kg(result.getString("y.period_kg"));
            sti.setClass_name(result.getString("class_name"));
        }
        return sti;
    }
}
