package kg.alex.spt.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.server.StreamResource;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.domain.EmployeeInfoPdf;
import kg.alex.spt.utils.Decliner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class ContractAcademicStuffPdf {

    static final Logger logger = LogManager.getLogger(ContractAcademicStuffPdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;
    private final EmployeeInfoPdf employeeInfo;

    private final static String FONT_LOCATION = "/home/logo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/logo/TimesNewRomanBold.ttf";

    public ContractAcademicStuffPdf(final MyVaadinUI myUi, EmployeeInfoPdf employeeInfo) {
        this.employeeInfo = employeeInfo;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 30, 30);
                document.setMargins(10, 10, 30, 30);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 9.5f);
                Font ordBoldFont = new Font(baseFontBold, 9.5f);
                Font boldFont = new Font(baseFontBold, 10);
                Font font_header = new Font(baseFontBold, 10.5f);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("Трудовой договор с педагогическим работником", font_header));
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
                table_date.addCell(new Phrase("г. " + this.employeeInfo.getSchool().getCity(), ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateRu.format(this.employeeInfo.getContract().getCreationDate()), ordBoldFont));
                document.add(table_date);
                document.add(new Paragraph(10, " "));

                Decliner dcl = new Decliner();

                Paragraph paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase(this.employeeInfo.getSchool().getName_ru()
                        + ", именуемый в дальнейшем «Сторона 1», в лице директора ", ordFont));
                String fullName = null;
                try {
                    boolean isFeminine = employeeInfo.getDirector().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(employeeInfo.getDirector().getSurname(), isFeminine)
                            + " " + dcl.DeclineNameGenitive(employeeInfo.getDirector().getName(), isFeminine, false);
                    if (!Objects.equals(employeeInfo.getDirector().getMiddle_name(), "")) {
                        fullName += " " + dcl.DeclinePatronymicGenitive(employeeInfo.getDirector().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                fullName = employeeInfo.getEmployeeSurname() + " " + employeeInfo.getEmployeeName();
                if (employeeInfo.getEmployeeMiddleName() != null) {
                    fullName += employeeInfo.getEmployeeMiddleName();
                }
                paragraph.add(new Phrase(", действующего на основании Устава, с одной стороны, и ", ordFont));
                paragraph.add(new Phrase(fullName + ", ", ordBoldFont));
                paragraph.add(new Phrase("именуемый в дальнейшем «Сторона 2», с другой стороны, заключили настоящий трудовой договор о нижеследующем:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("1. Общие положения", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. По настоящему трудовому договору Сторона 2 обязуется выполнять обязанности в должности преподавания: " +
                        dcl.DeclineNameGenitive(employeeInfo.getEmployeeBranch(), true, false)
                        + " Стороне 1 работу по вышеуказанной должности, а Сторона 1 обязуется обеспечивать ему необходимые условия труда.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.2. Местом работы по данному трудовому договору является Учреждение " + employeeInfo.getSchool().getName_ru() + ".", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.3. Дата начала работы по настоящему трудовому договору: "
                        + Settings.dateRu.format(employeeInfo.getContract().getFromDate()), ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.4. Срок действия настоящего трудового договора истекает "
                        + Settings.dateRu.format(employeeInfo.getContract().getTillDate()), ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2. Права и обязанности Стороны 2", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.1. Сторона 2 имеет право:", ordBoldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.1. выбирать и использовать методики обучения и воспитания, учебные пособия и материалы, учебники, методы оценки знаний учащихся в соответствии с государственными образовательными стандартами;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.2. на участие в разработке новых авторских программ, учебников и учебных пособий;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.3. на создание и использование новаторских методов и приемов, проведение научно-педагогического эксперимента, внедрение передового педагогического опыта в практику;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.4. повышать квалификацию в системе переподготовки и повышения квалификации;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.5. на защиту своего достоинства и профессиональной чести;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.6. на отдых и праздники, утвержденные Правительством КР и согласованные Стороной 1;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.7. обращаться к администрации Учреждения с заявлениями, которые подлежат к рассмотрению.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.2 Сторона 2 обязана:", ordBoldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.1. соблюдать действующее законодательство Кыргызской Республики, Устав Учреждения, правила внутреннего трудового распорядка и иные локальные нормативные акты Учреждения, в том числе нормы и правила по охране труда и технике безопасности;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.2. проводить обучение на высоком профессиональном уровне, обеспечивать усвоение учащимися учебной программы не ниже уровня государственных образовательных стандартов;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.3. проводить учебно-воспитательную работу в духе уважения к родителям, старшим, к культурно-историческим ценностям Кыргызстана, его государственному устройству, воспитывать бережное отношение к окружающей среде;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.4. утверждать уважение к принципам общечеловеческой морали: доброте, трудолюбию, гуманизму, патриотизму, правде, справедливости и толерантности;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.5. постоянно совершенствовать свои педагогические знания и мастерство;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.6. уважать честь и достоинство учащихся;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.7. готовить учащихся к сознательной жизни в духе взаимопонимания, мира, согласия между народами;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.8. содействовать развитию индивидуальных творческих способностей учащихся.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.9. соблюдать трудовую дисциплину;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.10. бережно относиться к имуществу Стороны 1, а также к любому чужому имуществу, возмещать материальный ущерб, причиненный Стороне 1, при необходимости заключить со Стороной 1 Договор о полной материальной ответственности;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.11. выполнять условия настоящего договора, должностную инструкцию;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.12. выполнять обязанности, вытекающие из индивидуального плана работы, расписания учебных занятий, своевременно и в полном объеме выполнять распоряжения администрации Учреждения;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.13. выполнять иные обязанности, установленные в надлежащем порядке;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.14. немедленно сообщать Стороне 1 о возникновении ситуации, представляющей угрозу жизни и здоровью людей, сохранности имущества Стороны 1.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.15. воздерживаться от прямого или косвенного участия в любой коммерческой или профессиональной деятельности, которая может спровоцировать конфликт интересов в отношении обязанностей Стороны 2 по настоящему договору, до окончания срока настоящего договора;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.16. не создавать чаты/группы в социальных мессенджерах с учащимися и их законными представителями, без согласования со Стороной 1;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.17. письменно информировать Сторону 1 о жалобах со стороны родителей и учащихся;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.18. не давать интервью журналистам СМИ о деятельности Учреждения и МОУ “Сапат”.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.19. в случае, если Сторона 2 допускает однократное грубое нарушение своих обязанностей или условий настоящего договора (каждое “События Грубого Нарушения”), Сторона 1, согласно статье 6, имеет право прекратить действие настоящего договора. Сторона 2 считается виновным в “Событии Грубого Нарушения”, если он/она:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(а) совершает действия, ущемляющие честь и достоинство учащихся и их законных представителей;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(б) при выполнении своих обязанностей и обязательств находится в состоянии алкогольного, наркотического или химического опьянения;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(в) совершает умышленную порчу или хищение имущества Сторона 1;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(г) раглашает коммерческую тайну Стороны 1 или любую конфиденциальную информацию без надлежащих полномочий;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(д) допускает нарушение правил безопасности, повлекшее тяжкие последствия, включая травмы и аварии;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(е) совершает дейстия, противоречащие миссии, а также наносящие ущерб интересам Стороны 1.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(ж) применяет, в том числе однократно, методы воспитания, связанные с физическим и (или) психическим насилием над личностью обущающегося;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(з) передает третьим лицам контактные данные учащихся и их законных представителей без согласования со Стороной 1.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(и) допускает получение со стороны родителей, либо лиц их заменяющих подарки, денежное вознаграждение и.т.д.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.20. При заключении трудового договора Сторона 2 должна предоставить все необходимые документы:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- трудовую книжку,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- копию паспорта,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- нотариально заверенную копию диплома,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- заявление о приеме на работу,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- фотографию 3x4, 2 шт.,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- характеристику с прежнего места работы (по необходимости)", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- санитарную книжку, подтверждающую отсутствие у Стороны 2 опасных инфекционных и других заболеваний", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- справку о неимении судимости.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. Права и обязанности Стороны 1", boldFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("3.1. Сторона 1 имеет права, предусмотренные законами и иными нормативными актами Кыргызской Республики о труде и образовании, Уставом Учреждения и другими локальными нормативными актами, настоящим трудовым договором.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2 Сторона 1 обязана:", ordBoldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2.1 обеспечить условия для безопасного и эффективного труда Стороны 2;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2.2 оборудовать рабочее место Стороны 2 в соответствии с правилами охраны труда и техники безопасности;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2.3 своевременно выплачивать Стороне 2 обусловленную настоящим договором заработную плату.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. Режим труда и отдыха", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Стороне 2 устанавливается режим труда и отдыха в соответствии с правилами внутреннего трудового распорядка Учреждения.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.2. Особенности рабочего времени определяется в соответствии с правилами внутреннего трудового распорядка Стороны 1.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.3. В своей работе Сторона 2 подчиняется Стороне 1, своевременно предоставляет все отчеты и необходимую информацию, запрашиваемую администрацией Учреждения.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.4. В случае болезни или опоздания Сторона 2 обязана за 2 часа до начала работ сообщить директору или заместителю директора по учебной части, во избежание срывов учебного процесса.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. Условия оплаты труда", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Стороне 2 выплачиваются:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("а) месячный должностной оклад в размере "
                        + Settings.dFormat2.format(employeeInfo.getContract().getSalary()) + " сом.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.2. Стороне 2 могут быть установлены иные выплаты, в том числе стимулирующие. Размеры, сроки и источники указанных выплат определяются приказом директора Учреждения.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.3. Прекращение, изменение размера и сроков выплат, установленных приказом директора Учреждения и не предусмотренных п. 5.1. настоящего договора и правовыми нормативными актами Кыргызской Республики как обязательные, производится приказом директора Учреждения без согласования со Стороной 2.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. Прекращение трудового договора", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.1. Настоящий договор расторгается по истечении срока его действия, предусмотренного в п.1.4. настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.2. Сторона 1 вправе расторгнуть настоящий договор, предварительно письменно уведомив об этом Сторону 2, за 2 (две) недели до расторжения данного договора, в случае:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(35);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- при наступлении “События Грубого Нарушения”,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(35);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- если Сторона 2 систематически нарушает свои обязательства и права Стороны 1.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.3. Настоящий договор может быть расторгнут досрочно по обоюдному письменному согласию Сторон, а также при ненадлежащем выполнении Стороной 2 своих обязанностей, согласно данного договора.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.4. Сторона 2 вправе расторгнуть настоящий договор досрочно, предварительно письменно уведомив об этом Сторону 1 за  (1) четверть до даты расторжения.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("7. Конфиденциальность", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.1. Под “конфиденциальной информацией” подразумевается вся информация, касающаяся Сторона 1, включая, без ограничения, закрытую информацию по деятельности и финансовому положению Сторона 1, о клиентах и поставщиках, коммерческую тайну Стороны 1, “ноу-хау” технологиях, а также размера вознаграждения, выплачиваемого Стороне 2 во время действия настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.2. Сторона 2 не имеет права разглашать, сообщать и/или намеренно позволять любому лицу получить конфиденциальную информацию на протяжении срока действия настоящего договора и двенадцати (12) месяцев после прекращения действия настоящего договора, если иное не предусмотрено действующим законодательством КР.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.3. Если Сторона 2 полагает, но не уверен в том, что определенная информация конфиденциальная, он обязан спросить у Стороны 1 разрешения, прежде чем разглашать такую информацию.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.4. После прекращения действия настоящего договора Сторона 2 обязано сдать Стороне 1 все книги, отчеты, документы и иное имущество, находящееся в его владении и/или хранении. Сторона 2 не должен делать копии таких документов или имущества ни для каких целей.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("8. Интеллектуальная собственность", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("8.1. Сторона 2, заключая настоящий договор, соглашается с тем, что любая интеллектуальная собственность (изобретения, патенты, технологии, “ноу-хау”, коммерческие секреты, авторские права, товарные знаки, фирменные наименования, программное обеспечение, шаблоны, образцы, технические данные, проекты, программы, формулы, гарантии качества, экспериментальные данные, а также любая иная информация, приобретенная в период действия настоящего договора), созданная Стороной 2 в процессе работы оказания услуг Стороне 1, является исключительной собственностью Стороны 1.  Сторона 2 передает все права на такую собственность Стороне 1.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("9. Заключительные положения", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.1. Трудовые споры из настоящего трудового договора разрешаются в порядке, установленном законодательством Кыргызской Республики о труде.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.2. Настоящий трудовой договор составлен в двух экземплярах, имеющих одинаковую юридическую силу. Один экземпляр хранится у Стороны 1, второй - у Стороны 2.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.3. Условия настоящего трудового договора могут быть изменены только по соглашению Сторон в письменной форме.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                document.newPage();
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("10. Реквизиты и подписи сторон", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));


                float[] table_info_colsWidth = {1f, 1f};
                PdfPTable table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.getDefaultCell().setPaddingRight(10);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);
                table_info.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                table_info.addCell(new Phrase("Сторона 1:", boldFont));
                table_info.addCell(new Phrase("Сторона 2:", boldFont));
                table_info.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                Paragraph text10 = new Paragraph();
                text10.add(new Phrase(this.employeeInfo.getSchool().getName_ru(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Адрес: ", ordBoldFont));
                text10.add(new Phrase(this.employeeInfo.getSchool().getAddress(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Директор: ", ordBoldFont));
                text10.add(new Phrase(this.employeeInfo.getDirector().getSurname() + " "
                        + employeeInfo.getDirector().getName() + " " +
                        (employeeInfo.getDirector().getMiddle_name() == null ?
                                "" : employeeInfo.getDirector().getMiddle_name()), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                table_info.addCell(text10);

                Paragraph text11 = new Paragraph();
                text11.add(new Phrase("Ф.И.О. ", ordBoldFont));
                text11.add(new Phrase(fullName, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Паспорт: № ", ordBoldFont));
                text11.add(new Phrase(employeeInfo.getContact().getPassport(), ordFont));
                text11.add(new Phrase(" выдан ", ordBoldFont));
                text11.add(new Phrase(employeeInfo.getContact().getPassportGiven(), ordFont));
                text11.add(new Phrase(" от ", ordBoldFont));
                text11.add(new Phrase(Settings.df.format(employeeInfo.getContact().getPassportDate()), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Место жительства: ", ordBoldFont));
                text11.add(new Phrase(employeeInfo.getContact().getAddress(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Тел.: ", ordBoldFont));
                text11.add(new Phrase(employeeInfo.getContact().getPhoneNumbers(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                table_info.addCell(text11);

                table_info.addCell(new Phrase("Подпись.: ________________________", ordBoldFont));
                table_info.addCell(new Phrase("Подпись.: ________________________", ordBoldFont));
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

        String nameOf = "Contract";
        StreamResource resource = new StreamResource(source1, nameOf
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUi.getPage().open(resource, nameOf, false);
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
