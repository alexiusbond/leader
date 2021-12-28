/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import kg.alex.spt.tableexport.EnhancedFormatExcelExport;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Set;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.dao.DbStudentContract;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.pdf.MonthsReportPdf;
import kg.alex.spt.utils.MyFilterDecorator;
import kg.alex.spt.pdf.SummaryReportPdf;
import kg.alex.spt.pdf.YearReportPdf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

public class YearMonthReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(YearMonthReport.class);
    private MyVaadinUI myUI;
    private Subject currentUser = SecurityUtils.getSubject();
    private Button generateBtn, makePdfBtn, selectAllBtn, deselectAllBtn,
            excelBtn;
    private HorizontalSplitPanel splitPanel;
    private GridLayout leftGrid;
    private FilterTable schoolTable;
    private ComboBoxMax yearSelect;
    private ComboBoxMultiselectMax educationStatusMCB;
    private EnhancedFormatExcelExport excelReport;

    private String[] NATURAL_COL_ORDER_YEAR;
    private String[] NATURAL_COL_ORDER_MONTH;
    private String[] NATURAL_COL_ORDER_SUMMARY;
    public VerticalLayout rightLay;
    private OptionGroup type;
    public int totalStudents = 0, totalActive = 0;
    public double contracts = 0.0, discounts = 0.0, debts = 0.0, corrections = 0.0, nets = 0.0,
            paids = 0.0, lefts = 0.0, inst_plans = 0.0;

    public YearMonthReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
        NATURAL_COL_ORDER_YEAR = new String[]{myUI.getMessage(SptMessages.ClassName),
                myUI.getMessage(SptMessages.Total_Active),
                myUI.getMessage(SptMessages.Contract),
                myUI.getMessage(SptMessages.Discount),
                myUI.getMessage(SptMessages.DiscountPercentage),
                myUI.getMessage(SptMessages.Correction),
                myUI.getMessage(SptMessages.PreviousYearDebt),
                myUI.getMessage(SptMessages.Net),
                myUI.getMessage(SptMessages.Paid),
                myUI.getMessage(SptMessages.Left),
                Settings.percentage};

        NATURAL_COL_ORDER_SUMMARY = new String[]{myUI.getMessage(SptMessages.School),
                myUI.getMessage(SptMessages.Total_Active),
                myUI.getMessage(SptMessages.Contract),
                myUI.getMessage(SptMessages.Discount),
                myUI.getMessage(SptMessages.DiscountPercentage),
                myUI.getMessage(SptMessages.Correction),
                myUI.getMessage(SptMessages.PreviousYearDebt),
                myUI.getMessage(SptMessages.Net),
                myUI.getMessage(SptMessages.Paid),
                myUI.getMessage(SptMessages.Left),
                Settings.percentage};

        NATURAL_COL_ORDER_MONTH = new String[]{myUI.getMessage(SptMessages.Month),
                myUI.getMessage(SptMessages.InstPlanDebt),
                myUI.getMessage(SptMessages.Paid),
                myUI.getMessage(SptMessages.Left),
                Settings.percentage};
    }

    private void buildLeftPanel() {
        leftGrid = new GridLayout(4, 6);
        leftGrid.setSpacing(true);
        leftGrid.setWidth(Settings.PERCENTS100);

        yearSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);

        educationStatusMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.EducationStatus));
        educationStatusMCB.setRequired(true);
        educationStatusMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        educationStatusMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        educationStatusMCB.setWidth(Settings.PERCENTS100);
        educationStatusMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        educationStatusMCB.setFilteringMode(FilteringMode.CONTAINS);
        educationStatusMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        educationStatusMCB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
        educationStatusMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            yearSelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbYear, true));
            educationStatusMCB.setContainerDataSource(
                    dbd.exec_for_select(myUI, Settings.dbEducationStatus, true));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        educationStatusMCB.setValue(Settings.convertToSet(
                educationStatusMCB.getContainerDataSource().getItemIds()));

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

        selectAllBtn = new Button(myUI.getMessage(SptMessages.AllSchools));
        selectAllBtn.setWidth(Settings.PERCENTS100);
        selectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllBtn.addClickListener(this);

        deselectAllBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllBtn.setWidth(Settings.PERCENTS100);
        deselectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllBtn.addClickListener(this);

        schoolTable = new FilterTable();
        schoolTable.setFilterDecorator(new MyFilterDecorator(myUI));
        schoolTable.setStyleName(ValoTheme.TABLE_SMALL);
        schoolTable.setSizeFull();
        schoolTable.setNullSelectionAllowed(false);
        schoolTable.setMultiSelect(true);
        schoolTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        schoolTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        schoolTable.setFilterBarVisible(true);
        schoolTable.setFooterVisible(false);
        schoolTable.setSelectable(true);

        schoolTable.addValueChangeListener(this);
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            schoolTable.setContainerDataSource(dbs.execSchoolSel(myUI, 0));
            schoolTable.setVisibleColumns(new String[]{myUI.getMessage(SptMessages.Title)});
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        type = new OptionGroup();
        type.setWidth(Settings.PERCENTS100);
        type.addItem(myUI.getMessage(SptMessages.Monthly));
        type.addItem(myUI.getMessage(SptMessages.Yearly));
        if (currentUser.hasRole(Settings.rnAdmin)) {
            type.addItem(myUI.getMessage(SptMessages.Summary));
        }
        type.setValue(myUI.getMessage(SptMessages.Monthly));
        type.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        type.addValueChangeListener(this);

        generateBtn = new Button(myUI.getMessage(SptMessages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        makePdfBtn = new Button();
        makePdfBtn.setDescription(myUI.getMessage(SptMessages.ExportToPdf));
        makePdfBtn.setWidth(Settings.PERCENTS100);
        makePdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        makePdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        makePdfBtn.addClickListener(this);
        makePdfBtn.setEnabled(false);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(SptMessages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(educationStatusMCB, 0, 1, 3, 1);
        leftGrid.addComponent(selectAllBtn, 0, 2, 1, 2);
        leftGrid.addComponent(deselectAllBtn, 2, 2, 3, 2);
        if (currentUser.hasRole(Settings.rnAdmin)) {
            leftGrid.setSizeFull();
            leftGrid.addComponent(schoolTable, 0, 3, 3, 3);
            leftGrid.setRowExpandRatio(3, 1);
        }
        leftGrid.addComponent(type, 0, 4, 3, 4);
        leftGrid.addComponent(generateBtn, 0, 5, 1, 5);
        leftGrid.addComponent(makePdfBtn, 2, 5);
        leftGrid.addComponent(excelBtn, 3, 5);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        rightLay = new VerticalLayout();
        rightLay.setMargin(true);
        rightLay.setSpacing(true);
        rightLay.setWidth(Settings.PERCENTS100);
        rightLay.setImmediate(true);
        splitPanel.setSecondComponent(rightLay);
    }

    public FormattedTable createTable(String caption) {
        IndexedContainer container = new IndexedContainer();
        if (type.getValue().toString().equals(myUI.getMessage(SptMessages.Yearly))) {
            container.addContainerProperty(myUI.getMessage(SptMessages.ClassName), String.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.Total_Active), String.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.Contract), Double.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.Discount), Double.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.DiscountPercentage), Double.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.Correction), Double.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.PreviousYearDebt), Double.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.Net), Double.class, null);
        } else if (type.getValue().toString().equals(myUI.getMessage(SptMessages.Summary))) {
            container.addContainerProperty(myUI.getMessage(SptMessages.School), String.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.Total_Active), String.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.Contract), Double.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.Discount), Double.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.DiscountPercentage), Double.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.Correction), Double.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.PreviousYearDebt), Double.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.Net), Double.class, null);
        } else {
            container.addContainerProperty(myUI.getMessage(SptMessages.Month), String.class, null);
            container.addContainerProperty(myUI.getMessage(SptMessages.InstPlanDebt), Double.class, null);
        }
        container.addContainerProperty(myUI.getMessage(SptMessages.Paid), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Left), Double.class, null);
        container.addContainerProperty(Settings.percentage, Double.class, 0.0);

        FormattedTable dataTable = new FormattedTable();
        dataTable.setCaption(caption);
        dataTable.setFooterVisible(true);
        dataTable.setWidth(Settings.PERCENTS100);
        dataTable.setPageLength(container.size());
        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setContainerDataSource(container);
        if (type.getValue().toString().equals(myUI.getMessage(SptMessages.Yearly))
                || type.getValue().toString().equals(myUI.getMessage(SptMessages.Summary))) {
            if (type.getValue().toString().equals(myUI.getMessage(SptMessages.Yearly))) {
                dataTable.setVisibleColumns(NATURAL_COL_ORDER_YEAR);
            } else {
                dataTable.setVisibleColumns(NATURAL_COL_ORDER_SUMMARY);
            }
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Total_Active), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Contract), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Discount), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.DiscountPercentage), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Correction), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.PreviousYearDebt), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Net), Table.Align.RIGHT);
        } else {
            dataTable.setVisibleColumns(NATURAL_COL_ORDER_MONTH);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.InstPlanDebt), Table.Align.RIGHT);
        }
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Left), Table.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Paid), Table.Align.RIGHT);
        dataTable.setColumnAlignment(Settings.percentage, Table.Align.RIGHT);
        if (container.size() != 0) {
            makePdfBtn.setEnabled(true);
            excelBtn.setEnabled(true);
        }
        rightLay.addComponent(dataTable);
        return dataTable;
    }

    public void clearLayout() {
        rightLay.removeAllComponents();
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (schoolTable.getValue() != null) {
                try {
                    DbStudentContract dbsc = new DbStudentContract();
                    dbsc.connect();
                    String school_ids;
                    if (currentUser.hasRole(Settings.rnAdmin)) {
                        school_ids = Settings.convertCollectionToStr((Set<?>) schoolTable.getValue());
                    } else {
                        school_ids = myUI.getUser().getSchool_id() + "";
                    }
                    if (school_ids != null) {
                        if (type.getValue().toString().equals(myUI.getMessage(SptMessages.Yearly))) {
                            rightLay.setHeightUndefined();
                            dbsc.execSQL_Yearly_by_classes(myUI, school_ids,
                                    Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                                    (Integer) yearSelect.getValue(), this);
                        } else if (type.getValue().toString().equals(myUI.getMessage(SptMessages.Summary))) {
                            dbsc.execSQL_Summary_report(myUI, school_ids,
                                    Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                                    (Integer) yearSelect.getValue(), this);
                            rightLay.setHeight("100%");
                            rightLay.getComponent(0).setHeight("100%");
                        } else {
                            rightLay.setHeightUndefined();
                            dbsc.execSQL_Monthly_by_classes(myUI, school_ids,
                                    Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                                    (Integer) yearSelect.getValue(), this);
                        }
                    } else {
                        Notification.show(myUI.getMessage(SptMessages.NotifNothingIsSelected),
                                Notification.Type.WARNING_MESSAGE);
                    }
                    dbsc.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                if (rightLay.getComponentCount() != 0) {
                    makePdfBtn.setEnabled(true);
                    excelBtn.setEnabled(true);
                }
            }
        } else if (source == makePdfBtn) {
            StudentInfoPdf st;
            try {
                DbSchool dbsc = new DbSchool();
                dbsc.connect();
                st = dbsc.execGetSchoolPdf(myUI.getUser().getSchool_id());
                dbsc.close();
                if (st.getScl_accountent_fullname() != null) {
                    if (st.getScl_address() != null && st.getScl_phone() != null
                            && st.getScl_name_ru() != null) {
                        if (type.getValue().toString().equals(myUI.getMessage(SptMessages.Yearly))) {
                            new YearReportPdf(myUI, rightLay, st);
                        } else if (type.getValue().toString().equals(myUI.getMessage(SptMessages.Monthly))) {
                            new MonthsReportPdf(myUI, rightLay, st);
                        } else {
                            new SummaryReportPdf(myUI, rightLay, st);
                        }
                    } else {
                        Notification.show(myUI.getMessage(SptMessages.FillSchoolInfo),
                                Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NoAccountent),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllBtn) {
            schoolTable.setValue(schoolTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            schoolTable.setValue(null);
        } else if (source == excelBtn) {
            try {
                if (rightLay.getComponentCount() != 0) {
                    int table_count = rightLay.getComponentCount();
                    for (int i = 0; i < table_count; i++) {
                        Table t = ((Table) rightLay.getComponent(i));
                        if (t.getContainerDataSource().size() != 0) {
                            if (excelReport == null) {
                                excelReport = new EnhancedFormatExcelExport(t, t.getCaption());
                            } else {
                                excelReport.setNextTable(t, t.getCaption());
                            }
                            excelReport.setReportTitle(t.getCaption());
                            excelReport.setDisplayTotals(true);
                            excelReport.convertTable();
                            if (type.getValue().toString().equals(myUI.getMessage(SptMessages.Monthly))) {
                                excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(4).setCellValue(
                                        t.getColumnFooter(Settings.percentage));
                            } else {
                                excelReport.getTotalsRow().getCell(1).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(9).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(1).setCellValue(
                                        t.getColumnFooter(myUI.getMessage(SptMessages.Total_Active)));
                                excelReport.getTotalsRow().getCell(4).setCellValue(
                                        t.getColumnFooter(myUI.getMessage(SptMessages.DiscountPercentage)));
                                excelReport.getTotalsRow().getCell(9).setCellValue(t.getColumnFooter(Settings.percentage));
                            }
                        }
                    }
                    if (excelReport != null) {
                        excelReport.sendConverted();
                        excelReport = null;
                    }
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
        if (property == schoolTable && schoolTable.getValue() != null) {
            makePdfBtn.setEnabled(false);
            excelBtn.setEnabled(false);
        } else if (property == yearSelect || (property == type && type.getValue() != null)) {
            makePdfBtn.setEnabled(false);
            excelBtn.setEnabled(false);
        }
    }
}
