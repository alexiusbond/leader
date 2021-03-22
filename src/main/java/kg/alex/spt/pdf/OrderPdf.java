/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.reports.ClassListReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

public class OrderPdf {

    static final Logger logger = LogManager.getLogger(OrderPdf.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    Date aDate = new Date(System.currentTimeMillis());


    public OrderPdf(final MyVaadinUI myUI) {
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
                PdfStamper pdfStamper = null;

                try {

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                    Font fontBold = new Font(baseFontBold, 13);
                    Font ordFont = new Font(baseFont, 10);
                    Font ordFontBold = new Font(baseFontBold, 10);
                    Font tableFont = new Font(baseFont, 9);
                    Font tableFontBold = new Font(baseFontBold, 9);

                    PdfReader pdfReader =
                            new PdfReader("/home/logo/discount_order.pdf");

                    pdfStamper = new PdfStamper(pdfReader, buffer);

                    PdfContentByte pageContentByte = pdfStamper.getOverContent(1);
                    ColumnText ct = new ColumnText(pageContentByte);
                    ct.setSimpleColumn(50, 680, 500, 500);
                    ct.addElement(new Phrase("№ 01-31/______“______”________ 2021-ж.", ordFont));

                    Paragraph spr = new Paragraph("ЧЫҢГЫЗ АЙТМАТОВ  АТЫНДАГЫ БИШКЕК ЛИЦЕЙИНИН ДИРЕКТОРУНА", fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    ct.addElement(new Paragraph(45, " "));
                    ct.addElement(spr);

                    spr = new Paragraph("Лицейдин 8-классынын окуучусу Асанов Асанга “Сапаттын” акылуу  билим берүү кызмат көрсөтүүдөгү жеңилдиктер жөнүндөгү Жобосунун 3-пунктунун  негизинде 2020-2021-окуу жылынын окуу төлөмүндө 10% жеңилдик берилсин.", ordFont);
                    spr.setAlignment(Element.ALIGN_LEFT);
                    ct.addElement(new Paragraph(45, " "));
                    ct.addElement(spr);
                    ct.addElement(new Paragraph(45, " "));

                    float[] Tdate_colsWidth = {1.0f, 2.0f, 1.0f};
                    PdfPTable Tdate = new PdfPTable(3);
                    Tdate.setWidthPercentage(90f);
                    Tdate.setWidths(Tdate_colsWidth);
                    Tdate.getDefaultCell().setBorder(0);
                    Tdate.addCell(new Phrase("Президент", ordFont));
                    Tdate.addCell(new Phrase("Президент", ordFont));
                    Tdate.addCell(new Phrase("Орхан Инанды", tableFont));
                    ct.addElement(Tdate);

                    ct.go();

                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                } finally {
                    if (pdfStamper != null) {
                        try {
                            pdfStamper.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                    }
                }
                b = buffer.toByteArray();
                return new ByteArrayInputStream(b);
            }
        };

        resource = new StreamResource(source1, "Order"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "Order", false);
    }
}
