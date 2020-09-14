/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author eldiyar
 */
public class StudentOrder implements Serializable {

    private int id;
    private int student_id;
    private int order_id;
    private String year;
    private int year_id;
    private String from_class;
    private int from_class_id;
    private String to_class;
    private int to_class_id;
    private String from_education_status;
    private int from_education_status_id;
    private String to_education_statuss;
    private int to_education_status_id;
    private int employee_id;
    private Date modification_date;
    private String reasons;

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
     * @return the student_id
     */
    public int getStudent_id() {
        return student_id;
    }

    /**
     * @param student_id the student_id to set
     */
    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    /**
     * @return the order_id
     */
    public int getOrder_id() {
        return order_id;
    }

    /**
     * @param order_id the order_id to set
     */
    public void setOrder_id(int order_id) {
        this.order_id = order_id;
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
     * @return the modification_date
     */
    public Date getModification_date() {
        return modification_date;
    }

    /**
     * @param modification_date the modification_date to set
     */
    public void setModification_date(Date modification_date) {
        this.modification_date = modification_date;
    }

    /**
     * @return the year
     */
    public String getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @return the year_id
     */
    public int getYear_id() {
        return year_id;
    }

    /**
     * @param year_id the year_id to set
     */
    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    /**
     * @return the from_class
     */
    public String getFrom_class() {
        return from_class;
    }

    /**
     * @param from_class the from_class to set
     */
    public void setFrom_class(String from_class) {
        this.from_class = from_class;
    }

    /**
     * @return the from_class_id
     */
    public int getFrom_class_id() {
        return from_class_id;
    }

    /**
     * @param from_class_id the from_class_id to set
     */
    public void setFrom_class_id(int from_class_id) {
        this.from_class_id = from_class_id;
    }

    /**
     * @return the to_class
     */
    public String getTo_class() {
        return to_class;
    }

    /**
     * @param to_class the to_class to set
     */
    public void setTo_class(String to_class) {
        this.to_class = to_class;
    }

    /**
     * @return the to_class_id
     */
    public int getTo_class_id() {
        return to_class_id;
    }

    /**
     * @param to_class_id the to_class_id to set
     */
    public void setTo_class_id(int to_class_id) {
        this.to_class_id = to_class_id;
    }

    /**
     * @return the from_education_status
     */
    public String getFrom_education_status() {
        return from_education_status;
    }

    /**
     * @param from_education_status the from_education_status to set
     */
    public void setFrom_education_status(String from_education_status) {
        this.from_education_status = from_education_status;
    }

    /**
     * @return the from_education_status_id
     */
    public int getFrom_education_status_id() {
        return from_education_status_id;
    }

    /**
     * @param from_education_status_id the from_education_status_id to set
     */
    public void setFrom_education_status_id(int from_education_status_id) {
        this.from_education_status_id = from_education_status_id;
    }

    /**
     * @return the to_education_statuss
     */
    public String getTo_education_statuss() {
        return to_education_statuss;
    }

    /**
     * @param to_education_statuss the to_education_statuss to set
     */
    public void setTo_education_statuss(String to_education_statuss) {
        this.to_education_statuss = to_education_statuss;
    }

    /**
     * @return the to_education_status_id
     */
    public int getTo_education_status_id() {
        return to_education_status_id;
    }

    /**
     * @param to_education_status_id the to_education_status_id to set
     */
    public void setTo_education_status_id(int to_education_status_id) {
        this.to_education_status_id = to_education_status_id;
    }

    /**
     * @return the reasons
     */
    public String getReasons() {
        return reasons;
    }

    /**
     * @param reasons the reasons to set
     */
    public void setReasons(String reasons) {
        this.reasons = reasons;
    }
}
