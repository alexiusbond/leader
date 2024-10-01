/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.pdf;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.vaadin.server.StreamResource;
import kg.alex.spt.MyVaadinUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TestPdf {

    static final Logger logger = LogManager.getLogger(TestPdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;

    public TestPdf(final MyVaadinUI myUI) {

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
// Get the PdfContentByte from the stamper
                    PdfContentByte canvas;
                    Map<String, String> replacements = new HashMap<>();
                    replacements.put("{{full_name}}", "John Doe");
                    replacements.put("{{department}}", "Computer Science");
                    // Iterate over all the pages
                    for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
                        canvas = pdfStamper.getOverContent(i);

                        // Get the text from the page
                        String pageText = PdfTextExtractor.getTextFromPage(pdfReader, i);

                        // Replace placeholders with actual values
                        for (Map.Entry<String, String> entry : replacements.entrySet()) {
                            pageText = pageText.replace(entry.getKey(), entry.getValue());
                        }

                        // Clear existing text and add the new content
                        canvas.beginText();
                        canvas.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED), 12);
                        canvas.showText(pageText);
                        canvas.endText();
                    }
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
