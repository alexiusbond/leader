/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

public class EmployeeWork implements Serializable {

    private int id;
    private int employee_id;
    private int own_id;
    private int working_status_id;
    private int work_place_id;
    private String main_position;
    private String extra_position;
    private String year;

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
     * @return the own_id
     */
    public int getOwn_id() {
        return own_id;
    }

    /**
     * @param own_id the own_id to set
     */
    public void setOwn_id(int own_id) {
        this.own_id = own_id;
    }

    /**
     * @return the main_position
     */
    public String getMain_position() {
        return main_position;
    }

    /**
     * @param main_position the main_position to set
     */
    public void setMain_position(String main_position) {
        this.main_position = main_position;
    }

    /**
     * @return the extra_position
     */
    public String getExtra_position() {
        return extra_position;
    }

    /**
     * @param extra_position the extra_position to set
     */
    public void setExtra_position(String extra_position) {
        this.extra_position = extra_position;
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
     * @return the working_status_id
     */
    public int getWorking_status_id() {
        return working_status_id;
    }

    /**
     * @param working_status_id the working_status_id to set
     */
    public void setWorking_status_id(int working_status_id) {
        this.working_status_id = working_status_id;
    }

    /**
     * @return the work_place_id
     */
    public int getWork_place_id() {
        return work_place_id;
    }

    /**
     * @param work_place_id the work_place_id to set
     */
    public void setWork_place_id(int work_place_id) {
        this.work_place_id = work_place_id;
    }

}
