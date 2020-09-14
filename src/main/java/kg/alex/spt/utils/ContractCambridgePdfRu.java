package kg.alex.spt.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ContractCambridgePdfRu {

    static final Logger logger = LogManager.getLogger(ContractCambridgePdfRu.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    private Document document = null;
    Date aDate = new Date(System.currentTimeMillis());
    private final String nameOf = "Contract";
    private MyVaadinUI myUI;
    private StudInfoPdf student;
    SystemSettings sysSettings = new SystemSettings();
    private final static String FONT_LOCATION = "/home/logo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/logo/TimesNewRomanBold.ttf";

    public ContractCambridgePdfRu(final MyVaadinUI ui, StudInfoPdf st_info, final IndexedContainer instPlanCont) {
        this.myUI = ui;
        this.student = st_info;

        source1 = new StreamResource.StreamSource() {

            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4, 10, 10, 80, 30);

                    PdfWriter writer = PdfWriter.getInstance(document, buffer);
                    writer.setPageEvent(new myPageEvent());

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    Font ordFont = new Font(baseFont, 11);
                    Font ordBoldFont = new Font(baseFontBold, 11);
                    Font boldFont = new Font(baseFontBold, 12);
                    Font font_header = new Font(baseFontBold, 13);

                    document.open();

                    document.add(new Paragraph(10, " "));

                    PdfContentByte punder = writer.getDirectContentUnder();

                    Paragraph spr = new Paragraph();
                    spr.add(new Phrase("ДОГОВОР", font_header));
                    spr.add(Chunk.NEWLINE);
                    spr.add(new Phrase("обучения за счет родительских взносов", font_header));
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
                        Tdate.addCell(new Phrase(sysSettings.dateRu.format(cal.getTime()), ordBoldFont));
                    } else {
                        Tdate.addCell(new Phrase(sysSettings.dateRu.format(aDate), ordBoldFont));
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
                    paragraph.add(new Phrase("Учреждение " + student.getScl_name_ru(), ordBoldFont));
                    paragraph.add(new Phrase(", именуемая в дальнейшем «Школа», в лице директора ", ordFont));
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
                    paragraph.add(new Phrase(", действующего на основании  Устава, с одной стороны, и ", ordFont));
                    paragraph.add(new Phrase(student.getRel_fullname() + ", ", ordBoldFont));
                    paragraph.add(new Phrase("являющаяся(щийся) ", ordFont));
                    paragraph.add(new Phrase(student.getRel_name_dec(), ordBoldFont));
                    paragraph.add(new Phrase(" «Учащегося» ", ordFont));
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
                    paragraph.add(new Phrase("с другой стороны, в интересах обучающегося, в соответствии с пунктом 1 статьи 14 Закона Кыргызской Республики «Об образовании», заключили настоящий Договор о нижеследующем:", ordFont));
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
                    temp = student.getScl_year_name().split("-");
                    paragraph.add(new Phrase("1.1. Предметом Договора является организация процесса обучения Учащегося, получение им образования по стандартам «Cambridge Assessment International Education» и программ соответствующего уровня образования по Британскому учебному плану, на период с 25 августа " + temp[0] + " года по 12 июня " + temp[1] + "года.", ordFont));
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
                    paragraph.add(new Phrase("2.1. Школа обязана:", boldFont));
                    paragraph.add(Chunk.NEWLINE);
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.1. Организовать и обеспечить надлежащее исполнение услуг, предусмотренных в статье 1.1. настоящего Договора. Образовательные услуги оказываются в соответствии с стандартом «Cambridge Assessment International Education» и программ соответствующего уровня образования по Британскому учебному плану.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.2. В целях усвоения Учащимся образовательных программ, являющихся предметом настоящего Договора, обеспечить Учащегося методической и консультационной помощью, оказываемой в порядке, установленном Школой.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.3. Предоставить Учащемуся на время обучения учебные кабинеты, компьютерный класс, библиотеку.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.4. Обеспечить Учащегося трехразовым питанием (чай, обед и ужин).", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.5. Сохранить место за Учащимся в случае пропуска занятий по уважительным причинам.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.6. Проявлять уважение к личности Учащегося, не допускать физического и психологического насилия, обеспечить условия укрепления  нравственного, физического и психологического здоровья, эмоционального благополучия Учащегося с учетом его индивидуальных особенностей.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.7. Отвечать за жизнь и здоровье Учащегося в период его пребывания в Школе, при условии неукоснительного соблюдения им  «Правил Внутреннего Распорядка».", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.2 Школа имеет право:", boldFont));
                    paragraph.add(Chunk.NEWLINE);
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.1. Самостоятельно, с учетом государственных программ, выбирать, разрабатывать и применять учебные программы и методики в процессе обучения Учащегося.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.2. Самостоятельно составлять меню, производить замены блюд.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.3. Отчислить Учащегося из Школы без возмещения стоимости обучения в следующих случаях:", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("а) грубого, систематического нарушения «Правил Внутреннего Распорядка» с предоставлением документов, подтверждающих нарушения, совершенные Учащимся,", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("б) противозаконных действий по отношению к сверстникам и персоналу Школы,", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("в) нарушения действующего  законодательства Кыргызской Республики.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.4. Расторгнуть настоящий Договор при условии не освоения Учащимся в установленный годовым календарным планом (графиком) срок образовательных программ, являющихся предметом настоящего Договора.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.5. Самостоятельно перевести Учащегося в параллельную группу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.6. Расторгать в одностороннем порядке договор с родителями, систематически нарушаюших п.3.1-3.4 настоящего Договора.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.3. Родители обязаны:", boldFont));
                    paragraph.add(Chunk.NEWLINE);
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.1. Своевременно оплатить взнос за обучение Учащегося в Школе, согласно п. 1.1. настоящего Договора, в сроки, оговоренные в пункте 3.1.-3.4. настоящего Договора. Обеспечить своего ребенка всеми необходимыми канцелярскими принадлежностями для собственного использования (тетради, цветные карандаши, точилки и т.д.).", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.2. Провести медицинскую диспансеризацию Учащегося, предоставить Администрации Школы заключение медицинских специалистов о состоянии здоровья Учащегося перед прибытием его в Школу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.3. Заполнить «Анкету учащегося» и «Заявление о предоставлении правдивой информации о состоянии здоровья учащегося».", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.4. Ознакомить Учащегося с «Правилами внутреннего распорядка». Обеспечить адекватное воздействие на Учащегося в случаях нарушения им  «Правил внутреннего распорядка».", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.5. Нести полную материальную ответственность за все действия Учащегося, повлекшие за собой порчу или уничтожение имущества Школы.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.6. Обеспечить подвоз ученика в школу и обратно в случае отказа от школьного транспорта.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.4. Родители имеют право:", boldFont));
                    paragraph.add(Chunk.NEWLINE);
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.1. Требовать от Администрации Школы выполнения условий, изложенных в пунктах 2.1.1. – 2.1.7. настоящего Договора.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.2. Досрочно расторгнуть Договор с возмещением стоимости обучения (за вычетом фактически понесенных расходов в период  пребывания Учащегося в Школе)  в следующих случаях:", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("а) внезапной тяжелой болезни Учащегося, делающей невозможным его дальнейшее пребывание в Школе, при наличии официального заключения о состоянии здоровья Учащегося медицинскими специалистами Кыргызской Республики,", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("б) нарушения Школой условий, изложенных в п.п. 2.1.1. – 2.1.7 настоящего Договора.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("в) в связи переводом в другую школу.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    paragraph.add(new Phrase("3. УСЛОВИЯ ОПЛАТЫ", boldFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("3.1. Администрацией школы установлены следующие сроки оплаты родительского взноса:", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.2. Оплата родительского взноса производиться ", ordFont));
                    paragraph.add(new Phrase("согласно Графику,", ordBoldFont));
                    paragraph.add(new Phrase(" подписанному обеими сторонами, являющегося неотъемлемой частью настоящего договора. При этом последний взнос должен быть внесен не позднее 28 февраля, следующего года.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.3.  Оплата родителями или лицами их заменяющими, родительского взноса ", ordFont));
                    paragraph.add(new Phrase("производится в сомах на банковский счет ", ordBoldFont));
                    paragraph.add(new Phrase("Школы , не позднее 3 календарных дней с даты, указанной в официальном счете/ invoice.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.4. Общая стоимость родительских взносов составляет ", ordFont));
                    paragraph.add(new Phrase(student.getCtr_k_oplate() + "", ordBoldFont));
                    paragraph.add(new Phrase(" долл. США, которая производится строго в сомах на день оплаты по курсу НБ КР.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.5. Учебники, подвоз детей в школу и обратно, питание предоставляется школой бесплатно.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.6. Школьная форма предоставляется ", ordFont));
                    paragraph.add(new Phrase("1 раз в год при поступлении ", ordBoldFont));
                    paragraph.add(new Phrase("в школу. В случае необходимости дополнительная школьная форма предоставляется за отдельную оплату за каждую единицу формы.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.7. Оплата за обучение не включает в себя оплату за официальный экзамен «Cambridge Assessment International Education».", ordFont));
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
                    paragraph.add(new Phrase("4.1. Ни одна из сторон не несет ответственности за полное или частичное неисполнение своих обязательств при возникновении обстоятельств, которые делают полностью или частично невозможным выполнением Договора одной из сторон, а именно: пожар, стихийное природное бедствие (землетрясение, наводнение и др.), война, военные действия всех видов, забастовка, блокада, изменение текущего Законодательства Кыргызской Республики и другие возможные обстоятельства непреодолимой силы, не зависящие от сторон, подписавших Договор.", ordFont));
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
                    paragraph.add(new Phrase("5.2. Настоящий Договор может быть расторгнут досрочно, согласно п.2.2.3. и 2.4.2.", ordFont));
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
                    paragraph.add(new Phrase("5.4. Все разногласия по данному Договору решаются сторонами в порядке переговоров. В случае невозможности разрешения спора путем переговоров, они решаются в порядке, установленном законодательством Кыргызской Республики.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.5. Настоящий Договор составлен в двух экземплярах на русском языке и подписан обеими сторонами. Оба экземпляра идентичны и имеют одинаковую юридическую силу. У каждой из сторон находится один экземпляр настоящего Договора.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

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
                    text10.add(new Phrase("ОКПО: 30404367", ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Банк: " + student.getScl_bank(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("БИК: 109008", ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Р/счет: " + student.getScl_bank_account(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Тел.: " + student.getScl_phone(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Директор Школы: " + student.getScl_dir_f_name(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("(Подпись)", ordFont));
                    Tinfo.addCell(text10);

                    Paragraph text11 = new Paragraph();
                    text11.add(new Phrase(student.getRel_name() + ": " + student.getRel_fullname(), ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Адрес: " + student.getRel_address(), ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Пасспорт: " + student.getRel_passport(), ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Тел.: " + student.getRel_phone(), ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase(student.getRel_name() + ": __________________", ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("                         (Подпись)", ordFont));
                    Tinfo.addCell(text11);

                    document.add(Tinfo);
                    document.newPage();

                    Paragraph text15 = new Paragraph();
                    text15.setIndentationLeft(30);
                    text15.setIndentationRight(30);
                    text15.add(new Phrase("График оплаты", boldFont));
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
                    text15.add(new Phrase((sysSettings.dFormat.format(student.getCtr_contract_sum()) + ""), ordBoldFont));
                    text15.add(new Phrase(" долларов США.", ordFont));
                    text15.add(Chunk.NEWLINE);
                    if (student.getCtr_debt() >= 0) {
                        text15.add(new Phrase("Долг с предыдущего года: ", ordFont));
                    } else {
                        text15.add(new Phrase("Переплата с предыдущего года: ", ordFont));
                    }
                    text15.add(new Phrase((sysSettings.dFormat.format(student.getCtr_debt()) + ""), ordBoldFont));
                    text15.add(new Phrase(" долларов США.", ordFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Скидка: ", ordFont));
                    if (student.getCtr_discountPerc() != null) {
                        text15.add(new Phrase(student.getCtr_discountStr(), ordBoldFont));
//                        text15.add(new Phrase(" (" + student.getCtr_discountPerc() + ")", ordFont));
                    }
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Предоплата: ", ordFont));
                    text15.add(new Phrase(sysSettings.dFormat.format(student.getCtr_init_payment()) + "", ordBoldFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Остаток: ", ordFont));
                    text15.add(new Phrase(sysSettings.dFormat.format(student.getCtr_ttl_left_sum()) + "", ordBoldFont));
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
                        TContract.addCell(new Phrase(((DateField) instPlanCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.Date)).getValue()).getValue().toString(), ordFont));
                        TContract.addCell(new Phrase(((TextField) instPlanCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.Amount)).getValue()).getValue(), ordFont));
                        TContract.addCell(new Phrase("", ordFont));
                        TContract.addCell(new Phrase("", ordFont));
                        n += 1;
                    }
                    TContract.addCell(new Phrase("", ordFont));
                    TContract.addCell(new Phrase("Итого:", ordBoldFont));
                    TContract.addCell(new Phrase(sysSettings.dFormat.format(student.getCtr_k_oplate()) + "", ordBoldFont));
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
            try {
                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                        BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                Font fontGray = new Font(baseFont, 10, Font.NORMAL, BaseColor.RED);
                Font ordFont = new Font(baseFont, 11.5f, Font.NORMAL);
                Font footerFontGray = new Font(baseFont, 6, Font.NORMAL, BaseColor.GRAY);
                Chunk SEPARATOR = new Chunk(
                        new LineSeparator(0.1f, 100, BaseColor.GRAY, Element.ALIGN_CENTER, 15f));

                ColumnText ct = new ColumnText(writer.getDirectContent());
                Rectangle rect = new Rectangle(36, 0, 560, 830);

                float[] col_widths = {3.3f, 0.6f, 2.7f};
                PdfPTable t = new PdfPTable(3);
                t.setWidthPercentage(100f);
                t.setWidths(col_widths);
                t.getDefaultCell().setBorder(0);
                t.getDefaultCell().setBorderColor(BaseColor.GRAY);
                t.getDefaultCell().setBorderWidthBottom(0.1f);
                t.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                t.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                Paragraph p = new Paragraph();
                p.add(new Phrase("Международная школа \n«Юнайтед Ворлд Интернэшнл Скул»", ordFont));
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("(Аккредитованные программы Cambridge Assessment \n и Advanced Placement)", fontGray));
                t.addCell(p);

                Image logo = Image.getInstance(SystemSettings.PATH_TO_UPLOADS + "cambridge_logo.png");
                logo.setAlignment(Image.MIDDLE);
                logo.scaleToFit(50, 50);
                Chunk chunk = new Chunk(logo, 0, -20);
                t.addCell(new Phrase(chunk));
                p = new Paragraph();
                p.add(new Phrase("United World International School", ordFont));
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("(Cambridge Assessment \n and AP Accredited Programs)", fontGray));
                t.addCell(p);

                ct.setSimpleColumn(rect);
                ct.setAlignment(Element.ALIGN_CENTER);
                ct.addElement(t);
                p = new Paragraph(8);
                p.setAlignment(Element.ALIGN_CENTER);
                p.add(new Phrase("Инн: 02705201910356    ОКПО: 30404367     Код ГНИ: 001 Октябрьский     Соц.Фонд: 104100013276     Банк: ОАО “Оптима Банк”    р/с: 1090805952270150     Бик: 109008", footerFontGray));
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("Адрес: г. Бишкек, ул. А.Масалиева 26, 720016    Телефоны: +996 (312) 884129, +996 (551) 932222     www.uwis.edu.kg     info@uwis.edu.kg", footerFontGray));
                ct.addElement(p);
                ct.addElement(SEPARATOR);
                ct.go();

            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }

    }
}
