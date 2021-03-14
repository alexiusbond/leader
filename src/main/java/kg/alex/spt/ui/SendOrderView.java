package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.*;
import kg.alex.spt.domain.ClassName;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Iterator;

public class SendOrderView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(SendOrderView.class);
    private MyVaadinUI myUI;
    private Button createBtn, deleteBtn, saveBtn, cancelBtn, pdfBtn;
    private ComboBoxMax schoolSelect, studentSelect;
    private ComboBoxMultiselectMax employeeMCB;
    private FilterTable dataTable;
    private TextField numberAndDateTF, headlineTF;
    private RichTextArea contentRTA;
    private TextArea messageTA;
    private boolean isNew;

    private String[] NATURAL_COL_ORDER;
    private VerticalLayout settingsLay;
    private Subject currentUser = SecurityUtils.getSubject();

    public SendOrderView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.Employee), myUI.getMessage(SptMessages.NumberAndDate),
                myUI.getMessage(SptMessages.Student), myUI.getMessage(SptMessages.Status)};
        buildSettingsLayout();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);

        dataTable = new FilterTable();
        dataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        dataTable.setStyleName(ValoTheme.TABLE_SMALL);
        dataTable.setSizeFull();
        dataTable.setNullSelectionAllowed(false);
        dataTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        dataTable.setFilterBarVisible(true);
        dataTable.setFooterVisible(true);
        dataTable.setSelectable(true);
        dataTable.addValueChangeListener(this);
        try {
            DbOrderMessage dbCon = new DbOrderMessage();
            dbCon.connect();
            dbCon.execSQL(myUI, 0, dataTable);
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns(NATURAL_COL_ORDER);
        vl.addComponent(dataTable);

        this.setSplitPosition(45, Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(vl);

        prepareNormalMode();
    }

    private void buildSettingsLayout() {

        settingsLay = new VerticalLayout();
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setSizeFull();

        HorizontalLayout buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);

        createBtn = new Button();
        createBtn.setEnabled(false);
        createBtn.setDescription(myUI.getMessage(SptMessages.CreateButton));
        createBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        createBtn.setIcon(FontAwesome.FILE_O);
        createBtn.addClickListener(this);
        buttonsLay.addComponent(createBtn);

        deleteBtn = new Button();
        deleteBtn.setEnabled(false);
        deleteBtn.setDescription(myUI.getMessage(SptMessages.DeleteButton));
        deleteBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.setIcon(FontAwesome.TRASH_O);
        deleteBtn.addClickListener(this);
        buttonsLay.addComponent(deleteBtn);

        saveBtn = new Button();
        saveBtn.setDescription(myUI.getMessage(SptMessages.SaveButton));
        saveBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);
        buttonsLay.addComponent(saveBtn);

        cancelBtn = new Button();
        cancelBtn.setDescription(myUI.getMessage(SptMessages.CancelButton));
        cancelBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        cancelBtn.setIcon(FontAwesome.BAN);
        cancelBtn.addClickListener(this);
        buttonsLay.addComponent(cancelBtn);

        pdfBtn = new Button();
        pdfBtn.setDescription(myUI.getMessage(SptMessages.ExportToPdf));
        pdfBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        pdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        pdfBtn.addClickListener(this);
        buttonsLay.addComponent(pdfBtn);
        settingsLay.addComponent(buttonsLay);

        schoolSelect = new ComboBoxMax(myUI.getMessage(SptMessages.School));
        schoolSelect.setNullSelectionAllowed(false);
        schoolSelect.setRequired(true);
        schoolSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        schoolSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        schoolSelect.setWidth("100%");
        schoolSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        schoolSelect.setFilteringMode(FilteringMode.CONTAINS);
        schoolSelect.addValueChangeListener(this);
        schoolSelect.setContainerDataSource(myUI.getSchoolCont());
        settingsLay.addComponent(schoolSelect);

        employeeMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Employee));
        employeeMCB.setRequired(true);
        employeeMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        employeeMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        employeeMCB.setWidth("100%");
        employeeMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        employeeMCB.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(employeeMCB);

        studentSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Student));
        studentSelect.setNullSelectionAllowed(false);
        studentSelect.setRequired(true);
        studentSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        studentSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        studentSelect.setWidth("100%");
        studentSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        studentSelect.setFilteringMode(FilteringMode.CONTAINS);
        studentSelect.addValueChangeListener(this);
        settingsLay.addComponent(studentSelect);

        numberAndDateTF = new TextField(myUI.getMessage(SptMessages.NumberAndDate));
        numberAndDateTF.setRequired(true);
        numberAndDateTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        numberAndDateTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        numberAndDateTF.setWidth("100%");
        numberAndDateTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 25, false));
        settingsLay.addComponent(numberAndDateTF);

        headlineTF = new TextField(myUI.getMessage(SptMessages.Headline));
        headlineTF.setRequired(true);
        headlineTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        headlineTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        headlineTF.setWidth("100%");
        headlineTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 300, false));
        settingsLay.addComponent(headlineTF);

        contentRTA = new RichTextArea(myUI.getMessage(SptMessages.Content));
        contentRTA.setRequired(true);
        contentRTA.setStyleName(ValoTheme.TEXTAREA_SMALL);
        contentRTA.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        contentRTA.setSizeFull();
        settingsLay.addComponent(contentRTA);
        settingsLay.setExpandRatio(contentRTA, 1);

        messageTA = new TextArea(myUI.getMessage(SptMessages.Message));
        messageTA.setRows(2);
        messageTA.setRequired(true);
        messageTA.setStyleName(ValoTheme.TEXTAREA_SMALL);
        messageTA.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        messageTA.setWidth("100%");
        settingsLay.addComponent(messageTA);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
        } else if (source == deleteBtn && dataTable.getValue() != null) {
            ConfirmDialog.show(myUI, myUI.getMessage(SptMessages.Question),
                    myUI.getMessage(SptMessages.ConfirmDeletion),
                    myUI.getMessage(SptMessages.Yes),
                    myUI.getMessage(SptMessages.No),
                    new ConfirmDialog.Listener() {
                        @Override
                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                execDelete();
                            }
                        }
                    });
        } else if (source == saveBtn) {
            try {
                if (validate(settingsLay)) {
                    DbClassName dbcn = new DbClassName();
                    dbcn.connect();
                    if (isNew) {
                        int id = dbcn.exec_insert(getClassName(0));
                        if (id != 0) {
                            addDatacontainerItem(id);
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        int status = 0;
                        try {
                            status = dbcn.exec_update(
                                    getClassName((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                            SystemSettings.id).getValue()));
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (status != 0) {
                            updateDatacontainer();
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    }
                    dbcn.close();
                    prepareNormalMode();
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == cancelBtn) {
            if (dataTable.getValue() != null) {
                fillFields();
            }
            prepareNormalMode();
        } else if (source == pdfBtn) {
            if (dataTable.getValue() != null) {

            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == dataTable) {
            if (dataTable.getItem(dataTable.getValue()) != null) {
                fillFields();
            }
        } else if (property == schoolSelect && schoolSelect.getValue() != null) {
            try {
                headlineTF.setValue(schoolSelect.getContainerProperty(schoolSelect.getValue(),
                        myUI.getMessage(SptMessages.TitleKg)).getValue().toString().toUpperCase()
                        + "НИН ДИРЕКТОРУНА");
                DbStudent dbSt = new DbStudent();
                dbSt.connect();
                studentSelect.setContainerDataSource(dbSt.exec_for_select(
                        myUI, (Integer) schoolSelect.getValue(), "1,2,3"));
                dbSt.close();
                DbEmployee dbe = new DbEmployee();
                dbe.connect();
                employeeMCB.setContainerDataSource(dbe.exec_for_select(myUI, (Integer) schoolSelect.getValue(),
                        myUI.getUser().getId(), false, false));
                dbe.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (property == studentSelect && studentSelect.getValue() != null) {
            contentRTA.setValue("<font face='Times New Roman, serif'>" +
                    "<span style='font-size: 14px;'>Лицейдин "
                    + studentSelect.getContainerProperty(
                    studentSelect.getValue(), myUI.getMessage(SptMessages.ClassNumber)).getValue()
                    + "-классынын окуучусу <b>" + studentSelect.getContainerProperty(
                    studentSelect.getValue(), myUI.getMessage(SptMessages.FullName)).getValue()
                    + "га</b> “Сапаттын” акылуу билим берүү кызмат көрсөтүүдөгү жеңилдиктер жөнүндөгү " +
                    "Жобосунун 3-пунктунун  негизинде <b>"
                    + myUI.getUser().getCurrent_year().getName()
                    + "-окуу жылынын окуу төлөмүндө __% жеңилдик берилсин.</b></span></font>");
        }
    }

    private void prepareModificationMode() {
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        dataTable.setEnabled(false);
        headlineTF.setEnabled(true);
        numberAndDateTF.setEnabled(true);
        contentRTA.setEnabled(true);
        messageTA.setEnabled(true);
        schoolSelect.setEnabled(true);
        studentSelect.setEnabled(true);
        employeeMCB.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(SystemSettings.cnSendOrderView + ":" + SystemSettings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(SystemSettings.cnSendOrderView + ":" + SystemSettings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        headlineTF.setEnabled(false);
        numberAndDateTF.setEnabled(false);
        contentRTA.setEnabled(false);
        messageTA.setEnabled(false);
        schoolSelect.setEnabled(false);
        studentSelect.setEnabled(false);
        employeeMCB.setEnabled(false);
    }

    private void fillFields() {
        headlineTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Title)).getValue().toString());
        schoolSelect.setValue(
                (Integer) dataTable.getContainerProperty(dataTable.getValue(),
                        SystemSettings.number_id).getValue());
    }

    private void clearFields() {
        headlineTF.setValue("");
        numberAndDateTF.setValue("");
        contentRTA.setValue("");
        messageTA.setValue("");
        schoolSelect.setValue(null);
        studentSelect.setValue(null);
        employeeMCB.setValue(null);
    }

    private void updateDatacontainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Title)).setValue(headlineTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                SystemSettings.number_id).setValue(schoolSelect
                .getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Number)).setValue(schoolSelect
                .getContainerProperty(schoolSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                SystemSettings.school_id).setValue(
                myUI.getUser().getSchool_id());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.School)).setValue(
                myUI.getUser().getSchool_name());
    }

    private void addDatacontainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(
                headlineTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Number)).setValue(
                schoolSelect.getContainerProperty(schoolSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        item.getItemProperty(SystemSettings.number_id).setValue(
                schoolSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.School)).setValue(
                myUI.getUser().getSchool_name());
        item.getItemProperty(SystemSettings.school_id).setValue(
                myUI.getUser().getSchool_id());
        item.getItemProperty(SystemSettings.id).setValue(id);
        dataTable.setValue(id);
    }

    private ClassName getClassName(int i) {
        ClassName d = new ClassName();
        d.setName(headlineTF.getValue());
        d.setClass_number_id((Integer) schoolSelect.getValue());
        d.setSchool_id(myUI.getUser().getSchool_id());
        d.setId(i);
        return d;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    SystemSettings.id).getValue(), SystemSettings.dbClass_name);
            if (st != 0) {
                dataTable.getContainerDataSource().removeItem(dataTable.getValue());
                if (dataTable.getContainerDataSource().size() != 0) {
                    dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
                } else {
                    clearFields();
                    dataTable.setValue(null);
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

    private boolean validate(ComponentContainer layout) {
        boolean result = true;
        Iterator<Component> i = layout.iterator();
        while (i.hasNext()) {
            Component c = i.next();
            if (c instanceof AbstractField) {
                try {
                    ((AbstractField) c).validate();
                } catch (Exception e) {
                    //((AbstractComponent) c).setComponentError(new UserError(e.getMessage()));
                    result = false;
                }
            } else if (c instanceof AbstractComponentContainer) {
                if (!validate((AbstractComponentContainer) c)) {
                    result = false;
                }
            }
        }
        return result;
    }
}
