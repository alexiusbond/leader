/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports.hr;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HRGeneralReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(HRGeneralReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, excelBtn;
    private HorizontalSplitPanel splitPanel;
    private VerticalLayout leftLay;
    private ComboBoxMultiselectMax schoolsMCB, positionsMCB, extraPositionsMCB, workingStatusesMCB, genderMCB, nationalityMCB,
            citizenshipMCB, martialStatusMCB, canBeAdvisorMCB, contractTypeMCB, gradSchoolMCB, healthStatusMCB, examMCB,
            mainBranchMCB, extraBranchMCB, universityMCB, workPlaceMCB, certificateMCB, languageMCB;
    private ComboBoxMax yearSelect;
    private EnhancedFormatExcelExport excelReport;
    private Grid.FooterRow footer;

    private Subject currentUser = SecurityUtils.getSubject();
    public Grid dataGrid;

    public HRGeneralReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {
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

        Panel p = new Panel();
        p.setSizeFull();
        p.setStyleName(ValoTheme.PANEL_BORDERLESS);

        leftLay = new VerticalLayout();
        leftLay.setWidth("100%");
        leftLay.setSpacing(true);

        yearSelect = new ComboBoxMax(myUI.getMessage(SptMessages.LessonsYear));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.addValueChangeListener(this);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        yearSelect.setWidth("100%");
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);
        leftLay.addComponent(yearSelect);

        schoolsMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Schools));
        schoolsMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        schoolsMCB.addValueChangeListener(this);
        schoolsMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        schoolsMCB.setWidth("100%");
        schoolsMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        schoolsMCB.setFilteringMode(FilteringMode.CONTAINS);
        schoolsMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        schoolsMCB.setShowSelectAllButton((filter, page) -> true);
        schoolsMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(schoolsMCB);

        positionsMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Positions));
        positionsMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        positionsMCB.addValueChangeListener(this);
        positionsMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        positionsMCB.setWidth("100%");
        positionsMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        positionsMCB.setFilteringMode(FilteringMode.CONTAINS);
        positionsMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        positionsMCB.setShowSelectAllButton((filter, page) -> true);
        positionsMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(positionsMCB);

        extraPositionsMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.ExtraPositions));
        extraPositionsMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        extraPositionsMCB.addValueChangeListener(this);
        extraPositionsMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        extraPositionsMCB.setWidth("100%");
        extraPositionsMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        extraPositionsMCB.setFilteringMode(FilteringMode.CONTAINS);
        extraPositionsMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        extraPositionsMCB.setShowSelectedOnTop(false);
        extraPositionsMCB.setShowSelectAllButton((filter, page) -> true);
        extraPositionsMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(extraPositionsMCB);

        workingStatusesMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.WorkingStatuses));
        workingStatusesMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        workingStatusesMCB.addValueChangeListener(this);
        workingStatusesMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        workingStatusesMCB.setWidth("100%");
        workingStatusesMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        workingStatusesMCB.setFilteringMode(FilteringMode.CONTAINS);
        workingStatusesMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        workingStatusesMCB.setShowSelectAllButton((filter, page) -> true);
        workingStatusesMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(workingStatusesMCB);

        genderMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Genders));
        genderMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        genderMCB.addValueChangeListener(this);
        genderMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        genderMCB.setWidth("100%");
        genderMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        genderMCB.setFilteringMode(FilteringMode.CONTAINS);
        genderMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        genderMCB.setShowSelectAllButton((filter, page) -> true);
        genderMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(genderMCB);

        nationalityMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Nationalities));
        nationalityMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        nationalityMCB.addValueChangeListener(this);
        nationalityMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        nationalityMCB.setWidth("100%");
        nationalityMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        nationalityMCB.setFilteringMode(FilteringMode.CONTAINS);
        nationalityMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        nationalityMCB.setShowSelectAllButton((filter, page) -> true);
        nationalityMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(nationalityMCB);

        citizenshipMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Citizenships));
        citizenshipMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        citizenshipMCB.addValueChangeListener(this);
        citizenshipMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        citizenshipMCB.setWidth("100%");
        citizenshipMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        citizenshipMCB.setFilteringMode(FilteringMode.CONTAINS);
        citizenshipMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        citizenshipMCB.setShowSelectAllButton((filter, page) -> true);
        citizenshipMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(citizenshipMCB);

        martialStatusMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.MartialStatuses));
        martialStatusMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        martialStatusMCB.addValueChangeListener(this);
        martialStatusMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        martialStatusMCB.setWidth("100%");
        martialStatusMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        martialStatusMCB.setFilteringMode(FilteringMode.CONTAINS);
        martialStatusMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        martialStatusMCB.setShowSelectAllButton((filter, page) -> true);
        martialStatusMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(martialStatusMCB);

        canBeAdvisorMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.CanBeAdvisors));
        canBeAdvisorMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        canBeAdvisorMCB.addValueChangeListener(this);
        canBeAdvisorMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        canBeAdvisorMCB.setWidth("100%");
        canBeAdvisorMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        canBeAdvisorMCB.setFilteringMode(FilteringMode.CONTAINS);
        canBeAdvisorMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        canBeAdvisorMCB.setShowSelectAllButton((filter, page) -> true);
        canBeAdvisorMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(canBeAdvisorMCB);

        contractTypeMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.ContractTypes));
        contractTypeMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        contractTypeMCB.addValueChangeListener(this);
        contractTypeMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        contractTypeMCB.setWidth("100%");
        contractTypeMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        contractTypeMCB.setFilteringMode(FilteringMode.CONTAINS);
        contractTypeMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        contractTypeMCB.setShowSelectAllButton((filter, page) -> true);
        contractTypeMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(contractTypeMCB);

        gradSchoolMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.GraduationSchools));
        gradSchoolMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        gradSchoolMCB.addValueChangeListener(this);
        gradSchoolMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        gradSchoolMCB.setWidth("100%");
        gradSchoolMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        gradSchoolMCB.setFilteringMode(FilteringMode.CONTAINS);
        gradSchoolMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        gradSchoolMCB.setShowSelectAllButton((filter, page) -> true);
        gradSchoolMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(gradSchoolMCB);

        healthStatusMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.HealthStatuses));
        healthStatusMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        healthStatusMCB.addValueChangeListener(this);
        healthStatusMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        healthStatusMCB.setWidth("100%");
        healthStatusMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        healthStatusMCB.setFilteringMode(FilteringMode.CONTAINS);
        healthStatusMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        healthStatusMCB.setShowSelectAllButton((filter, page) -> true);
        healthStatusMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(healthStatusMCB);

        examMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Exams));
        examMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        examMCB.addValueChangeListener(this);
        examMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        examMCB.setWidth("100%");
        examMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        examMCB.setFilteringMode(FilteringMode.CONTAINS);
        examMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        examMCB.setShowSelectAllButton((filter, page) -> true);
        examMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(examMCB);

        mainBranchMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.MainBranches));
        mainBranchMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        mainBranchMCB.addValueChangeListener(this);
        mainBranchMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        mainBranchMCB.setWidth("100%");
        mainBranchMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        mainBranchMCB.setFilteringMode(FilteringMode.CONTAINS);
        mainBranchMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        mainBranchMCB.setShowSelectAllButton((filter, page) -> true);
        mainBranchMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(mainBranchMCB);

        extraBranchMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.ExtraBranches));
        extraBranchMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        extraBranchMCB.addValueChangeListener(this);
        extraBranchMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        extraBranchMCB.setWidth("100%");
        extraBranchMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        extraBranchMCB.setFilteringMode(FilteringMode.CONTAINS);
        extraBranchMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        extraBranchMCB.setShowSelectAllButton((filter, page) -> true);
        extraBranchMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(extraBranchMCB);

        universityMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Education));
        universityMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        universityMCB.addValueChangeListener(this);
        universityMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        universityMCB.setWidth("100%");
        universityMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        universityMCB.setFilteringMode(FilteringMode.CONTAINS);
        universityMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        universityMCB.setShowSelectAllButton((filter, page) -> true);
        universityMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(universityMCB);

        workPlaceMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.WorkPlaces));
        workPlaceMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        workPlaceMCB.addValueChangeListener(this);
        workPlaceMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        workPlaceMCB.setWidth("100%");
        workPlaceMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        workPlaceMCB.setFilteringMode(FilteringMode.CONTAINS);
        workPlaceMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        workPlaceMCB.setShowSelectAllButton((filter, page) -> true);
        workPlaceMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(workPlaceMCB);

        certificateMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Certificates));
        certificateMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        certificateMCB.addValueChangeListener(this);
        certificateMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        certificateMCB.setWidth("100%");
        certificateMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        certificateMCB.setFilteringMode(FilteringMode.CONTAINS);
        certificateMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        certificateMCB.setShowSelectAllButton((filter, page) -> true);
        certificateMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(certificateMCB);

        languageMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Languages));
        languageMCB.setInputPrompt(myUI.getMessage(SptMessages.All));
        languageMCB.addValueChangeListener(this);
        languageMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        languageMCB.setWidth("100%");
        languageMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        languageMCB.setFilteringMode(FilteringMode.CONTAINS);
        languageMCB.setClearButtonCaption(myUI.getMessage(SptMessages.Clear));
        languageMCB.setShowSelectAllButton((filter, page) -> true);
        languageMCB.setSelectAllButtonCaption(myUI.getMessage(SptMessages.SelectAll));
        leftLay.addComponent(languageMCB);

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
            healthStatusMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbHealthStatus, false));
            examMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbExamTable, false));
            mainBranchMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbBranchTable, false));
            extraBranchMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbBranchTable, false));
            universityMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbUniversityTable, false));
            workPlaceMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbWork_placeTable, false));
            certificateMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbCertificateTable, false));
            languageMCB.setContainerDataSource(dbd.exec_for_select(myUI, SystemSettings.dbLanguageTable, false));
            canBeAdvisorMCB.setContainerDataSource(dbd.execSQL_yes_no(myUI));
            positionsMCB.setContainerDataSource(dbd.exec_positions_for_select(myUI,
                    currentUser.hasRole(SystemSettings.rnAdmin), currentUser.hasRole(SystemSettings.rnHr)));
            extraPositionsMCB.setContainerDataSource(dbd.exec_positions_for_select(myUI,
                    currentUser.hasRole(SystemSettings.rnAdmin), currentUser.hasRole(SystemSettings.rnHr)));
            dbd.close();
            DbSchool dbCon = new DbSchool();
            dbCon.connect();
            schoolsMCB.setContainerDataSource(dbCon.execSchoolSel(myUI, "1,3,4,5,6"));
            gradSchoolMCB.setContainerDataSource(dbCon.execSchoolSel(myUI, "1,3,4"));
            Item item = gradSchoolMCB.getContainerDataSource().addItem(-1);
            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(myUI.getMessage(SptMessages.OtherSchool));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());

        p.setContent(leftLay);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(p, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(true);
        vl.setSizeFull();
        dataGrid = new Grid();
        dataGrid.setFooterVisible(true);
        dataGrid.setSizeFull();
        dataGrid.setSelectionMode(Grid.SelectionMode.NONE);
        dataGrid.setStyleName(ValoTheme.TABLE_COMPACT);
        dataGrid.addStyleName("noWrapHeader");
        dataGrid.addStyleName("noWrap");
        vl.addComponent(dataGrid);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (yearSelect.isValid() && workingStatusesMCB.isValid()) {
                try {
                    Map<String, String> params = new HashMap<>();
                    insertParameter(params, myUI.getMessage(SptMessages.Schools), schoolsMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.Positions), positionsMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.ExtraPositions), extraPositionsMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.WorkingStatuses), workingStatusesMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.Genders), genderMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.Nationalities), nationalityMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.MartialStatuses), martialStatusMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.CanBeAdvisors), canBeAdvisorMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.ContractTypes), contractTypeMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.Citizenships), citizenshipMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.GraduationSchools), gradSchoolMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.HealthStatuses), healthStatusMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.MainBranches), mainBranchMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.ExtraPositions), extraPositionsMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.Education), universityMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.WorkPlaces), workPlaceMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.Certificates), certificateMCB);
                    insertParameter(params, myUI.getMessage(SptMessages.Languages), languageMCB);

                    DbEmployee dbEmployee = new DbEmployee();
                    dbEmployee.connect();
                    dataGrid.setContainerDataSource(dbEmployee.execSQL(myUI, (Integer) yearSelect.getValue(), params));
                    dbEmployee.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                dataGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
                    if (cellReference.getProperty().getType() == Double.class
                            || cellReference.getProperty().getType() == Integer.class) {
                        return "align-right";
                    } else {
                        return null;
                    }
                });
                if (footer == null) {
                    footer = dataGrid.appendFooterRow();
                }
                footer.getCell(myUI.getMessage(SptMessages.School)).setText(myUI.getMessage(SptMessages.Total));
                footer.getCell(myUI.getMessage(SptMessages.LastName)).setText(dataGrid.getContainerDataSource().size() + "");
                dataGrid.setSizeFull();
                if (dataGrid.getContainerDataSource().size() != 0) {
                    excelBtn.setEnabled(true);
                }
            } else {
                Notification.show(myUI.getMessage(SptMessages.RequiredField),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == excelBtn) {
            try {
                Table t = new Table();
                t.setContainerDataSource(dataGrid.getContainerDataSource());

                Window w = new Window();
                w.setModal(true);
                myUI.addWindow(w);
                w.setContent(t);

                excelReport = new EnhancedFormatExcelExport(t, myUI.getMessage(SptMessages.HRGeneralReport));
                excelReport.setReportTitle(myUI.getMessage(SptMessages.HRGeneralReport));
                excelReport.setDisplayTotals(true);
                excelReport.convertTable();
                excelReport.sendConverted();
                w.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    private void insertParameter(Map<String, String> params, String key, ComboBoxMultiselectMax cb) {
        if (cb.getContainerDataSource().size() != ((Set<?>) schoolsMCB.getValue()).size()) {
            params.put(key, SystemSettings.convertCollectionToStr((Set<?>) schoolsMCB.getValue()));
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
