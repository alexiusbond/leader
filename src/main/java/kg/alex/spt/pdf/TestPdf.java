/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.pdf;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.vaadin.server.StreamResource;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.domain.OrderMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;

public class TestPdf {

    static final Logger logger = LogManager.getLogger(TestPdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;

    public TestPdf(final MyVaadinUI myUI ) {

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

                    PdfReader pdfReader = new PdfReader("/home/logo/contract_bachelor.pdf");

                    pdfStamper = new PdfStamper(pdfReader, buffer);

                    // Access the form fields (AcroForm) in the PDF
                    AcroFields form = pdfStamper.getAcroFields();
                    for (String key : form.getFields().keySet()) {
                        System.out.println("Field: " + key);
                    }

                    // Replace placeholders with dynamic data
                    form.setField("full_name", "John Doe");
                    form.setField("department", "Computer Engineering");

                    // Flatten the form to make the fields static text
                    pdfStamper.setFormFlattening(true);

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

        StreamResource resource = new StreamResource(source1, "Test"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "Test", false);
    }
}
