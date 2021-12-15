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
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

public class CallsPdf {

    static final Logger logger = LogManager.getLogger(CallsPdf.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    private Document document = null;
    Date aDate = new Date(System.currentTimeMillis());

    public CallsPdf(final MyVaadinUI myUI, final Table dataTable, final StudentInfoPdf st) {
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

                    Font fontBold = new Font(baseFontBold, 13);
                    Font ordFont = new Font(baseFont, 10);
                    Font ordFontBold = new Font(baseFontBold, 10);
                    Font tableFont = new Font(baseFont, 9);
                    Font tableFontBold = new Font(baseFontBold, 9);

                    document.open();

                    float[] Tdate_colsWidth = {3.5f, 1f};
                    PdfPTable Tdate = new PdfPTable(2);
                    Tdate.setWidthPercentage(90f);
                    Tdate.setWidths(Tdate_colsWidth);
                    Tdate.getDefaultCell().setBorder(0);
                    Tdate.addCell(new Phrase(" ", ordFont));
                    Tdate.addCell(new Phrase("Дата: " + SystemSettings.df.format(aDate), tableFont));
                    document.add(Tdate);

                    Paragraph spr = new Paragraph(myUI.getMessage(SptMessages.ClassList), fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(9, " "));
                    document.add(spr);
                    document.add(new Paragraph(20, " "));

                    //installment plan table
                    float[] Tplan_colsWidth = {0.1f, 0.27f, 0.3f, 0.3f, 0.15f, 0.65f, 0.27f, 0.27f, 0.51f, 0.47f};
                    PdfPTable t = new PdfPTable(10);
                    t.setWidthPercentage(90f);
                    t.setWidths(Tplan_colsWidth);
                    t.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                    t.addCell(new Phrase(" №", ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(SptMessages.Id), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(SptMessages.FirstName), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(SptMessages.LastName), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(SptMessages.ClassName), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(SptMessages.Phone), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(SptMessages.InstPlanDebt), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(SptMessages.PlanDebtDate), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(SptMessages.LastCall), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(SptMessages.LastPayment), ordFontBold));

                    Iterator iter = dataTable.getItemIds().iterator();
                    int i = 0;
                    if (dataTable.size() > 0) {
                        i = 1;
                    }
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        t.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        t.addCell(new Phrase(i + "", tableFont));
                        t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Id)).getValue().toString(), tableFont));
                        t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.FirstName)).getValue().toString(), tableFont));
                        t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.LastName)).getValue().toString(), tableFont));
                        t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.ClassName)).getValue().toString(), tableFont));
                        t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Phone)).getValue().toString(), tableFont));
                        t.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        t.addCell(new Phrase(SystemSettings.dFormat.format(dataTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.InstPlanDebt)).getValue()), tableFont));
                        t.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.PlanDebtDate)).getValue().toString(), tableFont));
                        t.addCell(new Phrase("", tableFont));
                        t.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        if (dataTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.LastPayment)).getValue() != null) {
                            t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.LastPayment)).getValue().toString(), tableFont));
                        } else {
                            t.addCell(new Phrase("", tableFont));
                        }
                        i++;
                    }
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(dataTable.getColumnFooter(
                            myUI.getMessage(SptMessages.InstPlanDebt)), tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));

                    document.add(t);

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

        resource = new StreamResource(source1, "Calls"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "Calls", false);
    }
}
