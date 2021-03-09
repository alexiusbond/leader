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
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
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

public class DebtsPdf {

    static final Logger logger = LogManager.getLogger(DebtsPdf.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    private Document document = null;
    Date aDate = new Date(System.currentTimeMillis());
    private Date fromDate, tillDate;
    SystemSettings sysSettings = new SystemSettings();

    public DebtsPdf(final MyVaadinUI myUI, final IndexedContainer planCont, final String year,
            final Date fDate, final Date tDate, final StudInfoPdf st, final double ttl_plan,
            final double total_paid, final double total_debt) {
        this.fromDate = fDate;
        this.tillDate = tDate;
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

                    Font fontBold = new Font(baseFontBold, 14);
                    Font ordFont = new Font(baseFont, 11);
                    Font ordFontBold = new Font(baseFontBold, 11);
                    Font tableFont = new Font(baseFont, 10);

                    document.open();

                    float[] Tdate_colsWidth = {3.5f, 1f};
                    PdfPTable Tdate = new PdfPTable(2);
                    Tdate.setWidthPercentage(90f);
                    Tdate.setWidths(Tdate_colsWidth);
                    Tdate.getDefaultCell().setBorder(0);
                    Tdate.addCell(new Phrase(" ", ordFont));
                    Tdate.addCell(new Phrase("Дата: " + sysSettings.df.format(aDate), tableFont));
                    document.add(Tdate);

                    Paragraph spr = new Paragraph(myUI.getMessage(SptMessages.ClassInstallementPlan) + ": "
                            + year + " (" + sysSettings.df.format(fromDate) + " - " + sysSettings.df.format(tillDate) + ")", fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(12, " "));
                    document.add(spr);
                    document.add(new Paragraph(24, " "));

                    //installment plan table
                    float[] Tplan_colsWidth = {1f, 4f, 4f, 4f, 4f, 4f, 4f};
                    PdfPTable Tplan = new PdfPTable(7);
                    Tplan.setWidthPercentage(90f);
                    Tplan.setWidths(Tplan_colsWidth);
                    Tplan.getDefaultCell().
                            setVerticalAlignment(Element.ALIGN_BOTTOM);
                    Tplan.addCell(new Phrase(" №", ordFontBold));
                    Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.Firstname), ordFontBold));
                    Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.Surname), ordFontBold));
                    Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.ClassName), ordFontBold));
                    Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.InstallmentPlan), ordFontBold));
                    Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.Paid), ordFontBold));
                    Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.Debt), ordFontBold));

                    Iterator iter = planCont.getItemIds().iterator();
                    int i = 0;
                    if (planCont.size() > 0) {
                        i = 1;
                    }
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        Tplan.addCell(new Phrase(i + "", tableFont));
                        Tplan.addCell(new Phrase(planCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Firstname)).getValue().toString(), tableFont));
                        Tplan.addCell(new Phrase(planCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Surname)).getValue().toString(), tableFont));
                        Tplan.addCell(new Phrase(planCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.ClassName)).getValue().toString(), tableFont));
                        Tplan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        Tplan.addCell(new Phrase(sysSettings.dFormat.format(
                                (Double) planCont.getContainerProperty(next,
                                        myUI.getMessage(SptMessages.InstallmentPlan)).getValue()), tableFont));
                        Tplan.addCell(new Phrase(sysSettings.dFormat.format(
                                (Double) planCont.getContainerProperty(next,
                                        myUI.getMessage(SptMessages.Paid)).getValue()), tableFont));
                        Tplan.addCell(new Phrase(sysSettings.dFormat.format(
                                (Double) planCont.getContainerProperty(next,
                                        myUI.getMessage(SptMessages.Debt)).getValue()), tableFont));
                        Tplan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        i++;
                    }
                    Tplan.addCell(new Phrase(" ", ordFontBold));
                    Tplan.addCell(new Phrase(" ", ordFontBold));
                    Tplan.addCell(new Phrase(" ", ordFontBold));
                    Tplan.addCell(new Phrase(" ", ordFontBold));
                    Tplan.addCell(new Phrase(sysSettings.dFormat.format(ttl_plan), ordFontBold));
                    Tplan.addCell(new Phrase(sysSettings.dFormat.format(total_paid), ordFontBold));
                    Tplan.addCell(new Phrase(sysSettings.dFormat.format(total_debt), ordFontBold));

                    document.add(Tplan);

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

        resource = new StreamResource(source1, "TokenReport"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "TokenReport", false);
    }
}
