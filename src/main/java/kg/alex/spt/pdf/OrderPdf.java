/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.server.StreamResource;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.domain.OrderMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class OrderPdf {

    static final Logger logger = LogManager.getLogger(OrderPdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;

    public OrderPdf(final MyVaadinUI myUI, final OrderMessage orderMessage) {

        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/logo/TimesNewRomanRegular.ttf";
            private final static String FONT_LOCATION2 = "/home/logo/TimesNewRomanBold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();
                PdfStamper pdfStamper = null;

                try {

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);


                    Font fontBold = new Font(baseFontBold, 14);
                    Font fontRegular = new Font(baseFont, 14);
                    Font dateFont = new Font(baseFont, 10);

                    PdfReader pdfReader =
                            new PdfReader("/home/logo/discount_order.pdf");

                    pdfStamper = new PdfStamper(pdfReader, buffer);

                    PdfContentByte pageContentByte = pdfStamper.getOverContent(1);
                    ColumnText ct = new ColumnText(pageContentByte);
                    ct.setSimpleColumn(50, 680, 550, 50);
                    ct.addElement(new Phrase("№ " + orderMessage.getOrder_number() +
                            "  " + Settings.dateKg.format(orderMessage.getDate()), dateFont));

                    Paragraph spr = new Paragraph(orderMessage.getTitle(), fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    ct.addElement(new Paragraph(45, " "));
                    ct.addElement(spr);

                    Paragraph paragraph = new Paragraph(orderMessage.getContent(), fontRegular);
                    paragraph.setFirstLineIndent(20.0f);
                    paragraph.setAlignment(Element.ALIGN_LEFT);
                    ct.addElement(new Paragraph(45, " "));
                    ct.addElement(paragraph);
                    ct.addElement(new Paragraph(45, " "));

                    /*Image img;
                    if (orderMessage.getDate().after(Settings.df.parse("25-08-2021"))) {
                        img = Image.getInstance(Settings.PATH_TO_UPLOADS + "signature.png");
                    } else {
                        img = Image.getInstance(Settings.PATH_TO_UPLOADS + "signature.jpg");
                    }
                    img.setAlignment(Image.RIGHT);
                    img.scaleAbsolute(250, 110);*/
                    float[] table_date_colsWidth = {1.0f, 1.0f, 1.0f};
                    PdfPTable table_date = new PdfPTable(3);
                    table_date.setWidthPercentage(90f);
                    table_date.setWidths(table_date_colsWidth);
                    table_date.getDefaultCell().setBorder(0);
                    table_date.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table_date.addCell(new Phrase("президент", fontBold));
                    //table_date.addCell(img);
                    table_date.addCell(new Phrase(" "));
                    if (orderMessage.getDate().after(Settings.df.parse("25-08-2021"))) {
                        table_date.addCell(new Phrase("Кудайбердиев Н.Ш.", fontBold));
                    } else {
                        table_date.addCell(new Phrase("Орхан Инанды", fontBold));
                    }
                    ct.addElement(table_date);

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

        StreamResource resource = new StreamResource(source1, "Order"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "Order", false);
    }
}
