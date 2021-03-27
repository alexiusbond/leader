package kg.alex.spt;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UI;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.annotation.WebServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.vaadin.ui.themes.ValoTheme;
import kg.alex.spt.dao.DbCurrencyRate;
import kg.alex.spt.dao.DbEmployeeMessage;
import kg.alex.spt.dao.DbSchool;
import kg.alex.spt.dao.DbUserDetails;
import kg.alex.spt.domain.UserDetails;
import kg.alex.spt.i18n.SptMessages;
import kg.alex.spt.ui.ViewManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Theme("mytheme")
@SuppressWarnings("serial")
@PreserveOnRefresh
public class MyVaadinUI extends UI {

    static final Logger logger = LogManager.getLogger(MyVaadinUI.class);
    private ViewManager viewManager;
    private ResourceBundle i18nBundle;
    private UserDetails user;
    public VaadinRequest r;
    private IndexedContainer schoolCont;
    private double nbkr_currency_rate;
    private boolean isMannualRate;
    private Button messagesBtn;

    @WebServlet(value = {"/*", "/VAADIN/*"}, asyncSupported = true)
    @VaadinServletConfiguration(productionMode = true,
            ui = MyVaadinUI.class,
            widgetset = "kg.alex.spt.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {
        r = request;
//        i18nBundle = ResourceBundle.getBundle(SptMessages.class.getName(), this.getLocale());
        i18nBundle = ResourceBundle.getBundle(SptMessages.class.getName(), new Locale("ru"));

        getPage().setTitle(i18nBundle.getString(SptMessages.AppTitle));

        viewManager = new ViewManager(this);
        viewManager.switchScreen(LoginScreen.class.getName(), new LoginScreen(this));

    }

    public void logout() {

        Subject currentUser = SecurityUtils.getSubject();

        if (currentUser.isAuthenticated()) {
            currentUser.logout();
        }
        getUI().getSession().close();

        // Invalidate underlying session instead if login info is stored there
        // VaadinService.getCurrentRequest().getWrappedSession().invalidate();
        // Redirect to avoid keeping the removed UI open in the browser
        getUI().getPage().setLocation("");

    }

    public void login(String username, String password) {
        UsernamePasswordToken token;

        token = new UsernamePasswordToken(username, password);
        // ”Remember Me” built-in, just do this:
        token.setRememberMe(true);

        // With most of Shiro, you'll always want to make sure you're working
        // with the currently executing user,
        // referred to as the subject
        Subject currentUser = SecurityUtils.getSubject();

        // Authenticate
        currentUser.login(token);

    }

    public static MyVaadinUI getInstance() {
        return (MyVaadinUI) MyVaadinUI.getCurrent();
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        i18nBundle = ResourceBundle.getBundle(SptMessages.class.getName(),
                getLocale());
    }

    public ResourceBundle getBundle() {
        return i18nBundle;
    }

    public String getMessage(String key) {
        return i18nBundle.getString(key);
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public void workingDetails(Subject currentUser) throws Exception {
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            setSchoolCont(dbs.execSchoolSel(this, 0));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        try {
            DbUserDetails dbu = new DbUserDetails();
            dbu.connect();
            setUser(dbu.execSQLUserInfo(currentUser.getPrincipal().toString()));
            dbu.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        try {
            DbEmployeeMessage dbu = new DbEmployeeMessage();
            dbu.connect();
            getUser().setUnreadMessages(dbu.isUnread(getUser().getId()));
            dbu.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    /**
     * @return the user
     */
    public UserDetails getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(UserDetails user) {
        this.user = user;
    }

    public static class LogoutListener implements Button.ClickListener {

        private static final long serialVersionUID = 1L;
        private MyVaadinUI myUI;

        public LogoutListener(MyVaadinUI myUi) {
            this.myUI = myUi;
        }

        @Override
        public void buttonClick(ClickEvent event) {
            this.myUI.logout();
        }
    }

    public IndexedContainer getSchoolCont() {
        return schoolCont;
    }

    public void setSchoolCont(IndexedContainer schoolCont) {
        this.schoolCont = schoolCont;
    }

    public double getCurrencyRateFromBank() {
        if (nbkr_currency_rate == 0.00) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            DecimalFormat format = new DecimalFormat("##.##");
            format.setDecimalFormatSymbols(symbols);
            try {
                URL url = new URL("https://www.nbkr.kg/XML/daily.xml");
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(url.openStream());
                NodeList nl = doc.getElementsByTagName("Currency");
                // looping through all item nodes <artist>
                for (int temp = 0; temp < nl.getLength(); temp++) {
                    Node nNode = nl.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        if (eElement.getAttribute("ISOCode").equals("USD")) {
                            nbkr_currency_rate = (Double) format.parse(eElement.getElementsByTagName("Value")
                                    .item(0).getTextContent()).doubleValue();
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        return nbkr_currency_rate;
    }

    public double getDb_currency_rate() {
        double db_currency_rate = 0.0;
        try {
            DbCurrencyRate dbCon = new DbCurrencyRate();
            dbCon.connect();
            db_currency_rate = dbCon.execSQL_last_rate(getUser().getSchool_id());
            if (db_currency_rate == 0.0) {
                isMannualRate = false;
                db_currency_rate = this.getCurrencyRateFromBank();
            } else {
                isMannualRate = true;
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        return db_currency_rate;
    }

    /**
     * @return the isMannualRate
     */
    public boolean isMannualRate() {
        return isMannualRate;
    }

    public void repaintMessagesButton() {
        try {
            DbEmployeeMessage dbCon = new DbEmployeeMessage();
            dbCon.connect();
            getUser().setUnreadMessages(dbCon.isUnread(getUser().getId()));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (getUser().isUnreadMessages()) {
            getMessagesBtn().setStyleName("unread");
            getMessagesBtn().setIcon(FontAwesome.ENVELOPE);
        } else {
            getMessagesBtn().setStyleName(ValoTheme.BUTTON_FRIENDLY);
            getMessagesBtn().setIcon(FontAwesome.ENVELOPE_OPEN);
        }
        getMessagesBtn().addStyleName(ValoTheme.BUTTON_SMALL);
    }

    public Button getMessagesBtn() {
        return messagesBtn;
    }

    public void setMessagesBtn(Button messagesBtn) {
        this.messagesBtn = messagesBtn;
    }
}
