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
    private int position_id;
    private String fullName;
    private int school_id;
    private int branch_id;
    private String school_name;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public int getWorking_status_id() {
        return working_status_id;
    }

    public void setWorking_status_id(int working_status_id) {
        this.working_status_id = working_status_id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getSchool_id() {
        return school_id;
    }

    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    public String getSchool_name() {
        return school_name;
    }

    public void setSchool_name(String school_name) {
        this.school_name = school_name;
    }

    public String getSchool_logo() {
        return school_logo;
    }

    public void setSchool_logo(String school_logo) {
        this.school_logo = school_logo;
    }

    public Definition getCurrent_year() {
        return current_year;
    }

    public void setCurrent_year(Definition current_year) {
        this.current_year = current_year;
    }

    public int getPosition_id() {
        return position_id;
    }

    public void setPosition_id(int position_id) {
        this.position_id = position_id;
    }
}
