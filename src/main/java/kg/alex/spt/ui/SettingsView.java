/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.ui;

import kg.alex.spt.utils.ComboBoxMax;
import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbExam;
import kg.alex.spt.dao.DbPaymentCategory;
import kg.alex.spt.dao.DbPosition;
import kg.alex.spt.dao.DbProductCategories;
import kg.alex.spt.dao.DbSalaryCategories;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SettingsView extends GridLayout implements Button.ClickListener {

    static final Logger logger = LogManager.getLogger(SettingsView.class);
    private MyVaadinUI myUI;
    private Button saveBtn;
    private Label initPayLab, currLab, prevLab, futrLab, outcomesLab, earlyPayLab, salaryLab, localSalaryLab;
    private Label foodLab, stationeryLab, buildingMaterialsLab, washingMaterialsLab, otherProductsLab;
    private ComboBoxMax initPayCb, currCb, prevCb, futrCb, outcomesCb, earlyPayCb, salaryCb, localSalaryCb;
    private ComboBoxMax foodCb, stationeryCb, buildingMaterialsCb, washingMaterialsCb, otherProductsCb;
    private Label adminLab, directorLab, accountentLab, supervisorLab, hrLab, supplyManagerLab, warehouseManagerLab, sapat_secretaryLab;
    private ComboBoxMax adminCb, directorCb, accountentCb, supervisorCb, hrCb, supplyManagerCb, warehouseManagerCb, sapat_secretaryCb;
    private Label sebatExamLab;
    private ComboBoxMax sebatExamCb;
    private IndexedContainer paymentsContainer = new IndexedContainer();
    private IndexedContainer salaryContainer = new IndexedContainer();
    private IndexedContainer productsContainer = new IndexedContainer();
    private IndexedContainer positionsContainer = new IndexedContainer();

    public SettingsView(MyVaadinUI myUI) {
        this.myUI = myUI;

        this.setRows(16);
        this.setColumns(4);
        this.setWidth("100%");
        this.setSpacing(true);
        this.setMargin(true);

        try {
            DbPaymentCategory dbpc = new DbPaymentCategory();
            dbpc.connect();
            paymentsContainer = dbpc.execSQL(myUI);
            dbpc.close();
            DbPosition dbp = new DbPosition();
            dbp.connect();
            positionsContainer = dbp.execSQL_cont(myUI);
            dbp.close();
            DbProductCategories dbprCat = new DbProductCategories();
            dbprCat.connect();
            productsContainer = dbprCat.execSQL_cont(myUI);
            dbprCat.close();
            DbSalaryCategories dbslCat = new DbSalaryCategories();
            dbslCat.connect();
            salaryContainer = dbslCat.execSQL_cont(myUI);
            dbslCat.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        Label captionAccounting = new Label();
        captionAccounting.setSizeFull();
        captionAccounting.setContentMode(ContentMode.HTML);
        captionAccounting.setValue(myUI.getMessage(SptMessages.AccountingSettings));
        captionAccounting.setStyleName("tableCpt");

        initPayLab = new Label();
        initPayLab.setSizeUndefined();
        initPayLab.setValue(paymentsContainer.getContainerProperty(1,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        currLab = new Label();
        currLab.setSizeUndefined();
        currLab.setValue(paymentsContainer.getContainerProperty(2,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        prevLab = new Label();
        prevLab.setSizeUndefined();
        prevLab.setValue(paymentsContainer.getContainerProperty(4,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        futrLab = new Label();
        futrLab.setSizeUndefined();
        futrLab.setValue(paymentsContainer.getContainerProperty(5,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        outcomesLab = new Label();
        outcomesLab.setSizeUndefined();
        outcomesLab.setValue(paymentsContainer.getContainerProperty(3,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        earlyPayLab = new Label();
        earlyPayLab.setSizeUndefined();
        earlyPayLab.setValue(paymentsContainer.getContainerProperty(6,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        salaryLab = new Label();
        salaryLab.setSizeUndefined();
        salaryLab.setValue(salaryContainer.getContainerProperty(34,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        localSalaryLab = new Label();
        localSalaryLab.setSizeUndefined();
        localSalaryLab.setValue(salaryContainer.getContainerProperty(37,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        initPayCb = new ComboBoxMax();
        initPayCb.setNullSelectionAllowed(false);
        initPayCb.setRequired(true);
        initPayCb.setData(1);
        initPayCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        initPayCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        initPayCb.setWidth("100%");
        initPayCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        initPayCb.setFilteringMode(FilteringMode.CONTAINS);

        currCb = new ComboBoxMax();
        currCb.setNullSelectionAllowed(false);
        currCb.setRequired(true);
        currCb.setData(2);
        currCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        currCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        currCb.setWidth("100%");
        currCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        currCb.setFilteringMode(FilteringMode.CONTAINS);

        prevCb = new ComboBoxMax();
        prevCb.setNullSelectionAllowed(false);
        prevCb.setRequired(true);
        prevCb.setData(4);
        prevCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        prevCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        prevCb.setWidth("100%");
        prevCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        prevCb.setFilteringMode(FilteringMode.CONTAINS);

        futrCb = new ComboBoxMax();
        futrCb.setNullSelectionAllowed(false);
        futrCb.setRequired(true);
        futrCb.setData(5);
        futrCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        futrCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        futrCb.setWidth("100%");
        futrCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        futrCb.setFilteringMode(FilteringMode.CONTAINS);

        outcomesCb = new ComboBoxMax();
        outcomesCb.setNullSelectionAllowed(false);
        outcomesCb.setRequired(true);
        outcomesCb.setData(3);
        outcomesCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        outcomesCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        outcomesCb.setWidth("100%");
        outcomesCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        outcomesCb.setFilteringMode(FilteringMode.CONTAINS);

        earlyPayCb = new ComboBoxMax();
        earlyPayCb.setNullSelectionAllowed(false);
        earlyPayCb.setRequired(true);
        earlyPayCb.setData(6);
        earlyPayCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        earlyPayCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        earlyPayCb.setWidth("100%");
        earlyPayCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        earlyPayCb.setFilteringMode(FilteringMode.CONTAINS);

        salaryCb = new ComboBoxMax();
        salaryCb.setNullSelectionAllowed(false);
        salaryCb.setRequired(true);
        salaryCb.setData(34);
        salaryCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        salaryCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        salaryCb.setWidth("100%");
        salaryCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        salaryCb.setFilteringMode(FilteringMode.CONTAINS);

        localSalaryCb = new ComboBoxMax();
        localSalaryCb.setNullSelectionAllowed(false);
        localSalaryCb.setRequired(true);
        localSalaryCb.setData(37);
        localSalaryCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        localSalaryCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        localSalaryCb.setWidth("100%");
        localSalaryCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        localSalaryCb.setFilteringMode(FilteringMode.CONTAINS);

        Label captionStock = new Label();
        captionStock.setSizeFull();
        captionStock.setContentMode(ContentMode.HTML);
        captionStock.setValue(myUI.getMessage(SptMessages.StockSettings));
        captionStock.setStyleName("tableCpt");

        foodLab = new Label();
        foodLab.setSizeUndefined();
        foodLab.setValue(productsContainer.getContainerProperty(501,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        stationeryLab = new Label();
        stationeryLab.setSizeUndefined();
        stationeryLab.setValue(productsContainer.getContainerProperty(502,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        buildingMaterialsLab = new Label();
        buildingMaterialsLab.setSizeUndefined();
        buildingMaterialsLab.setValue(productsContainer.getContainerProperty(503,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        washingMaterialsLab = new Label();
        washingMaterialsLab.setSizeUndefined();
        washingMaterialsLab.setValue(productsContainer.getContainerProperty(504,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        otherProductsLab = new Label();
        otherProductsLab.setSizeUndefined();
        otherProductsLab.setValue(productsContainer.getContainerProperty(505,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        foodCb = new ComboBoxMax();
        foodCb.setNullSelectionAllowed(false);
        foodCb.setRequired(true);
        foodCb.setData(501);
        foodCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        foodCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        foodCb.setWidth("100%");
        foodCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        foodCb.setFilteringMode(FilteringMode.CONTAINS);

        stationeryCb = new ComboBoxMax();
        stationeryCb.setNullSelectionAllowed(false);
        stationeryCb.setRequired(true);
        stationeryCb.setData(502);
        stationeryCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        stationeryCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        stationeryCb.setWidth("100%");
        stationeryCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        stationeryCb.setFilteringMode(FilteringMode.CONTAINS);

        buildingMaterialsCb = new ComboBoxMax();
        buildingMaterialsCb.setNullSelectionAllowed(false);
        buildingMaterialsCb.setRequired(true);
        buildingMaterialsCb.setData(503);
        buildingMaterialsCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        buildingMaterialsCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        buildingMaterialsCb.setWidth("100%");
        buildingMaterialsCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        buildingMaterialsCb.setFilteringMode(FilteringMode.CONTAINS);

        washingMaterialsCb = new ComboBoxMax();
        washingMaterialsCb.setNullSelectionAllowed(false);
        washingMaterialsCb.setRequired(true);
        washingMaterialsCb.setData(504);
        washingMaterialsCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        washingMaterialsCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        washingMaterialsCb.setWidth("100%");
        washingMaterialsCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        washingMaterialsCb.setFilteringMode(FilteringMode.CONTAINS);

        otherProductsCb = new ComboBoxMax();
        otherProductsCb.setNullSelectionAllowed(false);
        otherProductsCb.setRequired(true);
        otherProductsCb.setData(505);
        otherProductsCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        otherProductsCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        otherProductsCb.setWidth("100%");
        otherProductsCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.FullName));
        otherProductsCb.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            initPayCb.setContainerDataSource(dbac.exec_for_select(myUI, 1));
            currCb.setContainerDataSource(dbac.exec_for_select(myUI, 1));
            prevCb.setContainerDataSource(dbac.exec_for_select(myUI, 1));
            futrCb.setContainerDataSource(dbac.exec_for_select(myUI, 1));
            outcomesCb.setContainerDataSource(dbac.exec_for_select(myUI, 2));
            earlyPayCb.setContainerDataSource(dbac.exec_for_select(myUI, 1));
            salaryCb.setContainerDataSource(dbac.exec_for_select(myUI, 2));
            localSalaryCb.setContainerDataSource(dbac.exec_for_select(myUI, 2));
            foodCb.setContainerDataSource(dbac.exec_for_select(myUI, 2));
            stationeryCb.setContainerDataSource(dbac.exec_for_select(myUI, 2));
            buildingMaterialsCb.setContainerDataSource(dbac.exec_for_select(myUI, 2));
            washingMaterialsCb.setContainerDataSource(dbac.exec_for_select(myUI, 2));
            otherProductsCb.setContainerDataSource(dbac.exec_for_select(myUI, 2));
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        initPayCb.setValue(paymentsContainer.getContainerProperty((Integer) initPayCb.getData(),
                Settings.acc_category_id).getValue());
        currCb.setValue(paymentsContainer.getContainerProperty((Integer) currCb.getData(),
                Settings.acc_category_id).getValue());
        prevCb.setValue(paymentsContainer.getContainerProperty((Integer) prevCb.getData(),
                Settings.acc_category_id).getValue());
        futrCb.setValue(paymentsContainer.getContainerProperty((Integer) futrCb.getData(),
                Settings.acc_category_id).getValue());
        outcomesCb.setValue(paymentsContainer.getContainerProperty((Integer) outcomesCb.getData(),
                Settings.acc_category_id).getValue());
        earlyPayCb.setValue(paymentsContainer.getContainerProperty((Integer) earlyPayCb.getData(),
                Settings.acc_category_id).getValue());
        salaryCb.setValue(salaryContainer.getContainerProperty((Integer) salaryCb.getData(),
                Settings.acc_category_id).getValue());
        localSalaryCb.setValue(salaryContainer.getContainerProperty((Integer) localSalaryCb.getData(),
                Settings.acc_category_id).getValue());

        foodCb.setValue(productsContainer.getContainerProperty((Integer) foodCb.getData(),
                Settings.acc_category_id).getValue());
        stationeryCb.setValue(productsContainer.getContainerProperty((Integer) stationeryCb.getData(),
                Settings.acc_category_id).getValue());
        buildingMaterialsCb.setValue(productsContainer.getContainerProperty((Integer) buildingMaterialsCb.getData(),
                Settings.acc_category_id).getValue());
        washingMaterialsCb.setValue(productsContainer.getContainerProperty((Integer) washingMaterialsCb.getData(),
                Settings.acc_category_id).getValue());
        otherProductsCb.setValue(productsContainer.getContainerProperty((Integer) otherProductsCb.getData(),
                Settings.acc_category_id).getValue());

        Label captionPositions = new Label();
        captionPositions.setSizeFull();
        captionPositions.setContentMode(ContentMode.HTML);
        captionPositions.setValue(myUI.getMessage(SptMessages.PositionsSettings));
        captionPositions.setStyleName("tableCpt");

        adminLab = new Label();
        adminLab.setSizeUndefined();
        adminLab.setValue(positionsContainer.getContainerProperty(5,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        hrLab = new Label();
        hrLab.setSizeUndefined();
        hrLab.setValue(positionsContainer.getContainerProperty(25,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        supplyManagerLab = new Label();
        supplyManagerLab.setSizeUndefined();
        supplyManagerLab.setValue(positionsContainer.getContainerProperty(17,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        warehouseManagerLab = new Label();
        warehouseManagerLab.setSizeUndefined();
        warehouseManagerLab.setValue(positionsContainer.getContainerProperty(100,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        sapat_secretaryLab = new Label();
        sapat_secretaryLab.setSizeUndefined();
        sapat_secretaryLab.setValue(positionsContainer.getContainerProperty(115,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        directorLab = new Label();
        directorLab.setSizeUndefined();
        directorLab.setValue(positionsContainer.getContainerProperty(1,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        accountentLab = new Label();
        accountentLab.setSizeUndefined();
        accountentLab.setValue(positionsContainer.getContainerProperty(2,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        supervisorLab = new Label();
        supervisorLab.setSizeUndefined();
        supervisorLab.setValue(positionsContainer.getContainerProperty(21,
                myUI.getMessage(SptMessages.Title)).getValue().toString());

        adminCb = new ComboBoxMax();
        adminCb.setNullSelectionAllowed(false);
        adminCb.setRequired(true);
        adminCb.setData(5);
        adminCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        adminCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        adminCb.setWidth("100%");
        adminCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        adminCb.setFilteringMode(FilteringMode.CONTAINS);

        hrCb = new ComboBoxMax();
        hrCb.setNullSelectionAllowed(false);
        hrCb.setRequired(true);
        hrCb.setData(25);
        hrCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        hrCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        hrCb.setWidth("100%");
        hrCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        hrCb.setFilteringMode(FilteringMode.CONTAINS);

        supplyManagerCb = new ComboBoxMax();
        supplyManagerCb.setNullSelectionAllowed(false);
        supplyManagerCb.setRequired(true);
        supplyManagerCb.setData(17);
        supplyManagerCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        supplyManagerCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        supplyManagerCb.setWidth("100%");
        supplyManagerCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        supplyManagerCb.setFilteringMode(FilteringMode.CONTAINS);

        warehouseManagerCb = new ComboBoxMax();
        warehouseManagerCb.setNullSelectionAllowed(false);
        warehouseManagerCb.setRequired(true);
        warehouseManagerCb.setData(100);
        warehouseManagerCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        warehouseManagerCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        warehouseManagerCb.setWidth("100%");
        warehouseManagerCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        warehouseManagerCb.setFilteringMode(FilteringMode.CONTAINS);

        sapat_secretaryCb = new ComboBoxMax();
        sapat_secretaryCb.setNullSelectionAllowed(false);
        sapat_secretaryCb.setRequired(true);
        sapat_secretaryCb.setData(115);
        sapat_secretaryCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        sapat_secretaryCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        sapat_secretaryCb.setWidth("100%");
        sapat_secretaryCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        sapat_secretaryCb.setFilteringMode(FilteringMode.CONTAINS);

        directorCb = new ComboBoxMax();
        directorCb.setNullSelectionAllowed(false);
        directorCb.setRequired(true);
        directorCb.setData(1);
        directorCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        directorCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        directorCb.setWidth("100%");
        directorCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        directorCb.setFilteringMode(FilteringMode.CONTAINS);

        accountentCb = new ComboBoxMax();
        accountentCb.setNullSelectionAllowed(false);
        accountentCb.setRequired(true);
        accountentCb.setData(2);
        accountentCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        accountentCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        accountentCb.setWidth("100%");
        accountentCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        accountentCb.setFilteringMode(FilteringMode.CONTAINS);

        supervisorCb = new ComboBoxMax();
        supervisorCb.setNullSelectionAllowed(false);
        supervisorCb.setRequired(true);
        supervisorCb.setData(21);
        supervisorCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        supervisorCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        supervisorCb.setWidth("100%");
        supervisorCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        supervisorCb.setFilteringMode(FilteringMode.CONTAINS);

        Label captionExams = new Label();
        captionExams.setSizeFull();
        captionExams.setContentMode(ContentMode.HTML);
        captionExams.setValue(myUI.getMessage(SptMessages.ExamsSettings));
        captionExams.setStyleName("tableCpt");

        sebatExamLab = new Label();
        sebatExamLab.setSizeUndefined();
        sebatExamLab.setValue(myUI.getMessage(SptMessages.SebatExam));

        sebatExamCb = new ComboBoxMax();
        sebatExamCb.setNullSelectionAllowed(false);
        sebatExamCb.setRequired(true);
        sebatExamCb.setData(5);
        sebatExamCb.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        sebatExamCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        sebatExamCb.setWidth("100%");
        sebatExamCb.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        sebatExamCb.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            adminCb.setContainerDataSource(dbd.exec_for_select(myUI, Settings.hr_positionTable, true));
            hrCb.setContainerDataSource(dbd.exec_for_select(myUI, Settings.hr_positionTable, true));
            supplyManagerCb.setContainerDataSource(dbd.exec_for_select(myUI, Settings.hr_positionTable, true));
            warehouseManagerCb.setContainerDataSource(dbd.exec_for_select(myUI, Settings.hr_positionTable, true));
            sapat_secretaryCb.setContainerDataSource(dbd.exec_for_select(myUI, Settings.hr_positionTable, true));
            directorCb.setContainerDataSource(dbd.exec_for_select(myUI, Settings.hr_positionTable, true));
            accountentCb.setContainerDataSource(dbd.exec_for_select(myUI, Settings.hr_positionTable, true));
            supervisorCb.setContainerDataSource(dbd.exec_for_select(myUI, Settings.hr_positionTable, true));
            sebatExamCb.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbExamTable, true));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        int main_exam = 0;
        try {
            DbExam dbe = new DbExam();
            dbe.connect();
            main_exam = dbe.exec_main_exam();
            dbe.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        adminCb.setValue(positionsContainer.getContainerProperty((Integer) adminCb.getData(),
                Settings.position_id).getValue());
        hrCb.setValue(positionsContainer.getContainerProperty((Integer) hrCb.getData(),
                Settings.position_id).getValue());
        supplyManagerCb.setValue(positionsContainer.getContainerProperty((Integer) supplyManagerCb.getData(),
                Settings.position_id).getValue());
        warehouseManagerCb.setValue(positionsContainer.getContainerProperty((Integer) warehouseManagerCb.getData(),
                Settings.position_id).getValue());
        sapat_secretaryCb.setValue(positionsContainer.getContainerProperty((Integer) sapat_secretaryCb.getData(),
                Settings.position_id).getValue());
        directorCb.setValue(positionsContainer.getContainerProperty((Integer) directorCb.getData(),
                Settings.position_id).getValue());
        accountentCb.setValue(positionsContainer.getContainerProperty((Integer) accountentCb.getData(),
                Settings.position_id).getValue());
        supervisorCb.setValue(positionsContainer.getContainerProperty((Integer) supervisorCb.getData(),
                Settings.position_id).getValue());
        sebatExamCb.setValue(main_exam);

        saveBtn = new Button(myUI.getMessage(SptMessages.SaveButton));
        saveBtn.setStyleName(ValoTheme.BUTTON_SMALL);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);

        addComponent(captionAccounting, 0, 0, 1, 0);
        addComponent(initPayLab, 0, 1);
        addComponent(initPayCb, 1, 1);
        addComponent(currLab, 0, 2);
        addComponent(currCb, 1, 2);
        addComponent(prevLab, 0, 3);
        addComponent(prevCb, 1, 3);
        addComponent(futrLab, 0, 4);
        addComponent(futrCb, 1, 4);
        addComponent(outcomesLab, 0, 5);
        addComponent(outcomesCb, 1, 5);
        addComponent(earlyPayLab, 0, 6);
        addComponent(earlyPayCb, 1, 6);
        addComponent(salaryLab, 0, 7);
        addComponent(salaryCb, 1, 7);
        addComponent(localSalaryLab, 0, 8);
        addComponent(localSalaryCb, 1, 8);
        addComponent(captionStock, 0, 9, 1, 9);
        addComponent(foodLab, 0, 10);
        addComponent(foodCb, 1, 10);
        addComponent(stationeryLab, 0, 11);
        addComponent(stationeryCb, 1, 11);
        addComponent(buildingMaterialsLab, 0, 12);
        addComponent(buildingMaterialsCb, 1, 12);
        addComponent(washingMaterialsLab, 0, 13);
        addComponent(washingMaterialsCb, 1, 13);
        addComponent(otherProductsLab, 0, 14);
        addComponent(otherProductsCb, 1, 14);
        addComponent(captionPositions, 2, 0, 3, 0);
        addComponent(adminLab, 2, 1);
        addComponent(adminCb, 3, 1);
        addComponent(hrLab, 2, 2);
        addComponent(hrCb, 3, 2);
        addComponent(supplyManagerLab, 2, 3);
        addComponent(supplyManagerCb, 3, 3);
        addComponent(directorLab, 2, 4);
        addComponent(directorCb, 3, 4);
        addComponent(accountentLab, 2, 5);
        addComponent(accountentCb, 3, 5);
        addComponent(supervisorLab, 2, 6);
        addComponent(supervisorCb, 3, 6);
        addComponent(warehouseManagerLab, 2, 7);
        addComponent(warehouseManagerCb, 3, 7);
        addComponent(sapat_secretaryLab, 2, 8);
        addComponent(sapat_secretaryCb, 3, 8);
        addComponent(captionExams, 2, 9, 3, 9);
        addComponent(sebatExamLab, 2, 10);
        addComponent(sebatExamCb, 3, 10);
        addComponent(saveBtn, 0, 15, 3, 15);

        setComponentAlignment(initPayLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(currLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(prevLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(futrLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(outcomesLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(earlyPayLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(salaryLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(localSalaryLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(foodLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(stationeryLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(buildingMaterialsLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(washingMaterialsLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(otherProductsLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(adminLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(hrLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(supplyManagerLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(warehouseManagerLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(sapat_secretaryLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(directorLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(accountentLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(supervisorLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(sebatExamLab, Alignment.BOTTOM_LEFT);
        setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == saveBtn) {
            int st = 0;
            if (validate(this)) {
                try {
                    DbPaymentCategory dbpc = new DbPaymentCategory();
                    dbpc.connect();
                    dbpc.exec_update((Integer) initPayCb.getValue(), (Integer) initPayCb.getData());
                    dbpc.exec_update((Integer) currCb.getValue(), (Integer) currCb.getData());
                    dbpc.exec_update((Integer) prevCb.getValue(), (Integer) prevCb.getData());
                    dbpc.exec_update((Integer) futrCb.getValue(), (Integer) futrCb.getData());
                    dbpc.exec_update((Integer) outcomesCb.getValue(), (Integer) outcomesCb.getData());
                    dbpc.exec_update((Integer) earlyPayCb.getValue(), (Integer) earlyPayCb.getData());
                    dbpc.close();

                    DbProductCategories dbprc = new DbProductCategories();
                    dbprc.connect();
                    dbprc.exec_update((Integer) foodCb.getValue(), (Integer) foodCb.getData());
                    dbprc.exec_update((Integer) stationeryCb.getValue(), (Integer) stationeryCb.getData());
                    dbprc.exec_update((Integer) buildingMaterialsCb.getValue(), (Integer) buildingMaterialsCb.getData());
                    dbprc.exec_update((Integer) washingMaterialsCb.getValue(), (Integer) washingMaterialsCb.getData());
                    dbprc.exec_update((Integer) otherProductsCb.getValue(), (Integer) otherProductsCb.getData());
                    dbprc.close();

                    DbSalaryCategories dbsl = new DbSalaryCategories();
                    dbsl.connect();
                    dbsl.exec_update((Integer) salaryCb.getValue(), (Integer) salaryCb.getData());
                    dbsl.exec_update((Integer) localSalaryCb.getValue(), (Integer) localSalaryCb.getData());
                    dbsl.close();

                    DbPosition dbp = new DbPosition();
                    dbp.connect();
                    dbp.exec_update((Integer) adminCb.getValue(), (Integer) adminCb.getData());
                    dbp.exec_update((Integer) directorCb.getValue(), (Integer) directorCb.getData());
                    dbp.exec_update((Integer) accountentCb.getValue(), (Integer) accountentCb.getData());
                    dbp.exec_update((Integer) supervisorCb.getValue(), (Integer) supervisorCb.getData());
                    dbp.exec_update((Integer) hrCb.getValue(), (Integer) hrCb.getData());
                    dbp.exec_update((Integer) supplyManagerCb.getValue(), (Integer) supplyManagerCb.getData());
                    dbp.exec_update((Integer) warehouseManagerCb.getValue(), (Integer) warehouseManagerCb.getData());
                    dbp.exec_update((Integer) sapat_secretaryCb.getValue(), (Integer) sapat_secretaryCb.getData());
                    dbp.close();

                    DbExam dbe = new DbExam();
                    dbe.connect();
                    dbe.exec_update((Integer) sebatExamCb.getValue());
                    dbe.close();
                    st = 1;
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                if (st != 0) {
                    Notification.show(myUI.getMessage(SptMessages.ValueSaved));
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } else {
                Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                        Notification.Type.WARNING_MESSAGE);
            }
        }
    }

    private boolean validate(ComponentContainer layout) {
        boolean result = true;
        Iterator<Component> i = layout.iterator();
        while (i.hasNext()) {
            Component c = i.next();
            if (c instanceof AbstractField) {
                try {
                    ((AbstractField) c).validate();
                } catch (Exception e) {
                    //((AbstractComponent) c).setComponentError(new UserError(e.getMessage()));
                    result = false;
                }
            } else if (c instanceof AbstractComponentContainer) {
                if (!validate((AbstractComponentContainer) c)) {
                    result = false;
                }
            }
        }
        return result;
    }
}
