/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

public class InvoiceInfoPdf implements Serializable {

    private String login;
    private String class_name;
    private String stud_fullname;
    private String whopaid_fullname;
    private String school_name;
    private String order_number;
    private String scl_logo;
    private String payment_type;
    private Date payment_date;
    private double amount;
    private double kurs;

    public double getKurs() {
        return kurs;
    }

    public void setKurs(double kurs) {
        this.kurs = kurs;
    }

    public String getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(String payment_type) {
        this.payment_type = payment_type;
    }

    public String getScl_logo() {
        return scl_logo;
    }

    public void setScl_logo(String scl_logo) {
        this.scl_logo = scl_logo;
    }

    public String getSchool_name() {
        return school_name;
    }

    public void setSchool_name(String school_name) {
        this.school_name = school_name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public String getStud_fullname() {
        return stud_fullname;
    }

    public void setStud_fullname(String stud_fullname) {
        this.stud_fullname = stud_fullname;
    }

    public String getWhopaid_fullname() {
        return whopaid_fullname;
    }

    public void setWhopaid_fullname(String whopiad_fullname) {
        this.whopaid_fullname = whopiad_fullname;
    }

    public String getOrder_number() {
        return order_number;
    }

    public void setOrder_number(String order_number) {
        this.order_number = order_number;
    }

    public Date getPayment_date() {
        return payment_date;
    }

    public void setPayment_date(Date payment_date) {
        this.payment_date = payment_date;
    }

}
