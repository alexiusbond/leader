/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.utils;

import com.vaadin.data.Validator;

/**
 *
 * @author alex
 */
public class BalanceValidator implements Validator {

    public boolean isValid(Object value) {

        int number;
        try {
            number = Integer.parseInt(value.toString());
        } catch (Exception e) {
            return false;
        }
        if (number < 0 || number > 100000) {
            return false;
        }
        return true;
    }

    // Upon failure, the validate() method throws an exception
    // with an error message.
    public void validate(Object value)
            throws Validator.InvalidValueException {
        if (!isValid(value)) {
//            if (value != null
//                    && value.toString().startsWith("-")) {
//                throw new Validator.InvalidValueException(
//                        myui.getMessage(SptMessages.NotifWrongValue));
//            } else {
//                throw new Validator.InvalidValueException(
//                        myui.getMessage(SptMessages.NotifWrongValue));
//            }
        }
    }
}
