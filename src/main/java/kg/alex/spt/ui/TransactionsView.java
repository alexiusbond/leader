/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.ui;

import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.ui.*;
import kg.alex.spt.utils.ComboBoxMax;
import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbAccTransactions;
import kg.alex.spt.dao.DbCurrencyRate;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbEmployee;
import kg.alex.spt.domain.AccTransaction;
import kg.alex.spt.domain.CurrencyRate;
import kg.alex.spt.domain.SchoolAccounting;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.pdf.TransactionInvoicePDF;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

public class TransactionsView extends GridLayout implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(TransactionsView.class);
    private MyVaadinUI myUI;
    private Button plusButton, saveButton, searchButton;
    private OptionGroup currencySettingsOG;
    private TextField currencyTF;
    private Label incomeTtlLab, expenseTtlLab, ttlLab, prev_balanceLab, currencyLab;
    public SchoolAccounting schoolAcc;
    private DateField fromDateDF, tillDateDF;

    private String[] NATURAL_COL_ORDER_INCOMES, NATURAL_COL_ORDER_EXPENSES;
    private FormattedTable incomesTable, expensesTable;
    private IndexedContainer incomesCont = null, expensesCont = null;
    private int r_table_counter = 1000;
    private Accordion accordition;
    private Subject currentUser = SecurityUtils.getSubject();
    private HorizontalLayout currencyHl;

    public TransactionsView(MyVaadinUI myUI) {
        this.myUI = myUI;

        setRows(3);
        setColumns(4);
        setSizeFull();
        setMargin(true);
        setSpacing(true);

        buildLabels();
        buildIncomesTable();
        buildExpensesTable();
        accordition = new Accordion();
        accordition.setSizeFull();
        accordition.addTab(incomesTable, myUI.getMessage(SptMessages.Incomes));
        accordition.addTab(expensesTable, myUI.getMessage(SptMessages.Outcomes));
        accordition.setSelectedTab(expensesTable);
        addComponent(accordition, 0, 2, 3, 2);
        getTotals();
        recount();
    }

    private void buildLabels() {

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);

        fromDateDF = new DateField();
        fromDateDF.setDescription(myUI.getMessage(SptMessages.FromDate));
        fromDateDF.setWidth("100%");
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        fromDateDF.setDateFormat(SystemSettings.datePattern);
        fromDateDF.setValue(new Date());

        tillDateDF = new DateField();
        tillDateDF.setDescription(myUI.getMessage(SptMessages.TillDate));
        tillDateDF.setWidth("100%");
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setDateFormat(SystemSettings.datePattern);
        tillDateDF.setValue(new Date());

        searchButton = new Button();
        searchButton.addStyleName(ValoTheme.BUTTON_SMALL);
        searchButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        searchButton.setIcon(FontAwesome.SEARCH);
        searchButton.addClickListener(this);

        hl.addComponent(fromDateDF);
        hl.addComponent(tillDateDF);
        hl.addComponent(searchButton);
        hl.setExpandRatio(fromDateDF, 1);
        hl.setExpandRatio(tillDateDF, 1);

        incomeTtlLab = new Label();
        incomeTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        incomeTtlLab.setImmediate(true);
        incomeTtlLab.setWidth("100%");

        expenseTtlLab = new Label();
        expenseTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        expenseTtlLab.setImmediate(true);
        expenseTtlLab.setWidth("100%");

        ttlLab = new Label();
        ttlLab.setContentMode(ContentMode.HTML);
        ttlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        ttlLab.setImmediate(true);
        ttlLab.setWidth("100%");

        prev_balanceLab = new Label();
        prev_balanceLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        prev_balanceLab.setImmediate(true);
        prev_balanceLab.setSizeFull();

        plusButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusButton.addStyleName(ValoTheme.BUTTON_SMALL);
        plusButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusButton.setWidth("100%");
        plusButton.addClickListener(this);

        currencyHl = new HorizontalLayout();
        currencyHl.setWidth("100%");
        currencyHl.setSpacing(true);
        currencyHl.setEnabled(currentUser.isPermitted(SystemSettings.cnTransactionsView + ":" + SystemSettings.prmChangeCurrencyRate) && myUI.isMannualRate());

        currencyLab = new Label();
        currencyLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        currencyLab.setImmediate(true);
        currencyLab.setSizeUndefined();
        currencyLab.setValue(myUI.getMessage(SptMessages.RateUSD));

        currencyTF = new TextField();
        ObjectProperty<Double> property = new ObjectProperty<Double>(0.0);
        currencyTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        currencyTF.setWidth("100%");
        currencyTF.setNullRepresentation("0.1");
        currencyTF.setNullSettingAllowed(false);
        currencyTF.setConverter(SystemSettings.getStringToDoubleConverter());
        currencyTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
        currencyTF.setPropertyDataSource(property);
        currencyTF.getPropertyDataSource().setValue(myUI.getDb_currency_rate());

        currencyHl.addComponent(currencyLab);
        currencyHl.addComponent(currencyTF);
        currencyHl.setExpandRatio(currencyTF, 1);

        HorizontalLayout currencySettingsHl = new HorizontalLayout();
        currencySettingsHl.setWidth("100%");
        currencySettingsHl.setSpacing(true);
        currencySettingsHl.setEnabled(currentUser.isPermitted(SystemSettings.cnTransactionsView + ":" + SystemSettings.prmChangeCurrencyRate));

        currencySettingsOG = new OptionGroup();
        currencySettingsOG.setWidth("100%");
        currencySettingsOG.setStyleName(ValoTheme.OPTIONGROUP_SMALL);
        currencySettingsOG.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        currencySettingsOG.addItem(myUI.getMessage(SptMessages.NBKR));
        currencySettingsOG.addItem(myUI.getMessage(SptMessages.Mannual));
        currencySettingsOG.addValueChangeListener(this);
        if (myUI.isMannualRate()) {
            currencySettingsOG.select(myUI.getMessage(SptMessages.Mannual));
        } else {
            currencySettingsOG.select(myUI.getMessage(SptMessages.NBKR));
        }

        saveButton = new Button();
        saveButton.setDescription(myUI.getMessage(SptMessages.SaveButton));
        saveButton.addStyleName(ValoTheme.BUTTON_SMALL);
        saveButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveButton.setIcon(FontAwesome.SAVE);
        saveButton.addClickListener(this);

        currencySettingsHl.addComponent(currencySettingsOG);
        currencySettingsHl.addComponent(saveButton);
        currencySettingsHl.setExpandRatio(currencySettingsOG, 1);

        addComponent(hl, 0, 0);
        addComponent(plusButton, 0, 1);
        addComponent(prev_balanceLab, 1, 0);
        addComponent(incomeTtlLab, 1, 1);
        addComponent(expenseTtlLab, 2, 1);
        addComponent(ttlLab, 2, 0);
        addComponent(currencyHl, 3, 0);
        addComponent(currencySettingsHl, 3, 1);
        setComponentAlignment(plusButton, Alignment.BOTTOM_RIGHT);
        setComponentAlignment(ttlLab, Alignment.MIDDLE_RIGHT);
        setComponentAlignment(prev_balanceLab, Alignment.BOTTOM_LEFT);
        setColumnExpandRatio(0, 2);
        setColumnExpandRatio(1, 4);
        setColumnExpandRatio(2, 4);
        setColumnExpandRatio(3, 2);
    }

    private void buildIncomesTable() {
        incomesTable = new FormattedTable();
        incomesTable.setSizeFull();
        incomesTable.setStyleName(ValoTheme.TABLE_SMALL);
        incomesTable.setNullSelectionAllowed(false);
        setIncomesTable();
        setRowExpandRatio(2, 1);
    }

    private void buildExpensesTable() {
        expensesTable = new FormattedTable();
        expensesTable.setSizeFull();
        expensesTable.setStyleName(ValoTheme.TABLE_SMALL);
        expensesTable.setNullSelectionAllowed(false);
        setExpensesTable();
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == searchButton) {
            if (fromDateDF.getValue() != null && tillDateDF.getValue() != null) {
                long diff = tillDateDF.getValue().getTime() - fromDateDF.getValue().getTime();
                if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) > 10) {
                    Notification.show(myUI.getMessage(SptMessages.NotifDateRageExceeds), Notification.Type.WARNING_MESSAGE);
                } else {
                    setIncomesTable();
                    setExpensesTable();
                    getTotals();
                    recount();
                }
            }
        } else if (source == plusButton) {
            if (accordition.getSelectedTab() == incomesTable) {
                addIncomesItem();
            } else {
                addExpensesItem();
            }
        } else if (source == saveButton) {
            if (currencyTF.isValid()) {
                int st = 0;
                CurrencyRate cr = new CurrencyRate();
                try {
                    DbCurrencyRate dbCon = new DbCurrencyRate();
                    dbCon.connect();
                    cr.setEmployee_id(myUI.getUser().getId());
                    cr.setSchool_id(myUI.getUser().getSchool_id());
                    if (currencySettingsOG.getValue().equals(myUI.getMessage(SptMessages.Mannual))) {
                        cr.setValue((Double) currencyTF.getPropertyDataSource().getValue());
                        cr.setMannual(1);
                    } else {
                        cr.setMannual(0);
                    }
                    st = dbCon.exec_insert(cr);
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                if (st != 0) {
                    if (cr.getMannual() == 0) {
                        currencyTF.getPropertyDataSource().setValue(myUI.getCurrencyRateFromBank());
                    }
                    Notification.show(myUI.getMessage(SptMessages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                }
            } else {
                Notification.show(myUI.getMessage(SptMessages.NotifWrongValue), Notification.Type.WARNING_MESSAGE);
            }
        } else if (source.getCaption().equals(myUI.getMessage(SptMessages.Print))) {
            AccTransaction tr = getTransaction(source.getData().toString());
            Table t = null;
            int acc_type_id = 0;
            String orderName = null;
            if (expensesTable.getContainerProperty(source.getData(), myUI.getMessage(SptMessages.Category)) != null) {
                t = expensesTable;
                acc_type_id = 2;
                orderName = myUI.getMessage(SptMessages.ExpenceOrder);
            } else if (incomesTable.getContainerProperty(source.getData(), myUI.getMessage(SptMessages.Category)) != null) {
                t = incomesTable;
                acc_type_id = 1;
                orderName = myUI.getMessage(SptMessages.IncomeOrder);
            }
            tr.setCategory(((ComboBoxMax) t.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Category)).getValue()).getItemCaption(((ComboBoxMax) t.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Category)).getValue()).getValue()));
            if (t.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.ToEmployee)) != null && ((ComboBoxMax) t.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.ToEmployee)).getValue()).getValue() != null) {
                tr.setFrom_to_employee(((ComboBoxMax) t.getContainerProperty(source.getData(),
                        myUI.getMessage(SptMessages.ToEmployee)).getValue()).getItemCaption(
                        ((ComboBoxMax) t.getContainerProperty(source.getData(),
                                myUI.getMessage(SptMessages.ToEmployee)).getValue()).getValue()));
            }
            if (tr.getOrder_number() == 0) {
                try {
                    DbAccTransactions dbTr = new DbAccTransactions();
                    dbTr.connect();
                    tr.setOrder_number(dbTr.exec_update_order_number(tr.getId(), tr.getSchool_id(), acc_type_id));
                    dbTr.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            t.getContainerProperty(source.getData().toString(), SystemSettings.order_number).setValue(tr.getOrder_number());
            new TransactionInvoicePDF(myUI, tr, myUI.getUser().getSchool_name(), myUI.getUser().getSchool_logo(), orderName);
        } else {
            if (source.getData().toString().contains(SystemSettings.FreshItem)) {
                if (source.getCaption().equals(myUI.getMessage(SptMessages.Incomes))) {
                    incomesTable.removeItem(source.getData().toString());
                } else if (source.getCaption().equals(myUI.getMessage(SptMessages.Outcomes))) {
                    expensesTable.removeItem(source.getData().toString());
                }
            } else {
                ConfirmDialog.show(myUI, myUI.getMessage(SptMessages.Question),
                        myUI.getMessage(SptMessages.ConfirmDeletion),
                        myUI.getMessage(SptMessages.Yes),
                        myUI.getMessage(SptMessages.No),
                        new ConfirmDialog.Listener() {
                            @Override
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    execDelete(source);
                                }
                            }
                        });
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == currencySettingsOG) {
            if (currencySettingsOG.getValue().equals(myUI.getMessage(SptMessages.Mannual))) {
                currencyHl.setEnabled(true);
            } else {
                currencyHl.setEnabled(false);
            }
        } else if (property instanceof DateField || property instanceof TextField || property instanceof ComboBoxMax) {
            String itemId = ((AbstractField) property).getData().toString();
            if (((AbstractField) property).getId() != null && ((AbstractField) property).getId().equals(myUI.getMessage(SptMessages.Outcomes))) {
                if (((TextField) expensesTable.getContainerProperty(((AbstractField) property).getData().toString(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue() != null
                        && ((TextField) expensesTable.getContainerProperty(((AbstractField) property).getData().toString(),
                        myUI.getMessage(SptMessages.Rate)).getValue()).getPropertyDataSource().getValue() != null
                        && ((DateField) expensesTable.getContainerProperty(((AbstractField) property).getData().toString(),
                        myUI.getMessage(SptMessages.Date)).getValue()).getValue() != null
                        && ((ComboBoxMax) expensesTable.getContainerProperty(((AbstractField) property).getData().toString(),
                        myUI.getMessage(SptMessages.Category)).getValue()).getValue() != null) {
                    TextField tf = null;
                    try {
                        tf = (TextField) expensesTable.getContainerProperty(itemId, myUI.getMessage(SptMessages.Amount)).getValue();
                        double amount = Double.parseDouble(tf.getValue());
                        boolean isKGS = (Integer) ((ComboBoxMax) expensesTable.getContainerProperty(itemId, myUI.getMessage(SptMessages.Currency)).getValue()).getValue() == 1;
                        if (isKGS) {
                            amount = SystemSettings.round(amount
                                    / Double.parseDouble(((TextField) expensesTable.getContainerProperty(itemId, myUI.getMessage(SptMessages.Rate)).getValue()).getValue()), 2);
                        }
                        if (amount > 0.0) {
                            double old_amount = (Double) expensesTable.getContainerProperty(itemId, SystemSettings.old_amount).getValue();
                            if ((Integer) expensesTable.getContainerProperty(itemId, SystemSettings.old_currency).getValue() == 1) {
                                old_amount = SystemSettings.round(old_amount / (Double) expensesTable.getContainerProperty(itemId, SystemSettings.old_rate).getValue(), 2);
                            }
                            DbAccTransactions dbTr = new DbAccTransactions();
                            dbTr.connect();
                            AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool_id(),
                                    ((DateField) expensesTable.getContainerProperty(itemId,
                                            myUI.getMessage(SptMessages.Date)).getValue()).getValue(), old_amount, amount, 2);
                            if (tr != null) {
                                tf.removeAllValidators();
                                double limit = tr.getLimit();
                                if (isKGS) {
                                    limit = limit * Double.parseDouble(((TextField) expensesTable.getContainerProperty(itemId, myUI.getMessage(SptMessages.Rate)).getValue()).getValue());
                                }
                                tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.LowBalance) + SystemSettings.dFormat.format(tr.getOverlimit())
                                        + " $ (" + SystemSettings.df.format(tr.getDate()) + ")", 0.1, SystemSettings.round(limit, 2)));
                                Notification.show(myUI.getMessage(SptMessages.LowBalance) + SystemSettings.dFormat.format(tr.getOverlimit())
                                        + " $ (" + SystemSettings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                            } else {
                                if (validateRow(itemId)) {
                                    updateTable(itemId);
                                }
                            }
                            dbTr.close();
                        } else {
                            tf.removeAllValidators();
                            tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
                        }
                    } catch (Exception e) {
                        tf.removeAllValidators();
                        tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
                        logger.error(e);
                        logger.catching(e);
                    }
                }
            } else if (((AbstractField) property).getId() != null && ((AbstractField) property).getId().equals(myUI.getMessage(SptMessages.Incomes))
                    && !itemId.contains(SystemSettings.FreshItem)) {
                if (((TextField) incomesTable.getContainerProperty(((AbstractField) property).getData().toString(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue() != null
                        && ((TextField) incomesTable.getContainerProperty(((AbstractField) property).getData().toString(),
                        myUI.getMessage(SptMessages.Rate)).getValue()).getPropertyDataSource().getValue() != null
                        && ((DateField) incomesTable.getContainerProperty(((AbstractField) property).getData().toString(),
                        myUI.getMessage(SptMessages.Date)).getValue()).getValue() != null
                        && ((ComboBoxMax) incomesTable.getContainerProperty(((AbstractField) property).getData().toString(),
                        myUI.getMessage(SptMessages.Category)).getValue()).getValue() != null) {
                    TextField tf = null;
                    try {
                        tf = (TextField) incomesTable.getContainerProperty(itemId, myUI.getMessage(SptMessages.Amount)).getValue();
                        double amount = Double.parseDouble(tf.getValue());
                        boolean isKGS = (Integer) ((ComboBoxMax) incomesTable.getContainerProperty(itemId, myUI.getMessage(SptMessages.Currency)).getValue()).getValue() == 1;
                        if (isKGS) {
                            amount = SystemSettings.round(amount
                                    / Double.parseDouble(((TextField) incomesTable.getContainerProperty(itemId, myUI.getMessage(SptMessages.Rate)).getValue()).getValue()), 2);
                        }
                        if (amount > 0.0) {
                            double old_amount = (Double) incomesTable.getContainerProperty(itemId, SystemSettings.old_amount).getValue();
                            if ((Integer) incomesTable.getContainerProperty(itemId, SystemSettings.old_currency).getValue() == 1) {
                                old_amount = SystemSettings.round(old_amount / (Double) incomesTable.getContainerProperty(itemId, SystemSettings.old_rate).getValue(), 2);
                            }
                            if (amount <= old_amount || DateUtils.truncate(((DateField) incomesTable.getContainerProperty(itemId,
                                    myUI.getMessage(SptMessages.Date)).getValue()).getValue(), java.util.Calendar.DAY_OF_MONTH)
                                    .compareTo((Date) incomesTable.getContainerProperty(itemId, SystemSettings.old_date).getValue()) != 0) {
                                DbAccTransactions dbTr = new DbAccTransactions();
                                dbTr.connect();
                                AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool_id(),
                                        ((DateField) incomesTable.getContainerProperty(itemId,
                                                myUI.getMessage(SptMessages.Date)).getValue()).getValue(), old_amount, amount, 1);
                                if (tr != null) {
                                    tf.removeAllValidators();
                                    double limit = tr.getLimit();
                                    if (isKGS) {
                                        limit = limit * Double.parseDouble(((TextField) incomesTable.getContainerProperty(itemId, myUI.getMessage(SptMessages.Rate)).getValue()).getValue());
                                    }
                                    tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.LowBalance) + SystemSettings.dFormat.format(tr.getOverlimit())
                                            + " $ (" + SystemSettings.df.format(tr.getDate()) + ")", SystemSettings.round(limit, 2), null));
                                    Notification.show(myUI.getMessage(SptMessages.LowBalance) + SystemSettings.dFormat.format(tr.getOverlimit())
                                            + " $ (" + SystemSettings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                                } else {
                                    if (validateRow(itemId)) {
                                        updateTable(itemId);
                                    }
                                }
                                dbTr.close();
                            } else {
                                tf.removeAllValidators();
                                tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), amount, null));
                                if (validateRow(itemId)) {
                                    updateTable(itemId);
                                }
                            }
                        } else {
                            tf.removeAllValidators();
                            tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
                        }
                    } catch (Exception e) {
                        tf.removeAllValidators();
                        tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
                        logger.error(e);
                        logger.catching(e);
                    }
                }
            } else {
                if (validateRow(itemId)) {
                    updateTable(itemId);
                }
            }
        }
    }

    private void setIncomesTable() {
        NATURAL_COL_ORDER_INCOMES = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.Category), myUI.getMessage(SptMessages.Currency),
                myUI.getMessage(SptMessages.Rate),
                myUI.getMessage(SptMessages.Amount), myUI.getMessage(SptMessages.Note)};
        try {
            DbAccTransactions dbat = new DbAccTransactions();
            dbat.connect();
            incomesTable.setContainerDataSource(dbat.execSQL(myUI, 1,
                    myUI.getUser().getSchool_id(), this, fromDateDF.getValue(), tillDateDF.getValue()));
            incomesTable.setColumnWidth(SystemSettings.button, 62);
            incomesTable.setColumnWidth(myUI.getMessage(SptMessages.Currency), 70);
            incomesTable.setColumnWidth(myUI.getMessage(SptMessages.Rate), 55);
            incomesTable.setColumnWidth(myUI.getMessage(SptMessages.Amount), 120);
            incomesTable.setColumnWidth(myUI.getMessage(SptMessages.Date), 140);
            dbat.close();
            incomesTable.setVisibleColumns(NATURAL_COL_ORDER_INCOMES);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setExpensesTable() {
        NATURAL_COL_ORDER_EXPENSES = new String[]{SystemSettings.button, myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.Category), myUI.getMessage(SptMessages.Currency),
                myUI.getMessage(SptMessages.Rate), myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.Note), myUI.getMessage(SptMessages.ToEmployee)};
        try {
            DbAccTransactions dbat = new DbAccTransactions();
            dbat.connect();
            expensesTable.setContainerDataSource(dbat.execSQL(
                    myUI, 2, myUI.getUser().getSchool_id(), this, fromDateDF.getValue(),
                    tillDateDF.getValue()));
            expensesTable.setColumnWidth(SystemSettings.button, 62);
            expensesTable.setColumnWidth(myUI.getMessage(SptMessages.Currency), 70);
            expensesTable.setColumnWidth(myUI.getMessage(SptMessages.Rate), 55);
            expensesTable.setColumnWidth(myUI.getMessage(SptMessages.Amount), 120);
            expensesTable.setColumnWidth(myUI.getMessage(SptMessages.Date), 140);
            expensesTable.setColumnWidth(myUI.getMessage(SptMessages.ToEmployee), 240);
            dbat.close();
            expensesTable.setVisibleColumns(NATURAL_COL_ORDER_EXPENSES);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    public Button createButton(String description, String itemId, String caption, boolean isDisabled, FontAwesome icon) {
        Button btn = new Button(caption);
        if (isDisabled) {
            btn.setEnabled(false);
        }
        btn.setDescription(description);
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.addStyleName(ValoTheme.BUTTON_TINY);
        btn.setIcon(icon);
        btn.setData(itemId);
        btn.addClickListener(this);
        return btn;
    }

    public DateField createDateField(Date value, String description, String itemId,
                                     boolean isDisabled, String tableName) {
        DateField df = new DateField();
        if (isDisabled) {
            df.setEnabled(false);
        }
        df.setDescription(description);
        df.setRangeEnd(new Date());
        df.setRequired(true);
        df.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        df.setStyleName(ValoTheme.DATEFIELD_TINY);
        df.setWidth("100%");
        df.setDateFormat(SystemSettings.dateTimeMinPattern);
        df.setResolution(Resolution.MINUTE);
        if (value != null) {
            df.setValue(value);
        }
        if (currentUser.isPermitted(SystemSettings.cnTransactionsView + ":" + SystemSettings.prmChangeOldTransactions)) {
            df.setRangeStart(myUI.getUser().getTransactions_start_date());
        } else if (!isDisabled) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -1441);
            df.setRangeStart(calendar.getTime());
            df.addValidator(new DateRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue),
                    df.getRangeStart(), df.getRangeEnd(), Resolution.MINUTE));
        }
        df.setData(itemId);
        df.addValueChangeListener(this);
        df.setImmediate(true);
        df.setId(tableName);
        return df;
    }

    public ComboBoxMax createCombobox(int value, String description, String itemId,
                                      String dbTable, boolean isDisabled, boolean isRequired, boolean isValueChangeLsitener) {
        ComboBoxMax cb = new ComboBoxMax();

        cb.setDescription(description);
        if (isDisabled) {
            cb.setEnabled(false);
        }
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth("100%");
        if (isRequired) {
            cb.setRequired(true);
            cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        }
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        if (dbTable != null) {
            try {
                DbDefinition dbd = new DbDefinition();
                dbd.connect();
                cb.setContainerDataSource(dbd.exec_for_select(myUI, dbTable, true));
                dbd.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        cb.setNullSelectionAllowed(false);
        cb.setData(itemId);
        cb.setValue(value);
        if (isValueChangeLsitener) {
            cb.addValueChangeListener(this);
        }
        return cb;
    }

    public ComboBoxMax createComboboxCategory(int value, String description, String itemId,
                                              int cat_type_id, boolean isDisabled, String tableName) {
        ComboBoxMax cb = new ComboBoxMax();
        if (isDisabled) {
            cb.setEnabled(false);
        }
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth("100%");
        cb.setRequired(true);
        cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbAccTransactions dbac = new DbAccTransactions();
            dbac.connect();
            cb.setContainerDataSource(dbac.exec_for_select(myUI, cat_type_id, myUI.getUser().getSchool_id(), value));
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setNullSelectionAllowed(false);
        cb.setData(itemId);
        cb.setValue(value);
            cb.addValueChangeListener(this);
        cb.setId(tableName);
        return cb;
    }

    public TextField createTextfieldDouble(Double value, String description, String itemId,
                                           boolean rate, boolean isDisabled, String tableName) {
        ObjectProperty<Double> property = new ObjectProperty<Double>(0.0);
        TextField tf = new TextField(property);
        if (isDisabled) {
            tf.setEnabled(false);
        }
        tf.setDescription(description);
        tf.setRequired(true);
        tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth("100%");
        tf.setData(itemId);
        tf.setNullRepresentation("0.0");
        tf.setNullSettingAllowed(false);
        tf.setConverter(SystemSettings.getStringToDoubleConverter());
        tf.addValidator(new DoubleRangeValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
        if (rate == true) {
            if (value == null) {
                tf.getPropertyDataSource().setValue(myUI.getDb_currency_rate());
            } else {
                tf.getPropertyDataSource().setValue(value);
            }
        } else if (rate == false) {
            tf.getPropertyDataSource().setValue(value);
        }
        tf.addValueChangeListener(this);
        tf.setImmediate(true);
        tf.setId(tableName);
        return tf;
    }

    public TextField createTextfieldNote(String value, String description, String itemId, boolean isDisabled) {
        TextField tf = new TextField();
        if (isDisabled) {
            tf.setEnabled(false);
        }
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth("100%");
        if (value != null) {
            tf.setValue(value);
        }
        tf.setData(itemId);
        tf.addValueChangeListener(this);
        tf.setImmediate(true);
        tf.setRequired(true);
        tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        return tf;
    }

    public IndexedContainer prepareIncomesContainer() {
        if (incomesCont == null) {
            incomesCont = new IndexedContainer();
            incomesCont.addContainerProperty(SystemSettings.button, HorizontalLayout.class, null);
            incomesCont.addContainerProperty(myUI.getMessage(SptMessages.Date), DateField.class, null);
            incomesCont.addContainerProperty(myUI.getMessage(SptMessages.Category), ComboBoxMax.class, null);
            incomesCont.addContainerProperty(myUI.getMessage(SptMessages.Currency), ComboBoxMax.class, null);
            incomesCont.addContainerProperty(myUI.getMessage(SptMessages.Rate), TextField.class, null);
            incomesCont.addContainerProperty(myUI.getMessage(SptMessages.Amount), TextField.class, null);
            incomesCont.addContainerProperty(myUI.getMessage(SptMessages.Note), TextField.class, null);
            incomesCont.addContainerProperty(SystemSettings.old_amount, Double.class, 0.0);
            incomesCont.addContainerProperty(SystemSettings.old_rate, Double.class, 0.0);
            incomesCont.addContainerProperty(SystemSettings.old_currency, Integer.class, 0);
            incomesCont.addContainerProperty(SystemSettings.old_date, Date.class, null);
            incomesCont.addContainerProperty(SystemSettings.from_employee_id, String.class, null);
            incomesCont.addContainerProperty(SystemSettings.order_number, Integer.class, 0);
        } else {
            incomesCont.removeAllItems();
        }
        return incomesCont;
    }

    public IndexedContainer prepareExpensesContainer() {
        if (expensesCont == null) {
            expensesCont = new IndexedContainer();
            expensesCont.addContainerProperty(SystemSettings.button, HorizontalLayout.class, null);
            expensesCont.addContainerProperty(myUI.getMessage(SptMessages.Date), DateField.class, null);
            expensesCont.addContainerProperty(myUI.getMessage(SptMessages.Category), ComboBoxMax.class, null);
            expensesCont.addContainerProperty(myUI.getMessage(SptMessages.Currency), ComboBoxMax.class, null);
            expensesCont.addContainerProperty(myUI.getMessage(SptMessages.Rate), TextField.class, null);
            expensesCont.addContainerProperty(myUI.getMessage(SptMessages.Amount), TextField.class, null);
            expensesCont.addContainerProperty(myUI.getMessage(SptMessages.Note), TextField.class, null);
            expensesCont.addContainerProperty(myUI.getMessage(SptMessages.ToEmployee), ComboBoxMax.class, null);
            expensesCont.addContainerProperty(SystemSettings.from_employee_id, String.class, null);
            expensesCont.addContainerProperty(SystemSettings.old_amount, Double.class, 0.0);
            expensesCont.addContainerProperty(SystemSettings.old_rate, Double.class, 0.0);
            expensesCont.addContainerProperty(SystemSettings.old_currency, Integer.class, 0);
            expensesCont.addContainerProperty(SystemSettings.old_date, Date.class, null);
            expensesCont.addContainerProperty(SystemSettings.order_number, Integer.class, 0);
        } else {
            expensesCont.removeAllItems();
        }
        return expensesCont;
    }

    private void addIncomesItem() {
        NATURAL_COL_ORDER_INCOMES = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.Category), myUI.getMessage(SptMessages.Currency),
                myUI.getMessage(SptMessages.Rate),
                myUI.getMessage(SptMessages.Amount), myUI.getMessage(SptMessages.Note)};

        String id = SystemSettings.FreshItem + (--r_table_counter);

        if (incomesTable.getContainerDataSource().size() == 0) {
            incomesTable.setContainerDataSource(prepareIncomesContainer());
        }
        Item item;
        item = ((IndexedContainer) incomesTable.getContainerDataSource()).addItemAt(0, id);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.addComponent(createButton(myUI.getMessage(SptMessages.DeleteButton), id, myUI.getMessage(SptMessages.Incomes),
                false, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(SystemSettings.button).setValue(hl);
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                createDateField(new Date(), myUI.getMessage(SptMessages.Date), id, false, myUI.getMessage(SptMessages.Incomes)));
        item.getItemProperty(myUI.getMessage(SptMessages.Category)).setValue(
                createComboboxCategory(0, myUI.getMessage(SptMessages.Category), id, 1, false,
                        myUI.getMessage(SptMessages.Incomes)));
        item.getItemProperty(myUI.getMessage(SptMessages.Currency)).setValue(
                createCombobox(2, myUI.getMessage(SptMessages.Currency), id,
                SystemSettings.dbAcc_currency, false, true, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(createTextfieldDouble(null, myUI.getMessage(SptMessages.Rate), id, true,
                !currentUser.isPermitted(SystemSettings.cnTransactionsView + ":" + SystemSettings.prmChangeCurrencyRate), null));
        item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                createTextfieldDouble(null, myUI.getMessage(SptMessages.Amount), id, false, false, myUI.getMessage(SptMessages.Incomes)));
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(createTextfieldNote(null, myUI.getMessage(SptMessages.Note), id, false));
        incomesTable.setVisibleColumns(NATURAL_COL_ORDER_INCOMES);
    }

    private void updateIncomesItem(int new_id, AccTransaction t) {
        String id = new_id + "";
        if (incomesTable.getContainerDataSource().size() == 0) {
            incomesTable.setContainerDataSource(prepareIncomesContainer());
        }
        Item item;
        item = ((IndexedContainer) incomesTable.getContainerDataSource()).addItemAt(0, id);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.addComponent(createButton(myUI.getMessage(SptMessages.DeleteButton), id, myUI.getMessage(SptMessages.Incomes),
                false, FontAwesome.MINUS_SQUARE));
        hl.addComponent(createButton(myUI.getMessage(SptMessages.Print), id, myUI.getMessage(SptMessages.Print), false, FontAwesome.FILE_PDF_O));
        item.getItemProperty(SystemSettings.button).setValue(hl);
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                createDateField(t.getDate(), myUI.getMessage(SptMessages.Date), id, false, myUI.getMessage(SptMessages.Incomes)));
        item.getItemProperty(myUI.getMessage(SptMessages.Category)).setValue(
                createComboboxCategory(t.getCategory_id(), myUI.getMessage(SptMessages.Category), id, 1,
                        false, myUI.getMessage(SptMessages.Incomes)));
        item.getItemProperty(myUI.getMessage(SptMessages.Currency)).setValue(createCombobox(t.getCurrency_id(), myUI.getMessage(SptMessages.Currency), id,
                SystemSettings.dbAcc_currency, false, true, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(createTextfieldDouble(t.getCurrency_rate(),
                myUI.getMessage(SptMessages.Rate), id, true,
                !currentUser.isPermitted(SystemSettings.cnTransactionsView + ":" + SystemSettings.prmChangeCurrencyRate), null));
        item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                createTextfieldDouble(t.getAmount(), myUI.getMessage(SptMessages.Amount), id, false, false, myUI.getMessage(SptMessages.Incomes)));
        item.getItemProperty(SystemSettings.from_employee_id).setValue(myUI.getUser().getFullname());
        item.getItemProperty(SystemSettings.old_amount).setValue(t.getAmount());
        item.getItemProperty(SystemSettings.old_rate).setValue(t.getCurrency_rate());
        item.getItemProperty(SystemSettings.old_currency).setValue(t.getCurrency_id());
        item.getItemProperty(SystemSettings.old_date).setValue(t.getDate());
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(createTextfieldNote(t.getNote(), myUI.getMessage(SptMessages.Note), id, false));
        incomesTable.setVisibleColumns(NATURAL_COL_ORDER_INCOMES);
    }

    private void updateExpensesItem(int new_id, AccTransaction t) {
        String id = new_id + "";
        if (expensesTable.getContainerDataSource().size() == 0) {
            expensesTable.setContainerDataSource(prepareExpensesContainer());
        }
        Item item;
        item = ((IndexedContainer) expensesTable.getContainerDataSource()).addItemAt(0, id);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.addComponent(createButton(myUI.getMessage(SptMessages.DeleteButton), id, myUI.getMessage(SptMessages.Outcomes), false, FontAwesome.MINUS_SQUARE));
        hl.addComponent(createButton(myUI.getMessage(SptMessages.Print), id, myUI.getMessage(SptMessages.Print), false, FontAwesome.FILE_PDF_O));
        item.getItemProperty(SystemSettings.button).setValue(hl);
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                createDateField(t.getDate(), myUI.getMessage(SptMessages.Date), id, false, myUI.getMessage(SptMessages.Outcomes)));
        item.getItemProperty(myUI.getMessage(SptMessages.Category)).setValue(
                createComboboxCategory(t.getCategory_id(), myUI.getMessage(SptMessages.Category), id, 2,
                        false, myUI.getMessage(SptMessages.Outcomes)));
        item.getItemProperty(myUI.getMessage(SptMessages.Currency)).setValue(createCombobox(t.getCurrency_id(), myUI.getMessage(SptMessages.Currency), id,
                SystemSettings.dbAcc_currency, false, true, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(createTextfieldDouble(t.getCurrency_rate(),
                myUI.getMessage(SptMessages.Rate), id, true,
                !currentUser.isPermitted(SystemSettings.cnTransactionsView + ":" + SystemSettings.prmChangeCurrencyRate), null));
        item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                createTextfieldDouble(t.getAmount(), myUI.getMessage(SptMessages.Amount), id, false, false, myUI.getMessage(SptMessages.Outcomes)));
        item.getItemProperty(SystemSettings.from_employee_id).setValue(myUI.getUser().getFullname());
        item.getItemProperty(SystemSettings.old_amount).setValue(t.getAmount());
        item.getItemProperty(SystemSettings.old_rate).setValue(t.getCurrency_rate());
        item.getItemProperty(SystemSettings.old_currency).setValue(t.getCurrency_id());
        item.getItemProperty(SystemSettings.old_date).setValue(t.getDate());
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(createTextfieldNote(t.getNote(), myUI.getMessage(SptMessages.Note), id, false));
        ComboBoxMax cb = createCombobox(0, myUI.getMessage(SptMessages.ToEmployee), id, SystemSettings.dbEmployee,
                false, false, true);
        try {
            DbEmployee dbCon = new DbEmployee();
            dbCon.connect();
            cb.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool_id(), 0));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setNullSelectionAllowed(true);
        cb.setValue(t.getFrom_to_employee_id());
        item.getItemProperty(myUI.getMessage(SptMessages.ToEmployee)).setValue(cb);
        expensesTable.setVisibleColumns(NATURAL_COL_ORDER_EXPENSES);
    }

    private void addExpensesItem() {
        NATURAL_COL_ORDER_EXPENSES = new String[]{SystemSettings.button, myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.Category), myUI.getMessage(SptMessages.Currency),
                myUI.getMessage(SptMessages.Rate), myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.Note), myUI.getMessage(SptMessages.ToEmployee)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (expensesTable.getContainerDataSource().size() == 0) {
            expensesTable.setContainerDataSource(prepareExpensesContainer());
        }
        Item item;
        item = ((IndexedContainer) expensesTable.getContainerDataSource()).addItemAt(0, id);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.addComponent(createButton(myUI.getMessage(SptMessages.DeleteButton), id, myUI.getMessage(SptMessages.Outcomes),
                false, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(SystemSettings.button).setValue(hl);
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                createDateField(new Date(), myUI.getMessage(SptMessages.Date), id, false, myUI.getMessage(SptMessages.Outcomes)));
        item.getItemProperty(myUI.getMessage(SptMessages.Category)).setValue(
                createComboboxCategory(0, myUI.getMessage(SptMessages.Category), id, 2,
                        false, myUI.getMessage(SptMessages.Outcomes)));
        item.getItemProperty(myUI.getMessage(SptMessages.Currency)).setValue(
                createCombobox(2, myUI.getMessage(SptMessages.Currency), id, SystemSettings.dbAcc_currency, false, true, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(createTextfieldDouble(null, myUI.getMessage(SptMessages.Rate), id, true,
                !currentUser.isPermitted(SystemSettings.cnTransactionsView + ":" + SystemSettings.prmChangeCurrencyRate), null));
        item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                createTextfieldDouble(null, myUI.getMessage(SptMessages.Amount), id, false, false, myUI.getMessage(SptMessages.Outcomes)));
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(createTextfieldNote(null, myUI.getMessage(SptMessages.Note), id, false));
        ComboBoxMax cb = createCombobox(0, myUI.getMessage(SptMessages.ToEmployee), id, SystemSettings.dbEmployee,
                false, false, true);
        try {
            DbEmployee dbCon = new DbEmployee();
            dbCon.connect();
            cb.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool_id(), 0));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setNullSelectionAllowed(true);
        item.getItemProperty(myUI.getMessage(SptMessages.ToEmployee)).setValue(cb);
        expensesTable.setVisibleColumns(NATURAL_COL_ORDER_EXPENSES);
    }

    private boolean validateRow(String id) {
        boolean result = true;
        try {
            if (incomesTable.getContainerProperty(id, SystemSettings.button) != null) {
                ((DateField) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Date)).getValue()).validate();
                ((ComboBoxMax) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Category)).getValue()).validate();
                ((ComboBoxMax) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Currency)).getValue()).validate();
                ((TextField) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Rate)).getValue()).validate();
                ((TextField) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Amount)).getValue()).validate();
                ((TextField) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Note)).getValue()).validate();
            } else if (expensesTable.getContainerProperty(id, SystemSettings.button) != null) {
                ((DateField) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Date)).getValue()).validate();
                ((ComboBoxMax) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Category)).getValue()).validate();
                ((ComboBoxMax) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Currency)).getValue()).validate();
                ((TextField) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Rate)).getValue()).validate();
                ((TextField) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Amount)).getValue()).validate();
                ((TextField) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Note)).getValue()).validate();
            }
        } catch (Exception e) {
//            logger.catching(e);
            result = false;
        }

        return result;
    }

    private void execDelete(Button source) {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            if (source.getCaption().equals(myUI.getMessage(SptMessages.Incomes))) {
                DbAccTransactions dbTr = new DbAccTransactions();
                dbTr.connect();
                TextField tf = (TextField) incomesTable.getContainerProperty(source.getData().toString(),
                        myUI.getMessage(SptMessages.Amount)).getValue();
                double amount = (Double) tf.getPropertyDataSource().getValue();
                if ((Integer) ((ComboBoxMax) incomesTable.getContainerProperty(source.getData().toString(),
                        myUI.getMessage(SptMessages.Currency)).getValue()).getValue() == 1) {
                    amount = SystemSettings.round(amount / Double.parseDouble(((TextField) incomesTable.getContainerProperty(
                            source.getData().toString(), myUI.getMessage(SptMessages.Rate)).getValue()).getValue()), 2);
                }
                AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool_id(),
                        ((DateField) incomesTable.getContainerProperty(source.getData().toString(),
                                myUI.getMessage(SptMessages.Date)).getValue()).getValue(),
                        amount, 0.0, 1);
                if (tr != null) {
                    Notification.show(myUI.getMessage(SptMessages.LowBalance) + SystemSettings.dFormat.format(tr.getOverlimit())
                            + " $ (" + SystemSettings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                } else {
                    dbDef.exec_update_emp_id(Integer.parseInt(source.getData().toString()),
                            myUI.getUser().getId(), SystemSettings.dbAcc_transactions);
                    int st = dbDef.exec_delete(Integer.parseInt(source.getData().toString()), SystemSettings.dbAcc_transactions);
                    if (st != 0) {
                        Notification.show(myUI.getMessage(SptMessages.ValueDeleted), Notification.Type.HUMANIZED_MESSAGE);
                        incomesTable.removeItem(source.getData().toString());
                        getTotals();
                        recount();
                    }
                }
                dbTr.close();
            } else {
                dbDef.exec_update_emp_id(Integer.parseInt(source.getData().toString()),
                        myUI.getUser().getId(), SystemSettings.dbAcc_transactions);
                int st = dbDef.exec_delete(Integer.parseInt(source.getData().toString()), SystemSettings.dbAcc_transactions);
                if (st != 0) {
                    Notification.show(myUI.getMessage(SptMessages.ValueDeleted),
                            Notification.Type.HUMANIZED_MESSAGE);
                    expensesTable.removeItem(source.getData().toString());
                    getTotals();
                    recount();
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

    private AccTransaction getTransaction(String id) {
        AccTransaction t = new AccTransaction();
        if (incomesTable.getContainerProperty(id, SystemSettings.button) != null) {
            t.setId(id);
            t.setDate(((DateField) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Date)).getValue()).getValue());
            t.setCategory_id((Integer) ((ComboBoxMax) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Category)).getValue()).getValue());
            t.setCurrency_id((Integer) ((ComboBoxMax) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Currency)).getValue()).getValue());
            t.setCurrency_rate((Double) ((TextField) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Rate)).getValue()).getPropertyDataSource().getValue());
            t.setAmount((Double) ((TextField) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue());
            t.setNote(((TextField) incomesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Note)).getValue()).getValue());
            if (incomesTable.getContainerProperty(id, SystemSettings.from_employee_id).getValue() != null) {
                t.setEmployee(incomesTable.getContainerProperty(id, SystemSettings.from_employee_id).getValue().toString());
            }
            t.setOrder_number((Integer) incomesTable.getContainerProperty(id, SystemSettings.order_number).getValue());
        } else if (expensesTable.getContainerProperty(id, SystemSettings.button) != null) {
            t.setId(id);
            t.setDate(((DateField) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Date)).getValue()).getValue());
            t.setCategory_id((Integer) ((ComboBoxMax) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Category)).getValue()).getValue());
            t.setCurrency_id((Integer) ((ComboBoxMax) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Currency)).getValue()).getValue());
            t.setCurrency_rate((Double) ((TextField) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Rate)).getValue()).getPropertyDataSource().getValue());
            t.setAmount((Double) ((TextField) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue());
            if (((ComboBoxMax) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.ToEmployee)).getValue()).getValue() != null) {
                t.setFrom_to_employee_id((Integer) ((ComboBoxMax) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.ToEmployee)).getValue()).getValue());
            }
            t.setNote(((TextField) expensesTable.getContainerProperty(id, myUI.getMessage(SptMessages.Note)).getValue()).getValue());
            if (expensesTable.getContainerProperty(id, SystemSettings.from_employee_id).getValue() != null) {
                t.setEmployee(expensesTable.getContainerProperty(id, SystemSettings.from_employee_id).getValue().toString());
            }
            t.setOrder_number((Integer) expensesTable.getContainerProperty(id, SystemSettings.order_number).getValue());
        }
        t.setEmployee_id(myUI.getUser().getId());
        t.setSchool_id(myUI.getUser().getSchool_id());
        return t;
    }

    private void updateTable(String item_id) {
        try {
            DbAccTransactions dbta = new DbAccTransactions();
            dbta.connect();
            int update_st = dbta.exec_update(getTransaction(item_id));
            if (update_st != 0) {
                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                        Notification.Type.HUMANIZED_MESSAGE);
                schoolAcc = dbta.exec_get_ttls(myUI.getUser().getSchool_id(),
                        fromDateDF.getValue(), tillDateDF.getValue(), null);
                recount();
                if (incomesTable.getContainerProperty(item_id, SystemSettings.button) != null) {
                    incomesTable.getContainerProperty(item_id, SystemSettings.old_amount).setValue(
                            Double.parseDouble(((TextField) incomesTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.Amount)).getValue()).getValue()));
                    incomesTable.getContainerProperty(item_id, SystemSettings.old_rate).setValue(
                            Double.parseDouble(((TextField) incomesTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.Rate)).getValue()).getValue()));
                    incomesTable.getContainerProperty(item_id, SystemSettings.old_currency).setValue(
                            ((ComboBoxMax) incomesTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.Currency)).getValue()).getValue());
                    incomesTable.getContainerProperty(item_id, SystemSettings.old_date).setValue(
                            ((DateField) incomesTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.Date)).getValue()).getValue());
                    incomesTable.getContainerProperty(item_id, SystemSettings.from_employee_id).setValue(myUI.getUser().getFullname());
                } else if (expensesTable.getContainerProperty(item_id, SystemSettings.button) != null) {
                    expensesTable.getContainerProperty(item_id, SystemSettings.old_amount).setValue(
                            Double.parseDouble(((TextField) expensesTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.Amount)).getValue()).getValue()));
                    expensesTable.getContainerProperty(item_id, SystemSettings.old_rate).setValue(
                            Double.parseDouble(((TextField) expensesTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.Rate)).getValue()).getValue()));
                    expensesTable.getContainerProperty(item_id, SystemSettings.old_currency).setValue(
                            (Integer) ((ComboBoxMax) expensesTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.Currency)).getValue()).getValue());
                    expensesTable.getContainerProperty(item_id, SystemSettings.from_employee_id).setValue(myUI.getUser().getFullname());
                }
            } else {
                AccTransaction accTr = getTransaction(item_id);
                int new_id = dbta.exec_insert(accTr, dbta.getConnection());
                if (new_id != 0) {
                    if (incomesTable.getContainerProperty(item_id, SystemSettings.button) != null) {
                        incomesTable.removeItem(item_id);
                        updateIncomesItem(new_id, accTr);
                    } else if (expensesTable.getContainerProperty(item_id, SystemSettings.button) != null) {
                        expensesTable.removeItem(item_id);
                        updateExpensesItem(new_id, accTr);
                    }
                    Notification.show(myUI.getMessage(SptMessages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                    schoolAcc = dbta.exec_get_ttls(myUI.getUser().getSchool_id(),
                            fromDateDF.getValue(), tillDateDF.getValue(), null);
                    recount();
                }
            }
            dbta.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void recount() {
        incomeTtlLab.setValue(myUI.getMessage(SptMessages.IncomesTotal) + ": " + SystemSettings.round(schoolAcc.getTotal_income(), 2) + "$");
        expenseTtlLab.setValue(myUI.getMessage(SptMessages.ExpensesTotal) + ": " + SystemSettings.round(schoolAcc.getTotal_outcome(), 2) + "$");
        ttlLab.setValue("<b>" + myUI.getMessage(SptMessages.Transactions) + ": " + SystemSettings.round(
                (schoolAcc.getPrevious_balance() + schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome()), 2) + "$" + "</b>");
        Calendar c = Calendar.getInstance();
        c.setTime(fromDateDF.getValue());
        c.add(Calendar.DAY_OF_MONTH, -1);
        prev_balanceLab.setValue(myUI.getMessage(SptMessages.Balance) + " (" + SystemSettings.df.format(c.getTime()) + "): "
                + SystemSettings.round(schoolAcc.getPrevious_balance(), 2) + "$");
    }

    private void getTotals() {
        try {
            DbAccTransactions dbAc = new DbAccTransactions();
            dbAc.connect();
            schoolAcc = dbAc.exec_get_ttls(myUI.getUser().getSchool_id(),
                    fromDateDF.getValue(), tillDateDF.getValue(), null);
            dbAc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
