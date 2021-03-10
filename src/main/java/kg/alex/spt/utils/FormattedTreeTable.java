/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.utils;

import com.vaadin.data.Property;
import com.vaadin.ui.TreeTable;
import kg.alex.spt.SystemSettings;

/**
 * @author alex
 */
public class FormattedTreeTable extends TreeTable {

    public FormattedTreeTable() {
        super();
    }

    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property property) {

        if (property.getType() == Double.class) {

            if (property.getValue() != null) // Format a decimal value for a specific locale
            {
                return SystemSettings.dFormat.format((Double) property.getValue());
            }
        }
        return super.formatPropertyValue(rowId, colId, property);
    }
}
