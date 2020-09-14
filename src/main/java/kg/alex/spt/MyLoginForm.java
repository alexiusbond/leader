/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt;

import com.ejt.vaadin.loginform.LoginForm;
import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import java.util.Locale;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;

/**
 *
 * @author alex
 */
public class MyLoginForm extends LoginForm {

    static final Logger logger = LogManager.getLogger(MyLoginForm.class);
    private MyVaadinUI myUI;
    private Button kyrgyz, english, russian, turkish;
    private Label errorLbl = new Label();

    public MyLoginForm(MyVaadinUI ui) {
        myUI = ui;
    }

    @Override
    protected Component createContent(TextField userNameField,
            PasswordField passwordField, Button loginButton) {
        GridLayout layout = new GridLayout(3, 5);
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setMargin(true);

        errorLbl.addStyleName("failure");
        errorLbl.setSizeUndefined();
        errorLbl.setVisible(false);
        errorLbl.setImmediate(true);

        Label languageLbl = new Label(myUI.getMessage(SptMessages.Language));
        languageLbl.setSizeUndefined();
        languageLbl.addStyleName("large");
        Label userNameLbl = new Label(myUI.getMessage(SptMessages.Username));
        userNameLbl.setSizeUndefined();
        userNameLbl.addStyleName("large");
        Label passwordLbl = new Label(myUI.getMessage(SptMessages.Password));
        passwordLbl.setSizeUndefined();
        passwordLbl.addStyleName("large");
        loginButton.setCaption(myUI.getMessage(SptMessages.Login));
        loginButton.setStyleName("large");
        loginButton.setStyleName("primary");
        loginButton.setWidth("100%");
        loginButton.setIcon(FontAwesome.SIGN_IN);
        userNameField.setCaption(null);
        userNameField.setSizeFull();
        userNameField.addStyleName("large");
        passwordField.setCaption(null);
        passwordField.setSizeFull();
        passwordField.addStyleName("large");

        final HorizontalLayout languageBar = new HorizontalLayout();
        languageBar.setSpacing(true);

        ThemeResource iconKG = new ThemeResource("../mytheme/icons/24/kg.png");
        kyrgyz = new Button();
        kyrgyz.addClickListener(new MyLoginForm.SwitchLanguage(myUI));
        kyrgyz.setStyleName("borderless");
        kyrgyz.setIcon(iconKG);
        kyrgyz.setEnabled(false);
//        kyrgyz.setEnabled(!myUI.getLocale().getLanguage().equals("ky"));
        languageBar.addComponent(kyrgyz);

        ThemeResource iconUK = new ThemeResource("../mytheme/icons/24/en.png");
        english = new Button();
        english.addClickListener(new MyLoginForm.SwitchLanguage(myUI));
        english.setStyleName("borderless");
        english.setIcon(iconUK);
        english.setEnabled(true);
//        english.setEnabled(!myUI.getLocale().getLanguage().equals("en"));
        languageBar.addComponent(english);

        ThemeResource iconRU = new ThemeResource("../mytheme/icons/24/ru.png");
        russian = new Button();
        russian.addClickListener(new MyLoginForm.SwitchLanguage(myUI));
        russian.setStyleName("borderless");
        russian.setIcon(iconRU);
        russian.setEnabled(false);
//        russian.setEnabled(!myUI.getLocale().getLanguage().equals("ru"));
        languageBar.addComponent(russian);

        ThemeResource iconTR = new ThemeResource("../mytheme/icons/24/tr.png");
        turkish = new Button();
        turkish.addClickListener(new MyLoginForm.SwitchLanguage(myUI));
        turkish.setStyleName("borderless");
        turkish.setIcon(iconTR);
        turkish.setEnabled(false);
//        turkish.setEnabled(!myUI.getLocale().getLanguage().equals("tr"));
        languageBar.addComponent(turkish);

        layout.addComponent(errorLbl, 0, 0, 2, 0);
        layout.setComponentAlignment(errorLbl, Alignment.MIDDLE_CENTER);
        layout.addComponent(userNameLbl, 0, 1);
        layout.setComponentAlignment(userNameLbl, Alignment.MIDDLE_RIGHT);
        layout.addComponent(userNameField, 1, 1, 2, 1);
        layout.addComponent(passwordLbl, 0, 2);
        layout.setComponentAlignment(passwordLbl, Alignment.MIDDLE_RIGHT);
        layout.addComponent(passwordField, 1, 2, 2, 2);
        layout.addComponent(languageLbl, 0, 4);
        layout.setComponentAlignment(languageLbl, Alignment.MIDDLE_RIGHT);
        layout.addComponent(languageBar, 1, 4);
        layout.setComponentAlignment(languageBar, Alignment.MIDDLE_LEFT);
        layout.addComponent(loginButton, 2, 4);
        layout.setComponentAlignment(loginButton, Alignment.MIDDLE_RIGHT);
        layout.setColumnExpandRatio(2, 1);
        return layout;
    }

    @Override
    protected void login(String userName, String password) {
        boolean error = false;
        if (userName != null && password != null && !userName.equals("") && !password.equals("")) {
            try {
                MyVaadinUI.getInstance().login(userName, password);
                // Switch to the protected view
                myUI.setContent(new AuthenticatedScreen(myUI));
            } catch (UnknownAccountException uae) {
                uae.printStackTrace();
                error = true;
            } catch (IncorrectCredentialsException ice) {
                ice.printStackTrace();
                error = true;
            } catch (LockedAccountException lae) {
                lae.printStackTrace();
                error = true;
            } catch (ExcessiveAttemptsException eae) {
                eae.printStackTrace();
                error = true;
            } catch (AuthenticationException ae) {
                ae.printStackTrace();
                error = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                error = true;
            }
            if (error) {
                errorLbl.setValue(myUI.getMessage(SptMessages.InvalidUserNamePassword));
                errorLbl.setVisible(true);
            }
        } else {
            errorLbl.setValue(myUI.getMessage(SptMessages.LoginUsernameNotEmpty));
            errorLbl.setVisible(true);
        }
    }

    class SwitchLanguage implements Button.ClickListener {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private MyVaadinUI myUI;

        public SwitchLanguage(MyVaadinUI myUI) {
            this.myUI = myUI;
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            final Button source = event.getButton();
            if (source == english) {
                myUI.setLocale(new Locale("en"));
            }
            if (source == turkish) {
                myUI.setLocale(new Locale("tr"));
            }
            if (source == russian) {
                myUI.setLocale(new Locale("ru"));
            }
            if (source == kyrgyz) {
                myUI.setLocale(new Locale("ky"));
            }

            myUI.getViewManager().switchScreen(LoginScreen.class.getName(),
                    new LoginScreen(myUI));
        }
    }

}
