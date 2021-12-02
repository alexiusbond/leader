package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

public class StudentContractCorrection implements Serializable {
    private String id;
    private double amount;
    private String note;
    private Date registration_date;
    private int student_contract_id;
    private int correction_type_id;
    private int employee_id;

    public Date getRegistration_date() {
        return registration_date;
    }

    public void setRegistration_date(Date registration_date) {
        this.registration_date = registration_date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getStudent_contract_id() {
        return student_contract_id;
    }

    public void setStudent_contract_id(int student_contract_id) {
        this.student_contract_id = student_contract_id;
    }

    public int getCorrection_type_id() {
        return correction_type_id;
    }

    public void setCorrection_type_id(int correction_type_id) {
        this.correction_type_id = correction_type_id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

}
