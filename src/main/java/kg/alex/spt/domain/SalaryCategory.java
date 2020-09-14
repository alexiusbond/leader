/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

public class SalaryCategory implements Serializable {

    private int id;
    private String acc_category_code;
    private String acc_category_name;

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
     * @return the acc_category_code
     */
    public String getAcc_category_code() {
        return acc_category_code;
    }

    /**
     * @param acc_category_code the acc_category_code to set
     */
    public void setAcc_category_code(String acc_category_code) {
        this.acc_category_code = acc_category_code;
    }

    /**
     * @return the acc_category_name
     */
    public String getAcc_category_name() {
        return acc_category_name;
    }

    /**
     * @param acc_category_name the acc_category_name to set
     */
    public void setAcc_category_name(String acc_category_name) {
        this.acc_category_name = acc_category_name;
    }
}
