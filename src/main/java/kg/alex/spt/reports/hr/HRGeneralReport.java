/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports.hr;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbEmployee;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.tableexport.EnhancedFormatExcelExport;
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

import java.util.Set;

public class HRGeneralReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(HRGeneralReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, excelBtn, selectAllSchoolsBtn, deselectAllSchoolsBtn,
            selectAllPositionsBtn, deselectAllPositionsBtn, selectAllExtraPositionsBtn, deselectAllExtraPositionsBtn;
    private HorizontalSplitPanel splitPanel;
    private GridLayout leftGrid;
    private FilterTable schoolTable, positionTable, extraPositionTable;
    private ComboBoxMultiselectMax workingStatusesMCB, genderMCB, nationalityMCB, citizenshipMCB, martialStatusMCB, canBeAdvisorMCB, contractTypeMCB;
    private ComboBoxMax yearSelect;
    private EnhancedFormatExcelExport excelReport;
    private String[] NATURAL_COL_ORDER;

    private Subject currentUser = SecurityUtils.getSubject();
    public FormattedTable dataTable;

    public HRGeneralReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Title)};
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        Panel p = new Panel();
        p.setSizeFull();
        p.setStyleName(ValoTheme.PANEL_BORDERLESS);

        leftGrid = new GridLayout(4, 14);
        leftGrid.setWidth("100%");
        leftGrid.setSpacing(true);

        yearSelect = new ComboBoxMax(myUI.getMessage(SptMessages.LessonsYear));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        yearSelect.setWidth("100%");
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
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
        schoolTable.setWidth("100%");
        schoolTable.setPageLength(5);
        schoolTable.setNullSelectionAllowed(false);
        schoolTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        schoolTable.setFilterBarVisible(true);
        schoolTable.setFooterVisible(false);
        schoolTable.setSelectable(true);
        schoolTable.setNullSelectionAllowed(false);
        schoolTable.setMultiSelect(true);
        schoolTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
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
        positionTable.setWidth("100%");
        positionTable.setPageLength(5);
        positionTable.setNullSelectionAllowed(false);
        positionTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        positionTable.setFilterBarVisible(true);
        positionTable.setFooterVisible(false);
        positionTable.setSelectable(true);
        positionTable.setNullSelectionAllowed(false);
        positionTable.setMultiSelect(true);
        positionTable.setMultiSelectMode(MultiSelectMode.SIMPLE);

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
        extraPositionTable.setWidth("100%");
        extraPositionTable.setPageLength(5);
        extraPositionTable.setNullSelectionAllowed(false);
        extraPositionTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        extraPositionTable.setFilterBarVisible(true);
        extraPositionTable.setFooterVisible(false);
        extraPositionTable.setSelectable(true);
        extraPositionTable.setNullSelectionAllowed(false);
        extraPositionTable.setMultiSelect(true);
        extraPositionTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        extraPositionTable.addValueChangeListener(this);

        workingStatusesMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.WorkingStatus));
        workingStatusesMCB.setRequired(true);
        workingStatusesMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        workingStatusesMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        workingStatusesMCB.setWidth("100%");
        workingStatusesMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
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

        genderMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Gender));
        genderMCB.setRequired(true);
        genderMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        genderMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        genderMCB.setWidth("100%");
        genderMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        genderMCB.setFilteringMode(FilteringMode.CONTAINS);
        genderMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        genderMCB.setShowSelectedOnTop(false);
        genderMCB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
        genderMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));

        nationalityMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Nationality));
        nationalityMCB.setRequired(true);
        nationalityMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        nationalityMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        nationalityMCB.setWidth("100%");
        nationalityMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        nationalityMCB.setFilteringMode(FilteringMode.CONTAINS);
        nationalityMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        nationalityMCB.setShowSelectedOnTop(false);
        nationalityMCB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
        nationalityMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));

        citizenshipMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Citizenship));
        citizenshipMCB.setRequired(true);
        citizenshipMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        citizenshipMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        citizenshipMCB.setWidth("100%");
        citizenshipMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        citizenshipMCB.setFilteringMode(FilteringMode.CONTAINS);
        citizenshipMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        citizenshipMCB.setShowSelectedOnTop(false);
        citizenshipMCB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
        citizenshipMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));

        martialStatusMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.MartialStatus));
        martialStatusMCB.setRequired(true);
        martialStatusMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        martialStatusMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        martialStatusMCB.setWidth("100%");
        martialStatusMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        martialStatusMCB.setFilteringMode(FilteringMode.CONTAINS);
        martialStatusMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        martialStatusMCB.setShowSelectedOnTop(false);
        martialStatusMCB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
        martialStatusMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));

        canBeAdvisorMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.CanBeAdvisor));
        canBeAdvisorMCB.setRequired(true);
        canBeAdvisorMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        canBeAdvisorMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        canBeAdvisorMCB.setWidth("100%");
        canBeAdvisorMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        canBeAdvisorMCB.setFilteringMode(FilteringMode.CONTAINS);
        canBeAdvisorMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        canBeAdvisorMCB.setShowSelectedOnTop(false);
        canBeAdvisorMCB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
        canBeAdvisorMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));

        contractTypeMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.ContractType));
        contractTypeMCB.setRequired(true);
        contractTypeMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        contractTypeMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        contractTypeMCB.setWidth("100%");
        contractTypeMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        contractTypeMCB.setFilteringMode(FilteringMode.CONTAINS);
        contractTypeMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        contractTypeMCB.setShowSelectedOnTop(false);
        contractTypeMCB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
        contractTypeMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));

        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            yearSelect.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbYear, true));
            workingStatusesMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbWorking_status, false));
            genderMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbGender, false));
            nationalityMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbNationality, false));
            citizenshipMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbCountry, false));
            martialStatusMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbMartialStatus, false));
            contractTypeMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbSalaryCategory, false));
            canBeAdvisorMCB.setContainerDataSource(dbd.execSQL_yes_no(myUI));
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

        workingStatusesMCB.setValue(SystemSettings.convertToSet(workingStatusesMCB.getContainerDataSource().getItemIds()));
        workingStatusesMCB.addValueChangeListener(this);
        genderMCB.setValue(SystemSettings.convertToSet(genderMCB.getContainerDataSource().getItemIds()));
        genderMCB.addValueChangeListener(this);
        nationalityMCB.setValue(SystemSettings.convertToSet(nationalityMCB.getContainerDataSource().getItemIds()));
        nationalityMCB.addValueChangeListener(this);
        citizenshipMCB.setValue(SystemSettings.convertToSet(citizenshipMCB.getContainerDataSource().getItemIds()));
        citizenshipMCB.addValueChangeListener(this);
        martialStatusMCB.setValue(SystemSettings.convertToSet(martialStatusMCB.getContainerDataSource().getItemIds()));
        martialStatusMCB.addValueChangeListener(this);
        canBeAdvisorMCB.setValue(SystemSettings.convertToSet(canBeAdvisorMCB.getContainerDataSource().getItemIds()));
        canBeAdvisorMCB.addValueChangeListener(this);
        contractTypeMCB.setValue(SystemSettings.convertToSet(contractTypeMCB.getContainerDataSource().getItemIds()));
        contractTypeMCB.addValueChangeListener(this);
        schoolTable.setValue(SystemSettings.convertToSet(schoolTable.getContainerDataSource().getItemIds()));
        schoolTable.addValueChangeListener(this);
        positionTable.setValue(SystemSettings.convertToSet(positionTable.getContainerDataSource().getItemIds()));
        positionTable.addValueChangeListener(this);

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

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);
        hl.addComponent(generateBtn);
        hl.addComponent(excelBtn);
        hl.setExpandRatio(generateBtn, 75);
        hl.setExpandRatio(excelBtn, 25);

        ((GridLayout) splitPanel.getFirstComponent()).addComponent(hl, 0, 2);

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(selectAllSchoolsBtn, 0, 1, 1, 1);
        leftGrid.addComponent(deselectAllSchoolsBtn, 2, 1, 3, 1);
        leftGrid.addComponent(schoolTable, 0, 2, 3, 2);
        leftGrid.addComponent(selectAllPositionsBtn, 0, 3, 1, 3);
        leftGrid.addComponent(deselectAllPositionsBtn, 2, 3, 3, 3);
        leftGrid.addComponent(positionTable, 0, 4, 3, 4);
        leftGrid.addComponent(selectAllExtraPositionsBtn, 0, 5, 1, 5);
        leftGrid.addComponent(deselectAllExtraPositionsBtn, 2, 5, 3, 5);
        leftGrid.addComponent(extraPositionTable, 0, 6, 3, 6);
        leftGrid.addComponent(workingStatusesMCB, 0, 7, 3, 7);
        leftGrid.addComponent(genderMCB, 0, 8, 3, 8);
        leftGrid.addComponent(nationalityMCB, 0, 9, 3, 9);
        leftGrid.addComponent(citizenshipMCB, 0, 10, 3, 10);
        leftGrid.addComponent(martialStatusMCB, 0, 11, 3, 11);
        leftGrid.addComponent(canBeAdvisorMCB, 0, 12, 3, 12);
        leftGrid.addComponent(contractTypeMCB, 0, 13, 3, 13);

        p.setContent(leftGrid);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(p, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(true);
        vl.setSizeFull();
        dataTable = new FormattedTable();
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.addStyleName("noWrapHeader");
        dataTable.setColumnFooter(myUI.getMessage(SptMessages.School), myUI.getMessage(SptMessages.Total));
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (yearSelect.isValid() && workingStatusesMCB.isValid()) {
                if (!((Set<?>) schoolTable.getValue()).isEmpty() && !((Set<?>) positionTable.getValue()).isEmpty()) {
                    try {
                        DbEmployee dbEmployee = new DbEmployee();
                        dbEmployee.connect();
                        dataTable.setContainerDataSource(dbEmployee.execSQL(myUI, (Integer) yearSelect.getValue(),
                                SystemSettings.convertCollectionToStr((Set<?>) schoolTable.getValue()),
                                SystemSettings.convertCollectionToStr((Set<?>) positionTable.getValue()),
                                SystemSettings.convertCollectionToStr((Set<?>) extraPositionTable.getValue()),
                                SystemSettings.convertCollectionToStr((Set<?>) workingStatusesMCB.getValue()),
                                SystemSettings.convertCollectionToStr((Set<?>) genderMCB.getValue()),
                                SystemSettings.convertCollectionToStr((Set<?>) nationalityMCB.getValue()),
                                SystemSettings.convertCollectionToStr((Set<?>) martialStatusMCB.getValue()),
                                SystemSettings.convertCollectionToStr((Set<?>) canBeAdvisorMCB.getValue()),
                                SystemSettings.convertCollectionToStr((Set<?>) contractTypeMCB.getValue()),
                                SystemSettings.convertCollectionToStr((Set<?>) citizenshipMCB.getValue())));
                        dbEmployee.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    dataTable.setColumnFooter(myUI.getMessage(SptMessages.FirstName), dataTable.size() + "");
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Hours), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(SptMessages.ExtraHours), Table.Align.RIGHT);
                    if (dataTable.getContainerDataSource().size() != 0) {
                        excelBtn.setEnabled(true);
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
                excelReport = new EnhancedFormatExcelExport(dataTable, myUI.getMessage(SptMessages.HRGeneralReport));
                excelReport.setReportTitle(myUI.getMessage(SptMessages.HRGeneralReport));
                excelReport.setDisplayTotals(true);
                excelReport.convertTable();
                excelReport.sendConverted();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllSchoolsBtn) {
            schoolTable.setValue(schoolTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllSchoolsBtn) {
            schoolTable.setValue(null);
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
