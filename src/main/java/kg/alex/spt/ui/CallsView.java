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
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbClassName;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.dao.DbStudent;
import kg.alex.spt.dao.DbStudentCalls;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.pdf.CallsPdf;
import kg.alex.spt.tableexport.EnhancedFormatExcelExport;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;

import java.util.Set;

public class CallsView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(CallsView.class);
    private final MyVaadinUI myUI;
    private GridLayout leftLay;
    private FormattedTable dataTable;
    private FilterTable classTable;
    private Button generateBtn, excelBtn, makePdfBtn, selectAllBtn, deselectAllBtn;

    public double total;

    public CallsView(MyVaadinUI myUI) {
        this.myUI = myUI;

        buildLeftPanel();

        this.setSplitPosition(24, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(leftLay);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        Button source = event.getButton();
        if (source == generateBtn) {
            buildRightPanel();
            if (dataTable.size() != 0) {
                excelBtn.setEnabled(true);
                makePdfBtn.setEnabled(true);
            }
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
                        new CallsPdf(myUI, dataTable, st);
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
        Property property = event.getProperty();
        if (property == classTable) {
            excelBtn.setEnabled(false);
            makePdfBtn.setEnabled(false);
        }
    }

    private void buildLeftPanel() {
        leftLay = new GridLayout(4, 3);
        leftLay.setMargin(new MarginInfo(true, false, true, true));
        leftLay.setSpacing(true);
        leftLay.setSizeFull();

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
            classTable.setContainerDataSource(
                    dbcn.execClass_sel(myUI, myUI.getUser().getSchool_id()));
            dbcn.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

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

        leftLay.addComponent(selectAllBtn, 0, 0, 1, 0);
        leftLay.addComponent(deselectAllBtn, 2, 0, 3, 0);
        leftLay.addComponent(classTable, 0, 1, 3, 1);
        leftLay.addComponent(generateBtn, 0, 2, 1, 2);
        leftLay.addComponent(makePdfBtn, 2, 2);
        leftLay.addComponent(excelBtn, 3, 2);
        leftLay.setRowExpandRatio(1, 1);
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
        dataTable.addStyleName("noWrap");
        try {
            DbStudent dbst = new DbStudent();
            dbst.connect();
            dataTable.clear();
            total = 0;
            dataTable.setContainerDataSource(dbst.execSQLCalls(myUI,
                    myUI.getUser().getCurrent_year().getId(),
                    Settings.convertCollectionToStr((Set<?>) classTable.getValue()), this));
            dbst.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setColumnFooter(myUI.getMessage(SptMessages.InstPlanDebt), "Total "
                + Settings.dFormat.format(total));
        dataTable.setColumnWidth(myUI.getMessage(SptMessages.Note), 350);
        dataTable.setColumnWidth(myUI.getMessage(SptMessages.PlanDebtDate), 85);
        dataTable.setColumnWidth(myUI.getMessage(SptMessages.LastPayment), 150);
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.InstPlanDebt), Table.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.LastPayment), Table.Align.RIGHT);

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
        container.addContainerProperty(myUI.getMessage(SptMessages.PlanDebtDate), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastCall), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.LastPayment), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Note), TextField.class, null);
        container.addContainerProperty(Settings.button, Button.class, null);
        return container;
    }
}
