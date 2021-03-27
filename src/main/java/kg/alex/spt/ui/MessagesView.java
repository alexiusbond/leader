package kg.alex.spt.ui;

import com.vaadin.ui.CustomTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.dao.DbEmployeeMessage;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;

public class MessagesView extends VerticalLayout {

    static final Logger logger = LogManager.getLogger(MessagesView.class);
    private MyVaadinUI myUI;
    private FilterTable dataTable;
    private String[] NATURAL_COL_ORDER;

    public MessagesView(MyVaadinUI myUI) {
        this.myUI = myUI;
        setSpacing(true);
        NATURAL_COL_ORDER = new String[]{myUI.getMessage(SptMessages.Date),
                myUI.getMessage(SptMessages.OrderNumber), myUI.getMessage(SptMessages.Student),
                myUI.getMessage(SptMessages.Message), myUI.getMessage(SptMessages.Status),
                SystemSettings.button};
        buildBody();
    }

    public void buildBody() {

        dataTable = new FilterTable();
        dataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        dataTable.setStyleName(ValoTheme.TABLE_SMALL);
        dataTable.addStyleName("noWrap");
        dataTable.setSizeFull();
        dataTable.setFilterBarVisible(true);
        dataTable.setFooterVisible(true);
        try {
            DbEmployeeMessage dbCon = new DbEmployeeMessage();
            dbCon.connect();
            dbCon.execSQL(myUI, myUI.getUser().getId(), dataTable);
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns(NATURAL_COL_ORDER);
        dataTable.setColumnExpandRatio(myUI.getMessage(SptMessages.Message), 1);
        dataTable.setColumnWidth(myUI.getMessage(SptMessages.Date), 80);
        dataTable.setColumnWidth(SystemSettings.button, 60);
        dataTable.setCellStyleGenerator(new CustomTable.CellStyleGenerator() {
            @Override
            public String getStyle(CustomTable source, Object itemId, Object propertyId) {

                if (propertyId == null) {
                    // Styling for row
                    if ((Integer) source.getContainerProperty(itemId,
                            SystemSettings.status_id).getValue() == 2) {
                        return "highlight-red";
                    } else {
                        return null;
                    }
                } else {
                    // styling for column propertyId
                    return null;
                }
            }
        });
        addComponent(dataTable);
    }
}
