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
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
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

public class SummaryReportPdf {

    static final Logger logger = LogManager.getLogger(SummaryReportPdf.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    private Document document = null;
    Date aDate = new Date(System.currentTimeMillis());
    


    public SummaryReportPdf(final MyVaadinUI myUI, final ComponentContainer layout,
                            final StudInfoPdf st) {
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

                    document = new Document(PageSize.A4.rotate(), 10, 10, 70, 40);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    HeaderFooterLandscape event = new HeaderFooterLandscape(myUI, st.getScl_name_ru(), st.getScl_address(), st.getScl_phone());
                    writer.setPageEvent(event);

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                    Font fontBold = new Font(baseFontBold, 14);
                    Font ordFont = new Font(baseFont, 11);
                    Font ordFontBold = new Font(baseFontBold, 11);
                    Font tableFont = new Font(baseFont, 10);
                    Font tableFontBold = new Font(baseFontBold, 10);

                    document.open();

                    float[] Tdate_colsWidth = {3.5f, 1f};
                    PdfPTable Tdate = new PdfPTable(2);
                    Tdate.setWidthPercentage(90f);
                    Tdate.setWidths(Tdate_colsWidth);
                    Tdate.getDefaultCell().setBorder(0);
                    Tdate.addCell(new Phrase(" ", ordFont));
                    Tdate.addCell(new Phrase("Дата: " + SystemSettings.df.format(aDate), tableFont));
                    document.add(Tdate);

                    Paragraph spr = new Paragraph(myUI.getMessage(SptMessages.Yearly)
                            + " " + myUI.getMessage(SptMessages.Report), fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(12, " "));
                    document.add(spr);

                    Iterator<Component> i = layout.iterator();
                    while (i.hasNext()) {
                        document.add(new Paragraph(24, " "));
                        Table dataTable = (Table) i.next();
                        Paragraph p = new Paragraph(dataTable.getCaption(), ordFontBold);
                        p.setIndentationLeft(30);
                        p.setSpacingAfter(7);
                        document.add(p);

                        //installment plan table
                        float[] Tplan_colsWidth = {0.07f, 0.8f, 0.2f, 0.25f, 0.23f, 0.15f, 0.2f, 0.2f, 0.2f, 0.11f};
                        PdfPTable pdfTable = new PdfPTable(10);
                        pdfTable.setWidthPercentage(90f);
                        pdfTable.setWidths(Tplan_colsWidth);
                        pdfTable.getDefaultCell().
                                setVerticalAlignment(Element.ALIGN_BOTTOM);
                        pdfTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        pdfTable.addCell(new Phrase(" №", tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(SptMessages.School), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(SptMessages.Total_Active), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(SptMessages.Contract), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(SptMessages.Discount), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(SptMessages.PreviousYearDebt), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(SptMessages.Net), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(SptMessages.Paid), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(SptMessages.Left), tableFontBold));
                        pdfTable.addCell(new Phrase(SystemSettings.percentage, tableFontBold));

                        Iterator iter = dataTable.getContainerDataSource().getItemIds().iterator();
                        int j = 0;
                        if (dataTable.getContainerDataSource().size() > 0) {
                            j = 1;
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            pdfTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            pdfTable.addCell(new Phrase(j + "", tableFont));
                            pdfTable.addCell(new Phrase(dataTable.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.School)).getValue().toString(), tableFont));
                            pdfTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            pdfTable.addCell(new Phrase(dataTable.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Total_Active)).getValue().toString(), tableFont));
                            pdfTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) dataTable.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Contract)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) dataTable.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Discount)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) dataTable.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.PreviousYearDebt)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) dataTable.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Net)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) dataTable.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Paid)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) dataTable.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Left)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) dataTable.getContainerProperty(next,
                                    SystemSettings.percentage).getValue()), tableFont));
                            j++;
                        }
                        pdfTable.addCell(new Phrase(" ", tableFontBold));
                        pdfTable.addCell(new Phrase(" ", tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(SptMessages.Total_Active)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(SptMessages.Contract)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(SptMessages.Discount)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(SptMessages.PreviousYearDebt)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(SptMessages.Net)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(SptMessages.Paid)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(SptMessages.Left)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                SystemSettings.percentage), tableFontBold));

                        document.add(pdfTable);
                    }

                    document.add(new Paragraph(12, " "));
                    float[] T2_colsWidth = {2f, 2f};
                    PdfPTable T2 = new PdfPTable(2);
                    T2.setWidths(T2_colsWidth);
                    T2.setWidthPercentage(90);
                    T2.getDefaultCell().setBorder(0);
                    T2.getDefaultCell().
                            setHorizontalAlignment(Element.ALIGN_LEFT);
                    T2.addCell(new Phrase(myUI.getMessage(SptMessages.Accountant), ordFontBold));
                    T2.addCell(new Phrase(myUI.getMessage(SptMessages.Director), ordFontBold));
                    T2.addCell(new Phrase(st.getScl_accountent_fullname(), ordFont));
                    T2.addCell(new Phrase(st.getScl_dir_f_name(), ordFont));
                    document.add(T2);

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

        resource = new StreamResource(source1, "SummaryReport"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "SummaryReport", false);
    }
}
