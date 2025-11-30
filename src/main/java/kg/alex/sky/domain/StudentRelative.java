/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.sky.domain;

import java.io.Serializable;

public class StudentRelative implements Serializable {

    private int id;
    private int student_id;
    private String fullName;
    private int workPlaceId;
    private String phone;
    private String addressLine;
    private int addressId;
    private String passport;
    private String INN;
    private int is_main;
    private int relative_id;
    private String relativeDeclarative;
    private String relativeTitle;

    public String getINN() {
        return INN;
    }

    public void setINN(String INN) {
        this.INN = INN;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getRelativeTitle() {
        return relativeTitle;
    }

    public void setRelativeTitle(String relativeTitle) {
        this.relativeTitle = relativeTitle;
    }

    public String getRelativeDeclarative() {
        return relativeDeclarative;
    }

    public void setRelativeDeclarative(String relativeDeclarative) {
        this.relativeDeclarative = relativeDeclarative;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getWorkPlaceId() {
        return workPlaceId;
    }

    public void setWorkPlaceId(int workPlaceId) {
        this.workPlaceId = workPlaceId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public int getIs_main() {
        return is_main;
    }

    public void setIs_main(int is_main) {
        this.is_main = is_main;
    }

    public int getRelative_id() {
        return relative_id;
    }

    public void setRelative_id(int relative_id) {
        this.relative_id = relative_id;
    }

}
