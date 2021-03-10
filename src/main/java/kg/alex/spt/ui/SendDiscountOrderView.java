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
import kg.alex.spt.dao.DbClassName;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.domain.ClassName;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Iterator;

public class SendDiscountOrderView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(SendDiscountOrderView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBoxMax classNumberSelect, statusSelect;
    private Table dataTable;
    private TextField nameTF;
    private boolean isNew;

    private String[] NATURAL_COL_ORDER;
    private VerticalLayout settingsLay;
    private Subject currentUser = SecurityUtils.getSubject();

    public SendDiscountOrderView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Number),
            myUI.getMessage(SptMessages.Name), myUI.getMessage(SptMessages.School),
            myUI.getMessage(SptMessages.Status)};
        buildSettingsLayout();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new Table();
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        dataTable.setSelectable(true);
        dataTable.addValueChangeListener(this);
        try {
            DbClassName dbDef = new DbClassName();
            dbDef.connect();
            dataTable.setContainerDataSource(dbDef.execSQL(myUI, myUI.getUser().getSchool_id()));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns(NATURAL_COL_ORDER);
        if (dataTable.getContainerDataSource().size() != 0) {
            dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
        }
        dataTable.setNullSelectionAllowed(false);
        vl.addComponent(dataTable);

        this.setSplitPosition(24, Unit.PERCENTAGE);
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
        settingsLay.setWidth("100%");

        HorizontalLayout buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);

        modifyBtn = new Button();
        modifyBtn.setEnabled(false);
        modifyBtn.setDescription(myUI.getMessage(SptMessages.ModifyButton));
        modifyBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        modifyBtn.setIcon(FontAwesome.PENCIL);
        modifyBtn.addClickListener(this);
        buttonsLay.addComponent(modifyBtn);

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
        settingsLay.addComponent(buttonsLay);

        nameTF = new TextField(myUI.getMessage(SptMessages.Name));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        nameTF.setWidth("100%");
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 15, false));
        settingsLay.addComponent(nameTF);

        classNumberSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Number));
        classNumberSelect.setNullSelectionAllowed(false);
        classNumberSelect.setRequired(true);
        classNumberSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        classNumberSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        classNumberSelect.setWidth("100%");
        classNumberSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        classNumberSelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(classNumberSelect);

        statusSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Status));
        statusSelect.setNullSelectionAllowed(false);
        statusSelect.setRequired(true);
        statusSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        statusSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        statusSelect.setWidth("100%");
        statusSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        statusSelect.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            statusSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, SystemSettings.dbActivity_status));
            classNumberSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, SystemSettings.classTable));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        settingsLay.addComponent(statusSelect);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == modifyBtn && dataTable.getValue() != null) {
            isNew = false;
            fillFields();
            prepareModificationMode();
            nameTF.focus();
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
            nameTF.focus();
            statusSelect.setValue(2);
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
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == dataTable) {
            if (dataTable.getItem(dataTable.getValue()) != null) {
                fillFields();
            }
        }
    }

    private void prepareModificationMode() {
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        dataTable.setEnabled(false);
        nameTF.setEnabled(true);
        classNumberSelect.setEnabled(true);
        statusSelect.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(SystemSettings.cnClassNameDefinitionView + ":" + SystemSettings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(SystemSettings.cnClassNameDefinitionView + ":" + SystemSettings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(SystemSettings.cnClassNameDefinitionView + ":" + SystemSettings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        classNumberSelect.setEnabled(false);
        statusSelect.setEnabled(false);
    }

    private void fillFields() {
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Name)).getValue().toString());
        classNumberSelect.setValue(
                (Integer) dataTable.getContainerProperty(dataTable.getValue(),
                        SystemSettings.number_id).getValue());
        statusSelect.setValue((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                SystemSettings.status_id).getValue());
    }

    private void clearFields() {
        nameTF.setValue("");
        classNumberSelect.setValue(null);
        statusSelect.setValue(null);
    }

    private void updateDatacontainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Name)).setValue(nameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                SystemSettings.number_id).setValue(classNumberSelect
                        .getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Number)).setValue(classNumberSelect
                .getContainerProperty(classNumberSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                SystemSettings.status_id).setValue(statusSelect
                        .getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Status)).setValue(statusSelect.
                getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());
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
        item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(
                nameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Number)).setValue(
                classNumberSelect.getContainerProperty(classNumberSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue());
        item.getItemProperty(SystemSettings.number_id).setValue(
                classNumberSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());
        item.getItemProperty(SystemSettings.status_id).setValue(
                statusSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.School)).setValue(
                myUI.getUser().getSchool_name());
        item.getItemProperty(SystemSettings.school_id).setValue(
                myUI.getUser().getSchool_id());
        item.getItemProperty(SystemSettings.id).setValue(id);
        dataTable.setValue(id);
    }

    private ClassName getClassName(int i) {
        ClassName d = new ClassName();
        d.setName(nameTF.getValue());
        d.setClass_number_id((Integer) classNumberSelect.getValue());
        d.setStatus_id((Integer) statusSelect.getValue());
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

    public Component getNewObj() {
        return new SendDiscountOrderView(myUI);
    }
}
