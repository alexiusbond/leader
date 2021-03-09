package kg.alex.spt.ui;

import kg.alex.spt.utils.ComboBoxMax;
import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;

import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbSalaryCategories;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.domain.School;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class SchoolModificationView extends GridLayout implements Button.ClickListener {

    static final Logger logger = LogManager.getLogger(SchoolModificationView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBoxMax statusSelect;
    private TextField nameKgTF, nameEnTF, codeTF, nameRuTF, directorFullNameTF, addressTF,
            innTF, bankTF, bankAccountTF, phoneTF, cityTF;
    private SystemSettings sysSettings = new SystemSettings();
    private int school_id;
    private Upload photoUpl;
    private File myFile;
    private Window statusWindow;
    private Button cancelButton;
    private Label state, textualProgress, fName;
    private ProgressIndicator pi;
    private String photoName, mimeType;
    private MyReceiver receiver;
    private Embedded photoEmb;
    private Subject currentUser = SecurityUtils.getSubject();
    private School school = new School();

    public SchoolModificationView(MyVaadinUI myUI, int scl_id) {
        this.myUI = myUI;
        this.school_id = scl_id;

        this.setRows(7);
        this.setColumns(3);
        this.setWidth("60%");
        this.setSpacing(true);
        this.setMargin(true);

        HorizontalLayout buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);

        modifyBtn = new Button();
        modifyBtn.setEnabled(false);
        modifyBtn.setDescription(myUI.getMessage(SptMessages.ModifyButton));
        modifyBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        modifyBtn.setIcon(FontAwesome.PENCIL);
        modifyBtn.addClickListener(this);
        buttonsLay.addComponent(modifyBtn);

        createBtn = new Button();
        createBtn.setEnabled(false);
        createBtn.setDescription(myUI.getMessage(SptMessages.CreateButton));
        createBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        createBtn.setIcon(FontAwesome.FILE_O);
        createBtn.addClickListener(this);
        buttonsLay.addComponent(createBtn);

        deleteBtn = new Button();
        deleteBtn.setEnabled(false);
        deleteBtn.setDescription(myUI.getMessage(SptMessages.DeleteButton));
        deleteBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.setIcon(FontAwesome.TRASH_O);
        deleteBtn.addClickListener(this);
        buttonsLay.addComponent(deleteBtn);

        saveBtn = new Button();
        saveBtn.setDescription(myUI.getMessage(SptMessages.SaveButton));
        saveBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);
        buttonsLay.addComponent(saveBtn);

        cancelBtn = new Button();
        cancelBtn.setDescription(myUI.getMessage(SptMessages.CancelButton));
        cancelBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        cancelBtn.setIcon(FontAwesome.BAN);
        cancelBtn.addClickListener(this);
        buttonsLay.addComponent(cancelBtn);

        codeTF = new TextField(myUI.getMessage(SptMessages.Code));
        codeTF.setRequired(true);
        codeTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        codeTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        codeTF.setWidth("100%");

        nameRuTF = new TextField(myUI.getMessage(SptMessages.NameRu));
        nameRuTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        nameRuTF.setRequired(true);
        nameRuTF.setNullRepresentation("");
        nameRuTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameRuTF.setWidth("100%");

        nameKgTF = new TextField(myUI.getMessage(SptMessages.NameKg));
        nameKgTF.setNullRepresentation("");
        nameKgTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameKgTF.setWidth("100%");

        nameEnTF = new TextField(myUI.getMessage(SptMessages.NameEn));
        nameEnTF.setNullRepresentation("");
        nameEnTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameEnTF.setWidth("100%");

        directorFullNameTF = new TextField(myUI.getMessage(SptMessages.DirectorFullName) + " (Ru/Kg)");
        directorFullNameTF.setRequired(false);
        directorFullNameTF.setNullRepresentation("");
        directorFullNameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        directorFullNameTF.setWidth("100%");

        cityTF = new TextField(myUI.getMessage(SptMessages.City) + " (Ru/Kg)");
        cityTF.setRequired(false);
        cityTF.setNullRepresentation("");
        cityTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        cityTF.setWidth("100%");

        addressTF = new TextField(myUI.getMessage(SptMessages.Address) + " (Ru/Kg)");
        addressTF.setRequired(false);
        addressTF.setNullRepresentation("");
        addressTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        addressTF.setWidth("100%");

        innTF = new TextField(myUI.getMessage(SptMessages.INN));
        innTF.setRequired(false);
        innTF.setNullRepresentation("");
        innTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        innTF.setWidth("100%");

        bankTF = new TextField(myUI.getMessage(SptMessages.Bank) + " (Ru/Kg)");
        bankTF.setRequired(false);
        bankTF.setNullRepresentation("");
        bankTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        bankTF.setWidth("100%");

        bankAccountTF = new TextField(myUI.getMessage(SptMessages.BankAccount));
        bankAccountTF.setRequired(false);
        bankAccountTF.setNullRepresentation("");
        bankAccountTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        bankAccountTF.setWidth("100%");

        phoneTF = new TextField(myUI.getMessage(SptMessages.Phone));
        phoneTF.setRequired(false);
        phoneTF.setNullRepresentation("");
        phoneTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        phoneTF.setWidth("100%");

        statusSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Status));
        statusSelect.setNullSelectionAllowed(false);
        statusSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        statusSelect.setRequired(true);
        statusSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        statusSelect.setWidth("100%");
        statusSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Name));
        statusSelect.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            statusSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, sysSettings.dbActivity_status));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        photoEmb = new Embedded();
        photoEmb.setHeight("100%");
        photoEmb.setWidth("95%");
        photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoEmb.setImmediate(true);
        buildUpload();

        this.addComponent(buttonsLay, 0, 0, 1, 0);
        this.addComponent(codeTF, 0, 1);
        this.addComponent(nameRuTF, 1, 1);
        this.addComponent(nameKgTF, 0, 2);
        this.addComponent(nameEnTF, 1, 2);
        this.addComponent(directorFullNameTF, 0, 3);
        this.addComponent(cityTF, 1, 3);
        this.addComponent(addressTF, 0, 4);
        this.addComponent(phoneTF, 1, 4);
        this.addComponent(bankTF, 0, 5);
        this.addComponent(bankAccountTF, 1, 5);
        this.addComponent(innTF, 0, 6);
        this.addComponent(statusSelect, 1, 6);
        this.addComponent(photoEmb, 2, 1, 2, 3);
        this.addComponent(photoUpl, 2, 4);

        fillFields(school_id);
        prepareNormalMode();
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == modifyBtn) {
            prepareModificationMode();
        } else if (source == saveBtn) {
            try {
                if (validate(this)) {
                    DbSchool dbScl = new DbSchool();
                    dbScl.connect();
                    School sch = getSchool(school_id);
                    int status = 0;
                    try {
                        status = dbScl.exec_update(sch);
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    if (status != 0) {
                        if (!sch.getCode().equals(school.getCode()) || !sch.getName_ru().equals(school.getName_ru())) {
                            DbSalaryCategories dbsc = new DbSalaryCategories();
                            dbsc.connect();
                            IndexedContainer salCont = dbsc.execSQL(myUI);
                            dbsc.close();
                            DbAccCategory dba = new DbAccCategory();
                            dba.connect();
                            Iterator iter = salCont.getItemIds().iterator();
                            while (iter.hasNext()) {
                                Object next = iter.next();
                                int id = dba.exec_id((Integer) next, sch.getId());
                                dba.exec_update_code(id, sch.getCode(), salCont.getContainerProperty(next,
                                        myUI.getMessage(SptMessages.Name)).getValue() + " - " + sch.getName_ru());
                                dba.exec_update_all_parent_codes(id, salCont.getContainerProperty(next,
                                        myUI.getMessage(SptMessages.Code)).getValue() + "." + sch.getCode(), false);
                            }
                            dba.close();
                        }
                        Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                Notification.Type.HUMANIZED_MESSAGE);
                        myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool_id(),
                                myUI.getMessage(SptMessages.Name)).setValue(codeTF.getValue() + " - " + nameRuTF.getValue());
                        myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool_id(),
                                myUI.getMessage(SptMessages.Logo)).setValue(sch.getPhoto());
                    } else {
                        Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                Notification.Type.WARNING_MESSAGE);
                    }
                    dbScl.close();
                    prepareNormalMode();
                } else {
                    Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == cancelBtn) {
            prepareNormalMode();
        }
    }

    private void prepareModificationMode() {
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        nameKgTF.setEnabled(true);
        nameEnTF.setEnabled(true);
        statusSelect.setEnabled(false);
        codeTF.setEnabled(false);
        nameRuTF.setEnabled(true);
        directorFullNameTF.setEnabled(true);
        cityTF.setEnabled(true);
        innTF.setEnabled(true);
        addressTF.setEnabled(true);
        bankTF.setEnabled(true);
        bankAccountTF.setEnabled(true);
        phoneTF.setEnabled(true);
        photoUpl.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(sysSettings.cnSchoolModificationView + ":" + sysSettings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(sysSettings.cnSchoolModificationView + ":" + sysSettings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(sysSettings.cnSchoolModificationView + ":" + sysSettings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        nameKgTF.setEnabled(false);
        nameEnTF.setEnabled(false);
        statusSelect.setEnabled(false);
        codeTF.setEnabled(false);
        nameRuTF.setEnabled(false);
        directorFullNameTF.setEnabled(false);
        cityTF.setEnabled(false);
        innTF.setEnabled(false);
        addressTF.setEnabled(false);
        bankTF.setEnabled(false);
        bankAccountTF.setEnabled(false);
        phoneTF.setEnabled(false);
        photoUpl.setEnabled(false);

    }

    private void fillFields(int school_id) {

        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            school = dbs.execSchool(school_id);
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        codeTF.setValue(school.getCode());
        nameKgTF.setValue(school.getName_kg());
        nameEnTF.setValue(school.getName_en());
        nameRuTF.setValue(school.getName_ru());
        directorFullNameTF.setValue(school.getDirector_f_name());
        cityTF.setValue(school.getCity());
        addressTF.setValue(school.getAddress());
        innTF.setValue(school.getInn());
        bankTF.setValue(school.getBank());
        bankAccountTF.setValue(school.getBank_account());
        phoneTF.setValue(school.getPhone());
        statusSelect.setValue(school.getStatus_id());
        if (school.getPhoto() != null && !school.getPhoto().equals("")) {
            photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS
                    + school.getPhoto())));
            photoName = school.getPhoto();
        } else {
            photoName = null;
        }
    }

    private School getSchool(int i) {
        School s = new School();
        s.setId(i);
        s.setCode(codeTF.getValue());
        s.setName_kg(nameKgTF.getValue());
        s.setName_en(nameEnTF.getValue());
        s.setName_ru(nameRuTF.getValue());
        s.setDirector_f_name(directorFullNameTF.getValue());
        s.setCity(cityTF.getValue());
        s.setAddress(addressTF.getValue());
        s.setInn(innTF.getValue());
        s.setBank(bankTF.getValue());
        s.setBank_account(bankAccountTF.getValue());
        s.setPhone(phoneTF.getValue());
        s.setStatus_id((Integer) statusSelect.getValue());
        s.setSchool_type_id(1);
        s.setYear_id(myUI.getUser().getCurrent_year().getId());
        s.setPhoto(photoName);
        return s;
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

    public class MyReceiver implements Upload.Receiver {

        @Override
        public OutputStream receiveUpload(String filename, String mimetype) {
            mimeType = mimetype;
            FileOutputStream fos; // Output stream to write to
            photoName = codeTF.getValue() + ".jpg";
            try {
                myFile = new File(SystemSettings.PATH_TO_UPLOADS + photoName);

                // Open the file for writing.
                fos = new FileOutputStream(myFile);
            } catch (Exception e) {
                // Error while opening the file. Not reported here.
                logger.error(e);
                logger.catching(e);
                return null;
            }
            return fos; // Return the output stream to write tou
        }
    }

    private void buildUploadWindow() {
        state = new Label();
        textualProgress = new Label();
        fName = new Label();
        pi = new ProgressIndicator();

        statusWindow = new Window(myUI.getMessage(SptMessages.UploadStatus));
        statusWindow.setResizable(false);
        statusWindow.setDraggable(false);
        statusWindow.addStyleName("upload-info");

        final FormLayout l = new FormLayout();
        statusWindow.setContent(l);

        l.setMargin(true);

        final HorizontalLayout stateLayout = new HorizontalLayout();
        stateLayout.setSpacing(true);
        stateLayout.addComponent(state);

        cancelButton = new Button(myUI.getMessage(SptMessages.Cancel));
        cancelButton.addClickListener(this);
        cancelButton.setVisible(false);
        cancelButton.setStyleName("small");
        stateLayout.addComponent(cancelButton);

        stateLayout.setCaption(myUI.getMessage(SptMessages.CurrentState));
        state.setValue(myUI.getMessage(SptMessages.Idle));
        l.addComponent(stateLayout);

        fName.setCaption(myUI.getMessage(SptMessages.FileName));
        l.addComponent(fName);

        pi.setCaption(myUI.getMessage(SptMessages.Progress));
        pi.setVisible(false);
        l.addComponent(pi);

        textualProgress.setVisible(false);
        l.addComponent(textualProgress);

    }

    private void buildUpload() {
        receiver = new MyReceiver();
        photoUpl = new Upload(null, receiver);
        photoUpl.setImmediate(true);
        photoUpl.setStyleName(ValoTheme.BUTTON_SMALL);
        photoUpl.setButtonCaption(myUI.getMessage(SptMessages.Upload));
        photoUpl.setWidth("100%");

        photoUpl.addStartedListener(new Upload.StartedListener() {
            @Override
            public void uploadStarted(Upload.StartedEvent event) {
                // This method gets called immediatedly after upload is started

                buildUploadWindow();
                myUI.addWindow(statusWindow);
                statusWindow.setClosable(false);

                pi.setValue(0f);
                pi.setVisible(true);
                pi.setPollingInterval(500); // hit server frequantly to get
                textualProgress.setVisible(true);
                // updates to client
                state.setValue(myUI.getMessage(SptMessages.Uploading));
                fName.setValue(event.getFilename());

                cancelButton.setVisible(true);

            }
        });

        photoUpl.addProgressListener(new Upload.ProgressListener() {
            @Override
            public void updateProgress(long readBytes, long contentLength) {
                // This method gets called several times during the update
                if (!mimeType.equals("image/jpeg")) {
                    photoUpl.interruptUpload();
                    myFile.delete();
                    photoName = null;
                    Notification.show(myUI.getMessage(SptMessages.OnlyJpg),
                            Notification.Type.WARNING_MESSAGE);
                } else if (contentLength >= 5000000) {
                    photoUpl.interruptUpload();
                    myFile.delete();
                    photoName = null;
                    Notification.show(myUI.getMessage(SptMessages.Maxsize),
                            Notification.Type.WARNING_MESSAGE);
                } else {

                    pi.setValue(new Float(readBytes / (float) contentLength));
                    textualProgress.setValue(myUI.getMessage(SptMessages.Processed) + " "
                            + readBytes + " " + myUI.getMessage(SptMessages.BytesOf) + " "
                            + contentLength);
                }
            }
        });

        photoUpl.addSucceededListener(new Upload.SucceededListener() {
            @Override
            public void uploadSucceeded(Upload.SucceededEvent event) {
                // This method gets called when the upload finished successfully
                photoEmb.setSource(new FileResource(myFile));
            }
        });

        photoUpl.addFailedListener(new Upload.FailedListener() {
            @Override
            public void uploadFailed(Upload.FailedEvent event) {
            }
        });

        photoUpl.addFinishedListener(new Upload.FinishedListener() {
            @Override
            public void uploadFinished(Upload.FinishedEvent event) {
                if (statusWindow != null) {
                    statusWindow.close();
                }
            }
        });
    }
}
