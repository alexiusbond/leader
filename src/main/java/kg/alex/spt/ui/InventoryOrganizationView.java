package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.*;
import kg.alex.spt.domain.InventoryInvoice;
import kg.alex.spt.domain.StockMovement;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.FormattedFilterTable;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class InventoryOrganizationView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(InventoryOrganizationView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn, printBtn, addBtn;
    private PopupButton searchBtn;
    private ComboBoxMax blockSelect, floorSelect, roomSelect;
    private FormattedTable movementsTable;
    private TextField invoiceNumberTF;
    private TextArea noteTF;
    private DateField dateDF;
    private boolean isNew;

    private String[] NATURAL_COL_ORDER, NATURAL_COL_ORDER_MOVEMENTS;
    private GridLayout settingsLay, rightLay;
    private Subject currentUser = SecurityUtils.getSubject();
    private FormattedFilterTable invoicesTable;
    private IndexedContainer movementsCont;
    private int r_table_counter = 1000;
    private ArrayList<String> delMovementIds = new ArrayList<>();
    private int invID;

    public InventoryOrganizationView(MyVaadinUI myUI) {
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

        movementsTable = new FormattedTable();
        movementsTable.setStyleName(ValoTheme.TABLE_COMPACT);
        movementsTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        movementsTable.setSizeFull();
        movementsTable.setFooterVisible(true);
        rightLay.addComponent(movementsTable, 0, 1, 2, 1);
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

        settingsLay = new GridLayout(2, 7);
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

        printBtn = new Button();
        printBtn.setDescription(myUI.getMessage(SptMessages.Print));
        printBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        printBtn.setIcon(FontAwesome.FILE_PDF_O);
        printBtn.addClickListener(this);
        buttonsLay.addComponent(printBtn);
        settingsLay.addComponent(buttonsLay, 0, 0, 1, 0);

        invoiceNumberTF = new TextField(myUI.getMessage(SptMessages.InvoiceNumber));
        invoiceNumberTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        invoiceNumberTF.setWidth("100%");
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

        dateDF = createDateFiled(new Date(), null, myUI.getMessage(SptMessages.Date),
                Resolution.MINUTE, SystemSettings.dateTimeMinPattern);
        dateDF.addValueChangeListener(this);
        settingsLay.addComponent(dateDF, 0, 2, 1, 2);

        blockSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Block));
        blockSelect.setNullSelectionAllowed(false);
        blockSelect.setRequired(true);
        blockSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        blockSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        blockSelect.setWidth("100%");
        blockSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        blockSelect.setFilteringMode(FilteringMode.CONTAINS);
        blockSelect.addValueChangeListener(this);
        settingsLay.addComponent(blockSelect, 0, 3, 1, 3);

        floorSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Floor));
        floorSelect.setNullSelectionAllowed(false);
        floorSelect.setRequired(true);
        floorSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        floorSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        floorSelect.setWidth("100%");
        floorSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        floorSelect.setFilteringMode(FilteringMode.CONTAINS);
        floorSelect.addValueChangeListener(this);
        settingsLay.addComponent(floorSelect, 0, 4, 1, 4);
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            blockSelect.setContainerDataSource(dbDef.exec_for_select(myUI,
                    SystemSettings.dbBlock, myUI.getUser().getSchool_id(), false));
            floorSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, SystemSettings.dbFloor, false));
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
        roomSelect.setWidth("100%");
        roomSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        roomSelect.setFilteringMode(FilteringMode.CONTAINS);
        roomSelect.addValueChangeListener(this);
        settingsLay.addComponent(roomSelect, 0, 5, 1, 5);

        noteTF = new TextArea(myUI.getMessage(SptMessages.Note));
        noteTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        noteTF.setWidth("100%");
        noteTF.setRows(3);
        settingsLay.addComponent(noteTF, 0, 6, 1, 6);
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
            invoicesTable.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool_id(), 2));
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
            invoiceNumberTF.focus();
        } else if (source == printBtn) {
            /*
            StockInvoice stInv = getInvoice(invID);
            stInv.setStock(roomSelect.getItemCaption(roomSelect.getValue()));
            stInv.setAcc_category(productCategorySelect.getItemCaption(productCategorySelect.getValue()));
            stInv.setFrom_employee(fromEmployeeSelect.getItemCaption(fromEmployeeSelect.getValue()));
            stInv.setTo_employee(toEmployeeSelect.getItemCaption(toEmployeeSelect.getValue()));
            try {
                DbSchool dbsc = new DbSchool();
                dbsc.connect();
                StudentInfoPdf st = dbsc.execGetSchoolPdf(myUI.getUser().getSchool_id());
                dbsc.close();
                if (st.getScl_accountent_fullname() != null) {
                    if (st.getScl_address() != null && st.getScl_phone() != null
                            && st.getScl_name_ru() != null) {
                        new StockMovementsPdf(myUI, myUI.getMessage(SptMessages.StockIncome), stInv, movementsCont, st,
                                movementsTable.getColumnFooter(myUI.getMessage(SptMessages.Amount)));
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
             */
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
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
                if (validate(settingsLay) && validateTable(movementsTable, false)) {
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
                                insertMovements(id);
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
                                insertMovements(invID);
                                updateDatacontainer();
                                setMovementsTable();
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
                setMovementsTable();
            }
            delMovementIds.clear();
            prepareNormalMode();
        } else if (source == addBtn) {
            if (blockSelect.getValue() == null || floorSelect.getValue() == null
                    || roomSelect.getValue() == null) {
                Notification.show(myUI.getMessage(SptMessages.RequiredField),
                        Notification.Type.WARNING_MESSAGE);
            } else {
                addMovementsItem();
            }
        } else if (source.getId() != null && source.getId().equals(SystemSettings.dbInventoryMovement)) {
            delMovementIds.add(source.getData().toString());
            movementsTable.removeItem(event.getButton().getData().toString());
            repaintMovementsFooter();
            repaintCodes();
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
                setMovementsTable();
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
                    if (invoicesTable.getValue() != null) {
                        roomSelect.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                                SystemSettings.room_id).getValue());
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
                repaintCodes();
            } else {
                rightLay.setEnabled(false);
            }
        } else if (property == invoiceNumberTF) {
            invoiceNumberTF.removeValueChangeListener(this);
            Iterator iter = invoicesTable.getItemIds().iterator();
            boolean isFound = false;
            Object next = null;
            while (iter.hasNext()) {
                next = iter.next();
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
        } else {
            Object changedItemId = ((AbstractField) property).getId();
            if (((AbstractField) property).getData() != null && (((AbstractField) property).getData().equals(myUI.getMessage(SptMessages.Quantity))
                    || ((AbstractField) property).getData().equals(myUI.getMessage(SptMessages.Price)))) {
                TextField quantityTF = (TextField) movementsTable.getContainerProperty(changedItemId,
                        myUI.getMessage(SptMessages.Quantity)).getValue();
                TextField priceTF = (TextField) movementsTable.getContainerProperty(changedItemId,
                        myUI.getMessage(SptMessages.Price)).getValue();
                if (priceTF != null && priceTF.getPropertyDataSource().getValue() != null
                        && quantityTF != null && quantityTF.getPropertyDataSource().getValue() != null) {
                    movementsTable.getContainerProperty(changedItemId, myUI.getMessage(SptMessages.Amount)).setValue(
                            (Integer) quantityTF.getPropertyDataSource().getValue()
                                    * (Double) priceTF.getPropertyDataSource().getValue());
                    repaintMovementsFooter();
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
        dateDF.setEnabled(true);
        noteTF.setEnabled(true);
        if (roomSelect.getValue() != null) {
            rightLay.setEnabled(true);
        }
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(SystemSettings.cnStockIncomeView + ":" + SystemSettings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(SystemSettings.cnStockIncomeView + ":" + SystemSettings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(SystemSettings.cnStockIncomeView + ":" + SystemSettings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        invoiceNumberTF.setEnabled(true);
        searchBtn.setEnabled(true);
        roomSelect.setEnabled(false);
        blockSelect.setEnabled(false);
        floorSelect.setEnabled(false);
        dateDF.setEnabled(false);
        noteTF.setEnabled(false);
        rightLay.setEnabled(false);
    }

    private void fillFields() {
        invoiceNumberTF.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                myUI.getMessage(SptMessages.InvoiceNumber)).getValue().toString());
        try {
            dateDF.setValue(SystemSettings.dtmf.parse(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                    myUI.getMessage(SptMessages.Date)).getValue().toString()));
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        blockSelect.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                SystemSettings.block_id).getValue());
        floorSelect.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                SystemSettings.floor_id).getValue());
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
        movementsTable.removeAllItems();
        setMovementsFooter(null, null);
    }

    private void updateDatacontainer() {
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Date)).setValue(
                SystemSettings.dtmf.format(dateDF.getValue()));
        try {
            invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Quantity)).setValue(
                    Integer.parseInt(movementsTable.getColumnFooter(myUI.getMessage(SptMessages.Quantity))));
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Note)).setValue(
                noteTF.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Room)).setValue(
                roomSelect.getContainerProperty(roomSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), SystemSettings.room_id).setValue(
                roomSelect.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Block)).setValue(
                blockSelect.getContainerProperty(blockSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), SystemSettings.block_id).setValue(
                blockSelect.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Floor)).setValue(
                floorSelect.getContainerProperty(floorSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), SystemSettings.floor_id).setValue(
                floorSelect.getValue());
    }

    private void addDatacontainerItem(int id) {
        Item item = ((IndexedContainer) invoicesTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                SystemSettings.dtmf.format(dateDF.getValue()));
        try {
            item.getItemProperty(myUI.getMessage(SptMessages.Quantity)).setValue(
                    Integer.parseInt(movementsTable.getColumnFooter(myUI.getMessage(SptMessages.Quantity))));
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                noteTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Room)).setValue(
                roomSelect.getContainerProperty(roomSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        item.getItemProperty(SystemSettings.room_id).setValue(roomSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Block)).setValue(
                blockSelect.getContainerProperty(blockSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        item.getItemProperty(SystemSettings.block_id).setValue(blockSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Floor)).setValue(
                floorSelect.getContainerProperty(floorSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue());
        item.getItemProperty(SystemSettings.floor_id).setValue(
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
        movementsTable.setValue(id);
    }

    private InventoryInvoice getInvoice(int inv_id) {
        InventoryInvoice inv = new InventoryInvoice();
        if (inv_id == 0) {
            try {
                DbInventoryInvoice dbCon = new DbInventoryInvoice();
                dbCon.connect();
                inv.setInvoice_number(dbCon.execSQL_max_invoice_number(myUI.getUser().getSchool_id(), 2) + 1);
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
        inv.setActivity_status_id(2);
        inv.setSchool_id(myUI.getUser().getSchool_id());
        inv.setEmployee_id(myUI.getUser().getId());
        inv.setId(inv_id);
        return inv;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            DbStockMovements dbCon = new DbStockMovements();
            dbCon.connect();
            int st = dbCon.exec_delete((Integer) invoicesTable.getValue());
            if (st != 0) {
                movementsTable.removeAllItems();
                st = dbDef.exec_delete((Integer) invoicesTable.getValue(), SystemSettings.db_dp_invoice);
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
                    invoicesTable.getContainerProperty(next, SystemSettings.room_id).getValue())) {
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

    public ComboBoxMax createCombobox(int value, String description, String dbtable, boolean isRequired, boolean isEnabled) {
        ComboBoxMax cb = new ComboBoxMax();
        if (isEnabled) {
            cb.setDescription(description);
        } else {
            cb.setDescription(myUI.getMessage(SptMessages.CanNotModify));
        }
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth("100%");
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
        btn.setWidth("100%");
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
        tf.setWidth("100%");
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
        df.setWidth("100%");
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
        tf.setWidth("100%");
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

    public IndexedContainer prepareMovementsContainer() {
        if (movementsCont == null) {
            movementsCont = new IndexedContainer();
            movementsCont.addContainerProperty(SystemSettings.button, Button.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Remain), Integer.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Code), String.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Category), ComboBoxMax.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Brand), ComboBoxMax.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Title), ComboBoxMax.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Quantity), TextField.class, null);
            movementsCont.addContainerProperty(SystemSettings.quantity_id, Integer.class, 0);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Price), TextField.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Amount), Double.class, 0.0);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.PurchaseYear), DateField.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.LifeTime), TextField.class, null);
            movementsCont.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            movementsCont.removeAllItems();
        }
        return movementsCont;
    }

    private void addMovementsItem() {
        NATURAL_COL_ORDER_MOVEMENTS = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.Remain),
                myUI.getMessage(SptMessages.Code),
                myUI.getMessage(SptMessages.Category),
                myUI.getMessage(SptMessages.Brand),
                myUI.getMessage(SptMessages.Title),
                myUI.getMessage(SptMessages.Quantity),
                myUI.getMessage(SptMessages.Price),
                myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.PurchaseYear),
                myUI.getMessage(SptMessages.LifeTime)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (movementsTable.getContainerDataSource().size() == 0) {
            movementsTable.setContainerDataSource(prepareMovementsContainer());
        }
        Item item;
        item = ((IndexedContainer) movementsTable.getContainerDataSource()).addItemAt(
                movementsTable.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbInventoryMovement, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Category)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.Category),
                        SystemSettings.dbInventoryCategoryTable, true, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Code)).setValue(
                blockSelect.getItemCaption(blockSelect.getValue()) + "-" +
                        floorSelect.getItemCaption(floorSelect.getValue()) + "-" +
                        roomSelect.getContainerProperty(roomSelect.getValue(),
                                myUI.getMessage(SptMessages.Room)).getValue() + "-" +
                        movementsTable.size());
        item.getItemProperty(myUI.getMessage(SptMessages.Brand)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.Brand),
                        SystemSettings.dbInventoryBrandTable, true, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.Title),
                        SystemSettings.dbInventoryTitleTable, true, true));
        item.getItemProperty(myUI.getMessage(SptMessages.PurchaseYear)).setValue(
                createDateFiled(new Date(), myUI.getMessage(SptMessages.PurchaseYear),
                        null, Resolution.YEAR, SystemSettings.yearPattern));
        item.getItemProperty(myUI.getMessage(SptMessages.LifeTime)).setValue(
                createTextfieldWithProperty(null, myUI.getMessage(SptMessages.LifeTime),
                        new IntegerRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 1, null),
                        new ObjectProperty<Integer>(0), SystemSettings.getStringToIntegerConverter(), true));
        TextField tf = createTextfieldWithProperty(
                1, myUI.getMessage(SptMessages.Quantity),
                new IntegerRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 1, null),
                new ObjectProperty<Integer>(0), SystemSettings.getStringToIntegerConverter(), true);
        tf.addValueChangeListener(this);
        tf.setId(id);
        tf.setData(myUI.getMessage(SptMessages.Quantity));
        item.getItemProperty(myUI.getMessage(SptMessages.Quantity)).setValue(tf);
        tf = createTextfieldWithProperty(null, myUI.getMessage(SptMessages.Price),
                new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                new ObjectProperty<Double>(0.0), SystemSettings.getStringToDoubleConverter(), true);
        tf.addValueChangeListener(this);
        tf.setId(id);
        tf.setData(myUI.getMessage(SptMessages.Price));
        item.getItemProperty(myUI.getMessage(SptMessages.Price)).setValue(tf);
        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        movementsTable.setVisibleColumns(NATURAL_COL_ORDER_MOVEMENTS);
        movementsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Title), 1);
        movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
        movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Remain), Table.Align.RIGHT);
        movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Quantity), Table.Align.RIGHT);
        movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Price), Table.Align.RIGHT);
    }

    private void repaintCodes() {
        Iterator iter = movementsTable.getContainerDataSource().getItemIds().iterator();
        int counter = 0;
        while (iter.hasNext()) {
            Object next = iter.next();
            movementsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Code)).setValue(
                    blockSelect.getItemCaption(blockSelect.getValue()) + "-" +
                            floorSelect.getItemCaption(floorSelect.getValue()) + "-" +
                            roomSelect.getContainerProperty(roomSelect.getValue(),
                                    myUI.getMessage(SptMessages.Room)).getValue() + "-" +
                            (++counter));
        }
    }

    private void setMovementsTable() {
        try {
            NATURAL_COL_ORDER_MOVEMENTS = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.Remain),
                    myUI.getMessage(SptMessages.Code),
                    myUI.getMessage(SptMessages.Category),
                    myUI.getMessage(SptMessages.Brand),
                    myUI.getMessage(SptMessages.Title),
                    myUI.getMessage(SptMessages.Quantity),
                    myUI.getMessage(SptMessages.Price),
                    myUI.getMessage(SptMessages.Amount),
                    myUI.getMessage(SptMessages.PurchaseYear),
                    myUI.getMessage(SptMessages.LifeTime)};
            DbInventoryMovements dbCon = new DbInventoryMovements();
            dbCon.connect();
            movementsTable.setContainerDataSource(dbCon.execSQL(myUI, invID, this));
            dbCon.close();
            movementsTable.setVisibleColumns(NATURAL_COL_ORDER_MOVEMENTS);
            movementsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Title), 1);
            movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
            movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Remain), Table.Align.RIGHT);
            movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Quantity), Table.Align.RIGHT);
            movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Price), Table.Align.RIGHT);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void repaintMovementsFooter() {
        double totalAmount = 0.0;
        int totalQuantity = 0;
        if (movementsTable.getContainerDataSource().size() > 0) {
            Iterator iter = movementsTable.getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                totalAmount += (Double) movementsTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue();
                totalQuantity += (Integer) ((TextField) movementsTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue();
            }
        }
        setMovementsFooter(totalAmount, totalQuantity);
    }

    private void insertMovements(int invoice_id) {
        try {
            DbStockMovements dbCon = new DbStockMovements();
            dbCon.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delMovementIds.size() > 0) {
                for (int i = 0; i < delMovementIds.size(); i++) {
                    dbd.exec_delete(delMovementIds.get(i), SystemSettings.dbStockMovement);
                }
            }
            if (movementsTable.getContainerDataSource().size() > 0) {
                Iterator iter = movementsTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    StockMovement smv = new StockMovement();
                    smv.setInvoice_id(invoice_id);
                    smv.setPrice((Double) ((TextField) movementsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Price)).getValue()).getPropertyDataSource().getValue());
                    smv.setRate((Double) movementsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Rate)).getValue());
                    smv.setQuantity((Double) ((TextField) movementsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue());
                    smv.setNote(((TextField) movementsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Note)).getValue()).getValue().toString());
                    smv.setAcc_category_id((Integer) ((ComboBoxMax) movementsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Product)).getValue()).getValue());
                    ComboBoxMax cb = (ComboBoxMax) movementsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Measurement)).getValue();
                    smv.setMeasurement_id((Integer) cb.getValue());

                    if (movementsTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        smv.setId(Integer.parseInt(next.toString()));
                        dbCon.exec_update(smv);
                    } else if (movementsTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbCon.exec_insert(smv);
                    }
                }
            }
            delMovementIds.clear();
            dbCon.close();
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    public void setMovementsFooter(Double amount, Integer quantity) {
        if (amount != null) {
            movementsTable.setColumnFooter(myUI.getMessage(SptMessages.Amount), SystemSettings.dFormat.format(amount));
        } else {
            movementsTable.setColumnFooter(myUI.getMessage(SptMessages.Amount), null);
        }
        if (quantity != null) {
            movementsTable.setColumnFooter(myUI.getMessage(SptMessages.Quantity), quantity + "");
        } else {
            movementsTable.setColumnFooter(myUI.getMessage(SptMessages.Quantity), null);
        }
    }

    public Component getNewObj() {
        return new InventoryOrganizationView(myUI);
    }
}
