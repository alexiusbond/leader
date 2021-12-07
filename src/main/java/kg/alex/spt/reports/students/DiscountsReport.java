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
import kg.alex.spt.dao.DbClassName;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Set;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbDiscount;
import kg.alex.spt.dao.DbStudentContract;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

public class DiscountsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(DiscountsReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, selectAllClassesBtn, deselectAllClassesBtn,
            selectAllDiscountsBtn, deselectAllDiscountsBtn, excelBtn;
    private HorizontalSplitPanel splitPanel;
    private GridLayout leftGrid;
    private ComboBoxMax yearSelect;
    private ComboBoxMultiselectMax educationStatusMCB;
    private FormattedTable dataTable;
    private FilterTable classTable, discountsTable;
    private IndexedContainer dataCont;
    private EnhancedFormatExcelExport excelReport;

    private String[] NATURAL_COL_ORDER;
    public int activeStudents, discountedStudents;
    public double contracts, discounts, corrections, debts, nets, paids, lefts;

    public DiscountsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Id),
                myUI.getMessage(SptMessages.EducationStatus),
                myUI.getMessage(SptMessages.FirstName),
                myUI.getMessage(SptMessages.LastName),
                myUI.getMessage(SptMessages.ClassName),
                myUI.getMessage(SptMessages.Contract),
                myUI.getMessage(SptMessages.DiscountType),
                myUI.getMessage(SptMessages.Discount),
                myUI.getMessage(SptMessages.CorrectionType),
                myUI.getMessage(SptMessages.Correction),
                myUI.getMessage(SptMessages.PreviousYearDebt),
                myUI.getMessage(SptMessages.Net),
                myUI.getMessage(SptMessages.Paid),
                myUI.getMessage(SptMessages.Left)};
    }

    private void buildLeftPanel() {

        leftGrid = new GridLayout(4, 7);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        yearSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        yearSelect.setWidth("100%");
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);

        educationStatusMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.EducationStatus));
        educationStatusMCB.setRequired(true);
        educationStatusMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        educationStatusMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        educationStatusMCB.setWidth("100%");
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
            yearSelect.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbYear, true));
            educationStatusMCB.setContainerDataSource(
                    dbd.exec_for_select(myUI, SystemSettings.dbEducationStatus, true));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        educationStatusMCB.setValue(SystemSettings.convertToSet(
                educationStatusMCB.getContainerDataSource().getItemIds()));

        selectAllDiscountsBtn = new Button(myUI.getMessage(SptMessages.AllDiscounts));
        selectAllDiscountsBtn.setWidth("100%");
        selectAllDiscountsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllDiscountsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllDiscountsBtn.addClickListener(this);

        deselectAllDiscountsBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllDiscountsBtn.setWidth("100%");
        deselectAllDiscountsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllDiscountsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllDiscountsBtn.addClickListener(this);

        discountsTable = new FilterTable();
        discountsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        discountsTable.setStyleName(ValoTheme.TABLE_SMALL);
        discountsTable.setSizeFull();
        discountsTable.setNullSelectionAllowed(false);
        discountsTable.setMultiSelect(true);
        discountsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        discountsTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        discountsTable.setFilterBarVisible(true);
        discountsTable.setFooterVisible(false);
        discountsTable.setSelectable(true);
        discountsTable.addValueChangeListener(this);

        selectAllClassesBtn = new Button(myUI.getMessage(SptMessages.AllClasses));
        selectAllClassesBtn.setWidth("100%");
        selectAllClassesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllClassesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllClassesBtn.addClickListener(this);

        deselectAllClassesBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllClassesBtn.setWidth("100%");
        deselectAllClassesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllClassesBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllClassesBtn.addClickListener(this);

        classTable = new FilterTable();
        classTable.setFilterDecorator(new MyFilterDecorator(myUI));
        classTable.setStyleName(ValoTheme.TABLE_SMALL);
        classTable.setSizeFull();
        classTable.setNullSelectionAllowed(false);
        classTable.setMultiSelect(true);
        classTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        classTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        classTable.setFilterBarVisible(true);
        classTable.setFooterVisible(false);
        classTable.setSelectable(true);
        classTable.addValueChangeListener(this);
        try {
            DbClassName dbcn = new DbClassName();
            dbcn.connect();
            classTable.setContainerDataSource(
                    dbcn.execClass_sel(myUI, myUI.getUser().getSchool_id()));
            dbcn.close();
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

        yearSelect.addValueChangeListener(this);
        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(educationStatusMCB, 0, 1, 3, 1);
        leftGrid.addComponent(selectAllDiscountsBtn, 0, 2, 1, 2);
        leftGrid.addComponent(deselectAllDiscountsBtn, 2, 2, 3, 2);
        leftGrid.addComponent(discountsTable, 0, 3, 3, 3);
        leftGrid.addComponent(selectAllClassesBtn, 0, 4, 1, 4);
        leftGrid.addComponent(deselectAllClassesBtn, 2, 4, 3, 4);
        leftGrid.addComponent(classTable, 0, 5, 3, 5);
        leftGrid.addComponent(generateBtn, 0, 6, 2, 6);
        leftGrid.addComponent(excelBtn, 3, 6);
        leftGrid.setRowExpandRatio(3, 1);
        leftGrid.setRowExpandRatio(5, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);

    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new FormattedTable();
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (!((Set<?>) classTable.getValue()).isEmpty()
                    && !((Set<?>) discountsTable.getValue()).isEmpty()) {
                try {
                    DbStudentContract dbsc = new DbStudentContract();
                    dbsc.connect();
                    dataCont = dbsc.execSQL_Discounts(myUI,
                            SystemSettings.convertCollectionToStr((Set<?>) classTable.getValue()),
                            SystemSettings.convertCollectionToStr((Set<?>) discountsTable.getValue()),
                            (Integer) yearSelect.getValue(),
                            SystemSettings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                            this);
                    dataTable.setContainerDataSource(dataCont);
                    dataTable.setVisibleColumns(NATURAL_COL_ORDER);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Contract), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Discount), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Correction), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.PreviousYearDebt), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Net), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Paid), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Left), Table.Align.RIGHT);
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.Id),
                            myUI.getMessage(SptMessages.Students) + ": " + dataCont.size());
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.EducationStatus),
                            myUI.getMessage(SptMessages.Active) + activeStudents);
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.Contract),
                            SystemSettings.dFormat.format(contracts));
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.Discount),
                            SystemSettings.dFormat.format(discounts));
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.Correction),
                            SystemSettings.dFormat.format(corrections));
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.PreviousYearDebt),
                            SystemSettings.dFormat.format(debts));
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.Net),
                            SystemSettings.dFormat.format(nets));
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.Paid),
                            SystemSettings.dFormat.format(paids));
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.Left),
                            SystemSettings.dFormat.format(lefts));
                    if (dataCont.size() != 0) {
                        dataTable.setColumnFooter(myUI.getMessage(SptMessages.DiscountType), myUI.getMessage(SptMessages.Discounted)
                                + discountedStudents + " (" + discountedStudents * 100 / dataCont.size() + "%)");
                        excelBtn.setEnabled(true);
                    }
                    dbsc.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            if (dataTable.getContainerDataSource().size() != 0) {
                excelReport = new EnhancedFormatExcelExport(dataTable);
                excelReport.setReportTitle(myUI.getMessage(SptMessages.DiscountsReport));
                excelReport.setDisplayTotals(true);
                excelReport.convertTable();
                excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                excelReport.getTotalsRow().getCell(1).setCellFormula(null);
                excelReport.getTotalsRow().getCell(6).setCellFormula(null);
                excelReport.getTotalsRow().getCell(0).setCellValue(dataTable.getColumnFooter(myUI.getMessage(SptMessages.Id)));
                excelReport.getTotalsRow().getCell(1).setCellValue(dataTable.getColumnFooter(myUI.getMessage(SptMessages.EducationStatus)));
                excelReport.getTotalsRow().getCell(6).setCellValue(dataTable.getColumnFooter(myUI.getMessage(SptMessages.DiscountType)));
                excelReport.sendConverted();
            }
        } else if (source == selectAllClassesBtn) {
            classTable.setValue(classTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllClassesBtn) {
            classTable.setValue(null);
        } else if (source == selectAllDiscountsBtn) {
            discountsTable.setValue(discountsTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllDiscountsBtn) {
            discountsTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == classTable && classTable.getValue() != null) {
            excelBtn.setEnabled(false);
        } else if (property == discountsTable && discountsTable.getValue() != null) {
            excelBtn.setEnabled(false);
        } else if (property == yearSelect) {
            try {
                DbDiscount dbd = new DbDiscount();
                dbd.connect();
                discountsTable.setContainerDataSource(
                        dbd.exec_disc_select(myUI, (Integer) yearSelect.getValue()));
                dbd.close();
                discountsTable.setVisibleColumns(new String[]{myUI.getMessage(SptMessages.Title)});
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            excelBtn.setEnabled(false);
        }
    }
}
