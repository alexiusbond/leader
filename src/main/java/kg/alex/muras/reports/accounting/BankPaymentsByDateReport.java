package kg.alex.muras.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.muras.MyVaadinUI;
import kg.alex.muras.dao.DbDefinition;
import kg.alex.muras.dao.DbSchool;
import kg.alex.muras.dao.DbStudentPayment;
import kg.alex.muras.domain.StudentInfoPdf;
import kg.alex.muras.i18n.Messages;
import kg.alex.muras.pdf.BankDetailedPaymentsPdf;
import kg.alex.muras.pdf.BankGroupPaymentsPdf;
import kg.alex.muras.tableexport.ExcelExport;
import kg.alex.muras.utils.FormattedTable;
import kg.alex.muras.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.util.Date;

public class BankPaymentsByDateReport extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(BankPaymentsByDateReport.class);
    private final MyVaadinUI myUI;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button generateBtn, excelBtn, pdfBtn;
    private OptionGroup typeOG;
    private ComboBox schoolSelect, currencySelect;
    private DateField fromDateDF, tillDateDF;
    private FormattedTable dataTable;

    public BankPaymentsByDateReport(final MyVaadinUI ui) {
        this.myUI = ui;
        this.setSplitPosition(23, Unit.PERCENTAGE);
        this.setStyleName(ValoTheme.SPLITPANEL_LARGE);
        this.setSizeFull();
        this.setLocked(true);
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 5);
        leftGrid.setWidth("100%");
        leftGrid.setSpacing(true);
        leftGrid.setMargin(new MarginInfo(false, false, false, true));

        generateBtn = new Button(myUI.getMessage(Messages.ShowButton));
        generateBtn.setWidth("100%");
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        pdfBtn = new Button();
        pdfBtn.setDescription(myUI.getMessage(Messages.ExportToPdf));
        pdfBtn.setWidth(Settings.PERCENTS100);
        pdfBtn.setEnabled(false);
        pdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        pdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        pdfBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(Messages.ExportToExcel));
        excelBtn.setWidth("100%");
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        fromDateDF = new DateField(myUI.getMessage(Messages.FromDate));
        fromDateDF.setWidth("100%");
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        fromDateDF.setRequired(true);
        fromDateDF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setValue(new Date());
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(Messages.TillDate));
        tillDateDF.setWidth("100%");
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setRequired(true);
        tillDateDF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setValue(new Date());
        tillDateDF.addValueChangeListener(this);

        typeOG = new OptionGroup();
        typeOG.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        typeOG.setNullSelectionAllowed(false);
        typeOG.setWidth("100%");
        typeOG.addValueChangeListener(this);
        typeOG.addItem(myUI.getMessage(Messages.AggregatedReport));
        typeOG.addItem(myUI.getMessage(Messages.DetailedReport));
        typeOG.setValue(myUI.getMessage(Messages.DetailedReport));

        schoolSelect = new ComboBox(myUI.getMessage(Messages.School));
        schoolSelect.setNullSelectionAllowed(false);
        schoolSelect.setRequired(true);
        schoolSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        schoolSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        schoolSelect.setWidth("100%");
        schoolSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        schoolSelect.setFilteringMode(FilteringMode.CONTAINS);
        schoolSelect.addValueChangeListener(this);
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            schoolSelect.setContainerDataSource(
                    dbs.execSchoolSel(myUI, Settings.MAIN_OFFICE_ID));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (!currentUser.hasRole(Settings.rnBank)) {
            schoolSelect.setValue(myUI.getUser().getSchool().getId());
        }
        schoolSelect.setVisible(currentUser.hasRole(Settings.rnBank));

        currencySelect = new ComboBox(myUI.getMessage(Messages.Currency));
        currencySelect.setNullSelectionAllowed(false);
        currencySelect.setRequired(true);
        currencySelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        currencySelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        currencySelect.setWidth("100%");
        currencySelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        currencySelect.setFilteringMode(FilteringMode.CONTAINS);
        currencySelect.addValueChangeListener(this);
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            currencySelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbAcc_currency, false));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        currencySelect.setValue(1);

        leftGrid.addComponent(typeOG, 0, 0, 3, 0);
        leftGrid.addComponent(fromDateDF, 0, 1, 1, 1);
        leftGrid.addComponent(tillDateDF, 2, 1, 3, 1);
        leftGrid.addComponent(schoolSelect, 0, 2, 3, 2);
        leftGrid.addComponent(currencySelect, 0, 3, 3, 3);
        leftGrid.addComponent(generateBtn, 0, 4, 1, 4);
        leftGrid.addComponent(excelBtn, 2, 4);
        leftGrid.addComponent(pdfBtn, 3, 4);

        this.setFirstComponent(leftGrid);
    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(true);
        vl.setSizeFull();
        dataTable = new FormattedTable(myUI);
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.addStyleName("noWrapHeader");
        vl.addComponent(dataTable);
        this.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (fromDateDF.isValid() && tillDateDF.isValid() && currencySelect.isValid()) {
                try {
                    DbStudentPayment dbCon = new DbStudentPayment();
                    dbCon.connect();
                    if (typeOG.getValue().equals(myUI.getMessage(Messages.DetailedReport))) {
                        dbCon.execSQL_Payments(myUI, (Integer) currencySelect.getValue(), (Integer) schoolSelect.getValue(),
                                fromDateDF.getValue(), tillDateDF.getValue(), dataTable);

                        dataTable.setColumnAlignment(myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.Rate), Table.Align.RIGHT);
                    } else {
                        dbCon.execSQL_Payments_group_by_date(myUI, (Integer) currencySelect.getValue(), (Integer) schoolSelect.getValue(),
                                fromDateDF.getValue(), tillDateDF.getValue(), dataTable);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.TransactionsQuantity), Table.Align.RIGHT);
                    }
                    if (dataTable.getContainerDataSource().size() != 0) {
                        excelBtn.setEnabled(true);
                        pdfBtn.setEnabled(true);
                    }
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    ExcelExport excelReport = new ExcelExport(dataTable);
                    excelReport.setReportTitle("Optima Bank " + typeOG.getValue() + " - [" + myUI.getMessage(Messages.From).toLowerCase() + " "
                            + Settings.df.format(fromDateDF.getValue())
                            + " " + myUI.getMessage(Messages.To).toLowerCase() + " " + Settings.df.format(tillDateDF.getValue()) + "]");
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                    if (typeOG.getValue().equals(myUI.getMessage(Messages.DetailedReport))) {
                        excelReport.getTotalsRow().getCell(1).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(2).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(2).setCellValue(dataTable.getColumnFooter(myUI.getMessage(Messages.Amount)));
                        excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(5).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(5).setCellValue(dataTable.getColumnFooter(myUI.getMessage(Messages.TransactionNumber)));
                    } else {
                        excelReport.getTotalsRow().getCell(1).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(1).setCellValue(dataTable.getColumnFooter(myUI.getMessage(Messages.Amount)));
                        excelReport.getTotalsRow().getCell(3).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(3).setCellValue(dataTable.getColumnFooter(myUI.getMessage(Messages.TransactionsQuantity)));
                    }
                    excelReport.sendConverted();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == pdfBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    StudentInfoPdf studentInfo = new StudentInfoPdf();
                    try {
                        DbSchool dbsc = new DbSchool();
                        dbsc.connect();
                        studentInfo.setSchool(dbsc.execSchool(myUI.getUser().getSchool().getId()));
                        dbsc.close();
                        if (studentInfo.getSchool().getAddress() != null) {
                            if (typeOG.getValue().equals(myUI.getMessage(Messages.DetailedReport))) {
                                new BankDetailedPaymentsPdf(myUI,
                                        (IndexedContainer) dataTable.getContainerDataSource(),
                                        "Optima Bank " + typeOG.getValue() + " - ["
                                                + myUI.getMessage(Messages.From).toLowerCase() + " "
                                                + Settings.df.format(fromDateDF.getValue())
                                                + " " + myUI.getMessage(Messages.To).toLowerCase()
                                                + " " + Settings.df.format(tillDateDF.getValue()) + "]",
                                        studentInfo,
                                        dataTable.getColumnFooter(myUI.getMessage(Messages.Amount)));
                            } else {
                                new BankGroupPaymentsPdf(myUI,
                                        (IndexedContainer) dataTable.getContainerDataSource(),
                                        "Optima Bank " + typeOG.getValue() + " - ["
                                                + myUI.getMessage(Messages.From).toLowerCase() + " "
                                                + Settings.df.format(fromDateDF.getValue())
                                                + " " + myUI.getMessage(Messages.To).toLowerCase()
                                                + " " + Settings.df.format(tillDateDF.getValue()) + "]",
                                        studentInfo,
                                        dataTable.getColumnFooter(myUI.getMessage(Messages.Amount)));
                            }
                        } else {

                            Notification.show(myUI.getMessage(Messages.FillSchoolInfo),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
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
        if (excelBtn.isEnabled()) {
            if (property == tillDateDF || property == fromDateDF || property == typeOG || property == currencySelect || property == schoolSelect) {
                excelBtn.setEnabled(false);
                pdfBtn.setEnabled(false);
                dataTable.setContainerDataSource(null);
            }
        }
    }
}
