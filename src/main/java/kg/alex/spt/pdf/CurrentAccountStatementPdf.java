/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Table;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CurrentAccountStatementPdf {

    static final Logger logger = LogManager.getLogger(CurrentAccountStatementPdf.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    private Document document = null;
    Date aDate = new Date(System.currentTimeMillis());
    


    public CurrentAccountStatementPdf(final MyVaadinUI myUI, final Table t,
            final String acc_category, final String currency, final Date from, final Date to, final StudInfoPdf st) {
        source1 = new StreamResource.StreamSource() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/logo/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/logo/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4, 10, 10, 70, 40);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    HeaderFooterPortrait event = new HeaderFooterPortrait(myUI, st.getScl_name_ru(), st.getScl_address(), st.getScl_phone());
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

                    float[] Tdate_colsWidth = {2f, 1f};
                    PdfPTable Tdate = new PdfPTable(2);
                    Tdate.setWidthPercentage(90f);
                    Tdate.setWidths(Tdate_colsWidth);
                    Tdate.getDefaultCell().setBorder(0);
                    Tdate.addCell(new Phrase("Счет: " + acc_category, ordFont));
                    Tdate.addCell(new Phrase("Дата: " + SystemSettings.df.format(aDate), tableFont));
                    Tdate.addCell(new Phrase("Валюта: " + currency, tableFont));
                    Tdate.addCell(new Phrase(" " , tableFont));
                    document.add(Tdate);

                    Paragraph spr = new Paragraph(myUI.getMessage(SptMessages.CurrentAccountStatementReport) + " "
                            + myUI.getMessage(SptMessages.From) + " " + SystemSettings.df.format(from) + " "
                            + myUI.getMessage(SptMessages.To) + " " + SystemSettings.df.format(to), fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(9, " "));
                    document.add(spr);
                    document.add(new Paragraph(20, " "));

                    //installment plan table
                    float[] Tplan_colsWidth = {0.1f, 0.4f, 0.4f, 1.2f, 0.4f, 0.4f, 0.4f};
                    PdfPTable dataTable = new PdfPTable(7);
                    dataTable.setWidthPercentage(90f);
                    dataTable.setWidths(Tplan_colsWidth);
                    dataTable.getDefaultCell().
                            setVerticalAlignment(Element.ALIGN_BOTTOM);
                    dataTable.addCell(new Phrase(" №", ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Date), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Type), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Note), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Accrual), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Payout), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Balance), ordFontBold));

                    Iterator iter = t.getItemIds().iterator();
                    int i = 0;
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        dataTable.addCell(new Phrase((++i) + "", tableFont));
                        dataTable.addCell(new Phrase(t.getContainerProperty(next,
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
                        if (t.getContainerProperty(next, myUI.getMessage(SptMessages.Accrual)).getValue() != null) {
                            dataTable.addCell(new Phrase(SystemSettings.dFormat.format(t.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Accrual)).getValue()), tableFont));
                        } else {
                            dataTable.addCell(new Phrase("", tableFont));
                        }
                        if (t.getContainerProperty(next, myUI.getMessage(SptMessages.Payout)).getValue() != null) {
                            dataTable.addCell(new Phrase(SystemSettings.dFormat.format(t.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Payout)).getValue()), tableFont));
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
                    dataTable.addCell(new Phrase(t.getColumnFooter(myUI.getMessage(SptMessages.Accrual)), tableFontBold));
                    dataTable.addCell(new Phrase(t.getColumnFooter(myUI.getMessage(SptMessages.Payout)), tableFontBold));
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

        resource = new StreamResource(source1, "TokenReport"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "TokenReport", false);
    }
}
