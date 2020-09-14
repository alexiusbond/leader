/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.utils;

import com.vaadin.ui.ComboBox;

/**
 *
 * @author alex
 */
public class ComboBoxMax extends ComboBox {

    public ComboBoxMax() {
        this.setPageLength(100);
    }
    
    public ComboBoxMax(String caption) {
        this.setCaption(caption);
        this.setPageLength(100);
    }
}
