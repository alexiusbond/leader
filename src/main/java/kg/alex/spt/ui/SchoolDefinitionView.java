package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.Settings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbDefinition;
import kg.alex.spt.dao.DbSalaryCategories;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.domain.AccCategory;
import kg.alex.spt.domain.School;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.ComboBoxMax;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Iterator;

public class SchoolDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(SchoolDefinitionView.class);
    private MyVaadinUI myUI;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBoxMax statusSelect;
    private Table dataTable;
    private TextField nameKgTF, nameEnTF, codeTF, nameRuTF, directorFullNameTF, addressTF,
            innTF, bankTF, bankAccountTF, phoneTF, cityTF;
    private boolean isNew;

    private String[] NATURAL_COL_ORDER;
    private Upload photoUpl;
    private File myFile;
    private Window statusWindow;
    private Button cancelButton;
    private ProgressBar uploadProgressBar;
    private String photoName, mimeType;
    private MyReceiver receiver;
    private Embedded photoEmb;
    private GridLayout settingsLay;
    private Subject currentUser = SecurityUtils.getSubject();

    public SchoolDefinitionView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Code),
                myUI.getMessage(SptMessages.TitleRu), myUI.getMessage(SptMessages.TitleKg), myUI.getMessage(SptMessages.TitleEn),
                myUI.getMessage(SptMessages.Year), myUI.getMessage(SptMessages.Status),
                myUI.getMessage(SptMessages.DirectorFullName), myUI.getMessage(SptMessages.Bank),
                myUI.getMessage(SptMessages.BankAccount), myUI.getMessage(SptMessages.Address),
                myUI.getMessage(SptMessages.Phone)};
        buildSettingsLayout();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new Table();
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        dataTable.setSelectable(true);
        dataTable.addValueChangeListener(this);
        try {
            DbSchool dbscl = new DbSchool();
            dbscl.connect();
            dataTable.setContainerDataSource(dbscl.execSQL(myUI));
            dbscl.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setColumnCollapsingAllowed(true);
        dataTable.setColumnCollapsed(myUI.getMessage(SptMessages.TitleKg), true);
        dataTable.setColumnCollapsed(myUI.getMessage(SptMessages.TitleEn), true);
        dataTable.setColumnCollapsed(myUI.getMessage(SptMessages.DirectorFullName), true);
        dataTable.setColumnCollapsed(myUI.getMessage(SptMessages.Bank), true);
        dataTable.setColumnCollapsed(myUI.getMessage(SptMessages.BankAccount), true);
        dataTable.setColumnCollapsed(myUI.getMessage(SptMessages.Address), true);
        dataTable.setColumnCollapsed(myUI.getMessage(SptMessages.Phone), true);
        dataTable.setColumnCollapsed(myUI.getMessage(SptMessages.Logo), true);
        dataTable.setVisibleColumns(NATURAL_COL_ORDER);
        if (dataTable.getContainerDataSource().size() != 0) {
            dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
        }
        dataTable.setNullSelectionAllowed(false);
        vl.addComponent(dataTable);

        this.setSplitPosition(40, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(vl);

        prepareNormalMode();
    }

    private void buildSettingsLayout() {

        settingsLay = new GridLayout(2, 8);
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setSizeFull();
        settingsLay.setRowExpandRatio(7, 1);

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
        settingsLay.addComponent(buttonsLay, 0, 0, 1, 0);

        codeTF = new TextField(myUI.getMessage(SptMessages.Code));
        codeTF.setRequired(true);
        codeTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        codeTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        codeTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(codeTF);

        innTF = new TextField(myUI.getMessage(SptMessages.INN));
        innTF.setRequired(false);
        innTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        innTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(innTF);

        nameRuTF = new TextField(myUI.getMessage(SptMessages.TitleRu));
        nameRuTF.setRequired(true);
        nameRuTF.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        nameRuTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameRuTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(nameRuTF);

        nameKgTF = new TextField(myUI.getMessage(SptMessages.TitleKg));
        nameKgTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameKgTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(nameKgTF);

        nameEnTF = new TextField(myUI.getMessage(SptMessages.TitleEn));
        nameEnTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameEnTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(nameEnTF);

        directorFullNameTF = new TextField(myUI.getMessage(SptMessages.DirectorFullName) + " (Ru/Kg)");
        directorFullNameTF.setRequired(false);
        directorFullNameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        directorFullNameTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(directorFullNameTF);

        cityTF = new TextField(myUI.getMessage(SptMessages.City) + " (Ru/Kg)");
        cityTF.setRequired(false);
        cityTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        cityTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(cityTF);

        addressTF = new TextField(myUI.getMessage(SptMessages.Address) + " (Ru/Kg)");
        addressTF.setRequired(false);
        addressTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        addressTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(addressTF);

        phoneTF = new TextField(myUI.getMessage(SptMessages.Phone));
        phoneTF.setRequired(false);
        phoneTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        phoneTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(phoneTF);

        bankTF = new TextField(myUI.getMessage(SptMessages.Bank) + " (Ru/Kg)");
        bankTF.setRequired(false);
        bankTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        bankTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(bankTF);

        bankAccountTF = new TextField(myUI.getMessage(SptMessages.BankAccount));
        bankAccountTF.setRequired(false);
        bankAccountTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        bankAccountTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(bankAccountTF);

        statusSelect = new ComboBoxMax(myUI.getMessage(SptMessages.Status));
        statusSelect.setNullSelectionAllowed(false);
        statusSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        statusSelect.setRequired(true);
        statusSelect.setRequiredError(myUI.getMessage(SptMessages.RequiredField));
        statusSelect.setWidth(Settings.PERCENTS100);
        statusSelect.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        statusSelect.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            statusSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbActivity_status, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        settingsLay.addComponent(statusSelect);

        photoEmb = new Embedded();
        photoEmb.setHeight("100%");
        photoEmb.setWidth("95%");
        photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoEmb.setImmediate(true);
        settingsLay.addComponent(photoEmb);
        settingsLay.setComponentAlignment(photoEmb, Alignment.MIDDLE_CENTER);

        buildUpload();
        settingsLay.addComponent(new Label());
        settingsLay.addComponent(photoUpl);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source.getId() != null && source.getId().equals(Settings.cancel_upload_button)) {
            if (photoUpl != null) {
                photoUpl.interruptUpload();
            }
        } else if (source == modifyBtn && dataTable.getValue() != null) {
            isNew = false;
            fillFields();
            prepareModificationMode();
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
            codeTF.focus();
            statusSelect.setValue(2);
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
                    DbSchool dbScl = new DbSchool();
                    dbScl.connect();
                    if (isNew) {
                        School sch = getSchool(0);
                        int id = dbScl.exec_insert(sch);
                        if (id != 0) {
                            DbSalaryCategories dbsc = new DbSalaryCategories();
                            dbsc.connect();
                            IndexedContainer salCont = dbsc.execSQL(myUI);
                            dbsc.close();
                            DbAccCategory dba = new DbAccCategory();
                            dba.connect();
                            Iterator iter = salCont.getItemIds().iterator();
                            while (iter.hasNext()) {
                                Object next = iter.next();
                                AccCategory ac = new AccCategory();
                                ac.setCode(sch.getCode());
                                ac.setName(salCont.getContainerProperty(next, myUI.getMessage(SptMessages.Title)).getValue() + " - " + sch.getName_ru());
                                ac.setParent_code(salCont.getContainerProperty(next, myUI.getMessage(SptMessages.Code)).getValue().toString());
                                ac.setParent_id((Integer) next);
                                ac.setStatus_id(2);
                                ac.setType_id(2);
                                ac.setSchool_id(id);
                                dba.exec_insert(ac);
                            }
                            dba.close();
                            addDatacontainerItem(id);
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                            Item item = (myUI.getSchoolCont()).addItem(id);
                            item.getItemProperty(myUI.getMessage(SptMessages.Logo)).setValue(sch.getPhoto());
                            item.getItemProperty(myUI.getMessage(SptMessages.Code)).setValue(codeTF.getValue());
                            item.getItemProperty(myUI.getMessage(SptMessages.Title)).setValue(
                                    codeTF.getValue() + " - " + nameRuTF.getValue());
                            item.getItemProperty(Settings.year_id).setValue(
                                    myUI.getUser().getCurrent_year().getId());
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        int status = 0;
                        School school = getSchool((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                Settings.id).getValue());
                        try {
                            status = dbScl.exec_update(school);
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (status != 0) {
                            if (!school.getCode().equals(dataTable.getContainerProperty((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                    Settings.id).getValue(), myUI.getMessage(SptMessages.Code)).getValue())
                                    || !school.getName_kg().equals(dataTable.getContainerProperty((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                    Settings.id).getValue(), myUI.getMessage(SptMessages.TitleRu)).getValue())) {
                                DbSalaryCategories dbsc = new DbSalaryCategories();
                                dbsc.connect();
                                IndexedContainer salCont = dbsc.execSQL(myUI);
                                dbsc.close();
                                DbAccCategory dba = new DbAccCategory();
                                dba.connect();
                                Iterator iter = salCont.getItemIds().iterator();
                                while (iter.hasNext()) {
                                    Object next = iter.next();
                                    int id = dba.exec_id((Integer) next, school.getId());
                                    dba.exec_update_code(id, school.getCode(), salCont.getContainerProperty(next,
                                            myUI.getMessage(SptMessages.Title)).getValue() + " - " + school.getName_ru());
                                    dba.exec_update_all_parent_codes(id, salCont.getContainerProperty(next,
                                            myUI.getMessage(SptMessages.Code)).getValue() + "." + school.getCode(), false);

                                }
                                dba.close();
                            }
                            updateDatacontainer();
                            Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                            myUI.getSchoolCont().getContainerProperty(dataTable.getValue(),
                                    myUI.getMessage(SptMessages.Title)).setValue(codeTF.getValue() + " - " + nameRuTF.getValue());
                        } else {
                            Notification.show(myUI.getMessage(SptMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
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
        nameKgTF.setEnabled(true);
        statusSelect.setEnabled(true);
        codeTF.setEnabled(true);
        nameRuTF.setEnabled(true);
        nameEnTF.setEnabled(true);
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
        if (currentUser.isPermitted(Settings.cnSchoolDefinitionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnSchoolDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnSchoolDefinitionView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameKgTF.setEnabled(false);
        statusSelect.setEnabled(false);
        codeTF.setEnabled(false);
        nameRuTF.setEnabled(false);
        nameEnTF.setEnabled(false);
        directorFullNameTF.setEnabled(false);
        cityTF.setEnabled(false);
        innTF.setEnabled(false);
        addressTF.setEnabled(false);
        bankTF.setEnabled(false);
        bankAccountTF.setEnabled(false);
        phoneTF.setEnabled(false);
        photoUpl.setEnabled(false);

    }

    private void fillFields() {
        codeTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Code)).getValue().toString());
        nameRuTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.TitleRu)).getValue().toString());
        statusSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).getValue());
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.TitleKg)).getValue() != null) {
            nameKgTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.TitleKg)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.TitleEn)).getValue() != null) {
            nameEnTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.TitleEn)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.DirectorFullName)).getValue() != null) {
            directorFullNameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.DirectorFullName)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.City)).getValue() != null) {
            cityTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.City)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Address)).getValue() != null) {
            addressTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.Address)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.INN)).getValue() != null) {
            innTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.INN)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Bank)).getValue() != null) {
            bankTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.Bank)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.BankAccount)).getValue() != null) {
            bankAccountTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.BankAccount)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Phone)).getValue() != null) {
            phoneTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.Phone)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Logo)).getValue() != null) {
            photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS
                    + dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.Logo)).getValue().toString())));
            photoName = dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(SptMessages.Logo)).getValue().toString();
        } else {
            photoName = null;
        }

    }

    private void clearFields() {
        codeTF.setValue("");
        nameKgTF.setValue("");
        nameRuTF.setValue("");
        nameEnTF.setValue("");
        directorFullNameTF.setValue("");
        cityTF.setValue("");
        addressTF.setValue("");
        innTF.setValue("");
        bankTF.setValue("");
        bankAccountTF.setValue("");
        phoneTF.setValue("");
        statusSelect.setValue(null);
        photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoName = null;
    }

    private void updateDatacontainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Code)).setValue(codeTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.TitleKg)).setValue(nameKgTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.TitleRu)).setValue(nameRuTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.TitleEn)).setValue(nameEnTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.DirectorFullName)).setValue(directorFullNameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.City)).setValue(cityTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Address)).setValue(addressTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.INN)).setValue(innTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Address)).setValue(addressTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Bank)).setValue(bankTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.BankAccount)).setValue(bankAccountTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Phone)).setValue(phoneTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Logo)).setValue(photoName);
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).setValue(statusSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(SptMessages.Status)).setValue(statusSelect.
                getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
    }

    private void addDatacontainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(SptMessages.Code)).setValue(codeTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.TitleKg)).setValue(nameKgTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.TitleRu)).setValue(nameRuTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.TitleEn)).setValue(nameEnTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.DirectorFullName)).setValue(directorFullNameTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.City)).setValue(cityTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Address)).setValue(addressTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.INN)).setValue(innTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Bank)).setValue(bankTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.BankAccount)).setValue(bankAccountTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Phone)).setValue(phoneTF.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Logo)).setValue(photoName);
        item.getItemProperty(myUI.getMessage(SptMessages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(SptMessages.Title)).getValue().toString());
        item.getItemProperty(Settings.status_id).setValue(statusSelect.getValue());
        item.getItemProperty(myUI.getMessage(SptMessages.Year)).setValue(
                myUI.getUser().getCurrent_year().getName());
        item.getItemProperty(Settings.year_id).setValue(myUI.getUser().getCurrent_year().getId());
        item.getItemProperty(Settings.id).setValue(id);
        dataTable.setValue(id);
    }

    private School getSchool(int i) {
        School d = new School();
        d.setId(i);
        d.setCode(codeTF.getValue());
        d.setName_kg(nameKgTF.getValue());
        d.setName_ru(nameRuTF.getValue());
        d.setName_en(nameEnTF.getValue());
        d.setDirector_f_name(directorFullNameTF.getValue());
        d.setCity(cityTF.getValue());
        d.setAddress(addressTF.getValue());
        d.setInn(innTF.getValue());
        d.setBank(bankTF.getValue());
        d.setBank_account(bankAccountTF.getValue());
        d.setPhone(phoneTF.getValue());
        d.setStatus_id((Integer) statusSelect.getValue());
        d.setSchool_type_id(1);
        d.setYear_id(myUI.getUser().getCurrent_year().getId());
        d.setPhoto(photoName);
        return d;
    }

    private void execDelete() {
        DbDefinition dbDef = null;
        try {
            dbDef = new DbDefinition();
            dbDef.connect();
            dbDef.getConnection().setAutoCommit(false);
            dbDef.exec_delete(dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue().toString(), Settings.dbAcc_category, Settings.school_id);
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue(), Settings.dbSchool);
            if (st != 0) {
                myUI.getSchoolCont().removeItem(dataTable.getContainerProperty(
                        dataTable.getValue(), Settings.id).getValue());
                dataTable.getContainerDataSource().removeItem(dataTable.getValue());
                if (dataTable.getContainerDataSource().size() != 0) {
                    dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
                } else {
                    clearFields();
                    dataTable.setValue(null);
                }
            }
            dbDef.getConnection().commit();
            dbDef.getConnection().setAutoCommit(true);
            dbDef.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            Notification.show(myUI.getMessage(SptMessages.CanNotDelete),
                    Notification.Type.WARNING_MESSAGE);
            if (dbDef != null) {
                try {
                    dbDef.getConnection().rollback();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            }
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            if (dbDef != null) {
                try {
                    dbDef.getConnection().rollback();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            }
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
            photoName = codeTF.getValue() + ".jpg";
            try {
                myFile = new File(Settings.PATH_TO_UPLOADS + photoName);

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
        uploadProgressBar = new ProgressBar();
        uploadProgressBar.setWidth("90%");

        statusWindow = new Window(myUI.getMessage(SptMessages.UploadStatus));
        statusWindow.setResizable(false);
        statusWindow.setDraggable(false);
        statusWindow.setModal(true);
        statusWindow.setWidth("20%");
        statusWindow.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);

        final HorizontalLayout l = new HorizontalLayout();
        l.setSpacing(true);
        l.setWidth(Settings.PERCENTS100);
        l.setMargin(true);
        statusWindow.setContent(l);

        cancelButton = new Button();
        cancelButton.addClickListener(this);
        cancelButton.setId(Settings.cancel_upload_button);
        cancelButton.setVisible(false);
        cancelButton.setIcon(FontAwesome.CLOSE);
        cancelButton.setStyleName(ValoTheme.BUTTON_TINY);
        cancelButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        l.addComponent(cancelButton);
        l.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);

        uploadProgressBar.setCaption(myUI.getMessage(SptMessages.Progress));
        uploadProgressBar.setVisible(false);
        l.addComponent(uploadProgressBar);
        l.setExpandRatio(uploadProgressBar, 1);
    }

    private void buildUpload() {
        receiver = new MyReceiver();
        photoUpl = new Upload(null, receiver);
        photoUpl.setImmediate(true);
        photoUpl.setStyleName(ValoTheme.BUTTON_SMALL);
        photoUpl.setButtonCaption(myUI.getMessage(SptMessages.Upload));
        photoUpl.setWidth(Settings.PERCENTS100);

        photoUpl.addStartedListener(new Upload.StartedListener() {
            @Override
            public void uploadStarted(Upload.StartedEvent event) {
                // This method gets called immediatedly after upload is started

                buildUploadWindow();
                myUI.addWindow(statusWindow);
                statusWindow.setClosable(false);

                uploadProgressBar.setValue(0f);
                uploadProgressBar.setVisible(true);
                UI.getCurrent().setPollInterval(500); // hit server frequantly to get

                cancelButton.setVisible(true);
            }
        });

        photoUpl.addProgressListener(new Upload.ProgressListener() {
            @Override
            public void updateProgress(long readBytes, long contentLength) {
                // This method gets called several times during the update
                if (!mimeType.equals("image/jpeg")) {
                    photoUpl.interruptUpload();
                    photoName = null;
                    Notification.show(myUI.getMessage(SptMessages.OnlyJpg),
                            Notification.Type.WARNING_MESSAGE);
                } else if (contentLength >= 5000000) {
                    photoUpl.interruptUpload();
                    photoName = null;
                    Notification.show(myUI.getMessage(SptMessages.Maxsize),
                            Notification.Type.WARNING_MESSAGE);
                } else {

                    uploadProgressBar.setValue(new Float(readBytes / (float) contentLength));
                }
            }
        });

        photoUpl.addSucceededListener(new Upload.SucceededListener() {
            @Override
            public void uploadSucceeded(Upload.SucceededEvent event) {
                // This method gets called when the upload finished successfully
                try {
                    Thumbnails.of(myFile).size(300, 300).toFile(myFile);
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
                if (statusWindow != null) {
                    statusWindow.close();
                }
                try {
                    myFile.delete();
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }
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
