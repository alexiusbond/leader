package kg.alex.spt.ui;

import kg.alex.spt.utils.ComboBoxMax;
import com.vaadin.data.Property;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.CallsReport;
import kg.alex.spt.reports.ClassDiscountsReport;
import kg.alex.spt.reports.ClassInstPlanReport;
import kg.alex.spt.reports.ClassListReport;
import kg.alex.spt.reports.ClassPaymentsReport;
import kg.alex.spt.reports.DebtReport;
import kg.alex.spt.reports.DiscountsReport;
import kg.alex.spt.reports.InstplanPaymentsReport;
import kg.alex.spt.reports.OutOfList;
import kg.alex.spt.reports.SchoolDiscountsReport;
import kg.alex.spt.reports.StatusesReport;
import kg.alex.spt.reports.YearMonthReport;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class ReportsView extends HorizontalSplitPanel implements Property.ValueChangeListener {

    private MyVaadinUI myUI;
    private ComboBoxMax repTypeSelect;
    private SystemSettings sysSettings = new SystemSettings();
    private GridLayout leftGrid, rightGrid;
    private Subject currentUser = SecurityUtils.getSubject();

    public ReportsView(MyVaadinUI myUI) {
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
        rightGrid.setWidth("100%");

        repTypeSelect = new ComboBoxMax(myUI.getMessage(SptMessages.ReportType));
        repTypeSelect.setNullSelectionAllowed(false);
        repTypeSelect.setRequired(true);
        repTypeSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        repTypeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        repTypeSelect.setWidth("100%");
        repTypeSelect.setFilteringMode(FilteringMode.CONTAINS);
        repTypeSelect.addValueChangeListener(this);
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmPlanPayments)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.PlanPayments));
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmClassPayments)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.ClassPayments));
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmClassInstPlan)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.ClassInstallementPlan));
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmDebtReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.DebtReport));
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmSchoolDiscounts)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.SchoolDiscounts));
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmClassDiscounts)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.ClassDiscounts));
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmDiscountsReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.DiscountsReport));
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmClassList)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.ClassList));
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmStatusesReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.StatusesReport));
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmYearMonthReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.YearMonthReport));
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmCallsReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.CallsReport));
        }
        if (currentUser.isPermitted(sysSettings.cnReportsView + ":" + sysSettings.prmOutOfReport)) {
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
                new InstplanPaymentsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.ClassPayments))) {
                new ClassPaymentsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.ClassDiscounts))) {
                new ClassDiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.DiscountsReport))) {
                new DiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.SchoolDiscounts))) {
                new SchoolDiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.ClassInstallementPlan))) {
                new ClassInstPlanReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.DebtReport))) {
                new DebtReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.ClassList))) {
                new ClassListReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.StatusesReport))) {
                new StatusesReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.YearMonthReport))) {
                new YearMonthReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.CallsReport))) {
                new CallsReport(myUI, this);
            } 
            else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.OutOfReport))) {
                new OutOfList(myUI, this);
            }
        }
    }

}
