/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.*;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.pdf.CallsPdf;
import kg.alex.spt.tableexport.EnhancedFormatExcelExport;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Set;

public class CallsView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(CallsView.class);
    private final MyVaadinUI myUI;
    private GridLayout leftLay;
    private FormattedTable dataTable;
    private ComboBox yearSelect;
    private ComboBoxMultiselect educationStatusMCB;
    private FilterTable classTable;
    private Button generateBtn, excelBtn, makePdfBtn, selectAllBtn, deselectAllBtn;

    public double total;

    public CallsView(MyVaadinUI myUI) {
        this.myUI = myUI;

        buildLeftPanel();

        this.setSplitPosition(15, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(leftLay);

        buildRightPanel();
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        Button source = event.getButton();
        if (source == generateBtn) {
            try {
                DbStudent dbst = new DbStudent();
                dbst.connect();
                dataTable.clear();
                total = 0;
                dataTable.setContainerDataSource(dbst.execSQLCalls(myUI, (Integer) yearSelect.getValue(),
                        Settings.convertCollectionToStr((Set<?>) classTable.getValue()),
                        Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()), this));
                dbst.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            dataTable.setColumnFooter(myUI.getMessage(SptMessages.InstPlanDebt), "Total "
                    + Settings.dFormat2.format(total));
            dataTable.setColumnWidth(myUI.getMessage(SptMessages.Note), 220);
            dataTable.setColumnWidth(myUI.getMessage(SptMessages.Id), 80);
            dataTable.setColumnWidth(myUI.getMessage(SptMessages.FirstName), 100);
            dataTable.setColumnWidth(myUI.getMessage(SptMessages.LastName), 120);
            dataTable.setColumnWidth(myUI.getMessage(SptMessages.Phone), 100);
            dataTable.setColumnWidth(myUI.getMessage(SptMessages.PlanDebtDate), 85);
            dataTable.setColumnWidth(myUI.getMessage(SptMessages.LastPayment), 95);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.InstPlanDebt), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.LastPayment), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Remain), Table.Align.RIGHT);
            dataTable.setColumnExpandRatio(myUI.getMessage(SptMessages.LastCall), 1);
            if (dataTable.size() != 0) {
                excelBtn.setEnabled(true);
                makePdfBtn.setEnabled(true);
            }
        } else if (source == makePdfBtn) {
            StudentInfoPdf studentInfo = new StudentInfoPdf();
            try {
                DbSchool dbsc = new DbSchool();
                dbsc.connect();
                studentInfo.setSchool(dbsc.execSchool(myUI.getUser().getSchool().getId()));
                dbsc.close();
                DbEmployee dbEmployee = new DbEmployee();
                dbEmployee.connect();
                studentInfo.setDirector(dbEmployee.exec_by_position_id(1, myUI.getUser().getSchool().getId()));
                studentInfo.setAccountant(dbEmployee.exec_by_position_id(2, myUI.getUser().getSchool().getId()));
                dbEmployee.close();
                if (studentInfo.getAccountant() != null) {
                    if (studentInfo.getSchool().getAddress() != null) {
                        new CallsPdf(myUI, dataTable, studentInfo);
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
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    dataTable.setColumnCollapsingAllowed(true);
                    dataTable.setColumnCollapsed(Settings.button, true);
                    dataTable.setColumnCollapsed(myUI.getMessage(SptMessages.Note), true);
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable, "sheet1");
                    excelReport.excludeCollapsedColumns();
                    excelReport.setReportTitle(myUI.getMessage(SptMessages.Calls));
                    excelReport.setDisplayTotals(true);
                    excelReport.export();
                    dataTable.setColumnCollapsed(Settings.button, false);
                    dataTable.setColumnCollapsed(myUI.getMessage(SptMessages.Note), false);
                    dataTable.setColumnCollapsingAllowed(false);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllBtn) {
            classTable.setValue(classTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            classTable.setValue(null);
        } else {
            int i;
            try {
                DbStudentCalls dbsc = new DbStudentCalls();
                dbsc.connect();
                i = dbsc.exec_insert((Integer) source.getData(),
                        myUI.getUser().getCurrent_year().getId(),
                        myUI.getUser().getId(),
                        ((TextField) dataTable.getContainerProperty(source.getData(),
                                myUI.getMessage(SptMessages.Note)).getValue()).getValue());
                if (i != 0) {
                    dataTable.getContainerProperty(source.getData(), myUI.getMessage(SptMessages.LastCall))
                            .setValue(dbsc.exec_getLastCall((Integer) source.getData()));
                    ((TextField) dataTable.getContainerProperty(source.getData(),
                            myUI.getMessage(SptMessages.Note)).getValue()).setValue("");
                }
                dbsc.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        if (event.getProperty() == classTable || event.getProperty() == yearSelect || event.getProperty() == educationStatusMCB) {
            excelBtn.setEnabled(false);
            makePdfBtn.setEnabled(false);
        }
    }

    private void buildLeftPanel() {
        leftLay = new GridLayout(4, 5);
        leftLay.setMargin(new MarginInfo(true, false, true, true));
        leftLay.setSpacing(true);
        leftLay.setSizeFull();

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
        educationStatusMCB.setValue(Settings.convertToSet(educationStatusMCB.getContainerDataSource().getItemIds()));
        educationStatusMCB.addValueChangeListener(this);

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

        classTable = new FilterTable();
        classTable.setSizeFull();
        classTable.setFilterDecorator(new MyFilterDecorator(myUI));
        classTable.setStyleName(ValoTheme.TABLE_COMPACT);
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
            classTable.setContainerDataSource(dbcn.execClass_sel(myUI, myUI.getUser().getSchool().getId()));
            dbcn.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        classTable.setVisibleColumns(myUI.getMessage(SptMessages.Title));

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

        makePdfBtn = new Button();
        makePdfBtn.setDescription(myUI.getMessage(SptMessages.ExportToPdf));
        makePdfBtn.setWidth(Settings.PERCENTS100);
        makePdfBtn.setEnabled(false);
        makePdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        makePdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        makePdfBtn.addClickListener(this);

        leftLay.addComponent(yearSelect, 0, 0, 3, 0);
        leftLay.addComponent(educationStatusMCB, 0, 1, 3, 1);
        leftLay.addComponent(selectAllBtn, 0, 2, 1, 2);
        leftLay.addComponent(deselectAllBtn, 2, 2, 3, 2);
        leftLay.addComponent(classTable, 0, 3, 3, 3);
        leftLay.addComponent(generateBtn, 0, 4, 1, 4);
        leftLay.addComponent(makePdfBtn, 2, 4);
        leftLay.addComponent(excelBtn, 3, 4);
        leftLay.setRowExpandRatio(3, 1);
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
        dataTable.addStyleName("noWrap");
        dataTable.addStyleName("noWrapHeader");

        vl.addComponent(dataTable);
        this.setSecondComponent(vl);

    }

    public Button createButton(int itemId) {
        Button btn = new Button();
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.setStyleName(ValoTheme.BUTTON_SMALL);
        btn.setIcon(FontAwesome.SAVE);
        btn.setWidth(Settings.PERCENTS100);
        btn.setData(itemId);
        btn.addClickListener(this);
        return btn;
    }

    public TextField createTextField(int itemId) {
        TextField tf = new TextField();
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        tf.setData(itemId);
        tf.setImmediate(true);
        return tf;
    }

    public IndexedContainer prepareContainer() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Id), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Phone), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.InstPlanDebt), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Remain), Double.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.PlanDebtDate), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastCall), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastPayment), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Note), TextField.class, null);
        container.addContainerProperty(Settings.button, Button.class, null);
        return container;
    }
}
