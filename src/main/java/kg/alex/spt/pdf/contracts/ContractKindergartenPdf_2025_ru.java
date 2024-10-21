package kg.alex.spt.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.dao.DbRelative;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.Messages;
import kg.alex.spt.utils.Decliner;
import kg.alex.spt.utils.Settings;
import kg.alex.spt.utils.money.WritableSummRuUSD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class ContractKindergartenPdf_2025_ru {

    static final Logger logger = LogManager.getLogger(ContractLisePdf_2025_ru.class);
    private final static String FONT_LOCATION = "/home/logo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/logo/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractKindergartenPdf_2025_ru(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
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
                Font ordBoldUnderlinedFont = new Font(baseFontBold, 10, Font.UNDERLINE);
                Font boldFont = new Font(baseFontBold, 11);
                Font boldUnderlinedFont = new Font(baseFontBold, 11, Font.UNDERLINE);
                Font font_header = new Font(baseFontBold, 11);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("ДОГОВОР № "
                        + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), font_header));
                spr.add(Chunk.NEWLINE);

                spr.add(new Phrase("между дошкольным образовательным учреждением и родителями ребенка.", font_header));
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
                paragraph.add(new Phrase("Детский сад при «Иссык-Кульском лицее имени Х. Карасаева» именуемый в дальнейшем \"Исполнитель\", в лице директора ", ordFont));
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
                paragraph.add(new Phrase(", действующего на основании Устава, с одной стороны, и ", ordFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getFullName() + ", ", ordBoldFont));
                paragraph.add(new Phrase("являющаяся(щийся) ", ordFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getRelativeDeclarative(), ordBoldFont));
                paragraph.add(new Phrase(" ребенка ", ordFont));
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
                paragraph.add(new Phrase(", проживающий по адресу ", ordFont));
                paragraph.add(new Phrase(st_info.getRelative().getAddress(), ordBoldFont));
                paragraph.add(new Phrase(", именуемый в дальнейшем \"Воспитанник(ца)\"", ordFont));
                paragraph.add(new Phrase(", совместно именуемые стороны  заключили настоящий договор о нижеследующем:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("1. Детский сад обязуется:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. Обеспечить безопасность ребенка во время его пребывания, защиту прав и достоинства ребенка, его эмоциональное благополучие.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.2. Обеспечить психическое и физическое развитие ребенка, учитывать и развивать его индивидуальные особенности.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.3. Организовывать деятельность ребенка в соответствии с его возрастом, индивидуальными особенностями, содержанием образовательной программы, обеспечивая его интеллектуальное, физическое и личностное развитие.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.4. Обучать Воспитанника по образовательной программе согласно государственному стандарту КР используя современные педагогические технологии и воспитательные программы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.5. Выполнять санитарные правила и нормы для дошкольных учреждений.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.6. Устанавливать график работы групп детского сада с понедельника по пятницу с 8.00 до 17.00 часов. Выходные дни: суббота, воскресенье и праздничные дни.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.7. Организовать сбалансированное питание, обеспечить соблюдение режима питания. Обеспечить Воспитанника питанием 2 раза в день и полдником.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.8. Регулярно информировать родителей о жизни и деятельности ребенка, его личностном развитии.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.9. Организовать предметно-развивающую среду, способствующую развитию ребенка.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.10. Разрешить родителям в период адаптации ребенка оставаться с ним необходимое время.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.11. Сотрудничать с родителями, проявлять уважение к их запросам.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.12. Обеспечить детей необходимыми для их развития пособиями и материалами для творчества.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.13. Сохранять место за ребенком в случае: его болезни, санитарно-курортного лечения, карантина, отпуска родителей и временного отсутствия родителя по уважительной причине (болезнь, командировки и прочее) при 100% оплате.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("2. Родители обязуются:", boldFont));
                document.add(paragraph);

                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.clear();
                paragraph.add(new Phrase("2.1. Своевременно вносить оплату за содержание Воспитанника в установленном размере до 5 числа каждого месяца.", ordFont));
                document.add(paragraph);
                WritableSummRuUSD convertToLetters = new WritableSummRuUSD();
                paragraph.clear();
                paragraph.add(new Phrase("2.2. Общая стоимость родительских взносов составляет ", ordFont));
                paragraph.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getContract()), ordBoldFont));
                paragraph.add(new Phrase(" (" + convertToLetters.numberToString(studentInfo.getContractInfo().getContract()).replace("ноль центов ", "США") + ")", ordBoldFont));
                paragraph.add(new Phrase(" за год.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3. Своевременно забирать ребенка.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4. Лично передавать и забирать ребенка у воспитателя или другого работника; в других случаях своевременно сообщать данные тех, кому родители доверяют приводить или забирать ребенка.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.5. Выполнять требования санитарных норм и правил для ДОУ (приводить ребенка в опрятном виде и должна быть сменная обувь и одежда).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.6. В течение одного дня извещать о болезни Воспитанника, информировать о предстоящем отсутствии Воспитанника по каким-либо причинам.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.7. Посещать родительские собрания и специально организованные встречи.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.8. Своевременно сообщать администрации о болезни ребенка; приводить ребенка только здоровым, без признаков болезни и недомогания, а после болезни предъявлять справку с ГСВ с разрешением посещать детское дошкольное учреждение.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.9. Обеспечивать канцелярскими товарами и одеждой.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.10. Своевременно сообщать об изменении контактных телефонов.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.11. Выполнять требования, относящиеся к организации жизнедеятельности ребенка.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.12. Месячная оплата 120 долларов США за посещение ребенком детского сада не зависит от пропущенных дней ребенком.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. Детский сад имеет право:", boldFont));
                document.add(paragraph);

                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.clear();
                paragraph.add(new Phrase("3.1. Вносить предложения по совершенствованию воспитания Воспитанника в семье.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2. Не принимать в группу Воспитанника с признаками простуды или другого заболевания без справки от врача, а также по причине отсутствия более трех дней без справки участкового врача ГСВ. Несвоевременно предоставленные справки считаются недействительными.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.3. Не принимать Воспитанника в детский сад в случае несвоевременной оплаты за его содержание.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.4. Отчислять Воспитанника из детского сада, если он отсутствует без уважительной причины более 5 дней.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.5. Требовать документы, удостоверяющие личность у лиц, забирающих Воспитанников. Не отдавать Воспитанника родителям в нетрезвом состоянии.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.6. Расторгнуть настоящий договор досрочно при систематическом невыполнении родителями своих обязательств, при этом оплаченные денежные средства не возвращаются.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. Родители имеют право", boldFont));
                document.add(paragraph);

                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.clear();
                paragraph.add(new Phrase("4.1. Принимать участие в работе Совета детского сада.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2. Вносить предложения по улучшению работы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.3. Высказывать замечания и пожелания администрации детского сада.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.4. В качестве пожертвований:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- оказывать материальную помощь для развития детского сада и группы;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- оказывать материальную помощь для ремонта детского сада;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.5. Оказывать посильную помощь по благоустройству и озеленению территории детского сада.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.6. Расторгнуть настоящий договор при условии предварительного уведомления об этом администрацию детского сада.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. Оплата услуг", boldFont));
                document.add(paragraph);

                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.clear();
                paragraph.add(new Phrase("5.1. Размер ежемесячной оплаты детскому саду на содержание Воспитанника составляет ", ordFont));
                paragraph.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getContract() / 9.0)
                        + " ДОЛЛАРОВ США.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.2. Оплата производится авансом Заказчиком с 1-го числа текущего месяца по 30-е число следующего месяца (т. е. сумма вносится за месяц вперед) перечислением денежных средств на расчетный счет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.3. Оплата производиться ежемесячно с 1 по 5 число. В случае несвоевременной оплаты Заказчики обязаны уплатить пеню в размере 1 % за каждый просроченный день.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.4. Вступительный взнос производится Заказчиком однократно и единовременно в момент подписания данного договора.", ordFont));
                document.add(paragraph);

                String[] temp = studentInfo.getYear().split("-");
                paragraph.clear();
                paragraph.add(new Phrase("5.5. Срок действия договора. Договор начинает действовать ", ordFont));
                paragraph.add(new Phrase("с «01» сентября " + temp[0] + " г. и действует по «30» июня " + temp[1] + " года.", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. Порядок расторжения договора", boldFont));
                document.add(paragraph);

                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.clear();
                paragraph.add(new Phrase("6.1. Договор может быть расторгнут по соглашению сторон.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("6.2. Сторона, решившая расторгнуть настоящий договор досрочно, обязана предупредить другую сторону не менее чем за 3 дня до момента расторжения.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("6.3. Договор может быть расторгнут администрацией детского сада в одностороннем порядке при невыполнении Заказчиком условий Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("6.4. Договор может быть продлен по согласованию сторон.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("6.5. За неисполнение либо ненадлежащее исполнение обязательств по настоящему Договору Исполнитель и Заказчик несут ответственность, предусмотренную законодательством КР и настоящим Договором.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("7. Заключительные положения", boldFont));
                document.add(paragraph);

                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.clear();
                paragraph.add(new Phrase("7.1. Условия настоящего договора распространяются на отношения между Учреждением и родителями (законными представителями) ", ordFont));
                paragraph.add(new Phrase("Воспитанника с «01» сентября " + temp[0] + " г. и действует по «30» июня " + temp[1] + " года.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.2. Настоящий Договор составлен в двух экземплярах, имеющих равную юридическую силу, по одному для каждой из Сторон.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.3. Стороны обязуются письменно извещать друг друга о смене реквизитов, адресов и иных существенных изменениях.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.4. Все споры и разногласия, которые могут возникнуть при исполнении условий настоящего Договора, Стороны будут стремиться разрешать путем переговоров.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.5. Споры, не урегулированные путем переговоров, разрешаются в судебном порядке, установленном законодательством КР.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                //document.newPage();
                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("8.  Адреса сторон и подписи", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                float[] table_info_colsWidth = {1.5f, 1f};
                PdfPTable table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);
                Paragraph text10 = new Paragraph();
                text10.add(new Phrase("Лицей: " + studentInfo.getSchool().getName_ru(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Адрес: " + studentInfo.getSchool().getAddress(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("ИНН: " + studentInfo.getSchool().getInn(), ordFont));
                text10.add(Chunk.NEWLINE);
                String[] banks = studentInfo.getSchool().getBank().split("<br>");
                String[] bankAccounts = studentInfo.getSchool().getBank_account().split("<br>");
                for (int i = 0; i < banks.length; i++) {
                    text10.add(new Phrase("Банк: " + banks[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Р/счет: " + bankAccounts[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                }
                text10.add(new Phrase("Тел.: " + studentInfo.getSchool().getPhone(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Директор детского сада: " + studentInfo.getDirector().getSurname() + " "
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
                                myUI.getMessage(Messages.FullName)).getValue().toString();
                        f_work_place = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.WorkPlace)).getValue().toString();
                    }
                    if ((Integer) obj == 2) {
                        m_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.FullName)).getValue().toString();
                        m_work_place = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.WorkPlace)).getValue().toString();

                    }
                    if ((Integer) relativeCont.getContainerProperty(obj,
                            Settings.is_main).getValue() == 1) {
                        text18.add(new Phrase("Контактный тел: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Phone)).getValue().toString(), ordFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("Адрес места жительства: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Address)).getValue().toString(), ordFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("Данные паспорта: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Passport)).getValue().toString(), ordFont));
                    }
                }
                text11.add(new Phrase("Ф.И.О. отца: " + f_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Ф.И.О. матери: " + m_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Ф.И.О. ученика: ", ordFont));
                text11.add(new Phrase(studentInfo.getStudent().getSurname() + " "
                        + studentInfo.getStudent().getName() + " " + studentInfo.getStudent().getMiddle_name(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Место работы отца: " + f_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Место работы матери: " + m_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(text18);
                table_info.addCell(text11);
                table_info.addCell(new Phrase(" (М.П)", ordFont));
                table_info.addCell(new Phrase("Подпись:", ordFont));

                document.add(table_info);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Примечание: при заключении договора при себе необходимо иметь следующие документы:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(50);
                paragraph.setIndentationRight(30);
                paragraph.add(new Phrase("- Паспорт", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- Справка с места жительства и состав семьи", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- Справка с ГСВ", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- Св-во о рождении ребенка", ordFont));
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
