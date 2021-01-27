package kg.alex.spt.ui;

import com.vaadin.ui.*;
import kg.alex.spt.utils.ComboBoxMax;
import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbAccTransactions;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbEmployee;
import kg.alex.spt.dao.DbProductCategories;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.dao.DbStockInvoice;
import kg.alex.spt.dao.DbStockMovements;
import kg.alex.spt.domain.AccTransaction;
import kg.alex.spt.domain.StockInvoice;
import kg.alex.spt.domain.StockMovement;
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.FormattedFilterTable;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import kg.alex.spt.utils.StockMovementsPdf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;

public class StockIncomeView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(StockIncomeView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn, printBtn, addBtn;
    private PopupButton searchBtn;
    private ComboBoxMax stockSelect, fromEmployeeSelect, toEmployeeSelect, productCategorySelect;
    private FormattedTable movementsTable;
    private TextField invoiceNumberTF;
    private TextArea noteTF;
    private DateField dateDF;
    private boolean isNew;
    private SystemSettings sysSettings = new SystemSettings();
    private String[] NATURAL_COL_ORDER, NATURAL_COL_ORDER_MOVEMENTS;
    private GridLayout settingsLay, rightLay;
    private Subject currentUser = SecurityUtils.getSubject();
    private FormattedFilterTable invoicesTable;
    private IndexedContainer movementsCont;
    private int r_table_counter = 1000;
    private ArrayList<String> delMovementIds = new ArrayList<>();
    private int invID;
    private IndexedContainer originalCont;

    public StockIncomeView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.InvoiceNumber),
                myUI.getMessage(SptMessages.Stock),
                myUI.getMessage(SptMessages.Date), myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.FromEmployee), myUI.getMessage(SptMessages.ToEmployee),
                myUI.getMessage(SptMessages.Note)};

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
        this.setSplitPosition(24, Sizeable.Unit.PERCENTAGE);
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

        dateDF = new DateField(myUI.getMessage(SptMessages.Date));
        dateDF.setResolution(Resolution.MINUTE);
        dateDF.setWidth("100%");
        dateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        dateDF.setRequired(true);
        dateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        dateDF.setDateFormat(sysSettings.dateTimeMinPattern);
        dateDF.setValue(new Date());
        dateDF.addValueChangeListener(this);
        settingsLay.addComponent(dateDF, 0, 2, 1, 2);

        stockSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Stock));
        stockSelect.setNullSelectionAllowed(false);
        stockSelect.setRequired(true);
        stockSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        stockSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        stockSelect.setWidth("100%");
        stockSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        stockSelect.setFilteringMode(FilteringMode.CONTAINS);
        stockSelect.addValueChangeListener(this);
        settingsLay.addComponent(stockSelect, 0, 3, 1, 3);

        productCategorySelect = new ComboBoxMax(myUI.getMessage(SptMessages.ProductCategory));
        productCategorySelect.setNullSelectionAllowed(false);
        productCategorySelect.setRequired(true);
        productCategorySelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        productCategorySelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        productCategorySelect.setWidth("100%");
        productCategorySelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        productCategorySelect.setFilteringMode(FilteringMode.CONTAINS);
        productCategorySelect.addValueChangeListener(this);
        settingsLay.addComponent(productCategorySelect, 0, 4, 1, 4);

        fromEmployeeSelect = new ComboBoxMax(myUI.getMessage(SptMessages.FromEmployee));
        fromEmployeeSelect.setNullSelectionAllowed(false);
        fromEmployeeSelect.setRequired(true);
        fromEmployeeSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        fromEmployeeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        fromEmployeeSelect.setWidth("100%");
        fromEmployeeSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        fromEmployeeSelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(fromEmployeeSelect, 0, 5, 1, 5);

        toEmployeeSelect = new ComboBoxMax(myUI.getMessage(SptMessages.ToEmployee));
        toEmployeeSelect.setNullSelectionAllowed(false);
        toEmployeeSelect.setRequired(true);
        toEmployeeSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        toEmployeeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        toEmployeeSelect.setWidth("100%");
        toEmployeeSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        toEmployeeSelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(toEmployeeSelect, 0, 6, 1, 6);

        try {
            DbEmployee dbCon = new DbEmployee();
            dbCon.connect();
            fromEmployeeSelect.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool_id(), 17));
            if (fromEmployeeSelect.getContainerDataSource() != null) {
                fromEmployeeSelect.setValue(((IndexedContainer) fromEmployeeSelect.getContainerDataSource()).firstItemId());
            }
            toEmployeeSelect.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool_id(), 100));
            if (toEmployeeSelect.getContainerDataSource() != null) {
                toEmployeeSelect.setValue(((IndexedContainer) toEmployeeSelect.getContainerDataSource()).firstItemId());
            }

            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        noteTF = new TextArea(myUI.getMessage(SptMessages.Note));
        noteTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        noteTF.setWidth("100%");
        noteTF.setRows(3);
        settingsLay.addComponent(noteTF, 0, 7, 1, 7);
        settingsLay.setColumnExpandRatio(0, 1);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            stockSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, sysSettings.dbStock, myUI.getUser().getSchool_id()));
            dbDef.close();
            DbProductCategories dbpc = new DbProductCategories();
            dbpc.connect();
            productCategorySelect.setContainerDataSource(dbpc.execSQL_for_select(myUI));
            dbpc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
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
            DbStockInvoice dbCon = new DbStockInvoice();
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
        invoicesTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), CustomTable.Align.RIGHT);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == modifyBtn && invoicesTable.getValue() != null) {
            isNew = false;
            fillFields();
            prepareModificationMode();
            invoiceNumberTF.focus();
            stockSelect.setEnabled(false);
            productCategorySelect.setEnabled(false);
            dateDF.setEnabled(false);
        } else if (source == printBtn) {
            StockInvoice stInv = getInvoice(invID);
            stInv.setStock(stockSelect.getItemCaption(stockSelect.getValue()));
            stInv.setAcc_category(productCategorySelect.getItemCaption(productCategorySelect.getValue()));
            stInv.setFrom_employee(fromEmployeeSelect.getItemCaption(fromEmployeeSelect.getValue()));
            stInv.setTo_employee(toEmployeeSelect.getItemCaption(toEmployeeSelect.getValue()));
            try {
                DbSchool dbsc = new DbSchool();
                dbsc.connect();
                StudInfoPdf st = dbsc.execGetSchoolPdf(myUI.getUser().getSchool_id());
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
                                Iterator iter = movementsTable.getItemIds().iterator();
                                boolean isDeletePossible = true;
                                while (iter.hasNext()) {
                                    Object next = iter.next();
                                    if (!(Boolean) movementsTable.getContainerProperty(next, sysSettings.is_modifiable).getValue()) {
                                        isDeletePossible = false;
                                        break;
                                    }
                                }
                                if (isDeletePossible) {
                                    execDelete();
                                } else {
                                    Notification.show(myUI.getMessage(SptMessages.CanNotDelete),
                                            Notification.Type.WARNING_MESSAGE);
                                }
                            }
                        }
                    });
        } else if (source == saveBtn) {
            try {
                if (validate(settingsLay) && validateTable(movementsTable, false)) {
                    DbStockInvoice dbCon = new DbStockInvoice();
                    dbCon.connect();
                    AccTransaction at = new AccTransaction();
                    try {
                        at.setAmount(sysSettings.dFormat.parse(movementsTable.getColumnFooter(
                                myUI.getMessage(SptMessages.Amount))).doubleValue());
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    at.setCurrency_rate(myUI.getDb_currency_rate());
                    at.setCurrency_id(1);
                    at.setEmployee_id(myUI.getUser().getId());
                    at.setSchool_id(myUI.getUser().getSchool_id());
                    /*DbAccTransactions dbat = new DbAccTransactions();
                    dbat.connect();*/
                    if (isNew) {
                        StockInvoice inv = getInvoice(0);
                        int id = dbCon.exec_insert(inv);
                        at.setCategory_id((inv.getAcc_category_id()));
                        at.setDate(inv.getCreation_date());
                        at.setFrom_to_employee_id(inv.getTo_employee_id());
                        at.setNote(myUI.getMessage(SptMessages.InvoiceNumber) + ": " + String.format("%07d", inv.getInvoice_number()));
                        at.setDp_invoice_id(id);
                        if (id != 0) {
                            insertMovements(id);
                            addDatacontainerItem(id);
                            invoicesTable.setValue(id);
                            /*dbat.exec_insert(at, dbat.getConnection());*/
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved), Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        int status = 0;
                        StockInvoice inv = getInvoice(invID);
                        at.setCategory_id((inv.getAcc_category_id()));
                        at.setDate(inv.getCreation_date());
                        at.setFrom_to_employee_id(inv.getTo_employee_id());
                        at.setNote(myUI.getMessage(SptMessages.InvoiceNumber) + ": " + String.format("%07d", inv.getInvoice_number()));
                        at.setDp_invoice_id(invID);
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
                            /*dbat.exec_update(at, sysSettings.db_dp_invoice_id, invID, dbat.getConnection());*/
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    }
                    /*dbat.close();*/
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
            if (invoicesTable.getValue() != null) {
                fillFields();
                setMovementsTable();
            }
            delMovementIds.clear();
            prepareNormalMode();
        } else if (source == addBtn) {
            if (myUI.getDb_currency_rate() != 0.0) {
                addMovementsItem();
                stockSelect.setEnabled(false);
                productCategorySelect.setEnabled(false);
                dateDF.setEnabled(false);
            } else {
                Notification.show(myUI.getMessage(SptMessages.CanNotGetCurrency),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source.getId() != null && source.getId().equals(sysSettings.dbStockMovement)) {
            delMovementIds.add(source.getData().toString());

            if (((ComboBoxMax) movementsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Product)).getValue()).getValue() != null
                    && ((ComboBoxMax) movementsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Measurement)).getValue()).getValue() != null
                    && ((TextField) movementsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue() != null
                    && ((TextField) movementsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Price)).getValue()).getPropertyDataSource().getValue() != null) {
                double quantity = (Double) ((TextField) movementsTable.getContainerProperty(source.getData(),
                        myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue();
                int acc_category_id = (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(source.getData(),
                        myUI.getMessage(SptMessages.Product)).getValue()).getValue();
                int measurement_id = (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(source.getData(),
                        myUI.getMessage(SptMessages.Measurement)).getValue()).getValue();
                recalculateRemaindersAfterDelete(acc_category_id, measurement_id, quantity);
            }
            movementsTable.removeItem(event.getButton().getData().toString());
            if (movementsTable.size() == 0) {
                stockSelect.setEnabled(true);
                productCategorySelect.setEnabled(true);
                dateDF.setEnabled(true);
            }
            repaintMovementsFooter();
            if (!disableFields(movementsTable)) {
                enableFields(movementsTable);
            }
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
        } else if (property == stockSelect || property == dateDF || property == productCategorySelect) {
            if (stockSelect.getValue() != null && dateDF.getValue() != null && productCategorySelect.getValue() != null
                    && !rightLay.isEnabled() && stockSelect.isEnabled()) {
                rightLay.setEnabled(true);
            } else if (dateDF.getValue() == null && rightLay.isEnabled()) {
                rightLay.setEnabled(false);
            } else if (productCategorySelect.getValue() == null && rightLay.isEnabled()) {
                rightLay.setEnabled(false);
            }
        } else if (property == invoiceNumberTF) {
            invoiceNumberTF.removeValueChangeListener(this);
            Iterator iter = invoicesTable.getItemIds().iterator();
            boolean isFound = false;
            Object next = null;
            while (iter.hasNext()) {
                next = iter.next();
                if (invoicesTable.getContainerProperty(next, myUI.getMessage(SptMessages.InvoiceNumber)).getValue().equals(property.getValue())) {
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
                            (Double) quantityTF.getPropertyDataSource().getValue()
                                    * (Double) priceTF.getPropertyDataSource().getValue());
                    repaintMovementsFooter();
                }
            }

            if (((ComboBoxMax) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(SptMessages.Product)).getValue()).getValue() != null
                    && ((ComboBoxMax) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(SptMessages.Measurement)).getValue()).getValue() != null
                    && ((TextField) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue() != null) {
                ComboBoxMax catCB = (ComboBoxMax) movementsTable.getContainerProperty(changedItemId,
                        myUI.getMessage(SptMessages.Product)).getValue();
                catCB.removeAllValidators();
                int acc_category_id = (Integer) movementsTable.getContainerProperty(changedItemId, sysSettings.acc_category_id).getValue();
                int measurement_id = (Integer) movementsTable.getContainerProperty(changedItemId, sysSettings.measurement_id).getValue();
                if ((int) catCB.getValue() == (int) movementsTable.getContainerProperty(changedItemId, sysSettings.acc_category_id).getValue()
                        && (int) ((ComboBoxMax) movementsTable.getContainerProperty(changedItemId,
                        myUI.getMessage(SptMessages.Measurement)).getValue()).getValue()
                        == (int) movementsTable.getContainerProperty(changedItemId, sysSettings.measurement_id).getValue()) {
                    double quantity = (Double) movementsTable.getContainerProperty(changedItemId, sysSettings.quantity_id).getValue();
                    recalculateRemaindersAfterDelete(acc_category_id, measurement_id, quantity);
                } else if ((Integer) movementsTable.getContainerProperty(changedItemId, sysSettings.acc_category_id).getValue() != 0) {
                    double quantity = (Double) ((TextField) movementsTable.getContainerProperty(changedItemId,
                            myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue();
                    recalculateRemaindersAfterDelete(acc_category_id, measurement_id, quantity);
                }
                recalculateRemaindersAfterInsert(changedItemId);
                updateOldValues(changedItemId);
                try {
                    DbStockMovements dbCon = new DbStockMovements();
                    dbCon.connect();
                    if (!dbCon.execSQL_allowNewInsert((Integer) catCB.getValue(),
                            (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(changedItemId,
                                    myUI.getMessage(SptMessages.Measurement)).getValue()).getValue(),
                            (Integer) stockSelect.getValue(), dateDF.getValue())) {
                        catCB.addValidator(new Validator() {
                            @Override
                            public void validate(Object value) throws Validator.InvalidValueException {
                                throw new Validator.InvalidValueException(myUI.getMessage(SptMessages.NotifWrongValue));
                            }
                        });
                    }
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        }
    }

    private void updateOldValues(Object changedItemId) {
        int acc_category_id = (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(changedItemId,
                myUI.getMessage(SptMessages.Product)).getValue()).getValue();
        int measurement_id = (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(changedItemId,
                myUI.getMessage(SptMessages.Measurement)).getValue()).getValue();
        double quantity = (Double) ((TextField) movementsTable.getContainerProperty(changedItemId,
                myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue();

        movementsTable.getContainerProperty(changedItemId, sysSettings.acc_category_id).setValue(acc_category_id);
        movementsTable.getContainerProperty(changedItemId, sysSettings.measurement_id).setValue(measurement_id);
        movementsTable.getContainerProperty(changedItemId, sysSettings.quantity_id).setValue(quantity);
    }

    private void recalculateRemaindersAfterDelete(int acc_category_id, int measurement_id, double quantity) {
        Iterator iter = movementsTable.getItemIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            if (((ComboBoxMax) movementsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Product)).getValue()).getValue() != null
                    && ((ComboBoxMax) movementsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Measurement)).getValue()).getValue() != null
                    && ((TextField) movementsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue() != null) {
                if (acc_category_id == (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(
                        next, myUI.getMessage(SptMessages.Product)).getValue()).getValue()
                        && measurement_id == (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(
                        next, myUI.getMessage(SptMessages.Measurement)).getValue()).getValue()) {
                    double remain = 0;
                    if (movementsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Remain)).getValue() != null) {
                        remain = (Double) movementsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Remain)).getValue();
                    }
                    movementsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Remain)).setValue(remain - quantity);
                }
            }
        }
        if (!disableFields(movementsTable)) {
            enableFields(movementsTable);
        }
    }

    private void recalculateRemaindersAfterInsert(Object changedItemId) {
        int acc_category_id = (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(changedItemId,
                myUI.getMessage(SptMessages.Product)).getValue()).getValue();
        int measurement_id = (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(changedItemId,
                myUI.getMessage(SptMessages.Measurement)).getValue()).getValue();
        double curr_remainder = 0.0;
        boolean isExistsSame = false;
        Iterator iter = movementsTable.getItemIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            if (((ComboBoxMax) movementsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Product)).getValue()).getValue() != null
                    && ((ComboBoxMax) movementsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Measurement)).getValue()).getValue() != null
                    && ((TextField) movementsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue() != null) {
                if (!next.equals(changedItemId)
                        && acc_category_id == (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(
                        next, myUI.getMessage(SptMessages.Product)).getValue()).getValue()
                        && measurement_id == (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(
                        next, myUI.getMessage(SptMessages.Measurement)).getValue()).getValue()) {
                    curr_remainder = (Double) movementsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Remain)).getValue()
                            + (Double) ((TextField) movementsTable.getContainerProperty(changedItemId,
                            myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue();
                    isExistsSame = true;
                    break;
                }
            }
        }
        if (curr_remainder == 0.00) {
            double old_value = 0.0;
            if (getOriginalCont() != null) {
                iter = getOriginalCont().getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    if (acc_category_id == (Integer) getOriginalCont().getContainerProperty(next, sysSettings.acc_category_id).getValue()
                            && measurement_id == (Integer) getOriginalCont().getContainerProperty(next, sysSettings.measurement_id).getValue()) {
                        old_value += (Double) getOriginalCont().getContainerProperty(next, sysSettings.quantity_id).getValue();
                    }
                }
            }
            try {
                DbStockMovements dbCon = new DbStockMovements();
                dbCon.connect();
                curr_remainder = dbCon.execSQL_remain(acc_category_id, measurement_id, (Integer) stockSelect.getValue())
                        + (Double) ((TextField) movementsTable.getContainerProperty(changedItemId,
                        myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue() - old_value;
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        if (isExistsSame) {
            iter = movementsTable.getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (((ComboBoxMax) movementsTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Product)).getValue()).getValue() != null
                        && ((ComboBoxMax) movementsTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Measurement)).getValue()).getValue() != null
                        && ((TextField) movementsTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Quantity)).getValue()).getPropertyDataSource().getValue() != null) {
                    if (acc_category_id == (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Product)).getValue()).getValue()
                            && measurement_id == (Integer) ((ComboBoxMax) movementsTable.getContainerProperty(
                            next, myUI.getMessage(SptMessages.Measurement)).getValue()).getValue()) {
                        movementsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Remain)).setValue(curr_remainder);
                    }
                }
            }
        } else {
            movementsTable.getContainerProperty(changedItemId, myUI.getMessage(SptMessages.Remain)).setValue(curr_remainder);
        }
        if (!disableFields(movementsTable)) {
            enableFields(movementsTable);
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
        stockSelect.setEnabled(true);
        productCategorySelect.setEnabled(true);
        fromEmployeeSelect.setEnabled(true);
        dateDF.setEnabled(true);
        toEmployeeSelect.setEnabled(true);
        noteTF.setEnabled(true);
        if (stockSelect.getValue() != null && dateDF.getValue() != null && productCategorySelect.getValue() != null) {
            rightLay.setEnabled(true);
        }
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(sysSettings.cnStockIncomeView + ":" + sysSettings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(sysSettings.cnStockIncomeView + ":" + sysSettings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(sysSettings.cnStockIncomeView + ":" + sysSettings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        invoiceNumberTF.setEnabled(true);
        searchBtn.setEnabled(true);
        stockSelect.setEnabled(false);
        productCategorySelect.setEnabled(false);
        fromEmployeeSelect.setEnabled(false);
        dateDF.setEnabled(false);
        toEmployeeSelect.setEnabled(false);
        noteTF.setEnabled(false);
        rightLay.setEnabled(false);
    }

    private void fillFields() {
        invoiceNumberTF.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                myUI.getMessage(SptMessages.InvoiceNumber)).getValue().toString());
        try {
            dateDF.setValue(sysSettings.dtmf.parse(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                    myUI.getMessage(SptMessages.Date)).getValue().toString()));
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        stockSelect.setValue((Integer) invoicesTable.getContainerProperty(invoicesTable.getValue(),
                sysSettings.stock_id).getValue());
        productCategorySelect.setValue((Integer) invoicesTable.getContainerProperty(invoicesTable.getValue(),
                sysSettings.acc_category_id).getValue());
        fromEmployeeSelect.setValue((Integer) invoicesTable.getContainerProperty(invoicesTable.getValue(),
                sysSettings.from_employee_id).getValue());
        toEmployeeSelect.setValue((Integer) invoicesTable.getContainerProperty(invoicesTable.getValue(),
                sysSettings.to_employee_id).getValue());
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
        stockSelect.setValue(null);
        productCategorySelect.setValue(null);
        fromEmployeeSelect.setValue(null);
        toEmployeeSelect.setValue(null);
        noteTF.setValue("");
        movementsTable.removeAllItems();
        originalCont = null;
        movementsTable.setColumnFooter(myUI.getMessage(SptMessages.Amount), null);
    }

    private void updateDatacontainer() {
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Date)).setValue(
                sysSettings.dtmf.format(dateDF.getValue()));
        try {
            invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Amount)).setValue(
                    sysSettings.dFormat.parse(movementsTable.getColumnFooter(myUI.getMessage(SptMessages.Amount))).doubleValue());
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Note)).setValue(
                noteTF.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.Stock)).setValue(
                stockSelect.getContainerProperty(stockSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), sysSettings.stock_id).setValue(
                stockSelect.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.FromEmployee)).setValue(
                fromEmployeeSelect.getContainerProperty(fromEmployeeSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), sysSettings.from_employee_id).setValue(
                fromEmployeeSelect.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(SptMessages.ToEmployee)).setValue(
                toEmployeeSelect.getContainerProperty(toEmployeeSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), sysSettings.to_employee_id).setValue(
                toEmployeeSelect.getValue());
    }

    private void addDatacontainerItem(int id) {
        Item item = ((IndexedContainer) invoicesTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                sysSettings.dtmf.format(dateDF.getValue()));
        try {
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                    sysSettings.dFormat.parse(movementsTable.getColumnFooter(myUI.getMessage(SptMessages.Amount))).doubleValue());
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                noteTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Stock)).setValue(
                stockSelect.getContainerProperty(stockSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue());
        item.getItemProperty(sysSettings.stock_id).setValue(stockSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.FromEmployee)).setValue(
                fromEmployeeSelect.getContainerProperty(fromEmployeeSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue());
        item.getItemProperty(sysSettings.from_employee_id).setValue(fromEmployeeSelect.getValue());
        item.getItemProperty(sysSettings.acc_category_id).setValue(productCategorySelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.ToEmployee)).setValue(
                toEmployeeSelect.getContainerProperty(toEmployeeSelect.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue());
        item.getItemProperty(sysSettings.to_employee_id).setValue(
                toEmployeeSelect.getValue());
        try {
            DbStockInvoice dbCon = new DbStockInvoice();
            dbCon.connect();
            item.getItemProperty(myUI.getMessage(SptMessages.InvoiceNumber)).setValue(dbCon.execSQL_invoice_number(id));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        movementsTable.setValue(id);
    }

    private StockInvoice getInvoice(int inv_id) {
        StockInvoice inv = new StockInvoice();
        if (inv_id == 0) {
            try {
                DbStockInvoice dbCon = new DbStockInvoice();
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
        inv.setStock_id((Integer) stockSelect.getValue());
        inv.setTo_employee_id((Integer) toEmployeeSelect.getValue());
        inv.setFrom_employee_id((Integer) fromEmployeeSelect.getValue());
        inv.setAcc_category_id((Integer) productCategorySelect.getValue());
        inv.setService_type_id(1);
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
            /*DbAccTransactions dbAt = new DbAccTransactions();
            dbAt.connect();*/
            int st = dbCon.exec_delete((Integer) invoicesTable.getValue());
            if (st != 0) {
                movementsTable.removeAllItems();
                /*dbAt.exec_delete(sysSettings.db_dp_invoice_id, invoicesTable.getValue().toString(), dbAt.getConnection());*/
                st = dbDef.exec_delete((Integer) invoicesTable.getValue(), sysSettings.db_dp_invoice);
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
            /*dbAt.close();*/
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
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        if (isRequired) {
            cb.setRequired(true);
            cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        }
        try {
            if (dbtable != null) {
                DbDefinition dbp = new DbDefinition();
                dbp.connect();
                cb.setContainerDataSource(dbp.exec_for_select(myUI, dbtable));
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
        tf.setWidth("100%");
        tf.addValidator(validator);
        tf.getPropertyDataSource().setValue(value);
        tf.setEnabled(isEnabled);
        return tf;
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
            movementsCont.addContainerProperty(sysSettings.button, Button.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Product), ComboBoxMax.class, null);
            movementsCont.addContainerProperty(sysSettings.acc_category_id, Integer.class, 0);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Note), TextField.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Quantity), TextField.class, null);
            movementsCont.addContainerProperty(sysSettings.quantity_id, Double.class, 0.0);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Remain), Double.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Measurement), ComboBoxMax.class, null);
            movementsCont.addContainerProperty(sysSettings.measurement_id, Integer.class, 0);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Price), TextField.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Rate), Double.class, 0.0);
            movementsCont.addContainerProperty(myUI.getMessage(SptMessages.Amount), Double.class, 0.0);
            movementsCont.addContainerProperty(sysSettings.is_modifiable, Boolean.class, true);
            movementsCont.addContainerProperty(sysSettings.crud_status, String.class, null);
        } else {
            movementsCont.removeAllItems();
        }
        return movementsCont;
    }

    public IndexedContainer prepareOriginalContainer() {
        if (getOriginalCont() == null) {
            originalCont = new IndexedContainer();
            getOriginalCont().addContainerProperty(sysSettings.acc_category_id, Integer.class, 0);
            getOriginalCont().addContainerProperty(sysSettings.quantity_id, Double.class, 0.0);
            getOriginalCont().addContainerProperty(sysSettings.measurement_id, Integer.class, 0);
            getOriginalCont().addContainerProperty(myUI.getMessage(SptMessages.Rate), Double.class, 0.0);
        } else {
            getOriginalCont().removeAllItems();
        }
        return getOriginalCont();
    }

    private void addMovementsItem() {
        NATURAL_COL_ORDER_MOVEMENTS = new String[]{sysSettings.button,
                myUI.getMessage(SptMessages.Remain),
                myUI.getMessage(SptMessages.Product),
                myUI.getMessage(SptMessages.Measurement),
                myUI.getMessage(SptMessages.Note),
                myUI.getMessage(SptMessages.Quantity),
                myUI.getMessage(SptMessages.Price),
                myUI.getMessage(SptMessages.Rate),
                myUI.getMessage(SptMessages.Amount)};
        String id = sysSettings.FreshItem + (--r_table_counter);
        if (movementsTable.getContainerDataSource().size() == 0) {
            movementsTable.setContainerDataSource(prepareMovementsContainer());
        }
        Item item;
        item = ((IndexedContainer) movementsTable.getContainerDataSource()).addItemAt(
                movementsTable.getContainerDataSource().size(), id);
        item.getItemProperty(sysSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, sysSettings.dbStockMovement, true));
        ComboBoxMax cb = createCombobox(0, myUI.getMessage(SptMessages.Product), null, true, true);
        try {
            DbAccCategory dbCon = new DbAccCategory();
            dbCon.connect();
            cb.setContainerDataSource(dbCon.exec_for_select(myUI, productCategorySelect.getValue() + ""));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        cb.addValueChangeListener(this);
        cb.setId(id);
        item.getItemProperty(myUI.getMessage(SptMessages.Product)).setValue(cb);
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(createTextfield(
                null, myUI.getMessage(SptMessages.Note),
                new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 250, false), true));
        cb = createCombobox(0, myUI.getMessage(SptMessages.Measurement), sysSettings.dbMeasurement, true, true);
        cb.addValueChangeListener(this);
        cb.setId(id);
        item.getItemProperty(myUI.getMessage(SptMessages.Measurement)).setValue(cb);
        TextField tf = createTextfieldWithProperty(null, myUI.getMessage(SptMessages.Quantity),
                new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                new ObjectProperty<Double>(0.0), sysSettings.getStringToDoubleConverter(), true);
        tf.addValueChangeListener(this);
        tf.setId(id);
        tf.setData(myUI.getMessage(SptMessages.Quantity));
        item.getItemProperty(myUI.getMessage(SptMessages.Quantity)).setValue(tf);
        tf = createTextfieldWithProperty(null, myUI.getMessage(SptMessages.Price),
                new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                new ObjectProperty<Double>(0.0), sysSettings.getStringToDoubleConverter(), true);
        tf.addValueChangeListener(this);
        tf.setId(id);
        tf.setData(myUI.getMessage(SptMessages.Price));
        item.getItemProperty(myUI.getMessage(SptMessages.Price)).setValue(tf);
        item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(myUI.getDb_currency_rate());
        item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        movementsTable.setVisibleColumns(NATURAL_COL_ORDER_MOVEMENTS);
        movementsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Product), 1);
        movementsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Note), 1);
        movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
        movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Remain), Table.Align.RIGHT);
        movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Rate), Table.Align.RIGHT);

    }

    private void setMovementsTable() {
        System.out.println("Set movements table");
        try {
            NATURAL_COL_ORDER_MOVEMENTS = new String[]{sysSettings.button,
                    myUI.getMessage(SptMessages.Remain),
                    myUI.getMessage(SptMessages.Product),
                    myUI.getMessage(SptMessages.Measurement),
                    myUI.getMessage(SptMessages.Note),
                    myUI.getMessage(SptMessages.Quantity),
                    myUI.getMessage(SptMessages.Price),
                    myUI.getMessage(SptMessages.Rate),
                    myUI.getMessage(SptMessages.Amount)};
            DbStockMovements dbepn = new DbStockMovements();
            dbepn.connect();
            movementsTable.setContainerDataSource(dbepn.execSQL(myUI, invID, this));
            dbepn.close();
            movementsTable.setVisibleColumns(NATURAL_COL_ORDER_MOVEMENTS);
            movementsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Product), 1);
            movementsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Note), 1);
            movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
            movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Remain), Table.Align.RIGHT);
            movementsTable.setColumnAlignment(myUI.getMessage(SptMessages.Rate), Table.Align.RIGHT);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void repaintMovementsFooter() {
        double totPrice = 0.0;
        if (movementsTable.getContainerDataSource().size() > 0) {
            Iterator iter = movementsTable.getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                totPrice += (Double) movementsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue();
            }
        }
        movementsTable.setColumnFooter(myUI.getMessage(SptMessages.Amount), sysSettings.dFormat.format(totPrice));
    }

    private void insertMovements(int invoice_id) {
        try {
            DbStockMovements dbCon = new DbStockMovements();
            dbCon.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delMovementIds.size() > 0) {
                for (int i = 0; i < delMovementIds.size(); i++) {
                    dbd.exec_delete(delMovementIds.get(i), sysSettings.dbStockMovement);
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

                    if (movementsTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        smv.setId(Integer.parseInt(next.toString()));
                        dbCon.exec_update(smv);
                    } else if (movementsTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
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

    private void enableFields(Table t) {
        Iterator iter = ((IndexedContainer) t
                .getContainerDataSource()).getItemIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            Iterator iterProp = ((IndexedContainer) t.getContainerDataSource()).getContainerPropertyIds().iterator();
            while (iterProp.hasNext()) {
                Object next1 = iterProp.next();
                Object c = t.getItem(next).getItemProperty(next1).getValue();
                if (c instanceof AbstractField) {
                    if (!((AbstractField) c).getDescription().equals(myUI.getMessage(SptMessages.CanNotModify))) {
                        ((AbstractField) c).setEnabled(true);
                    }
                } else if (c instanceof AbstractComponent) {
                    if (!((AbstractComponent) c).getDescription().equals(myUI.getMessage(SptMessages.CanNotDelete))) {
                        ((AbstractComponent) c).setEnabled(true);
                    }
                }
            }
        }
    }

    private boolean disableFields(Table t) {
        Object idToEnable = null;
        Iterator iter = ((IndexedContainer) t
                .getContainerDataSource()).getItemIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            Iterator iterProp = ((IndexedContainer) t.getContainerDataSource()).getContainerPropertyIds().iterator();
            while (iterProp.hasNext()) {
                Object next1 = iterProp.next();
                Object c = t.getItem(next).getItemProperty(next1).getValue();
                if (c instanceof AbstractField) {
                    if (!((AbstractField) c).isValid()) {
                        idToEnable = next;
                    } else {
                        if (!((AbstractField) c).getDescription().equals(myUI.getMessage(SptMessages.CanNotModify))) {
                            ((AbstractField) c).setEnabled(false);
                        }
                    }
                } else if (c instanceof AbstractComponent) {
                    if (!((AbstractComponent) c).getDescription().equals(myUI.getMessage(SptMessages.CanNotDelete))) {
                        ((AbstractComponent) c).setEnabled(false);
                    }
                }
            }
        }
        if (idToEnable == null) {
            return false;
        } else {
            enableRow(t, idToEnable);
            return true;
        }
    }

    public void enableRow(Table t, Object itemId) {
        Iterator iterProp = ((IndexedContainer) t.getContainerDataSource()).getContainerPropertyIds().iterator();
        while (iterProp.hasNext()) {
            Object next1 = iterProp.next();
            Object c = t.getItem(itemId).getItemProperty(next1).getValue();
            if (c instanceof AbstractField) {
                if (!((AbstractField) c).getDescription().equals(myUI.getMessage(SptMessages.CanNotModify))) {
                    ((AbstractField) c).setEnabled(true);
                }
            } else if (c instanceof AbstractComponent) {
                if (!((AbstractComponent) c).getDescription().equals(myUI.getMessage(SptMessages.CanNotDelete))) {
                    ((AbstractComponent) c).setEnabled(true);
                }
            }
        }
    }

    public void setMovementsFooter(double amount) {
        movementsTable.setColumnFooter(myUI.getMessage(SptMessages.Amount), sysSettings.dFormat.format(amount));
    }

    public ComboBoxMax getProductCategorySelect() {
        return productCategorySelect;
    }

    /**
     * @return the originalCont
     */
    public IndexedContainer getOriginalCont() {
        return originalCont;
    }

    public Component getNewObj() {
        return new StockIncomeView(myUI);
    }
}
