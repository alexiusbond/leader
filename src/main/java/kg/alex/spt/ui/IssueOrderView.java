package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.utils.Settings;
import kg.alex.spt.dao.*;
import kg.alex.spt.domain.StudentOrder;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class IssueOrderView extends HorizontalSplitPanel implements Button.ClickListener {

    static final Logger logger = LogManager.getLogger(IssueOrderView.class);
    private final MyVaadinUI myUI;
    private final FilterTable studentsTable;
    private final Table historyTable;
    private final String[] HISTORY_NATURAL_COL_ORDER;
    private Button saveBtn;
    private ComboBox classSelect, orderSelect;
    private ComboBoxMultiselect reasonsMCB;
    private VerticalLayout settingsLay;
    private int selected_student_id = 0;

    public IssueOrderView(MyVaadinUI myUI) {
        this.myUI = myUI;

        String[] STUDENTS_NATURAL_COL_ORDER = new String[]{Settings.button,
                myUI.getMessage(SptMessages.Id), myUI.getMessage(SptMessages.FirstName),
                myUI.getMessage(SptMessages.LastName), myUI.getMessage(SptMessages.ClassName),
                myUI.getMessage(SptMessages.EducationStatus)};
        HISTORY_NATURAL_COL_ORDER = new String[]{Settings.button, myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.OrderType), myUI.getMessage(SptMessages.FromClass),
                myUI.getMessage(SptMessages.ToClass), myUI.getMessage(SptMessages.FromEducationStatus),
                myUI.getMessage(SptMessages.ToEducationStatus), myUI.getMessage(SptMessages.Year),
                myUI.getMessage(SptMessages.Reasons)};
        buildSettingsLayout();

        VerticalLayout tablesLay = new VerticalLayout();
        tablesLay.setMargin(true);
        tablesLay.setSpacing(true);
        tablesLay.setSizeFull();

        studentsTable = new FilterTable();
        studentsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        studentsTable.setStyleName(ValoTheme.TABLE_COMPACT);
        studentsTable.setCaption(myUI.getMessage(SptMessages.AllStudents));
        studentsTable.setSizeFull();
        studentsTable.setNullSelectionAllowed(false);
        studentsTable.setMultiSelect(true);
        studentsTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        studentsTable.setFilterBarVisible(true);
        studentsTable.setFooterVisible(true);
        studentsTable.setSelectable(true);
        try {
            DbStudent dbs = new DbStudent();
            dbs.connect();
            studentsTable.setContainerDataSource(dbs.execSQL_for_orders(myUI,
                    myUI.getUser().getSchool().getId(), myUI.getUser().getCurrent_year().getId(), this));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        studentsTable.setVisibleColumns((Object[]) STUDENTS_NATURAL_COL_ORDER);
        studentsTable.setColumnFooter(myUI.getMessage(SptMessages.EducationStatus),
                "total  " + studentsTable.size());
        tablesLay.addComponent(studentsTable);

        historyTable = new Table();
        historyTable.setStyleName(ValoTheme.TABLE_COMPACT);
        historyTable.setCaption(myUI.getMessage(SptMessages.OrdersHistory));
        historyTable.setSizeFull();
        historyTable.setSelectable(false);
        tablesLay.addComponent(historyTable);
        tablesLay.setExpandRatio(studentsTable, 2);
        tablesLay.setExpandRatio(historyTable, 1);

        this.setSplitPosition(24, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(tablesLay);

    }

    private void buildSettingsLayout() {

        settingsLay = new VerticalLayout();
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setWidth(Settings.PERCENTS100);

        DateField dateDF = new DateField(myUI.getMessage(SptMessages.Date));
        dateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        dateDF.setRequired(true);
        dateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        dateDF.setWidth(Settings.PERCENTS100);
        dateDF.setValue(new Date());
        dateDF.setDateFormat(Settings.datePattern);
        settingsLay.addComponent(dateDF);

        orderSelect = new ComboBox(myUI.getMessage(SptMessages.OrderType));
        orderSelect.setNullSelectionAllowed(false);
        orderSelect.setRequired(true);
        orderSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        orderSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        orderSelect.setWidth(Settings.PERCENTS100);
        orderSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        orderSelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(orderSelect);

        classSelect = new ComboBox(myUI.getMessage(SptMessages.ClassName));
        classSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        classSelect.setWidth(Settings.PERCENTS100);
        classSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        classSelect.setFilteringMode(FilteringMode.CONTAINS);
        classSelect.setRequired(true);
        classSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        try {
            DbClassName dbcn = new DbClassName();
            dbcn.connect();
            classSelect.setContainerDataSource(
                    dbcn.execClass_sel(myUI, myUI.getUser().getSchool().getId()));
            dbcn.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        settingsLay.addComponent(classSelect);

        reasonsMCB = new ComboBoxMultiselect(myUI.getMessage(SptMessages.Reasons));
        reasonsMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        reasonsMCB.setWidth(Settings.PERCENTS100);
        reasonsMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        reasonsMCB.setFilteringMode(FilteringMode.CONTAINS);
        reasonsMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        reasonsMCB.setShowSelectAllButton((filter, page) -> true);
        reasonsMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        settingsLay.addComponent(reasonsMCB);

        try {
            DbDefinition dbDef = new DbDefinition();
            DbLeavingReasons dblr = new DbLeavingReasons();
            dbDef.connect();
            dblr.connect();
            orderSelect.setContainerDataSource(
                    dbDef.exec_order_for_sel(myUI));
            reasonsMCB.setContainerDataSource(
                    dblr.exec_for_select(myUI, false));
            dbDef.close();
            dblr.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        saveBtn = new Button(myUI.getMessage(SptMessages.SaveButton));
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);
        settingsLay.addComponent(saveBtn);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == saveBtn) {
            try {
                int counter = 0;
                if (studentsTable.getValue() != null && !((Set<?>) studentsTable.getValue()).isEmpty()) {
                    if (Settings.validate(settingsLay)) {
                        DbStudentOrder dbso = new DbStudentOrder();
                        dbso.connect();
                        DbStudentContract dbsc = new DbStudentContract();
                        dbsc.connect();
                        Iterator<?> iter = ((Set<?>) studentsTable.getValue()).iterator();
                        StudentOrder so = new StudentOrder();
                        so.setEmployee_id(myUI.getUser().getId());
                        so.setOrder_id((Integer) orderSelect.getValue());
                        so.setTo_education_status_id((Integer) orderSelect.getContainerProperty(
                                orderSelect.getValue(), Settings.education_status_id).getValue());
                        so.setTo_class_id((Integer) classSelect.getValue());
                        so.setOrder_id((Integer) orderSelect.getValue());
                        while (iter.hasNext()) {
                            int contr_status = 0;
                            Object next = iter.next();
                            if ((Integer) studentsTable.getContainerProperty(
                                    next, Settings.education_status_id).getValue() != 4
                                    && (Integer) studentsTable.getContainerProperty(
                                    next, Settings.education_status_id).getValue() != 5) {
                                so.setStudent_id((Integer) next);
                                so.setYear_id(myUI.getUser().getCurrent_year().getId());
                                if (so.getTo_education_status_id() == 3) {
                                    if (dbsc.execSQL_get_st_contract(so.getStudent_id(), so.getYear_id()) != 0) {
                                        contr_status = 1;
                                        so.setTo_education_status_id(2);
                                    }
                                }
                                so.setFrom_class_id((Integer) studentsTable.getContainerProperty(
                                        next, Settings.class_id).getValue());
                                so.setFrom_education_status_id((Integer) studentsTable.getContainerProperty(
                                        next, Settings.education_status_id).getValue());
                                so.setReasons(getMultiComboCaptions((Set<?>) reasonsMCB.getValue()));
                                int st = 0;
                                try {
                                    st = dbso.exec_insert(so);
                                } catch (Exception ignored) {
                                }
                                if (so.getOrder_id() == 3) {
                                    if (st == 0) {
                                        int o_id = dbso.exec_order_id(so);
                                        st = dbso.exec_update_existed_to_class(so, o_id);
                                        dbso.exec_update_from_class(so,
                                                (Integer) studentsTable.getContainerProperty(
                                                        next, Settings.class_id).getValue(), o_id);
                                        dbso.exec_update_to_class(so,
                                                (Integer) studentsTable.getContainerProperty(
                                                        next, Settings.class_id).getValue(), o_id);
                                    }
                                    dbso.exec_update_from_class(so,
                                            (Integer) studentsTable.getContainerProperty(
                                                    next, Settings.class_id).getValue());
                                    dbso.exec_update_to_class(so,
                                            (Integer) studentsTable.getContainerProperty(
                                                    next, Settings.class_id).getValue());
                                } else {
                                    dbso.exec_update_future_orders(so.getYear_id(), 0, so.getStudent_id());
                                }
                                if (st != 0) {
                                    studentsTable.getContainerProperty(next, Settings.class_id)
                                            .setValue(so.getTo_class_id());
                                    studentsTable.getContainerProperty(next, Settings.education_status_id)
                                            .setValue(so.getTo_education_status_id());
                                    Object cl_filled = studentsTable.getFilterFieldValue(myUI.getMessage(SptMessages.ClassName));
                                    Object edu_filled = studentsTable.getFilterFieldValue(myUI.getMessage(SptMessages.EducationStatus));
                                    if (cl_filled != null && !cl_filled.equals("")) {
                                        studentsTable.setFilterFieldValue(myUI.getMessage(SptMessages.ClassName), null);
                                        studentsTable.getContainerProperty(next,
                                                        myUI.getMessage(SptMessages.ClassName))
                                                .setValue(classSelect.getItemCaption(classSelect.getValue()));
                                        if (contr_status == 1) {
                                            studentsTable.getContainerDataSource().getContainerProperty(next,
                                                            myUI.getMessage(SptMessages.EducationStatus))
                                                    .setValue(Settings.activeStatus);
                                        } else {
                                            studentsTable.getContainerDataSource().getContainerProperty(next,
                                                            myUI.getMessage(SptMessages.EducationStatus))
                                                    .setValue(orderSelect.getContainerProperty(orderSelect.getValue(),
                                                            myUI.getMessage(SptMessages.EducationStatus)).getValue());
                                        }
                                        studentsTable.setFilterFieldValue(myUI.getMessage(SptMessages.ClassName), cl_filled);
                                    } else if (edu_filled != null && !edu_filled.equals("")) {
                                        studentsTable.setFilterFieldValue(myUI.getMessage(SptMessages.EducationStatus), null);
                                        if (contr_status == 1) {
                                            studentsTable.getContainerDataSource().getContainerProperty(next,
                                                            myUI.getMessage(SptMessages.EducationStatus))
                                                    .setValue(Settings.activeStatus);
                                        } else {
                                            studentsTable.getContainerDataSource().getContainerProperty(next,
                                                            myUI.getMessage(SptMessages.EducationStatus))
                                                    .setValue(orderSelect.getContainerProperty(orderSelect.getValue(),
                                                            myUI.getMessage(SptMessages.EducationStatus)).getValue());
                                        }
                                        studentsTable.getContainerProperty(next,
                                                        myUI.getMessage(SptMessages.ClassName))
                                                .setValue(classSelect.getItemCaption(classSelect.getValue()));
                                        studentsTable.setFilterFieldValue(myUI.getMessage(SptMessages.EducationStatus), edu_filled);
                                    } else {
                                        studentsTable.getContainerProperty(next,
                                                        myUI.getMessage(SptMessages.ClassName))
                                                .setValue(classSelect.getItemCaption(classSelect.getValue()));
                                        if (contr_status == 1) {
                                            studentsTable.getContainerDataSource().getContainerProperty(next,
                                                            myUI.getMessage(SptMessages.EducationStatus))
                                                    .setValue(Settings.activeStatus);
                                        } else {
                                            studentsTable.getContainerDataSource().getContainerProperty(next,
                                                            myUI.getMessage(SptMessages.EducationStatus))
                                                    .setValue(orderSelect.getContainerProperty(orderSelect.getValue(),
                                                            myUI.getMessage(SptMessages.EducationStatus)).getValue());
                                        }
                                    }
                                    dbsc.exec_update_status(so.getStudent_id(), 1, myUI.getUser().getId());
                                    counter++;
                                }
                            }
                        }
                        if (counter != 0) {
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved) + " " + counter,
                                    Notification.Type.WARNING_MESSAGE);
                            historyTable.setContainerDataSource(null);
                        }
                        dbso.close();
                        dbsc.close();
                    } else {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NotificationNothingIsSelected),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source.getDescription()
                .equals(myUI.getMessage(SptMessages.Details))) {
            try {
                selected_student_id = Integer.parseInt(source.getData().toString());
                DbStudentOrder dbso = new DbStudentOrder();
                dbso.connect();
                historyTable.setContainerDataSource(dbso.execSQL(myUI, selected_student_id, this));
                dbso.close();
                historyTable.setVisibleColumns((Object[]) HISTORY_NATURAL_COL_ORDER);
                historyTable.setCaption(myUI.getMessage(SptMessages.OrdersHistory) + " - "
                        + studentsTable.getContainerProperty(selected_student_id,
                        myUI.getMessage(SptMessages.FirstName)).getValue() + " "
                        + studentsTable.getContainerProperty(selected_student_id,
                        myUI.getMessage(SptMessages.LastName)).getValue());
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source.getDescription()
                .equals(myUI.getMessage(SptMessages.DeleteButton))) {
            ConfirmDialog.show(myUI, myUI.getMessage(SptMessages.Question),
                    myUI.getMessage(SptMessages.ConfirmDeletion),
                    myUI.getMessage(SptMessages.Yes),
                    myUI.getMessage(SptMessages.No),
                    (ConfirmDialog.Listener) dialog -> {
                        if (dialog.isConfirmed()) {
                            execDelete(Integer.parseInt(source.getData().toString()));
                        }
                    });

        }
    }

    private void execDelete(int id) {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete(id, Settings.dbStudentOrders);
            if (st != 0) {
                DbStudentOrder dbso = new DbStudentOrder();
                dbso.connect();
                StudentOrder so = new StudentOrder();
                so.setStudent_id((Integer) historyTable.getContainerProperty(id,
                        Settings.student_id).getValue());
                so.setOrder_id((Integer) historyTable.getContainerProperty(id,
                        Settings.order_id).getValue());
                so.setTo_class_id((Integer) historyTable.getContainerProperty(id,
                        Settings.from_class_id).getValue());
                so.setYear_id(myUI.getUser().getCurrent_year().getId());
                if (so.getOrder_id() == 3) {
                    dbso.exec_update_from_class(so,
                            (Integer) historyTable.getContainerProperty(id,
                                    Settings.to_class_id).getValue(), id);
                    dbso.exec_update_to_class(so,
                            (Integer) historyTable.getContainerProperty(id,
                                    Settings.to_class_id).getValue(), id);

                    dbso.exec_update_from_class(so,
                            (Integer) historyTable.getContainerProperty(id,
                                    Settings.to_class_id).getValue());
                    dbso.exec_update_to_class(so,
                            (Integer) historyTable.getContainerProperty(id,
                                    Settings.to_class_id).getValue());
                }

                dbso.exec_update_future_orders(myUI.getUser().getCurrent_year().getId(),
                        1, selected_student_id);
                dbso.close();
                DbStudent dbs = new DbStudent();
                dbs.connect();
                DbStudentContract dbsc = new DbStudentContract();
                dbsc.connect();
                dbsc.exec_update_status_by_id(selected_student_id, 2, myUI.getUser().getId());
                dbsc.close();
                studentsTable.getContainerProperty(selected_student_id, Settings.class_id)
                        .setValue(historyTable.getContainerProperty(
                                id, Settings.from_class_id).getValue());
                studentsTable.getContainerProperty(selected_student_id, Settings.education_status_id)
                        .setValue(historyTable.getContainerProperty(
                                id, Settings.from_education_status_id).getValue());
                Object cl_filled = studentsTable.getFilterFieldValue(myUI.getMessage(SptMessages.ClassName));
                Object edu_filled = studentsTable.getFilterFieldValue(myUI.getMessage(SptMessages.EducationStatus));
                if (cl_filled != null && !cl_filled.equals("")) {
                    studentsTable.setFilterFieldValue(myUI.getMessage(SptMessages.ClassName), null);
                    studentsTable.getContainerProperty(selected_student_id,
                                    myUI.getMessage(SptMessages.ClassName))
                            .setValue(historyTable.getContainerProperty(
                                    id, myUI.getMessage(SptMessages.FromClass)).getValue().toString());
                    studentsTable.getContainerProperty(selected_student_id,
                                    myUI.getMessage(SptMessages.EducationStatus))
                            .setValue(historyTable.getContainerProperty(
                                    id, myUI.getMessage(SptMessages.FromEducationStatus)).getValue().toString());
                    studentsTable.setFilterFieldValue(myUI.getMessage(SptMessages.ClassName), cl_filled);
                } else if (edu_filled != null && !edu_filled.equals("")) {
                    studentsTable.setFilterFieldValue(myUI.getMessage(SptMessages.EducationStatus), null);
                    studentsTable.getContainerProperty(selected_student_id,
                                    myUI.getMessage(SptMessages.ClassName))
                            .setValue(historyTable.getContainerProperty(
                                    id, myUI.getMessage(SptMessages.FromClass)).getValue().toString());
                    studentsTable.getContainerProperty(selected_student_id,
                                    myUI.getMessage(SptMessages.EducationStatus))
                            .setValue(historyTable.getContainerProperty(
                                    id, myUI.getMessage(SptMessages.FromEducationStatus)).getValue().toString());
                    studentsTable.setFilterFieldValue(myUI.getMessage(SptMessages.EducationStatus), edu_filled);
                } else {
                    studentsTable.getContainerProperty(selected_student_id,
                                    myUI.getMessage(SptMessages.ClassName))
                            .setValue(historyTable.getContainerProperty(
                                    id, myUI.getMessage(SptMessages.FromClass)).getValue().toString());
                    studentsTable.getContainerProperty(selected_student_id,
                                    myUI.getMessage(SptMessages.EducationStatus))
                            .setValue(historyTable.getContainerProperty(
                                    id, myUI.getMessage(SptMessages.FromEducationStatus)).getValue().toString());
                }

                historyTable.getContainerDataSource().removeItem(id);
                if (historyTable.getContainerDataSource().size() != 0) {
                    historyTable.getContainerProperty(((IndexedContainer) historyTable.getContainerDataSource()).firstItemId(), Settings.button)
                            .setValue(createButton(myUI.getMessage(SptMessages.DeleteButton),
                                    ((IndexedContainer) historyTable.getContainerDataSource())
                                            .firstItemId().toString(), FontAwesome.MINUS));
                }
            }
            dbDef.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            Notification.show(myUI.getMessage(SptMessages.CanNotDelete),
                    Notification.Type.WARNING_MESSAGE);
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    public Component getNewObj() {
        return new IssueOrderView(myUI);
    }

    public Button createButton(String description, String itemId, FontAwesome icon) {
        Button btn = new Button();
        btn.setDescription(description);
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.addStyleName(ValoTheme.BUTTON_TINY);
        btn.setIcon(icon);
        btn.setData(itemId);
        btn.addClickListener(this);
        return btn;
    }

    private String getMultiComboCaptions(Set<?> set) {
        if (!set.isEmpty()) {
            Iterator<?> iter = set.iterator();
            boolean isFirst = true;
            StringBuilder reasons = new StringBuilder();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (!isFirst) {
                    reasons.append(", ");
                }
                reasons.append(reasonsMCB.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Title)).getValue());
                isFirst = false;
            }
            return reasons.toString();
        } else {
            return null;
        }
    }
}
