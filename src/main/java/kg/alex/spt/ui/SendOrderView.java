package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.*;
import kg.alex.spt.domain.EmployeeMessage;
import kg.alex.spt.domain.OrderMessage;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.pdf.OrderPdf;
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
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class SendOrderView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(SendOrderView.class);
    private MyVaadinUI myUI;
    private Button sendBtn;
    private ComboBoxMax schoolSelect, studentSelect;
    private ComboBoxMultiselectMax employeeMCB;
    private FilterTable dataTable;
    private TextField orderNumberTF, headlineTF;
    private DateField dateDF;
    private RichTextArea contentRTA;
    private TextArea messageTA;

    private String[] NATURAL_COL_ORDER;
    private GridLayout settingsLay;
    private Subject currentUser = SecurityUtils.getSubject();

    public SendOrderView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.Employee), myUI.getMessage(SptMessages.OrderNumber),
                myUI.getMessage(SptMessages.Student), myUI.getMessage(SptMessages.Title),
                myUI.getMessage(SptMessages.Message), myUI.getMessage(SptMessages.Status),
                SystemSettings.button};
        buildSettingsLayout();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);

        dataTable = new FilterTable();
        dataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        dataTable.setStyleName(ValoTheme.TABLE_SMALL);
        dataTable.addStyleName("noWrap");
        dataTable.setSizeFull();
        dataTable.setFilterBarVisible(true);
        dataTable.setFooterVisible(true);
        try {
            DbOrderMessage dbCon = new DbOrderMessage();
            dbCon.connect();
            if (currentUser.hasRole("admin")) {
                dbCon.execSQL(myUI, 0, dataTable, this);
            } else {
                dbCon.execSQL(myUI, myUI.getUser().getId(), dataTable, this);
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns(NATURAL_COL_ORDER);
        dataTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Message), 1);
        dataTable.setColumnWidth(myUI.getMessage(SptMessages.Date), 80);
        dataTable.setColumnWidth(SystemSettings.button, 60);
        vl.addComponent(dataTable);

        this.setSplitPosition(30, Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(vl);
    }

    private void buildSettingsLayout() {

        settingsLay = new GridLayout(2, 7);
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setSizeFull();

        sendBtn = new Button();
        sendBtn.setCaption(myUI.getMessage(SptMessages.Send));
        sendBtn.setIcon(FontAwesome.SHARE_SQUARE_O);
        sendBtn.addClickListener(this);
        settingsLay.addComponent(sendBtn, 0, 0, 1, 0);

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
        settingsLay.addComponent(schoolSelect, 0, 1, 1, 1);

        employeeMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.ToEmployees));
        employeeMCB.setRequired(true);
        employeeMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        employeeMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        employeeMCB.setWidth("100%");
        employeeMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        employeeMCB.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(employeeMCB, 0, 2);

        studentSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Student));
        studentSelect.setNullSelectionAllowed(false);
        studentSelect.setRequired(true);
        studentSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        studentSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        studentSelect.setWidth("100%");
        studentSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        studentSelect.setFilteringMode(FilteringMode.CONTAINS);
        studentSelect.addValueChangeListener(this);
        settingsLay.addComponent(studentSelect, 1, 2);

        dateDF = new DateField(myUI.getMessage(SptMessages.Date));
        dateDF.setResolution(Resolution.MINUTE);
        dateDF.setWidth("100%");
        dateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        dateDF.setRequired(true);
        dateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        dateDF.setDateFormat(SystemSettings.datePattern);
        dateDF.setValue(new Date());
        settingsLay.addComponent(dateDF, 0, 3);

        orderNumberTF = new TextField(myUI.getMessage(SptMessages.OrderNumber));
        orderNumberTF.setRequired(true);
        orderNumberTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        orderNumberTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        orderNumberTF.setWidth("100%");
        orderNumberTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 25, false));
        settingsLay.addComponent(orderNumberTF, 1, 3);

        headlineTF = new TextField(myUI.getMessage(SptMessages.Headline));
        headlineTF.setRequired(true);
        headlineTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        headlineTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        headlineTF.setWidth("100%");
        headlineTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 300, false));
        settingsLay.addComponent(headlineTF, 0, 4, 1, 4);

        contentRTA = new RichTextArea(myUI.getMessage(SptMessages.Content));
        contentRTA.setRequired(true);
        contentRTA.setStyleName(ValoTheme.TEXTAREA_SMALL);
        contentRTA.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        contentRTA.setSizeFull();
        settingsLay.addComponent(contentRTA, 0, 5, 1, 5);

        messageTA = new TextArea(myUI.getMessage(SptMessages.Message));
        messageTA.setRows(2);
        messageTA.setRequired(true);
        messageTA.setStyleName(ValoTheme.TEXTAREA_SMALL);
        messageTA.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        messageTA.setWidth("100%");
        settingsLay.addComponent(messageTA, 0, 6, 1, 6);
        settingsLay.setRowExpandRatio(5, 1);
        settingsLay.setColumnExpandRatio(0, 2);
        settingsLay.setColumnExpandRatio(1, 1);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == sendBtn) {
            try {
                if (validate(settingsLay)) {
                    DbOrderMessage dbcn = new DbOrderMessage();
                    dbcn.connect();
                    int id = dbcn.exec_insert(getOrderMessage(0));
                    if (id != 0) {
                        Iterator iter = ((Set<?>) employeeMCB.getValue()).iterator();
                        DbEmployeeMessage dbem = new DbEmployeeMessage();
                        dbem.connect();
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            EmployeeMessage employeeMessage = new EmployeeMessage();
                            employeeMessage.setEmployee_id((Integer) next);
                            employeeMessage.setOrder_message_id(id);
                            employeeMessage.setMessage_status_id(2);
                            int em_id = dbem.exec_insert(employeeMessage);
                            employeeMessage.setId(em_id);
                            employeeMessage.setEmployee(employeeMCB.getContainerProperty(
                                    next, myUI.getMessage(SptMessages.Title)).getValue().toString());
                            addDatacontainerItem(employeeMessage);
                        }
                        dbem.close();
                        Notification.show(myUI.getMessage(SptMessages.Sent),
                                Notification.Type.HUMANIZED_MESSAGE);
                    } else {
                        Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                Notification.Type.WARNING_MESSAGE);
                    }
                    dbcn.close();
                    clearFields();
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source.getId() != null && source.getId().equals(SystemSettings.actDelete)) {
            EmployeeMessage employeeMessage = (EmployeeMessage) source.getData();
            if ((Integer) dataTable.getContainerProperty(employeeMessage.getId(),
                    SystemSettings.status_id).getValue() == 2) {
                ConfirmDialog.show(myUI, myUI.getMessage(SptMessages.Question),
                        myUI.getMessage(SptMessages.ConfirmDeletion),
                        myUI.getMessage(SptMessages.Yes),
                        myUI.getMessage(SptMessages.No),
                        new ConfirmDialog.Listener() {
                            @Override
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    execDelete(employeeMessage);
                                }
                            }
                        });
            } else {
                Notification.show(myUI.getMessage(SptMessages.CanNotDelete),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else {
            new OrderPdf(myUI);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == schoolSelect && schoolSelect.getValue() != null) {
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

    private void clearFields() {
        headlineTF.setValue("");
        orderNumberTF.setValue("");
        contentRTA.setValue("");
        messageTA.setValue("");
        schoolSelect.setValue(null);
        dateDF.setValue(new Date());
        studentSelect.setValue(null);
        employeeMCB.setValue(null);
    }

    private void addDatacontainerItem(EmployeeMessage employeeMessage) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, employeeMessage.getId());
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                SystemSettings.df.format(dateDF.getValue()));
        item.getItemProperty(myUI.getMessage(SptMessages.Employee)).setValue(
                employeeMessage.getEmployee());
        item.getItemProperty(myUI.getMessage(SptMessages.Message)).setValue(messageTA.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(headlineTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Student)).setValue(
                studentSelect.getContainerProperty(studentSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        item.getItemProperty(SystemSettings.status_id).setValue(2);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.addComponent(createButton(myUI.getMessage(SptMessages.DeleteButton),
                SystemSettings.actDelete, FontAwesome.BAN, employeeMessage));
        hl.addComponent(createButton(myUI.getMessage(SptMessages.ExportToPdf),
                SystemSettings.actPdf, FontAwesome.FILE_PDF_O, employeeMessage));
        item.getItemProperty(SystemSettings.button).setValue(hl);
        item.getItemProperty(myUI.getMessage(SptMessages.Status)).setValue(
                myUI.getMessage(SptMessages.UnRead));

    }

    public Button createButton(String description, String button_id, Resource icon,
                               EmployeeMessage employeeMessage) {
        Button btn = new Button();
        btn.setDescription(description);
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.addStyleName(ValoTheme.BUTTON_TINY);
        btn.setIcon(icon);
        btn.setData(employeeMessage);
        btn.setId(button_id);
        btn.addClickListener(this);
        return btn;
    }

    private OrderMessage getOrderMessage(int i) {
        OrderMessage om = new OrderMessage();
        om.setMessage(messageTA.getValue());
        om.setContent(contentRTA.getValue());
        om.setStudent_id((Integer) studentSelect.getValue());
        om.setDate(dateDF.getValue());
        om.setOrder_number(orderNumberTF.getValue());
        om.setTitle(headlineTF.getValue());
        om.setEmployee_id(myUI.getUser().getId());
        om.setId(i);
        return om;
    }

    private void execDelete(EmployeeMessage employeeMessage) {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete(employeeMessage.getId(),
                    SystemSettings.dbEmployeeMessageTable);
            if (st != 0) {
                dataTable.getContainerDataSource().removeItem(employeeMessage.getId());
                try {
                    dbDef.exec_delete(employeeMessage.getOrder_message_id(),
                            SystemSettings.orderMessagesTable);
                } catch (Exception e) {
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
