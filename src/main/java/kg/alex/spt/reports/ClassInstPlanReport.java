/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.addon.tableexport.EnhancedFormatExcelExport;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import kg.alex.spt.dao.DbClassName;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Date;
import java.util.Set;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.dao.DbStudInstallmentPlan;
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import kg.alex.spt.pdf.ClassInstPlanPdf;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

public class ClassInstPlanReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(ClassInstPlanReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, makePdfBtn, selectAllBtn, deselectAllBtn,
            excelBtn;
    private HorizontalSplitPanel spltPanel;
    private EnhancedFormatExcelExport excelReport;
    private GridLayout leftGrid;
    private FilterTable classTable;
    private ComboBoxMax yearSelect;
    private ComboBoxMultiselectMax educationStatusMCB;
    private DateField tillDateDF, fromDateDF;
    private FormattedTable dataTable;
    private IndexedContainer installmentCont;
    private Date fromDate, tillDate;
    private String[] NATURAL_COL_ORDER;
    public double total;

    public ClassInstPlanReport(final MyVaadinUI ui, final HorizontalSplitPanel spltPanel) {
        this.myUI = ui;
        this.spltPanel = spltPanel;
        buildLeftPanel();
        NATURAL_COL_ORDER = new String[]{
            myUI.getMessage(SptMessages.Firstname), myUI.getMessage(SptMessages.Surname),
            myUI.getMessage(SptMessages.ClassName), myUI.getMessage(SptMessages.Phone),
            myUI.getMessage(SptMessages.Date), myUI.getMessage(SptMessages.Amount)};
    }

    private void buildLeftPanel() {
        leftGrid = new GridLayout(4, 6);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        yearSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        yearSelect.setWidth("100%");
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);

        educationStatusMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.EducationStatus));
        educationStatusMCB.setRequired(true);
        educationStatusMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        educationStatusMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        educationStatusMCB.setWidth("100%");
        educationStatusMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
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
            yearSelect.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbYear));
            educationStatusMCB.setContainerDataSource(
                    dbd.exec_for_select(myUI, SystemSettings.dbEducationStatus));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        educationStatusMCB.setValue(SystemSettings.convertToSet(
                educationStatusMCB.getContainerDataSource().getItemIds()));

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

        selectAllBtn = new Button(myUI.getMessage(SptMessages.AllClasses));
        selectAllBtn.setWidth("100%");
        selectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllBtn.addClickListener(this);

        deselectAllBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllBtn.setWidth("100%");
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
            classTable.setContainerDataSource(
                    dbcn.execClass_sel(myUI, myUI.getUser().getSchool_id()));
            dbcn.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        fromDateDF = new DateField(myUI.getMessage(SptMessages.FromDate));
        fromDateDF.setWidth("100%");
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        fromDateDF.setRequired(true);
        fromDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        fromDateDF.setDateFormat(SystemSettings.datePattern);
        fromDateDF.setValue(new Date());

        tillDateDF = new DateField(myUI.getMessage(SptMessages.TillDate));
        tillDateDF.setWidth("100%");
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setRequired(true);
        tillDateDF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tillDateDF.setDateFormat(SystemSettings.datePattern);
        tillDateDF.setValue(new Date());

        generateBtn = new Button(myUI.getMessage(SptMessages.ShowButton));
        generateBtn.setWidth("100%");
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        makePdfBtn = new Button();
        makePdfBtn.setDescription(myUI.getMessage(SptMessages.ExportToPdf));
        makePdfBtn.setWidth("100%");
        makePdfBtn.setEnabled(false);
        makePdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        makePdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        makePdfBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(SptMessages.ExportToExcel));
        excelBtn.setWidth("100%");
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
        ((GridLayout) spltPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) spltPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            buildRightPanel();
            makePdfBtn.setEnabled(true);
            excelBtn.setEnabled(true);
        } else if (source == makePdfBtn) {
            StudInfoPdf st;
            try {
                DbSchool dbsc = new DbSchool();
                dbsc.connect();
                st = dbsc.execGetSchoolPdf(myUI.getUser().getSchool_id());
                dbsc.close();

                if (st.getScl_accountent_fullname() != null) {
                    if (st.getScl_address() != null && st.getScl_phone() != null
                            && st.getScl_name_ru() != null) {
                        fromDate = fromDateDF.getValue();
                        tillDate = tillDateDF.getValue();
                        new ClassInstPlanPdf(myUI, installmentCont,
                                yearSelect.getContainerProperty(yearSelect.getValue(),
                                        myUI.getMessage(SptMessages.Name)).getValue().toString(),
                                fromDate, tillDate, st, total);
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
            classTable.setValue(classTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            classTable.setValue(null);
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    excelReport = new EnhancedFormatExcelExport(dataTable, "sheet1");
                    excelReport.setReportTitle(myUI.getMessage(SptMessages.InstpLanPaymentsReport));
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
        dataTable = new FormattedTable();
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(FormattedTable.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        try {
            DbStudInstallmentPlan dbip = new DbStudInstallmentPlan();
            dbip.connect();
            dataTable.clear();
            total = 0;
            installmentCont = dbip.execSQL_InstPlanByClass(myUI,
                    fromDateDF.getValue(), tillDateDF.getValue(),
                    (Integer) yearSelect.getValue(),
                    SystemSettings.convertCollectionToStr((Set<?>) classTable.getValue()),
                    SystemSettings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()), this);
            dataTable.setContainerDataSource(installmentCont);
            dbip.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
        dataTable.setColumnFooter(myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.Total) + ": " + SystemSettings.dFormat.format(total));
        dataTable.setVisibleColumns(NATURAL_COL_ORDER);
        vl.addComponent(dataTable);
        spltPanel.setSecondComponent(vl);
    }
}
