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
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ContractSchoolPdfKg {

    static final Logger logger = LogManager.getLogger(ContractSchoolPdfKg.class);
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

    public ContractSchoolPdfKg(final MyVaadinUI ui, StudInfoPdf st_info, final IndexedContainer instPlanCont) {
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
                    Font ordFont = new Font(baseFont, 11);
                    Font ordBoldFont = new Font(baseFontBold, 11);
                    Font boldFont = new Font(baseFontBold, 12);
                    Font font_header = new Font(baseFontBold, 13);

                    document.open();

                    document.add(new Paragraph(10, " "));

                    PdfContentByte punder = writer.getDirectContentUnder();

                    Paragraph spr = new Paragraph();
                    spr.add(new Phrase("Акылуу билим берүү кызматын сунуу", font_header));
                    spr.add(Chunk.NEWLINE);
                    spr.add(new Phrase("боюнча КЕЛИШИМ", font_header));
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
                    Tdate.addCell(new Phrase(student.getScl_city() + " ш.", ordBoldFont));
                    Tdate.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    if (aDate.before(cal.getTime())) {
                        Tdate.addCell(new Phrase(sysSettings.df.format(cal.getTime()), ordBoldFont));
                    } else {
                        Tdate.addCell(new Phrase(sysSettings.df.format(aDate), ordBoldFont));
                    }
                    document.add(Tdate);
                    document.add(new Paragraph(10, " "));

                    Paragraph paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("Уставдын негизинде ишмердүүлүгүн жүргүзгөн ", ordFont));                                 
                    paragraph.add(new Phrase("“" + student.getScl_name_kg(), ordBoldFont));
                    paragraph.add(new Phrase(" (мындан ары “Мектеп” деп белгиленет) атынан директору ", ordFont));    
                    paragraph.add(new Phrase(student.getScl_dir_f_name(), ordBoldFont));
                    paragraph.add(new Phrase(" биринчи тараптан жана ", ordFont));
                    paragraph.add(new Phrase(student.getRel_fullname(), ordBoldFont));
                    paragraph.add(new Phrase(" окуучунун ата-энеси (мындан ары “Ата-эне” деп белгиленет) экинчи тараптан ", ordFont));
                    String fullname = student.getStud_sur_name() + " " + student.getStud_name();
                    if (!student.getStud_middle_name().equals("")) {
                        fullname = fullname + " " + student.getStud_middle_name();
                    }
                    paragraph.add(new Phrase(fullname, ordBoldFont));
                    paragraph.add(new Phrase(" окуучунун кызыкчылыгы үчүн Кыргыз Республикасынын “Билим берүү жөнүндө” мыйзамына ылайык төмөндөгү келишимди түзүштү:", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("1. Келишимдин предмети", boldFont));
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("1.1. Окуучунун окутуу процессин жана тамактануусун уюштуруу, ", ordFont));
                    paragraph.add(new Phrase(student.getPeriod_kg(), ordBoldFont));
                    paragraph.add(new Phrase(" чейин мамлекеттик билим берүү стандартынын жана тиешелүү билим берүү программасынын алкагында билим алуусу Келишимдин предмети болуп саналат.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2. Тараптардын укук жана милдеттери", boldFont));
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.1. Мектептин милдеттери:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.1. Келишимдин 1.1.-пунктунда каралган кызматтарды уюштуруу жана камсыз кылуу. Билим берүү кызматтары Кыргыз Республикасынын билим берүү жана илим министрлиги тарабынан бекитилген мамлекеттик билим берүү стандартына жана тиешелүү деңгээлдеги программасына ылайык көрсөтүлөт.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.2. Келишимдин предмети болгон билим берүү  программаларынын Окуучу тарабынан өздөштүрүүсү максатында  Мектеп тарабынан белгиленген тартипте Окуучуга жеке билим берүү жардамын көрсөтүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.3. Окуучуну Мектеп китепканасындагы окуу китептери жана окутуунун I, II баскычтарынын предмет жана окуу планынын программасына тиешелүү башка адабияттар менен камсыз кылуу, Окуучуга жабдылган окуу кабинеттерин, компьютердик класс, китепкананы колдонууга берүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.4. Жаш өзгөчөлүгүнө жараша Окуучуну экинчи эртең мененки, түшкү, түштөн кийинки жеңил тамак менен камсыз кылуу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.5. Мектеп тарабынан белгиленген тартипте Окуучуну чейректик, жарым жылдык, жылдык аттестациядан өткөрүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.6. Окуучунун себептүү келбей калган күндөрүндө Окуучунун ордун сактоо.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.7. Педагогикалык кеңештин чечимине ылайык, Мектептин белгиленген тартибинде Окуучуну класстан класска көчүрүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.8. Окуучунун ден-соолугун, күнүмдүк көзөмөлүн медициналык квалификациясы документалдык түрдө тастыкталган Мектептин медицина кызматкери тарабынан камсыздоо.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.9. Окуучунун Мектепте жүргөн учурунда «Күн тартибин»(Тиркеме№1), «Ички тартиптин»(Тиркеме№ 2) кынтыксыз аткарган учурда жүрүш-турушун көзөмөлдөө жана керектүү учурда ата-энеге билдирүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.10. Окуучунун ден соолугу, жетишкендиги жана жүрүш-турушу тууралуу ар бир чейректин жыйынтыгы боюнча Ата-энеге маалымат берүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.11. Окуучунун алгач бир жолу мектеп формасы менен камсыздоо Мектеп тарабынан болот, ал эми муктаждык келип чыккан кийинки учурларда мектеп формасы ата-эне тарабынан сатып алынат.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.12. Окуучунун  Мектептин ички локалдык актылары жана “Ички тартип эрежелери” , билим берүү чөйрөсүнүн коопсуздугу жана саламаттыкты сактоо боюнча эрежелерди сактоо менен тааныштыруу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.2 Мектептин укуктары:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.1. Өз алдынча тамак тизмесин түзүү, тамактын түрлөрүн алмаштыруу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.2. Жылдык чыгашалар сметасына жараша ата-энелерден алынуучу акылардын төлөмдөрдүн өлчөмүн өз алдынча белгилөө жана өзгөртүү. Мектеп мүдүрүнүн жана «Сапат» ЭББМ жетекчилигинин чечимине ылайык Мектептин жылсайын жеңилдик өлчөмдөрүн өзгөртүүгө укугу бар. Окуучунун тартип бузуулары болсо, берилген жеңилдиктер ", ordFont));
                    paragraph.add(new Phrase("жокко чыгарылат.", ordBoldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.3. Ата-эне тарабынан окуу төлөмдөрү (бул Келишимдин 3-пункту) убагында төлөнбөгөн учурда, Ата- энеге маалымдоо менен Окуучуну окуу жана окуудан сырткаркы иш-чараларга катышуусун, материалдык-техникалык шарттарды (сабактар, китепкана, ашкана, ийримдер, этюддар ж.б.), сынактарга киргизбөө, Эдупейж тутумуна байланышына чектөө коюлат. Муну менен катар, Ата-энеге эскертүүсүз Мектеп тарабынан мурда берилген жеңилдиктер ", ordFont));
                    paragraph.add(new Phrase("жокко чыгарылат.", ordBoldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.4. Окуучу кийинки учурларда Мектептен чыгарылат:", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("А) Мектептин “Ички тартип эрежелерин” орой, системалуу түрдө бузган учурда; бул тартип бузуулар Мектептин Педагогикалык кеңешинин чечими менен тастыкталган учурда;", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("Б) Окуу жылынын аягында предметтерден системалуу түрдө жетишпеген (методикалык жардам берилгенден кийин дагы) учурда;", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("В) Мектептин кызматкерлерине жана өз курактагы балдарга мыйзамсыз аракеттерди көрсөткөн учурда;", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("Г) Ата-энеси тарабынан окуу акысы төлөнбөгөн учурда.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.5. Окуучу тарабынанМектептин мүлкүнө зыян келтирген учурда анын баасы Ата-энеден төлөп берүүсүн талап кылуу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.6. Окуучуга жеңилдик бир жылдык гана берилет. Жеңилдик менен окуган окуучу окуудан жетишпей калган учурда же тартип бузуу катталган учурда жеңилдик өз күчүн жоготот, жетекчилик тешелүү чараларды көрөт.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.7. Окуучунун аралыктуу аттестациядан өтүү тартиби жана мөөнөтүн аныктоо.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.8. Ата-эненин каалоосу менен Келишим токтотулган кезде, ", ordFont));
                    paragraph.add(new Phrase("учурдагы чейрек үчүн окуу акысын кармап калуу, мында берилген жеңилдиктер эсепке алынбайт.", ordBoldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.9. Күтүүсүз себептер менен Келишим  токтотулган учурда Ата-эне тарабынан төлөнгөн окуу акысынын калган бөлүгү Мектептин мүмкүнчүлүгүнө жараша, бирок кийинки жылдын май айынан кеч эмес мөөнөттө төлөнөт.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.10. Келишимдин 2.4.2. пунктун системалуу түрдө бузган Ата-энелерменен келишимдин мөөнөтүн узартпоо.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.3. Ата-эненин укуктары:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.1. Бул Келишимдин 2.1.1.-2.1.9. пункттарындагы шарттарды аткаруусун Мектептен талап кылуу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.2. Балдарынын мыйзамдуу укук жана кызыкчылыктарын коргоо.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.3. Окутуу процессинин жүрүшү, ошондой эле жетишкендиктери менен таанышуу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.4. Ата-эненин милдеттери:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.1. Жыл сайын 1-апрелден 15-майга чейин ", ordFont));
                    paragraph.add(new Phrase("20% алдын ала төлөмдү", ordBoldFont));
                    paragraph.add(new Phrase(" сөзсүз төлөө менен кийинки окуу жылына Келишим түзүү.", ordFont));
                    document.add(paragraph);
                    
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("- белгиленген мөөнөттө келишим түзүлбөгөн учурда", ordBoldFont));
                    paragraph.add(new Phrase(" окуучунун орду башка окуучуларга берилет жана берилген жеңилдиктерден пайдалана албайт.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.2. Мектеп менен Ата-эненин ортосунда окуу акысы тууралуу графикке ылайык ата-энелер тарабынан төлөнүүчү акыны өз убагында төлөп туруу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.3. Мектепке келерден мурда Окуучуну толугу менен медициналык кароодон өткөрүү, Мектептин жетекчилигине Окуучунун саламаттыгы тууралуу медициналык адистердин жыйынтыктарын тапшыруу .", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.4. Медициналык кызматтарга жазуу түрүндө ", ordFont));
                    paragraph.add(new Phrase("макулдугун берүү же баш тартуу:", ordBoldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("А) окуучуга медициналык жардам көрсөтүү муктаждыгында (дары-дармек алуу)", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.5. Окуучунун өнөкөт оорулары тууралуу медпункт кызматкерлерине билдирүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.6. Окуучуну Мектептин “Күн тартиби” жана “Ички тартип эрежелери” менен тааныштыруу. “Күн тартибин” жана “Ички тартип эрежелерин” бузган учурда Окуучуга таасир берүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.7. Мектепке келтирилген зыянды Мектептин жетекчилиги тарабынан жиберилген расмий эсепти алгандан кийин, 10 календардык күндүн ичинде төлөп берүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.8. Окуучулардын жетишкендиги, жүрүш-турушу, окуучулардын сабакка келбей калган күндөрүнө, саламаттыгына байланыштуу иш-чаралардын бардыгына катышуу. (Ден-соолугуна байланыштуу окууга келбейкалган учурда , келбей калган ар бир күн үчүн медициналык аныктаманы алып келүү, башка себептерге байланыштуу келбеген учурда класс жетекчисине же жардамчы мугалимине маалымат берүү).", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.9. Мектеп жетекчилиги менен дайыма телефон байланышында болуп туруу :", ordFont));
                    document.add(paragraph);                   
                    
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("- эки жумада бир жолу Окуучунун саламаттыгы, окуу программалары, сабактан тышкаркы иш-чаралары тууралуу кызыкдар болуу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.10. Ата-энелер окуу китептеринин сакталышынан жооптуу. Китептер жоголуп кетсе, же жакшы сакталбаса, толук баасы төлөнөт.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.11. ", ordFont));
                    paragraph.add(new Phrase("Дарек менен телефондор алмашкан учурда Ата-эне Мектептин жетекчилигине кабар берүү.", ordBoldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.12. Мектепке балдарды алып келүү жана алып кетүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.13. Мектепте сабактар 8:30-16:45 ке чейин, жума күндөрү 16:00гө чейин болот. Сабактар бүткөндөн кийин Ата-энелер өз убагында алып кетүүгө милдеттүү. Мектеп окуучунун коопсуздугун жана ден-соолугу үчүн жогорудагы белгиленген гана мөөнөткө жоопкерчилик ала алат.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    paragraph.add(new Phrase("3. Акы төлөө шарттары", boldFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("3.1. Мектептин жетекчилиги тарабынан окуу акыларын төлөө мөөнөттөрү  төмөндөгүдөй белгиленген:", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.1.1. Ата-энелер тарабынан төлөнүүчү акы Келишимдин ажырагыс бөлүгү болгон эки тарап менен бирге кол коюлган ", ordFont));
                    paragraph.add(new Phrase("Графикке жараша", ordBoldFont));
                    paragraph.add(new Phrase(" жүргүзүлөт. Акыркы төлөм кийинки жылдын 28-февралынан кечиктирилбестен төлөнүшү зарыл.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.1.2.  Төлөмдөр ата-энелер, же алардын ордундагы кишилер тарабынан расмий эсеп келгенден кийин 3 календардык күн ичинде Мектептин ", ordFont));
                    paragraph.add(new Phrase("банк эсебине сом менен", ordBoldFont));
                    paragraph.add(new Phrase(" төлөнөт.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("3.1.3. Ата-энелердин төлөмү ", ordFont));
                    paragraph.add(new Phrase(student.getCtr_k_oplate() + "", ordBoldFont));
                    paragraph.add(new Phrase(" АКШ долларын түзөт, төлөмдөр төлөм жүргүзүлүүчү күнгө карата КР УБ курсу менен сом түрүндө төлөнөт.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    paragraph.add(new Phrase("4. Форс-мажор", boldFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("4.1. Келишимге кол койгон эки тараптын талаптарын аткарууга мүмкүнчүлүк бербеген : өзгөчө кырдаал, өрт, табият кырсыктары (жер титирөө, сел ж.б.), согуш, бардык түрдөгү аскердик аракеттер, иш таштоо, блокада, Кыргыз Республикасынын мыйзамдарынын өзгөрүшү жана ушуга байланыштуу Келишимге кол койгон тараптарга баш ийбеген окуялар үчүн эки тарап тең жооп бербейт.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("4.2. Бул келишим өзгөчө кырдаал учурларында же күтүлбөгөн жагдайлардын айынан бузулган учурда, кайтарыла турган сумма мектептин сарптаган бардык чыгымдарды эске алуу менен Мектептин мүмүкүнчүлүгүнө жараша, бирок кйинки жылдын май айына чейин кайтарылат. Мында бул пункт жеңилдиктерге ээ болгон окуучуларга жарабайт, тиешелүү эмес.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    paragraph.add(new Phrase("5. Келишимдин мөөнөтү, өзгөртүү, кошумчалоо жана токтотуу тартиби", boldFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.1. Бул Келишим эки тарап тең кол коюлган күндөн баштап күчүнө кирет.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.2. Бул Келишим 2.2.4 жана 2.2.8.-пункттарына ылайык мөөнөтүнөн мурда токтотулат.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.3. Бул Келишимге киргизилүүчү өзгөртүүлөр жана кошумчалар жазуу түрүндө белгиленип, эки тарап кол койгондон кийин гана күчүнө кирет.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.4. Бул Келишимге тиешелүү түшүнбөстүктөр сүйлөшүүлөр аркылуу чечилет. Талаштар сүйлөшүүлөр аркылуу чечилбеген учурда, Кыргыз Республикасынын мыйзамдарында белгиленген тартипте чечилет.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.5. Тараптар Келишимдин катышуучу тарабына ылайык экендигин тастыктай алуучу бири-бирине жиберилген иш корреспонденцияларынын факсимилдик, электрондук жана башка байланыш жолу менен жиберилгенде юридикалык күчкө ээ боло тургандыгы тууралуу макулдашышты.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.6. Бул Келишим 2 нускада, кыргыз тилинде түзүлдү жана эки тарап тарабынан кол коюлду. Эки нускасы тең бирдей юридикалык күчкө ээ. Ар бир тарапта Келишимдин бирден нускасы сакталат.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.7. Келишимдин мөөнөтү ", ordFont));
                    paragraph.add(new Phrase("бир жыл.", ordBoldFont));
                    document.add(paragraph);
                    document.newPage();

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    paragraph.add(new Phrase("6. Тараптардын реквизиттери", boldFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

                    float[] Tinfo_colsWidth = {1.5f, 1f};
                    PdfPTable Tinfo = new PdfPTable(2);
                    Tinfo.getDefaultCell().setBorder(0);
                    Tinfo.setWidthPercentage(90f);
                    Tinfo.setWidths(Tinfo_colsWidth);
                    Paragraph text10 = new Paragraph();
                    text10.add(new Phrase("Мектеп: " + student.getScl_name_kg(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Дареги: " + student.getScl_address(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("ИНН: " + student.getScl_inn(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("ОКПО: " + student.getScl_bank(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Эсеп: " + student.getScl_bank_account(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Тел.: " + student.getScl_phone(), ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Мектептин мүдүрү: " + student.getScl_dir_f_name(), ordFont));
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
                                sysSettings.is_main).getValue() == 1) {
                            text18.add(new Phrase("Тел номери: ", ordFont));
                            text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.Phone)).getValue().toString(), ordFont));
                            text18.add(Chunk.NEWLINE);
                            text18.add(new Phrase("Жашаган жери: ", ordFont));
                            text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.Address)).getValue().toString(), ordFont));
                            text18.add(Chunk.NEWLINE);
                            text18.add(new Phrase("Паспорт маалыматы: ", ordFont));
                            text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(SptMessages.Passport)).getValue().toString(), ordFont));
                        }
                    }
                    text11.add(new Phrase("Атасынын аты, жөнү: " + f_name, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Апасынын аты, жөнү: " + m_name, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Окуучунун аты, жөнү: ", ordFont));
                    text11.add(new Phrase(student.getStud_sur_name() + " "
                            + student.getStud_name() + " " + student.getStud_middle_name(), ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Атасынын иштеген жери: " + f_worplace, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(new Phrase("Апасынын иштеген жери: " + m_worplace, ordFont));
                    text11.add(Chunk.NEWLINE);
                    text11.add(text18);
                    Tinfo.addCell(text11);
                    Tinfo.addCell(new Phrase("                             (Мөөр)", ordFont));
                    Tinfo.addCell(new Phrase("Колу:", ordFont));

                    document.add(Tinfo);
                    
                    document.add(new Paragraph(10, " "));

                    Paragraph text15 = new Paragraph();
                    text15.setIndentationLeft(30);
                    text15.setIndentationRight(30);
                    text15.add(new Phrase("Төлөө графиги", boldFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Окуучунун аты, жөнү: ", ordFont));
                    text15.add(new Phrase(student.getStud_sur_name() + " "
                            + student.getStud_name() + " " + student.getStud_middle_name(), ordBoldFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Классы: ", ordFont));
                    text15.add(new Phrase(student.getClass_name(), ordBoldFont));
                    text15.add(new Phrase(" Каттоо Датасы: ", ordFont));
                    text15.add(new Phrase(df.format(new Date()), ordBoldFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Төлөмдүн көлөмү: ", ordFont));
                    text15.add(new Phrase((sysSettings.dFormat.format(student.getCtr_contract_sum()) + ""), ordBoldFont));
                    text15.add(new Phrase(" АКШ доллары.", ordFont));
                    text15.add(Chunk.NEWLINE);
                    if (student.getCtr_debt() >= 0) {
                        text15.add(new Phrase("Мурунку жылдагы карыз: ", ordFont));
                    } else {
                        text15.add(new Phrase("Мурунку жылдагы ашыкча төлөө: ", ordFont));
                    }
                    text15.add(new Phrase((sysSettings.dFormat.format(student.getCtr_debt()) + ""), ordBoldFont));
                    text15.add(new Phrase(" АКШ доллары.", ordFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Жеңилдик: ", ordFont));
                    if (student.getCtr_discountPerc() != null) {
                        text15.add(new Phrase(student.getCtr_discountStr(), ordBoldFont));
//                        text15.add(new Phrase(" (" + student.getCtr_discountPerc() + ")", ordFont));
                    }
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Алдын ала төлөм: ", ordFont));
                    text15.add(new Phrase(sysSettings.dFormat.format(student.getCtr_init_payment()) + "", ordBoldFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Калган төлөм: ", ordFont));
                    text15.add(new Phrase(sysSettings.dFormat.format(student.getCtr_ttl_left_sum()) + "", ordBoldFont));
                    document.add(text15);
                    document.add(new Paragraph(10, " "));

                    Paragraph text16 = new Paragraph();
                    text16.setIndentationLeft(30);
                    text16.setIndentationRight(30);
                    text16.add(new Phrase("1-таблица", ordBoldFont));
                    document.add(text16);
                    document.add(new Paragraph(10, " "));
                    text16.add(Chunk.NEWLINE);

                    float[] TContract_colsWidth = {1f, 4f, 4f, 4f, 4f};
                    PdfPTable TContract = new PdfPTable(5);
                    TContract.setWidthPercentage(90f);
                    TContract.setWidths(TContract_colsWidth);
                    TContract.addCell(new Phrase("№", ordBoldFont));
                    TContract.addCell(new Phrase("Төлөө күнү", ordBoldFont));
                    TContract.addCell(new Phrase("Суммасы", ordBoldFont));
                    TContract.addCell(new Phrase("Тастыктаган документ", ordBoldFont));
                    TContract.addCell(new Phrase("Колу ", ordBoldFont));
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
                    TContract.addCell(new Phrase("ЖАЛПЫ:", ordBoldFont));
                    TContract.addCell(new Phrase(sysSettings.dFormat.format(student.getCtr_k_oplate()) + "", ordBoldFont));
                    TContract.addCell(new Phrase("", ordFont));
                    TContract.addCell(new Phrase("", ordFont));

                    document.add(TContract);

                    paragraph = new Paragraph();
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.add(Chunk.NEWLINE);
                    paragraph.add(new Phrase("Ата-эненин колу: ", ordBoldFont));
                    document.add(paragraph);
                    document.add(new Paragraph(5, " "));
                    paragraph = new Paragraph();
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.add(new Phrase("Мүдүр: ", ordBoldFont));
                    document.add(paragraph);
                    document.add(new Paragraph(5, " "));
                    paragraph = new Paragraph();
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.add(new Phrase("Башкы эсепчи: ", ordBoldFont));
                    document.add(paragraph);
                    document.add(new Paragraph(5, " "));
                    paragraph = new Paragraph();
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.add(new Phrase("Мөөр", ordBoldFont));
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
