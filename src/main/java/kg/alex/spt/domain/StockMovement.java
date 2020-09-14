/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

public class StockMovement implements Serializable {

    private int id;
    private double quantity;
    private double price;
    private double rate;
    private double remain;
    private int order_number;
    private int measurement_id;
    private int acc_category_id;
    private int invoice_id;
    private int stock_movement_id;
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
     * @return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * @return the measurement_id
     */
    public int getMeasurement_id() {
        return measurement_id;
    }

    /**
     * @param measurement_id the measurement_id to set
     */
    public void setMeasurement_id(int measurement_id) {
        this.measurement_id = measurement_id;
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
     * @return the quantity
     */
    public double getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(double quantity) {
        this.quantity = quantity;
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

    public int getStock_movement_id() {
        return stock_movement_id;
    }

    public void setStock_movement_id(int stock_movement_id) {
        this.stock_movement_id = stock_movement_id;
    }

    /**
     * @return the remain
     */
    public double getRemain() {
        return remain;
    }

    /**
     * @param remain the remain to set
     */
    public void setRemain(double remain) {
        this.remain = remain;
    }

    /**
     * @return the order_number
     */
    public int getOrder_number() {
        return order_number;
    }

    /**
     * @param order_number the order_number to set
     */
    public void setOrder_number(int order_number) {
        this.order_number = order_number;
    }
}
