/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.*;
import kg.alex.spt.domain.StudentContract;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.domain.StudentPayment;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.pdf.InstallmentPlanPaymentsPdf;
import kg.alex.spt.tableexport.EnhancedFormatExcelExport;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.FormattedTable;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;

import java.io.File;
import java.util.Iterator;

public class InstallmentPlanPaymentsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(InstallmentPlanPaymentsReport.class);
    private MyVaadinUI myUI;
    private Button generateBtn, makePdfBtn, excelBtn;
    private HorizontalSplitPanel splitPanel;
    private GridLayout leftGrid, rightGrid;
    private EnhancedFormatExcelExport excelReport;
    private double debt, amount, plan_debt, ttl_payment, kOplate, ttl_left;
    private String discounts, corrections;
    private StudentInfoPdf st;
    private FormattedTable installmentTable, paymentsTable;
    private FilterTable studentsTable, classTable;
    private ComboBoxMax yearSelect;
    private IndexedContainer installmentCont, paymentsCont;
    private CheckBox paymentsCkb, instPlanCkb;
    private Label debtLab, contractLab, discountLab, correctionLab, planDebt, netLab, paidLab,
            leftLab, loginLab, nameLab, surnameLab, classLab;
    private Embedded photoEmb;

    private String[] NATURAL_COL_ORDER;
    public double total_inst, total_pay;

    public InstallmentPlanPaymentsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.Amount),
                myUI.getMessage(SptMessages.WhoPaid),
                myUI.getMessage(SptMessages.PaymentCategoryType)};
    }

    private void buildLeftPanel() {
        leftGrid = new GridLayout(4, 5);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        yearSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            yearSelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbYear, true));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

        classTable = new FilterTable();
        classTable.setFilterDecorator(new MyFilterDecorator(myUI));
        classTable.setStyleName(ValoTheme.TABLE_SMALL);
        classTable.setCaption(myUI.getMessage(SptMessages.AllClasses));
        classTable.setSizeFull();
        classTable.setNullSelectionAllowed(false);
        classTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        classTable.setFilterBarVisible(true);
        classTable.setFooterVisible(false);
        classTable.setSelectable(true);
        classTable.addValueChangeListener(this);
        try {
            DbClassName dbcn = new DbClassName();
            dbcn.connect();
            classTable.setContainerDataSource(
                    dbcn.execClass_sel(myUI, myUI.getUser().getSchool_id()));
            dbcn.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        studentsTable = new FilterTable();
        studentsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        studentsTable.setStyleName(ValoTheme.TABLE_SMALL);
        studentsTable.setCaption(myUI.getMessage(SptMessages.AllStudents));
        studentsTable.setSizeFull();
        studentsTable.setNullSelectionAllowed(false);
        studentsTable.setMultiSelect(false);
        studentsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        studentsTable.setFilterBarVisible(true);
        studentsTable.setFooterVisible(false);
        studentsTable.setSelectable(true);
        studentsTable.addValueChangeListener(this);

        instPlanCkb = new CheckBox(myUI.getMessage(SptMessages.InstallmentPlan));
        instPlanCkb.setWidth(Settings.PERCENTS100);
        instPlanCkb.setValue(true);
        instPlanCkb.addValueChangeListener(this);

        paymentsCkb = new CheckBox(myUI.getMessage(SptMessages.Payments));
        paymentsCkb.setWidth(Settings.PERCENTS100);
        paymentsCkb.setValue(false);
        paymentsCkb.addValueChangeListener(this);

        generateBtn = new Button(myUI.getMessage(SptMessages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        makePdfBtn = new Button();
        makePdfBtn.setDescription(myUI.getMessage(SptMessages.ExportToPdf));
        makePdfBtn.setWidth(Settings.PERCENTS100);
        makePdfBtn.setEnabled(false);
        makePdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        makePdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        makePdfBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(SptMessages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(classTable, 0, 1, 3, 1);
        leftGrid.addComponent(studentsTable, 0, 2, 3, 2);
        leftGrid.addComponent(instPlanCkb, 0, 3, 1, 3);
        leftGrid.addComponent(paymentsCkb, 2, 3, 3, 3);
        leftGrid.addComponent(generateBtn, 0, 4, 1, 4);
        leftGrid.addComponent(makePdfBtn, 2, 4);
        leftGrid.addComponent(excelBtn, 3, 4);
        leftGrid.setRowExpandRatio(1, 1);
        leftGrid.setRowExpandRatio(2, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (studentsTable.getValue() != null) {
                try {
                    DbStudent dbs = new DbStudent();
                    dbs.connect();
                    st = dbs.execStudInfo_pdf((Integer) studentsTable.getValue(),
                            (Integer) yearSelect.getValue());
                    dbs.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                if (st.getScl_accountent_fullname() != null) {
                    buildRightPanel();
                    st.setCtr_contract_sum(amount);
                    st.setCtr_debt(debt);
                    st.setCtr_instplan_debt(plan_debt - ttl_payment);
                    st.setCtr_discountStr(discounts);
                    st.setCtr_Correction(corrections);
                    st.setCtr_k_oplate(kOplate);
                    st.setCtr_ttl_left_sum(ttl_left);
                    st.setCtr_paid(ttl_payment);
                    st.setYear(yearSelect.getContainerProperty(yearSelect.getValue(),
                            myUI.getMessage(SptMessages.Title)).getValue().toString());
                    makePdfBtn.setEnabled(true);
                    excelBtn.setEnabled(true);
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NoAccountent),
                            Notification.Type.WARNING_MESSAGE);
                }
            }
        } else if (source == makePdfBtn) {
            if (st.getScl_accountent_fullname() != null) {
                if (st.getScl_address() != null && st.getScl_phone() != null
                        && st.getScl_name_ru() != null) {
                    new InstallmentPlanPaymentsPdf(myUI, st, installmentCont, paymentsCont, total_inst,
                            total_pay);
                } else {
                    Notification.show(myUI.getMessage(SptMessages.FillSchoolInfo),
                            Notification.Type.WARNING_MESSAGE);
                }
            } else {
                Notification.show(myUI.getMessage(SptMessages.NoAccountent),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == excelBtn) {
            try {
                if (paymentsCkb.getValue() == true && instPlanCkb.getValue() == false) {
                    if (paymentsTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(paymentsTable, "sheet1");
                        excelReport.setReportTitle(paymentsTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.export();
                    }
                } else if (paymentsCkb.getValue() == false && instPlanCkb.getValue() == true) {
                    if (installmentTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(installmentTable, "sheet1");
                        excelReport.setReportTitle(installmentTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.export();
                    }
                } else if (paymentsCkb.getValue() == true && instPlanCkb.getValue() == true) {
                    if (installmentTable.getContainerDataSource().size() != 0
                            && paymentsTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(paymentsTable, "sheet1");
                        excelReport.setReportTitle(paymentsTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        excelReport.setNextTable(installmentTable, "sheet2");
                        excelReport.setReportTitle(installmentTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.export();
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        makePdfBtn.setEnabled(false);
        if ((property == classTable || property == yearSelect)
                && classTable.getValue() != null && yearSelect.getValue() != null) {
            try {
                DbStudent dbst = new DbStudent();
                dbst.connect();
                studentsTable.setContainerDataSource(
                        dbst.execStud_sel(myUI, (Integer) classTable.getValue(),
                                (Integer) yearSelect.getValue()));
                dbst.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (property == studentsTable || property == instPlanCkb
                || property == paymentsCkb || property == yearSelect) {
            makePdfBtn.setEnabled(false);
            excelBtn.setEnabled(false);
        }
    }

    private void buildRightPanel() {
        rightGrid = new GridLayout(7, 5);
        rightGrid.setSizeFull();
        rightGrid.setMargin(true);
        buildStudInfo();
        buildContractInfo();
        recount();
        installmentCont = null;
        paymentsCont = null;
        if (instPlanCkb.getValue() == true) {
            installmentTable = new FormattedTable();
            installmentTable.setCaption(myUI.getMessage(SptMessages.InstallmentPlan));
            installmentTable.setFooterVisible(true);
            installmentTable.setSizeFull();
            installmentTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
            installmentTable.setStyleName(ValoTheme.TABLE_COMPACT);
            try {
                DbStudentInstallmentPlan dbip = new DbStudentInstallmentPlan();
                dbip.connect();
                total_inst = 0;
                installmentCont = dbip.execSQL_InstPLan(myUI,
                        (Integer) studentsTable.getValue(), (Integer) yearSelect.getValue(), this);
                installmentTable.setContainerDataSource(installmentCont);
                dbip.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            installmentTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
            installmentTable.setColumnFooter(myUI.getMessage(SptMessages.Amount), "Total "
                    + Settings.dFormat.format(total_inst));
            if (paymentsCkb.getValue() == false) {
                rightGrid.addComponent(installmentTable, 0, 4, 6, 4);
            } else {
                rightGrid.addComponent(installmentTable, 0, 4, 2, 4);
            }
            rightGrid.setRowExpandRatio(4, 1);
        }
        if (paymentsCkb.getValue() == true) {
            paymentsTable = new FormattedTable();
            paymentsTable.setCaption(myUI.getMessage(SptMessages.Payments));
            paymentsTable.setFooterVisible(true);
            paymentsTable.setSizeFull();
            paymentsTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
            paymentsTable.setStyleName(ValoTheme.TABLE_COMPACT);
            try {
                DbStudentPayment dbsp = new DbStudentPayment();
                dbsp.connect();
                total_pay = 0;
                paymentsCont = dbsp.execSQL_Payment(myUI,
                        (Integer) studentsTable.getValue(),
                        (Integer) yearSelect.getValue(), this);
                paymentsTable.setContainerDataSource(paymentsCont);
                dbsp.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            paymentsTable.setColumnAlignment(myUI.getMessage(SptMessages.Amount), Table.Align.RIGHT);
            paymentsTable.setColumnFooter(myUI.getMessage(SptMessages.Amount), "Total "
                    + Settings.dFormat.format(total_pay));

            if (instPlanCkb.getValue() == false) {
                rightGrid.addComponent(paymentsTable, 0, 4, 6, 4);
            } else {
                rightGrid.addComponent(paymentsTable, 3, 4, 6, 4);
            }
            paymentsTable.setVisibleColumns(NATURAL_COL_ORDER);
            rightGrid.setRowExpandRatio(4, 1);
        }
        splitPanel.setSecondComponent(rightGrid);
    }

    private void buildContractInfo() {

        contractLab = new Label();
        contractLab.setSizeFull();
        contractLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        contractLab.setValue(myUI.getMessage(SptMessages.Contract) + ":");
        rightGrid.addComponent(contractLab, 3, 0, 4, 0);

        discountLab = new Label();
        discountLab.setSizeFull();
        discountLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        discountLab.setValue(myUI.getMessage(SptMessages.Discount) + ":");
        rightGrid.addComponent(discountLab, 3, 1, 4, 1);

        correctionLab = new Label();
        correctionLab.setSizeFull();
        correctionLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        correctionLab.setValue(myUI.getMessage(SptMessages.Discount) + ":");
        rightGrid.addComponent(correctionLab, 3, 2, 4, 2);

        debtLab = new Label();
        debtLab.setSizeFull();
        debtLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        debtLab.setValue(myUI.getMessage(SptMessages.PreviousYearDebt) + ":");
        rightGrid.addComponent(debtLab, 3, 3, 4, 3);

        netLab = new Label();
        netLab.setSizeFull();
        netLab.setContentMode(ContentMode.HTML);
        netLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        netLab.setValue(myUI.getMessage(SptMessages.Net) + ":");
        rightGrid.addComponent(netLab, 5, 0, 6, 0);

        paidLab = new Label();
        paidLab.setSizeFull();
        paidLab.setContentMode(ContentMode.HTML);
        paidLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        paidLab.setValue(myUI.getMessage(SptMessages.Paid) + ":");
        rightGrid.addComponent(paidLab, 5, 1, 6, 1);

        leftLab = new Label();
        leftLab.setSizeFull();
        leftLab.setContentMode(ContentMode.HTML);
        leftLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        leftLab.setValue(myUI.getMessage(SptMessages.Left) + ":");
        rightGrid.addComponent(leftLab, 5, 2, 6, 2);

        planDebt = new Label();
        planDebt.setSizeFull();
        planDebt.setStyleName(ValoTheme.LABEL_SUCCESS);
        planDebt.setValue(myUI.getMessage(SptMessages.InstPlanDebt) + ":");
        rightGrid.addComponent(planDebt, 5, 3, 6, 3);
    }

    private void buildStudInfo() {
        if (studentsTable.getValue() != null) {
            photoEmb = new Embedded();
            if (st.getStud_photo() != null && !st.getStud_photo().equals("")) {
                photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + st.getStud_photo())));
            } else {
                photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
            }
            photoEmb.setImmediate(true);
            photoEmb.setHeight("110px");
            rightGrid.addComponent(photoEmb, 0, 0, 0, 3);

            loginLab = new Label();
            loginLab.setSizeFull();
            loginLab.setStyleName(ValoTheme.LABEL_SUCCESS);
            loginLab.setValue(myUI.getMessage(SptMessages.Id) + ": " + st.getStud_login());
            rightGrid.addComponent(loginLab, 1, 0, 2, 0);

            nameLab = new Label();
            nameLab.setSizeFull();
            nameLab.setStyleName(ValoTheme.LABEL_SUCCESS);
            nameLab.setValue(myUI.getMessage(SptMessages.FirstName) + ": " + st.getStud_name());
            rightGrid.addComponent(nameLab, 1, 1, 2, 1);

            surnameLab = new Label();
            surnameLab.setSizeFull();
            surnameLab.setStyleName(ValoTheme.LABEL_SUCCESS);
            surnameLab.setValue(myUI.getMessage(SptMessages.LastName) + ": " + st.getStud_sur_name());
            rightGrid.addComponent(surnameLab, 1, 2, 2, 2);

            classLab = new Label();
            classLab.setSizeFull();
            classLab.setStyleName(ValoTheme.LABEL_SUCCESS);
            classLab.setValue(myUI.getMessage(SptMessages.ClassName) + ": " + st.getStud_class_name());
            rightGrid.addComponent(classLab, 1, 3, 2, 3);
        }
    }

    private void recount() {
        StudentContract c = new StudentContract();
        StudentPayment sp = new StudentPayment();
        IndexedContainer discCont = new IndexedContainer();
        try {
            DbStudentContract dbsc = new DbStudentContract();
            DbStudentDiscount dbsd = new DbStudentDiscount();
            DbStudentPayment dbsp = new DbStudentPayment();
            dbsc.connect();
            dbsd.connect();
            dbsp.connect();
            c = dbsc.exec_recount_contract((Integer) studentsTable.getValue(),
                    (Integer) yearSelect.getValue());
            discCont = dbsd.exec_disc_strCont(myUI, (Integer) studentsTable.getValue(),
                    (Integer) yearSelect.getValue());
            sp = dbsp.exec_recount_payment((Integer) studentsTable.getValue(),
                    (Integer) yearSelect.getValue());
            amount = c.getAmount();
            debt = dbsc.exec_get_debt((Integer) studentsTable.getValue(),
                    (Integer) yearSelect.getValue());
            ttl_payment = sp.getTtl_pay();
            plan_debt = c.getPlan_debt() - total_pay;
            kOplate = c.getContr_with_disc() + debt + c.getCorrection();
            ttl_left = (c.getContr_with_disc() + debt) - ttl_payment + c.getCorrection();
            dbsc.close();
            dbsd.close();
            dbsp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        Iterator iter = discCont.getItemIds().iterator();
        discounts = "";
        while (iter.hasNext()) {
            Object next = iter.next();
            if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 1) {
                discounts += Settings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue()).toString() + "%";
                if (iter.hasNext()) {
                    discounts += ", ";
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 2) {
                discounts += Settings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.Amount)).getValue()).toString() + "$";
                if (iter.hasNext()) {
                    discounts += ", ";
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 3) {
                discounts += Settings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.FreeAmount)).getValue()).toString() + "%";
                if (iter.hasNext()) {
                    discounts += ", ";
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 4) {
                discounts += Settings.dFormat.format(discCont.getContainerProperty(next,
                        myUI.getMessage(SptMessages.FreeAmount)).getValue()).toString() + "$";
                if (iter.hasNext()) {
                    discounts += ", ";
                }
            }
        }
        corrections = c.getCorrectionDetails() == null ? "0.00$" : c.getCorrectionDetails();
        contractLab.setValue(myUI.getMessage(SptMessages.Contract) + ": " + Settings.dFormat.format(c.getAmount()) + "$");
        discountLab.setValue(myUI.getMessage(SptMessages.Discount) + ": " + discounts);
        correctionLab.setValue(myUI.getMessage(SptMessages.Correction) + ": " + corrections);
        if (debt > 0) {
            debtLab.setStyleName(ValoTheme.LABEL_FAILURE);
        } else {
            debtLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        }
        debtLab.setValue(myUI.getMessage(SptMessages.PreviousYearDebt) + ": " + Settings.dFormat.format(debt) + "$");
        netLab.setValue(myUI.getMessage(SptMessages.Net) + ": " + Settings.dFormat.format(c.getContr_with_disc() + debt + c.getCorrection()) + "$");
        paidLab.setValue(myUI.getMessage(SptMessages.Paid) + ": " + Settings.dFormat.format(ttl_payment) + "$");
        leftLab.setValue(myUI.getMessage(SptMessages.Left) + ": " + Settings.dFormat.format(
                (c.getContr_with_disc() + debt) - ttl_payment + c.getCorrection()) + "$");
        if ((c.getPlan_debt() - ttl_payment) > 0) {
            planDebt.setStyleName(ValoTheme.LABEL_FAILURE);
            planDebt.setValue(myUI.getMessage(SptMessages.InstPlanDebt) + ": " + Settings.dFormat.format(
                    c.getPlan_debt() - ttl_payment + c.getCorrection()) + "$");
        } else {
            planDebt.setStyleName(ValoTheme.LABEL_SUCCESS);
            planDebt.setValue(myUI.getMessage(SptMessages.InstPlanDebt) + ": " + Settings.dFormat.format(0.0) + "$");
        }
    }
}
