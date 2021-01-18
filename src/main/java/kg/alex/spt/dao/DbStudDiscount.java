/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.Set;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.StudDiscount;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.ClassDiscountsReport;
import kg.alex.spt.reports.SchoolDiscountsReport;
import kg.alex.spt.ui.StudentDefinitionView;

public class DbStudDiscount extends BaseDb {

    public DbStudDiscount() throws Exception {
        super();
    }

    public int exec_insert_st_discount(StudDiscount d) throws SQLException {
        String sql = "INSERT INTO student_discount (free_entry_amount,discount_id,"
                + "student_id,year_id,employee_id,modification_date,note,discount_value) "
                + "VALUES(?,?,?,?,?,NOW(),?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (d.getFree_entry_amount() == 0.0) {
            stat.setNull(1, Types.VARCHAR);
        } else {
            stat.setDouble(1, d.getFree_entry_amount());
        }
        stat.setInt(2, d.getDiscount_id());
        stat.setInt(3, d.getStudent_id());
        stat.setInt(4, d.getYear_id());
        stat.setInt(5, d.getEmployee_id());
        stat.setString(6, d.getNote());
        stat.setDouble(7, d.getDiscount_value());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(StudDiscount sd) throws SQLException {
        String sql = "update student_discount set free_entry_amount=?, "
                + "discount_id=?, student_id=?, year_id=?, employee_id=?, "
                + "modification_date=NOW(), note=?, discount_value=? WHERE id=?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDouble(1, sd.getFree_entry_amount());
        stat.setDouble(2, sd.getDiscount_id());
        stat.setInt(3, sd.getStudent_id());
        stat.setInt(4, sd.getYear_id());
        stat.setInt(5, sd.getEmployee_id());
        stat.setString(6, sd.getNote());
        stat.setDouble(7, sd.getDiscount_value());
        stat.setString(8, sd.getId());
        return stat.executeUpdate();
    }

    public int exec_delete(String disc_id) throws SQLException {
        String sql = "DELETE FROM student_discount WHERE id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, disc_id);
        return stat.executeUpdate();
    }

    public IndexedContainer exec_disc_strCont(MyVaadinUI myUI, int stud_id, int year_id) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT sd.id, d.discount_type_id, d.amount, sd.free_entry_amount "
                + "FROM student_discount as sd "
                + "left join discount as d on sd.discount_id = d.id "
                + "where sd.student_id = ? and sd.year_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(sysSettings.discount_type_id, Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.FreeAmount), Double.class, 0.0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("sd.id"));
            item.getItemProperty(sysSettings.discount_type_id)
                    .setValue(result.getInt("d.discount_type_id"));
            item.getItemProperty(myUI.getMessage(SptMessages.Amount))
                    .setValue(result.getDouble("d.amount"));
            item.getItemProperty(myUI.getMessage(SptMessages.FreeAmount))
                    .setValue(result.getDouble("sd.free_entry_amount"));
        }
        return container;
    }

    public IndexedContainer execSQL_St_Discounts(MyVaadinUI myUI, int stud_id, int year_id,
            StudentDefinitionView dw) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT sd.id, sd.free_entry_amount, sd.discount_id, sd.note, "
                + "d.discount_type_id, d.id, d.amount FROM student_discount as sd "
                + "left join discount as d on d.id = sd.discount_id "
                + "where sd.student_id = ? and sd.year_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = dw.prepareDiscountsContainer();
        while (result.next()) {
            String id = result.getString("sd.id");
            Item item = container.addItem(id);
            item.getItemProperty(sysSettings.button).setValue(
                    dw.createButton(myUI.getMessage(SptMessages.DeleteButton), id, false, true));
            item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(
                    dw.createComboboxDisc(result.getInt("d.id"),
                            myUI.getMessage(SptMessages.Name), id));
            if (result.getString("d.discount_type_id").equals("1")
                    || result.getString("d.discount_type_id").equals("2")) {
                item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                        dw.createTextfieldDisc(result.getDouble("d.amount"), null,
                                myUI.getMessage(SptMessages.DiscountAmount), id, true));
            } else if (result.getString("d.discount_type_id").equals("3")
                    || result.getString("d.discount_type_id").equals("4")) {
                item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                        dw.createTextfieldDisc(result.getDouble("sd.free_entry_amount"),
                                result.getDouble("d.amount"),
                                myUI.getMessage(SptMessages.DiscountAmount), id, false));
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                    dw.createTextfield(result.getString("sd.note"),
                            myUI.getMessage(SptMessages.Note), id, true, false));
            item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Update));
        }
        return container;
    }

    public void execSQL_Discounts_by_classes(MyVaadinUI myUI, int year_id,
            String edu_statuses_ids, ClassDiscountsReport cdr) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT sd.discount_id, COUNT(sd.discount_id) AS disc_quantity, "
                + "SUM(sd.discount_value) AS disc_amount, "
                + "SUM(c.amount) AS contr_amount";
        Iterator class_iter = ((Set<?>) cdr.classTable.getValue()).iterator();
        while (class_iter.hasNext()) {
            Object next = class_iter.next();
            sql += ", COUNT(IF(cna.id = " + next + ", 1, NULL)) AS disc_quantity" + next + ", "
                    + "SUM(IF(cna.id = " + next + ", sd.discount_value, 0)) AS disc_amount" + next + ", "
                    + "SUM(IF(cna.id = " + next + ", c.amount, 0)) AS contr_amount" + next + " ";
        }
        sql += " FROM student_discount AS sd "
                + "LEFT JOIN student AS st ON sd.student_id = st.id "
                + "LEFT JOIN student_contract AS sc ON sd.student_id = sc.student_id "
                + "AND sd.year_id = sc.year_id "
                + "LEFT JOIN contract AS c ON c.id = sc.contract_id "
                + "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN class_name AS cna ON cna.id = CASE "
                + "WHEN stud_o.to_class_name_id IS NULL THEN st.class_name_id "
                + "ELSE stud_o.to_class_name_id END "
                + "LEFT JOIN education_status AS edu ON edu.id = "
                + "CASE WHEN stud_o.to_education_status_id IS NULL "
                + "THEN st.education_status_id ELSE stud_o.to_education_status_id END "
                + "WHERE sd.year_id = ? AND cna.id IN ("
                + sysSettings.convertCollectionToStr(((Set<?>) cdr.classTable.getValue())) + ") "
                + "AND sd.discount_id IN ("
                + sysSettings.convertCollectionToStr(((Set<?>) cdr.discountsTable.getValue())) + ") "
                + "AND edu.id IN (" + edu_statuses_ids + ") "
                + "GROUP BY sd.discount_id;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Discount), String.class, 0);
        class_iter = ((Set<?>) cdr.classTable.getValue()).iterator();
        while (class_iter.hasNext()) {
            Object next = class_iter.next();
            container.addContainerProperty(cdr.classTable.getContainerProperty(
                    next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                    + myUI.getMessage(SptMessages.Students), Integer.class, 0);
            container.addContainerProperty(cdr.classTable.getContainerProperty(
                    next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                    + myUI.getMessage(SptMessages.DiscountAmount), Double.class, 0.0);
            container.addContainerProperty(cdr.classTable.getContainerProperty(
                    next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                    + myUI.getMessage(SptMessages.Average) + "%", Double.class, 0.0);
        }
        container.addContainerProperty(myUI.getMessage(SptMessages.Total) + " "
                + myUI.getMessage(SptMessages.Students), Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Total) + " "
                + myUI.getMessage(SptMessages.DiscountAmount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Total) + " "
                + myUI.getMessage(SptMessages.Average) + "%", Double.class, 0.0);
        cdr.dataTable.setContainerDataSource(container);
        Iterator disc_iter = ((Set<?>) cdr.discountsTable.getValue()).iterator();
        while (disc_iter.hasNext()) {
            Object next = disc_iter.next();
            Item item = container.addItem((Integer) next);
            item.getItemProperty(myUI.getMessage(SptMessages.Discount)).setValue(
                    cdr.discountsTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue());
        }
        cdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Discount),
                myUI.getMessage(SptMessages.Total));
        cdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                + myUI.getMessage(SptMessages.Average) + "%", "100.00");
        int counter = 0;
        while (result.next()) {
            Item item = container.getItem(result.getInt("sd.discount_id"));
            class_iter = ((Set<?>) cdr.classTable.getValue()).iterator();
            String footerVal;
            while (class_iter.hasNext()) {
                Object next = class_iter.next();
                item.getItemProperty(cdr.classTable.getContainerProperty(
                        next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                        + myUI.getMessage(SptMessages.Students)).setValue(
                        result.getInt("disc_quantity" + next));
                footerVal = cdr.dataTable.getColumnFooter(cdr.classTable.getContainerProperty(
                        next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                        + myUI.getMessage(SptMessages.Students));
                if (footerVal == null) {
                    footerVal = "0";
                }
                if (counter != 0) {
                    cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.Students), (Integer.parseInt(footerVal)
                            + result.getInt("disc_quantity" + next)) + "");
                } else {
                    cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.Students),
                            result.getInt("disc_quantity" + next) + "");
                }
                item.getItemProperty(cdr.classTable.getContainerProperty(
                        next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                        + myUI.getMessage(SptMessages.DiscountAmount)).setValue(
                        result.getDouble("disc_amount" + next));
                footerVal = cdr.dataTable.getColumnFooter(
                        cdr.classTable.getContainerProperty(
                                next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                        + myUI.getMessage(SptMessages.DiscountAmount));
                if (footerVal == null) {
                    footerVal = "0";
                }
                if (counter != 0) {
                    cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.DiscountAmount),
                            sysSettings.dFormat.format(Double.parseDouble(footerVal)
                                    + result.getDouble("disc_amount" + next)));
                } else {
                    cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.DiscountAmount),
                            sysSettings.dFormat.format(result.getDouble("disc_amount" + next)));
                }
                if (result.getDouble("contr_amount" + next) != 0.0) {
                    double val = result.getDouble("disc_amount" + next)
                            / result.getDouble("contr_amount" + next) * 100.0;
                    item.getItemProperty(cdr.classTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.Average) + "%").setValue(val);
                    footerVal = cdr.dataTable.getColumnFooter(cdr.classTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.Average) + "%");
                    if (footerVal == null) {
                        footerVal = "0";
                    }
                    if (counter != 0) {
                        cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                                next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                                + myUI.getMessage(SptMessages.Average) + "%",
                                sysSettings.dFormat.format(Double.parseDouble(footerVal)
                                        + val));
                    } else {
                        cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                                next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                                + myUI.getMessage(SptMessages.Average) + "%",
                                sysSettings.dFormat.format(val));
                    }
                }
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Total) + " "
                    + myUI.getMessage(SptMessages.Students)).setValue(
                    result.getInt("disc_quantity"));
            footerVal = cdr.dataTable.getColumnFooter(
                    myUI.getMessage(SptMessages.Total) + " "
                    + myUI.getMessage(SptMessages.Students));
            if (footerVal == null) {
                footerVal = "0";
            }
            if (counter != 0) {
                cdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                        + myUI.getMessage(SptMessages.Students), (Integer.parseInt(footerVal)
                        + result.getInt("disc_quantity")) + "");
            } else {
                cdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                        + myUI.getMessage(SptMessages.Students),
                        result.getInt("disc_quantity") + "");
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Total) + " "
                    + myUI.getMessage(SptMessages.DiscountAmount)).setValue(
                    result.getDouble("disc_amount"));
            footerVal = cdr.dataTable.getColumnFooter(
                    myUI.getMessage(SptMessages.Total) + " "
                    + myUI.getMessage(SptMessages.DiscountAmount));
            if (footerVal == null) {
                footerVal = "0";
            }
            if (counter != 0) {
                cdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                        + myUI.getMessage(SptMessages.DiscountAmount),
                        sysSettings.dFormat.format(Double.parseDouble(footerVal)
                                + result.getDouble("disc_amount")));
            } else {
                cdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                        + myUI.getMessage(SptMessages.DiscountAmount),
                        sysSettings.dFormat.format(result.getDouble("disc_amount")));
            }
            counter++;
        }
        Iterator iter = cdr.dataTable.getContainerDataSource().getItemIds().iterator();
        double totalDiscAmount = 0.0;
        String footerVal = cdr.dataTable.getColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                + myUI.getMessage(SptMessages.DiscountAmount));
        if (footerVal != null) {
            totalDiscAmount = Double.parseDouble(footerVal);
            while (iter.hasNext()) {
                Object next = iter.next();
                if (totalDiscAmount != 0.0) {
                    cdr.dataTable.getContainerProperty(next, myUI.getMessage(SptMessages.Total) + " "
                            + myUI.getMessage(SptMessages.Average) + "%").setValue((Double) cdr.dataTable.getContainerProperty(next, myUI.getMessage(SptMessages.Total) + " "
                            + myUI.getMessage(SptMessages.DiscountAmount)).getValue() / (totalDiscAmount / 100));
                }
            }
        }
    }

    public void execSQL_Discounts_by_schools(MyVaadinUI myUI, int year_id,
            String edu_statuses_ids, SchoolDiscountsReport sdr) throws SQLException {
        SystemSettings sysSettings = new SystemSettings();
        String sql = "SELECT sd.discount_id, COUNT(sd.discount_id) AS disc_quantity, "
                + "SUM(sd.discount_value) AS disc_amount, "
                + "SUM(c.amount) AS contr_amount";
        Iterator school_iter = ((Set<?>) sdr.schoolTable.getValue()).iterator();
        while (school_iter.hasNext()) {
            Object next = school_iter.next();
            sql += ", COUNT(IF(st.school_id = " + next + ", 1, NULL)) AS disc_quantity" + next + ", "
                    + "SUM(IF(st.school_id = " + next + ", sd.discount_value, 0)) AS disc_amount" + next + ", "
                    + "SUM(IF(st.school_id = " + next + ", c.amount, 0)) AS contr_amount" + next + " ";
        }
        sql += " FROM student_discount AS sd "
                + "LEFT JOIN student AS st ON sd.student_id = st.id "
                + "LEFT JOIN student_contract AS sc ON sd.student_id = sc.student_id "
                + "AND sd.year_id = sc.year_id "
                + "LEFT JOIN contract AS c ON c.id = sc.contract_id "
                + "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id "
                + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 "
                + "GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id "
                + "LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid "
                + "LEFT JOIN education_status AS edu ON edu.id = "
                + "CASE WHEN stud_o.to_education_status_id IS NULL "
                + "THEN st.education_status_id ELSE stud_o.to_education_status_id END "
                + "WHERE sd.year_id = ? AND st.school_id IN ("
                + sysSettings.convertCollectionToStr(((Set<?>) sdr.schoolTable.getValue())) + ") "
                + "AND sd.discount_id IN ("
                + sysSettings.convertCollectionToStr(((Set<?>) sdr.discountsTable.getValue())) + ") "
                + "AND edu.id IN (" + edu_statuses_ids + ") "
                + "GROUP BY sd.discount_id;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Discount), String.class, 0);
        school_iter = ((Set<?>) sdr.schoolTable.getValue()).iterator();
        while (school_iter.hasNext()) {
            Object next = school_iter.next();
            container.addContainerProperty(sdr.schoolTable.getContainerProperty(
                    next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                    + myUI.getMessage(SptMessages.Students), Integer.class, 0);
            container.addContainerProperty(sdr.schoolTable.getContainerProperty(
                    next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                    + myUI.getMessage(SptMessages.DiscountAmount), Double.class, 0.0);
            container.addContainerProperty(sdr.schoolTable.getContainerProperty(
                    next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                    + myUI.getMessage(SptMessages.Average) + "%", Double.class, 0.0);
        }
        container.addContainerProperty(myUI.getMessage(SptMessages.Total) + " "
                + myUI.getMessage(SptMessages.Students), Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Total) + " "
                + myUI.getMessage(SptMessages.DiscountAmount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Total) + " "
                + myUI.getMessage(SptMessages.Average) + "%", Double.class, 0.0);
        sdr.dataTable.setContainerDataSource(container);
        Iterator disc_iter = ((Set<?>) sdr.discountsTable.getValue()).iterator();
        while (disc_iter.hasNext()) {
            Object next = disc_iter.next();
            Item item = container.addItem((Integer) next);
            item.getItemProperty(myUI.getMessage(SptMessages.Discount)).setValue(
                    sdr.discountsTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue());
        }
        sdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Discount),
                myUI.getMessage(SptMessages.Total));
        sdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                + myUI.getMessage(SptMessages.Average) + "%", "100.00");
        int counter = 0;
        while (result.next()) {
            Item item = container.getItem(result.getInt("sd.discount_id"));
            school_iter = ((Set<?>) sdr.schoolTable.getValue()).iterator();
            String footerVal;
            while (school_iter.hasNext()) {
                Object next = school_iter.next();
                item.getItemProperty(sdr.schoolTable.getContainerProperty(
                        next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                        + myUI.getMessage(SptMessages.Students)).setValue(
                        result.getInt("disc_quantity" + next));
                footerVal = sdr.dataTable.getColumnFooter(sdr.schoolTable.getContainerProperty(
                        next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                        + myUI.getMessage(SptMessages.Students));
                if (footerVal == null) {
                    footerVal = "0";
                }
                if (counter != 0) {
                    sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.Students), (Integer.parseInt(footerVal)
                            + result.getInt("disc_quantity" + next)) + "");
                } else {
                    sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.Students),
                            result.getInt("disc_quantity" + next) + "");
                }
                item.getItemProperty(sdr.schoolTable.getContainerProperty(
                        next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                        + myUI.getMessage(SptMessages.DiscountAmount)).setValue(
                        result.getDouble("disc_amount" + next));
                footerVal = sdr.dataTable.getColumnFooter(
                        sdr.schoolTable.getContainerProperty(
                                next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                        + myUI.getMessage(SptMessages.DiscountAmount));
                if (footerVal == null) {
                    footerVal = "0";
                }
                if (counter != 0) {
                    sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.DiscountAmount),
                            sysSettings.dFormat.format(Double.parseDouble(footerVal)
                                    + result.getDouble("disc_amount" + next)));
                } else {
                    sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.DiscountAmount),
                            sysSettings.dFormat.format(result.getDouble("disc_amount" + next)));
                }
                if (result.getDouble("contr_amount" + next) != 0.0) {
                    double val = result.getDouble("disc_amount" + next)
                            / result.getDouble("contr_amount" + next) * 100.0;
                    item.getItemProperty(sdr.schoolTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.Average) + "%").setValue(val);
                    footerVal = sdr.dataTable.getColumnFooter(sdr.schoolTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                            + myUI.getMessage(SptMessages.Average) + "%");
                    if (footerVal == null) {
                        footerVal = "0";
                    }
                    if (counter != 0) {
                        sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                                next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                                + myUI.getMessage(SptMessages.Average) + "%",
                                sysSettings.dFormat.format(Double.parseDouble(footerVal)
                                        + val));
                    } else {
                        sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                                next, myUI.getMessage(SptMessages.Name)).getValue() + " "
                                + myUI.getMessage(SptMessages.Average) + "%",
                                sysSettings.dFormat.format(val));
                    }
                }
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Total) + " "
                    + myUI.getMessage(SptMessages.Students)).setValue(
                    result.getInt("disc_quantity"));
            footerVal = sdr.dataTable.getColumnFooter(
                    myUI.getMessage(SptMessages.Total) + " "
                    + myUI.getMessage(SptMessages.Students));
            if (footerVal == null) {
                footerVal = "0";
            }
            if (counter != 0) {
                sdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                        + myUI.getMessage(SptMessages.Students), (Integer.parseInt(footerVal)
                        + result.getInt("disc_quantity")) + "");
            } else {
                sdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                        + myUI.getMessage(SptMessages.Students),
                        result.getInt("disc_quantity") + "");
            }
            item.getItemProperty(myUI.getMessage(SptMessages.Total) + " "
                    + myUI.getMessage(SptMessages.DiscountAmount)).setValue(
                    result.getDouble("disc_amount"));
            footerVal = sdr.dataTable.getColumnFooter(
                    myUI.getMessage(SptMessages.Total) + " "
                    + myUI.getMessage(SptMessages.DiscountAmount));
            if (footerVal == null) {
                footerVal = "0";
            }
            if (counter != 0) {
                sdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                        + myUI.getMessage(SptMessages.DiscountAmount),
                        sysSettings.dFormat.format(Double.parseDouble(footerVal)
                                + result.getDouble("disc_amount")));
            } else {
                sdr.dataTable.setColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                        + myUI.getMessage(SptMessages.DiscountAmount),
                        sysSettings.dFormat.format(result.getDouble("disc_amount")));
            }

            counter++;
        }
        Iterator iter = sdr.dataTable.getContainerDataSource().getItemIds().iterator();
        double totalDiscAmount = 0.0;
        String footerVal = sdr.dataTable.getColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                + myUI.getMessage(SptMessages.DiscountAmount));
        if (footerVal != null) {
            totalDiscAmount = Double.parseDouble(footerVal);
            while (iter.hasNext()) {
                Object next = iter.next();
                if (totalDiscAmount != 0.0) {
                    sdr.dataTable.getContainerProperty(next, myUI.getMessage(SptMessages.Total) + " "
                            + myUI.getMessage(SptMessages.Average) + "%").setValue((Double) sdr.dataTable.getContainerProperty(next, myUI.getMessage(SptMessages.Total) + " "
                            + myUI.getMessage(SptMessages.DiscountAmount)).getValue() / (totalDiscAmount / 100));
                }
            }
        }
    }

    public int exec_disc_count(int id) throws SQLException {
        String sql = "SELECT count(discount_id) FROM student_discount "
                + "where discount_id = ?;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        int i = 0;
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            i = result.getInt("count(discount_id)");
        }
        return i;
    }

    public int exec_update_emp_id(int emp_id, String id) throws SQLException {
        String sql = "UPDATE student_discount SET employee_id=? "
                + "WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, emp_id);
        stat.setString(2, id);
        int status = stat.executeUpdate();
        return status;
    }
}
