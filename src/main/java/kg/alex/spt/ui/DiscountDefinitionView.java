package kg.alex.spt.ui;

import kg.alex.spt.utils.ComboBoxMax;
import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbDiscount;
import kg.alex.spt.dao.DbStudDiscount;
import kg.alex.spt.domain.Discount;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.DefinitionsFilterGenerator;
import kg.alex.spt.utils.FormattedFilterTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;

public class DiscountDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(DiscountDefinitionView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBoxMax discTypeSelect, statusSelect, yearSelect;
    private FormattedFilterTable dataTable;
    private TextField nameTF, valueTF;
    private PopupButton copyButton;
    private boolean isNew;

    private String[] NATURAL_COL_ORDER;
    private VerticalLayout settingsLay;
    private Subject currentUser = SecurityUtils.getSubject();

    public DiscountDefinitionView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Name),
            myUI.getMessage(SptMessages.Value), myUI.getMessage(SptMessages.DiscountType),
            myUI.getMessage(SptMessages.Year), myUI.getMessage(SptMessages.Status)};
        buildSettingsLayout();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new FormattedFilterTable();
        dataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        dataTable.setSelectable(true);
        dataTable.setFilterBarVisible(true);
        dataTable.addValueChangeListener(this);
        setTable_options();
        dataTable.setNullSelectionAllowed(false);
        dataTable.setFilterFieldValue(myUI.getMessage(SptMessages.Year),
                myUI.getUser().getCurrent_year().getName());
        dataTable.setFilterGenerator(new DefinitionsFilterGenerator(dataTable));
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
        buttonsLay.setWidth("100%");

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

        yearSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        yearSelect.addValueChangeListener(this);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);

        copyButton = new PopupButton(myUI.getMessage(SptMessages.Copy));
        copyButton.setWidth("100%");
        copyButton.setDescription(myUI.getMessage(SptMessages.Copy));
        copyButton.setIcon(FontAwesome.COPY);
        copyButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        copyButton.setImmediate(true);
        copyButton.setEnabled(false);
        copyButton.addClickListener(this);
        copyButton.setContent(yearSelect);
        buttonsLay.addComponent(copyButton);
        buttonsLay.setExpandRatio(copyButton, 1);

        discTypeSelect = new ComboBoxMax(myUI.getMessage(SptMessages.DiscountType));
        discTypeSelect.setNullSelectionAllowed(false);
        discTypeSelect.setRequired(true);
        discTypeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        discTypeSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        discTypeSelect.setWidth("100%");
        discTypeSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        discTypeSelect.setFilteringMode(FilteringMode.CONTAINS);
        discTypeSelect.addValueChangeListener(this);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            discTypeSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, SystemSettings.dbDiscountType));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        settingsLay.addComponent(discTypeSelect);

        nameTF = new TextField(myUI.getMessage(SptMessages.Name));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        nameTF.setWidth("100%");
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 250, false));
        settingsLay.addComponent(nameTF);

        ObjectProperty<Double> property = new ObjectProperty<Double>(0.0);
        valueTF = new TextField(myUI.getMessage(SptMessages.Value), property);
        valueTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        valueTF.setRequired(true);
        valueTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        valueTF.setNullRepresentation("");
        valueTF.setConverter(SystemSettings.getStringToDoubleConverter());
        valueTF.setWidth("100%");
        valueTF.addValidator(new DoubleRangeValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
        settingsLay.addComponent(valueTF);

        statusSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Status));
        statusSelect.setNullSelectionAllowed(false);
        statusSelect.setRequired(true);
        statusSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        statusSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        statusSelect.setWidth("100%");
        statusSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        statusSelect.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            statusSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, SystemSettings.dbActivity_status));
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
            int i = 0;
            try {
                DbStudDiscount dbsd = new DbStudDiscount();
                dbsd.connect();
                i = dbsd.exec_disc_count((Integer) dataTable.getValue());
                dbsd.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            if (i != 0) {
                discTypeSelect.setEnabled(false);
                valueTF.setEnabled(false);
            } else {
                discTypeSelect.setEnabled(true);
                valueTF.setEnabled(true);
            }
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
            discTypeSelect.setEnabled(true);
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
                    DbDiscount dbDis = new DbDiscount();
                    dbDis.connect();
                    if (isNew) {
                        int id = dbDis.exec_insert(getDiscount(0));
                        if (id != 0) {
                            removeTableFilters();
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
                            status = dbDis.exec_update(
                                    getDiscount((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                            SystemSettings.id).getValue()));
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (status != 0) {
                            removeTableFilters();
                            updateDatacontainer();
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    }
                    dbDis.close();
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
        } else if (source == copyButton) {
            try {
                DbDiscount dbDis = new DbDiscount();
                dbDis.connect();
                yearSelect.setContainerDataSource(
                        dbDis.execSQL_for_year_sel(myUI, myUI.getUser().getCurrent_year().getId()));
                dbDis.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == dataTable) {
            if (dataTable.getItem(dataTable.getValue()) != null) {
                fillFields();
            } else if (dataTable.getItem(dataTable.getValue()) == null) {
                clearFields();
            }
        } else if (property == discTypeSelect && discTypeSelect.getValue() != null) {
            if ((Integer) discTypeSelect.getValue() == 1) {
                valueTF.setCaption(myUI.getMessage(SptMessages.Value));
                valueTF.removeAllValidators();
                valueTF.addValidator(new DoubleRangeValidator(
                        myUI.getMessage(SptMessages.NotifWrongPercentValue), 0.1, 100.0));
            } else if ((Integer) discTypeSelect.getValue() == 3) {
                valueTF.setCaption(myUI.getMessage(SptMessages.MaxValue));
                valueTF.removeAllValidators();
                valueTF.addValidator(new DoubleRangeValidator(
                        myUI.getMessage(SptMessages.NotifWrongPercentValue), 0.1, 100.0));
            } else if ((Integer) discTypeSelect.getValue() == 2) {
                valueTF.setCaption(myUI.getMessage(SptMessages.Value));
                valueTF.removeAllValidators();
                valueTF.addValidator(new DoubleRangeValidator(
                        myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
            } else if ((Integer) discTypeSelect.getValue() == 4) {
                valueTF.setCaption(myUI.getMessage(SptMessages.MaxValue));
                valueTF.removeAllValidators();
                valueTF.addValidator(new DoubleRangeValidator(
                        myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
            }
        } else if (property == yearSelect && yearSelect.getValue() != null
                && dataTable.getValue() != null) {
            try {
                ConfirmDialog.show(myUI, myUI.getMessage(SptMessages.Question),
                        myUI.getMessage(SptMessages.ConfirmDiscountCopy)
                        + yearSelect.getContainerProperty(yearSelect.getValue(),
                                myUI.getMessage(SptMessages.Name))
                                .getValue().toString() + " года?",
                        myUI.getMessage(SptMessages.Yes),
                        myUI.getMessage(SptMessages.No),
                        new ConfirmDialog.Listener() {
                    @Override
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            copyDiscounts();
                        }
                    }
                });
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }

        }
    }

    private void prepareModificationMode() {
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        copyButton.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        dataTable.setEnabled(false);
        nameTF.setEnabled(true);
        valueTF.setEnabled(true);
//        discTypeSelect.setEnabled(true);
        statusSelect.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(SystemSettings.cnDiscountDefinitionView + ":" + SystemSettings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(SystemSettings.cnDiscountDefinitionView + ":" + SystemSettings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(SystemSettings.cnDiscountDefinitionView + ":" + SystemSettings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(SystemSettings.cnDiscountDefinitionView + ":" + SystemSettings.actCopy)) {
            copyButton.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        valueTF.setEnabled(false);
        discTypeSelect.setEnabled(false);
        statusSelect.setEnabled(false);
    }

    private void fillFields() {
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Name)).getValue().toString());
        valueTF.getPropertyDataSource().setValue(
                (Double) dataTable.getContainerProperty(dataTable.getValue(),
                        myUI.getMessage(SptMessages.Value)).getValue());
        discTypeSelect.setValue(Integer.parseInt(dataTable.getContainerProperty(dataTable.getValue(),
                SystemSettings.discount_type_id).getValue().toString()));
        statusSelect.setValue(Integer.parseInt(dataTable.getContainerProperty(dataTable.getValue(),
                SystemSettings.status_id).getValue().toString()));

    }

    private void clearFields() {
        nameTF.setValue("");
        valueTF.setValue(null);
        valueTF.removeAllValidators();
        discTypeSelect.setValue(null);
        statusSelect.setValue(null);

    }

    private void updateDatacontainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Name)).setValue(nameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Value)).setValue(
                (Double) valueTF.getPropertyDataSource().getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.DiscountType)).setValue(discTypeSelect
                .getContainerProperty(discTypeSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                SystemSettings.discount_type_id).setValue(
                        (Integer) discTypeSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                SystemSettings.status_id).setValue(
                        (Integer) statusSelect.getValue());
    }

    private void addDatacontainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(
                nameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Value)).setValue(
                (Double) valueTF.getPropertyDataSource().getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.DiscountType)).setValue(
                discTypeSelect.getContainerProperty(discTypeSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());
        item.getItemProperty(SystemSettings.discount_type_id).setValue(
                (Integer) discTypeSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());
        item.getItemProperty(SystemSettings.status_id).setValue(
                (Integer) statusSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Year)).setValue(
                myUI.getUser().getCurrent_year().getName());
        item.getItemProperty(SystemSettings.year_id).setValue(
                myUI.getUser().getCurrent_year().getId());
        item.getItemProperty(SystemSettings.id).setValue(id);
        dataTable.setValue(id);
    }

    private Discount getDiscount(int i) {
        Discount d = new Discount();
        d.setDisc_type_id((Integer) discTypeSelect.getValue());
        d.setName(nameTF.getValue());
        d.setAmount((Double) valueTF.getPropertyDataSource().getValue());
        d.setYear_id(myUI.getUser().getCurrent_year().getId());
        d.setStatus_id((Integer) statusSelect.getValue());
        d.setId(i);
        return d;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    SystemSettings.id).getValue(), SystemSettings.dbDiscount);
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

    private void setTable_options() {
        try {
            DbDiscount dbd = new DbDiscount();
            dbd.connect();
            dataTable.setContainerDataSource(dbd.execSQL(myUI));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns(NATURAL_COL_ORDER);
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Value), CustomTable.Align.RIGHT);
        if (dataTable.getContainerDataSource().size() != 0) {
            dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
        }
    }

    private void copyDiscounts() {
        int status = 0;
        try {
            DbDiscount dbDis = new DbDiscount();
            dbDis.connect();
            status = dbDis.exec_copy(myUI.getUser().getCurrent_year().getId(),
                    (Integer) yearSelect.getValue());
            dbDis.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (status != 0) {
            setTable_options();
            Notification.show(myUI.getMessage(SptMessages.ValuesCopied),
                    Notification.Type.HUMANIZED_MESSAGE);
        } else {
            Notification.show(myUI.getMessage(SptMessages.ValuesCanNotBeCopied),
                    Notification.Type.WARNING_MESSAGE);
        }
    }

    private void removeTableFilters() {
        Iterator iter = dataTable.getContainerPropertyIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            dataTable.setFilterFieldValue(next, null);
        }
    }
}
