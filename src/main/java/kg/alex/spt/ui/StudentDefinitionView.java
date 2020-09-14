package kg.alex.spt.ui;

import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.ComboBoxMultiselectMax;
import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbAccTransactions;
import kg.alex.spt.dao.DbAccessories;
import kg.alex.spt.dao.DbClassName;
import kg.alex.spt.dao.DbContract;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbDiscount;
import kg.alex.spt.dao.DbPaymentCategory;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.dao.DbStudRelative;
import kg.alex.spt.dao.DbStudAccessories;
import kg.alex.spt.dao.DbStudContract;
import kg.alex.spt.dao.DbStudDiscount;
import kg.alex.spt.dao.DbStudInfoPdf;
import kg.alex.spt.dao.DbStudInstallmentPlan;
import kg.alex.spt.dao.DbStudPayment;
import kg.alex.spt.dao.DbStudent;
import kg.alex.spt.dao.DbStudentCalls;
import kg.alex.spt.dao.DbStudentOrder;
import kg.alex.spt.domain.AccTransaction;
import kg.alex.spt.domain.InvoiceInfoPdf;
import kg.alex.spt.domain.StudInstallmentPlan;
import kg.alex.spt.domain.StudRelative;
import kg.alex.spt.domain.StudAccessories;
import kg.alex.spt.domain.StudContract;
import kg.alex.spt.domain.StudDiscount;
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.domain.StudPayment;
import kg.alex.spt.domain.Student;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ContractCambridgePdfEn;
import kg.alex.spt.utils.ContractCambridgePdfRu;
import kg.alex.spt.utils.ContractLisePdfKg;
import kg.alex.spt.utils.ContractLisePdfRu;
import kg.alex.spt.utils.ContractSchoolPdfKg;
import kg.alex.spt.utils.ContractSchoolPdfRu;
import kg.alex.spt.utils.ContractSilkPdfRu;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MakeInvoicePDF;
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

public class StudentDefinitionView extends VerticalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(StudentDefinitionView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn, divideBtn;
    private FilterTable studDataTable;
    private OptionGroup optionGroup;
    private TextField nameTF, loginTF, surnameTF, middlenameTF, divideTF, initialPaymentTF;
    private DateField birthDate, currDate;
    private ComboBoxMax genderCB, classCB, statusCB, contractCB;
    private OptionGroup contLangOG;
    private FormLayout fieldsLay1, fieldsLay2;
    private int r_table_counter = 1000, receive = 2, give = 1, discCounter, contr_id;
    private Double left, instCtrAmount, netContrAmount,
            instFirstPay, instPlanContSum, contr_with_disc, ttl_left,
            ttl_payment, init_payment, discountAmount, debt, kOplate;
    private Button plusRelButton, plusMatGiveButton, plusInstButton, plusPayButton,
            plusMatReceiveButton, plusDiscButton, plusCallButton;
    private FormattedTable relativesTable, acsGiveTable, installmentTable,
            paymentsTable, acsReceiveTable, discountsTable, callsTable;
    private TabSheet tabs;
    private ArrayList<String> delPayIds = new ArrayList<>();
    private ArrayList<String> delCallIds = new ArrayList<>();
    private ArrayList<String> delDiscIds = new ArrayList<>();
    private ArrayList<String> delRelIds = new ArrayList<>();
    private PopupButton printButton;
    private IndexedContainer productsContainer = null,
            acsGivContainer = null, acsRecContainer = null, instPlanCont = null,
            paymentCont = null, discountCont = null, callsCont = null;
    private boolean isNew, isNewContract = false;
    private Label debtLab, contractLab, discountLab, netLab, paidLab, leftLab,
            instPlanTtlLab, netIPlanTtlLab, planDebt, instPlanDifLab, eduStatTtlLab,
            tabContractLab, tabContractNetLab, filteredLab;
    private SystemSettings sysSettings = new SystemSettings();
    private String[] NATURAL_COL_ORDER, NATURAL_COL_ORDER_PAYMENTS, NATURAL_COL_ORDER_CALLS,
            NATURAL_COL_ORDER_INST_PLAN, NATURAL_COL_ORDER_DISCOUNTS, NATURAL_COL_ORDER_RElATIVES;
    private VerticalLayout contractLay, famTableLay, acsGiveTableLay, payTablelay,
            acsReceiveTableLay, callsTableLay;
    private GridLayout gridStudLay, contractTabLay, instPlanLay, studSearchLay;
    private HorizontalSplitPanel horSplitPanel;
    private HorizontalLayout buttonsLay;
    private Upload photoUpl;
    private File myFile;
    private Window statusWindow;
    private Button cancelButton;
    private Label state, textualProgress, fName;
    private ProgressIndicator pi;
    private String photoName, mimeType, discounts = "";
    private MyReceiver receiver;
    private Embedded photoEmb;
    private Subject currentUser = SecurityUtils.getSubject();
    public IndexedContainer eduStatCont;

    public StudentDefinitionView(final MyVaadinUI myUI) {
        this.myUI = myUI;

        buildButtonsLayout();
        buildStudGridLayout();
        try {
            DbDefinition dbed = new DbDefinition();
            dbed.connect();
            eduStatCont = dbed.execSQL_statuses_with_count(myUI, sysSettings.dbEducationStatus, true);
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
            myUI.getMessage(SptMessages.Firstname),
            myUI.getMessage(SptMessages.Surname), myUI.getMessage(SptMessages.ClassName),
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
            eduContainer = dbd.exec_for_select(myUI, sysSettings.dbEducationStatus);
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        optionGroup = new OptionGroup();
        optionGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        optionGroup.setMultiSelect(true);
        optionGroup.addItems(eduContainer.getItemIds());
        Iterator iter = eduContainer.getItemIds().iterator();
        while (iter.hasNext()) {
            Integer next = (Integer) iter.next();
            optionGroup.setItemCaption(next, eduContainer.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Name)).getValue().toString());
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
        studSearchLayFooter.setWidth("100%");
        eduStatTtlLab = new Label();
        eduStatTtlLab.setSizeUndefined();
        eduStatTtlLab.setImmediate(true);
        eduStatTtlLab.setContentMode(ContentMode.HTML);
        filteredLab = new Label();
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
        famTableLay.setComponentAlignment(plusRelButton, Alignment.BOTTOM_RIGHT);
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
        acsGiveTableLay.setComponentAlignment(plusMatGiveButton, Alignment.BOTTOM_RIGHT);
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
        acsReceiveTableLay.setComponentAlignment(plusMatReceiveButton, Alignment.BOTTOM_RIGHT);
        acsReceiveTableLay.addComponent(acsReceiveTable);
        acsReceiveTableLay.setExpandRatio(acsReceiveTable, 1);

        buildContractTab();

        paymentsTable = new FormattedTable();
        paymentsTable.setSizeFull();
        paymentsTable.setStyleName(ValoTheme.TABLE_SMALL);
        paymentsTable.setNullSelectionAllowed(false);
        payTablelay = new VerticalLayout();
        payTablelay.setSizeFull();
        payTablelay.setSpacing(true);
        payTablelay.setMargin(true);
        payTablelay.addComponent(plusPayButton);
        payTablelay.setComponentAlignment(plusPayButton, Alignment.BOTTOM_RIGHT);
        payTablelay.addComponent(paymentsTable);
        payTablelay.setExpandRatio(paymentsTable, 1);

        callsTable = new FormattedTable();
        callsTable.setSizeFull();
        callsTable.setStyleName(ValoTheme.TABLE_SMALL);
        callsTable.setNullSelectionAllowed(false);
        callsTableLay = new VerticalLayout();
        callsTableLay.setSizeFull();
        callsTableLay.setSpacing(true);
        callsTableLay.setMargin(true);
        callsTableLay.addComponent(plusCallButton);
        callsTableLay.setComponentAlignment(plusCallButton, Alignment.BOTTOM_RIGHT);
        callsTableLay.addComponent(callsTable);
        callsTableLay.setExpandRatio(callsTable, 1);

        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabs.addTab(studSearchLay).setCaption(myUI.getMessage(SptMessages.Search));
        tabs.addTab(famTableLay).setCaption(myUI.getMessage(SptMessages.FamilyInfo));
        tabs.addTab(contractTabLay).setCaption(myUI.getMessage(SptMessages.Contract));
        tabs.addTab(payTablelay).setCaption(myUI.getMessage(SptMessages.Payment));
        tabs.addTab(acsGiveTableLay).setCaption(myUI.getMessage(SptMessages.MaterialsGive));
        tabs.addTab(acsReceiveTableLay).setCaption(myUI.getMessage(SptMessages.MaterialsReceive));
        tabs.addTab(callsTableLay).setCaption(myUI.getMessage(SptMessages.Calls));
        tabs.addSelectedTabChangeListener(
                new TabSheet.SelectedTabChangeListener() {
            public void selectedTabChange(SelectedTabChangeEvent event) {
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
                    setContractTab((Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId());
                    if (initialPaymentTF.isValid()) {
                        recountInstPlanLabel();
                    }
                    prepareNormalMode();
                    if ((Integer) studDataTable.getContainerProperty(studDataTable.getValue(),
                            sysSettings.education_status_id).getValue() == 4
                            || (Integer) studDataTable.getContainerProperty(studDataTable.getValue(),
                                    sysSettings.education_status_id).getValue() == 5) {
                        //if student outOf or graduated 
                        modifyBtn.setEnabled(false);
                    }
//                    checkForLastContract((Integer) studDataTable.getValue(), myUI.getUser().getCurrent_year().getId());

                } else if (event.getTabSheet().getSelectedTab() == payTablelay
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
            }
        });

        this.setSecondComponent(tabs);
        prepareNormalMode();
    }

    private void buildButtonsLayout() {

        buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);
        buttonsLay.setWidth("100%");

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

        contLangOG = new OptionGroup();
        contLangOG.setNullSelectionAllowed(true);
        contLangOG.addValueChangeListener(this);
        contLangOG.addItem(myUI.getMessage(SptMessages.LiseContrRu));
        contLangOG.addItem(myUI.getMessage(SptMessages.LiseContrKg));
        contLangOG.addItem(myUI.getMessage(SptMessages.SchoolContrRu));
        contLangOG.addItem(myUI.getMessage(SptMessages.SchoolContrKg));
        contLangOG.addItem(myUI.getMessage(SptMessages.SilkRoadRu));
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

        buildphotoLayout();
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

    private void buildphotoLayout() {
        photoEmb = new Embedded();
        photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoEmb.setImmediate(true);
        photoEmb.setHeight("100px");

        buildUpload();
    }

    private void buildFieldsLayout1() {
        fieldsLay1 = new FormLayout();
        fieldsLay1.setSpacing(false);

        loginTF = new TextField(myUI.getMessage(SptMessages.Id));
        loginTF.setRequired(true);
        loginTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        loginTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        loginTF.setWidth("100%");
        loginTF.addValueChangeListener(this);
        loginTF.addValidator(new RegexpValidator("[0-9]{1,25}", true,
                myUI.getMessage(SptMessages.NotifWrongValue)));
        fieldsLay1.addComponent(loginTF);

        nameTF = new TextField(myUI.getMessage(SptMessages.Firstname));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        nameTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        nameTF.setWidth("100%");
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 100, false));
        fieldsLay1.addComponent(nameTF);

        surnameTF = new TextField(myUI.getMessage(SptMessages.Surname));
        surnameTF.setRequired(true);
        surnameTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        surnameTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        surnameTF.setWidth("100%");
        surnameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 1, 100, false));
        fieldsLay1.addComponent(surnameTF);

        middlenameTF = new TextField(myUI.getMessage(SptMessages.Middlename));
        middlenameTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        middlenameTF.setWidth("100%");
        fieldsLay1.addComponent(middlenameTF);
    }

    private void buildFieldsLayout2() {
        fieldsLay2 = new FormLayout();
        fieldsLay2.setSpacing(false);

        genderCB = new ComboBoxMax(myUI.getMessage(SptMessages.Gender));
        genderCB.setNullSelectionAllowed(false);
        genderCB.setRequired(true);
        genderCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        genderCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        genderCB.setWidth("100%");
        genderCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        genderCB.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            genderCB.setContainerDataSource(
                    dbDef.exec_for_select(myUI, sysSettings.dbGender));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        fieldsLay2.addComponent(genderCB);

        birthDate = new DateField(myUI.getMessage(SptMessages.DateOfBirth));
        birthDate.setWidth("100%");
        birthDate.setStyleName(ValoTheme.DATEFIELD_TINY);
        birthDate.setRequired(true);
        birthDate.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        birthDate.setDateFormat(sysSettings.datePattern);
        birthDate.setValue(new Date());
        fieldsLay2.addComponent(birthDate);

        classCB = new ComboBoxMax(myUI.getMessage(SptMessages.ClassName));
        classCB.setNullSelectionAllowed(false);
        classCB.setRequired(true);
        classCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        classCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        classCB.setWidth("100%");
        classCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        classCB.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbClassName dbcn = new DbClassName();
            dbcn.connect();
            classCB.setContainerDataSource(
                    dbcn.execClass_sel(myUI, myUI.getUser().getSchool_id()));
            dbcn.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        fieldsLay2.addComponent(classCB);

        statusCB = new ComboBoxMax(myUI.getMessage(SptMessages.EducationStatus));
        statusCB.setNullSelectionAllowed(false);
        statusCB.setEnabled(false);
        statusCB.setRequired(true);
        statusCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        statusCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        statusCB.setWidth("100%");
        statusCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        statusCB.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            statusCB.setContainerDataSource(
                    dbDef.exec_for_select(myUI, sysSettings.dbEducationStatus));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        fieldsLay2.addComponent(statusCB);

    }

    private void buildContractLayout() {

        modifyBtn.setEnabled(true);
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

        if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.prmContractInfo)) {
            contractLay.addComponent(contractLab);
            contractLay.setComponentAlignment(contractLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(discountLab);
            contractLay.setComponentAlignment(discountLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(debtLab);
            contractLay.setComponentAlignment(debtLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(netLab);
            contractLay.setComponentAlignment(netLab, Alignment.BOTTOM_LEFT);

        }
        if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":"
                + sysSettings.prmContractInfoLeftDebt)) {
            contractLay.addComponent(leftLab);
            contractLay.setComponentAlignment(leftLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(planDebt);
            contractLay.setComponentAlignment(planDebt, Alignment.BOTTOM_LEFT);
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == modifyBtn) {
            if (studDataTable.getValue() != null) {
                isNew = false;
                photoUpl.setEnabled(true);
                fillFields();
                prepareModificationMode();
                classCB.setEnabled(false);
                addRowIftableEmpty();
                if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                        && contractCB.getValue() == null) {
                    isNewContract = true;
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
                                myUI.getMessage(SptMessages.Firstname)).getValue().toString()
                        + " " + studDataTable.getContainerProperty(studDataTable.getValue(),
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
        } else if (source == saveBtn) {
            try {
                if (validate(horSplitPanel)) {
                    if (validateRelativesTable(relativesTable)) {
                        if (validateAcsGiveTable(acsGiveTable)) {
                            if (validateAcsReceiveTable(acsReceiveTable)) {
                                if (validateContractsTab(contractTabLay)) {
                                    if (validatePaymentsTable(paymentsTable)) {
                                        DbStudent dbst = new DbStudent();
                                        dbst.connect();
                                        if (isNew) {
                                            int id = dbst.exec_insert(getStudent(0));
                                            if (id != 0) {
                                                insertNewStOrder(id);
                                                if (relativesTable.isEnabled()) {
                                                    saveRelatives(id);
                                                }
                                                addDatacontainerItem(id);
                                                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                                prepareNormalMode();
                                                eduStatCont.getContainerProperty(1, sysSettings.count)
                                                        .setValue(((Integer) eduStatCont.getContainerProperty(1,
                                                                sysSettings.count).getValue()) + 1);
                                                eduStatCont.getContainerProperty(6, sysSettings.count)
                                                        .setValue(((Integer) eduStatCont.getContainerProperty(6, sysSettings.count)
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
                                                    updateDatacontainer();
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
                                                    updateDatacontainer();
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
                                                saveDiscoutns();
                                                insertInitialPayment((Integer) studDataTable.getValue());
                                                insertContractToDb();
                                                execDeleteInstPlanFromDb((Integer) studDataTable.getValue(),
                                                        myUI.getUser().getCurrent_year().getId());
                                                insertInstPlanToDb((Integer) studDataTable.getValue());
                                                setInstPlanTable();
                                                recount();
                                                updateNetPaymentDb(ttl_payment, (Integer) studDataTable.getValue(),
                                                        myUI.getUser().getCurrent_year().getId());
                                                prepareNormalMode();
                                                insertNewStCtrOrder();
                                                updateStudEduStatus();
                                                Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            } //pressed save button on payments tab
                                            else if (tabs.getSelectedTab() == tabs.getTab(payTablelay).getComponent()) {
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
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == cancelBtn) {
            clearFields();
            delPayIds.clear();
            delCallIds.clear();
            delDiscIds.clear();
            delRelIds.clear();
            if (studDataTable.getValue() != null) {
                fillFields();
            }
            prepareNormalMode();
            if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
                recountInstPlanLabel();
            }
        } else if (source == printButton) {
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
        } else if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                && source.getCaption() == null) {
            installmentTable.removeItem(event.getButton().getData().toString());
            if (initialPaymentTF.isValid()) {
                recountInstPlanLabel();
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                && source.getCaption().equals(myUI.getMessage(SptMessages.Discount))) {
            discountsTable.removeItem(event.getButton().getData().toString());
            if (initialPaymentTF.isValid()) {
                recountInstPlanLabel();
            }
            discCounter--;
            delDiscIds.add((String) source.getData());
        } else if (tabs.getSelectedTab() == tabs.getTab(payTablelay).getComponent() && source.getCaption() == null) {
            AccTransaction tr = null;
            delPayIds.add(source.getData().toString());
            if (paymentsTable.getContainerProperty(source.getData().toString(), sysSettings.crud_status).getValue().toString()
                    .equals(myUI.getMessage(SptMessages.Update))) {
                tr = insertTestPayments((Date) paymentsTable.getContainerProperty(source.getData().toString(),
                        sysSettings.old_date).getValue(), source.getData().toString());
            } else {
                tr = insertTestPayments(((DateField) paymentsTable.getContainerProperty(source.getData().toString(),
                        myUI.getMessage(SptMessages.Date)).getValue()).getValue(), source.getData().toString());
            }
            if (tr == null) {
                paymentsTable.removeItem(event.getButton().getData().toString());
            } else {
                delPayIds.remove(source.getData().toString());
                Notification.show(myUI.getMessage(SptMessages.LowBalance) + sysSettings.dFormat.format(tr.getOverlimit())
                        + " $ (" + sysSettings.df.format(tr.getDate()) + ")",
                        Notification.Type.ERROR_MESSAGE);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(payTablelay).getComponent()
                && source.getCaption().equals(myUI.getMessage(SptMessages.Invoice))) {
            InvoiceInfoPdf iip = new InvoiceInfoPdf();
            iip.setLogin(loginTF.getValue().toString());
            iip.setClass_name(classCB.getContainerProperty(classCB.getValue(),
                    myUI.getMessage(SptMessages.Name)).getValue().toString());
            iip.setStud_fullname(nameTF.getValue().toString() + " " + surnameTF.getValue().toString()
                    + " " + middlenameTF.getValue().toString());
            iip.setWhopaid_fullname(((TextField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.WhoPaid)).getValue()).getValue().toString());
            iip.setPayment_date(((DateField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Date)).getValue()).getValue());
            iip.setAmount((Double) (((TextField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue()));
            iip.setKurs((Double) (((TextField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.Rate)).getValue()).getPropertyDataSource().getValue()));
            iip.setSchool_name(myUI.getUser().getSchool_name());
            iip.setPayment_type(((ComboBoxMax) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(SptMessages.PaymentType)).getValue())
                    .getContainerProperty(((ComboBoxMax) paymentsTable.getContainerProperty(source.getData(),
                            myUI.getMessage(SptMessages.PaymentType)).getValue()).getValue(),
                            myUI.getMessage(SptMessages.Name)).getValue().toString());
            try {
                DbStudPayment dbsp = new DbStudPayment();
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
                iip.setScl_logo(dbs.execGet_logo(loginTF.getValue().toString()));
                dbs.close();
            } catch (Exception e) {
            }
            if (iip.getScl_logo() != null) {
                new MakeInvoicePDF(myUI, iip);
            } else {
                Notification.show(myUI.getMessage(SptMessages.NoSchoolLogo),
                        Notification.Type.WARNING_MESSAGE);
            }
        }
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
                setContractCb(contr_id);
                initialPaymentTF.setData(null);
            }
        } else if (property == loginTF) {
            if (loginTF.getValue() != null && !loginTF.getValue().equals("")
                    && loginTF.isEnabled()) {
                photoUpl.setEnabled(true);
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
                    if (amount <= ((StudPayment) initialPaymentTF.getData()).getAmount()) {
                        DbAccTransactions dbTr = new DbAccTransactions();
                        dbTr.connect();
                        AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool_id(),
                                ((StudPayment) initialPaymentTF.getData()).getModification_date(),
                                ((StudPayment) initialPaymentTF.getData()).getAmount(), amount, 1);
                        if (tr != null) {
                            initialPaymentTF.removeAllValidators();
                            initialPaymentTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.LowBalance) + sysSettings.dFormat.format(tr.getOverlimit())
                                    + " $ (" + sysSettings.df.format(tr.getDate()) + ")", tr.getLimit(), null));
                            Notification.show(myUI.getMessage(SptMessages.LowBalance) + sysSettings.dFormat.format(tr.getOverlimit())
                                    + " $ (" + sysSettings.df.format(tr.getDate()) + ")",
                                    Notification.Type.ERROR_MESSAGE);
                            initialPaymentTF.setRequired(true);
                            initialPaymentTF.setRequiredError(myUI.getMessage(SptMessages.LowBalance) + sysSettings.dFormat.format(tr.getOverlimit())
                                    + " $ (" + sysSettings.df.format(tr.getDate()) + ")");
                        } else {
                            initialPaymentTF.setRequired(false);
                        }
                        dbTr.close();
                    } else {
                        initialPaymentTF.removeAllValidators();
                        initialPaymentTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
                    }
                } else {
                    initialPaymentTF.removeAllValidators();
                    initialPaymentTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
                }
            } catch (Exception e) {
                initialPaymentTF.removeAllValidators();
                initialPaymentTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
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
        } else if (((AbstractField) property).getId() != null
                && ((AbstractField) property).getId().equals(myUI.getMessage(SptMessages.Payments))) {
            Object itemId = ((AbstractField) property).getData();
            if (((ComboBoxMax) paymentsTable.getContainerProperty(itemId,
                    myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getValue() != null
                    && ((TextField) paymentsTable.getContainerProperty(itemId,
                            myUI.getMessage(SptMessages.Amount)).getValue()).getValue() != null
                    && ((DateField) paymentsTable.getContainerProperty(itemId,
                            myUI.getMessage(SptMessages.Date)).getValue()).getValue() != null) {
                AccTransaction tr = null;
                TextField tf = (TextField) paymentsTable.getContainerProperty(itemId,
                        myUI.getMessage(SptMessages.Amount)).getValue();
                if ((Integer) ((ComboBoxMax) paymentsTable.getContainerProperty(itemId,
                        myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getValue() != 3
                        && (paymentsTable.getContainerProperty(itemId,
                                sysSettings.crud_status).getValue().equals(myUI.getMessage(SptMessages.Insert))
                        || DateUtils.truncate(((DateField) paymentsTable.getContainerProperty(itemId,
                                myUI.getMessage(SptMessages.Date)).getValue()).getValue(), java.util.Calendar.DAY_OF_MONTH).compareTo(
                                (Date) paymentsTable.getContainerProperty(itemId, sysSettings.old_date).getValue()) == 0)
                        && (Double) ((TextField) paymentsTable.getContainerProperty(itemId,
                                myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue()
                        > (Double) paymentsTable.getContainerProperty(itemId, sysSettings.old_amount).getValue()) {
                    tf.removeAllValidators();
                    tf.addValidator(new DoubleRangeValidator(
                            myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
                } else {
                    if (paymentsTable.getContainerProperty(itemId, sysSettings.old_date).getValue() != null
                            && DateUtils.truncate(((DateField) paymentsTable.getContainerProperty(itemId,
                                    myUI.getMessage(SptMessages.Date)).getValue()).getValue(), java.util.Calendar.DAY_OF_MONTH).compareTo(
                                    (Date) paymentsTable.getContainerProperty(itemId, sysSettings.old_date).getValue()) != 0) {
                        tr = insertTestPayments((Date) paymentsTable.getContainerProperty(itemId,
                                sysSettings.old_date).getValue(), "");
                    }
                    if (tr == null) {
                        tr = insertTestPayments(((DateField) paymentsTable.getContainerProperty(itemId,
                                myUI.getMessage(SptMessages.Date)).getValue()).getValue(), "");
                    }
                    if (tr != null) {
                        tf.removeAllValidators();
                        if ((Integer) ((ComboBoxMax) paymentsTable.getContainerProperty(itemId,
                                myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getValue() == 3) {
                            tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.LowBalance)
                                    + sysSettings.dFormat.format(tr.getOverlimit())
                                    + " $ (" + sysSettings.df.format(tr.getDate()) + ")", 0.1,
                                    (Double) tf.getPropertyDataSource().getValue() - tr.getOverlimit()));
                        } else {
                            tf.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.LowBalance)
                                    + sysSettings.dFormat.format(tr.getOverlimit())
                                    + " $ (" + sysSettings.df.format(tr.getDate()) + ")",
                                    (Double) tf.getPropertyDataSource().getValue() + tr.getOverlimit(), null));
                        }
                        Notification.show(myUI.getMessage(SptMessages.LowBalance) + sysSettings.dFormat.format(tr.getOverlimit())
                                + " $ (" + sysSettings.df.format(tr.getDate()) + ")",
                                Notification.Type.ERROR_MESSAGE);
                    } else {
                        tf.removeAllValidators();
                        tf.addValidator(new DoubleRangeValidator(
                                myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
                    }
                }
            }
        } else if (property == contLangOG && contLangOG.getValue() != null) {
            if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                    && studDataTable.getValue() != null) {
                if (contractCB.getValue() != null) {
                    StudInfoPdf studInfo = new StudInfoPdf();
                    try {
                        DbStudInfoPdf dbs = new DbStudInfoPdf();
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
                        Iterator iter = discountsTable.getItemIds().iterator();
                        String allDists = "";
                        String dis = "";
                        double count_amount = (Double) (contractCB.getContainerProperty(contractCB.getValue(),
                                myUI.getMessage(SptMessages.Amount)).getValue());
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            dis = ((((ComboBoxMax) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue())
                                    .getContainerProperty(((ComboBoxMax) discountsTable
                                            .getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                            myUI.getMessage(SptMessages.Name)).getValue().toString()));
                            dis = dis.substring(0, dis.indexOf(" - "));
                            allDists += dis;

                            if (((Integer) ((ComboBoxMax) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue())
                                    .getContainerProperty(((ComboBoxMax) discountsTable
                                            .getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 1)
                                    || ((Integer) ((ComboBoxMax) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue())
                                            .getContainerProperty(((ComboBoxMax) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                                    myUI.getMessage(SptMessages.DiscountType)).getValue() == 3)) {
                                allDists += " - " + ((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue())
                                        .getPropertyDataSource().getValue().toString() + "%(" + sysSettings.dFormat.format(count_amount
                                                * ((Double) ((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue())
                                                        .getPropertyDataSource().getValue()) / 100) + "$)";
                                count_amount -= count_amount
                                        * ((Double) ((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue())
                                                .getPropertyDataSource().getValue()) / 100;
                            } else if (((Integer) ((ComboBoxMax) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue())
                                    .getContainerProperty(((ComboBoxMax) discountsTable
                                            .getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 2)
                                    || ((Integer) ((ComboBoxMax) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue())
                                            .getContainerProperty(((ComboBoxMax) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                                    myUI.getMessage(SptMessages.DiscountType)).getValue() == 4)) {
                                allDists += "(" + sysSettings.dFormat.format((Double) ((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue())
                                        .getPropertyDataSource().getValue()).toString() + "$)";
                                count_amount -= (Double) ((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue())
                                        .getPropertyDataSource().getValue();
                            }

                            if (iter.hasNext()) {
                                allDists += ", ";
                            }
                        }
                        studInfo.setCtr_discountStr(allDists);
                        studInfo.setCtr_discountPerc(discounts);
                    }
                    studInfo.setCtr_init_payment(instFirstPay);
                    studInfo.setCtr_k_oplate(kOplate);
                    studInfo.setCtr_ttl_left_sum(ttl_left);
                    if (studInfo.getRel_fullname() != null && studInfo.getRel_name() != null
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
                            } else if (contLangOG.getValue().toString().equals(myUI.getMessage(SptMessages.SilkRoadRu))) {
                                new ContractSilkPdfRu(myUI, studInfo, instPlanCont);
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
                && property != surnameTF && property != middlenameTF && property != divideTF
                && property != initialPaymentTF && ((TextField) property).getDescription()
                        .equals(myUI.getMessage(SptMessages.Amount))) {
            if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
                if (initialPaymentTF.isValid()) {
                    recountInstPlanLabel();
                }
            }
        } else if (property instanceof TextField && property != nameTF && property != loginTF
                && property != surnameTF && property != middlenameTF && property != divideTF
                && property != initialPaymentTF && (((TextField) property).getDescription()
                        .equals(myUI.getMessage(SptMessages.DiscountAmount))
                || ((TextField) property).getDescription().equals(myUI.getMessage(SptMessages.Note)))) {
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
                familyTableCheck((Boolean) property.getValue() == true, property);

            }
        } else if (property instanceof ComboBoxMax && property != statusCB && property != contractCB
                && property != genderCB && property != classCB && property != contLangOG) {
            if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
                checkDiscountsTable(property);
            }
        }
    }

    private void prepareModificationMode() {
        if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()
                || tabs.getSelectedTab() == tabs.getTab(studSearchLay).getComponent()) {
            loginTF.setEnabled(true);
            nameTF.setEnabled(true);
            surnameTF.setEnabled(true);
            middlenameTF.setEnabled(true);
            genderCB.setEnabled(true);
            birthDate.setEnabled(true);
            classCB.setEnabled(true);
        }
        contractCB.setEnabled(true);
        if (currentUser.isPermitted(sysSettings.cnTransactionsView + ":" + sysSettings.prmChangeOlder5Days)
                || initialPaymentTF.getData() == null
                || DateUtils.truncate(((StudPayment) initialPaymentTF.getData()).getModification_date(),
                        java.util.Calendar.DAY_OF_MONTH).compareTo(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH)) == 0) {
            initialPaymentTF.setEnabled(true);
        }
        discountsTable.setEnabled(true);
        plusDiscButton.setEnabled(true);
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
        plusPayButton.setEnabled(true);
        plusCallButton.setEnabled(true);
        relativesTable.setEnabled(true);
        paymentsTable.setEnabled(true);
        callsTable.setEnabled(true);
        acsGiveTable.setEnabled(true);
        acsReceiveTable.setEnabled(true);

        Iterator iter = ((IndexedContainer) installmentTable
                .getContainerDataSource()).getItemIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            if ((Integer) installmentTable.getContainerProperty(next, sysSettings.status_id).getValue() == 0) {
                ((Button) installmentTable.getContainerProperty(next, sysSettings.button).getValue()).setEnabled(false);
                ((DateField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Date)).getValue()).setEnabled(false);
                ((TextField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue()).setEnabled(false);
            } else {
                ((Button) installmentTable.getContainerProperty(next, sysSettings.button)
                        .getValue()).setEnabled(true);
                ((DateField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Date)).getValue()).setEnabled(true);
                ((TextField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue()).setEnabled(true);
            }

        }
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
        if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (tabs.getSelectedTab() == tabs.getTab(studSearchLay).getComponent()) {
            if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.actDelete)) {
                deleteBtn.setEnabled(true);
            }
        } else {
            deleteBtn.setEnabled(false);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        studDataTable.setEnabled(true);
        loginTF.setEnabled(false);
        nameTF.setEnabled(false);
        surnameTF.setEnabled(false);
        middlenameTF.setEnabled(false);
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
        Iterator iter = ((IndexedContainer) paymentsTable
                .getContainerDataSource()).getItemIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            ((Button) paymentsTable.getContainerProperty(next, sysSettings.button).getValue()).setEnabled(false);
            ((ComboBoxMax) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).setEnabled(false);
            ((ComboBoxMax) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.PaymentType)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Amount)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Rate)).getValue()).setEnabled(false);
            ((DateField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Date)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.WhoPaid)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(SptMessages.Note)).getValue()).setEnabled(false);

        }
        contractCB.setEnabled(false);
        discountsTable.setEnabled(false);
        currDate.setEnabled(false);
        divideTF.setEnabled(false);
        plusInstButton.setEnabled(false);
        plusDiscButton.setEnabled(false);
        initialPaymentTF.setEnabled(false);
        installmentTable.setEnabled(false);
        divideBtn.setEnabled(false);
        Iterator<Component> iterator = tabs.iterator();
        while (iterator.hasNext()) {
            Component component = iterator.next();
            TabSheet.Tab tab = tabs.getTab(component);
            if (tab.getComponent() == contractTabLay) {
                if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.prmTabContract)) {
                    tab.setEnabled(true);
                } else {
                    tab.setEnabled(false);
                }
            } else if (tab.getComponent() == payTablelay) {
                if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.prmTabPayments)) {
                    tab.setEnabled(true);
                } else {
                    tab.setEnabled(false);
                }
            } else if (tab.getComponent() == callsTableLay) {
                if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.prmTabCalls)) {
                    tab.setEnabled(true);
                } else {
                    tab.setEnabled(false);
                }
            } else if (tab.getComponent() == famTableLay) {
                if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.prmTabFamilyTab)) {
                    tab.setEnabled(true);
                } else {
                    tab.setEnabled(false);
                }
            } else if (tab.getComponent() == acsGiveTableLay) {
                if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.prmTabAccessoriesGive)) {
                    tab.setEnabled(true);
                } else {
                    tab.setEnabled(false);
                }
            } else if (tab.getComponent() == acsReceiveTableLay) {
                if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.prmTabAccessoriesReceive)) {
                    tab.setEnabled(true);
                } else {
                    tab.setEnabled(false);
                }
            } else if (tab.getComponent() == studSearchLay) {
                tab.setEnabled(true);
            }
        }

    }

    private void fillFields() {
        loginTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(SptMessages.Id))
                .getValue().toString());
        nameTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(SptMessages.Firstname))
                .getValue().toString());
        surnameTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(SptMessages.Surname))
                .getValue().toString());
        if (studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(SptMessages.Middlename))
                .getValue() != null) {
            middlenameTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                    studDataTable.getValue(), myUI.getMessage(SptMessages.Middlename))
                    .getValue().toString());
        }
        genderCB.setValue((Integer) studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), sysSettings.gender_id)
                .getValue());
        birthDate.setValue((Date) studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(SptMessages.DateOfBirth))
                .getValue());
        classCB.setValue((Integer) studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), sysSettings.class_name_id)
                .getValue());
        statusCB.setValue((Integer) studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), sysSettings.education_status_id)
                .getValue());
        if (studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.Photo)).getValue() != null) {
            photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS
                    + studDataTable.getContainerProperty(studDataTable.getValue(),
                            myUI.getMessage(SptMessages.Photo)).getValue().toString())));
            photoName = studDataTable.getContainerProperty(studDataTable.getValue(),
                    myUI.getMessage(SptMessages.Photo)).getValue().toString();
        } else {
            photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS + "no_photo.jpg")));
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
            setContractTab((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
        } else if (tabs.getSelectedTab() == tabs.getTab(payTablelay).getComponent()) {
            setPaymentsTable();
        } else if (tabs.getSelectedTab() == tabs.getTab(callsTableLay).getComponent()) {
            setCallsTable();
        }

    }

    private void clearFields() {
        loginTF.setValue("");
        nameTF.setValue("");
        surnameTF.setValue("");
        middlenameTF.setValue("");
        genderCB.setValue(null);
        birthDate.setValue(null);
        classCB.setValue(null);
        statusCB.setValue(null);
        contractCB.setValue(null);
        divideTF.setValue(null);
        currDate.setValue(null);
        photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoName = null;
        relativesTable.removeAllItems();
        acsGiveTable.removeAllItems();
        acsReceiveTable.removeAllItems();
        installmentTable.removeAllItems();
        paymentsTable.removeAllItems();
        callsTable.removeAllItems();
        discountsTable.removeAllItems();
    }

    private void updateDatacontainer() {
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.Id)).setValue(loginTF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.Firstname)).setValue(nameTF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.Surname)).setValue(surnameTF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.Middlename)).setValue(middlenameTF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.Photo)).setValue(photoName);
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.DateOfBirth)).setValue(birthDate.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                sysSettings.class_name_id).setValue(classCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.ClassName)).setValue(classCB
                .getContainerDataSource().getContainerProperty(classCB.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                sysSettings.education_status_id).setValue(statusCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                sysSettings.gender_id).setValue(genderCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.EducationStatus)).setValue(statusCB
                .getContainerDataSource().getContainerProperty(statusCB.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());

    }

    private void addDatacontainerItem(int id) {
        Item item = ((IndexedContainer) studDataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Id)).setValue(
                loginTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Firstname)).setValue(
                nameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Surname)).setValue(
                surnameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Middlename)).setValue(
                middlenameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.DateOfBirth)).setValue(
                birthDate.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Photo)).setValue(
                photoName);
        item.getItemProperty(sysSettings.gender_id).setValue(
                genderCB.getValue());
        item.getItemProperty(sysSettings.class_name_id).setValue(
                classCB.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                classCB.getContainerDataSource().getContainerProperty(classCB.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());
        item.getItemProperty(sysSettings.education_status_id).setValue(
                statusCB.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.EducationStatus)).setValue(
                statusCB.getContainerDataSource().getContainerProperty(statusCB.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());
        studDataTable.clearFilters();
        studDataTable.setValue(id);
    }

    private Student getStudent(int id) {
        Student s = new Student();
        s.setPhoto(photoName);
        s.setLogin(loginTF.getValue().toString());
        s.setPassword(new Sha256Hash(loginTF.getValue()).toString());
        s.setName(nameTF.getValue().toString());
        s.setSur_name(surnameTF.getValue().toString());
        s.setMiddle_name(middlenameTF.getValue().toString());
        s.setGender_id((Integer) genderCB.getValue());
        s.setBirth_date((Date) birthDate.getValue());
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
                Notification.show(myUI.getMessage(SptMessages.LowBalance) + sysSettings.dFormat.format(acTr.getOverlimit())
                        + " $ (" + sysSettings.df.format(acTr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
            } else {
                DbStudent dbst = new DbStudent();
                DbDefinition dbdef = new DbDefinition();
                dbst.connect();
                dbdef.connect();
                dbt.exec_delete_by_st_id((Integer) studDataTable.getValue());
                dbst.exec_delete((Integer) studDataTable.getValue());
                int st = dbdef.exec_delete((Integer) studDataTable.getValue(), sysSettings.dbStudent);
                if (st != 0) {
                    clearFields();
                    eduStatCont.getContainerProperty((Integer) studDataTable
                            .getContainerProperty(studDataTable.getValue(),
                                    sysSettings.education_status_id).getValue(), sysSettings.count)
                            .setValue(((Integer) eduStatCont.getContainerProperty((Integer) studDataTable
                                    .getContainerProperty(studDataTable.getValue(),
                                            sysSettings.education_status_id).getValue(),
                                    sysSettings.count).getValue()) - 1);
                    eduStatCont.getContainerProperty(6, sysSettings.count)
                            .setValue(((Integer) eduStatCont.getContainerProperty(6, sysSettings.count)
                                    .getValue()) - 1);
                    repaint();
                    studDataTable.removeItem((Integer) studDataTable.getValue());
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
            DbStudAccessories dbsa = new DbStudAccessories();
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
            DbStudInstallmentPlan dbip = new DbStudInstallmentPlan();
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
        Iterator<Component> i = layout.iterator();
        while (i.hasNext()) {
            Component c = i.next();
            if (c instanceof AbstractField) {
                try {
                    ((AbstractField) c).validate();
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

        @Override
        public OutputStream receiveUpload(String filename, String mimetype) {
            mimeType = mimetype;
            FileOutputStream fos; // Output stream to write to
            photoName = loginTF.getValue() + ".jpg";
            try {
                myFile = new File(SystemSettings.PATH_TO_UPLOADS + photoName);

                // Open the file for writing.
                fos = new FileOutputStream(myFile);
            } catch (Exception e) {
                // Error while opening the file. Not reported here.
                logger.error(e);
                logger.catching(e);
                return null;
            }
            return fos; // Return the output stream to write tou
        }
    }

    private void buildUploadWindow() {
        state = new Label();
        textualProgress = new Label();
        fName = new Label();
        pi = new ProgressIndicator();

        statusWindow = new Window(myUI.getMessage(SptMessages.UploadStatus));
        statusWindow.setResizable(false);
        statusWindow.setDraggable(false);
        statusWindow.addStyleName("upload-info");

        final FormLayout l = new FormLayout();
        statusWindow.setContent(l);

        l.setMargin(true);

        final HorizontalLayout stateLayout = new HorizontalLayout();
        stateLayout.setSpacing(true);
        stateLayout.addComponent(state);

        cancelButton = new Button(myUI.getMessage(SptMessages.CancelKey));
        cancelButton.addClickListener(this);
        cancelButton.setVisible(false);
        cancelButton.setStyleName("small");
        stateLayout.addComponent(cancelButton);

        stateLayout.setCaption(myUI.getMessage(SptMessages.CurrentState));
        state.setValue(myUI.getMessage(SptMessages.Idle));
        l.addComponent(stateLayout);

        fName.setCaption(myUI.getMessage(SptMessages.FileName));
        l.addComponent(fName);

        pi.setCaption(myUI.getMessage(SptMessages.Progress));
        pi.setVisible(false);
        l.addComponent(pi);

        textualProgress.setVisible(false);
        l.addComponent(textualProgress);

    }

    private void buildUpload() {
        receiver = new MyReceiver();
        photoUpl = new Upload(null, receiver);
        photoUpl.setImmediate(true);
        photoUpl.setStyleName(ValoTheme.BUTTON_TINY);
        photoUpl.setButtonCaption(myUI.getMessage(SptMessages.Upload));
        photoUpl.addStartedListener(new Upload.StartedListener() {
            @Override
            public void uploadStarted(Upload.StartedEvent event) {
                // This method gets called immediatedly after upload is started

                buildUploadWindow();
                myUI.addWindow(statusWindow);
                statusWindow.setClosable(false);

                pi.setValue(0f);
                pi.setVisible(true);
                pi.setPollingInterval(500); // hit server frequantly to get
                textualProgress.setVisible(true);
                // updates to client
                state.setValue(myUI.getMessage(SptMessages.Uploading));
                fName.setValue(event.getFilename());

                cancelButton.setVisible(true);

            }
        });

        photoUpl.addProgressListener(new Upload.ProgressListener() {
            @Override
            public void updateProgress(long readBytes, long contentLength) {
                // This method gets called several times during the update
                if (!mimeType.equals("image/jpeg")) {
                    photoUpl.interruptUpload();
                    myFile.delete();
                    photoName = null;
                    Notification.show(myUI.getMessage(SptMessages.Jpeg),
                            Notification.Type.WARNING_MESSAGE);
                } else if (contentLength >= 5000000) {
                    photoUpl.interruptUpload();
                    myFile.delete();
                    photoName = null;
                    Notification.show(myUI.getMessage(SptMessages.Maxsize),
                            Notification.Type.WARNING_MESSAGE);
                } else {

                    pi.setValue(new Float(readBytes / (float) contentLength));
                    textualProgress.setValue(myUI.getMessage(SptMessages.Processed) + " "
                            + readBytes + " " + myUI.getMessage(SptMessages.BytesOf) + " "
                            + contentLength);
                }
            }
        });

        photoUpl.addSucceededListener(new Upload.SucceededListener() {
            @Override
            public void uploadSucceeded(Upload.SucceededEvent event) {
                // This method gets called when the upload finished successfully
                try {
                    Thumbnails.of(myFile).size(200, 200).toFile(myFile);
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                photoEmb.setSource(new FileResource(myFile));
            }
        });

        photoUpl.addFailedListener(new Upload.FailedListener() {
            @Override
            public void uploadFailed(Upload.FailedEvent event) {
            }
        });

        photoUpl.addFinishedListener(new Upload.FinishedListener() {
            @Override
            public void uploadFinished(Upload.FinishedEvent event) {
                if (statusWindow != null) {
                    statusWindow.close();
                }
            }
        });
    }

    private void setRelativesTable() {
        NATURAL_COL_ORDER_RElATIVES = new String[]{sysSettings.button,
            myUI.getMessage(SptMessages.RelativeType),
            myUI.getMessage(SptMessages.FullName), myUI.getMessage(SptMessages.Address),
            myUI.getMessage(SptMessages.Phone), myUI.getMessage(SptMessages.WorkPlace),
            myUI.getMessage(SptMessages.Passport), myUI.getMessage(SptMessages.Responsible)};
        try {
            DbStudRelative dbr = new DbStudRelative();
            dbr.connect();
            relativesTable.setContainerDataSource(
                    dbr.execSQL_St_Rel(myUI, (Integer) studDataTable.getValue(), this));
            dbr.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        relativesTable.setVisibleColumns(NATURAL_COL_ORDER_RElATIVES);
    }

    public IndexedContainer prepareRelativesContainer() {
        if (productsContainer == null) {
            productsContainer = new IndexedContainer();
            productsContainer.addContainerProperty(sysSettings.button, Button.class, null);
            productsContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.RelativeType), ComboBoxMax.class, null);
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
                    sysSettings.crud_status, String.class, null);
        } else {
            productsContainer.removeAllItems();
        }
        return productsContainer;
    }

    public Button createButton(String description, String itemId, boolean print,
            boolean discount) {
        Button btn1 = new Button();
        if (print && !discount) {
            Button btn = new Button(myUI.getMessage(SptMessages.Invoice));
            btn.setDescription(description);
            btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btn.addStyleName(ValoTheme.BUTTON_TINY);
            btn.setIcon(FontAwesome.PRINT);
            btn.setData(itemId);
            btn.addClickListener(this);
            btn1 = btn;
        } else if (!print && discount) {
            Button btn = new Button(myUI.getMessage(SptMessages.Discount));
            btn.setDescription(description);
            btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btn.addStyleName(ValoTheme.BUTTON_TINY);
            btn.setIcon(FontAwesome.MINUS_SQUARE);
            btn.setData(itemId);
            btn.addClickListener(this);
            btn1 = btn;
        } else if (!print && !discount) {
            Button btn = new Button();
            btn.setDescription(description);
            btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btn.addStyleName(ValoTheme.BUTTON_TINY);
            btn.setIcon(FontAwesome.MINUS_SQUARE);
            btn.setData(itemId);
            btn.addClickListener(this);
            btn1 = btn;
        }
        return btn1;
    }

    public TextField createTextfield(String value, String description, String itemId,
            boolean isFamTab, boolean isMain) {
        TextField tf = new TextField();
        tf.setDescription(description);
        if (!isFamTab) {
            tf.setRequired(true);
            tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
            tf.addValidator(new StringLengthValidator(
                    myUI.getMessage(SptMessages.NotifWrongValue), null, 100, false));
        }
        if (isMain) {
            tf.setRequired(true);
            tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));

        }
        tf.addValidator(new StringLengthValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), null, 100, false));
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth("100%");
        if (value != null) {
            tf.setValue(value);
        }
        tf.setData(itemId);
        tf.addValueChangeListener(this);
        tf.setImmediate(true);
        return tf;
    }

    public TextField createTextfieldDouble(Double value, String description, String itemId) {
        ObjectProperty<Double> property = new ObjectProperty<Double>(0.0);
        TextField tf = new TextField(property);
        tf.setDescription(description);
        tf.setRequired(true);
        tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth("100%");
        tf.addValidator(new DoubleRangeValidator(
                myUI.getMessage(SptMessages.NotifWrongValue), 0.1, null));
        tf.setConverter(sysSettings.getStringToDoubleConverter());
        tf.setNullRepresentation("0.0");
        tf.setNullSettingAllowed(false);
        tf.setData(itemId);
        tf.getPropertyDataSource().setValue(value);
        tf.addValueChangeListener(this);
        return tf;
    }

    public TextField createTextfieldDisc(Double value, Double maxValue, String description, String itemId, boolean isDisabled) {
        ObjectProperty<Double> property = new ObjectProperty<Double>(0.0);
        TextField tf = new TextField(property);
        tf.setDescription(description);
        tf.setRequired(true);
        if (isDisabled) {
            tf.setEnabled(false);
        } else {
            tf.addValidator(new DoubleRangeValidator(
                    myUI.getMessage(SptMessages.NotifWrongValue), 0.1, maxValue));
        }
        tf.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth("100%");
        tf.getPropertyDataSource().setValue(value);
        tf.setNullRepresentation("0.0");
        tf.setNullSettingAllowed(false);
        tf.setConverter(sysSettings.getStringToDoubleConverter());
        tf.setData(itemId);
        tf.addValueChangeListener(this);
        tf.setImmediate(true);
        return tf;
    }

    public TextField createTextfieldNote(String value, String description, String itemId) {
        TextField tf = new TextField();
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth("100%");
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
        df.setWidth("100%");
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
            df.setDateFormat(sysSettings.dateTimeMinPattern);
            df.setResolution(Resolution.MINUTE);
        } else {
            df.setDateFormat(sysSettings.datePattern);
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

    public ComboBoxMax createCombobox(int value, String description, String itemId,
            String dbtable, boolean setDefYear, boolean setDefPayType,
            boolean setDefPayCatType, boolean isdisabled) {
        ComboBoxMax cb = new ComboBoxMax();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth("100%");
        cb.setRequired(true);
        cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbDefinition dbp = new DbDefinition();
            dbp.connect();
            cb.setContainerDataSource(dbp.exec_for_select(myUI, dbtable));
            dbp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (isdisabled) {
            cb.setEnabled(false);
        }
        cb.setNullSelectionAllowed(false);
        cb.setData(itemId);
        if (setDefYear == true) {
            cb.setValue(myUI.getUser().getCurrent_year().getId());
        } else if (setDefPayCatType == true) {
            cb.setValue(2);
        } else if (setDefPayType == true) {
            cb.setValue(1);
        } else {
            cb.setValue(value);
        }
        cb.addValueChangeListener(this);
        return cb;
    }

    public ComboBoxMax createComboboxPayment(int value, String description, String itemId,
            boolean isFromDb, boolean defType) {
        ComboBoxMax cb = new ComboBoxMax();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth("100%");
        cb.setRequired(true);
        cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
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

    public ComboBoxMax createComboboxDisc(int value, String description, String itemId) {
        ComboBoxMax cb = new ComboBoxMax();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth("100%");
        cb.setRequired(true);
        cb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        cb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
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
        cb.setValue(value);
        cb.addValueChangeListener(this);
        return cb;
    }

    public ComboBoxMultiselectMax createComboboxMultiAcs(String value, String description,
            String itemId, int cat_id) {
        ComboBoxMultiselectMax comboMCB = new ComboBoxMultiselectMax(myUI.getMessage(SptMessages.Materials));
        comboMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        comboMCB.setWidth("100%");
        comboMCB.setRequired(true);
        comboMCB.setImmediate(true);
        comboMCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        comboMCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        comboMCB.setShowSelectAllButton(new ComboBoxMultiselect.ShowButton() {
            @Override
            public boolean isShow(String filter, int page) {
                return true;
            }
        });
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
                Notification.show(myUI.getMessage(SptMessages.NotifEmptyTable),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            } else {
                Iterator iter = ((IndexedContainer) relativesTable
                        .getContainerDataSource()).getItemIds().iterator();
                int counter = 0;
                while (iter.hasNext()) {
                    Object next = iter.next();
                    if (((CheckBox) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Responsible)).getValue()).getValue() == true) {
                        counter++;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.FullName)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.WorkPlace)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Passport)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Phone)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Address)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((ComboBoxMax) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.RelativeType)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                }
                if (counter != 1) {
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValueCounter),
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
                Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            } else {
                Iterator iter = ((IndexedContainer) t
                        .getContainerDataSource()).getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();

                    if (!((ComboBoxMax) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Year)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((ComboBoxMultiselectMax) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Materials)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
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
                Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            } else {
                Iterator iter = ((IndexedContainer) t
                        .getContainerDataSource()).getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();

                    if (!((ComboBoxMax) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Year)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((ComboBoxMultiselectMax) t.getItem(next).getItemProperty(
                            myUI.getMessage(SptMessages.Materials)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
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
        if (tabs.getSelectedTab() == tabs.getTab(payTablelay).getComponent()) {
            Iterator iter = ((IndexedContainer) t.getContainerDataSource()).getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (!((ComboBoxMax) t.getItem(next).getItemProperty(
                        myUI.getMessage(SptMessages.PaymentType)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((TextField) t.getItem(next).getItemProperty(
                        myUI.getMessage(SptMessages.Amount)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((TextField) t.getItem(next).getItemProperty(
                        myUI.getMessage(SptMessages.WhoPaid)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((DateField) t.getItem(next).getItemProperty(
                        myUI.getMessage(SptMessages.Date)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
            }

        }
        return true;
    }

    private void addRelativeItem() {
        NATURAL_COL_ORDER_RElATIVES = new String[]{sysSettings.button,
            myUI.getMessage(SptMessages.RelativeType),
            myUI.getMessage(SptMessages.FullName), myUI.getMessage(SptMessages.Address),
            myUI.getMessage(SptMessages.Phone), myUI.getMessage(SptMessages.WorkPlace),
            myUI.getMessage(SptMessages.Passport), myUI.getMessage(SptMessages.Responsible)};
        String id = sysSettings.FreshItem + (--r_table_counter);
        if (relativesTable.getContainerDataSource().size() == 0) {
            relativesTable.setContainerDataSource(prepareRelativesContainer());
        }
        Item item;
        item = ((IndexedContainer) relativesTable.getContainerDataSource()).addItemAt(
                relativesTable.getContainerDataSource().size(), id);
        item.getItemProperty(sysSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, false, false));
        item.getItemProperty(myUI.getMessage(SptMessages.Responsible)).setValue(
                createCheckBox(false, myUI.getMessage(SptMessages.Responsible), id));
        item.getItemProperty(myUI.getMessage(SptMessages.FullName)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.FullName), id, false, false));
        item.getItemProperty(myUI.getMessage(SptMessages.Passport)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Passport), id, true, false));
        item.getItemProperty(myUI.getMessage(SptMessages.Phone)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Phone), id, true, false));
        item.getItemProperty(myUI.getMessage(SptMessages.WorkPlace)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.WorkPlace), id, true, false));
        item.getItemProperty(myUI.getMessage(SptMessages.Address)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Address), id, true, false));
        item.getItemProperty(myUI.getMessage(SptMessages.RelativeType)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.RelativeType), id,
                        sysSettings.dbRelatives, false, false, false, false));
        item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));

        relativesTable.setVisibleColumns(NATURAL_COL_ORDER_RElATIVES);
    }

    private void insertInitialPayment(int student_id) {
        try {
            if (initialPaymentTF.isValid()) {
                DbStudPayment dbsp = new DbStudPayment();
                DbAccTransactions dbat = new DbAccTransactions();
                dbsp.connect();
                DbPaymentCategory dbpc = new DbPaymentCategory();
                dbpc.connect();
                DbStudRelative dbsr = new DbStudRelative();
                dbsr.connect();
                int status = 0;
                AccTransaction tr = new AccTransaction();
                StudPayment sp = new StudPayment();
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
                sp.setNoteForKassa(studDataTable.getContainerProperty(studDataTable.getValue(),
                        myUI.getMessage(SptMessages.ClassName)).getValue().toString() + " "
                        + studDataTable.getContainerProperty(studDataTable.getValue(),
                                myUI.getMessage(SptMessages.Id)).getValue().toString() + " "
                        + studDataTable.getContainerProperty(studDataTable.getValue(),
                                myUI.getMessage(SptMessages.Firstname)).getValue().toString() + " "
                        + studDataTable.getContainerProperty(studDataTable.getValue(),
                                myUI.getMessage(SptMessages.Surname)).getValue().toString());
                sp.setEmplooyee_id(myUI.getUser().getId());
                sp.setSchool_id(myUI.getUser().getSchool_id());
                tr.setAmount(sp.getAmount());
                tr.setDate(sp.getModification_date());
                tr.setCategory_id(dbpc.get_initial_payment_category_id());
                tr.setCurrency_id(2);
                tr.setCurrency_rate(sp.getRate());
                tr.setNote(sp.getNoteForKassa());
                tr.setEmployee_id(sp.getEmplooyee_id());
                tr.setSchool_id(sp.getSchool_id());
                if (initialPaymentTF.getPropertyDataSource().getValue() == null && initialPaymentTF.getData() != null) {
                    sp.setId(((StudPayment) initialPaymentTF.getData()).getId());
                    String payment_id = Integer.toString(((StudPayment) initialPaymentTF.getData()).getId());
                    dbat.exec_delete(sysSettings.dbColumnStudent_payments_id, payment_id, dbsp.getConnection());//delete transaction
                    dbsp.exec_update_emp_id(myUI.getUser().getId(), payment_id);
                    dbsp.exec_delete(payment_id);
                    initialPaymentTF.setData(null);
                } else if (initialPaymentTF.getPropertyDataSource().getValue() != null
                        && (Double) initialPaymentTF.getPropertyDataSource().getValue() != 0.0 && initialPaymentTF.getData() != null) {
                    sp.setId(((StudPayment) initialPaymentTF.getData()).getId());
                    sp.setModification_date(((StudPayment) initialPaymentTF.getData()).getModification_date());
                    tr.setStudent_payments_id(sp.getId());
                    tr.setDate(sp.getModification_date());
                    status = dbsp.exec_update(sp);
                    if (status != 0) {
                        if (sp.getModification_date().after(myUI.getUser().getTransactions_start_date())
                                || sp.getModification_date().equals(myUI.getUser().getTransactions_start_date())) {
                            int update_status = dbat.exec_update(tr, sysSettings.dbColumnStudent_payments_id,
                                    tr.getStudent_payments_id(), dbsp.getConnection());//update transaction normal
                            if (update_status == 0) {
                                dbat.exec_insert(tr, dbsp.getConnection());//insert transaction if it doesn't exist, on payment update if payment date is after transactions start date
                            }
                        } else if (sp.getModification_date().before(myUI.getUser().getTransactions_start_date())) {
                            dbat.exec_delete(sysSettings.dbColumnStudent_payments_id, Integer.toString(sp.getId()), dbsp.getConnection()); //delete transaction on payment update if payment date is before transactions start date
                        }
                    }

                } else if (initialPaymentTF.getPropertyDataSource().getValue() != null && status == 0) {
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
            DbStudInstallmentPlan dbip = new DbStudInstallmentPlan();
            DbStudPayment dbsp = new DbStudPayment();
            dbip.connect();
            dbsp.connect();
            Iterator iter = ((IndexedContainer) installmentTable
                    .getContainerDataSource()).getItemIds().iterator();
            int z;
            while (iter.hasNext()) {

                Object next = iter.next();
                StudInstallmentPlan ip = getInstPlan(0, student_id, installmentTable.getItem(next));
                if ((Integer) installmentTable.getContainerProperty(next,
                        sysSettings.status_id).getValue() != 0) {
                    z = dbip.exec_insert(ip);
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
                DbStudAccessories dbsa = new DbStudAccessories();
                dbsa.connect();
                Iterator iter = ((IndexedContainer) acsGiveTable
                        .getContainerDataSource()).getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    String[] acs_ids = sysSettings.convertCollectionToStr((Set<?>) ((ComboBoxMultiselectMax) (acsGiveTable
                            .getContainerDataSource().getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Materials))
                            .getValue())).getValue()).split(",");
                    for (String s : acs_ids) {
                        int acs_id = Integer.parseInt(s);
                        StudAccessories acs = getAccessories(0, student_id, acs_id, acsGiveTable.getItem(next));
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
                DbStudAccessories dbsa = new DbStudAccessories();
                dbsa.connect();
                Iterator iter = ((IndexedContainer) acsReceiveTable
                        .getContainerDataSource()).getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    String[] acs_ids = sysSettings.convertCollectionToStr((Set<?>) ((ComboBoxMultiselectMax) (acsReceiveTable
                            .getContainerDataSource().getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Materials))
                            .getValue())).getValue()).split(",");
                    for (String s : acs_ids) {
                        int acs_id = Integer.parseInt(s);
                        StudAccessories acs = getAccessories(0, student_id, acs_id, acsReceiveTable.getItem(next));
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

    private StudRelative getRelative(int id, int student_id, Item item) {
        StudRelative rel = new StudRelative();
        rel.setStudent_id(student_id);
        rel.setFullname(((TextField) item.getItemProperty(
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
                myUI.getMessage(SptMessages.Responsible)).getValue()).getValue() == true) {
            rel.setIs_main(1);
        } else {
            rel.setIs_main(0);
        }
        rel.setRelatives_id((Integer) ((ComboBoxMax) item.getItemProperty(
                myUI.getMessage(SptMessages.RelativeType)).getValue()).getValue());

        rel.setId(id);
        return rel;
    }

    private StudPayment getPayment(int id, int student_id, Item item) {
        StudPayment sp = new StudPayment();
        sp.setStudent_id(student_id);
        sp.setYear_id(myUI.getUser().getCurrent_year().getId());
        sp.setPayment_cat_type_id((Integer) ((ComboBoxMax) item.getItemProperty(
                myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getValue());
        sp.setPayment_type_id((Integer) ((ComboBoxMax) item.getItemProperty(
                myUI.getMessage(SptMessages.PaymentType)).getValue()).getValue());
        sp.setAmount((Double) ((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue());
        sp.setRate((Double) ((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.Rate)).getValue()).getPropertyDataSource().getValue());
        sp.setWho_paid(((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.WhoPaid)).getValue()).getValue());
        sp.setNote(((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.Note)).getValue()).getValue());
        sp.setNoteForKassa(studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.ClassName)).getValue().toString() + " "
                + studDataTable.getContainerProperty(studDataTable.getValue(),
                        myUI.getMessage(SptMessages.Id)).getValue().toString() + " "
                + studDataTable.getContainerProperty(studDataTable.getValue(),
                        myUI.getMessage(SptMessages.Firstname)).getValue().toString() + " "
                + studDataTable.getContainerProperty(studDataTable.getValue(),
                        myUI.getMessage(SptMessages.Surname)).getValue().toString());
        sp.setEmplooyee_id(myUI.getUser().getId());
        sp.setSchool_id(myUI.getUser().getSchool_id());
        sp.setModification_date(((DateField) item.getItemProperty(
                myUI.getMessage(SptMessages.Date)).getValue()).getValue());
        sp.setId(id);
        return sp;
    }

    private StudInstallmentPlan getInstPlan(int id, int student_id, Item item) {
        StudInstallmentPlan ip = new StudInstallmentPlan();
        ip.setStudent_id(student_id);
        ip.setYear_id(myUI.getUser().getCurrent_year().getId());
        ip.setDate_of_payment(((DateField) item.getItemProperty(
                myUI.getMessage(SptMessages.Date)).getValue()).getValue());
        ip.setAmount((Double) (((TextField) item.getItemProperty(
                myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue()));
        ip.setId(id);
        return ip;
    }

    private StudAccessories getAccessories(int id, int student_id, int acs_id, Item item) {
        StudAccessories acc = new StudAccessories();
        acc.setStudent_id(student_id);
        acc.setYear_id((Integer) ((ComboBoxMax) item.getItemProperty(
                myUI.getMessage(SptMessages.Year)).getValue()).getValue());
        acc.setAccessories_id(acs_id);
        acc.setEmployee_id(myUI.getUser().getId());
        acc.setId(id);
        return acc;
    }

    private void setMaterialsTable(int cat_id) {
        try {
            DbStudAccessories dbsa = new DbStudAccessories();
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
        NATURAL_COL_ORDER_PAYMENTS = new String[]{sysSettings.button,
            myUI.getMessage(SptMessages.PaymentCategoryType),
            myUI.getMessage(SptMessages.PaymentType), myUI.getMessage(SptMessages.Amount),
            myUI.getMessage(SptMessages.Rate),
            myUI.getMessage(SptMessages.WhoPaid), myUI.getMessage(SptMessages.Date),
            myUI.getMessage(SptMessages.Note), myUI.getMessage(SptMessages.Print)};
        try {
            DbStudPayment dbsa = new DbStudPayment();
            dbsa.connect();
            paymentsTable.setContainerDataSource(
                    dbsa.execSQL_St_Payments(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbsa.close();
            paymentsTable.setVisibleColumns(NATURAL_COL_ORDER_PAYMENTS);
            paymentsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.WhoPaid), 1);
            paymentsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Note), 1);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setCallsTable() {
        NATURAL_COL_ORDER_CALLS = new String[]{sysSettings.button,
            myUI.getMessage(SptMessages.Date),
            myUI.getMessage(SptMessages.WhoCalled), myUI.getMessage(SptMessages.Note)};
        try {
            DbStudentCalls dbsc = new DbStudentCalls();
            dbsc.connect();
            callsTable.setContainerDataSource(
                    dbsc.execSQL_St_Calls(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbsc.close();
            callsTable.setVisibleColumns(NATURAL_COL_ORDER_CALLS);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setInstPlanTable() {
        NATURAL_COL_ORDER_INST_PLAN = new String[]{sysSettings.button,
            myUI.getMessage(SptMessages.Date),
            myUI.getMessage(SptMessages.Amount)};
        try {
            DbStudInstallmentPlan dbip = new DbStudInstallmentPlan();
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
        installmentTable.setVisibleColumns(NATURAL_COL_ORDER_INST_PLAN);
    }

    private void setDiscountsTable() {
        NATURAL_COL_ORDER_DISCOUNTS = new String[]{sysSettings.button,
            myUI.getMessage(SptMessages.Name),
            myUI.getMessage(SptMessages.Amount), myUI.getMessage(SptMessages.Note)};
        try {
            DbStudDiscount dbsd = new DbStudDiscount();
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
        discountsTable.setVisibleColumns(NATURAL_COL_ORDER_DISCOUNTS);
    }

    public IndexedContainer prepareMaterialsGivContainer() {
        if (acsGivContainer == null) {
            acsGivContainer = new IndexedContainer();
            acsGivContainer.addContainerProperty(sysSettings.button, Button.class, null);
            acsGivContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Year), ComboBoxMax.class, null);
            acsGivContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Materials), ComboBoxMultiselectMax.class, null);

        } else {
            acsGivContainer.removeAllItems();
        }
        return acsGivContainer;
    }

    public IndexedContainer prepareMaterialsRecContainer() {
        if (acsRecContainer == null) {
            acsRecContainer = new IndexedContainer();
            acsRecContainer.addContainerProperty(sysSettings.button, Button.class, null);
            acsRecContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Year), ComboBoxMax.class, null);
            acsRecContainer.addContainerProperty(
                    myUI.getMessage(SptMessages.Materials), ComboBoxMultiselectMax.class, null);

        } else {
            acsRecContainer.removeAllItems();
        }
        return acsRecContainer;
    }

    public IndexedContainer preparePaymentsContainer() {
        if (paymentCont == null) {
            paymentCont = new IndexedContainer();
            paymentCont.addContainerProperty(sysSettings.button, Button.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.PaymentCategoryType), ComboBoxMax.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.PaymentType), ComboBoxMax.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Amount), TextField.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Rate), TextField.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.WhoPaid), TextField.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Date), DateField.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Note), TextField.class, null);
            paymentCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Print), Button.class, null);
            paymentCont.addContainerProperty(sysSettings.old_amount, Double.class, 0.0);
            paymentCont.addContainerProperty(sysSettings.old_date, Date.class, null);
            paymentCont.addContainerProperty(sysSettings.old_category, Integer.class, 0);
            paymentCont.addContainerProperty(sysSettings.crud_status, String.class, null);

        } else {
            paymentCont.removeAllItems();
        }
        return paymentCont;
    }

    public IndexedContainer prepareCallsContainer() {
        if (callsCont == null) {
            callsCont = new IndexedContainer();
            callsCont.addContainerProperty(sysSettings.button, Button.class, null);
            callsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Date), String.class, null);
            callsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.WhoCalled), String.class, null);
            callsCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Note), TextField.class, null);
            callsCont.addContainerProperty(sysSettings.crud_status, String.class, null);
        } else {
            callsCont.removeAllItems();
        }
        return callsCont;
    }

    public IndexedContainer prepareDiscountsContainer() {
        if (discountCont == null) {
            discountCont = new IndexedContainer();
            discountCont.addContainerProperty(sysSettings.button, Button.class, null);
            discountCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Name), ComboBoxMax.class, null);
            discountCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Amount), TextField.class, null);
            discountCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Note), TextField.class, null);
            discountCont.addContainerProperty(
                    sysSettings.crud_status, String.class, null);
        } else {
            discountCont.removeAllItems();
        }
        return discountCont;
    }

    public IndexedContainer prepareInstPlanContainer() {
        if (instPlanCont == null) {
            instPlanCont = new IndexedContainer();
            instPlanCont.addContainerProperty(sysSettings.button, Button.class, null);
            instPlanCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Date), DateField.class, null);
            instPlanCont.addContainerProperty(
                    myUI.getMessage(SptMessages.Amount), TextField.class, null);
            instPlanCont.addContainerProperty(sysSettings.status_id, Integer.class, 0);

        } else {
            instPlanCont.removeAllItems();
        }
        return instPlanCont;
    }

    private void addAccessoriesItem(int cat_id) {
        if (cat_id == give) {
            String id = sysSettings.FreshItem + (--r_table_counter);
            if (acsGiveTable.getContainerDataSource().size() == 0) {
                acsGiveTable.setContainerDataSource(prepareMaterialsGivContainer());
            }
            Item item;
            item = ((IndexedContainer) acsGiveTable.getContainerDataSource()).addItemAt(
                    acsGiveTable.getContainerDataSource().size(), id);
            item.getItemProperty(sysSettings.button).setValue(
                    createButton(myUI.getMessage(SptMessages.DeleteButton), id, false, false));
            item.getItemProperty(myUI.getMessage(SptMessages.Year)).setValue(
                    createCombobox(0, myUI.getMessage(SptMessages.Year), id,
                            sysSettings.dbYear, true, false, false, false));
            item.getItemProperty(myUI.getMessage(SptMessages.Materials)).setValue(
                    createComboboxMultiAcs(null, myUI.getMessage(SptMessages.Materials), id, cat_id));
        } else if (cat_id == receive) {
            String id = sysSettings.FreshItem + (--r_table_counter);
            if (acsReceiveTable.getContainerDataSource().size() == 0) {
                acsReceiveTable.setContainerDataSource(prepareMaterialsRecContainer());
            }
            Item item;
            item = ((IndexedContainer) acsReceiveTable.getContainerDataSource()).addItemAt(
                    acsReceiveTable.getContainerDataSource().size(), id);
            item.getItemProperty(sysSettings.button).setValue(
                    createButton(myUI.getMessage(SptMessages.DeleteButton), id, false, false));
            item.getItemProperty(myUI.getMessage(SptMessages.Year)).setValue(
                    createCombobox(0, myUI.getMessage(SptMessages.Year), id,
                            sysSettings.dbYear, true, false, false, false));
            item.getItemProperty(myUI.getMessage(SptMessages.Materials)).setValue(
                    createComboboxMultiAcs(null, myUI.getMessage(SptMessages.Materials), id, cat_id));
        }
    }

    private void addPaymentsItem() {
        NATURAL_COL_ORDER_PAYMENTS = new String[]{sysSettings.button,
            myUI.getMessage(SptMessages.PaymentCategoryType),
            myUI.getMessage(SptMessages.PaymentType), myUI.getMessage(SptMessages.Amount),
            myUI.getMessage(SptMessages.Rate),
            myUI.getMessage(SptMessages.WhoPaid), myUI.getMessage(SptMessages.Date),
            myUI.getMessage(SptMessages.Note), myUI.getMessage(SptMessages.Print)};
        String id = sysSettings.FreshItem + (--r_table_counter);
        if (paymentsTable.getContainerDataSource().size() == 0) {
            paymentsTable.setContainerDataSource(preparePaymentsContainer());
        }
        Item item;
        item = ((IndexedContainer) paymentsTable.getContainerDataSource()).addItemAt(
                paymentsTable.getContainerDataSource().size(), id);
        item.getItemProperty(sysSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, false, false));
        ComboBoxMax cb = createComboboxPayment(0, myUI.getMessage(SptMessages.PaymentCategoryType), id, false, true);
        cb.setId(myUI.getMessage(SptMessages.Payments));
        item.getItemProperty(myUI.getMessage(SptMessages.PaymentCategoryType)).setValue(cb);
        item.getItemProperty(myUI.getMessage(SptMessages.PaymentType)).setValue(
                createCombobox(0, myUI.getMessage(SptMessages.PaymentType), id,
                        sysSettings.dbPaymentType, false, true, false, false));
        TextField tf = createTextfieldDouble(null, myUI.getMessage(SptMessages.Amount), id);
        tf.setId(myUI.getMessage(SptMessages.Payments));
        item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(tf);
        item.getItemProperty(myUI.getMessage(SptMessages.Rate)).setValue(
                createTextfieldDouble(myUI.getDb_currency_rate(), myUI.getMessage(SptMessages.Rate), id));
        String wh_paid = null;
        try {
            DbStudRelative dbsr = new DbStudRelative();
            dbsr.connect();
            wh_paid = dbsr.exec_get_who_paid((Integer) studDataTable.getValue());
            dbsr.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        item.getItemProperty(myUI.getMessage(SptMessages.WhoPaid)).setValue(
                createTextfield(wh_paid, myUI.getMessage(SptMessages.WhoPaid), id, false, false));
        DateField df = createDateField(null, myUI.getMessage(SptMessages.Date), id, true, false);
        df.setId(myUI.getMessage(SptMessages.Payments));
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(df);
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                createTextfieldNote(null, myUI.getMessage(SptMessages.Note), id));
        item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        paymentsTable.setVisibleColumns(NATURAL_COL_ORDER_PAYMENTS);
        paymentsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.WhoPaid), 1);
        paymentsTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Note), 1);
    }

    private void addCallsItem() {
        NATURAL_COL_ORDER_CALLS = new String[]{sysSettings.button,
            myUI.getMessage(SptMessages.Date),
            myUI.getMessage(SptMessages.WhoCalled), myUI.getMessage(SptMessages.Note)};
        String id = sysSettings.FreshItem + (--r_table_counter);
        if (callsTable.getContainerDataSource().size() == 0) {
            callsTable.setContainerDataSource(prepareCallsContainer());
        }
        Item item;
        item = ((IndexedContainer) callsTable.getContainerDataSource()).addItemAt(
                callsTable.getContainerDataSource().size(), id);
        item.getItemProperty(sysSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, false, false));
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(sysSettings.mysql_df.format(new Date()));
        item.getItemProperty(myUI.getMessage(SptMessages.WhoCalled)).setValue(myUI.getUser().getFullname());
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                createTextfieldNote(null, myUI.getMessage(SptMessages.Note), id));
        item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        callsTable.setVisibleColumns(NATURAL_COL_ORDER_CALLS);

    }

    private void addDiscountsItem() {
        NATURAL_COL_ORDER_DISCOUNTS = new String[]{sysSettings.button,
            myUI.getMessage(SptMessages.Name),
            myUI.getMessage(SptMessages.Amount), myUI.getMessage(SptMessages.Note)};
        discCounter++;
        String id = sysSettings.FreshItem + (--r_table_counter);
        if (discountsTable.getContainerDataSource().size() == 0) {
            discountsTable.setContainerDataSource(prepareDiscountsContainer());
        }
        Item item;
        item = ((IndexedContainer) discountsTable.getContainerDataSource()).addItemAt(
                discountsTable.getContainerDataSource().size(), id);
        item.getItemProperty(sysSettings.button).setValue(
                createButton(myUI.getMessage(SptMessages.DeleteButton), id, false, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(
                createComboboxDisc(0, myUI.getMessage(SptMessages.Name), id));
        item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                createTextfieldDisc(null, null, myUI.getMessage(SptMessages.DiscountAmount), id, true));
        item.getItemProperty(myUI.getMessage(SptMessages.Note)).setValue(
                createTextfield(null, myUI.getMessage(SptMessages.Note), id, true, false));
        item.getItemProperty(sysSettings.crud_status).setValue(myUI.getMessage(SptMessages.Insert));
        discountsTable.setVisibleColumns(NATURAL_COL_ORDER_DISCOUNTS);
    }

    private void addInstallmentPlanItem(Boolean autoFill) {
        NATURAL_COL_ORDER_INST_PLAN = new String[]{sysSettings.button,
            myUI.getMessage(SptMessages.Date),
            myUI.getMessage(SptMessages.Amount)};
        if (!autoFill) {
            String id = sysSettings.FreshItem + (--r_table_counter);
            if (installmentTable.getContainerDataSource().size() == 0) {
                installmentTable.setContainerDataSource(prepareInstPlanContainer());
            }
            Item item;
            item = ((IndexedContainer) installmentTable.getContainerDataSource()).addItemAt(
                    installmentTable.getContainerDataSource().size(), id);
            item.getItemProperty(sysSettings.button).setValue(
                    createButton(myUI.getMessage(SptMessages.DeleteButton), id, false, false));
            item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                    createDateField(currDate.getValue(), myUI.getMessage(SptMessages.Date), id, false, true));
            item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                    createTextfieldDouble(null, myUI.getMessage(SptMessages.Amount), id));
            item.getItemProperty(sysSettings.status_id).setValue(1);
        } else if (autoFill) {
            installmentTable.removeAllItems();
            Iterator iter = instPlanCont.getItemIds().iterator();
            Double s = 0.0;
            left = instCtrAmount;
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
                Double divSum = left / Integer.parseInt(divideTF.getValue());
                Calendar cal = Calendar.getInstance();
                cal.setTime(currDate.getValue());
                cal.add(Calendar.MONTH, i + 1);
                String id = sysSettings.FreshItem + (--r_table_counter);
                if (installmentTable.getContainerDataSource().size() == 0) {
                    installmentTable.setContainerDataSource(prepareInstPlanContainer());
                }
                Item item;
                item = ((IndexedContainer) installmentTable.getContainerDataSource()).addItemAt(
                        installmentTable.getContainerDataSource().size(), id);
                item.getItemProperty(sysSettings.button).setValue(
                        createButton(myUI.getMessage(SptMessages.DeleteButton), id, false, false));
                item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                        createDateField(cal.getTime(), myUI.getMessage(SptMessages.Date), id, false, true));
                item.getItemProperty(myUI.getMessage(SptMessages.Amount)).setValue(
                        createTextfieldDouble(sysSettings.round(divSum, 2), myUI.getMessage(SptMessages.Amount), id));
                item.getItemProperty(sysSettings.status_id).setValue(1);
            }
        }
        installmentTable.setVisibleColumns(NATURAL_COL_ORDER_INST_PLAN);
    }

    private Set<?> convertStrToSet(String str) {
        String[] strArr = str.split(",");
        HashSet<String> hs = new HashSet<String>(strArr.length);
        for (int i = 0; i < strArr.length; i++) {
            hs.add(strArr[i]);
        }
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
        installmentTable.setCaption(myUI.getMessage(SptMessages.InstallmentPlan));
        installmentTable.setStyleName(ValoTheme.TABLE_SMALL);
        installmentTable.setNullSelectionAllowed(false);

        discountsTable = new FormattedTable();
        discountsTable.setSizeFull();
        discountsTable.setSelectable(false);
        discountsTable.setCaption(myUI.getMessage(SptMessages.Discount));
        discountsTable.setStyleName(ValoTheme.TABLE_SMALL);
        discountsTable.setNullSelectionAllowed(false);

        contractCB = new ComboBoxMax(myUI.getMessage(SptMessages.Contract));
        contractCB.setWidth("100%");
        contractCB.setNullSelectionAllowed(false);
        contractCB.setRequired(true);
        contractCB.addValueChangeListener(this);
        contractCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        contractCB.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        contractCB.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
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

        ObjectProperty<Double> property = new ObjectProperty<Double>(0.0);
        initialPaymentTF = new TextField(myUI.getMessage(SptMessages.InitialPayment), property);
        initialPaymentTF.addValidator(new DoubleRangeValidator(myUI.getMessage(SptMessages.NotifWrongValue), 0.01, null));
        initialPaymentTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        initialPaymentTF.setRequired(false);
        initialPaymentTF.setNullSettingAllowed(true);
        initialPaymentTF.setNullRepresentation("");
        initialPaymentTF.setConverter(sysSettings.getStringToDoubleConverter());
        initialPaymentTF.addValueChangeListener(this);

        currDate = new DateField(myUI.getMessage(SptMessages.StartDate));
        currDate.setWidth("100%");
        currDate.setStyleName(ValoTheme.DATEFIELD_TINY);
        currDate.setDateFormat(sysSettings.datePattern);

        divideTF = new TextField(myUI.getMessage(SptMessages.DivideInto));
        divideTF.setWidth("100%");
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

        contractTabLay.addComponent(contractCB, 0, 0);
        contractTabLay.addComponent(initialPaymentTF, 1, 0);
        contractTabLay.addComponent(instPlanLay, 5, 0);
        contractTabLay.setComponentAlignment(instPlanLay, Alignment.TOP_RIGHT);
        contractTabLay.setRowExpandRatio(2, 1);
        contractTabLay.addComponent(plusDiscButton, 1, 1);
        contractTabLay.setComponentAlignment(plusDiscButton, Alignment.BOTTOM_RIGHT);
        contractTabLay.addComponent(currDate, 2, 1);
        contractTabLay.addComponent(divideTF, 3, 1);
        contractTabLay.addComponent(divideBtn, 4, 1);
        contractTabLay.setComponentAlignment(divideBtn, Alignment.BOTTOM_LEFT);
        contractTabLay.addComponent(plusInstButton, 5, 1);
        contractTabLay.setComponentAlignment(plusInstButton, Alignment.BOTTOM_RIGHT);
        contractTabLay.addComponent(discountsTable, 0, 2, 1, 2);
        contractTabLay.addComponent(installmentTable, 2, 2, 5, 2);

        contractTabLay.setColumnExpandRatio(0, 1);
        contractTabLay.setColumnExpandRatio(1, 1);
        contractTabLay.setColumnExpandRatio(2, 1);
        contractTabLay.setColumnExpandRatio(3, 1);
        contractTabLay.setColumnExpandRatio(4, 0.2f);
        contractTabLay.setColumnExpandRatio(5, 1);

    }

    private Boolean validateContractsTab(ComponentContainer layout) {
        if (tabs.getSelectedTab() == tabs.getTab(layout).getComponent()) {
            Iterator<Component> it = layout.iterator();
            ArrayList<String> dates = new ArrayList<String>();
            ArrayList<Integer> discount_ids = new ArrayList<Integer>();
            while (it.hasNext()) {
                Component next = it.next();
                if (next instanceof ComboBoxMax) {
                    if (!((ComboBoxMax) next).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                } else if (next instanceof TextField) {
                    if (!((TextField) next).isValid()) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                } else if (next instanceof Table && next != discountsTable) {
                    Iterator iter = instPlanCont.getItemIds().iterator();
                    Double amount = 0.0;
                    while (iter.hasNext()) {
                        Object obj = iter.next();
                        if (!((TextField) installmentTable.getItem(obj).getItemProperty(
                                myUI.getMessage(SptMessages.Amount)).getValue()).isValid()) {
                            Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                    Notification.Type.WARNING_MESSAGE);
                            return false;
                        }
                        if (!((DateField) installmentTable.getItem(obj).getItemProperty(
                                myUI.getMessage(SptMessages.Date)).getValue()).isValid()) {
                            Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                    Notification.Type.WARNING_MESSAGE);
                            return false;
                        }
                        if (((DateField) installmentTable.getItem(obj).getItemProperty(
                                myUI.getMessage(SptMessages.Date)).getValue()).isValid()) {
                            if (dates.contains(sysSettings.mysql_df.format((Date) ((DateField) installmentTable.getItem(obj).getItemProperty(
                                    myUI.getMessage(SptMessages.Date)).getValue()).getValue()))) {
                                Notification.show(myUI.getMessage(SptMessages.NotifSameDatesAreNotAllowed),
                                        Notification.Type.WARNING_MESSAGE);
                                return false;
                            } else {
                                dates.add(sysSettings.mysql_df.format((Date) ((DateField) installmentTable.getItem(obj).getItemProperty(
                                        myUI.getMessage(SptMessages.Date)).getValue()).getValue()));
                            }
                        }
                        if ((Integer) installmentTable.getItem(obj).getItemProperty(
                                sysSettings.status_id).getValue() != 0) {
                            amount += (Double) (((TextField) installmentTable.getItem(obj).getItemProperty(
                                    myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue());
                        }
                    }
                    recount();

                    if (sysSettings.round(instCtrAmount, 2) != sysSettings.round(amount, 2)) {
                        Notification.show(myUI.getMessage(SptMessages.NotifWrongSumInstSum),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }

                } else if (next instanceof Table && next != instPlanCont) {
                    Iterator iter = discountsTable.getItemIds().iterator();
                    while (iter.hasNext()) {
                        Object obj = iter.next();
                        if (!((TextField) discountsTable.getItem(obj).getItemProperty(
                                myUI.getMessage(SptMessages.Amount)).getValue()).isValid()) {
                            Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                    Notification.Type.WARNING_MESSAGE);
                            return false;
                        }
                        if (((TextField) discountsTable.getItem(obj).getItemProperty(
                                myUI.getMessage(SptMessages.Amount)).getValue()).isValid()) {
                            if (discount_ids.contains((Integer) ((ComboBoxMax) discountsTable.getItem(obj).getItemProperty(
                                    myUI.getMessage(SptMessages.Name)).getValue()).getValue())) {
                                Notification.show(myUI.getMessage(SptMessages.NotifSameDiscountsAreNotAllowed),
                                        Notification.Type.WARNING_MESSAGE);
                                return false;
                            } else {
                                discount_ids.add((Integer) ((ComboBoxMax) discountsTable.getItem(obj).getItemProperty(
                                        myUI.getMessage(SptMessages.Name)).getValue()).getValue());
                            }
                        }
                        if (!((ComboBoxMax) discountsTable.getItem(obj).getItemProperty(
                                myUI.getMessage(SptMessages.Name)).getValue()).isValid()) {
                            Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                                    Notification.Type.WARNING_MESSAGE);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void insertContractToDb() {
        try {
            DbStudContract dbc = new DbStudContract();
            dbc.connect();
            if (isNewContract) {
                dbc.exec_delete((Integer) studDataTable.getValue(), myUI.getUser().getCurrent_year().getId());
                dbc.exec_insert_st_contract(getStudentContract((Integer) studDataTable.getValue(),
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

    private StudContract getStudentContract(int st_id, int year_id) {
        double debt = 0.0;
        try {
            DbStudContract dbsc = new DbStudContract();
            dbsc.connect();
            debt = dbsc.exec_get_debt((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            dbsc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        StudContract c = new StudContract();
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
        StudPayment ip = null;
        try {
            DbStudContract dbsc = new DbStudContract();
            DbStudPayment dbsp = new DbStudPayment();
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
        if (ip == null) {
            initialPaymentTF.removeValueChangeListener(this);
            initialPaymentTF.setValue(null);
            initialPaymentTF.addValueChangeListener(this);
        } else {
            initialPaymentTF.removeValueChangeListener(this);
            initialPaymentTF.getPropertyDataSource().setValue(ip.getAmount());
            initialPaymentTF.addValueChangeListener(this);
            initialPaymentTF.setData(ip);
        }
    }

    private StudDiscount getStudentDiscount(int st_id, int year_id, String disc_id) {
        StudDiscount d = new StudDiscount();
        //if discount type is Free$|%. setFreeAmount()
        if ((((ComboBoxMax) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue())
                .getContainerProperty(((ComboBoxMax) discountsTable
                        .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                        myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("3"))
                || (((ComboBoxMax) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue())
                        .getContainerProperty(((ComboBoxMax) discountsTable
                                .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("4"))) {
            d.setFree_entry_amount((Double) (((TextField) discountsTable.getContainerProperty(disc_id,
                    myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue()));
        }
        d.setDiscount_id(Integer.parseInt(((ComboBoxMax) discountsTable
                .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue()).getValue().toString()));
        d.setStudent_id(st_id);
        d.setEmployee_id(myUI.getUser().getId());
        d.setYear_id(year_id);
        d.setId(disc_id);
        d.setNote(((TextField) discountsTable
                .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Note)).getValue()).getValue().toString());
        if (contractCB.getValue() != null) {
            discountAmount = (Double) (((TextField) discountsTable.getContainerProperty(disc_id,
                    myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue());
            //if Discount type type is %.
            if ((((ComboBoxMax) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue())
                    .getContainerProperty(((ComboBoxMax) discountsTable
                            .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("1"))
                    || (((ComboBoxMax) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue())
                            .getContainerProperty(((ComboBoxMax) discountsTable
                                    .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                    myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("3"))) {
                d.setDiscount_value(sysSettings.round((contr_with_disc * discountAmount / 100), 2));
                contr_with_disc -= contr_with_disc * discountAmount / 100;

                //if discount type is $.
            } else if ((((ComboBoxMax) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue())
                    .getContainerProperty(((ComboBoxMax) discountsTable
                            .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("2"))
                    || (((ComboBoxMax) discountsTable.getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue())
                            .getContainerProperty(((ComboBoxMax) discountsTable
                                    .getContainerProperty(disc_id, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                    myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("4"))) {
                d.setDiscount_value(sysSettings.round((discountAmount), 2));
                contr_with_disc = contr_with_disc - discountAmount;
            }

        }
        return d;
    }

    private void recount() {
        StudContract c = new StudContract();
        StudPayment sp = new StudPayment();
        IndexedContainer discCont = new IndexedContainer();
        try {
            DbStudContract dbsc = new DbStudContract();
            DbStudDiscount dbsd = new DbStudDiscount();
            DbStudPayment dbsp = new DbStudPayment();
            dbsc.connect();
            dbsd.connect();
            dbsp.connect();
            c = dbsc.exec_recount_contract((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            discCont = dbsd.exec_disc_strCont(myUI, (Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            debt = dbsc.exec_get_debt((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            sp = dbsp.exec_recount_payment((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            ttl_payment = sp.getTtl_pay();
            init_payment = sp.getInit_pay();
            kOplate = c.getContr_with_disc() + debt;
            ttl_left = (c.getContr_with_disc() + debt) - ttl_payment;
            dbsc.close();
            dbsd.close();
            dbsp.close();
            contr_id = c.getContract_id();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        Iterator iter = discCont.getItemIds().iterator();
        discounts = "";
        while (iter.hasNext()) {
            Object next = iter.next();
            if ((Integer) discCont.getContainerProperty(next,
                    sysSettings.discount_type_id).getValue() == 1) {
                discounts += sysSettings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue()).toString() + "%";
                if (iter.hasNext()) {
                    discounts += ", ";
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    sysSettings.discount_type_id).getValue() == 2) {
                discounts += sysSettings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue()).toString() + "$";
                if (iter.hasNext()) {
                    discounts += ", ";
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    sysSettings.discount_type_id).getValue() == 3) {
                discounts += sysSettings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.FreeAmount)).getValue()).toString() + "%";
                if (iter.hasNext()) {
                    discounts += ", ";
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    sysSettings.discount_type_id).getValue() == 4) {
                discounts += sysSettings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.FreeAmount)).getValue()).toString() + "$";
                if (iter.hasNext()) {
                    discounts += ", ";
                }
            }
        }
        if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.prmContractInfo)) {
            modifyBtn.setEnabled(true);
            contractLab.setValue(myUI.getMessage(SptMessages.Contract) + ": " + sysSettings.dFormat.format(c.getAmount()) + " $");
            discountLab.setValue(myUI.getMessage(SptMessages.Discount) + ": " + discounts);
            if (debt > 0) {
                debtLab.setStyleName(ValoTheme.LABEL_FAILURE);
            } else {
                debtLab.setStyleName(ValoTheme.LABEL_SUCCESS);
            }
            debtLab.setValue(myUI.getMessage(SptMessages.PreviousYearDebt) + ": " + sysSettings.dFormat.format(debt) + " $");
            netLab.setValue(myUI.getMessage(SptMessages.Net) + ": " + sysSettings.dFormat.format(c.getContr_with_disc() + debt) + " $");
            paidLab.setValue(myUI.getMessage(SptMessages.Paid) + ": " + sysSettings.dFormat.format(ttl_payment) + " $");
        }
        if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.prmContractInfoLeftDebt)) {
            leftLab.setValue(myUI.getMessage(SptMessages.Left) + ": " + sysSettings.dFormat.format((c.getContr_with_disc() + debt) - ttl_payment) + " $");
            if ((c.getPlan_debt() - ttl_payment) > 0) {
                planDebt.setStyleName(ValoTheme.LABEL_FAILURE);
                planDebt.setValue(myUI.getMessage(SptMessages.InstPlanDebt) + ": " + sysSettings.dFormat.format(c.getPlan_debt() - ttl_payment) + " $");
            } else {
                planDebt.setStyleName(ValoTheme.LABEL_SUCCESS);
                planDebt.setValue(myUI.getMessage(SptMessages.InstPlanDebt) + ": " + sysSettings.dFormat.format(0.0) + " $");
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
        eduStatCont.getContainerProperty((Integer) studDataTable
                .getContainerProperty(studDataTable.getValue(),
                        sysSettings.education_status_id).getValue(), sysSettings.count)
                .setValue(((Integer) eduStatCont.getContainerProperty((Integer) studDataTable
                        .getContainerProperty(studDataTable.getValue(),
                                sysSettings.education_status_id).getValue(),
                        sysSettings.count).getValue()) - 1);
        eduStatCont.getContainerProperty(2, sysSettings.count)
                .setValue(((Integer) eduStatCont.getContainerProperty(2,
                        sysSettings.count).getValue()) + 1);
        repaint();
        studDataTable.getContainerProperty(studDataTable.getValue(),
                sysSettings.education_status_id).setValue(statusCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(SptMessages.EducationStatus)).setValue(statusCB
                .getContainerProperty(statusCB.getValue(),
                        myUI.getMessage(SptMessages.Name)).getValue().toString());

    }

    private void addRowIftableEmpty() {
        if (tabs.getSelectedTab() == tabs.getTab(acsGiveTableLay).getComponent()
                && acsGiveTable.size() == 0) {
            plusMatGiveButton.click();
        } else if (tabs.getSelectedTab() == tabs.getTab(acsReceiveTableLay).getComponent()
                && acsReceiveTable.size() == 0) {
            plusMatReceiveButton.click();
        } else if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                && installmentTable.size() == 0) {
            plusInstButton.click();
        } else if (tabs.getSelectedTab() == tabs.getTab(payTablelay).getComponent()
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
                Iterator iter = discountsTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    if (((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue() != null
                            && (!((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue().equals(""))) {
                        discountAmount = (Double) (((TextField) discountsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue());
                        //if Discount type type is %.
                        if ((((ComboBoxMax) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue())
                                .getContainerProperty(((ComboBoxMax) discountsTable
                                        .getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                        myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("1"))
                                || (((ComboBoxMax) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue())
                                        .getContainerProperty(((ComboBoxMax) discountsTable
                                                .getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                                myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("3"))) {
                            instCtrAmount -= instCtrAmount * discountAmount / 100;

                            //if discount type is $.
                        } else if ((((ComboBoxMax) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue())
                                .getContainerProperty(((ComboBoxMax) discountsTable
                                        .getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                        myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("2"))
                                || (((ComboBoxMax) discountsTable.getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue())
                                        .getContainerProperty(((ComboBoxMax) discountsTable
                                                .getContainerProperty(next, myUI.getMessage(SptMessages.Name)).getValue()).getValue(),
                                                myUI.getMessage(SptMessages.DiscountType)).getValue().toString().equals("4"))) {
                            instCtrAmount = instCtrAmount - discountAmount;
                        }
                    }
                }
            }
            if (installmentTable.size() > 0) {
                Iterator iter2 = installmentTable.getItemIds().iterator();
                while (iter2.hasNext()) {
                    Object obj = iter2.next();
                    if (((TextField) installmentTable.getItem(obj).getItemProperty(
                            myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().getValue() != null) {
                        if ((Integer) installmentTable.getItem(obj).getItemProperty(sysSettings.status_id).getValue() != 0) {
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
            netIPlanTtlLab.setValue(myUI.getMessage(SptMessages.ToPlan) + ": " + sysSettings.dFormat.format(sysSettings.round(instCtrAmount, 2)) + " $");
            instPlanTtlLab.setValue(myUI.getMessage(SptMessages.InstallmentPlanTotal) + ": " + sysSettings.dFormat.format(instPlanContSum) + " $");
            if (instPlanCont != null) {
                instPlanDifLab.setValue(myUI.getMessage(SptMessages.Difference) + ": " + sysSettings.dFormat.format(sysSettings.round(instCtrAmount, 2)
                        - sysSettings.round(instPlanContSum, 2)) + " $");
            }
            if (contractCB.getValue() != null) {
                tabContractLab.setValue(myUI.getMessage(SptMessages.Contract) + ": "
                        + sysSettings.dFormat.format(contractCB.getContainerProperty(contractCB.getValue(),
                                myUI.getMessage(SptMessages.Amount)).getValue()) + " $");
                tabContractNetLab.setValue(myUI.getMessage(SptMessages.Net) + ": "
                        + sysSettings.dFormat.format(netContrAmount) + " $");
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
                    + sysSettings.dFormat.format(contractCB.getContainerProperty(contractCB.getValue(),
                            myUI.getMessage(SptMessages.Amount)).getValue()) + " $");
        }

        tabContractNetLab = new Label();
        tabContractNetLab.setSizeUndefined();
        tabContractNetLab.setContentMode(ContentMode.HTML);
        if (contractCB.getValue() != null) {
            tabContractNetLab.setValue(myUI.getMessage(SptMessages.Net) + ": "
                    + sysSettings.dFormat.format(netContrAmount + " $"));
        }

        netIPlanTtlLab = new Label();
        netIPlanTtlLab.setSizeUndefined();
        netIPlanTtlLab.setContentMode(ContentMode.HTML);
        if (instCtrAmount != null) {
            netIPlanTtlLab.setValue(myUI.getMessage(SptMessages.ToPlan) + ": " + sysSettings.dFormat.format(instCtrAmount) + " $");
        }

        instPlanTtlLab = new Label();
        instPlanTtlLab.setSizeUndefined();
        instPlanTtlLab.setContentMode(ContentMode.HTML);
        if (instPlanCont != null) {
            instPlanTtlLab.setValue(myUI.getMessage(SptMessages.InstallmentPlanTotal) + ": " + sysSettings.dFormat.format(instPlanContSum) + " $");
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
                    myUI.getMessage(SptMessages.Passport)).getValue()).setRequiredError(myUI.getMessage(SptMessages.NotifWrongValue));
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Passport)).getValue()).addValidator(new StringLengthValidator(
                    myUI.getMessage(SptMessages.NotifWrongValue), 1, 100, false));

            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.WorkPlace)).getValue()).setRequired(true);
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.WorkPlace)).getValue()).setRequiredError(myUI.getMessage(SptMessages.NotifWrongValue));
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.WorkPlace)).getValue()).addValidator(new StringLengthValidator(
                    myUI.getMessage(SptMessages.NotifWrongValue), 1, 100, false));

            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Address)).getValue()).setRequired(true);
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Address)).getValue()).setRequiredError(myUI.getMessage(SptMessages.NotifWrongValue));
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Address)).getValue()).addValidator(new StringLengthValidator(
                    myUI.getMessage(SptMessages.NotifWrongValue), 1, 100, false));

            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Phone)).getValue()).setRequired(true);
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Phone)).getValue()).setRequiredError(myUI.getMessage(SptMessages.NotifWrongValue));
            ((TextField) relativesTable.getContainerProperty(((CheckBox) property).getData(),
                    myUI.getMessage(SptMessages.Phone)).getValue()).addValidator(new StringLengthValidator(
                    myUI.getMessage(SptMessages.NotifWrongValue), 1, 100, false));
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
            if ((Integer) ((ComboBoxMax) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                    myUI.getMessage(SptMessages.Name)).getValue())
                    .getContainerProperty(property.getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 1
                    || (Integer) ((ComboBoxMax) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                            myUI.getMessage(SptMessages.Name)).getValue())
                            .getContainerProperty(property.getValue(),
                                    myUI.getMessage(SptMessages.DiscountType)).getValue() == 2) {
                ((TextField) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).setEnabled(false);
                ((TextField) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).removeAllValidators();
                ((TextField) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().setValue(
                        (Double) ((ComboBoxMax) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                                myUI.getMessage(SptMessages.Name)).getValue())
                                .getContainerProperty(property.getValue(),
                                        myUI.getMessage(SptMessages.Amount)).getValue());
            } else if ((Integer) ((ComboBoxMax) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                    myUI.getMessage(SptMessages.Name)).getValue())
                    .getContainerProperty(property.getValue(),
                            myUI.getMessage(SptMessages.DiscountType)).getValue() == 3
                    || (Integer) ((ComboBoxMax) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                            myUI.getMessage(SptMessages.Name)).getValue())
                            .getContainerProperty(property.getValue(),
                                    myUI.getMessage(SptMessages.DiscountType)).getValue() == 4) {
                ((TextField) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).removeAllValidators();

                ((TextField) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).addValidator(new DoubleRangeValidator(
                        myUI.getMessage(SptMessages.NotifWrongValue), 0.1,
                        (Double) ((ComboBoxMax) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                                myUI.getMessage(SptMessages.Name)).getValue())
                                .getContainerProperty(property.getValue(),
                                        myUI.getMessage(SptMessages.Amount)).getValue()));
                ((TextField) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).setEnabled(true);
                ((TextField) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).getPropertyDataSource().setValue(null);
                ((TextField) discountsTable.getContainerProperty(((ComboBoxMax) property).getData(),
                        myUI.getMessage(SptMessages.Amount)).getValue()).setRequired(true);
            }
        }
    }

    private void saveDiscoutns() {
        try {
            DbStudDiscount dbsd = new DbStudDiscount();
            dbsd.connect();
            if (delDiscIds.size() > 0) {
                for (int i = 0; i < delDiscIds.size(); i++) {
                    dbsd.exec_update_emp_id(myUI.getUser().getId(), delDiscIds.get(i));
                    dbsd.exec_delete(delDiscIds.get(i));
                }
            }
            contr_with_disc = (Double) contractCB.getContainerProperty(contractCB.getValue(), myUI.getMessage(SptMessages.Amount)).getValue();
            if (discountsTable.getContainerDataSource().size() > 0) {
                Iterator iter = discountsTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    if (discountsTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        dbsd.exec_update(getStudentDiscount((Integer) studDataTable.getValue(),
                                myUI.getUser().getCurrent_year().getId(), ((String) next)));
                    } else if (discountsTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbsd.exec_insert_st_discount(getStudentDiscount((Integer) studDataTable.getValue(),
                                myUI.getUser().getCurrent_year().getId(), ((String) next)));
                    }
                }
            }
            delDiscIds.clear();
            dbsd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void saveRelatives(int student_id) {
        try {
            DbStudRelative dbsr = new DbStudRelative();
            dbsr.connect();
            if (delRelIds.size() > 0) {
                for (int i = 0; i < delRelIds.size(); i++) {
                    dbsr.exec_delete(delRelIds.get(i));
                }
            }
            if (relativesTable.getContainerDataSource().size() > 0) {
                Iterator iter = relativesTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    if (relativesTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        dbsr.exec_update(getRelative(Integer.parseInt(next.toString()), student_id, relativesTable.getItem(next)));
                    } else if (relativesTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
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
            DbStudPayment dbsp = new DbStudPayment();
            DbAccTransactions dbat = new DbAccTransactions();
            dbsp.connect();
            for (int i = 0; i < delPayIds.size(); i++) {
                dbat.exec_delete(sysSettings.dbColumnStudent_payments_id, delPayIds.get(i), dbsp.getConnection());//delete transaction
                dbsp.exec_update_emp_id(myUI.getUser().getId(), delPayIds.get(i));
                dbsp.exec_delete(delPayIds.get(i));
            }
            if (paymentsTable.getContainerDataSource().size() > 0) {
                Iterator iter = paymentsTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    if (paymentsTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        StudPayment sp = getPayment(Integer.parseInt(next.toString()), student_id, paymentsTable.getItem(next));
                        AccTransaction tr = new AccTransaction();
                        tr.setAmount(sp.getAmount());
                        tr.setDate(sp.getModification_date());
                        tr.setCategory_id((Integer) ((ComboBoxMax) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                                sysSettings.acc_category_id).getValue());
                        tr.setCurrency_id(2);
                        tr.setCurrency_rate(sp.getRate());
                        tr.setNote(sp.getNoteForKassa());
                        tr.setEmployee_id(sp.getEmplooyee_id());
                        tr.setSchool_id(sp.getSchool_id());
                        tr.setStudent_payments_id(sp.getId());
                        dbsp.exec_update(sp);
                        if (sp.getModification_date().after(myUI.getUser().getTransactions_start_date())
                                || sp.getModification_date().equals(myUI.getUser().getTransactions_start_date())) {
                            int update_status = dbat.exec_update(tr, sysSettings.dbColumnStudent_payments_id,
                                    tr.getStudent_payments_id(), dbsp.getConnection());//update transaction normal
                            if (update_status == 0) {
                                dbat.exec_insert(tr, dbsp.getConnection());//insert transaction if it doesn't exist, on payment update if payment date is after transactions start date
                            }
                        } else if (sp.getModification_date().before(myUI.getUser().getTransactions_start_date())) {
                            dbat.exec_delete(sysSettings.dbColumnStudent_payments_id, (String) next, dbsp.getConnection()); //delete transaction on payment update if payment date is before transactions start date
                        }
                    } else if (paymentsTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        int order_num = dbsp.getMaxOrderNum((Integer) studDataTable.getValue());
                        StudPayment sp = getPayment(0, student_id, paymentsTable.getItem(next));
                        int payment_id = dbsp.exec_insert(sp, order_num);
                        sp.setId(payment_id);//payment Id
                        AccTransaction tr = new AccTransaction();
                        tr.setAmount(sp.getAmount());
                        tr.setDate(sp.getModification_date());
                        tr.setCategory_id((Integer) ((ComboBoxMax) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                                sysSettings.acc_category_id).getValue());
                        tr.setCurrency_id(2);
                        tr.setCurrency_rate(sp.getRate());
                        tr.setNote(sp.getNoteForKassa());
                        tr.setEmployee_id(sp.getEmplooyee_id());
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
                    dbat.exec_delete(sysSettings.dbColumnStudent_payments_id, delPayIds.get(i),
                            dbat.getConnection());//delete transaction
                }
                AccTransaction tr = null;
                Iterator iter = paymentsTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    if (!next.toString().equals(payment_id)) {
                        StudPayment sp = getPayment(0, 0, paymentsTable.getItem(next));
                        tr = new AccTransaction();
                        tr.setAmount(Double.valueOf(((TextField) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Amount)).getValue()).getValue()));
                        tr.setDate(sp.getModification_date());
                        tr.setCategory_id((Integer) ((ComboBoxMax) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                                sysSettings.acc_category_id).getValue());
                        tr.setCurrency_id(2);
                        tr.setCurrency_rate(sp.getRate());
                        tr.setNote(sp.getNoteForKassa());
                        tr.setEmployee_id(sp.getEmplooyee_id());
                        tr.setSchool_id(sp.getSchool_id());
                        if (paymentsTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
                                .equals(myUI.getMessage(SptMessages.Update))) {
                            tr.setStudent_payments_id(Integer.parseInt(next.toString()));
                            if (sp.getModification_date().after(myUI.getUser().getTransactions_start_date())
                                    || sp.getModification_date().equals(myUI.getUser().getTransactions_start_date())) {
                                int update_status = dbat.exec_update(tr, sysSettings.dbColumnStudent_payments_id,
                                        tr.getStudent_payments_id(), dbat.getConnection());//update transaction normal
                                if (update_status == 0) {
                                    dbat.exec_insert(tr, dbat.getConnection());//insert transaction if it doesn't exist, on payment update if payment date is after transactions start date
                                }
                            } else if (sp.getModification_date().before(myUI.getUser().getTransactions_start_date())) {
                                dbat.exec_delete(sysSettings.dbColumnStudent_payments_id, (String) next, dbat.getConnection()); //delete transaction on payment update if payment date is before transactions start date
                            }
                        } else if (paymentsTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
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
                Iterator iter = callsTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object next = iter.next();
                    if (callsTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Update))) {
                        dbsc.exec_update(((TextField) callsTable.getItem(next).getItemProperty(
                                myUI.getMessage(SptMessages.Note)).getValue()).getValue().toString(), Integer.parseInt(next.toString()));
                    } else if (callsTable.getContainerProperty(next, sysSettings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(SptMessages.Insert))) {
                        dbsc.exec_insert(student_id, myUI.getUser().getCurrent_year().getId(), myUI.getUser().getId(),
                                ((TextField) callsTable.getItem(next).getItemProperty(
                                        myUI.getMessage(SptMessages.Note)).getValue()).getValue().toString());
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
        eduStatTtlLab.setValue("active:" + eduStatCont.getContainerProperty(2, sysSettings.count).getValue().toString()
                + "&emsp;" + "pre-registered:" + eduStatCont.getContainerProperty(1, sysSettings.count).getValue().toString()
                + "&emsp;" + "not confirmed:" + eduStatCont.getContainerProperty(3, sysSettings.count).getValue().toString()
                + "&emsp;" + "graduated:" + eduStatCont.getContainerProperty(5, sysSettings.count).getValue().toString()
                + "&emsp;" + "out of:" + eduStatCont.getContainerProperty(4, sysSettings.count).getValue().toString()
                + "&emsp;" + "total:" + eduStatCont.getContainerProperty(6, sysSettings.count).getValue().toString());
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
            DbStudContract dbsc = new DbStudContract();
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
        studDataTable.setVisibleColumns(NATURAL_COL_ORDER);
    }

}
