/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

public class Transfer implements Serializable {

    private int id;
    private double amount;
    private double rate;
    private int currency_id;
    private int acc_category_id;
    private int invoice_id;
    private String note;

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
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * @return the rate
     */
    public double getRate() {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    public void setRate(double rate) {
        this.rate = rate;
    }

    /**
     * @return the currency_id
     */
    public int getCurrency_id() {
        return currency_id;
    }

    /**
     * @param currency_id the currency_id to set
     */
    public void setCurrency_id(int currency_id) {
        this.currency_id = currency_id;
    }

    /**
     * @return the acc_category_id
     */
    public int getAcc_category_id() {
        return acc_category_id;
    }

    /**
     * @param acc_category_id the acc_category_id to set
     */
    public void setAcc_category_id(int acc_category_id) {
        this.acc_category_id = acc_category_id;
    }

    /**
     * @return the invoice_id
     */
    public int getInvoice_id() {
        return invoice_id;
    }

    /**
     * @param invoice_id the invoice_id to set
     */
    public void setInvoice_id(int invoice_id) {
        this.invoice_id = invoice_id;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }
}
