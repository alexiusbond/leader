/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbClassName;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbDiscount;
import kg.alex.spt.dao.DbStudentDiscount;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.tableexport.EnhancedFormatExcelExport;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Iterator;
import java.util.Set;

public class ClassDiscountsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(ClassDiscountsReport.class);
    private final MyVaadinUI myUI;
    private Button generateBtn, selectAllClassesBtn, deselectAllClassesBtn,
            selectAllDiscountsBtn, deselectAllDiscountsBtn, excelBtn;
    private final HorizontalSplitPanel splitPanel;
    private ComboBox yearSelect;
    private ComboBoxMultiselect educationStatusMCB;
    public FormattedTable dataTable;
    public FilterTable classTable, discountsTable;

    public ClassDiscountsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 7);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        selectAllClassesBtn = new Button(myUI.getMessage(SptMessages.AllClasses));
        selectAllClassesBtn.setWidth(Settings.PERCENTS100);
        selectAllClassesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllClassesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllClassesBtn.addClickListener(this);

        deselectAllClassesBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllClassesBtn.setWidth(Settings.PERCENTS100);
        deselectAllClassesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllClassesBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllClassesBtn.addClickListener(this);

        classTable = new FilterTable();
        classTable.setFilterDecorator(new MyFilterDecorator(myUI));
        classTable.setStyleName(ValoTheme.TABLE_SMALL);
        classTable.setSizeFull();
        classTable.setNullSelectionAllowed(false);
        classTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        classTable.setFilterBarVisible(true);
        classTable.setFooterVisible(false);
        classTable.setSelectable(true);
        classTable.setNullSelectionAllowed(false);
        classTable.setMultiSelect(true);
        classTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        classTable.addValueChangeListener(this);
        try {
            DbClassName dbCon = new DbClassName();
            dbCon.connect();
            classTable.setContainerDataSource(dbCon.execClass_sel(myUI, myUI.getUser().getSchool_id()));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        classTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(SptMessages.Title)});

        selectAllDiscountsBtn = new Button(myUI.getMessage(SptMessages.AllDiscounts));
        selectAllDiscountsBtn.setWidth(Settings.PERCENTS100);
        selectAllDiscountsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllDiscountsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllDiscountsBtn.addClickListener(this);

        deselectAllDiscountsBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllDiscountsBtn.setWidth(Settings.PERCENTS100);
        deselectAllDiscountsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllDiscountsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllDiscountsBtn.addClickListener(this);

        discountsTable = new FilterTable();
        discountsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        discountsTable.setStyleName(ValoTheme.TABLE_SMALL);
        discountsTable.setSizeFull();
        discountsTable.setNullSelectionAllowed(false);
        discountsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        discountsTable.setFilterBarVisible(true);
        discountsTable.setFooterVisible(false);
        discountsTable.setSelectable(true);
        discountsTable.setNullSelectionAllowed(false);
        discountsTable.setMultiSelect(true);
        discountsTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        discountsTable.addValueChangeListener(this);

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

        yearSelect = new ComboBox(myUI.getMessage(SptMessages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);

        educationStatusMCB = new ComboBoxMultiselect(myUI.getMessage(SptMessages.EducationStatus));
        educationStatusMCB.setRequired(true);
        educationStatusMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        educationStatusMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        educationStatusMCB.setWidth(Settings.PERCENTS100);
        educationStatusMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        educationStatusMCB.setFilteringMode(FilteringMode.CONTAINS);
        educationStatusMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        educationStatusMCB.setShowSelectAllButton((filter, page) -> true);
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

        yearSelect.addValueChangeListener(this);
        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(educationStatusMCB, 0, 1, 3, 1);
        leftGrid.addComponent(selectAllClassesBtn, 0, 2, 1, 2);
        leftGrid.addComponent(deselectAllClassesBtn, 2, 2, 3, 2);
        leftGrid.addComponent(classTable, 0, 3, 3, 3);
        leftGrid.addComponent(selectAllDiscountsBtn, 0, 4, 1, 4);
        leftGrid.addComponent(deselectAllDiscountsBtn, 2, 4, 3, 4);
        leftGrid.addComponent(discountsTable, 0, 5, 3, 5);
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
        dataTable.addStyleName("noWrapHeader");
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (!((Set<?>) classTable.getValue()).isEmpty() && !((Set<?>) discountsTable.getValue()).isEmpty()) {
                try {
                    DbStudentDiscount dbCon = new DbStudentDiscount();
                    dbCon.connect();
                    dataTable.setContainerDataSource(null);
                    dbCon.execSQL_Discounts_by_classes(myUI, (Integer) yearSelect.getValue(),
                            Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                            this);
                    for (Object next : (Set<?>) classTable.getValue()) {
                        dataTable.setColumnAlignment(classTable.getContainerProperty(
                                next, myUI.getMessage(SptMessages.Title)).getValue() + " "
                                + myUI.getMessage(SptMessages.Students), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(classTable.getContainerProperty(
                                next, myUI.getMessage(SptMessages.Title)).getValue() + " "
                                + myUI.getMessage(SptMessages.DiscountAmount), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(classTable.getContainerProperty(
                                next, myUI.getMessage(SptMessages.Title)).getValue()
                                + " " + myUI.getMessage(SptMessages.Average) + "%", Table.Align.RIGHT);
                        try {
                            dataTable.setColumnFooter(classTable.getContainerProperty(
                                            next, myUI.getMessage(SptMessages.Title)).getValue()
                                            + " " + myUI.getMessage(SptMessages.Average) + "%",
                                    Settings.dFormat.format(Double.parseDouble(
                                            dataTable.getColumnFooter(classTable.getContainerProperty(
                                                    next, myUI.getMessage(SptMessages.Title)).getValue()
                                                    + " " + myUI.getMessage(SptMessages.Average) + "%"))
                                            / dataTable.getContainerDataSource().size()));
                        } catch (Exception ignored) {
                        }
                    }
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Total) + " "
                            + myUI.getMessage(SptMessages.Students), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Total) + " "
                            + myUI.getMessage(SptMessages.DiscountAmount), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Total) + " "
                            + myUI.getMessage(SptMessages.Average) + "%", Table.Align.RIGHT);

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
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable);
                    excelReport.setReportTitle(myUI.getMessage(SptMessages.SchoolDiscounts));
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    Iterator<?> school_iter = ((Set<?>) classTable.getValue()).iterator();
                    int i = 3;
                    while (school_iter.hasNext()) {
                        Object next = school_iter.next();
                        excelReport.getTotalsRow().getCell(i).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(i).setCellValue(
                                dataTable.getColumnFooter(classTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue()
                                        + " " + myUI.getMessage(SptMessages.Average) + "%"));
                        i += 3;
                    }
                    excelReport.getTotalsRow().getCell(i).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(i).setCellValue(
                            dataTable.getColumnFooter(myUI.getMessage(SptMessages.Total) + " "
                                    + myUI.getMessage(SptMessages.Average) + "%"));
                    excelReport.sendConverted();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
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
            excelBtn.setEnabled(false);
            try {
                DbDiscount dbd = new DbDiscount();
                dbd.connect();
                discountsTable.setContainerDataSource(
                        dbd.exec_disc_select(myUI, (Integer) yearSelect.getValue()));
                dbd.close();
                discountsTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(SptMessages.Title)});
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }
}
