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
import kg.alex.spt.reports.AccountingDateReport;
import kg.alex.spt.reports.AccountingMonthReport;
import kg.alex.spt.reports.AccountingGeneralReport;
import kg.alex.spt.reports.AccountingSchoolsReport;
import kg.alex.spt.reports.CurrentAccountStatementReport;
import kg.alex.spt.reports.SalariesReport;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class AccountingReportsView extends HorizontalSplitPanel implements Property.ValueChangeListener {

    private MyVaadinUI myUI;
    private ComboBoxMax repTypeSelect;
    private SystemSettings sysSettings = new SystemSettings();
    private GridLayout leftGrid, rightGrid;
    private Subject currentUser = SecurityUtils.getSubject();
    public HorizontalSplitPanel mainPage;

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
        rightGrid.setWidth("100%");

        repTypeSelect = new ComboBoxMax(myUI.getMessage(SptMessages.ReportType));
        repTypeSelect.setNullSelectionAllowed(false);
        repTypeSelect.setRequired(true);
        repTypeSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        repTypeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        repTypeSelect.setWidth("100%");
        repTypeSelect.setFilteringMode(FilteringMode.CONTAINS);
        repTypeSelect.addValueChangeListener(this);
        if (currentUser.isPermitted(sysSettings.cnAccountingReportsView + ":" + sysSettings.prmMonthReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.MonthReport));
        }
        if (currentUser.isPermitted(sysSettings.cnAccountingReportsView + ":" + sysSettings.prmByDateReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.ByDateReport));
        }
        if (currentUser.isPermitted(sysSettings.cnAccountingReportsView + ":" + sysSettings.prmSchoolAccountingReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.SchoolAccountingReport));
        }
        if (currentUser.isPermitted(sysSettings.cnAccountingReportsView + ":" + sysSettings.prmGeneralReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.GeneralAccountingReport));
        }
        if (currentUser.isPermitted(sysSettings.cnAccountingReportsView + ":" + sysSettings.prmCurrentAccountStatement)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.CurrentAccountStatementReport));
        }
        if (currentUser.isPermitted(sysSettings.cnAccountingReportsView + ":" + sysSettings.prmSalariesReport)) {
            repTypeSelect.addItem(myUI.getMessage(SptMessages.SalariesReport));
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
                new AccountingMonthReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.SchoolAccountingReport))) {
                new AccountingSchoolsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.ByDateReport))) {
                new AccountingDateReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.GeneralAccountingReport))) {
                new AccountingGeneralReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.CurrentAccountStatementReport))) {
                new CurrentAccountStatementReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(SptMessages.SalariesReport))) {
                new SalariesReport(myUI, this);
            }
        }
    }

}
