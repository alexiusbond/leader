package kg.alex.spt;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.dao.DbStudent;
import kg.alex.spt.dao.DbStudentOrder;
import kg.alex.spt.domain.School;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.AccessoriesDefinitionView;
import kg.alex.spt.ui.AccountingReportsView;
import kg.alex.spt.ui.SettingsView;
import kg.alex.spt.ui.BackupView;
import kg.alex.spt.ui.CallsView;
import kg.alex.spt.ui.ChangeUserData;
import kg.alex.spt.ui.ClassNameDefinitionView;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.ui.ContractDefintionView;
import kg.alex.spt.ui.DefinitionView;
import kg.alex.spt.ui.DiscountDefinitionView;
import kg.alex.spt.ui.EmployeeDefinitionView;
import kg.alex.spt.ui.EmployeeTransferView;
import kg.alex.spt.ui.HRReportsView;
import kg.alex.spt.ui.HomePageView;
import kg.alex.spt.ui.ImportFromExcelView;
import kg.alex.spt.ui.AccCategoriesDefinitionView;
import kg.alex.spt.ui.TransfersView;
import kg.alex.spt.ui.IssueOrderView;
import kg.alex.spt.ui.LeavingReasonsDefinitionView;
import kg.alex.spt.ui.LessonAssessmentView;
import kg.alex.spt.ui.MessagesView;
import kg.alex.spt.ui.PayoutsView;
import kg.alex.spt.ui.PositionDefinitionView;
import kg.alex.spt.ui.ReportsView;
import kg.alex.spt.ui.SchoolDefinitionView;
import kg.alex.spt.ui.SchoolModificationView;
import kg.alex.spt.ui.StockDefinitionView;
import kg.alex.spt.ui.StockIncomeView;
import kg.alex.spt.ui.StockOutcomeView;
import kg.alex.spt.ui.StockReportsView;
import kg.alex.spt.ui.StudentDefinitionView;
import kg.alex.spt.ui.TransactionsView;
import kg.alex.spt.ui.YearDefinitionView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

public class AuthenticatedScreen extends VerticalLayout implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(AuthenticatedScreen.class);
    private MyVaadinUI myUI;
    private Subject currentUser = SecurityUtils.getSubject();
    private VerticalSplitPanel verticalPanel;
    private GridLayout upperLay = new GridLayout(3, 3);
    private Button changePassButton;
    public ComboBoxMax yearSelect, schoolSelect;
    private Label header = new Label();
    private Label infoLabel, schoolLabel, yearLabel;
    private SystemSettings sysSettings = new SystemSettings();
    School scl = new School();
    int st = 0;

    public AuthenticatedScreen(MyVaadinUI myUi) {

        super();
        this.myUI = myUi;

        verticalPanel = new VerticalSplitPanel();
        verticalPanel.setSplitPosition(97, Sizeable.Unit.PIXELS);
        verticalPanel.setSizeFull();
        verticalPanel.setLocked(true);

        setSizeFull();
        setSpacing(true);
        try {
            myUI.workingDetails(currentUser);
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        if (currentUser.isPermitted(sysSettings.cnHomePageView + ":" + sysSettings.prmMenu)) {
            verticalPanel.setSecondComponent(new HomePageView(myUI));
        }

        header.setSizeUndefined();
        header.setStyleName("mylabel");
        header.setImmediate(true);
        header.setValue((myUI.getMessage(SptMessages.Welcome)).toUpperCase());

        changePassButton = new Button(myUI.getMessage(SptMessages.ChangePasswordButton));
        changePassButton.setStyleName(ValoTheme.BUTTON_LINK);
        changePassButton.setIcon(FontAwesome.KEY);
        changePassButton.addClickListener(this);

        Button logout = new Button(myUi.getMessage(SptMessages.LogoutButton));
        logout.addClickListener(new MyVaadinUI.LogoutListener(this.myUI));
        logout.setStyleName(ValoTheme.BUTTON_PRIMARY);
        logout.addStyleName(ValoTheme.BUTTON_SMALL);
        logout.setIcon(FontAwesome.SIGN_OUT);

        upperLay.setSizeFull();
        upperLay.setSpacing(false);

        upperLay.addComponent(buildInfoLay(), 0, 0, 2, 0);
        upperLay.addComponent(changePassButton, 0, 1);
        upperLay.addComponent(header, 1, 1);
        upperLay.setComponentAlignment(header, Alignment.MIDDLE_CENTER);
        upperLay.addComponent(logout, 2, 1);
        upperLay.setComponentAlignment(logout, Alignment.MIDDLE_RIGHT);
        upperLay.addComponent(buildMenu(), 0, 2, 2, 2);
        upperLay.setColumnExpandRatio(0, 2.5f);
        upperLay.setColumnExpandRatio(1, 7.5f);
        upperLay.setColumnExpandRatio(2, 1);
        upperLay.setRowExpandRatio(2, 1);

        Label warning;
        if (myUi.getUser().getWorking_status_id() == 1) {
            warning = new Label(myUI.getMessage(SptMessages.SystemClosedNotif));
            warning.setSizeUndefined();
            warning.setStyleName("mylabel");
            changePassButton.setEnabled(false);
            upperLay.removeComponent(header);
            upperLay.addComponent(warning, 1, 1);
        }

        verticalPanel.setFirstComponent(upperLay);
        this.addComponent(verticalPanel);
    }

    private HorizontalLayout buildInfoLay() {

        infoLabel = new Label();
        infoLabel.setSizeUndefined();
        infoLabel.setContentMode(ContentMode.HTML);
        infoLabel.setStyleName("labelInfo");
        updateInfo();

        schoolLabel = new Label();
        schoolLabel.setSizeUndefined();
        schoolLabel.setContentMode(ContentMode.HTML);
        schoolLabel.setStyleName("labelInfo");
        schoolLabel.setValue("<i class=\"fa fa-university fa-inverse\"></i><b> "
                + myUI.getMessage(SptMessages.School) + ": </b>");

        schoolSelect = new ComboBoxMax();
        schoolSelect.setImmediate(true);
        if (currentUser.isPermitted(sysSettings.prmChangeSchool + ":" + sysSettings.actModify)) {
            schoolSelect.setEnabled(true);
        } else {
            schoolSelect.setEnabled(false);
        }
        schoolSelect.setNullSelectionAllowed(false);
        schoolSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        schoolSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        schoolSelect.setContainerDataSource(myUI.getSchoolCont());
        schoolSelect.setValue(myUI.getUser().getSchool_id());
        schoolSelect.setFilteringMode(FilteringMode.CONTAINS);
        schoolSelect.addValueChangeListener(this);

        HorizontalLayout schHl = new HorizontalLayout();
        schHl.addComponent(schoolLabel);
        schHl.addComponent(schoolSelect);

        yearLabel = new Label();
        yearLabel.setSizeUndefined();
        yearLabel.setContentMode(ContentMode.HTML);
        yearLabel.setStyleName("labelInfo");
        yearLabel.setValue("<i class=\"fa fa-calendar fa-inverse\"></i><b> "
                + myUI.getMessage(SptMessages.Year) + ": </b>");

        yearSelect = new ComboBoxMax();
        if (currentUser.isPermitted(sysSettings.prmChangeYear + ":" + sysSettings.actModify)) {
            yearSelect.setEnabled(true);
        } else {
            yearSelect.setEnabled(false);
        }
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        setYearSel(myUI.getUser().getCurrent_year().getId());

        HorizontalLayout yearHl = new HorizontalLayout();
        yearHl.addComponent(yearLabel);
        yearHl.addComponent(yearSelect);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setMargin(new MarginInfo(false, false, false, true));
        hl.setStyleName("loginLayout");
        hl.addComponent(infoLabel);
        hl.addComponent(yearHl);
        hl.addComponent(schHl);
        return hl;
    }

    public void updateInfo() {
        infoLabel.setValue("<i class=\"fa fa-user fa-inverse\"></i><b> "
                + myUI.getMessage(SptMessages.LogInAsLabel) + ": </b>"
                + myUI.getUser().getFullname());
    }

    private MenuBar buildMenu() {

        final MenuBar menubar = new MenuBar();
        menubar.setSizeFull();
        menubar.setHeight("35px");
        // menubar.setStyleName("mymenu");

        MenuBar.MenuItem mi;

        if (currentUser.isPermitted(sysSettings.cnHomePageView + ":" + sysSettings.prmMenu)) {
            menubar.addItem(myUI.getMessage(SptMessages.HomePage), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnEmployeeDefinitionView + ":" + sysSettings.prmMenu)) {
            menubar.addItem(myUI.getMessage(SptMessages.EmployeeDefinition), menuCommand);
        } else {
            menubar.addItem(myUI.getMessage(SptMessages.MyInfo), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnStudentDefinitionView + ":" + sysSettings.prmMenu)) {
            menubar.addItem(myUI.getMessage(SptMessages.StudentDefiniton), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnTransactionsView + ":" + sysSettings.prmMenu)) {
            menubar.addItem(myUI.getMessage(SptMessages.Transactions), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnAccrualsView + ":" + sysSettings.prmMenu)) {
            menubar.addItem(myUI.getMessage(SptMessages.Accruals), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnPayoutsView + ":" + sysSettings.prmMenu)) {
            menubar.addItem(myUI.getMessage(SptMessages.Payouts), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmMenu)) {
            menubar.addItem(myUI.getMessage(SptMessages.Reports), menuCommand);
        }

        mi = menubar.addItem(myUI.getMessage(SptMessages.Definitions), null);
        if (currentUser.isPermitted(sysSettings.cnDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.YearDefinition), menuCommand);
            mi.addItem(myUI.getMessage(SptMessages.ClassNumberDefinition), menuCommand);

        }
        if (currentUser.isPermitted(sysSettings.cnClassNameDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.ClassNameDefinition), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnDiscountDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.DiscountDefinition), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnContractDefintionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.ContractDefinition), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnSchoolDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.SchoolDefinition), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnAccessoriesDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.AccessoriesDefinition), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnLeavingReasonsDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.LeavingReasonsDefinition), menuCommand);
        }

        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }

        mi = menubar.addItem(myUI.getMessage(SptMessages.OtherFunctions), null);

        if (currentUser.isPermitted(sysSettings.cnSchoolModificationView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.SchoolModification), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnIssueOrderView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.IssueStudentOrder), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnImportFromExcelView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.ImportStudentsFromExcel), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnMessagesView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.MessagesDefinition), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnBackupView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.Backup), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnCallsView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.Calls), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnSettingsView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.Settings), menuCommand);
        }
        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }

        mi = menubar.addItem(myUI.getMessage(SptMessages.Accounting), null);
        if (currentUser.isPermitted(sysSettings.cnIncomesDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.IncomesDefinition), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnExpensesDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.ExpensesDefinition), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnReturnableAssetsDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.ReturnableAssetsDefinition), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnShortTermDebtsDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.ShortTermDebtsDefinition), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnShortTermDebtsView + ":" + sysSettings.prmMenu)
                || currentUser.isPermitted(sysSettings.cnReturnableAssetsView + ":" + sysSettings.prmMenu)) {
            if (mi.getChildren() != null && !mi.getChildren().isEmpty()) {
                mi.addSeparator();
            }
            if (currentUser.isPermitted(sysSettings.cnReturnableAssetsView + ":" + sysSettings.prmMenu)) {
                mi.addItem(myUI.getMessage(SptMessages.ReturnableAssets), menuCommand);
            }
            if (currentUser.isPermitted(sysSettings.cnShortTermDebtsView + ":" + sysSettings.prmMenu)) {
                mi.addItem(myUI.getMessage(SptMessages.ShortTermDebts), menuCommand);
            }
        }
        if (currentUser.isPermitted(sysSettings.cnAccountingReportsView + ":" + sysSettings.prmMenu)) {
            if (mi.getChildren() != null && !mi.getChildren().isEmpty()) {
                mi.addSeparator();
            }
            mi.addItem(myUI.getMessage(SptMessages.AccountingReports), menuCommand);
        }
        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }

        mi = menubar.addItem(myUI.getMessage(SptMessages.Stock), null);
        if (currentUser.isPermitted(sysSettings.cnStockDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.StocksDefinition), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnStockIncomeView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.StockIncome), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnStockOutcomeView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.StockOutcome), menuCommand);
        }
        if (currentUser.isPermitted(sysSettings.cnStockReportsView + ":" + sysSettings.prmMenu)) {
            if (mi.getChildren() != null && !mi.getChildren().isEmpty()) {
                mi.addSeparator();
            }
            mi.addItem(myUI.getMessage(SptMessages.StockReports), menuCommand);
        }
        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }

        mi = menubar.addItem(myUI.getMessage(SptMessages.HR), null);
        if (currentUser.isPermitted(sysSettings.cnHRDefinitionView + ":" + sysSettings.prmMenu)) {
            mi.addItem(myUI.getMessage(SptMessages.PositionDefinition), menuCommand);
            mi.addItem(myUI.getMessage(SptMessages.BranchDefinition), menuCommand);
            mi.addItem(myUI.getMessage(SptMessages.LanguageDefinition), menuCommand);
            mi.addItem(myUI.getMessage(SptMessages.ExamDefinition), menuCommand);
            mi.addItem(myUI.getMessage(SptMessages.UniversityDefinition), menuCommand);
            mi.addItem(myUI.getMessage(SptMessages.WorkPlacesDefinition), menuCommand);
            mi.addItem(myUI.getMessage(SptMessages.QuestionDefinition), menuCommand);
        }
        if (mi.getChildren() != null && !mi.getChildren().isEmpty() && (currentUser.isPermitted(sysSettings.cnEmployeeTransferView + ":" + sysSettings.prmMenu)
                || currentUser.isPermitted(sysSettings.cnHRReportsView + ":" + sysSettings.prmMenu))) {
            mi.addSeparator();
            if (currentUser.isPermitted(sysSettings.cnEmployeeTransferView + ":" + sysSettings.prmMenu)) {
                mi.addItem(myUI.getMessage(SptMessages.EmployeeTransfer), menuCommand);
            }
            if (currentUser.isPermitted(sysSettings.cnLessonAssessmentView + ":" + sysSettings.prmMenu)) {
                mi.addItem(myUI.getMessage(SptMessages.LessonAssessment), menuCommand);
            }
            if (currentUser.isPermitted(sysSettings.cnHRReportsView + ":" + sysSettings.prmMenu)) {
                mi.addItem(myUI.getMessage(SptMessages.HRReports), menuCommand);
            }
        }
        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }
        return menubar;

    }

    private Command menuCommand = new Command() {

        @Override
        public void menuSelected(MenuItem selectedItem) {
            if (selectedItem != null) {
                String eventPressed = selectedItem.getText();
                if (eventPressed.equals(myUI.getMessage(SptMessages.ClassNumberDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, sysSettings.classTable, false, sysSettings.cnDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.YearDefinition))) {
                    verticalPanel.setSecondComponent(new YearDefinitionView(myUI, AuthenticatedScreen.this));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.LanguageDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, sysSettings.dbLanguageTable, false, sysSettings.cnHRDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.ExamDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, sysSettings.dbExamTable, false, sysSettings.cnHRDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.UniversityDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, sysSettings.dbUniversityTable, false, sysSettings.cnHRDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.WorkPlacesDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, sysSettings.dbWork_placeTable, false, sysSettings.cnHRDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.QuestionDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, sysSettings.dbQuestion, true, sysSettings.cnHRDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.EmployeeTransfer))) {
                    verticalPanel.setSecondComponent(new EmployeeTransferView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.LessonAssessment))) {
                    verticalPanel.setSecondComponent(new LessonAssessmentView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.BranchDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, sysSettings.dbBranchTable, true, sysSettings.cnHRDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.ClassNameDefinition))) {
                    verticalPanel.setSecondComponent(new ClassNameDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.PositionDefinition))) {
                    verticalPanel.setSecondComponent(new PositionDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.DiscountDefinition))) {
                    verticalPanel.setSecondComponent(new DiscountDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.AccessoriesDefinition))) {
                    verticalPanel.setSecondComponent(new AccessoriesDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.LeavingReasonsDefinition))) {
                    verticalPanel.setSecondComponent(new LeavingReasonsDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.ContractDefinition))) {
                    verticalPanel.setSecondComponent(new ContractDefintionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.SchoolDefinition))) {
                    verticalPanel.setSecondComponent(new SchoolDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.EmployeeDefinition))
                        || eventPressed.equals(myUI.getMessage(SptMessages.MyInfo))) {
                    verticalPanel.setSecondComponent(new EmployeeDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.SchoolModification))) {
                    verticalPanel.setSecondComponent(new SchoolModificationView(
                            myUI, myUI.getUser().getSchool_id()));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.StudentDefiniton))) {
                    verticalPanel.setSecondComponent(new StudentDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.IssueStudentOrder))) {
                    verticalPanel.setSecondComponent(new IssueOrderView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.ImportStudentsFromExcel))) {
                    verticalPanel.setSecondComponent(new ImportFromExcelView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.MessagesDefinition))) {
                    verticalPanel.setSecondComponent(new MessagesView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.Reports))) {
                    verticalPanel.setSecondComponent(new ReportsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.AccountingReports))) {
                    verticalPanel.setSecondComponent(new AccountingReportsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.StockReports))) {
                    verticalPanel.setSecondComponent(new StockReportsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.HRReports))) {
                    verticalPanel.setSecondComponent(new HRReportsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.Backup))) {
                    verticalPanel.setSecondComponent(new BackupView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.Calls))) {
                    verticalPanel.setSecondComponent(new CallsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.HomePage))) {
                    verticalPanel.setSecondComponent(new HomePageView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.Transactions))) {
                    verticalPanel.setSecondComponent(new TransactionsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.Accruals))) {
                    verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(SptMessages.Accruals),
                            sysSettings.cnAccrualsView, 2, 1));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.ShortTermDebts))) {
                    verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(SptMessages.ShortTermDebts),
                            sysSettings.cnShortTermDebtsView, 4, 4));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.ReturnableAssets))) {
                    verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(SptMessages.ReturnableAssets),
                            sysSettings.cnReturnableAssetsView, 3, 3));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.Payouts))) {
                    verticalPanel.setSecondComponent(new PayoutsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.StocksDefinition))) {
                    verticalPanel.setSecondComponent(new StockDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.StockIncome))) {
                    verticalPanel.setSecondComponent(new StockIncomeView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.StockOutcome))) {
                    verticalPanel.setSecondComponent(new StockOutcomeView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.Settings))) {
                    verticalPanel.setSecondComponent(new SettingsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.IncomesDefinition))) {
                    verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI, 1));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.ExpensesDefinition))) {
                    verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI, 2));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.ReturnableAssetsDefinition))) {
                    verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI, 3));
                } else if (eventPressed.equals(myUI.getMessage(SptMessages.ShortTermDebtsDefinition))) {
                    verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI, 4));
                }

                header.setValue(eventPressed.toUpperCase());
            }
        }
    };

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == changePassButton) {
            this.verticalPanel.setSecondComponent(new ChangeUserData(myUI));
            header.setValue(myUI.getMessage(SptMessages.ChangeUserDataHeader)
                    .toUpperCase());
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == yearSelect) {
            if (yearSelect.getValue() != null) {
                ConfirmDialog.show(myUI, myUI.getMessage(SptMessages.Question),
                        myUI.getMessage(SptMessages.ConfirmChangeYear),
                        myUI.getMessage(SptMessages.Yes),
                        myUI.getMessage(SptMessages.No),
                        new ConfirmDialog.Listener() {
                            @Override
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    int currentDbYear = 0;
                                    try {
                                        scl.setYear_id((Integer) yearSelect.getValue());
                                        scl.setId(myUI.getUser().getSchool_id());
                                        DbSchool dbd = new DbSchool();
                                        dbd.connect();
                                        currentDbYear = dbd.execGetCurrentDbSchoolYear(myUI.getUser().getSchool_id());
                                        if (currentDbYear != (Integer) yearSelect.getValue()) {
                                            st = dbd.execUpdateYear(scl);
                                        } else {
                                            st = 1;
                                        }
                                        dbd.close();
                                    } catch (Exception e) {
                                        logger.error(e);
                                        logger.catching(e);
                                    }
                                    if (st != 0) {
                                        myUI.getUser().getCurrent_year().setId(scl.getYear_id());
                                        myUI.getUser().getCurrent_year().setName(yearSelect
                                                .getItemCaption(yearSelect.getValue()));
                                        Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                                Notification.Type.HUMANIZED_MESSAGE);
                                        if (currentDbYear != (Integer) yearSelect.getValue()) {
                                            insertPre_regOrders((Integer) yearSelect.getValue(), (Integer) schoolSelect.getValue(),
                                                    myUI.getUser().getId());
                                        }
                                        changeYear((Integer) yearSelect.getValue(), (Integer) schoolSelect.getValue());
                                        yearSelect.removeValueChangeListener(AuthenticatedScreen.this);
                                        setYearSel(myUI.getUser().getCurrent_year().getId());
                                        updatePage();
                                    } else if (st == 0) {
                                        Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                                Notification.Type.WARNING_MESSAGE);
                                    }
                                } else {
                                    yearSelect.removeValueChangeListener(AuthenticatedScreen.this);
                                    yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
                                    yearSelect.addValueChangeListener(AuthenticatedScreen.this);
                                }
                            }
                        });
            }
        } else if (property == schoolSelect) {
            if (schoolSelect.getValue() != null) {
                int curSchoolId = myUI.getUser().getSchool_id();
                String curSchoolName = myUI.getUser().getSchool_name();
                if (curSchoolId != (Integer) schoolSelect.getValue()
                        && !curSchoolName.equals(schoolSelect.getItemCaption(schoolSelect.getValue()))) {
                    myUI.getUser().setSchool_id((Integer) schoolSelect.getValue());
                    myUI.getUser().setSchool_name(schoolSelect.getItemCaption(schoolSelect.getValue()));
                    myUI.getUser().setSchool_code(schoolSelect.getContainerProperty(schoolSelect.getValue(),
                            myUI.getMessage(SptMessages.Code)).getValue().toString());
                    if (schoolSelect.getContainerProperty(schoolSelect.getValue(),
                            myUI.getMessage(SptMessages.Logo)).getValue() != null) {
                        myUI.getUser().setSchool_logo(schoolSelect.getContainerProperty(schoolSelect.getValue(),
                                myUI.getMessage(SptMessages.Logo)).getValue().toString());
                    }
                    yearSelect.removeValueChangeListener(this);
                    setYearSel((Integer) schoolSelect.getContainerProperty(schoolSelect.getValue(),
                            sysSettings.year_id).getValue());
                    myUI.getUser().getCurrent_year().setId((Integer) yearSelect.getValue());
                    myUI.getUser().getCurrent_year().setName(yearSelect.getContainerDataSource()
                            .getContainerProperty(yearSelect.getValue(), myUI.getMessage(SptMessages.Name))
                            .getValue().toString());
                    updatePage();
                } else {
                    Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                            Notification.Type.WARNING_MESSAGE);
                }
            }
        }
    }

    private void updatePage() {
        if (header.getValue() != null) {
            if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.ClassNumberDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new DefinitionView(
                        myUI, sysSettings.classTable, false, sysSettings.cnDefinitionView));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.YearDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new YearDefinitionView(myUI, AuthenticatedScreen.this));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.BranchDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new DefinitionView(
                        myUI, sysSettings.dbBranchTable, true, sysSettings.cnHRDefinitionView));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.ClassNameDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new ClassNameDefinitionView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.DiscountDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new DiscountDefinitionView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.ContractDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new ContractDefintionView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.AccessoriesDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new AccessoriesDefinitionView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(SptMessages.LeavingReasonsDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new LeavingReasonsDefinitionView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.SchoolDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new SchoolDefinitionView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.EmployeeDefinition)).toUpperCase())
                    || header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.MyInfo)).toUpperCase())) {
                verticalPanel.setSecondComponent(new EmployeeDefinitionView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.SchoolModification)).toUpperCase())) {
                verticalPanel.setSecondComponent(new SchoolModificationView(
                        myUI, myUI.getUser().getSchool_id()));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.StudentDefiniton)).toUpperCase())) {
                verticalPanel.setSecondComponent(new StudentDefinitionView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.Reports)).toUpperCase())) {
                verticalPanel.setSecondComponent(new ReportsView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.AccountingReports)).toUpperCase())) {
                verticalPanel.setSecondComponent(new AccountingReportsView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.StockReports)).toUpperCase())) {
                verticalPanel.setSecondComponent(new StockReportsView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.LessonAssessment)).toUpperCase())) {
                verticalPanel.setSecondComponent(new LessonAssessmentView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.HRReports)).toUpperCase())) {
                verticalPanel.setSecondComponent(new HRReportsView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.MessagesDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new MessagesView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.ImportStudentsFromExcel)).toUpperCase())) {
                verticalPanel.setSecondComponent(new ImportFromExcelView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(SptMessages.IssueStudentOrder)).toUpperCase())) {
                verticalPanel.setSecondComponent(new IssueOrderView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.Backup)).toUpperCase())) {
                verticalPanel.setSecondComponent(new BackupView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.Calls)).toUpperCase())) {
                verticalPanel.setSecondComponent(new CallsView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.HomePage)).toUpperCase())) {
                verticalPanel.setSecondComponent(new HomePageView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.Welcome)).toUpperCase())) {
                verticalPanel.setSecondComponent(new HomePageView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.Transactions)).toUpperCase())) {
                verticalPanel.setSecondComponent(new TransactionsView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(SptMessages.Accruals)).toUpperCase())) {
                verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(SptMessages.Accruals),
                        sysSettings.cnAccrualsView, 2, 1));
            } else if (header.getValue().equals(((String) myUI.getMessage(SptMessages.ShortTermDebts)).toUpperCase())) {
                verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(SptMessages.ShortTermDebts),
                        sysSettings.cnShortTermDebtsView, 4, 4));
            } else if (header.getValue().equals(((String) myUI.getMessage(SptMessages.ReturnableAssets)).toUpperCase())) {
                verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(SptMessages.ReturnableAssets),
                        sysSettings.cnReturnableAssetsView, 3, 3));
            } else if (header.getValue().equals(((String) myUI.getMessage(SptMessages.Payouts)).toUpperCase())) {
                verticalPanel.setSecondComponent(new PayoutsView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.IncomesDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI, 1));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.StocksDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new StockDefinitionView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.StockIncome)).toUpperCase())) {
                verticalPanel.setSecondComponent(new StockIncomeView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(
                    SptMessages.StockOutcome)).toUpperCase())) {
                verticalPanel.setSecondComponent(new StockOutcomeView(myUI));
            } else if (header.getValue().equals(((String) myUI.getMessage(SptMessages.Outcomes)).toUpperCase())) {
                verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI, 2));
            } else if (header.getValue().equals(((String) myUI.getMessage(SptMessages.Settings)).toUpperCase())) {
                verticalPanel.setSecondComponent(new SettingsView(myUI));
            }

        }
    }

    private void setYearSel(int year_id) {
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            yearSelect.setContainerDataSource(dbd.exec_years_for_select(myUI, year_id));
            yearSelect.setValue(year_id);
            yearSelect.addValueChangeListener(this);
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void changeYear(int year_id, int school_id) {
        try {
            DbStudent dbst = new DbStudent();
            dbst.connect();
            dbst.exec_ChangeYear(year_id, school_id);
            dbst.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertPre_regOrders(int year_id, int school_id, int emp_id) {
        try {
            DbStudentOrder dbso = new DbStudentOrder();
            dbso.connect();
            dbso.insertPre_regChange_year(year_id, school_id, emp_id);
            dbso.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
