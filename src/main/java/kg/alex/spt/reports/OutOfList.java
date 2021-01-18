/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.addon.tableexport.ExcelExport;
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
import java.util.Iterator;
import java.util.Set;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbLeavingReasonsView;
import kg.alex.spt.dao.DbStudentOrder;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

public class OutOfList implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(OutOfList.class);
    private MyVaadinUI myUI;
    private Button generateBtn, selectAllBtn, deselectAllBtn, excelBtn;
    private HorizontalSplitPanel spltPanel;
    private GridLayout leftGrid;
    private ComboBoxMultiselectMax yearSelectMCB, reasonsMCB;
    private FormattedTable dataTable;
    private FilterTable fromClassTable;
    private IndexedContainer dataCont;
    private ExcelExport excelReport;
    private SystemSettings sysSettings = new SystemSettings();
    private String[] NATURAL_COL_ORDER;
    public int activeStudents, discountedStudents;
    public double nets, paids, lefts;

    public OutOfList(final MyVaadinUI ui, final HorizontalSplitPanel spltPanel) {
        this.myUI = ui;
        this.spltPanel = spltPanel;
        buildLeftPanel();
        buildRightLayout();
        NATURAL_COL_ORDER = new String[]{
            myUI.getMessage(SptMessages.Id),
            myUI.getMessage(SptMessages.Firstname),
            myUI.getMessage(SptMessages.Surname),
            myUI.getMessage(SptMessages.Year),
            myUI.getMessage(SptMessages.Reasons),
            myUI.getMessage(SptMessages.FromClass),
            myUI.getMessage(SptMessages.ToClass),
            myUI.getMessage(SptMessages.Date),
            myUI.getMessage(SptMessages.Net),
            myUI.getMessage(SptMessages.Paid),
            myUI.getMessage(SptMessages.Left)};
    }

    private void buildLeftPanel() {

        leftGrid = new GridLayout(4, 5);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        yearSelectMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Year));
        yearSelectMCB.setRequired(true);
        yearSelectMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelectMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        yearSelectMCB.setWidth("100%");
        yearSelectMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        yearSelectMCB.setFilteringMode(FilteringMode.CONTAINS);
        yearSelectMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        yearSelectMCB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
        yearSelectMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));

        reasonsMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Reasons));
        reasonsMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        reasonsMCB.setWidth("100%");
        reasonsMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        reasonsMCB.setFilteringMode(FilteringMode.CONTAINS);
        reasonsMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        reasonsMCB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
        reasonsMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));

        try {
            DbDefinition dbd = new DbDefinition();
            DbLeavingReasonsView dblr = new DbLeavingReasonsView();
            dbd.connect();
            dblr.connect();
            yearSelectMCB.setContainerDataSource(dbd.exec_for_select(myUI, sysSettings.dbYear));
            reasonsMCB.setContainerDataSource(
                    dblr.exec_for_select(myUI, true));
            dblr.close();
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        };
        yearSelectMCB.addValueChangeListener(this);

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

        fromClassTable = new FilterTable();
        fromClassTable.setFilterDecorator(new MyFilterDecorator(myUI));
        fromClassTable.setStyleName(ValoTheme.TABLE_SMALL);
        fromClassTable.setSizeFull();
        fromClassTable.setNullSelectionAllowed(false);
        fromClassTable.setMultiSelect(true);
        fromClassTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        fromClassTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        fromClassTable.setFilterBarVisible(true);
        fromClassTable.setFooterVisible(false);
        fromClassTable.setSelectable(true);
        fromClassTable.addValueChangeListener(this);
        try {
            DbClassName dbcn = new DbClassName();
            dbcn.connect();
            fromClassTable.setContainerDataSource(
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

        leftGrid.addComponent(yearSelectMCB, 0, 0, 3, 0);
        leftGrid.addComponent(reasonsMCB, 0, 1, 3, 1);
        leftGrid.addComponent(selectAllBtn, 0, 2, 1, 2);
        leftGrid.addComponent(deselectAllBtn, 2, 2, 3, 2);
        leftGrid.addComponent(fromClassTable, 0, 3, 3, 3);
        leftGrid.addComponent(generateBtn, 0, 4, 2, 4);
        leftGrid.addComponent(excelBtn, 3, 4);
        leftGrid.setRowExpandRatio(3, 1);
        ((GridLayout) spltPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) spltPanel.getFirstComponent()).setRowExpandRatio(1, 1);

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
        spltPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (fromClassTable.getValue() != null) {
                try {
                    DbStudentOrder dbor = new DbStudentOrder();
                    dbor.connect();
                    dataCont = dbor.execSQL_outOf(myUI,
                            sysSettings.convertCollectionToStr((Set<?>) yearSelectMCB.getValue()),
                            sysSettings.convertCollectionToStr((Set<?>) fromClassTable.getValue()),
                            getMulticomboCaptions((Set<?>) reasonsMCB.getValue()),
                            myUI.getUser().getSchool_id(), this);
                    dataTable.setContainerDataSource(dataCont);
                    dataTable.setVisibleColumns(NATURAL_COL_ORDER);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Net), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Paid), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Left), Table.Align.RIGHT);

                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.Id),
                            myUI.getMessage(SptMessages.Students) + ": " + dataCont.size());
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.Net),
                            sysSettings.dFormat.format(nets));
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.Paid),
                            sysSettings.dFormat.format(paids));
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.Left),
                            sysSettings.dFormat.format(nets - paids));
                    if (dataCont.size() != 0) {
                        excelBtn.setEnabled(true);
                    }
                    dbor.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    excelReport = new ExcelExport(dataTable);
                    excelReport.setReportTitle(myUI.getMessage(SptMessages.OutOfReport));
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(0).setCellValue(dataTable.getColumnFooter(myUI.getMessage(SptMessages.Id)));
                    excelReport.sendConverted();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllBtn) {
            fromClassTable.setValue(fromClassTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            fromClassTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == fromClassTable && fromClassTable.getValue() != null) {
            excelBtn.setEnabled(false);
        } else if (property == yearSelectMCB) {
            excelBtn.setEnabled(false);
        }
    }

    private String getMulticomboCaptions(Set<?> set) {
        if (!set.isEmpty()) {
            Iterator iter = set.iterator();
            boolean isFirst = true;
            String reasons = "";
            while (iter.hasNext()) {
                Object next = iter.next();
                if (!isFirst) {
                    reasons += ", ";
                }
                reasons += reasonsMCB.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Name)).getValue();
                isFirst = false;
            }
            return reasons;
        } else {
            return null;
        }
    }
}
