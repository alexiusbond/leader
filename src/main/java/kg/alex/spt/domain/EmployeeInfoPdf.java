/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

public class EmployeeInfoPdf implements Serializable {
    private String employeeName;
    private String employeeSurname;
    private String employeeMiddleName;
    private School school;
    private boolean isEmployeeFemininity;
    private int contractNumber;
    private Date contractCreationDate;

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeSurname() {
        return employeeSurname;
    }

    public void setEmployeeSurname(String employeeSurname) {
        this.employeeSurname = employeeSurname;
    }

    public String getEmployeeMiddleName() {
        return employeeMiddleName;
    }

    public void setEmployeeMiddleName(String employeeMiddleName) {
        this.employeeMiddleName = employeeMiddleName;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public boolean isEmployeeFemininity() {
        return isEmployeeFemininity;
    }

    public void setEmployeeFemininity(boolean employeeFemininity) {
        isEmployeeFemininity = employeeFemininity;
    }

    public int getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(int contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Date getContractCreationDate() {
        return contractCreationDate;
    }

    public void setContractCreationDate(Date contractCreationDate) {
        this.contractCreationDate = contractCreationDate;
    }
}
