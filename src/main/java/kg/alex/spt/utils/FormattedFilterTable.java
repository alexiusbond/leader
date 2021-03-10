/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.spt.utils;

import com.vaadin.data.Property;
import kg.alex.spt.SystemSettings;
import org.tepi.filtertable.FilterTable;

/**
 *
 * @author alex
 */
public class FormattedFilterTable extends FilterTable {

    public FormattedFilterTable() {
        super();
    }

    @Override
    protected String formatPropertyValue(Object rowId,
            Object colId, Property property) {

        if (property.getType() == Double.class) {

            if (property.getValue() != null) // Format a decimal value for a specific locale
            {
                return SystemSettings.dFormat.format((Double) property.getValue());
            }
        }
        return super.formatPropertyValue(rowId, colId, property);
    }
}
