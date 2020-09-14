/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

public class EmployeeCertificate implements Serializable {

    private int id;
    private int employee_id;
    private String name;
    private String given_by;
    private Date date_of_issue;

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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the given_by
     */
    public String getGiven_by() {
        return given_by;
    }

    /**
     * @param given_by the given_by to set
     */
    public void setGiven_by(String given_by) {
        this.given_by = given_by;
    }

    /**
     * @return the date_of_issue
     */
    public Date getDate_of_issue() {
        return date_of_issue;
    }

    /**
     * @param date_of_issue the date_of_issue to set
     */
    public void setDate_of_issue(Date date_of_issue) {
        this.date_of_issue = date_of_issue;
    }

}
