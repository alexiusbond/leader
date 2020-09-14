/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.utils;

import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

/**
 *
 * @author alex
 */
public class ComboBoxMultiselectMax extends ComboBoxMultiselect {

    public ComboBoxMultiselectMax() {
        this.setPageLength(100);
    }
    
    public ComboBoxMultiselectMax(String caption) {
        this.setCaption(caption);
        this.setPageLength(100);
    }
}
