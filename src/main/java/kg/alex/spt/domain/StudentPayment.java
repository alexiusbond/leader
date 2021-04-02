/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

public class StudentPayment implements Serializable {

    private int id;
    private int student_id;
    private int year_id;
    private double amount;
    private double init_pay;
    private double ttl_pay;
    private double rate;
    private int payment_type_id;
    private int payment_cat_type_id;
    private int emplooyee_id;
    private int school_id;
    private String who_paid;
    private String note;
    private String noteForKassa;
    private Date modification_date;

    public String getNoteForKassa() {
        return noteForKassa;
    }

    public void setNoteForKassa(String noteForKassa) {
        this.noteForKassa = noteForKassa;
    }

    public int getSchool_id() {
        return school_id;
    }

    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getInit_pay() {
        return init_pay;
    }

    public void setInit_pay(double init_pay) {
        this.init_pay = init_pay;
    }

    public double getTtl_pay() {
        return ttl_pay;
    }

    public void setTtl_pay(double ttl_pay) {
        this.ttl_pay = ttl_pay;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getWho_paid() {
        return who_paid;
    }

    public void setWho_paid(String who_paid) {
        this.who_paid = who_paid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPayment_cat_type_id() {
        return payment_cat_type_id;
    }

    public void setPayment_cat_type_id(int payment_cat_type_id) {
        this.payment_cat_type_id = payment_cat_type_id;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getYear_id() {
        return year_id;
    }

    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getPayment_type_id() {
        return payment_type_id;
    }

    public void setPayment_type_id(int payment_type_id) {
        this.payment_type_id = payment_type_id;
    }

    public int getEmplooyee_id() {
        return emplooyee_id;
    }

    public void setEmplooyee_id(int emplooyee_id) {
        this.emplooyee_id = emplooyee_id;
    }

    public Date getModification_date() {
        return modification_date;
    }

    public void setModification_date(Date modification_date) {
        this.modification_date = modification_date;
    }

}
