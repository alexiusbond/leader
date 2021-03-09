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

public class ContractLisePdfKg {

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
    private StudInfoPdf student;
    SystemSettings sysSettings = new SystemSettings();
    private final static String FONT_LOCATION = "/home/logo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/logo/TimesNewRomanBold.ttf";

    public ContractLisePdfKg(final MyVaadinUI ui, StudInfoPdf st_info, final IndexedContainer instPlanCont) {
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

                    Decliner dcl = new Decliner();

                    Paragraph paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("Уставдын негизинде ишмердүүлүгүн жүргүзгөн “" + student.getScl_name_ru()
                            + "” билим берүү мекемесинин (мындан ары “Лицей” деп белгиленет) атынан директору ", ordFont));
                    paragraph.add(new Phrase(student.getScl_dir_f_name(), ordBoldFont));
                    paragraph.add(new Phrase(" биринчи тараптан жана ", ordFont));
                    paragraph.add(new Phrase(student.getRel_fullname(), ordBoldFont));
                    paragraph.add(new Phrase(" окуучунун ата-энеси (мындан ары“Ата-эне” деп белгиленет) экинчи тараптан ", ordFont));
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
                    paragraph.add(new Phrase("1.1. Окуучунун толук пансиондук шарттарына ылайык, окутуу процессин, тамактануу жана жатакана менен камсыз болууну уюштуруу, ", ordFont));
                    paragraph.add(new Phrase(student.getPeriod_kg(), ordBoldFont));
                    paragraph.add(new Phrase(" чейин мамлекеттик билим берүү стандартынын жана жалпы орто билим берүүчү мектептин программасынын алкагында билим алуусу Келишимдин предмети болуп саналат.", ordFont));
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
                    paragraph.add(new Phrase("2.1. Лицейдин милдеттери:", boldFont));
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
                    paragraph.add(new Phrase("2.1.2. Келишимдин предмети болгон билим берүүчү программаларынын Окуучу тарабынан өздөштүрүүсү максатында Лицей тарабынан белгиленген тартипте Окуучуга жеке билим берүү жардамын көрсөтүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.3. Окуучуну Лицейдин китепканасындагы окуу китептери жана окутуунун II, III баскычтарынын (керектүүсүнүн асты сызылат) предмет жана окуу планынын программасына тиешелүү башка адабияттар менен камсыз кылуу, Окуучуга жабдылган окуу кабинеттерин, компьютердик класс, китепкананы колдонууга берүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.4. Окуучуну алгач лицейге өткөндө бир жолу мектеп формасы камсыздоо мектеп тарабынан болот, муктаждык келип чыккан кийинки учурларда мектеп формасы ата-эне тарабынан сатып алынат.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.5. Окуучуну жаш өзгөчөлүгүнө ылайык 3 маал тамак менен камсыз кылуу.", ordFont));
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
                    paragraph.add(new Phrase("2.1.7. Кыргыз Республикасынын билим берүү жана илим министрлиги тарабынан бекитилген окуу планына ылайык, Лицейдин белгилеген тартибинде Окуучунун жарым жылдык жана жылдык аттестациядан өткөрүүнү ишке ашыруу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.8. Кыргыз Республикасынын билим берүү жана илим министрлигинин Жобосуна ылайык, Лицей белгилеген тартипте педагогикалык кеңештин чечими менен Окуучуну класстан класска көчүрүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.9. Окуучуга окутуунун II деңгээлин аяктаганда - күбөлүк, III деңгээлин аяктаганда белгиленген үлгүдөгү мамлекеттик аттестат берүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.10. Окуучунун жеке инсандыгына урмат көрсөтүү, физикалык жана психологиялык зордук көрсөтпөө, окуучунун жеке өзгөчөлүктөрүн эске алуу менен адеп-ахлактык, физикалык жана психологиялык ден соолугун, эмоционалдык бейпилдигин бекемдөө үчүн шарттарды камсыздоо.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.11. Окуучуну мектептин ички локалдык актылары жана “Ички тартип эрежелери” , билим берүү чөйрөсүнүн коопсуздугу жана саламаттыкты сактоо боюнча эрэжелерди сактоо менен тааныштыруу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.12. Окуучу тарабынан Лицейдин Уставы жана “Ички тартип Эрежелери” кынтыксыз аткарылган учурда, анын Лицейде жүргөн убактысында өмүрү жана ден соолугу үчүн жооп берүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1.13. Окуучунун ден соолугу, жетишкендиги жана жүрүш-турушу тууралуу ар бир чейректин жыйынтыгы боюнча Ата-энеге маалымат берүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.2 Лицейдин укуктары:", boldFont));
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
                    paragraph.add(new Phrase("2.2.2. Жылдык чыгашалар сметасына жараша ата-энелерден алынуучу акыларлын өлчөмүн өз алдынча белгилөө жана өзгөртүү. Педкеңештин чечимине ылайык окуу төлөмдөрүнө берилүүчү жеңилдиктердин көлөмүн өзгөртүүгө укуктуу. Берилген жеңилдиктер Окуучунун дисциплиналык жаза алган учурунда ", ordFont));
                    paragraph.add(new Phrase("жокко чыгарылат.", ordBoldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.3. Ата-эне тарабынан окуу төлөмдөрү (бул Келишимдин 3-пункту) убагында төлөнбөгөн учурда, Ата- энеге эскертүү менен Окуучуну окуудан четтетет. Муну менен катар, Ата-энеге эскертүү менен Лицей тарабынан мурда берилген жеңилдиктер ", ordFont));
                    paragraph.add(new Phrase("жокко чыгарылат.", ordBoldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.4. Окуучу кийинки учурларда окуу төлөмдөрүнүн кайтарымысыз мектептен окуудан чыгарылат  жана Эдупейдж тутумундагы бааларын окуучу көрө албайт жана сынактарга катыша албайт.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("а) Лицейдин “Ички тартип эрежелерин” орой, системалуу түрдө бузган учурда;", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("б) Лицейдин кызматкерлерине жана өз курактагы балдарга мыйзамсыз аракеттерди көрсөткөн учурда;", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("в) Кыргыз Республикасынын мыйзамдарын бузганда. (Кыргыз Республикасынын Жаза кодексинин 154-беренесинин негизинде жашы жете элек кызын /уулун үй-бүлө курууга мажбурлаган учурда Ата-энелер жооп тартат.)", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.5. “Кыргыз Республикасынын мамлекеттик жана мамлекеттик эмес жалпы орто билим берүү мекемелериндеги бүтүрүүчүлөрдүн мамлекеттик жыйынтыктоочу аттестациясы жана класстан класска көчүрүү тартиби тууралуу Жобонун” талаптарын аткарбаган учурда Окуучуга тиешелүү билими тууралуу мамлекеттик үлгүдөгү документти берүүдөн баш тартуу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.6. Өнөкөт оорулары (эпилепсия, астма, тубаса жүрөк оорусу, энурез ж.б.) боюнча диспансердик учетто турган окуууларга атайын шарттар түзүлбөйт.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.7. Ата-эненин каалоосу менен Келишим токтотулган учурда ", ordFont));
                    paragraph.add(new Phrase("окуп жаткан чейрек үчүн окуу төлөмүнүн суммасын ", ordBoldFont));
                    paragraph.add(new Phrase("кесип калуу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.8. Күтүүсүз себептер менен Келишим токтотулган учурда Ата-эне тарабынан төлөнгөн окуу акысынын калган бөлүгү Лицейдин мүмкүнчүлүгүнө жараша, бирок кийинки жылдын май айынан кеч эмес мөөнөттө төлөнөт.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.2.9. Бул келишим күтүлбөгөн жагдайлардын айынан бузулган учурда, кайтарыла турган сумма мектептин сарптаган бардык чыгымдарын эске алуу менен мектептин мүмкүнчүлүгүнө жараша, бирок кийинки жылдын май айына чейин кайтарылат. Мындай учурда бул пункт жеңилдиктерге ээ болгон окуучуларга тарабайт.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.3. Ата-эненин милдеттери:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.1. Бул келишимдин 1.1. пунктуна ылайык, Келишимдин 3.1.1.-3.1.3. пункттарында каралган мөөнөттөрдө, өз убагында окуу акыларын төлөө.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.2. Жыл сайын 1-апрелден 15-майга чейин ", ordFont));
                    paragraph.add(new Phrase("20% алдын ала төлөмдү ", ordBoldFont));
                    paragraph.add(new Phrase("сөзсүз төлөө менен кийинки окуу жылына Келишим түзүү. ", ordFont));
                    paragraph.add(new Phrase("Белгиленген мөөнөттө келишим түзүлбөгөн учурда", ordBoldFont));
                    paragraph.add(new Phrase(" окуучунун орду башка окуучуларга берилиши мүмкүн жана берилген жеңилдиктер алынып салынышы мүмкүн.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.3. Лицейге келерден мурда Окуучуну толугу менен медициналык кароодон өткөрүү, Лицейдин администрациясына Окуучунун саламаттыгы тууралуу медициналык адистердин жыйынтыктарын тапшыруу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.4. “Окуучунун анкетасын” (Тиркеме №1), “Өз алдынча чыгууга уруксат кагазын” (Тиркеме №2) жана “Окуучунун саламаттыгы тууралуу чынчыл маалыматтарды берүү арызын” (Тиркеме №3) толтуруу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.5. Окуучу тарабынан Лицейдин “Ички тартип эрежелерин” аткарууга көмөктөш болуу. Лицейдин “Ички тартип эрежелерин” бузган учурда Окуучуга таасир берүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.6. Окуучунун Лицейдин мүлкүнүн талкалануу же жок кылуусуна алып келген бардык аракеттери үчүн жоопкерчилигин мойнуна алуу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.7. Лицейге келтирилген зыянды Лицейдин администрациясы тарабынан жиберилген расмий эсепти/invoice алгандан кийин, 7 календардык күндүн ичинде төлөп берүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.8. Лицейдин администрациясы менен дайыма телефон байланышында болуп туруу:", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("- Эки жумада бир жолу Окуучунун саламаттыгы, окуу программалары, сабактан тышкаркы иш-чаралары тууралуу кызыкдар болуу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.3.9. Лицейге балдарды алып келүү жана алып кетүү.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.add(new Phrase("2.4. Ата-энелердин укуктары:", boldFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.1. Лицейдин администрациясынан Келишимдин 2.1.1.-2.1.13.-пункттарында белгиленген шарттарды аткаруусун талап кылуу.", ordFont));
                    document.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.4.2. Окуучунун Лицейде калган мөөнөттөрүн төлөө менен Келишимдин 2.2.9.-2.2.10.-пункттарына ылайык, Келишимди мөөнөтүнөн мурда токтотуу.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

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
                    paragraph.add(new Phrase("3.1. Лицейдин администрациясы тарабынан окуу акыларын төлөө мөөнөттөрү төмөндөгүдөй белгиленген:", ordFont));
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
                    paragraph.add(new Phrase("3.1.2.  Төлөмдөр ата-энелер, же алардын ордундагы кишилер тарабынан расмий эсеп/invoice келгенден кийин 3 календардык күн ичинде Лицейдин ", ordFont));
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
                    paragraph.add(new Phrase("4.1. Келишимге кол койго эки тараптын талаптарын аткарууга мүмкүнчүлүк бербеген : өрт, табият кырсыктары (жер титирөө, сел ж.б.), согуш, бардык түрдөгү аскердик аракеттер, иш таштоо, блокада, Кыргыз Республикасынын мыйзамдарынын өзгөрүшү жана ушуга байланыштуу Келишимге кол койгон тараптарга баш ийбеген окуялара үчүн эки тарап тең жооп бербейт.", ordFont));
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
                    paragraph.add(new Phrase("5.2. Бул Келишим 2.2.3 жана 2.4.2.-пункттарына ылайык мөөнөтүнөн мурда токтотулат.", ordFont));
                    document.add(paragraph);
                    paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(30);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("5.3. Бул Келишимге киргизилүүчү өзгөртүүлөр жана кошумчалар жазуу түрүндө белгиленип, эки тарап тарабынан кол коюлганда гана жарамдуу болот.", ordFont));
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
                    paragraph.add(new Phrase("5.6. Бул Келишим 2 нускада, кыргыз тилинде түзүлдү жана эки тарап тарабынан кол коюлду. Эки нускасы тең бирдей юридикалык күчкө ээ. Ар бир тарапта Келишимдин өз нускасы бар.", ordFont));
                    document.add(paragraph);
                    document.add(new Paragraph(10, " "));

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
                    text10.add(new Phrase("Лицей: " + student.getScl_name_ru(), ordFont));
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
                    text10.add(new Phrase("Лицейдин директору: " + student.getScl_dir_f_name(), ordFont));
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
                    document.newPage();

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
                    paragraph.add(new Phrase("Директор: ", ordBoldFont));
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
