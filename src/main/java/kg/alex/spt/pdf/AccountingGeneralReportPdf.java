package kg.alex.spt.pdf;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.ImgTemplate;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.domain.ContractTotal;
import kg.alex.spt.domain.SchoolAccounting;
import kg.alex.spt.domain.StudentInfoPdf;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.FormattedTable;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;

public class AccountingGeneralReportPdf {

    static final Logger logger = LogManager.getLogger(AccountingGeneralReportPdf.class);
    private byte[] b = null;
    private StreamResource.StreamSource source1 = null;
    ByteArrayOutputStream buffer = null;
    StreamResource resource = null;
    private Document document = null;
    private String svgStrPayments, svgStrDiscounts, svgStrPaid;
    private PdfWriter writer;
    Date aDate = new Date(System.currentTimeMillis());
    private SchoolAccounting sclAccInfo;
    private FormattedTable accTransactionsTable, paymentsTable;
    private ContractTotal contrTtl;

    public AccountingGeneralReportPdf(final MyVaadinUI myUI, String svgPms, String svgPaid, String svgDisc,
            SchoolAccounting schoolAcc, FormattedTable transactionsTable, ContractTotal contractTtl,
            FormattedTable pTable, final int scl_id, final String year, final String prevDay) {
        svgStrPayments = svgPms;
        svgStrDiscounts = svgDisc;
        svgStrPaid = svgPaid;
        sclAccInfo = schoolAcc;
        accTransactionsTable = transactionsTable;
        contrTtl = contractTtl;
        paymentsTable = pTable;
        source1 = new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {
                    


                    document = new Document(PageSize.A4, 10, 10, 10, 10);
                    writer = PdfWriter.getInstance(document, buffer);
                    StudentInfoPdf st = new StudentInfoPdf();
                    try {
                        DbSchool dbs = new DbSchool();
                        dbs.connect();
                        st = dbs.execGetSchoolPdf(scl_id);
                        dbs.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    HeaderFooterPortrait event = new HeaderFooterPortrait(myUI, st.getScl_name_ru(), st.getScl_address(), st.getScl_phone());
                    writer.setPageEvent(event);

                    final String FONT_LOCATION = "/home/logo/PT_Sans-Web-Regular.ttf";
                    final String FONT_LOCATION_BOLD = "/home/logo/PT_Sans-Web-Bold.ttf";

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFont_bold = BaseFont.createFont(FONT_LOCATION_BOLD,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    Font title_font = new Font(baseFont_bold, 14, Font.NORMAL);
                    Font normal_font = new Font(baseFont, 8, Font.NORMAL);
                    Font normal_bold_font = new Font(baseFont_bold, 8, Font.NORMAL);
                    Font date_normal_font = new Font(baseFont, 10, Font.NORMAL);
                    Font caption_bold_font = new Font(baseFont_bold, 10, Font.NORMAL);

                    document.open();
                    //дата
                    document.add(new Paragraph(45, " "));
                    float[] Tdate_colsWidth = {4f, 1f};
                    PdfPTable Tdate = new PdfPTable(2);
                    Tdate.setWidthPercentage(90f);
                    Tdate.setWidths(Tdate_colsWidth);
                    Tdate.getDefaultCell().setBorder(0);
                    Tdate.addCell(new Phrase(" ", normal_font));
                    Tdate.addCell(new Phrase("Дата: " + SystemSettings.df.format(aDate), date_normal_font));
                    document.add(Tdate);

                    //информация по кассе
                    Paragraph header = new Paragraph(myUI.getMessage(SptMessages.GeneralAccountingReport) + " (" + year + ")", title_font);
                    header.setAlignment(Element.ALIGN_CENTER);
//                    document.add(new Paragraph(45, " "));
                    document.add(header);
                    document.add(new Paragraph(15, " "));
                    Paragraph caption1 = new Paragraph(myUI.getMessage(SptMessages.AccountingInformationCaption), caption_bold_font);
                    caption1.setIndentationLeft(30);
                    document.add(caption1);
                    document.add(new Paragraph(10, " "));

                    float[] accTrInfo_colsWidth = {0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f};
                    PdfPTable accInfoTable = new PdfPTable(6);
                    accInfoTable.setWidthPercentage(90f);
                    accInfoTable.setWidths(accTrInfo_colsWidth);
                    accInfoTable.getDefaultCell().setBorder(0);

                    accInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    accInfoTable.addCell(new Phrase(myUI.getMessage(SptMessages.IncomesTotal) + ":", normal_font));
                    accInfoTable.addCell(new Phrase(myUI.getMessage(SptMessages.LastIncomeDate) + ":", normal_font));
                    accInfoTable.addCell(new Phrase(myUI.getMessage(SptMessages.ExpensesTotal) + ":", normal_font));
                    accInfoTable.addCell(new Phrase(myUI.getMessage(SptMessages.LastExpenseDate) + ":", normal_font));
                    accInfoTable.addCell(new Phrase(myUI.getMessage(SptMessages.Balance) + " (" + prevDay + "):", normal_font));
                    accInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    accInfoTable.addCell(new Phrase(myUI.getMessage(SptMessages.Transactions) + ":", normal_font));

                    accInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    accInfoTable.addCell(new Phrase(SystemSettings.dFormat.format(sclAccInfo.getTotal_income()) + "$", normal_font));
                    accInfoTable.addCell(new Phrase(sclAccInfo.getLast_income_date(), normal_font));
                    accInfoTable.addCell(new Phrase(SystemSettings.dFormat.format(sclAccInfo.getTotal_outcome()) + "$", normal_font));
                    accInfoTable.addCell(new Phrase(sclAccInfo.getLast_outcome_date(), normal_font));
                    accInfoTable.addCell(new Phrase(SystemSettings.dFormat.format(sclAccInfo.getPrevious_balance()) + "$", normal_font));
                    accInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    accInfoTable.addCell(new Phrase(SystemSettings.dFormat.format(sclAccInfo.getPrevious_balance() + sclAccInfo.getTotal_income()
                            - sclAccInfo.getTotal_outcome()) + "$", normal_font));

                    document.add(accInfoTable);

                    //Сумма доходов и расходов по месяцам
                    document.add(new Paragraph(20, " "));
                    Paragraph caption2 = new Paragraph(myUI.getMessage(SptMessages.IncomeOutcomeMonthlyCaption), caption_bold_font);
                    caption2.setIndentationLeft(30);
                    document.add(caption2);
                    document.add(new Paragraph(10, " "));

                    float[] accTrans_colsWidth = {0.1f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f};
                    PdfPTable accTransTable = new PdfPTable(7);
                    accTransTable.setWidthPercentage(90f);
                    accTransTable.setWidths(accTrans_colsWidth);

                    accTransTable.addCell(new Phrase(" "));
                    accTransTable.addCell(new Phrase(myUI.getMessage(SptMessages.Month), normal_bold_font));
                    accTransTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    accTransTable.addCell(new Phrase(myUI.getMessage(SptMessages.InstallmentPlan), normal_bold_font));
                    accTransTable.addCell(new Phrase(myUI.getMessage(SptMessages.Payments), normal_bold_font));
                    accTransTable.addCell(new Phrase(myUI.getMessage(SptMessages.Incomes), normal_bold_font));
                    accTransTable.addCell(new Phrase(myUI.getMessage(SptMessages.Outcomes), normal_bold_font));
                    accTransTable.addCell(new Phrase(myUI.getMessage(SptMessages.Difference), normal_bold_font));

                    Iterator iter = accTransactionsTable.getItemIds().iterator();
                    int i = 0;
                    if (accTransactionsTable.size() > 0) {
                        i = 1;
                    }
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        accTransTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        accTransTable.addCell(new Phrase(i + "", normal_font));
                        accTransTable.addCell(new Phrase(accTransactionsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Month)).getValue().toString(), normal_font));
                        accTransTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        accTransTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) accTransactionsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.InstallmentPlan)).getValue()), normal_font));
                        accTransTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) accTransactionsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Payments)).getValue()), normal_font));
                        accTransTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) accTransactionsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Incomes)).getValue()), normal_font));
                        accTransTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) accTransactionsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Outcomes)).getValue()), normal_font));
                        accTransTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) accTransactionsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Difference)).getValue()), normal_font));
                        i++;

                    }
                    accTransTable.addCell(new Phrase(" "));
                    accTransTable.addCell(new Phrase(" "));
                    accTransTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    accTransTable.addCell(new Phrase(accTransactionsTable.getColumnFooter(myUI.getMessage(SptMessages.InstallmentPlan)), normal_bold_font));
                    accTransTable.addCell(new Phrase(accTransactionsTable.getColumnFooter(myUI.getMessage(SptMessages.Payments)), normal_bold_font));
                    accTransTable.addCell(new Phrase(accTransactionsTable.getColumnFooter(myUI.getMessage(SptMessages.Incomes)), normal_bold_font));
                    accTransTable.addCell(new Phrase(accTransactionsTable.getColumnFooter(myUI.getMessage(SptMessages.Outcomes)), normal_bold_font));
                    accTransTable.addCell(new Phrase(accTransactionsTable.getColumnFooter(myUI.getMessage(SptMessages.Difference)), normal_bold_font));

                    document.add(accTransTable);
                    //Итого     Скидки   Оплаты   
                    //inner table                   
                    document.add(new Paragraph(15, " "));
                    float[] ttlContr_colsWidth = {0.1f, 0.1f};
                    PdfPTable ttlContrTable = new PdfPTable(2);
                    ttlContrTable.setWidthPercentage(100f);
                    ttlContrTable.setWidths(ttlContr_colsWidth);
                    ttlContrTable.getDefaultCell().setBorder(0);
                    ttlContrTable.addCell(new Phrase(myUI.getMessage(SptMessages.Students) + ":", normal_bold_font));
                    ttlContrTable.addCell(new Phrase(contrTtl.getTtl_students() + "", normal_font));
                    ttlContrTable.addCell(new Phrase(myUI.getMessage(SptMessages.TotalContract), normal_bold_font));
                    ttlContrTable.addCell(new Phrase(contrTtl.getTtl_contract() + "", normal_font));
                    ttlContrTable.addCell(new Phrase(myUI.getMessage(SptMessages.TotalDebt), normal_bold_font));
                    ttlContrTable.addCell(new Phrase(contrTtl.getTtl_debt() + "", normal_font));
                    ttlContrTable.addCell(new Phrase(myUI.getMessage(SptMessages.TotalDiscount), normal_bold_font));
                    ttlContrTable.addCell(new Phrase(contrTtl.getTtl_disc() + "", normal_font));
                    ttlContrTable.addCell(new Phrase(myUI.getMessage(SptMessages.TotalCorrection), normal_bold_font));
                    ttlContrTable.addCell(new Phrase(contrTtl.getTtl_correction() + "", normal_font));
                    ttlContrTable.addCell(new Phrase(myUI.getMessage(SptMessages.Net), normal_bold_font));
                    ttlContrTable.addCell(new Phrase(contrTtl.getTtl_net() + "", normal_font));
                    ttlContrTable.addCell(new Phrase(myUI.getMessage(SptMessages.TotalPayment), normal_bold_font));
                    ttlContrTable.addCell(new Phrase(contrTtl.getTtl_payments() + "", normal_font));
                    ttlContrTable.addCell(new Phrase(myUI.getMessage(SptMessages.TotalLeft), normal_bold_font));
                    ttlContrTable.addCell(new Phrase(contrTtl.getTtl_left() + "", normal_font));

                    float[] ttl_colsWidth = {0.5f, 0.95f, 1.05f};
                    PdfPTable ttlTable = new PdfPTable(3);
                    ttlTable.setWidthPercentage(90f);
                    ttlTable.setWidths(ttl_colsWidth);
                    ttlTable.getDefaultCell().setBorder(0);
                    ttlTable.addCell(new Phrase(myUI.getMessage(SptMessages.Total), caption_bold_font));
                    ttlTable.addCell(new Phrase(myUI.getMessage(SptMessages.Discounts), caption_bold_font));
                    ttlTable.addCell(new Phrase(myUI.getMessage(SptMessages.Payments), caption_bold_font));
                    ttlTable.addCell(ttlContrTable);
                    ttlTable.getDefaultCell().setFixedHeight(75f);
                    ttlTable.addCell(createSvgImage(writer.getDirectContent(), svgStrDiscounts));
                    ttlTable.addCell(createSvgImage(writer.getDirectContent(), svgStrPaid));
                    document.add(ttlTable);

                    //Сумма оплат и плана оплат по месяцам
                    //inner table
                    document.add(new Paragraph(20, " "));
                    Paragraph caption3 = new Paragraph(myUI.getMessage(SptMessages.PaymentsMonthlyCaption), caption_bold_font);
                    caption3.setIndentationLeft(30);
                    document.add(caption3);
                    document.add(new Paragraph(10, " "));

                    float[] instPlan_colsWidth = {0.15f, 0.4f, 0.4f, 0.4f, 0.4f};
                    PdfPTable inPaymentsTable = new PdfPTable(5);
                    inPaymentsTable.setWidthPercentage(100f);
                    inPaymentsTable.setWidths(instPlan_colsWidth);
                    inPaymentsTable.addCell(new Phrase(""));
                    inPaymentsTable.addCell(new Phrase(myUI.getMessage(SptMessages.Month), normal_bold_font));
                    inPaymentsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    inPaymentsTable.addCell(new Phrase(myUI.getMessage(SptMessages.InstallmentPlan), normal_bold_font));
                    inPaymentsTable.addCell(new Phrase(myUI.getMessage(SptMessages.Payments), normal_bold_font));
                    inPaymentsTable.addCell(new Phrase(myUI.getMessage(SptMessages.Debt), normal_bold_font));

                    Iterator iter1 = paymentsTable.getItemIds().iterator();
                    int x = 0;
                    if (paymentsTable.size() > 0) {
                        x = 1;
                    }
                    while (iter1.hasNext()) {
                        Object next = iter1.next();
                        inPaymentsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        inPaymentsTable.addCell(new Phrase(x + "", normal_font));
                        inPaymentsTable.addCell(new Phrase(paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Month)).getValue().toString(), normal_font));
                        inPaymentsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        inPaymentsTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.InstallmentPlan)).getValue()), normal_font));
                        inPaymentsTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Payments)).getValue()), normal_font));
                        inPaymentsTable.addCell(new Phrase(SystemSettings.dFormat.format((Double) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Debt)).getValue()), normal_font));
                        x++;
                    }

                    inPaymentsTable.addCell(new Phrase(" "));
                    inPaymentsTable.addCell(new Phrase(" "));
                    inPaymentsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    inPaymentsTable.addCell(new Phrase(paymentsTable.getColumnFooter(myUI.getMessage(SptMessages.InstallmentPlan)), normal_bold_font));
                    inPaymentsTable.addCell(new Phrase(paymentsTable.getColumnFooter(myUI.getMessage(SptMessages.Payments)), normal_bold_font));
                    inPaymentsTable.addCell(new Phrase(paymentsTable.getColumnFooter(myUI.getMessage(SptMessages.Debt)), normal_bold_font));

                    //outer table
                    float[] colsWidth = {0.45f, 0.55f};
                    PdfPTable paymentsTable = new PdfPTable(2);
                    paymentsTable.setWidthPercentage(90f);
                    paymentsTable.setWidths(colsWidth);
                    paymentsTable.getDefaultCell().setBorder(0);
                    paymentsTable.addCell(inPaymentsTable);
                    paymentsTable.addCell(createSvgImage(writer.getDirectContent(), svgStrPayments));
                    document.add(paymentsTable);

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

        resource = new StreamResource(source1, "AccountingGeneralReport"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "AccountingGeneralReport", false);
    }

    private Image drawUnscaledSvg(PdfContentByte contentByte, String svg)
            throws IOException {

        // First, lets create a graphics node for the SVG image.
        GraphicsNode imageGraphics = buildBatikGraphicsNode(svg);

        // SVG's width and height
        float width = (float) imageGraphics.getBounds().getWidth();
        float height = (float) imageGraphics.getBounds().getHeight();

        // Create a PDF template for the SVG image
        PdfTemplate template = contentByte.createTemplate(width, height);
        // Create Graphics2D rendered object from the template
        Graphics2D graphics = template.createGraphics(width, height);
        try {
            // SVGs can have their corner at coordinates other than (0,0).
            Rectangle2D bounds = imageGraphics.getBounds();
            graphics.translate(-bounds.getX(), -bounds.getY());

            // Paint SVG GraphicsNode with the 2d-renderer.
            imageGraphics.paint(graphics);

            // Create and return a iText Image element that contains the SVG
            // image.
            return new ImgTemplate(template);
        } catch (BadElementException e) {
            throw new RuntimeException("Couldn't generate PDF from SVG", e);
        } finally {
            // Manual cleaning (optional)
            graphics.dispose();
        }
    }

    /**
     * Use Batik SVG Toolkit to create GraphicsNode for the target SVG.
     * <ol>
     * <li>Create SVGDocument</li>
     * <li>Create BridgeContext</li>
     * <li>Build GVT tree. Results to GraphicsNode</li>
     * </ol>
     *
     * @param svg SVG as a String
     * @return GraphicsNode
     * @throws IOException Thrown when SVG could not be read properly.
     */
    private GraphicsNode buildBatikGraphicsNode(String svg) throws IOException {
        UserAgent agent = new UserAgentAdapter();

        SVGDocument svgdoc = createSVGDocument(svg, agent);

        DocumentLoader loader = new DocumentLoader(agent);
        BridgeContext bridgeContext = new BridgeContext(agent, loader);
        bridgeContext.setDynamicState(BridgeContext.STATIC);

        GVTBuilder builder = new GVTBuilder();

        GraphicsNode imageGraphics = builder.build(bridgeContext, svgdoc);

        return imageGraphics;
    }

    private SVGDocument createSVGDocument(String svg, UserAgent agent)
            throws IOException {
        SVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(
                agent.getXMLParserClassName(), true);

        SVGDocument svgdoc = documentFactory.createSVGDocument(null,
                new StringReader(svg));
        return svgdoc;
    }

    private Image createSvgImage(PdfContentByte contentByte, String imgSvg) throws IOException {
        Image image = drawUnscaledSvg(contentByte, imgSvg);
        image.scalePercent(100);
        return image;
    }

}
