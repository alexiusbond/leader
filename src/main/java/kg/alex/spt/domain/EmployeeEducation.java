/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

public class EmployeeEducation implements Serializable {

    private int id;
    private int employee_id;
    private int university_id;
    private int own_id;
    private String faculty;
    private String department;
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
     * @return the university_id
     */
    public int getUniversity_id() {
        return university_id;
    }

    /**
     * @param university_id the university_id to set
     */
    public void setUniversity_id(int university_id) {
        this.university_id = university_id;
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
     * @return the faculty
     */
    public String getFaculty() {
        return faculty;
    }

    /**
     * @param faculty the faculty to set
     */
    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    /**
     * @return the department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * @param department the department to set
     */
    public void setDepartment(String department) {
        this.department = department;
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
}
