/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbEmployeeLessons;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

public class HRLessonHoursReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(HRLessonHoursReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, excelBtn, selectAllSchoolsBtn, deselectAllSchoolsBtn, selectAllBranchesBtn, deselectAllBranchesBtn, selectAllPositionsBtn, deselectAllPositionsBtn, selectAllExtraPositionsBtn, deselectAllExtraPositionsBtn;
    private HorizontalSplitPanel spltPanel;
    private GridLayout leftGrid;
    public FilterTable schoolTable, branchTable, positionTable, extraPositionTable;
    private ComboBoxMultiselectMax workingStatusesMCB;
    private ComboBoxMax yearSelect;
    private ExcelExport excelReport;
    private String[] NATURAL_COL_ORDER;
    private SystemSettings sysSettings = new SystemSettings();
    private Subject currentUser = SecurityUtils.getSubject();
    private VerticalLayout vl = new VerticalLayout();

    public HRLessonHoursReport(final MyVaadinUI ui, final HorizontalSplitPanel spltPanel) {
        this.myUI = ui;
        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Name)};
        this.spltPanel = spltPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        leftGrid = new GridLayout(4, 4);
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

        schoolTable = new FilterTable();
        schoolTable.setFilterDecorator(new MyFilterDecorator(myUI));
        schoolTable.setStyleName(ValoTheme.TABLE_SMALL);
        schoolTable.setSizeFull();
        schoolTable.setNullSelectionAllowed(false);
        schoolTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        schoolTable.setFilterBarVisible(true);
        schoolTable.setFooterVisible(false);
        schoolTable.setSelectable(true);
        schoolTable.setNullSelectionAllowed(false);
        schoolTable.setMultiSelect(true);
        schoolTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        schoolTable.addValueChangeListener(this);
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            schoolTable.setContainerDataSource(dbs.execSchoolSel(myUI, 0));
            schoolTable.setVisibleColumns(NATURAL_COL_ORDER);
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        GridLayout schoolsGrid = new GridLayout(2, 2);
        schoolsGrid.setMargin(new MarginInfo(true, false));
        schoolsGrid.setSizeFull();
        schoolsGrid.setSpacing(true);
        schoolsGrid.addComponent(selectAllSchoolsBtn);
        schoolsGrid.addComponent(deselectAllSchoolsBtn);
        schoolsGrid.addComponent(schoolTable, 0, 1, 1, 1);
        schoolsGrid.setRowExpandRatio(1, 1);

        selectAllBranchesBtn = new Button(myUI.getMessage(SptMessages.AllBranches));
        selectAllBranchesBtn.setWidth("100%");
        selectAllBranchesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllBranchesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllBranchesBtn.addClickListener(this);

        deselectAllBranchesBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllBranchesBtn.setWidth("100%");
        deselectAllBranchesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllBranchesBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllBranchesBtn.addClickListener(this);

        branchTable = new FilterTable();
        branchTable.setFilterDecorator(new MyFilterDecorator(myUI));
        branchTable.setStyleName(ValoTheme.TABLE_SMALL);
        branchTable.setSizeFull();
        branchTable.setNullSelectionAllowed(false);
        branchTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        branchTable.setFilterBarVisible(true);
        branchTable.setFooterVisible(false);
        branchTable.setSelectable(true);
        branchTable.setNullSelectionAllowed(false);
        branchTable.setMultiSelect(true);
        branchTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        branchTable.addValueChangeListener(this);

        GridLayout branchesGrid = new GridLayout(2, 2);
        branchesGrid.setMargin(new MarginInfo(true, false));
        branchesGrid.setSizeFull();
        branchesGrid.setSpacing(true);
        branchesGrid.addComponent(selectAllBranchesBtn);
        branchesGrid.addComponent(deselectAllBranchesBtn);
        branchesGrid.addComponent(branchTable, 0, 1, 1, 1);
        branchesGrid.setRowExpandRatio(1, 1);

        selectAllPositionsBtn = new Button(myUI.getMessage(SptMessages.AllPositions));
        selectAllPositionsBtn.setWidth("100%");
        selectAllPositionsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllPositionsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllPositionsBtn.addClickListener(this);

        deselectAllPositionsBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllPositionsBtn.setWidth("100%");
        deselectAllPositionsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllPositionsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllPositionsBtn.addClickListener(this);

        positionTable = new FilterTable();
        positionTable.setFilterDecorator(new MyFilterDecorator(myUI));
        positionTable.setStyleName(ValoTheme.TABLE_SMALL);
        positionTable.setSizeFull();
        positionTable.setNullSelectionAllowed(false);
        positionTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        positionTable.setFilterBarVisible(true);
        positionTable.setFooterVisible(false);
        positionTable.setSelectable(true);
        positionTable.setNullSelectionAllowed(false);
        positionTable.setMultiSelect(true);
        positionTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        positionTable.addValueChangeListener(this);

        GridLayout positionsGrid = new GridLayout(2, 2);
        positionsGrid.setMargin(new MarginInfo(true, false));
        positionsGrid.setSizeFull();
        positionsGrid.setSpacing(true);
        positionsGrid.addComponent(selectAllPositionsBtn);
        positionsGrid.addComponent(deselectAllPositionsBtn);
        positionsGrid.addComponent(positionTable, 0, 1, 1, 1);
        positionsGrid.setRowExpandRatio(1, 1);

        selectAllExtraPositionsBtn = new Button(myUI.getMessage(SptMessages.AllExtraPositions));
        selectAllExtraPositionsBtn.setWidth("100%");
        selectAllExtraPositionsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllExtraPositionsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllExtraPositionsBtn.addClickListener(this);

        deselectAllExtraPositionsBtn = new Button(myUI.getMessage(SptMessages.Clear));
        deselectAllExtraPositionsBtn.setWidth("100%");
        deselectAllExtraPositionsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllExtraPositionsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllExtraPositionsBtn.addClickListener(this);

        extraPositionTable = new FilterTable();
        extraPositionTable.setFilterDecorator(new MyFilterDecorator(myUI));
        extraPositionTable.setStyleName(ValoTheme.TABLE_SMALL);
        extraPositionTable.setSizeFull();
        extraPositionTable.setNullSelectionAllowed(false);
        extraPositionTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        extraPositionTable.setFilterBarVisible(true);
        extraPositionTable.setFooterVisible(false);
        extraPositionTable.setSelectable(true);
        extraPositionTable.setNullSelectionAllowed(false);
        extraPositionTable.setMultiSelect(true);
        extraPositionTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        extraPositionTable.addValueChangeListener(this);

        GridLayout extraPositionsGrid = new GridLayout(2, 2);
        extraPositionsGrid.setMargin(new MarginInfo(true, false));
        extraPositionsGrid.setSizeFull();
        extraPositionsGrid.setSpacing(true);
        extraPositionsGrid.addComponent(selectAllExtraPositionsBtn);
        extraPositionsGrid.addComponent(deselectAllExtraPositionsBtn);
        extraPositionsGrid.addComponent(extraPositionTable, 0, 1, 1, 1);
        extraPositionsGrid.setRowExpandRatio(1, 1);

        workingStatusesMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.WorkingStatus));
        workingStatusesMCB.setRequired(true);
        workingStatusesMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        workingStatusesMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        workingStatusesMCB.setWidth("100%");
        workingStatusesMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        workingStatusesMCB.setFilteringMode(FilteringMode.CONTAINS);
        workingStatusesMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        workingStatusesMCB.setShowSelectedOnTop(false);
        workingStatusesMCB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
        workingStatusesMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            yearSelect.setContainerDataSource(dbd.exec_for_select(myUI, sysSettings.dbYear));
            workingStatusesMCB.setContainerDataSource(dbd.exec_for_select(myUI, sysSettings.dbWorking_status));
            branchTable.setContainerDataSource(dbd.exec_for_select(myUI, sysSettings.dbBranchTable));
            positionTable.setContainerDataSource(dbd.exec_positions_for_select(myUI,
                    currentUser.hasRole(SystemSettings.rnAdmin), currentUser.hasRole(SystemSettings.rnHr)));
            positionTable.setVisibleColumns(NATURAL_COL_ORDER);
            extraPositionTable.setContainerDataSource(dbd.exec_positions_for_select(myUI,
                    currentUser.hasRole(SystemSettings.rnAdmin), currentUser.hasRole(SystemSettings.rnHr)));
            extraPositionTable.setVisibleColumns(NATURAL_COL_ORDER);
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        workingStatusesMCB.setValue(sysSettings.convertToSet(
                workingStatusesMCB.getContainerDataSource().getItemIds()));
        workingStatusesMCB.addValueChangeListener(this);

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

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

        Accordion acr = new Accordion();
        acr.setSizeFull();
        acr.setTabCaptionsAsHtml(true);
        acr.addTab(schoolsGrid, myUI.getMessage(SptMessages.Schools) + "<font color='red'> *</font>");
        acr.addTab(branchesGrid, myUI.getMessage(SptMessages.Lessons) + "<font color='red'> *</font>");
        acr.addTab(positionsGrid, myUI.getMessage(SptMessages.MainPositions) + "<font color='red'> *</font>");
        acr.addTab(extraPositionsGrid, myUI.getMessage(SptMessages.ExtraPosition));

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(acr, 0, 1, 3, 1);
        leftGrid.addComponent(workingStatusesMCB, 0, 2, 3, 2);
        leftGrid.addComponent(generateBtn, 0, 3, 2, 3);
        leftGrid.addComponent(excelBtn, 3, 3);
        leftGrid.setRowExpandRatio(1, 1);
        ((GridLayout) spltPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) spltPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        vl.setWidth("100%");
        vl.setSpacing(true);
        vl.setMargin(true);
        spltPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            vl.setHeightUndefined();
            vl.removeAllComponents();
            if (yearSelect.isValid() && workingStatusesMCB.isValid()) {
                if (!((Set<?>) schoolTable.getValue()).isEmpty() && !((Set<?>) branchTable.getValue()).isEmpty()
                        && !((Set<?>) positionTable.getValue()).isEmpty()) {
                    IndexedContainer container = null;
                    LinkedList<Integer> branchIds = null;
                    int totalEmpl = 0;
                    int totalAsMainBranch = 0;
                    int totalAsExtraBranch = 0;
                    if (((Set<?>) schoolTable.getValue()).size() > 1) {
                        container = new IndexedContainer();
                        container.addContainerProperty(myUI.getMessage(SptMessages.Lesson), String.class, null);
                        container.addContainerProperty(myUI.getMessage(SptMessages.Employee), String.class,
                                myUI.getMessage(SptMessages.TotalEmployees) + "0");
                        container.addContainerProperty(myUI.getMessage(SptMessages.Hours), Integer.class, 0);
                        container.addContainerProperty(myUI.getMessage(SptMessages.ExtraHours), Integer.class, 0);
                        container.addContainerProperty(myUI.getMessage(SptMessages.MainBranch), String.class, "0");
                        container.addContainerProperty(myUI.getMessage(SptMessages.ExtraBranches), String.class, "0");
                        container.addItem("t");
                        branchIds = new LinkedList<Integer>((Set<Integer>) branchTable.getValue());
                        Collections.sort(branchIds);
                    }
                    Iterator<Integer> iter = ((Set<Integer>) schoolTable.getValue()).iterator();
                    while (iter.hasNext()) {
                        Integer next = iter.next();
                        try {
                            Label schholNameLbl = new Label();
                            schholNameLbl.setWidth("100%");
                            schholNameLbl.setContentMode(ContentMode.HTML);
                            schholNameLbl.setValue(schoolTable.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Name)).getValue().toString());
                            schholNameLbl.setStyleName("tableCpt");
                            vl.addComponent(schholNameLbl);

                            DbEmployeeLessons dbel = new DbEmployeeLessons();
                            dbel.connect();
                            FormattedTable dataTable = new FormattedTable();
                            dataTable.setFooterVisible(false);
                            dataTable.setWidth("100%");
                            dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
                            dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
                            dataTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
                            dataTable.addStyleName("noWrapHeader");
                            dataTable.setCellStyleGenerator(new Table.CellStyleGenerator() {

                                @Override
                                public String getStyle(Table source, Object itemId, Object propertyId) {

                                    if (propertyId == null) {
                                        // Styling for row
                                        if (itemId.toString().startsWith("s")) {
                                            return "highlight-green";
                                        } else if (itemId.toString().startsWith("t")) {
                                            return "highlight-darkgreen";
                                        } else {
                                            return null;
                                        }
                                    } else {
                                        // styling for column propertyId
                                        return null;
                                    }
                                }
                            });
                            vl.addComponent(dataTable);
                            if (((Set<?>) schoolTable.getValue()).size() == 1) {
                                vl.setHeight("100%");
                                vl.setExpandRatio(dataTable, 1);
                                dataTable.setHeight("100%");
                            } else {
                                dataTable.setPageLength(0);
                            }

                            dataTable.setContainerDataSource(dbel.execSQLHours(myUI, (Integer) yearSelect.getValue(), next,
                                    sysSettings.convertCollectionToStr((Set<?>) branchTable.getValue()),
                                    sysSettings.convertCollectionToStr((Set<?>) positionTable.getValue()),
                                    sysSettings.convertCollectionToStr((Set<?>) extraPositionTable.getValue()),
                                    sysSettings.convertCollectionToStr((Set<?>) workingStatusesMCB.getValue())));
                            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Hours), Table.Align.RIGHT);
                            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.ExtraHours), Table.Align.RIGHT);
                            if (((Set<?>) schoolTable.getValue()).size() > 1) {

                                Item item = container.getItem("t");
                                item.getItemProperty(myUI.getMessage(SptMessages.Hours)).setValue(
                                        (Integer) item.getItemProperty(myUI.getMessage(SptMessages.Hours)).getValue()
                                        + (Integer) dataTable.getContainerProperty("t", myUI.getMessage(SptMessages.Hours)).getValue());
                                item.getItemProperty(myUI.getMessage(SptMessages.ExtraHours)).setValue(
                                        (Integer) item.getItemProperty(myUI.getMessage(SptMessages.ExtraHours)).getValue()
                                        + (Integer) dataTable.getContainerProperty("t", myUI.getMessage(SptMessages.ExtraHours)).getValue());
                                totalEmpl += Integer.parseInt((dataTable.getContainerProperty("t", myUI.getMessage(SptMessages.Employee)).getValue()
                                        .toString().replace(myUI.getMessage(SptMessages.TotalEmployees), "")));
                                item.getItemProperty(myUI.getMessage(SptMessages.Employee)).setValue(
                                        myUI.getMessage(SptMessages.TotalEmployees) + totalEmpl);
                                totalAsMainBranch += Integer.parseInt((dataTable.getContainerProperty("t", myUI.getMessage(SptMessages.MainBranch)).getValue()
                                        .toString().replace(myUI.getMessage(SptMessages.TotalEmployeesAsMainBranch) + ": ", "")));
                                item.getItemProperty(myUI.getMessage(SptMessages.MainBranch)).setValue(
                                        myUI.getMessage(SptMessages.TotalEmployeesAsMainBranch) + ": " + totalAsMainBranch);
                                totalAsExtraBranch += Integer.parseInt((dataTable.getContainerProperty("t", myUI.getMessage(SptMessages.ExtraBranches)).getValue()
                                        .toString().replace(myUI.getMessage(SptMessages.TotalEmployeesAsExtraBranch) + ": ", "")));
                                item.getItemProperty(myUI.getMessage(SptMessages.ExtraBranches)).setValue(
                                        myUI.getMessage(SptMessages.TotalEmployeesAsExtraBranch) + ": " + totalAsExtraBranch);
                                item.getItemProperty(myUI.getMessage(SptMessages.Lesson)).setValue(
                                        myUI.getMessage(SptMessages.TotalBranches) + ((Set<?>) branchTable.getValue()).size());

                                Iterator<Integer> iter2 = branchIds.descendingIterator();
                                while (iter2.hasNext()) {
                                    Integer branchId = iter2.next();
                                    item = container.addItem("s" + branchId);
                                    if (item == null) {
                                        item = container.getItem("s" + branchId);
                                    }
                                    item.getItemProperty(myUI.getMessage(SptMessages.Lesson)).setValue(
                                            branchTable.getContainerProperty(branchId, myUI.getMessage(SptMessages.Name)).getValue());
                                    item.getItemProperty(myUI.getMessage(SptMessages.Hours)).setValue(
                                            (Integer) item.getItemProperty(myUI.getMessage(SptMessages.Hours)).getValue()
                                            + (Integer) dataTable.getContainerProperty("s" + branchId, myUI.getMessage(SptMessages.Hours)).getValue());
                                    item.getItemProperty(myUI.getMessage(SptMessages.ExtraHours)).setValue(
                                            (Integer) item.getItemProperty(myUI.getMessage(SptMessages.ExtraHours)).getValue()
                                            + (Integer) dataTable.getContainerProperty("s" + branchId, myUI.getMessage(SptMessages.ExtraHours)).getValue());
                                    item.getItemProperty(myUI.getMessage(SptMessages.Employee)).setValue(
                                            myUI.getMessage(SptMessages.TotalEmployees)
                                            + (Integer.parseInt(item.getItemProperty(myUI.getMessage(SptMessages.Employee)).getValue().toString()
                                                    .replace(myUI.getMessage(SptMessages.TotalEmployees), ""))
                                            + Integer.parseInt(dataTable.getContainerProperty("s" + branchId, myUI.getMessage(SptMessages.Employee)).getValue().toString()
                                                    .replace(myUI.getMessage(SptMessages.TotalEmployees), ""))));
                                    item.getItemProperty(myUI.getMessage(SptMessages.MainBranch)).setValue(
                                            myUI.getMessage(SptMessages.TotalEmployeesAsMainBranch)
                                            + branchTable.getContainerProperty(branchId, myUI.getMessage(SptMessages.Name)).getValue() + ": "
                                            + (Integer.parseInt(item.getItemProperty(myUI.getMessage(SptMessages.MainBranch)).getValue().toString()
                                                    .replace(myUI.getMessage(SptMessages.TotalEmployeesAsMainBranch)
                                                            + branchTable.getContainerProperty(branchId, myUI.getMessage(SptMessages.Name)).getValue() + ": ", ""))
                                            + Integer.parseInt(dataTable.getContainerProperty("s" + branchId, myUI.getMessage(SptMessages.MainBranch)).getValue().toString()
                                                    .replace(myUI.getMessage(SptMessages.TotalEmployeesAsMainBranch)
                                                            + branchTable.getContainerProperty(branchId, myUI.getMessage(SptMessages.Name)).getValue() + ": ", ""))));
                                    item.getItemProperty(myUI.getMessage(SptMessages.ExtraBranches)).setValue(
                                            myUI.getMessage(SptMessages.TotalEmployeesAsExtraBranch)
                                            + branchTable.getContainerProperty(branchId, myUI.getMessage(SptMessages.Name)).getValue() + ": "
                                            + (Integer.parseInt(item.getItemProperty(myUI.getMessage(SptMessages.ExtraBranches)).getValue().toString()
                                                    .replace(myUI.getMessage(SptMessages.TotalEmployeesAsExtraBranch)
                                                            + branchTable.getContainerProperty(branchId, myUI.getMessage(SptMessages.Name)).getValue() + ": ", ""))
                                            + Integer.parseInt(dataTable.getContainerProperty("s" + branchId, myUI.getMessage(SptMessages.ExtraBranches)).getValue().toString()
                                                    .replace(myUI.getMessage(SptMessages.TotalEmployeesAsExtraBranch)
                                                            + branchTable.getContainerProperty(branchId, myUI.getMessage(SptMessages.Name)).getValue() + ": ", ""))));
                                }
                            }
                            if (dataTable.getContainerDataSource().size() != 0) {
                                excelBtn.setEnabled(true);
                            }
                            dbel.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                    }
                    if (((Set<?>) schoolTable.getValue()).size() > 1) {
                        Label schholNameLbl = new Label();
                        schholNameLbl.setWidth("100%");
                        schholNameLbl.setContentMode(ContentMode.HTML);
                        schholNameLbl.setValue(myUI.getMessage(SptMessages.AllSchools));
                        schholNameLbl.setStyleName("tableCpt");
                        vl.addComponent(schholNameLbl);

                        FormattedTable dataTable = new FormattedTable();
                        dataTable.setFooterVisible(false);
                        dataTable.setWidth("100%");
                        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
                        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
                        dataTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
                        dataTable.addStyleName("noWrapHeader");
                        dataTable.setCellStyleGenerator(new Table.CellStyleGenerator() {

                            @Override
                            public String getStyle(Table source, Object itemId, Object propertyId) {

                                if (propertyId == null) {
                                    // Styling for row
                                    if (itemId.toString().startsWith("s")) {
                                        return "highlight-green";
                                    } else if (itemId.toString().startsWith("t")) {
                                        return "highlight-darkgreen";
                                    } else {
                                        return null;
                                    }
                                } else {
                                    // styling for column propertyId
                                    return null;
                                }
                            }
                        });
                        vl.addComponent(dataTable);
                        dataTable.setPageLength(0);

                        dataTable.setContainerDataSource(container);
                        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Hours), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.ExtraHours), Table.Align.RIGHT);

                    }
                } else {
                    Notification.show(myUI.getMessage(SptMessages.RequiredField),
                            Notification.Type.WARNING_MESSAGE);
                }
            } else {
                Notification.show(myUI.getMessage(SptMessages.RequiredField),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == excelBtn) {
            try {
                for (int i = 0; i < vl.getComponentCount(); i += 2) {
                    if (i == 0) {
                        excelReport = new ExcelExport((Table) vl.getComponent(i + 1), ((Label) vl.getComponent(i)).getValue());
                        excelReport.setReportTitle(((Label) vl.getComponent(i)).getValue());
                        excelReport.setDisplayTotals(false);

                    } else {
                        excelReport.convertTable();
                        excelReport.setNextTable((Table) vl.getComponent(i + 1), ((Label) vl.getComponent(i)).getValue());
                        excelReport.setReportTitle(((Label) vl.getComponent(i)).getValue());
                        excelReport.setDisplayTotals(false);
                    }
                }
                excelReport.export();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllSchoolsBtn) {
            schoolTable.setValue(schoolTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllSchoolsBtn) {
            schoolTable.setValue(null);
        } else if (source == selectAllBranchesBtn) {
            branchTable.setValue(branchTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBranchesBtn) {
            branchTable.setValue(null);
        } else if (source == selectAllPositionsBtn) {
            positionTable.setValue(positionTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllPositionsBtn) {
            positionTable.setValue(null);
        } else if (source == selectAllExtraPositionsBtn) {
            extraPositionTable.setValue(extraPositionTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllExtraPositionsBtn) {
            extraPositionTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property.getValue() != null) {
            excelBtn.setEnabled(false);
        }
    }
}
