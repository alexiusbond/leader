/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbTransfers;
import kg.alex.spt.domain.SchoolAccounting;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.FormattedTreeTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tepi.filtertable.FilterTreeTable;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.*;

public class AccountingBalanceReport implements Button.ClickListener,
        Property.ValueChangeListener, Serializable {

    static final Logger logger = LogManager.getLogger(AccountingBalanceReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, selectAllAssertsBtn, deselectAllAssertsBtn,
            selectAllDebtsBtn, deselectAllDebtsBtn, excelBtn;
    private HorizontalSplitPanel spltPanel;
    private GridLayout leftGrid;
    private DateField fromDateDF, tillDateDF;
    public FormattedTreeTable assertsDataTable, debtsDataTable;
    public FilterTreeTable assertsCategoriesTable, debtsCategoriesTable;
    private SystemSettings sysSettings = new SystemSettings();
    private Label assertsTtlLab, debtsTtlLab, ttlLab;
    private HorizontalLayout infoLay;
    private SchoolAccounting schoolAcc;
    private TabSheet tabSheet;
    private Calendar fromDate = Calendar.getInstance(), tillDate = Calendar.getInstance();
    private FileDownloader fd;

    public AccountingBalanceReport(final MyVaadinUI ui, final HorizontalSplitPanel spltPanel) {
        this.myUI = ui;
        this.spltPanel = spltPanel;
        buildLeftPanel();
        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.setStyleName(ValoTheme.TABSHEET_FRAMED);

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        vl.addComponent(tabSheet);

        spltPanel.setSecondComponent(vl);
    }

    private void buildLeftPanel() {

        leftGrid = new GridLayout(4, 6);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        selectAllAssertsBtn = new Button(myUI.getMessage(SptMessages.AllAsserts));
        selectAllAssertsBtn.setWidth("100%");
        selectAllAssertsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllAssertsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllAssertsBtn.addClickListener(this);

        deselectAllAssertsBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllAssertsBtn.setWidth("100%");
        deselectAllAssertsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllAssertsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllAssertsBtn.addClickListener(this);

        assertsCategoriesTable = new FilterTreeTable();
        assertsCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        assertsCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        assertsCategoriesTable.setSizeFull();
        assertsCategoriesTable.setNullSelectionAllowed(false);
        assertsCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        assertsCategoriesTable.setFilterBarVisible(true);
        assertsCategoriesTable.setFooterVisible(false);
        assertsCategoriesTable.setSelectable(true);
        assertsCategoriesTable.setNullSelectionAllowed(false);
        assertsCategoriesTable.setMultiSelect(true);
        assertsCategoriesTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        assertsCategoriesTable.addValueChangeListener(this);

        selectAllDebtsBtn = new Button(myUI.getMessage(SptMessages.AllDebts));
        selectAllDebtsBtn.setWidth("100%");
        selectAllDebtsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllDebtsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllDebtsBtn.addClickListener(this);

        deselectAllDebtsBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllDebtsBtn.setWidth("100%");
        deselectAllDebtsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllDebtsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllDebtsBtn.addClickListener(this);

        debtsCategoriesTable = new FilterTreeTable();
        debtsCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        debtsCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        debtsCategoriesTable.setSizeFull();
        debtsCategoriesTable.setNullSelectionAllowed(false);
        debtsCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        debtsCategoriesTable.setFilterBarVisible(true);
        debtsCategoriesTable.setFooterVisible(false);
        debtsCategoriesTable.setSelectable(true);
        debtsCategoriesTable.setNullSelectionAllowed(false);
        debtsCategoriesTable.setMultiSelect(true);
        debtsCategoriesTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        debtsCategoriesTable.addValueChangeListener(this);
        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            dbac.execSQL_for_select_as_tree(myUI, 3, assertsCategoriesTable, Integer.toString(myUI.getUser().getSchool_id()));
            dbac.execSQL_for_select_as_tree(myUI, 4, debtsCategoriesTable, Integer.toString(myUI.getUser().getSchool_id()));
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

        fromDateDF = new DateField(myUI.getMessage(SptMessages.FromDate));
        fromDateDF.setWidth("100%");
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        fromDateDF.setRequired(true);
        fromDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        fromDateDF.setResolution(Resolution.MONTH);
        fromDateDF.setDateFormat(sysSettings.yearMonthPattern);
        fromDateDF.setValue(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(SptMessages.TillDate));
        tillDateDF.setWidth("100%");
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setRequired(true);
        tillDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tillDateDF.setResolution(Resolution.MONTH);
        tillDateDF.setDateFormat(sysSettings.yearMonthPattern);
        tillDateDF.setValue(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
        tillDateDF.addValueChangeListener(this);

        fromDate.setTime(fromDateDF.getValue());
        fromDate.set(Calendar.DAY_OF_MONTH, 1);
        tillDate.setTime(tillDateDF.getValue());
        tillDate.set(Calendar.DAY_OF_MONTH, tillDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        leftGrid.addComponent(fromDateDF, 0, 0, 1, 0);
        leftGrid.addComponent(tillDateDF, 2, 0, 3, 0);
        leftGrid.addComponent(selectAllAssertsBtn, 0, 1, 1, 1);
        leftGrid.addComponent(deselectAllAssertsBtn, 2, 1, 3, 1);
        leftGrid.addComponent(assertsCategoriesTable, 0, 2, 3, 2);
        leftGrid.addComponent(selectAllDebtsBtn, 0, 3, 1, 3);
        leftGrid.addComponent(deselectAllDebtsBtn, 2, 3, 3, 3);
        leftGrid.addComponent(debtsCategoriesTable, 0, 4, 3, 4);
        leftGrid.addComponent(generateBtn, 0, 5, 2, 5);
        leftGrid.addComponent(excelBtn, 3, 5);
        leftGrid.setRowExpandRatio(2, 1);
        leftGrid.setRowExpandRatio(4, 1);
        ((GridLayout) spltPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) spltPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private VerticalLayout buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);
        vl.setSizeFull();

        assertsDataTable = new FormattedTreeTable();
        assertsDataTable.setCaption(myUI.getMessage(SptMessages.Asserts));
        assertsDataTable.setSizeFull();
        assertsDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        assertsDataTable.addStyleName("noWrapHeader");

        debtsDataTable = new FormattedTreeTable();
        debtsDataTable.setCaption(myUI.getMessage(SptMessages.Debts));
        debtsDataTable.setSizeFull();
        debtsDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        debtsDataTable.addStyleName("noWrapHeader");

        infoLay = new HorizontalLayout();
        infoLay.setSpacing(true);
        infoLay.setWidth("100%");

        assertsTtlLab = new Label();
        assertsTtlLab.setContentMode(ContentMode.HTML);
        assertsTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        assertsTtlLab.setImmediate(true);
        assertsTtlLab.setWidth("100%");
        infoLay.addComponent(assertsTtlLab);

        debtsTtlLab = new Label();
        debtsTtlLab.setContentMode(ContentMode.HTML);
        debtsTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        debtsTtlLab.setImmediate(true);
        debtsTtlLab.setWidth("100%");
        infoLay.addComponent(debtsTtlLab);

        ttlLab = new Label();
        ttlLab.setContentMode(ContentMode.HTML);
        ttlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        ttlLab.setImmediate(true);
        ttlLab.setWidth("100%");
        infoLay.addComponent(ttlLab);
        return vl;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (fromDateDF.isValid() && tillDateDF.isValid()) {
                if (!((Set<?>) assertsCategoriesTable.getValue()).isEmpty()
                        || !((Set<?>) debtsCategoriesTable.getValue()).isEmpty()) {
                    tabSheet.removeAllComponents();
                    Calendar current = Calendar.getInstance();
                    Calendar end_date = Calendar.getInstance();
                    current.setTime(fromDate.getTime());
                    while (current.before(tillDate)) {
                        end_date.setTime(current.getTime());
                        end_date.set(Calendar.DAY_OF_MONTH, end_date.getActualMaximum(Calendar.DAY_OF_MONTH));
                        VerticalLayout rightLayout = buildRightLayout();
                        tabSheet.addTab(rightLayout, sysSettings.ymdf.format(current.getTime()));
                        Set<Integer> catIds = new HashSet<>();
                        if (!((Set<?>) assertsCategoriesTable.getValue()).isEmpty()) {
                            catIds.addAll((Set<Integer>) assertsCategoriesTable.getValue());
                            rightLayout.addComponent(assertsDataTable);
                            rightLayout.setExpandRatio(assertsDataTable, 1);
                            try {
                                DbTransfers dbCon = new DbTransfers();
                                dbCon.connect();
                                dbCon.exec_report_by_date(myUI, 3, myUI.getUser().getSchool_id(), current.getTime(), end_date.getTime(),
                                        assertsDataTable, sysSettings.convertCollectionToStr(catIds));
                                assertsDataTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), FormattedTreeTable.Align.RIGHT);
                                assertsDataTable.setColumnAlignment(myUI.getMessage(SptMessages.Rate), FormattedTreeTable.Align.RIGHT);
                                if (assertsDataTable.getContainerDataSource().size() != 0) {
                                    excelBtn.setEnabled(true);
                                }
                                dbCon.close();
                            } catch (Exception e) {
                                logger.error(e);
                                logger.catching(e);
                            }
                        }
                        if (!((Set<?>) debtsCategoriesTable.getValue()).isEmpty()) {
                            catIds.addAll((Set<Integer>) debtsCategoriesTable.getValue());
                            rightLayout.addComponent(debtsDataTable);
                            rightLayout.setExpandRatio(debtsDataTable, 1);
                            try {
                                DbTransfers dbsc = new DbTransfers();
                                dbsc.connect();
                                dbsc.exec_report_by_date(myUI, 4, myUI.getUser().getSchool_id(), current.getTime(), end_date.getTime(),
                                        debtsDataTable, sysSettings.convertCollectionToStr(catIds));
                                debtsDataTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), FormattedTreeTable.Align.RIGHT);
                                debtsDataTable.setColumnAlignment(myUI.getMessage(SptMessages.Rate), FormattedTreeTable.Align.RIGHT);
                                if (debtsDataTable.getContainerDataSource().size() != 0) {
                                    excelBtn.setEnabled(true);
                                }
                                dbsc.close();
                            } catch (Exception e) {
                                logger.error(e);
                                logger.catching(e);
                            }
                        }
                        try {
                            DbTransfers dbtr = new DbTransfers();
                            dbtr.connect();
                            schoolAcc = dbtr.exec_get_ttls(myUI.getUser().getSchool_id(), current.getTime(),
                                    end_date.getTime(), sysSettings.convertCollectionToStr(catIds));
                            assertsTtlLab.setValue("<b>" + myUI.getMessage(SptMessages.AssertsTotal) + ": " + sysSettings.round(schoolAcc.getTotal_income(), 2) + "$</b>");
                            debtsTtlLab.setValue("<b>" + myUI.getMessage(SptMessages.DebtsTotal) + ": " + sysSettings.round(schoolAcc.getTotal_outcome(), 2) + "$</b>");
                            ttlLab.setValue("<b>" + myUI.getMessage(SptMessages.Total) + ": " + sysSettings.round(
                                    (schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome()), 2) + "$</b>");
                            assertsDataTable.setData(schoolAcc.getTotal_income());
                            debtsDataTable.setData(schoolAcc.getTotal_outcome());
                            ttlLab.setData(schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome());
                            ttlLab.setId(myUI.getMessage(SptMessages.Total));
                            dbtr.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        rightLayout.addComponent(infoLay);
                        current.add(Calendar.MONTH, 1);
                    }
                    createWorkbook();
                }
            }
        } else if (source == selectAllAssertsBtn) {
            assertsCategoriesTable.setValue(assertsCategoriesTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllAssertsBtn) {
            assertsCategoriesTable.setValue(null);
        } else if (source == selectAllDebtsBtn) {
            debtsCategoriesTable.setValue(debtsCategoriesTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllDebtsBtn) {
            debtsCategoriesTable.setValue(null);
        }
    }

    private void createWorkbook() {
        if (excelBtn.isEnabled()) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            Font fontBoldWht = workbook.createFont();
            fontBoldWht.setColor(IndexedColors.WHITE.getIndex());
            fontBoldWht.setBold(true);

            Font fontBlk = workbook.createFont();
            fontBoldWht.setColor(IndexedColors.BLACK.getIndex());

            CellStyle cellStyleGreen = workbook.createCellStyle();
            cellStyleGreen.setFont(fontBoldWht);
            cellStyleGreen.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            cellStyleGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle cellStyleLightGreen = workbook.createCellStyle();
            cellStyleLightGreen.setFont(fontBlk);
            cellStyleLightGreen.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            cellStyleLightGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < tabSheet.getComponentCount(); i++) {
                TabSheet.Tab tab = tabSheet.getTab(i);
                ArrayList<Label> labels = new ArrayList<>();
                ArrayList<FormattedTreeTable> tables = new ArrayList<>();
                for (int j = 0; j < ((VerticalLayout) tab.getComponent()).getComponentCount(); j++) {
                    if (((VerticalLayout) tab.getComponent()).getComponent(j) instanceof FormattedTreeTable) {
                        tables.add((FormattedTreeTable) ((VerticalLayout) tab.getComponent()).getComponent(j));
                    } else if (((VerticalLayout) tab.getComponent()).getComponent(j) instanceof HorizontalLayout) {
                        HorizontalLayout hl = (HorizontalLayout) ((VerticalLayout) tab.getComponent()).getComponent(j);
                        for (int k = 0; k < hl.getComponentCount(); k++) {
                            if (hl.getComponent(k) instanceof Label) {
                                labels.add((Label) hl.getComponent(k));
                            }
                        }
                    }
                }
                XSSFSheet sheet = workbook.createSheet(tab.getCaption());

                int rowNum;
                int colNum;
                Row row;
                Cell cell;

                for (int k = 0; k < tables.size(); k++) {
                    rowNum = 1;
                    colNum = k * 6 + 1;
                    if (sheet.getRow(rowNum) != null) {
                        row = sheet.getRow(rowNum);
                    } else {
                        row = sheet.createRow(rowNum);
                    }
                    Iterator propIter = tables.get(k).getContainerPropertyIds().iterator();
                    while (propIter.hasNext()) {
                        Object nextProp = propIter.next();
                        cell = row.createCell(colNum);
                        cell.setCellStyle(cellStyleGreen);
                        if (nextProp.equals(myUI.getMessage(SptMessages.Name))) {
                            cell.setCellValue(tables.get(k).getCaption());
                        } else if (nextProp.equals(myUI.getMessage(SptMessages.Amount))) {
                            cell.setCellValue((Double) tables.get(k).getData());
                        }
                        sheet.autoSizeColumn(colNum);
                        colNum++;
                    }
                    rowNum += 2;
                    colNum = k * 6 + 1;
                    if (sheet.getRow(rowNum) != null) {
                        row = sheet.getRow(rowNum);
                    } else {
                        row = sheet.createRow(rowNum);
                    }
                    rowNum++;
                    propIter = tables.get(k).getContainerPropertyIds().iterator();
                    while (propIter.hasNext()) {
                        Object nextProp = propIter.next();
                        cell = row.createCell(colNum);
                        cell.setCellStyle(cellStyleGreen);
                        cell.setCellValue(nextProp.toString());
                        sheet.autoSizeColumn(colNum);
                        colNum++;
                    }
                    colNum = k * 6 + 1;
                    Iterator itemsIter = tables.get(k).getItemIds().iterator();
                    while (itemsIter.hasNext()) {
                        Object nextItem = itemsIter.next();
                        if (sheet.getRow(rowNum) != null) {
                            row = sheet.getRow(rowNum);
                        } else {
                            row = sheet.createRow(rowNum);
                        }
                        rowNum++;
                        propIter = tables.get(k).getContainerPropertyIds().iterator();
                        while (propIter.hasNext()) {
                            Object nextProp = propIter.next();
                            cell = row.createCell(colNum);
                            if (tables.get(k).getContainerDataSource().getChildren(nextItem) != null) {
                                cell.setCellStyle(cellStyleLightGreen);
                            }
                            if (tables.get(k).getContainerProperty(nextItem, nextProp).getValue() instanceof String) {
                                cell.setCellValue((String) tables.get(k).getContainerProperty(nextItem, nextProp).getValue());
                            } else if (tables.get(k).getContainerProperty(nextItem, nextProp).getValue() instanceof Double) {
                                cell.setCellValue((Double) tables.get(k).getContainerProperty(nextItem, nextProp).getValue());
                            }
                            sheet.autoSizeColumn(colNum);
                            colNum++;
                        }
                        colNum = k * 6 + 1;
                    }
                    if (k == tables.size() - 1) {
                        for (int j = 2; j < labels.size(); j++) {
                            rowNum++;
                            if (sheet.getRow(rowNum) != null) {
                                row = sheet.getRow(rowNum);
                            } else {
                                row = sheet.createRow(rowNum);
                            }
                            cell = row.createCell(colNum);
                            cell.setCellStyle(cellStyleGreen);
                            cell.setCellValue(labels.get(j).getId());
                            sheet.autoSizeColumn(colNum);
                            colNum++;
                            cell = row.createCell(colNum);
                            cell.setCellStyle(cellStyleGreen);
                            colNum++;
                            cell = row.createCell(colNum);
                            cell.setCellStyle(cellStyleGreen);
                            colNum++;
                            cell = row.createCell(colNum);
                            cell.setCellStyle(cellStyleGreen);
                            cell.setCellValue((Double) labels.get(j).getData());
                            sheet.autoSizeColumn(colNum);
                            colNum++;
                            cell = row.createCell(colNum);
                            cell.setCellStyle(cellStyleGreen);
                            colNum = k * 6 + 1;
                            rowNum++;
                        }
                    }
                }
            }

            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                workbook.write(baos);
                // Close the document
                workbook.close();
                if (fd != null && excelBtn.getExtensions().contains(fd)) {
                    excelBtn.removeExtension(fd);
                }
                fd = new FileDownloader(new StreamResource(() -> {
                    return new ByteArrayInputStream(baos.toByteArray());
                }, myUI.getMessage(SptMessages.AccountingBalanceReport) + System.currentTimeMillis() + ".xlsx"));
                fd.extend(excelBtn);
            } catch (FileNotFoundException e) {
                logger.error(e);
                logger.catching(e);
            } catch (IOException e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (excelBtn.isEnabled()) {
            if (property == assertsCategoriesTable || property == debtsCategoriesTable
                    || property == tillDateDF || property == fromDateDF) {
                excelBtn.setEnabled(false);
                tabSheet.removeAllComponents();
            }
        }
        if ((property == fromDateDF || property == tillDateDF) && fromDateDF.isValid() && tillDateDF.isValid()) {
            fromDate.setTime(fromDateDF.getValue());
            fromDate.set(Calendar.DAY_OF_MONTH, 1);
            tillDate.setTime(tillDateDF.getValue());
            tillDate.set(Calendar.DAY_OF_MONTH, tillDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        }
    }
}
