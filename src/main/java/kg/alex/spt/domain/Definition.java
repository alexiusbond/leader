/*
 * Semester.java
 * Created on December 18, 2007, 1:32 PM
 */
package kg.alex.spt.domain;

import java.io.Serializable;

/**
 * @author Alex
 */
public class Definition implements Serializable {

    private int id;
    private String name;
    private int activity_status_id;

    public Definition() {
    }

    public Definition(int id, String name) {
        this.id = id;
        this.name = name;
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the activity_status_id
     */
    public int getActivity_status_id() {
        return activity_status_id;
    }

    /**
     * @param activity_status_id the activity_status_id to set
     */
    public void setActivity_status_id(int activity_status_id) {
        this.activity_status_id = activity_status_id;
    }

}
