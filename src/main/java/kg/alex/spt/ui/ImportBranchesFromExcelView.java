package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbBranch;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbEmployee;
import kg.alex.spt.dao.DbEmployeeLessons;
import kg.alex.spt.domain.EmployeeBranchesExcel;
import kg.alex.spt.domain.EmployeeLessons;
import kg.alex.spt.excel.ExcelUploader;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.pdf.BranchCodesPdf;
import kg.alex.spt.utils.ComboBoxMax;
import kg.alex.spt.utils.FormattedFilterTable;
import kg.alex.spt.utils.FormattedTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImportBranchesFromExcelView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(ImportBranchesFromExcelView.class);
    private MyVaadinUI myUI;
    private Button deleteBtn, saveBtn, templateBtn, branchesPDF_Btn;
    private ComboBoxMax yearSelect;
    private FormattedFilterTable dataTable;
    private FormattedTable importTable;
    private Subject currentUser = SecurityUtils.getSubject();
    private VerticalLayout settingsLay;
    private Upload fileUpl;
    private ExcelUploader<?> excelUploader;
    private List list = null;

    public ImportBranchesFromExcelView(MyVaadinUI myUI) {
        this.myUI = myUI;

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setSpacing(true);
        vl.setMargin(true);

        Label captionFromFile = new Label();
        captionFromFile.setWidth(Settings.PERCENTS100);
        captionFromFile.setContentMode(ContentMode.HTML);
        captionFromFile.setValue(myUI.getMessage(SptMessages.FileData));
        captionFromFile.setStyleName("tableCpt");
        vl.addComponent(captionFromFile);

        importTable = new FormattedTable();
        importTable.setStyleName(ValoTheme.TABLE_COMPACT);
        importTable.setSizeFull();
        vl.addComponent(importTable);
        vl.setExpandRatio(importTable, 1);

        Label captionFromDb = new Label();
        captionFromDb.setWidth(Settings.PERCENTS100);
        captionFromDb.setContentMode(ContentMode.HTML);
        captionFromDb.setValue(myUI.getMessage(SptMessages.DbData));
        captionFromDb.setStyleName("tableCpt");
        vl.addComponent(captionFromDb);

        dataTable = new FormattedFilterTable();
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        vl.addComponent(dataTable);
        vl.setExpandRatio(dataTable, 1);

        buildSettingsLayout();

        this.setSplitPosition(24, Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(vl);
    }

    private void setTableData() {
        try {
            DbEmployeeLessons dbCon = new DbEmployeeLessons();
            dbCon.connect();
            dataTable.setContainerDataSource(dbCon.execSQL(myUI, (Integer) yearSelect.getValue(), myUI.getUser().getSchool_id()));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns(new String[]{
                myUI.getMessage(SptMessages.Lecturer), myUI.getMessage(SptMessages.Lesson),
                myUI.getMessage(SptMessages.ClassName),
                myUI.getMessage(SptMessages.Hours),
                myUI.getMessage(SptMessages.ExtraHours)});
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.ClassName), CustomTable.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.Hours), CustomTable.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(SptMessages.ExtraHours), CustomTable.Align.RIGHT);
    }

    private void buildSettingsLayout() {

        settingsLay = new VerticalLayout();
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setWidth(Settings.PERCENTS100);

        HorizontalLayout buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);

        saveBtn = new Button();
        saveBtn.setDescription(myUI.getMessage(SptMessages.SaveButton));
        saveBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);
        saveBtn.setEnabled(currentUser.isPermitted(Settings.cnImportBranchesFromExcelView + ":" + Settings.actImport));
        buttonsLay.addComponent(saveBtn);

        templateBtn = new Button();
        templateBtn.setDescription(myUI.getMessage(SptMessages.DownloadTemplateBtn));
        templateBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        templateBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        templateBtn.addClickListener(this);
        buttonsLay.addComponent(templateBtn);

        branchesPDF_Btn = new Button();
        branchesPDF_Btn.setDescription(myUI.getMessage(SptMessages.DownloadBranchesCode));
        branchesPDF_Btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        branchesPDF_Btn.setIcon(FontAwesome.FILE_PDF_O);
        branchesPDF_Btn.addClickListener(this);
        buttonsLay.addComponent(branchesPDF_Btn);

        deleteBtn = new Button();
        deleteBtn.setEnabled(false);
        deleteBtn.setDescription(myUI.getMessage(SptMessages.DeleteAllButton));
        deleteBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.setIcon(FontAwesome.TRASH_O);
        deleteBtn.addClickListener(this);
        deleteBtn.setEnabled(currentUser.isPermitted(Settings.cnImportBranchesFromExcelView + ":" + Settings.actDelete));
        buttonsLay.addComponent(deleteBtn);

        excelUploader = new ExcelUploader<>(EmployeeBranchesExcel.class);
        excelUploader.addSucceededListener((event, items) -> {
            list = items;
            importTable.setContainerDataSource(buildContainer(items));
            importTable.setColumnAlignment(myUI.getMessage(SptMessages.Hours), Table.Align.RIGHT);
            importTable.setColumnAlignment(myUI.getMessage(SptMessages.ExtraHours), Table.Align.RIGHT);
            importTable.setColumnAlignment(myUI.getMessage(SptMessages.ClassName), Table.Align.RIGHT);
        });

        fileUpl = new Upload(null, excelUploader);
        fileUpl.setImmediate(true);
        fileUpl.setStyleName("large");
        fileUpl.setButtonCaption(myUI.getMessage(SptMessages.UploadExcel));
        fileUpl.setDescription(myUI.getMessage(SptMessages.UploadExcel));
        fileUpl.addSucceededListener(excelUploader);
        buttonsLay.addComponent(fileUpl);

        yearSelect = new ComboBoxMax(myUI.getMessage(SptMessages.LessonsYear));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);
        yearSelect.addValueChangeListener(this);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            yearSelect.setContainerDataSource(dbDef.exec_for_select(myUI, Settings.dbYear, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        settingsLay.addComponent(buttonsLay);
        settingsLay.addComponent(yearSelect);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == deleteBtn && yearSelect.getValue() != null) {
            ConfirmDialog.show(myUI, myUI.getMessage(SptMessages.Question),
                    myUI.getMessage(SptMessages.ConfirmDeletionAll) + " " + yearSelect.getItemCaption(yearSelect.getValue()) + " "
                            + myUI.getMessage(SptMessages.Year) + "?",
                    myUI.getMessage(SptMessages.Yes),
                    myUI.getMessage(SptMessages.No),
                    (ConfirmDialog.Listener) dialog -> {
                        if (dialog.isConfirmed()) {
                            execDelete();
                            setTableData();
                        }
                    });
        } else if (source == saveBtn) {
            List itemsToDel = new ArrayList();
            int counter = 0;
            try {
                DbEmployee dbEmp = new DbEmployee();
                dbEmp.connect();
                DbEmployeeLessons dbEl = new DbEmployeeLessons();
                dbEl.connect();
                DbDefinition dbDef = new DbDefinition();
                dbDef.connect();
                Iterator iter = importTable.getItemIds().iterator();
                while (iter.hasNext()) {
                    Object item_id = iter.next();
                    EmployeeLessons el = new EmployeeLessons();
                    el.setSchool_id(myUI.getUser().getSchool_id());
                    el.setYear_id((Integer) yearSelect.getValue());
                    if (importTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.Hours)).getValue() != null) {
                        el.setHours((Integer) importTable.getContainerProperty(item_id,
                                myUI.getMessage(SptMessages.Hours)).getValue());
                    } else {
                        continue;
                    }
                    if (importTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.ExtraHours)).getValue() != null) {
                        el.setExtra_hours((Integer) importTable.getContainerProperty(item_id,
                                myUI.getMessage(SptMessages.ExtraHours)).getValue());
                    } else {
                        el.setExtra_hours(0);
                    }
                    if (importTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.LecturerID)).getValue() != null) {
                        int id = 0;
                        try {
                            id = dbEmp.execSQL_id(myUI.getUser().getSchool_id(), importTable.getContainerProperty(item_id,
                                    myUI.getMessage(SptMessages.LecturerID)).getValue().toString());
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (id != 0) {
                            el.setEmployee_id(id);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    if (importTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.CourseCode)).getValue() != null) {
                        int id = 0;
                        try {
                            id = dbDef.search_id(Settings.dbBranchTable, Settings.dbColumnCode, importTable.getContainerProperty(item_id,
                                    myUI.getMessage(SptMessages.CourseCode)).getValue().toString());
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (id != 0) {
                            el.setBranch_id(id);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    if (importTable.getContainerProperty(item_id, myUI.getMessage(SptMessages.ClassName)).getValue() != null) {
                        int id = 0;
                        try {
                            id = dbDef.search_id(Settings.classTable, Settings.dbColumnName, importTable.getContainerProperty(item_id,
                                    myUI.getMessage(SptMessages.ClassName)).getValue().toString());
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (id != 0) {
                            el.setClass_number_id(id);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    try {
                        if (dbEl.exec_insert(el) != 0) {
                            counter++;
                            itemsToDel.add(item_id);
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                }
                dbEl.close();
                dbEmp.close();
                dbDef.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            for (Object id : itemsToDel) {
                importTable.removeItem(id);
            }
            if (importTable.size() != 0) {
                Notification.show(counter + " " + myUI.getMessage(SptMessages.InsertedAmount)
                                + " " + myUI.getMessage(SptMessages.NotInserted),
                        Notification.Type.WARNING_MESSAGE);
            } else {
                Notification.show(counter + " " + myUI.getMessage(SptMessages.InsertedAmount),
                        Notification.Type.HUMANIZED_MESSAGE);
            }
            if (counter > 0) {
                setTableData();
            }
        } else if (source == branchesPDF_Btn) {
            try {
                DbBranch dbCon = new DbBranch();
                dbCon.connect();
                new BranchCodesPdf(myUI, dbCon.execSQL(myUI));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == templateBtn) {
            try {
                myUI.getPage().open(new FileResource(
                                new File(Settings.PATH_TO_UPLOADS + "spt_hr_branches.xlsx")),
                        "_blank", false);
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == yearSelect) {
            if (yearSelect.getValue() != null) {
                setTableData();
            }
        }
    }

    private void execDelete() {
        try {
            DbEmployeeLessons dbCon = new DbEmployeeLessons();
            dbCon.connect();
            dbCon.exec_delete(myUI.getUser().getSchool_id(), (Integer) yearSelect.getValue());
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private IndexedContainer buildContainer(List<EmployeeBranchesExcel> list) {

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.LecturerID), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.CourseCode), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.ClassName), Integer.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Hours), Integer.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.ExtraHours), Integer.class, null);
        for (int i = 0; i < list.size(); i++) {
            Item item = container.addItem(i);
            if (item != null) {
                item.getItemProperty(myUI.getMessage(SptMessages.LecturerID)).setValue(list.get(i).getLogin());
                item.getItemProperty(myUI.getMessage(SptMessages.CourseCode)).setValue(
                        list.get(i).getCourse_code());
                try {
                    item.getItemProperty(myUI.getMessage(SptMessages.Hours)).setValue(
                            Integer.parseInt(list.get(i).getHours()));
                } catch (Exception e) {
                }
                try {
                    item.getItemProperty(myUI.getMessage(SptMessages.ExtraHours)).setValue(
                            Integer.parseInt(list.get(i).getExtra_hours()));
                } catch (Exception e) {
                }
                try {
                    item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                            Integer.parseInt(list.get(i).getClass_number()));
                } catch (Exception e) {
                }
            }
        }
        return container;
    }

    public Component getNewObj() {
        return new ImportBranchesFromExcelView(myUI);
    }
}
