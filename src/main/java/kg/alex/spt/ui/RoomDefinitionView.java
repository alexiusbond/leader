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
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbInventoryInvoice;
import kg.alex.spt.dao.DbRoom;
import kg.alex.spt.domain.Room;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Iterator;

public class RoomDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(RoomDefinitionView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBoxMax blockSelect, floorSelect, statusSelect;
    private Table dataTable;
    private TextField nameTF, descriptionTF;
    private boolean isNew;

    private String[] NATURAL_COL_ORDER;
    private VerticalLayout settingsLay;
    private Subject currentUser = SecurityUtils.getSubject();

    public RoomDefinitionView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Block),
                myUI.getMessage(SptMessages.Floor), myUI.getMessage(SptMessages.Title),
                myUI.getMessage(SptMessages.Description), myUI.getMessage(SptMessages.Status)};
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
            DbRoom dbCon = new DbRoom();
            dbCon.connect();
            dataTable.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool_id()));
            dbCon.close();
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

        nameTF = new TextField(myUI.getMessage(SptMessages.Title));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        nameTF.setWidth("100%");
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 50, false));
        settingsLay.addComponent(nameTF);

        descriptionTF = new TextField(myUI.getMessage(SptMessages.Description));
        descriptionTF.setRequired(true);
        descriptionTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        descriptionTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        descriptionTF.setWidth("100%");
        descriptionTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 150, false));
        settingsLay.addComponent(descriptionTF);

        blockSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Block));
        blockSelect.setNullSelectionAllowed(false);
        blockSelect.setRequired(true);
        blockSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        blockSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        blockSelect.setWidth("100%");
        blockSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        blockSelect.setFilteringMode(FilteringMode.CONTAINS);

        floorSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Floor));
        floorSelect.setNullSelectionAllowed(false);
        floorSelect.setRequired(true);
        floorSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        floorSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        floorSelect.setWidth("100%");
        floorSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        floorSelect.setFilteringMode(FilteringMode.CONTAINS);

        statusSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Status));
        statusSelect.setNullSelectionAllowed(false);
        statusSelect.setRequired(true);
        statusSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        statusSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        statusSelect.setWidth("100%");
        statusSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        statusSelect.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            blockSelect.setContainerDataSource(dbDef.exec_for_select(myUI,
                    Settings.dbBlock, myUI.getUser().getSchool_id(), false));
            floorSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbFloor, false));
            statusSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbActivity_status, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        settingsLay.addComponent(blockSelect);
        settingsLay.addComponent(floorSelect);
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
                    DbRoom dbCon = new DbRoom();
                    dbCon.connect();
                    if (isNew) {
                        int id = dbCon.exec_insert(getRoom(0));
                        if (id != 0) {
                            addDatacontainerItem(id);
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        boolean isUsed = false;
                        try {
                            DbInventoryInvoice dbii = new DbInventoryInvoice();
                            dbii.connect();
                            isUsed = dbii.isUsed(0, (Integer) dataTable.getContainerProperty(
                                    dataTable.getValue(), Settings.id).getValue());
                            dbii.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (isUsed) {
                            Notification.show(myUI.getMessage(SptMessages.ValueIsUsed),
                                    Notification.Type.WARNING_MESSAGE);
                        } else {
                            int status = 0;
                            try {
                                status = dbCon.exec_update(
                                        getRoom((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                                Settings.id).getValue()));
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
                    }
                    dbCon.close();
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
        descriptionTF.setEnabled(true);
        blockSelect.setEnabled(true);
        floorSelect.setEnabled(true);
        statusSelect.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnBlockDefinitionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnBlockDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnBlockDefinitionView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        descriptionTF.setEnabled(false);
        blockSelect.setEnabled(false);
        floorSelect.setEnabled(false);
        statusSelect.setEnabled(false);
    }

    private void fillFields() {
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Title)).getValue().toString());
        descriptionTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Description)).getValue().toString());
        blockSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.block_id).getValue());
        floorSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.floor_id).getValue());
        statusSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).getValue());
    }

    private void clearFields() {
        nameTF.setValue("");
        descriptionTF.setValue("");
        blockSelect.setValue(null);
        floorSelect.setValue(null);
        statusSelect.setValue(null);
    }

    private void updateDatacontainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Title)).setValue(nameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Description)).setValue(descriptionTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.block_id).setValue(blockSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Block)).setValue(blockSelect.
                getContainerProperty(blockSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.floor_id).setValue(floorSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Floor)).setValue(floorSelect.
                getContainerProperty(floorSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).setValue(statusSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Status)).setValue(statusSelect.
                getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
    }

    private void addDatacontainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(
                nameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Description)).setValue(
                descriptionTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
        item.getItemProperty(Settings.status_id).setValue(
                statusSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Block)).setValue(
                blockSelect.getContainerProperty(blockSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
        item.getItemProperty(Settings.block_id).setValue(
                blockSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Floor)).setValue(
                floorSelect.getContainerProperty(floorSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
        item.getItemProperty(Settings.floor_id).setValue(
                floorSelect.getValue());
        item.getItemProperty(Settings.id).setValue(id);
        dataTable.setValue(id);
    }

    private Room getRoom(int i) {
        Room room = new Room();
        room.setName(nameTF.getValue());
        room.setDescription(descriptionTF.getValue());
        room.setBlock_id((Integer) blockSelect.getValue());
        room.setFloor_id((Integer) floorSelect.getValue());
        room.setActivity_status_id((Integer) statusSelect.getValue());
        room.setId(i);
        return room;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue(), Settings.dbRoom);
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
        return new RoomDefinitionView(myUI);
    }
}
