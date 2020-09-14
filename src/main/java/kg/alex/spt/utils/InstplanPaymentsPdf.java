/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.domain.StudInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author eldiyar
 */
public class InstplanPaymentsPdf {

    static final Logger logger = LogManager.getLogger(InstplanPaymentsPdf.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    private Document document = null;
    Date aDate = new Date(System.currentTimeMillis());
    private StudInfoPdf st;
    SystemSettings sysSettings = new SystemSettings();

    public InstplanPaymentsPdf(final MyVaadinUI myUI, StudInfoPdf s, final IndexedContainer planCont,
            final IndexedContainer paymentsCont, final double ttl_inst, final double ttl_pay) {
        this.st = s;

        source1 = new StreamResource.StreamSource() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/logo/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/logo/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4, 10, 10, 60, 30);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    HeaderFooterPortrait event = new HeaderFooterPortrait(myUI, st.getScl_name_ru(), st.getScl_address(), st.getScl_phone());
                    writer.setPageEvent(event);

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                    Font fontBold = new Font(baseFontBold, 14);
                    Font ordFont = new Font(baseFont, 11);
                    Font ordFontBold = new Font(baseFontBold, 11);
                    Font tableFont = new Font(baseFont, 10);

                    document.open();

                    float[] Tdate_colsWidth = {3.5f, 1f};
                    PdfPTable Tdate = new PdfPTable(2);
                    Tdate.setWidthPercentage(90f);
                    Tdate.setWidths(Tdate_colsWidth);
                    Tdate.getDefaultCell().setBorder(0);
                    Tdate.addCell(new Phrase(" ", ordFont));
                    Tdate.addCell(new Phrase("Дата: " + sysSettings.df.format(aDate), tableFont));
                    document.add(Tdate);

                    Paragraph spr = new Paragraph(myUI.getMessage(SptMessages.InstpLanPaymentsReport)
                            + " (" + st.getYear() + ")", fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(24, " "));
                    document.add(spr);
                    document.add(new Paragraph(30, " "));
                    float[] Thead_colsWidth = {2f, 2f};
                    PdfPTable Thead = new PdfPTable(2);
                    Thead.setWidthPercentage(90f);
                    Thead.setWidths(Thead_colsWidth);

                    Thead.getDefaultCell().setBorder(0);
                    Thead.getDefaultCell().setFixedHeight(15f);
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.Student), ordFontBold));
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.Contract), ordFontBold));
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.Id)
                            + ": " + st.getStud_login(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.Contract)
                            + ": " + st.getCtr_contract_sum(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.Firstname)
                            + ": " + st.getStud_name(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.Discount)
                            + ": " + st.getCtr_discountStr(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.Surname)
                            + ": " + st.getStud_sur_name(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.PreviousYearDebt)
                            + ": " + st.getCtr_debt(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.ClassName)
                            + ": " + st.getStud_class_name(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.Net)
                            + ": " + st.getCtr_k_oplate(), ordFont));
                    Thead.addCell(new Phrase());
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.Paid)
                            + ": " + st.getCtr_paid(), ordFont));
                    Thead.addCell(new Phrase());
                    Thead.addCell(new Phrase(myUI.getMessage(SptMessages.Left)
                            + ": " + sysSettings.dFormat.format(st.getCtr_ttl_left_sum()), ordFont));
                    Thead.addCell(new Phrase());
                    if (st.getCtr_instplan_debt() > 0) {
                        Thead.addCell(new Phrase(myUI.getMessage(SptMessages.InstPlanDebt)
                                + ": " + sysSettings.dFormat.format(st.getCtr_instplan_debt()), ordFont));
                    } else {
                        Thead.addCell(new Phrase(myUI.getMessage(SptMessages.InstPlanDebt)
                                + ": 0.00", ordFont));
                    }

                    document.add(Thead);
                    document.add(new Paragraph(20, " "));

                    if (planCont == null && paymentsCont != null) {
                        //payments table
                        float[] Tpayments_colsWidth = {0.75f, 2.5f, 2.5f, 4f, 3.2f};
                        PdfPTable Tpayments = new PdfPTable(5);
                        Tpayments.setWidthPercentage(90f);
                        Tpayments.setWidths(Tpayments_colsWidth);
                        Tpayments.getDefaultCell().
                                setVerticalAlignment(Element.ALIGN_BOTTOM);
                        Tpayments.addCell(new Phrase(" №", ordFontBold));
                        Tpayments.addCell(new Phrase(myUI.getMessage(SptMessages.Date), ordFontBold));
                        Tpayments.addCell(new Phrase(myUI.getMessage(SptMessages.Amount), ordFontBold));
                        Tpayments.addCell(new Phrase(myUI.getMessage(SptMessages.WhoPaid), ordFontBold));
                        Tpayments.addCell(new Phrase(myUI.getMessage(SptMessages.PaymentCategoryType), ordFontBold));

                        Iterator iter1 = paymentsCont.getItemIds().iterator();
                        int y = 0;
                        if (paymentsCont.size() > 0) {
                            y = 1;
                        }
                        while (iter1.hasNext()) {
                            Object next = iter1.next();
                            Tpayments.addCell(new Phrase(new Phrase(y + "", tableFont)));
                            Tpayments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Date)).getValue().toString(), tableFont));
                            Tpayments.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            Tpayments.addCell(new Phrase(sysSettings.dFormat.format((Double) paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Amount)).getValue()), tableFont));
                            Tpayments.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            Tpayments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.WhoPaid)).getValue().toString(), tableFont));
                            Tpayments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.PaymentCategoryType)).getValue().toString(), tableFont));
                            y++;
                        }

                        Tpayments.addCell(new Phrase(" ", ordFontBold));
                        Tpayments.addCell(new Phrase(" ", ordFontBold));
                        Tpayments.addCell(new Phrase(myUI.getMessage(SptMessages.Total)
                                + ": " + sysSettings.dFormat.format(ttl_pay), ordFontBold));
                        Tpayments.addCell(new Phrase(" ", ordFontBold));
                        Tpayments.addCell(new Phrase(" ", ordFontBold));

                        document.add(Tpayments);
                    }
                    if (paymentsCont == null && planCont != null) {
                        //installment plan table
                        float[] Tplan_colsWidth = {0.75f, 3f, 3f};
                        PdfPTable Tplan = new PdfPTable(3);
                        Tplan.setWidthPercentage(90f);
                        Tplan.setWidths(Tplan_colsWidth);
                        Tplan.getDefaultCell().
                                setVerticalAlignment(Element.ALIGN_BOTTOM);
                        Tplan.addCell(new Phrase(" №", ordFontBold));
                        Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.Date), ordFontBold));
                        Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.Amount), ordFontBold));

                        Iterator iter = planCont.getItemIds().iterator();
                        int i = 0;
                        if (planCont.size() > 0) {
                            i = 1;
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            Tplan.addCell(new Phrase(i + "", tableFont));
                            Tplan.addCell(new Phrase(planCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Date)).getValue().toString(), tableFont));
                            Tplan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            Tplan.addCell(new Phrase(sysSettings.dFormat.format((Double) planCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Amount)).getValue()), tableFont));
                            Tplan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            i++;
                        }
                        Tplan.addCell(new Phrase(" ", ordFontBold));
                        Tplan.addCell(new Phrase(" ", ordFontBold));
                        Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.Total)
                                + ": " + sysSettings.dFormat.format(ttl_inst), ordFontBold));

                        document.add(Tplan);
                    }
                    if (planCont != null && paymentsCont != null) {
                        float[] Tbody_colsWidth = {2f, 4f};
                        PdfPTable Tbody = new PdfPTable(2);
                        Tbody.setWidthPercentage(90f);
                        Tbody.setWidths(Tbody_colsWidth);
                        Tbody.getDefaultCell().setBorder(0);
                        Tbody.addCell(new Phrase(myUI.getMessage(SptMessages.InstallmentPlan), ordFontBold));
                        Tbody.addCell(new Phrase(myUI.getMessage(SptMessages.Payments), ordFontBold));
                        //installment plan table
                        float[] Tplan_colsWidth = {0.75f, 3f, 3f};
                        PdfPTable Tplan = new PdfPTable(3);
                        Tplan.setWidthPercentage(100f);
                        Tplan.setWidths(Tplan_colsWidth);
                        Tplan.getDefaultCell().
                                setVerticalAlignment(Element.ALIGN_BOTTOM);
                        Tplan.addCell(new Phrase(" №", ordFontBold));
                        Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.Date), ordFontBold));
                        Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.Amount), ordFontBold));

                        Iterator iter = planCont.getItemIds().iterator();
                        int i = 0;
                        double ttl_plan = 0;
                        if (planCont.size() > 0) {
                            i = 1;
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            Tplan.addCell(new Phrase(i + "", tableFont));
                            Tplan.addCell(new Phrase(planCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Date)).getValue().toString(), tableFont));
                            Tplan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            Tplan.addCell(new Phrase(sysSettings.dFormat.format((Double) planCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Amount)).getValue()), tableFont));
                            Tplan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            i++;
                            ttl_plan += (Double) planCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Amount)).getValue();
                        }
                        Tplan.addCell(new Phrase(" ", ordFontBold));
                        Tplan.addCell(new Phrase(" ", ordFontBold));
                        Tplan.addCell(new Phrase(myUI.getMessage(SptMessages.Total)
                                + ": " + sysSettings.dFormat.format(ttl_plan), ordFontBold));

                        Tbody.addCell(Tplan);

                        //payments table
                        float[] Tpayments_colsWidth = {0.75f, 2.5f, 2.5f, 4f, 3.2f};
                        PdfPTable Tpayments = new PdfPTable(5);
                        Tpayments.setWidthPercentage(100f);
                        Tpayments.setWidths(Tpayments_colsWidth);
                        Tpayments.getDefaultCell().
                                setVerticalAlignment(Element.ALIGN_BOTTOM);
                        Tpayments.addCell(new Phrase(" №", ordFontBold));
                        Tpayments.addCell(new Phrase(myUI.getMessage(SptMessages.Date), ordFontBold));
                        Tpayments.addCell(new Phrase(myUI.getMessage(SptMessages.Amount), ordFontBold));
                        Tpayments.addCell(new Phrase(myUI.getMessage(SptMessages.WhoPaid), ordFontBold));
                        Tpayments.addCell(new Phrase(myUI.getMessage(SptMessages.PaymentCategoryType), ordFontBold));

                        Iterator iter1 = paymentsCont.getItemIds().iterator();
                        int y = 0;
                        double ttl_pay = 0;
                        if (planCont.size() > 0) {
                            y = 1;
                        }
                        while (iter1.hasNext()) {
                            Object next = iter1.next();
                            Tpayments.addCell(new Phrase(y + "", tableFont));
                            Tpayments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Date)).getValue().toString(), tableFont));
                            Tpayments.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            Tpayments.addCell(new Phrase(sysSettings.dFormat.format((Double) paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Amount)).getValue()), tableFont));
                            Tpayments.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            Tpayments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.WhoPaid)).getValue().toString(), tableFont));
                            Tpayments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.PaymentCategoryType)).getValue().toString(), tableFont));
                            y++;

                            if ((Integer) paymentsCont.getContainerProperty(next,
                                    sysSettings.payment_category_id).getValue() != 3) {
                                ttl_pay += (Double) paymentsCont.getContainerProperty(next,
                                        myUI.getMessage(SptMessages.Amount)).getValue();
                            } else {
                                ttl_pay -= (Double) paymentsCont.getContainerProperty(next,
                                        myUI.getMessage(SptMessages.Amount)).getValue();
                            }
                        }

                        Tpayments.addCell(new Phrase(" ", ordFontBold));
                        Tpayments.addCell(new Phrase(" ", ordFontBold));
                        Tpayments.addCell(new Phrase(myUI.getMessage(SptMessages.Total)
                                + ": " + sysSettings.dFormat.format(ttl_pay), ordFontBold));
                        Tpayments.addCell(new Phrase(" ", ordFontBold));
                        Tpayments.addCell(new Phrase(" ", ordFontBold));

                        Tbody.addCell(Tpayments);

                        document.add(Tbody);

                    }

                    document.add(new Paragraph(35, " "));
                    float[] T2_colsWidth = {2f, 2f};
                    PdfPTable T2 = new PdfPTable(2);
                    T2.setWidths(T2_colsWidth);
                    T2.setWidthPercentage(90);
                    T2.getDefaultCell().setBorder(0);
                    T2.getDefaultCell().
                            setHorizontalAlignment(Element.ALIGN_LEFT);
                    T2.addCell(new Phrase(myUI.getMessage(SptMessages.Accountent), ordFontBold));
                    T2.addCell(new Phrase(myUI.getMessage(SptMessages.Director), ordFontBold));
                    T2.addCell(new Phrase(st.getScl_accountent_fullname(), ordFont));
                    T2.addCell(new Phrase(st.getScl_dir_f_name(), ordFont));
                    document.add(T2);

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

        resource = new StreamResource(source1, "TokenReport"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "TokenReport", false);
    }
}
