/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.utils;

import com.vaadin.data.Container;
import com.vaadin.data.Validator;
import java.util.Iterator;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.i18n.SptMessages;

/**
 *
 * @author alex
 */
public class ExistsValidator implements Validator {

    private Container container;
    private MyVaadinUI myUi;
    private ComboBoxMax comboBox;

    public ExistsValidator(MyVaadinUI myUi, Container container, ComboBoxMax comboBox) {
        this.myUi = myUi;
        this.container = container;
        this.comboBox = comboBox;
    }

    @Override
    public void validate(Object value) throws InvalidValueException {
        if (!isValid(value)) {
            throw new InvalidValueException(myUi.getMessage(SptMessages.ExistsNotification));
        }
    }

    private boolean isValid(Object value) {
        Iterator iter = container.getItemIds().iterator();
        System.out.println("____________");
        while (iter.hasNext()) {
            Object next = iter.next();
            System.out.println((int) value);
            System.out.println((int) ((ComboBoxMax) container.getItem(next).getItemProperty(
                    myUi.getMessage(SptMessages.Category)).getValue()).getValue());
            System.out.println("Not same " + (comboBox != container.getItem(next).getItemProperty(myUi.getMessage(SptMessages.Category)).getValue()));
            System.out.println("The exists " + ((int) ((ComboBoxMax) container.getItem(next).getItemProperty(
                    myUi.getMessage(SptMessages.Category)).getValue()).getValue() == (int) value));
            if (comboBox != container.getItem(next).getItemProperty(myUi.getMessage(SptMessages.Category)).getValue()
                    && (int) ((ComboBoxMax) container.getItem(next).getItemProperty(
                            myUi.getMessage(SptMessages.Category)).getValue()).getValue() == (int) value) {
                return false;
            }
        }
        return true;
    }
}
