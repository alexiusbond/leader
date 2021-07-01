/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

public class InventoryInvoice extends Invoice implements Serializable {

    private int id;
    private int room_id;
    private String invoiceNumberStr;
    private int employee_id;
    private int school_id;
    private int activity_status_id;

    @Override
    public int getSchool_id() {
        return school_id;
    }

    @Override
    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getInvoiceNumberStr() {
        return invoiceNumberStr;
    }

    public void setInvoiceNumberStr(String invoiceNumberStr) {
        this.invoiceNumberStr = invoiceNumberStr;
    }

    @Override
    public int getEmployee_id() {
        return employee_id;
    }

    @Override
    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getActivity_status_id() {
        return activity_status_id;
    }

    public void setActivity_status_id(int activity_status_id) {
        this.activity_status_id = activity_status_id;
    }
}
