/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;
import de.datenhahn.vaadin.componentrenderer.ComponentRenderer;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbAccTransactions;
import kg.alex.spt.dao.DbCurrencyRate;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbEmployee;
import kg.alex.spt.domain.AccTransaction;
import kg.alex.spt.domain.CurrencyRate;
import kg.alex.spt.domain.SchoolAccounting;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.pdf.TransactionInvoicePDF;
import kg.alex.spt.utils.ValueFromContainerConverter;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.grid.cellrenderers.view.RowIndexRenderer;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Calendar;
import java.util.Date;

public class CashBoxView extends GridLayout implements Button.ClickListener,
        Property.ValueChangeListener, FieldGroup.CommitHandler {

    static final Logger logger = LogManager.getLogger(CashBoxView.class);
    private final MyVaadinUI myUI;
    private Button addButton, saveButton, searchButton;
    private OptionGroup currencySettingsOG;
    private TextField currencyTF;
    private Label incomeTtlLab;
    private Label expenseTtlLab;
    private Label ttlLab;
    private Label prev_balanceLab;
    public SchoolAccounting schoolAcc;
    private DateField fromDateDF, tillDateDF;

    private final Grid expensesGrid, incomesGrid;
    private GeneratedPropertyContainer incomesCont = null, expensesCont = null;
    private int r_table_counter = 1000;
    private final Accordion accordion;
    private final Subject currentUser = SecurityUtils.getSubject();
    private HorizontalLayout currencyHl;
    private ComboBox expensesCategoryCb, incomesCategoryCb, toEmployeesCb;
    private Date today;

    public CashBoxView(MyVaadinUI myUI) {
        this.myUI = myUI;

        setRows(3);
        setColumns(4);
        setSizeFull();
        setMargin(true);
        setSpacing(true);

        buildLabels();
        accordion = new Accordion();
        accordion.setSizeFull();
        incomesGrid = new Grid();
        buildGrid(incomesGrid);
        expensesGrid = new Grid();
        buildGrid(expensesGrid);
        TabSheet.Tab tab = accordion.addTab(incomesGrid, myUI.getMessage(SptMessages.Incomes));
        tab.setId("1");
        tab = accordion.addTab(expensesGrid, myUI.getMessage(SptMessages.Expenses));
        tab.setId("2");
        accordion.setSelectedTab(expensesGrid);
        accordion.addSelectedTabChangeListener((TabSheet.SelectedTabChangeListener)
                event -> setGridData(Integer.parseInt(accordion.getSelectedTab().getId())));
        addComponent(accordion, 0, 2, 3, 2);
        setRowExpandRatio(2, 1);
        getTotals();
        recount();
    }

    private void buildLabels() {

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);
        hl.setSpacing(true);

        fromDateDF = new DateField();
        fromDateDF.setDescription(myUI.getMessage(SptMessages.FromDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setValue(new Date());

        tillDateDF = new DateField();
        tillDateDF.setDescription(myUI.getMessage(SptMessages.TillDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setDateFormat(Settings.datePattern);
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
        incomeTtlLab.setWidth(Settings.PERCENTS100);

        expenseTtlLab = new Label();
        expenseTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        expenseTtlLab.setImmediate(true);
        expenseTtlLab.setWidth(Settings.PERCENTS100);

        ttlLab = new Label();
        ttlLab.setContentMode(ContentMode.HTML);
        ttlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        ttlLab.setImmediate(true);
        ttlLab.setWidth(Settings.PERCENTS100);

        prev_balanceLab = new Label();
        prev_balanceLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        prev_balanceLab.setImmediate(true);
        prev_balanceLab.setSizeFull();

        addButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        addButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        addButton.addStyleName(ValoTheme.BUTTON_SMALL);
        addButton.setIcon(FontAwesome.PLUS_SQUARE);
        addButton.setWidth(Settings.PERCENTS100);
        addButton.addClickListener(this);

        currencyHl = new HorizontalLayout();
        currencyHl.setWidth(Settings.PERCENTS100);
        currencyHl.setSpacing(true);
        currencyHl.setEnabled(currentUser.hasRole(Settings.rnAdmin) && myUI.isManualRate());

        Label currencyLab = new Label();
        currencyLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        currencyLab.setImmediate(true);
        currencyLab.setSizeUndefined();
        currencyLab.setValue(myUI.getMessage(SptMessages.RateUSD));

        currencyTF = new TextField();
        ObjectProperty<Double> property = new ObjectProperty<>(0.0);
        currencyTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        currencyTF.setWidth(Settings.PERCENTS100);
        currencyTF.setNullRepresentation("0.1");
        currencyTF.setNullSettingAllowed(false);
        currencyTF.setConverter(Settings.getStringToDoubleConverter());
        currencyTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
        currencyTF.setPropertyDataSource(property);
        currencyTF.getPropertyDataSource().setValue(myUI.getDb_currency_rate());

        currencyHl.addComponent(currencyLab);
        currencyHl.addComponent(currencyTF);
        currencyHl.setExpandRatio(currencyTF, 1);

        HorizontalLayout currencySettingsHl = new HorizontalLayout();
        currencySettingsHl.setWidth(Settings.PERCENTS100);
        currencySettingsHl.setSpacing(true);
        currencySettingsHl.setEnabled(currentUser.hasRole(Settings.rnAdmin));

        currencySettingsOG = new OptionGroup();
        currencySettingsOG.setWidth(Settings.PERCENTS100);
        currencySettingsOG.setStyleName(ValoTheme.OPTIONGROUP_SMALL);
        currencySettingsOG.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        currencySettingsOG.addItem(myUI.getMessage(SptMessages.NBKR));
        currencySettingsOG.addItem(myUI.getMessage(SptMessages.Manual));
        currencySettingsOG.addValueChangeListener(this);
        if (myUI.isManualRate()) {
            currencySettingsOG.select(myUI.getMessage(SptMessages.Manual));
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
        addComponent(addButton, 0, 1);
        addComponent(prev_balanceLab, 1, 0);
        addComponent(incomeTtlLab, 1, 1);
        addComponent(expenseTtlLab, 2, 1);
        addComponent(ttlLab, 2, 0);
        addComponent(currencyHl, 3, 0);
        addComponent(currencySettingsHl, 3, 1);
        setComponentAlignment(addButton, Alignment.BOTTOM_RIGHT);
        setComponentAlignment(ttlLab, Alignment.MIDDLE_RIGHT);
        setComponentAlignment(prev_balanceLab, Alignment.BOTTOM_LEFT);
        setColumnExpandRatio(0, 2);
        setColumnExpandRatio(1, 4);
        setColumnExpandRatio(2, 4);
        setColumnExpandRatio(3, 2);
    }

    private void buildGrid(Grid grid) {
        int in_out_id = grid == expensesGrid ? 2 : 1;
        grid.setId(in_out_id + "");
        setGridData(in_out_id);
        grid.setSizeFull();
        grid.setEditorEnabled(true);
        grid.setEditorBuffered(true);
        grid.addItemClickListener((ItemClickEvent.ItemClickListener) event -> {
            if (!(Boolean) event.getItem().getItemProperty(Settings.is_disabled).getValue()) {
                grid.setEditorEnabled(true);
                if (!grid.isEditorActive()) {
                    try {
                        grid.editItem(event.getItemId());
                    } catch (Exception ignored) {
                    }
                }
                TextField amountTf = (TextField) grid.getColumn(myUI.getMessage(SptMessages.Amount)).getEditorField();
                amountTf.removeAllValidators();
                amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
            } else {
                grid.setEditorEnabled(false);
            }
        });
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.getColumn(Settings.from_employee_id).setHidden(true);
        grid.getColumn(Settings.is_disabled).setHidden(true);
        grid.getColumn(Settings.order_number).setHidden(true);
        if (grid == expensesGrid) {
            if (expensesCategoryCb == null) {
                expensesCategoryCb = createCombobox(null, true, null);
                try {
                    DbAccTransactions dbac = new DbAccTransactions();
                    dbac.connect();
                    expensesCategoryCb.setContainerDataSource(dbac.exec_for_select(myUI, 2, myUI.getUser().getSchool_id(), 0));
                    dbac.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            grid.getColumn(myUI.getMessage(SptMessages.Category)).setEditorField(expensesCategoryCb);
            if (toEmployeesCb == null) {
                toEmployeesCb = createCombobox(null, false, null);
                try {
                    DbEmployee dbCon = new DbEmployee();
                    dbCon.connect();
                    toEmployeesCb.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool_id(), 0));
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            grid.getColumn(myUI.getMessage(SptMessages.ToEmployee)).setEditorField(toEmployeesCb);
        } else {
            if (incomesCategoryCb == null) {
                incomesCategoryCb = createCombobox(null, true, null);
                try {
                    DbAccTransactions dbac = new DbAccTransactions();
                    dbac.connect();
                    incomesCategoryCb.setContainerDataSource(dbac.exec_for_select(myUI, 1, myUI.getUser().getSchool_id(), 0));
                    dbac.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            grid.getColumn(myUI.getMessage(SptMessages.Category)).setEditorField(incomesCategoryCb);
        }
        grid.getColumn(myUI.getMessage(SptMessages.Currency)).setEditorField(createCombobox(Settings.dbAcc_currency, true, this));
        grid.getColumn(myUI.getMessage(SptMessages.Date)).setEditorField(createDateField(this));
        grid.getColumn(myUI.getMessage(SptMessages.Amount)).setEditorField(createTextField(
                new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.01, null),
                new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(), this));
        grid.getColumn(myUI.getMessage(SptMessages.Rate)).setEditorField(createTextField(
                new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.01, null),
                new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(), this));
        grid.getColumn(myUI.getMessage(SptMessages.Note)).setEditorField(createTextField(
                new StringLengthValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 1, 350, false),
                null, null, null));

        grid.getColumn(Settings.hashTags).setSortable(false);
        grid.getColumn(Settings.hashTags).setEditable(false);
        grid.getColumn(Settings.button).setEditable(false);
        grid.getColumn(Settings.from_employee_id).setEditable(false);
        grid.getColumn(Settings.order_number).setEditable(false);
        grid.getColumn(Settings.is_disabled).setEditable(false);
        grid.getColumn(myUI.getMessage(SptMessages.Rate)).setEditable(
                currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate));
        grid.setEditorSaveCaption(myUI.getMessage(SptMessages.SaveButton));
        grid.setEditorCancelCaption(myUI.getMessage(SptMessages.Cancel));

        grid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (cellReference.getProperty().getType() == Double.class || cellReference.getProperty().getType() == Date.class
                    || cellReference.getPropertyId().equals(Settings.hashTags)
                    || cellReference.getPropertyId().equals(myUI.getMessage(SptMessages.Currency))) {
                return "align-right";
            } else {
                return null;
            }
        });
        grid.setRowStyleGenerator((Grid.RowStyleGenerator) rowReference -> {
            if ((Boolean) rowReference.getItem().getItemProperty(Settings.is_disabled).getValue()) {
                return "yellow";
            }
            if (rowReference.getItemId().toString().contains(Settings.FreshItem)) {
                return "green";
            }
            return "ord";
        });

        grid.getColumn(myUI.getMessage(SptMessages.Date)).setRenderer(new DateRenderer(Settings.dtmf));
        grid.getColumn(myUI.getMessage(SptMessages.Amount)).setRenderer(
                new NumberRenderer(Settings.getNumberFormat()));
        grid.getColumn(myUI.getMessage(SptMessages.Rate)).setRenderer(
                new NumberRenderer(Settings.getNumberFormat()));
        if (grid == expensesGrid) {
            grid.getColumn(myUI.getMessage(SptMessages.Category)).setRenderer(
                    new HtmlRenderer(), new ValueFromContainerConverter((IndexedContainer) expensesCategoryCb.getContainerDataSource(),
                            myUI.getMessage(SptMessages.Category), myUI));
            grid.getColumn(myUI.getMessage(SptMessages.ToEmployee)).setRenderer(
                    new HtmlRenderer(), new ValueFromContainerConverter((IndexedContainer) toEmployeesCb.getContainerDataSource(),
                            myUI.getMessage(SptMessages.ToEmployee), myUI));
        } else {
            grid.getColumn(myUI.getMessage(SptMessages.Category)).setRenderer(
                    new HtmlRenderer(), new ValueFromContainerConverter((IndexedContainer) incomesCategoryCb.getContainerDataSource(),
                            myUI.getMessage(SptMessages.Category), myUI));
        }
        grid.getColumn(myUI.getMessage(SptMessages.Currency)).setRenderer(
                new HtmlRenderer(), new ValueFromContainerConverter((IndexedContainer) ((ComboBox) grid.getEditorFieldGroup().getField(
                        myUI.getMessage(SptMessages.Currency))).getContainerDataSource(), null, myUI));
        RowIndexRenderer rir = new RowIndexRenderer();
        rir.setOffset(1);
        grid.getColumn(Settings.hashTags).setRenderer(rir);
        grid.getColumn(Settings.button).setRenderer(new ComponentRenderer());

        grid.getColumn(Settings.button).setWidth(52);
        grid.getColumn(myUI.getMessage(SptMessages.Date)).setWidth(120);
        grid.getColumn(myUI.getMessage(SptMessages.Rate)).setWidth(80);
        grid.getColumn(myUI.getMessage(SptMessages.Category)).setWidth(350);
        if (grid.getColumn(myUI.getMessage(SptMessages.ToEmployee)) != null) {
            grid.getColumn(myUI.getMessage(SptMessages.ToEmployee)).setWidth(230);
        }
        grid.getColumn(myUI.getMessage(SptMessages.Note)).setExpandRatio(1);
        grid.getEditorFieldGroup().addCommitHandler(this);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == searchButton) {
            if (fromDateDF.getValue() != null && tillDateDF.getValue() != null) {
                setGridData(Integer.parseInt(accordion.getSelectedTab().getId()));
                getTotals();
                recount();
            }
        } else if (source == addButton) {
            addGridItem();
        } else if (source == saveButton) {
            if (currencyTF.isValid()) {
                int st = 0;
                CurrencyRate cr = new CurrencyRate();
                try {
                    DbCurrencyRate dbCon = new DbCurrencyRate();
                    dbCon.connect();
                    cr.setEmployee_id(myUI.getUser().getId());
                    cr.setSchool_id(myUI.getUser().getSchool_id());
                    if (currencySettingsOG.getValue().equals(myUI.getMessage(SptMessages.Manual))) {
                        cr.setValue((Double) currencyTF.getPropertyDataSource().getValue());
                        cr.setManual(1);
                    } else {
                        cr.setManual(0);
                    }
                    st = dbCon.exec_insert(cr);
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                if (st != 0) {
                    if (cr.getManual() == 0) {
                        currencyTF.getPropertyDataSource().setValue(myUI.getCurrencyRateFromBank());
                    }
                    Notification.show(myUI.getMessage(SptMessages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                }
            } else {
                Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue), Notification.Type.WARNING_MESSAGE);
            }
        } else if (source.getData().equals(myUI.getMessage(SptMessages.Print)) && !source.getId().contains(Settings.FreshItem)) {
            Item item;
            AccTransaction tr;
            ComboBox categoryCb;
            int acc_type_id;
            String orderName;
            if (accordion.getSelectedTab() == incomesGrid) {
                item = incomesGrid.getContainerDataSource().getItem(source.getId());
                tr = getTransaction(item, source.getId());
                categoryCb = incomesCategoryCb;
                acc_type_id = 1;
                orderName = myUI.getMessage(SptMessages.IncomeOrder);
            } else {
                item = expensesGrid.getContainerDataSource().getItem(source.getId());
                tr = getTransaction(item, source.getId());
                categoryCb = expensesCategoryCb;
                acc_type_id = 2;
                orderName = myUI.getMessage(SptMessages.ExpenseOrder);
                if (item.getItemProperty(myUI.getMessage(SptMessages.ToEmployee)).getValue() != null
                        && (Integer) item.getItemProperty(myUI.getMessage(SptMessages.ToEmployee)).getValue() != 0) {
                    tr.setFrom_to_employee(toEmployeesCb.getContainerProperty(
                            item.getItemProperty(myUI.getMessage(SptMessages.ToEmployee)).getValue(),
                            myUI.getMessage(SptMessages.Title)).getValue().toString());
                }
            }
            tr.setId(source.getId());
            tr.setCategory(categoryCb.getContainerProperty(
                    item.getItemProperty(myUI.getMessage(SptMessages.Category)).getValue(),
                    myUI.getMessage(SptMessages.Title)).getValue().toString());
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
            item.getItemProperty(Settings.order_number).setValue(tr.getOrder_number());
            new TransactionInvoicePDF(myUI, tr, myUI.getUser().getSchool_name(), myUI.getUser().getSchool_logo(), orderName);
        } else if (source.getData().equals(myUI.getMessage(SptMessages.DeleteButton))) {
            if (source.getId().contains(Settings.FreshItem)) {
                ((Grid) accordion.getSelectedTab()).getContainerDataSource().removeItem(source.getId());
            } else {
                ConfirmDialog.show(myUI, myUI.getMessage(SptMessages.Question),
                        myUI.getMessage(SptMessages.ConfirmDeletion),
                        myUI.getMessage(SptMessages.Yes),
                        myUI.getMessage(SptMessages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                execDelete(source);
                            }
                        });
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == currencySettingsOG) {
            currencyHl.setEnabled(currencySettingsOG.getValue().equals(myUI.getMessage(SptMessages.Manual)));
        } else {

            Grid grid = (Grid) accordion.getSelectedTab();
            TextField rateTf = null;

            if (grid != null && grid.isEditorActive() && grid.getEditedItemId() != null) {
                TextField amountTf = (TextField) grid.getEditorFieldGroup().getField(myUI.getMessage(SptMessages.Amount));
                String itemId = grid.getEditedItemId().toString();
                ComboBox currencyCb = (ComboBox) grid.getEditorFieldGroup().getField(myUI.getMessage(SptMessages.Currency));
                if (currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate)) {
                    rateTf = (TextField) grid.getEditorFieldGroup().getField(myUI.getMessage(SptMessages.Rate));
                }
                DateField dateDf = (DateField) grid.getEditorFieldGroup().getField(myUI.getMessage(SptMessages.Date));
                Item item = grid.getContainerDataSource().getItem(itemId);

                if (accordion.getSelectedTab() == expensesGrid) {
                    if (amountTf.isValid() && amountTf.getValue() != null &&
                            (!currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate) ||
                                    rateTf.isValid() && rateTf.getValue() != null) && dateDf.isValid() && dateDf.getValue() != null) {
                        try {
                            double amount = Settings.dFormat.parse(amountTf.getValue()).doubleValue();
                            double rate = currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate) ?
                                    Settings.dFormat.parse(rateTf.getValue()).doubleValue() :
                                    (Double) item.getItemProperty(myUI.getMessage(SptMessages.Rate)).getValue();
                            boolean isKGS = (Integer) currencyCb.getValue() == 1;
                            if (isKGS) {
                                if (currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate)) {
                                    amount = Settings.round(amount / rate, 2);
                                } else {
                                    amount = Settings.round(amount / (Double) item.getItemProperty(myUI.getMessage(SptMessages.Rate)).getValue(), 2);
                                }
                            }
                            if (amount > 0.0) {
                                double old_amount = (Double) item.getItemProperty(myUI.getMessage(SptMessages.Amount)).getValue();
                                if ((Integer) item.getItemProperty(myUI.getMessage(SptMessages.Currency)).getValue() == 1) {
                                    old_amount = Settings.round(old_amount / (Double) item.getItemProperty(myUI.getMessage(SptMessages.Rate)).getValue(), 2);
                                }
                                DbAccTransactions dbTr = new DbAccTransactions();
                                dbTr.connect();
                                AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool_id(),
                                        dateDf.getValue(), old_amount, amount, 2);
                                amountTf.removeAllValidators();
                                if (tr != null) {
                                    double limit = tr.getLimit();
                                    if (isKGS) {
                                        limit = limit * rate;
                                    }
                                    amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.LowBalance) + Settings.dFormat.format(tr.getOverLimit())
                                            + " $ (" + Settings.df.format(tr.getDate()) + ")", 0.1, Settings.round(limit, 2)));
                                    Notification.show(myUI.getMessage(SptMessages.LowBalance) + Settings.dFormat.format(tr.getOverLimit())
                                            + " $ (" + Settings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                                } else {
                                    amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                                }
                                dbTr.close();
                            } else {
                                amountTf.removeAllValidators();
                                amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                            }
                        } catch (Exception e) {
                            amountTf.removeAllValidators();
                            amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                            logger.error(e);
                            logger.catching(e);
                        }
                    } else {
                        amountTf.removeAllValidators();
                        amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                    }
                } else if (accordion.getSelectedTab() == incomesGrid && !itemId.contains(Settings.FreshItem)) {
                    if (amountTf.isValid() && amountTf.getValue() != null &&
                            (!currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate) ||
                                    rateTf.isValid() && rateTf.getValue() != null) && dateDf.isValid() && dateDf.getValue() != null) {
                        try {
                            double amount = Settings.dFormat.parse(amountTf.getValue()).doubleValue();
                            double rate;
                            rate = currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate) ?
                                    Settings.dFormat.parse(rateTf.getValue()).doubleValue() :
                                    (Double) item.getItemProperty(myUI.getMessage(SptMessages.Rate)).getValue();
                            boolean isKGS = (Integer) currencyCb.getValue() == 1;
                            if (isKGS) {
                                if (currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate)) {
                                    amount = Settings.round(amount / rate, 2);
                                } else {
                                    amount = Settings.round(amount / (Double) item.getItemProperty(myUI.getMessage(SptMessages.Rate)).getValue(), 2);
                                }
                            }
                            if (amount > 0.0) {
                                double old_amount = (Double) item.getItemProperty(myUI.getMessage(SptMessages.Amount)).getValue();
                                if ((Integer) item.getItemProperty(myUI.getMessage(SptMessages.Currency)).getValue() == 1) {
                                    old_amount = Settings.round(old_amount / (Double) item.getItemProperty(myUI.getMessage(SptMessages.Rate)).getValue(), 2);
                                }
                                if (amount <= old_amount || DateUtils.truncate(dateDf.getValue(), Calendar.DAY_OF_MONTH)
                                        .compareTo((Date) item.getItemProperty(myUI.getMessage(SptMessages.Date)).getValue()) != 0) {
                                    DbAccTransactions dbTr = new DbAccTransactions();
                                    dbTr.connect();
                                    AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool_id(),
                                            dateDf.getValue(), old_amount, amount, 1);
                                    amountTf.removeAllValidators();
                                    if (tr != null) {
                                        double limit = tr.getLimit();
                                        if (isKGS) {
                                            limit = limit * rate;
                                        }
                                        amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.LowBalance) + Settings.dFormat.format(tr.getOverLimit())
                                                + " $ (" + Settings.df.format(tr.getDate()) + ")", Settings.round(limit, 2), null));
                                        Notification.show(myUI.getMessage(SptMessages.LowBalance) + Settings.dFormat.format(tr.getOverLimit())
                                                + " $ (" + Settings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                                    } else {
                                        amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                                    }
                                    dbTr.close();
                                } else {
                                    amountTf.removeAllValidators();
                                    amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), amount, null));
                                }
                            } else {
                                amountTf.removeAllValidators();
                                amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                            }
                        } catch (Exception e) {
                            amountTf.removeAllValidators();
                            amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                            logger.error(e);
                            logger.catching(e);
                        }
                    } else {
                        amountTf.removeAllValidators();
                        amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                    }
                } else {
                    amountTf.removeAllValidators();
                    amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                }
            }
        }
    }

    private void setGridData(int in_out) {
        try {
            DbAccTransactions dbCon = new DbAccTransactions();
            dbCon.connect();
            if (in_out == 1) {
                dbCon.execSQL(myUI, in_out, myUI.getUser().getSchool_id(), incomesGrid, this, fromDateDF.getValue(),
                        tillDateDF.getValue());
            } else {
                dbCon.execSQL(myUI, in_out, myUI.getUser().getSchool_id(), expensesGrid, this, fromDateDF.getValue(),
                        tillDateDF.getValue());
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

    }

    public Button createButton(String description, Object itemId, String data, boolean isDisabled, FontAwesome icon, String style) {
        Button btn = new Button();
        if (isDisabled) {
            btn.setEnabled(false);
        }
        btn.setData(data);
        btn.setDescription(description);
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.setStyleName(ValoTheme.BUTTON_LINK);
        btn.addStyleName(ValoTheme.BUTTON_TINY);
        if (style != null) {
            btn.addStyleName(style);
        }
        btn.setIcon(icon);
        btn.setId(itemId.toString());
        btn.addClickListener(this);
        return btn;
    }

    public DateField createDateField(Property.ValueChangeListener valueChangeListener) {
        DateField df = new DateField();
        df.setRangeEnd(new Date());
        df.setRequired(true);
        df.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        df.setStyleName(ValoTheme.DATEFIELD_TINY);
        df.setWidth(Settings.PERCENTS100);
        df.setDateFormat(Settings.dateTimeMinPattern);
        df.setResolution(Resolution.MINUTE);
        df.setRangeStart(myUI.getUser().getTransactions_start_date());
        if (valueChangeListener != null) {
            df.addValueChangeListener(valueChangeListener);
        }
        return df;
    }

    public ComboBox createCombobox(String dbTable, boolean isRequired, Property.ValueChangeListener valueChangeListener) {
        ComboBox cb = new ComboBox();
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
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
        if (valueChangeListener != null) {
            cb.addValueChangeListener(valueChangeListener);
        }
        return cb;
    }

    public TextField createTextField(Validator validator, ObjectProperty<?> property, Converter<String, ?> converter,
                                     Property.ValueChangeListener valueChangeListener) {
        TextField tf = new TextField();
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        if (validator != null) {
            tf.addValidator(validator);
        }
        tf.setRequired(true);
        tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        if (property != null) {
            tf.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
            tf.setPropertyDataSource(property);
            tf.setNullRepresentation("0.0");
            tf.setNullSettingAllowed(false);
            tf.setConverter(converter);
        } else {
            tf.setNullRepresentation("");
        }
        tf.setValue(null);
        if (valueChangeListener != null) {
            tf.addValueChangeListener(valueChangeListener);
        }
        return tf;
    }

    public GeneratedPropertyContainer prepareIncomesContainer() {
        if (incomesCont == null) {
            incomesCont = new GeneratedPropertyContainer(prepareContainerProperties());
        } else {
            incomesCont.removeAllItems();
        }
        return incomesCont;
    }

    public GeneratedPropertyContainer prepareExpensesContainer() {
        if (expensesCont == null) {
            IndexedContainer cont = prepareContainerProperties();
            cont.addContainerProperty(myUI.getMessage(SptMessages.ToEmployee), Integer.class, null);
            expensesCont = new GeneratedPropertyContainer(cont);
        } else {
            expensesCont.removeAllItems();
        }
        return expensesCont;
    }

    private IndexedContainer prepareContainerProperties() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(Settings.button, HorizontalLayout.class, null);
        container.addContainerProperty(Settings.hashTags, String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Date), Date.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Category), Integer.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Currency), Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Rate), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(SptMessages.Note), String.class, null);
        container.addContainerProperty(Settings.from_employee_id, String.class, null);
        container.addContainerProperty(Settings.order_number, Integer.class, 0);
        container.addContainerProperty(Settings.is_disabled, Boolean.class, false);
        return container;
    }

    private void addGridItem() {
        String id = Settings.FreshItem + (--r_table_counter);
        if (today == null) {
            today = new Date();
        }
        Item item;
        if (accordion.getSelectedTab() == incomesGrid) {
            item = incomesCont.addItemAt(0, id);
        } else {
            item = expensesCont.addItemAt(0, id);
            item.getItemProperty(Settings.from_employee_id).setValue(myUI.getUser().getFullName());
        }
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(today);
        item.getItemProperty(myUI.getMessage(SptMessages.Currency)).setValue(1);
        item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(myUI.getDb_currency_rate());
    }

    private void copyItemAndDelete(String oldItemId, String itemId) {
        Container.Indexed container;
        if (accordion.getSelectedTab() == incomesGrid) {
            container = incomesCont.getWrappedContainer();
        } else {
            container = expensesCont.getWrappedContainer();
        }
        Item item = container.addItemAt(0, itemId);
        for (Object propId : container.getContainerPropertyIds()) {
            item.getItemProperty(propId).setValue(container.getItem(oldItemId).getItemProperty(propId).getValue());
        }
        container.removeItem(oldItemId);
    }

    private void execDelete(Button source) {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            if (accordion.getSelectedTab() == incomesGrid) {
                DbAccTransactions dbTr = new DbAccTransactions();
                dbTr.connect();
                double amount = (Double) incomesCont.getContainerProperty(source.getId(),
                        myUI.getMessage(SptMessages.Amount)).getValue();
                if ((Integer) incomesCont.getContainerProperty(source.getId(),
                        myUI.getMessage(SptMessages.Currency)).getValue() == 1) {
                    amount = Settings.round(amount / (Double) incomesCont.getContainerProperty(source.getId(),
                            myUI.getMessage(SptMessages.Rate)).getValue(), 2);
                }
                AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool_id(),
                        (Date) incomesCont.getContainerProperty(source.getId(),
                                myUI.getMessage(SptMessages.Date)).getValue(), amount, 0.0, 1);
                if (tr != null) {
                    Notification.show(myUI.getMessage(SptMessages.LowBalance) + Settings.dFormat.format(tr.getOverLimit())
                            + " $ (" + Settings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                } else {
                    dbDef.exec_update_emp_id(Integer.parseInt(source.getId()),
                            myUI.getUser().getId(), Settings.dbAcc_transactions);
                    int st = dbDef.exec_delete(Integer.parseInt(source.getId()), Settings.dbAcc_transactions);
                    if (st != 0) {
                        Notification.show(myUI.getMessage(SptMessages.ValueDeleted), Notification.Type.HUMANIZED_MESSAGE);
                        incomesCont.removeItem(source.getId());
                        getTotals();
                        recount();
                    }
                }
                dbTr.close();
            } else {
                dbDef.exec_update_emp_id(Integer.parseInt(source.getId()),
                        myUI.getUser().getId(), Settings.dbAcc_transactions);
                int st = dbDef.exec_delete(Integer.parseInt(source.getId()), Settings.dbAcc_transactions);
                if (st != 0) {
                    Notification.show(myUI.getMessage(SptMessages.ValueDeleted),
                            Notification.Type.HUMANIZED_MESSAGE);
                    expensesCont.removeItem(source.getId());
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

    private AccTransaction getTransaction(Item item, String id) {
        AccTransaction t = new AccTransaction();
        t.setId(id);
        t.setDate((Date) item.getItemProperty(myUI.getMessage(SptMessages.Date)).getValue());
        t.setCategory_id((Integer) item.getItemProperty(myUI.getMessage(SptMessages.Category)).getValue());
        t.setCurrency_id((Integer) item.getItemProperty(myUI.getMessage(SptMessages.Currency)).getValue());
        t.setCurrency_rate((Double) item.getItemProperty(myUI.getMessage(SptMessages.Rate)).getValue());
        t.setAmount((Double) item.getItemProperty(myUI.getMessage(SptMessages.Amount)).getValue());
        if (item.getItemProperty(myUI.getMessage(SptMessages.ToEmployee)) != null &&
                item.getItemProperty(myUI.getMessage(SptMessages.ToEmployee)).getValue() != null) {
            t.setFrom_to_employee_id((Integer) item.getItemProperty(myUI.getMessage(SptMessages.ToEmployee)).getValue());
        }
        t.setNote(item.getItemProperty(myUI.getMessage(SptMessages.Note)).getValue().toString());
        if (item.getItemProperty(Settings.from_employee_id).getValue() != null) {
            t.setEmployee(item.getItemProperty(Settings.from_employee_id).getValue().toString());
        }
        t.setOrder_number((Integer) item.getItemProperty(Settings.order_number).getValue());
        t.setEmployee_id(myUI.getUser().getId());
        t.setSchool_id(myUI.getUser().getSchool_id());
        return t;
    }

    private void recount() {
        incomeTtlLab.setValue(myUI.getMessage(SptMessages.IncomesTotal) + ": " + Settings.dFormat.format(schoolAcc.getTotal_income()) + "$");
        expenseTtlLab.setValue(myUI.getMessage(SptMessages.ExpensesTotal) + ": " + Settings.dFormat.format(schoolAcc.getTotal_outcome()) + "$");
        ttlLab.setValue("<b>" + myUI.getMessage(SptMessages.CashBox) + ": " + Settings.dFormat.format(
                (schoolAcc.getPrevious_balance() + schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome())) + "$" + "</b>");
        Calendar c = Calendar.getInstance();
        c.setTime(fromDateDF.getValue());
        c.add(Calendar.DAY_OF_MONTH, -1);
        prev_balanceLab.setValue(myUI.getMessage(SptMessages.PreviousBalance) + " (" + Settings.df.format(c.getTime()) + "): "
                + Settings.dFormat.format(schoolAcc.getPrevious_balance()) + "$");
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

    @Override
    public void preCommit(FieldGroup.CommitEvent commitEvent) {
    }

    @Override
    public void postCommit(FieldGroup.CommitEvent commitEvent) {
        int status;
        Grid grid = (Grid) accordion.getSelectedTab();

        String itemId = grid.getEditedItemId().toString();
        AccTransaction tr = getTransaction(grid.getContainerDataSource().getItem(itemId), itemId);

        try {
            DbAccTransactions dbCon = new DbAccTransactions();
            dbCon.connect();
            if (itemId.contains(Settings.FreshItem)) {
                status = dbCon.exec_insert(tr, dbCon.getConnection());
                copyItemAndDelete(itemId, status + "");
                itemId = status + "";
            } else {
                status = dbCon.exec_update(tr);
            }

            if (status != 0) {
                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                        Notification.Type.HUMANIZED_MESSAGE);
                Item item = grid.getContainerDataSource().getItem(itemId);
                item.getItemProperty(Settings.from_employee_id).setValue(myUI.getUser().getFullName());
                schoolAcc = dbCon.exec_get_ttls(myUI.getUser().getSchool_id(),
                        fromDateDF.getValue(), tillDateDF.getValue(), null);
                recount();
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
