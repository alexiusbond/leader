package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.*;
import kg.alex.spt.domain.InventoryInvoice;
import kg.alex.spt.domain.InventoryLiquidation;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.pdf.InventoryLiquidationPdf;
import kg.alex.spt.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.inputmask.InputMask;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class InventoryLiquidationView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {


    static final Logger logger = LogManager.getLogger(InventoryLiquidationView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn, printBtn, addBtn;
    private PopupButton searchBtn;
    private ComboBoxMax blockSelect, floorSelect, roomSelect;
    private FormattedTable inventoriesTable;
    private TextField invoiceNumberTF, codeTF;
    private TextArea noteTF;
    private DateField dateDF;
    private boolean isNew;

    private String[] NATURAL_COL_ORDER, NATURAL_COL_ORDER_INVENTORIES;
    private GridLayout settingsLay, rightLay;
    private Subject currentUser = SecurityUtils.getSubject();
    private FormattedFilterTable invoicesTable;
    private IndexedContainer inventoriesCont;
    private int r_table_counter = 1000;
    private ArrayList<String> delInventoryIds = new ArrayList<>();
    private int invID;
    private String[] parsedValues = null;

    public InventoryLiquidationView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.InvoiceNumber),
                myUI.getMessage(SptMessages.Block), myUI.getMessage(SptMessages.Floor),
                myUI.getMessage(SptMessages.Room),
                myUI.getMessage(SptMessages.Date), myUI.getMessage(SptMessages.Quantity),
                myUI.getMessage(SptMessages.Employee), myUI.getMessage(SptMessages.Note)};

        rightLay = new GridLayout(3, 2);
        rightLay.setSpacing(true);
        rightLay.setSizeFull();
        rightLay.setMargin(true);

        addBtn = new Button(myUI.getMessage(SptMessages.AddRecord));
        addBtn.setStyleName(ValoTheme.BUTTON_SMALL);
        addBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        addBtn.setIcon(FontAwesome.PLUS_SQUARE);
        addBtn.addClickListener(this);
        rightLay.addComponent(addBtn, 2, 0);

        inventoriesTable = new FormattedTable();
        inventoriesTable.setStyleName(ValoTheme.TABLE_COMPACT);
        inventoriesTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        inventoriesTable.setSizeFull();
        inventoriesTable.setFooterVisible(true);
        rightLay.addComponent(inventoriesTable, 0, 1, 2, 1);
        rightLay.setRowExpandRatio(1, 1);
        rightLay.setColumnExpandRatio(1, 1);

        buildSettingsLayout();
        this.setSplitPosition(24, Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(rightLay);

        prepareNormalMode();
    }

    private void buildSettingsLayout() {

        settingsLay = new GridLayout(2, 8);
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setWidth(Settings.PERCENTS100);

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

        printBtn = new Button();
        printBtn.setDescription(myUI.getMessage(SptMessages.Print));
        printBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        printBtn.setIcon(FontAwesome.FILE_PDF_O);
        printBtn.addClickListener(this);
        buttonsLay.addComponent(printBtn);
        settingsLay.addComponent(buttonsLay, 0, 0, 1, 0);

        invoiceNumberTF = new TextField(myUI.getMessage(SptMessages.InvoiceNumber));
        invoiceNumberTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        invoiceNumberTF.setWidth(Settings.PERCENTS100);
        invoiceNumberTF.addValueChangeListener(this);
        settingsLay.addComponent(invoiceNumberTF);

        searchBtn = new PopupButton();
        searchBtn.setDescription(myUI.getMessage(SptMessages.Search));
        searchBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        searchBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        searchBtn.setIcon(FontAwesome.BINOCULARS);
        searchBtn.addClickListener(this);
        settingsLay.addComponent(searchBtn);
        settingsLay.setComponentAlignment(searchBtn, Alignment.BOTTOM_RIGHT);

        codeTF = new TextField(myUI.getMessage(SptMessages.SearchByCode));
        codeTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        codeTF.setIcon(FontAwesome.SEARCH);
        codeTF.setWidth(Settings.PERCENTS100);
        codeTF.addValueChangeListener(this);
        settingsLay.addComponent(codeTF, 0, 2, 1, 2);

        dateDF = createDateFiled(new Date(), null, myUI.getMessage(SptMessages.Date),
                Resolution.MINUTE, Settings.dateTimeMinPattern);
        dateDF.addValueChangeListener(this);
        settingsLay.addComponent(dateDF, 0, 3, 1, 3);

        blockSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Block));
        blockSelect.setNullSelectionAllowed(false);
        blockSelect.setRequired(true);
        blockSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        blockSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        blockSelect.setWidth(Settings.PERCENTS100);
        blockSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        blockSelect.setFilteringMode(FilteringMode.CONTAINS);
        blockSelect.addValueChangeListener(this);
        settingsLay.addComponent(blockSelect, 0, 4, 1, 4);

        floorSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Floor));
        floorSelect.setNullSelectionAllowed(false);
        floorSelect.setRequired(true);
        floorSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        floorSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        floorSelect.setWidth(Settings.PERCENTS100);
        floorSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        floorSelect.setFilteringMode(FilteringMode.CONTAINS);
        floorSelect.addValueChangeListener(this);
        settingsLay.addComponent(floorSelect, 0, 5, 1, 5);
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            blockSelect.setContainerDataSource(dbDef.exec_for_select(myUI,
                    Settings.dbBlock, myUI.getUser().getSchool_id(), false));
            floorSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbFloor, false));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        roomSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Room));
        roomSelect.setNullSelectionAllowed(false);
        roomSelect.setRequired(true);
        roomSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        roomSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        roomSelect.setWidth(Settings.PERCENTS100);
        roomSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        roomSelect.setFilteringMode(FilteringMode.CONTAINS);
        roomSelect.addValueChangeListener(this);
        settingsLay.addComponent(roomSelect, 0, 6, 1, 6);

        noteTF = new TextArea(myUI.getMessage(SptMessages.Note));
        noteTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        noteTF.setWidth(Settings.PERCENTS100);
        noteTF.setRows(3);
        settingsLay.addComponent(noteTF, 0, 7, 1, 7);
        settingsLay.setColumnExpandRatio(0, 1);

        buildSearchLayout();
        searchBtn.setContent(invoicesTable);
    }

    private void buildSearchLayout() {

        invoicesTable = new FormattedFilterTable();
        invoicesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        invoicesTable.setStyleName(ValoTheme.TABLE_COMPACT);
        invoicesTable.setSelectable(true);
        invoicesTable.setFilterBarVisible(true);
        invoicesTable.setNullSelectionAllowed(false);
        invoicesTable.addValueChangeListener(this);
        try {
            DbInventoryInvoice dbCon = new DbInventoryInvoice();
            dbCon.connect();
            invoicesTable.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool_id(), 1));
            dbCon.close();
            if (invoicesTable.getContainerDataSource().size() != 0) {
                invoicesTable.setValue(((IndexedContainer) invoicesTable.getContainerDataSource()).firstItemId());
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        invoicesTable.setVisibleColumns(NATURAL_COL_ORDER);
        invoicesTable.setPageLength(5);
        invoicesTable.setColumnAlignment(myUI.getMessage(SptMessages.Quantity), CustomTable.Align.RIGHT);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == modifyBtn && invoicesTable.getValue() != null) {
            isNew = false;
            fillFields();
            prepareModificationMode();
            blockSelect.setEnabled(false);
            floorSelect.setEnabled(false);
            roomSelect.setEnabled(false);
            InputMask im = new InputMask(blockSelect.getItemCaption(blockSelect.getValue()) + "-" +
                    floorSelect.getItemCaption(floorSelect.getValue()) + "-" +
                    roomSelect.getItemCaption(roomSelect.getValue()).trim() + "-\\d{1,}");
            im.setRegexMask(true);
            im.extend(codeTF);
            invoiceNumberTF.focus();
        } else if (source == printBtn) {
            InventoryInvoice inv = getInvoice(invID);
            inv.setRoom(roomSelect.getItemCaption(roomSelect.getValue()));
            inv.setBlock(blockSelect.getItemCaption(blockSelect.getValue()));
            inv.setFloor(floorSelect.getItemCaption(floorSelect.getValue()));
            try {
                DbSchool dbsc = new DbSchool();
                dbsc.connect();
                StudentInfoPdf st = dbsc.execGetSchoolPdf(myUI.getUser().getSchool_id());
                dbsc.close();
                if (st.getScl_address() != null && st.getScl_phone() != null
                        && st.getScl_name_ru() != null) {
                    new InventoryLiquidationPdf(myUI, myUI.getMessage(SptMessages.InventoryLiquidation), inv, inventoriesCont,
                            inventoriesTable.getColumnFooter(myUI.getMessage(SptMessages.Quantity)), st);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }

        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
            InputMask im = new InputMask("[a-zA-Z0-9_]{1,}-\\d{1,}-\\d{1,}-\\d{1,}");
            im.setRegexMask(true);
            im.extend(codeTF);
            codeTF.focus();
        } else if (source == deleteBtn && invoicesTable.getValue() != null) {
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
                if (validate(settingsLay) && validateTable(inventoriesTable, false)) {
                    DbInventoryInvoice dbCon = new DbInventoryInvoice();
                    dbCon.connect();
                    if (isNew) {
                        String usedInvoiceNumber = isUsed(null);
                        if (usedInvoiceNumber != null) {
                            Notification.show(myUI.getMessage(SptMessages.RoomIsUsed)
                                    + " (" + myUI.getMessage(SptMessages.InvoiceNumber) + " - "
                                    + usedInvoiceNumber + ")", Notification.Type.WARNING_MESSAGE);
                        } else {
                            InventoryInvoice inv = getInvoice(0);
                            int id = dbCon.exec_insert(inv);
                            if (id != 0) {
                                insertInventories(id);
                                addDatacontainerItem(id);
                                invoicesTable.setValue(id);
                                Notification.show(myUI.getMessage(SptMessages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                                prepareNormalMode();
                            } else {
                                Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved), Notification.Type.WARNING_MESSAGE);
                            }
                        }
                    } else {
                        String usedInvoiceNumber = isUsed(invID);
                        if (usedInvoiceNumber != null) {
                            Notification.show(myUI.getMessage(SptMessages.RoomIsUsed)
                                    + " (" + myUI.getMessage(SptMessages.InvoiceNumber) + " - "
                                    + usedInvoiceNumber + ")", Notification.Type.WARNING_MESSAGE);
                        } else {
                            int status = 0;
                            InventoryInvoice inv = getInvoice(invID);
                            try {
                                status = dbCon.exec_update(inv);
                            } catch (Exception e) {
                                logger.error(e);
                                logger.catching(e);
                            }
                            if (status != 0) {
                                insertInventories(invID);
                                updateDatacontainer();
                                setInventoriesTable();
                                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                        Notification.Type.HUMANIZED_MESSAGE);
                                prepareNormalMode();
                            } else {
                                Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                        Notification.Type.WARNING_MESSAGE);
                            }
                        }
                    }
                    dbCon.close();
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == cancelBtn) {
            if (invoicesTable.getValue() != null) {
                fillFields();
                setInventoriesTable();
            }
            delInventoryIds.clear();
            prepareNormalMode();
        } else if (source == addBtn) {
            if (blockSelect.getValue() == null || floorSelect.getValue() == null
                    || roomSelect.getValue() == null) {
                Notification.show(myUI.getMessage(SptMessages.RequiredField),
                        Notification.Type.WARNING_MESSAGE);
            } else {
                addInventoriesItem();
                blockSelect.setEnabled(false);
                floorSelect.setEnabled(false);
                roomSelect.setEnabled(false);
                InputMask im = new InputMask(blockSelect.getItemCaption(blockSelect.getValue()) + "-" +
                        floorSelect.getItemCaption(floorSelect.getValue()) + "-" +
                        roomSelect.getItemCaption(roomSelect.getValue()).trim() + "-\\d{1,}");
                im.setRegexMask(true);
                im.extend(codeTF);
            }
        } else if (source.getId() != null && source.getId().equals(Settings.dbInventoryLiquidation)) {
            delInventoryIds.add(source.getData().toString());
            inventoriesTable.removeItem(event.getButton().getData().toString());
            if (inventoriesTable.size() == 0) {
                blockSelect.setEnabled(true);
                floorSelect.setEnabled(true);
                roomSelect.setEnabled(true);
                codeTF.setEnabled(true);
            }
            repaintInventoriesFooter();
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == invoicesTable) {
            invoiceNumberTF.removeValueChangeListener(this);
            if (invoicesTable.getItem(invoicesTable.getValue()) != null) {
                invID = (Integer) invoicesTable.getValue();
                fillFields();
                setInventoriesTable();
            }
            invoiceNumberTF.addValueChangeListener(this);
        } else if (property == blockSelect || property == floorSelect) {
            if (blockSelect.getValue() != null && floorSelect.getValue() != null) {
                try {
                    DbRoom dbCon = new DbRoom();
                    dbCon.connect();
                    roomSelect.setContainerDataSource(dbCon.exec_for_select(myUI,
                            (Integer) blockSelect.getValue(), (Integer) floorSelect.getValue()));
                    dbCon.close();
                    if (invoicesTable.getValue() != null && !isNew) {
                        roomSelect.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                                Settings.room_id).getValue());
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            } else {
                roomSelect.setContainerDataSource(null);
            }
        } else if (property == roomSelect) {
            if (roomSelect.getValue() != null) {
                rightLay.setEnabled(true);
            } else {
                rightLay.setEnabled(false);
            }
        } else if (property == invoiceNumberTF) {
            invoiceNumberTF.removeValueChangeListener(this);
            Iterator iter = invoicesTable.getItemIds().iterator();
            boolean isFound = false;
            while (iter.hasNext()) {
                Object next = iter.next();
                if (invoicesTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.InvoiceNumber)).getValue().equals(
                        property.getValue())) {
                    invoicesTable.setValue(next);
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                invoiceNumberTF.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                        myUI.getMessage(SptMessages.InvoiceNumber)).getValue().toString());
            }
            invoiceNumberTF.addValueChangeListener(this);
        } else if (property == codeTF && codeTF.getValue() != null && !codeTF.getValue().equals("")) {
            parsedValues = codeTF.getValue().split("-");
            if (parsedValues.length > 0) {
                setValueByItemCaption(blockSelect, parsedValues[0]);
            }
            if (parsedValues.length > 1) {
                setValueByItemCaption(floorSelect, parsedValues[1]);
            }
            if (blockSelect.getValue() != null && floorSelect.getValue() != null) {
                try {
                    DbRoom dbCon = new DbRoom();
                    dbCon.connect();
                    roomSelect.setContainerDataSource(dbCon.exec_for_select(myUI,
                            (Integer) blockSelect.getValue(), (Integer) floorSelect.getValue()));
                    dbCon.close();
                    if (parsedValues != null && parsedValues.length > 2) {
                        setValueByItemCaption(roomSelect, parsedValues[2]);
                        if (roomSelect.getValue() != null) {
                            addInventoriesItem();
                            if (((ComboBoxMax) inventoriesTable.getContainerProperty(Settings.FreshItem + r_table_counter,
                                    myUI.getMessage(SptMessages.InventoryItem)).getValue()).getValue() != null) {
                                blockSelect.setEnabled(false);
                                floorSelect.setEnabled(false);
                                roomSelect.setEnabled(false);
                                InputMask im = new InputMask(blockSelect.getItemCaption(blockSelect.getValue()) + "-" +
                                        floorSelect.getItemCaption(floorSelect.getValue()) + "-" +
                                        roomSelect.getItemCaption(roomSelect.getValue()).trim() + "-\\d{1,}");
                                im.setRegexMask(true);
                                im.extend(codeTF);
                            } else {
                                inventoriesTable.removeItem(Settings.FreshItem + r_table_counter);
                                Notification.show(myUI.getMessage(SptMessages.InventoryNotFound),
                                        Notification.Type.WARNING_MESSAGE);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            parsedValues = null;
            codeTF.setValue("");
            codeTF.focus();
        } else {
            Object changedItemId = ((AbstractField) property).getId();
            if (((AbstractField) property).getData() != null
                    && (((AbstractField) property).getData().equals(myUI.getMessage(SptMessages.Quantity))
                    || ((AbstractField) property).getData().equals(myUI.getMessage(SptMessages.InventoryItem)))) {
                TextField quantityTF = (TextField) inventoriesTable.getContainerProperty(changedItemId,
                        myUI.getMessage(SptMessages.Quantity)).getValue();
                ComboBoxMax inventoryCB = (ComboBoxMax) inventoriesTable.getContainerProperty(changedItemId,
                        myUI.getMessage(SptMessages.InventoryItem)).getValue();
                if (((AbstractField) property).getData().equals(myUI.getMessage(SptMessages.InventoryItem))) {
                    quantityTF.removeAllValidators();
                    quantityTF.addValidator(new IntegerRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue),
                            1, (Integer) inventoryCB.getContainerProperty(
                            inventoryCB.getValue(), myUI.getMessage(SptMessages.Remain)).getValue()
                            + (Integer) inventoryCB.getContainerProperty(
                            inventoryCB.getValue(), myUI.getMessage(SptMessages.Quantity)).getValue()));
                }
                if (quantityTF != null && quantityTF.getPropertyDataSource().getValue() != null) {
                    if (inventoryCB != null && inventoryCB.getValue() != null) {
                        inventoriesTable.getContainerProperty(changedItemId, myUI.getMessage(SptMessages.Remain)).setValue(
                                (Integer) inventoryCB.getContainerProperty(inventoryCB.getValue(),
                                        myUI.getMessage(SptMessages.Remain)).getValue() +
                                        ((Integer) inventoryCB.getContainerProperty(inventoryCB.getValue(),
                                                myUI.getMessage(SptMessages.Quantity)).getValue() -
                                                (Integer) quantityTF.getPropertyDataSource().getValue()));
                    }
                }
                repaintInventoriesFooter();
            }
        }
    }

    private void setValueByItemCaption(ComboBoxMax cb, String value) {
        if (value != null && !value.isEmpty()) {
            Iterator iter = cb.getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (cb.getContainerProperty(next, myUI.getMessage(SptMessages.Title))
                        .getValue().toString().trim().equalsIgnoreCase(value)) {
                    cb.setValue(next);
                    break;
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
        searchBtn.setEnabled(false);
        invoiceNumberTF.setEnabled(false);
        roomSelect.setEnabled(true);
        blockSelect.setEnabled(true);
        floorSelect.setEnabled(true);
        codeTF.setEnabled(true);
        dateDF.setEnabled(true);
        noteTF.setEnabled(true);
        if (roomSelect.getValue() != null) {
            rightLay.setEnabled(true);
        }
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnStockIncomeView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnStockIncomeView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnStockIncomeView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        invoiceNumberTF.setEnabled(true);
        searchBtn.setEnabled(true);
        roomSelect.setEnabled(false);
        blockSelect.setEnabled(false);
        floorSelect.setEnabled(false);
        codeTF.setEnabled(false);
        dateDF.setEnabled(false);
        noteTF.setEnabled(false);
        rightLay.setEnabled(false);
    }

    private void fillFields() {
        invoiceNumberTF.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                myUI.getMessage(SptMessages.InvoiceNumber)).getValue().toString());
        try {
            dateDF.setValue(Settings.dtmf.parse(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                    myUI.getMessage(SptMessages.Date)).getValue().toString()));
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        blockSelect.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                Settings.block_id).getValue());
        floorSelect.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                Settings.floor_id).getValue());
        roomSelect.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                Settings.room_id).getValue());
        if (invoicesTable.getContainerProperty(invoicesTable.getValue(),
                myUI.getMessage(SptMessages.Note)).getValue() != null) {
            noteTF.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                    myUI.getMessage(SptMessages.Note)).getValue().toString());
        } else {
            noteTF.setValue("");
        }
    }

    private void clearFields() {
        invoiceNumberTF.removeValueChangeListener(this);
        invoiceNumberTF.setValue("");
        invoiceNumberTF.addValueChangeListener(this);
        dateDF.setValue(new Date());
        roomSelect.setValue(null);
        blockSelect.setValue(null);
        floorSelect.setValue(null);
        noteTF.setValue("");
        codeTF.setValue("");
        inventoriesTable.removeAllItems();
        setInventoriesFooter(null);
    }

    private void updateDatacontainer() {
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Date)).setValue(
                Settings.dtmf.format(dateDF.getValue()));
        try {
            invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Quantity)).setValue(
                    Integer.parseInt(inventoriesTable.getColumnFooter(myUI.getMessage(SptMessages.Quantity))));
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Note)).setValue(
                noteTF.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Room)).setValue(
                roomSelect.getContainerProperty(roomSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.room_id).setValue(
                roomSelect.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Block)).setValue(
                blockSelect.getContainerProperty(blockSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.block_id).setValue(
                blockSelect.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Floor)).setValue(
                floorSelect.getContainerProperty(floorSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.floor_id).setValue(
                floorSelect.getValue());
    }

    private void addDatacontainerItem(int id) {
        Item item = ((IndexedContainer) invoicesTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                Settings.dtmf.format(dateDF.getValue()));
        try {
            item.getItemProperty(myUI.getMessage(SptMessages.Quantity)).setValue(
                    Integer.parseInt(inventoriesTable.getColumnFooter(myUI.getMessage(SptMessages.Quantity))));
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                noteTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Room)).setValue(
                roomSelect.getContainerProperty(roomSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        item.getItemProperty(Settings.room_id).setValue(roomSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Block)).setValue(
                blockSelect.getContainerProperty(blockSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        item.getItemProperty(Settings.block_id).setValue(blockSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Floor)).setValue(
                floorSelect.getContainerProperty(floorSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        item.getItemProperty(Settings.floor_id).setValue(
                floorSelect.getValue());
        try {
            DbInventoryInvoice dbCon = new DbInventoryInvoice();
            dbCon.connect();
            item.getItemProperty(myUI.getMessage(SptMessages.InvoiceNumber)).setValue(dbCon.execSQL_invoice_number(id));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        inventoriesTable.setValue(id);
    }

    private InventoryInvoice getInvoice(int inv_id) {
        InventoryInvoice inv = new InventoryInvoice();
        if (inv_id == 0) {
            try {
                DbInventoryInvoice dbCon = new DbInventoryInvoice();
                dbCon.connect();
                inv.setInvoice_number(dbCon.execSQL_max_invoice_number(myUI.getUser().getSchool_id(), 1) + 1);
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else {
            inv.setInvoiceNumberStr(invoicesTable.getContainerProperty(inv_id, myUI.getMessage(SptMessages.InvoiceNumber)).getValue().toString());
        }
        if (noteTF.getValue() != null && !noteTF.getValue().equals("")) {
            inv.setNote(noteTF.getValue());
        }
        inv.setCreation_date(dateDF.getValue());
        inv.setRoom_id((Integer) roomSelect.getValue());
        inv.setActivity_status_id(1);
        inv.setSchool_id(myUI.getUser().getSchool_id());
        inv.setEmployee_id(myUI.getUser().getId());
        inv.setEmployee(myUI.getUser().getFullname());
        inv.setId(inv_id);
        return inv;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            DbInventoryLiquidation dbCon = new DbInventoryLiquidation();
            dbCon.connect();
            int st = dbCon.exec_delete((Integer) invoicesTable.getValue());
            if (st != 0) {
                inventoriesTable.removeAllItems();
                st = dbDef.exec_delete((Integer) invoicesTable.getValue(), Settings.db_dm_invoice);
                if (st != 0) {
                    invoicesTable.getContainerDataSource().removeItem(invoicesTable.getValue());
                    Notification.show(myUI.getMessage(SptMessages.ValueDeleted), Notification.Type.HUMANIZED_MESSAGE);
                    if (invoicesTable.getContainerDataSource().size() != 0) {
                        invoicesTable.setValue(((IndexedContainer) invoicesTable.getContainerDataSource()).firstItemId());
                    } else {
                        clearFields();
                        invoicesTable.setValue(null);
                    }
                }
            }
            dbCon.close();
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

    private String isUsed(Object selected) {
        Iterator iter = invoicesTable.getContainerDataSource().getItemIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            if ((selected == null || !selected.equals(next)) && roomSelect.getValue().equals(
                    invoicesTable.getContainerProperty(next, Settings.room_id).getValue())) {
                return invoicesTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.InvoiceNumber)).getValue().toString();
            }
        }
        return null;
    }

    private boolean validateTable(Table t, boolean isEmptyAllowed) {
        if (t.size() == 0 && !isEmptyAllowed) {
            Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                    Notification.Type.WARNING_MESSAGE);
            return false;
        } else {
            Iterator iter = ((IndexedContainer) t
                    .getContainerDataSource()).getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                Iterator iterProp = ((IndexedContainer) t
                        .getContainerDataSource()).getContainerPropertyIds().iterator();
                while (iterProp.hasNext()) {
                    Object next1 = iterProp.next();
                    Object c = t.getItem(next).getItemProperty(
                            next1).getValue();
                    if (c instanceof AbstractField) {
                        try {
                            ((AbstractField) c).validate();
                        } catch (Exception e) {
                            //((AbstractComponent) c).setComponentError(new UserError(e.getMessage()));
                            return false;
                        }
                    } else if (c instanceof AbstractComponentContainer) {
                        if (!validate((AbstractComponentContainer) c)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public ComboBoxMax createCombobox(int value, String description, String dbtable,
                                      boolean isRequired, boolean isEnabled, boolean isExistsValiator) {
        ComboBoxMax cb = new ComboBoxMax();
        if (isExistsValiator) {
            cb.addValidator(new ExistsValidator(myUI, inventoriesCont, cb, description));
        }
        if (isEnabled) {
            cb.setDescription(description);
        } else {
            cb.setDescription(myUI.getMessage(SptMessages.CanNotModify));
        }
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        if (isRequired) {
            cb.setRequired(true);
            cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        }
        try {
            if (dbtable != null) {
                DbDefinition dbp = new DbDefinition();
                dbp.connect();
                cb.setContainerDataSource(dbp.exec_for_select(myUI, dbtable, true));
                dbp.close();
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setNullSelectionAllowed(false);
        cb.setValue(value);
        cb.setEnabled(isEnabled);
        return cb;
    }

    public Button createButton(String description, String itemId, String tableName, boolean is_enabled) {
        Button btn = new Button();
        if (is_enabled) {
            btn.setDescription(description);
        } else {
            btn.setDescription(myUI.getMessage(SptMessages.CanNotDelete));
        }
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.addStyleName(ValoTheme.BUTTON_TINY);
        btn.setIcon(FontAwesome.MINUS_SQUARE);
        btn.setData(itemId);
        btn.setId(tableName);
        btn.addClickListener(this);
        btn.setEnabled(is_enabled);
        return btn;
    }

    public TextField createTextfieldWithProperty(Object value, String description, Validator validator, Property p,
                                                 Converter conv, boolean isEnabled) {
        TextField tf = new TextField(p);
        if (isEnabled) {
            tf.setDescription(description);
        } else {
            tf.setDescription(myUI.getMessage(SptMessages.CanNotModify));
        }
        tf.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.setRequired(true);
        tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tf.setNullRepresentation("");
        tf.setConverter(conv);
        tf.setWidth(Settings.PERCENTS100);
        tf.addValidator(validator);
        tf.getPropertyDataSource().setValue(value);
        tf.setEnabled(isEnabled);
        return tf;
    }

    public DateField createDateFiled(Date value, String description, String caption,
                                     Resolution resolution, String pattern) {
        DateField df = new DateField(caption);
        df.setDescription(description);
        df.setResolution(resolution);
        df.setWidth(Settings.PERCENTS100);
        df.setStyleName(ValoTheme.DATEFIELD_SMALL);
        df.setRequired(true);
        df.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        df.setDateFormat(pattern);
        df.setValue(value);
        return df;
    }

    public TextField createTextfield(String value, String description, Validator validator, boolean isRequired) {
        TextField tf = new TextField();
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        tf.addValidator(validator);
        if (isRequired) {
            tf.setRequired(true);
            tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        }
        if (value != null) {
            tf.setValue(value);
        }
        return tf;
    }

    public IndexedContainer prepareInventoriesContainer() {
        if (inventoriesCont == null) {
            inventoriesCont = new IndexedContainer();
            inventoriesCont.addContainerProperty(Settings.button, Button.class, null);
            inventoriesCont.addContainerProperty(myUI.getMessage(SptMessages.Remain), Integer.class, null);
            inventoriesCont.addContainerProperty(myUI.getMessage(SptMessages.InventoryItem), ComboBoxMax.class, null);
            inventoriesCont.addContainerProperty(myUI.getMessage(SptMessages.Quantity), TextField.class, null);
            inventoriesCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            inventoriesCont.removeAllItems();
        }
        return inventoriesCont;
    }

    private void addInventoriesItem() {
        NATURAL_COL_ORDER_INVENTORIES = new String[]{Settings.button,
                myUI.getMessage(SptMessages.Remain),
                myUI.getMessage(SptMessages.InventoryItem),
                myUI.getMessage(SptMessages.Quantity)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (inventoriesTable.getContainerDataSource().size() == 0) {
            inventoriesTable.setContainerDataSource(prepareInventoriesContainer());
        }
        Item item = ((IndexedContainer) inventoriesTable.getContainerDataSource()).addItemAt(
                inventoriesTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, Settings.dbInventoryLiquidation, true));
        ComboBoxMax cb = createCombobox(0, myUI.getMessage(SptMessages.InventoryItem),
                null, true, true, true);
        try {
            int invoice_id = 0;
            if (!isNew) {
                invoice_id = invID;
            }
            DbInventoryOrganization dbCon = new DbInventoryOrganization();
            dbCon.connect();
            cb.setContainerDataSource(dbCon.execSQL_for_select(myUI, (Integer) roomSelect.getValue(), invoice_id));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.addValueChangeListener(this);
        cb.setId(id);
        cb.setData(myUI.getMessage(SptMessages.InventoryItem));
        item.getItemProperty(myUI.getMessage(SptMessages.InventoryItem)).setValue(cb);
        TextField tf = createTextfieldWithProperty(
                1, myUI.getMessage(SptMessages.Quantity),
                new IntegerRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 1, null),
                new ObjectProperty<Integer>(0), Settings.getStringToIntegerConverter(), true);
        tf.addValueChangeListener(this);
        tf.setId(id);
        tf.setData(myUI.getMessage(SptMessages.Quantity));
        item.getItemProperty(myUI.getMessage(SptMessages.Quantity)).setValue(tf);
        if (parsedValues != null && parsedValues.length > 3) {
            cb.setValue(codeTF.getValue().toLowerCase());
        }
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        inventoriesTable.setVisibleColumns(NATURAL_COL_ORDER_INVENTORIES);
        inventoriesTable.setColumnExpandRatio(myUI.getMessage(SptMessages.InventoryItem), 1);
        inventoriesTable.setColumnAlignment(myUI.getMessage(SptMessages.Quantity), Table.Align.RIGHT);
        repaintInventoriesFooter();
    }

    private void setInventoriesTable() {
        try {
            NATURAL_COL_ORDER_INVENTORIES = new String[]{Settings.button,
                    myUI.getMessage(SptMessages.Remain),
                    myUI.getMessage(SptMessages.InventoryItem),
                    myUI.getMessage(SptMessages.Quantity)};
            DbInventoryLiquidation dbCon = new DbInventoryLiquidation();
            dbCon.connect();
            inventoriesTable.setContainerDataSource(dbCon.execSQL(myUI, invID, this));
            dbCon.close();
            inventoriesTable.setVisibleColumns(NATURAL_COL_ORDER_INVENTORIES);
            inventoriesTable.setColumnExpandRatio(myUI.getMessage(SptMessages.InventoryItem), 1);
            inventoriesTable.setColumnAlignment(myUI.getMessage(SptMessages.Quantity), Table.Align.RIGHT);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void repaintInventoriesFooter() {
        int totalQuantity = 0;
        if (inventoriesTable.getContainerDataSource().size() > 0) {
            Iterator iter = inventoriesTable.getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                totalQuantity += (Integer) ((TextField) inventoriesTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue();
            }
        }
        setInventoriesFooter(totalQuantity);
    }

    private void insertInventories(int invoice_id) {
        try {
            DbInventoryLiquidation dbCon = new DbInventoryLiquidation();
            dbCon.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delInventoryIds.size() > 0) {
                for (int i = 0; i < delInventoryIds.size(); i++) {
                    dbd.exec_delete(delInventoryIds.get(i), Settings.dbInventoryLiquidation);
                }
            }
            if (inventoriesTable.getContainerDataSource().size() > 0) {
                Iterator iter = inventoriesTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    InventoryLiquidation imv = new InventoryLiquidation();
                    imv.setInvoice_id(invoice_id);
                    imv.setQuantity((Integer) ((TextField) inventoriesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue());
                    imv.setRemain((Integer) inventoriesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Remain)).getValue());
                    ComboBoxMax cb = (ComboBoxMax) inventoriesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.InventoryItem)).getValue();
                    imv.setInventory_id((Integer) cb.getContainerProperty(cb.getValue(), Settings.id).getValue());
                    if (inventoriesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        imv.setId(Integer.parseInt(next.toString()));
                        dbCon.exec_update(imv);
                    } else if (inventoriesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbCon.exec_insert(imv);
                    }
                }
            }
            delInventoryIds.clear();
            dbCon.close();
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    public void setInventoriesFooter(Integer quantity) {
        if (quantity != null) {
            inventoriesTable.setColumnFooter(myUI.getMessage(SptMessages.Quantity), quantity + "");
        } else {
            inventoriesTable.setColumnFooter(myUI.getMessage(SptMessages.Quantity), null);
        }
    }

    public Component getNewObj() {
        return new InventoryLiquidationView(myUI);
    }
}
