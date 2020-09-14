package kg.alex.spt.ui;

import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbPosition;
import kg.alex.spt.domain.Position;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

public class PositionDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(PositionDefinitionView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBoxMax categorySelect, statusSelect;
    private Table dataTable, permissionTable;
    private IndexedContainer permissionCont;
    private TextField nameTF;
    private boolean isNew;
    private SystemSettings sysSettings = new SystemSettings();
    private String[] NATURAL_COL_ORDER, PERMISSION_NATURAL_COL_ORDER;
    private VerticalLayout settingsLay;
    private Subject currentUser = SecurityUtils.getSubject();

    public PositionDefinitionView(MyVaadinUI myUI) {
        this.myUI = myUI;

        PERMISSION_NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.ClassCaption),
            myUI.getMessage(SptMessages.Functions)};
        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Name),
            myUI.getMessage(SptMessages.Category),
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
            DbPosition dbp = new DbPosition();
            dbp.connect();
            dataTable.setContainerDataSource(dbp.execSQL(myUI));
            dbp.close();
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

        this.setSplitPosition(24, Sizeable.Unit.PERCENTAGE);
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
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 100, false));
        settingsLay.addComponent(nameTF);

        categorySelect = new ComboBoxMax(myUI.getMessage(SptMessages.Category));
        categorySelect.setNullSelectionAllowed(false);
        categorySelect.setRequired(true);
        categorySelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        categorySelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        categorySelect.setWidth("100%");
        categorySelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        categorySelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(categorySelect);

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
                    dbDef.exec_for_select(myUI, sysSettings.dbActivity_status));
            categorySelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, sysSettings.positionCategoryTable));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        settingsLay.addComponent(statusSelect);

        permissionTable = new Table();
        permissionTable.setCaption(myUI.getMessage(SptMessages.Permissions));
        permissionTable.setStyleName(ValoTheme.TABLE_SMALL);
        permissionTable.setSizeFull();
        permissionTable.setSelectable(false);
        settingsLay.addComponent(permissionTable);
        setPermContainer();

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
                    DbPosition dbp = new DbPosition();
                    dbp.connect();
                    if (isNew) {
                        int id = dbp.exec_insert(getPosition(0));
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
                            status = dbp.exec_update(
                                    getPosition((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                            sysSettings.id).getValue()));
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
                    dbp.close();
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
                if ((Integer) dataTable.getContainerProperty(dataTable.getValue(), sysSettings.position_id).getValue() == 5
                        || (Integer) dataTable.getContainerProperty(dataTable.getValue(), sysSettings.position_id).getValue() == 25) {
                    permissionTable.setVisible(false);
                } else {
                    permissionTable.setVisible(true);
                }
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
        categorySelect.setEnabled(true);
        statusSelect.setEnabled(true);
        permissionTable.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(sysSettings.cnHRDefinitionView + ":" + sysSettings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(sysSettings.cnHRDefinitionView + ":" + sysSettings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(sysSettings.cnHRDefinitionView + ":" + sysSettings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        categorySelect.setEnabled(false);
        statusSelect.setEnabled(false);
        permissionTable.setEnabled(false);
    }

    private void fillFields() {
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Name)).getValue().toString());
        categorySelect.setValue(
                (Integer) dataTable.getContainerProperty(dataTable.getValue(),
                        sysSettings.hr_position_category_id).getValue());
        statusSelect.setValue(
                (Integer) dataTable.getContainerProperty(dataTable.getValue(),
                        sysSettings.activity_status_id).getValue());
        clearPermissionTable();
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Permissions)).getValue() != null) {
            setPermTable_options(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.Permissions)).getValue().toString());
        }
    }

    private void clearFields() {
        nameTF.setValue("");
        categorySelect.setValue(null);
        statusSelect.setValue(null);
        clearPermissionTable();
    }

    private void updateDatacontainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Name)).setValue(nameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                sysSettings.hr_position_category_id).setValue(categorySelect
                        .getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Category)).setValue(categorySelect
                .getContainerProperty(categorySelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Permissions)).setValue(permJoinSingleStr());
        dataTable.getContainerProperty(dataTable.getValue(),
                sysSettings.activity_status_id).setValue(statusSelect
                        .getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Status)).setValue(statusSelect.
                getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());
    }

    private void addDatacontainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(
                nameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Category)).setValue(
                categorySelect.getContainerProperty(categorySelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Permissions)).setValue(permJoinSingleStr());
        item.getItemProperty(sysSettings.hr_position_category_id).setValue(
                categorySelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());
        item.getItemProperty(sysSettings.activity_status_id).setValue(
                statusSelect.getValue());
        item.getItemProperty(sysSettings.id).setValue(id);
        dataTable.setValue(id);
    }

    private Position getPosition(int i) {
        Position p = new Position();
        p.setName(nameTF.getValue());
        p.setPosition_category_id((Integer) categorySelect.getValue());
        p.setActivity_status_id((Integer) statusSelect.getValue());
        p.setPermissions(permJoinSingleStr());
        p.setId(i);
        return p;
    }

    private void execDelete() {

        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            int st = dbd.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    sysSettings.id).getValue(), sysSettings.hr_positionTable);
            if (st != 0) {
                dataTable.getContainerDataSource().removeItem(dataTable.getValue());
                if (dataTable.getContainerDataSource().size() != 0) {
                    dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
                } else {
                    clearFields();
                    dataTable.setValue(null);
                }
            }
            dbd.close();
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
        return new PositionDefinitionView(myUI);
    }

    private void setPermContainer() {
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            permissionCont = dbd.execPermissionSQL(myUI);
            dbd.close();
            Iterator iter = permissionCont.getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                ComboBoxMultiselectMax permMCB = new ComboBoxMultiselectMax();
                permMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
                permMCB.setWidth("100%");
                permMCB.setShowSelectAllButton(new ComboBoxMultiselectMax.ShowButton() {
                    @Override
                    public boolean isShow(String filter, int page) {
                        return true;
                    }
                });
                permMCB.addItems(convertStrToSet(permissionCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Value)).getValue().toString()));
                permissionCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Functions)).setValue(permMCB);
            }
            permissionTable.setContainerDataSource(permissionCont);
            permissionTable.setVisibleColumns(PERMISSION_NATURAL_COL_ORDER);
            permissionTable.setPageLength(7);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private Set<?> convertStrToSet(String str) {
        String[] strArr = str.split(",");
        HashSet<String> hs = new HashSet<String>(strArr.length);
        for (int i = 0; i < strArr.length; i++) {
            hs.add(strArr[i]);
        }
        return hs;
    }

    private String permJoinSingleStr() {
        Iterator iter = ((IndexedContainer) permissionTable
                .getContainerDataSource()).getItemIds().iterator();
        String permissions = "";
        while (iter.hasNext()) {
            Object next = iter.next();
            if (sysSettings.convertCollectionToStr((Set) ((ComboBoxMultiselectMax) (permissionTable
                    .getContainerProperty(next, myUI.getMessage(SptMessages.Functions))
                    .getValue())).getValue()) != null) {
                permissions += next + ":" + (sysSettings.convertCollectionToStr((Set) ((ComboBoxMultiselectMax) (permissionTable
                        .getContainerProperty(next, myUI.getMessage(SptMessages.Functions))
                        .getValue())).getValue())) + ";";
            }
        }
        if (permissions.length() > 1) {
            permissions = permissions.substring(0, permissions.length() - 1);
        }
        if (permissions.equals("")) {
            return null;
        } else {
            return permissions;
        }
    }

    private void clearPermissionTable() {
        if (permissionCont != null) {
            for (int i = 0; i < permissionCont.size(); i++) {
                ((ComboBoxMultiselectMax) permissionCont.getContainerProperty(permissionCont.getIdByIndex(i),
                        myUI.getMessage(SptMessages.Functions)).getValue()).setValue(null);
            }
        }
    }

    private void setPermTable_options(String oneStringAll) {
        try {
            String[] dividedStringAll = oneStringAll.split(";");
            int z = permissionCont.size();
            for (int i = 0; i < z; i++) {
                if (i < dividedStringAll.length && dividedStringAll[i] != null) {
                    String[] byClassName = dividedStringAll[i].split(":");
                    Item itm = permissionCont.getItem(byClassName[0]);
                    if (itm != null) {
                        ((ComboBoxMultiselectMax) itm.getItemProperty(
                                myUI.getMessage(SptMessages.Functions)).getValue())
                                .setValue(convertStrToSet(byClassName[1]));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
