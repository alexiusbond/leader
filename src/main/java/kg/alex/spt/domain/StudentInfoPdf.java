/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

public class StudentInfoPdf implements Serializable {

    private int stud_id;
    private String stud_login;
    private String stud_photo;
    private String stud_name;
    private String stud_sur_name;
    private String stud_middle_name;
    private String stud_class_name;
    private String scl_name_ru;
    private String scl_name_kg;
    private String scl_year_name;
    private String scl_city;
    private String scl_dir_f_name;
    private String scl_address;
    private String scl_inn;
    private String scl_bank;
    private String scl_bank_account;
    private String scl_phone;
    private String scl_accountant_full_name;
    private String rel_full_name;
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
    private double ctr_installment_plan_debt;
    private double ctr_debt;
    private double ctr_to_pay;
    private double ctr_paid;
    private String ctr_discountStr;
    private String ctr_discount_percentage;
    private String ctr_Correction;
    private boolean isStudentFemininity;
    private int contract_number;

    public int getContract_number() {
        return contract_number;
    }

    public void setContract_number(int contract_number) {
        this.contract_number = contract_number;
    }

    public String getCtr_Correction() {
        return ctr_Correction;
    }

    public void setCtr_Correction(String ctr_Correction) {
        this.ctr_Correction = ctr_Correction;
    }

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

    public double getCtr_to_pay() {
        return ctr_to_pay;
    }

    public void setCtr_to_pay(double ctr_to_pay) {
        this.ctr_to_pay = ctr_to_pay;
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
    public String getScl_accountant_full_name() {
        return scl_accountant_full_name;
    }

    public void setScl_accountant_full_name(String scl_accountant_full_name) {
        this.scl_accountant_full_name = scl_accountant_full_name;
    }

    public double getCtr_installment_plan_debt() {
        return ctr_installment_plan_debt;
    }

    public void setCtr_installment_plan_debt(double ctr_installment_plan_debt) {
        this.ctr_installment_plan_debt = ctr_installment_plan_debt;
    }

    public String getCtr_discount_percentage() {
        return ctr_discount_percentage;
    }

    public void setCtr_discount_percentage(String ctr_discount_percentage) {
        this.ctr_discount_percentage = ctr_discount_percentage;
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

    public String getRel_full_name() {
        return rel_full_name;
    }

    public void setRel_full_name(String rel_full_name) {
        this.rel_full_name = rel_full_name;
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

    public boolean isStudentFemininity() {
        return isStudentFemininity;
    }

    public void setIsStudentFemininity(boolean isStudentFemininity) {
        this.isStudentFemininity = isStudentFemininity;
    }

    public String getRel_phone() {
        return rel_phone;
    }

    public void setRel_phone(String rel_phone) {
        this.rel_phone = rel_phone;
    }

    public String getRel_address() {
        return rel_address;
    }

    public void setRel_address(String rel_address) {
        this.rel_address = rel_address;
    }

    public String getScl_name_kg() {
        return scl_name_kg;
    }

    public void setScl_name_kg(String scl_name_kg) {
        this.scl_name_kg = scl_name_kg;
    }

    public String getRel_passport() {
        return rel_passport;
    }

    public void setRel_passport(String rel_passport) {
        this.rel_passport = rel_passport;
    }
}
