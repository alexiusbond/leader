package kg.alex.spt.ui;

import com.vaadin.data.Property;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.utils.Settings;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.students.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class StudentReportsView extends HorizontalSplitPanel implements Property.ValueChangeListener {

    private final MyVaadinUI myUI;
    private final Subject currentUser = SecurityUtils.getSubject();
    private ComboBox repTypeSelect;
    private GridLayout leftGrid, rightGrid;

    public StudentReportsView(MyVaadinUI myUI) {
        this.myUI = myUI;
        buildGridLayouts();
        this.setSplitPosition(23, Sizeable.Unit.PERCENTAGE);
        this.setStyleName(ValoTheme.SPLITPANEL_LARGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(leftGrid);
        this.setSecondComponent(rightGrid);
    }

    private void buildGridLayouts() {

        leftGrid = new GridLayout(1, 2);
        leftGrid.setMargin(new MarginInfo(true, false, true, true));
        leftGrid.setSpacing(true);
        leftGrid.setSizeFull();

        rightGrid = new GridLayout(2, 2);
        rightGrid.setMargin(true);
        rightGrid.setSpacing(true);
        rightGrid.setWidth(Settings.PERCENTS100);

        repTypeSelect = new ComboBox(myUI.getMessage(SptMessages.ReportType));
        repTypeSelect.setNullSelectionAllowed(false);
        repTypeSelect.setRequired(true);
        repTypeSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        repTypeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        repTypeSelect.setWidth(Settings.PERCENTS100);
        repTypeSelect.setFilteringMode(FilteringMode.CONTAINS);
        repTypeSelect.addValueChangeListener(this);
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmPlanPayments)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.PlanPayments));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassPayments)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.ClassPayments));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassInstPlan)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.ClassInstallmentPlan));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmDebtReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.DebtReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmSchoolDiscounts)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.SchoolDiscounts));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassDiscounts)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.ClassDiscounts));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmDiscountsReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.DiscountsReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassList)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.ClassList));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmDebtAndRepayment)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.DebtsAndRepaymentsReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmStatusesReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.StatusesReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmYearMonthReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.YearMonthReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmCallsReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.CallsReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmOutOfReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.OutOfReport));
        }
        leftGrid.addComponent(repTypeSelect, 0, 0);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == repTypeSelect) {
            this.setSecondComponent(null);
            leftGrid.removeComponent(0, 1);
            if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.PlanPayments))) {
                new InstallmentPlanPaymentsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.ClassPayments))) {
                new ClassPaymentsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.ClassDiscounts))) {
                new ClassDiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.DiscountsReport))) {
                new DiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.SchoolDiscounts))) {
                new SchoolDiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.ClassInstallmentPlan))) {
                new ClassInstPlanReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.DebtReport))) {
                new DebtReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.ClassList))) {
                new ClassListReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.DebtsAndRepaymentsReport))) {
                new DebtsAndRepaymentsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.StatusesReport))) {
                new StatusesReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.YearMonthReport))) {
                new YearMonthReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.CallsReport))) {
                new CallsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.OutOfReport))) {
                new OutOfList(myUI, this);
            }
        }
    }

}
