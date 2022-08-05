package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.dao.*;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.tableexport.EnhancedFormatExcelExport;
import kg.alex.spt.utils.FormattedTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StudentFinancialHistoryWindow extends Window implements Button.ClickListener {
    static final Logger logger = LogManager.getLogger(StudentFinancialHistoryWindow.class);
    private final MyVaadinUI myUI;
    private int studentId;
    private FormattedTable dataTable;
    private Button excelBtn;

    public StudentFinancialHistoryWindow(MyVaadinUI myUI, String title, int studentId) {
        this.myUI = myUI;
        this.setWidth("90%");
        this.setHeight("90%");
        this.setModal(true);
        this.setCaption(title);
        this.studentId = studentId;

        VerticalLayout mainLay = new VerticalLayout();
        mainLay.setSizeFull();
        mainLay.setSpacing(true);
        mainLay.setMargin(true);
        this.setContent(mainLay);

        excelBtn = new Button(myUI.getMessage(SptMessages.ExportToExcel));
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);
        mainLay.addComponent(excelBtn);

        dataTable = new FormattedTable();
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        mainLay.addComponent(dataTable);
        mainLay.setExpandRatio(dataTable, 1);

        try {
            DbStudentContract dbCon = new DbStudentContract();
            dbCon.connect();
            dbCon.execFinancialHistory(myUI, studentId, dataTable);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Accrual), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Payment), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Balance), Table.Align.RIGHT);

            if (dataTable.getContainerDataSource().size() != 0) {
                excelBtn.setEnabled(true);
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        if (event.getButton() == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable);
                    excelReport.setExportFileName(myUI.getMessage(SptMessages.FinancialHistory));
                    excelReport.setReportTitle(this.getCaption());
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(3).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(6).setCellValue(dataTable.getColumnFooter(myUI.getMessage(SptMessages.Balance)));
                    excelReport.sendConverted();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }
}
