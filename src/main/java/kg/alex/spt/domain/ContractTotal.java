/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.domain;

import java.io.Serializable;

/**
 *
 * @author eldiyar
 */
public class ContractTotal implements Serializable {

    private Double ttl_contract;
    private Double ttl_debt;
    private Double ttl_disc;
    private Double ttl_payments;
    private Double ttl_left;
    private Double ttl_net;
    private int ttl_students;

    public Double getTtl_net() {
        return ttl_net;
    }

    public void setTtl_net(Double ttl_net) {
        this.ttl_net = ttl_net;
    }

    public int getTtl_students() {
        return ttl_students;
    }

    public void setTtl_students(int ttl_students) {
        this.ttl_students = ttl_students;
    }

    public Double getTtl_disc() {
        return ttl_disc;
    }

    public void setTtl_disc(Double ttl_disc) {
        this.ttl_disc = ttl_disc;
    }

    public Double getTtl_contract() {
        return ttl_contract;
    }

    public void setTtl_contract(Double ttl_contract) {
        this.ttl_contract = ttl_contract;
    }

    public Double getTtl_debt() {
        return ttl_debt;
    }

    public void setTtl_debt(Double ttl_debt) {
        this.ttl_debt = ttl_debt;
    }

    public Double getTtl_payments() {
        return ttl_payments;
    }

    public void setTtl_payments(Double ttl_payments) {
        this.ttl_payments = ttl_payments;
    }

    public Double getTtl_left() {
        return ttl_left;
    }

    public void setTtl_left(Double ttl_left) {
        this.ttl_left = ttl_left;
    }

}
