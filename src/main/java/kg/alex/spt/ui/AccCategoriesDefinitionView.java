package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.domain.AccCategory;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Iterator;

public class AccCategoriesDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(AccCategoriesDefinitionView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBoxMax parentSelect, statusSelect;
    private TreeTable dataTable;
    private TextField nameTF, codeTF;
    private TextArea noteTF;
    private boolean isNew;

    private String[] NATURAL_COL_ORDER;
    private String parent_code = "";
    private Label parent_code_label;
    private VerticalLayout settingsLay;
    private GridLayout codeLay;
    private Subject currentUser = SecurityUtils.getSubject();
    private int movement_type_id;

    public AccCategoriesDefinitionView(MyVaadinUI myUI, int movement_type_id) {
        this.myUI = myUI;
        this.movement_type_id = movement_type_id;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Code),
                myUI.getMessage(SptMessages.Title), myUI.getMessage(SptMessages.Parent),
                myUI.getMessage(SptMessages.Status), myUI.getMessage(SptMessages.Note)};
        buildSettingsLayout();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new TreeTable();
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        dataTable.setSelectable(true);
        dataTable.addValueChangeListener(this);
        dataTable.setNullSelectionAllowed(false);
        setTableOptions();
        vl.addComponent(dataTable);

        this.setSplitPosition(24, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(vl);

        prepareNormalMode();
    }

    private void setTableOptions() {
        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            dbac.execSQL(myUI, movement_type_id, dataTable);
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns(NATURAL_COL_ORDER);
        if (dataTable.getContainerDataSource().size() != 0) {
            dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
        }
    }

    private void buildSettingsLayout() {

        settingsLay = new VerticalLayout();
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setWidth(Settings.PERCENTS100);
        codeLay = new GridLayout(2, 1);
        codeLay.setMargin(false);
        codeLay.setSpacing(false);
        codeLay.setWidth(Settings.PERCENTS100);

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

        parentSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Parent));
        parentSelect.setNullSelectionAllowed(true);
        parentSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        parentSelect.setWidth(Settings.PERCENTS100);
        parentSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        parentSelect.setFilteringMode(FilteringMode.CONTAINS);
        setParentCombo();
        parentSelect.addValueChangeListener(this);
        settingsLay.addComponent(parentSelect);

        nameTF = new TextField(myUI.getMessage(SptMessages.Title));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        nameTF.setWidth(Settings.PERCENTS100);
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 350, false));
        settingsLay.addComponent(nameTF);

        parent_code_label = new Label();
        parent_code_label.setImmediate(true);
        parent_code_label.setSizeUndefined();
        codeLay.addComponent(parent_code_label, 0, 0);
        codeLay.setComponentAlignment(parent_code_label, Alignment.BOTTOM_LEFT);

        codeTF = new TextField(myUI.getMessage(SptMessages.Code));
        codeTF.setRequired(true);
        codeTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        codeTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        codeTF.setWidth(Settings.PERCENTS100);
        codeTF.addValidator(new RegexpValidator("[0-9]{1,20}", true,
                myUI.getMessage(SptMessages.NotifWrongValue)));
        codeLay.addComponent(codeTF, 1, 0);
        codeLay.setColumnExpandRatio(1, 1);
        settingsLay.addComponent(codeLay);

        noteTF = new TextArea(myUI.getMessage(SptMessages.Note));
        noteTF.setWidth(Settings.PERCENTS100);
        noteTF.setImmediate(true);
        noteTF.setRows(3);
        noteTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        noteTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 0, 200, false));
        settingsLay.addComponent(noteTF);

        statusSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Status));
        statusSelect.setNullSelectionAllowed(false);
        statusSelect.setRequired(true);
        statusSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        statusSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        statusSelect.setWidth(Settings.PERCENTS100);
        statusSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        statusSelect.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            statusSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbActivity_status, true));
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
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
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
                    DbAccCategory dbac = new DbAccCategory();
                    dbac.connect();
                    if (isNew) {
                        int id = dbac.exec_insert(getAccCategory(0));
                        if (id != 0) {
                            setTableOptions();
                            setParentCombo();
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                            prepareNormalMode();
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        int status = 0;
                        AccCategory acc_category = getAccCategory((Integer) dataTable.getValue());
                        try {
                            status = dbac.exec_update(acc_category);
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (status != 0) {
                            dbac.exec_update_all_parent_codes(acc_category.getId(), acc_category.getCode(), true);
                            setTableOptions();
                            setParentCombo();
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                            prepareNormalMode();
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    }
                    dbac.close();
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
        } else if (property == parentSelect) {
            if (parentSelect.getValue() != null) {
                parent_code = parentSelect.getContainerProperty(parentSelect.getValue(),
                        myUI.getMessage(SptMessages.Code)).getValue().toString();
                parent_code_label.setValue(parent_code + ".");

            } else {
                parent_code = "";
                parent_code_label.setValue(parent_code);
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
        noteTF.setEnabled(true);
        codeTF.setEnabled(true);
        parentSelect.setEnabled(true);
        statusSelect.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnIncomesDefinitionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnIncomesDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnIncomesDefinitionView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        noteTF.setEnabled(false);
        codeTF.setEnabled(false);
        parentSelect.setEnabled(false);
        statusSelect.setEnabled(false);
    }

    private void fillFields() {
        if ((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                Settings.parent_id).getValue() != 0) {
            parentSelect.setValue((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.parent_id).getValue());
        } else {
            parentSelect.setValue(null);
        }
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Title)).getValue().toString());
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Note)).getValue() != null) {
            noteTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.Note)).getValue().toString());
        } else {
            noteTF.setValue("");
        }
        if (parentSelect.getValue() != null) {
            parent_code = parentSelect.getContainerProperty(parentSelect.getValue(),
                    myUI.getMessage(SptMessages.Code)).getValue().toString();
            parent_code_label.setValue(parent_code + ".");
        } else {
            parent_code = "";
            parent_code_label.setValue("");
        }
        codeTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Code)).getValue().toString().replace((parent_code + "."), ""));
        statusSelect.setValue((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).getValue());
    }

    private void clearFields() {
        nameTF.setValue("");
        noteTF.setValue("");
        codeTF.setValue("");
        parent_code = "";
        parent_code_label.setValue(parent_code);
        parentSelect.setValue(null);
        statusSelect.setValue(null);
    }

    private AccCategory getAccCategory(int id) {
        AccCategory ac = new AccCategory();
        ac.setName(nameTF.getValue());
        ac.setNote(noteTF.getValue());
        ac.setCode(codeTF.getValue());
        if (parentSelect.getValue() != null) {
            ac.setParent_id((Integer) parentSelect.getValue());
            ac.setParent_code(parent_code);
        }
        ac.setStatus_id((Integer) statusSelect.getValue());
        ac.setType_id(movement_type_id);
        ac.setModified_employee_id(myUI.getUser().getId());
        ac.setId(id);
        return ac;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete((Integer) dataTable.getValue(), Settings.dbAcc_category);
            if (st != 0) {
                setParentCombo();
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
        return new AccCategoriesDefinitionView(myUI, movement_type_id);
    }

    private void setParentCombo() {
        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            parentSelect.setContainerDataSource(
                    dbac.exec_for_select(myUI, movement_type_id));
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
