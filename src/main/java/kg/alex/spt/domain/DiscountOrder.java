/*
 * Semester.java
 * Created on December 18, 2007, 1:32 PM
 */
package kg.alex.spt.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Alex
 */
public class DiscountOrder implements Serializable {

    private int id;
    private String order_number;
    private String title;
    private String content;
    private String message;
    private Date date;
    private int student_id;
    private int employee_id;
    private int to_employee_id;
    private int message_status_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrder_number() {
        return order_number;
    }

    public void setOrder_number(String order_number) {
        this.order_number = order_number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getTo_employee_id() {
        return to_employee_id;
    }

    public void setTo_employee_id(int to_employee_id) {
        this.to_employee_id = to_employee_id;
    }

    public int getMessage_status_id() {
        return message_status_id;
    }

    public void setMessage_status_id(int message_status_id) {
        this.message_status_id = message_status_id;
    }
}
