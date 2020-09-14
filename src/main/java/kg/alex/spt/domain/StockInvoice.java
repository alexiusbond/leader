/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

public class StockInvoice extends Invoice implements Serializable {

    private int stock_id;
    private String invoiceNumberStr;
    private int from_employee_id;
    private int to_employee_id;
    private String from_employee;
    private String to_employee;
    private String stock;
    private String acc_category;
    private int service_type_id;
    private int acc_category_id;

    /**
     * @return the stock_id
     */
    public int getStock_id() {
        return stock_id;
    }

    /**
     * @param stock_id the stock_id to set
     */
    public void setStock_id(int stock_id) {
        this.stock_id = stock_id;
    }

    /**
     * @return the from_employee_id
     */
    public int getFrom_employee_id() {
        return from_employee_id;
    }

    /**
     * @param from_employee_id the from_employee_id to set
     */
    public void setFrom_employee_id(int from_employee_id) {
        this.from_employee_id = from_employee_id;
    }

    /**
     * @return the to_employee_id
     */
    public int getTo_employee_id() {
        return to_employee_id;
    }

    /**
     * @param to_employee_id the to_employee_id to set
     */
    public void setTo_employee_id(int to_employee_id) {
        this.to_employee_id = to_employee_id;
    }

    /**
     * @return the service_type_id
     */
    public int getService_type_id() {
        return service_type_id;
    }

    /**
     * @param service_type_id the service_type_id to set
     */
    public void setService_type_id(int service_type_id) {
        this.service_type_id = service_type_id;
    }

    /**
     * @return the invoiceNumberStr
     */
    public String getInvoiceNumberStr() {
        return invoiceNumberStr;
    }

    /**
     * @param invoiceNumberStr the invoiceNumberStr to set
     */
    public void setInvoiceNumberStr(String invoiceNumberStr) {
        this.invoiceNumberStr = invoiceNumberStr;
    }

    /**
     * @return the from_employee
     */
    public String getFrom_employee() {
        return from_employee;
    }

    /**
     * @param from_employee the from_employee to set
     */
    public void setFrom_employee(String from_employee) {
        this.from_employee = from_employee;
    }

    /**
     * @return the to_employee
     */
    public String getTo_employee() {
        return to_employee;
    }

    /**
     * @param to_employee the to_employee to set
     */
    public void setTo_employee(String to_employee) {
        this.to_employee = to_employee;
    }

    /**
     * @return the stock
     */
    public String getStock() {
        return stock;
    }

    /**
     * @param stock the stock to set
     */
    public void setStock(String stock) {
        this.stock = stock;
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
     * @return the acc_category
     */
    public String getAcc_category() {
        return acc_category;
    }

    /**
     * @param acc_category the acc_category to set
     */
    public void setAcc_category(String acc_category) {
        this.acc_category = acc_category;
    }

}
