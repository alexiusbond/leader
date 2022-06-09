/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author alex
 */
public class UserDetails implements Serializable {

    private int id;
    private String login;
    private int working_status_id;
    private String fullName;
    private int school_id;
    private int school_type_id;
    private int branch_id;
    private String school_name;
    private String school_code;
    private String school_logo;
    private Definition current_year;
    private Date transactions_start_date;
    private boolean isUnreadMessages;

    public int getBranch_id() {
        return branch_id;
    }

    public void setBranch_id(int branch_id) {
        this.branch_id = branch_id;
    }

    public Date getTransactions_start_date() {
        return transactions_start_date;
    }

    public void setTransactions_start_date(Date transactions_start_date) {
        this.transactions_start_date = transactions_start_date;
    }

    public boolean isUnreadMessages() {
        return isUnreadMessages;
    }

    public void setUnreadMessages(boolean unreadMessages) {
        isUnreadMessages = unreadMessages;
    }

    public int getSchool_type_id() {
        return school_type_id;
    }

    public void setSchool_type_id(int school_type_id) {
        this.school_type_id = school_type_id;
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
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return the fullname
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullname to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
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
     * @return the current_year
     */
    public Definition getCurrent_year() {
        return current_year;
    }

    /**
     * @param current_year the current_year to set
     */
    public void setCurrent_year(Definition current_year) {
        this.current_year = current_year;
    }

    public String getSchool_name() {
        return school_name;
    }

    public void setSchool_name(String school_name) {
        this.school_name = school_name;
    }

    /**
     * @return the school_logo
     */
    public String getSchool_logo() {
        return school_logo;
    }

    /**
     * @param school_logo the school_logo to set
     */
    public void setSchool_logo(String school_logo) {
        this.school_logo = school_logo;
    }

    /**
     * @return the school_code
     */
    public String getSchool_code() {
        return school_code;
    }

    /**
     * @param school_code the school_code to set
     */
    public void setSchool_code(String school_code) {
        this.school_code = school_code;
    }
}
