/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbAccTransactions;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.tableexport.EnhancedFormatExcelExport;
import kg.alex.spt.utils.FormattedTreeTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTreeTable;

public class PayoutsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(PayoutsReport.class);
    private final MyVaadinUI myUI;
    private Button generateBtn, excelBtn, selectAllBtn, deselectAllBtn;
    private final HorizontalSplitPanel splitPanel;
    private ComboBox currencySelect;
    public FormattedTreeTable dataTable;
    public FilterTreeTable employeeCategoriesTable;

    public PayoutsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 4);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        employeeCategoriesTable = new FilterTreeTable();
        employeeCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        employeeCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        employeeCategoriesTable.setSizeFull();
        employeeCategoriesTable.setNullSelectionAllowed(false);
        employeeCategoriesTable.setMultiSelect(true);
        employeeCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        employeeCategoriesTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        employeeCategoriesTable.setFilterBarVisible(true);
        employeeCategoriesTable.setFooterVisible(false);
        employeeCategoriesTable.setSelectable(true);
        employeeCategoriesTable.setNullSelectionAllowed(false);
        employeeCategoriesTable.addValueChangeListener(this);

        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            dbac.execSQL_for_select_as_tree(myUI, "2", employeeCategoriesTable,
                    Integer.toString(myUI.getUser().getSchool().getId()), true);
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        employeeCategoriesTable.setVisibleColumns(myUI.getMessage(SptMessages.Title));

        generateBtn = new Button(myUI.getMessage(SptMessages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(SptMessages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        currencySelect = new ComboBox(myUI.getMessage(SptMessages.Currency));
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

        selectAllBtn = new Button(myUI.getMessage(SptMessages.AllCategories));
        selectAllBtn.setWidth(Settings.PERCENTS100);
        selectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllBtn.addClickListener(this);

        deselectAllBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllBtn.setWidth(Settings.PERCENTS100);
        deselectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllBtn.addClickListener(this);

        leftGrid.addComponent(currencySelect, 0, 0, 3, 0);
        leftGrid.addComponent(selectAllBtn, 0, 1, 1, 1);
        leftGrid.addComponent(deselectAllBtn, 2, 1, 3, 1);
        leftGrid.addComponent(employeeCategoriesTable, 0, 2, 3, 2);
        leftGrid.addComponent(generateBtn, 0, 3, 2, 3);
        leftGrid.addComponent(excelBtn, 3, 3);
        leftGrid.setRowExpandRatio(2, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(true);
        vl.setSizeFull();
        dataTable = new FormattedTreeTable(myUI);
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
            if (employeeCategoriesTable.getValue() != null && currencySelect.isValid()) {
                try {
                    DbAccTransactions dbat = new DbAccTransactions();
                    dbat.connect();
                    dbat.exec_account_remains(myUI, employeeCategoriesTable,
                            (Integer) currencySelect.getValue(), myUI.getUser().getSchool().getId(), dataTable);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Remain), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Salary), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Ratio), Table.Align.RIGHT);

                    if (dataTable.getContainerDataSource().size() != 0) {
                        excelBtn.setEnabled(true);
                    }
                    dbat.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            try {
                EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable, myUI.getMessage(SptMessages.SalariesReport) + " ("
                        + currencySelect.getItemCaption(currencySelect.getValue()) + ")");
                excelReport.setReportTitle(myUI.getMessage(SptMessages.SalariesReport) + " ("
                        + currencySelect.getItemCaption(currencySelect.getValue()) + ")");
                excelReport.setDisplayTotals(true);
                excelReport.convertTable();
                excelReport.getTotalsRow().getCell(excelReport.getTotalsRow().getLastCellNum() - 1).setCellFormula(null);
                excelReport.getTotalsRow().getCell(excelReport.getTotalsRow().getLastCellNum() - 1).setCellValue(
                        dataTable.getColumnFooter(myUI.getMessage(SptMessages.Ratio)));
                excelReport.sendConverted();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllBtn) {
            employeeCategoriesTable.setValue(employeeCategoriesTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            employeeCategoriesTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (excelBtn.isEnabled()) {
            if (property == employeeCategoriesTable || property == currencySelect) {
                excelBtn.setEnabled(false);
                dataTable.getContainerDataSource().removeAllItems();
            }
        }
    }
}
