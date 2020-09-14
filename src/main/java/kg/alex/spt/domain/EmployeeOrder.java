/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

public class EmployeeOrder implements Serializable {

    private int id;
    private String idStr;
    private int employee_id;
    private int order_id;
    private int school_id;
    private int from_to_school_id;
    private int position_id;
    private int class_name_id;
    private int m_employee_id;
    private Date from_date;
    private Date to_date;
    private String note;
    private int effected_by_id;
    private int can_not_delete;

    /**
     * @return the can_not_delete
     */
    public int getCan_not_delete() {
        return can_not_delete;
    }

    /**
     * @param can_not_delete the can_not_delete to set
     */
    public void setCan_not_delete(int can_not_delete) {
        this.can_not_delete = can_not_delete;
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
     * @return the position_id
     */
    public int getPosition_id() {
        return position_id;
    }

    /**
     * @param position_id the position_id to set
     */
    public void setPosition_id(int position_id) {
        this.position_id = position_id;
    }

    /**
     * @return the class_name_id
     */
    public int getClass_name_id() {
        return class_name_id;
    }

    /**
     * @param class_name_id the class_name_id to set
     */
    public void setClass_name_id(int class_name_id) {
        this.class_name_id = class_name_id;
    }

    /**
     * @return the from_date
     */
    public Date getFrom_date() {
        return from_date;
    }

    /**
     * @param from_date the from_date to set
     */
    public void setFrom_date(Date from_date) {
        this.from_date = from_date;
    }

    /**
     * @return the to_date
     */
    public Date getTo_date() {
        return to_date;
    }

    /**
     * @param to_date the to_date to set
     */
    public void setTo_date(Date to_date) {
        this.to_date = to_date;
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
     * @return the m_employee_id
     */
    public int getM_employee_id() {
        return m_employee_id;
    }

    /**
     * @param m_employee_id the m_employee_id to set
     */
    public void setM_employee_id(int m_employee_id) {
        this.m_employee_id = m_employee_id;
    }

    /**
     * @return the effected_by_id
     */
    public int getEffected_by_id() {
        return effected_by_id;
    }

    /**
     * @param effected_by_id the effected_by_id to set
     */
    public void setEffected_by_id(int effected_by_id) {
        this.effected_by_id = effected_by_id;
    }

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
     * @return the from_to_school_id
     */
    public int getFrom_to_school_id() {
        return from_to_school_id;
    }

    /**
     * @param from_to_school_id the from_to_school_id to set
     */
    public void setFrom_to_school_id(int from_to_school_id) {
        this.from_to_school_id = from_to_school_id;
    }

    /**
     * @return the idStr
     */
    public String getIdStr() {
        return idStr;
    }

    /**
     * @param idStr the idStr to set
     */
    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

}
