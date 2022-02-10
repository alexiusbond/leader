/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbAccTransactions;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.pdf.CurrentAccountStatementPdf;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTreeTable;

import java.util.Date;

public class CurrentAccountStatementReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(CurrentAccountStatementReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, pdfBtn;
    private HorizontalSplitPanel splitPanel;
    private ComboBoxMax currencySelect;
    private GridLayout leftGrid;
    private DateField fromDateDF, tillDateDF;
    public FormattedTable dataTable;
    public FilterTreeTable employeeCategoriesTable;

    public CurrentAccountStatementReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        leftGrid = new GridLayout(4, 4);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        employeeCategoriesTable = new FilterTreeTable();
        employeeCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        employeeCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        employeeCategoriesTable.setSizeFull();
        employeeCategoriesTable.setNullSelectionAllowed(false);
        employeeCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        employeeCategoriesTable.setFilterBarVisible(true);
        employeeCategoriesTable.setFooterVisible(false);
        employeeCategoriesTable.setSelectable(true);
        employeeCategoriesTable.setNullSelectionAllowed(false);
        employeeCategoriesTable.addValueChangeListener(this);

        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            dbac.execSQL_for_select_as_tree(myUI, 2, employeeCategoriesTable, Integer.toString(myUI.getUser().getSchool_id()), false);
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        generateBtn = new Button(myUI.getMessage(SptMessages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        pdfBtn = new Button();
        pdfBtn.setDescription(myUI.getMessage(SptMessages.ExportToPdf));
        pdfBtn.setWidth(Settings.PERCENTS100);
        pdfBtn.setEnabled(false);
        pdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        pdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        pdfBtn.addClickListener(this);

        fromDateDF = new DateField(myUI.getMessage(SptMessages.FromDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        fromDateDF.setRequired(true);
        fromDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setValue(new Date());
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(SptMessages.TillDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setRequired(true);
        tillDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setValue(new Date());
        tillDateDF.addValueChangeListener(this);

        currencySelect = new ComboBoxMax(myUI.getMessage(SptMessages.Currency));
        currencySelect.setNullSelectionAllowed(false);
        currencySelect.setRequired(true);
        currencySelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        currencySelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        currencySelect.setWidth(Settings.PERCENTS100);
        currencySelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        currencySelect.setFilteringMode(FilteringMode.CONTAINS);
        currencySelect.addValueChangeListener(this);
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            currencySelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbAcc_currency, true));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        currencySelect.setValue(2);

        leftGrid.addComponent(fromDateDF, 0, 0, 1, 0);
        leftGrid.addComponent(tillDateDF, 2, 0, 3, 0);
        leftGrid.addComponent(currencySelect, 0, 1, 3, 1);
        leftGrid.addComponent(employeeCategoriesTable, 0, 2, 3, 2);
        leftGrid.addComponent(generateBtn, 0, 3, 2, 3);
        leftGrid.addComponent(pdfBtn, 3, 3);
        leftGrid.setRowExpandRatio(2, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(true);
        vl.setSizeFull();
        dataTable = new FormattedTable();
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.addStyleName("noWrapHeader");
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (employeeCategoriesTable.getValue() != null && currencySelect.isValid() && fromDateDF.isValid() && tillDateDF.isValid()) {
                try {
                    DbAccTransactions dbat = new DbAccTransactions();
                    dbat.connect();
                    dbat.exec_current_account_state(myUI, (Integer) employeeCategoriesTable.getValue(), fromDateDF.getValue(),
                            tillDateDF.getValue(), dataTable, (Integer) currencySelect.getValue(), myUI.getUser().getSchool_id());

                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Rate), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Accrual), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Payout), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Balance), Table.Align.RIGHT);

                    if (dataTable.getContainerDataSource().size() != 0) {
                        pdfBtn.setEnabled(true);
                    }
                    dbat.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == pdfBtn) {
            StudentInfoPdf st;
            try {
                DbSchool dbsc = new DbSchool();
                dbsc.connect();
                st = dbsc.execGetSchoolPdf(myUI.getUser().getSchool_id());
                dbsc.close();
                if (st.getScl_address() != null && st.getScl_phone() != null
                        && st.getScl_name_ru() != null) {
                    new CurrentAccountStatementPdf(myUI, dataTable, employeeCategoriesTable.getContainerProperty(employeeCategoriesTable.getValue(),
                            myUI.getMessage(SptMessages.Title)).getValue().toString(),
                            currencySelect.getItemCaption(currencySelect.getValue()), fromDateDF.getValue(), tillDateDF.getValue(), st);
                } else {
                    Notification.show(myUI.getMessage(SptMessages.FillSchoolInfo),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (pdfBtn.isEnabled()) {
            if (property == employeeCategoriesTable || property == tillDateDF || property == fromDateDF || property == currencySelect) {
                pdfBtn.setEnabled(false);
                dataTable.setContainerDataSource(null);
            }
        }
    }
}
