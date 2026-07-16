/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.leader.dao;

import kg.alex.leader.domain.ContractInfo;
import kg.alex.leader.domain.Student;
import kg.alex.leader.domain.StudentInfoPdf;
import kg.alex.leader.domain.StudentRelative;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbStudentInfoPdf extends BaseDb {

    public DbStudentInfoPdf() throws Exception {
        super();
    }

    public StudentInfoPdf execSQL(int year_id, int student_id) throws SQLException {
        StudentInfoPdf sti = new StudentInfoPdf();
        String sql = "SELECT s.id, s.login, s.photo, s.surname, s.name, s.gender_id, s.date_of_birth, sr.fullname, "
                + "sr.phone, sr.passport, CONCAT_WS(', ', ad.name, sr.address_line) as addr, r.name_ru, r.name_ru_dec, "
                + "y.period, y.period_kg, y.name, sc.contract_number, sc.creation_date, vcs.class_name "
                + "FROM student as s "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = s.id and vcs.year_id = ? "
                + "left join student_relatives as sr on sr.student_id = s.id "
                + "left join relatives as r on r.id = sr.relatives_id "
                + "left join address as adr on adr.id = sr.address_id "
                + "left join addable_titles as ad on ad.id = adr.city_id and ad.addable_types_id = 5 "
                + "left join year as y on y.id = ? "
                + "left join student_contract as sc on sc.student_id = s.id and sc.year_id = y.id "
                + "where s.id = ? and sr.is_main = 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, student_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            sti.setStudent(new Student());
            sti.setContractInfo(new ContractInfo());
            sti.setRelative(new StudentRelative());
            sti.getStudent().setId(result.getInt("s.id"));
            sti.getStudent().setLogin(result.getString("s.login"));
            sti.getStudent().setBirth_date(result.getDate("s.date_of_birth"));
            sti.getStudent().setPhoto(result.getString("s.photo"));
            sti.getStudent().setName(result.getString("s.name"));
            sti.getStudent().setSurname(result.getString("s.surname"));
            sti.getStudent().setGender_id(result.getInt("s.gender_id"));
            sti.getStudent().setClass_name(result.getString("vcs.class_name"));
            sti.getRelative().setFullName(result.getString("sr.fullname"));
            sti.getRelative().setPhone(result.getString("sr.phone"));
            sti.getRelative().setAddressLine(result.getString("addr"));
            sti.getRelative().setPassport(result.getString("sr.passport"));
            sti.getRelative().setRelativeTitle(result.getString("r.name_ru"));
            sti.getRelative().setRelativeDeclarative(result.getString("r.name_ru_dec"));
            sti.setPeriod(result.getString("y.period"));
            sti.setPeriod_kg(result.getString("y.period_kg"));
            sti.setYear(result.getString("y.name"));
            sti.getContractInfo().setContractNumber(result.getInt("sc.contract_number"));
            sti.getContractInfo().setCreationDate(result.getDate("sc.creation_date"));
        }
        return sti;
    }
}
