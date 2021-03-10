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
import com.vaadin.ui.TextField;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.StockInvoice;
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StockMovementsPdf {

    static final Logger logger = LogManager.getLogger(StockMovementsPdf.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    private Document document = null;
    


    public StockMovementsPdf(final MyVaadinUI myUI, final String title, final StockInvoice stockInvoice,
            final IndexedContainer stockMovementsCont, final StudInfoPdf schoolInfo, final String total) {

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

                    document = new Document(PageSize.A4, 10, 10, 60, 30);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    HeaderFooterPortrait event = new HeaderFooterPortrait(myUI,
                            schoolInfo.getScl_name_ru(), schoolInfo.getScl_address(), schoolInfo.getScl_phone());
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

                    Paragraph spr = new Paragraph(title, fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(spr);
                    document.add(new Paragraph(30, " "));
                    float[] Thead_colsWidth = {2f, 2f};
                    PdfPTable Thead = new PdfPTable(2);
                    Thead.setWidthPercentage(90f);
                    Thead.setWidths(Thead_colsWidth);

                    Thead.getDefaultCell().setBorder(0);
                    Thead.getDefaultCell().setFixedHeight(15f);
                    Phrase p = new Phrase();
                    p.add(new Phrase(myUI.getMessage(SptMessages.Date) + ": ", ordFontBold));
                    p.add(new Phrase(df.format(stockInvoice.getCreation_date()), ordFont));
                    Thead.addCell(p);
                    p = new Phrase();
                    p.add(new Phrase(myUI.getMessage(SptMessages.InvoiceNumber) + ": ", ordFontBold));
                    p.add(new Phrase(stockInvoice.getInvoiceNumberStr(), ordFont));
                    Thead.addCell(p);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(SptMessages.Stock) + ": ", ordFontBold));
                    p.add(new Phrase(stockInvoice.getStock(), ordFont));
                    Thead.addCell(p);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(SptMessages.ProductCategory) + ": ", ordFontBold));
                    p.add(new Phrase(stockInvoice.getAcc_category(), ordFont));
                    Thead.addCell(p);

                    document.add(Thead);
                    document.add(new Paragraph(20, " "));

                    float[] Tpayments_colsWidth = {0.75f, 6f, 2.3f, 3.3f, 1.7f, 1.8f, 1.2f, 2f};
                    PdfPTable table = new PdfPTable(8);
                    table.setWidthPercentage(90f);
                    table.setWidths(Tpayments_colsWidth);
                    table.getDefaultCell().
                            setVerticalAlignment(Element.ALIGN_BOTTOM);
                    table.addCell(new Phrase(" №", ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(SptMessages.Product), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(SptMessages.Measurement), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(SptMessages.Note), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(SptMessages.QuantityShort), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(SptMessages.Price), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(SptMessages.Rate), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(SptMessages.Amount), ordFontBold));

                    Iterator iter1 = stockMovementsCont.getItemIds().iterator();
                    int y = 0;
                    if (stockMovementsCont.size() > 0) {
                        y = 1;
                    }
                    while (iter1.hasNext()) {
                        Object next = iter1.next();
                        table.addCell(new Phrase(y + "", tableFont));
                        ComboBoxMax cb = (ComboBoxMax) stockMovementsCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Product)).getValue();
                        table.addCell(new Phrase(cb.getItemCaption(cb.getValue()).toString(), tableFont));
                        cb = (ComboBoxMax) stockMovementsCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Measurement)).getValue();
                        table.addCell(new Phrase(cb.getItemCaption(cb.getValue()).toString(), tableFont));
                        TextField tf = (TextField) stockMovementsCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Note)).getValue();
                        table.addCell(new Phrase(tf.getValue(), tableFont));
                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        tf = (TextField) stockMovementsCont.getContainerProperty(next, myUI.getMessage(SptMessages.Quantity)).getValue();
                        table.addCell(new Phrase(SystemSettings.dFormat.format((Double) tf.getPropertyDataSource().getValue()), tableFont));
                        if (title.equals(myUI.getMessage(SptMessages.StockIncome))) {
                            tf = (TextField) stockMovementsCont.getContainerProperty(next, myUI.getMessage(SptMessages.Price)).getValue();
                            table.addCell(new Phrase(SystemSettings.dFormat.format((Double) tf.getPropertyDataSource().getValue()), tableFont));
                        } else {
                            table.addCell(new Phrase(SystemSettings.dFormat.format((Double) stockMovementsCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Price)).getValue()), tableFont));
                        }
                        table.addCell(new Phrase(SystemSettings.dFormat.format((Double) stockMovementsCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Rate)).getValue()), tableFont));
                        table.addCell(new Phrase(SystemSettings.dFormat.format((Double) stockMovementsCont.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Amount)).getValue()), tableFont));
                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        y++;
                    }

                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(SptMessages.Total) + ": " + total, ordFontBold));

                    document.add(table);

                    document.add(new Paragraph(35, " "));
                    float[] T2_colsWidth = {2f, 2f};
                    PdfPTable T2 = new PdfPTable(2);
                    T2.setWidths(T2_colsWidth);
                    T2.setWidthPercentage(90);
                    T2.getDefaultCell().setBorder(0);
                    T2.getDefaultCell().
                            setHorizontalAlignment(Element.ALIGN_LEFT);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(SptMessages.FromEmployee) + ": ", ordFontBold));
                    p.add(new Phrase(stockInvoice.getFrom_employee(), ordFont));
                    T2.addCell(p);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(SptMessages.ToEmployee) + ": ", ordFontBold));
                    p.add(new Phrase(stockInvoice.getTo_employee(), ordFont));
                    T2.addCell(p);
                    T2.addCell(new Phrase(" ", ordFont));
                    T2.addCell(new Phrase(" ", ordFont));
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(SptMessages.Director) + ": ", ordFontBold));
                    p.add(new Phrase(schoolInfo.getScl_dir_f_name(), ordFont));
                    T2.addCell(p);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(SptMessages.Accountant) + ": ", ordFontBold));
                    p.add(new Phrase(schoolInfo.getScl_accountent_fullname(), ordFont));
                    T2.addCell(p);
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
