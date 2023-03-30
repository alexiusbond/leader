/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Table;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.domain.School;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

public class IncomeExpenseAccountStatementPdf {

    static final Logger logger = LogManager.getLogger(IncomeExpenseAccountStatementPdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;
    private final Date aDate = new Date(System.currentTimeMillis());


    public IncomeExpenseAccountStatementPdf(final MyVaadinUI myUI, final Table t,
                                            final String acc_category, final String currency, final Date from,
                                            final Date to, final School schoolInfo) {
        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/logo/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/logo/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4, 10, 10, 70, 40);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    HeaderFooterPortrait event = new HeaderFooterPortrait(myUI,
                            schoolInfo.getName_ru(), schoolInfo.getAddress(), schoolInfo.getPhone());
                    writer.setPageEvent(event);

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                    Font fontBold = new Font(baseFontBold, 13);
                    Font ordFont = new Font(baseFont, 10);
                    Font ordFontBold = new Font(baseFontBold, 10);
                    Font tableFont = new Font(baseFont, 9);
                    Font tableFontBold = new Font(baseFontBold, 9);

                    document.open();

                    float[] table_date_colsWidth = {2f, 1f};
                    PdfPTable table_date = new PdfPTable(2);
                    table_date.setWidthPercentage(90f);
                    table_date.setWidths(table_date_colsWidth);
                    table_date.getDefaultCell().setBorder(0);
                    table_date.addCell(new Phrase("Счет: " + acc_category, ordFont));
                    table_date.addCell(new Phrase("Дата: " + Settings.df.format(aDate), tableFont));
                    table_date.addCell(new Phrase("Валюта: " + currency, tableFont));
                    table_date.addCell(new Phrase(" ", tableFont));
                    document.add(table_date);

                    Paragraph spr = new Paragraph(myUI.getMessage(SptMessages.IncomeExpenseAccountStatementReport) + " "
                            + myUI.getMessage(SptMessages.From) + " " + Settings.df.format(from) + " "
                            + myUI.getMessage(SptMessages.To) + " " + Settings.df.format(to), fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(9, " "));
                    document.add(spr);
                    document.add(new Paragraph(20, " "));

                    //installment plan table
                    float[] table_plan_colsWidth = {0.1f, 0.4f, 0.4f, 1.2f, 0.4f, 0.4f, 0.4f};
                    PdfPTable dataTable = new PdfPTable(7);
                    dataTable.setWidthPercentage(90f);
                    dataTable.setWidths(table_plan_colsWidth);
                    dataTable.getDefaultCell().
                            setVerticalAlignment(Element.ALIGN_BOTTOM);
                    dataTable.addCell(new Phrase(" №", ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Date), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Type), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Note), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Income), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Expense), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Balance), ordFontBold));

                    Iterator<?> iter = t.getItemIds().iterator();
                    int i = 0;
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        dataTable.addCell(new Phrase((++i) + "", tableFont));
                        dataTable.addCell(new Phrase(
                                t.getContainerProperty(next, myUI.getMessage(SptMessages.Date)).getValue() == null ? "" :
                                        t.getContainerProperty(next,
                                                myUI.getMessage(SptMessages.Date)).getValue().toString(), tableFont));
                        dataTable.addCell(new Phrase(t.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Type)).getValue().toString(), tableFont));
                        if (t.getContainerProperty(next, myUI.getMessage(SptMessages.Note)).getValue() != null) {
                            dataTable.addCell(new Phrase(t.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Note)).getValue().toString(), tableFont));
                        } else {
                            dataTable.addCell(new Phrase("", tableFont));
                        }
                        dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        if (t.getContainerProperty(next, myUI.getMessage(SptMessages.Income)).getValue() != null) {
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(t.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Income)).getValue()), tableFont));
                        } else {
                            dataTable.addCell(new Phrase("", tableFont));
                        }
                        if (t.getContainerProperty(next, myUI.getMessage(SptMessages.Expense)).getValue() != null) {
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(t.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Expense)).getValue()), tableFont));
                        } else {
                            dataTable.addCell(new Phrase("", tableFont));
                        }
                        dataTable.addCell(new Phrase(t.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Balance)).getValue().toString(), tableFont));
                    }
                    dataTable.addCell(new Phrase(" ", tableFontBold));
                    dataTable.addCell(new Phrase(" ", tableFontBold));
                    dataTable.addCell(new Phrase(" ", tableFontBold));
                    dataTable.addCell(new Phrase(" ", tableFontBold));
                    dataTable.addCell(new Phrase(t.getColumnFooter(myUI.getMessage(SptMessages.Income)), tableFontBold));
                    dataTable.addCell(new Phrase(t.getColumnFooter(myUI.getMessage(SptMessages.Expense)), tableFontBold));
                    dataTable.addCell(new Phrase(t.getColumnFooter(myUI.getMessage(SptMessages.Balance)), tableFontBold));

                    document.add(dataTable);

                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                } finally {
                    if (document != null) {
                        document.close();
                    }
                }
                b = buffer.toByteArray();
                return new ByteArrayInputStream(b);
            }
        };

        StreamResource resource = new StreamResource(source1, "IncomeExpenseAccountStatement"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "IncomeExpenseAccountStatement", false);
    }
}
