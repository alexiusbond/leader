/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

public class StudInfoPdf implements Serializable {

    private int stud_id;
    private String stud_login;
    private String stud_photo;
    private String stud_name;
    private String stud_sur_name;
    private String stud_middle_name;
    private String stud_class_name;
    private String scl_name_ru;
    private String scl_name_en;
    private String scl_name_kg;
    private int scl_contr_type;
    private String scl_year_name;
    private String scl_city;
    private String scl_dir_f_name;
    private String scl_address;
    private String scl_inn;
    private String scl_bank;
    private String scl_bank_account;
    private String scl_phone;
    private String scl_photo;
    private String scl_accountent_fullname;
    private String rel_fullname;
    private String rel_phone;
    private String rel_passport;
    private String rel_address;
    private String rel_name;
    private String rel_name_dec;
    private String period;
    private String period_kg;
    private String class_name;
    private String year;
    private double ctr_contract_sum;
    private double ctr_init_payment;
    private double ctr_ttl_left_sum;
    private double ctr_ttl_instplan_sum;
    private double ctr_instplan_debt;
    private double ctr_debt;
    private double ctr_k_oplate;
    private double ctr_paid;
    private String ctr_discountStr;
    private String ctr_discountPerc;
    private boolean isStudentFeminitive;

    public double getCtr_paid() {
        return ctr_paid;
    }

    public void setCtr_paid(double ctr_paid) {
        this.ctr_paid = ctr_paid;
    }

    public String getScl_year_name() {
        return scl_year_name;
    }

    public void setScl_year_name(String scl_year_name) {
        this.scl_year_name = scl_year_name;
    }

    public int getScl_contr_type() {
        return scl_contr_type;
    }

    public void setScl_contr_type(int scl_contr_type) {
        this.scl_contr_type = scl_contr_type;
    }

    public double getCtr_k_oplate() {
        return ctr_k_oplate;
    }

    public void setCtr_k_oplate(double ctr_k_oplate) {
        this.ctr_k_oplate = ctr_k_oplate;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getStud_photo() {
        return stud_photo;
    }

    public void setStud_photo(String stud_photo) {
        this.stud_photo = stud_photo;
    }

    public String getStud_login() {
        return stud_login;
    }

    public void setStud_login(String stud_login) {
        this.stud_login = stud_login;
    }

    public double getCtr_debt() {
        return ctr_debt;
    }

    public void setCtr_debt(double ctr_debt) {
        this.ctr_debt = ctr_debt;
    }

    public String getStud_class_name() {
        return stud_class_name;
    }

    public void setStud_class_name(String stud_class_name) {
        this.stud_class_name = stud_class_name;
    }

    public String getScl_photo() {
        return scl_photo;
    }

    public void setScl_photo(String scl_photo) {
        this.scl_photo = scl_photo;
    }

    public String getScl_accountent_fullname() {
        return scl_accountent_fullname;
    }

    public void setScl_accountent_fullname(String scl_accountent_fullname) {
        this.scl_accountent_fullname = scl_accountent_fullname;
    }

    public double getCtr_instplan_debt() {
        return ctr_instplan_debt;
    }

    public void setCtr_instplan_debt(double ctr_instplan_debt) {
        this.ctr_instplan_debt = ctr_instplan_debt;
    }

    public String getCtr_discountPerc() {
        return ctr_discountPerc;
    }

    public void setCtr_discountPerc(String ctr_discountPerc) {
        this.ctr_discountPerc = ctr_discountPerc;
    }

    public String getPeriod_kg() {
        return period_kg;
    }

    public void setPeriod_kg(String period_kg) {
        this.period_kg = period_kg;
    }

    public int getStud_id() {
        return stud_id;
    }

    public void setStud_id(int stud_id) {
        this.stud_id = stud_id;
    }

    public double getCtr_ttl_instplan_sum() {
        return ctr_ttl_instplan_sum;
    }

    public void setCtr_ttl_instplan_sum(double ctr_ttl_instplan_sum) {
        this.ctr_ttl_instplan_sum = ctr_ttl_instplan_sum;
    }

    public double getCtr_contract_sum() {
        return ctr_contract_sum;
    }

    public void setCtr_contract_sum(double ctr_contract_sum) {
        this.ctr_contract_sum = ctr_contract_sum;
    }

    public double getCtr_init_payment() {
        return ctr_init_payment;
    }

    public void setCtr_init_payment(double ctr_init_payment) {
        this.ctr_init_payment = ctr_init_payment;
    }

    public double getCtr_ttl_left_sum() {
        return ctr_ttl_left_sum;
    }

    public void setCtr_ttl_left_sum(double ctr_ttl_left_sum) {
        this.ctr_ttl_left_sum = ctr_ttl_left_sum;
    }

    public String getCtr_discountStr() {
        return ctr_discountStr;
    }

    public void setCtr_discountStr(String ctr_discountStr) {
        this.ctr_discountStr = ctr_discountStr;
    }

    public String getStud_name() {
        return stud_name;
    }

    public void setStud_name(String stud_name) {
        this.stud_name = stud_name;
    }

    public String getStud_sur_name() {
        return stud_sur_name;
    }

    public void setStud_surname(String stud_sur_name) {
        this.stud_sur_name = stud_sur_name;
    }

    public String getStud_middle_name() {
        return stud_middle_name;
    }

    public void setStud_middle_name(String stud_middle_name) {
        this.stud_middle_name = stud_middle_name;
    }

    public String getScl_name_ru() {
        return scl_name_ru;
    }

    public void setScl_name_ru(String scl_name_ru) {
        this.scl_name_ru = scl_name_ru;
    }

    public String getScl_city() {
        return scl_city;
    }

    public void setScl_city(String scl_city) {
        this.scl_city = scl_city;
    }

    public String getScl_dir_f_name() {
        return scl_dir_f_name;
    }

    public void setScl_dir_f_name(String scl_dir_f_name) {
        this.scl_dir_f_name = scl_dir_f_name;
    }

    public String getScl_address() {
        return scl_address;
    }

    public void setScl_address(String scl_address) {
        this.scl_address = scl_address;
    }

    public String getScl_inn() {
        return scl_inn;
    }

    public void setScl_inn(String scl_inn) {
        this.scl_inn = scl_inn;
    }

    public String getScl_bank() {
        return scl_bank;
    }

    public void setScl_bank(String scl_bank) {
        this.scl_bank = scl_bank;
    }

    public String getScl_bank_account() {
        return scl_bank_account;
    }

    public void setScl_bank_account(String scl_bank_account) {
        this.scl_bank_account = scl_bank_account;
    }

    public String getScl_phone() {
        return scl_phone;
    }

    public void setScl_phone(String scl_phone) {
        this.scl_phone = scl_phone;
    }

    public String getRel_fullname() {
        return rel_fullname;
    }

    public void setRel_fullname(String rel_fullname) {
        this.rel_fullname = rel_fullname;
    }

    public String getRel_name() {
        return rel_name;
    }

    public void setRel_name(String rel_name) {
        this.rel_name = rel_name;
    }

    public String getRel_name_dec() {
        return rel_name_dec;
    }

    public void setRel_name_dec(String rel_name_dec) {
        this.rel_name_dec = rel_name_dec;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    /**
     * @return the isStudentFeminitive
     */
    public boolean isStudentFeminitive() {
        return isStudentFeminitive;
    }

    /**
     * @param isStudentFeminitive the isStudentFeminitive to set
     */
    public void setIsStudentFeminitive(boolean isStudentFeminitive) {
        this.isStudentFeminitive = isStudentFeminitive;
    }

    /**
     * @return the rel_phone
     */
    public String getRel_phone() {
        return rel_phone;
    }

    /**
     * @param rel_phone the rel_phone to set
     */
    public void setRel_phone(String rel_phone) {
        this.rel_phone = rel_phone;
    }

    /**
     * @return the rel_address
     */
    public String getRel_address() {
        return rel_address;
    }

    /**
     * @param rel_address the rel_address to set
     */
    public void setRel_address(String rel_address) {
        this.rel_address = rel_address;
    }

    /**
     * @return the scl_name
     */
    public String getScl_name_kg() {
        return scl_name_kg;
    }

    /**
     * @param scl_name_kg the scl_name to set
     */
    public void setScl_name_kg(String scl_name_kg) {
        this.scl_name_kg = scl_name_kg;
    }

    /**
     * @return the rel_passport
     */
    public String getRel_passport() {
        return rel_passport;
    }

    /**
     * @param rel_passport the rel_passport to set
     */
    public void setRel_passport(String rel_passport) {
        this.rel_passport = rel_passport;
    }
}
