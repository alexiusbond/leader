package kg.alex.spt.ui;

import kg.alex.spt.utils.ComboBoxMax;
import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbEmployee;
import kg.alex.spt.dao.DbEmployeeOrder;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.domain.AccCategory;
import kg.alex.spt.domain.EmployeeOrder;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.FormattedTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmployeeTransferView extends VerticalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener, DropHandler {

    static final Logger logger = LogManager.getLogger(EmployeeTransferView.class);
    private MyVaadinUI myUI;
    private Button saveBtn, cancelBtn;
    private ComboBoxMax school1Select, school2Select;
    private Table data1Table, data2Table;
    private TextField search1TF, search2TF;
    private GridLayout settingsLay;
    private String[] NATURAL_COL_ORDER;

    public EmployeeTransferView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Id),
            myUI.getMessage(SptMessages.Firstname),
            myUI.getMessage(SptMessages.Surname),
            myUI.getMessage(SptMessages.MainPosition),
            myUI.getMessage(SptMessages.FromDate),
            myUI.getMessage(SptMessages.Note)};

        GridLayout gl = new GridLayout(2, 3);
        gl.setMargin(true);
        gl.setSpacing(true);
        gl.setSizeFull();
        gl.setRowExpandRatio(2, 1);

        Label caption = new Label();
        caption.setWidth("100%");
        caption.setContentMode(ContentMode.HTML);
        caption.setValue(myUI.getMessage(SptMessages.TransferInstruction));
        caption.setStyleName("tableCpt");
        gl.addComponent(caption, 0, 0, 1, 0);

        search1TF = new TextField();
        search1TF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        search1TF.setWidth("100%");
        search1TF.setInputPrompt(myUI.getMessage(SptMessages.Search) + " ("
                + myUI.getMessage(SptMessages.Id) + " / " + myUI.getMessage(SptMessages.Firstname) + " / "
                + myUI.getMessage(SptMessages.Surname) + " / " + myUI.getMessage(SptMessages.Position) + ")");
        search1TF.addValueChangeListener(this);
        gl.addComponent(search1TF);

        search2TF = new TextField();
        search2TF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        search2TF.setWidth("100%");
        search1TF.setInputPrompt(myUI.getMessage(SptMessages.Search) + " ("
                + myUI.getMessage(SptMessages.Id) + " / " + myUI.getMessage(SptMessages.Firstname) + " / "
                + myUI.getMessage(SptMessages.Surname) + " / " + myUI.getMessage(SptMessages.Position) + ")");
        search2TF.addValueChangeListener(this);
        gl.addComponent(search2TF);

        data1Table = new Table();
        data1Table.setId("1");
        data1Table.setStyleName(ValoTheme.TABLE_COMPACT);
        data1Table.setSizeFull();
        data1Table.setSelectable(true);
        data1Table.setDragMode(Table.TableDragMode.ROW);
        data1Table.setDropHandler(this);
        data1Table.addValueChangeListener(this);
        gl.addComponent(data1Table);

        data2Table = new Table();
        data2Table.setId("2");
        data2Table.setStyleName(ValoTheme.TABLE_COMPACT);
        data2Table.setSizeFull();
        data2Table.setSelectable(true);
        data2Table.setDragMode(Table.TableDragMode.ROW);
        data2Table.setDropHandler(this);
        data2Table.addValueChangeListener(this);
        gl.addComponent(data2Table);

        buildSettingsLayout();
        this.setSplitPosition(55, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(false);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(gl);

    }

    private void buildSettingsLayout() {

        settingsLay = new GridLayout(4, 2);
        settingsLay.setRowExpandRatio(1, 1);
        settingsLay.setMargin(true);
        settingsLay.setSpacing(true);
        settingsLay.setSizeFull();

        saveBtn = new Button(myUI.getMessage(SptMessages.SaveButton));
        saveBtn.setWidth("100%");
        saveBtn.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        saveBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);
        settingsLay.addComponent(saveBtn, 1, 0);
        settingsLay.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);

        cancelBtn = new Button(myUI.getMessage(SptMessages.CancelButton));
        cancelBtn.setWidth("100%");
        cancelBtn.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        cancelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        cancelBtn.setIcon(FontAwesome.FLOPPY_O);
        cancelBtn.addClickListener(this);
        settingsLay.addComponent(cancelBtn, 2, 0);
        settingsLay.setComponentAlignment(cancelBtn, Alignment.BOTTOM_CENTER);

        Label l1 = new Label(myUI.getMessage(SptMessages.School) + " 1:");
        l1.setSizeUndefined();

        school1Select = new ComboBoxMax();
        school1Select.setNullSelectionAllowed(false);
        school1Select.setStyleName(ValoTheme.COMBOBOX_SMALL);
        school1Select.setWidth("100%");
        school1Select.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        school1Select.setFilteringMode(FilteringMode.CONTAINS);
        school1Select.addValueChangeListener(this);

        HorizontalLayout hl1 = new HorizontalLayout();
        hl1.setSizeFull();
        hl1.setSpacing(true);
        hl1.addComponent(l1);
        hl1.addComponent(school1Select);
        hl1.setExpandRatio(school1Select, 1);
        settingsLay.addComponent(hl1, 0, 0);

        Label l2 = new Label(myUI.getMessage(SptMessages.School) + " 2:");
        l2.setSizeUndefined();

        school2Select = new ComboBoxMax();
        school2Select.setNullSelectionAllowed(false);
        school2Select.setStyleName(ValoTheme.COMBOBOX_SMALL);
        school2Select.setWidth("100%");
        school2Select.setItemCaptionPropertyId(myUI.getMessage(SptMessages.Title));
        school2Select.setFilteringMode(FilteringMode.CONTAINS);
        school2Select.addValueChangeListener(this);

        HorizontalLayout hl2 = new HorizontalLayout();
        hl2.setSizeFull();
        hl2.setSpacing(true);
        hl2.addComponent(l2);
        hl2.addComponent(school2Select);
        hl2.setExpandRatio(school2Select, 1);
        settingsLay.addComponent(hl2, 3, 0);

        settingsLay.setColumnExpandRatio(0, 3);
        settingsLay.setColumnExpandRatio(3, 3);
        settingsLay.setColumnExpandRatio(1, 1);
        settingsLay.setColumnExpandRatio(2, 1);

        try {
            DbSchool dbCon = new DbSchool();
            dbCon.connect();
            school1Select.setContainerDataSource(dbCon.execSchoolSel(myUI, 0));
            school2Select.setContainerDataSource(dbCon.execSchoolSel(myUI, 0));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        school1Select.setValue(myUI.getUser().getSchool_id());
        school2Select.setValue(((IndexedContainer) school2Select.getContainerDataSource()).nextItemId(school1Select.getValue()));
    }

    private GridLayout buildInfoLayout(Item item, int employee_id) {
        GridLayout gl = new GridLayout(3, 9);
        gl.setSizeFull();

        String str = "";

        Embedded photoEmb = new Embedded();
        if (item.getItemProperty(myUI.getMessage(SptMessages.Photo)).getValue() == null) {
            photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS_HR + "no_photo.jpg")));
        } else {
            photoEmb.setSource(new FileResource(new File(SystemSettings.PATH_TO_UPLOADS_HR + item.getItemProperty(myUI.getMessage(SptMessages.Photo)).getValue())));
        }
        photoEmb.setImmediate(true);
        photoEmb.setWidth("120px");
        gl.addComponent(photoEmb, 0, 0, 0, 5);

        Label idLb = new Label();
        idLb.setWidth("100%");
        idLb.setContentMode(ContentMode.HTML);
        idLb.setStyleName(ValoTheme.LABEL_SUCCESS);
        idLb.setValue("<b>" + myUI.getMessage(SptMessages.Id) + ":</b> " + item.getItemProperty(myUI.getMessage(SptMessages.Id)).getValue().toString());
        gl.addComponent(idLb, 1, 0);

        Label fullnameLb = new Label();
        fullnameLb.setWidth("100%");
        fullnameLb.setContentMode(ContentMode.HTML);
        fullnameLb.setStyleName(ValoTheme.LABEL_SUCCESS);
        fullnameLb.setValue("<b>" + myUI.getMessage(SptMessages.FullName) + ":</b> " + item.getItemProperty(myUI.getMessage(SptMessages.Firstname)).getValue()
                + " " + item.getItemProperty(myUI.getMessage(SptMessages.Surname)).getValue());
        gl.addComponent(fullnameLb, 2, 0);

        Label mainPositionLb = new Label();
        mainPositionLb.setWidth("100%");
        mainPositionLb.setContentMode(ContentMode.HTML);
        mainPositionLb.setStyleName(ValoTheme.LABEL_SUCCESS);
        mainPositionLb.setValue("<b>" + myUI.getMessage(SptMessages.MainPositionShort) + ":</b> " + item.getItemProperty(myUI.getMessage(SptMessages.Position)).getValue());
        gl.addComponent(mainPositionLb, 1, 1);

        Label mainBranchLb = new Label();
        mainBranchLb.setWidth("100%");
        mainBranchLb.setContentMode(ContentMode.HTML);
        mainBranchLb.setStyleName(ValoTheme.LABEL_SUCCESS);
        str = "";
        if (item != null && item.getItemProperty(myUI.getMessage(SptMessages.MainBranch)).getValue() != null) {
            str = item.getItemProperty(myUI.getMessage(SptMessages.MainBranch)).getValue().toString();
        }
        mainBranchLb.setValue("<b>" + myUI.getMessage(SptMessages.MainBranchShort) + ":</b> " + str);
        gl.addComponent(mainBranchLb, 2, 1);

        Label extraPositionsLb = new Label();
        extraPositionsLb.setWidth("100%");
        extraPositionsLb.setContentMode(ContentMode.HTML);
        extraPositionsLb.setStyleName(ValoTheme.LABEL_SUCCESS);
        str = "";
        if (item != null && item.getItemProperty(myUI.getMessage(SptMessages.ExtraPosition)).getValue() != null) {
            str = item.getItemProperty(myUI.getMessage(SptMessages.ExtraPosition)).getValue().toString();
        }
        extraPositionsLb.setValue("<b>" + myUI.getMessage(SptMessages.ExtraPosition) + ":</b> " + str);
        gl.addComponent(extraPositionsLb, 1, 2, 2, 2);

        Label extraBranchesLb = new Label();
        extraBranchesLb.setWidth("100%");
        extraBranchesLb.setContentMode(ContentMode.HTML);
        extraBranchesLb.setStyleName(ValoTheme.LABEL_SUCCESS);
        str = "";
        if (item != null && item.getItemProperty(myUI.getMessage(SptMessages.ExtraBranches)).getValue() != null) {
            str = item.getItemProperty(myUI.getMessage(SptMessages.ExtraBranches)).getValue().toString();
        }
        extraBranchesLb.setValue("<b>" + myUI.getMessage(SptMessages.ExtraBranches) + ":</b> " + str);
        gl.addComponent(extraBranchesLb, 1, 3, 2, 3);

        Label lessonsLb = new Label();
        lessonsLb.setWidth("100%");
        lessonsLb.setContentMode(ContentMode.HTML);
        lessonsLb.setStyleName(ValoTheme.LABEL_SUCCESS);
        str = "";
        if (item != null && item.getItemProperty(myUI.getMessage(SptMessages.Lessons)).getValue() != null) {
            str = item.getItemProperty(myUI.getMessage(SptMessages.Lessons)).getValue().toString();
        }
        lessonsLb.setValue("<b>" + myUI.getMessage(SptMessages.Lessons) + " (" + myUI.getUser().getCurrent_year().getName() + "):</b> " + str);
        gl.addComponent(lessonsLb, 1, 4, 2, 4);

        Label spouseLb = new Label();
        spouseLb.setWidth("100%");
        spouseLb.setContentMode(ContentMode.HTML);
        spouseLb.setStyleName(ValoTheme.LABEL_SUCCESS);
        str = "";
        if (item != null && item.getItemProperty(myUI.getMessage(SptMessages.SpouseInfo)).getValue() != null) {
            str = item.getItemProperty(myUI.getMessage(SptMessages.SpouseInfo)).getValue().toString();
        }
        spouseLb.setValue("<b>" + myUI.getMessage(SptMessages.SpouseInfo) + ":</b> " + str);
        gl.addComponent(spouseLb, 1, 5, 2, 5);

        Label childrenLb = new Label();
        childrenLb.setWidth("100%");
        childrenLb.setContentMode(ContentMode.HTML);
        childrenLb.setStyleName(ValoTheme.LABEL_SUCCESS);
        str = "";
        if (item != null && item.getItemProperty(myUI.getMessage(SptMessages.Children)).getValue() != null) {
            str = item.getItemProperty(myUI.getMessage(SptMessages.Children)).getValue().toString();
        }
        childrenLb.setValue("<b>" + myUI.getMessage(SptMessages.Children) + ":</b> " + str);
        gl.addComponent(childrenLb, 1, 6, 2, 6);

        Label captionOrders = new Label();
        captionOrders.setWidth("100%");
        captionOrders.setContentMode(ContentMode.HTML);
        captionOrders.setValue(myUI.getMessage(SptMessages.OrdersHistory));
        captionOrders.setStyleName("tableCpt");
        gl.addComponent(captionOrders, 0, 7, 2, 7);

        FormattedTable ordersTable = new FormattedTable();
        ordersTable.setImmediate(true);
        ordersTable.setPageLength(4);
        ordersTable.setSizeFull();
        ordersTable.setStyleName(ValoTheme.TABLE_SMALL);
        try {
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            ordersTable.setContainerDataSource(dbeo.execSQL(myUI, employee_id));
            dbeo.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        gl.addComponent(ordersTable, 0, 8, 2, 8);

        gl.setColumnExpandRatio(1, 1);
        gl.setColumnExpandRatio(2, 1);
        gl.setRowExpandRatio(8, 1);
        return gl;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == saveBtn) {
            if (!validateTable(data1Table, true) || !validateTable(data1Table, true)) {
                Notification.show(myUI.getMessage(SptMessages.NotifWrongValue),
                        Notification.Type.WARNING_MESSAGE);
            } else {
                try {
                    int st = insertTableValuesToDb(data1Table);
                    int st2 = insertTableValuesToDb(data2Table);
                    if (st != 0 || st2 != 0) {
                        Notification.show(myUI.getMessage(SptMessages.ValueSaved),
                                Notification.Type.HUMANIZED_MESSAGE);
                        setTableOptions(data1Table, (Integer) school1Select.getValue());
                        setTableOptions(data2Table, (Integer) school2Select.getValue());
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == cancelBtn) {
            setTableOptions(data1Table, (Integer) school1Select.getValue());
            setTableOptions(data2Table, (Integer) school2Select.getValue());
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == school1Select) {
            setTableOptions(data1Table, (Integer) school1Select.getValue());
        } else if (property == school2Select) {
            setTableOptions(data2Table, (Integer) school2Select.getValue());
        } else if (property == data1Table) {
            settingsLay.removeComponent(0, 1);
            if (data1Table.getValue() != null) {
                settingsLay.addComponent(buildInfoLayout(data1Table.getItem(data1Table.getValue()), (Integer) data1Table.getValue()), 0, 1, 1, 1);
            }
        } else if (property == data2Table) {
            settingsLay.removeComponent(2, 1);
            if (data2Table.getValue() != null) {
                settingsLay.addComponent(buildInfoLayout(data2Table.getItem(data2Table.getValue()), (Integer) data2Table.getValue()), 2, 1, 3, 1);
            }
        } else if (data1Table != null && data1Table.size() > 0 && property == search1TF) {
            if (property.getValue() != null && property.getValue().toString().length() > 1) {
                Container.Filter filter = new Or(
                        new SimpleStringFilter(myUI.getMessage(SptMessages.Firstname), property.getValue().toString(), true, false),
                        new SimpleStringFilter(myUI.getMessage(SptMessages.Surname), property.getValue().toString(), true, false),
                        new SimpleStringFilter(myUI.getMessage(SptMessages.Id), property.getValue().toString(), true, false),
                        new SimpleStringFilter(myUI.getMessage(SptMessages.Position), property.getValue().toString(), true, false));

                ((IndexedContainer) data1Table.getContainerDataSource()).removeAllContainerFilters();
                ((IndexedContainer) data1Table.getContainerDataSource()).addContainerFilter(filter);
            } else {
                ((IndexedContainer) data1Table.getContainerDataSource()).removeAllContainerFilters();
            }
        } else if (data2Table != null && data2Table.size() > 0 && property == search2TF) {
            if (property.getValue() != null && property.getValue().toString().length() > 1) {
                Container.Filter filter = new Or(
                        new SimpleStringFilter(myUI.getMessage(SptMessages.Firstname), property.getValue().toString(), true, false),
                        new SimpleStringFilter(myUI.getMessage(SptMessages.Surname), property.getValue().toString(), true, false),
                        new SimpleStringFilter(myUI.getMessage(SptMessages.Id), property.getValue().toString(), true, false),
                        new SimpleStringFilter(myUI.getMessage(SptMessages.Position), property.getValue().toString(), true, false));

                ((IndexedContainer) data2Table.getContainerDataSource()).removeAllContainerFilters();
                ((IndexedContainer) data2Table.getContainerDataSource()).addContainerFilter(filter);
            } else {
                ((IndexedContainer) data2Table.getContainerDataSource()).removeAllContainerFilters();
            }
        }
    }

    private void setTableOptions(Table t, int school_id) {
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            t.setContainerDataSource(dbe.execSQL(myUI, school_id));
            t.setId(school_id + "");
            dbe.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        t.setVisibleColumns(NATURAL_COL_ORDER);
        t.setColumnExpandRatio(myUI.getMessage(SptMessages.Note), 1);
    }

    private boolean validateTable(Table t, boolean isEmptyAllowed) {
        if (t.size() == 0 && !isEmptyAllowed) {
            return false;
        } else {
            Iterator iter = ((IndexedContainer) t
                    .getContainerDataSource()).getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                Iterator iterProp = ((IndexedContainer) t
                        .getContainerDataSource()).getContainerPropertyIds().iterator();
                while (iterProp.hasNext()) {
                    Object next1 = iterProp.next();
                    Object c = t.getItem(next).getItemProperty(
                            next1).getValue();
                    if (c instanceof AbstractField) {
                        try {
                            ((AbstractField) c).validate();
                        } catch (Exception e) {
                            //((AbstractComponent) c).setComponentError(new UserError(e.getMessage()));
                            return false;
                        }
                    } else if (c instanceof AbstractComponentContainer) {
                        if (!validate((AbstractComponentContainer) c)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private int insertTableValuesToDb(Table t) {
        int st = 0;
        try {
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            Iterator iter = ((IndexedContainer) t
                    .getContainerDataSource()).getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (t.getContainerProperty(next, SystemSettings.crud_status).getValue() != null) {
                    EmployeeOrder eo = new EmployeeOrder();
                    eo.setEmployee_id((Integer) next);
                    eo.setOrder_id(5);
                    eo.setFrom_to_school_id(Integer.parseInt(t.getId()));
                    eo.setSchool_id((Integer) t.getContainerProperty(next, SystemSettings.school_id).getValue());
                    eo.setPosition_id((Integer) t.getContainerProperty(next, SystemSettings.position_id).getValue());
                    eo.setFrom_date(((DateField) t.getContainerProperty(
                            next, myUI.getMessage(SptMessages.FromDate)).getValue()).getValue());
                    if (((TextField) t.getContainerProperty(next,
                            myUI.getMessage(SptMessages.Note)).getValue()).getValue() != null
                            && !((TextField) t.getContainerProperty(next,
                                    myUI.getMessage(SptMessages.Note)).getValue()).getValue().equals("")) {
                        eo.setNote(((TextField) t.getContainerProperty(next,
                                myUI.getMessage(SptMessages.Note)).getValue()).getValue());
                    }
                    eo.setM_employee_id(myUI.getUser().getId());
                    st = dbeo.exec_insert(eo);
                    eo.setFrom_to_school_id(eo.getSchool_id());
                    eo.setSchool_id(Integer.parseInt(t.getId()));
                    eo.setOrder_id(8);
                    st = dbeo.exec_insert(eo);
                    eo.setPosition_id((Integer) ((ComboBoxMax) t.getContainerProperty(
                            next, myUI.getMessage(SptMessages.MainPosition)).getValue()).getValue());
                    eo.setOrder_id(1);
                    st = dbeo.exec_insert(eo);
                    DbAccCategory dbAc = new DbAccCategory();
                    dbAc.connect();
                    AccCategory ac = dbAc.exec_sql(eo.getEmployee_id(), eo.getFrom_to_school_id(), eo.getSchool_id());
                    dbAc.exec_insert(ac);
                    dbAc.exec_update_activity_status((Integer) t.getContainerProperty(eo.getEmployee_id(),
                            SystemSettings.acc_category_id).getValue(), 1, SystemSettings.transfered);
                    dbAc.close();
                }
            }
            dbeo.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        return st;
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

    @Override
    public void drop(DragAndDropEvent event) {
        Transferable t = event.getTransferable();
        Object sourceItemId = t.getData("itemId");
        Table targetTable = (Table) ((AbstractSelect.AbstractSelectTargetDetails) event.getTargetDetails()).getTarget();
        Table sourceTable = (Table) t.getSourceComponent();
        if (targetTable != sourceTable && sourceTable.getContainerProperty(sourceItemId, SystemSettings.crud_status).getValue() == null) {
            Object[] propertyIds = sourceTable.getContainerPropertyIds().toArray();
            int size = propertyIds.length;
            Object[][] properties = new Object[size][2];

            // backup source item properties and values
            for (int i = 0; i < size; i++) {
                Object propertyId = propertyIds[i];
                Object value = sourceTable.getItem(sourceItemId).getItemProperty(propertyId).getValue();
                properties[i][0] = propertyId;
                properties[i][1] = value;
            }
            sourceTable.removeItem(sourceItemId);
            Item item = targetTable.addItem(sourceItemId);

            // restore source item properties and values
            for (int i = 0; i < size; i++) {
                Object propertyId = properties[i][0];
                Object value = properties[i][1];
                if (propertyId.equals(SystemSettings.crud_status)) {
                    item.getItemProperty(propertyId).setValue(SystemSettings.FreshItem);
                } else {
                    item.getItemProperty(propertyId).setValue(value);
                }
                if (value instanceof DateField) {
                    ((DateField) value).setEnabled(true);
                    ((DateField) value).setValue(new Date());
                } else if (value instanceof ComboBoxMax) {
                    ((AbstractField) value).setEnabled(true);
                    ((AbstractField) value).setValue(null);
                } else if (value instanceof TextField) {
                    ((AbstractField) value).setEnabled(true);
                    ((AbstractField) value).setValue("");
                }
            }
        }
    }

    @Override
    public AcceptCriterion getAcceptCriterion() {
        return AcceptAll.get();
    }
}
