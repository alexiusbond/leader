package kg.alex.spt.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.domain.InvoiceInfoPdf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;

public class InvoicePDF {

    static final Logger logger = LogManager.getLogger(InvoicePDF.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    private Document document = null;
    private final String nameOf = "Invoice";

    public InvoicePDF(final MyVaadinUI myUI, final InvoiceInfoPdf student) {

        source1 = new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {


                    SimpleDateFormat dateRu = new SimpleDateFormat(
                            "«dd» MMMMM yyyy г.", myDateFormatSymbols);

                    document = new Document(PageSize.A4, 10, 10, 10, 10);
                    PdfWriter.getInstance(document, buffer);

                    final String FONT_LOCATION = "/home/logo/PT_Sans-Web-Regular.ttf";
                    final String FONT_LOCATION_BOLD = "/home/logo/PT_Sans-Web-Bold.ttf";

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFont_bold = BaseFont.createFont(FONT_LOCATION_BOLD,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    Font title_font = new Font(baseFont, 14, Font.NORMAL);
                    Font normal_font = new Font(baseFont, 11, Font.NORMAL);
                    Font underlined_font = new Font(baseFont, 11, Font.UNDERLINE);
                    Font bold_font = new Font(baseFont_bold, 11, Font.NORMAL);

                    document.open();
                    float[] mainTableWidth = {2.2f, 1.3f};
                    PdfPTable mainTable = new PdfPTable(2);
                    mainTable.setWidthPercentage(95f);
                    mainTable.setWidths(mainTableWidth);
                    mainTable.getDefaultCell().setPaddingBottom(15);
                    mainTable.getDefaultCell().setBorder(Rectangle.BOTTOM);

                    float[] dateTableWidth = {1f, 1f};
                    PdfPTable dateTable = new PdfPTable(2);
                    dateTable.setWidths(dateTableWidth);
                    dateTable.getDefaultCell().setLeading(20, 0);
                    dateTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                    dateTable.addCell(new Phrase("Номер документа", normal_font));
                    dateTable.addCell(new Phrase("Дата составления", normal_font));
                    dateTable.addCell(new Phrase(" ", normal_font));
                    dateTable.addCell(new Phrase(Settings.dtmf.format(student.getPayment_date()), normal_font));

                    float[] orderTableWidth = {0.6f, 0.4f};
                    PdfPTable orderTable = new PdfPTable(2);
                    orderTable.setWidths(orderTableWidth);
                    orderTable.getDefaultCell().setLeading(20, 0);
                    orderTable.getDefaultCell().setBorder(0);
                    PdfPCell cell = new PdfPCell(new Phrase(student.getSchool_name(), title_font));
                    cell.setColspan(2);
                    cell.setBorder(0);
                    cell.setPaddingBottom(20);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    orderTable.addCell(cell);

                    cell = new PdfPCell(new Phrase("ПРИХОДНЫЙ КАССОВЫЙ ОРДЕР", bold_font));
                    cell.setBorder(0);
                    cell.setPaddingBottom(20);
                    orderTable.addCell(cell);

                    Image logo = Image.getInstance(Settings.PATH_TO_UPLOADS + student.getScl_logo());
                    logo.setAlignment(Image.MIDDLE);
                    logo.scaleAbsolute(70, 70);
                    cell = new PdfPCell(logo);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setBorder(0);
                    cell.setRowspan(2);
                    cell.setPaddingBottom(5);
                    orderTable.addCell(cell);
                    orderTable.addCell(dateTable);

                    Paragraph namePar = new Paragraph();
                    namePar.setLeading(20);
                    namePar.add(new Chunk("Принято от: ", bold_font));
                    if (student.getWhopaid_fullname() != null && !student.getWhopaid_fullname().equals("")) {
                        namePar.add(new Chunk(student.getLogin() + ", " + student.getClass_name() + ", "
                                + student.getStud_fullname() + " (" + student.getWhopaid_fullname() + ")", underlined_font));
                    } else {
                        namePar.add(new Chunk(student.getLogin() + ", " + student.getClass_name() + ", "
                                + student.getStud_fullname(), underlined_font));
                    }
                    cell = new PdfPCell(namePar);
                    cell.setColspan(2);
                    cell.setBorder(0);
                    cell.setLeading(20, 0);
                    orderTable.addCell(cell);

                    Paragraph reasonPar = new Paragraph();
                    reasonPar.add(new Chunk("Основание: ", bold_font));
                    reasonPar.add(new Chunk("оплата за учебу - " + myUI.getUser().getCurrent_year().getName(),
                            underlined_font));
                    cell = new PdfPCell(reasonPar);
                    cell.setColspan(2);
                    cell.setLeading(20, 0);
                    cell.setBorder(0);
                    orderTable.addCell(cell);

                    Paragraph payTypePar = new Paragraph();
                    payTypePar.add(new Chunk("Тип оплаты: ", bold_font));
                    payTypePar.add(new Chunk(student.getPayment_type(), underlined_font));
                    cell = new PdfPCell(payTypePar);
                    cell.setColspan(2);
                    cell.setLeading(20, 0);
                    cell.setBorder(0);
                    orderTable.addCell(cell);

                    Paragraph sumPar = new Paragraph();
                    double kurs = student.getKurs();
                    sumPar.add(new Chunk("Сумма цифрами: ", bold_font));

//                    sumPar.add(new Chunk(student.getAmount() + " USD", underlined_font));
                    sumPar.add(new Chunk(Math.round(student.getAmount() * kurs) + " сом (" +
                            student.getAmount() + " USD)", underlined_font));

                    cell = new PdfPCell(sumPar);
                    cell.setColspan(2);
                    cell.setLeading(20, 0);
                    cell.setBorder(0);
                    orderTable.addCell(cell);

                    WritableSummRu convertToLetters;

//                    convertToLetters = new WritableSummRuUSD();
                    convertToLetters = new WritableSummRuSOM();

                    Paragraph sumLetterPar = new Paragraph();
                    sumLetterPar.add(new Chunk("Сумма прописью: ", bold_font));

//                    sumLetterPar.add(new Chunk(convertToLetters.numberToString(
//                            student.getAmount()), underlined_font));
                    sumLetterPar.add(new Chunk(convertToLetters.numberToString(
                            Math.round(student.getAmount() * kurs)), underlined_font));
                    cell = new PdfPCell(sumLetterPar);
                    cell.setColspan(2);
                    cell.setLeading(20, 0);
                    cell.setBorder(0);
                    orderTable.addCell(cell);

                    Paragraph accPar = new Paragraph();
                    accPar.add(new Chunk("Получил кассир:________________", bold_font));
                    cell = new PdfPCell(accPar);
                    cell.setLeading(20, 0);
                    cell.setBorder(0);
                    orderTable.addCell(cell);
                    orderTable.addCell(new Phrase("Подпись_____________", bold_font));

                    mainTable.addCell(orderTable);

                    float[] invoiceTableWidth = {1f};
                    PdfPTable invoiceTable = new PdfPTable(1);
                    invoiceTable.setWidths(invoiceTableWidth);
                    invoiceTable.getDefaultCell().setLeading(20, 0);
                    invoiceTable.getDefaultCell().setBorder(Rectangle.LEFT);
                    invoiceTable.getDefaultCell().setPaddingLeft(7);
                    cell = new PdfPCell(new Phrase(student.getSchool_name(), normal_font));
                    cell.setBorder(Rectangle.LEFT);
                    cell.setPaddingLeft(7);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invoiceTable.addCell(cell);

                    cell = new PdfPCell(new Phrase("КВИТАНЦИЯ", bold_font));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBorder(Rectangle.LEFT);
                    cell.setPaddingLeft(7);
                    invoiceTable.addCell(cell);

                    cell = new PdfPCell(new Phrase("к приходному кассовому ордеру №__________", bold_font));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBorder(Rectangle.LEFT);
                    cell.setPaddingLeft(7);
                    invoiceTable.addCell(cell);

                    Phrase datePhr = new Phrase("от " + dateRu.format(student.getPayment_date()), normal_font);
                    invoiceTable.addCell(datePhr);
                    Paragraph nameInvPar = new Paragraph();
                    nameInvPar.setLeading(20);
                    nameInvPar.add(new Chunk("Принято от: ", bold_font));
                    if (student.getWhopaid_fullname() != null && !student.getWhopaid_fullname().equals("")) {
                        nameInvPar.add(new Chunk(student.getLogin() + ", " + student.getClass_name() + ", "
                                + student.getStud_fullname() + " (" + student.getWhopaid_fullname() + ")", underlined_font));
                    } else {
                        nameInvPar.add(new Chunk(student.getLogin() + ", " + student.getClass_name() + ", "
                                + student.getStud_fullname(), underlined_font));
                    }
                    cell = new PdfPCell(nameInvPar);
                    cell.setBorder(Rectangle.LEFT);
                    cell.setPaddingLeft(7);
                    invoiceTable.addCell(cell);
                    invoiceTable.addCell(reasonPar);
                    invoiceTable.addCell(payTypePar);
                    invoiceTable.addCell(sumPar);
                    invoiceTable.addCell(sumLetterPar);
                    invoiceTable.addCell(new Phrase(dateRu.format(student.getPayment_date()), normal_font));
                    invoiceTable.addCell(new Phrase("М. П. (штампа)", normal_font));

                    Paragraph accInvPar = new Paragraph();
                    accInvPar.add(new Chunk("Кассир:__________________", bold_font));
                    cell = new PdfPCell(accInvPar);
                    cell.setBorder(Rectangle.LEFT);
                    cell.setPaddingLeft(7);
                    invoiceTable.addCell(cell);

                    mainTable.addCell(invoiceTable);
                    document.add(new Paragraph(30, "  "));
                    document.add(mainTable);

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

        resource = new StreamResource(source1, nameOf
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");
        myUI.getPage().open(resource, nameOf, false);
    }

    private static DateFormatSymbols myDateFormatSymbols = new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return new String[]{"января", "февраля", "марта", "апреля", "мая", "июня",
                "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }
    };

}
