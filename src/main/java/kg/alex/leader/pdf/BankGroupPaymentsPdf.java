/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.leader.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import kg.alex.leader.MyVaadinUI;
import kg.alex.leader.domain.StudentInfoPdf;
import kg.alex.leader.i18n.Messages;
import kg.alex.leader.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

public class BankGroupPaymentsPdf {

    static final Logger logger = LogManager.getLogger(BankGroupPaymentsPdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public BankGroupPaymentsPdf(final MyVaadinUI myUI, final IndexedContainer dataCont,
                                final String title, final StudentInfoPdf studentInfo,
                                final String total) {

        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/leader/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/leader/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4, 10, 10, 70, 40);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    HeaderFooterPortrait event = new HeaderFooterPortrait(myUI, studentInfo.getSchool().getName_ru(),
                            studentInfo.getSchool().getAddress(), studentInfo.getSchool().getPhone());
                    writer.setPageEvent(event);

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                    Font fontBold = new Font(baseFontBold, 14);
                    Font ordFontBold = new Font(baseFontBold, 11);
                    Font tableFont = new Font(baseFont, 10);

                    document.open();

                    Paragraph spr = new Paragraph(title, fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(spr);
                    document.add(new Paragraph(20, " "));

                    float[] table_colsWidth = {0.2f, 1f, 1f, 1f, 1f};
                    PdfPTable infoTable = new PdfPTable(5);
                    infoTable.setWidthPercentage(90f);
                    infoTable.setWidths(table_colsWidth);
                    infoTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                    infoTable.addCell(new Phrase(" №", ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.Date), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.Amount), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.Currency), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.TransactionsQuantity), ordFontBold));

                    Iterator<?> iter = dataCont.getItemIds().iterator();
                    int i = 0;
                    if (dataCont.size() > 0) {
                        i = 1;
                    }
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        infoTable.addCell(new Phrase(i + "", tableFont));
                        infoTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(Messages.Date)).getValue().toString(), tableFont));
                        infoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        infoTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                myUI.getMessage(Messages.Amount)).getValue()), tableFont));
                        infoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        infoTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(Messages.Currency)).getValue().toString(), tableFont));
                        infoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        infoTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(Messages.TransactionsQuantity)).getValue() + "", tableFont));
                        i++;
                    }
                    infoTable.addCell(new Phrase("", ordFontBold));
                    infoTable.addCell(new Phrase("", ordFontBold));
                    infoTable.addCell(new Phrase(total, ordFontBold));
                    infoTable.addCell(new Phrase("", ordFontBold));
                    infoTable.addCell(new Phrase(dataCont.size() + "", ordFontBold));
                    document.add(infoTable);

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

        StreamResource resource = new StreamResource(source1, "ClassPayments"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "ClassPayments", false);
    }
}
