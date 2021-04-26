package kg.alex.spt.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
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
import kg.alex.spt.dao.DbRelative;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ContractLisePdfRu {

    static final Logger logger = LogManager.getLogger(ContractLisePdfRu.class);
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

    public ContractLisePdfRu(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
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
                    spr.add(new Phrase("ДОГОВОР", font_header));
                    spr.add(Chunk.NEWLINE);
                    spr.add(new Phrase("об оказании платных образовательных услуг", font_header));
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
                        Tdate.addCell(new Phrase(SystemSettings.dateRu.format(cal.getTime()), ordBoldFont));
                    } else {
                        Tdate.addCell(new Phrase(SystemSettings.dateRu.format(aDate), ordBoldFont));
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
                    paragraph.add(new Phrase("Образовательное учреждение " + student.getScl_name_ru()
                            + ", именуемый в дальнейшем «Лицей», в лице директора ", ordFont));
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
                    paragraph.add(new Phrase(", действующего на основании Устава, с одной стороны, и ", ordFont));
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
                    paragraph.add(new Phrase("с другой стороны, в интересах обучающегося, в соответствии с Законом Кыргызской Республики «Об образовании», заключили настоящий Договор о нижеследующем:", ordFont));
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
                    paragraph.add(new Phrase("1.1. Предметом Договора является организация процесса обучения, воспитания и проживания Учащегося на условиях полного пансиона, получение им образования в рамках государственного образовательного стандарта и программ общеобразовательной средней школы, на период ", ordFont));
                    paragraph.add(new Phrase(student.getPeriod() + ".", ordFont));
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
                    paragraph.add(new Phrase("2.1. Лицей обязан:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.1. Организовать и обеспечить надлежащее исполнение услуг, предусмотренных в п. 1.1. настоящего Договора.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.2. В целях усвоения Учащимся образовательных программ, являющихся предметом настоящего Договора, обеспечить Учащегося индивидуальной образовательной помощью, оказываемой в порядке, установленном Лицеем.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.3. Предоставлять Учащемуся на время обучения учебники и другую литературу по предметам и программам учебного плана II, III ступени обучения (нужное подчеркнуть), имеющуюся в библиотеке Лицея, предоставить в его распоряжение оборудованные учебные кабинеты, компьютерный класс, библиотеку.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.4. Во взаимодействии со структурными подразделениями Лицея, осуществляющими организацию внутриобъектного и пропускного режимов, обеспечить безопасность Учащегося.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.5. Разместить Учащегося на период обучения, утвержденный МОиН КР, в благоустроенном общежитии с наличием коммунальных удобств. В случае расторжения контракта право на проживание в общежитии Учащимся утрачивается.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.6. Обеспечить Учащегося 3-х разовым питанием в соответствии с возрастными особенностями.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.7. При первоначальном поступлении в Лицей разово обеспечить Учащегося школьной формой, которая в последующем, по мере необходимости должна приобретаться за счет Родителя.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.8. Сохранить место за Учащимся в случае пропуска занятий по уважительным причинам.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.9. Осуществлять в установленном Уставе Лицея порядке полугодовую и годовую аттестацию Учащегося, соответствующую учебному плану Лицея, утвержденному Министерством образования и науки Кыргызской Республики.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.10. Переводить Учащегося в следующий класс в установленном порядке по решению педагогического совета Лицея, на основании Положения Министерства образования и науки Кыргызской Республики.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.11. При успешном окончании Учащимся II ступени образования выдать свидетельство, III ступени - аттестат государственного образца.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.12. Проявлять уважение к личности Учащегося, не допускать физического и психологического насилия, обеспечить условия укрепления нравственного, физического и психологического здоровья, эмоционального благополучия Учащегося с учетом его индивидуальных особенностей.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.13. Обеспечить текущий контроль за состоянием здоровья Учащегося медицинским работником Лицея, медицинская квалификация которого подтверждена документально.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.14. Ознакомить Учащегося с внутренними локальными актами и «Правилами внутреннего распорядка» Лицея, по соблюдению требований по безопасности образовательной среды и здоровье сберегающих правил.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.15. Предоставлять информацию Родителю о состоянии здоровья, учебы и поведения Учащегося по результатам каждой учебной четверти.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.2 Лицей имеет право:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.1. Самостоятельно составлять меню, производить замены блюд.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.2. Самостоятельно устанавливать и изменять размер родительских взносов согласно годовой смете расходов. Лицей имеет право ежегодно решением Педсовета изменять размер предоставленной скидки в оплате. Установленные скидки (за успеваемость, призер олимпиады, скидка за высший балл при поступлении, в том числе скидки, предоставленные Генеральной дирекцией МОУ «Сапат» и др.) ", ordFont));
                    paragraph.add(new Phrase("аннулируются ", ordBoldFont));
                    paragraph.add(new Phrase("без предупреждения, в случае наличия у учащегося дисциплинарного взыскания.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.3. При несвоевременной оплате Родителем взноса, с извещением Родителя ограничить доступ посещения Учащимся ко всем формам учебных и не учебных занятий и использования материально-технических условий (занятий, библиотеки, столовой, кружков, этюдов и тд., не допускать на экзамены, не выставлять оценки “Эдупэйдж”, “Emektep”).", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.4. Отчислить Учащегося из Лицея по решению Педагогического совета Лицея без возмещения стоимости обучения в следующих случаях:", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("а) грубого, систематического нарушения «Правил Внутреннего Распорядка» Лицея и общежития; (употребление спиртных напитков, наркотических средств, электронные сигареты, использование никотиносодержащих средств, играть в азартные игры, покидать территорию Лицея во время перемен и учебных занятий без разрешения);", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("б) противозаконных действий по отношению к сверстникам и персоналу Лицея (употребление спиртных напитков, наркотических средств, электронные сигареты, использование никотиносодержащих средств, играть в азартные игры, покидать территорию лицея во время перемен и учебных занятий без разрешения);", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("в) нарушения действующего Законодательства Кыргызской Республики. (Родители несут уголовную ответственность в случае принуждения вступления в брак несовершеннолетней дочери/сына на основании статьи №154 Уголовного кодекса Кыргызской Республики).", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.5. Не принимать Учащегося в общежитие после 19:00 часов.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.6. Отказать Учащемуся в выдаче документа государственного образца о соответствующем образовании в случае невыполнения им требований «Положения о проведении государственной итоговой аттестации выпускников и порядке перевода учащихся в последующий класс в государственных и негосударственных общеобразовательных организациях Кыргызской Республики».", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.7. В случае пропуска учебных 45 дней по/без уважительной причины, Учащийся остается на повторный курс обучения (на второй год).", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.8. В целях компетентного подхода к трудовому воспитанию, администрация Лицея имеет право привлекать учащихся к уборке своего рабочего места в классных комнатах и общежитии.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.9. Лицей по желанию и согласованию с родителями, организовывает проведение подготовительных курсов к ОРТ, TOEFL, SAT, IELTS и т.д., кружковую работу по интересам учащихся, за отдельную плату. В связи с чем администрация Лицея имеет право запретить учащимся посещение дополнительных образовательных занятий вне лицея.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.10. Учащимся, состоящим на диспансерном учете по хроническим заболеваниям (эпилепсии, астмы, порока сердца, энуреза и др.) особые условия не предоставляются. Родители, либо законные представители Учащихся, обязаны предупредить в письменном виде медицинского сотрудника Лицея о хронических заболеваниях Учащегося.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.11. Удержать ", ordFont));
                    paragraph.add(new Phrase("сумму родительского взноса за текущую четверть", ordBoldFont));
                    paragraph.add(new Phrase(", при расторжении  настоящего договора по инициативе Родителя, при этом ранее предусмотренные скидки ", ordFont));
                    paragraph.add(new Phrase("не учитываются.", ordBoldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.12. При расторжении настоящего договора по непредвиденным обстоятельствам, с учетом всех понесенных расходов Лицея, сумма подлежащяя к возврату, возвращается по мере возможности Лицея, но не позднее мая следующего года.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.13. В целях рекламы деятельности Лицея без уведомления учащегося и родителей размещать фото и видеоматериалы.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.14. Лицей может иметь иные права, предусмотренные законодательством КР.", ordFont));
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
                    paragraph.add(new Phrase("2.3.1. Своевременно оплачивать родительские взносы, согласно п. 1.1. настоящего Договора, в сроки, оговоренные в пункте 3.1.1. - 3.1.3. настоящего Договора.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.2. Ежегодно с 1 апреля по 15 мая родители обязаны заключать контракт на следующий учебный год, с обязательным внесением ", ordFont));
                    paragraph.add(new Phrase("20% предоплаты ", ordBoldFont));
                    paragraph.add(new Phrase("родительского взноса.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("- В случае ", ordFont));
                    paragraph.add(new Phrase("не заключения контракта  в указанные сроки", ordBoldFont));
                    paragraph.add(new Phrase(" учебное место будет предоставлено другим учащимся.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.3. Провести полную медицинскую диспансеризацию Учащегося, предоставить Администрации Лицея заключение медицинских специалистов о состоянии здоровья Учащегося перед прибытием его в Лицей. Дать письменное согласие или отказ от медицинских услуг:", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("а) при необходимости оказания медицинской помощи Учащимся;", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("б) услуг психолога", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("В случае необходимости в дорогостоящих препаратах либо вызове частной медицинской скорой помощи, оплатить расходы.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.4. Заполнить «Анкету учащегося» (Приложение №_____), «Разрешение на самостоятельные прогулки» (Приложение №____) и «Заявление о предоставлении правдивой информации о состоянии здоровья учащегося» (Приложение №____).", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.5. Содействовать выполнению Учащимся «Правил внутреннего распорядка» Лицея. Воздействовать на Учащегося в случаях нарушения им «Правил внутреннего распорядка» Лицея.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.6. Обеспечить Обучающегося всеми необходимыми канцелярскими принадлежностями для собственного использования (тетради, альбомы, ручки, цветные карандаши, точилки и т.д.).", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.7. Нести полную материальную ответственность за все действия Учащегося, повлекшие за собой порчу или уничтожение имущества Лицея.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.8. Возместить Лицею стоимость нанесенного ущерба в течение 7 календарных дней со дня получения официального счета/invoice от Администрации Лицея.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.9. Поддерживать постоянную телефонную связь с Администрацией Лицея:", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("- не реже одного раза в две недели интересоваться о текущем состоянии здоровья Учащегося, образовательных программ внеклассных мероприятий.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.10. Осуществлять подвоз детей в Лицей и обратно.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.11. Вне территории Лицея ответственность за жизнь и безопасность Учащегося Лицей не несет.", ordFont));
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
                    paragraph.add(new Phrase("2.4.1. Требовать от Администрации Лицея выполнения условий, изложенных в пунктах 2.1.1. – 2.1.14. настоящего Договора.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.2. Досрочно расторгнуть Договор с возмещением стоимости обучения и проживания в период пребывания Учащегося в Лицее в соответствии с п.2.2.9, 2.2.10.", ordFont));
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
                    paragraph.add(new Phrase("3.1. Администрацией Лицея установлены следующие сроки оплаты:", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.1.1. Оплата родительского взноса производится ", ordFont));
                    paragraph.add(new Phrase("согласно Графику", ordBoldFont));
                    paragraph.add(new Phrase(", подписанному обеими сторонами, являющегося неотъемлемой частью настоящего договора. При этом последний взнос должен быть внесен не позднее 28го февраля, следующего года.  Размер родительского взноса не изменяется даже при условии перехода на дистанционную форму обучения.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.1.2.  Оплата ", ordFont));
                    paragraph.add(new Phrase("производится в сомах на банковский счет ", ordBoldFont));
                    paragraph.add(new Phrase("Лицея родителями или лицами, их заменяющими, не позднее 3 календарных дней с даты, указанной в официальном счете/invoice.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.1.3. Общая стоимость родительских взносов составляет ", ordFont));
                    paragraph.add(new Phrase(student.getCtr_k_oplate() + "", ordBoldFont));
                    paragraph.add(new Phrase(" долл.США, которая производиться строго в сомах на день оплаты по курсу НБКР.", ordFont));
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
                    paragraph.add(new Phrase("4.1. Ни одна из сторон не несет ответственности за полное или частичное неисполнение своих обязательств при возникновении обстоятельств, которые делают полностью или частично невозможным выполнением Договора одной из сторон, а именно: пожар, стихийное природное бедствие (землетрясение, наводнение и др.), война, военные действия всех видов, забастовка, блокада, эпидемия, изменение текущего Законодательства Кыргызской Республики и другие возможные обстоятельства непреодолимой силы, не зависящие от сторон, подписавших Договор.", ordFont));
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
                    paragraph.add(new Phrase("5.5. Стороны договорились, что деловая корреспонденция и иные документы, касающиеся настоящего Договора, отправленные и полученные посредством факсимильной, электронной связи или иным способом, позволяющим достоверно установить, что документ исходит от стороны по Договору, признаются имеющими юридическую силу.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.6. Настоящий Договор составлен в двух экземплярах на русском языке и подписан обеими сторонами. Оба экземпляра идентичны и имеют одинаковую юридическую силу. У каждой из сторон находится один экземпляр настоящего Договора.", ordFont));
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
                    text10.add(new Phrase("Лицей: " + student.getScl_name_ru(), ordFont));
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
                    text10.add(new Phrase("Директор Лицея: " + student.getScl_dir_f_name(), ordFont));
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
                                SystemSettings.is_main).getValue() == 1) {
                            text18.add(new Phrase("Контактный тел: ", ordFont));
                            text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.Phone)).getValue().toString(), ordFont));
                            text18.add(Chunk.NEWLINE);
                            text18.add(new Phrase("Адрес места жительства: ", ordFont));
                            text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.Address)).getValue().toString(), ordFont));
                            text18.add(Chunk.NEWLINE);
                            text18.add(new Phrase("Данные паспорта: ", ordFont));
                            text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.Passport)).getValue().toString(), ordFont));
                        }
                    }
                    text11.add(new Phrase("Ф.И.О. отца: " + f_name, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Ф.И.О. матери: " + m_name, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Ф.И.О. ученика: ", ordFont));
                    text11.add(new Phrase(student.getStud_sur_name() + " "
                            + student.getStud_name() + " " + student.getStud_middle_name(), ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Место работы отца: " + f_worplace, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Место работы матери: " + m_worplace, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(text18);
                    Tinfo.addCell(text11);
                    Tinfo.addCell(new Phrase("                             (М.П)", ordFont));
                    Tinfo.addCell(new Phrase("Подпись:", ordFont));

                    document.add(Tinfo);
                    document.add(new Paragraph(10, " "));

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
                    text15.add(new Phrase((SystemSettings.dFormat.format(student.getCtr_contract_sum()) + ""), ordBoldFont));
                    text15.add(new Phrase(" долларов США.", ordFont));
                    text15.add(Chunk.NEWLINE);
                    if (student.getCtr_debt() >= 0) {
                        text15.add(new Phrase("Долг с предыдущего года: ", ordFont));
                    } else {
                        text15.add(new Phrase("Переплата с предыдущего года: ", ordFont));
                    }
                    text15.add(new Phrase((SystemSettings.dFormat.format(student.getCtr_debt()) + ""), ordBoldFont));
                    text15.add(new Phrase(" долларов США.", ordFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Скидка: ", ordFont));
                    if (student.getCtr_discountPerc() != null) {
                        text15.add(new Phrase(student.getCtr_discountStr(), ordBoldFont));
//                        text15.add(new Phrase(" (" + student.getCtr_discountPerc() + ")", ordFont));
                    }
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Предоплата: ", ordFont));
                    text15.add(new Phrase(SystemSettings.dFormat.format(student.getCtr_init_payment()) + "", ordBoldFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Остаток: ", ordFont));
                    text15.add(new Phrase(SystemSettings.dFormat.format(student.getCtr_ttl_left_sum()) + "", ordBoldFont));
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
                        TContract.addCell(new Phrase(SystemSettings.df.format(((DateField) instPlanCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.Date)).getValue()).getValue()), ordFont));
                        TContract.addCell(new Phrase(((TextField) instPlanCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.Amount)).getValue()).getValue(), ordFont));
                        TContract.addCell(new Phrase("", ordFont));
                        TContract.addCell(new Phrase("", ordFont));
                        n += 1;
                    }
                    TContract.addCell(new Phrase("", ordFont));
                    TContract.addCell(new Phrase("Итого:", ordBoldFont));
                    TContract.addCell(new Phrase(SystemSettings.dFormat.format(student.getCtr_k_oplate()) + "", ordBoldFont));
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
