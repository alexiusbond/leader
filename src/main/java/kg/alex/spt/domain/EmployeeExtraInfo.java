/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

public class EmployeeExtraInfo implements Serializable {

    private int employee_id;
    private int health_status_id;
    private String hobbies;
    private String fobbies;
    private String short_notes;
    private String health_notes;

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
     * @return the hobbies
     */
    public String getHobbies() {
        return hobbies;
    }

    /**
     * @param hobbies the hobbies to set
     */
    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    /**
     * @return the fobbies
     */
    public String getFobbies() {
        return fobbies;
    }

    /**
     * @param fobbies the fobbies to set
     */
    public void setFobbies(String fobbies) {
        this.fobbies = fobbies;
    }

    /**
     * @return the short_notes
     */
    public String getShort_notes() {
        return short_notes;
    }

    /**
     * @param short_notes the short_notes to set
     */
    public void setShort_notes(String short_notes) {
        this.short_notes = short_notes;
    }

    /**
     * @return the health_notes
     */
    public String getHealth_notes() {
        return health_notes;
    }

    /**
     * @param health_notes the health_notes to set
     */
    public void setHealth_notes(String health_notes) {
        this.health_notes = health_notes;
    }

}
