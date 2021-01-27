/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.data.util.converter.StringToIntegerConverter;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * @author alex
 */
public class SystemSettings implements Serializable {

    public static final String PATH_TO_UPLOADS = "/home/logo/";
    public static final String PATH_TO_UPLOADS_HR = "/home/logo/hr/";
    public final DecimalFormat dFormat = new DecimalFormat("0.00");
    public final DecimalFormat dMonth = new DecimalFormat("00");
    public final String datePattern = "dd-MM-yyyy";
    public final String yearMonthPattern = "MM-yyyy";
    public static final String yearPattern = "yyyy";
    public final SimpleDateFormat dateRu = new SimpleDateFormat(
            "«dd» MMMMM yyyy г.", myDateFormatSymbols);
    public final SimpleDateFormat dateEn = new SimpleDateFormat(
            "«dd» MMMMM yyyy");
    public final SimpleDateFormat df = new SimpleDateFormat(datePattern);
    public static final SimpleDateFormat mysql_only_year = new SimpleDateFormat("yyyy-01-01");
    public final String dateTimeMinPattern = "dd-MM-yyyy HH:mm";
    public final SimpleDateFormat dtmf = new SimpleDateFormat(dateTimeMinPattern);
    public final SimpleDateFormat ymdf = new SimpleDateFormat(yearMonthPattern);

    public final String id = "id";
    public final String count = "count";
    public final String email = "E-mail";
    public final String resigned = " [уволен]";
    public final String transfered = " [переведен]";
    public final String number_id = "number_id";
    public final String status_id = "status_id";
    public final String acc_category_id = "acc_category_id";
    public final String parent_id = "parent_id";
    public final String school_type_id = "school_type_id";
    public final String category_id = "category_id";
    public final String payment_category_id = "payment_category_id";
    public final String is_main = "is_main";
    public final String year_id = "year_id";
    public final String school_id = "school_id";
    public final String discount_type_id = "discount_type_id";
    public final String position_id = "position_id";
    public final String extra_position_ids = "extra_position_ids";
    public final String salary_category_id = "salary_category_id";
    public final String gender_id = "gender_id";
    public final String nationality_id = "nationality_id";
    public final String martial_status_id = "martial_status_id";
    public final String activity_status_id = "activity_status_id";
    public final String working_status_id = "working_status_id";
    public final String education_status_id = "education_status_id";
    public final String class_name_id = "class_name_id";
    public final String class_id = "class_id";
    public final String visible_hr_orders = "visible_hr_orders";
    public final String is_modifiable = "is_modifiable";
    public final String order_id = "order_id";
    public final String order_number = "order_number";
    public final String from_education_status_id = "from_education_status_id";
    public final String from_class_id = "from_class_id";
    public final String from_employee_id = "from_employee_id";
    public final String to_employee_id = "to_employee_id";
    public final String to_class_id = "to_class_id";
    public final String student_id = "student_id";
    public final String stock_id = "stock_id";
    public final String quantity_id = "remain_id";
    public final String measurement_id = "measurement_id";
    public final String crud_status = "crud_status";
    public final String effected_by_id = "effected_by_id";
    public final String classTable = "class_number";
    public final String dbLanguageTable = "hr_language";
    public final String dbLanguageLevelTable = "hr_language_level";
    public final String dbExamTable = "hr_exam";
    public final String positionCategoryTable = "hr_position_category";
    public final String dbBranchTable = "hr_branch";
    public final String dbUniversityTable = "hr_university";
    public final String dbWork_placeTable = "hr_work_place";
    public final String dbCertificateTable = "hr_certificate";
    public final String dbAttachmentsTable = "hr_attachments";
    public final String dbQuestion = "hr_question";
    public final String dbActivity_status = "activity_status";
    public final String dbSchoolType = "school_type";
    public final String dbAccessoriesCategory = "accessories_category";
    public final String dbGender = "gender";
    public final String dbCountry = "hr_country";
    public final String dbEduLevel = "hr_education_level";
    public final String dbHealthStatus = "hr_health_status";
    public final String dbAcc_currency = "acc_currency";
    public final String acc_currency_id = "acc_currency_id";
    public final String dbAcc_transactions = "acc_transactions";
    public final String dbPaymentType = "payment_type";
    public final String dbWorking_status = "working_status";
    public final String dbClass_name = "class_name";
    public final String db_dp_invoice = "dp_invoice";
    public final String dbAccInvoice = "acc_invoice";
    public final String db_acc_invoice_id = "acc_invoice_id";
    public final String invoice_id = "invoice_id";
    public final String dbStock = "dp_stock";
    public final String dbOperation = "dp_service_type";
    public final String dbAcc_category = "acc_category";
    public final String dbRelatives = "relatives";
    public final String dbMartialStatus = "hr_martial_status";
    public final String dbMeasurement = "dp_measurement";
    public final String dbStockMovement = "dp_stock_movements";
    public final String dbTransfers = "acc_transfers";
    public final String dbNationality = "nationality";
    public final String dbEducationStatus = "education_status";
    public final String dbDiscountType = "discount_type";
    public final String hr_positionTable = "hr_position";
    public final String dbStudent = "student";
    public final String dbStudentContract = "student_contract";
    public final String dbDiscount = "discount";
    public final String dbEmployee = "employee";
    public final String dbEmployeePhoneNumber = "hr_employee_phone_number";
    public final String dbEmployeeChildren = "hr_employee_children";
    public final String dbEmployeeEducation = "hr_employee_education";
    public final String dbEmployeeLanguage = "hr_employee_language";
    public final String dbEmployeeExams = "hr_employee_exam";
    public final String dbEmployeeBranch = "hr_employee_branch";
    public final String dbEmployeeBranchHours = "hr_employee_branch_hours";
    public final String dbEmployeeOrder = "hr_employee_order";
    public final String dbEmployeeSeminar = "hr_employee_seminar";
    public final String dbEmployeeCertificate = "hr_employee_certificate";
    public final String dbEmployeeWork = "hr_employee_work";
    public final String dbEmployeeQuestion = "hr_employee_question";
    public final String dbPhoneType = "hr_phone_type";
    public final String dbHrEducationStatus = "hr_education_status";
    public final String employee_id = "employee_id";
    public final String dbAccessories = "accessories";
    public final String dbContract = "contract";
    public final String dbOrderReason = "order_reason";
    public final String dbSchool = "school";
    public final String dbMessages = "messages";
    public final String dbYear = "year";
    public final String dbStudentOrders = "student_orders";
    public static final String rnAdmin = "admin";
    public static final String rnHr = "hr";
    public final String cnAccessoriesDefinitionView = "AccessoriesDefinitionView";
    public final String cnIssueOrderView = "IssueOrderView";
    public final String cnClassNameDefinitionView = "ClassNameDefinitionView";
    public final String cnContractDefintionView = "ContractDefintionView";
    public final String cnLeavingReasonsDefinitionView = "LeavingReasonsDefinitionView";
    public final String cnDefinitionView = "DefinitionView";
    public final String cnHRDefinitionView = "HRDefinitionView";
    public final String cnHomePageView = "HomePageView";
    public final String cnDiscountDefinitionView = "DiscountDefinitionView";
    public final String cnExpensesDefinitionView = "ExpensesDefinitionView";
    public final String cnShortTermDebtsDefinitionView = "ShortTermDebtsDefinitionView";
    public final String cnReturnableAssetsDefinitionView = "ReturnableAssetsDefinitionView";
    public final String cnIncomesDefinitionView = "IncomesDefinitionView";
    public final String cnStockDefinitionView = "StockDefinitionView";
    public final String cnStockIncomeView = "StockIncomeView";
    public final String cnStockOutcomeView = "StockOutcomeView";
    public final String cnTransactionsView = "TransactionsView";
    public final String cnAccrualsView = "AccrualsView";
    public final String cnShortTermDebtsView = "ShortTermDebtsView";
    public final String cnReturnableAssetsView = "ReturnableAssetsView";
    public final String cnPayoutsView = "PayoutsView";
    public final String cnSchoolDefinitionView = "SchoolDefinitionView";
    public final String cnEmployeeDefinitionView = "EmployeeDefinitionView";
    public final String cnEmployeeTransferView = "EmployeeTransferView";
    public final String cnLessonAssessmentView = "LessonAssessmentView";
    public final String cnMessagesView = "MessagesView";
    public final String cnSchoolModificationView = "SchoolModificationView";
    public final String cnStudentDefinitionView = "StudentDefinitionView";
    public final String cnReportsView = "ReportsView";
    public final String cnAccountingReportsView = "AccountingReportsView";
    public final String cnStockReportsView = "StockReportsView";
    public final String cnHRReportsView = "HRReportsView";
    public final String actAdd = "добавление";
    public final String actModify = "изменение";
    public final String prmChangeOldTransactions = "изменение старых записей";
    public final String prmChangeCurrencyRate = "изменение общего курса доллара";
    public final String prmGeneralInfo = "общая информация";
    public final String prmStudentsInfo = "информация об учениках";
    public final String prmAccountingInfo = "информация по бухгалтерии";
    public final String prmLogsInfo = "логи системы";
    public final String prmAccountingLogsSelect = "логи по бухгалтерии";
    public final String actDelete = "удаление";
    public final String prmContractInfo = "информация о контракте";
    public final String prmContractInfoLeftDebt = "информация о задолженностях";
    public final String prmMenu = "меню";
    public final String actCopy = "копирование";
    public final String prmPlanPayments = "план оплат и оплаты";
    public final String prmClassPayments = "оплаты по классам";
    public final String prmClassDiscounts = "скидки по классам";
    public final String prmDiscountsReport = "отчет по скидкам";
    public final String prmSchoolDiscounts = "скидки по школам";
    public final String prmDebtReport = "отчет по долгам";
    public final String prmClassInstPlan = "план оплат по классам";
    public final String prmClassList = "список класса";
    public final String prmStatusesReport = "отчет по статусам";
    public final String prmYearMonthReport = "годовой и месячный отчет";
    public final String prmMonthReport = "отчет по месяцам";
    public final String prmAccountingBalanceReport = "балансовый отчет";
    public final String prmByDateReport = "отчет по датам";
    public final String prmSchoolAccountingReport = "school_accounting_report";
    public final String prmGeneralReport = "общий отчет по бухгалтерии";
    public final String prmCurrentAccountStatement = "выписка по текущему счету";
    public final String prmSalariesReport = "отчет по выплатам";
    public final String prmLessonHoursReport = "отчет по количеству преподаваемых часов";
    public final String prmCallsReport = "отчет о вызовах";
    public final String prmOutOfReport = "отчет о выбывших";
    public final String prmTabContract = "вкладка контракта";
    public final String prmTabPayments = "вкладка оплат";
    public final String prmTabCalls = "вкладка звонков";
    public final String prmTabFamilyTab = "вкладка информации о семье";
    public final String prmTabAccessoriesGive = "вкладка выданных материалов";
    public final String prmTabAccessoriesReceive = "вкладка полученных материалов";
    public final String prmTabActivities = "вкладка деятельности";
    public final String prmTabOrders = "вкладка приказов";
    public final String prmTabSearch = "вкладка поиска";
    public final String prmChangeYear = "ChangeYear";
    public final String prmChangeSchool = "ChangeSchool";
    public final String prmProductMovementsReport = "отчет по передвижениям товара";
    public final String prmStockGeneralReport = "общий отчет по складам";
    public final String prmConfirmationControl = "контроль подтверждений";
    public final String button = "##";
    public final String FreshItem = "fresh";
    public final String cnBackupView = "BackupView";
    public final String cnCallsView = "CallsView";
    public final String cnSettingsView = "SettingsView";
    public final String percentage = "%";
    public final String activeStatus = "active";
    public final String entering_year_id = "entering_year_id";
    public final String hr_position_category_id = "hr_position_category_id";
    public final String dbColumnStudent_payments_id = "student_payments_id";
    public final String old_amount = "old_amount";
    public final String old_currency = "old_currency";
    public final String old_rate = "old_rate";
    public final String old_date = "old_date";
    public final String old_category = "old_category";
    public final String download_button = "download_button";

    public StringToDoubleConverter getStringToDoubleConverter() {
        StringToDoubleConverter plainConverter = new StringToDoubleConverter() {
            @Override
            protected java.text.NumberFormat getFormat(Locale locale) {
                NumberFormat format = super.getFormat(Locale.ENGLISH);
                format.setGroupingUsed(false);
                format.setMaximumFractionDigits(2);
                format.setMinimumFractionDigits(2);
                return format;
            }
        };
        return plainConverter;
    }

    public StringToIntegerConverter getStringToIntegerConverter() {
        StringToIntegerConverter plainConverter = new StringToIntegerConverter() {
            @Override
            protected java.text.NumberFormat getFormat(Locale locale) {
                NumberFormat format = super.getFormat(Locale.ENGLISH);
                format.setGroupingUsed(false);
                format.setMaximumFractionDigits(0);
                return format;
            }
        };
        return plainConverter;
    }

    public Set<?> convertToSet(Collection<?> coll) {
        Iterator iter = coll.iterator();
        HashSet<Integer> hs = new HashSet<Integer>();
        while (iter.hasNext()) {
            Object next = iter.next();
            hs.add((Integer) next);
        }
        return hs;
    }

    public String convertCollectionToStr(Collection<?> set) {
        if (!set.isEmpty()) {
            return StringUtils.join(set, ",");
        } else {
            return null;
        }
    }

    public String convertCollectionToStr(Collection<?> set, IndexedContainer container, Object property) {
        Set ids = new HashSet();
        if (!set.isEmpty()) {
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                ids.add(container.getContainerProperty(iterator.next(), property).getValue());
            }
            return StringUtils.join(ids, ",");
        } else {
            return null;
        }
    }

    public Set<Integer> getChild_ids(HierarchicalContainer container, Set<?> selectedIds) {
        Set<Integer> set = new HashSet<>();
        Iterator selectedIter = selectedIds.iterator();
        while (selectedIter.hasNext()) {
            Integer nextId = (Integer) selectedIter.next();
            set.add(nextId);
            if (container.hasChildren(nextId)) {
                Iterator iter = container.getChildren(nextId).iterator();
                while (iter.hasNext()) {
                    Set<Integer> setChild = new HashSet<>();
                    setChild.add((Integer) iter.next());
                    set.addAll(getChild_ids(container, setChild));
                }
            }
        }
        return set;
    }

    public double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static String transliterate(String message) {
        char[] abcCyr = {' ', 'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String[] abcLat = {" ", "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "ju", "ja", "A", "B", "V", "G", "D", "E", "E", "Zh", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Sch", "", "I", "", "E", "Ju", "Ja", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            for (int x = 0; x < abcCyr.length; x++) {
                if (message.charAt(i) == abcCyr[x]) {
                    builder.append(abcLat[x]);
                }
            }
        }
        return builder.toString();
    }

    private static final DateFormatSymbols myDateFormatSymbols = new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return new String[]{"января", "февраля", "марта", "апреля", "мая", "июня",
                    "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }
    };
}
