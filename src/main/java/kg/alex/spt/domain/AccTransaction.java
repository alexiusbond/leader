/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

public class AccTransaction implements Serializable {

    private String id;
    private Date date;
    private int category_id;
    private int currency_id;
    private double currency_rate;
    private double amount;
    private double overlimit;
    private double limit;
    private int employee_id;
    private int school_id;
    private String note;
    private String category;
    private String employee;
    private String from_to_employee;
    private int dp_invoice_id;
    private int acc_invoice_id;
    private int student_payments_id;
    private int from_to_employee_id;
    private int order_number;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSchool_id() {
        return school_id;
    }

    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public int getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(int currency_id) {
        this.currency_id = currency_id;
    }

    public double getCurrency_rate() {
        return currency_rate;
    }

    public void setCurrency_rate(double currency_rate) {
        this.currency_rate = currency_rate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /**
     * @return the student_payments_id
     */
    public int getStudent_payments_id() {
        return student_payments_id;
    }

    /**
     * @param student_payments_id the student_payments_id to set
     */
    public void setStudent_payments_id(int student_payments_id) {
        this.student_payments_id = student_payments_id;
    }

    /**
     * @return the overlimit
     */
    public double getOverlimit() {
        return overlimit;
    }

    /**
     * @param overlimit the overlimit to set
     */
    public void setOverlimit(double overlimit) {
        this.overlimit = overlimit;
    }

    /**
     * @return the limit
     */
    public double getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit(double limit) {
        this.limit = limit;
    }

    /**
     * @return the from_to_employee_id
     */
    public int getFrom_to_employee_id() {
        return from_to_employee_id;
    }

    /**
     * @param from_to_employee_id the from_to_employee_id to set
     */
    public void setFrom_to_employee_id(int from_to_employee_id) {
        this.from_to_employee_id = from_to_employee_id;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return the employee
     */
    public String getEmployee() {
        return employee;
    }

    /**
     * @param employee the employee to set
     */
    public void setEmployee(String employee) {
        this.employee = employee;
    }

    /**
     * @return the from_to_employee
     */
    public String getFrom_to_employee() {
        return from_to_employee;
    }

    /**
     * @param from_to_employee the from_to_employee to set
     */
    public void setFrom_to_employee(String from_to_employee) {
        this.from_to_employee = from_to_employee;
    }

    /**
     * @return the order_number
     */
    public int getOrder_number() {
        return order_number;
    }

    /**
     * @param order_number the order_number to set
     */
    public void setOrder_number(int order_number) {
        this.order_number = order_number;
    }

    /**
     * @return the dp_invoice_id
     */
    public int getDp_invoice_id() {
        return dp_invoice_id;
    }

    /**
     * @param dp_invoice_id the dp_invoice_id to set
     */
    public void setDp_invoice_id(int dp_invoice_id) {
        this.dp_invoice_id = dp_invoice_id;
    }

    /**
     * @return the acc_invoice_id
     */
    public int getAcc_invoice_id() {
        return acc_invoice_id;
    }

    /**
     * @param acc_invoice_id the acc_invoice_id to set
     */
    public void setAcc_invoice_id(int acc_invoice_id) {
        this.acc_invoice_id = acc_invoice_id;
    }

}
