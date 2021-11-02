/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.ui;

import com.vaadin.server.*;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import kg.alex.spt.dao.*;
import kg.alex.spt.domain.*;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.themes.ValoTheme;

import java.io.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.GenerateRandomString;
import kg.alex.spt.utils.MyFilterDecorator;
import kg.alex.spt.utils.MyFilterGenerator;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.simplefiledownloader.SimpleFileDownloader;

/**
 * @author alex
 */
public class EmployeeDefinitionView extends HorizontalSplitPanel
        implements Button.ClickListener, Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(EmployeeDefinitionView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn, generateBtn;
    private FilterTable employeesDataTable;
    private Table documentsDataTable;
    private int emplID;
    private OptionGroup optionGroup;
    private CheckBox canBeAdvisor;
    private TextField nameTF, loginTF, passwordTF, surnameTF, middlenameTF,
            birth_placeTF, emailTF, spouseFullnameTF, spousePhoneTF,
            spouseHealthNotesTF, hobbiesTF, fobbiesTF;
    private TextArea addressTA, healthNotesTA, shortNotesTA;
    private DateField birthDateDF, gradSchoolStartDF, gradSchoolEndDF;
    private ComboBoxMax genderCB, nationalityCB, martialStatusCB, mainPositionCB, citizenshipCB,
            spouseHealthCB, healthCB, contractCategoryCB, gradSchoolCB;
    private FormLayout formLay, fieldsLayContacts, fieldsLayFamily, fieldsLayExtra;
    private TabSheet tabs;
    private boolean isNew;
    private Label workingStatusLb, mainPositionLb, extraPositionsLb, mainBrancLb, extraBrancesLb, totalHoursLb,
            workingStatTtlLb, filteredLab, captionSpouseInfo;

    private String[] NATURAL_COL_ORDER, NATURAL_COL_ORDER_PHONES, NATURAL_COL_ORDER_CHILDREN,
            NATURAL_COL_ORDER_EDU, NATURAL_COL_ORDER_WORK,
            NATURAL_COL_ORDER_QUESTIONING, NATURAL_COL_ORDER_EXAMS, NATURAL_COL_ORDER_SEMINARS,
            NATURAL_COL_ORDER_LANGUAGES, NATURAL_COL_ORDER_CERTIFICATES, NATURAL_COL_ORDER_BRANCHES, NATURAL_COL_ORDER_LESSONS,
            NATURAL_COL_ORDER_PERMISSIONS, NATURAL_COL_ORDER_ORDERS;
    private VerticalLayout infoLay, documentsLay;
    private GridLayout empSearchLay, contactInfoLay, familyInfoLay, extraInfoLay,
            achievementsInfoLay, profInfoLay, ordersInfoLay;
    private VerticalLayout schoolInfoLay, permissionsLay, leftLay;
    private HorizontalLayout buttonsLay;
    private Upload photoUpl;
    private File myFile;
    private Window statusWindow;
    private Button cancelButton;
    private ProgressBar uploadProgressBar;
    private String photoName, fileName, mimeType;
    private Embedded photoEmb;
    private IndexedContainer workingStatCont;
    public Subject currentUser = SecurityUtils.getSubject();
    private FormattedTable phonesTable, childrenTable, spouseEducationTable, spouseWorkPlacesTable, questioningTable,
            examsTable, seminarsTable, certificatesTable, languagesTable, educationTable, workPlacesTable, branchesTable, lessonsTable,
            permissionTable, ordersTable;
    private Button plusPhonesButton, plusChildButton, plusSpouseEducationButton, plusSpouseWorkPlacesButton,
            plusExamButton, plusSeminarButton, plusCertificateButton, plusLanguageButton,
            plusEducationButton, plusWorkPlaceButton, plusBranchButton, plusLessonsButton, plusOrdersButton;
    private int r_table_counter = 1000;
    private IndexedContainer phonesCont, childrenCont, spouseEducationCont, spouseWorkCont,
            educationCont, workPlacesCont, examsCont, languagesCont, seminarsCont,
            certificatesCont, branchesCont, lessonsCont, permissionCont, ordersCont;
    private ArrayList<String> delPhoneIds = new ArrayList<>();
    private ArrayList<String> delChildIds = new ArrayList<>();
    private ArrayList<EmployeeEducation> delSpouseEducationIds = new ArrayList<>();
    private ArrayList<String> delSpouseWorkIds = new ArrayList<>();
    private ArrayList<String> delLanguagesIds = new ArrayList<>();
    private ArrayList<String> delSeminarsIds = new ArrayList<>();
    private ArrayList<EmployeeCertificate> delCertificatesIds = new ArrayList<>();
    private ArrayList<EmployeeExam> delExamsIds = new ArrayList<>();
    private ArrayList<EmployeeEducation> delEducationIds = new ArrayList<>();
    private ArrayList<String> delWorkPlacesIds = new ArrayList<>();
    private ArrayList<String> delBranchesIds = new ArrayList<>();
    private ArrayList<String> delLessonIds = new ArrayList<>();
    private ArrayList<EmployeeOrder> delOrderIds = new ArrayList<>();
    private SimpleFileDownloader downloader = null;

    public EmployeeDefinitionView(final MyVaadinUI myUI) {
        this.myUI = myUI;

        if (!currentUser.hasRole(SystemSettings.rnAdmin) && !currentUser.hasRole(SystemSettings.rnHr)) {
            emplID = myUI.getUser().getId();
        }
        buildButtonsLayout();
        buildLeftLayout();
        try {
            DbDefinition dbed = new DbDefinition();
            dbed.connect();
            workingStatCont = dbed.execSQL_statuses_with_count(myUI, SystemSettings.dbWorking_status, false);
            dbed.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Id), myUI.getMessage(SptMessages.Surname),
                myUI.getMessage(SptMessages.Firstname),
                myUI.getMessage(SptMessages.WorkingStatus),
                myUI.getMessage(SptMessages.MainPosition)};

        Label eduStatusLab = new Label();
        eduStatusLab.setSizeUndefined();
        eduStatusLab.setContentMode(ContentMode.HTML);
        eduStatusLab.setValue(myUI.getMessage(SptMessages.ShowByWorkingStatuses) + ": ");

        optionGroup = new OptionGroup();
        optionGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        optionGroup.setMultiSelect(true);
        optionGroup.setContainerDataSource(workingStatCont);
        optionGroup.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        optionGroup.select(2);
        optionGroup.addValueChangeListener(this);

        employeesDataTable = new FilterTable();
        employeesDataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        employeesDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        employeesDataTable.setSizeFull();
        employeesDataTable.setNullSelectionAllowed(false);
        employeesDataTable.setFilterBarVisible(true);
        employeesDataTable.setSelectable(true);
        employeesDataTable.addValueChangeListener(this);

        setEmployeesDataTable(optionGroup.getValue().toString());

        HorizontalLayout searchLayFooter = new HorizontalLayout();
        searchLayFooter.setWidth("100%");
        workingStatTtlLb = new Label();
        workingStatTtlLb.setSizeUndefined();
        workingStatTtlLb.setImmediate(true);
        workingStatTtlLb.setContentMode(ContentMode.HTML);
        filteredLab = new Label();
        filteredLab.setSizeUndefined();
        filteredLab.setImmediate(true);
        filteredLab.setContentMode(ContentMode.HTML);
        filteredLab.setValue(myUI.getMessage(SptMessages.Filtered) + ": 0");
        repaint();

        employeesDataTable.setFilterGenerator(new MyFilterGenerator(
                filteredLab, myUI.getMessage(SptMessages.Filtered), employeesDataTable));

        empSearchLay = new GridLayout(2, 3);
        empSearchLay.setSizeFull();
        empSearchLay.setMargin(true);
        searchLayFooter.addComponent(filteredLab);
        searchLayFooter.addComponent(workingStatTtlLb);
        searchLayFooter.setComponentAlignment(filteredLab, Alignment.BOTTOM_LEFT);
        searchLayFooter.setComponentAlignment(workingStatTtlLb, Alignment.BOTTOM_RIGHT);
        searchLayFooter.setExpandRatio(filteredLab, 1);
        searchLayFooter.setExpandRatio(workingStatTtlLb, 2);

        empSearchLay.addComponent(eduStatusLab, 0, 0);
        empSearchLay.addComponent(optionGroup, 1, 0);
        empSearchLay.addComponent(employeesDataTable, 0, 1, 1, 1);
        empSearchLay.addComponent(searchLayFooter, 0, 2, 1, 2);
        empSearchLay.setRowExpandRatio(1, 1);
        empSearchLay.setColumnExpandRatio(0, 1);
        empSearchLay.setComponentAlignment(optionGroup, Alignment.MIDDLE_RIGHT);
        empSearchLay.setComponentAlignment(eduStatusLab, Alignment.MIDDLE_RIGHT);

        this.setSplitPosition(25, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(leftLay);

        buildContactsLayout();
        buildFamilyLayout();
        buildExtraLayout();
        buildAchievementsLayout();
        buildProfLayout();
        buildSchoolInfoLayout();
        buildPermissionsLayout();
        buildOrdersLayout();
        buildDocumentsLayout();

        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabs.addTab(empSearchLay).setCaption(myUI.getMessage(SptMessages.Search));
        if (!currentUser.isPermitted(SystemSettings.cnEmployeeDefinitionView + ":" + SystemSettings.prmTabSearch)) {
            tabs.getTab(empSearchLay).setVisible(false);
        }
        tabs.addTab(contactInfoLay).setCaption(myUI.getMessage(SptMessages.ContactInfo));
        tabs.addTab(profInfoLay).setCaption(myUI.getMessage(SptMessages.ProfInfo));
        tabs.addTab(achievementsInfoLay).setCaption(myUI.getMessage(SptMessages.Achievements));
        tabs.addTab(familyInfoLay).setCaption(myUI.getMessage(SptMessages.FamilyInfo));
        tabs.addTab(extraInfoLay).setCaption(myUI.getMessage(SptMessages.ExtraInfo));
        tabs.addTab(schoolInfoLay).setCaption(myUI.getMessage(SptMessages.EduActivitiesInfo));
        if (!currentUser.isPermitted(SystemSettings.cnEmployeeDefinitionView + ":" + SystemSettings.prmTabEduActivities)) {
            tabs.getTab(schoolInfoLay).setVisible(false);
        }
        tabs.addTab(permissionsLay).setCaption(myUI.getMessage(SptMessages.Permissions));
        if (!currentUser.isPermitted(SystemSettings.cnEmployeeDefinitionView + ":" + SystemSettings.prmTabPermissions)) {
            tabs.getTab(permissionsLay).setVisible(false);
        }
        tabs.addTab(ordersInfoLay).setCaption(myUI.getMessage(SptMessages.OrdersHistory));
        if (!currentUser.isPermitted(SystemSettings.cnEmployeeDefinitionView + ":" + SystemSettings.prmTabOrders)) {
            tabs.getTab(ordersInfoLay).setVisible(false);
        }
        tabs.addTab(documentsLay).setCaption(myUI.getMessage(SptMessages.Documents));
        tabs.addSelectedTabChangeListener(
                new TabSheet.SelectedTabChangeListener() {
                    public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                        prepareNormalMode();
                        if (event.getTabSheet().getSelectedTab() == contactInfoLay
                                && emplID != 0) {
                            setPhonesTable();
                            setContactFields();
                        } else if (event.getTabSheet().getSelectedTab() == familyInfoLay
                                && emplID != 0) {
                            setChildrenTable();
                            setEducationTable(spouseEducationTable, spouseEducationCont, 2);
                            setWorkTable(spouseWorkPlacesTable, spouseWorkCont, 2);
                            setSpouseFields();
                        } else if (event.getTabSheet().getSelectedTab() == extraInfoLay
                                && emplID != 0) {
                            setQuestioningTable();
                            setExtraInfoFields();
                        } else if (event.getTabSheet().getSelectedTab() == achievementsInfoLay
                                && emplID != 0) {
                            setLanguagesTable();
                            setCertificatesTable();
                            setSeminarsTable();
                            setExamsTable();
                        } else if (event.getTabSheet().getSelectedTab() == profInfoLay
                                && emplID != 0) {
                            setEducationTable(educationTable, educationCont, 1);
                            setWorkTable(workPlacesTable, workPlacesCont, 1);
                            setBranchesTable();
                            setGradSchoolFields();
                        } else if (event.getTabSheet().getSelectedTab() == schoolInfoLay && emplID != 0) {
                            setLessonsTable();
                            canBeAdvisor.setValue((Boolean) employeesDataTable.getContainerProperty(
                                    emplID, myUI.getMessage(SptMessages.CanBeAdvisor)).getValue());
                        } else if (event.getTabSheet().getSelectedTab() == permissionsLay && emplID != 0) {
                            if (employeesDataTable.getContainerDataSource().
                                    getContainerProperty(emplID, myUI.getMessage(
                                            SptMessages.Permissions)).getValue() != null) {
                                setPermTable_options(employeesDataTable.getContainerDataSource().
                                        getContainerProperty(emplID, myUI.getMessage(
                                                SptMessages.Permissions)).getValue().toString());
                            } else {
                                clearPermissionsTable();
                            }
                        } else if (event.getTabSheet().getSelectedTab() == ordersInfoLay && emplID != 0) {
                            setOrdersTable();
                        } else if (event.getTabSheet().getSelectedTab() == documentsLay && emplID != 0) {
                            setDocumentsTable();
                        }
                    }
                });
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(new MarginInfo(true, false, false, false));
        vl.addComponent(tabs);
        this.setSecondComponent(vl);
        prepareNormalMode();
        updateInfoLayout();
        employeesDataTable.setValue(emplID);
    }

    private void buildContactsLayout() {

        fieldsLayContacts = new FormLayout();
        fieldsLayContacts.setWidth("100%");
        fieldsLayContacts.setSpacing(false);
        fieldsLayContacts.setMargin(false);

        citizenshipCB = createCombobox(0, null, SystemSettings.dbCountry, true);
        citizenshipCB.setCaption(myUI.getMessage(SptMessages.Citizenship));
        fieldsLayContacts.addComponent(citizenshipCB);

        birth_placeTF = createTextfield(null, null, new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 250, false), true);
        birth_placeTF.setCaption(myUI.getMessage(SptMessages.BirthPlace));
        fieldsLayContacts.addComponent(birth_placeTF);

        emailTF = createTextfield(null, null, new EmailValidator(myUI.getMessage(SptMessages.NotifWrongValue)), true);
        emailTF.setCaption(SystemSettings.email);
        fieldsLayContacts.addComponent(emailTF);

        addressTA = new TextArea(myUI.getMessage(SptMessages.Address));
        addressTA.setRequired(true);
        addressTA.setStyleName(ValoTheme.TEXTFIELD_TINY);
        addressTA.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        addressTA.setWidth("100%");
        addressTA.setRows(7);
        addressTA.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 400, false));
        fieldsLayContacts.addComponent(addressTA);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");

        Label captionPhones = new Label();
        captionPhones.setSizeFull();
        captionPhones.setContentMode(ContentMode.HTML);
        captionPhones.setValue(myUI.getMessage(SptMessages.PhoneNumbers));
        captionPhones.setStyleName("tableCpt");

        Label captionContactsInfo = new Label();
        captionContactsInfo.setSizeFull();
        captionContactsInfo.setContentMode(ContentMode.HTML);
        captionContactsInfo.setValue(myUI.getMessage(SptMessages.ContactInfo));
        captionContactsInfo.setStyleName("tableCpt");

        plusPhonesButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusPhonesButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusPhonesButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusPhonesButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusPhonesButton.addClickListener(this);

        hl.addComponent(captionPhones);
        hl.setSpacing(true);
        hl.addComponent(plusPhonesButton);
        hl.setExpandRatio(captionPhones, 1);

        phonesTable = new FormattedTable();
        phonesTable.setSizeFull();
        phonesTable.setStyleName(ValoTheme.TABLE_SMALL);
        contactInfoLay = new GridLayout(2, 2);
        contactInfoLay.setSizeFull();
        contactInfoLay.setSpacing(true);
        contactInfoLay.setMargin(true);
        contactInfoLay.addComponent(captionContactsInfo, 0, 0);
        contactInfoLay.addComponent(hl, 1, 0);
        contactInfoLay.addComponent(fieldsLayContacts, 0, 1);
        contactInfoLay.addComponent(phonesTable, 1, 1);
        contactInfoLay.setRowExpandRatio(1, 1);
    }

    private void buildFamilyLayout() {

        familyInfoLay = new GridLayout(2, 8);
        familyInfoLay.setWidth("100%");
        familyInfoLay.setSpacing(true);
        familyInfoLay.setMargin(true);
        familyInfoLay.setColumnExpandRatio(0, 1);

        captionSpouseInfo = new Label();
        captionSpouseInfo.setWidth("100%");
        captionSpouseInfo.setContentMode(ContentMode.HTML);
        captionSpouseInfo.setValue(myUI.getMessage(SptMessages.SpouseInfo));
        captionSpouseInfo.setStyleName("tableCpt");
        familyInfoLay.addComponent(captionSpouseInfo, 0, 0, 1, 0);

        fieldsLayFamily = new FormLayout();
        fieldsLayFamily.setWidth("100%");
        fieldsLayFamily.setSpacing(false);
        fieldsLayFamily.setMargin(false);
        familyInfoLay.addComponent(fieldsLayFamily, 0, 1, 1, 1);

        spouseFullnameTF = createTextfield(null, null, new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true);
        spouseFullnameTF.setCaption(myUI.getMessage(SptMessages.FullName));
        spouseFullnameTF.setWidth("50%");
        fieldsLayFamily.addComponent(spouseFullnameTF);

        spousePhoneTF = createTextfield(null, null, new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), null, 100, true), false);
        spousePhoneTF.setCaption(myUI.getMessage(SptMessages.Phone));
        spousePhoneTF.setWidth("50%");
        fieldsLayFamily.addComponent(spousePhoneTF);

        spouseHealthCB = createCombobox(0, null, SystemSettings.dbHealthStatus, true);
        spouseHealthCB.setCaption(myUI.getMessage(SptMessages.HealthStatus));
        spouseHealthCB.setWidth("50%");
        fieldsLayFamily.addComponent(spouseHealthCB);

        spouseHealthNotesTF = new TextField(myUI.getMessage(SptMessages.HealthNotes));
        spouseHealthNotesTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        spouseHealthNotesTF.setWidth("50%");
        spouseHealthNotesTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), null, 350, true));
        fieldsLayFamily.addComponent(spouseHealthNotesTF);

        Label captionSpouseEducation = new Label();
        captionSpouseEducation.setSizeFull();
        captionSpouseEducation.setContentMode(ContentMode.HTML);
        captionSpouseEducation.setValue(myUI.getMessage(SptMessages.SpouseEducation));
        captionSpouseEducation.setStyleName("tableCpt");
        familyInfoLay.addComponent(captionSpouseEducation, 0, 2);

        plusSpouseEducationButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusSpouseEducationButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusSpouseEducationButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusSpouseEducationButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusSpouseEducationButton.addClickListener(this);
        familyInfoLay.addComponent(plusSpouseEducationButton, 1, 2);

        spouseEducationTable = new FormattedTable();
        spouseEducationTable.setSizeFull();
        spouseEducationTable.setStyleName(ValoTheme.TABLE_SMALL);
        spouseEducationTable.setPageLength(8);
        familyInfoLay.addComponent(spouseEducationTable, 0, 3, 1, 3);

        Label captionSpouseWorkPlaces = new Label();
        captionSpouseWorkPlaces.setSizeFull();
        captionSpouseWorkPlaces.setContentMode(ContentMode.HTML);
        captionSpouseWorkPlaces.setValue(myUI.getMessage(SptMessages.SpouseWorkPlaces));
        captionSpouseWorkPlaces.setStyleName("tableCpt");
        familyInfoLay.addComponent(captionSpouseWorkPlaces, 0, 4);

        plusSpouseWorkPlacesButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusSpouseWorkPlacesButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusSpouseWorkPlacesButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusSpouseWorkPlacesButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusSpouseWorkPlacesButton.addClickListener(this);
        familyInfoLay.addComponent(plusSpouseWorkPlacesButton, 1, 4);

        spouseWorkPlacesTable = new FormattedTable();
        spouseWorkPlacesTable.setSizeFull();
        spouseWorkPlacesTable.setStyleName(ValoTheme.TABLE_SMALL);
        spouseWorkPlacesTable.setPageLength(8);
        familyInfoLay.addComponent(spouseWorkPlacesTable, 0, 5, 1, 5);


        Label captionChildren = new Label();
        captionChildren.setSizeFull();
        captionChildren.setContentMode(ContentMode.HTML);
        captionChildren.setValue(myUI.getMessage(SptMessages.Children));
        captionChildren.setStyleName("tableCpt");
        familyInfoLay.addComponent(captionChildren, 0, 6);

        plusChildButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusChildButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusChildButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusChildButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusChildButton.addClickListener(this);
        familyInfoLay.addComponent(plusChildButton, 1, 6);

        childrenTable = new FormattedTable();
        childrenTable.setSizeFull();
        childrenTable.setStyleName(ValoTheme.TABLE_SMALL);
        childrenTable.setPageLength(8);
        familyInfoLay.addComponent(childrenTable, 0, 7, 1, 7);
            }


    private void buildDocumentsLayout() {

        Label captionDocuments = new Label();
        captionDocuments.setWidth("100%");
        captionDocuments.setContentMode(ContentMode.HTML);
        captionDocuments.setValue(myUI.getMessage(SptMessages.ListOfDocuments));
        captionDocuments.setStyleName("tableCpt");

        documentsDataTable = new Table();
        documentsDataTable.setSizeFull();
        documentsDataTable.setStyleName(ValoTheme.TABLE_SMALL);

        documentsLay = new VerticalLayout();
        documentsLay.setSizeFull();
        documentsLay.setSpacing(true);
        documentsLay.setMargin(true);
        documentsLay.addComponent(captionDocuments);
        documentsLay.addComponent(documentsDataTable);
        documentsLay.setExpandRatio(documentsDataTable, 1);
    }

    private void buildExtraLayout() {

        fieldsLayExtra = new FormLayout();
        fieldsLayExtra.setSizeFull();
        fieldsLayExtra.setSpacing(false);
        fieldsLayExtra.setMargin(false);

        healthCB = createCombobox(0, null, SystemSettings.dbHealthStatus, true);
        healthCB.setCaption(myUI.getMessage(SptMessages.HealthStatus));
        fieldsLayExtra.addComponent(healthCB);

        healthNotesTA = new TextArea(myUI.getMessage(SptMessages.HealthNotes));
        healthNotesTA.setStyleName(ValoTheme.TEXTFIELD_TINY);
        healthNotesTA.setWidth("100%");
        healthNotesTA.setRows(5);
        healthNotesTA.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), null, 350, true));
        fieldsLayExtra.addComponent(healthNotesTA);

        hobbiesTF = createTextfield(null, null, new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), null, 300, true), false);
        hobbiesTF.setCaption(myUI.getMessage(SptMessages.Hobbies));
        fieldsLayExtra.addComponent(hobbiesTF);

        fobbiesTF = createTextfield(null, null, new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), null, 300, true), false);
        fobbiesTF.setCaption(myUI.getMessage(SptMessages.Fobbies));
        fieldsLayExtra.addComponent(fobbiesTF);

        shortNotesTA = new TextArea(myUI.getMessage(SptMessages.ShortNote));
        shortNotesTA.setStyleName(ValoTheme.TEXTFIELD_TINY);
        shortNotesTA.setWidth("100%");
        shortNotesTA.setRows(3);
        shortNotesTA.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), null, 300, true));
        fieldsLayExtra.addComponent(shortNotesTA);

        Label captionQuestioning = new Label();
        captionQuestioning.setWidth("100%");
        captionQuestioning.setContentMode(ContentMode.HTML);
        captionQuestioning.setValue(myUI.getMessage(SptMessages.Questioning));
        captionQuestioning.setStyleName("tableCpt");

        questioningTable = new FormattedTable();
        questioningTable.setSizeFull();
        questioningTable.setStyleName(ValoTheme.TABLE_SMALL);

        Label captionExtraInfo = new Label();
        captionExtraInfo.setWidth("100%");
        captionExtraInfo.setContentMode(ContentMode.HTML);
        captionExtraInfo.setValue(myUI.getMessage(SptMessages.ExtraInfo));
        captionExtraInfo.setStyleName("tableCpt");

        extraInfoLay = new GridLayout(2, 2);
        extraInfoLay.setSizeFull();
        extraInfoLay.setSpacing(true);
        extraInfoLay.setMargin(true);
        extraInfoLay.addComponent(captionExtraInfo, 0, 0);
        extraInfoLay.addComponent(fieldsLayExtra, 0, 1);
        extraInfoLay.addComponent(captionQuestioning, 1, 0);
        extraInfoLay.addComponent(questioningTable, 1, 1);
        extraInfoLay.setRowExpandRatio(1, 1);
        extraInfoLay.setColumnExpandRatio(0, 1.2f);
        extraInfoLay.setColumnExpandRatio(1, 1.8f);
    }

    private void buildAchievementsLayout() {

        achievementsInfoLay = new GridLayout(2, 8);
        achievementsInfoLay.setWidth("100%");
        achievementsInfoLay.setSpacing(true);
        achievementsInfoLay.setMargin(true);
        achievementsInfoLay.setColumnExpandRatio(0, 1);

        Label captionExams = new Label();
        captionExams.setSizeFull();
        captionExams.setContentMode(ContentMode.HTML);
        captionExams.setValue(myUI.getMessage(SptMessages.Exams));
        captionExams.setStyleName("tableCpt");
        achievementsInfoLay.addComponent(captionExams, 0, 0);

        plusExamButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusExamButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusExamButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusExamButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusExamButton.addClickListener(this);
        achievementsInfoLay.addComponent(plusExamButton, 1, 0);

        examsTable = new FormattedTable();
        examsTable.setSizeFull();
        examsTable.setStyleName(ValoTheme.TABLE_SMALL);
        examsTable.setPageLength(8);
        achievementsInfoLay.addComponent(examsTable, 0, 1, 1, 1);

        Label captionSeminars = new Label();
        captionSeminars.setSizeFull();
        captionSeminars.setContentMode(ContentMode.HTML);
        captionSeminars.setValue(myUI.getMessage(SptMessages.Seminars));
        captionSeminars.setStyleName("tableCpt");
        achievementsInfoLay.addComponent(captionSeminars, 0, 2);

        plusSeminarButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusSeminarButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusSeminarButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusSeminarButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusSeminarButton.addClickListener(this);
        achievementsInfoLay.addComponent(plusSeminarButton, 1, 2);

        seminarsTable = new FormattedTable();
        seminarsTable.setSizeFull();
        seminarsTable.setStyleName(ValoTheme.TABLE_SMALL);
        seminarsTable.setPageLength(8);
        achievementsInfoLay.addComponent(seminarsTable, 0, 3, 1, 3);

        Label captionCertificates = new Label();
        captionCertificates.setSizeFull();
        captionCertificates.setContentMode(ContentMode.HTML);
        captionCertificates.setValue(myUI.getMessage(SptMessages.Certificates));
        captionCertificates.setStyleName("tableCpt");
        achievementsInfoLay.addComponent(captionCertificates, 0, 4);

        plusCertificateButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusCertificateButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusCertificateButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusCertificateButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusCertificateButton.addClickListener(this);
        achievementsInfoLay.addComponent(plusCertificateButton, 1, 4);

        certificatesTable = new FormattedTable();
        certificatesTable.setSizeFull();
        certificatesTable.setStyleName(ValoTheme.TABLE_SMALL);
        certificatesTable.setPageLength(8);
        achievementsInfoLay.addComponent(certificatesTable, 0, 5, 1, 5);

        Label captionLanguages = new Label();
        captionLanguages.setSizeFull();
        captionLanguages.setContentMode(ContentMode.HTML);
        captionLanguages.setValue(myUI.getMessage(SptMessages.Languages));
        captionLanguages.setStyleName("tableCpt");
        achievementsInfoLay.addComponent(captionLanguages, 0, 6);

        plusLanguageButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusLanguageButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusLanguageButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusLanguageButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusLanguageButton.addClickListener(this);
        achievementsInfoLay.addComponent(plusLanguageButton, 1, 6);

        languagesTable = new FormattedTable();
        languagesTable.setSizeFull();
        languagesTable.setStyleName(ValoTheme.TABLE_SMALL);
        languagesTable.setPageLength(8);
        achievementsInfoLay.addComponent(languagesTable, 0, 7, 1, 7);
    }

    private void buildProfLayout() {

        profInfoLay = new GridLayout(3, 8);
        profInfoLay.setSpacing(true);
        profInfoLay.setMargin(true);
        profInfoLay.setWidth("100%");
        profInfoLay.setColumnExpandRatio(0, 1);

        Label captionGradSchool = new Label();
        captionGradSchool.setWidth("100%");
        captionGradSchool.setContentMode(ContentMode.HTML);
        captionGradSchool.setValue(myUI.getMessage(SptMessages.GraduationSchool));
        captionGradSchool.setStyleName("tableCpt");
        profInfoLay.addComponent(captionGradSchool, 0, 0, 2, 0);

        gradSchoolCB = createCombobox(0, null, null, true);
        gradSchoolCB.setCaption(myUI.getMessage(SptMessages.GraduationSchool));
        try {
            DbSchool dbCon = new DbSchool();
            dbCon.connect();
            gradSchoolCB.setContainerDataSource(dbCon.execSchoolSel(myUI, "1,3,4"));
            Item item = gradSchoolCB.getContainerDataSource().addItem(0);
            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(myUI.getMessage(SptMessages.OtherSchool));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        gradSchoolCB.setValue(0);
        profInfoLay.addComponent(gradSchoolCB, 0, 1);

        gradSchoolStartDF = createDateField(null, null,
                myUI.getMessage(SptMessages.Start),
                true, SystemSettings.yearPattern, Resolution.YEAR);
        gradSchoolStartDF.setSizeUndefined();
        gradSchoolStartDF.setResolution(Resolution.YEAR);
        profInfoLay.addComponent(gradSchoolStartDF, 1, 1);

        gradSchoolEndDF = createDateField(null, null,
                myUI.getMessage(SptMessages.End),
                true, SystemSettings.yearPattern, Resolution.YEAR);
        gradSchoolEndDF.setResolution(Resolution.YEAR);
        profInfoLay.addComponent(gradSchoolEndDF, 2, 1);

        Label captionBranches = new Label();
        captionBranches.setSizeFull();
        captionBranches.setContentMode(ContentMode.HTML);
        captionBranches.setValue(myUI.getMessage(SptMessages.Branches));
        captionBranches.setStyleName("tableCpt");
        profInfoLay.addComponent(captionBranches, 0, 2, 1, 2);

        plusBranchButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusBranchButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusBranchButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusBranchButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusBranchButton.addClickListener(this);
        profInfoLay.addComponent(plusBranchButton, 2, 2);

        branchesTable = new FormattedTable();
        branchesTable.setSizeFull();
        branchesTable.setStyleName(ValoTheme.TABLE_SMALL);
        branchesTable.setPageLength(8);
        profInfoLay.addComponent(branchesTable, 0, 3, 2, 3);

        Label captionEducation = new Label();
        captionEducation.setSizeFull();
        captionEducation.setContentMode(ContentMode.HTML);
        captionEducation.setValue(myUI.getMessage(SptMessages.Education));
        captionEducation.setStyleName("tableCpt");
        profInfoLay.addComponent(captionEducation, 0, 4, 1, 4);

        plusEducationButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusEducationButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusEducationButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusEducationButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusEducationButton.addClickListener(this);
        profInfoLay.addComponent(plusEducationButton, 2, 4);

        educationTable = new FormattedTable();
        educationTable.setSizeFull();
        educationTable.setStyleName(ValoTheme.TABLE_SMALL);
        educationTable.setPageLength(8);
        profInfoLay.addComponent(educationTable, 0, 5, 2, 5);

        Label captionWorkPlaces = new Label();
        captionWorkPlaces.setSizeFull();
        captionWorkPlaces.setContentMode(ContentMode.HTML);
        captionWorkPlaces.setValue(myUI.getMessage(SptMessages.WorkPlaces));
        captionWorkPlaces.setStyleName("tableCpt");
        profInfoLay.addComponent(captionWorkPlaces, 0, 6, 1, 6);

        plusWorkPlaceButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusWorkPlaceButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusWorkPlaceButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusWorkPlaceButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusWorkPlaceButton.addClickListener(this);
        profInfoLay.addComponent(plusWorkPlaceButton, 2, 6);

        workPlacesTable = new FormattedTable();
        workPlacesTable.setSizeFull();
        workPlacesTable.setStyleName(ValoTheme.TABLE_SMALL);
        workPlacesTable.setPageLength(8);
        profInfoLay.addComponent(workPlacesTable, 0, 7, 2, 7);
    }

    private void buildOrdersLayout() {

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");

        Label captionOrders = new Label();
        captionOrders.setSizeFull();
        captionOrders.setContentMode(ContentMode.HTML);
        captionOrders.setValue(myUI.getMessage(SptMessages.OrdersHistory));
        captionOrders.setStyleName("tableCpt");

        plusOrdersButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusOrdersButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusOrdersButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusOrdersButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusOrdersButton.addClickListener(this);

        hl.addComponent(captionOrders);
        hl.setSpacing(true);
        hl.addComponent(plusOrdersButton);
        hl.setExpandRatio(captionOrders, 1);

        ordersTable = new FormattedTable();
        ordersTable.setImmediate(true);
        ordersTable.setSizeFull();
        ordersTable.setStyleName(ValoTheme.TABLE_SMALL);

        ordersInfoLay = new GridLayout(1, 2);
        ordersInfoLay.setSizeFull();
        ordersInfoLay.setSpacing(true);
        ordersInfoLay.setMargin(true);
        ordersInfoLay.addComponent(hl, 0, 0);
        ordersInfoLay.addComponent(ordersTable, 0, 1);
        ordersInfoLay.setRowExpandRatio(1, 1);
    }

    private void buildSchoolInfoLayout() {

        canBeAdvisor = new CheckBox(myUI.getMessage(SptMessages.CanBeAdvisor));
        canBeAdvisor.setWidth("100%");

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");

        Label captionLessons = new Label();
        captionLessons.setSizeFull();
        captionLessons.setContentMode(ContentMode.HTML);
        captionLessons.setValue(myUI.getMessage(SptMessages.Lessons));
        captionLessons.setStyleName("tableCpt");

        plusLessonsButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusLessonsButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusLessonsButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusLessonsButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusLessonsButton.addClickListener(this);

        hl.addComponent(captionLessons);
        hl.setSpacing(true);
        hl.addComponent(plusLessonsButton);
        hl.setExpandRatio(captionLessons, 1);

        lessonsTable = new FormattedTable();
        lessonsTable.setSizeFull();
        lessonsTable.setStyleName(ValoTheme.TABLE_SMALL);

        schoolInfoLay = new VerticalLayout();
        schoolInfoLay.setSizeFull();
        schoolInfoLay.setSpacing(true);
        schoolInfoLay.setMargin(true);
        schoolInfoLay.addComponent(canBeAdvisor);
        schoolInfoLay.addComponent(hl);
        schoolInfoLay.addComponent(lessonsTable);
        schoolInfoLay.setExpandRatio(lessonsTable, 1);
    }

    private void buildPermissionsLayout() {

        Label captionPermissions = new Label();
        captionPermissions.setWidth("100%");
        captionPermissions.setHeight("27px");
        captionPermissions.setContentMode(ContentMode.HTML);
        captionPermissions.setValue(myUI.getMessage(SptMessages.Permissions));
        captionPermissions.setStyleName("tableCpt");

        NATURAL_COL_ORDER_PERMISSIONS = new String[]{
                myUI.getMessage(SptMessages.Functions), myUI.getMessage(SptMessages.ClassCaption)};
        permissionTable = new FormattedTable();
        permissionTable.setSizeFull();
        permissionTable.setStyleName(ValoTheme.TABLE_SMALL);
        try {
            DbDefinition dbe = new DbDefinition();
            dbe.connect();
            permissionCont = dbe.execPermissionSQL(myUI);
            dbe.close();
            Iterator iter = permissionCont.getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                ComboBoxMultiselectMax permMCB = new ComboBoxMultiselectMax();
                permMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
                permMCB.setWidth("100%");
                permMCB.setShowSelectAllButton(new ComboBoxMultiselectMax.ShowButton() {
                    @Override
                    public boolean isShow(String filter, int page) {
                        return true;
                    }
                });
                permMCB.addItems(convertStrToSet(permissionCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Value)).getValue().toString()));
                permissionCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Functions)).setValue(permMCB);
            }
            permissionTable.setContainerDataSource(permissionCont);
            permissionTable.setVisibleColumns(NATURAL_COL_ORDER_PERMISSIONS);
            permissionTable.setColumnWidth(myUI.getMessage(SptMessages.ClassCaption), 235);
            permissionTable.setPageLength(0);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }

        permissionsLay = new VerticalLayout();
        permissionsLay.setSizeFull();
        permissionsLay.setSpacing(true);
        permissionsLay.setMargin(true);
        permissionsLay.addComponent(captionPermissions);
        permissionsLay.addComponent(permissionTable);
        permissionsLay.setExpandRatio(permissionTable, 1);
    }

    private Set<?> convertStrToSet(String str) {
        String[] strArr = str.split(",");
        HashSet<String> hs = new HashSet<String>(strArr.length);
        for (int i = 0; i < strArr.length; i++) {
            hs.add(strArr[i]);
        }
        return hs;
    }

    private void setPermTable_options(String oneStringAll) {
        try {
            String[] dividedStringAll = oneStringAll.split(";");
            int z = permissionCont.size();
            for (int i = 0; i < z; i++) {
                if (i < dividedStringAll.length && dividedStringAll[i] != null) {
                    String[] byClassName = dividedStringAll[i].split(":");
                    Item itm = permissionCont.getItem(byClassName[0]);
                    if (itm != null) {
                        ((ComboBoxMultiselectMax) itm.getItemProperty(
                                myUI.getMessage(SptMessages.Functions)).getValue())
                                .setValue(convertStrToSet(byClassName[1]));
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setPhonesTable() {
        try {
            NATURAL_COL_ORDER_PHONES = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.Type),
                    myUI.getMessage(SptMessages.Number)};
            DbEmployeePhoneNumber dbepn = new DbEmployeePhoneNumber();
            dbepn.connect();
            phonesTable.setContainerDataSource(
                    dbepn.execSQL(myUI, emplID, this));
            dbepn.close();
            phonesTable.setVisibleColumns(NATURAL_COL_ORDER_PHONES);
            phonesTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Number), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setChildrenTable() {
        try {
            NATURAL_COL_ORDER_CHILDREN = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.FullName),
                    myUI.getMessage(SptMessages.DateOfBirth),
                    myUI.getMessage(SptMessages.Institution),
                    myUI.getMessage(SptMessages.EducationStatus),
                    myUI.getMessage(SptMessages.HealthStatus)};
            DbEmployeeChildren dbech = new DbEmployeeChildren();
            dbech.connect();
            childrenTable.setContainerDataSource(dbech.execSQL(myUI, emplID, this));
            dbech.close();
            childrenTable.setVisibleColumns(NATURAL_COL_ORDER_CHILDREN);
            childrenTable.setColumnExpandRatio(myUI.getMessage(SptMessages.FullName), 1);
            childrenTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Institution), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setEducationTable(Table t, IndexedContainer c, int own_id) {
        try {
            if (own_id == 1) {
                NATURAL_COL_ORDER_EDU = new String[]{SystemSettings.button,
                        myUI.getMessage(SptMessages.University),
                        myUI.getMessage(SptMessages.Country),
                        myUI.getMessage(SptMessages.Department),
                        myUI.getMessage(SptMessages.EduLevel),
                        myUI.getMessage(SptMessages.Start),
                        myUI.getMessage(SptMessages.End),
                        myUI.getMessage(SptMessages.Document)};
            } else {
                NATURAL_COL_ORDER_EDU = new String[]{SystemSettings.button,
                        myUI.getMessage(SptMessages.University),
                        myUI.getMessage(SptMessages.Country),
                        myUI.getMessage(SptMessages.Department),
                        myUI.getMessage(SptMessages.EduLevel),
                        myUI.getMessage(SptMessages.Start),
                        myUI.getMessage(SptMessages.End)};

            }
            DbEmployeeEducation dbed = new DbEmployeeEducation();
            dbed.connect();
            t.setContainerDataSource(dbed.execSQL(myUI, emplID, own_id, c, this));
            dbed.close();
            t.setVisibleColumns(NATURAL_COL_ORDER_EDU);
            t.setColumnExpandRatio(myUI.getMessage(SptMessages.University), 1);
            t.setColumnExpandRatio(myUI.getMessage(SptMessages.Department), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setWorkTable(Table t, IndexedContainer c, int own_id) {
        try {
            NATURAL_COL_ORDER_WORK = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.WorkPlace),
                    myUI.getMessage(SptMessages.Sapat),
                    myUI.getMessage(SptMessages.MainPosition),
                    myUI.getMessage(SptMessages.ExtraPositions),
                    myUI.getMessage(SptMessages.WorkingStatus),
                    myUI.getMessage(SptMessages.Start),
                    myUI.getMessage(SptMessages.End)};
            DbEmployeeWork dbew = new DbEmployeeWork();
            dbew.connect();
            t.setContainerDataSource(dbew.execSQL(myUI, emplID, own_id, c, this));
            dbew.close();
            t.setVisibleColumns(NATURAL_COL_ORDER_WORK);
            t.setColumnExpandRatio(myUI.getMessage(SptMessages.WorkPlace), 1);
            t.setColumnExpandRatio(myUI.getMessage(SptMessages.MainPosition), 1);
            t.setColumnExpandRatio(myUI.getMessage(SptMessages.ExtraPositions), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setQuestioningTable() {
        try {
            NATURAL_COL_ORDER_QUESTIONING = new String[]{
                    myUI.getMessage(SptMessages.Question),
                    myUI.getMessage(SptMessages.Answer)};
            DbEmployeeQuestion dbq = new DbEmployeeQuestion();
            dbq.connect();
            questioningTable.setContainerDataSource(dbq.execSQL(myUI, emplID, this));
            dbq.close();
            questioningTable.setVisibleColumns(NATURAL_COL_ORDER_QUESTIONING);
            questioningTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Question), 1);
            questioningTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Answer), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setLanguagesTable() {
        try {
            NATURAL_COL_ORDER_LANGUAGES = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.Language),
                    myUI.getMessage(SptMessages.Level)};
            DbEmployeeLanguage dbel = new DbEmployeeLanguage();
            dbel.connect();
            languagesTable.setContainerDataSource(
                    dbel.execSQL(myUI, emplID, this));
            dbel.close();
            languagesTable.setVisibleColumns(NATURAL_COL_ORDER_LANGUAGES);
            languagesTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Language), 1);
            languagesTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Level), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setCertificatesTable() {
        try {
            NATURAL_COL_ORDER_CERTIFICATES = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.Certificate),
                    myUI.getMessage(SptMessages.GivenBy),
                    myUI.getMessage(SptMessages.IssueDate),
                    myUI.getMessage(SptMessages.Note),
                    myUI.getMessage(SptMessages.Document)};
            DbEmployeeCertificate dbec = new DbEmployeeCertificate();
            dbec.connect();
            certificatesTable.setContainerDataSource(
                    dbec.execSQL(myUI, emplID, this));
            dbec.close();
            certificatesTable.setVisibleColumns(NATURAL_COL_ORDER_CERTIFICATES);
            certificatesTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Title), 1);
            certificatesTable.setColumnExpandRatio(myUI.getMessage(SptMessages.GivenBy), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setSeminarsTable() {
        try {
            NATURAL_COL_ORDER_SEMINARS = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.Title),
                    myUI.getMessage(SptMessages.Subject),
                    myUI.getMessage(SptMessages.Note),
                    myUI.getMessage(SptMessages.IssueDate)};
            DbEmployeeSeminar dbes = new DbEmployeeSeminar();
            dbes.connect();
            seminarsTable.setContainerDataSource(
                    dbes.execSQL(myUI, emplID, this));
            dbes.close();
            seminarsTable.setVisibleColumns(NATURAL_COL_ORDER_SEMINARS);
            seminarsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Title), 1);
            seminarsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Subject), 1);
            seminarsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Note), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setExamsTable() {
        try {
            NATURAL_COL_ORDER_EXAMS = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.Exam),
                    myUI.getMessage(SptMessages.Score),
                    myUI.getMessage(SptMessages.IssueDate),
                    myUI.getMessage(SptMessages.Document)};
            DbEmployeeExam dbex = new DbEmployeeExam();
            dbex.connect();
            examsTable.setContainerDataSource(
                    dbex.execSQL(myUI, emplID, this));
            dbex.close();
            examsTable.setVisibleColumns(NATURAL_COL_ORDER_EXAMS);
            examsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Exam), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setBranchesTable() {
        try {
            NATURAL_COL_ORDER_BRANCHES = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.Branch),
                    myUI.getMessage(SptMessages.Main)};
            DbEmployeeBranch dbeb = new DbEmployeeBranch();
            dbeb.connect();
            branchesTable.setContainerDataSource(
                    dbeb.execSQL(myUI, emplID, this));
            dbeb.close();
            branchesTable.setVisibleColumns(NATURAL_COL_ORDER_BRANCHES);
            branchesTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Branch), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setLessonsTable() {
        try {
            NATURAL_COL_ORDER_LESSONS = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.Lesson),
                    myUI.getMessage(SptMessages.ClassName),
                    myUI.getMessage(SptMessages.AcademicYear),
                    myUI.getMessage(SptMessages.Hours),
                    myUI.getMessage(SptMessages.ExtraHours)};
            DbEmployeeLessons dbel = new DbEmployeeLessons();
            dbel.connect();
            lessonsTable.setContainerDataSource(
                    dbel.execSQL(myUI, emplID, myUI.getUser().getSchool_id(), this));
            dbel.close();
            lessonsTable.setVisibleColumns(NATURAL_COL_ORDER_LESSONS);
            lessonsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Lessons), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setOrdersTable() {
        try {
            NATURAL_COL_ORDER_ORDERS = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.OrderType),
                    myUI.getMessage(SptMessages.Details),
                    myUI.getMessage(SptMessages.FromDate),
                    myUI.getMessage(SptMessages.TillDate),
                    myUI.getMessage(SptMessages.Note)};
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            ordersTable.setContainerDataSource(
                    dbeo.execSQL(myUI, emplID, myUI.getUser().getSchool_id(), currentUser.hasRole(SystemSettings.rnAdmin), currentUser.hasRole(SystemSettings.rnHr), this));
            dbeo.close();
            ordersTable.setVisibleColumns(NATURAL_COL_ORDER_ORDERS);
            ordersTable.setColumnExpandRatio(myUI.getMessage(SptMessages.OrderType), 1);
            ordersTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Note), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setDocumentsTable() {
        try {
            DbAttachment dbCon = new DbAttachment();
            dbCon.connect();
            documentsDataTable.setContainerDataSource(dbCon.execSQL(myUI, emplID, this));
            dbCon.close();
            documentsDataTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Title), 1);
            documentsDataTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Details), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setContactFields() {
        try {
            DbEmployeeContact dbec = new DbEmployeeContact();
            dbec.connect();
            EmployeeContact ec = dbec.execSQL(emplID);
            dbec.close();
            if (ec != null) {
                addressTA.setValue(ec.getAddress());
                birth_placeTF.setValue(ec.getBirth_place());
                emailTF.setValue(ec.getEmail());
                citizenshipCB.setValue(ec.getCitizenship_id());
            } else {
                clearContactFields();
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setSpouseFields() {
        try {
            DbEmployeeSpouse dbes = new DbEmployeeSpouse();
            dbes.connect();
            EmployeeSpouse es = dbes.execSQL(emplID);
            dbes.close();
            if (es != null) {
                spouseFullnameTF.setValue(es.getFullname());
                if (es.getPhone() != null) {
                    spousePhoneTF.setValue(es.getPhone());
                }
                if (es.getHealth_notes() != null) {
                    spouseHealthNotesTF.setValue(es.getHealth_notes());
                }
                spouseHealthCB.setValue(es.getHealth_status_id());
            } else {
                spouseHealthCB.setValue(null);
                spouseFullnameTF.setValue("");
                spousePhoneTF.setValue("");
                spouseHealthNotesTF.setValue("");
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setExtraInfoFields() {
        try {
            DbEmployeeExtraInfo dbei = new DbEmployeeExtraInfo();
            dbei.connect();
            EmployeeExtraInfo eei = dbei.execSQL(emplID);
            dbei.close();
            if (eei != null) {
                if (eei.getHobbies() != null) {
                    hobbiesTF.setValue(eei.getHobbies());
                } else {
                    hobbiesTF.setValue("");
                }
                if (eei.getFobbies() != null) {
                    fobbiesTF.setValue(eei.getFobbies());
                } else {
                    fobbiesTF.setValue("");
                }
                if (eei.getHealth_notes() != null) {
                    healthNotesTA.setValue(eei.getHealth_notes());
                } else {
                    healthNotesTA.setValue("");
                }
                if (eei.getShort_notes() != null) {
                    shortNotesTA.setValue(eei.getShort_notes());
                } else {
                    shortNotesTA.setValue("");
                }
                healthCB.setValue(eei.getHealth_status_id());
            } else {
                clearExtraInfoFields();
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setGradSchoolFields() {
        try {
            DbEmployeeGraduationSchool dbCon = new DbEmployeeGraduationSchool();
            dbCon.connect();
            EmployeeGraduationSchool egs = dbCon.execSQL(emplID);
            dbCon.close();
            if (egs != null) {
                gradSchoolCB.setValue(egs.getSchool_id());
                gradSchoolStartDF.setValue(egs.getStart());
                gradSchoolEndDF.setValue(egs.getEnd());
            } else {
                clearGradSchoolFields();
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void prepareModificationMode() {
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        optionGroup.setEnabled(false);
        employeesDataTable.setEnabled(false);
        contactInfoLay.setEnabled(true);
        familyInfoLay.setEnabled(true);
        extraInfoLay.setEnabled(true);
        formLay.setEnabled(true);
        achievementsInfoLay.setEnabled(true);
        enableUploads(certificatesTable);
        enableUploads(examsTable);
        enableUploads(educationTable);
        profInfoLay.setEnabled(true);
        schoolInfoLay.setEnabled(true);
        permissionsLay.setEnabled(true);
        ordersInfoLay.setEnabled(true);
        photoUpl.setEnabled(true);

        Iterator<Component> iterator = tabs.iterator();
        while (iterator.hasNext()) {
            Component component = iterator.next();
            TabSheet.Tab tab = tabs.getTab(component);
            if (tab == tabs.getTab(tabs.getSelectedTab())) {
                tab.setEnabled(true);
            } else {
                tab.setEnabled(false);
            }
        }
    }

    private void prepareNormalMode() {
        if (emplID == myUI.getUser().getId()) {
            modifyBtn.setEnabled(true);
        } else if (currentUser.isPermitted(SystemSettings.cnEmployeeDefinitionView + ":" + SystemSettings.actModify) && emplID != 0
                && (Boolean) employeesDataTable.getContainerProperty(emplID, SystemSettings.is_modifiable).getValue()) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(SystemSettings.cnEmployeeDefinitionView + ":" + SystemSettings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (tabs.getSelectedTab() == tabs.getTab(empSearchLay).getComponent()) {
            if (currentUser.isPermitted(SystemSettings.cnEmployeeDefinitionView + ":" + SystemSettings.actDelete) && emplID != 0
                    && (Boolean) employeesDataTable.getContainerProperty(emplID, SystemSettings.is_modifiable).getValue()) {
                deleteBtn.setEnabled(true);
            }
        } else {
            deleteBtn.setEnabled(false);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        employeesDataTable.setEnabled(true);
        optionGroup.setEnabled(true);
        contactInfoLay.setEnabled(false);
        familyInfoLay.setEnabled(false);
        extraInfoLay.setEnabled(false);
        formLay.setEnabled(false);
        achievementsInfoLay.setEnabled(false);
        schoolInfoLay.setEnabled(false);
        permissionsLay.setEnabled(false);
        profInfoLay.setEnabled(false);
        ordersInfoLay.setEnabled(false);
        photoUpl.setEnabled(false);

        Iterator<Component> iterator = tabs.iterator();
        while (iterator.hasNext()) {
            Component component = iterator.next();
            TabSheet.Tab tab = tabs.getTab(component);
            if (tab.getComponent() == empSearchLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == contactInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == familyInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == extraInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == achievementsInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == profInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == schoolInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == permissionsLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == ordersInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == documentsLay) {
                tab.setEnabled(true);
            }
        }
    }

    private void repaint() {
        String str = "";
        Iterator<Integer> iter = (Iterator<Integer>) workingStatCont.getItemIds().iterator();
        int total = 0;
        while (iter.hasNext()) {
            Integer next = iter.next();
            str += "&emsp;" + workingStatCont.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue() + ": "
                    + workingStatCont.getContainerProperty(next, SystemSettings.count).getValue();
            total += (Integer) workingStatCont.getContainerProperty(next, SystemSettings.count).getValue();
        }
        str += "&emsp;" + myUI.getMessage(SptMessages.Total) + ": " + total;
        workingStatTtlLb.setValue(str);
    }

    private void setEmployeesDataTable(String edu_st_ids) {
        edu_st_ids = edu_st_ids.substring(1, edu_st_ids.length() - 1);
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            int id = 0;
            if (!currentUser.isPermitted(SystemSettings.cnEmployeeDefinitionView + ":" + SystemSettings.prmMenu)) {
                id = emplID;
            }
            employeesDataTable.setContainerDataSource(
                    dbe.execSQL(myUI, myUI.getUser().getSchool_id(), edu_st_ids, workingStatCont, currentUser.hasRole(SystemSettings.rnAdmin),
                            currentUser.hasRole(SystemSettings.rnHr), id));
            dbe.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        employeesDataTable.setVisibleColumns(NATURAL_COL_ORDER);
    }

    private void buildLeftLayout() {
        leftLay = new VerticalLayout();
        leftLay.setSpacing(true);
        leftLay.setMargin(true);
        leftLay.setWidth("100%");

        buildPhotoLayout();
        buildFormLayout();
        buildInfoLayout();

        Label lb = new Label(myUI.getMessage(SptMessages.MainInfo));
        lb.setStyleName(ValoTheme.LABEL_LARGE);
        lb.setSizeUndefined();

        leftLay.addComponent(buttonsLay);
        leftLay.addComponent(lb);
        leftLay.setComponentAlignment(lb, Alignment.MIDDLE_CENTER);
        leftLay.addComponent(infoLay);
        leftLay.addComponent(photoEmb);
        leftLay.addComponent(photoUpl);
        leftLay.addComponent(formLay);
    }

    private void buildInfoLayout() {

        infoLay = new VerticalLayout();

        workingStatusLb = new Label();
        workingStatusLb.setContentMode(ContentMode.HTML);
        workingStatusLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        mainPositionLb = new Label();
        mainPositionLb.setContentMode(ContentMode.HTML);
        mainPositionLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        extraPositionsLb = new Label();
        extraPositionsLb.setContentMode(ContentMode.HTML);
        extraPositionsLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        mainBrancLb = new Label();
        mainBrancLb.setContentMode(ContentMode.HTML);
        mainBrancLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        extraBrancesLb = new Label();
        extraBrancesLb.setContentMode(ContentMode.HTML);
        extraBrancesLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        totalHoursLb = new Label();
        totalHoursLb.setContentMode(ContentMode.HTML);
        totalHoursLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        infoLay.addComponent(workingStatusLb);
        infoLay.setComponentAlignment(workingStatusLb, Alignment.BOTTOM_LEFT);
        infoLay.addComponent(mainPositionLb);
        infoLay.setComponentAlignment(mainPositionLb, Alignment.BOTTOM_LEFT);
        infoLay.addComponent(extraPositionsLb);
        infoLay.setComponentAlignment(extraPositionsLb, Alignment.BOTTOM_LEFT);
        infoLay.addComponent(mainBrancLb);
        infoLay.setComponentAlignment(mainBrancLb, Alignment.BOTTOM_LEFT);
        infoLay.addComponent(extraBrancesLb);
        infoLay.setComponentAlignment(extraBrancesLb, Alignment.BOTTOM_LEFT);
        infoLay.addComponent(totalHoursLb);
        infoLay.setComponentAlignment(totalHoursLb, Alignment.BOTTOM_LEFT);
    }

    private void updateInfoLayout() {
        if (employeesDataTable.getValue() != null) {
            workingStatusLb.setValue(myUI.getMessage(SptMessages.WorkingStatus) + ": "
                    + employeesDataTable.getContainerProperty(emplID, myUI.getMessage(SptMessages.WorkingStatus)).getValue());
            mainPositionLb.setValue(myUI.getMessage(SptMessages.MainPosition) + ": "
                    + employeesDataTable.getContainerProperty(emplID, myUI.getMessage(SptMessages.MainPosition)).getValue());
            if (employeesDataTable.getContainerProperty(emplID, myUI.getMessage(SptMessages.ExtraPosition)).getValue() != null) {
                extraPositionsLb.setValue(myUI.getMessage(SptMessages.ExtraPosition) + ": "
                        + employeesDataTable.getContainerProperty(emplID, myUI.getMessage(SptMessages.ExtraPosition)).getValue());
            } else {
                extraPositionsLb.setValue(myUI.getMessage(SptMessages.ExtraPosition) + ":");
            }
            if (employeesDataTable.getContainerProperty(emplID, myUI.getMessage(SptMessages.MainBranch)).getValue() != null) {
                mainBrancLb.setValue(myUI.getMessage(SptMessages.MainBranch) + ": "
                        + employeesDataTable.getContainerProperty(emplID, myUI.getMessage(SptMessages.MainBranch)).getValue());
            } else {
                mainBrancLb.setValue(myUI.getMessage(SptMessages.MainBranch) + ":");
            }
            if (employeesDataTable.getContainerProperty(emplID, myUI.getMessage(SptMessages.ExtraBranches)).getValue() != null) {
                extraBrancesLb.setValue(myUI.getMessage(SptMessages.ExtraBranches) + ": "
                        + employeesDataTable.getContainerProperty(emplID, myUI.getMessage(SptMessages.ExtraBranches)).getValue());
            } else {
                extraBrancesLb.setValue(myUI.getMessage(SptMessages.ExtraBranches) + ":");
            }
            totalHoursLb.setValue(myUI.getMessage(SptMessages.TotalHours) + myUI.getUser().getCurrent_year().getName() + ": "
                    + employeesDataTable.getContainerProperty(emplID, myUI.getMessage(SptMessages.Hours)).getValue()
                    + ", " + myUI.getMessage(SptMessages.ExtraHours) + ": "
                    + employeesDataTable.getContainerProperty(emplID, myUI.getMessage(SptMessages.ExtraHours)).getValue());
            mainPositionCB.setValue((Integer) employeesDataTable.getContainerProperty(emplID, SystemSettings.position_id).getValue());
            contractCategoryCB.setValue((Integer) employeesDataTable.getContainerProperty(emplID, SystemSettings.salary_category_id).getValue());
        } else {
            workingStatusLb.setValue(myUI.getMessage(SptMessages.WorkingStatus) + ":");
            mainPositionLb.setValue(myUI.getMessage(SptMessages.MainPosition) + ":");
            extraPositionsLb.setValue(myUI.getMessage(SptMessages.ExtraPosition) + ":");
            mainBrancLb.setValue(myUI.getMessage(SptMessages.MainBranch) + ":");
            extraBrancesLb.setValue(myUI.getMessage(SptMessages.ExtraBranches) + ":");
            totalHoursLb.setValue(myUI.getMessage(SptMessages.TotalHours) + myUI.getUser().getCurrent_year().getName() + ":");
            mainPositionCB.setValue(null);
            contractCategoryCB.setValue(((IndexedContainer) contractCategoryCB.getContainerDataSource()).lastItemId());
        }
    }

    private void buildFormLayout() {
        formLay = new FormLayout();
        formLay.setSpacing(false);
        formLay.setMargin(false);

        loginTF = createTextfield(null, null, new RegexpValidator("^[1-9][0-9][0-9][0-9][0-9][0-9]$", true,
                myUI.getMessage(SptMessages.NotifWrongValue)), true);
        loginTF.setCaption(myUI.getMessage(SptMessages.Id));
        loginTF.addValueChangeListener(this);
        formLay.addComponent(loginTF);

        HorizontalLayout passwordLay = new HorizontalLayout();
        passwordLay.setCaption(myUI.getMessage(SptMessages.Password));
        passwordLay.setSpacing(true);
        passwordLay.setWidth("100%");

        passwordTF = createTextfield(null, null, new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), null, 20, true), false);
        passwordTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        passwordLay.addComponent(passwordTF);
        passwordLay.setExpandRatio(passwordTF, 1);

        generateBtn = new Button();
        generateBtn.setDescription(myUI.getMessage(SptMessages.GenerateButton));
        generateBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        generateBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        generateBtn.setIcon(FontAwesome.REFRESH);
        generateBtn.addClickListener(this);
        passwordLay.addComponent(generateBtn);
        passwordLay.setComponentAlignment(generateBtn, Alignment.BOTTOM_RIGHT);
        formLay.addComponent(passwordLay);

        surnameTF = createTextfield(null, null, new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 100, false), true);
        surnameTF.setCaption(myUI.getMessage(SptMessages.Surname));
        formLay.addComponent(surnameTF);

        nameTF = createTextfield(null, null, new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 100, false), true);
        nameTF.setCaption(myUI.getMessage(SptMessages.Firstname));
        formLay.addComponent(nameTF);

        middlenameTF = createTextfield(null, null, new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), null, 100, true), false);
        middlenameTF.setCaption(myUI.getMessage(SptMessages.Middlename));
        formLay.addComponent(middlenameTF);

        birthDateDF = createDateField(new Date(), null, myUI.getMessage(SptMessages.DateOfBirth),
                true, SystemSettings.datePattern, Resolution.DAY);
        formLay.addComponent(birthDateDF);

        genderCB = createCombobox(0, null, SystemSettings.dbGender, true);
        genderCB.setCaption(myUI.getMessage(SptMessages.Gender));
        formLay.addComponent(genderCB);

        nationalityCB = createCombobox(0, null, SystemSettings.dbNationality, true);
        nationalityCB.setCaption(myUI.getMessage(SptMessages.Nationality));
        formLay.addComponent(nationalityCB);

        martialStatusCB = createCombobox(0, null, SystemSettings.dbMartialStatus, true);
        martialStatusCB.setCaption(myUI.getMessage(SptMessages.MartialStatus));
        martialStatusCB.addValueChangeListener(this);
        formLay.addComponent(martialStatusCB);

        mainPositionCB = new ComboBoxMax(myUI.getMessage(SptMessages.MainPosition));
        mainPositionCB.setNullSelectionAllowed(false);
        mainPositionCB.setRequired(true);
        mainPositionCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        mainPositionCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        mainPositionCB.setWidth("100%");
        mainPositionCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        mainPositionCB.setFilteringMode(FilteringMode.CONTAINS);
        formLay.addComponent(mainPositionCB);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            mainPositionCB.setContainerDataSource(
                    dbDef.exec_positions_for_select(myUI, currentUser.hasRole(SystemSettings.rnAdmin), currentUser.hasRole(SystemSettings.rnHr)));
            dbDef.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }

        contractCategoryCB = new ComboBoxMax(myUI.getMessage(SptMessages.SalaryCategory));
        contractCategoryCB.setNullSelectionAllowed(false);
        contractCategoryCB.setRequired(true);
        contractCategoryCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        contractCategoryCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        contractCategoryCB.setWidth("100%");
        contractCategoryCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        contractCategoryCB.setFilteringMode(FilteringMode.CONTAINS);
        if (currentUser.hasRole(SystemSettings.rnAdmin) || currentUser.hasRole(SystemSettings.rnHr)) {
            contractCategoryCB.setEnabled(true);
        } else {
            contractCategoryCB.setEnabled(false);
        }
        if (!currentUser.isPermitted(SystemSettings.cnEmployeeDefinitionView
                + ":" + SystemSettings.prmContractVisible)) {
            contractCategoryCB.setVisible(false);
        }
        formLay.addComponent(contractCategoryCB);

        try {
            DbSalaryCategories dbCon = new DbSalaryCategories();
            dbCon.connect();
            contractCategoryCB.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool_id()));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        contractCategoryCB.setValue(((IndexedContainer) contractCategoryCB.getContainerDataSource()).lastItemId());
    }


    private void buildPhotoLayout() {
        photoEmb = new Embedded();
        photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS_HR + "no_photo.jpg")));
        photoEmb.setImmediate(true);
        photoEmb.setHeight("140px");

        photoUpl = createUpload(myUI.getMessage(SptMessages.Upload), true);
    }

    private void buildButtonsLayout() {

        buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);

        modifyBtn = new Button();
        modifyBtn.setEnabled(false);
        modifyBtn.setDescription(myUI.getMessage(SptMessages.ModifyButton));
        modifyBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        modifyBtn.setIcon(FontAwesome.PENCIL);
        modifyBtn.addClickListener(this);
        buttonsLay.addComponent(modifyBtn);

        createBtn = new Button();
        createBtn.setEnabled(false);
        createBtn.setDescription(myUI.getMessage(SptMessages.CreateButton));
        createBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        createBtn.setIcon(FontAwesome.FILE_O);
        createBtn.addClickListener(this);
        buttonsLay.addComponent(createBtn);

        deleteBtn = new Button();
        deleteBtn.setEnabled(false);
        deleteBtn.setDescription(myUI.getMessage(SptMessages.DeleteButton));
        deleteBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.setIcon(FontAwesome.TRASH_O);
        deleteBtn.addClickListener(this);
        buttonsLay.addComponent(deleteBtn);

        saveBtn = new Button();
        saveBtn.setDescription(myUI.getMessage(SptMessages.SaveButton));
        saveBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);
        buttonsLay.addComponent(saveBtn);

        cancelBtn = new Button();
        cancelBtn.setDescription(myUI.getMessage(SptMessages.CancelButton));
        cancelBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        cancelBtn.setIcon(FontAwesome.BAN);
        cancelBtn.addClickListener(this);
        buttonsLay.addComponent(cancelBtn);

    }

    private boolean validate(ComponentContainer layout, boolean isDocumentsRequired) {
        boolean result = true;
        Iterator<Component> i = layout.iterator();
        while (i.hasNext()) {
            Component c = i.next();
            if (isDocumentsRequired && c instanceof Button) {
                if (((Button) c).getData() == null) {
                    return false;
                }
            } else if (c instanceof AbstractField) {
                try {
                    ((AbstractField) c).validate();
                } catch (Exception ex) {
                    //((AbstractComponent) c).setComponentError(new UserError(e.getMessage()));
                    result = false;
                }
            } else if (c instanceof AbstractComponentContainer) {
                if (!validate((AbstractComponentContainer) c, isDocumentsRequired)) {
                    result = false;
                }
            }
        }
        return result;
    }

    private boolean enableUploads(Table t) {
        if (t.size() > 0) {
            Iterator iter = ((IndexedContainer) t
                    .getContainerDataSource()).getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                (((HorizontalLayout) t.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Document)).getValue()).getComponent(1)).setEnabled(true);
            }
        }
        return true;
    }

    private boolean validateTable(Table t, boolean isEmptyAllowed, boolean isDocumentsRequired) {
        if (t.size() == 0 && !isEmptyAllowed) {
            Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                    Notification.Type.WARNING_MESSAGE);
            return false;
        } else {
            Iterator iter = ((IndexedContainer) t
                    .getContainerDataSource()).getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                Iterator iterProp = (t.getContainerDataSource()).getContainerPropertyIds().iterator();
                while (iterProp.hasNext()) {
                    Object next1 = iterProp.next();
                    Object c = t.getItem(next).getItemProperty(next1).getValue();
                    if (c instanceof AbstractField) {
                        try {
                            ((AbstractField) c).validate();
                        } catch (Exception ex) {
                            //((AbstractComponent) c).setComponentError(new UserError(e.getMessage()));
                            return false;
                        }
                    } else if (c instanceof AbstractComponentContainer) {
                        if (!validate((AbstractComponentContainer) c, isDocumentsRequired)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean validateMainBranches() {
        int counter = 0;
        Iterator iter = ((IndexedContainer) branchesTable
                .getContainerDataSource()).getItemIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            if ((Boolean) ((CheckBox) branchesTable.getItem(next).getItemProperty(
                    myUI.getMessage(SptMessages.Main)).getValue()).getValue() == true) {
                counter++;
            }
            if (counter > 1) {
                return false;
            }
        }
        return true;
    }

    private boolean validateOrdersTable() {
        List<?> list = ((IndexedContainer) ordersTable.getContainerDataSource()).getItemIds();
        ListIterator iter2 = list.listIterator(list.size());
        while (iter2.hasPrevious()) {
            Object next2 = iter2.previous();
            Item lastItem = ordersTable.getItem(next2);
            int hr_order_id = (Integer) ((ComboBox) lastItem.getItemProperty(myUI.getMessage(SptMessages.OrderType)).getValue()).getValue();
            Iterator iter = ((IndexedContainer) ordersTable.getContainerDataSource()).getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (next.equals(next2)) {
                    break;
                }
                if (iter.hasNext()) {
                    if (hr_order_id == 3 || hr_order_id == 1 || hr_order_id == 2 || hr_order_id == 7) {
                        if (hr_order_id != 3 || (Integer) ((ComboBox) ordersCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.OrderType)).getValue()).getValue() == hr_order_id) {
                            if (ordersCont.getContainerProperty(next, myUI.getMessage(SptMessages.Details)).getValue() != null
                                    && lastItem.getItemProperty(myUI.getMessage(SptMessages.Details)).getValue() != null
                                    && (Integer) ((ComboBox) ordersCont.getContainerProperty(next, myUI.getMessage(SptMessages.Details)).getValue()).getValue()
                                    == (Integer) ((ComboBox) lastItem.getItemProperty(myUI.getMessage(SptMessages.Details)).getValue()).getValue()) {
                                if (((DateField) ordersCont.getContainerProperty(next, myUI.getMessage(SptMessages.TillDate)).getValue()).getValue() == null
                                        || ((DateField) lastItem.getItemProperty(myUI.getMessage(SptMessages.FromDate)).getValue()).getValue().before(
                                        ((DateField) ordersCont.getContainerProperty(next, myUI.getMessage(SptMessages.TillDate)).getValue()).getValue())) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public class MyReceiver implements Upload.Receiver {
        boolean isPhoto;

        public MyReceiver(boolean isPhoto) {
            this.isPhoto = isPhoto;
        }

        @Override
        public OutputStream receiveUpload(String filename, String mimetype) {
            fileName = filename;
            mimeType = mimetype;
            FileOutputStream fos; // Output stream to write to
            if (isPhoto) {
                photoName = loginTF.getValue() + ".jpg";
            }
            try {
                if (isPhoto) {
                    myFile = new File(SystemSettings.PATH_TO_UPLOADS_HR + photoName);
                } else {
                    myFile = new File(SystemSettings.PATH_TO_UPLOADS_HR + System.currentTimeMillis() + "_" + filename);
                }

                fos = new FileOutputStream(myFile);
            } catch (Exception ex) {
                // Error while opening the file. Not reported here.
                logger.error(ex);
                logger.catching(ex);
                return null;
            }
            return fos; // Return the output stream to write tou
        }
    }

    private void buildUploadWindow() {
        uploadProgressBar = new ProgressBar();
        uploadProgressBar.setWidth("90%");

        statusWindow = new Window(myUI.getMessage(SptMessages.UploadStatus));
        statusWindow.setResizable(false);
        statusWindow.setDraggable(false);
        statusWindow.setModal(true);
        statusWindow.setWidth("20%");
        statusWindow.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);

        final HorizontalLayout l = new HorizontalLayout();
        l.setSpacing(true);
        l.setWidth("100%");
        l.setMargin(true);
        statusWindow.setContent(l);

        cancelButton = new Button();
        cancelButton.setImmediate(true);
        cancelButton.addClickListener(this);
        cancelButton.setId(SystemSettings.cancel_upload_button);
        cancelButton.setVisible(false);
        cancelButton.setIcon(FontAwesome.CLOSE);
        cancelButton.setStyleName(ValoTheme.BUTTON_TINY);
        cancelButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        l.addComponent(cancelButton);
        l.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);

        uploadProgressBar.setCaption(myUI.getMessage(SptMessages.Progress));
        uploadProgressBar.setVisible(false);
        l.addComponent(uploadProgressBar);
        l.setExpandRatio(uploadProgressBar, 1);
    }

    public Upload createUpload(String caption, boolean isPhoto) {
        Upload upl = new Upload(null, new MyReceiver(isPhoto));
        upl.setImmediate(true);
        if (!isPhoto) {
            upl.setStyleName("with-icon");
        }
        upl.addStyleName(ValoTheme.BUTTON_TINY);
        upl.setButtonCaption(caption);
        upl.addStartedListener(new Upload.StartedListener() {
            @Override
            public void uploadStarted(Upload.StartedEvent event) {
                // This method gets called immediatedly after upload is started

                buildUploadWindow();
                myUI.addWindow(statusWindow);
                statusWindow.setClosable(false);

                uploadProgressBar.setValue(0f);
                uploadProgressBar.setVisible(true);
                UI.getCurrent().setPollInterval(500); // hit server frequantly to get
                cancelButton.setVisible(true);
                cancelButton.addClickListener((Button.ClickListener) clickEvent -> {
                    try {
                        upl.interruptUpload();
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                });
            }
        });

        upl.addProgressListener(new Upload.ProgressListener() {
            @Override
            public void updateProgress(long readBytes, long contentLength) {
                // This method gets called several times during the update
                if ((isPhoto && !mimeType.equals("image/jpeg")) || (!mimeType.equals("image/jpeg") && !mimeType.equals("application/pdf"))) {
                    try {
                        upl.interruptUpload();
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                    if (isPhoto) {
                        photoName = null;
                        Notification.show(myUI.getMessage(SptMessages.OnlyJpg), Notification.Type.WARNING_MESSAGE);
                    } else {
                        fileName = null;
                        Button b = (Button) upl.getData();
                        b.setEnabled(false);
                        b.setData(null);
                        Notification.show(myUI.getMessage(SptMessages.OnlyJpgOrPdf), Notification.Type.WARNING_MESSAGE);
                    }
                } else if (contentLength >= 15000000) {
                    try {
                        upl.interruptUpload();
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                    photoName = null;
                    fileName = null;
                    Button b = (Button) upl.getData();
                    b.setEnabled(false);
                    b.setData(null);
                    Notification.show(myUI.getMessage(SptMessages.Maxsize), Notification.Type.WARNING_MESSAGE);
                } else {
                    uploadProgressBar.setValue(new Float(readBytes / (float) contentLength));
                }
            }
        });

        upl.addSucceededListener(new Upload.SucceededListener() {
            @Override
            public void uploadSucceeded(Upload.SucceededEvent event) {
                // This method gets called when the upload finished successfully
                if (isPhoto) {
                    try {
                        Thumbnails.of(myFile).size(200, 200).toFile(myFile);
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                    photoEmb.setSource(new FileResource(myFile));
                } else {
                    Button b = (Button) upl.getData();
                    Attachment attachment = null;
                    try {
                        attachment = new Attachment();
                        attachment.setUnique_name(myFile.getName());
                        attachment.setExtension(mimeType);
                        attachment.setName(fileName);
                        DbAttachment dbCon = new DbAttachment();
                        dbCon.connect();
                        int id = dbCon.exec_insert(attachment);
                        if (id != 0) {
                            attachment.setId(id);
                        }
                        dbCon.close();
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                    b.setData(attachment);
                }
                Notification.show(myUI.getMessage(SptMessages.UploadedSuccessfully),
                        Notification.Type.TRAY_NOTIFICATION);
            }
        });

        upl.addFailedListener(new Upload.FailedListener() {
            @Override
            public void uploadFailed(Upload.FailedEvent event) {
                if (statusWindow != null) {
                    statusWindow.close();
                }
                Notification.show(myUI.getMessage(SptMessages.UploadFailed), Notification.Type.ERROR_MESSAGE);
                try {
                    myFile.delete();
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }
            }
        });

        upl.addFinishedListener(new Upload.FinishedListener() {
            @Override
            public void uploadFinished(Upload.FinishedEvent event) {
                if (statusWindow != null) {
                    statusWindow.close();
                }
            }
        });
        return upl;
    }

    private void clearEmployeeFields(boolean generateNewLogin) {
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (generateNewLogin) {
                loginTF.setValue(dbd.execSQL_login() + "");
            } else {
                loginTF.setValue("");
            }
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        passwordTF.setValue("");
        nameTF.setValue("");
        surnameTF.setValue("");
        middlenameTF.setValue("");
        genderCB.setValue(null);
        birthDateDF.setValue(null);
        nationalityCB.setValue(null);
        martialStatusCB.setValue(null);
        mainPositionCB.setValue(null);
        contractCategoryCB.setValue(((IndexedContainer) contractCategoryCB.getContainerDataSource()).lastItemId());
        photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS_HR + "no_photo.jpg")));
        photoName = null;
    }

    private void clearContactFields() {
        phonesTable.removeAllItems();
        addressTA.setValue("");
        emailTF.setValue("");
        birth_placeTF.setValue("");
        citizenshipCB.setValue(null);
    }

    private void clearSpouseFields() {
        childrenTable.removeAllItems();
        spouseEducationTable.removeAllItems();
        spouseWorkPlacesTable.removeAllItems();
        spouseHealthCB.setValue(null);
        spouseFullnameTF.setValue("");
        spousePhoneTF.setValue("");
        spouseHealthNotesTF.setValue("");
    }

    private void clearExtraInfoFields() {
        healthCB.setValue(null);
        hobbiesTF.setValue("");
        fobbiesTF.setValue("");
        healthNotesTA.setValue("");
        shortNotesTA.setValue("");
    }

    private void clearGradSchoolFields() {
        gradSchoolStartDF.setValue(null);
        gradSchoolEndDF.setValue(null);
        gradSchoolCB.setValue(null);
    }

    private void clearAchievementsFields() {
        languagesTable.removeAllItems();
        seminarsTable.removeAllItems();
        certificatesTable.removeAllItems();
        examsTable.removeAllItems();
    }

    private void clearProfFields() {
        educationTable.removeAllItems();
        workPlacesTable.removeAllItems();
        branchesTable.removeAllItems();
    }

    private void clearPermissionsTable() {
        if (permissionCont != null) {
            for (int i = 0; i < permissionCont.size(); i++) {
                ((ComboBoxMultiselectMax) permissionCont.getContainerProperty(permissionCont.getIdByIndex(i),
                        myUI.getMessage(SptMessages.Functions)).getValue()).setValue(null);
            }
        }
    }

    private void fillFields() {
        loginTF.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                emplID, myUI.getMessage(SptMessages.Id)).getValue().toString());
        nameTF.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                emplID, myUI.getMessage(SptMessages.Firstname)).getValue().toString());
        surnameTF.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                emplID, myUI.getMessage(SptMessages.Surname)).getValue().toString());
        if (employeesDataTable.getContainerDataSource().getContainerProperty(
                emplID, myUI.getMessage(SptMessages.Middlename)).getValue() != null) {
            middlenameTF.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                    emplID, myUI.getMessage(SptMessages.Middlename)).getValue().toString());
        } else {
            middlenameTF.setValue("");
        }
        genderCB.setValue((Integer) employeesDataTable.getContainerDataSource().getContainerProperty(
                emplID, SystemSettings.gender_id).getValue());
        birthDateDF.setValue((Date) employeesDataTable.getContainerDataSource().getContainerProperty(
                emplID, myUI.getMessage(SptMessages.DateOfBirth)).getValue());
        nationalityCB.setValue((Integer) employeesDataTable.getContainerDataSource().getContainerProperty(
                emplID, SystemSettings.nationality_id).getValue());
        martialStatusCB.setValue((Integer) employeesDataTable.getContainerDataSource().getContainerProperty(
                emplID, SystemSettings.martial_status_id).getValue());
        mainPositionCB.setValue((Integer) employeesDataTable.getContainerDataSource().getContainerProperty(
                emplID, SystemSettings.position_id).getValue());
        contractCategoryCB.setValue((Integer) employeesDataTable.getContainerDataSource().getContainerProperty(
                emplID, SystemSettings.salary_category_id).getValue());
        if (employeesDataTable.getContainerProperty(emplID,
                myUI.getMessage(SptMessages.Photo)).getValue() != null) {
            photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS_HR
                    + employeesDataTable.getContainerProperty(emplID,
                    myUI.getMessage(SptMessages.Photo)).getValue().toString())));
            photoName = employeesDataTable.getContainerProperty(emplID,
                    myUI.getMessage(SptMessages.Photo)).getValue().toString();
        } else {
            photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS_HR + "no_photo.jpg")));
            photoName = null;
        }
        if (tabs.getSelectedTab() == tabs.getTab(contactInfoLay).getComponent()) {
            setPhonesTable();
            setContactFields();
        } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent()) {
            setChildrenTable();
            setEducationTable(spouseEducationTable, spouseEducationCont, 2);
            setWorkTable(spouseWorkPlacesTable, spouseWorkCont, 2);
            setSpouseFields();
        } else if (tabs.getSelectedTab() == tabs.getTab(extraInfoLay).getComponent()) {
            setQuestioningTable();
            setExtraInfoFields();
        } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent()) {
            setLanguagesTable();
            setCertificatesTable();
            setSeminarsTable();
            setExamsTable();
        } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent()) {
            setEducationTable(educationTable, educationCont, 1);
            setWorkTable(workPlacesTable, workPlacesCont, 1);
            setBranchesTable();
            setGradSchoolFields();
        } else if (tabs.getSelectedTab() == tabs.getTab(schoolInfoLay).getComponent()) {
            setLessonsTable();
            canBeAdvisor.setValue((Boolean) employeesDataTable.getContainerProperty(
                    emplID, myUI.getMessage(SptMessages.CanBeAdvisor)).getValue());
        } else if (tabs.getSelectedTab() == tabs.getTab(permissionsLay).getComponent()) {
            if (employeesDataTable != null && employeesDataTable.getContainerDataSource().
                    getContainerProperty(emplID, myUI.getMessage(
                            SptMessages.Permissions)).getValue() != null) {
                setPermTable_options(employeesDataTable.getContainerDataSource().
                        getContainerProperty(emplID, myUI.getMessage(
                                SptMessages.Permissions)).getValue().toString());
            } else {
                clearPermissionsTable();
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(ordersInfoLay).getComponent()) {
            setOrdersTable();
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == createBtn) {
            isNew = true;
            tabs.setSelectedTab(empSearchLay);
            clearEmployeeFields(true);
            clearContactFields();
            clearSpouseFields();
            clearExtraInfoFields();
            clearGradSchoolFields();
            questioningTable.removeAllItems();
            clearAchievementsFields();
            clearProfFields();
            lessonsTable.removeAllItems();
            ordersTable.removeAllItems();
            prepareModificationMode();
            passwordTF.setRequired(true);
            mainPositionCB.setEnabled(true);
            loginTF.focus();
        } else if (source == modifyBtn) {
            if (emplID != 0) {
                isNew = false;
                photoUpl.setEnabled(true);
                prepareModificationMode();
                mainPositionCB.setEnabled(false);
                passwordTF.setRequired(false);
            }
        } else if (source == cancelBtn) {
            clearEmployeeFields(false);
            clearContactFields();
            clearSpouseFields();
            clearExtraInfoFields();
            clearGradSchoolFields();
            questioningTable.removeAllItems();
            clearAchievementsFields();
            clearProfFields();
            lessonsTable.removeAllItems();
            ordersTable.removeAllItems();
            if (emplID != 0) {
                fillFields();
                passwordTF.setRequired(false);
            }
            prepareNormalMode();
            delPhoneIds.clear();
            delChildIds.clear();
            delSpouseEducationIds.clear();
            delSpouseWorkIds.clear();
            delLanguagesIds.clear();
            delSeminarsIds.clear();
            delCertificatesIds.clear();
            delExamsIds.clear();
            delEducationIds.clear();
            delWorkPlacesIds.clear();
            delBranchesIds.clear();
            delLessonIds.clear();
            delOrderIds.clear();
            fileName = null;
        } else if (source == saveBtn) {
            try {
                if (validate(leftLay, false)) {
                    if (tabs.getSelectedTab() == tabs.getTab(contactInfoLay).getComponent()
                            && (!validateTable(phonesTable, true, false) || !validate(contactInfoLay, false))) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent()
                            && (!validateTable(childrenTable, true, false)
                            || !validateTable(spouseEducationTable, true, false)
                            || !validateTable(spouseWorkPlacesTable, true, false)
                            || !validate(familyInfoLay, false))) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(extraInfoLay).getComponent()
                            && (!validateTable(questioningTable, true, false) || !validate(extraInfoLay, false))) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent()
                            && (!validateTable(languagesTable, true, false) || !validateTable(examsTable, true, true)
                            || !validateTable(certificatesTable, true, true) || !validateTable(seminarsTable, true, false))) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent()
                            && !validateMainBranches()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifOnlyOneMain),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent()
                            && (!validateTable(educationTable, true, true)
                            || !validateTable(workPlacesTable, true, false)
                            || !validateTable(branchesTable, true, false)
                            || !validate(profInfoLay, false))) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(schoolInfoLay).getComponent()
                            && !validateTable(lessonsTable, true, false)) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(ordersInfoLay).getComponent()
                            && (!validateTable(ordersTable, true, false) || !validateOrdersTable())) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else {
                        DbEmployee dbe = new DbEmployee();
                        dbe.connect();
                        if (isNew) {
                            Employee empl = getEmployee(0);
                            int id = dbe.exec_insert(empl);
                            if (id != 0) {
                                EmployeeOrder eo = new EmployeeOrder();
                                eo.setOrder_id(1);
                                eo.setEmployee_id(id);
                                eo.setSchool_id(myUI.getUser().getSchool_id());
                                eo.setPosition_id((Integer) mainPositionCB.getValue());
                                eo.setM_employee_id(myUI.getUser().getId());
                                eo.setFrom_date(new Date());
                                eo.setCan_not_delete(1);
                                insertEmplOrder(eo);
                                String roleName = loginTF.getValue();
                                List<String> extra_position_ids = null;
                                if (employeesDataTable.getContainerProperty(emplID, SystemSettings.extra_position_ids) != null
                                        && employeesDataTable.getContainerProperty(emplID, SystemSettings.extra_position_ids).getValue() != null) {
                                    extra_position_ids = Arrays.asList(employeesDataTable.getContainerProperty(emplID,
                                            SystemSettings.extra_position_ids).getValue().toString().split(", "));
                                }
                                if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() == 5
                                        || (extra_position_ids != null && extra_position_ids.contains("5"))) {
                                    roleName = SystemSettings.rnAdmin;
                                } else if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() == 25
                                        || (extra_position_ids != null && extra_position_ids.contains("25"))) {
                                    roleName = SystemSettings.rnHr;
                                } else if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() == 115
                                        || (extra_position_ids != null && extra_position_ids.contains("115"))) {
                                    roleName = SystemSettings.rnSapatSecretary;
                                }
                                insertloginRoleName(loginTF.getValue(), roleName);
                                if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() != 5
                                        && (extra_position_ids == null || !extra_position_ids.contains("5"))
                                        && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() != 25
                                        && (extra_position_ids == null || !extra_position_ids.contains("25"))
                                        && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() != 115
                                        && (extra_position_ids == null || !extra_position_ids.contains("115"))
                                        && mainPositionCB.getContainerProperty(mainPositionCB.getValue(),
                                        myUI.getMessage(SptMessages.Permissions)).getValue() != null) {
                                    insertPermissions(loginTF.getValue(),
                                            mainPositionCB.getContainerProperty(mainPositionCB.getValue(),
                                                    myUI.getMessage(SptMessages.Permissions)).getValue().toString());
                                }
                                DbAccCategory dba = new DbAccCategory();
                                dba.connect();
                                AccCategory ac = new AccCategory();
                                ac.setEmployee_id(id);
                                ac.setName(empl.getSurname() + " " + empl.getName());
                                ac.setStatus_id(2);
                                ac.setType_id(2);
                                ac.setSchool_id(myUI.getUser().getSchool_id());
                                ac.setCode(empl.getLogin());
                                ac.setParent_code(contractCategoryCB.getContainerProperty(contractCategoryCB.getValue(),
                                        myUI.getMessage(SptMessages.Code)).getValue().toString());
                                ac.setParent_id((Integer) contractCategoryCB.getValue());
                                int acc_id = dba.exec_insert(ac);
                                dba.close();
                                addDatacontainerItem(id, acc_id);
                                Notification.show(myUI.getMessage(SptMessages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                                prepareNormalMode();
                                passwordTF.setValue("");
                                workingStatCont.getContainerProperty(2, SystemSettings.count)
                                        .setValue(((Integer) workingStatCont.getContainerProperty(2, SystemSettings.count).getValue()) + 1);
                                repaint();
                            } else {
                                Notification.show(myUI.getMessage(SptMessages.CanNotSaveIdNumber), Notification.Type.WARNING_MESSAGE);
                                prepareModificationMode();
                            }
                        } else {
                            String oldLogin = employeesDataTable.getContainerDataSource()
                                    .getContainerProperty(emplID, myUI.getMessage(SptMessages.Id)).getValue().toString();
                            int status = 0;
                            Employee e = getEmployee((Integer) employeesDataTable.getContainerProperty(emplID, SystemSettings.id).getValue());
                            try {
                                status = dbe.exec_update(e, checkPassword());
                            } catch (Exception ex) {
                                logger.error(ex);
                                ex.printStackTrace();
                            }
                            DbAccCategory dba = new DbAccCategory();
                            dba.connect();
                            AccCategory ac = new AccCategory();
                            ac.setId((Integer) employeesDataTable.getContainerProperty(emplID, SystemSettings.acc_category_id).getValue());
                            ac.setName(e.getSurname() + " " + e.getName());
                            ac.setStatus_id(2);
                            ac.setCode(e.getLogin());
                            ac.setParent_code(contractCategoryCB.getContainerProperty(contractCategoryCB.getValue(),
                                    myUI.getMessage(SptMessages.Code)).getValue().toString());
                            ac.setParent_id((Integer) contractCategoryCB.getValue());
                            dba.exec_update(ac);
                            dba.close();
                            if (status != 0) {
                                updateDatacontainer();
                                if (tabs.getSelectedTab() == tabs.getTab(contactInfoLay).getComponent()) {
                                    insertPhones(emplID);
                                    insertEmplContact(getEmployeeContact((Integer) emplID));
                                    setPhonesTable();
                                    setContactFields();
                                } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent()) {
                                    insertChildren(emplID);
                                    insertEducation(emplID, 2, spouseEducationTable, delSpouseEducationIds);
                                    insertWorkPlaces(emplID, 2, spouseWorkPlacesTable, delSpouseWorkIds);
                                    insertEmplSpouse(getEmployeeSpouse(emplID));
                                    setChildrenTable();
                                    setEducationTable(spouseEducationTable, spouseEducationCont, 2);
                                    setWorkTable(spouseWorkPlacesTable, spouseWorkCont, 2);
                                    setSpouseFields();
                                } else if (tabs.getSelectedTab() == tabs.getTab(extraInfoLay).getComponent()) {
                                    insertQuestioning(emplID);
                                    insertEmplExtraInfo(getEmployeeExtraInfo(emplID));
                                    setQuestioningTable();
                                    setExtraInfoFields();
                                } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent()) {
                                    insertLanguages(emplID);
                                    insertCertificates(emplID);
                                    insertSeminars(emplID);
                                    insertExams(emplID);
                                    setLanguagesTable();
                                    setCertificatesTable();
                                    setSeminarsTable();
                                    setExamsTable();
                                    fileName = null;
                                } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent()) {
                                    insertEducation(emplID, 1, educationTable, delEducationIds);
                                    insertWorkPlaces(emplID, 1, workPlacesTable, delWorkPlacesIds);
                                    insertBranches(emplID);
                                    insertEmplGradSchool(getEmployeeGradSchool(emplID));
                                    setEducationTable(educationTable, educationCont, 1);
                                    setWorkTable(workPlacesTable, workPlacesCont, 1);
                                    setBranchesTable();
                                    setGradSchoolFields();
                                    updateInfoLayout();
                                    fileName = null;
                                } else if (tabs.getSelectedTab() == tabs.getTab(schoolInfoLay).getComponent()) {
                                    dbe.exec_update(emplID, canBeAdvisor.getValue());
                                    employeesDataTable.getContainerProperty(emplID,
                                            myUI.getMessage(SptMessages.CanBeAdvisor)).setValue(
                                            canBeAdvisor.getValue());
                                    insertLessons(emplID);
                                    updateInfoLayout();
                                    setLessonsTable();
                                } else if (tabs.getSelectedTab() == tabs.getTab(permissionsLay).getComponent()) {
                                    dbe.exec_delete_perm(oldLogin);
                                    List<String> extra_position_ids = null;
                                    if (employeesDataTable.getContainerProperty(emplID, SystemSettings.extra_position_ids).getValue() != null) {
                                        extra_position_ids = Arrays.asList(employeesDataTable.getContainerProperty(emplID,
                                                SystemSettings.extra_position_ids).getValue().toString().split(", "));
                                    }
                                    if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() != 5
                                            && (extra_position_ids == null || !extra_position_ids.contains("5"))
                                            && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() != 25
                                            && (extra_position_ids == null || !extra_position_ids.contains("25"))
                                            && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() != 115
                                            && (extra_position_ids == null || !extra_position_ids.contains("115"))) {
                                        insertPermissions(loginTF.getValue());
                                    }
                                    if (employeesDataTable != null && employeesDataTable.getContainerDataSource().
                                            getContainerProperty(emplID, myUI.getMessage(
                                                    SptMessages.Permissions)).getValue() != null) {
                                        setPermTable_options(employeesDataTable.getContainerDataSource().
                                                getContainerProperty(emplID, myUI.getMessage(
                                                        SptMessages.Permissions)).getValue().toString());
                                    } else {
                                        clearPermissionsTable();
                                    }
                                } else if (tabs.getSelectedTab() == tabs.getTab(ordersInfoLay).getComponent()) {
                                    insertOrders(emplID);
                                    setOrdersTable();
                                    updateInfoLayout();
                                }
                                String roleName = loginTF.getValue();
                                List<String> extra_position_ids = null;
                                if (employeesDataTable.getContainerProperty(emplID, SystemSettings.extra_position_ids).getValue() != null) {
                                    extra_position_ids = Arrays.asList(employeesDataTable.getContainerProperty(emplID,
                                            SystemSettings.extra_position_ids).getValue().toString().split(", "));
                                }
                                if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() == 5
                                        || (extra_position_ids != null && extra_position_ids.contains("5"))) {
                                    roleName = SystemSettings.rnAdmin;
                                    dbe.exec_delete_perm(oldLogin);
                                } else if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() == 25
                                        || (extra_position_ids != null && extra_position_ids.contains("25"))) {
                                    roleName = SystemSettings.rnHr;
                                    dbe.exec_delete_perm(oldLogin);
                                } else if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() == 115
                                        || (extra_position_ids != null && extra_position_ids.contains("115"))) {
                                    roleName = SystemSettings.rnSapatSecretary;
                                    dbe.exec_delete_perm(oldLogin);
                                }
                                dbe.exec_update_role(oldLogin, loginTF.getValue(), roleName);
                                if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() != 5
                                        && (extra_position_ids == null || !extra_position_ids.contains("5"))
                                        && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() != 25
                                        && (extra_position_ids == null || !extra_position_ids.contains("25"))
                                        && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), SystemSettings.position_id).getValue() != 115
                                        && (extra_position_ids == null || !extra_position_ids.contains("115"))
                                        && !oldLogin.equals(loginTF.getValue())) {
                                    dbe.exec_update_perm(oldLogin, loginTF.getValue());
                                }
                                passwordTF.setValue("");
                                prepareNormalMode();
                                try {
                                    DbDefinition dbCon = new DbDefinition();
                                    dbCon.connect();
                                    dbCon.exec_delete_not_referenced(SystemSettings.dbAttachmentsTable);
                                    dbCon.close();
                                } catch (Exception ex) {
                                    logger.error(ex);
                                    logger.catching(ex);
                                }
                                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                        Notification.Type.HUMANIZED_MESSAGE);
                            } else {
                                Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                        Notification.Type.WARNING_MESSAGE);
                            }

                        }
                        dbe.close();
                    }
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                logger.error(ex);
                logger.catching(ex);
            }
        } else if (source == deleteBtn) {
            if (emplID != 0) {
                ConfirmDialog.show(myUI, myUI.getMessage(SptMessages.Question),
                        myUI.getMessage(SptMessages.ConfirmStudentDeletion)
                                + " " + employeesDataTable.getContainerProperty(emplID,
                                myUI.getMessage(SptMessages.Firstname)).getValue().toString()
                                + " " + employeesDataTable.getContainerProperty(emplID,
                                myUI.getMessage(SptMessages.Surname)).getValue().toString()
                                + "?",
                        myUI.getMessage(SptMessages.Yes),
                        myUI.getMessage(SptMessages.No),
                        new ConfirmDialog.Listener() {
                            @Override
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    execDelete();
                                }
                            }
                        });
            }
        } else if (source == generateBtn) {
            GenerateRandomString gns = new GenerateRandomString();
            String newPass = gns.getAlphaNumeric(8);
            passwordTF.setValue(newPass);
        } else if (source == plusPhonesButton) {
            addPhonesItem();
        } else if (source == plusChildButton) {
            addChildItem();
        } else if (source == plusSpouseEducationButton) {
            addEducationItem(spouseEducationTable, spouseEducationCont, 2);
        } else if (source == plusSpouseWorkPlacesButton) {
            addWorkItem(spouseWorkPlacesTable, spouseWorkCont);
        } else if (source == plusEducationButton) {
            addEducationItem(educationTable, educationCont, 1);
        } else if (source == plusWorkPlaceButton) {
            addWorkItem(workPlacesTable, workPlacesCont);
        } else if (source == plusLanguageButton) {
            addLanguageItem();
        } else if (source == plusCertificateButton) {
            addCertificateItem();
        } else if (source == plusSeminarButton) {
            addSeminarItem();
        } else if (source == plusExamButton) {
            addExamItem();
        } else if (source == plusBranchButton) {
            addBranchItem();
        } else if (source == plusLessonsButton) {
            addLessonItem();
        } else if (source == plusOrdersButton) {
            Object last_id = ((IndexedContainer) ordersTable.getContainerDataSource()).lastItemId();
            if (!((IndexedContainer) ordersTable.getContainerDataSource()).getItem(last_id).getItemProperty(
                    SystemSettings.crud_status).getValue().equals(myUI.getMessage(SptMessages.Insert))
                    && (Integer) ((ComboBox) ((IndexedContainer) ordersTable.getContainerDataSource()).getItem(last_id).getItemProperty(
                    myUI.getMessage(SptMessages.OrderType)).getValue()).getValue() != 5) {
                addOrderItem();
            } else {
                Notification.show(myUI.getMessage(SptMessages.CannotInsertOrder),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(contactInfoLay).getComponent()) {
            delPhoneIds.add(source.getData().toString());
            phonesTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeChildren)) {
            delChildIds.add(source.getData().toString());
            childrenTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeEducation)) {
            EmployeeEducation employeeEducation = new EmployeeEducation();
            employeeEducation.setIdStr(source.getData().toString());
            delEducationIds.add(employeeEducation);
            delSpouseEducationIds.add(employeeEducation);
            spouseEducationTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeWork)) {
            delSpouseWorkIds.add(source.getData().toString());
            spouseWorkPlacesTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeExams)) {
            EmployeeExam employeeExam = new EmployeeExam();
            employeeExam.setIdStr(source.getData().toString());
            Button b = (Button) ((HorizontalLayout) examsTable.getContainerProperty(employeeExam.getIdStr(),
                    myUI.getMessage(SptMessages.Document)).getValue()).getComponent(0);
            if (b.getData() != null) {
                employeeExam.setAttachmentUniqueName(((Attachment) b.getData()).getUnique_name());
            }
            delExamsIds.add(employeeExam);
            examsTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeLanguage)) {
            delLanguagesIds.add(source.getData().toString());
            languagesTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeSeminar)) {
            delSeminarsIds.add(source.getData().toString());
            seminarsTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeCertificate)) {
            EmployeeCertificate employeeCertificate = new EmployeeCertificate();
            employeeCertificate.setIdStr(source.getData().toString());
            Button b = (Button) ((HorizontalLayout) certificatesTable.getContainerProperty(employeeCertificate.getIdStr(),
                    myUI.getMessage(SptMessages.Document)).getValue()).getComponent(0);
            if (b.getData() != null) {
                employeeCertificate.setAttachmentUniqueName(((Attachment) b.getData()).getUnique_name());
            }
            delCertificatesIds.add(employeeCertificate);
            certificatesTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeWork)) {
            delWorkPlacesIds.add(source.getData().toString());
            workPlacesTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeEducation)) {
            EmployeeEducation employeeEducation = new EmployeeEducation();
            employeeEducation.setIdStr(source.getData().toString());
            Button b = (Button) ((HorizontalLayout) educationTable.getContainerProperty(employeeEducation.getIdStr(),
                    myUI.getMessage(SptMessages.Document)).getValue()).getComponent(0);
            if (b.getData() != null) {
                employeeEducation.setAttachmentUniqueName(((Attachment) b.getData()).getUnique_name());
            }
            delEducationIds.add(employeeEducation);
            educationTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeBranch)) {
            delBranchesIds.add(source.getData().toString());
            branchesTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(schoolInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeBranchHours)) {
            delLessonIds.add(source.getData().toString());
            lessonsTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(ordersInfoLay).getComponent() && source.getId().equals(SystemSettings.dbEmployeeOrder)) {
            EmployeeOrder eo = new EmployeeOrder();
            eo.setIdStr(source.getData().toString());
            delOrderIds.add(eo);
            if (eo.getIdStr().startsWith("_")) {
                eo.setOrder_id((Integer) ((ComboBox) ordersTable.getItem(event.getButton().getData().toString().replace("_", "")).getItemProperty(
                        myUI.getMessage(SptMessages.OrderType)).getValue()).getValue());
                ordersTable.removeItem(event.getButton().getData().toString().replace("_", ""));
            } else {
                if (((ComboBox) ordersTable.getItem(event.getButton().getId()).getItemProperty(
                        myUI.getMessage(SptMessages.OrderType)).getValue()).getValue() != null) {
                    eo.setOrder_id((Integer) ((ComboBox) ordersTable.getItem(event.getButton().getId()).getItemProperty(
                            myUI.getMessage(SptMessages.OrderType)).getValue()).getValue());
                }
                ordersTable.removeItem(event.getButton().getData().toString());
            }
        } else if (source.getId().equals(SystemSettings.download_button)) {
            if (downloader == null) {
                downloader = new SimpleFileDownloader();
                addExtension(downloader);
            }
            downloader.setFileDownloadResource(getFileStream(new File(SystemSettings.PATH_TO_UPLOADS_HR
                    + ((Attachment) source.getData()).getUnique_name())));
            downloader.download();
        }
    }

    private StreamResource getFileStream(File inputfile) {

        StreamResource.StreamSource source = new StreamResource.StreamSource() {

            public InputStream getStream() {

                InputStream input = null;
                try {
                    input = new FileInputStream(inputfile);
                } catch (FileNotFoundException ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
                return input;

            }
        };
        StreamResource resource = new StreamResource(source, inputfile.getName());
        return resource;
    }

    private void insertPhones(int employee_id) {
        try {
            DbEmployeePhoneNumber dbepn = new DbEmployeePhoneNumber();
            dbepn.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delPhoneIds.size() > 0) {
                for (int i = 0; i < delPhoneIds.size(); i++) {
                    dbd.exec_delete(delPhoneIds.get(i), SystemSettings.dbEmployeePhoneNumber);
                }
            }
            if (phonesTable.getContainerDataSource().size() > 0) {
                Iterator iter = phonesTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    EmployeePhoneNumber epn = new EmployeePhoneNumber();
                    epn.setEmployee_id(employee_id);
                    epn.setNumber(((TextField) phonesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Number)).getValue()).getValue().toString());
                    epn.setPhone_type_id((Integer) ((ComboBox) phonesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Type)).getValue()).getValue());
                    if (phonesTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        epn.setId(Integer.parseInt(next.toString()));
                        dbepn.exec_update(epn);
                    } else if (phonesTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbepn.exec_insert(epn);
                    }
                }
            }
            delPhoneIds.clear();
            dbepn.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertChildren(int employee_id) {
        try {
            DbEmployeeChildren dbech = new DbEmployeeChildren();
            dbech.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delChildIds.size() > 0) {
                for (int i = 0; i < delChildIds.size(); i++) {
                    dbd.exec_delete(delChildIds.get(i), SystemSettings.dbEmployeeChildren);
                }
            }
            if (childrenTable.getContainerDataSource().size() > 0) {
                Iterator iter = childrenTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    EmployeeChildren ec = new EmployeeChildren();
                    ec.setEmployee_id(employee_id);
                    ec.setFullname(((TextField) childrenTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.FullName)).getValue()).getValue().toString());
                    ec.setInstitution(((TextField) childrenTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Institution)).getValue()).getValue().toString());
                    ec.setDate_of_birth(((DateField) childrenTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.DateOfBirth)).getValue()).getValue());
                    if (((ComboBox) childrenTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.EducationStatus)).getValue()).getValue() != null) {
                        ec.setEducation_status_id((Integer) ((ComboBox) childrenTable.getItem(next).getItemProperty(
                                myUI.getMessage(SptMessages.EducationStatus)).getValue()).getValue());
                    }
                    ec.setHealth_status_id((Integer) ((ComboBox) childrenTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.HealthStatus)).getValue()).getValue());
                    if (childrenTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        ec.setId(Integer.parseInt(next.toString()));
                        dbech.exec_update(ec);
                    } else if (childrenTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbech.exec_insert(ec);
                    }
                }
            }
            delChildIds.clear();
            dbech.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertEducation(int employee_id, int own_id, Table t, ArrayList<?> list) {
        EmployeeEducation ed = null;
        try {
            DbEmployeeEducation dbed = new DbEmployeeEducation();
            dbed.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    try {
                        if (delEducationIds.get(i).getAttachmentUniqueName() != null) {
                            File f = new File(SystemSettings.PATH_TO_UPLOADS_HR + delEducationIds.get(i).getAttachmentUniqueName());
                            f.delete();
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                    dbd.exec_delete(((EmployeeEducation) list.get(i)).getIdStr(), SystemSettings.dbEmployeeEducation);
                }
            }
            if (t.getContainerDataSource().size() > 0) {
                Iterator iter = t.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    ed = new EmployeeEducation();
                    ed.setEmployee_id(employee_id);
                    ed.setOwn_id(own_id);
                    ed.setDepartment(((TextField) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Department)).getValue()).getValue());
                    ed.setStart(((DateField) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Start)).getValue()).getValue());
                    ed.setEnd(((DateField) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.End)).getValue()).getValue());
                    ed.setCountry_id((Integer) ((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Country)).getValue()).getValue());
                    ed.setEducation_level_id((Integer) ((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.EduLevel)).getValue()).getValue());
                    ed.setUniversity_id((Integer) ((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.University)).getValue()).getValue());
                    if (t.getContainerProperty(next,
                            myUI.getMessage(SptMessages.Document)).getValue() != null) {
                        Button b = (Button) ((HorizontalLayout) t.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Document)).getValue()).getComponent(0);
                        if (b.getData() != null) {
                            ed.setAttachment_id(((Attachment) b.getData()).getId());
                        }
                    }
                    if (t.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        ed.setId(Integer.parseInt(next.toString()));
                        dbed.exec_update(ed);
                    } else if (t.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbed.exec_insert(ed);
                    }
                }
            }
            list.clear();
            dbed.close();
            dbd.close();
        } catch (Exception ex) {
            logger.info(ed.getId() + " " + ed.getAttachment_id());
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertWorkPlaces(int employee_id, int own_id, Table t, ArrayList<String> list) {
        try {
            DbEmployeeWork dbew = new DbEmployeeWork();
            dbew.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    dbd.exec_delete(list.get(i), SystemSettings.dbEmployeeWork);
                }
            }
            if (t.getContainerDataSource().size() > 0) {
                Iterator iter = t.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    EmployeeWork ew = new EmployeeWork();
                    ew.setEmployee_id(employee_id);
                    ew.setOwn_id(own_id);
                    ew.setWork_place_id((Integer) ((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.WorkPlace)).getValue()).getValue());
                    ew.setMain_position_id((Integer) ((ComboBoxMax) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.MainPosition)).getValue()).getValue());
                    if (((ComboBoxMultiselectMax) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.ExtraPositions)).getValue()).getValue() != null
                            && !((Set<?>) ((ComboBoxMultiselectMax) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.ExtraPositions)).getValue()).getValue()).isEmpty()) {
                        ew.setExtra_position_ids((Set<?>) ((ComboBoxMultiselectMax) t.getItem(next).getItemProperty(
                                myUI.getMessage(SptMessages.ExtraPositions)).getValue()).getValue());
                    } else {
                        ew.setExtra_position_ids(null);
                    }
                    ew.setWorking_status_id((Integer) ((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.WorkingStatus)).getValue()).getValue());
                    ew.setStart(((DateField) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Start)).getValue()).getValue());
                    ew.setEnd(((DateField) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.End)).getValue()).getValue());
                    ew.setSapat(((CheckBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Sapat)).getValue()).getValue());
                    if (t.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        ew.setId(Integer.parseInt(next.toString()));
                        dbew.exec_update(ew);
                    } else if (t.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        ew.setId(dbew.exec_insert(ew));
                    }
                    DbDefinition dbCon = new DbDefinition();
                    dbCon.connect();
                    dbCon.exec_delete(ew.getId() + "", SystemSettings.dbEmployeeWorkExtraPosition, SystemSettings.employee_work_id);
                    if (ew.getExtra_position_ids() != null && !ew.getExtra_position_ids().isEmpty()) {
                        Iterator extraIter = ew.getExtra_position_ids().iterator();
                        while (extraIter.hasNext()) {
                            int position_id = (Integer) extraIter.next();
                            dbew.exec_insert_extra_position(ew.getId(), position_id);
                        }
                    }
                    dbCon.close();
                }
            }
            list.clear();
            dbew.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertQuestioning(int employee_id) {
        try {
            DbEmployeeQuestion dbeq = new DbEmployeeQuestion();
            dbeq.connect();
            if (questioningTable.getContainerDataSource().size() > 0) {
                Iterator iter = questioningTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    if (((TextField) questioningTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Answer)).getValue()).getValue() != null
                            && !((TextField) questioningTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Answer)).getValue()).getValue().equals("")) {
                        EmployeeQuestioning eq = new EmployeeQuestioning();
                        eq.setEmployee_id(employee_id);
                        eq.setQuestion_id(Integer.parseInt(next.toString()));
                        eq.setAnswer(((TextField) questioningTable.getItem(next).getItemProperty(
                                myUI.getMessage(SptMessages.Answer)).getValue()).getValue().toString());
                        int st = dbeq.exec_insert(eq);
                        if (st == 0) {
                            dbeq.exec_update(eq);
                        }
                    } else {
                        DbDefinition dbd = new DbDefinition();
                        dbd.connect();
                        dbd.exec_delete((Integer) questioningTable.getContainerProperty(next, SystemSettings.id).getValue(), SystemSettings.dbEmployeeQuestion);
                        dbd.close();
                    }
                }
            }
            dbeq.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertLanguages(int employee_id) {
        try {
            DbEmployeeLanguage dbel = new DbEmployeeLanguage();
            dbel.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delLanguagesIds.size() > 0) {
                for (int i = 0; i < delLanguagesIds.size(); i++) {
                    dbd.exec_delete(delLanguagesIds.get(i), SystemSettings.dbEmployeeLanguage);
                }
            }
            if (languagesTable.getContainerDataSource().size() > 0) {
                Iterator iter = languagesTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    EmployeeLanguage el = new EmployeeLanguage();
                    el.setEmployee_id(employee_id);
                    el.setLanguage_id((Integer) ((ComboBox) languagesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Language)).getValue()).getValue());
                    el.setLevel_id((Integer) ((ComboBox) languagesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Level)).getValue()).getValue());
                    if (languagesTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        el.setId(Integer.parseInt(next.toString()));
                        dbel.exec_update(el);
                    } else if (languagesTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbel.exec_insert(el);
                    }
                }
            }
            delLanguagesIds.clear();
            dbel.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertCertificates(int employee_id) {
        EmployeeCertificate ec = null;
        try {
            DbEmployeeCertificate dbec = new DbEmployeeCertificate();
            dbec.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delCertificatesIds.size() > 0) {
                for (int i = 0; i < delCertificatesIds.size(); i++) {
                    try {
                        if (delCertificatesIds.get(i).getAttachmentUniqueName() != null) {
                            File f = new File(SystemSettings.PATH_TO_UPLOADS_HR + delCertificatesIds.get(i).getAttachmentUniqueName());
                            f.delete();
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                    dbd.exec_delete(delCertificatesIds.get(i).getIdStr(), SystemSettings.dbEmployeeCertificate);
                }
            }
            if (certificatesTable.getContainerDataSource().size() > 0) {
                Iterator iter = certificatesTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    ec = new EmployeeCertificate();
                    ec.setEmployee_id(employee_id);
                    ec.setNote(((TextField) certificatesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Note)).getValue()).getValue());
                    ec.setCertificate_id((Integer) ((ComboBoxMax) certificatesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Certificate)).getValue()).getValue());
                    ec.setGiven_by(((TextField) certificatesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.GivenBy)).getValue()).getValue());
                    ec.setDate_of_issue(((DateField) certificatesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.IssueDate)).getValue()).getValue());
                    Button b = (Button) ((HorizontalLayout) certificatesTable.getContainerProperty(next,
                            myUI.getMessage(SptMessages.Document)).getValue()).getComponent(0);
                    if (b.getData() != null) {
                        ec.setAttachment_id(((Attachment) b.getData()).getId());
                    }
                    if (certificatesTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        ec.setId(Integer.parseInt(next.toString()));
                        dbec.exec_update(ec);
                    } else if (certificatesTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbec.exec_insert(ec);
                    }
                }
            }
            delCertificatesIds.clear();
            dbec.close();
            dbd.close();
        } catch (Exception ex) {
            logger.info(ec.getId() + " " + ec.getAttachment_id());
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertSeminars(int employee_id) {
        try {
            DbEmployeeSeminar dbes = new DbEmployeeSeminar();
            dbes.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delSeminarsIds.size() > 0) {
                for (int i = 0; i < delSeminarsIds.size(); i++) {
                    dbd.exec_delete(delSeminarsIds.get(i), SystemSettings.dbEmployeeSeminar);
                }
            }
            if (seminarsTable.getContainerDataSource().size() > 0) {
                Iterator iter = seminarsTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    EmployeeSeminar es = new EmployeeSeminar();
                    es.setEmployee_id(employee_id);
                    es.setName(((TextField) seminarsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Title)).getValue()).getValue());
                    es.setSubject(((TextField) seminarsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Subject)).getValue()).getValue());
                    es.setNote(((TextField) seminarsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Note)).getValue()).getValue());
                    es.setDate_of_issue(((DateField) seminarsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.IssueDate)).getValue()).getValue());
                    if (seminarsTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        es.setId(Integer.parseInt(next.toString()));
                        dbes.exec_update(es);
                    } else if (seminarsTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbes.exec_insert(es);
                    }
                }
            }
            delSeminarsIds.clear();
            dbes.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertExams(int employee_id) {
        EmployeeExam exam = null;
        try {
            DbEmployeeExam dbex = new DbEmployeeExam();
            dbex.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delExamsIds.size() > 0) {
                for (int i = 0; i < delExamsIds.size(); i++) {
                    try {
                        if (delExamsIds.get(i).getAttachmentUniqueName() != null) {
                            File f = new File(SystemSettings.PATH_TO_UPLOADS_HR + delExamsIds.get(i).getAttachmentUniqueName());
                            f.delete();
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                    dbd.exec_delete(delExamsIds.get(i).getIdStr(), SystemSettings.dbEmployeeExams);
                }
            }
            if (examsTable.getContainerDataSource().size() > 0) {
                Iterator iter = examsTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    exam = new EmployeeExam();
                    exam.setEmployee_id(employee_id);
                    exam.setExam_id((Integer) ((ComboBox) examsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Exam)).getValue()).getValue());
                    exam.setScore((Double) ((TextField) examsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Score)).getValue()).getPropertyDataSource().getValue());
                    exam.setDate_of_issue(((DateField) examsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.IssueDate)).getValue()).getValue());
                    Button b = (Button) ((HorizontalLayout) examsTable.getContainerProperty(next,
                            myUI.getMessage(SptMessages.Document)).getValue()).getComponent(0);
                    if (b.getData() != null) {
                        exam.setAttachment_id(((Attachment) b.getData()).getId());
                    }
                    if (examsTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        exam.setId(Integer.parseInt(next.toString()));
                        dbex.exec_update(exam);
                    } else if (examsTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbex.exec_insert(exam);
                    }
                }
            }
            delExamsIds.clear();
            dbex.close();
            dbd.close();
        } catch (Exception ex) {
            logger.info(exam.getId() + " " + exam.getAttachment_id());
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertBranches(int employee_id) {
        try {
            DbEmployeeBranch dbeb = new DbEmployeeBranch();
            dbeb.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delBranchesIds.size() > 0) {
                for (int i = 0; i < delBranchesIds.size(); i++) {
                    dbd.exec_delete(delBranchesIds.get(i), SystemSettings.dbEmployeeBranch);
                }
            }
            if (branchesTable.getContainerDataSource().size() > 0) {
                employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(SptMessages.ExtraBranches)).setValue(null);
                Iterator iter = branchesTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    EmployeeBranch eb = new EmployeeBranch();
                    eb.setEmployee_id(employee_id);
                    eb.setBranch_id((Integer) ((ComboBox) branchesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Branch)).getValue()).getValue());
                    eb.setMain((Boolean) ((CheckBox) branchesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Main)).getValue()).getValue());
                    String str = ((ComboBox) branchesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Branch)).getValue()).getItemCaption(eb.getBranch_id());
                    if (eb.isMain()) {
                        employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(SptMessages.MainBranch)).setValue(
                                ((ComboBox) branchesTable.getItem(next).getItemProperty(
                                        myUI.getMessage(SptMessages.Branch)).getValue()).getItemCaption(eb.getBranch_id()));
                    } else {
                        if (employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(SptMessages.ExtraBranches)).getValue() == null) {
                            employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(SptMessages.ExtraBranches)).setValue(str);
                        } else {
                            employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(SptMessages.ExtraBranches)).setValue(
                                    employeesDataTable.getContainerProperty(employee_id,
                                            myUI.getMessage(SptMessages.ExtraBranches)).getValue().toString() + ", " + str);
                        }
                    }
                    if (branchesTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        eb.setId(Integer.parseInt(next.toString()));
                        dbeb.exec_update(eb);
                    } else if (branchesTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbeb.exec_insert(eb);
                    }

                }
            }
            delBranchesIds.clear();
            dbeb.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertLessons(int employee_id) {
        try {
            DbEmployeeLessons dbel = new DbEmployeeLessons();
            dbel.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delLessonIds.size() > 0) {
                for (int i = 0; i < delLessonIds.size(); i++) {
                    dbd.exec_delete(delLessonIds.get(i), SystemSettings.dbEmployeeBranchHours);
                }
            }
            if (lessonsTable.getContainerDataSource().size() > 0) {
                Iterator iter = lessonsTable.getItemIds().iterator();
                int hours = 0, extra = 0;
                while (iter.hasNext()) {
                    Object next = iter.next();
                    EmployeeLessons el = new EmployeeLessons();
                    el.setEmployee_id(employee_id);
                    el.setSchool_id(myUI.getUser().getSchool_id());
                    el.setBranch_id((Integer) ((ComboBox) lessonsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Lesson)).getValue()).getValue());
                    el.setClass_number_id((Integer) ((ComboBox) lessonsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.ClassName)).getValue()).getValue());
                    el.setYear_id((Integer) ((ComboBox) lessonsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.AcademicYear)).getValue()).getValue());
                    el.setHours((Integer) ((TextField) lessonsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Hours)).getValue()).getPropertyDataSource().getValue());
                    el.setExtra_hours((Integer) ((TextField) lessonsTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.ExtraHours)).getValue()).getPropertyDataSource().getValue());
                    if (el.getYear_id() == myUI.getUser().getCurrent_year().getId()) {
                        hours += el.getHours();
                        extra += el.getExtra_hours();
                    }
                    if (lessonsTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        el.setId(Integer.parseInt(next.toString()));
                        dbel.exec_update(el);
                    } else if (lessonsTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbel.exec_insert(el);
                    }
                }
                employeesDataTable.getContainerProperty(employeesDataTable.getValue(), myUI.getMessage(SptMessages.Hours)).setValue(hours);
                employeesDataTable.getContainerProperty(employeesDataTable.getValue(), myUI.getMessage(SptMessages.ExtraHours)).setValue(extra);
            }
            delLessonIds.clear();
            dbel.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertOrders(int employee_id) {
        try {
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delOrderIds.size() > 0) {
                for (int i = 0; i < delOrderIds.size(); i++) {
                    if (delOrderIds.get(i).getIdStr().startsWith("_")) {
                        employeesDataTable.getContainerProperty(employee_id, SystemSettings.is_modifiable).setValue(false);
                        delOrderIds.get(i).setIdStr(delOrderIds.get(i).getIdStr().replace("_", ""));
                    }
                    dbeo.exec_update_before_delete(delOrderIds.get(i).getIdStr());
                    dbd.exec_delete(delOrderIds.get(i).getIdStr(), SystemSettings.dbEmployeeOrder);
                    if (delOrderIds.get(i).getOrder_id() == 6 || delOrderIds.get(i).getOrder_id() == 5) {
                        DbAccCategory dbAc = new DbAccCategory();
                        dbAc.connect();
                        String namePostfix = SystemSettings.transfered;
                        if (delOrderIds.get(i).getOrder_id() == 6) {
                            namePostfix = SystemSettings.resigned;
                        }
                        dbAc.exec_update_activity_status((Integer) employeesDataTable.getContainerProperty(
                                employee_id, SystemSettings.acc_category_id).getValue(), 2, namePostfix);
                        dbAc.close();
                    }
                    if (delOrderIds.get(i).getOrder_id() == 8) {
                        DbDefinition dbCon = new DbDefinition();
                        dbCon.connect();
                        dbCon.exec_delete((Integer) employeesDataTable.getContainerProperty(employee_id,
                                SystemSettings.acc_category_id).getValue(), SystemSettings.dbAcc_category);
                        dbCon.close();
                    }
                }
            }
            if (ordersTable.getContainerDataSource().size() > 0) {
                employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(SptMessages.ExtraPosition)).setValue(null);
                employeesDataTable.getContainerProperty(employee_id, SystemSettings.extra_position_ids).setValue(null);
                Iterator iter = ordersTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    EmployeeOrder eo = new EmployeeOrder();
                    eo.setEmployee_id(employee_id);
                    eo.setOrder_id((Integer) ((ComboBox) ordersTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.OrderType)).getValue()).getValue());
                    eo.setSchool_id(myUI.getUser().getSchool_id());
                    eo.setPosition_id((Integer) employeesDataTable.getContainerProperty(employee_id, SystemSettings.position_id).getValue());
                    if (eo.getOrder_id() == 5) {
                        eo.setFrom_to_school_id((Integer) ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(SptMessages.Details)).getValue()).getValue());
                    } else if (eo.getOrder_id() == 3) {
                        eo.setClass_name_id((Integer) ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(SptMessages.Details)).getValue()).getValue());
                    } else if (eo.getOrder_id() == 1 || eo.getOrder_id() == 2 || eo.getOrder_id() == 7) {
                        eo.setPosition_id((Integer) ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(SptMessages.Details)).getValue()).getValue());
                    }
                    eo.setFrom_date(((DateField) ordersTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.FromDate)).getValue()).getValue());
                    boolean isContain = false;
                    for (EmployeeOrder temp : delOrderIds) {
                        if (ordersTable.getItem(next).getItemProperty(
                                SystemSettings.effected_by_id).getValue() != null && ordersTable.getItem(next).getItemProperty(
                                SystemSettings.effected_by_id).getValue().toString().equals(temp.getIdStr())) {
                            isContain = true;
                        }
                    }
                    if (ordersTable.getItem(next).getItemProperty(
                            SystemSettings.effected_by_id).getValue() == null || !isContain) {
                        eo.setTo_date(((DateField) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(SptMessages.TillDate)).getValue()).getValue());
                    }
                    if (((TextField) ordersTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Note)).getValue()).getValue() != null
                            && !((TextField) ordersTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Note)).getValue()).getValue().equals("")) {
                        eo.setNote(((TextField) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(SptMessages.Note)).getValue()).getValue());
                    }
                    eo.setM_employee_id(myUI.getUser().getId());
                    if (ordersTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        eo.setId(Integer.parseInt(next.toString()));
                        dbeo.exec_update(eo);
                    } else if (ordersTable.getContainerProperty(next, SystemSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbeo.exec_insert(eo);
                        if (eo.getOrder_id() == 6) {
                            DbAccCategory dbAc = new DbAccCategory();
                            dbAc.connect();
                            dbAc.exec_update_activity_status((Integer) employeesDataTable.getContainerProperty(employee_id,
                                    SystemSettings.acc_category_id).getValue(), 1, SystemSettings.resigned);
                            dbAc.close();
                        }
                        if (eo.getOrder_id() == 5) {
                            eo.setSchool_id(eo.getFrom_to_school_id());
                            eo.setFrom_to_school_id(myUI.getUser().getSchool_id());
                            eo.setOrder_id(8);
                            dbeo.exec_insert(eo);
                            eo.setOrder_id(5);
                            DbAccCategory dbAc = new DbAccCategory();
                            dbAc.connect();
                            AccCategory ac = dbAc.exec_sql(employee_id, eo.getFrom_to_school_id(), eo.getSchool_id());
                            dbAc.exec_insert(ac);
                            dbAc.exec_update_activity_status((Integer) employeesDataTable.getContainerProperty(employee_id,
                                    SystemSettings.acc_category_id).getValue(), 1, SystemSettings.transfered);
                            dbAc.close();
                        }
                    }
                    if (eo.getOrder_id() == 1) {
                        employeesDataTable.getContainerProperty(employee_id, SystemSettings.position_id).setValue(eo.getPosition_id());
                        employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(SptMessages.MainPosition)).setValue(
                                ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                        myUI.getMessage(SptMessages.Details)).getValue()).getItemCaption(eo.getPosition_id()));
                    } else if (eo.getOrder_id() == 2) {
                        String str = ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(SptMessages.Details)).getValue()).getItemCaption(eo.getPosition_id());
                        String str_ids = eo.getPosition_id() + "";
                        if (employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(SptMessages.ExtraPosition)).getValue() == null) {
                            employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(SptMessages.ExtraPosition)).setValue(str);
                            employeesDataTable.getContainerProperty(employee_id, SystemSettings.extra_position_ids).setValue(str_ids);
                        } else {
                            employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(SptMessages.ExtraPosition)).setValue(
                                    employeesDataTable.getContainerProperty(employee_id,
                                            myUI.getMessage(SptMessages.ExtraPosition)).getValue().toString() + ", " + str);
                            employeesDataTable.getContainerProperty(employee_id, SystemSettings.extra_position_ids).setValue(
                                    employeesDataTable.getContainerProperty(employee_id, SystemSettings.extra_position_ids).getValue().toString() + ", " + str_ids);
                        }
                    } else if (eo.getOrder_id() == 5) {
                        employeesDataTable.getContainerProperty(employee_id, SystemSettings.is_modifiable).setValue(false);
                    }
                    if ((Integer) ((ComboBox) ordersTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.OrderType)).getValue()).getContainerProperty(eo.getOrder_id(),
                            SystemSettings.working_status_id).getValue() != 0) {
                        employeesDataTable.getContainerProperty(employee_id, SystemSettings.working_status_id).setValue(
                                (Integer) ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                        myUI.getMessage(SptMessages.OrderType)).getValue()).getContainerProperty(eo.getOrder_id(),
                                        SystemSettings.working_status_id).getValue());
                        employeesDataTable.getContainerProperty(employee_id, SystemSettings.visible_hr_orders).setValue(
                                ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                        myUI.getMessage(SptMessages.OrderType)).getValue()).getContainerProperty(eo.getOrder_id(),
                                        SystemSettings.visible_hr_orders).getValue().toString());
                        employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(SptMessages.WorkingStatus)).setValue(
                                ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                        myUI.getMessage(SptMessages.OrderType)).getValue()).getContainerProperty(eo.getOrder_id(),
                                        myUI.getMessage(SptMessages.WorkingStatus)).getValue().toString());
                    }
                }
            }
            delOrderIds.clear();
            dbeo.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void addPhonesItem() {
        NATURAL_COL_ORDER_PHONES = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.Type),
                myUI.getMessage(SptMessages.Number)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (phonesTable.getContainerDataSource().size() == 0) {
            phonesTable.setContainerDataSource(preparePhonesContainer());
        }
        Item item;
        item = ((IndexedContainer) phonesTable.getContainerDataSource()).addItemAt(
                phonesTable.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, null, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.Type)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.Type), SystemSettings.dbPhoneType, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Number)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Number),
                        new RegexpValidator("^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s0-9]*$", true,
                                myUI.getMessage(SptMessages.NotifWrongValue)), true));
        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        phonesTable.setVisibleColumns(NATURAL_COL_ORDER_PHONES);

    }

    private void addChildItem() {
        NATURAL_COL_ORDER_CHILDREN = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.FullName),
                myUI.getMessage(SptMessages.DateOfBirth),
                myUI.getMessage(SptMessages.Institution),
                myUI.getMessage(SptMessages.EducationStatus),
                myUI.getMessage(SptMessages.HealthStatus)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (childrenTable.getContainerDataSource().size() == 0) {
            childrenTable.setContainerDataSource(prepareChildrenContainer());
        }
        Item item;
        item = ((IndexedContainer) childrenTable.getContainerDataSource()).addItemAt(
                childrenTable.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbEmployeeChildren, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.EducationStatus)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.EducationStatus), SystemSettings.dbHrEducationStatus, false));
        item.getItemProperty(myUI.getMessage(SptMessages.HealthStatus)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.HealthStatus), SystemSettings.dbHealthStatus, true));
        item.getItemProperty(myUI.getMessage(SptMessages.FullName)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.FullName),
                        new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true));
        item.getItemProperty(myUI.getMessage(SptMessages.Institution)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Institution),
                        new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 300, true), false));
        item.getItemProperty(myUI.getMessage(SptMessages.DateOfBirth)).setValue(
                createDateField(null, myUI.getMessage(SptMessages.DateOfBirth),
                        null, true, SystemSettings.datePattern, Resolution.DAY));
        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        childrenTable.setVisibleColumns(NATURAL_COL_ORDER_CHILDREN);

    }

    private void addEducationItem(final Table t, IndexedContainer c, int own_id) {
        if (own_id == 1) {
            NATURAL_COL_ORDER_EDU = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.University),
                    myUI.getMessage(SptMessages.Country),
                    myUI.getMessage(SptMessages.Department),
                    myUI.getMessage(SptMessages.EduLevel),
                    myUI.getMessage(SptMessages.Start),
                    myUI.getMessage(SptMessages.End),
                    myUI.getMessage(SptMessages.Document)};
        } else {
            NATURAL_COL_ORDER_EDU = new String[]{SystemSettings.button,
                    myUI.getMessage(SptMessages.University),
                    myUI.getMessage(SptMessages.Country),
                    myUI.getMessage(SptMessages.Department),
                    myUI.getMessage(SptMessages.EduLevel),
                    myUI.getMessage(SptMessages.Start),
                    myUI.getMessage(SptMessages.End)};

        }
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (t.getContainerDataSource().size() == 0) {
            t.setContainerDataSource(prepareEducationContainer(c));
        }
        Item item;
        item = ((IndexedContainer) t.getContainerDataSource()).addItemAt(
                t.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbEmployeeEducation, FontAwesome.MINUS_SQUARE));
        final ComboBox cb = createCombobox(0, myUI.getMessage(SptMessages.University), SystemSettings.dbUniversityTable, true);
        cb.setNewItemsAllowed(true);
        cb.setNewItemHandler(new AbstractSelect.NewItemHandler() {
            @Override
            public void addNewItem(String newItemCaption) {
                try {
                    DbDefinition dbd = new DbDefinition();
                    dbd.connect();
                    int id = dbd.exec_insert(new Definition(0, newItemCaption), SystemSettings.dbUniversityTable, false);
                    dbd.close();
                    if (id != 0) {
                        Iterator iter = c.getItemIds().iterator();
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            Item item = ((IndexedContainer) ((ComboBox) c.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.University)).getValue()).getContainerDataSource()).addItem(id);
                            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(newItemCaption);
                            cb.setValue(id);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        });
        item.getItemProperty(myUI.getMessage(SptMessages.University)).setValue(cb);
        item.getItemProperty(myUI.getMessage(SptMessages.Department)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Department),
                        new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 250, true), true));
        item.getItemProperty(myUI.getMessage(SptMessages.Start)).setValue(
                createDateField(null, myUI.getMessage(SptMessages.Start),
                        null, true, SystemSettings.yearPattern, Resolution.YEAR));
        item.getItemProperty(myUI.getMessage(SptMessages.End)).setValue(
                createDateField(null, myUI.getMessage(SptMessages.End),
                        null, true, SystemSettings.yearPattern, Resolution.YEAR));
        item.getItemProperty(myUI.getMessage(SptMessages.Country)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.Country), SystemSettings.dbCountry, true));
        item.getItemProperty(myUI.getMessage(SptMessages.EduLevel)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.EduLevel), SystemSettings.dbEduLevel, true));
        if (own_id == 1) {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);

            Button b = createButton(myUI.getMessage(SptMessages.DownLoad), null, SystemSettings.download_button, FontAwesome.DOWNLOAD);
            b.setStyleName(ValoTheme.BUTTON_SMALL);
            b.setEnabled(false);
            hl.addComponent(b);

            Upload u = createUpload("", false);
            u.setId(id);
            u.setData(b);
            hl.addComponent(u);
            item.getItemProperty(myUI.getMessage(SptMessages.Document)).setValue(hl);
        }
        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        t.setVisibleColumns(NATURAL_COL_ORDER_EDU);
        t.setColumnExpandRatio(myUI.getMessage(SptMessages.University), 1);
        t.setColumnExpandRatio(myUI.getMessage(SptMessages.Department), 1);
    }

    private void addWorkItem(final Table t, IndexedContainer c) {

        NATURAL_COL_ORDER_WORK = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.WorkPlace),
                myUI.getMessage(SptMessages.Sapat),
                myUI.getMessage(SptMessages.MainPosition),
                myUI.getMessage(SptMessages.ExtraPositions),
                myUI.getMessage(SptMessages.WorkingStatus),
                myUI.getMessage(SptMessages.Start),
                myUI.getMessage(SptMessages.End)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (t.getContainerDataSource().size() == 0) {
            t.setContainerDataSource(prepareWorkContainer(c));
        }
        Item item;
        item = ((IndexedContainer) t.getContainerDataSource()).addItemAt(
                t.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbEmployeeWork, FontAwesome.MINUS_SQUARE));
        ComboBoxMax cb = createCombobox(0, myUI.getMessage(SptMessages.MainPosition), null, true);
        item.getItemProperty(myUI.getMessage(SptMessages.MainPosition)).setValue(cb);
        ComboBoxMultiselectMax cb3 = createComboboxMulti(myUI.getMessage(SptMessages.ExtraPosition), false);
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            cb3.setContainerDataSource(
                    dbDef.exec_positions_for_select(myUI, false, true));
            cb.setContainerDataSource(
                    dbDef.exec_positions_for_select(myUI, false, true));
            dbDef.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        item.getItemProperty(myUI.getMessage(SptMessages.ExtraPositions)).setValue(cb3);
        cb = createCombobox(0, myUI.getMessage(SptMessages.WorkingStatus), null, true);
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            cb.setContainerDataSource(dbd.exec_for_select_general_working_statuses(myUI));
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        item.getItemProperty(myUI.getMessage(SptMessages.WorkingStatus)).setValue(cb);
        final ComboBoxMax cb2 = createCombobox(0, myUI.getMessage(SptMessages.WorkPlace), SystemSettings.dbWork_placeTable, true);
        cb2.setNewItemsAllowed(true);
        cb2.setNewItemHandler(new AbstractSelect.NewItemHandler() {
            @Override
            public void addNewItem(String newItemCaption) {
                try {
                    DbDefinition dbd = new DbDefinition();
                    dbd.connect();
                    int id = dbd.exec_insert(new Definition(0, newItemCaption), SystemSettings.dbWork_placeTable, false);
                    dbd.close();
                    if (id != 0) {
                        Iterator iter = c.getItemIds().iterator();
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            Item item = ((IndexedContainer) ((ComboBox) c.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.WorkPlace)).getValue()).getContainerDataSource()).addItem(id);
                            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(newItemCaption);
                            cb2.setValue(id);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        });
        item.getItemProperty(myUI.getMessage(SptMessages.WorkPlace)).setValue(cb2);
        item.getItemProperty(myUI.getMessage(SptMessages.Start)).setValue(
                createDateField(null, myUI.getMessage(SptMessages.Start), null,
                        true, SystemSettings.datePattern, Resolution.DAY));
        item.getItemProperty(myUI.getMessage(SptMessages.End)).setValue(
                createDateField(null, myUI.getMessage(SptMessages.End), null,
                        false, SystemSettings.datePattern, Resolution.DAY));
        item.getItemProperty(myUI.getMessage(SptMessages.Sapat)).setValue(
                createCheckBox(false, myUI.getMessage(SptMessages.Sapat)));

        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        t.setVisibleColumns(NATURAL_COL_ORDER_WORK);
    }

    private void addLanguageItem() {

        NATURAL_COL_ORDER_LANGUAGES = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.Language),
                myUI.getMessage(SptMessages.Level)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (languagesTable.getContainerDataSource().size() == 0) {
            languagesTable.setContainerDataSource(prepareLanguageContainer());
        }
        Item item;
        item = ((IndexedContainer) languagesTable.getContainerDataSource()).addItemAt(
                languagesTable.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbEmployeeLanguage, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.Language)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.Language), SystemSettings.dbLanguageTable, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Level)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.Level), SystemSettings.dbLanguageLevelTable, true));
        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        languagesTable.setVisibleColumns(NATURAL_COL_ORDER_LANGUAGES);
    }

    private void addCertificateItem() {

        NATURAL_COL_ORDER_CERTIFICATES = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.Certificate),
                myUI.getMessage(SptMessages.GivenBy),
                myUI.getMessage(SptMessages.IssueDate),
                myUI.getMessage(SptMessages.Note),
                myUI.getMessage(SptMessages.Document)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (certificatesTable.getContainerDataSource().size() == 0) {
            certificatesTable.setContainerDataSource(prepareCertificateContainer());
        }
        Item item;
        item = ((IndexedContainer) certificatesTable.getContainerDataSource()).addItemAt(
                certificatesTable.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbEmployeeCertificate, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Note),
                        new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue),
                                null, 250, true), false));
        item.getItemProperty(myUI.getMessage(SptMessages.GivenBy)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.GivenBy),
                        new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true));
        item.getItemProperty(myUI.getMessage(SptMessages.IssueDate)).setValue(
                createDateField(null, myUI.getMessage(SptMessages.IssueDate),
                        null, true, SystemSettings.datePattern, Resolution.DAY));
        final ComboBoxMax cb = createCombobox(0, myUI.getMessage(SptMessages.Certificate), SystemSettings.dbCertificateTable, true);
        cb.setNewItemsAllowed(true);
        cb.setNewItemHandler(new AbstractSelect.NewItemHandler() {
            @Override
            public void addNewItem(String newItemCaption) {
                try {
                    DbDefinition dbd = new DbDefinition();
                    dbd.connect();
                    int id = dbd.exec_insert(new Definition(0, newItemCaption), SystemSettings.dbCertificateTable, false);
                    dbd.close();
                    if (id != 0) {
                        Iterator iter = certificatesTable.getContainerDataSource().getItemIds().iterator();
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            Item item = ((IndexedContainer) ((ComboBox) certificatesTable.getContainerDataSource().getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Certificate)).getValue()).getContainerDataSource()).addItem(id);
                            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(newItemCaption);
                            cb.setValue(id);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        });
        item.getItemProperty(myUI.getMessage(SptMessages.Certificate)).setValue(cb);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        Button b = createButton(myUI.getMessage(SptMessages.DownLoad), null, SystemSettings.download_button, FontAwesome.DOWNLOAD);
        b.setStyleName(ValoTheme.BUTTON_SMALL);
        b.setEnabled(false);
        hl.addComponent(b);

        Upload u = createUpload("", false);
        u.setId(id);
        u.setData(b);
        hl.addComponent(u);
        item.getItemProperty(myUI.getMessage(SptMessages.Document)).setValue(hl);

        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        certificatesTable.setVisibleColumns(NATURAL_COL_ORDER_CERTIFICATES);

    }

    private void addSeminarItem() {

        NATURAL_COL_ORDER_SEMINARS = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.Title),
                myUI.getMessage(SptMessages.Subject),
                myUI.getMessage(SptMessages.Note),
                myUI.getMessage(SptMessages.IssueDate)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (seminarsTable.getContainerDataSource().size() == 0) {
            seminarsTable.setContainerDataSource(prepareSeminarContainer());
        }
        Item item;
        item = ((IndexedContainer) seminarsTable.getContainerDataSource()).addItemAt(
                seminarsTable.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbEmployeeSeminar, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Title),
                        new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true));
        item.getItemProperty(myUI.getMessage(SptMessages.Subject)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Subject),
                        new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), true));
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Note),
                        new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 200, true), false));
        item.getItemProperty(myUI.getMessage(SptMessages.IssueDate)).setValue(
                createDateField(null, myUI.getMessage(SptMessages.IssueDate),
                        null, true, SystemSettings.datePattern, Resolution.DAY));
        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        seminarsTable.setVisibleColumns(NATURAL_COL_ORDER_SEMINARS);

    }

    private void addExamItem() {

        NATURAL_COL_ORDER_EXAMS = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.Exam),
                myUI.getMessage(SptMessages.Score),
                myUI.getMessage(SptMessages.IssueDate),
                myUI.getMessage(SptMessages.Document)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (examsTable.getContainerDataSource().size() == 0) {
            examsTable.setContainerDataSource(prepareExamContainer());
        }
        Item item;
        item = ((IndexedContainer) examsTable.getContainerDataSource()).addItemAt(
                examsTable.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbEmployeeExams, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.Exam)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.Exam), SystemSettings.dbExamTable, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Score)).setValue(
                createTextfieldWithProperty(null, myUI.getMessage(SptMessages.Score),
                        new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null),
                        new ObjectProperty<Double>(0.0), SystemSettings.getStringToDoubleConverter()));
        item.getItemProperty(myUI.getMessage(SptMessages.IssueDate)).setValue(
                createDateField(null, myUI.getMessage(SptMessages.IssueDate),
                        null, true, SystemSettings.datePattern, Resolution.DAY));

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        Button b = createButton(myUI.getMessage(SptMessages.DownLoad), null, SystemSettings.download_button, FontAwesome.DOWNLOAD);
        b.setStyleName(ValoTheme.BUTTON_SMALL);
        b.setEnabled(false);
        hl.addComponent(b);

        Upload u = createUpload("", false);
        u.setId(id);
        u.setData(b);
        hl.addComponent(u);
        item.getItemProperty(myUI.getMessage(SptMessages.Document)).setValue(hl);
        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        examsTable.setVisibleColumns(NATURAL_COL_ORDER_EXAMS);

    }

    private void addBranchItem() {

        NATURAL_COL_ORDER_BRANCHES = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.Branch),
                myUI.getMessage(SptMessages.Main)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (branchesTable.getContainerDataSource().size() == 0) {
            branchesTable.setContainerDataSource(prepareBranchContainer());
        }
        Item item;
        item = ((IndexedContainer) branchesTable.getContainerDataSource()).addItemAt(
                branchesTable.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbEmployeeBranch, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.Branch)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.Branch), SystemSettings.dbBranchTable, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Main)).setValue(
                createCheckBox(false, myUI.getMessage(SptMessages.Main)));
        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        branchesTable.setVisibleColumns(NATURAL_COL_ORDER_BRANCHES);

    }

    private void addLessonItem() {

        NATURAL_COL_ORDER_LESSONS = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.Lesson),
                myUI.getMessage(SptMessages.ClassName),
                myUI.getMessage(SptMessages.AcademicYear),
                myUI.getMessage(SptMessages.Hours),
                myUI.getMessage(SptMessages.ExtraHours)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (lessonsTable.getContainerDataSource().size() == 0) {
            lessonsTable.setContainerDataSource(prepareLessonsContainer());
        }
        Item item;
        item = ((IndexedContainer) lessonsTable.getContainerDataSource()).addItemAt(
                lessonsTable.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbEmployeeBranchHours, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.Lesson)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.Lesson), SystemSettings.dbBranchTable, true));
        item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.ClassName), SystemSettings.classTable, true));
        item.getItemProperty(myUI.getMessage(SptMessages.AcademicYear)).setValue(
                createCombobox(myUI.getUser().getCurrent_year().getId(), myUI.getMessage(SptMessages.AcademicYear), SystemSettings.dbYear, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Hours)).setValue(
                createTextfieldWithProperty(null, myUI.getMessage(SptMessages.Hours),
                        new IntegerRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 1, 999),
                        new ObjectProperty<Integer>(0), SystemSettings.getStringToIntegerConverter()));
        item.getItemProperty(myUI.getMessage(SptMessages.ExtraHours)).setValue(
                createTextfieldWithProperty(null, myUI.getMessage(SptMessages.ExtraHours),
                        new IntegerRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0, 999),
                        new ObjectProperty<Integer>(0), SystemSettings.getStringToIntegerConverter()));
        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        lessonsTable.setVisibleColumns(NATURAL_COL_ORDER_LESSONS);

    }

    private void addOrderItem() {
        NATURAL_COL_ORDER_ORDERS = new String[]{SystemSettings.button,
                myUI.getMessage(SptMessages.OrderType),
                myUI.getMessage(SptMessages.Details),
                myUI.getMessage(SptMessages.FromDate),
                myUI.getMessage(SptMessages.TillDate),
                myUI.getMessage(SptMessages.Note)};
        String id = SystemSettings.FreshItem + (--r_table_counter);
        if (ordersTable.getContainerDataSource().size() == 0) {
            ordersTable.setContainerDataSource(prepareOrdersContainer());
        }
        Item item;
        item = ((IndexedContainer) ordersTable.getContainerDataSource()).addItemAt(
                ordersTable.getContainerDataSource().size(), id);
        item.getItemProperty(SystemSettings.button).setValue(createButton(myUI.getMessage(SptMessages.DeleteButton), id, SystemSettings.dbEmployeeOrder, FontAwesome.MINUS_SQUARE));
        ComboBox cb = createCombobox(0, myUI.getMessage(SptMessages.OrderType), null, true);
        cb.setData(id);
        cb.addValueChangeListener(this);
        try {
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            cb.setContainerDataSource(dbeo.execOrderTypesSel(myUI,
                    employeesDataTable.getContainerProperty(emplID, SystemSettings.visible_hr_orders).getValue().toString()));
            if (!currentUser.isPermitted(SystemSettings.cnEmployeeTransferView + ":" + SystemSettings.actAdd)) {
                cb.removeItem(5);
            }
            dbeo.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        item.getItemProperty(myUI.getMessage(SptMessages.OrderType)).setValue(cb);
        cb = createCombobox(0, null, null, false);
        cb.setVisible(false);
        cb.setRequired(false);
        item.getItemProperty(myUI.getMessage(SptMessages.Details)).setValue(cb);
        DateField df = createDateField(new Date(),
                myUI.getMessage(SptMessages.FromDate), null, true,
                SystemSettings.datePattern, Resolution.DAY);
        df.setRangeEnd(new Date());
        item.getItemProperty(myUI.getMessage(SptMessages.FromDate)).setValue(df);
        df = createDateField(null, myUI.getMessage(SptMessages.TillDate), null,
                false, SystemSettings.datePattern, Resolution.DAY);
        df.setEnabled(false);
        item.getItemProperty(myUI.getMessage(SptMessages.TillDate)).setValue(df);
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Note),
                        new StringLengthValidator(myUI.getMessage(SptMessages.NotifWrongValue), null, 300, true), false));
        item.getItemProperty(SystemSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        ordersTable.setVisibleColumns(NATURAL_COL_ORDER_ORDERS);

    }

    public TextField createTextfield(String value, String description, Validator validator, boolean isRequired) {
        TextField tf = new TextField();
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth("100%");
        tf.addValidator(validator);
        if (isRequired) {
            tf.setRequired(true);
            tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        }
        if (value != null) {
            tf.setValue(value);
        }
        return tf;
    }

    public TextField createTextfieldWithProperty(Object value, String description, Validator validator, Property p, Converter conv) {
        TextField tf = new TextField(p);
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.setRequired(true);
        tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tf.setNullRepresentation("");
        tf.setConverter(conv);
        tf.setWidth("100%");
        tf.addValidator(validator);
        tf.getPropertyDataSource().setValue(value);
        return tf;
    }

    public DateField createDateField(Date value, String description, String caption,
                                     boolean isRequired, String date_format, Resolution resolution) {
        DateField df = new DateField();
        df.setCaption(caption);
        df.setWidth("100%");
        df.setStyleName(ValoTheme.DATEFIELD_TINY);
        if (isRequired) {
            df.setRequired(true);
            df.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        }
        df.setDateFormat(date_format);
        df.setResolution(resolution);
        df.setDescription(description);
        if (value != null) {
            df.setValue(value);
        }
        return df;
    }

    public CheckBox createCheckBox(boolean value, String description) {
        CheckBox ckb = new CheckBox();
        ckb.setDescription(description);
        ckb.setValue(value);
        return ckb;
    }

    public ComboBoxMultiselectMax createComboboxMulti(String description, boolean isRequired) {
        ComboBoxMultiselectMax cb = new ComboBoxMultiselectMax();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth("100%");
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        if (isRequired) {
            cb.setRequired(true);
            cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        }
        cb.setClearButtonCaption(null);
        cb.setSelectAllButtonCaption(null);
        return cb;
    }

    public ComboBoxMax createCombobox(int value, String description, String dbtable, boolean isRequired) {
        ComboBoxMax cb = new ComboBoxMax();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth("100%");
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        if (isRequired) {
            cb.setRequired(true);
            cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        }
        try {
            if (dbtable != null) {
                DbDefinition dbp = new DbDefinition();
                dbp.connect();
                cb.setContainerDataSource(dbp.exec_for_select(myUI, dbtable, true));
                dbp.close();
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        cb.setNullSelectionAllowed(false);
        cb.setValue(value);
        return cb;
    }

    public Button createButton(String description, String itemId, String table_name, Resource icon) {

        Button btn = new Button();
        btn.setDescription(description);
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.addStyleName(ValoTheme.BUTTON_TINY);
        btn.setIcon(icon);
        btn.setData(itemId);
        btn.setId(table_name);
        btn.addClickListener(this);

        return btn;
    }

    public IndexedContainer preparePhonesContainer() {
        if (phonesCont == null) {
            phonesCont = new IndexedContainer();
            phonesCont.addContainerProperty(SystemSettings.button, Button.class, null);
            phonesCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Type), ComboBoxMax.class, null);
            phonesCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Number), TextField.class, null);
            phonesCont.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            phonesCont.removeAllItems();
        }
        return phonesCont;
    }

    public IndexedContainer prepareChildrenContainer() {
        if (childrenCont == null) {
            childrenCont = new IndexedContainer();
            childrenCont.addContainerProperty(SystemSettings.button, Button.class, null);
            childrenCont.addContainerProperty(
                    myUI.getMessage(SptMessages.FullName), TextField.class, null);
            childrenCont.addContainerProperty(
                    myUI.getMessage(SptMessages.DateOfBirth), DateField.class, null);
            childrenCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Institution), TextField.class, null);
            childrenCont.addContainerProperty(
                    myUI.getMessage(SptMessages.EducationStatus), ComboBoxMax.class, null);
            childrenCont.addContainerProperty(
                    myUI.getMessage(SptMessages.HealthStatus), ComboBoxMax.class, null);
            childrenCont.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            childrenCont.removeAllItems();
        }
        return childrenCont;
    }

    public IndexedContainer prepareEducationContainer(IndexedContainer c) {
        if (c == null) {
            c = new IndexedContainer();
            c.addContainerProperty(SystemSettings.button, Button.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.University), ComboBoxMax.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.Department), TextField.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.Start), DateField.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.End), DateField.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.Country), ComboBoxMax.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.EduLevel), ComboBoxMax.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.Document), HorizontalLayout.class, null);
            c.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            c.removeAllItems();
        }
        return c;
    }

    public IndexedContainer prepareWorkContainer(IndexedContainer c) {
        if (c == null) {
            c = new IndexedContainer();
            c.addContainerProperty(SystemSettings.button, Button.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.WorkPlace), ComboBoxMax.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.MainPosition), ComboBoxMax.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.ExtraPositions), ComboBoxMultiselectMax.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.WorkingStatus), ComboBoxMax.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.Start), DateField.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.End), DateField.class, null);
            c.addContainerProperty(myUI.getMessage(SptMessages.Sapat), CheckBox.class, null);
            c.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            c.removeAllItems();
        }
        return c;
    }

    public IndexedContainer prepareLanguageContainer() {
        if (languagesCont == null) {
            languagesCont = new IndexedContainer();
            languagesCont.addContainerProperty(SystemSettings.button, Button.class, null);
            languagesCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Language), ComboBoxMax.class, null);
            languagesCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Level), ComboBoxMax.class, null);
            languagesCont.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            languagesCont.removeAllItems();
        }
        return languagesCont;
    }

    public IndexedContainer prepareCertificateContainer() {
        if (certificatesCont == null) {
            certificatesCont = new IndexedContainer();
            certificatesCont.addContainerProperty(SystemSettings.button, Button.class, null);
            certificatesCont.addContainerProperty(myUI.getMessage(SptMessages.Note), TextField.class, null);
            certificatesCont.addContainerProperty(myUI.getMessage(SptMessages.GivenBy), TextField.class, null);
            certificatesCont.addContainerProperty(myUI.getMessage(SptMessages.IssueDate), DateField.class, null);
            certificatesCont.addContainerProperty(myUI.getMessage(SptMessages.Certificate), ComboBoxMax.class, null);
            certificatesCont.addContainerProperty(myUI.getMessage(SptMessages.Document), HorizontalLayout.class, null);
            certificatesCont.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            certificatesCont.removeAllItems();
        }
        return certificatesCont;
    }

    public IndexedContainer prepareSeminarContainer() {
        if (seminarsCont == null) {
            seminarsCont = new IndexedContainer();
            seminarsCont.addContainerProperty(SystemSettings.button, Button.class, null);
            seminarsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Title), TextField.class, null);
            seminarsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Subject), TextField.class, null);
            seminarsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Note), TextField.class, null);
            seminarsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.IssueDate), DateField.class, null);
            seminarsCont.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            seminarsCont.removeAllItems();
        }
        return seminarsCont;
    }

    public IndexedContainer prepareExamContainer() {
        if (examsCont == null) {
            examsCont = new IndexedContainer();
            examsCont.addContainerProperty(SystemSettings.button, Button.class, null);
            examsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Exam), ComboBox.class, null);
            examsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Score), TextField.class, null);
            examsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.IssueDate), DateField.class, null);
            examsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Document), HorizontalLayout.class, null);
            examsCont.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            examsCont.removeAllItems();
        }
        return examsCont;
    }

    public IndexedContainer prepareBranchContainer() {
        if (branchesCont == null) {
            branchesCont = new IndexedContainer();
            branchesCont.addContainerProperty(SystemSettings.button, Button.class, null);
            branchesCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Branch), ComboBox.class, null);
            branchesCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Main), CheckBox.class, null);
            branchesCont.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            branchesCont.removeAllItems();
        }
        return branchesCont;
    }

    public IndexedContainer prepareLessonsContainer() {
        if (lessonsCont == null) {
            lessonsCont = new IndexedContainer();
            lessonsCont.addContainerProperty(SystemSettings.button, Button.class, null);
            lessonsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Lesson), ComboBox.class, null);
            lessonsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.ClassName), ComboBoxMax.class, null);
            lessonsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.AcademicYear), ComboBox.class, null);
            lessonsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Hours), TextField.class, null);
            lessonsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.ExtraHours), TextField.class, null);
            lessonsCont.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            lessonsCont.removeAllItems();
        }
        return lessonsCont;
    }

    public IndexedContainer prepareOrdersContainer() {
        if (ordersCont == null) {
            ordersCont = new IndexedContainer();
            ordersCont.addContainerProperty(SystemSettings.button, Button.class, null);
            ordersCont.addContainerProperty(
                    myUI.getMessage(SptMessages.OrderType), ComboBox.class, null);
            ordersCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Details), ComboBox.class, null);
            ordersCont.addContainerProperty(
                    myUI.getMessage(SptMessages.FromDate), DateField.class, null);
            ordersCont.addContainerProperty(
                    myUI.getMessage(SptMessages.TillDate), DateField.class, null);
            ordersCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Note), TextField.class, null);
            ordersCont.addContainerProperty(SystemSettings.effected_by_id, String.class, null);
            ordersCont.addContainerProperty(SystemSettings.crud_status, String.class, null);
        } else {
            ordersCont.removeAllItems();
        }
        return ordersCont;
    }

    private void insertEmplContact(EmployeeContact ec) {
        try {
            DbEmployeeContact dbec = new DbEmployeeContact();
            dbec.connect();
            int st = dbec.exec_insert(ec);
            if (st == 0) {
                dbec.exec_update(ec);
            }
            dbec.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertEmplSpouse(EmployeeSpouse es) {
        try {
            if (spouseHealthCB.getValue() != null &&
                    spouseFullnameTF.getValue() != null && !spouseFullnameTF.getValue().equals("")) {
                DbEmployeeSpouse dbes = new DbEmployeeSpouse();
                dbes.connect();
                int st = dbes.exec_insert(es);
                if (st == 0) {
                    dbes.exec_update(es);
                }
                dbes.close();
            } else {
                DbDefinition dbCon = new DbDefinition();
                dbCon.connect();
                dbCon.exec_delete(es.getEmployee_id() + "",
                        SystemSettings.dbEmployeeSpouse, SystemSettings.employee_id);
                dbCon.close();
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertEmplExtraInfo(EmployeeExtraInfo eei) {
        try {
            DbEmployeeExtraInfo dbei = new DbEmployeeExtraInfo();
            dbei.connect();
            int st = dbei.exec_insert(eei);
            if (st == 0) {
                dbei.exec_update(eei);
            }
            dbei.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertEmplGradSchool(EmployeeGraduationSchool egs) {
        try {
            DbEmployeeGraduationSchool dbCon = new DbEmployeeGraduationSchool();
            dbCon.connect();
            int st = dbCon.exec_insert(egs);
            if (st == 0) {
                dbCon.exec_update(egs);
            }
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertEmplOrder(EmployeeOrder eo) {
        try {
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            dbeo.exec_insert(eo);
            dbeo.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertloginRoleName(String loginRName, String roleName) {
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            dbe.exec_insert_role(loginRName, roleName);
            dbe.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertPermissions(String login, String permissions) {
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            String[] parsedArr = permissions.split(";");
            for (String string : parsedArr) {
                dbe.exec_insert_perm(login, string);
            }
            dbe.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertPermissions(String login) {
        String permOneStr = "";
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            Iterator iter = ((IndexedContainer) permissionTable
                    .getContainerDataSource()).getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                String permission = "";
                if (SystemSettings.convertCollectionToStr((Set) ((ComboBoxMultiselectMax) (permissionTable
                        .getContainerProperty(next, myUI.getMessage(SptMessages.Functions))
                        .getValue())).getValue()) != null) {
                    permission = next + ":" + (SystemSettings.convertCollectionToStr((Set) ((ComboBoxMultiselectMax) (permissionTable
                            .getContainerProperty(next, myUI.getMessage(SptMessages.Functions))
                            .getValue())).getValue()));
                    dbe.exec_insert_perm(login, permission);
                    permOneStr += permission;
                    if (iter.hasNext()) {
                        permOneStr += ";";
                    }
                }
            }
            employeesDataTable.getContainerProperty(emplID, myUI.getMessage(SptMessages.Permissions)).setValue(permOneStr);
            dbe.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private String checkPassword() {
        String password = "";
        if (passwordTF.getValue() != null
                && !passwordTF.getValue().equals("")) {
            password = (new Sha256Hash(passwordTF.getValue()).toString());
        }
        return password;
    }

    private Employee getEmployee(int id) {
        Employee e = new Employee();
        e.setPhoto(photoName);
        e.setLogin(loginTF.getValue());
        e.setPassword(new Sha256Hash(passwordTF.getValue()).toString());
        e.setName(nameTF.getValue());
        e.setSurname(surnameTF.getValue());
        if (middlenameTF.getValue() != null && !middlenameTF.getValue().equals("")) {
            e.setMiddle_name(middlenameTF.getValue());
        }
        e.setGender_id((Integer) genderCB.getValue());
        e.setBirth_date(birthDateDF.getValue());
        e.setNationality_id((Integer) nationalityCB.getValue());
        e.setMartial_status_id((Integer) martialStatusCB.getValue());
        e.setModified_by_id(myUI.getUser().getId());
        e.setId(id);
        return e;
    }

    private EmployeeContact getEmployeeContact(int employee_id) {
        EmployeeContact ec = new EmployeeContact();
        ec.setEmployee_id(employee_id);
        ec.setEmail(emailTF.getValue());
        ec.setAddress(addressTA.getValue());
        ec.setBirth_place(birth_placeTF.getValue());
        ec.setCitizenship_id((Integer) citizenshipCB.getValue());
        return ec;
    }

    private EmployeeSpouse getEmployeeSpouse(int employee_id) {
        EmployeeSpouse es = new EmployeeSpouse();
        es.setEmployee_id(employee_id);
        es.setFullname(spouseFullnameTF.getValue());
        es.setPhone(spousePhoneTF.getValue());
        es.setHealth_notes(spouseHealthNotesTF.getValue());
        if (spouseHealthCB.getValue() != null) {
            es.setHealth_status_id((Integer) spouseHealthCB.getValue());
        }
        return es;
    }

    private EmployeeExtraInfo getEmployeeExtraInfo(int employee_id) {
        EmployeeExtraInfo es = new EmployeeExtraInfo();
        es.setEmployee_id(employee_id);
        es.setHobbies(hobbiesTF.getValue());
        es.setFobbies(fobbiesTF.getValue());
        es.setHealth_notes(healthNotesTA.getValue());
        es.setShort_notes(shortNotesTA.getValue());
        es.setHealth_status_id((Integer) healthCB.getValue());
        return es;
    }

    private EmployeeGraduationSchool getEmployeeGradSchool(int employee_id) {
        EmployeeGraduationSchool es = new EmployeeGraduationSchool();
        es.setEmployee_id(employee_id);
        es.setStart(gradSchoolStartDF.getValue());
        es.setEnd(gradSchoolEndDF.getValue());
        es.setSchool_id((Integer) gradSchoolCB.getValue());
        return es;
    }

    private void addDatacontainerItem(int id, int acc_category_id) {
        Item item = ((IndexedContainer) employeesDataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Id)).setValue(loginTF.getValue());
        item.getItemProperty(SystemSettings.id).setValue(id);
        item.getItemProperty(myUI.getMessage(SptMessages.Firstname)).setValue(nameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Surname)).setValue(surnameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Middlename)).setValue(middlenameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.DateOfBirth)).setValue(birthDateDF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Photo)).setValue(photoName);
        item.getItemProperty(myUI.getMessage(SptMessages.Permissions)).setValue(
                mainPositionCB.getContainerProperty(mainPositionCB.getValue(),
                        myUI.getMessage(SptMessages.Permissions)).getValue());
        item.getItemProperty(SystemSettings.gender_id).setValue(genderCB.getValue());
        item.getItemProperty(SystemSettings.nationality_id).setValue(nationalityCB.getValue());
        item.getItemProperty(SystemSettings.martial_status_id).setValue(martialStatusCB.getValue());
        item.getItemProperty(SystemSettings.position_id).setValue(mainPositionCB.getValue());
        item.getItemProperty(SystemSettings.salary_category_id).setValue(contractCategoryCB.getValue());
        item.getItemProperty(SystemSettings.working_status_id).setValue(2);
        item.getItemProperty(SystemSettings.acc_category_id).setValue(acc_category_id);
        item.getItemProperty(myUI.getMessage(SptMessages.MainPosition)).setValue(
                mainPositionCB.getContainerDataSource().getContainerProperty(mainPositionCB.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
        item.getItemProperty(myUI.getMessage(SptMessages.WorkingStatus)).setValue(
                workingStatCont.getContainerProperty(2, myUI.getMessage(SptMessages.Title)).getValue().toString());
        employeesDataTable.clearFilters();
        employeesDataTable.setValue(id);
    }

    private void updateDatacontainer() {
        employeesDataTable.getContainerProperty(emplID,
                myUI.getMessage(SptMessages.Id)).setValue(loginTF.getValue());
        employeesDataTable.getContainerProperty(emplID,
                myUI.getMessage(SptMessages.Firstname)).setValue(nameTF.getValue());
        employeesDataTable.getContainerProperty(emplID,
                myUI.getMessage(SptMessages.Surname)).setValue(surnameTF.getValue());
        employeesDataTable.getContainerProperty(emplID,
                myUI.getMessage(SptMessages.Middlename)).setValue(middlenameTF.getValue());
        employeesDataTable.getContainerProperty(emplID,
                myUI.getMessage(SptMessages.Photo)).setValue(photoName);
        employeesDataTable.getContainerProperty(emplID,
                myUI.getMessage(SptMessages.DateOfBirth)).setValue(birthDateDF.getValue());
        employeesDataTable.getContainerProperty(emplID,
                SystemSettings.gender_id).setValue(genderCB.getValue());
        employeesDataTable.getContainerProperty(emplID,
                SystemSettings.nationality_id).setValue(nationalityCB.getValue());
        employeesDataTable.getContainerProperty(emplID,
                SystemSettings.martial_status_id).setValue(martialStatusCB.getValue());
        employeesDataTable.getContainerProperty(emplID,
                SystemSettings.salary_category_id).setValue(contractCategoryCB.getValue());
        employeesDataTable.getContainerProperty(emplID,
                myUI.getMessage(SptMessages.MainPosition)).setValue(mainPositionCB
                .getContainerDataSource().getContainerProperty(mainPositionCB.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
    }

    private void execDelete() {
        DbDefinition dbDef = null;
        int st = 0;
        try {
            dbDef = new DbDefinition();
            dbDef.connect();
            dbDef.getConnection().setAutoCommit(false);
            st = dbDef.exec_delete((Integer) employeesDataTable.getContainerProperty(emplID, SystemSettings.acc_category_id).getValue(), SystemSettings.dbAcc_category);
            if (st != 0) {
                dbDef.exec_delete((Integer) employeesDataTable.getContainerProperty(emplID,
                        SystemSettings.id).getValue(), SystemSettings.dbEmployee);
            }
            dbDef.getConnection().commit();
            dbDef.getConnection().setAutoCommit(true);
            dbDef.close();
        } catch (SQLIntegrityConstraintViolationException ex) {
            Notification.show(myUI.getMessage(SptMessages.CanNotDelete),
                    Notification.Type.WARNING_MESSAGE);
            if (dbDef != null) {
                try {
                    dbDef.getConnection().rollback();
                } catch (Exception exx) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            }
            logger.error(ex);
            logger.catching(ex);
        } catch (Exception ex) {
            if (dbDef != null) {
                try {
                    dbDef.getConnection().rollback();
                } catch (Exception exx) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            }
            logger.error(ex);
            logger.catching(ex);
        }
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            if (st != 0) {
                dbe.exec_delete_perm(employeesDataTable.getContainerDataSource().getContainerProperty(emplID,
                        myUI.getMessage(SptMessages.Id)).getValue().toString());
                dbe.exec_delete_role(employeesDataTable.getContainerDataSource().getContainerProperty(emplID,
                        myUI.getMessage(SptMessages.Id)).getValue().toString());
                clearEmployeeFields(false);
                clearContactFields();
                clearSpouseFields();
                clearExtraInfoFields();
                clearGradSchoolFields();
                canBeAdvisor.setValue(false);
                questioningTable.removeAllItems();
                clearAchievementsFields();
                clearProfFields();
                lessonsTable.removeAllItems();
                ordersTable.removeAllItems();
                workingStatCont.getContainerProperty((Integer) employeesDataTable
                                .getContainerProperty(emplID,
                                        SystemSettings.working_status_id).getValue(), SystemSettings.count)
                        .setValue(((Integer) workingStatCont.getContainerProperty((Integer) employeesDataTable
                                        .getContainerProperty(emplID,
                                                SystemSettings.working_status_id).getValue(),
                                SystemSettings.count).getValue()) - 1);
                repaint();
                employeesDataTable.getContainerDataSource().removeItem(emplID);
                employeesDataTable.setValue(null);
                Notification.show(myUI.getMessage(SptMessages.ValueDeleted), Notification.Type.HUMANIZED_MESSAGE);
            }
            dbe.close();
        } catch (Exception ex) {
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == loginTF) {
            if (isNew && loginTF.getValue() != null && !loginTF.getValue().equals("")
                    && loginTF.isEnabled()) {
                photoUpl.setEnabled(true);
            } else {
                photoUpl.setEnabled(false);
            }
        } else if (property == optionGroup) {
            setEmployeesDataTable(property.getValue().toString());
            repaint();
        } else if (property == martialStatusCB) {
            if (martialStatusCB.getValue() != null && (Integer) martialStatusCB.getValue() == 2) {
                spouseHealthCB.setRequired(true);
                spouseFullnameTF.setRequired(true);
                fieldsLayFamily.setVisible(true);
                captionSpouseInfo.setVisible(true);
            } else {
                spouseHealthCB.setRequired(false);
                spouseFullnameTF.setRequired(false);
                fieldsLayFamily.setVisible(false);
                captionSpouseInfo.setVisible(false);
            }
        } else if (property == employeesDataTable) {
            if (employeesDataTable.getItem(employeesDataTable.getValue()) != null) {
                emplID = (Integer) employeesDataTable.getValue();
                fillFields();
                if (emplID == myUI.getUser().getId()) {
                    modifyBtn.setEnabled(true);
                } else if (!(Boolean) employeesDataTable.getContainerProperty(
                        emplID, SystemSettings.is_modifiable).getValue()) {
                    modifyBtn.setEnabled(false);
                    deleteBtn.setEnabled(false);
                } else {
                    if (currentUser.isPermitted(SystemSettings.cnEmployeeDefinitionView + ":" + SystemSettings.actModify)) {
                        modifyBtn.setEnabled(true);
                    }
                    if (tabs.getSelectedTab() == tabs.getTab(empSearchLay).getComponent()) {
                        if (currentUser.isPermitted(SystemSettings.cnEmployeeDefinitionView + ":" + SystemSettings.actDelete)) {
                            deleteBtn.setEnabled(true);
                        }
                    }
                }
            } else {
                emplID = 0;
            }
            updateInfoLayout();
        } else {
            ComboBox ordersCB = (ComboBox) property;
            if (ordersCB.getValue() != null) {
                ComboBox orders_extraCB = ((ComboBox) ordersTable.getContainerProperty(ordersCB.getData(), myUI.getMessage(SptMessages.Details)).getValue());
                DateField orderToDateDF = ((DateField) ordersTable.getContainerProperty(ordersCB.getData(), myUI.getMessage(SptMessages.TillDate)).getValue());

                try {
                    if ((Integer) ordersCB.getValue() == 3) {
                        orderToDateDF.setEnabled(true);
                        orders_extraCB.setVisible(true);
                        orders_extraCB.setDescription(myUI.getMessage(SptMessages.ClassName));
                        orders_extraCB.setRequired(true);
                        orders_extraCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
                        DbClassName dbcn = new DbClassName();
                        dbcn.connect();
                        orders_extraCB.setContainerDataSource(
                                dbcn.execClass_sel(myUI, myUI.getUser().getSchool_id()));
                        dbcn.close();
                    } else if ((Integer) ordersCB.getValue() == 5) {
                        orders_extraCB.setVisible(true);
                        orders_extraCB.setDescription(myUI.getMessage(SptMessages.School));
                        orders_extraCB.setRequired(true);
                        orders_extraCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
                        DbSchool dbs = new DbSchool();
                        dbs.connect();
                        orders_extraCB.setContainerDataSource(
                                dbs.execSchoolSel(myUI, myUI.getUser().getSchool_id()));
                        dbs.close();
                    } else if ((Integer) ordersCB.getValue() == 1 || (Integer) ordersCB.getValue() == 2
                            || (Integer) ordersCB.getValue() == 7) {
                        if ((Integer) ordersCB.getValue() == 2) {
                            orderToDateDF.setEnabled(true);
                        } else {
                            orderToDateDF.setValue(null);
                            orderToDateDF.setEnabled(false);
                        }
                        orders_extraCB.setVisible(true);
                        orders_extraCB.setDescription(myUI.getMessage(SptMessages.Position));
                        orders_extraCB.setRequired(true);
                        orders_extraCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
                        DbDefinition dbd = new DbDefinition();
                        dbd.connect();
                        orders_extraCB.setContainerDataSource(
                                dbd.exec_positions_for_select(myUI, currentUser.hasRole(SystemSettings.rnAdmin), currentUser.hasRole(SystemSettings.rnHr)));
                        dbd.close();
                    } else {
                        orders_extraCB.setValue(null);
                        orders_extraCB.setVisible(false);
                        orders_extraCB.setRequired(false);
                        orderToDateDF.setValue(null);
                        orderToDateDF.setEnabled(false);
                    }
                    ordersTable.refreshRowCache();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            }
        }
    }
}
