/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import kg.alex.spt.tableexport.EnhancedFormatExcelExport;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbAccTransactions;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.domain.SchoolAccounting;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.FormattedTreeTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.tepi.filtertable.FilterTreeTable;

public class MonthReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(MonthReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, selectAllIncomesBtn, deselectAllIncomesBtn, selectAllSchoolsBtn, deselectAllSchoolsBtn,
            selectAllOutcomesBtn, deselectAllOutcomesBtn, excelBtn;
    private HorizontalSplitPanel splitPanel;
    private GridLayout leftGrid;
    private DateField fromDateDF, tillDateDF;
    public FormattedTreeTable incomesDataTable, outcomesDataTable;
    public FilterTreeTable incomeCategoriesTable, outcomeCategoriesTable;
    public FilterTable schoolsTable;
    private EnhancedFormatExcelExport excelReport;

    private VerticalLayout rightLayout;
    private Subject currentUser = SecurityUtils.getSubject();
    private Calendar fromDate = Calendar.getInstance(), tillDate = Calendar.getInstance();

    public MonthReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        leftGrid = new GridLayout(4, 8);
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

        selectAllSchoolsBtn = new Button(myUI.getMessage(SptMessages.AllSchools));
        selectAllSchoolsBtn.setWidth("100%");
        selectAllSchoolsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllSchoolsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllSchoolsBtn.addClickListener(this);

        deselectAllSchoolsBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllSchoolsBtn.setWidth("100%");
        deselectAllSchoolsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllSchoolsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllSchoolsBtn.addClickListener(this);

        schoolsTable = new FilterTable();
        schoolsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        schoolsTable.setStyleName(ValoTheme.TABLE_SMALL);
        schoolsTable.setSizeFull();
        schoolsTable.setNullSelectionAllowed(false);
        schoolsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        schoolsTable.setFilterBarVisible(true);
        schoolsTable.setFooterVisible(false);
        schoolsTable.setSelectable(true);
        schoolsTable.setNullSelectionAllowed(false);
        schoolsTable.setMultiSelect(true);
        schoolsTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        schoolsTable.addValueChangeListener(this);

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
        if (!currentUser.hasRole(Settings.rnAdmin)) {
            try {
                DbAccCategory dbac = new DbAccCategory();
                dbac.connect();
                dbac.execSQL_for_select_as_tree(myUI, 2, outcomeCategoriesTable, Integer.toString(myUI.getUser().getSchool_id()), false);
                dbac.execSQL_for_select_as_tree(myUI, 1, incomeCategoriesTable, Integer.toString(myUI.getUser().getSchool_id()), false);
                dbac.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            schoolsTable.setContainerDataSource(dbs.execSchoolSel(myUI, 0));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        schoolsTable.setVisibleColumns(new String[]{myUI.getMessage(SptMessages.Title)});

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
        fromDateDF.setRequired(true);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        fromDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        fromDateDF.setWidth("100%");
        fromDateDF.setResolution(Resolution.MONTH);
        fromDateDF.setDateFormat(Settings.yearMonthPattern);
        fromDateDF.setValue(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(SptMessages.TillDate));
        tillDateDF.setRequired(true);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tillDateDF.setWidth("100%");
        tillDateDF.setResolution(Resolution.MONTH);
        tillDateDF.setDateFormat(Settings.yearMonthPattern);
        tillDateDF.setValue(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
        tillDateDF.addValueChangeListener(this);

        fromDate.setTime(fromDateDF.getValue());
        fromDate.set(Calendar.DAY_OF_MONTH, 1);
        tillDate.setTime(tillDateDF.getValue());
        tillDate.set(Calendar.DAY_OF_MONTH, tillDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        leftGrid.addComponent(fromDateDF, 0, 0, 1, 0);
        leftGrid.addComponent(tillDateDF, 2, 0, 3, 0);
        if (currentUser.hasRole(Settings.rnAdmin)) {
            leftGrid.addComponent(selectAllSchoolsBtn, 0, 1, 1, 1);
            leftGrid.addComponent(deselectAllSchoolsBtn, 2, 1, 3, 1);
            leftGrid.addComponent(schoolsTable, 0, 2, 3, 2);
            leftGrid.setRowExpandRatio(2, 1);
        }
        leftGrid.addComponent(selectAllIncomesBtn, 0, 3, 1, 3);
        leftGrid.addComponent(deselectAllIncomesBtn, 2, 3, 3, 3);
        leftGrid.addComponent(incomeCategoriesTable, 0, 4, 3, 4);
        leftGrid.addComponent(selectAllOutcomesBtn, 0, 5, 1, 5);
        leftGrid.addComponent(deselectAllOutcomesBtn, 2, 5, 3, 5);
        leftGrid.addComponent(outcomeCategoriesTable, 0, 6, 3, 6);
        leftGrid.addComponent(generateBtn, 0, 7, 2, 7);
        leftGrid.addComponent(excelBtn, 3, 7);
        leftGrid.setRowExpandRatio(4, 1);
        leftGrid.setRowExpandRatio(6, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);

    }

    private void buildRightLayout() {
        rightLayout = new VerticalLayout();
        rightLayout.setMargin(true);
        rightLayout.setSpacing(true);
        rightLayout.setSizeFull();
        rightLayout.setImmediate(true);

        incomesDataTable = new FormattedTreeTable();
        incomesDataTable.setCaption(myUI.getMessage(SptMessages.Incomes));
        incomesDataTable.setFooterVisible(true);
        incomesDataTable.setSizeFull();
        incomesDataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        incomesDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        incomesDataTable.addStyleName("noWrapHeader");
        incomesDataTable.addStyleName("noWrapFooter");

        outcomesDataTable = new FormattedTreeTable();
        outcomesDataTable.setCaption(myUI.getMessage(SptMessages.Outcomes));
        outcomesDataTable.setFooterVisible(true);
        outcomesDataTable.setSizeFull();
        outcomesDataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        outcomesDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        outcomesDataTable.addStyleName("noWrapHeader");
        outcomesDataTable.addStyleName("noWrapFooter");

        splitPanel.setSecondComponent(rightLayout);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (fromDateDF.isValid() && tillDateDF.isValid()) {
                if (!currentUser.hasRole(Settings.rnAdmin) || !((Set<?>) schoolsTable.getValue()).isEmpty()) {
                    if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()
                            || !((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                        rightLayout.removeAllComponents();
                        try {
                            DbAccTransactions dbTr = new DbAccTransactions();
                            dbTr.connect();
                            if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()) {
                                rightLayout.addComponent(incomesDataTable);
                                rightLayout.setExpandRatio(incomesDataTable, 1);
                                if (currentUser.hasRole(Settings.rnAdmin)) {
                                    dbTr.execSQL_by_months(myUI, 1,
                                            schoolsTable, incomeCategoriesTable, fromDate, tillDate, incomesDataTable);
                                } else {
                                    dbTr.execSQL_by_months(myUI, 1,
                                            myUI.getUser().getSchool_id(),
                                            incomeCategoriesTable, fromDate, tillDate, incomesDataTable);
                                }
                                if (incomesDataTable.getContainerDataSource().size() != 0) {
                                    excelBtn.setEnabled(true);
                                }

                            }
                            if (!((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                                rightLayout.addComponent(outcomesDataTable);
                                rightLayout.setExpandRatio(outcomesDataTable, 1);
                                if (currentUser.hasRole(Settings.rnAdmin)) {
                                    dbTr.execSQL_by_months(myUI, 2, schoolsTable,
                                            outcomeCategoriesTable, fromDate, tillDate, outcomesDataTable);
                                } else {
                                    dbTr.execSQL_by_months(myUI, 2, myUI.getUser().getSchool_id(),
                                            outcomeCategoriesTable, fromDate, tillDate, outcomesDataTable);
                                }
                                if (outcomesDataTable.getContainerDataSource().size() != 0) {
                                    excelBtn.setEnabled(true);
                                }
                            }
                            Iterator school_iter = null;
                            Calendar current = Calendar.getInstance();
                            Calendar prev_date = Calendar.getInstance();
                            current.setTime(fromDate.getTime());
                            Set<Integer> catIds = new HashSet<>();
                            while (current.before(tillDate)) {
                                school_iter = ((Set<?>) schoolsTable.getValue()).iterator();
                                prev_date.setTime(current.getTime());
                                prev_date.add(Calendar.DATE, -1);
                                while (school_iter.hasNext()) {
                                    Object nextSchool = school_iter.next();
                                    if (!((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                                        catIds.addAll((Set<Integer>) outcomeCategoriesTable.getValue());
                                        outcomesDataTable.setColumnAlignment(schoolsTable.getContainerProperty(
                                                nextSchool, myUI.getMessage(SptMessages.Title)).getValue()
                                                        + " - " + Settings.ymdf.format(current.getTime()),
                                                Table.Align.RIGHT);
                                        outcomesDataTable.setColumnAlignment(schoolsTable.getContainerProperty(
                                                nextSchool, myUI.getMessage(SptMessages.Title)).getValue()
                                                        + " - " + myUI.getMessage(SptMessages.Total),
                                                Table.Align.RIGHT);
                                    }
                                    if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()) {
                                        catIds.addAll((Set<Integer>) incomeCategoriesTable.getValue());
                                        incomesDataTable.setColumnAlignment(schoolsTable.getContainerProperty(
                                                nextSchool, myUI.getMessage(SptMessages.Title)).getValue()
                                                        + " - " + Settings.ymdf.format(current.getTime()),
                                                Table.Align.RIGHT);
                                        incomesDataTable.setColumnAlignment(schoolsTable.getContainerProperty(
                                                nextSchool, myUI.getMessage(SptMessages.Title)).getValue()
                                                        + " - " + myUI.getMessage(SptMessages.Total),
                                                Table.Align.RIGHT);
                                        SchoolAccounting scAcc = dbTr.exec_get_ttls((Integer) nextSchool, current.getTime(),
                                                tillDate.getTime(), Settings.convertCollectionToStr(catIds));
                                        incomesDataTable.setColumnFooter(schoolsTable.getContainerProperty(
                                                nextSchool, myUI.getMessage(SptMessages.Title)).getValue() + " - "
                                                + Settings.ymdf.format(current.getTime()), myUI.getMessage(SptMessages.Balance) + " ("
                                                + Settings.df.format(prev_date.getTime()) + "): " + Settings.dFormat.format(scAcc.getPrevious_balance())
                                                + "; " + myUI.getMessage(SptMessages.Total) + ": " + incomesDataTable.getColumnFooter(schoolsTable.getContainerProperty(
                                                nextSchool, myUI.getMessage(SptMessages.Title)).getValue() + " - "
                                                + Settings.ymdf.format(current.getTime())));
                                    }
                                }
                                if (!currentUser.hasRole(Settings.rnAdmin)) {
                                    SchoolAccounting scAcc = dbTr.exec_get_ttls(myUI.getUser().getSchool_id(), current.getTime(),
                                            tillDate.getTime(), Settings.convertCollectionToStr(catIds));
                                    incomesDataTable.setColumnFooter(
                                            Settings.ymdf.format(current.getTime()), myUI.getMessage(SptMessages.Balance) + " ("
                                                    + Settings.df.format(prev_date.getTime()) + ") - " + Settings.dFormat.format(scAcc.getPrevious_balance())
                                                    + "; " + myUI.getMessage(SptMessages.Total) + " - " + incomesDataTable.getColumnFooter(Settings.ymdf.format(current.getTime())));
                                    incomesDataTable.setColumnAlignment(Settings.ymdf.format(current.getTime()), Table.Align.RIGHT);
                                    outcomesDataTable.setColumnAlignment(Settings.ymdf.format(current.getTime()), Table.Align.RIGHT);
                                }
                                current.add(Calendar.MONTH, 1);
                            }
                            incomesDataTable.setColumnAlignment(myUI.getMessage(SptMessages.Total) + "", Table.Align.RIGHT);
                            outcomesDataTable.setColumnAlignment(myUI.getMessage(SptMessages.Total) + "", Table.Align.RIGHT);
                            dbTr.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                    }
                }
            }
        } else if (source == excelBtn) {
            try {
                if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()
                        && !((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                    if (incomesDataTable.getContainerDataSource().size() != 0
                            && outcomesDataTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(incomesDataTable, myUI.getMessage(SptMessages.Incomes));
                        excelReport.setReportTitle(incomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        int i = 1;
                        Iterator iter = incomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(incomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.setNextTable(outcomesDataTable, myUI.getMessage(SptMessages.Outcomes));
                        excelReport.setReportTitle(outcomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        i = 1;
                        iter = outcomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(outcomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.sendConverted();
                    } else if (incomesDataTable.getContainerDataSource().size() != 0
                            && outcomesDataTable.getContainerDataSource().size() == 0) {
                        excelReport = new EnhancedFormatExcelExport(incomesDataTable, myUI.getMessage(SptMessages.Incomes));
                        excelReport.setReportTitle(incomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        int i = 1;
                        Iterator iter = incomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(incomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.sendConverted();
                    } else if (incomesDataTable.getContainerDataSource().size() == 0
                            && outcomesDataTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(outcomesDataTable, myUI.getMessage(SptMessages.Outcomes));
                        excelReport.setReportTitle(outcomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        int i = 1;
                        Iterator iter = outcomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(outcomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.sendConverted();
                    }
                } else if (!((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                    if (outcomesDataTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(outcomesDataTable, myUI.getMessage(SptMessages.Outcomes));
                        excelReport.setReportTitle(outcomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        int i = 1;
                        Iterator iter = outcomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(outcomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.sendConverted();
                    }
                } else if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()) {
                    if (incomesDataTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(incomesDataTable, myUI.getMessage(SptMessages.Incomes));
                        excelReport.setReportTitle(incomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        int i = 1;
                        Iterator iter = incomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(incomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.sendConverted();
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllSchoolsBtn) {
            schoolsTable.setValue(schoolsTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllSchoolsBtn) {
            schoolsTable.setValue(null);
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
        if (property == schoolsTable) {
            if (schoolsTable.getValue() != null && !((Set<?>) schoolsTable.getValue()).isEmpty()) {
                try {
                    DbAccCategory dbac = new DbAccCategory();
                    dbac.connect();
                    dbac.execSQL_for_select_as_tree(myUI, 2, outcomeCategoriesTable, Settings.convertCollectionToStr((Set<?>) schoolsTable.getValue()), false);
                    dbac.execSQL_for_select_as_tree(myUI, 1, incomeCategoriesTable, Settings.convertCollectionToStr((Set<?>) schoolsTable.getValue()), false);
                    dbac.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            } else {
                outcomeCategoriesTable.getContainerDataSource().removeAllItems();
                incomeCategoriesTable.getContainerDataSource().removeAllItems();
            }
        }
        if (excelBtn.isEnabled()) {
            if (property == incomeCategoriesTable || property == outcomeCategoriesTable
                    || property == fromDateDF || property == tillDateDF) {
                excelBtn.setEnabled(false);
                rightLayout.removeAllComponents();
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
