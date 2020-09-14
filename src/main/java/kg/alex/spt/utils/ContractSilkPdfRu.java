package kg.alex.spt.utils;

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

public class ContractSilkPdfRu {

    static final Logger logger = LogManager.getLogger(ContractSilkPdfRu.class);
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
    private IndexedContainer  relativeCont;
    SystemSettings sysSettings = new SystemSettings();
    private final static String FONT_LOCATION = "/home/logo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/logo/TimesNewRomanBold.ttf";

    public ContractSilkPdfRu(final MyVaadinUI ui, StudInfoPdf st_info,
            final IndexedContainer instPlanCont) {
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

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(
                            FONT_LOCATION2, BaseFont.IDENTITY_H,
                            BaseFont.NOT_EMBEDDED);
                    Font ordFont = new Font(baseFont, 8);
                    Font ordBoldFont = new Font(baseFontBold, 8);
                    Font boldFont = new Font(baseFontBold, 8);
                    Font ordBoldUndFont = new Font(baseFontBold, 8, Font.UNDERLINE);
                    Font ordBoldItaFont = new Font(baseFontBold, 8, Font.ITALIC);
                    Font font_header = new Font(baseFontBold, 8);

                    document.open();

                    document.add(new Paragraph(10, " "));

                    PdfContentByte punder = writer.getDirectContentUnder();

                    Paragraph spr = new Paragraph();
                    spr.add(new Phrase("ДОГОВОР", font_header));
                    spr.add(Chunk.NEWLINE);
                    spr.add(new Phrase("об оказании платных образовательных услуг",
                            font_header));
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

                    float[] Tdate_colsWidth = {5f, 1f};
                    PdfPTable Tdate = new PdfPTable(2);
                    Tdate.setWidthPercentage(85f);
                    Tdate.setWidths(Tdate_colsWidth);
                    Tdate.getDefaultCell().setBorder(0);
                    Tdate.addCell(new Phrase("г." + student.getScl_city(), ordFont));

                    if (aDate.before(cal.getTime())) {
                        Tdate.addCell(new Phrase(df.format(cal.getTime()), ordFont));
                    } else {
                        Tdate.addCell(new Phrase(df.format(aDate), ordFont));
                    }
                    document.add(Tdate);
                    document.add(new Paragraph(10, " "));

                    Paragraph text = new Paragraph();
                    text.setFirstLineIndent(30);
                    text.setIndentationLeft(30);
                    text.setIndentationRight(30);
                    text.setLeading(0, 1);
                    text.add(new Phrase("Международная школа «Силк Роад Интернешнл Скул», именуемая в дальнейшем «Школа», в лице директора ", ordFont));
                    text.add(new Phrase(student.getScl_dir_f_name(), ordBoldUndFont));
                    text.add(new Phrase(", действующего на основании Устава, с одной стороны, и ", ordFont));
                    text.add(new Phrase(student.getRel_fullname(), ordBoldUndFont));
                    text.add(new Phrase(", являющаяся (щийся) ", ordFont));
                    text.add(new Phrase(student.getRel_name_dec(), ordBoldUndFont));
                    text.add(new Phrase(" «Учащегося» ", ordFont));
                    text.add(new Phrase(student.getStud_sur_name() + " "
                            + student.getStud_name() + " " + student.getStud_middle_name(), ordBoldUndFont));
                    text.add(new Phrase(", именуемый в дальнейшем ", ordFont));
                    text.add(new Phrase("«" + student.getRel_name() + "»", ordBoldUndFont));
                    text.add(new Phrase(" с другой стороны, в интересах обучающегося, "
                            + "в соответствии с Законом Кыргызской Республики «Об образовании», заключили "
                            + "настоящий Договор о нижеследующем: ", ordFont));
                    document.add(text);
                    text.add(Chunk.NEWLINE);

                    document.add(new Paragraph(10, " "));
                    Paragraph text1 = new Paragraph();
                    text1.setIndentationLeft(30);
                    text1.setIndentationRight(30);
                    text1.setLeading(0, 1);
                    text1.add(new Phrase("1. ПРЕДМЕТ ДОГОВОРА", boldFont));
                    text1.add(Chunk.NEWLINE);
                    text1.add(new Phrase("1.1. Предметом Договора является организация процесса обучения и питания Учащегося на условиях, получение им образования в рамках государственного образовательного стандарта и программ соответствующего уровня образования, на период с ", ordFont));
                    text1.add(new Phrase("01.09." + student.getScl_year_name().substring(0, 4), ordBoldUndFont));
                    text1.add(new Phrase(" года по ", ordFont));
                    text1.add(new Phrase("01.09." + student.getScl_year_name().substring(5), ordBoldUndFont));
                    text1.add(new Phrase(" года.", ordFont));

                    document.add(text1);
                    document.add(new Paragraph(10, " "));

                    Paragraph text2 = new Paragraph();
                    text2.setIndentationLeft(30);
                    text2.setIndentationRight(30);
                    text2.setLeading(0, 1);
                    text2.add(new Phrase("2. ПРАВА И ОБЯЗАННОСТИ СТОРОН", boldFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.1. Школа обязана:", ordBoldUndFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.1.1. Организовать и обеспечить надлежащее исполнение процесса обучения, предусмотренных в пункте 1.1.настоящего Договора. ", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.1.2. В целях усвоения Учащимся образовательных программ, являющихся предметом настоящего Договора, обеспечить Учащегося индивидуальной помощью, оказываемой в порядке, установленном Школой. ", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.1.3. Предоставить Учащемуся на время обучения учебные кабинеты, компьютерный класс, библиотеку.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.1.4. Обеспечить Учащегося разовым питанием и легким завтраком.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.1.5. Осуществлять в установленном Школой порядке четвертную, полугодовую, годовую аттестацию Учащихся.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.1.6. Сохранить место за Учащимся в случае пропуска занятий по уважительным причинам.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.1.7. Переводить Учащихся в следующий класс в установленном порядке по решению педагогического совета Школы на основании Положения МОиН КР.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.1.8. Обеспечить текущий контроль за состоянием здоровья Учащихся медицинском работником Школы, медицинская квалификация которого подтверждена документально.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.1.9. Контролировать поведения Учащегося в период его пребывания в Школе, при условии неукоснительного соблюдения им «Правил Внутреннего Распорядка SRIS Handbook».", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2. Школа имеет право:", ordBoldUndFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2.1. Самостоятельно, с учетом государственных программ, выбирать, разрабатывать и применять учебные программы и методики в процессе обучения Учащегося.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2.2. Самостоятельно составлять меню, производить замены блюд.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2.3. Самостоятельно устанавливать и изменять размер родительских взносов согласно годовой смете расходов.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2.4. При несвоевременной оплате Родителем родительского взноса, без предупреждения Родителя ограничить доступ посещения Учащимся библиотеки, столовой, кружков и т.д. Помимо этого без предупреждения Родителя аннулируется предоставленные Школой скидки.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2.5. Отчислить Учащегося из Школы в следующих случаях:", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("          а) грубого, систематического нарушения «Правил Внутреннего Распорядка SRIS Handbook» с предоставлением документов, подтверждающих нарушения, совершенные Учащимся, по решению педагогического совета Школы;", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("          б) противозаконных действий по отношению к сверстникам и персоналу Школы;", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("          в) в случае систематической неуспеваемости по предметам, (даже после оказания методической помощи) по окончании учебного года;", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("          г) в случае неоплаты Родителем родительского взноса.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2.6. Требовать от Родителя компенсации стоимости причиненного ущерба имуществу Школы.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2.7. Самостоятельно перевести Учащегося в параллельную группу. ", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2.8. Установить порядок и сроки проведения промежуточной аттестации Учащихся.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2.9. Удержать сумму родительского взноса за текущую четверть при расторжении настоящего договора по инициативе Родителя.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2.10. При расторжении настоящего договора по непредвиденным обстоятельствам сумма подлежащее к возврату возвращается по мере возможности Школы, но не позднее мая следующего года.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.2.11. Не продлевать договор на обучение и питание с родителями, систематически нарушающих пункт 2.4.2 настоящего договора. ", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.3. Родители имеют право:", ordBoldUndFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.3.1. Требовать от Администрации Школы выполнения условий, изложенных в пунктах 2.1.1. – 2.1.11. настоящего Договора.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.3.2. На защиту законных прав и интересов детей", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.3.3. На ознакомление с ходом и содержанием образовательного процесса, а так же с оценками успеваемости.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.3.4. Досрочно расторгнуть Договор с возмещением стоимости обучения в период пребывания Учащегося в Школе в соответствии  с п.2.2.9. и  2.2.10.", ordFont));
                    text2.add(Chunk.NEWLINE);

                    text2.add(new Phrase("2.4. Родители обязаны:", ordBoldUndFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.1 Ежегодно с 1 апреля по 15 мая родители обязаны заключать контракт на следующий учебный год, с обязательным внесением 20% предоплаты родительского взноса. В случае не заключения контракта в указанные сроки учебное место будет предоставлено другим учащимся.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.2.Своевременно оплатить взнос за обучение и питание Учащегося в Школе согласно договору и графику, в сроки оговоренные в пунктах 3.1.-3.3. настоящего Договора. ", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.3. Обеспечить своего ребенка всеми необходимыми канцелярскими принадлежностями для собственного использования (тетради, цветные карандаши, точилки и т.д.).", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.4. Провести медицинскую диспансеризацию Учащегося, предоставить Администрации Школы заключение медицинских специалистов о состоянии здоровья Учащегося перед прибытием его в Школу. Предупредить о хронических заболеваниях Учащегося медицинского сотрудника Школы.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.5. Ознакомить Учащегося с «Правилами внутреннего распорядка SRIS Handbook». Обеспечить адекватное воздействие на Учащегося в случаях нарушения им «Правил внутреннего распорядка SRIS Handbook».", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.6. Нести полную материальную ответственность за все действия Учащегося, повлекшие за собой порчу или уничтожение имущества Школы.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.7. Возместить Школе стоимость нанесенного ущерба в течение 10 календарных дней со дня получения официального счета от администрации Школы.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.8. Участвовать во всех мероприятиях, проводимых Школой по поводу успеваемости, поведения, пропуска уроков, состояния здоровья Учащихся. (В случае отсутствия учащегося на занятиях по состоянию здоровья, на каждый день отсутствия предоставить медицинские справки; при отсутствии по другим уважительным причинам заранее поставить в известность Школу)", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.9. Поддерживать постоянную телефонную связь с Администрацией Школы: не реже одного раза в две недели интересоваться о текущем состоянии образовательных программ и внеклассных мероприятий.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.10. В случае изменения адреса и телефона Родитель обязан уведомить администрацию Школы.  ", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.11. Осуществлять подвоз детей в Школу и обратно.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    text2.add(new Phrase("2.4.12. Время занятий с 8.50 до 16.00. После завершения занятий Родители своевременно обязаны забрать своих детей. Школа несет ответственность только за время нахождения ученика в вышеуказанное время.", ordFont));
                    text2.add(Chunk.NEWLINE);
                    document.add(text2);
                    document.add(new Paragraph(10, " "));

                    Paragraph text3 = new Paragraph();
                    text3.setIndentationLeft(30);
                    text3.setIndentationRight(30);
                    text3.setLeading(0, 1);
                    text3.add(new Phrase("3. УСЛОВИЯ ВНЕСЕНИЯ РОДИТЕЛЬСКИХ ВЗНОСОВ", boldFont));
                    text3.add(Chunk.NEWLINE);
                    text3.add(new Phrase("3.1. «Родитель» обязан полностью оплатить общую сумму за согласованный период ", ordFont));
                    text3.add(new Phrase("согласно графику ", ordBoldFont));
                    text3.add(new Phrase("подписанному обеими сторонами, независимо от посещения «Учащегося» уроков", ordFont));
                    text3.add(Chunk.NEWLINE);
                    text3.add(new Phrase("3.2. Оплата за обучение производится в ", ordFont));
                    text3.add(new Phrase("начале каждой четверти, ", ordBoldFont));
                    text3.add(new Phrase("на банковский счет Школы, ученики за которых оплата не была произведена не будут допускаться к урокам.", ordFont));
                    text3.add(Chunk.NEWLINE);
                    text3.add(new Phrase("3.3. Общая стоимость обучения Учащегося за счет родительских взносов составляет ", ordFont));
                    text3.add(new Phrase(student.getCtr_k_oplate() + "", ordBoldUndFont));
                    text3.add(new Phrase(" долларов США, которая производиться строго в сомах на день оплату по курсу НБКР. Иностранные ученики по желанию оплату могут произвести в долларах США.", ordFont));
                    text3.add(Chunk.NEWLINE);
                    text3.add(new Phrase("3.4. Учебники, питание предоставляется бесплатно.", ordFont));
                    text3.add(Chunk.NEWLINE);
                    document.add(text3);
                    document.add(new Paragraph(10, " "));

                    Paragraph text3b = new Paragraph();
                    text3b.setIndentationLeft(30);
                    text3b.setIndentationRight(30);
                    text3b.setLeading(0, 1);
                    text3b.add(new Phrase("4. ФОРС-МАЖОР", boldFont));
                    text3b.add(Chunk.NEWLINE);
                    text3b.add(new Phrase("4.1. Ни одна из сторон не несет ответственности за полное или частичное неисполнение своих обязательств при возникновении обстоятельств, которые делают полностью или частично невозможным выполнением Договора одной из сторон, а именно: пожар, стихийное природное бедствие (землетрясение, наводнение и др.), война, военные действия всех видов, забастовка, блокада, изменение текущего Законодательства Кыргызской Республики и другие возможные обстоятельства непреодолимой силы, не зависящие от сторон, подписавших Договор.", ordFont));
                    document.add(text3b);
                    document.add(new Paragraph(10, " "));

                    Paragraph text4 = new Paragraph();
                    text4.setIndentationLeft(30);
                    text4.setIndentationRight(30);
                    text4.setLeading(0, 1);
                    text4.add(new Phrase("5. СРОК ДЕЙСТВИЯ ДОГОВОРА, ПОРЯДОК ИЗМЕНЕНИЯ, ДОПОЛНЕНИЯ И РАСТОРЖЕНИЯ", boldFont));
                    text4.add(Chunk.NEWLINE);
                    text4.add(new Phrase("5.1. Настоящий Договор вступает в силу с момента его подписания обеими сторонами.", ordFont));
                    text4.add(Chunk.NEWLINE);
                    text4.add(new Phrase("5.2. Настоящий Договор может быть расторгнут досрочно, согласно п.2.2.5. и 2.2.9.", ordFont));
                    text4.add(Chunk.NEWLINE);
                    text4.add(new Phrase("5.3. Любые дополнения и изменения к настоящему Договору действительны лишь при условии, что они совершены в письменной форме и подписаны обеими сторонами. ", ordFont));
                    text4.add(Chunk.NEWLINE);
                    text4.add(new Phrase("5.4. Все разногласия по данному Договору решаются сторонами в порядке переговоров. В случае невозможности разрешения спора путем переговоров, они решаются в порядке, установленном законодательством Кыргызской Республики.", ordFont));
                    text4.add(Chunk.NEWLINE);
                    text4.add(new Phrase("5.5. Стороны договорились, что деловая корреспонденция и иные документы, касающиеся настоящего Договора, отправленные и полученные посредством факсимильной, электронной связи или иным способом, позволяющим достоверно установить, что документ исходит от стороны по Договору, признаются имеющими юридическую силу.  ", ordFont));
                    text4.add(Chunk.NEWLINE);
                    text4.add(new Phrase("5.6. Настоящий Договор составлен в двух экземплярах на русском языке и подписан обеими сторонами. Оба экземпляра идентичны и имеют одинаковую юридическую силу. У каждой из сторон находится один экземпляр настоящего Договора.", ordFont));
                    text4.add(Chunk.NEWLINE);
                    text4.add(new Phrase("5.7. Срок действия настоящего договора один год.", ordFont));
                    text4.add(Chunk.NEWLINE);
                    document.add(text4);
                    document.add(new Paragraph(10, " "));

                    Paragraph text15 = new Paragraph();
                    text15.setIndentationLeft(30);
                    text15.setIndentationRight(30);
                    text15.setLeading(0, 1);
                    text15.add(new Phrase("График оплаты", boldFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Ф.И.О. Ученика: ", ordBoldFont));
                    text15.add(new Phrase(student.getStud_sur_name() + " "
                            + student.getStud_name() + " " + student.getStud_middle_name(), ordBoldUndFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Кл.: ", ordFont));
                    text15.add(new Phrase(student.getClass_name(), ordBoldUndFont));
                    text15.add(new Phrase(" Дата регистрации: ", ordFont));
                    text15.add(new Phrase(df.format(new Date()), ordBoldUndFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("ИТОГО взноса: ", ordFont));
                    text15.add(new Phrase((sysSettings.dFormat.format(student.getCtr_contract_sum()) + ""), ordBoldUndFont));
                    text15.add(new Phrase(" долларов США.", ordFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Скидка: ", ordFont));
                    if (student.getCtr_discountPerc() != null) {
                        text15.add(new Phrase(student.getCtr_discountStr(), ordBoldUndFont));
//                        text15.add(new Phrase(" (" + student.getCtr_discountPerc() + ")", ordFont));
                    }
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Предоплата: ", ordFont));
                    text15.add(new Phrase(sysSettings.dFormat.format(student.getCtr_init_payment()) + "", ordBoldUndFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Остаток: ", ordFont));
                    text15.add(new Phrase(sysSettings.dFormat.format(student.getCtr_ttl_left_sum()) + "", ordBoldUndFont));
                    document.add(text15);
                    document.add(new Paragraph(10, " "));

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

                    document.newPage();
                    Paragraph text9 = new Paragraph();
                    text9.setIndentationLeft(30);
                    text9.setIndentationRight(30);
                    text9.setLeading(0, 1);
                    text9.add(new Phrase("6. РЕКВИЗИТЫ СТОРОН", boldFont));
                    text9.add(Chunk.NEWLINE);
                    document.add(text9);
                    document.add(new Paragraph(10, " "));

                    float[] Tinfo_colsWidth = {1.5f, 1f};
                    PdfPTable Tinfo = new PdfPTable(2);
                    Tinfo.getDefaultCell().setBorder(0);
                    Tinfo.setWidthPercentage(90f);
                    Tinfo.setWidths(Tinfo_colsWidth);
                    Paragraph text10 = new Paragraph();
                    text10.setLeading(0, 1);
                    text10.add(new Phrase("Школа: " + student.getScl_name_ru(), ordBoldFont));
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

                    Tinfo.addCell(text10);
                    try {
                        DbRelative dbr = new DbRelative();
                        dbr.connect();
                        relativeCont = dbr.execSQLSilkRoad(myUI, student.getStud_id());
                        dbr.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    Paragraph text18 = new Paragraph();
                    text18.setLeading(0, 1);
                    Iterator iter = relativeCont.getItemIds().iterator();
                    while (iter.hasNext()) {
                        Object obj = iter.next();
                        text18.add(new Phrase("Родитель: ", ordBoldFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.FullName)).getValue().toString(), ordBoldFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("Адрес: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.Address)).getValue().toString(), ordFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("Тел: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(SptMessages.Phone)).getValue().toString(), ordFont));

                    }

                    Tinfo.addCell(text18);
                    Tinfo.addCell(new Phrase("                             (М.П)", ordFont));
                    Tinfo.addCell(new Phrase("Подпись:", ordFont));

                    document.add(Tinfo);

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
