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
import kg.alex.spt.Settings;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.accounting.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class AccountingReportsView extends HorizontalSplitPanel implements Property.ValueChangeListener {

    private final MyVaadinUI myUI;
    private ComboBox repTypeSelect;
    private GridLayout leftGrid, rightGrid;
    private final Subject currentUser = SecurityUtils.getSubject();

    public AccountingReportsView(MyVaadinUI myUI) {
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
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmMonthReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.MonthReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmByDateReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.ByDateReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmSchoolAccountingReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.SchoolAccountingReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmGeneralReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.GeneralAccountingReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmCurrentAccountStatement)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.CurrentAccountStatementReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmIncomeExpenseAccountStatement)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.IncomeExpenseAccountStatementReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmSalariesReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.SalariesReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmAccountingBalanceReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.AccountingBalanceReport));
        }
        leftGrid.addComponent(repTypeSelect, 0, 0);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == repTypeSelect) {
            this.setSecondComponent(null);
            leftGrid.removeComponent(0, 1);
            if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.MonthReport))) {
                new MonthReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.SchoolAccountingReport))) {
                new SchoolsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.ByDateReport))) {
                new DateReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.GeneralAccountingReport))) {
                new GeneralReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.CurrentAccountStatementReport))) {
                new CurrentAccountStatementReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.IncomeExpenseAccountStatementReport))) {
                new IncomeExpenseAccountStatementReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.SalariesReport))) {
                new PayoutsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.AccountingBalanceReport))) {
                new BalanceReport(myUI, this);
            }
        }
    }

}
