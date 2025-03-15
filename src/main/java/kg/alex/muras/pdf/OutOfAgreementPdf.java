package kg.alex.muras.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.server.StreamResource;
import kg.alex.muras.MyVaadinUI;
import kg.alex.muras.dao.DbRelative;
import kg.alex.muras.dao.DbStudentOrder;
import kg.alex.muras.domain.StudentInfoPdf;
import kg.alex.muras.domain.StudentOrder;
import kg.alex.muras.domain.StudentRelative;
import kg.alex.muras.utils.Decliner;
import kg.alex.muras.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class OutOfAgreementPdf {

    static final Logger logger = LogManager.getLogger(OutOfAgreementPdf.class);
    private final static String FONT_LOCATION = "/home/muras/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/muras/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public OutOfAgreementPdf(final MyVaadinUI ui, StudentInfoPdf st_info) {
        this.myUI = ui;
        this.studentInfo = st_info;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 30, 30);
                document.setMargins(10, 10, 30, 30);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 10);
                Font ordBoldFont = new Font(baseFontBold, 10);
                Font boldFont = new Font(baseFontBold, 11);
                Font font_header = new Font(baseFontBold, 11);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("Соглашение о расторжении", font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("Договора об оказании платных образовательных услуг", font_header));
                spr.add(Chunk.NEWLINE);

                spr.setAlignment(Element.ALIGN_CENTER);
                document.add(spr);
                document.add(new Paragraph(10, " "));

                float[] table_date_colsWidth = {4.5f, 1.5f};
                PdfPTable table_date = new PdfPTable(2);
                table_date.setWidthPercentage(90f);
                table_date.setWidths(table_date_colsWidth);
                table_date.getDefaultCell().setBorder(0);
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table_date.addCell(new Phrase("г. " + studentInfo.getSchool().getCity(), ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateRu.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                document.add(table_date);
                document.add(new Paragraph(10, " "));

                Decliner dcl = new Decliner();

                Paragraph paragraph = new Paragraph();
                paragraph.setFirstLineIndent(25);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Учреждение " + studentInfo.getSchool().getName_ru()
                        + " именуемое в дальнейшем «Учреждение», в лице директора ", ordFont));
                String fullName = null;
                try {
                    boolean isFeminine = studentInfo.getDirector().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(studentInfo.getDirector().getSurname(), isFeminine)
                            + " " + dcl.DeclineNameGenitive(studentInfo.getDirector().getName(), isFeminine, false);
                    if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().equals("")) {
                        fullName += " " + dcl.DeclinePatronymicGenitive(studentInfo.getDirector().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(", действующего на основании Устава и ", ordFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getFullName() + ", ", ordBoldFont));
                paragraph.add(new Phrase("именуемый в дальнейшем «", ordFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getRelativeDeclarative(), ordBoldFont));
                paragraph.add(new Phrase("», Обучающегося ", ordFont));
                fullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                try {
                    boolean isFeminine = studentInfo.getStudent().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(studentInfo.getStudent().getSurname(), isFeminine) + " "
                            + dcl.DeclineNameGenitive(studentInfo.getStudent().getName(), isFeminine, false);
                    if (!studentInfo.getStudent().getMiddle_name().equals("")) {
                        fullName = fullName + " "
                                + dcl.DeclinePatronymicGenitive(studentInfo.getStudent().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(" «" + studentInfo.getStudent().getClass_name() + "»", ordBoldFont));
                paragraph.add(new Phrase(" класса, совместно именуемые Стороны, заключили настоящее соглашение к Договору об оказании платных образовательных услуг  от ", ordFont));
                paragraph.add(new Phrase(Settings.dateRu.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                paragraph.add(new Phrase(" (далее – Договор) о нижеследующем:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));
                StudentOrder so = null;
                try {
                    DbStudentOrder studentOrder = new DbStudentOrder();
                    studentOrder.connect();
                    so = studentOrder.execOrder(studentInfo.getStudent().getId(), 1);
                    studentOrder.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                if (so == null) {
                    paragraph.add(new Phrase("1. Стороны пришли к соглашению расторгнуть Договор с «____» _____________ 20_____г. в связи __________________________________________________________________________.", ordFont));
                } else if (so.getReasons() == null || so.getReasons().isEmpty()) {
                    paragraph.add(new Phrase("1. Стороны пришли к соглашению расторгнуть Договор с "
                            + Settings.dateRu.format(so.getModification_date()) + " в связи __________________________________________________________________________.", ordFont));
                } else {
                    paragraph.add(new Phrase("1. Стороны пришли к соглашению расторгнуть Договор с "
                            + Settings.dateRu.format(so.getModification_date()) + " в связи " + so.getReasons() + ".", ordFont));
                }
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                if (studentInfo.getContractInfo().getLeft() > 0) {
                    paragraph.add(new Phrase("2. Стороны подтверждают наличие задолженности на дату расторжения Договора по оплате за обучение за Родителем, в размере "
                            + Settings.dFormat2.format(studentInfo.getContractInfo().getLeft())
                            + " долларов США, которые Родитель обязуется оплатить до «______»_______________________ 20_____г.", ordFont));
                } else {
                    paragraph.add(new Phrase("Стороны подтверждают отсутствие взаимных претензий по обязательствам, вытекающим из Договора, в том числе задолженности по оплате обучения.", ordFont));
                }
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3. Настоящее дополнительное соглашение является неотъемлемой частью Договора, вступает в силу с момента его подписания Сторонами.", ordFont));
                document.add(paragraph);

                document.add(new Paragraph(10, " "));

                //document.newPage();
                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("Адреса и реквизиты Сторон", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                float[] table_info_colsWidth = {1.5f, 1f};
                PdfPTable table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);
                Paragraph text10 = new Paragraph();
                text10.add(new Phrase("Учреждение", boldFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase(studentInfo.getSchool().getName_ru(), boldFont));
                text10.add(new Phrase("Адрес: " + studentInfo.getSchool().getAddress(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("ИНН: " + studentInfo.getSchool().getInn(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                String[] banks = studentInfo.getSchool().getBank().split("<br>");
                String[] bankAccounts = studentInfo.getSchool().getBank_account().split("<br>");
                for (int i = 0; i < banks.length; i++) {
                    text10.add(new Phrase("Банк: " + banks[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Р/счет: " + bankAccounts[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(Chunk.NEWLINE);
                }
                text10.add(new Phrase("Тел.: " + studentInfo.getSchool().getPhone(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Директор Лицея: " + studentInfo.getDirector().getSurname() + " "
                        + studentInfo.getDirector().getName() + " " +
                        (studentInfo.getDirector().getMiddle_name() == null ?
                                "" : studentInfo.getDirector().getMiddle_name()), ordFont));
                text10.add(Chunk.NEWLINE);

                table_info.addCell(text10);
                StudentRelative relative = null;
                try {
                    DbRelative dbr = new DbRelative();
                    dbr.connect();
                    relative = dbr.execMainSQL(studentInfo.getStudent().getId());
                    dbr.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                Paragraph text11 = new Paragraph();
                text11.setAlignment(Element.ALIGN_CENTER);
                text11.add(new Phrase("Родитель", boldFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.setAlignment(Element.ALIGN_LEFT);
                text11.add(new Phrase(relative.getFullName(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("паспорт: " + relative.getPassport(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("адрес: " + relative.getAddress(), ordFont));
                table_info.addCell(text11);
                table_info.addCell(new Phrase(" (М.П)", ordFont));
                table_info.addCell(new Phrase("Подпись:", ordFont));

                document.add(table_info);

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

        };

        String nameOf = "Agreement";
        StreamResource resource = new StreamResource(source1, nameOf
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, nameOf, false);
    }

    private static class myPageEvent extends PdfPageEventHelper {

        Font f_font = new Font(Font.FontFamily.UNDEFINED, 10, Font.NORMAL);

        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            try {
                Phrase ft = new Phrase(String.format(" %d",
                        writer.getPageNumber()), f_font);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, ft,
                        (document.right() - 30),
                        document.bottom() - 10, 0);

            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }
}
