package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbMessage;
import kg.alex.spt.domain.Messages;
import kg.alex.spt.i18n.SptMessages;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

public class MessagesView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(MessagesView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private Table dataTable;
    private TextField subjectTF;
    private TextArea messageTA, feedbackTA;
    private boolean isNew;
    private SystemSettings sysSettings = new SystemSettings();
    private String[] NATURAL_COL_ORDER;
    private Upload photoUpl;
    private File myFile;
    private Window statusWindow;
    private Button cancelButton;
    private Label state, textualProgress, fName;
    private ProgressIndicator pi;
    private String photoName, mimeType;
    private MyReceiver receiver;
    private Embedded photoEmb;
    private VerticalLayout settingsLay;
    private Subject currentUser = SecurityUtils.getSubject();

    public MessagesView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Date),
            myUI.getMessage(SptMessages.Employee), myUI.getMessage(SptMessages.School),
            myUI.getMessage(SptMessages.Subject), myUI.getMessage(SptMessages.Message),
            myUI.getMessage(SptMessages.Feedback)};
        buildSettingsLayout();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new Table();
        dataTable.setStyleName(ValoTheme.TABLE_SMALL);
        dataTable.addStyleName("noWrap");
        dataTable.setSizeFull();
        dataTable.setSelectable(true);
        dataTable.addValueChangeListener(this);
        try {
            DbMessage dbmsg = new DbMessage();
            dbmsg.connect();
            dataTable.setContainerDataSource(dbmsg.execSQL(myUI));
            dataTable.setVisibleColumns(NATURAL_COL_ORDER);
            dbmsg.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (dataTable.getContainerDataSource().size() != 0) {
            dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
        }
        dataTable.setNullSelectionAllowed(false);
        dataTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Message), 1);
        dataTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Feedback), 1);

        vl.addComponent(dataTable);

        this.setSplitPosition(24, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(vl);

        prepareNormalMode();
    }

    private void buildSettingsLayout() {

        settingsLay = new VerticalLayout();
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setSizeFull();

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
        settingsLay.addComponent(buttonsLay);

        subjectTF = new TextField(myUI.getMessage(SptMessages.Subject));
        subjectTF.setRequired(true);
        subjectTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        subjectTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        subjectTF.setWidth("100%");
        settingsLay.addComponent(subjectTF);

        messageTA = new TextArea(myUI.getMessage(SptMessages.Message));
        messageTA.setRequired(true);
        messageTA.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        messageTA.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        messageTA.setWidth("100%");
        messageTA.setRows(4);
        settingsLay.addComponent(messageTA);

        feedbackTA = new TextArea(myUI.getMessage(SptMessages.Feedback));
        feedbackTA.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        feedbackTA.setWidth("100%");
        feedbackTA.setRows(4);
        settingsLay.addComponent(feedbackTA);

        buildUpload();
        settingsLay.addComponent(photoUpl);

        photoEmb = new Embedded();
        photoEmb.setWidth("95%");
        photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS + "no_image.png")));
        photoEmb.setImmediate(true);
        settingsLay.addComponent(photoEmb);
        settingsLay.setComponentAlignment(photoEmb, Alignment.MIDDLE_CENTER);
        settingsLay.setExpandRatio(photoEmb, 1);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == modifyBtn && dataTable.getValue() != null) {
            isNew = false;
            fillFields();
            prepareModificationMode();
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
            subjectTF.focus();
        } else if (source == deleteBtn && dataTable.getValue() != null) {
            ConfirmDialog.show(myUI, myUI.getMessage(SptMessages.Question),
                    myUI.getMessage(SptMessages.ConfirmDeletion),
                    myUI.getMessage(SptMessages.Yes),
                    myUI.getMessage(SptMessages.No),
                    new ConfirmDialog.Listener() {
                @Override
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        execDelete();
                    }
                }
            });
        } else if (source == saveBtn) {
            try {
                if (validate(settingsLay)) {
                    DbMessage dbMsg = new DbMessage();
                    dbMsg.connect();
                    if (isNew) {
                        int id = dbMsg.exec_insert(getMessage(0));
                        if (id != 0) {
                            addDatacontainerItem(id);
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        int status = 0;
                        try {
                            status = dbMsg.exec_update(
                                    getMessage((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                            sysSettings.id).getValue()));
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (status != 0) {
                            updateDatacontainer();
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    }
                    dbMsg.close();
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
            if (dataTable.getValue() != null) {
                fillFields();
            }
            prepareNormalMode();
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == dataTable) {
            if (dataTable.getItem(dataTable.getValue()) != null) {
                clearFields();
                fillFields();
                if (currentUser.hasRole("admin") || (myUI.getUser().getId()
                        == (Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                sysSettings.employee_id).getValue()
                        && dataTable.getContainerProperty(dataTable.getValue(),
                                myUI.getMessage(SptMessages.Feedback)).getValue() == null)) {
                    modifyBtn.setEnabled(true);
                    deleteBtn.setEnabled(true);
                } else {
                    modifyBtn.setEnabled(false);
                    deleteBtn.setEnabled(false);
                }
            }
        }
    }

    private void prepareModificationMode() {
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        dataTable.setEnabled(false);
        subjectTF.setEnabled(true);
        messageTA.setEnabled(true);
        if (currentUser.hasRole("admin")) {
            feedbackTA.setEnabled(true);
        }
        photoUpl.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(sysSettings.cnMessagesView + ":" + sysSettings.actModify)) {
            if (currentUser.hasRole("admin") || (myUI.getUser().getId()
                    == (Integer) dataTable.getContainerProperty(dataTable.getValue(),
                            sysSettings.employee_id).getValue()
                    && dataTable.getContainerProperty(dataTable.getValue(),
                            myUI.getMessage(SptMessages.Feedback)).getValue() == null)) {
                modifyBtn.setEnabled(true);
            }
        }
        if (currentUser.isPermitted(sysSettings.cnMessagesView + ":" + sysSettings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(sysSettings.cnMessagesView + ":" + sysSettings.actDelete)) {
            if (currentUser.hasRole("admin") || (myUI.getUser().getId()
                    == (Integer) dataTable.getContainerProperty(dataTable.getValue(),
                            sysSettings.employee_id).getValue()
                    && dataTable.getContainerProperty(dataTable.getValue(),
                            myUI.getMessage(SptMessages.Feedback)).getValue() == null)) {
                deleteBtn.setEnabled(true);
            }
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        subjectTF.setEnabled(false);
        messageTA.setEnabled(false);
        feedbackTA.setEnabled(false);
        photoUpl.setEnabled(false);

    }

    private void fillFields() {
        messageTA.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Message)).getValue().toString());
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Feedback)).getValue() != null) {
            feedbackTA.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.Feedback)).getValue().toString());
        }
        subjectTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Subject)).getValue().toString());
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Photo)).getValue() != null) {
            photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS
                    + dataTable.getContainerProperty(dataTable.getValue(),
                            myUI.getMessage(SptMessages.Photo)).getValue().toString())));
            photoName = dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.Photo)).getValue().toString();
        } else {
            photoName = null;
        }
    }

    private void clearFields() {
        messageTA.setValue("");
        feedbackTA.setValue("");
        subjectTF.setValue("");
        photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS + "no_image.png")));
        photoName = null;
    }

    private void updateDatacontainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Message)).setValue(messageTA.getValue());
        if (!feedbackTA.getValue().equals("")) {
            dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.Feedback)).setValue(feedbackTA.getValue());
        }
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Subject)).setValue(subjectTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Date)).setValue(
                sysSettings.df.format(new Date()));
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Employee)).setValue(
                myUI.getUser().getFullname());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.School)).setValue(
                myUI.getUser().getSchool_name());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Photo)).setValue(photoName);
    }

    private void addDatacontainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                sysSettings.df.format(new Date()));
        item.getItemProperty(myUI.getMessage(SptMessages.Message)).setValue(
                messageTA.getValue());
        if (!feedbackTA.getValue().equals("")) {
            item.getItemProperty(myUI.getMessage(SptMessages.Feedback)).setValue(
                    feedbackTA.getValue());
        }
        item.getItemProperty(myUI.getMessage(SptMessages.Subject)).setValue(
                subjectTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Employee)).setValue(
                myUI.getUser().getFullname());
        item.getItemProperty(myUI.getMessage(SptMessages.School)).setValue(
                myUI.getUser().getSchool_name());
        item.getItemProperty(myUI.getMessage(SptMessages.Photo)).setValue(photoName);
        item.getItemProperty(sysSettings.id).setValue(id);
        dataTable.setValue(id);
    }

    private Messages getMessage(int i) {
        Messages m = new Messages();
        m.setId(i);
        m.setMessage(messageTA.getValue());
        if (!feedbackTA.getValue().equals("")) {
            m.setFeedback(feedbackTA.getValue());
        }
        m.setSubject(subjectTF.getValue());
        m.setEmployee_id(myUI.getUser().getId());
        m.setPhoto(photoName);
        return m;
    }

    private void execDelete() {

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    sysSettings.id).getValue(), sysSettings.dbMessages);
            if (st != 0) {
                dataTable.getContainerDataSource().removeItem(dataTable.getValue());
                if (dataTable.getContainerDataSource().size() != 0) {
                    dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
                } else {
                    clearFields();
                    dataTable.setValue(null);
                }
            }
            dbDef.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            Notification.show(myUI.getMessage(SptMessages.CanNotDelete),
                    Notification.Type.WARNING_MESSAGE);
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
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

    public class MyReceiver implements Upload.Receiver {

        @Override
        public OutputStream receiveUpload(String filename, String mimetype) {
            mimeType = mimetype;
            FileOutputStream fos; // Output stream to write to
            photoName = new Date().getTime() + ".jpg";
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
        photoUpl.setWidth("50%");

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
                    Notification.show(myUI.getMessage(SptMessages.Jpeg),
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
                try {
                    Thumbnails.of(myFile).size(900, 900).toFile(myFile);
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
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
