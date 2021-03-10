/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.Employee;
import kg.alex.spt.domain.EmployeesCount;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DbEmployee extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbEmployee.class);

    public DbEmployee() throws Exception {
        super();
    }

    public EmployeesCount execSQL(int scl_id)
            throws SQLException {
        String sql = "SELECT COUNT(IF(p.id > 2, p.id, NULL)) AS others_count, "
                + "IFNULL(GROUP_CONCAT(CASE WHEN p.id > 2 THEN CONCAT(e.surname, ' ', e.name) "
                + "ELSE NULL END ORDER BY p.id ASC SEPARATOR ', '), '') AS others, "
                + "IFNULL(GROUP_CONCAT(CASE WHEN p.id = 1 THEN CONCAT(e.surname, ' ', e.name) "
                + "ELSE NULL END), '') AS director, "
                + "IFNULL(GROUP_CONCAT(CASE WHEN p.id = 2 THEN CONCAT(e.surname, ' ', e.name) "
                + "ELSE NULL END), '') AS accountent "
                + "FROM spt.school AS scl "
                + "LEFT JOIN hr_employee_order AS eo ON scl.id = eo.school_id AND eo.to_date IS NULL "
                + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id "
                + "LEFT JOIN hr_position AS hrp ON eo.hr_position_id = hrp.id "
                + "LEFT JOIN position AS p ON hrp.id = p.hr_position_id "
                + "LEFT JOIN employee AS e ON eo.employee_id = e.id "
                + "WHERE eo.school_id = ? AND ord.working_status_id = 2;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        EmployeesCount e = new EmployeesCount();
        while (result.next()) {
            e.setDirector(result.getString("director"));
            e.setAccountent(result.getString("accountent"));
            e.setOthers(result.getString("others"));
            e.setOthers_count(result.getInt("others_count"));
        }
        return e;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int school_id, String working_statuses,
                                    IndexedContainer workingStatCont, boolean withAdmin, boolean withHr, int employee_id) throws SQLException {


        if (working_statuses.equals("") || working_statuses == null) {
            working_statuses = "-1";
        }
        String sql = "SELECT e.id, e.login, e.name, e.surname, e.middle_name, e.date_of_birth, e.photo, "
                + "g.id, g.name, n.id, n.name, m.id, m.name, ws.name, ws.id, p.name, p.id, ord.visible_hr_orders, "
                + "group_concat(DISTINCT p2.id ORDER BY eo2.id ASC separator ', ') as extra_position_ids, "
                + "group_concat(DISTINCT p2.name ORDER BY eo2.id ASC separator ', ') as extra_positions, cat.id, "
                + "group_concat(DISTINCT if(eb.hr_importance_id = 1, br.name, null) ORDER BY eb.id ASC separator ', ') as main_branch, "
                + "group_concat(DISTINCT if(eb.hr_importance_id = 2, br.name, null) ORDER BY eb.id ASC separator ', ') as extra_branches, "
                + "group_concat(DISTINCT up.permissions separator ';') as permissions, ebh.hours, ebh.extra, eo3.id, cat.parent_id "
                + "FROM employee AS e LEFT JOIN gender AS g ON g.id = e.gender_id "
                + "LEFT JOIN nationality AS n ON n.id = e.nationality_id "
                + "LEFT JOIN hr_martial_status AS m ON m.id = e.hr_martial_status_id "
                + "LEFT JOIN hr_employee_branch AS eb ON eb.employee_id = e.id "
                + "LEFT JOIN hr_branch AS br ON br.id = eb.hr_branch_id "
                + "LEFT JOIN "
                + "(select sum(hours) as hours, sum(extra_hours) as extra, employee_id as e_id, year_id as y_id, school_id as sch_id "
                + "from hr_employee_branch_hours group by employee_id, year_id, school_id) AS ebh ON ebh.e_id = e.id and ebh.y_id = ? and ebh.sch_id = ? "
                + "LEFT JOIN hr_employee_order AS eo ON eo.employee_id = e.id AND eo.to_date IS NULL "
                + "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                + "LEFT JOIN position AS pos ON p.id = pos.hr_position_id "
                + "LEFT JOIN hr_employee_order AS eo2 ON eo2.employee_id = e.id AND eo2.hr_orders_id = 2 and (eo2.to_date IS NULL or eo2.to_date>=NOW()) "
                + "LEFT JOIN hr_position AS p2 ON p2.id = eo2.hr_position_id "
                + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id "
                + "LEFT JOIN working_status AS ws ON ord.working_status_id = ws.id "
                + "LEFT JOIN hr_employee_order AS eo3 ON eo3.employee_id = e.id AND eo3.from_to_school_id = ? AND eo3.hr_orders_id = 8 "
                + "left join user_permission as up on up.role_name = e.login "
                + "LEFT JOIN acc_category as cat ON cat.employee_id = e.id and cat.school_id = eo.school_id "
                + "WHERE eo.school_id = ? ";
        if (!withAdmin) {
            sql += " and (pos.id IS NULL or pos.id != 5) ";
        } else if (!withAdmin && !withHr) {
            sql += " and (pos.id IS NULL or (pos.id != 5 and pos.id !=25) ";
        }
        if (employee_id != 0) {
            sql += " and e.id= " + employee_id + " ";
        } else {
            sql += " AND ord.working_status_id IS NOT NULL and ws.id in (" + working_statuses + ") ";
        }
        sql += " group by e.id order by e.id DESC ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, myUi.getUser().getCurrent_year().getId());
        stat.setInt(2, school_id);
        stat.setInt(3, school_id);
        stat.setInt(4, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Id), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Firstname), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Surname), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Middlename), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.DateOfBirth), Date.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Photo), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.MainPosition), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.MainBranch), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.ExtraBranches), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.ExtraPosition), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Hours), Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.ExtraHours), Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(SptMessages.WorkingStatus), String.class, null);
        container.addContainerProperty(SystemSettings.gender_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.nationality_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.martial_status_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.working_status_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.position_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.extra_position_ids, String.class, null);
        container.addContainerProperty(SystemSettings.salary_category_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.acc_category_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.is_modifiable, Boolean.class, true);
        container.addContainerProperty(SystemSettings.visible_hr_orders, String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Permissions), String.class, null);
        container.addContainerProperty(SystemSettings.id, Integer.class, 0);

        Iterator iter = workingStatCont.getItemIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            workingStatCont.getContainerProperty(next, SystemSettings.count).setValue(0);
        }
        while (result.next()) {
            Item item = container.addItem(result.getInt("e.id"));
            item.getItemProperty(SystemSettings.id).setValue(result.getInt("e.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Id)).setValue(
                    result.getString("e.login"));
            item.getItemProperty(myUi.getMessage(SptMessages.Firstname)).setValue(
                    result.getString("e.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Surname)).setValue(
                    result.getString("e.surname"));
            item.getItemProperty(myUi.getMessage(SptMessages.Middlename)).setValue(
                    result.getString("e.middle_name"));
            item.getItemProperty(myUi.getMessage(SptMessages.DateOfBirth)).setValue(
                    result.getDate("e.date_of_birth"));
            item.getItemProperty(myUi.getMessage(SptMessages.Photo)).setValue(
                    result.getString("e.photo"));
            item.getItemProperty(myUi.getMessage(SptMessages.MainPosition)).setValue(
                    result.getString("p.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.MainBranch)).setValue(
                    result.getString("main_branch"));
            item.getItemProperty(myUi.getMessage(SptMessages.WorkingStatus)).setValue(
                    result.getString("ws.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.ExtraPosition)).setValue(
                    result.getString("extra_positions"));
            item.getItemProperty(SystemSettings.extra_position_ids).setValue(
                    result.getString("extra_position_ids"));
            item.getItemProperty(myUi.getMessage(SptMessages.ExtraBranches)).setValue(
                    result.getString("extra_branches"));
            item.getItemProperty(myUi.getMessage(SptMessages.Hours)).setValue(
                    result.getInt("ebh.hours"));
            item.getItemProperty(myUi.getMessage(SptMessages.ExtraHours)).setValue(
                    result.getInt("ebh.extra"));
            item.getItemProperty(SystemSettings.gender_id).setValue(
                    result.getInt("g.id"));
            item.getItemProperty(SystemSettings.nationality_id).setValue(
                    result.getInt("n.id"));
            item.getItemProperty(SystemSettings.martial_status_id).setValue(
                    result.getInt("m.id"));
            item.getItemProperty(SystemSettings.working_status_id).setValue(
                    result.getInt("ws.id"));
            if (result.getInt("eo3.id") != 0) {
                item.getItemProperty(SystemSettings.is_modifiable).setValue(false);
            }
            item.getItemProperty(SystemSettings.visible_hr_orders).setValue(result.getString("ord.visible_hr_orders"));
            item.getItemProperty(myUi.getMessage(SptMessages.Permissions)).setValue(result.getString("permissions"));
            item.getItemProperty(SystemSettings.position_id).setValue(result.getInt("p.id"));
            item.getItemProperty(SystemSettings.salary_category_id).setValue(result.getInt("cat.parent_id"));
            item.getItemProperty(SystemSettings.acc_category_id).setValue(result.getInt("cat.id"));
            workingStatCont.getContainerProperty(result.getInt("ws.id"), SystemSettings.count).setValue(
                    (Integer) workingStatCont.getContainerProperty(result.getInt("ws.id"), SystemSettings.count).getValue() + 1);
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int school_id,
                                    boolean withAdmin, boolean withHr) throws SQLException {
        String sql = "SELECT e.id, CONCAT(e.surname, ' ', e.name, ' ', IFNULL(e.middle_name, '')) AS fullname, p.name, "
                + "group_concat(DISTINCT p2.name ORDER BY eo2.id ASC separator ', ') as extra_positions, "
                + "group_concat(DISTINCT if(eb.hr_importance_id = 1, br.name, null) ORDER BY eb.id ASC separator ', ') as main_branch, "
                + "group_concat(DISTINCT if(eb.hr_importance_id = 2, br.name, null) ORDER BY eb.id ASC separator ', ') as extra_branches, "
                + "ebh.hours, ebh.extra "
                + "FROM employee AS e "
                + "LEFT JOIN hr_employee_branch AS eb ON eb.employee_id = e.id "
                + "LEFT JOIN hr_branch AS br ON br.id = eb.hr_branch_id "
                + "LEFT JOIN "
                + "(select sum(hours) as hours, sum(extra_hours) as extra, employee_id as e_id, year_id as y_id, school_id as sch_id "
                + "from hr_employee_branch_hours group by employee_id, year_id, school_id) AS ebh ON ebh.e_id = e.id "
                + "and ebh.y_id = ? and ebh.sch_id = ? "
                + "LEFT JOIN hr_employee_order AS eo ON eo.employee_id = e.id AND eo.to_date IS NULL "
                + "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                + "LEFT JOIN position AS pos ON p.id = pos.hr_position_id "
                + "LEFT JOIN hr_employee_order AS eo2 ON eo2.employee_id = e.id AND eo2.hr_orders_id = 2 and (eo2.to_date IS NULL or eo2.to_date>=NOW()) "
                + "LEFT JOIN hr_position AS p2 ON p2.id = eo2.hr_position_id "
                + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id "
                + "LEFT JOIN working_status AS ws ON ord.working_status_id = ws.id "
                + "WHERE eo.school_id = ? ";
        if (!withAdmin) {
            sql += " and (pos.id IS NULL or pos.id != 5) ";
        } else if (!withAdmin && !withHr) {
            sql += " and (pos.id IS NULL or (pos.id != 5 and pos.id !=25) ";
        }
        sql += " AND ord.working_status_id IS NOT NULL and ws.id in (2,5) group by e.id order by p.hr_position_category_id, p.id DESC ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, myUi.getUser().getCurrent_year().getId());
        stat.setInt(2, school_id);
        stat.setInt(3, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.FullName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.MainPosition), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.ExtraPosition), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.MainBranch), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.ExtraBranches), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.TotalHours) + myUi.getUser().getCurrent_year().getName(), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("e.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.FullName)).setValue(
                    result.getString("fullname"));
            item.getItemProperty(myUi.getMessage(SptMessages.MainPosition)).setValue(
                    result.getString("p.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.MainBranch)).setValue(
                    result.getString("main_branch"));
            item.getItemProperty(myUi.getMessage(SptMessages.ExtraPosition)).setValue(
                    result.getString("extra_positions"));
            item.getItemProperty(myUi.getMessage(SptMessages.ExtraBranches)).setValue(
                    result.getString("extra_branches"));
            item.getItemProperty(myUi.getMessage(SptMessages.TotalHours) + myUi.getUser().getCurrent_year().getName()).setValue(
                    result.getInt("ebh.hours") + " (" + result.getInt("ebh.extra") + ")");
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int school_id, int position_id) throws SQLException {
        String sql = "SELECT e.id, CONCAT(e.surname, ' ', e.name, ' ', IFNULL(e.middle_name, '')) AS fullname FROM employee AS e "
                + "LEFT JOIN hr_employee_order AS eo ON eo.employee_id = e.id AND eo.to_date IS NULL "
                + "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                + "LEFT JOIN position AS pos ON p.id = pos.hr_position_id "
                + "LEFT JOIN hr_employee_order AS eo2 ON eo2.employee_id = e.id AND eo2.hr_orders_id = 2 and (eo2.to_date IS NULL or eo2.to_date>=NOW()) "
                + "LEFT JOIN hr_position AS p2 ON p2.id = eo2.hr_position_id "
                + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id "
                + "WHERE eo.school_id = ? ";
        if (position_id != 0) {
            sql += " and (p2.id = " + position_id + " or pos.id = " + position_id + ") ";
        }
        sql += " AND ord.working_status_id IS NOT NULL and ord.working_status_id in (2,5) group by e.id order by p.hr_position_category_id, p.id DESC ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Name), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("e.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Name)).setValue(
                    result.getString("fullname"));
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int school_id) throws SQLException {


        String sql = "SELECT e.id, e.login, e.name, e.surname, e.photo, p.id, p.name, eo.from_date, eo.note, ebh.lessons, ebh.hours, ebh.extra, "
                + "es.fullname, GROUP_CONCAT(DISTINCT IF(eb.hr_importance_id = 1, br.name, NULL) ORDER BY eb.id ASC SEPARATOR ', ') AS main_branch, "
                + "GROUP_CONCAT(DISTINCT IF(eb.hr_importance_id = 2, br.name, NULL) ORDER BY eb.id ASC SEPARATOR ', ') AS extra_branches, "
                + "GROUP_CONCAT(DISTINCT p2.name ORDER BY eo2.id ASC SEPARATOR ', ') AS extra_positions, "
                + "GROUP_CONCAT(DISTINCT CONCAT(wp.name, ' (', p3.name, ')') ORDER BY ew.id ASC SEPARATOR ', ') AS spouse_work, "
                + "GROUP_CONCAT(DISTINCT CONCAT(ech.fullname, ' ', (YEAR(NOW()) - YEAR(ech.date_of_birth)), ' y/o', IF(ech.hr_education_status_id = 1, "
                + "CONCAT(' (', ech.institution, ')'), '')) ORDER BY ech.id ASC SEPARATOR ', ') AS children, cat.id "
                + "FROM employee AS e "
                + "LEFT JOIN hr_employee_order AS eo ON eo.employee_id = e.id AND eo.to_date IS NULL "
                + "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                + "LEFT JOIN hr_employee_order AS eo2 ON eo2.employee_id = e.id AND eo2.hr_orders_id = 2 "
                + "AND (eo2.to_date IS NULL OR eo2.to_date >= NOW()) LEFT JOIN hr_position AS p2 ON p2.id = eo2.hr_position_id "
                + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id LEFT JOIN working_status AS ws ON ord.working_status_id = ws.id "
                + "LEFT JOIN hr_employee_branch AS eb ON e.id = eb.employee_id LEFT JOIN hr_branch AS br ON br.id = eb.hr_branch_id "
                + "LEFT JOIN (SELECT t1.e_id AS e_id, t1.y_id AS y_id, t1.sch_id AS sch_id, SUM(t1.hours) AS hours, SUM(t1.extra) AS extra,"
                + " GROUP_CONCAT(DISTINCT t1.lessons SEPARATOR ', ') AS lessons FROM (SELECT CONCAT(br.name, ' - ', "
                + "SUM(el.hours), ' (', SUM(el.extra_hours), ')') AS lessons, el.employee_id AS e_id, el.year_id AS y_id, el.school_id "
                + "AS sch_id, SUM(el.hours) AS hours, SUM(el.extra_hours) AS extra FROM hr_employee_branch_hours AS el "
                + "LEFT JOIN hr_branch AS br ON br.id = el.hr_branch_id GROUP BY el.employee_id , el.hr_branch_id , el.year_id , el.school_id) AS t1 "
                + "GROUP BY t1.e_id , t1.y_id , t1.sch_id) AS ebh ON ebh.e_id = e.id AND ebh.y_id = ? AND ebh.sch_id = ? "
                + "LEFT JOIN hr_employee_spouse AS es ON es.employee_id = e.id "
                + "LEFT JOIN hr_employee_work AS ew ON ew.employee_id = e.id AND ew.hr_own_id = 2 AND ew.working_status_id = 2 "
                + "LEFT JOIN hr_work_place AS wp ON ew.hr_work_place_id = wp.id "
                + "LEFT JOIN hr_position AS p3 ON ew.position_id = p3.id "
                + "LEFT JOIN hr_employee_children AS ech ON ech.employee_id = e.id "
                + "LEFT JOIN acc_category as cat ON cat.employee_id = e.id and cat.school_id = eo.school_id "
                + "WHERE eo.school_id = ? "
                + "AND ord.working_status_id IS NOT NULL AND ws.id = 2 GROUP BY e.id ORDER BY eo.id;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, myUi.getUser().getCurrent_year().getId());
        stat.setInt(2, school_id);
        stat.setInt(3, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.Id), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Firstname), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Surname), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.MainPosition), ComboBoxMax.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.FromDate), DateField.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Note), TextField.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Position), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.ExtraPosition), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.MainBranch), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.ExtraBranches), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Lessons), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.SpouseInfo), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Children), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Photo), String.class, null);
        container.addContainerProperty(SystemSettings.crud_status, String.class, null);
        container.addContainerProperty(SystemSettings.school_id, Integer.class, school_id);
        container.addContainerProperty(SystemSettings.position_id, Integer.class, 0);
        container.addContainerProperty(SystemSettings.acc_category_id, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("e.id"));
            item.getItemProperty(myUi.getMessage(SptMessages.Id)).setValue(
                    result.getString("e.login"));
            item.getItemProperty(myUi.getMessage(SptMessages.Firstname)).setValue(
                    result.getString("e.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.Surname)).setValue(
                    result.getString("e.surname"));
            item.getItemProperty(myUi.getMessage(SptMessages.Position)).setValue(
                    result.getString("p.name"));
            item.getItemProperty(myUi.getMessage(SptMessages.ExtraPosition)).setValue(
                    result.getString("extra_positions"));
            item.getItemProperty(myUi.getMessage(SptMessages.MainBranch)).setValue(
                    result.getString("main_branch"));
            item.getItemProperty(myUi.getMessage(SptMessages.ExtraBranches)).setValue(
                    result.getString("extra_branches"));
            if (result.getString("ebh.lessons") != null) {
                item.getItemProperty(myUi.getMessage(SptMessages.Lessons)).setValue(
                        result.getString("ebh.lessons") + "; " + myUi.getMessage(SptMessages.Total) + " - "
                                + result.getInt("ebh.hours") + "(" + result.getInt("ebh.extra") + ")");
            }
            item.getItemProperty(myUi.getMessage(SptMessages.SpouseInfo)).setValue(
                    result.getString("es.fullname"));
            if (result.getString("es.fullname") != null) {
                item.getItemProperty(myUi.getMessage(SptMessages.SpouseInfo)).setValue(
                        result.getString("es.fullname") + " - " + result.getString("spouse_work"));
            }
            item.getItemProperty(myUi.getMessage(SptMessages.Children)).setValue(
                    result.getString("children"));
            item.getItemProperty(myUi.getMessage(SptMessages.Photo)).setValue(
                    result.getString("e.photo"));
            ComboBoxMax cb = new ComboBoxMax();
            cb.setDescription(myUi.getMessage(SptMessages.MainPosition));
            cb.setStyleName(ValoTheme.COMBOBOX_TINY);
            cb.setWidth("100%");
            cb.setItemCaptionPropertyId(myUi.getMessage(SptMessages.Name));
            cb.setFilteringMode(FilteringMode.CONTAINS);
            cb.setRequired(true);
            cb.setRequiredError(myUi.getMessage(SptMessages.RequiredField));
            try {
                DbDefinition dbp = new DbDefinition();
                dbp.connect();
                cb.setContainerDataSource(dbp.exec_for_select(myUi, SystemSettings.hr_positionTable));
                dbp.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setNullSelectionAllowed(false);
            cb.setValue(result.getInt("p.id"));
            cb.setEnabled(false);
            item.getItemProperty(myUi.getMessage(SptMessages.MainPosition)).setValue(cb);

            DateField df = new DateField();
            df.setWidth("100%");
            df.setStyleName(ValoTheme.DATEFIELD_TINY);
            df.setRequired(true);
            df.setRequiredError(myUi.getMessage(SptMessages.RequiredField));
            df.setDateFormat(SystemSettings.datePattern);
            df.setDescription(myUi.getMessage(SptMessages.FromDate));
            df.setValue(result.getDate("eo.from_date"));
            df.setRangeEnd(new Date());
            df.setEnabled(false);
            item.getItemProperty(myUi.getMessage(SptMessages.FromDate)).setValue(df);

            TextField tf = new TextField();
            tf.setDescription(myUi.getMessage(SptMessages.Note));
            tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
            tf.setWidth("100%");
            tf.addValidator(new StringLengthValidator(myUi.getMessage(SptMessages.NotifWrongValue), null, 300, true));
            if (result.getString("eo.note") != null) {
                tf.setValue(result.getString("eo.note"));
            }
            tf.setEnabled(false);
            item.getItemProperty(myUi.getMessage(SptMessages.Note)).setValue(tf);
            item.getItemProperty(SystemSettings.position_id).setValue(result.getInt("p.id"));
            item.getItemProperty(SystemSettings.acc_category_id).setValue(result.getInt("cat.id"));
        }
        return container;
    }

    public int exec_insert(Employee e) throws SQLException {
        String sql = "INSERT ignore INTO employee (login, password, name, "
                + "surname, middle_name, date_of_birth, photo, gender_id, "
                + "hr_martial_status_id,nationality_id,employee_id,modification_date) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,NOW());";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, e.getLogin());
        stat.setString(2, e.getPassword());
        stat.setString(3, e.getName());
        stat.setString(4, e.getSurname());
        if (e.getMiddle_name() != null) {
            stat.setString(5, e.getMiddle_name());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        stat.setDate(6, new java.sql.Date(e.getBirth_date().getTime()));
        stat.setString(7, e.getPhoto());
        stat.setInt(8, e.getGender_id());
        stat.setInt(9, e.getMartial_status_id());
        stat.setInt(10, e.getNationality_id());
        stat.setInt(11, e.getModified_by_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Employee e, String pass) throws SQLException {
        String sql = "UPDATE employee SET login=?, name=?, surname=?, middle_name=?, ";
        if (pass != null && !pass.equals("")) {
            sql += ("password='" + pass + "',");
        }
        sql += "date_of_birth=?, nationality_id=?, gender_id=?, hr_martial_status_id=?, employee_id=?, photo=?, modification_date=NOW() "
                + "WHERE id=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, e.getLogin());
        stat.setString(2, e.getName());
        stat.setString(3, e.getSurname());
        if (e.getMiddle_name() != null) {
            stat.setString(4, e.getMiddle_name());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        stat.setDate(5, new java.sql.Date(e.getBirth_date().getTime()));
        stat.setInt(6, e.getNationality_id());
        stat.setInt(7, e.getGender_id());
        stat.setInt(8, e.getMartial_status_id());
        stat.setInt(9, e.getModified_by_id());
        stat.setString(10, e.getPhoto());
        stat.setInt(11, e.getId());
        int status = stat.executeUpdate();
        return status;
    }

    public int exec_delete_perm(String role_name) throws SQLException {
        String sql = "DELETE FROM user_permission WHERE role_name=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, role_name);
        return stat.executeUpdate();
    }

    public int exec_delete_role(String login) throws SQLException {
        String sql = "DELETE FROM user_roles WHERE login=?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, login);
        return stat.executeUpdate();
    }

    public int exec_insert_perm(String login, String perm) throws SQLException {
        String sql = "INSERT IGNORE INTO user_permission (role_name,permissions) "
                + "VALUES(?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, login);
        stat.setString(2, perm);
        int st = stat.executeUpdate();
        return st;
    }

    public int exec_insert_role(String loginRName, String roleName) throws SQLException {
        String sql = "INSERT IGNORE INTO user_roles (login,role_name) "
                + "VALUES(?,?);";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, loginRName);
        stat.setString(2, roleName);
        int st = stat.executeUpdate();
        return st;
    }

    public int exec_update_role(String oldLogin, String newLogin, String roleName)
            throws SQLException {
        String sql = "UPDATE user_roles SET login=?, role_name=? "
                + "WHERE login = ? ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, newLogin);
        stat.setString(2, roleName);
        stat.setString(3, oldLogin);
        int status = stat.executeUpdate();
        return status;
    }

    public int exec_update_perm(String oldroleName, String newroleName)
            throws SQLException {
        String sql = "UPDATE user_permission SET role_name=? "
                + "WHERE role_name = ? ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, newroleName);
        stat.setString(2, oldroleName);
        int status = stat.executeUpdate();
        return status;
    }

    public IndexedContainer execPermissionSQL(MyVaadinUI myUi) throws SQLException {
        String sql = "SELECT p.java_class_name, p.caption, p.permissions FROM permissions as p;";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.ClassCaption), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Value), String.class, null);
        container.addContainerProperty(myUi.getMessage(SptMessages.Functions), ComboBoxMultiselectMax.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getString("p.java_class_name"));
            item.getItemProperty(myUi.getMessage(SptMessages.ClassName)).setValue(
                    result.getString("p.java_class_name"));
            item.getItemProperty(myUi.getMessage(SptMessages.ClassCaption)).setValue(
                    result.getString("p.caption"));
            item.getItemProperty(myUi.getMessage(SptMessages.Value)).setValue(
                    result.getString("p.permissions"));
        }
        return container;
    }
}
