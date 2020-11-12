/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.*;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbAccTransactions;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.domain.SchoolAccounting;
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.tepi.filtertable.FilterTreeTable;

public class AccountingDateReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(AccountingDateReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, selectAllIncomesBtn, deselectAllIncomesBtn,
            selectAllOutcomesBtn, deselectAllOutcomesBtn, excelBtn;
    private HorizontalSplitPanel spltPanel;
    private GridLayout leftGrid;
    private DateField fromDateDF, tillDateDF;
    public FormattedTable incomesDataTable, outcomesDataTable;
    public FilterTreeTable incomeCategoriesTable, outcomeCategoriesTable;
    private ExcelExport excelReport;
    private SystemSettings sysSettings = new SystemSettings();
    private VerticalLayout rightLayout;
    private Label incomeTtlLab, expenseTtlLab, ttlLab, prev_balanceLab;
    private HorizontalLayout infoLay;
    private SchoolAccounting schoolAcc;

    public AccountingDateReport(final MyVaadinUI ui, final HorizontalSplitPanel spltPanel) {
        this.myUI = ui;
        this.spltPanel = spltPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        leftGrid = new GridLayout(4, 6);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        selectAllIncomesBtn = new Button(myUI.getMessage(SptMessages.AllIncomes));
        selectAllIncomesBtn.setWidth("100%");
        selectAllIncomesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllIncomesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllIncomesBtn.addClickListener(this);

        deselectAllIncomesBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllIncomesBtn.setWidth("100%");
        deselectAllIncomesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllIncomesBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllIncomesBtn.addClickListener(this);

        incomeCategoriesTable = new FilterTreeTable();
        incomeCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        incomeCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        incomeCategoriesTable.setSizeFull();
        incomeCategoriesTable.setNullSelectionAllowed(false);
        incomeCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        incomeCategoriesTable.setFilterBarVisible(true);
        incomeCategoriesTable.setFooterVisible(false);
        incomeCategoriesTable.setSelectable(true);
        incomeCategoriesTable.setNullSelectionAllowed(false);
        incomeCategoriesTable.setMultiSelect(true);
        incomeCategoriesTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        incomeCategoriesTable.addValueChangeListener(this);

        selectAllOutcomesBtn = new Button(myUI.getMessage(SptMessages.AllOutcomes));
        selectAllOutcomesBtn.setWidth("100%");
        selectAllOutcomesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllOutcomesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllOutcomesBtn.addClickListener(this);

        deselectAllOutcomesBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllOutcomesBtn.setWidth("100%");
        deselectAllOutcomesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllOutcomesBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllOutcomesBtn.addClickListener(this);

        outcomeCategoriesTable = new FilterTreeTable();
        outcomeCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        outcomeCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        outcomeCategoriesTable.setSizeFull();
        outcomeCategoriesTable.setNullSelectionAllowed(false);
        outcomeCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        outcomeCategoriesTable.setFilterBarVisible(true);
        outcomeCategoriesTable.setFooterVisible(false);
        outcomeCategoriesTable.setSelectable(true);
        outcomeCategoriesTable.setNullSelectionAllowed(false);
        outcomeCategoriesTable.setMultiSelect(true);
        outcomeCategoriesTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        outcomeCategoriesTable.addValueChangeListener(this);
        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            dbac.execSQL_for_select_as_tree(myUI, 2, outcomeCategoriesTable, Integer.toString(myUI.getUser().getSchool_id()));
            dbac.execSQL_for_select_as_tree(myUI, 1, incomeCategoriesTable, Integer.toString(myUI.getUser().getSchool_id()));
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

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
        fromDateDF.setDateFormat(sysSettings.datePattern);
        fromDateDF.setValue(new Date());
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(SptMessages.TillDate));
        tillDateDF.setWidth("100%");
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setRequired(true);
        tillDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tillDateDF.setDateFormat(sysSettings.datePattern);
        tillDateDF.setValue(new Date());
        tillDateDF.addValueChangeListener(this);

        leftGrid.addComponent(fromDateDF, 0, 0, 1, 0);
        leftGrid.addComponent(tillDateDF, 2, 0, 3, 0);
        leftGrid.addComponent(selectAllIncomesBtn, 0, 1, 1, 1);
        leftGrid.addComponent(deselectAllIncomesBtn, 2, 1, 3, 1);
        leftGrid.addComponent(incomeCategoriesTable, 0, 2, 3, 2);
        leftGrid.addComponent(selectAllOutcomesBtn, 0, 3, 1, 3);
        leftGrid.addComponent(deselectAllOutcomesBtn, 2, 3, 3, 3);
        leftGrid.addComponent(outcomeCategoriesTable, 0, 4, 3, 4);
        leftGrid.addComponent(generateBtn, 0, 5, 2, 5);
        leftGrid.addComponent(excelBtn, 3, 5);
        leftGrid.setRowExpandRatio(2, 1);
        leftGrid.setRowExpandRatio(4, 1);
        ((GridLayout) spltPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) spltPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        rightLayout = new VerticalLayout();
        rightLayout.setMargin(true);
        rightLayout.setSpacing(true);
        rightLayout.setSizeFull();
        rightLayout.setImmediate(true);

        incomesDataTable = new FormattedTable();
        incomesDataTable.setCaption(myUI.getMessage(SptMessages.Incomes));
        incomesDataTable.setSizeFull();
        incomesDataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        incomesDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        incomesDataTable.addStyleName("noWrapHeader");

        outcomesDataTable = new FormattedTable();
        outcomesDataTable.setCaption(myUI.getMessage(SptMessages.Outcomes));
        outcomesDataTable.setSizeFull();
        outcomesDataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        outcomesDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        outcomesDataTable.addStyleName("noWrapHeader");

        infoLay = new HorizontalLayout();
        infoLay.setSpacing(true);
        infoLay.setWidth("100%");

        incomeTtlLab = new Label();
        incomeTtlLab.setContentMode(ContentMode.HTML);
        incomeTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        incomeTtlLab.setImmediate(true);
        incomeTtlLab.setWidth("100%");
        infoLay.addComponent(incomeTtlLab);

        expenseTtlLab = new Label();
        expenseTtlLab.setContentMode(ContentMode.HTML);
        expenseTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        expenseTtlLab.setImmediate(true);
        expenseTtlLab.setWidth("100%");
        infoLay.addComponent(expenseTtlLab);

        prev_balanceLab = new Label();
        prev_balanceLab.setContentMode(ContentMode.HTML);
        prev_balanceLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        prev_balanceLab.setImmediate(true);
        prev_balanceLab.setSizeFull();
        infoLay.addComponent(prev_balanceLab);

        ttlLab = new Label();
        ttlLab.setContentMode(ContentMode.HTML);
        ttlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        ttlLab.setImmediate(true);
        ttlLab.setWidth("100%");
        infoLay.addComponent(ttlLab);

        spltPanel.setSecondComponent(rightLayout);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (fromDateDF.isValid() && tillDateDF.isValid()) {
                if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()
                        || !((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                    rightLayout.removeAllComponents();
                    Set<Integer> catIds = new HashSet<>();
                    if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()) {
                        catIds.addAll((Set<Integer>) incomeCategoriesTable.getValue());
                        Iterator iter = ((Set<Integer>) incomeCategoriesTable.getValue()).iterator();
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            if (incomeCategoriesTable.getChildren(next) != null) {
                                catIds.addAll(new HashSet<Integer>(
                                        (Collection<Integer>) incomeCategoriesTable.getChildren(next)));
                            }
                        }
                        rightLayout.addComponent(incomesDataTable);
                        rightLayout.setExpandRatio(incomesDataTable, 1);
                        try {
                            DbAccTransactions dbsc = new DbAccTransactions();
                            dbsc.connect();
                            incomesDataTable.setContainerDataSource(dbsc.exec_report_by_date(myUI, 1,
                                    myUI.getUser().getSchool_id(), fromDateDF.getValue(), tillDateDF.getValue(),
                                    incomeCategoriesTable));
                            incomesDataTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
                            incomesDataTable.setColumnAlignment(myUI.getMessage(SptMessages.Rate), Table.Align.RIGHT);
                            if (incomesDataTable.getContainerDataSource().size() != 0) {
                                excelBtn.setEnabled(true);
                            }
                            dbsc.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                    }
                    if (!((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                        catIds.addAll((Set<Integer>) outcomeCategoriesTable.getValue());
                        Iterator iter = ((Set<Integer>) outcomeCategoriesTable.getValue()).iterator();
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            if (outcomeCategoriesTable.getChildren(next) != null) {
                                catIds.addAll(new HashSet<Integer>(
                                        (Collection<Integer>) outcomeCategoriesTable.getChildren(next)));
                            }
                        }
                        rightLayout.addComponent(outcomesDataTable);
                        rightLayout.setExpandRatio(outcomesDataTable, 1);
                        try {
                            DbAccTransactions dbsc = new DbAccTransactions();
                            dbsc.connect();
                            outcomesDataTable.setContainerDataSource(dbsc.exec_report_by_date(myUI, 2,
                                    myUI.getUser().getSchool_id(), fromDateDF.getValue(), tillDateDF.getValue(),
                                    outcomeCategoriesTable));

                            outcomesDataTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
                            outcomesDataTable.setColumnAlignment(myUI.getMessage(SptMessages.Rate), Table.Align.RIGHT);
                            if (outcomesDataTable.getContainerDataSource().size() != 0) {
                                excelBtn.setEnabled(true);
                            }
                            dbsc.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                    }
                    try {
                        DbAccTransactions dbtr = new DbAccTransactions();
                        dbtr.connect();
                        schoolAcc = dbtr.exec_get_ttls(myUI.getUser().getSchool_id(), fromDateDF.getValue(),
                                tillDateDF.getValue(), sysSettings.convertCollectionToStr(catIds));
                        incomeTtlLab.setValue("<b>" + myUI.getMessage(SptMessages.IncomesTotal) + ": " + sysSettings.round(schoolAcc.getTotal_income(), 2) + "$</b>");
                        expenseTtlLab.setValue("<b>" + myUI.getMessage(SptMessages.ExpensesTotal) + ": " + sysSettings.round(schoolAcc.getTotal_outcome(), 2) + "$</b>");
                        ttlLab.setValue("<b>" + myUI.getMessage(SptMessages.Transactions) + ": " + sysSettings.round(
                                (schoolAcc.getPrevious_balance() + schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome()), 2) + "$</b>");
                        Calendar c = Calendar.getInstance();
                        c.setTime(fromDateDF.getValue());
                        c.add(Calendar.DAY_OF_MONTH, -1);
                        prev_balanceLab.setValue("<b>" + myUI.getMessage(SptMessages.Balance) + " (" + sysSettings.df.format(c.getTime()) + "): " + schoolAcc.getPrevious_balance() + "$</b>");
                        dbtr.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    rightLayout.addComponent(infoLay);
                }
            }
        } else if (source == excelBtn) {
            StudInfoPdf st;
            try {
                Calendar c = Calendar.getInstance();
                c.setTime(fromDateDF.getValue());
                c.add(Calendar.DAY_OF_MONTH, -1);
                DbSchool dbsc = new DbSchool();
                dbsc.connect();
                st = dbsc.execGetSchoolPdf(myUI.getUser().getSchool_id());
                dbsc.close();
                if (st.getScl_accountent_fullname() != null) {
                    if (st.getScl_address() != null && st.getScl_phone() != null
                            && st.getScl_name_ru() != null) {
                        if (rightLayout.getComponentCount() != 0) {
                            int table_count = rightLayout.getComponentCount();
                            for (int i = 0; i < table_count && rightLayout.getComponent(i) instanceof Table; i++) {
                                Table t = ((Table) rightLayout.getComponent(i));
                                if (t.getContainerDataSource().size() != 0) {
                                    String sheet = t.getCaption();
                                    if (excelReport == null) {
                                        excelReport = new ExcelExport(t, sheet);
                                    } else {
                                        excelReport.setNextTable(t, sheet);
                                    }
                                    excelReport.setReportTitle(t.getCaption() + " (" + sysSettings.df.format(fromDateDF.getValue()) + " / "
                                            + sysSettings.df.format(tillDateDF.getValue()) + ")");
                                    excelReport.setDisplayTotals(true);
                                    excelReport.convertTable();
                                    CellStyle style = excelReport.getWorkbook().createCellStyle();

                                    Font font = excelReport.getWorkbook().createFont();
                                    font.setBold(true);
                                    style.setFont(font);
                                    
                                    int rowNum = excelReport.getTotalsRow().getRowNum();
                                    Row row = excelReport.getWorkbook().getSheet(sheet).createRow(++rowNum);
                                    row = excelReport.getWorkbook().getSheet(sheet).createRow(++rowNum);
                                    row = excelReport.getWorkbook().getSheet(sheet).createRow(++rowNum);

                                    Cell cell = row.createCell(1);
                                    cell.setCellValue(myUI.getMessage(SptMessages.Accountant));
                                    cell.setCellStyle(style);
                                    cell = row.createCell(5);
                                    cell.setCellValue(myUI.getMessage(SptMessages.Director));
                                    cell.setCellStyle(style);
                                    row = excelReport.getWorkbook().getSheet(sheet).createRow(++rowNum);
                                    row.createCell(1).setCellValue(st.getScl_accountent_fullname());
                                    row.createCell(5).setCellValue(st.getScl_dir_f_name());

                                    excelReport.getWorkbook().getSheet(sheet).addMergedRegion(
                                            new CellRangeAddress(excelReport.getTotalsRow().getRowNum(), excelReport.getTotalsRow().getRowNum(),
                                                    excelReport.getTotalsRow().getFirstCellNum(), excelReport.getTotalsRow().getLastCellNum() - 1));
                                    excelReport.getTotalsRow().getCell(excelReport.getTotalsRow().getFirstCellNum()).setCellValue(
                                            myUI.getMessage(SptMessages.IncomesTotal) + ": " + sysSettings.round(schoolAcc.getTotal_income(), 2) + "$\t "
                                            + myUI.getMessage(SptMessages.ExpensesTotal) + ": " + sysSettings.round(schoolAcc.getTotal_outcome(), 2) + "$\t "
                                            + myUI.getMessage(SptMessages.Balance) + " (" + sysSettings.df.format(c.getTime())
                                            + "): " + schoolAcc.getPrevious_balance() + "$\t "
                                            + myUI.getMessage(SptMessages.Transactions) + ": " + sysSettings.round(
                                            (schoolAcc.getPrevious_balance() + schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome()), 2) + "$\t ");
                                }
                            }
                        }
                    } else {
                        Notification.show(myUI.getMessage(SptMessages.FillSchoolInfo),
                                Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NoAccountent),
                            Notification.Type.WARNING_MESSAGE);
                }
                if (excelReport != null) {
                    excelReport.sendConverted();
                    excelReport = null;
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllIncomesBtn) {
            incomeCategoriesTable.setValue(incomeCategoriesTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllIncomesBtn) {
            incomeCategoriesTable.setValue(null);
        } else if (source == selectAllOutcomesBtn) {
            outcomeCategoriesTable.setValue(outcomeCategoriesTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllOutcomesBtn) {
            outcomeCategoriesTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (excelBtn.isEnabled()) {
            if (property == incomeCategoriesTable || property == outcomeCategoriesTable
                    || property == tillDateDF || property == fromDateDF) {
                excelBtn.setEnabled(false);
                rightLayout.removeAllComponents();
            }
        }
    }
}
