package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.*;
import kg.alex.spt.domain.*;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.pdf.*;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import kg.alex.spt.utils.MyFilterGenerator;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.simplefiledownloader.SimpleFileDownloader;

import java.io.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Calendar;
import java.util.*;

public class StudentDefinitionView extends VerticalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(StudentDefinitionView.class);
    private final MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn, divideBtn;
    private final FilterTable studDataTable;
    private final OptionGroup optionGroup;
    private TextField nameTF, loginTF, surnameTF, middleNameTF, divideTF, initialPaymentTF;
    private DateField birthDate, currDate;
    private ComboBox genderCB, classCB, statusCB, contractCB;
    private OptionGroup contLangOG;
    private FormLayout fieldsLay1, fieldsLay2;
    private int r_table_counter = 1000;
    private final int receive = 2;
    private final int give = 1;
    private int discCounter;
    private int contr_id;
    private Double instCtrAmount;
    private Double netContrAmount;
    private Double contract_amount;
    private Double instFirstPay;
    private Double instPlanContSum;
    private Double contr_with_disc;
    private Double ttl_left;
    private Double ttl_payment;
    private Double init_payment;
    private Double discountAmount;
    private Double debt;
    private Double toPay;
    private final Button plusRelButton;
    private final Button plusMatGiveButton;
    private final Button plusInstButton;
    private final Button plusPayButton;
    private final Button plusMatReceiveButton;
    private final Button plusDiscButton;
    private final Button plusCorrectionButton;
    private final Button plusCallButton;
    private final FormattedTable relativesTable;
    private final FormattedTable acsGiveTable;
    private FormattedTable installmentTable;
    private final FormattedTable paymentsTable;
    private final FormattedTable acsReceiveTable;
    private FormattedTable discountsTable;
    private FormattedTable correctionsTable;
    private final FormattedTable callsTable;
    private final TabSheet tabs;
    private final ArrayList<String> delPayIds = new ArrayList<>();
    private final ArrayList<String> delCallIds = new ArrayList<>();
    private final ArrayList<String> delCorrectionIds = new ArrayList<>();
    private final ArrayList<StudentDiscount> delDiscIds = new ArrayList<>();
    private final ArrayList<String> delRelIds = new ArrayList<>();
    private PopupButton printButton;
    private Button financialHistoryButton;
    private IndexedContainer productsContainer = null,
            acsGivContainer = null, acsRecContainer = null, instPlanCont = null,
            paymentCont = null, discountCont = null, correctionCont = null, callsCont = null;
    private boolean isNew, isNewContract = false;
    private Label debtLab;
    private Label contractLab;
    private Label discountLab;
    private Label correctionLab;
    private Label netLab;
    private Label paidLab;
    private Label leftLab;
    private Label instPlanTtlLab;
    private Label netIPlanTtlLab;
    private Label planDebt;
    private Label instPlanDifLab;
    private final Label eduStatTtlLab;
    private Label tabContractLab;
    private Label tabContractNetLab;

    private final String[] NATURAL_COL_ORDER;
    private String[] NATURAL_COL_ORDER_PAYMENTS;
    private String[] NATURAL_COL_ORDER_CALLS;
    private String[] NATURAL_COL_ORDER_INST_PLAN;
    private String[] NATURAL_COL_ORDER_DISCOUNTS;
    private String[] NATURAL_COL_ORDER_CORRECTIONS;
    private String[] NATURAL_COL_ORDER_RELATIVES;
    private VerticalLayout contractLay;
    private final VerticalLayout famTableLay;
    private final VerticalLayout acsGiveTableLay;
    private final VerticalLayout payTableLay;
    private final VerticalLayout acsReceiveTableLay;
    private final VerticalLayout callsTableLay;
    private GridLayout gridStudLay;
    private GridLayout contractTabLay;
    private GridLayout instPlanLay;
    private final GridLayout studSearchLay;
    private final HorizontalSplitPanel horSplitPanel;
    private HorizontalLayout buttonsLay;
    private Upload photoUpl;
    private File myFile;
    private Window statusWindow;
    private Button cancelButton;
    private ProgressBar uploadProgressBar;
    private String photoName, fileName, mimeType;
    StringBuilder discountsStr = new StringBuilder();
    private Embedded photoEmb;
    private final Subject currentUser = SecurityUtils.getSubject();
    public IndexedContainer eduStatCont;
    private SimpleFileDownloader downloader = null;

    public StudentDefinitionView(final MyVaadinUI myUI) {
        this.myUI = myUI;

        buildButtonsLayout();
        buildStudGridLayout();
        try {
            DbDefinition dbed = new DbDefinition();
            dbed.connect();
            eduStatCont = dbed.execSQL_statuses_with_count(myUI, Settings.dbEducationStatus, true);
            dbed.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        horSplitPanel = new HorizontalSplitPanel();
        horSplitPanel.setSplitPosition(76, Sizeable.Unit.PERCENTAGE);
        horSplitPanel.setSizeFull();
        horSplitPanel.setLocked(true);
        horSplitPanel.setFirstComponent(gridStudLay);
        horSplitPanel.setSecondComponent(contractLay);

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Id),
                myUI.getMessage(SptMessages.FirstName),
                myUI.getMessage(SptMessages.LastName), myUI.getMessage(SptMessages.ClassName),
                myUI.getMessage(SptMessages.EducationStatus),
                myUI.getMessage(SptMessages.EnteringYear)};

        Label eduStatusLab = new Label();
        eduStatusLab.setSizeUndefined();
        eduStatusLab.setContentMode(ContentMode.HTML);
        eduStatusLab.setValue(myUI.getMessage(SptMessages.ShowByEducationStatuses) + ": ");

        IndexedContainer eduContainer = new IndexedContainer();
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            eduContainer = dbd.exec_for_select(myUI, Settings.dbEducationStatus, true);
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        optionGroup = new OptionGroup();
        optionGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        optionGroup.setMultiSelect(true);
        optionGroup.addItems(eduContainer.getItemIds());
        for (Object o : eduContainer.getItemIds()) {
            Integer next = (Integer) o;
            optionGroup.setItemCaption(next, eduContainer.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Title)).getValue().toString());
            if (next <= 3) {
                optionGroup.select(next);
            }
        }
        optionGroup.addValueChangeListener(this);

        studDataTable = new FilterTable();
        studDataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        studDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        studDataTable.setSizeFull();
        studDataTable.setNullSelectionAllowed(false);
        studDataTable.setFilterBarVisible(true);
        studDataTable.setSelectable(true);
        studDataTable.addValueChangeListener(this);

        setStudDataTable(optionGroup.getValue().toString());

        HorizontalLayout studSearchLayFooter = new HorizontalLayout();
        studSearchLayFooter.setWidth(Settings.PERCENTS100);
        eduStatTtlLab = new Label();
        eduStatTtlLab.setSizeUndefined();
        eduStatTtlLab.setImmediate(true);
        eduStatTtlLab.setContentMode(ContentMode.HTML);
        Label filteredLab = new Label();
        filteredLab.setSizeUndefined();
        filteredLab.setImmediate(true);
        filteredLab.setContentMode(ContentMode.HTML);
        filteredLab.setValue(myUI.getMessage(SptMessages.Filtered) + ": 0");
        repaint();

        studDataTable.setFilterGenerator(new MyFilterGenerator(
                filteredLab, myUI.getMessage(SptMessages.Filtered), studDataTable));

        studSearchLay = new GridLayout(2, 3);
        studSearchLay.setSizeFull();
        studSearchLay.setMargin(true);
        studSearchLayFooter.addComponent(filteredLab);
        studSearchLayFooter.addComponent(eduStatTtlLab);
        studSearchLayFooter.setComponentAlignment(filteredLab, Alignment.BOTTOM_LEFT);
        studSearchLayFooter.setComponentAlignment(eduStatTtlLab, Alignment.BOTTOM_RIGHT);
        studSearchLayFooter.setExpandRatio(filteredLab, 1);
        studSearchLayFooter.setExpandRatio(eduStatTtlLab, 2);

        studSearchLay.addComponent(eduStatusLab, 0, 0);
        studSearchLay.addComponent(optionGroup, 1, 0);
        studSearchLay.addComponent(studDataTable, 0, 1, 1, 1);
        studSearchLay.addComponent(studSearchLayFooter, 0, 2, 1, 2);
        studSearchLay.setRowExpandRatio(1, 1);
        studSearchLay.setColumnExpandRatio(0, 1);
        studSearchLay.setComponentAlignment(optionGroup, Alignment.MIDDLE_RIGHT);
        studSearchLay.setComponentAlignment(eduStatusLab, Alignment.MIDDLE_RIGHT);

        this.setSplitPosition(37, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(horSplitPanel);

        plusRelButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusRelButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusRelButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusRelButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusRelButton.addClickListener(this);

        plusMatGiveButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusMatGiveButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusMatGiveButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusMatGiveButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusMatGiveButton.addClickListener(this);

        plusMatReceiveButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusMatReceiveButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusMatReceiveButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusMatReceiveButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusMatReceiveButton.addClickListener(this);

        plusDiscButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusDiscButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusDiscButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusDiscButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusDiscButton.addClickListener(this);

        plusCorrectionButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusCorrectionButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusCorrectionButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusCorrectionButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusCorrectionButton.addClickListener(this);

        plusInstButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusInstButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusInstButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusInstButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusInstButton.addClickListener(this);

        plusPayButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusPayButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusPayButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusPayButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusPayButton.addClickListener(this);

        plusCallButton = new Button(myUI.getMessage(SptMessages.AddRecord));
        plusCallButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusCallButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusCallButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusCallButton.addClickListener(this);

        relativesTable = new FormattedTable();
        relativesTable.setSizeFull();
        relativesTable.setStyleName(ValoTheme.TABLE_SMALL);
        relativesTable.setNullSelectionAllowed(false);

        famTableLay = new VerticalLayout();
        famTableLay.setSizeFull();
        famTableLay.setSpacing(true);
        famTableLay.setMargin(true);
        famTableLay.addComponent(plusRelButton);
        famTableLay.setComponentAlignment(plusRelButton, Alignment.BOTTOM_LEFT);
        famTableLay.addComponent(relativesTable);
        famTableLay.setExpandRatio(relativesTable, 1);

        acsGiveTable = new FormattedTable();
        acsGiveTable.setSizeFull();
        acsGiveTable.setStyleName(ValoTheme.TABLE_SMALL);
        acsGiveTable.setNullSelectionAllowed(false);
        acsGiveTableLay = new VerticalLayout();
        acsGiveTableLay.setSizeFull();
        acsGiveTableLay.setSpacing(true);
        acsGiveTableLay.setMargin(true);
        acsGiveTableLay.addComponent(plusMatGiveButton);
        acsGiveTableLay.setComponentAlignment(plusMatGiveButton, Alignment.BOTTOM_LEFT);
        acsGiveTableLay.addComponent(acsGiveTable);
        acsGiveTableLay.setExpandRatio(acsGiveTable, 1);

        acsReceiveTable = new FormattedTable();
        acsReceiveTable.setSizeFull();
        acsReceiveTable.setStyleName(ValoTheme.TABLE_SMALL);
        acsReceiveTable.setNullSelectionAllowed(false);
        acsReceiveTableLay = new VerticalLayout();
        acsReceiveTableLay.setSizeFull();
        acsReceiveTableLay.setSpacing(true);
        acsReceiveTableLay.setMargin(true);
        acsReceiveTableLay.addComponent(plusMatReceiveButton);
        acsReceiveTableLay.setComponentAlignment(plusMatReceiveButton, Alignment.BOTTOM_LEFT);
        acsReceiveTableLay.addComponent(acsReceiveTable);
        acsReceiveTableLay.setExpandRatio(acsReceiveTable, 1);

        buildContractTab();

        paymentsTable = new FormattedTable();
        paymentsTable.setSizeFull();
        paymentsTable.setStyleName(ValoTheme.TABLE_SMALL);
        paymentsTable.setNullSelectionAllowed(false);
        payTableLay = new VerticalLayout();
        payTableLay.setSizeFull();
        payTableLay.setSpacing(true);
        payTableLay.setMargin(true);
        payTableLay.addComponent(plusPayButton);
        payTableLay.setComponentAlignment(plusPayButton, Alignment.BOTTOM_LEFT);
        payTableLay.addComponent(paymentsTable);
        payTableLay.setExpandRatio(paymentsTable, 1);

        callsTable = new FormattedTable();
        callsTable.setSizeFull();
        callsTable.setStyleName(ValoTheme.TABLE_SMALL);
        callsTable.setNullSelectionAllowed(false);
        callsTableLay = new VerticalLayout();
        callsTableLay.setSizeFull();
        callsTableLay.setSpacing(true);
        callsTableLay.setMargin(true);
        callsTableLay.addComponent(plusCallButton);
        callsTableLay.setComponentAlignment(plusCallButton, Alignment.BOTTOM_LEFT);
        callsTableLay.addComponent(callsTable);
        callsTableLay.setExpandRatio(callsTable, 1);

        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabs.addTab(studSearchLay).setCaption(myUI.getMessage(SptMessages.Search));
        tabs.addTab(famTableLay).setCaption(myUI.getMessage(SptMessages.FamilyInfo));
        tabs.addTab(contractTabLay).setCaption(myUI.getMessage(SptMessages.Contract));
        tabs.addTab(payTableLay).setCaption(myUI.getMessage(SptMessages.Payment));
        tabs.addTab(acsGiveTableLay).setCaption(myUI.getMessage(SptMessages.MaterialsGive));
        tabs.addTab(acsReceiveTableLay).setCaption(myUI.getMessage(SptMessages.MaterialsReceive));
        tabs.addTab(callsTableLay).setCaption(myUI.getMessage(SptMessages.Calls));
        tabs.addSelectedTabChangeListener(
                (TabSheet.SelectedTabChangeListener) event -> {
                    if (event.getTabSheet().getSelectedTab() == famTableLay
                            && studDataTable.getValue() != null) {
                        setRelativesTable();
                        prepareNormalMode();
                    } else if (event.getTabSheet().getSelectedTab() == acsGiveTableLay
                            && studDataTable.getValue() != null) {
                        setMaterialsTable(give);
                        prepareNormalMode();
                    } else if (event.getTabSheet().getSelectedTab() == acsReceiveTableLay
                            && studDataTable.getValue() != null) {
                        setMaterialsTable(receive);
                        prepareNormalMode();
                    }
                    if (event.getTabSheet().getSelectedTab() == contractTabLay
                            && studDataTable.getValue() != null) {
                        setInstPlanTable();
                        setDiscountsTable();
                        setCorrectionsTable();
                        setCorrectionsTable();
                        setContractTab((Integer) studDataTable.getValue(),
                                myUI.getUser().getCurrent_year().getId());
                        if (initialPaymentTF.isValid()) {
                            recountInstPlanLabel();
                        }
                        prepareNormalMode();
                        if ((Integer) studDataTable.getContainerProperty(studDataTable.getValue(),
                                Settings.education_status_id).getValue() == 4
                                || (Integer) studDataTable.getContainerProperty(studDataTable.getValue(),
                                Settings.education_status_id).getValue() == 5) {
                            //if student outOf or graduated
                            modifyBtn.setEnabled(false);
                        }
//                    checkForLastContract((Integer) studDataTable.getValue(), myUI.getUser().getCurrent_year().getId());

                    } else if (event.getTabSheet().getSelectedTab() == payTableLay
                            && studDataTable.getValue() != null) {
                        setPaymentsTable();
                        prepareNormalMode();
//                    checkForLastContract((Integer) studDataTable.getValue(), myUI.getUser().getCurrent_year().getId());
                    } else if (event.getTabSheet().getSelectedTab() == callsTableLay
                            && studDataTable.getValue() != null) {
                        setCallsTable();
                        prepareNormalMode();
                    } else if (event.getTabSheet().getSelectedTab() == studSearchLay) {
                        prepareNormalMode();
                    }
                });
        this.setSecondComponent(tabs);
        prepareNormalMode();
    }

    private void buildButtonsLayout() {

        buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);
        buttonsLay.setWidth(Settings.PERCENTS100);

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

        financialHistoryButton = new Button();
        financialHistoryButton.setDescription(myUI.getMessage(SptMessages.FinancialHistory));
        financialHistoryButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        financialHistoryButton.setIcon(FontAwesome.DOLLAR);
        financialHistoryButton.setEnabled(false);
        financialHistoryButton.addClickListener(this);
        buttonsLay.addComponent(financialHistoryButton);

        contLangOG = new OptionGroup();
        contLangOG.setNullSelectionAllowed(true);
        contLangOG.addValueChangeListener(this);
        contLangOG.addItem(myUI.getMessage(SptMessages.LiseContrRu));
        contLangOG.addItem(myUI.getMessage(SptMessages.LiseContrKg));
        contLangOG.addItem(myUI.getMessage(SptMessages.SchoolContrRu));
        contLangOG.addItem(myUI.getMessage(SptMessages.SchoolContrKg));
        contLangOG.addItem(myUI.getMessage(SptMessages.CambridgeOshContrRu));
        contLangOG.addItem(myUI.getMessage(SptMessages.CambridgeContrRu));
        contLangOG.addItem(myUI.getMessage(SptMessages.CambridgeContrEn));

        printButton = new PopupButton(myUI.getMessage(SptMessages.Print));
        printButton.setDescription(myUI.getMessage(SptMessages.Print));
        printButton.setIcon(FontAwesome.PRINT);
        printButton.setImmediate(true);
        printButton.addClickListener(this);
        printButton.setEnabled(false);
        printButton.setContent(contLangOG);
        buttonsLay.addComponent(printButton);
        buttonsLay.setExpandRatio(printButton, 1);
    }

    private void buildStudGridLayout() {
        gridStudLay = new GridLayout(3, 3);
        gridStudLay.setSpacing(true);
        gridStudLay.setMargin(true);
        gridStudLay.setWidth("95%");

        buildPhotoLayout();
        buildFieldsLayout1();
        buildFieldsLayout2();
        buildContractLayout();

        gridStudLay.addComponent(buttonsLay);
        gridStudLay.addComponent(photoEmb, 0, 1);
        gridStudLay.addComponent(photoUpl, 0, 2);
        gridStudLay.addComponent(fieldsLay1, 1, 1, 1, 2);
        gridStudLay.addComponent(fieldsLay2, 2, 1, 2, 2);
        gridStudLay.setRowExpandRatio(1, 1);
        gridStudLay.setColumnExpandRatio(1, 1);
        gridStudLay.setColumnExpandRatio(2, 1);
    }

    private void buildPhotoLayout() {
        photoEmb = new Embedded();
        photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoEmb.setImmediate(true);
        photoEmb.setHeight("100px");

        photoUpl = createUpload(myUI.getMessage(SptMessages.Upload), true);
    }

    private void buildFieldsLayout1() {
        fieldsLay1 = new FormLayout();
        fieldsLay1.setSpacing(false);

        loginTF = new TextField(myUI.getMessage(SptMessages.Id));
        loginTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        loginTF.setWidth(Settings.PERCENTS100);
        loginTF.setEnabled(false);
        fieldsLay1.addComponent(loginTF);

        nameTF = new TextField(myUI.getMessage(SptMessages.FirstName));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        nameTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        nameTF.setWidth(Settings.PERCENTS100);
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotificationWrongValue), 1, 100, false));
        fieldsLay1.addComponent(nameTF);

        surnameTF = new TextField(myUI.getMessage(SptMessages.LastName));
        surnameTF.setRequired(true);
        surnameTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        surnameTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        surnameTF.setWidth(Settings.PERCENTS100);
        surnameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotificationWrongValue), 1, 100, false));
        fieldsLay1.addComponent(surnameTF);

        middleNameTF = new TextField(myUI.getMessage(SptMessages.MiddleName));
        middleNameTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        middleNameTF.setWidth(Settings.PERCENTS100);
        fieldsLay1.addComponent(middleNameTF);
    }

    private void buildFieldsLayout2() {
        fieldsLay2 = new FormLayout();
        fieldsLay2.setSpacing(false);

        genderCB = new ComboBox(myUI.getMessage(SptMessages.Gender));
        genderCB.setNullSelectionAllowed(false);
        genderCB.setRequired(true);
        genderCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        genderCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        genderCB.setWidth(Settings.PERCENTS100);
        genderCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        genderCB.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            genderCB.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbGender, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        fieldsLay2.addComponent(genderCB);

        birthDate = new DateField(myUI.getMessage(SptMessages.DateOfBirth));
        birthDate.setRangeEnd(new Date());
        birthDate.setWidth(Settings.PERCENTS100);
        birthDate.setStyleName(ValoTheme.DATEFIELD_TINY);
        birthDate.setRequired(true);
        birthDate.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        birthDate.setDateFormat(Settings.datePattern);
        birthDate.setValue(new Date());
        fieldsLay2.addComponent(birthDate);

        classCB = new ComboBox(myUI.getMessage(SptMessages.ClassName));
        classCB.setNullSelectionAllowed(false);
        classCB.setRequired(true);
        classCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        classCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        classCB.setWidth(Settings.PERCENTS100);
        classCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        classCB.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbClassName dbcn = new DbClassName();
            dbcn.connect();
            classCB.setContainerDataSource(dbcn.execClass_sel(myUI, myUI.getUser().getSchool_id()));
            dbcn.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        classCB.addValueChangeListener(this);
        fieldsLay2.addComponent(classCB);

        statusCB = new ComboBox(myUI.getMessage(SptMessages.EducationStatus));
        statusCB.setNullSelectionAllowed(false);
        statusCB.setEnabled(false);
        statusCB.setRequired(true);
        statusCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        statusCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        statusCB.setWidth(Settings.PERCENTS100);
        statusCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        statusCB.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            statusCB.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbEducationStatus, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        fieldsLay2.addComponent(statusCB);
    }

    private void buildContractLayout() {
        contractLay = new VerticalLayout();
        contractLay.setMargin(true);

        contractLab = new Label();
        contractLab.setContentMode(ContentMode.HTML);
        contractLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        contractLab.setValue(myUI.getMessage(SptMessages.Contract) + ":");

        discountLab = new Label();
        discountLab.setContentMode(ContentMode.HTML);
        discountLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        discountLab.setValue(myUI.getMessage(SptMessages.Discount) + ":");

        correctionLab = new Label();
        correctionLab.setContentMode(ContentMode.HTML);
        correctionLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        correctionLab.setValue(myUI.getMessage(SptMessages.Correction) + ":");

        debtLab = new Label();
        debtLab.setContentMode(ContentMode.HTML);
        debtLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        debtLab.setValue(myUI.getMessage(SptMessages.PreviousYearDebt) + ":");

        netLab = new Label();
        netLab.setContentMode(ContentMode.HTML);
        netLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        netLab.setValue(myUI.getMessage(SptMessages.Net) + ":");

        paidLab = new Label();
        paidLab.setContentMode(ContentMode.HTML);
        paidLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        paidLab.setValue(myUI.getMessage(SptMessages.Paid) + ":");

        leftLab = new Label();
        leftLab.setContentMode(ContentMode.HTML);
        leftLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        leftLab.setValue(myUI.getMessage(SptMessages.Left) + ":");

        planDebt = new Label();
        planDebt.setContentMode(ContentMode.HTML);
        planDebt.setStyleName(ValoTheme.LABEL_SUCCESS);
        planDebt.setValue(myUI.getMessage(SptMessages.InstPlanDebt) + ":");

        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.prmContractInfo)) {
            contractLay.addComponent(contractLab);
            contractLay.setComponentAlignment(contractLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(discountLab);
            contractLay.setComponentAlignment(discountLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(correctionLab);
            contractLay.setComponentAlignment(contractLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(debtLab);
            contractLay.setComponentAlignment(debtLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(netLab);
            contractLay.setComponentAlignment(netLab, Alignment.BOTTOM_LEFT);

        }
        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":"
                + Settings.prmContractInfoLeftDebt)) {
            contractLay.addComponent(leftLab);
            contractLay.setComponentAlignment(leftLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(planDebt);
            contractLay.setComponentAlignment(planDebt, Alignment.BOTTOM_LEFT);
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source.getId() != null && source.getId().equals(Settings.cancel_upload_button)) {
            if (photoUpl != null) {
                photoUpl.interruptUpload();
            }
        } else if (source == modifyBtn) {
            if (studDataTable.getValue() != null) {
                if ((tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                        || tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent())
                        && debt > 0.1 && contract_amount == 0.0 && !currentUser.hasRole(Settings.rnAdmin)) {
                    Notification.show(myUI.getMessage(SptMessages.OperationNotAllowedDueToDebt),
                            Notification.Type.WARNING_MESSAGE);
                } else {
                    isNew = false;
                    photoUpl.setEnabled(true);
                    fillFields();
                    prepareModificationMode();
                    classCB.setEnabled(false);
                    addRowIfTableEmpty();
                    if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                            && contractCB.getValue() == null) {
                        isNewContract = true;
                    }
                }
            }
        } else if (source == plusRelButton) {
            addRelativeItem();
        } else if (source == plusMatGiveButton) {
            addAccessoriesItem(give);
        } else if (source == plusMatReceiveButton) {
            addAccessoriesItem(receive);
        } else if (source == plusPayButton) {
            addPaymentsItem();
        } else if (source == plusCallButton) {
            addCallsItem();
        } else if (source == divideBtn) {
            if (divideTF.getValue() != null && !divideTF.getValue().equals("")) {
                recount();
                addInstallmentPlanItem(true);
                if (initialPaymentTF.isValid()) {
                    recountInstPlanLabel();
                }
            }
        } else if (source == plusInstButton) {
            addInstallmentPlanItem(false);
        } else if (source == plusDiscButton) {
            if (discCounter < 3) {
                addDiscountsItem();
            } else {
                Notification.show(myUI.getMessage(SptMessages.OnlyThreeDiscountsAllowed),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == plusCorrectionButton) {
            addCorrectionsItem();
        } else if (source == createBtn) {
            isNew = true;
            tabs.getTab(famTableLay).setEnabled(true);
            tabs.setSelectedTab(famTableLay);
            clearFields();
            prepareModificationMode();
            statusCB.setValue(1);
            loginTF.focus();
            delRelIds.clear();
            plusRelButton.click();
            clearContractInfo();
        } else if (source == deleteBtn) {
            if (studDataTable.getValue() != null) {
                ConfirmDialog.show(myUI, myUI.getMessage(SptMessages.Question),
                        myUI.getMessage(SptMessages.ConfirmStudentDeletion)
                                + " " + studDataTable.getContainerProperty(studDataTable.getValue(),
                                myUI.getMessage(SptMessages.FirstName)).getValue().toString()
                                + " " + studDataTable.getContainerProperty(studDataTable.getValue(),
                                myUI.getMessage(SptMessages.LastName)).getValue().toString()
                                + "?",
                        myUI.getMessage(SptMessages.Yes),
                        myUI.getMessage(SptMessages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                execDelete();
                            }
                        });
            }
        } else if (source.getId() != null && source.getId().equals(Settings.download_button)) {
            if (downloader == null) {
                downloader = new SimpleFileDownloader();
                addExtension(downloader);
            }
            downloader.setFileDownloadResource(getFileStream(new File(Settings.PATH_TO_UPLOADS
                    + ((Attachment) source.getData()).getUnique_name())));
            downloader.download();
        } else if (source.getId() != null && source.getId().equals(myUI.getMessage(SptMessages.Invoice))) {
            InvoiceInfoPdf iip = new InvoiceInfoPdf();
            iip.setLogin(loginTF.getValue());
            iip.setClass_name(classCB.getContainerProperty(classCB.getValue(),
                    myUI.getMessage(SptMessages.Title)).getValue().toString());
            iip.setStudentFullName(nameTF.getValue() + " " + surnameTF.getValue()
                    + " " + middleNameTF.getValue());
            iip.setWhoPaidFullName(((TextField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.WhoPaid)).getValue()).getValue());
            iip.setPayment_date(((DateField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Date)).getValue()).getValue());
            iip.setAmount((Double) (((TextField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue()));
            iip.setKurs((Double) (((TextField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Rate)).getValue()).getPropertyDataSource().getValue()));
            iip.setSchool_name(myUI.getUser().getSchool_name());
            iip.setPaymentCategoryId((Integer) ((ComboBox) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getValue());
            iip.setPayment_type(((ComboBox) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.PaymentType)).getValue())
                    .getContainerProperty(((ComboBox) paymentsTable.getContainerProperty(source.getData(),
                                    myUI.getMessage(SptMessages.PaymentType)).getValue()).getValue(),
                            myUI.getMessage(SptMessages.Title)).getValue().toString());
            try {
                DbStudentPayment dbsp = new DbStudentPayment();
                dbsp.connect();
                iip.setOrder_number(dbsp.getOrderNum((String) source.getData()));
                dbsp.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            try {
                DbSchool dbs = new DbSchool();
                dbs.connect();
                iip.setScl_logo(dbs.execGet_logo(loginTF.getValue()));
                dbs.close();
            } catch (Exception ignored) {
            }
            if (iip.getScl_logo() != null) {
                new InvoicePDF(myUI, iip);
            } else {
                Notification.show(myUI.getMessage(SptMessages.NoSchoolLogo),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == saveBtn) {
            try {
                if (validate(horSplitPanel)) {
                    if (validateRelativesTable(relativesTable)) {
                        if (validateAcsGiveTable(acsGiveTable)) {
                            if (validateAcsReceiveTable(acsReceiveTable)) {
                                if (validateContractsTab(contractTabLay) && validateDiscountsTable() &&
                                        validateCorrectionsTable() && validateInstallmentTable()) {
                                    if (validatePaymentsTable(paymentsTable)) {
                                        DbStudent dbst = new DbStudent();
                                        dbst.connect();
                                        if (isNew) {
                                            loginTF.setValue(generateStudId());
                                            Student student = getStudent(0);
                                            int id = dbst.exec_insert(student);
                                            if (id != 0) {
                                                insertNewStOrder(id);
                                                if (relativesTable.isEnabled()) {
                                                    saveRelatives(id);
                                                }
                                                addDataContainerItem(id);
                                                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                                prepareNormalMode();
                                                eduStatCont.getContainerProperty(1, Settings.count)
                                                        .setValue(((Integer) eduStatCont.getContainerProperty(1,
                                                                Settings.count).getValue()) + 1);
                                                eduStatCont.getContainerProperty(6, Settings.count)
                                                        .setValue(((Integer) eduStatCont.getContainerProperty(6, Settings.count)
                                                                .getValue()) + 1);
                                                repaint();
                                            } else {
                                                Notification.show(myUI.getMessage(SptMessages.CanNotSaveIdNumber),
                                                        Notification.Type.WARNING_MESSAGE);
                                                prepareModificationMode();
                                            }
                                        } else {
                                            if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()) {
                                                int status = 0;
                                                try {
                                                    status = dbst.exec_update(
                                                            getStudent((Integer) studDataTable.getValue()));
                                                } catch (Exception e) {
                                                    logger.error(e);
                                                    logger.catching(e);
                                                }
                                                if (status != 0) {
                                                    updateDataContainer();
                                                    saveRelatives((Integer) studDataTable.getValue());
                                                    setRelativesTable();
                                                    prepareNormalMode();
                                                    Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                            Notification.Type.HUMANIZED_MESSAGE);
                                                } else {
                                                    Notification.show(myUI.getMessage(SptMessages.CanNotSaveIdNumber),
                                                            Notification.Type.WARNING_MESSAGE);
                                                }

                                            } else if (tabs.getSelectedTab() == tabs.getTab(studSearchLay).getComponent()) {
                                                int status = 0;
                                                try {
                                                    status = dbst.exec_update(
                                                            getStudent((Integer) studDataTable.getValue()));
                                                    Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                            Notification.Type.HUMANIZED_MESSAGE);
                                                } catch (Exception e) {
                                                    logger.error(e);
                                                    logger.catching(e);
                                                }
                                                if (status != 0) {
                                                    updateDataContainer();
                                                    prepareNormalMode();
                                                    Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                            Notification.Type.HUMANIZED_MESSAGE);
                                                } else {
                                                    Notification.show(myUI.getMessage(SptMessages.CanNotSaveIdNumber),
                                                            Notification.Type.WARNING_MESSAGE);
                                                }

                                            } else if (tabs.getSelectedTab() == tabs.getTab(acsGiveTableLay).getComponent()) {
                                                execDeleteAccessoriesFromDb((Integer) studDataTable.getValue(),
                                                        myUI.getUser().getCurrent_year().getId(), give);
                                                insertAccessoriesToDb((Integer) studDataTable.getValue(), give);
                                                setMaterialsTable(give);
                                                prepareNormalMode();
                                                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            } else if (tabs.getSelectedTab() == tabs.getTab(acsReceiveTableLay).getComponent()) {
                                                execDeleteAccessoriesFromDb((Integer) studDataTable.getValue(),
                                                        myUI.getUser().getCurrent_year().getId(), receive);
                                                insertAccessoriesToDb((Integer) studDataTable.getValue(), receive);
                                                setMaterialsTable(receive);
                                                prepareNormalMode();
                                                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            } //pressed save button on contract tab
                                            else if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
                                                insertDiscounts();
                                                insertInitialPayment((Integer) studDataTable.getValue());
                                                insertContractToDb();
                                                execDeleteInstPlanFromDb((Integer) studDataTable.getValue(),
                                                        myUI.getUser().getCurrent_year().getId());
                                                insertInstPlanToDb((Integer) studDataTable.getValue());
                                                insertCorrections();
                                                setInstPlanTable();
                                                recount();
                                                updateNetPaymentDb(ttl_payment, (Integer) studDataTable.getValue(),
                                                        myUI.getUser().getCurrent_year().getId());
                                                prepareNormalMode();
                                                insertNewStCtrOrder();
                                                updateStudEduStatus();
                                                fileName = null;
                                                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            } //pressed save button on payments tab
                                            else if (tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent()) {
                                                insertPayments((Integer) studDataTable.getValue());
                                                setPaymentsTable();
                                                recount();
                                                updateNetPaymentDb(ttl_payment, (Integer) studDataTable.getValue(),
                                                        myUI.getUser().getCurrent_year().getId());
                                                prepareNormalMode();
                                                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            } else if (tabs.getSelectedTab() == tabs.getTab(callsTableLay).getComponent()) {
                                                //save calls
                                                insertCalls((Integer) studDataTable.getValue());
                                                setCallsTable();
                                                prepareNormalMode();
                                                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            }
                                        }
                                        dbst.close();
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == cancelBtn) {
            isNew = false;
            clearFields();
            delPayIds.clear();
            delCallIds.clear();
            delDiscIds.clear();
            delCorrectionIds.clear();
            delRelIds.clear();
            if (studDataTable.getValue() != null) {
                fillFields();
            }
            prepareNormalMode();
            if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
                recountInstPlanLabel();
            }
            fileName = null;
        } else if (source == printButton) {
        } else if (source == financialHistoryButton) {
            if (studDataTable.getValue() != null) {
                int st_id = (Integer) studDataTable.getValue();
                myUI.addWindow(new StudentFinancialHistoryWindow(myUI, myUI.getMessage(SptMessages.FinancialHistory) + " - " +
                        studDataTable.getContainerProperty(st_id, myUI.getMessage(SptMessages.FirstName)).getValue() + " " +
                        studDataTable.getContainerProperty(st_id, myUI.getMessage(SptMessages.LastName)).getValue() + "; " +
                        studDataTable.getContainerProperty(st_id, myUI.getMessage(SptMessages.ClassName)).getValue(), st_id));
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()) {
            delRelIds.add((String) source.getData());
            relativesTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(callsTableLay).getComponent()) {
            delCallIds.add((String) source.getData());
            callsTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(acsGiveTableLay).getComponent()) {
            acsGiveTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(acsReceiveTableLay).getComponent()) {
            acsReceiveTable.removeItem(event.getButton().getData().toString());
        } else if (source.getId() != null
                && source.getId().equals(Settings.dbStudentInstallment)) {
            installmentTable.removeItem(event.getButton().getData().toString());
            if (initialPaymentTF.isValid()) {
                recountInstPlanLabel();
            }
        } else if (source.getId() != null
                && source.getId().equals(Settings.dbStudentDiscount)) {
            discCounter--;
            StudentDiscount sd = new StudentDiscount();
            sd.setId(source.getData().toString());
            Button b = (Button) ((HorizontalLayout) discountsTable.getContainerProperty(sd.getId(),
                    myUI.getMessage(SptMessages.Document)).getValue()).getComponent(0);
            if (b.getData() != null) {
                sd.setAttachmentUniqueName(((Attachment) b.getData()).getUnique_name());
            }
            delDiscIds.add(sd);
            discountsTable.removeItem(event.getButton().getData().toString());
            if (initialPaymentTF.isValid()) {
                recountInstPlanLabel();
            }
        } else if (source.getId() != null
                && source.getId().equals(Settings.dbStudentCorrection)) {
            delCorrectionIds.add(source.getData().toString());
            correctionsTable.removeItem(event.getButton().getData().toString());
            if (initialPaymentTF.isValid()) {
                recountInstPlanLabel();
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent() && source.getCaption() == null) {
            AccTransaction tr;
            delPayIds.add(source.getData().toString());
            if (paymentsTable.getContainerProperty(source.getData().toString(), Settings.crud_status).getValue().toString()
                    .equals(myUI.getMessage(SptMessages.Update))) {
                tr = insertTestPayments((Date) paymentsTable.getContainerProperty(source.getData().toString(),
                        Settings.old_date).getValue(), source.getData().toString());
            } else {
                tr = insertTestPayments(((DateField) paymentsTable.getContainerProperty(source.getData().toString(),
                        myUI.getMessage(SptMessages.Date)).getValue()).getValue(), source.getData().toString());
            }
            if (tr == null) {
                paymentsTable.removeItem(event.getButton().getData().toString());
            } else {
                delPayIds.remove(source.getData().toString());
                Notification.show(myUI.getMessage(SptMessages.LowBalance) + Settings.dFormat.format(tr.getOverLimit())
                                + " $ (" + Settings.df.format(tr.getDate()) + ")",
                        Notification.Type.ERROR_MESSAGE);
            }
        }
    }

    private StreamResource getFileStream(File inputFile) {

        StreamResource.StreamSource source = () -> {

            InputStream input = null;
            try {
                input = new FileInputStream(inputFile);
            } catch (FileNotFoundException ex) {
                logger.error(ex);
                logger.catching(ex);
            }
            return input;

        };
        return new StreamResource(source, inputFile.getName());
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == studDataTable) {
            if (studDataTable.getItem(studDataTable.getValue()) != null) {
                clearFields();
                fillFields();
                recount();
                printButton.setEnabled(true);
                financialHistoryButton.setEnabled(true);
                setContractCb(contr_id);
                initialPaymentTF.setData(null);
            }
        } else if (property == classCB) {
            if (classCB.getValue() != null) {
                photoUpl.setEnabled(true);
                if (isNew) {
                    loginTF.setValue(generateStudId());
                }
            } else {
                photoUpl.setEnabled(false);
            }
        } else if (property == contractCB) {
            if (initialPaymentTF.isValid()) {
                recountInstPlanLabel();
            }
        } else if (property == initialPaymentTF && initialPaymentTF.isValid()) {
            try {
                double amount = 0.0;
                if (initialPaymentTF.getValue() != null && !initialPaymentTF.getValue().equals("")) {
                    amount = Double.parseDouble(initialPaymentTF.getValue());
                }
                if (amount >= 0 && initialPaymentTF.getData() != null) {
                    if (amount <= ((StudentPayment) initialPaymentTF.getData()).getAmount()) {
                        DbAccTransactions dbTr = new DbAccTransactions();
                        dbTr.connect();
                        AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool_id(),
                                ((StudentPayment) initialPaymentTF.getData()).getModification_date(),
                                ((StudentPayment) initialPaymentTF.getData()).getAmount(), amount, 1);
                        if (tr != null) {
                            initialPaymentTF.removeAllValidators();
                            initialPaymentTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.LowBalance) + Settings.dFormat.format(tr.getOverLimit())
                                    + " $ (" + Settings.df.format(tr.getDate()) + ")", tr.getLimit(), null));
                            Notification.show(myUI.getMessage(SptMessages.LowBalance) + Settings.dFormat.format(tr.getOverLimit())
                                            + " $ (" + Settings.df.format(tr.getDate()) + ")",
                                    Notification.Type.ERROR_MESSAGE);
                            initialPaymentTF.setRequired(true);
                            initialPaymentTF.setRequiredError(myUI.getMessage(SptMessages.LowBalance) + Settings.dFormat.format(tr.getOverLimit())
                                    + " $ (" + Settings.df.format(tr.getDate()) + ")");
                        } else {
                            initialPaymentTF.setRequired(false);
                        }
                        dbTr.close();
                    } else {
                        initialPaymentTF.removeAllValidators();
                        initialPaymentTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                    }
                } else {
                    initialPaymentTF.removeAllValidators();
                    initialPaymentTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                }
            } catch (Exception e) {
                initialPaymentTF.removeAllValidators();
                initialPaymentTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                logger.error(e);
                logger.catching(e);
            }
            if (initialPaymentTF.isValid()) {
                recountInstPlanLabel();
            }
        } else if (property == installmentTable) {
            if (initialPaymentTF.isValid()) {
                recountInstPlanLabel();
            }
        } else if (property == optionGroup) {
            setStudDataTable(property.getValue().toString());
            repaint();
        } else if (((AbstractField<?>) property).getId() != null
                && ((AbstractField<?>) property).getId().equals(myUI.getMessage(SptMessages.Payments))) {
            Object itemId = ((AbstractField<?>) property).getData();
            if (((ComboBox) paymentsTable.getContainerProperty(itemId,
                    myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getValue() != null
                    && ((TextField) paymentsTable.getContainerProperty(itemId,
                    myUI.getMessage(SptMessages.Amount)).getValue()).getValue() != null
                    && ((DateField) paymentsTable.getContainerProperty(itemId,
                    myUI.getMessage(SptMessages.Date)).getValue()).getValue() != null) {
                AccTransaction tr = null;
                TextField tf = (TextField) paymentsTable.getContainerProperty(itemId,
                        myUI.getMessage(SptMessages.Amount)).getValue();
                if ((Integer) ((ComboBox) paymentsTable.getContainerProperty(itemId,
                        myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getValue() != 3
                        && (paymentsTable.getContainerProperty(itemId,
                        Settings.crud_status).getValue().equals(myUI.getMessage(SptMessages.Insert))
                        || DateUtils.truncate(((DateField) paymentsTable.getContainerProperty(itemId,
                        myUI.getMessage(SptMessages.Date)).getValue()).getValue(), java.util.Calendar.DAY_OF_MONTH).compareTo(
                        (Date) paymentsTable.getContainerProperty(itemId, Settings.old_date).getValue()) == 0)
                        && (Double) ((TextField) paymentsTable.getContainerProperty(itemId,
                        myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue()
                        > (Double) paymentsTable.getContainerProperty(itemId, Settings.old_amount).getValue()) {
                    tf.removeAllValidators();
                    tf.addValidator(new DoubleRangeValidator(
                            myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                } else {
                    if (paymentsTable.getContainerProperty(itemId, Settings.old_date).getValue() != null
                            && DateUtils.truncate(((DateField) paymentsTable.getContainerProperty(itemId,
                            myUI.getMessage(SptMessages.Date)).getValue()).getValue(), java.util.Calendar.DAY_OF_MONTH).compareTo(
                            (Date) paymentsTable.getContainerProperty(itemId, Settings.old_date).getValue()) != 0) {
                        tr = insertTestPayments((Date) paymentsTable.getContainerProperty(itemId,
                                Settings.old_date).getValue(), "");
                    }
                    if (tr == null) {
                        tr = insertTestPayments(((DateField) paymentsTable.getContainerProperty(itemId,
                                myUI.getMessage(SptMessages.Date)).getValue()).getValue(), "");
                    }
                    tf.removeAllValidators();
                    if (tr != null) {
                        if ((Integer) ((ComboBox) paymentsTable.getContainerProperty(itemId,
                                myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getValue() == 3) {
                            tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.LowBalance)
                                    + Settings.dFormat.format(tr.getOverLimit())
                                    + " $ (" + Settings.df.format(tr.getDate()) + ")", 0.1,
                                    (Double) tf.getPropertyDataSource().getValue() - tr.getOverLimit()));
                        } else {
                            tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.LowBalance)
                                    + Settings.dFormat.format(tr.getOverLimit())
                                    + " $ (" + Settings.df.format(tr.getDate()) + ")",
                                    (Double) tf.getPropertyDataSource().getValue() + tr.getOverLimit(), null));
                        }
                        Notification.show(myUI.getMessage(SptMessages.LowBalance) + Settings.dFormat.format(tr.getOverLimit())
                                        + " $ (" + Settings.df.format(tr.getDate()) + ")",
                                Notification.Type.ERROR_MESSAGE);
                    } else {
                        tf.addValidator(new DoubleRangeValidator(
                                myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
                    }
                }
            }
        } else if (((AbstractField<?>) property).getId() != null && (((AbstractField<?>) property).getId().equals(Settings.KGS) ||
                ((AbstractField<?>) property).getId().equals(myUI.getMessage(SptMessages.Rate)))) {
            Object itemId = ((AbstractField<?>) property).getData();
            TextField tfAmount = (TextField) paymentsTable.getContainerProperty(itemId,
                    myUI.getMessage(SptMessages.Amount)).getValue();
            TextField tfRate = (TextField) paymentsTable.getContainerProperty(itemId,
                    myUI.getMessage(SptMessages.Rate)).getValue();
            TextField tfKGS = (TextField) paymentsTable.getContainerProperty(itemId, Settings.KGS).getValue();
            if (tfRate.isValid() && tfKGS.isValid()
                    && (Double) tfRate.getPropertyDataSource().getValue() != 0.0
                    && tfKGS.getPropertyDataSource().getValue() != null
                    && (Double) tfKGS.getPropertyDataSource().getValue() != 0.0) {
                tfAmount.getPropertyDataSource().setValue((Double) tfKGS.getPropertyDataSource().getValue() /
                        (Double) tfRate.getPropertyDataSource().getValue());
                tfKGS.getPropertyDataSource().setValue(0.0);
            }
        } else if (property == contLangOG && contLangOG.getValue() != null) {
            if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                    && studDataTable.getValue() != null) {
                if (contractCB.getValue() != null) {
                    StudentInfoPdf studInfo = new StudentInfoPdf();
                    try {
                        DbStudentInfoPdf dbs = new DbStudentInfoPdf();
                        dbs.connect();
                        studInfo = dbs.execSQL((Integer) studDataTable.getValue());
                        dbs.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    if (contractCB.getValue() != null) {
                        studInfo.setCtr_contract_sum((Double) (contractCB.getContainerProperty(contractCB.getValue(),
                                myUI.getMessage(SptMessages.Amount)).getValue()));
                    }
                    studInfo.setCtr_debt(debt);
                    if (discountsTable.size() > 0) {
                        Iterator<?> iter = discountsTable.getItemIds().iterator();
                        StringBuilder allDisc = new StringBuilder();
                        String dis;
                        double count_amount = (Double) (contractCB.getContainerProperty(contractCB.getValue(),
                                myUI.getMessage(SptMessages.Amount)).getValue());
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            dis = ((((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                                            myUI.getMessage(SptMessages.Title)).getValue().toString()));
                            dis = dis.substring(0, dis.indexOf(" - "));
                            allDisc.append(dis);

                            if (((Integer) ((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 1)
                                    || ((Integer) ((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 3)) {
                                allDisc.append(" - ").append(((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue())
                                        .getPropertyDataSource().getValue().toString()).append("% (").append(Settings.dFormat.format(count_amount
                                        * ((Double) ((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue())
                                        .getPropertyDataSource().getValue()) / 100)).append("$)");
                                count_amount -= count_amount
                                        * ((Double) ((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue())
                                        .getPropertyDataSource().getValue()) / 100;
                            } else if (((Integer) ((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 2)
                                    || ((Integer) ((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 4)) {
                                allDisc.append("(").append(Settings.dFormat.format(((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue())
                                        .getPropertyDataSource().getValue())).append("$)");
                                count_amount -= (Double) ((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue())
                                        .getPropertyDataSource().getValue();
                            }

                            if (iter.hasNext()) {
                                allDisc.append(", ");
                            }
                        }
                        studInfo.setCtr_discountStr(allDisc.toString());
                        studInfo.setCtr_discount_percentage(discountsStr.toString());
                    }
                    if (correctionsTable.size() > 0) {
                        Iterator<?> iter = correctionsTable.getItemIds().iterator();
                        StringBuilder allCorrections = new StringBuilder();
                        String dis;
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            dis = ((((ComboBox) correctionsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) correctionsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                                            myUI.getMessage(SptMessages.Title)).getValue().toString()));
                            allCorrections.append(dis);
                            allCorrections.append(" (").append(Settings.dFormat.format(((TextField) correctionsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue())
                                    .getPropertyDataSource().getValue())).append("$)");
                            if (iter.hasNext()) {
                                allCorrections.append(", ");
                            }
                        }
                        studInfo.setCtr_Correction(allCorrections.toString());
                    }
                    studInfo.setCtr_init_payment(instFirstPay);
                    studInfo.setCtr_to_pay(toPay);
                    studInfo.setCtr_ttl_left_sum(ttl_left);
                    if (studInfo.getRel_full_name() != null && studInfo.getRel_name() != null
                            && studInfo.getRel_name_dec() != null) {
                        if (studInfo.getScl_address() != null && studInfo.getScl_bank() != null
                                && studInfo.getScl_bank_account() != null && studInfo.getScl_city() != null
                                && studInfo.getScl_dir_f_name() != null && studInfo.getScl_inn() != null
                                && studInfo.getScl_name_ru() != null && studInfo.getScl_phone() != null) {
                            saveBtn.click();
                            if (contLangOG.getValue().toString().equals(myUI.getMessage(SptMessages.LiseContrRu))) {
                                new ContractLisePdfRu(myUI, studInfo, instPlanCont);
                                contLangOG.setValue(null);
                            } else if (contLangOG.getValue().toString().equals(myUI.getMessage(SptMessages.LiseContrKg))) {
                                new ContractLisePdfKg(myUI, studInfo, instPlanCont);
                                contLangOG.setValue(null);
                            } else if (contLangOG.getValue().toString().equals(myUI.getMessage(SptMessages.SchoolContrRu))) {
                                new ContractSchoolPdfRu(myUI, studInfo, instPlanCont);
                                contLangOG.setValue(null);
                            } else if (contLangOG.getValue().toString().equals(myUI.getMessage(SptMessages.SchoolContrKg))) {
                                new ContractSchoolPdfKg(myUI, studInfo, instPlanCont);
                                contLangOG.setValue(null);
                            } else if (contLangOG.getValue().toString().equals(myUI.getMessage(SptMessages.CambridgeOshContrRu))) {
                                new ContractCambridgeOshPdfRu(myUI, studInfo, instPlanCont);
                                contLangOG.setValue(null);
                            } else if (contLangOG.getValue().toString().equals(myUI.getMessage(SptMessages.CambridgeContrRu))) {
                                new ContractCambridgePdfRu(myUI, studInfo, instPlanCont);
                                contLangOG.setValue(null);
                            } else if (contLangOG.getValue().toString().equals(myUI.getMessage(SptMessages.CambridgeContrEn))) {
                                new ContractCambridgePdfEn(myUI, studInfo, instPlanCont);
                                contLangOG.setValue(null);
                            }

                        } else {
                            Notification.show(myUI.getMessage(SptMessages.FillSchoolInfo),
                                    Notification.Type.WARNING_MESSAGE);
                            contLangOG.setValue(null);
                        }
                    } else {
                        Notification.show(myUI.getMessage(SptMessages.FillRelativeInfo),
                                Notification.Type.WARNING_MESSAGE);
                        contLangOG.setValue(null);
                    }
                } else {
                    Notification.show(myUI.getMessage(SptMessages.SelectContract),
                            Notification.Type.WARNING_MESSAGE);
                    contLangOG.setValue(null);
                }
            } else {
                Notification.show(myUI.getMessage(SptMessages.SelectContractTab),
                        Notification.Type.WARNING_MESSAGE);
                contLangOG.setValue(null);
            }

        } else if (property instanceof TextField && property != nameTF && property != loginTF
                && property != surnameTF && property != middleNameTF && property != divideTF
                && property != initialPaymentTF && ((TextField) property).getDescription()
                .equals(myUI.getMessage(SptMessages.Amount))) {
            if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
                if (initialPaymentTF.isValid()) {
                    recountInstPlanLabel();
                }
            }
        } else if (property instanceof TextField && property != nameTF && property != loginTF
                && property != surnameTF && property != middleNameTF && property != divideTF
                && property != initialPaymentTF && (((TextField) property).getDescription()
                .equals(myUI.getMessage(SptMessages.DiscountAmount))
                || ((TextField) property).getDescription().equals(myUI.getMessage(SptMessages.CorrectionAmount)))) {
            if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
                if (initialPaymentTF.isValid()) {
                    recountInstPlanLabel();
                }
            }
        } else if (property instanceof DateField && property != birthDate
                && property != currDate) {
            if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
            }
        } else if (property instanceof CheckBox) {
            if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()) {
                familyTableCheck((Boolean) property.getValue(), property);
            }
        } else if (property instanceof ComboBox && ((ComboBox) property).getId() != null
                && ((ComboBox) property).getId().equals(Settings.discount_type_id)) {
            checkDiscountsTable(property);
        } else if (property instanceof ComboBox && ((ComboBox) property).getId() != null
                && ((ComboBox) property).getId().equals(Settings.correction_type_id)) {
            recountInstPlanLabel();
        }
    }

    private void prepareModificationMode() {
        if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()
                || tabs.getSelectedTab() == tabs.getTab(studSearchLay).getComponent()) {
            nameTF.setEnabled(true);
            surnameTF.setEnabled(true);
            middleNameTF.setEnabled(true);
            genderCB.setEnabled(true);
            birthDate.setEnabled(true);
            classCB.setEnabled(true);
        }
        contractCB.setEnabled(true);
        if (currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeOldTransactions)
                || initialPaymentTF.getData() == null
                || DateUtils.truncate(((StudentPayment) initialPaymentTF.getData()).getModification_date(),
                java.util.Calendar.DAY_OF_MONTH).compareTo(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH)) == 0) {
            initialPaymentTF.setEnabled(true);
        }
        discountsTable.setEnabled(true);
        correctionsTable.setEnabled(true);
        if (currentUser.isPermitted(Settings.discountsTable + ":" + Settings.actAdd)) {
            plusDiscButton.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.correctionsTable + ":" + Settings.actAdd)) {
            plusCorrectionButton.setEnabled(true);
        }
        divideBtn.setEnabled(true);
        divideTF.setEnabled(true);
        plusInstButton.setEnabled(true);
        installmentTable.setEnabled(true);
        currDate.setEnabled(true);
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        studDataTable.setEnabled(false);
        plusRelButton.setEnabled(true);
        plusMatGiveButton.setEnabled(true);
        plusMatReceiveButton.setEnabled(true);
        if (currentUser.isPermitted(Settings.paymentsTab + ":" + Settings.actAdd)) {
            plusPayButton.setEnabled(true);
        }
        plusCallButton.setEnabled(true);
        relativesTable.setEnabled(true);
        paymentsTable.setEnabled(true);
        callsTable.setEnabled(true);
        acsGiveTable.setEnabled(true);
        acsReceiveTable.setEnabled(true);

        for (Object next : ((IndexedContainer) installmentTable
                .getContainerDataSource()).getItemIds()) {
            if ((Integer) installmentTable.getContainerProperty(next, Settings.status_id).getValue() == 0) {
                ((Button) installmentTable.getContainerProperty(next, Settings.button).getValue()).setEnabled(false);
                ((DateField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Date)).getValue()).setEnabled(false);
                ((TextField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue()).setEnabled(false);
            } else {
                ((Button) installmentTable.getContainerProperty(next, Settings.button)
                        .getValue()).setEnabled(true);
                ((DateField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Date)).getValue()).setEnabled(true);
                ((TextField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue()).setEnabled(true);
            }

        }
        for (Component component : tabs) {
            TabSheet.Tab tab = tabs.getTab(component);
            tab.setEnabled(tab == tabs.getTab(tabs.getSelectedTab()));
        }
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (tabs.getSelectedTab() == tabs.getTab(studSearchLay).getComponent()) {
            if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.actDelete)) {
                deleteBtn.setEnabled(true);
            }
        } else {
            deleteBtn.setEnabled(false);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        studDataTable.setEnabled(true);
        nameTF.setEnabled(false);
        surnameTF.setEnabled(false);
        middleNameTF.setEnabled(false);
        genderCB.setEnabled(false);
        birthDate.setEnabled(false);
        classCB.setEnabled(false);
        photoUpl.setEnabled(false);
        relativesTable.setEnabled(false);
        plusRelButton.setEnabled(false);
        plusPayButton.setEnabled(false);
        plusCallButton.setEnabled(false);
        plusMatGiveButton.setEnabled(false);
        plusMatReceiveButton.setEnabled(false);
        acsGiveTable.setEnabled(false);
        callsTable.setEnabled(false);
        acsReceiveTable.setEnabled(false);
        for (Object next : ((IndexedContainer) paymentsTable
                .getContainerDataSource()).getItemIds()) {
            ((Button) paymentsTable.getContainerProperty(next, Settings.button).getValue()).setEnabled(false);
            ((ComboBox) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).setEnabled(false);
            ((ComboBox) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.PaymentType)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Amount)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Rate)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next, Settings.KGS).getValue()).setEnabled(false);
            ((DateField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Date)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.WhoPaid)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Note)).getValue()).setEnabled(false);

        }
        contractCB.setEnabled(false);
        discountsTable.setEnabled(false);
        correctionsTable.setEnabled(false);
        currDate.setEnabled(false);
        divideTF.setEnabled(false);
        plusInstButton.setEnabled(false);
        plusDiscButton.setEnabled(false);
        plusCorrectionButton.setEnabled(false);
        initialPaymentTF.setEnabled(false);
        installmentTable.setEnabled(false);
        divideBtn.setEnabled(false);
        for (Component component : tabs) {
            TabSheet.Tab tab = tabs.getTab(component);
            if (tab.getComponent() == contractTabLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.contractTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == payTableLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.paymentsTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == callsTableLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.callsTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == famTableLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.familyTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == acsGiveTableLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.giveAccessoriesTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == acsReceiveTableLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.takeAccessoriesTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == studSearchLay) {
                tab.setEnabled(true);
            }
        }
    }

    private void fillFields() {
        loginTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(SptMessages.Id)).getValue().toString());
        nameTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(SptMessages.FirstName)).getValue().toString());
        surnameTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(SptMessages.LastName)).getValue().toString());
        if (studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(SptMessages.MiddleName)).getValue() != null) {
            middleNameTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                    studDataTable.getValue(), myUI.getMessage(SptMessages.MiddleName)).getValue().toString());
        }
        genderCB.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), Settings.gender_id).getValue());
        birthDate.setValue((Date) studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(SptMessages.DateOfBirth)).getValue());
        classCB.removeValueChangeListener(this);
        classCB.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), Settings.class_name_id).getValue());
        classCB.addValueChangeListener(this);
        statusCB.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), Settings.education_status_id).getValue());
        if (studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.Photo)).getValue() != null) {
            photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS
                    + studDataTable.getContainerProperty(studDataTable.getValue(),
                    myUI.getMessage(SptMessages.Photo)).getValue().toString())));
            photoName = studDataTable.getContainerProperty(studDataTable.getValue(),
                    myUI.getMessage(SptMessages.Photo)).getValue().toString();
        } else {
            photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
            photoName = null;
        }
        if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()) {
            setRelativesTable();
        } else if (tabs.getSelectedTab() == tabs.getTab(acsGiveTableLay).getComponent()) {
            setMaterialsTable(give);
        } else if (tabs.getSelectedTab() == tabs.getTab(acsReceiveTableLay).getComponent()) {
            setMaterialsTable(receive);
        } else if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
            setInstPlanTable();
            setDiscountsTable();
            setCorrectionsTable();
            setContractTab((Integer) studDataTable.getValue(), myUI.getUser().getCurrent_year().getId());
        } else if (tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent()) {
            setPaymentsTable();
        } else if (tabs.getSelectedTab() == tabs.getTab(callsTableLay).getComponent()) {
            setCallsTable();
        }
    }

    private void clearFields() {
        loginTF.setValue("");
        nameTF.setValue("");
        surnameTF.setValue("");
        middleNameTF.setValue("");
        genderCB.setValue(null);
        birthDate.setValue(null);
        classCB.setValue(null);
        statusCB.setValue(null);
        contractCB.setValue(null);
        divideTF.setValue(null);
        currDate.setValue(null);
        photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoName = null;
        relativesTable.removeAllItems();
        acsGiveTable.removeAllItems();
        acsReceiveTable.removeAllItems();
        installmentTable.removeAllItems();
        paymentsTable.removeAllItems();
        callsTable.removeAllItems();
        discountsTable.removeAllItems();
    }

    private void updateDataContainer() {
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.Id)).setValue(loginTF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.FirstName)).setValue(nameTF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.LastName)).setValue(surnameTF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.MiddleName)).setValue(middleNameTF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.Photo)).setValue(photoName);
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.DateOfBirth)).setValue(birthDate.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                Settings.class_name_id).setValue(classCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.ClassName)).setValue(classCB
                .getContainerDataSource().getContainerProperty(classCB.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                Settings.education_status_id).setValue(statusCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                Settings.gender_id).setValue(genderCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.EducationStatus)).setValue(statusCB
                .getContainerDataSource().getContainerProperty(statusCB.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());

    }

    private void addDataContainerItem(int id) {
        Item item = ((IndexedContainer) studDataTable.getContainerDataSource()).addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Id)).setValue(loginTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.FirstName)).setValue(nameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.LastName)).setValue(surnameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.MiddleName)).setValue(
                middleNameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.DateOfBirth)).setValue(
                birthDate.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Photo)).setValue(
                photoName);
        item.getItemProperty(Settings.gender_id).setValue(
                genderCB.getValue());
        item.getItemProperty(Settings.class_name_id).setValue(
                classCB.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                classCB.getContainerDataSource().getContainerProperty(classCB.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
        item.getItemProperty(Settings.education_status_id).setValue(
                statusCB.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.EducationStatus)).setValue(
                statusCB.getContainerDataSource().getContainerProperty(statusCB.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
        item.getItemProperty(myUI.getMessage(SptMessages.EnteringYear)).setValue(
                myUI.getUser().getCurrent_year().getName());
        studDataTable.clearFilters();
        studDataTable.setValue(id);
    }

    private Student getStudent(int id) {
        Student s = new Student();
        s.setPhoto(photoName);
        s.setLogin(loginTF.getValue());
        s.setPassword(new Sha256Hash(loginTF.getValue()).toString());
        s.setName(nameTF.getValue());
        s.setSurname(surnameTF.getValue());
        s.setMiddle_name(middleNameTF.getValue());
        s.setGender_id((Integer) genderCB.getValue());
        s.setBirth_date(birthDate.getValue());
        s.setClass_name_id((Integer) classCB.getValue());
        s.setEdu_status_id((Integer) statusCB.getValue());
        s.setEntering_year_id(myUI.getUser().getCurrent_year().getId());
        s.setSchool_id(myUI.getUser().getSchool_id());
        s.setEmployee_id(myUI.getUser().getId());
        s.setId(id);
        return s;
    }

    private void execDelete() {
        try {
            DbAccTransactions dbt = new DbAccTransactions();
            dbt.connect();
            AccTransaction acTr = dbt.exec_allow_delete_by_st_id((Integer) studDataTable.getValue(), myUI.getUser().getSchool_id());
            if (acTr != null) {
                Notification.show(myUI.getMessage(SptMessages.LowBalance) + Settings.dFormat.format(acTr.getOverLimit())
                        + " $ (" + Settings.df.format(acTr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
            } else {
                DbStudent dbst = new DbStudent();
                DbDefinition dbdef = new DbDefinition();
                dbst.connect();
                dbdef.connect();
                dbt.exec_delete_by_st_id((Integer) studDataTable.getValue());
                dbst.exec_delete((Integer) studDataTable.getValue());
                int st = dbdef.exec_delete((Integer) studDataTable.getValue(), Settings.dbStudent);
                if (st != 0) {
                    clearFields();
                    eduStatCont.getContainerProperty(studDataTable
                                    .getContainerProperty(studDataTable.getValue(),
                                            Settings.education_status_id).getValue(), Settings.count)
                            .setValue(((Integer) eduStatCont.getContainerProperty(studDataTable
                                            .getContainerProperty(studDataTable.getValue(),
                                                    Settings.education_status_id).getValue(),
                                    Settings.count).getValue()) - 1);
                    eduStatCont.getContainerProperty(6, Settings.count)
                            .setValue(((Integer) eduStatCont.getContainerProperty(6, Settings.count)
                                    .getValue()) - 1);
                    repaint();
                    studDataTable.removeItem(studDataTable.getValue());
                    studDataTable.setValue(null);
                    Notification.show(myUI.getMessage(SptMessages.StudentDeletedSuccessfully),
                            Notification.Type.HUMANIZED_MESSAGE);
                    tabs.setSelectedTab(studDataTable);
                    clearContractInfo();
                }
                dbst.close();
                dbdef.close();
            }
            dbt.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            Notification.show(myUI.getMessage(SptMessages.CanNotDelete),
                    Notification.Type.WARNING_MESSAGE);
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void execDeleteAccessoriesFromDb(int stud_id, int year_id, int cat_id) {
        try {
            DbStudentAccessories dbsa = new DbStudentAccessories();
            dbsa.connect();
            dbsa.exec_delete(stud_id, year_id, cat_id);
            dbsa.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void execDeleteInstPlanFromDb(int stud_id, int year_id) {
        try {
            DbStudentInstallmentPlan dbip = new DbStudentInstallmentPlan();
            dbip.connect();
            dbip.exec_delete(stud_id, year_id);
            dbip.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private boolean validate(ComponentContainer layout) {
        boolean result = true;
        for (Component c : layout) {
            if (c instanceof AbstractField) {
                try {
                    ((AbstractField<?>) c).validate();
                } catch (Exception e) {
                    //((AbstractComponent) c).setComponentError(new UserError(e.getMessage()));
                    result = false;
                }
            } else if (c instanceof AbstractComponentContainer) {
                if (!validate((AbstractComponentContainer) c)) {
                    result = false;
                }
            }
        }
        return result;
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
                    myFile = new File(Settings.PATH_TO_UPLOADS + photoName);
                } else {
                    myFile = new File(Settings.PATH_TO_UPLOADS + System.currentTimeMillis() + "_" + filename);
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
        l.setWidth(Settings.PERCENTS100);
        l.setMargin(true);
        statusWindow.setContent(l);

        cancelButton = new Button();
        cancelButton.addClickListener(this);
        cancelButton.setId(Settings.cancel_upload_button);
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
        Upload upl = new Upload(null, new StudentDefinitionView.MyReceiver(isPhoto));
        upl.setImmediate(true);
        if (!isPhoto) {
            upl.setStyleName("with-icon");
        }
        upl.addStyleName(ValoTheme.BUTTON_TINY);
        upl.setButtonCaption(caption);
        upl.addStartedListener((Upload.StartedListener) event -> {

            buildUploadWindow();
            myUI.addWindow(statusWindow);
            statusWindow.setClosable(false);

            uploadProgressBar.setValue(0f);
            uploadProgressBar.setVisible(true);
            UI.getCurrent().setPollInterval(500);
            cancelButton.setVisible(true);
            cancelButton.addClickListener((Button.ClickListener) clickEvent -> {
                try {
                    upl.interruptUpload();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            });
        });

        upl.addProgressListener((Upload.ProgressListener) (readBytes, contentLength) -> {
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
                uploadProgressBar.setValue(readBytes / (float) contentLength);
            }
        });

        upl.addSucceededListener((Upload.SucceededListener) event -> {
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
                b.setStyleName(ValoTheme.BUTTON_FRIENDLY);
                b.addStyleName(ValoTheme.BUTTON_SMALL);
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
        });

        upl.addFailedListener((Upload.FailedListener) event -> {
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
        });

        upl.addFinishedListener((Upload.FinishedListener) event -> {
            if (statusWindow != null) {
                statusWindow.close();
            }
        });
        return upl;
    }

    private void setRelativesTable() {
        NATURAL_COL_ORDER_RELATIVES = new String[]{Settings.button,
                myUI.getMessage(SptMessages.RelativeType),
                myUI.getMessage(SptMessages.FullName), myUI.getMessage(SptMessages.Address),
                myUI.getMessage(SptMessages.Phone), myUI.getMessage(SptMessages.WorkPlace),
                myUI.getMessage(SptMessages.Passport), myUI.getMessage(SptMessages.Responsible)};
        try {
            DbStudentRelative dbr = new DbStudentRelative();
            dbr.connect();
            relativesTable.setContainerDataSource(
                    dbr.execSQL_St_Rel(myUI, (Integer) studDataTable.getValue(), this));
            dbr.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        relativesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_RELATIVES);
    }

    public IndexedContainer prepareRelativesContainer() {
        if (productsContainer == null) {
            productsContainer = new IndexedContainer();
            productsContainer.addContainerProperty(Settings.button, Button.class, null);
            productsContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.RelativeType), ComboBox.class, null);
            productsContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.FullName), TextField.class, null);
            productsContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Passport), TextField.class, null);
            productsContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.WorkPlace), TextField.class, null);
            productsContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Phone), TextField.class, null);
            productsContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Address), TextField.class, null);
            productsContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Responsible), CheckBox.class, false);
            productsContainer.addContainerProperty(
                    Settings.crud_status, String.class, null);
        } else {
            productsContainer.removeAllItems();
        }
        return productsContainer;
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

    public TextField createTextField(String value, String description, String itemId,
                                     boolean isFamTab, boolean isMain) {
        TextField tf = new TextField();
        tf.setDescription(description);
        if (!isFamTab) {
            tf.setRequired(true);
            tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
            tf.addValidator(new StringLengthValidator(
                    myUI.getMessage(SptMessages.NotificationWrongValue), null, 100, false));
        }
        if (isMain) {
            tf.setRequired(true);
            tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));

        }
        tf.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotificationWrongValue), null, 100, false));
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        if (value != null) {
            tf.setValue(value);
        }
        tf.setData(itemId);
        tf.addValueChangeListener(this);
        tf.setImmediate(true);
        return tf;
    }

    public TextField createTextFieldDouble(Double value, String description, String itemId) {
        ObjectProperty<Double> property = new ObjectProperty<>(0.0);
        TextField tf = new TextField(property);
        tf.setDescription(description);
        tf.setRequired(true);
        tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        tf.addValidator(new DoubleRangeValidator(
                myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, null));
        tf.setConverter(Settings.getStringToDoubleConverter());
        tf.setNullRepresentation("0.0");
        tf.setNullSettingAllowed(false);
        tf.setData(itemId);
        tf.getPropertyDataSource().setValue(value);
        tf.addValueChangeListener(this);
        return tf;
    }

    public TextField createTextFieldDisc(Double value, Double maxValue, String description, String itemId, boolean isDisabled) {
        ObjectProperty<Double> property = new ObjectProperty<>(0.0);
        TextField tf = new TextField(property);
        tf.setDescription(description);
        tf.setRequired(true);
        if (isDisabled) {
            tf.setEnabled(false);
        } else {
            tf.addValidator(new DoubleRangeValidator(
                    myUI.getMessage(SptMessages.NotificationWrongValue), 0.1, maxValue));
        }
        tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        tf.getPropertyDataSource().setValue(value);
        tf.setNullRepresentation("0.0");
        tf.setNullSettingAllowed(false);
        tf.setConverter(Settings.getStringToDoubleConverter());
        tf.setData(itemId);
        tf.addValueChangeListener(this);
        tf.setImmediate(true);
        return tf;
    }

    public TextField createTextFieldNote(String value, String description, String itemId) {
        TextField tf = new TextField();
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        if (value != null) {
            tf.setValue(value);
        }
        tf.setData(itemId);
        tf.setImmediate(true);
        return tf;
    }

    public DateField createDateField(Date value, String description, String itemId, boolean setDefDate, boolean isFutureAvailable) {
        DateField df = new DateField();
        df.setDescription(description);
        df.setRequired(true);
        df.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        df.setStyleName(ValoTheme.DATEFIELD_TINY);
        df.setWidth(Settings.PERCENTS100);
        if (setDefDate) {
            df.setValue(new Date());
        } else {
            df.setValue(value);
        }
        df.setData(itemId);
        df.addValueChangeListener(this);
        df.setImmediate(true);
        if (!isFutureAvailable) {
            df.setRangeEnd(new Date());
            df.setDateFormat(Settings.dateTimeMinPattern);
            df.setResolution(Resolution.MINUTE);
        } else {
            df.setDateFormat(Settings.datePattern);
        }
        return df;
    }

    public CheckBox createCheckBox(boolean value, String description, String itemId) {
        CheckBox ckb = new CheckBox();
        ckb.setDescription(description);
        ckb.setValue(value);
        ckb.setData(itemId);
        ckb.addValueChangeListener(this);
        return ckb;
    }

    public ComboBox createCombobox(int value, String description, String itemId,
                                   String db_table, boolean setDefYear, boolean setDefPayType,
                                   boolean setDefPayCatType, boolean is_disabled) {
        ComboBox cb = new ComboBox();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        cb.setRequired(true);
        cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbDefinition dbp = new DbDefinition();
            dbp.connect();
            cb.setContainerDataSource(dbp.exec_for_select(myUI, db_table, true));
            dbp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (is_disabled) {
            cb.setEnabled(false);
        }
        cb.setNullSelectionAllowed(false);
        cb.setData(itemId);
        if (setDefYear) {
            cb.setValue(myUI.getUser().getCurrent_year().getId());
        } else if (setDefPayCatType) {
            cb.setValue(2);
        } else if (setDefPayType) {
            cb.setValue(1);
        } else {
            cb.setValue(value);
        }
        cb.addValueChangeListener(this);
        return cb;
    }

    public ComboBox createComboboxPayment(int value, String description, String itemId,
                                          boolean isFromDb, boolean defType) {
        ComboBox cb = new ComboBox();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        cb.setRequired(true);
        cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);

        if (isFromDb) {
            try {
                DbPaymentCategory dbp = new DbPaymentCategory();
                dbp.connect();
                cb.setContainerDataSource(dbp.exec_for_select(myUI, true));
                dbp.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else {
            try {
                DbPaymentCategory dbp = new DbPaymentCategory();
                dbp.connect();
                cb.setContainerDataSource(dbp.exec_for_select(myUI, false));
                dbp.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        if (value == 1) {
            cb.setEnabled(false);
        }
        cb.setNullSelectionAllowed(false);
        cb.setData(itemId);
        if (defType) {
            cb.setValue(1);
        } else {
            cb.setValue(value);
        }
        cb.addValueChangeListener(this);
        return cb;
    }

    public ComboBox createComboboxDisc(int value, String description, String itemId) {
        ComboBox cb = new ComboBox();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        cb.setRequired(true);
        cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbDiscount dbd = new DbDiscount();
            dbd.connect();
            cb.setContainerDataSource(dbd.exec_for_select(myUI, myUI.getUser().getCurrent_year().getId(), value));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setNullSelectionAllowed(false);
        cb.setData(itemId);
        cb.setId(Settings.discount_type_id);
        cb.setValue(value);
        cb.addValueChangeListener(this);
        return cb;
    }

    public ComboBox createComboboxCorr(int value, String description, String itemId) {
        ComboBox cb = new ComboBox();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        cb.setRequired(true);
        cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            cb.setContainerDataSource(dbd.exec_correction_types(myUI));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setNullSelectionAllowed(false);
        cb.setData(itemId);
        cb.setId(Settings.correction_type_id);
        cb.setValue(value);
        cb.addValueChangeListener(this);
        return cb;
    }

    public ComboBoxMultiselect createComboboxMultiAcs(String value, int cat_id) {
        ComboBoxMultiselect comboMCB = new ComboBoxMultiselect(myUI.getMessage(SptMessages.Materials));
        comboMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        comboMCB.setWidth(Settings.PERCENTS100);
        comboMCB.setRequired(true);
        comboMCB.setImmediate(true);
        comboMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        comboMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        comboMCB.setShowSelectAllButton((filter, page) -> true);
        try {
            DbAccessories dba = new DbAccessories();
            dba.connect();
            comboMCB.setContainerDataSource(dba.exec_for_select(myUI, cat_id));
            dba.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (value != null) {
            comboMCB.setValue(convertStrToSet(value));
        }
        return comboMCB;
    }

    private boolean validateRelativesTable(Table t) {
        if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()) {
            if (t.size() == 0) {
                Notification.show(myUI.getMessage(SptMessages.NotificationEmptyTable),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            } else {
                Iterator<?> iter = ((IndexedContainer) relativesTable
                        .getContainerDataSource()).getItemIds().iterator();
                int counter = 0;
                while (iter.hasNext()) {
                    Object next = iter.next();
                    if (((CheckBox) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Responsible)).getValue()).getValue()) {
                        counter++;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.FullName)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.WorkPlace)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Passport)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Phone)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Address)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((ComboBox) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.RelativeType)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                }
                if (counter != 1) {
                    Notification.show(myUI.getMessage(SptMessages.NotificationWrongValueCounter),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }

            }
            return true;
        }
        return true;
    }

    private boolean validateAcsGiveTable(Table t) {
        if (tabs.getSelectedTab() == tabs.getTab(acsGiveTableLay).getComponent()) {
            if (t.size() == 0) {
                Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            } else {
                for (Object next : ((IndexedContainer) t
                        .getContainerDataSource()).getItemIds()) {
                    if (!((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Year)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((ComboBoxMultiselect) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Materials)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                }
            }
            return true;
        }
        return true;
    }

    private boolean validateAcsReceiveTable(Table t) {
        if (tabs.getSelectedTab() == tabs.getTab(acsReceiveTableLay).getComponent()) {
            if (t.size() == 0) {
                Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            } else {
                for (Object next : ((IndexedContainer) t
                        .getContainerDataSource()).getItemIds()) {
                    if (!((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Year)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((ComboBoxMultiselect) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Materials)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                }
            }
            return true;
        }
        return true;
    }

    private boolean validatePaymentsTable(Table t) {
        if (tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent()) {
            for (Object next : ((IndexedContainer) t.getContainerDataSource()).getItemIds()) {
                if (!((ComboBox) t.getItem(next).getItemProperty(
                        myUI.getMessage(SptMessages.PaymentType)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((TextField) t.getItem(next).getItemProperty(
                        myUI.getMessage(SptMessages.Amount)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((TextField) t.getItem(next).getItemProperty(
                        myUI.getMessage(SptMessages.WhoPaid)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((DateField) t.getItem(next).getItemProperty(
                        myUI.getMessage(SptMessages.Date)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
            }

        }
        return true;
    }

    private void addRelativeItem() {
        NATURAL_COL_ORDER_RELATIVES = new String[]{Settings.button,
                myUI.getMessage(SptMessages.RelativeType),
                myUI.getMessage(SptMessages.FullName), myUI.getMessage(SptMessages.Address),
                myUI.getMessage(SptMessages.Phone), myUI.getMessage(SptMessages.WorkPlace),
                myUI.getMessage(SptMessages.Passport), myUI.getMessage(SptMessages.Responsible)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (relativesTable.getContainerDataSource().size() == 0) {
            relativesTable.setContainerDataSource(prepareRelativesContainer());
        }
        Item item;
        item = ((IndexedContainer) relativesTable.getContainerDataSource()).addItemAt(
                relativesTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                        Settings.dbStudentRelatives, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.Responsible)).setValue(
                createCheckBox(false, myUI.getMessage(SptMessages.Responsible), id));
        item.getItemProperty(myUI.getMessage(SptMessages.FullName)).setValue(
                createTextField(null, myUI.getMessage(SptMessages.FullName), id, false, false));
        item.getItemProperty(myUI.getMessage(SptMessages.Passport)).setValue(
                createTextField(null, myUI.getMessage(SptMessages.Passport), id, true, false));
        item.getItemProperty(myUI.getMessage(SptMessages.Phone)).setValue(
                createTextField(null, myUI.getMessage(SptMessages.Phone), id, true, false));
        item.getItemProperty(myUI.getMessage(SptMessages.WorkPlace)).setValue(
                createTextField(null, myUI.getMessage(SptMessages.WorkPlace), id, true, false));
        item.getItemProperty(myUI.getMessage(SptMessages.Address)).setValue(
                createTextField(null, myUI.getMessage(SptMessages.Address), id, true, false));
        item.getItemProperty(myUI.getMessage(SptMessages.RelativeType)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.RelativeType), id,
                        Settings.dbRelatives, false, false, false, false));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));

        relativesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_RELATIVES);
    }

    private void insertInitialPayment(int student_id) {
        try {
            if (initialPaymentTF.isValid()) {
                DbStudentPayment dbsp = new DbStudentPayment();
                DbAccTransactions dbat = new DbAccTransactions();
                dbsp.connect();
                DbPaymentCategory dbpc = new DbPaymentCategory();
                dbpc.connect();
                DbStudentRelative dbsr = new DbStudentRelative();
                dbsr.connect();
                int status;
                AccTransaction tr = new AccTransaction();
                StudentPayment sp = new StudentPayment();
                sp.setStudent_id(student_id);
                sp.setModification_date(new Date());
                sp.setYear_id(myUI.getUser().getCurrent_year().getId());
                sp.setPayment_cat_type_id(1);
                sp.setPayment_type_id(1);
                if (initialPaymentTF.getPropertyDataSource().getValue() != null) {
                    sp.setAmount((Double) initialPaymentTF.getPropertyDataSource().getValue());
                }
                sp.setRate(myUI.getDb_currency_rate());
                sp.setWho_paid(dbsr.exec_get_who_paid(student_id));
                sp.setNoteForCashBox(studDataTable.getContainerProperty(studDataTable.getValue(),
                        myUI.getMessage(SptMessages.ClassName)).getValue().toString() + " "
                        + studDataTable.getContainerProperty(studDataTable.getValue(),
                        myUI.getMessage(SptMessages.Id)).getValue().toString() + " "
                        + studDataTable.getContainerProperty(studDataTable.getValue(),
                        myUI.getMessage(SptMessages.FirstName)).getValue().toString() + " "
                        + studDataTable.getContainerProperty(studDataTable.getValue(),
                        myUI.getMessage(SptMessages.LastName)).getValue().toString());
                sp.setEmployee_id(myUI.getUser().getId());
                sp.setSchool_id(myUI.getUser().getSchool_id());
                tr.setAmount(sp.getAmount());
                tr.setDate(sp.getModification_date());
                tr.setCategory_id(dbpc.get_initial_payment_category_id());
                tr.setCurrency_id(2);
                tr.setCurrency_rate(sp.getRate());
                tr.setNote(sp.getNoteForCashBox());
                tr.setEmployee_id(sp.getEmployee_id());
                tr.setSchool_id(sp.getSchool_id());
                if (initialPaymentTF.getPropertyDataSource().getValue() == null && initialPaymentTF.getData() != null) {
                    sp.setId(((StudentPayment) initialPaymentTF.getData()).getId());
                    String payment_id = Integer.toString(((StudentPayment) initialPaymentTF.getData()).getId());
                    dbat.exec_delete(Settings.dbColumnStudent_payments_id, payment_id, dbsp.getConnection());//delete transaction
                    dbsp.exec_update_emp_id(myUI.getUser().getId(), payment_id);
                    dbsp.exec_delete(payment_id);
                    initialPaymentTF.setData(null);
                } else if (initialPaymentTF.getPropertyDataSource().getValue() != null
                        && (Double) initialPaymentTF.getPropertyDataSource().getValue() != 0.0 && initialPaymentTF.getData() != null) {
                    sp.setId(((StudentPayment) initialPaymentTF.getData()).getId());
                    sp.setModification_date(((StudentPayment) initialPaymentTF.getData()).getModification_date());
                    sp.setRate(((StudentPayment) initialPaymentTF.getData()).getRate());
                    tr.setStudent_payments_id(sp.getId());
                    tr.setDate(sp.getModification_date());
                    status = dbsp.exec_update(sp);
                    if (status != 0) {
                        if (sp.getModification_date().after(myUI.getUser().getTransactions_start_date())
                                || sp.getModification_date().equals(myUI.getUser().getTransactions_start_date())) {
                            int update_status = dbat.exec_update(tr, Settings.dbColumnStudent_payments_id,
                                    tr.getStudent_payments_id(), dbsp.getConnection());//update transaction normal
                            if (update_status == 0) {
                                dbat.exec_insert(tr, dbsp.getConnection());//insert transaction if it doesn't exist, on payment update if payment date is after transactions start date
                            }
                        } else if (sp.getModification_date().before(myUI.getUser().getTransactions_start_date())) {
                            dbat.exec_delete(Settings.dbColumnStudent_payments_id, Integer.toString(sp.getId()), dbsp.getConnection()); //delete transaction on payment update if payment date is before transactions start date
                        }
                    }
                } else if (initialPaymentTF.getPropertyDataSource().getValue() != null) {
                    int order_num = dbsp.getMaxOrderNum(student_id);
                    int payment_id = dbsp.exec_insert(sp, order_num);
                    tr.setStudent_payments_id(payment_id);
                    if (sp.getModification_date().after(myUI.getUser().getTransactions_start_date())
                            || sp.getModification_date().equals(myUI.getUser().getTransactions_start_date())) {
                        dbat.exec_insert(tr, dbsp.getConnection());//insert transaction
                    }
                    initialPaymentTF.setData(dbsp.exec_get_init_payment(student_id, myUI.getUser().getCurrent_year().getId()));
                }
                dbpc.close();
                dbsr.close();
                dbsp.close();
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertInstPlanToDb(int student_id) {
        double diff;
        try {
            DbStudentInstallmentPlan dbip = new DbStudentInstallmentPlan();
            DbStudentPayment dbsp = new DbStudentPayment();
            dbip.connect();
            dbsp.connect();
            for (Object next : ((IndexedContainer) installmentTable
                    .getContainerDataSource()).getItemIds()) {

                StudentInstallmentPlan ip = getInstPlan(student_id, installmentTable.getItem(next));
                if ((Integer) installmentTable.getContainerProperty(next,
                        Settings.status_id).getValue() != 0) {
                    dbip.exec_insert(ip);
                }
            }
            diff = dbsp.exec_get_difference(student_id, myUI.getUser().getCurrent_year().getId());
            if (diff != 0) {
                dbip.exec_insert_notVisible(student_id, myUI.getUser().getCurrent_year().getId(), diff);
            }
            dbsp.close();
            dbip.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertAccessoriesToDb(int student_id, int cat_id) {
        if (cat_id == give) {
            try {
                DbStudentAccessories dbsa = new DbStudentAccessories();
                dbsa.connect();
                for (Object next : ((IndexedContainer) acsGiveTable
                        .getContainerDataSource()).getItemIds()) {
                    String[] acs_ids = Objects.requireNonNull(Settings.convertCollectionToStr((Set<?>) ((ComboBoxMultiselect) (acsGiveTable
                            .getContainerDataSource().getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Materials))
                            .getValue())).getValue())).split(",");
                    for (String s : acs_ids) {
                        int acs_id = Integer.parseInt(s);
                        StudentAccessories acs = getAccessories(student_id, acs_id, acsGiveTable.getItem(next));
                        dbsa.exec_insert(acs);
                    }
                }
                dbsa.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (cat_id == receive) {
            try {
                DbStudentAccessories dbsa = new DbStudentAccessories();
                dbsa.connect();
                for (Object next : ((IndexedContainer) acsReceiveTable
                        .getContainerDataSource()).getItemIds()) {
                    String[] acs_ids = Objects.requireNonNull(Settings.convertCollectionToStr((Set<?>) ((ComboBoxMultiselect) (acsReceiveTable
                            .getContainerDataSource().getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Materials))
                            .getValue())).getValue())).split(",");
                    for (String s : acs_ids) {
                        int acs_id = Integer.parseInt(s);
                        StudentAccessories acs = getAccessories(student_id, acs_id, acsReceiveTable.getItem(next));
                        dbsa.exec_insert(acs);
                    }
                }
                dbsa.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    private StudentRelative getRelative(int id, int student_id, Item item) {
        StudentRelative rel = new StudentRelative();
        rel.setStudent_id(student_id);
        rel.setFullName(((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.FullName)).getValue()).getValue());
        rel.setWork_place(((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.WorkPlace)).getValue()).getValue());
        rel.setPhone(((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.Phone)).getValue()).getValue());
        rel.setAddress(((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.Address)).getValue()).getValue());
        rel.setPassport(((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.Passport)).getValue()).getValue());
        if (((CheckBox) item.getItemProperty(
                myUI.getMessage(SptMessages.Responsible)).getValue()).getValue()) {
            rel.setIs_main(1);
        } else {
            rel.setIs_main(0);
        }
        rel.setRelatives_id((Integer) ((ComboBox) item.getItemProperty(
                myUI.getMessage(SptMessages.RelativeType)).getValue()).getValue());

        rel.setId(id);
        return rel;
    }

    private StudentPayment getPayment(int id, int student_id, Item item) {
        StudentPayment sp = new StudentPayment();
        sp.setStudent_id(student_id);
        sp.setYear_id(myUI.getUser().getCurrent_year().getId());
        sp.setPayment_cat_type_id((Integer) ((ComboBox) item.getItemProperty(
                myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getValue());
        sp.setPayment_type_id((Integer) ((ComboBox) item.getItemProperty(
                myUI.getMessage(SptMessages.PaymentType)).getValue()).getValue());
        sp.setAmount((Double) ((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue());
        sp.setRate((Double) ((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.Rate)).getValue()).getPropertyDataSource().getValue());
        sp.setWho_paid(((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.WhoPaid)).getValue()).getValue());
        sp.setNote(((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.Note)).getValue()).getValue());
        sp.setNoteForCashBox(studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.ClassName)).getValue().toString() + " "
                + studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.Id)).getValue().toString() + " "
                + studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.FirstName)).getValue().toString() + " "
                + studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.LastName)).getValue().toString());
        sp.setEmployee_id(myUI.getUser().getId());
        sp.setSchool_id(myUI.getUser().getSchool_id());
        sp.setModification_date(((DateField) item.getItemProperty(
                myUI.getMessage(SptMessages.Date)).getValue()).getValue());
        sp.setId(id);
        return sp;
    }

    private StudentInstallmentPlan getInstPlan(int student_id, Item item) {
        StudentInstallmentPlan ip = new StudentInstallmentPlan();
        ip.setStudent_id(student_id);
        ip.setYear_id(myUI.getUser().getCurrent_year().getId());
        ip.setDate_of_payment(((DateField) item.getItemProperty(
                myUI.getMessage(SptMessages.Date)).getValue()).getValue());
        ip.setAmount((Double) (((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue()));
        ip.setId(0);
        return ip;
    }

    private StudentAccessories getAccessories(int student_id, int acs_id, Item item) {
        StudentAccessories acc = new StudentAccessories();
        acc.setStudent_id(student_id);
        acc.setYear_id((Integer) ((ComboBox) item.getItemProperty(
                myUI.getMessage(SptMessages.Year)).getValue()).getValue());
        acc.setAccessories_id(acs_id);
        acc.setEmployee_id(myUI.getUser().getId());
        acc.setId(0);
        return acc;
    }

    private void setMaterialsTable(int cat_id) {
        try {
            DbStudentAccessories dbsa = new DbStudentAccessories();
            dbsa.connect();
            if (cat_id == give) {
                acsGiveTable.setContainerDataSource(
                        dbsa.execSQL_St_Acs(myUI, (Integer) studDataTable.getValue(), this, cat_id));
            } else if (cat_id == receive) {
                acsReceiveTable.setContainerDataSource(
                        dbsa.execSQL_St_Acs(myUI, (Integer) studDataTable.getValue(), this, cat_id));
            }
            dbsa.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setPaymentsTable() {
        NATURAL_COL_ORDER_PAYMENTS = new String[]{Settings.button,
                myUI.getMessage(SptMessages.PaymentCategoryType),
                myUI.getMessage(SptMessages.PaymentType), myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.Rate), Settings.KGS,
                myUI.getMessage(SptMessages.WhoPaid), myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.Note), myUI.getMessage(SptMessages.Print)};
        try {
            DbStudentPayment dbsa = new DbStudentPayment();
            dbsa.connect();
            paymentsTable.setContainerDataSource(
                    dbsa.execSQL_St_Payments(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbsa.close();
            paymentsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_PAYMENTS);
            paymentsTable.setColumnWidth(myUI.getMessage(SptMessages.Rate), 100);
            paymentsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.WhoPaid), 1);
            paymentsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Note), 1);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setCallsTable() {
        NATURAL_COL_ORDER_CALLS = new String[]{Settings.button,
                myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.WhoCalled), myUI.getMessage(SptMessages.Note)};
        try {
            DbStudentCalls dbsc = new DbStudentCalls();
            dbsc.connect();
            callsTable.setContainerDataSource(
                    dbsc.execSQL_St_Calls(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbsc.close();
            callsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CALLS);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setInstPlanTable() {
        NATURAL_COL_ORDER_INST_PLAN = new String[]{Settings.button,
                myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.Amount)};
        try {
            DbStudentInstallmentPlan dbip = new DbStudentInstallmentPlan();
            dbip.connect();
            installmentTable.setContainerDataSource(
                    dbip.execSQL_St_InstPLan(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbip.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        currDate.setValue(new Date());
        installmentTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_INST_PLAN);
    }

    private void setDiscountsTable() {
        NATURAL_COL_ORDER_DISCOUNTS = new String[]{Settings.button,
                myUI.getMessage(SptMessages.Title), myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.Note), myUI.getMessage(SptMessages.Document)};
        try {
            DbStudentDiscount dbsd = new DbStudentDiscount();
            dbsd.connect();
            discountsTable.setContainerDataSource(
                    dbsd.execSQL_St_Discounts(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbsd.close();

        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        currDate.setValue(new Date());
        discCounter = 0;
        for (int i = 0; i < discountsTable.size(); i++) {
            discCounter++;
        }
        discountsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_DISCOUNTS);
    }

    private void setCorrectionsTable() {
        NATURAL_COL_ORDER_CORRECTIONS = new String[]{Settings.button,
                myUI.getMessage(SptMessages.Title), myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.Note)};
        try {
            DbStudentCorrection dbsd = new DbStudentCorrection();
            dbsd.connect();
            correctionsTable.setContainerDataSource(
                    dbsd.execSQL_St_Discounts(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbsd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        correctionsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CORRECTIONS);
    }

    public IndexedContainer prepareMaterialsGivContainer() {
        if (acsGivContainer == null) {
            acsGivContainer = new IndexedContainer();
            acsGivContainer.addContainerProperty(Settings.button, Button.class, null);
            acsGivContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Year), ComboBox.class, null);
            acsGivContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Materials), ComboBoxMultiselect.class, null);

        } else {
            acsGivContainer.removeAllItems();
        }
        return acsGivContainer;
    }

    public IndexedContainer prepareMaterialsRecContainer() {
        if (acsRecContainer == null) {
            acsRecContainer = new IndexedContainer();
            acsRecContainer.addContainerProperty(Settings.button, Button.class, null);
            acsRecContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Year), ComboBox.class, null);
            acsRecContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Materials), ComboBoxMultiselect.class, null);

        } else {
            acsRecContainer.removeAllItems();
        }
        return acsRecContainer;
    }

    public IndexedContainer preparePaymentsContainer() {
        if (paymentCont == null) {
            paymentCont = new IndexedContainer();
            paymentCont.addContainerProperty(Settings.button, Button.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.PaymentCategoryType), ComboBox.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.PaymentType), ComboBox.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Amount), TextField.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Rate), TextField.class, null);
            paymentCont.addContainerProperty(Settings.KGS, TextField.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.WhoPaid), TextField.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Date), DateField.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Note), TextField.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Print), Button.class, null);
            paymentCont.addContainerProperty(Settings.old_amount, Double.class, 0.0);
            paymentCont.addContainerProperty(Settings.old_date, Date.class, null);
            paymentCont.addContainerProperty(Settings.old_category, Integer.class, 0);
            paymentCont.addContainerProperty(Settings.crud_status, String.class, null);

        } else {
            paymentCont.removeAllItems();
        }
        return paymentCont;
    }

    public IndexedContainer prepareCallsContainer() {
        if (callsCont == null) {
            callsCont = new IndexedContainer();
            callsCont.addContainerProperty(Settings.button, Button.class, null);
            callsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Date), String.class, null);
            callsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.WhoCalled), String.class, null);
            callsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Note), TextField.class, null);
            callsCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            callsCont.removeAllItems();
        }
        return callsCont;
    }

    public IndexedContainer prepareDiscountsContainer() {
        if (discountCont == null) {
            discountCont = new IndexedContainer();
            discountCont.addContainerProperty(Settings.button, Button.class, null);
            discountCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Title), ComboBox.class, null);
            discountCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Amount), TextField.class, null);
            discountCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Note), TextField.class, null);
            discountCont.addContainerProperty(myUI.getMessage(SptMessages.Document),
                    HorizontalLayout.class, null);
            discountCont.addContainerProperty(
                    Settings.crud_status, String.class, null);
        } else {
            discountCont.removeAllItems();
        }
        return discountCont;
    }

    public IndexedContainer prepareCorrectionsContainer() {
        if (correctionCont == null) {
            correctionCont = new IndexedContainer();
            correctionCont.addContainerProperty(Settings.button, Button.class, null);
            correctionCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Title), ComboBox.class, null);
            correctionCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Amount), TextField.class, null);
            correctionCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Note), TextField.class, null);
            correctionCont.addContainerProperty(
                    Settings.crud_status, String.class, null);
        } else {
            correctionCont.removeAllItems();
        }
        return correctionCont;
    }

    public IndexedContainer prepareInstPlanContainer() {
        if (instPlanCont == null) {
            instPlanCont = new IndexedContainer();
            instPlanCont.addContainerProperty(Settings.button, Button.class, null);
            instPlanCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Date), DateField.class, null);
            instPlanCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Amount), TextField.class, null);
            instPlanCont.addContainerProperty(Settings.status_id, Integer.class, 0);

        } else {
            instPlanCont.removeAllItems();
        }
        return instPlanCont;
    }

    private void addAccessoriesItem(int cat_id) {
        if (cat_id == give) {
            String id = Settings.FreshItem + (--r_table_counter);
            if (acsGiveTable.getContainerDataSource().size() == 0) {
                acsGiveTable.setContainerDataSource(prepareMaterialsGivContainer());
            }
            Item item;
            item = ((IndexedContainer) acsGiveTable.getContainerDataSource()).addItemAt(
                    acsGiveTable.getContainerDataSource().size(), id);
            item.getItemProperty(Settings.button).setValue(
                    createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                            Settings.dbStudentAccessories, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(SptMessages.Year)).setValue(
                    createCombobox(0, myUI.getMessage(SptMessages.Year), id,
                            Settings.dbYear, true, false, false, false));
            item.getItemProperty(myUI.getMessage(SptMessages.Materials)).setValue(
                    createComboboxMultiAcs(null, cat_id));
        } else if (cat_id == receive) {
            String id = Settings.FreshItem + (--r_table_counter);
            if (acsReceiveTable.getContainerDataSource().size() == 0) {
                acsReceiveTable.setContainerDataSource(prepareMaterialsRecContainer());
            }
            Item item;
            item = ((IndexedContainer) acsReceiveTable.getContainerDataSource()).addItemAt(
                    acsReceiveTable.getContainerDataSource().size(), id);
            item.getItemProperty(Settings.button).setValue(
                    createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                            Settings.dbStudentAccessories, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(SptMessages.Year)).setValue(
                    createCombobox(0, myUI.getMessage(SptMessages.Year), id,
                            Settings.dbYear, true, false, false, false));
            item.getItemProperty(myUI.getMessage(SptMessages.Materials)).setValue(
                    createComboboxMultiAcs(null, cat_id));
        }
    }

    private void addPaymentsItem() {
        NATURAL_COL_ORDER_PAYMENTS = new String[]{Settings.button,
                myUI.getMessage(SptMessages.PaymentCategoryType),
                myUI.getMessage(SptMessages.PaymentType), myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.Rate), Settings.KGS,
                myUI.getMessage(SptMessages.WhoPaid), myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.Note), myUI.getMessage(SptMessages.Print)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (paymentsTable.getContainerDataSource().size() == 0) {
            paymentsTable.setContainerDataSource(preparePaymentsContainer());
        }
        Item item;
        item = ((IndexedContainer) paymentsTable.getContainerDataSource()).addItemAt(
                paymentsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                        Settings.dbStudentPayments, FontAwesome.MINUS_SQUARE));
        ComboBox cb = createComboboxPayment(0, myUI.getMessage(SptMessages.PaymentCategoryType), id, false, true);
        cb.setId(myUI.getMessage(SptMessages.Payments));
        item.getItemProperty(myUI.getMessage(SptMessages.PaymentCategoryType)).setValue(cb);
        item.getItemProperty(myUI.getMessage(SptMessages.PaymentType)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.PaymentType), id,
                        Settings.dbPaymentType, false, true, false, false));
        TextField tf = createTextFieldDouble(null, myUI.getMessage(SptMessages.Amount), id);
        tf.setId(myUI.getMessage(SptMessages.Payments));
        item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(tf);
        tf = createTextFieldDouble(null, Settings.KGS, id);
        tf.setId(Settings.KGS);
        tf.setRequired(false);
        tf.removeAllValidators();
        tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.0, null));
        item.getItemProperty(Settings.KGS).setValue(tf);
        tf = createTextFieldDouble(myUI.getDb_currency_rate(), myUI.getMessage(SptMessages.Rate), id);
        tf.setId(myUI.getMessage(SptMessages.Rate));
        item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(tf);
        String wh_paid = null;
        try {
            DbStudentRelative dbsr = new DbStudentRelative();
            dbsr.connect();
            wh_paid = dbsr.exec_get_who_paid((Integer) studDataTable.getValue());
            dbsr.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        item.getItemProperty(myUI.getMessage(SptMessages.WhoPaid)).setValue(
                createTextField(wh_paid, myUI.getMessage(SptMessages.WhoPaid), id, false, false));
        DateField df = createDateField(null, myUI.getMessage(SptMessages.Date), id, true, false);
        df.setId(myUI.getMessage(SptMessages.Payments));
        if (currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeOldTransactions)) {
            df.setRangeStart(myUI.getUser().getTransactions_start_date());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -1441);
            df.setRangeStart(calendar.getTime());
            df.addValidator(new DateRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue),
                    df.getRangeStart(), df.getRangeEnd(), Resolution.MINUTE));
        }
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(df);
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                createTextFieldNote(null, myUI.getMessage(SptMessages.Note), id));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        paymentsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_PAYMENTS);
        paymentsTable.setColumnWidth(Settings.KGS, 100);
        paymentsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.WhoPaid), 1);
        paymentsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Note), 1);
    }

    private void addCallsItem() {
        NATURAL_COL_ORDER_CALLS = new String[]{Settings.button,
                myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.WhoCalled), myUI.getMessage(SptMessages.Note)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (callsTable.getContainerDataSource().size() == 0) {
            callsTable.setContainerDataSource(prepareCallsContainer());
        }
        Item item;
        item = ((IndexedContainer) callsTable.getContainerDataSource()).addItemAt(
                callsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                        Settings.dbStudentCalls, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(Settings.df.format(new Date()));
        item.getItemProperty(myUI.getMessage(SptMessages.WhoCalled)).setValue(myUI.getUser().getFullName());
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                createTextFieldNote(null, myUI.getMessage(SptMessages.Note), id));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        callsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CALLS);

    }

    private void addDiscountsItem() {
        NATURAL_COL_ORDER_DISCOUNTS = new String[]{Settings.button,
                myUI.getMessage(SptMessages.Title), myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.Note), myUI.getMessage(SptMessages.Document)};
        discCounter++;
        String id = Settings.FreshItem + (--r_table_counter);
        if (discountsTable.getContainerDataSource().size() == 0) {
            discountsTable.setContainerDataSource(prepareDiscountsContainer());
        }
        Item item;
        item = ((IndexedContainer) discountsTable.getContainerDataSource()).addItemAt(
                discountsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                Settings.dbStudentDiscount, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(
                createComboboxDisc(0, myUI.getMessage(SptMessages.Title), id));
        item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                createTextFieldDisc(null, null, myUI.getMessage(SptMessages.DiscountAmount), id, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                createTextField(null, myUI.getMessage(SptMessages.Note), id, true, false));

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        Button b = createButton(myUI.getMessage(SptMessages.DownLoad), null, Settings.download_button, FontAwesome.DOWNLOAD);
        b.setStyleName("unread");
        b.addStyleName(ValoTheme.BUTTON_SMALL);
        b.setEnabled(false);
        hl.addComponent(b);

        Upload u = createUpload("", false);
        u.setId(id);
        u.setData(b);
        hl.addComponent(u);
        item.getItemProperty(myUI.getMessage(SptMessages.Document)).setValue(hl);

        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        discountsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_DISCOUNTS);
    }

    private void addCorrectionsItem() {
        NATURAL_COL_ORDER_CORRECTIONS = new String[]{Settings.button,
                myUI.getMessage(SptMessages.Title), myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.Note)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (correctionsTable.getContainerDataSource().size() == 0) {
            correctionsTable.setContainerDataSource(prepareCorrectionsContainer());
        }
        Item item;
        item = ((IndexedContainer) correctionsTable.getContainerDataSource()).addItemAt(
                correctionsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                Settings.dbStudentCorrection, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(
                createComboboxCorr(0, myUI.getMessage(SptMessages.Title), id));
        item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                createTextFieldDouble(null, myUI.getMessage(SptMessages.CorrectionAmount), id));
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                createTextField(null, myUI.getMessage(SptMessages.Note), id, true, false));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        correctionsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CORRECTIONS);
    }

    private void addInstallmentPlanItem(Boolean autoFill) {
        NATURAL_COL_ORDER_INST_PLAN = new String[]{Settings.button,
                myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.Amount)};
        if (!autoFill) {
            String id = Settings.FreshItem + (--r_table_counter);
            if (installmentTable.getContainerDataSource().size() == 0) {
                installmentTable.setContainerDataSource(prepareInstPlanContainer());
            }
            Item item;
            item = ((IndexedContainer) installmentTable.getContainerDataSource()).addItemAt(
                    installmentTable.getContainerDataSource().size(), id);
            item.getItemProperty(Settings.button).setValue(
                    createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                            Settings.dbStudentInstallment, FontAwesome.MINUS_SQUARE));
            DateField df = createDateField(currDate.getValue(), myUI.getMessage(SptMessages.Date),
                    id, false, true);
            df.setRangeEnd(new Date(Settings.INSTALLMENT_DATE_LIMIT));
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(df);
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                    createTextFieldDouble(null, myUI.getMessage(SptMessages.Amount), id));
            item.getItemProperty(Settings.status_id).setValue(1);
        } else if (instCtrAmount != null) {
            installmentTable.removeAllItems();
            Iterator<?> iter = instPlanCont.getItemIds().iterator();
            Double s = 0.0;
            Double left = instCtrAmount;
            while (iter.hasNext()) {
                Object next = iter.next();
                if (((TextField) instPlanCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue() != null
                        && !((TextField) instPlanCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue().equals("")) {
                    s += (Double) ((TextField) instPlanCont.getContainerProperty(next,
                            myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue();
                }
            }
            left -= s;
            for (int i = 0; i < Integer.parseInt(divideTF.getValue()); i++) {
                double divSum = left / Integer.parseInt(divideTF.getValue());
                Calendar dateLimit = Calendar.getInstance();
                dateLimit.setTimeInMillis(Settings.INSTALLMENT_DATE_LIMIT);
                Calendar cal = Calendar.getInstance();
                cal.setTime(currDate.getValue());
                cal.add(Calendar.MONTH, i + 1);
                if (installmentTable.getContainerDataSource().size() == 0) {
                    installmentTable.setContainerDataSource(prepareInstPlanContainer());
                }
                if (dateLimit.after(cal)) {
                    String id = Settings.FreshItem + (--r_table_counter);
                    Item item = ((IndexedContainer) installmentTable.getContainerDataSource()).addItemAt(
                            installmentTable.getContainerDataSource().size(), id);
                    item.getItemProperty(Settings.button).setValue(
                            createButton(myUI.getMessage(SptMessages.DeleteButton), id,
                                    Settings.dbStudentInstallment, FontAwesome.MINUS_SQUARE));
                    DateField df = createDateField(cal.getTime(), myUI.getMessage(SptMessages.Date), id,
                            false, true);
                    df.setRangeEnd(new Date(Settings.INSTALLMENT_DATE_LIMIT));
                    item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(df);
                    item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                            createTextFieldDouble(Settings.round(divSum, 2), myUI.getMessage(SptMessages.Amount), id));
                    item.getItemProperty(Settings.status_id).setValue(1);
                } else {
                    String id = Settings.FreshItem + r_table_counter;
                    Item item = installmentTable.getContainerDataSource().getItem(id);
                    TextField tf = (TextField) item.getItemProperty(myUI.getMessage(SptMessages.Amount)).getValue();
                    tf.getPropertyDataSource().setValue(Settings.round(divSum * (Integer.parseInt(divideTF.getValue()) - i + 1), 2));
                    break;
                }
            }
        }
        installmentTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_INST_PLAN);
    }

    private Set<?> convertStrToSet(String str) {
        String[] strArr = str.split(",");
        HashSet<String> hs = new HashSet<>(strArr.length);
        Collections.addAll(hs, strArr);
        return hs;
    }

    private void buildContractTab() {
        contractTabLay = new GridLayout(6, 3);
        contractTabLay.setMargin(true);
        contractTabLay.setSpacing(true);
        contractTabLay.setSizeFull();

        installmentTable = new FormattedTable();
        installmentTable.setSizeFull();
        installmentTable.setSelectable(false);
        installmentTable.setStyleName(ValoTheme.TABLE_SMALL);
        installmentTable.setNullSelectionAllowed(false);

        discountsTable = new FormattedTable();
        discountsTable.setSizeFull();
        discountsTable.setSelectable(false);
        discountsTable.setStyleName(ValoTheme.TABLE_SMALL);
        discountsTable.setNullSelectionAllowed(false);

        correctionsTable = new FormattedTable();
        correctionsTable.setSizeFull();
        correctionsTable.setSelectable(false);
        correctionsTable.setStyleName(ValoTheme.TABLE_SMALL);
        correctionsTable.setNullSelectionAllowed(false);

        contractCB = new ComboBox(myUI.getMessage(SptMessages.Contract));
        contractCB.setWidth(Settings.PERCENTS100);
        contractCB.setNullSelectionAllowed(false);
        contractCB.setRequired(true);
        contractCB.addValueChangeListener(this);
        contractCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        contractCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        contractCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        contractCB.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbContract dbc = new DbContract();
            dbc.connect();
            contractCB.setContainerDataSource(
                    dbc.exec_contr_select(myUI, myUI.getUser().getCurrent_year().getId(),
                            myUI.getUser().getSchool_id(), contr_id));
            dbc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        ObjectProperty<Double> property = new ObjectProperty<>(0.0);
        initialPaymentTF = new TextField(myUI.getMessage(SptMessages.InitialPayment), property);
        initialPaymentTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotificationWrongValue), 0.01, null));
        initialPaymentTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        initialPaymentTF.setRequired(false);
        initialPaymentTF.setNullSettingAllowed(true);
        initialPaymentTF.setNullRepresentation("");
        initialPaymentTF.setConverter(Settings.getStringToDoubleConverter());
        initialPaymentTF.addValueChangeListener(this);

        currDate = new DateField(myUI.getMessage(SptMessages.StartDate));
        currDate.setWidth(Settings.PERCENTS100);
        currDate.setStyleName(ValoTheme.DATEFIELD_TINY);
        currDate.setDateFormat(Settings.datePattern);

        divideTF = new TextField(myUI.getMessage(SptMessages.DivideInto));
        divideTF.setWidth(Settings.PERCENTS100);
        divideTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        divideTF.setConverter(new StringToIntegerConverter());
        divideTF.addValidator(new IntegerRangeValidator(myUI.getMessage(SptMessages.OnlyInt), 1, 15));
        divideTF.setNullRepresentation("");
        divideTF.addValueChangeListener(this);

        divideBtn = new Button();
        divideBtn.setDescription(myUI.getMessage(SptMessages.DivideButton));
        divideBtn.setStyleName(ValoTheme.BUTTON_TINY);
        divideBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        divideBtn.setIcon(FontAwesome.CHECK);
        divideBtn.addClickListener(this);

        buildInstPlanLayout();

        Label captionInst = new Label();
        captionInst.setSizeFull();
        captionInst.setContentMode(ContentMode.HTML);
        captionInst.setValue(myUI.getMessage(SptMessages.InstallmentPlan));
        captionInst.setStyleName("tableCpt");

        GridLayout glInst = new GridLayout(2, 2);
        glInst.setSpacing(true);
        glInst.setSizeFull();
        glInst.addComponent(captionInst, 1, 0);
        glInst.addComponent(plusInstButton, 0, 0);
        glInst.addComponent(installmentTable, 0, 1, 1, 1);
        glInst.setRowExpandRatio(1, 1);
        glInst.setColumnExpandRatio(1, 1);

        contractTabLay.addComponent(contractCB, 0, 0);
        contractTabLay.addComponent(initialPaymentTF, 1, 0);
        contractTabLay.addComponent(instPlanLay, 5, 0);
        contractTabLay.setComponentAlignment(instPlanLay, Alignment.TOP_RIGHT);
        contractTabLay.setRowExpandRatio(2, 1);
        contractTabLay.addComponent(currDate, 2, 1);
        contractTabLay.addComponent(divideTF, 3, 1);
        contractTabLay.addComponent(divideBtn, 4, 1);
        contractTabLay.setComponentAlignment(divideBtn, Alignment.BOTTOM_LEFT);
        contractTabLay.addComponent(glInst, 2, 2, 5, 2);
        if (currentUser.isPermitted(Settings.discountsTable + ":" + Settings.prmMenu) ||
                currentUser.isPermitted(Settings.correctionsTable + ":" + Settings.prmMenu)) {

            VerticalLayout discountsLay = new VerticalLayout();
            discountsLay.setSpacing(true);
            discountsLay.setSizeFull();
            discountsLay.addComponent(plusDiscButton);
            discountsLay.setComponentAlignment(plusDiscButton, Alignment.BOTTOM_LEFT);
            discountsLay.addComponent(discountsTable);
            discountsLay.setExpandRatio(discountsTable, 1);

            VerticalLayout correctionsLay = new VerticalLayout();
            correctionsLay.setSpacing(true);
            correctionsLay.setSizeFull();
            correctionsLay.addComponent(plusCorrectionButton);
            correctionsLay.setComponentAlignment(plusCorrectionButton, Alignment.BOTTOM_LEFT);
            correctionsLay.addComponent(correctionsTable);
            correctionsLay.setExpandRatio(correctionsTable, 1);

            Accordion accordion = new Accordion();
            accordion.setSizeFull();
            if (currentUser.isPermitted(Settings.discountsTable + ":" + Settings.prmMenu)) {
                accordion.addTab(discountsLay, myUI.getMessage(SptMessages.Discounts));
            }
            if (currentUser.isPermitted(Settings.correctionsTable + ":" + Settings.prmMenu)) {
                accordion.addTab(correctionsLay, myUI.getMessage(SptMessages.Correction));
            }
            accordion.setSelectedTab(discountsLay);
            contractTabLay.addComponent(accordion, 0, 1, 1, 2);
        }

        contractTabLay.setColumnExpandRatio(0, 1);
        contractTabLay.setColumnExpandRatio(1, 1);
        contractTabLay.setColumnExpandRatio(2, 1);
        contractTabLay.setColumnExpandRatio(3, 1);
        contractTabLay.setColumnExpandRatio(4, 0.2f);
        contractTabLay.setColumnExpandRatio(5, 1.5f);

    }

    private Boolean validateDiscountsTable() {
        ArrayList<Integer> discount_ids = new ArrayList<>();
        for (Object obj : discountsTable.getItemIds()) {
            if (!((TextField) discountsTable.getItem(obj).getItemProperty(
                    myUI.getMessage(SptMessages.Amount)).getValue()).isValid()) {
                Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            }
            if (((TextField) discountsTable.getItem(obj).getItemProperty(
                    myUI.getMessage(SptMessages.Amount)).getValue()).isValid()) {
                if (discount_ids.contains((Integer) ((ComboBox) discountsTable.getItem(obj).getItemProperty(
                        myUI.getMessage(SptMessages.Title)).getValue()).getValue())) {
                    Notification.show(myUI.getMessage(SptMessages.NotificationSameDiscountsAreNotAllowed),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                } else {
                    discount_ids.add((Integer) ((ComboBox) discountsTable.getItem(obj).getItemProperty(
                            myUI.getMessage(SptMessages.Title)).getValue()).getValue());
                }
            }
            if (!((ComboBox) discountsTable.getItem(obj).getItemProperty(
                    myUI.getMessage(SptMessages.Title)).getValue()).isValid()) {
                Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            }
            if (((Button) ((HorizontalLayout) discountsTable.getItem(obj).getItemProperty(
                    myUI.getMessage(SptMessages.Document)).getValue()).getComponent(0)).getData() == null) {
                Notification.show(myUI.getMessage(SptMessages.NotificationUploadDocument),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private Boolean validateCorrectionsTable() {
        if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
            ArrayList<Integer> correction_ids = new ArrayList<>();
            for (Object obj : correctionCont.getItemIds()) {
                if (!((TextField) correctionCont.getItem(obj).getItemProperty(
                        myUI.getMessage(SptMessages.Amount)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (((TextField) correctionCont.getItem(obj).getItemProperty(
                        myUI.getMessage(SptMessages.Amount)).getValue()).isValid()) {
                    if (correction_ids.contains((Integer) ((ComboBox) correctionCont.getItem(obj).getItemProperty(
                            myUI.getMessage(SptMessages.Title)).getValue()).getValue())) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationSameCorrectionsAreNotAllowed),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    } else {
                        correction_ids.add((Integer) ((ComboBox) correctionCont.getItem(obj).getItemProperty(
                                myUI.getMessage(SptMessages.Title)).getValue()).getValue());
                    }
                }
                if (!((ComboBox) correctionCont.getItem(obj).getItemProperty(
                        myUI.getMessage(SptMessages.Title)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    private Boolean validateInstallmentTable() {
        if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
            ArrayList<String> dates = new ArrayList<>();
            Iterator<?> iter = instPlanCont.getItemIds().iterator();
            Double amount = 0.0;
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (!((TextField) installmentTable.getItem(obj).getItemProperty(
                        myUI.getMessage(SptMessages.Amount)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((DateField) installmentTable.getItem(obj).getItemProperty(
                        myUI.getMessage(SptMessages.Date)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (((DateField) installmentTable.getItem(obj).getItemProperty(
                        myUI.getMessage(SptMessages.Date)).getValue()).isValid()) {
                    if (dates.contains(Settings.df.format(((DateField) installmentTable.getItem(obj).getItemProperty(
                            myUI.getMessage(SptMessages.Date)).getValue()).getValue()))) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationSameDatesAreNotAllowed),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    } else {
                        dates.add(Settings.df.format(((DateField) installmentTable.getItem(obj).getItemProperty(
                                myUI.getMessage(SptMessages.Date)).getValue()).getValue()));
                    }
                }
                if ((Integer) installmentTable.getItem(obj).getItemProperty(
                        Settings.status_id).getValue() != 0) {
                    amount += (Double) (((TextField) installmentTable.getItem(obj).getItemProperty(
                            myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue());
                }
            }
            recount();
            if (Settings.round(instCtrAmount, 2) != Settings.round(amount, 2)) {
                Notification.show(myUI.getMessage(SptMessages.NotificationWrongSumInstSum),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private Boolean validateContractsTab(ComponentContainer layout) {
        if (tabs.getSelectedTab() == tabs.getTab(layout).getComponent()) {
            for (Component next : layout) {
                if (next instanceof ComboBox) {
                    if (!((ComboBox) next).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                } else if (next instanceof TextField) {
                    if (!((TextField) next).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void insertContractToDb() {
        try {
            DbStudentContract dbc = new DbStudentContract();
            dbc.connect();
            if (isNewContract) {
                dbc.exec_delete((Integer) studDataTable.getValue(), myUI.getUser().getCurrent_year().getId());
                dbc.exec_insert_st_contract(myUI, getStudentContract((Integer) studDataTable.getValue(),
                        myUI.getUser().getCurrent_year().getId()));
            } else {
                dbc.exec_update_st_contract(getStudentContract((Integer) studDataTable.getValue(),
                        myUI.getUser().getCurrent_year().getId()));

            }
            dbc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private StudentContract getStudentContract(int st_id, int year_id) {
        double debt = 0.0;
        try {
            DbStudentContract dbsc = new DbStudentContract();
            dbsc.connect();
            debt = dbsc.exec_get_debt((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            dbsc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        StudentContract c = new StudentContract();
        c.setStudent_id(st_id);
        c.setYear_id(year_id);
        c.setContract_id((Integer) contractCB.getValue());
        c.setDebt(debt);
        c.setEmployee_id(myUI.getUser().getId());
        c.setStatus_id(2);
        c.setContr_with_disc(contr_with_disc);

        return c;
    }

    private void setContractTab(int st_id, int year_id) {
        int contract_id = 0;
        StudentPayment ip = null;
        try {
            DbStudentContract dbsc = new DbStudentContract();
            DbStudentPayment dbsp = new DbStudentPayment();
            dbsc.connect();
            dbsp.connect();
            contract_id = dbsc.execSQL_get_st_contract(st_id, year_id);
            ip = dbsp.exec_get_init_payment(st_id, year_id);
            dbsc.close();
            dbsp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (contract_id != 0) {
            contractCB.removeValueChangeListener(this);
            contractCB.setValue(contract_id);
            contractCB.addValueChangeListener(this);
        }
        initialPaymentTF.removeValueChangeListener(this);
        if (ip == null) {
            initialPaymentTF.setValue(null);
            initialPaymentTF.addValueChangeListener(this);
            initialPaymentTF.setData(null);
        } else {
            initialPaymentTF.getPropertyDataSource().setValue(ip.getAmount());
            initialPaymentTF.addValueChangeListener(this);
            initialPaymentTF.setData(ip);
        }
    }

    private StudentDiscount getStudentDiscount(int st_id, int year_id, String disc_id) {
        StudentDiscount d = new StudentDiscount();
        //if discount type is Free$|%. setFreeAmount()
        if ((((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue())
                .getContainerProperty(((ComboBox) discountsTable
                                .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                        myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("3"))
                || (((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue())
                .getContainerProperty(((ComboBox) discountsTable
                                .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                        myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("4"))) {
            d.setFree_entry_amount((Double) (((TextField) discountsTable.getContainerProperty(disc_id,
                    myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue()));
        }
        d.setDiscount_id(Integer.parseInt(((ComboBox) discountsTable
                .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue()).getValue().toString()));
        d.setStudent_id(st_id);
        d.setEmployee_id(myUI.getUser().getId());
        d.setYear_id(year_id);
        d.setId(disc_id);
        d.setNote(((TextField) discountsTable
                .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Note)).getValue()).getValue());
        Button b = (Button) ((HorizontalLayout) discountsTable.getContainerProperty(disc_id,
                myUI.getMessage(SptMessages.Document)).getValue()).getComponent(0);
        if (b.getData() != null) {
            d.setAttachment_id(((Attachment) b.getData()).getId());
        }
        if (contractCB.getValue() != null) {
            discountAmount = (Double) (((TextField) discountsTable.getContainerProperty(disc_id,
                    myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue());
            if ((((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue())
                    .getContainerProperty(((ComboBox) discountsTable
                                    .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("1"))
                    || (((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue())
                    .getContainerProperty(((ComboBox) discountsTable
                                    .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("3"))) {
                d.setDiscount_value(Settings.round((contr_with_disc * discountAmount / 100), 2));
                contr_with_disc -= contr_with_disc * discountAmount / 100;
            } else if ((((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue())
                    .getContainerProperty(((ComboBox) discountsTable
                                    .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("2"))
                    || (((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue())
                    .getContainerProperty(((ComboBox) discountsTable
                                    .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("4"))) {
                d.setDiscount_value(Settings.round((discountAmount), 2));
                contr_with_disc = contr_with_disc - discountAmount;
            }

        }
        return d;
    }

    private StudentCorrection getStudentCorrection(int st_id, int year_id, String corr_id) {
        StudentCorrection c = new StudentCorrection();
        c.setAmount((Double) (((TextField) correctionsTable.getContainerProperty(corr_id,
                myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue()));
        c.setCorrection_type_id(Integer.parseInt(((ComboBox) correctionsTable
                .getContainerProperty(corr_id, myUI.getMessage(SptMessages.Title)).getValue()).getValue().toString()));
        c.setStudent_id(st_id);
        c.setEmployee_id(myUI.getUser().getId());
        c.setYear_id(year_id);
        c.setId(corr_id);
        c.setNote(((TextField) correctionsTable
                .getContainerProperty(corr_id, myUI.getMessage(SptMessages.Note)).getValue()).getValue());
        return c;
    }

    private void recount() {
        StudentContract studentContract = new StudentContract();
        StudentPayment sp;
        IndexedContainer discCont = new IndexedContainer();
        try {
            DbStudentContract dbsc = new DbStudentContract();
            DbStudentDiscount dbsd = new DbStudentDiscount();
            DbStudentPayment dbsp = new DbStudentPayment();
            dbsc.connect();
            dbsd.connect();
            dbsp.connect();
            studentContract = dbsc.exec_recount_contract((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            discCont = dbsd.exec_disc_strCont(myUI, (Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            debt = dbsc.exec_get_debt((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            sp = dbsp.exec_recount_payment((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            ttl_payment = sp.getTtl_pay();
            init_payment = sp.getInit_pay();
            contract_amount = studentContract.getAmount();
            toPay = studentContract.getContr_with_disc() + studentContract.getCorrection() + debt;
            ttl_left = (studentContract.getContr_with_disc() + studentContract.getCorrection() + debt) - ttl_payment;
            dbsc.close();
            dbsd.close();
            dbsp.close();
            contr_id = studentContract.getContract_id();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        discountsStr = new StringBuilder();
        Iterator<?> iter = discCont.getItemIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 1) {
                discountsStr.append(Settings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue())).append("%");
                if (iter.hasNext()) {
                    discountsStr.append(", ");
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 2) {
                discountsStr.append(Settings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue())).append("$");
                if (iter.hasNext()) {
                    discountsStr.append(", ");
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 3) {
                discountsStr.append(Settings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.FreeAmount)).getValue())).append("%");
                if (iter.hasNext()) {
                    discountsStr.append(", ");
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 4) {
                discountsStr.append(Settings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.FreeAmount)).getValue())).append("$");
                if (iter.hasNext()) {
                    discountsStr.append(", ");
                }
            }
        }
        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.prmContractInfo)) {
            String value = myUI.getMessage(SptMessages.Contract) + ": " +
                    Settings.dFormat.format(studentContract.getAmount()) + " $";
            if (studentContract.getCreationDate() != null) {
                value += " (" + Settings.df.format(studentContract.getCreationDate()) + ")";
            }
            contractLab.setValue(value);
            discountLab.setValue(myUI.getMessage(SptMessages.Discount) + ": " + discountsStr);
            correctionLab.setValue(myUI.getMessage(SptMessages.Correction) + ": " + (studentContract.getCorrectionDetails() == null ? "0.00 $" : studentContract.getCorrectionDetails()));
            if (debt > 0) {
                debtLab.setStyleName(ValoTheme.LABEL_FAILURE);
            } else {
                debtLab.setStyleName(ValoTheme.LABEL_SUCCESS);
            }
            debtLab.setValue(myUI.getMessage(SptMessages.PreviousYearDebt) + ": " + Settings.dFormat.format(debt) + " $");
            netLab.setValue(myUI.getMessage(SptMessages.Net) + ": " + Settings.dFormat.format(studentContract.getContr_with_disc() + studentContract.getCorrection() + debt) + " $");
            paidLab.setValue(myUI.getMessage(SptMessages.Paid) + ": " + Settings.dFormat.format(ttl_payment) + " $");
        }
        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.prmContractInfoLeftDebt)) {
            leftLab.setValue(myUI.getMessage(SptMessages.Left) + ": " + Settings.dFormat.format((studentContract.getContr_with_disc() + studentContract.getCorrection() + debt) - ttl_payment) + " $");
            if ((studentContract.getPlan_debt() - ttl_payment) > 0) {
                planDebt.setStyleName(ValoTheme.LABEL_FAILURE);
                planDebt.setValue(myUI.getMessage(SptMessages.InstPlanDebt) + ": " + Settings.dFormat.format(studentContract.getPlan_debt() - ttl_payment) + " $");
            } else {
                planDebt.setStyleName(ValoTheme.LABEL_SUCCESS);
                planDebt.setValue(myUI.getMessage(SptMessages.InstPlanDebt) + ": " + Settings.dFormat.format(0.0) + " $");
            }
        }
    }

    private void updateStudEduStatus() {
        try {
            DbStudent dbs = new DbStudent();
            dbs.connect();
            dbs.exec_update((Integer) studDataTable.getValue(), 2, myUI.getUser().getId());
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        int eduStatusActive = 2;
        statusCB.setValue(eduStatusActive);
        eduStatCont.getContainerProperty(studDataTable
                        .getContainerProperty(studDataTable.getValue(),
                                Settings.education_status_id).getValue(), Settings.count)
                .setValue(((Integer) eduStatCont.getContainerProperty(studDataTable
                                .getContainerProperty(studDataTable.getValue(),
                                        Settings.education_status_id).getValue(),
                        Settings.count).getValue()) - 1);
        eduStatCont.getContainerProperty(2, Settings.count)
                .setValue(((Integer) eduStatCont.getContainerProperty(2,
                        Settings.count).getValue()) + 1);
        repaint();
        studDataTable.getContainerProperty(studDataTable.getValue(),
                Settings.education_status_id).setValue(statusCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.EducationStatus)).setValue(statusCB
                .getContainerProperty(statusCB.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());

    }

    private void addRowIfTableEmpty() {
        if (tabs.getSelectedTab() == tabs.getTab(acsGiveTableLay).getComponent()
                && acsGiveTable.size() == 0) {
            plusMatGiveButton.click();
        } else if (tabs.getSelectedTab() == tabs.getTab(acsReceiveTableLay).getComponent()
                && acsReceiveTable.size() == 0) {
            plusMatReceiveButton.click();
        } else if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                && installmentTable.size() == 0) {
            plusInstButton.click();
        } else if (tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent()
                && paymentsTable.size() == 0) {
            plusPayButton.click();
        }
    }

    private void recountInstPlanLabel() {
        if (contractCB.getValue() != null) {
            instCtrAmount = 0.0;
            netContrAmount = 0.0;
            instFirstPay = 0.0;
            instPlanContSum = 0.0;
            instCtrAmount = Double.parseDouble(contractCB.getContainerProperty(contractCB.getValue(),
                    myUI.getMessage(SptMessages.Amount)).getValue().toString());

            if (discountsTable.size() > 0) {
                for (Object next : discountsTable.getItemIds()) {
                    if (((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue() != null
                            && (!((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue().equals(""))) {
                        discountAmount = (Double) (((TextField) discountsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue());
                        if ((((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue())
                                .getContainerProperty(((ComboBox) discountsTable
                                                .getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                                        myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("1"))
                                || (((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue())
                                .getContainerProperty(((ComboBox) discountsTable
                                                .getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                                        myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("3"))) {
                            instCtrAmount -= instCtrAmount * discountAmount / 100;
                        } else if ((((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue())
                                .getContainerProperty(((ComboBox) discountsTable
                                                .getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                                        myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("2"))
                                || (((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue())
                                .getContainerProperty(((ComboBox) discountsTable
                                                .getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue()).getValue(),
                                        myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("4"))) {
                            instCtrAmount = instCtrAmount - discountAmount;
                        }
                    }
                }
            }

            if (correctionsTable.size() > 0) {
                for (Object next : correctionsTable.getItemIds()) {
                    if (((TextField) correctionsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue() != null
                            && (!((TextField) correctionsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue().equals(""))) {
                        ComboBox cb = (ComboBox) correctionsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Title)).getValue();
                        instCtrAmount = instCtrAmount + (Double) (((TextField) correctionsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue())
                                * (Double) (cb.getContainerProperty(cb.getValue(), Settings.correction_type_id).getValue());
                    }
                }
            }
            if (installmentTable.size() > 0) {
                for (Object obj : installmentTable.getItemIds()) {
                    if (((TextField) installmentTable.getItem(obj).getItemProperty(
                            myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue() != null) {
                        if ((Integer) installmentTable.getItem(obj).getItemProperty(Settings.status_id).getValue() != 0) {
                            instPlanContSum += (Double) ((TextField) installmentTable.getItem(obj).getItemProperty(
                                    myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue();
                        }
                    }
                }
            }
            instCtrAmount += debt;
            netContrAmount = instCtrAmount;
            if (initialPaymentTF.getPropertyDataSource().getValue() != null
                    && !initialPaymentTF.getPropertyDataSource().getValue().equals("")) {
                instFirstPay = (Double) initialPaymentTF.getPropertyDataSource().getValue();
                instCtrAmount -= instFirstPay;
            }
            if (ttl_payment != null && ttl_payment != 0.0) {
                instCtrAmount -= (ttl_payment - init_payment);
            }
            netIPlanTtlLab.setValue(myUI.getMessage(SptMessages.ToPlan) + ": " + Settings.dFormat.format(Settings.round(instCtrAmount, 2)) + " $");
            instPlanTtlLab.setValue(myUI.getMessage(SptMessages.InstallmentPlanTotal) + ": " + Settings.dFormat.format(instPlanContSum) + " $");
            if (instPlanCont != null) {
                instPlanDifLab.setValue(myUI.getMessage(SptMessages.Difference) + ": " + Settings.dFormat.format(Settings.round(instCtrAmount, 2)
                        - Settings.round(instPlanContSum, 2)) + " $");
            }
            if (contractCB.getValue() != null) {
                tabContractLab.setValue(myUI.getMessage(SptMessages.Contract) + ": "
                        + Settings.dFormat.format(contractCB.getContainerProperty(contractCB.getValue(),
                        myUI.getMessage(SptMessages.Amount)).getValue()) + " $");
                tabContractNetLab.setValue(myUI.getMessage(SptMessages.Net) + ": "
                        + Settings.dFormat.format(netContrAmount) + " $");
            }
        } else {
            clearInstPlanInfo();
        }
    }

    private void buildInstPlanLayout() {

        instPlanLay = new GridLayout(2, 3);
        instPlanLay.setMargin(false);
        instPlanLay.setSizeFull();

        tabContractLab = new Label();
        tabContractLab.setSizeUndefined();
        tabContractLab.setContentMode(ContentMode.HTML);
        if (contractCB.getValue() != null) {
            tabContractLab.setValue(myUI.getMessage(SptMessages.Contract) + ": "
                    + Settings.dFormat.format(contractCB.getContainerProperty(contractCB.getValue(),
                    myUI.getMessage(SptMessages.Amount)).getValue()) + " $");
        }

        tabContractNetLab = new Label();
        tabContractNetLab.setSizeUndefined();
        tabContractNetLab.setContentMode(ContentMode.HTML);
        if (contractCB.getValue() != null) {
            tabContractNetLab.setValue(myUI.getMessage(SptMessages.Net) + ": "
                    + Settings.dFormat.format(netContrAmount + " $"));
        }

        netIPlanTtlLab = new Label();
        netIPlanTtlLab.setSizeUndefined();
        netIPlanTtlLab.setContentMode(ContentMode.HTML);
        if (instCtrAmount != null) {
            netIPlanTtlLab.setValue(myUI.getMessage(SptMessages.ToPlan) + ": " + Settings.dFormat.format(instCtrAmount) + " $");
        }

        instPlanTtlLab = new Label();
        instPlanTtlLab.setSizeUndefined();
        instPlanTtlLab.setContentMode(ContentMode.HTML);
        if (instPlanCont != null) {
            instPlanTtlLab.setValue(myUI.getMessage(SptMessages.InstallmentPlanTotal) + ": " + Settings.dFormat.format(instPlanContSum) + " $");
        }

        instPlanDifLab = new Label();
        instPlanDifLab.setSizeUndefined();
        instPlanDifLab.setContentMode(ContentMode.HTML);
        if (instPlanCont != null) {
            instPlanDifLab.setValue(myUI.getMessage(SptMessages.Difference) + ": " + (instCtrAmount - instPlanContSum) + " $");
        }

        instPlanLay.addComponent(tabContractLab, 0, 0);
        instPlanLay.addComponent(tabContractNetLab, 0, 1);
        instPlanLay.addComponent(netIPlanTtlLab, 1, 0);
        instPlanLay.addComponent(instPlanTtlLab, 1, 1);
        instPlanLay.addComponent(instPlanDifLab, 1, 2);
    }

    private void familyTableCheck(boolean b, Property property) {
        if (b) {
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Passport)).getValue()).setRequired(true);
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Passport)).getValue()).setRequiredError(myUI.getMessage(SptMessages.NotificationWrongValue));
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Passport)).getValue()).addValidator(new StringLengthValidator(
                    myUI.getMessage(SptMessages.NotificationWrongValue), 1, 100, false));

            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.WorkPlace)).getValue()).setRequired(true);
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.WorkPlace)).getValue()).setRequiredError(myUI.getMessage(SptMessages.NotificationWrongValue));
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.WorkPlace)).getValue()).addValidator(new StringLengthValidator(
                    myUI.getMessage(SptMessages.NotificationWrongValue), 1, 100, false));

            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Address)).getValue()).setRequired(true);
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Address)).getValue()).setRequiredError(myUI.getMessage(SptMessages.NotificationWrongValue));
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Address)).getValue()).addValidator(new StringLengthValidator(
                    myUI.getMessage(SptMessages.NotificationWrongValue), 1, 100, false));

            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Phone)).getValue()).setRequired(true);
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Phone)).getValue()).setRequiredError(myUI.getMessage(SptMessages.NotificationWrongValue));
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Phone)).getValue()).addValidator(new StringLengthValidator(
                    myUI.getMessage(SptMessages.NotificationWrongValue), 1, 100, false));
        } else {
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Passport)).getValue()).setRequired(false);
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Passport)).getValue()).setRequiredError("");
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Passport)).getValue()).removeAllValidators();

            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.WorkPlace)).getValue()).setRequired(false);
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.WorkPlace)).getValue()).setRequiredError("");
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.WorkPlace)).getValue()).removeAllValidators();

            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Address)).getValue()).setRequired(false);
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Address)).getValue()).setRequiredError("");
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Address)).getValue()).removeAllValidators();

            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Phone)).getValue()).setRequired(false);
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Phone)).getValue()).setRequiredError("");
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Phone)).getValue()).removeAllValidators();

        }
    }

    private void checkDiscountsTable(Property property) {
        if (discountsTable.size() > 0) {
            if ((Integer) ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                    myUI.getMessage(SptMessages.Title)).getValue())
                    .getContainerProperty(property.getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 1
                    || (Integer) ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                    myUI.getMessage(SptMessages.Title)).getValue())
                    .getContainerProperty(property.getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 2) {
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).setEnabled(false);
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).removeAllValidators();
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().setValue(
                        ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                                myUI.getMessage(SptMessages.Title)).getValue())
                                .getContainerProperty(property.getValue(),
                                        myUI.getMessage(SptMessages.Amount)).getValue());
            } else if ((Integer) ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                    myUI.getMessage(SptMessages.Title)).getValue())
                    .getContainerProperty(property.getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 3
                    || (Integer) ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                    myUI.getMessage(SptMessages.Title)).getValue())
                    .getContainerProperty(property.getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 4) {
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).removeAllValidators();

                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).addValidator(new DoubleRangeValidator(
                        myUI.getMessage(SptMessages.NotificationWrongValue), 0.1,
                        (Double) ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                                myUI.getMessage(SptMessages.Title)).getValue())
                                .getContainerProperty(property.getValue(),
                                        myUI.getMessage(SptMessages.Amount)).getValue()));
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).setEnabled(true);
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().setValue(null);
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).setRequired(true);
            }
        }
    }

    private void insertDiscounts() {
        try {
            DbStudentDiscount dbsd = new DbStudentDiscount();
            dbsd.connect();
            DbDefinition dbCon = new DbDefinition();
            dbCon.connect();
            if (delDiscIds.size() > 0) {
                for (int i = 0; i < delDiscIds.size(); i++) {
                    try {
                        if (delDiscIds.get(i).getAttachmentUniqueName() != null) {
                            File f = new File(Settings.PATH_TO_UPLOADS
                                    + delDiscIds.get(i).getAttachmentUniqueName());
                            f.delete();
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                    dbsd.exec_update_emp_id(myUI.getUser().getId(), delDiscIds.get(i).getId());
                    dbCon.exec_delete(delDiscIds.get(i).getId(), Settings.dbStudentDiscount);
                }
            }
            contr_with_disc = (Double) contractCB.getContainerProperty(contractCB.getValue(), myUI.getMessage(SptMessages.Amount)).getValue();
            if (discountsTable.getContainerDataSource().size() > 0) {
                for (Object next : discountsTable.getItemIds()) {
                    if (discountsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        dbsd.exec_update(getStudentDiscount((Integer) studDataTable.getValue(),
                                myUI.getUser().getCurrent_year().getId(), ((String) next)));
                    } else if (discountsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbsd.exec_insert_st_discount(getStudentDiscount((Integer) studDataTable.getValue(),
                                myUI.getUser().getCurrent_year().getId(), ((String) next)));
                    }
                }
            }
            delDiscIds.clear();
            dbsd.close();
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertCorrections() {
        try {
            DbStudentCorrection dbsd = new DbStudentCorrection();
            dbsd.connect();
            DbDefinition dbCon = new DbDefinition();
            dbCon.connect();
            if (delCorrectionIds.size() > 0) {
                for (int i = 0; i < delCorrectionIds.size(); i++) {
                    dbCon.exec_delete(delCorrectionIds.get(i), Settings.dbStudentCorrection);
                }
            }
            if (correctionsTable.getContainerDataSource().size() > 0) {
                for (Object next : correctionsTable.getItemIds()) {
                    if (correctionsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        dbsd.exec_update(getStudentCorrection((Integer) studDataTable.getValue(),
                                myUI.getUser().getCurrent_year().getId(), ((String) next)));
                    } else if (correctionsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbsd.exec_insert(getStudentCorrection((Integer) studDataTable.getValue(),
                                myUI.getUser().getCurrent_year().getId(), ((String) next)));
                    }
                }
            }
            delCorrectionIds.clear();
            dbCon.close();
            dbsd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void saveRelatives(int student_id) {
        try {
            DbStudentRelative dbsr = new DbStudentRelative();
            dbsr.connect();
            if (delRelIds.size() > 0) {
                for (int i = 0; i < delRelIds.size(); i++) {
                    dbsr.exec_delete(delRelIds.get(i));
                }
            }
            if (relativesTable.getContainerDataSource().size() > 0) {
                for (Object next : relativesTable.getItemIds()) {
                    if (relativesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        dbsr.exec_update(getRelative(Integer.parseInt(next.toString()), student_id, relativesTable.getItem(next)));
                    } else if (relativesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbsr.exec_insert(getRelative(0, student_id, relativesTable.getItem(next)));
                    }
                }
            }
            delRelIds.clear();
            dbsr.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertPayments(int student_id) {
        try {
            DbStudentPayment dbsp = new DbStudentPayment();
            DbAccTransactions dbat = new DbAccTransactions();
            dbsp.connect();
            for (int i = 0; i < delPayIds.size(); i++) {
                dbat.exec_delete(Settings.dbColumnStudent_payments_id, delPayIds.get(i), dbsp.getConnection());//delete transaction
                dbsp.exec_update_emp_id(myUI.getUser().getId(), delPayIds.get(i));
                dbsp.exec_delete(delPayIds.get(i));
            }
            if (paymentsTable.getContainerDataSource().size() > 0) {
                for (Object next : paymentsTable.getItemIds()) {
                    if (paymentsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        StudentPayment sp = getPayment(Integer.parseInt(next.toString()), student_id, paymentsTable.getItem(next));
                        AccTransaction tr = new AccTransaction();
                        tr.setAmount(sp.getAmount());
                        tr.setDate(sp.getModification_date());
                        tr.setCategory_id((Integer) ((ComboBox) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                                Settings.acc_category_id).getValue());
                        tr.setCurrency_id(2);
                        tr.setCurrency_rate(sp.getRate());
                        tr.setNote(sp.getNoteForCashBox());
                        tr.setEmployee_id(sp.getEmployee_id());
                        tr.setSchool_id(sp.getSchool_id());
                        tr.setStudent_payments_id(sp.getId());
                        dbsp.exec_update(sp);
                        if (sp.getModification_date().after(myUI.getUser().getTransactions_start_date())
                                || sp.getModification_date().equals(myUI.getUser().getTransactions_start_date())) {
                            int update_status = dbat.exec_update(tr, Settings.dbColumnStudent_payments_id,
                                    tr.getStudent_payments_id(), dbsp.getConnection());//update transaction normal
                            if (update_status == 0) {
                                dbat.exec_insert(tr, dbsp.getConnection());//insert transaction if it doesn't exist, on payment update if payment date is after transactions start date
                            }
                        } else if (sp.getModification_date().before(myUI.getUser().getTransactions_start_date())) {
                            dbat.exec_delete(Settings.dbColumnStudent_payments_id, (String) next, dbsp.getConnection()); //delete transaction on payment update if payment date is before transactions start date
                        }
                    } else if (paymentsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        int order_num = dbsp.getMaxOrderNum((Integer) studDataTable.getValue());
                        StudentPayment sp = getPayment(0, student_id, paymentsTable.getItem(next));
                        int payment_id = dbsp.exec_insert(sp, order_num);
                        sp.setId(payment_id);//payment Id
                        AccTransaction tr = new AccTransaction();
                        tr.setAmount(sp.getAmount());
                        tr.setDate(sp.getModification_date());
                        tr.setCategory_id((Integer) ((ComboBox) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                                Settings.acc_category_id).getValue());
                        tr.setCurrency_id(2);
                        tr.setCurrency_rate(sp.getRate());
                        tr.setNote(sp.getNoteForCashBox());
                        tr.setEmployee_id(sp.getEmployee_id());
                        tr.setSchool_id(sp.getSchool_id());
                        tr.setStudent_payments_id(sp.getId());
                        if (sp.getModification_date().after(myUI.getUser().getTransactions_start_date())
                                || sp.getModification_date().equals(myUI.getUser().getTransactions_start_date())) {
                            dbat.exec_insert(tr, dbsp.getConnection());//insert transaction
                        }
                    }
                }
            }
            delPayIds.clear();
            dbsp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private AccTransaction insertTestPayments(Date date, String payment_id) {
        AccTransaction lowBalance = null;
        try {
            DbAccTransactions dbat = new DbAccTransactions();
            dbat.connect();
            dbat.getConnection().setAutoCommit(false);
            try {
                for (int i = 0; i < delPayIds.size(); i++) {
                    dbat.exec_delete(Settings.dbColumnStudent_payments_id, delPayIds.get(i),
                            dbat.getConnection());//delete transaction
                }
                AccTransaction tr;
                for (Object next : paymentsTable.getItemIds()) {
                    if (!next.toString().equals(payment_id)) {
                        StudentPayment sp = getPayment(0, 0, paymentsTable.getItem(next));
                        tr = new AccTransaction();
                        tr.setAmount(Double.parseDouble(((TextField) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Amount)).getValue()).getValue()));
                        tr.setDate(sp.getModification_date());
                        tr.setCategory_id((Integer) ((ComboBox) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                                Settings.acc_category_id).getValue());
                        tr.setCurrency_id(2);
                        tr.setCurrency_rate(sp.getRate());
                        tr.setNote(sp.getNoteForCashBox());
                        tr.setEmployee_id(sp.getEmployee_id());
                        tr.setSchool_id(sp.getSchool_id());
                        if (paymentsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                                .equals(myUI.getMessage(SptMessages.Update))) {
                            tr.setStudent_payments_id(Integer.parseInt(next.toString()));
                            if (sp.getModification_date().after(myUI.getUser().getTransactions_start_date())
                                    || sp.getModification_date().equals(myUI.getUser().getTransactions_start_date())) {
                                int update_status = dbat.exec_update(tr, Settings.dbColumnStudent_payments_id,
                                        tr.getStudent_payments_id(), dbat.getConnection());//update transaction normal
                                if (update_status == 0) {
                                    dbat.exec_insert(tr, dbat.getConnection());//insert transaction if it doesn't exist, on payment update if payment date is after transactions start date
                                }
                            } else if (sp.getModification_date().before(myUI.getUser().getTransactions_start_date())) {
                                dbat.exec_delete(Settings.dbColumnStudent_payments_id, (String) next, dbat.getConnection()); //delete transaction on payment update if payment date is before transactions start date
                            }
                        } else if (paymentsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                                .equals(myUI.getMessage(SptMessages.Insert))) {
                            if (sp.getModification_date().after(myUI.getUser().getTransactions_start_date())
                                    || sp.getModification_date().equals(myUI.getUser().getTransactions_start_date())) {
                                dbat.exec_insert(tr, dbat.getConnection());//insert transaction
                            }
                        }
                    }
                }
                lowBalance = dbat.exec_low_balance(dbat.getConnection(), myUI.getUser().getSchool_id(), date, 0.0, 0.0, 2);
                dbat.getConnection().rollback();
            } catch (Exception e) {
                dbat.getConnection().rollback();
                logger.error(e);
                logger.catching(e);
            }
            dbat.getConnection().setAutoCommit(true);
            dbat.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        return lowBalance;
    }

    private void insertCalls(int student_id) {
        try {
            DbStudentCalls dbsc = new DbStudentCalls();
            dbsc.connect();
            if (delCallIds.size() > 0) {
                for (int i = 0; i < delCallIds.size(); i++) {
                    dbsc.exec_delete(delCallIds.get(i));
                }
            }
            if (callsTable.getContainerDataSource().size() > 0) {
                for (Object next : callsTable.getItemIds()) {
                    if (callsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        dbsc.exec_update(((TextField) callsTable.getItem(next).getItemProperty(
                                myUI.getMessage(SptMessages.Note)).getValue()).getValue(), Integer.parseInt(next.toString()));
                    } else if (callsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbsc.exec_insert(student_id, myUI.getUser().getCurrent_year().getId(), myUI.getUser().getId(),
                                ((TextField) callsTable.getItem(next).getItemProperty(
                                        myUI.getMessage(SptMessages.Note)).getValue()).getValue());
                    }
                }

            }
            delCallIds.clear();
            dbsc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void repaint() {
        eduStatTtlLab.setValue("active:" + eduStatCont.getContainerProperty(2, Settings.count).getValue().toString()
                + "&emsp;" + "pre-registered:" + eduStatCont.getContainerProperty(1, Settings.count).getValue().toString()
                + "&emsp;" + "not confirmed:" + eduStatCont.getContainerProperty(3, Settings.count).getValue().toString()
                + "&emsp;" + "graduated:" + eduStatCont.getContainerProperty(5, Settings.count).getValue().toString()
                + "&emsp;" + "out of:" + eduStatCont.getContainerProperty(4, Settings.count).getValue().toString()
                + "&emsp;" + "total:" + eduStatCont.getContainerProperty(6, Settings.count).getValue().toString());
    }

    private void clearContractInfo() {
        contractLab.setValue(myUI.getMessage(SptMessages.Contract) + ":");
        discountLab.setValue(myUI.getMessage(SptMessages.Discount) + ":");
        debtLab.setValue(myUI.getMessage(SptMessages.PreviousYearDebt) + ":");
        netLab.setValue(myUI.getMessage(SptMessages.Net) + ":");
        paidLab.setValue(myUI.getMessage(SptMessages.Paid) + ":");
        leftLab.setValue(myUI.getMessage(SptMessages.Left) + ":");
        planDebt.setValue(myUI.getMessage(SptMessages.InstPlanDebt) + ":");
    }

    private void clearInstPlanInfo() {
        netIPlanTtlLab.setValue(myUI.getMessage(SptMessages.ToPlan) + ": 0.00 $");
        instPlanTtlLab.setValue(myUI.getMessage(SptMessages.InstallmentPlanTotal) + ": 0.00 $");
        instPlanDifLab.setValue(myUI.getMessage(SptMessages.Difference) + ": 0.00 $");
        tabContractLab.setValue(myUI.getMessage(SptMessages.Contract) + ": 0.00 $");
        tabContractNetLab.setValue(myUI.getMessage(SptMessages.Net) + ": 0.00 $");

    }

    private void updateNetPaymentDb(double ttl_pay, int stud_id, int year_id) {
        try {
            DbStudentContract dbsc = new DbStudentContract();
            dbsc.connect();
            dbsc.execUpdateNetPayments(ttl_pay, stud_id, year_id);
            dbsc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setContractCb(int contract_id) {
        try {
            DbContract dbc = new DbContract();
            dbc.connect();
            contractCB.setContainerDataSource(
                    dbc.exec_contr_select(myUI, myUI.getUser().getCurrent_year().getId(),
                            myUI.getUser().getSchool_id(), contract_id));
            dbc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertNewStOrder(int id) {
        try {
            DbStudentOrder dbso = new DbStudentOrder();
            dbso.connect();
            dbso.insertNewStOrder(id, (Integer) classCB.getValue(),
                    myUI.getUser().getCurrent_year().getId(), myUI.getUser().getId());
            dbso.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertNewStCtrOrder() {
        try {
            DbStudentOrder dbso = new DbStudentOrder();
            dbso.connect();
            dbso.insertNewStCtrOrder((Integer) studDataTable.getValue(), (Integer) classCB.getValue(),
                    ((Integer) statusCB.getValue()), myUI.getUser().getCurrent_year().getId(), myUI.getUser().getId());
            dbso.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setStudDataTable(String edu_st_ids) {
        edu_st_ids = edu_st_ids.substring(1, edu_st_ids.length() - 1);
        try {
            DbStudent dbs = new DbStudent();
            dbs.connect();
            studDataTable.setContainerDataSource(
                    dbs.execSQL(myUI, myUI.getUser().getSchool_id(), this, edu_st_ids));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        studDataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
    }

    private String generateStudId() {
        String generated_id = null;
        try {
            DbStudent dbCon = new DbStudent();
            dbCon.connect();
            String school_code = (Integer) classCB.getContainerProperty(
                    classCB.getValue(), myUI.getMessage(SptMessages.ClassNumber)).getValue() < 7 ?
                    myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool_id(),
                            myUI.getMessage(SptMessages.PrimaryCode)).getValue().toString() :
                    myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool_id(),
                            myUI.getMessage(SptMessages.SecondaryCode)).getValue().toString();
            String school_level = null;
            if ((Integer) classCB.getContainerProperty(
                    classCB.getValue(), myUI.getMessage(SptMessages.ClassNumber)).getValue() < 7 &&
                    !myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool_id(),
                            myUI.getMessage(SptMessages.PrimaryCode)).getValue().equals(
                            myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool_id(),
                                    myUI.getMessage(SptMessages.SecondaryCode)).getValue())) {
                school_level = myUI.getMessage(SptMessages.PrimaryCode);
            } else if ((Integer) classCB.getContainerProperty(
                    classCB.getValue(), myUI.getMessage(SptMessages.ClassNumber)).getValue() >= 7 &&
                    !myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool_id(),
                            myUI.getMessage(SptMessages.PrimaryCode)).getValue().equals(
                            myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool_id(),
                                    myUI.getMessage(SptMessages.SecondaryCode)).getValue())) {
                myUI.getMessage(SptMessages.SecondaryCode);
            }
            int year_ord = Integer.parseInt(myUI.getUser().getCurrent_year().getName().substring(2, 4));
            String class_num = Integer.toString(year_ord - (Integer) classCB.getContainerProperty(
                    classCB.getValue(), Settings.class_order_number).getValue());
            char cl = (Integer) classCB.getContainerProperty(classCB.getValue(), Settings.class_type_id).getValue() >= 4
                    ? '0' : class_num.charAt(class_num.length() - 1);
            int order_number = 1;
            do {
                generated_id = school_code + year_ord + cl + String.format("%03d", dbCon.execSQL_login(myUI,
                        myUI.getUser().getCurrent_year().getId(),
                        myUI.getUser().getSchool_id(), (Integer) classCB.getContainerProperty(
                                classCB.getValue(), Settings.class_type_id).getValue(),
                        order_number, (Integer) classCB.getContainerProperty(
                                classCB.getValue(), Settings.min).getValue(),
                        (Integer) classCB.getContainerProperty(
                                classCB.getValue(), Settings.max).getValue(), school_level));
                order_number++;
                if (order_number == 100) {
                    break;
                }
            } while (dbCon.isLoginExists(generated_id));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        return generated_id;
    }
}
