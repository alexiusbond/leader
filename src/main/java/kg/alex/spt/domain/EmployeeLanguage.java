/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

public class EmployeeLanguage implements Serializable {

    private int id;
    private int employee_id;
    private int language_id;
    private int level_id;

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
     * @return the language_id
     */
    public int getLanguage_id() {
        return language_id;
    }

    /**
     * @param language_id the language_id to set
     */
    public void setLanguage_id(int language_id) {
        this.language_id = language_id;
    }

    /**
     * @return the level_id
     */
    public int getLevel_id() {
        return level_id;
    }

    /**
     * @param level_id the level_id to set
     */
    public void setLevel_id(int level_id) {
        this.level_id = level_id;
    }

}
