/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbClassName;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.dao.DbStudentContract;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.pdf.DebtsPdf;
import kg.alex.spt.tableexport.EnhancedFormatExcelExport;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Date;
import java.util.Set;

public class DebtReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(DebtReport.class);
    private final MyVaadinUI myUI;
    private Button generateBtn, makePdfBtn, selectAllBtn, deselectAllBtn,
            excelBtn;
    private final HorizontalSplitPanel splitPanel;
    private FilterTable classTable;
    private ComboBox yearSelect;
    private ComboBoxMultiselect educationStatusMCB;
    private DateField tillDateDF, fromDateDF;
    private FormattedTable dataTable;
    private IndexedContainer installmentCont;
    private final String[] NATURAL_COL_ORDER;
    public double inst_total, paid_total, debt_total;

    public DebtReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.FirstName),
                myUI.getMessage(SptMessages.LastName),
                myUI.getMessage(SptMessages.ClassName), myUI.getMessage(SptMessages.InstallmentPlan),
                myUI.getMessage(SptMessages.Paid), myUI.getMessage(SptMessages.Debt)};
    }

    private void buildLeftPanel() {
        GridLayout leftGrid = new GridLayout(4, 6);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

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

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

        selectAllBtn = new Button(myUI.getMessage(SptMessages.AllClasses));
        selectAllBtn.setWidth(Settings.PERCENTS100);
        selectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllBtn.addClickListener(this);

        deselectAllBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllBtn.setWidth(Settings.PERCENTS100);
        deselectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllBtn.addClickListener(this);

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
            classTable.setContainerDataSource(dbcn.execClass_sel(myUI, myUI.getUser().getSchool_id()));
            dbcn.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        classTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(SptMessages.Title)});

        fromDateDF = new DateField(myUI.getMessage(SptMessages.FromDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        fromDateDF.setRequired(true);
        fromDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setValue(new Date());

        tillDateDF = new DateField(myUI.getMessage(SptMessages.TillDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setRequired(true);
        tillDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setValue(new Date());

        generateBtn = new Button(myUI.getMessage(SptMessages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        makePdfBtn = new Button();
        makePdfBtn.setDescription(myUI.getMessage(SptMessages.ExportToPdf));
        makePdfBtn.setWidth(Settings.PERCENTS100);
        makePdfBtn.setEnabled(false);
        makePdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        makePdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        makePdfBtn.addClickListener(this);

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
        leftGrid.addComponent(classTable, 0, 3, 3, 3);
        leftGrid.addComponent(fromDateDF, 0, 4, 1, 4);
        leftGrid.addComponent(tillDateDF, 2, 4, 3, 4);
        leftGrid.addComponent(generateBtn, 0, 5, 1, 5);
        leftGrid.addComponent(makePdfBtn, 2, 5);
        leftGrid.addComponent(excelBtn, 3, 5);
        leftGrid.setRowExpandRatio(3, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            buildRightPanel();
            makePdfBtn.setEnabled(true);
            excelBtn.setEnabled(true);
        } else if (source == makePdfBtn) {
            StudentInfoPdf st;
            try {
                DbSchool dbsc = new DbSchool();
                dbsc.connect();
                st = dbsc.execGetSchoolPdf(myUI.getUser().getSchool_id());
                dbsc.close();

                if (st.getScl_accountant_full_name() != null) {
                    if (st.getScl_address() != null && st.getScl_phone() != null
                            && st.getScl_name_ru() != null) {
                        Date fromDate = fromDateDF.getValue();
                        Date tillDate = tillDateDF.getValue();
                        new DebtsPdf(myUI, installmentCont,
                                yearSelect.getContainerProperty(yearSelect.getValue(),
                                        myUI.getMessage(SptMessages.Title)).getValue().toString(),
                                fromDate, tillDate, st, inst_total, paid_total, debt_total);
                    } else {

                        Notification.show(myUI.getMessage(SptMessages.FillSchoolInfo),
                                Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NoAccountant),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllBtn) {
            classTable.setValue(classTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            classTable.setValue(null);
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable, "sheet1");
                    excelReport.setReportTitle(myUI.getMessage(SptMessages.DebtReport));
                    excelReport.setDisplayTotals(true);
                    excelReport.export();
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
        if (property == classTable || property == yearSelect) {
            makePdfBtn.setEnabled(false);
            excelBtn.setEnabled(false);
        }
    }

    private void buildRightPanel() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new FormattedTable(myUI);
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(FormattedTable.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        try {
            DbStudentContract dbsc = new DbStudentContract();
            dbsc.connect();
            dataTable.clear();
            inst_total = 0;
            paid_total = 0;
            debt_total = 0;
            installmentCont = dbsc.execSQL_DebtsByClass(myUI,
                    fromDateDF.getValue(), tillDateDF.getValue(),
                    (Integer) yearSelect.getValue(),
                    Settings.convertCollectionToStr((Set<?>) classTable.getValue()),
                    Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()), this);
            dataTable.setContainerDataSource(installmentCont);
            dbsc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.InstallmentPlan), Table.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Paid), Table.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Debt), Table.Align.RIGHT);
        dataTable.setColumnFooter(myUI.getMessage(SptMessages.InstallmentPlan),
                Settings.dFormat2.format(inst_total));
        dataTable.setColumnFooter(myUI.getMessage(SptMessages.Paid),
                Settings.dFormat2.format(paid_total));
        dataTable.setColumnFooter(myUI.getMessage(SptMessages.Debt),
                Settings.dFormat2.format(debt_total));
        dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }
}
