/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports.stock;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import kg.alex.spt.tableexport.EnhancedFormatExcelExport;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.*;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTreeTable;

import java.util.Date;

public class ProductMovementsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(ProductMovementsReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, excelBtn;
    private HorizontalSplitPanel splitPanel;
    private ComboBoxMax schoolSelect, stockSelect, categorySelect;
    private GridLayout leftGrid;
    private DateField fromDateDF, tillDateDF;
    private FormattedTable dataTable;
    private FilterTreeTable productsTable;

    private EnhancedFormatExcelExport excelReport;

    public ProductMovementsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        leftGrid = new GridLayout(4, 6);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        productsTable = new FilterTreeTable();
        productsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        productsTable.setStyleName(ValoTheme.TABLE_SMALL);
        productsTable.setSizeFull();
        productsTable.setNullSelectionAllowed(false);
        productsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        productsTable.setFilterBarVisible(true);
        productsTable.setFooterVisible(false);
        productsTable.setSelectable(true);
        productsTable.setNullSelectionAllowed(false);
        productsTable.addValueChangeListener(this);

        generateBtn = new Button(myUI.getMessage(SptMessages.ShowButton));
        generateBtn.setWidth("100%");
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(SptMessages.ExportToExcel));
        excelBtn.setWidth("100%");
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        fromDateDF = new DateField(myUI.getMessage(SptMessages.FromDate));
        fromDateDF.setWidth("100%");
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        fromDateDF.setRequired(true);
        fromDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setValue(new Date());
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(SptMessages.TillDate));
        tillDateDF.setWidth("100%");
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setRequired(true);
        tillDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setValue(new Date());
        tillDateDF.addValueChangeListener(this);
        tillDateDF.addValueChangeListener(this);

        stockSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Stocks));
        stockSelect.setNullSelectionAllowed(false);
        stockSelect.setRequired(true);
        stockSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        stockSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        stockSelect.setWidth("100%");
        stockSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        stockSelect.setFilteringMode(FilteringMode.CONTAINS);
        stockSelect.addValueChangeListener(this);

        categorySelect = new ComboBoxMax(myUI.getMessage(SptMessages.ProductCategory));
        categorySelect.setNullSelectionAllowed(false);
        categorySelect.setRequired(true);
        categorySelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        categorySelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        categorySelect.setWidth("100%");
        categorySelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        categorySelect.setFilteringMode(FilteringMode.CONTAINS);
        categorySelect.addValueChangeListener(this);


        schoolSelect = new ComboBoxMax(myUI.getMessage(SptMessages.School));
        schoolSelect.setNullSelectionAllowed(false);
        schoolSelect.setRequired(true);
        schoolSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        schoolSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        schoolSelect.setWidth("100%");
        schoolSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        schoolSelect.setFilteringMode(FilteringMode.CONTAINS);
        schoolSelect.addValueChangeListener(this);
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            schoolSelect.setContainerDataSource(dbs.execSchoolSel(myUI, 1));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        schoolSelect.setValue(myUI.getUser().getSchool_id());

        try {
            DbProductCategories dbpc = new DbProductCategories();
            dbpc.connect();
            categorySelect.setContainerDataSource(dbpc.execSQL_for_select(myUI));
            dbpc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        stockSelect.setValue(Settings.convertToSet(stockSelect.getContainerDataSource().getItemIds()));

        leftGrid.addComponent(schoolSelect, 0, 0, 3, 0);
        leftGrid.addComponent(fromDateDF, 0, 1, 1, 1);
        leftGrid.addComponent(tillDateDF, 2, 1, 3, 1);
        leftGrid.addComponent(stockSelect, 0, 2, 3, 2);
        leftGrid.addComponent(categorySelect, 0, 3, 3, 3);
        leftGrid.addComponent(productsTable, 0, 4, 3, 4);
        leftGrid.addComponent(generateBtn, 0, 5, 2, 5);
        leftGrid.addComponent(excelBtn, 3, 5);
        leftGrid.setRowExpandRatio(4, 1);
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
            if (productsTable.getValue() != null && stockSelect.isValid() && fromDateDF.isValid() && tillDateDF.isValid()) {
                try {
                    DbStockMovements dbCon = new DbStockMovements();
                    dbCon.connect();
                    dbCon.exec_product_movements(myUI, (Integer) productsTable.getValue(), fromDateDF.getValue(),
                            tillDateDF.getValue(), dataTable, (Integer) stockSelect.getValue());

                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Rate), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.StockOutcome), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.StockIncome), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Price), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Balance), Table.Align.RIGHT);

                    if (dataTable.getContainerDataSource().size() != 0) {
                        excelBtn.setEnabled(true);
                    }
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    excelReport = new EnhancedFormatExcelExport(dataTable);
                    excelReport.setReportTitle(myUI.getMessage(SptMessages.ProductMovementsReport) + " "
                            + productsTable.getContainerProperty(productsTable.getValue(),
                            myUI.getMessage(SptMessages.Title)).getValue() + " [" + fromDateDF.getCaption().toLowerCase() + " "
                            + Settings.df.format(fromDateDF.getValue())
                            + " " + tillDateDF.getCaption().toLowerCase() + " " + Settings.df.format(tillDateDF.getValue()) + "]");
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(5).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(6).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(7).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(7).setCellValue(dataTable.getColumnFooter(myUI.getMessage(SptMessages.Amount)));
                    excelReport.getTotalsRow().getCell(10).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(10).setCellValue(dataTable.getColumnFooter(myUI.getMessage(SptMessages.Balance)));
                    excelReport.sendConverted();
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
        if (excelBtn.isEnabled()) {
            if (property == productsTable || property == schoolSelect ||
                    property == tillDateDF || property == fromDateDF
                    || property == stockSelect || property == categorySelect) {
                excelBtn.setEnabled(false);
                dataTable.setContainerDataSource(null);
            }
        }
        if (property == categorySelect) {
            if (categorySelect.getValue() != null) {
                try {
                    DbAccCategory dbCon = new DbAccCategory();
                    dbCon.connect();
                    dbCon.execSQL_for_select_as_tree(myUI, productsTable, categorySelect.getValue() + "");
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            } else {
                productsTable.getContainerDataSource().removeAllItems();
            }
        } else if (property == schoolSelect) {
            try {
                DbDefinition dbCon = new DbDefinition();
                dbCon.connect();
                stockSelect.setContainerDataSource(dbCon.exec_for_select(myUI,
                        Settings.dbStock, (Integer) schoolSelect.getValue(), true));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }
}
