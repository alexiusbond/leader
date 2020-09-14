package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Upload;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.util.List;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbClassName;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbStudRelative;
import kg.alex.spt.dao.DbStudent;
import kg.alex.spt.dao.DbStudentOrder;
import kg.alex.spt.domain.StudRelative;
import kg.alex.spt.domain.Student;
import kg.alex.spt.domain.StudentExcel;
import kg.alex.spt.excel.ExcelUploader;
import kg.alex.spt.excel.ExcelUploaderSucceededListener;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.crypto.hash.Sha256Hash;

public class ImportFromExcelView extends GridLayout implements Button.ClickListener {

    static final Logger logger = LogManager.getLogger(ImportFromExcelView.class);
    private MyVaadinUI myUI;
    private Button templateBtn, saveBtn;
    private Table dataTable, importTable;
    private Upload upload;
    private SystemSettings sysSettings = new SystemSettings();
    private List list = null;
    private ExcelUploader<?> excelUploader;

    public ImportFromExcelView(MyVaadinUI myUI) {
        this.setSizeFull();
        this.setMargin(true);
        this.setSpacing(true);
        this.setRows(3);
        this.setColumns(3);
        this.myUI = myUI;
        buildBody();
    }

    private void buildBody() {

        templateBtn = new Button(myUI.getMessage(SptMessages.DownloadTemplateBtn));
        templateBtn.setWidth("50%");
        templateBtn.setStyleName(ValoTheme.BUTTON_SMALL);
        templateBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        templateBtn.addClickListener(this);
        this.addComponent(templateBtn);

        upload = new Upload();
        upload.setWidth("50%");
        upload.setButtonCaption(myUI.getMessage(SptMessages.Upload));
        upload.setImmediate(true);
        this.addComponent(upload);

        saveBtn = new Button(myUI.getMessage(SptMessages.SaveButton));
        saveBtn.setWidth("50%");
        saveBtn.setStyleName(ValoTheme.BUTTON_SMALL);
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addClickListener(this);
        this.addComponent(saveBtn);

        importTable = new Table(myUI.getMessage(SptMessages.FileData));
        importTable.setStyleName(ValoTheme.TABLE_COMPACT);
        importTable.setSizeFull();
        this.addComponent(importTable, 0, 1, 2, 1);
        this.setRowExpandRatio(1, 1);

        dataTable = new Table(myUI.getMessage(SptMessages.DbData));
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        this.addComponent(dataTable, 0, 2, 2, 2);
        this.setRowExpandRatio(2, 1);

        setTableData();
        list = null;
        importTable.setContainerDataSource(null);
        excelUploader = new ExcelUploader<StudentExcel>(StudentExcel.class);
        excelUploader.addSucceededListener(new ExcelUploaderSucceededListener() {
            @Override
            public void succeededListener(Upload.SucceededEvent event, List items) {
                list = items;
                importTable.setContainerDataSource(buildContainer(items));
            }
        });
        upload.setReceiver(excelUploader);
        upload.addSucceededListener(excelUploader);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == templateBtn) {
            try {
                myUI.getPage().open(new FileResource(
                        new File(SystemSettings.PATH_TO_UPLOADS + "students_template.xlsx")),
                        "_blank", false);
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == saveBtn) {
            if (list != null && list.size() > 0) {
                insertStudents();
            }
        }
    }

    private void setTableData() {
        try {
            DbStudent dbs = new DbStudent();
            dbs.connect();
            dataTable.setContainerDataSource(dbs.execSQL_for_import(
                    myUI, myUI.getUser().getSchool_id()));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private IndexedContainer buildContainer(List<StudentExcel> list) {

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(
                myUI.getMessage(SptMessages.Id), String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.Firstname), String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.Surname), String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.Middlename), String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.Gender), String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.DateOfBirth), String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.ClassName), String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.Relative), String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.FullName) + " ("
                + myUI.getMessage(SptMessages.Relative) + ")", String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.Passport) + " ("
                + myUI.getMessage(SptMessages.Relative) + ")", String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.WorkPlace) + " ("
                + myUI.getMessage(SptMessages.Relative) + ")", String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.Phone) + " ("
                + myUI.getMessage(SptMessages.Relative) + ")", String.class, null);
        container.addContainerProperty(
                myUI.getMessage(SptMessages.Address) + " ("
                + myUI.getMessage(SptMessages.Relative) + ")", String.class, null);
        for (int i = 0; i < list.size(); i++) {
            Item item = container.addItem(list.get(i).getLogin());
            if (item != null) {
                item.getItemProperty(myUI.getMessage(SptMessages.Id)).setValue(
                        list.get(i).getLogin());
                item.getItemProperty(myUI.getMessage(SptMessages.Firstname)).setValue(
                        list.get(i).getName());
                item.getItemProperty(myUI.getMessage(SptMessages.Surname)).setValue(
                        list.get(i).getSur_name());
                item.getItemProperty(myUI.getMessage(SptMessages.Middlename)).setValue(
                        list.get(i).getMiddle_name());
                item.getItemProperty(myUI.getMessage(SptMessages.Gender)).setValue(
                        list.get(i).getGender());
                item.getItemProperty(myUI.getMessage(SptMessages.DateOfBirth)).setValue(
                        list.get(i).getBirth_date());
                item.getItemProperty(myUI.getMessage(SptMessages.ClassName)).setValue(
                        list.get(i).getClass_name());
                item.getItemProperty(myUI.getMessage(SptMessages.Relative)).setValue(
                        list.get(i).getRelative_type());
                item.getItemProperty(myUI.getMessage(SptMessages.FullName) + " ("
                        + myUI.getMessage(SptMessages.Relative) + ")").setValue(
                        list.get(i).getRelative_fullname());
                item.getItemProperty(myUI.getMessage(SptMessages.Passport) + " ("
                        + myUI.getMessage(SptMessages.Relative) + ")").setValue(
                        list.get(i).getRelative_passport());
                item.getItemProperty(myUI.getMessage(SptMessages.WorkPlace) + " ("
                        + myUI.getMessage(SptMessages.Relative) + ")").setValue(
                        list.get(i).getRelative_work_place());
                item.getItemProperty(myUI.getMessage(SptMessages.Phone) + " ("
                        + myUI.getMessage(SptMessages.Relative) + ")").setValue(
                        list.get(i).getRelative_phone());
                item.getItemProperty(myUI.getMessage(SptMessages.Address) + " ("
                        + myUI.getMessage(SptMessages.Relative) + ")").setValue(
                        list.get(i).getRelative_address());
            }
        }
        return container;
    }

    private void insertStudents() {
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            IndexedContainer genderCont = dbd.execSQL(myUI, sysSettings.dbGender, false, true);
            IndexedContainer relativeCont = dbd.execSQL(myUI, sysSettings.dbRelatives, false, true);
            dbd.close();
            DbClassName dbcl = new DbClassName();
            dbcl.connect();
            IndexedContainer classCont = dbcl.execClass_for_import(myUI, myUI.getUser().getSchool_id());
            dbcl.close();
            DbStudent dbs = new DbStudent();
            dbs.connect();
            DbStudRelative dbsr = new DbStudRelative();
            dbsr.connect();
            int counter = 0;
            for (int i = 0; i < list.size(); i++) {
                StudentExcel stExcel = (StudentExcel) list.get(i);
                Student student = new Student();
                Property prop = genderCont.getContainerProperty(stExcel.getGender(), sysSettings.id);
                if (prop != null) {
                    student.setGender_id((Integer) prop.getValue());
                }
                prop = classCont.getContainerProperty(stExcel.getClass_name(), sysSettings.id);
                if (prop != null) {
                    student.setClass_name_id((Integer) prop.getValue());
                }
                try {
                    student.setBirth_date(sysSettings.df.parse(stExcel.getBirth_date()));
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                student.setLogin(stExcel.getLogin());
                student.setName(stExcel.getName());
                student.setSur_name(stExcel.getSur_name());
                student.setMiddle_name(stExcel.getMiddle_name());
                student.setEdu_status_id(1);
                student.setPassword(new Sha256Hash(stExcel.getLogin()).toString());
                student.setEntering_year_id(myUI.getUser().getCurrent_year().getId());
                student.setEmployee_id(myUI.getUser().getId());
                student.setSchool_id(myUI.getUser().getSchool_id());
                StudRelative relative = new StudRelative();
                prop = relativeCont.getContainerProperty(stExcel.getRelative_type(), sysSettings.id);
                if (prop != null) {
                    relative.setRelatives_id((Integer) prop.getValue());
                }
                relative.setFullname(stExcel.getRelative_fullname());
                relative.setPassport(stExcel.getRelative_passport());
                relative.setWork_place(stExcel.getRelative_work_place());
                relative.setPhone(stExcel.getRelative_phone());
                relative.setAddress(stExcel.getRelative_address());
                relative.setIs_main(1);
                int st_id = 0;
                if (student.getLogin() != null && !student.getLogin().equals("")
                        && student.getName() != null && !student.getName().equals("")
                        && student.getSur_name() != null && !student.getSur_name().equals("")
                        && student.getGender_id() != 0 && student.getClass_name_id() != 0
                        && student.getBirth_date() != null && relative.getRelatives_id() != 0
                        && relative.getFullname() != null && !relative.getFullname().equals("")
                        && relative.getPassport() != null && !relative.getPassport().equals("")
                        && relative.getWork_place() != null && !relative.getWork_place().equals("")
                        && relative.getPhone() != null && !relative.getPhone().equals("")
                        && relative.getAddress() != null && !relative.getAddress().equals("")) {
                    st_id = dbs.exec_insert(student);
                }
                if (st_id != 0) {
                    try {
                        DbStudentOrder dbso = new DbStudentOrder();
                        dbso.connect();
                        dbso.insertNewStOrder(st_id, student.getClass_name_id(),
                                myUI.getUser().getCurrent_year().getId(), myUI.getUser().getId());
                        dbso.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    relative.setStudent_id(st_id);
                    st_id = dbsr.exec_insert(relative);
                    if (st_id != 0) {
                        counter++;
                        importTable.removeItem(stExcel.getLogin());
                    }
                }
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
            dbs.close();
            dbsr.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
