package kg.alex.spt.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbRelative;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class ContractSchoolPdfRu {

    static final Logger logger = LogManager.getLogger(ContractSchoolPdfRu.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    private Document document = null;
    Date aDate = new Date(System.currentTimeMillis());
    private final String nameOf = "Contract";
    private MyVaadinUI myUI;
    private StudentInfoPdf student;


    private final static String FONT_LOCATION = "/home/logo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/logo/TimesNewRomanBold.ttf";

    public ContractSchoolPdfRu(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
        this.myUI = ui;
        this.student = st_info;

        source1 = new StreamResource.StreamSource() {

            public InputStream getStream() {

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
                    spr.add(new Phrase("ДОГОВОР № "
                            + String.format("%07d", student.getContract_number()), font_header));
                    spr.add(Chunk.NEWLINE);

                    spr.add(new Phrase("Об оказании платных образовательных услуг", font_header));
                    spr.add(Chunk.NEWLINE);

                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.set(2017, 6, 10);

                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(spr);
                    document.add(new Paragraph(10, " "));

                    float[] Tdate_colsWidth = {4.5f, 1.5f};
                    PdfPTable Tdate = new PdfPTable(2);
                    Tdate.setWidthPercentage(90f);
                    Tdate.setWidths(Tdate_colsWidth);
                    Tdate.getDefaultCell().setBorder(0);
                    Tdate.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    Tdate.addCell(new Phrase("г. " + student.getScl_city(), ordBoldFont));
                    Tdate.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    if (aDate.before(cal.getTime())) {
                        Tdate.addCell(new Phrase(Settings.dateRu.format(cal.getTime()), ordBoldFont));
                    } else {
                        Tdate.addCell(new Phrase(Settings.dateRu.format(aDate), ordBoldFont));
                    }
                    document.add(Tdate);
                    document.add(new Paragraph(10, " "));

                    Decliner dcl = new Decliner();

                    Paragraph paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("Учреждение " + student.getScl_name_ru()
                            + ", именуемая в дальнейшем «Школа», в лице директора ", ordFont));
                    String fullname;
                    String[] temp = student.getScl_dir_f_name().split(" ");
                    fullname = temp[0];
                    try {
                        if (temp.length > 1) {
                            fullname = fullname + " " + dcl.DeclineNameGenitive(temp[1], false, false);
                        }
                        if (temp.length > 2) {
                            fullname = fullname + " " + dcl.DeclinePatronymicGenitive(temp[2], null, false, false);
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    paragraph.add(new Phrase(fullname, ordBoldFont));
                    paragraph.add(new Phrase(", действующего на основании Устава, утвержденного согласно ст.10 Закона Кыргызской Республики «Об образовании», с одной стороны, ", ordFont));
                    paragraph.add(new Phrase(student.getRel_fullname() + ", ", ordBoldFont));
                    paragraph.add(new Phrase(" и являющаяся(щийся) ", ordFont));
                    paragraph.add(new Phrase(student.getRel_name_dec(), ordBoldFont));
                    paragraph.add(new Phrase(" Обучающегося " + student.getClass_name() + " класса ", ordFont));

                    fullname = student.getStud_sur_name() + " " + student.getStud_name();
                    try {
                        fullname = dcl.DeclineSurnameGenitive(student.getStud_sur_name(), student.isStudentFeminitive()) + " "
                                + dcl.DeclineNameGenitive(student.getStud_name(), student.isStudentFeminitive(), false);
                        if (!student.getStud_middle_name().equals("")) {
                            fullname = fullname + " "
                                    + dcl.DeclinePatronymicGenitive(student.getStud_middle_name(), null, student.isStudentFeminitive(), false);
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    paragraph.add(new Phrase(fullname, ordBoldFont));
                    paragraph.add(new Phrase(", именуемый(ая) в дальнейшем ", ordFont));
                    paragraph.add(new Phrase("«" + student.getRel_name() + "» ", ordBoldFont));
                    paragraph.add(new Phrase("именуемые в дальнейшем «Родители» с другой стороны, в интересах обучающегося, в соответствии со ст.4 Закона Кыргызской Республики «Об образовании», заключили настоящий Договор о нижеследующем:", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("1. ПРЕДМЕТ ДОГОВОРА", boldFont));
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("1.1. Предметом Договора является образовательная деятельность, предоставляемая школой в рамках государственного образовательного стандарта и программ соответствующего уровня образования на платной основе на период ", ordFont));
                    paragraph.add(new Phrase(student.getPeriod() + ".", ordBoldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("1.2. Родители обязуются вносить оплату за образовательные услуги согласно пункту 3 (Условия оплаты Родителей за образовательные услуги) данного Договора.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2. ПРАВА И ОБЯЗАННОСТИ СТОРОН", boldFont));
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.1. Школа обязуется:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.1. Организовать и обеспечить надлежащее исполнение образовательной деятельности, предусмотренной в пункте 1.1. настоящего Договора, согласно Устава Общеобразовательной Школы, ст.10 Закона Кыргызской Республики «Об образовании».", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.2. При первоначальном поступлении в Школу разово обеспечить Обучающегося школьной формой, которая в последующем, по мере необходимости должна приобретаться за счет Родителя.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.2 Школа имеет право:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.1. Самостоятельно устанавливать  и изменять размер родительских  взносов, согласно годовой смете расходов. Школа имеет право ежегодно изменять процентные ставки льготникам. Установленные льготы ", ordFont));
                    paragraph.add(new Phrase("аннулируются ", ordBoldFont));
                    paragraph.add(new Phrase("в случае наличия у Обучающегося дисциплинарного взыскания.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.2. При несвоевременной оплате Родителями образовательных услуг (пункт 3 данного Договора) Обучающийся не допускается к переводным экзаменам из класса в класс (Приказ №281/1 от 10.03.2017 г., п.70 Положения МОиН КР).", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.3. При несвоевременной оплате Родителем взноса, с извещением Родителя ограничить доступ посещения Обучающегося ко всем формам учебных и не учебных занятий и использования материально-технических условий (занятий, библиотеки, столовой, кружков, этюдов и тд., не допускать на экзамены, не выставлять оценки “Эдупэйдж”, “Emektep”). Помимо этого без предупреждения Родителя ", ordFont));
                    paragraph.add(new Phrase("аннулируются ", ordBoldFont));
                    paragraph.add(new Phrase("предоставленные Школой скидки (скидки за успеваемость, призерам олимпиад, скидка за высший балл при поступлении и тд).", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.4. Отчислить Обучающегося из Школы согласно Устава Школы:", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("а) грубого, систематического нарушения «Правил внутреннего распорядка» по решению педагогического совета Школы;", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("б) противозаконных действий по отношению к сверстникам и персоналу Школы.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.5. Удержать сумму родительского взноса за текующую четверть, при расторжении  настоящего договора по инициативе Родителя, при этом ранее предусмотренные скидки не учитываются.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.6. При расторжении настоящего договора по непредвиденным обстоятельствам, с учетом всех понесенных расходов школы, сумма подлежащяя к возврату, возвращается по мере возможности Школы, но не позднее мая следующего года. ", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.7. Во взаимодействии со структурными подразделениями Школы, осуществляющими организацию внутриобъектного и пропускного режимов, обеспечить безопасность Учащегося.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.3. Родители обязаны:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.1. Ознакомиться и утвердить свое согласие с Уставом и локальными актами Школы, регламентирующими правила поведения Обучающегося, взаимоотношений участников образовательного процесса, распорядка дня и т.д.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.2. Провести медицинский осмотр по форме М-86, и предоставить заключение о состоянии здоровья Обучающегося на начало нового учебного года.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.3. Согласовать с медслужбой Школы постановку на учет Обучающегося с хроническим заболеванием и последующие совместные действия.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.4. Обеспечить Обучающегося всеми необходимыми канцелярскими принадлежностями для собственного использования (тетради, альбомы, ручки, цветные карандаши, точилки и т.д.).", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.5. Компенсировать материальный ущерб за причиненный Обучающимся ущерб имуществу Школы.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.6. Содействовать и участвовать в традиционных мероприятиях Школы, предусматривающих участие всех участников образовательного процесса.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.7. Согласовать с Администрацией Школы осуществление подвоза детей в Школу и обратно.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.8. Родители ответственны за сохранность учебников, выданных Школой. В случае порчи или утери, возмещают полностью.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.9. Известить Администрацию Школы в течении 5 рабочих дней об изменении места жительства и/или контактных телефонов.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.10. Вне территории Школы, ответственность за жизнь и безопасность Учащегося Школа не несет.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.4. Родители имеют право:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.1. Требовать от Администрации Школы выполнения условий согласно настоящего Договора.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.2. На защиту законных прав  интересов детей.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.3. На ознакомление с содержанием, реализацией и результатами образовательного процесса.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.4. Избираться в состав Школьного или классного родительского комитета.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    paragraph.add(new Phrase("3. УСЛОВИЯ ОПЛАТЫ РОДИТЕЛЕЙ ЗА ОБРАЗОВАТЕЛЬНЫЕ УСЛУГИ", boldFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.1. Ежегодно с 1 апреля по 15 мая Родители обязаны заключить договор на образовательные услуги на следующий учебный год, ", ordFont));
                    paragraph.add(new Phrase("с обязательным внесением 20% предоплаты родительского взноса.", ordBoldFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("В случае не заключения договора в указанные сроки место Обучающегося будет предоставлено другим желающим.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.2. Своевременно вносить оплату за образовательные услуги Обучающегося в Школе, ", ordFont));
                    paragraph.add(new Phrase("согласно настоящего договора и индивидуального графика оплаты", ordBoldFont));
                    paragraph.add(new Phrase(" между Школой и Родителями.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.2.1. Родительская плата производится согласно Графику, подписанному обеими сторонами, являющегося неотъемлемой частью настоящего договора. При этом последний взнос должен быть внесен не позднее 28го февраля следующего года. Размер родительского взноса не изменяется даже при условии перехода Школы на дистанционную форму обучения.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.2.2. Общая стоимость платы за образовательные услуги Обучающегося (родительские взносы) составляет ", ordFont));
                    paragraph.add(new Phrase(student.getCtr_k_oplate() + "", ordBoldFont));
                    paragraph.add(new Phrase(" долларов США, которая производится строго в сомах на день оплаты по курсы НБ КР.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.2.3. Родительская плата производится ", ordFont));
                    paragraph.add(new Phrase("в сомах на банковский счет Школы,", ordBoldFont));
                    paragraph.add(new Phrase(" не позднее 3 календарных дней с даты, указанной в официальном счете/invoice.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.3. Школа удерживает  ", ordFont));
                    paragraph.add(new Phrase("сумму за одну четверть", ordBoldFont));
                    paragraph.add(new Phrase(" оплаты за образовательные услуги при расторжении настоящего договора по инициативе Родителей.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    paragraph.add(new Phrase("4. ФОРС-МАЖОР", boldFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("4.1. Ни одна из сторон не несет ответственности за полное или частичное неисполнение своих обязательств при возникновении обстоятельств, которые делают полностью или частично невозможным выполнение Договора одной из сторон, а именно: пожар, стихийное природное бедствие (землетрясение, наводнение и др.), война, военные действия всех видов, забастовка, блокада, эпидемия, изменение текущего Законодательства Кыргызской Республики и другие возможные обстоятельства непреодолимой силы, не зависящие от сторон, подписавших Договор.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    paragraph.add(new Phrase("5. СРОК ДЕЙСТВИЯ ДОГОВОРА, ПОРЯДОК ИЗМЕНЕНИЯ, ДОПОЛНЕНИЯ И РАСТОРЖЕНИЯ", boldFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.1. Настоящий Договор вступает в силу с момента его подписания обеими сторонами.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.2. Настоящий Договор может быть расторгнут досрочно, согласно п.2.2.2.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.3. Любые дополнения и изменения к настоящему Договору действительны лишь при условии, что они совершены в письменной форме и подписаны обеими сторонами.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.4. Все разногласия по данному Договору решаются сторонами в порядке переговоров. В случае невозможности разрешения спора путем переговоров, они решаются в судебном порядке, установленном законодательством Кыргызской Республики.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.5. Стороны договорились, что деловая корреспонденция и иные документы, касающиеся настоящего Договора, отправленные и полученные посредством факсимильной, электронной связи или иным способом, позволяющим достоверно установить, что документ исходит от стороны по Договору, признаются имеющими юридическую силу.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.6. Настоящий Договор составлен в двух экземплярах на русском (кыргызском) языке и подписан обеими сторонами. Оба экземпляра идентичны и имеют одинаковую юридическую силу. У каждой из сторон находится один экземпляр настоящего Договора.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.7. Срок действия настоящего договора ", ordFont));
                    paragraph.add(new Phrase("один учебный год.", ordBoldFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    //document.newPage();
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    paragraph.add(new Phrase("6. РЕКВИЗИТЫ СТОРОН", boldFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    float[] Tinfo_colsWidth = {1.5f, 1f};
                    PdfPTable Tinfo = new PdfPTable(2);
                    Tinfo.getDefaultCell().setBorder(0);
                    Tinfo.setWidthPercentage(90f);
                    Tinfo.setWidths(Tinfo_colsWidth);
                    Paragraph text10 = new Paragraph();
                    text10.add(new Phrase("Школа: " + student.getScl_name_ru(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Адрес: " + student.getScl_address(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("ИНН: " + student.getScl_inn(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Банк: " + student.getScl_bank(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Р/счет: " + student.getScl_bank_account(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Тел.: " + student.getScl_phone(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Директор Школы: " + student.getScl_dir_f_name(), ordFont));
                    text10.add(Chunk.NEWLINE);

                    IndexedContainer relativeCont = null;
                    Tinfo.addCell(text10);
                    try {
                        DbRelative dbr = new DbRelative();
                        dbr.connect();
                        relativeCont = dbr.execSQL(myUI, student.getStud_id());
                        dbr.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    Paragraph text11 = new Paragraph();
                    Paragraph text18 = new Paragraph();
                    Iterator iter = relativeCont.getItemIds().iterator();
                    String f_name = "";
                    String f_worplace = "";
                    String m_name = "";
                    String m_worplace = "";
                    String passport = "";
                    while (iter.hasNext()) {
                        Object obj = iter.next();
                        if ((Integer) obj == 1) {
                            f_name = relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.FullName)).getValue().toString();
                            f_worplace = relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.WorkPlace)).getValue().toString();
                        }
                        if ((Integer) obj == 2) {
                            m_name = relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.FullName)).getValue().toString();
                            m_worplace = relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.WorkPlace)).getValue().toString();

                        }
                        if ((Integer) relativeCont.getContainerProperty(obj,
                                Settings.is_main).getValue() == 1) {
                            text18.add(new Phrase("Контактный тел: ", ordFont));
                            text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.Phone)).getValue().toString(), ordFont));
                            text18.add(Chunk.NEWLINE);
                            text18.add(new Phrase("Адрес места жительства: ", ordFont));
                            text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.Address)).getValue().toString(), ordFont));
                            passport = relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.Passport)).getValue().toString();
                        }
                    }
                    text11.add(new Phrase("Ф.И.О. отца: " + f_name, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Ф.И.О. матери: " + m_name, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Данные паспорта: ", ordFont));
                    text11.add(new Phrase(passport, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Место работы отца: " + f_worplace, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Место работы матери: " + m_worplace, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(text18);
                    Tinfo.addCell(text11);
                    Tinfo.addCell(new Phrase("                             (М.П)", ordFont));
                    Tinfo.addCell(new Phrase("Подпись отца:   ________________", ordFont));
                    Tinfo.addCell(new Phrase(" ", ordFont));
                    Tinfo.addCell(new Phrase("Подпись матери: ________________", ordFont));

                    document.add(Tinfo);
                    document.add(new Paragraph(10, " "));

                    Paragraph text15 = new Paragraph();
                    text15.setIndentationLeft(30);
                    text15.setIndentationRight(30);
                    text15.add(new Phrase("График оплаты", boldFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("ID ученика: ", ordFont));
                    text15.add(new Phrase(student.getStud_login(), ordBoldFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Ф.И.О. Ученика: ", ordFont));
                    text15.add(new Phrase(student.getStud_sur_name() + " "
                            + student.getStud_name() + " " + student.getStud_middle_name(), ordBoldFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Кл.: ", ordFont));
                    text15.add(new Phrase(student.getClass_name(), ordBoldFont));
                    text15.add(new Phrase(" Дата регистрации: ", ordFont));
                    text15.add(new Phrase(df.format(new Date()), ordBoldFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("ИТОГО взноса: ", ordFont));
                    text15.add(new Phrase((Settings.dFormat.format(student.getCtr_contract_sum()) + ""), ordBoldFont));
                    text15.add(new Phrase(" долларов США.", ordFont));
                    text15.add(Chunk.NEWLINE);
                    if (student.getCtr_debt() >= 0) {
                        text15.add(new Phrase("Долг с предыдущего года: ", ordFont));
                    } else {
                        text15.add(new Phrase("Переплата с предыдущего года: ", ordFont));
                    }
                    text15.add(new Phrase((Settings.dFormat.format(student.getCtr_debt()) + ""), ordBoldFont));
                    text15.add(new Phrase(" долларов США.", ordFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Скидка: ", ordFont));
                    if (student.getCtr_discountPerc() != null) {
                        text15.add(new Phrase(student.getCtr_discountStr(), ordBoldFont));
//                        text15.add(new Phrase(" (" + student.getCtr_discountPerc() + ")", ordFont));
                    }
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Корректировка: ", ordFont));
                    if (student.getCtr_Correction() != null) {
                        text15.add(new Phrase(student.getCtr_Correction(), ordBoldFont));
                    }
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Предоплата: ", ordFont));
                    text15.add(new Phrase(Settings.dFormat.format(student.getCtr_init_payment()) + "", ordBoldFont));
                    text15.add(new Phrase(" долларов США.", ordFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Остаток: ", ordFont));
                    text15.add(new Phrase(Settings.dFormat.format(student.getCtr_ttl_left_sum()) + "", ordBoldFont));
                    text15.add(new Phrase(" долларов США.", ordFont));
                    document.add(text15);
                    document.add(new Paragraph(10, " "));

                    Paragraph text16 = new Paragraph();
                    text16.setIndentationLeft(30);
                    text16.setIndentationRight(30);
                    text16.add(new Phrase("Таблица 1.", ordBoldFont));
                    document.add(text16);
                    document.add(new Paragraph(10, " "));
                    text16.add(Chunk.NEWLINE);

                    float[] TContract_colsWidth = {1f, 4f, 4f, 4f, 4f};
                    PdfPTable TContract = new PdfPTable(5);
                    TContract.setWidthPercentage(90f);
                    TContract.setWidths(TContract_colsWidth);
                    TContract.addCell(new Phrase("№", ordBoldFont));
                    TContract.addCell(new Phrase("Дата оплаты", ordBoldFont));
                    TContract.addCell(new Phrase("Сумма", ordBoldFont));
                    TContract.addCell(new Phrase("Потверждающий документ", ordBoldFont));
                    TContract.addCell(new Phrase("Подпись ", ordBoldFont));
                    int n = 1;
                    Iterator iter1 = instPlanCont.getItemIds().iterator();
                    while (iter1.hasNext()) {
                        Object obj = iter1.next();
                        TContract.addCell(new Phrase(n + "", ordFont));
                        TContract.addCell(new Phrase(Settings.df.format(((DateField) instPlanCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.Date)).getValue()).getValue()), ordFont));
                        TContract.addCell(new Phrase(((TextField) instPlanCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.Amount)).getValue()).getValue(), ordFont));
                        TContract.addCell(new Phrase("", ordFont));
                        TContract.addCell(new Phrase("", ordFont));
                        n += 1;
                    }
                    TContract.addCell(new Phrase("", ordFont));
                    TContract.addCell(new Phrase("Итого:", ordBoldFont));
                    TContract.addCell(new Phrase(Settings.dFormat.format(student.getCtr_k_oplate()) + "", ordBoldFont));
                    TContract.addCell(new Phrase("", ordFont));
                    TContract.addCell(new Phrase("", ordFont));

                    document.add(TContract);

                    paragraph = new Paragraph();
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.add(Chunk.NEWLINE);
                    paragraph.add(new Phrase("Подпись Родителя: ", ordBoldFont));
                    document.add(paragraph);
                    document.add(new Paragraph(5, " "));
                    paragraph = new Paragraph();
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.add(new Phrase("Директор: ", ordBoldFont));
                    document.add(paragraph);
                    document.add(new Paragraph(5, " "));
                    paragraph = new Paragraph();
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.add(new Phrase("Гл. бухгалтер: ", ordBoldFont));
                    document.add(paragraph);
                    document.add(new Paragraph(5, " "));
                    paragraph = new Paragraph();
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.add(new Phrase("Печать ", ordBoldFont));
                    document.add(paragraph);
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

    private class myPageEvent extends PdfPageEventHelper {

        Font ffont = new Font(Font.FontFamily.UNDEFINED, 10, Font.NORMAL);

        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            try {
                Phrase ft = new Phrase(String.format(" %d",
                        writer.getPageNumber()), ffont);
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
