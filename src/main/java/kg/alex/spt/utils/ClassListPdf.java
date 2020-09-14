/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.utils;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.ClassListReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author eldiyar
 */
public class ClassListPdf {

    static final Logger logger = LogManager.getLogger(ClassListPdf.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    private Document document = null;
    Date aDate = new Date(System.currentTimeMillis());
    SystemSettings sysSettings = new SystemSettings();

    public ClassListPdf(final MyVaadinUI myUI, final IndexedContainer dataCont,
            final StudInfoPdf st, final ClassListReport clr) {
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
                    Tdate.addCell(new Phrase("Дата: " + sysSettings.df.format(aDate), tableFont));
                    document.add(Tdate);

                    Paragraph spr = new Paragraph(myUI.getMessage(SptMessages.ClassList), fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(9, " "));
                    document.add(spr);
                    document.add(new Paragraph(20, " "));

                    //installment plan table
                    float[] Tplan_colsWidth = {
                        0.1f, 0.22f, 0.23f, 0.4f, 0.4f, 0.15f, 0.25f, 0.68f,
                        0.21f, 0.25f, 0.25f, 0.25f, 0.25f,};
                    PdfPTable dataTable = new PdfPTable(13);
                    dataTable.setWidthPercentage(90f);
                    dataTable.setWidths(Tplan_colsWidth);
                    dataTable.getDefaultCell().
                            setVerticalAlignment(Element.ALIGN_BOTTOM);
                    dataTable.addCell(new Phrase(" №", ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Id), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.EducationStatus), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Firstname), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Surname), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.ClassName), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Contract), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.DiscountType), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Discount), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.PreviousYearDebt), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Net), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Paid), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Left), ordFontBold));

                    Iterator iter = dataCont.getItemIds().iterator();
                    int i = 0;
                    if (dataCont.size() > 0) {
                        i = 1;
                    }
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        dataTable.addCell(new Phrase(i + "", tableFont));
                        dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Id)).getValue().toString(), tableFont));
                        dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.EducationStatus)).getValue().toString(), tableFont));
                        dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Firstname)).getValue().toString(), tableFont));
                        dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Surname)).getValue().toString(), tableFont));
                        dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.ClassName)).getValue().toString(), tableFont));
                        dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        if (dataCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Contract)).getValue() != null) {
                            dataTable.addCell(new Phrase(sysSettings.dFormat.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Contract)).getValue()), tableFont));
                            dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            if (dataCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.DiscountType)).getValue() != null) {
                                dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                        myUI.getMessage(SptMessages.DiscountType)).getValue().toString(), tableFont));
                                dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                                dataTable.addCell(new Phrase(sysSettings.dFormat.format(dataCont.getContainerProperty(next,
                                        myUI.getMessage(SptMessages.Discount)).getValue()), tableFont));
                            } else {
                                dataTable.addCell(new Phrase(" ", tableFont));
                                dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                                dataTable.addCell(new Phrase("0.00", tableFont));
                            }
                            dataTable.addCell(new Phrase(sysSettings.dFormat.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.PreviousYearDebt)).getValue()), tableFont));
                            dataTable.addCell(new Phrase(sysSettings.dFormat.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Net)).getValue()), tableFont));
                            dataTable.addCell(new Phrase(sysSettings.dFormat.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Paid)).getValue()), tableFont));
                            dataTable.addCell(new Phrase(sysSettings.dFormat.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Left)).getValue()), tableFont));
                            dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        } else {
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        }
                        i++;
                    }
                    dataTable.addCell(new Phrase(" ", tableFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Students) + ": "
                            + dataCont.size(), tableFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Active)
                            + clr.activeStudents, tableFontBold));
                    dataTable.addCell(new Phrase(" ", tableFontBold));
                    dataTable.addCell(new Phrase(" ", tableFontBold));
                    dataTable.addCell(new Phrase(" ", tableFontBold));
                    dataTable.addCell(new Phrase(sysSettings.dFormat.format(clr.contracts), tableFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(SptMessages.Discounted)
                            + clr.discountedStudents, tableFontBold));
                    dataTable.addCell(new Phrase(sysSettings.dFormat.format(clr.discounts), tableFontBold));
                    dataTable.addCell(new Phrase(sysSettings.dFormat.format(clr.debts), tableFontBold));
                    dataTable.addCell(new Phrase(sysSettings.dFormat.format(clr.nets), tableFontBold));
                    dataTable.addCell(new Phrase(sysSettings.dFormat.format(clr.paids), tableFontBold));
                    dataTable.addCell(new Phrase(sysSettings.dFormat.format(clr.lefts), tableFontBold));

                    document.add(dataTable);

                    document.add(new Paragraph(12, " "));
                    float[] T2_colsWidth = {2f, 2f};
                    PdfPTable T2 = new PdfPTable(2);
                    T2.setWidths(T2_colsWidth);
                    T2.setWidthPercentage(90);
                    T2.getDefaultCell().setBorder(0);
                    T2.getDefaultCell().
                            setHorizontalAlignment(Element.ALIGN_LEFT);
                    T2.addCell(new Phrase(myUI.getMessage(SptMessages.Accountent), ordFontBold));
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
