/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

public class EmployeeChildren implements Serializable {

    private int id;
    private int employee_id;
    private int education_status_id;
    private int health_status_id;
    private String fullname;
    private String institution;
    private Date date_of_birth;

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
     * @return the education_status_id
     */
    public int getEducation_status_id() {
        return education_status_id;
    }

    /**
     * @param education_status_id the education_status_id to set
     */
    public void setEducation_status_id(int education_status_id) {
        this.education_status_id = education_status_id;
    }

    /**
     * @return the health_status_id
     */
    public int getHealth_status_id() {
        return health_status_id;
    }

    /**
     * @param health_status_id the health_status_id to set
     */
    public void setHealth_status_id(int health_status_id) {
        this.health_status_id = health_status_id;
    }

    /**
     * @return the fullname
     */
    public String getFullname() {
        return fullname;
    }

    /**
     * @param fullname the fullname to set
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     * @return the date_of_birth
     */
    public Date getDate_of_birth() {
        return date_of_birth;
    }

    /**
     * @param date_of_birth the date_of_birth to set
     */
    public void setDate_of_birth(Date date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    /**
     * @return the institution
     */
    public String getInstitution() {
        return institution;
    }

    /**
     * @param institution the institution to set
     */
    public void setInstitution(String institution) {
        this.institution = institution;
    }

}
