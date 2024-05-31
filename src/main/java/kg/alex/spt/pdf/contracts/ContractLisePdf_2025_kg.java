package kg.alex.spt.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.dao.DbRelative;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class ContractLisePdf_2025_kg {

    static final Logger logger = LogManager.getLogger(ContractLisePdf_2025_kg.class);
    private final static String FONT_LOCATION = "/home/logo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/logo/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractLisePdf_2025_kg(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
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
                spr.add(new Phrase("Акы төлөнүүчү билим берүү кызматтарын көрсөтүү жөнүндө ", font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("КЕЛИШИМ № "
                        + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), font_header));
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
                table_date.addCell(new Phrase(studentInfo.getSchool().getCity() + " ш.", ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateKg.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                document.add(table_date);
                document.add(new Paragraph(10, " "));

                Paragraph paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Уставдын негизинде иш алып барган, мындан ары \"Лицей\" деп аталуучу \"" + studentInfo.getSchool().getName_kg()
                        + "\" билим берүү мекемесинин директору ", ordFont));
                String fullName = studentInfo.getDirector().getSurname() + " " + studentInfo.getDirector().getName();
                if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().equals("")) {
                    fullName += " " + studentInfo.getDirector().getMiddle_name();
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(" бир тараптан, Кыргыз Республикасынын \"Билим берүү жөнүндө\" Мыйзамына ылайык окуучунун кызыкчылыгында мындан ары \"Ата-эне\" деп аталуучу, ", ordFont));
                fullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                if (!studentInfo.getStudent().getMiddle_name().equals("")) {
                    fullName = fullName + " " + studentInfo.getStudent().getMiddle_name();
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(" \"Окуучусунун\" ата-энеси болгон ", ordFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getFullName(), ordBoldFont));
                paragraph.add(new Phrase(" экинчи тараптан төмөнкүлөр жөнүндө Келишим түзүштү:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("1. КЕЛИШИМДИН ПРЕДМЕТИ", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. Келишимдин предмети болуп ", ordFont));
                paragraph.add(new Phrase(studentInfo.getPeriod_kg(), ordBoldFont));
                paragraph.add(new Phrase(" окуучуну толук пансион шарттарында окутуу, тарбиялоо жана жашоо процессин уюштуруу, анын мамлекеттик билим берүү стандартынын жана жалпы орто мектептин программаларынын алкагында билим алуусу саналат.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("2. ТАРАПТАРДЫН УКУК ЖАНА МИЛДЕТТЕРИ", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1. Лицейдин милдеттери:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.1. Ушул Келишимдин 1.1-пунктунда каралган кызмат көрсөтүүлөрдүн талаптагыдай аткарылышын уюштуруу жана камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.2. Ушул Келишимдин предмети болуп саналган билим берүү программаларын окуучуларга өздөштүрүү максатында окуучуну Лицей белгилеген тартипте көрсөтүлүүчү жекече билим берүү жардамы менен камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.3. Окуу убагында окуучуга окуу китептерин жана окуу II, III баскычынын окуу планынын (керектүүсүнүн астын сызып) предметтери жана программалары боюнча лицейдин китепканасында болгон окуу китептерин жана башка адабияттарды берүү, анын карамагына жабдылган окуу кабинеттерин, компьютердик класстарды, китепкананы берүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.4. Объект ичиндеги жана өткөрүү режимдерин уюштурууну жүзөгө ашыруучу лицейдин түзүмдүк бөлүмдөрү менен өз ара аракеттенүүдө окуучунун коопсуздугун камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.5. Окуучуну окуу мезгилине, коммуналдык ыңгайлуулуктары бар ыңгайлуу жатаканага жайгаштыруу. Контракт бузулган учурда окуучунун жатаканада жашоо укугу жоголот.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.6. Окуучуну жаш өзгөчөлүктөрүнө ылайык 3 маал тамак менен камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.7. Лицейге алгачкы тапшырууда окуучуну бир жолу мектеп формасы менен камсыз кылуу, ал кийин зарылдыгына жараша ата-эненин эсебинен сатып алынууга тийиш.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.8. Жүйөлүү себептер боюнча сабактарды өткөрүп жиберген учурда окуучунун ордун сактоо.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.9. Кыргыз Республикасынын Билим берүү жана илим министрлиги бекиткен лицейдин окуу Планына ылайык, лицейдин Уставында белгиленген тартипте окуучунун жарым жылдык жана жылдык аттестациялоосун жүзөгө ашыруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.10. Кыргыз Республикасынын Билим берүү жана илим министрлигинин жобосунун негизинде лицейдин педагогикалык кеңешинин чечими боюнча белгиленген тартипте окуучуну кийинки класска которуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.11. Окуучу II баскычын ийгиликтүү аяктаган учурда күбөлүк, III баскычын аяктаганда - мамлекеттик үлгүдөгү аттестат берүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.12. Окуучунун инсандыгын урматтоо, физикалык жана психологиялык зомбулукка жол бербөө, окуучунун жеке өзгөчөлүктөрүн эске алуу менен анын адеп-ахлактык, дене-бой жана психологиялык ден соолугун, эмоционалдык бейпилдигин чыңдоо шарттарын камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.13. Медициналык квалификациясы документ менен тастыкталган лицейдин медициналык кызматкери тарабынан окуучунун ден соолугунун абалына учурдагы көзөмөлдү камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.14. Окуучуну билим берүү чөйрөсүнүн коопсуздугу жана саламаттыгын сактоо боюнча талаптар камтылган лицейдин ички локалдык актылары жана \"Ички тартип эрежелери\" менен тааныштыруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.15. Ар бир окуу чейрегинин жыйынтыгы боюнча ата-энеге окуучунун ден-соолугу, окуусу жана жүрүм-туруму жөнүндө маалымат берүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2. Лицейдин укуктары:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.1. Менюну жеке түзүү, тамактарды алмаштыруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.2. Жылдык чыгымдардын сметасына ылайык ата-энелердин төгүмдөрүн өз алдынча белгилөө жана өзгөртүү. Лицей акы төлөөдө берилген арзандатуунун өлчөмүн жыл сайын өзгөртүүгө укуктуу, мында берилген арзандатуулар ошол окуу жылынын ичинде гана колдонулат. Белгиленген арзандатуулар (жетишкендиги үчүн, олимпиаданын призеру, кабыл алуу сынагындагы жогорку балл үчүн жеңилдик, анын ичинде \"Сапат\" ЭБМ Башкы дирекциясы тарабынан берилген арзандатуулар ж.б.) окуучуда тартип жазасы болгон учурда эскертүүсүз ", ordFont));
                paragraph.add(new Phrase("жокко чыгарылат.", ordBoldFont));
                paragraph.add(new Phrase("Ата-эне тарабынан төлөмдүн графиги 3 жолу бузулган учурда, мектеп тарабынан берилген арзандатуулар ата-энени эскертүүсүз ", ordFont));
                paragraph.add(new Phrase("жокко чыгарылат.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.3. Ата-эне төгүмдү өз убагында төлөбөгөн учурда ата-энеге билдирүү менен окуучунун окуу жана окуудан тышкары сабактарынын бардык формаларына жана материалдык-техникалык шарттарды (сабактарды, китепканаларды, ашканаларды, ийримдерди, этюддарды ж.б. пайдалануу, сынактарга киргизбөө, \"Эдупэйдж\", \"Emektep\" тутумдарында баа койбоо) чектөө.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.4. Төмөнкү учурларда окуу акысынын ордун толтурбастан лицейдин педагогикалык кеңешинин чечими боюнча окуучуну лицейден чыгаруу: ", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("а) лицейдин жана жатакананын \"Ички тартип эрежелерин\" одоно, дайыма бузганда; (спирт ичимдиктерин, баңги каражаттарын, электрондук тамекилерди колдонуу, никотин камтыган каражаттарды колдонуу, кумар оюндарын ойноо, лицейдин аймагынан таанпис жана окуу сабактары учурунда уруксатсыз чыгып кетүү);", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("б) лицейдин теңтуштарына жана кызматкерлерине карата мыйзамга каршы аракеттер (спирт ичимдиктерин, баңги каражаттарын, электрондук тамекилерди колдонуу, никотин камтыган каражаттарды колдонуу, кумар оюндарын ойноо, лицейдин аймагын өзгөртүү жана окуу сабактары учурунда уруксатсыз калтыруу);", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("в) Кыргыз Республикасынын колдонуудагы мыйзамдары бузулганда. ((Кыргыз Республикасынын Кылмыш-жаза кодексинин 154-беренесинин негизинде жашы жете элек кызы/уулу никеге турууга мажбур болгон учурда ата-энеси жазык жоопкерчилигине тартылат).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.5. Саат 19:00дөн кийин окуучуну Жатаканага кабыл албоо.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.6. Окуучу \"Кыргыз Республикасынын мамлекеттик жана мамлекеттик эмес жалпы билим берүү уюмдарында бүтүрүүчүлөрдү Мамлекеттик жыйынтыктоочу аттестациялоо жана окуучуларды кийинки класска которуу тартиби жөнүндө Жобонун\" талаптарын аткарбаган учурда тиешелүү билими жөнүндө мамлекеттик үлгүдөгү документти берүүдөн баш тартуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.7. Жүйөлүү себепсиз/45 күндүк окууну өткөрүп жиберген учурда, окуучу кайрадан окуу курсуна калат (экинчи жылга).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.8. Эмгекке тарбиялоого компетенттүү мамиле кылуу максатында лицейдин администрациясы окуучуларды класстардагы жана жатаканалардагы өзүнүн жумуш ордун тазалоого тартууга укуктуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.9. Лицей каалоо боюнча жана ата-энелер менен макулдашуу боюнча ЖРТ, TOEFL, SAT, IELTS ж.б. даярдоо курстарын, окуучулардын кызыкчылыктары боюнча ийрим ишин өзүнчө акы менен өткөрүүнү уюштурат. Ушуга байланыштуу Лицейдин администрациясы окуучуларга Лицейден тышкары кошумча билим берүү сабактарына катышууга тыюу салууга укуктуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.10. Өнөкөт оорулар (эпилепсия, астма, жүрөк кемтиги, энурез ж.б.) боюнча диспансердик каттоодо турган окуучуларга өзгөчө шарттар берилбейт. Окуучулардын ата-энелери же мыйзамдуу өкүлдөрү окуучунун өнөкөт оорулары жөнүндө лицейдин медициналык кызматкерине жазуу жүзүндө эскертүүгө милдеттүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.11. Ата-эненин демилгеси боюнча ушул келишимди бузууда ата-эненин ", ordFont));
                paragraph.add(new Phrase("учурдагы чейреги үчүн ата-эненин төгүмүнүн суммасын ", ordBoldFont));
                paragraph.add(new Phrase("кармап калууга, мында мурда каралган арзандатуулар ", ordFont));
                paragraph.add(new Phrase("эсепке алынбайт.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.12. Ушул келишим күтүлбөгөн жагдайлар боюнча бузулганда, лицейдин бардык чыгымдарын эске алуу менен кайтарылууга тийиш болгон сумма лицейдин мүмкүнчүлүгүнө жараша, бирок кийинки жылдын май айынан кечиктирилбестен кайтарылып берилет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.13. Билим берүү процессин жана лицейдин ишин кабарлоо максатында окуучуну жана ата-энелерди кабардар кылбастан фото жана видеоматериалдарды өздөрүнүн интернет баракчаларына жана ЖМКга жайгаштырат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.14. Лицей КР мыйзамдарында каралган башка укуктарга ээ болушу мүмкүн.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3. Ата-эненин милдеттери:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.1 Ушул Келишимдин 1.1-пунктка ылайык, ушул Келишимдин 3.1.1-3.1.3 пунктунда көрсөтүлгөн мөөнөттө ата-энелердин төгүмдөрүн өз убагында төлөө.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.2. Ата-энелер жыл сайын ата-эненин төгүмүнүн ", ordFont));
                paragraph.add(new Phrase("20% алдын-ала төлөмүн ", ordBoldFont));
                paragraph.add(new Phrase("милдеттүү түрдө төлөө менен 1-апрелден 15-майга чейин кийинки окуу жылына келишим түзүүгө милдеттүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- Белгиленген мөөнөттө контракт түзүлбөгөн учурда, ", ordBoldFont));
                paragraph.add(new Phrase(" окуу орду башка окуучуларга берилет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.3. Окуучуну толук медициналык диспансерлештирүүнү жүргүзүү, лицейдин администрациясына окуучунун лицейге келээрдин алдында анын ден соолугунун абалы жөнүндө медициналык адистердин корутундусун берүү. Жазуу жүзүндө макулдук берүү же медициналык кызматтардан баш тартуу:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("а) муктаждык болгон учурда Окуучуга медициналык жардам көрсөтүү;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("б) психологдун кызматы;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("Кымбат баалуу дары-дармектерге же жеке медициналык тез жардамды чакырууга зарыл болгон учурда чыгымдарды төлөө.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.4. \"Окуучунун анкетасын\" (№_____тиркеме), \"Өз алдынча сейилдөөгө уруксатты\" (№_____тиркеме) жана \"Окуучунун ден соолугунун абалы жөнүндө туура маалымат берүү жөнүндө арызды\" ((№_____тиркеме) толтуруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.5. Окуучуларга лицейдин \"Ички тартип эрежелерин\" аткарууга көмөк көрсөтүү. Окуучу лицейдин \"Ички тартип эрежелерин\" бузган учурларда ага таасир этүүгө.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.6. Окуучуну өзүнүн колдонуусу үчүн бардык керектүү канцелярдык буюмдар менен камсыз кылуу (дептерлер, альбомдор, калемдер, түстүү карандаштар, курчуткучтар ж.б.).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.7. Окуучунун лицейдин мүлкүн бузууга же жок кылууга алып келген бардык аракеттери үчүн толук материалдык жоопкерчилик тартуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.8. Лицейдин администрациясынан расмий эсеп/билдирүү алынган күндөн тартып 7 календарлык күндүн ичинде лицейге келтирилген зыяндын ордун толтуруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.9. Лицейдин администрациясы менен туруктуу телефон байланышын сактап туруу:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- эки жумада бир жолудан кем эмес окуучунун учурдагы ден соолугунун абалы, класстан тышкаркы иш-чаралардын билим берүү программалары жөнүндө билүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.10. Балдарды лицейге алып келүүнү жана кайра алып кетүүнү ишке ашыруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.11. Лицейдин аймагынан тышкары окуучунун өмүрү жана коопсуздугу үчүн Лицей жоопкерчилик тартпайт.", ordFont));
                document.add(paragraph);


                paragraph.clear();
                paragraph.add(new Phrase("2.4. Ата-энелердин укуктары:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.1. Лицейдин администрациясынан ушул Келишимдин 2.1.1-2.1.14.-пункттарында баяндалган шарттарды аткарууну талап кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.2. 2.2.9, 2.2.10-пункттарга ылайык окуучунун лицейде болгон мезгилинде окуу жана жашоо наркынын ордун толтуруу менен келишимди мөөнөтүнөн мурда бузуу.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. АКЫ ТӨЛӨӨ ШАРТТАРЫ", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1. Лицейдин администрациясы тарабынан акы төлөөнүн төмөнкүдөй мөөнөттөрү белгиленген:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.1. Ата-эненин акысын төлөө ушул Келишимдин ажырагыс бөлүгү болуп саналган эки тарап тең кол койгон ", ordFont));
                paragraph.add(new Phrase("Графикке ылайык ", ordBoldFont));
                paragraph.add(new Phrase("жүргүзүлөт. Мында акыркы төгүм кийинки жылдын 28-февралынан кечиктирилбестен төлөнүүгө тийиш. Ата-эненин төгүмүнүн өлчөмү аралыктан окутуу формасына өткөн шартта да өзгөрбөйт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.2. Төлөм лицейдин ", ordFont));
                paragraph.add(new Phrase("банктык эсебине ", ordBoldFont));
                paragraph.add(new Phrase("сом менен, ата-энелер же аларды алмаштырган адамдар тарабынан расмий эсепте/invoice көрсөтүлгөн күндөн тартып 3 календардык күндөн кечиктирилбестен жүргүзүлөт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.3. Ата-эненин төгүмдөрүнүн жалпы наркы ", ordFont));
                paragraph.add(new Phrase(studentInfo.getContractInfo().getContract() + "", ordBoldFont));
                paragraph.add(new Phrase(" АКШ долларын түзөт, ал КРУБдун курсу боюнча төлөө күнүнө карата сомдо гана жүргүзүлөт.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. ФОРС-МАЖОР", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Тараптардын бири келишимди толук же жарым-жартылай аткарууга мүмкүн болбогон жагдайлар, атап айтканда: өрт, табигый кырсык (жер титирөө, суу ташкыны ж.б.), согуш, бардык түрдөгү аскердик аракеттер, иш таштоо, блокада, эпидемия, Кыргыз Республикасынын учурдагы мыйзамдарынын өзгөрүшү жана келишимге кол койгон тараптарга көз каранды болбогон ал жеткис күчтүн башка мүмкүн болгон жагдайлары келип чыкканда Тараптардын бири да өз милдеттенмелерин толук же жарым-жартылай аткарбагандыгы үчүн жоопкерчилик тартпайт.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. КЕЛИШИМДИН МӨӨНӨТҮ, ӨЗГӨРТҮҮ, КОШУМЧАЛОО ЖАНА ТОКТОТУУ ТАРТИБИ", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Ушул келишим ага эки тарап тең кол койгон учурдан тартып күчүнө кирет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.2. Ушул Келишим 2.2.3-2.4.2-пунктка ылайык мөөнөтүнөн мурда бузулушу мүмкүн.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.3. Ушул келишимге кандай гана болбосун толуктоолор жана өзгөртүүлөр алар жазуу жүзүндө жасалган жана эки тарап тең кол койгон шартта гана жарактуу болот.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.4. Ушул Келишим боюнча бардык пикир келишпестиктер тарабтар тарабынан сүйлөшүү тартибинде чечилет. Талаш-тартышты сүйлөшүүлөр жолу менен чечүү мүмкүн болбогон учурда алар Кыргыз Республикасынын мыйзамдарында белгиленген тартипте чечилет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.5. Тарабтар ушул келишимге тиешелүү, факсимилдик, электрондук байланыш аркылуу же документ келишим боюнча тараптан келип чыккандыгын так аныктоого мүмкүндүк берүүчү башка ыкма менен жөнөтүлгөн жана алынган ишкердик кат-кабарлар жана башка документтер юридикалык күчкө ээ деп тааныларын макулдашышты.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.6. Ушул Келишим кыргыз тилинде эки нускада түзүлгөн жана эки тарап тең кол койгон. Эки нуска бирдей жана бирдей юридикалык күчкө ээ. Тараптардын ар биринде ушул Келишимдин бир нускасы бар.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. ТАРАПТАРДЫН РЕКВИЗИТТЕРИ", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                float[] table_info_colsWidth = {1.5f, 1f};
                PdfPTable table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);
                Paragraph text10 = new Paragraph();
                text10.add(new Phrase("Лицей: " + studentInfo.getSchool().getName_kg(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Дареги: " + studentInfo.getSchool().getAddress(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("ИНН: " + studentInfo.getSchool().getInn(), ordFont));
                text10.add(Chunk.NEWLINE);
                String[] banks = studentInfo.getSchool().getBank().split("<br>");
                String[] bankAccounts = studentInfo.getSchool().getBank_account().split("<br>");
                for (int i = 0; i < banks.length; i++) {
                    text10.add(new Phrase("<Банк>: " + banks[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Эсеп: " + bankAccounts[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                }
                text10.add(new Phrase("Тел.: " + studentInfo.getSchool().getPhone(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Лицейдин директору: " + studentInfo.getDirector().getSurname() + " "
                        + studentInfo.getDirector().getName() + " " +
                        (studentInfo.getDirector().getMiddle_name() == null ?
                                "" : studentInfo.getDirector().getMiddle_name()), ordFont));
                text10.add(Chunk.NEWLINE);

                IndexedContainer relativeCont = null;
                table_info.addCell(text10);
                try {
                    DbRelative dbr = new DbRelative();
                    dbr.connect();
                    relativeCont = dbr.execSQL(myUI, studentInfo.getStudent().getId());
                    dbr.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                Paragraph text11 = new Paragraph();
                Paragraph text18 = new Paragraph();
                Iterator<?> iter = null;
                if (relativeCont != null) {
                    iter = relativeCont.getItemIds().iterator();
                }
                String f_name = "";
                String f_work_place = "";
                String m_name = "";
                String m_work_place = "";
                while (iter != null && iter.hasNext()) {
                    Object obj = iter.next();
                    if ((Integer) obj == 1) {
                        f_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.FullName)).getValue().toString();
                        f_work_place = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.WorkPlace)).getValue().toString();
                    }
                    if ((Integer) obj == 2) {
                        m_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.FullName)).getValue().toString();
                        m_work_place = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.WorkPlace)).getValue().toString();

                    }
                    if ((Integer) relativeCont.getContainerProperty(obj,
                            Settings.is_main).getValue() == 1) {
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
                text11.add(new Phrase(studentInfo.getStudent().getSurname() + " "
                        + studentInfo.getStudent().getName() + " " + studentInfo.getStudent().getMiddle_name(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Атасынын иштеген жери: " + f_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Апасынын иштеген жери: " + m_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(text18);
                table_info.addCell(text11);
                table_info.addCell(new Phrase(" (Мөөр)", ordFont));
                table_info.addCell(new Phrase("Колу:", ordFont));

                document.add(table_info);

                Paragraph text15 = new Paragraph();
                text15.setIndentationLeft(30);
                text15.setIndentationRight(30);
                text15.add(new Phrase("Төлөө графиги", boldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Окуучунун ID: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getLogin(), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Окуучунун аты, жөнү: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getSurname() + " "
                        + studentInfo.getStudent().getName() + " " + studentInfo.getStudent().getMiddle_name(), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Классы: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getClass_name(), ordBoldFont));
                text15.add(new Phrase(". Каттоо Датасы: ", ordFont));
                text15.add(new Phrase(Settings.df.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Төлөмдүн көлөмү: ", ordFont));
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getContract()) + ""), ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                text15.add(Chunk.NEWLINE);
                if (studentInfo.getContractInfo().getDebt() >= 0) {
                    text15.add(new Phrase("Мурунку жылдагы карыз: ", ordFont));
                } else {
                    text15.add(new Phrase("Мурунку жылдагы ашыкча төлөө: ", ordFont));
                }
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getDebt()) + ""), ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Жеңилдик: ", ordFont));
                if (studentInfo.getContractInfo().getDiscountStr() != null) {
                    text15.add(new Phrase(studentInfo.getContractInfo().getDiscountStr(), ordBoldFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Тууралоо: ", ordFont));
                if (studentInfo.getContractInfo().getCorrectionStr() != null) {
                    text15.add(new Phrase(studentInfo.getContractInfo().getCorrectionStr(), ordBoldFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Алдын ала төлөм: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()) + "", ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Калган төлөм: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()) + "", ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
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
                for (Object obj : instPlanCont.getItemIds()) {
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
                TContract.addCell(new Phrase("БАРДЫГЫ:", ordBoldFont));
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getNet()) + "", ordBoldFont));
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

        };

        String nameOf = "Contract";
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
