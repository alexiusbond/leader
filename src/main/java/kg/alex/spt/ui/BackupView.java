package kg.alex.spt.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.SystemSettings;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BackupView extends HorizontalSplitPanel implements Button.ClickListener {

    static final Logger logger = LogManager.getLogger(BackupView.class);
    private MyVaadinUI myUI;
    private Table dataTable;
    private Button takeBakup;
    private SystemSettings sysSettings = new SystemSettings();
    private IndexedContainer container;

    public BackupView(MyVaadinUI myUI) {
        this.myUI = myUI;

        takeBakup = new Button(myUI.getMessage(SptMessages.TakeBackupButton));
        takeBakup.setWidth("100%");
        takeBakup.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        takeBakup.setIcon(FontAwesome.DATABASE);
        takeBakup.addClickListener(this);

        dataTable = new Table();
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        dataTable.setSelectable(false);
        setDataTable();

        this.setSplitPosition(24, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(takeBakup);
        this.setSecondComponent(dataTable);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == takeBakup) {
            try {
                ProcessBuilder pb = new ProcessBuilder("/home/logo/backup.sh", "myArg1", "myArg2");
                pb.start();
            } catch (IOException e) {
                logger.error(e);
                logger.catching(e);
            }
            setDataTable();
        } else {
            try {
                myUI.getPage().open(new FileResource(
                        new File("/home/logo/backups/" + source.getData())),
                        "_blank", false);
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    private Button createButton(String itemId) {
        Button btn = new Button(myUI.getMessage(SptMessages.DownloadBackupButton));
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.setIcon(FontAwesome.DOWNLOAD);
        btn.setData(itemId);
        btn.addClickListener(this);
        return btn;
    }

    private void setDataTable() {
        container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(SptMessages.Name), String.class, null);
        container.addContainerProperty(myUI.getMessage(SptMessages.Date), Date.class, null);
        container.addContainerProperty(sysSettings.button, Button.class, null);
        try {
            File folder = new File("/home/logo/backups");
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String id = listOfFiles[i].getName();
                    Item item = container.addItem(id);
                    item.getItemProperty(myUI.getMessage(SptMessages.Name)).setValue(
                            listOfFiles[i].getName());
                    item.getItemProperty(myUI.getMessage(SptMessages.Date)).setValue(
                            new Date(listOfFiles[i].lastModified()));
                    item.getItemProperty(sysSettings.button).setValue(createButton(id));
                }
            }
            dataTable.clear();
            container.sort(new String[]{myUI.getMessage(SptMessages.Date)},
                    new boolean[]{false});
            dataTable.setContainerDataSource(container);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

}
