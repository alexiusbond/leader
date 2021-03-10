package kg.alex.spt.reports;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.addon.tableexport.EnhancedFormatExcelExport;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbProductCategories;
import kg.alex.spt.dao.DbStockMovements;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import kg.alex.spt.utils.FormattedTreeTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTreeTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Date;
import java.util.Set;

public class StockGeneralReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(StockGeneralReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, excelBtn, selectAllBtn, deselectAllBtn;
    private HorizontalSplitPanel spltPanel;
    private ComboBoxMultiselectMax stocksMSB;
    private OptionGroup operationOG;
    private GridLayout leftGrid;
    private DateField fromDateDF, tillDateDF;
    private FormattedTreeTable dataTable;
    private FilterTreeTable productsTable;
    private EnhancedFormatExcelExport excelReport;

    public StockGeneralReport(final MyVaadinUI ui, final HorizontalSplitPanel spltPanel) {
        this.myUI = ui;
        this.spltPanel = spltPanel;
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
        productsTable.setMultiSelect(true);
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
        fromDateDF.setDateFormat(SystemSettings.datePattern);
        fromDateDF.setValue(new Date());
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(SptMessages.TillDate));
        tillDateDF.setWidth("100%");
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setRequired(true);
        tillDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tillDateDF.setDateFormat(SystemSettings.datePattern);
        tillDateDF.setValue(new Date());
        tillDateDF.addValueChangeListener(this);

        operationOG = new OptionGroup();
        operationOG.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        operationOG.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
        operationOG.setNullSelectionAllowed(false);
        operationOG.setWidth("100%");
        operationOG.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        operationOG.addValueChangeListener(this);

        stocksMSB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Stocks));
        stocksMSB.setRequired(true);
        stocksMSB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        stocksMSB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        stocksMSB.setWidth("100%");
        stocksMSB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        stocksMSB.setFilteringMode(FilteringMode.CONTAINS);
        stocksMSB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        stocksMSB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
        stocksMSB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        stocksMSB.addValueChangeListener(this);

        selectAllBtn = new Button(myUI.getMessage(SptMessages.AllCategories));
        selectAllBtn.setWidth("100%");
        selectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllBtn.addClickListener(this);

        deselectAllBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllBtn.setWidth("100%");
        deselectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllBtn.addClickListener(this);

        try {
            DbDefinition dbCon = new DbDefinition();
            dbCon.connect();
            DbAccCategory dbAc = new DbAccCategory();
            dbAc.connect();
            DbProductCategories dbPC = new DbProductCategories();
            dbPC.connect();
            stocksMSB.setContainerDataSource(
                    dbCon.exec_for_select(myUI, SystemSettings.dbStock, myUI.getUser().getSchool_id()));
            stocksMSB.setValue(SystemSettings.convertToSet(stocksMSB.getContainerDataSource().getItemIds()));
            operationOG.setContainerDataSource(dbCon.exec_for_select(myUI, SystemSettings.dbOperation));
            Item item = operationOG.getContainerDataSource().addItem(0);
            item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(myUI.getMessage(SptMessages.General));
            if (operationOG.getContainerDataSource() != null) {
                operationOG.setValue(((IndexedContainer) operationOG.getContainerDataSource()).firstItemId());
            }
            dbAc.execSQL_for_select_as_tree(myUI, productsTable,
                    SystemSettings.convertCollectionToStr(dbPC.execSQL_for_select(myUI).getItemIds()));
            dbAc.close();
            dbCon.close();
            dbPC.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        stocksMSB.setValue(SystemSettings.convertToSet(stocksMSB.getContainerDataSource().getItemIds()));

        leftGrid.addComponent(operationOG, 0, 0, 3, 0);
        leftGrid.addComponent(fromDateDF, 0, 1, 1, 1);
        leftGrid.addComponent(tillDateDF, 2, 1, 3, 1);
        leftGrid.addComponent(stocksMSB, 0, 2, 3, 2);
        leftGrid.addComponent(selectAllBtn, 0, 3, 1, 3);
        leftGrid.addComponent(deselectAllBtn, 2, 3, 3, 3);
        leftGrid.addComponent(productsTable, 0, 4, 3, 4);
        leftGrid.addComponent(generateBtn, 0, 5, 2, 5);
        leftGrid.addComponent(excelBtn, 3, 5);
        leftGrid.setRowExpandRatio(4, 1);
        ((GridLayout) spltPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) spltPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(true);
        vl.setSizeFull();
        dataTable = new FormattedTreeTable();
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.addStyleName("noWrapHeader");
        vl.addComponent(dataTable);
        spltPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (productsTable.getValue() != null && stocksMSB.isValid() && fromDateDF.isValid() && tillDateDF.isValid()) {
                try {
                    DbStockMovements dbCon = new DbStockMovements();
                    dbCon.connect();
                    if ((Integer) operationOG.getValue() == 0) {
                        dbCon.exec_stock_balance(myUI, productsTable, fromDateDF.getValue(), tillDateDF.getValue(),
                                SystemSettings.convertCollectionToStr((Set<?>) stocksMSB.getValue()), dataTable);

                        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.StockIncome) + " - " + myUI.getMessage(SptMessages.Quantity), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.StockOutcome) + " - " + myUI.getMessage(SptMessages.Quantity), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.StockIncome) + " - " + myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.StockOutcome) + " - " + myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Balance) + " " +
                                myUI.getMessage(SptMessages.ToThe).toLowerCase() + " " + SystemSettings.df.format(tillDateDF.getValue()), Table.Align.RIGHT);
                    } else {
                        dbCon.exec_stock_operations(myUI, productsTable, fromDateDF.getValue(), tillDateDF.getValue(), (Integer) operationOG.getValue(),
                                SystemSettings.convertCollectionToStr((Set<?>) stocksMSB.getValue()), dataTable);
                        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Quantity), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.AvaragePrice), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.AvarageRate), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
                    }


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
                    excelReport.setReportTitle(myUI.getMessage(SptMessages.StockGeneralReport) + " ("
                            + operationOG.getContainerProperty(operationOG.getValue(),
                            myUI.getMessage(SptMessages.Name)).getValue() + ") - [" + myUI.getMessage(SptMessages.From).toLowerCase() + " "
                            + SystemSettings.df.format(fromDateDF.getValue())
                            + " " + myUI.getMessage(SptMessages.To).toLowerCase() + " " + SystemSettings.df.format(tillDateDF.getValue()) + "]");
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                    if ((Integer) operationOG.getValue() == 0) {
                        excelReport.getTotalsRow().getCell(1).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(2).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(2).setCellValue(
                                dataTable.getColumnFooter(myUI.getMessage(SptMessages.StockIncome)
                                        + " - " + myUI.getMessage(SptMessages.Amount)));
                        excelReport.getTotalsRow().getCell(3).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(4).setCellValue(
                                dataTable.getColumnFooter(myUI.getMessage(SptMessages.StockOutcome)
                                + " - " + myUI.getMessage(SptMessages.Amount)));
                        excelReport.getTotalsRow().getCell(5).setCellFormula(null);
                    } else {
                        excelReport.getTotalsRow().getCell(1).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(3).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(5).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(5).setCellValue(dataTable.getColumnFooter(myUI.getMessage(SptMessages.Amount)));
                    }
                    excelReport.sendConverted();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllBtn) {
            productsTable.setValue(productsTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            productsTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (excelBtn.isEnabled()) {
            if (property == productsTable || property == tillDateDF || property == fromDateDF || property == stocksMSB || property == operationOG) {
                excelBtn.setEnabled(false);
                dataTable.getContainerDataSource().removeAllItems();
            }
        }
    }
}
