/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

public class Invoice implements Serializable {

    private int id;
    private int invoice_number;
    private Date creation_date;
    private String note;
    private int school_id;
    private int employee_id;
    private int acc_invoice_type_id;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the invoice_number
     */
    public int getInvoice_number() {
        return invoice_number;
    }

    /**
     * @param invoice_number the invoice_number to set
     */
    public void setInvoice_number(int invoice_number) {
        this.invoice_number = invoice_number;
    }

    /**
     * @return the creation_date
     */
    public Date getCreation_date() {
        return creation_date;
    }

    /**
     * @param creation_date the creation_date to set
     */
    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * @return the school_id
     */
    public int getSchool_id() {
        return school_id;
    }

    /**
     * @param school_id the school_id to set
     */
    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    /**
     * @return the employee_id
     */
    public int getEmployee_id() {
        return employee_id;
    }

    /**
     * @param employee_id the employee_id to set
     */
    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    /**
     * @return the acc_invoice_type_id
     */
    public int getAcc_invoice_type_id() {
        return acc_invoice_type_id;
    }

    /**
     * @param acc_invoice_type_id the acc_invoice_type_id to set
     */
    public void setAcc_invoice_type_id(int acc_invoice_type_id) {
        this.acc_invoice_type_id = acc_invoice_type_id;
    }

}
