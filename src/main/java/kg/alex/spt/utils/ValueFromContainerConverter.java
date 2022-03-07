package kg.alex.spt.utils;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.converter.Converter;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.dao.DbAccCategory;
import kg.alex.spt.dao.DbEmployee;
import kg.alex.spt.i18n.SptMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

public class ValueFromContainerConverter implements Converter<String, Integer> {
    static final Logger logger = LogManager.getLogger(ValueFromContainerConverter.class);
    private IndexedContainer container;
    private String propertyId;
    private MyVaadinUI myUi;

    public ValueFromContainerConverter(IndexedContainer container, String propertyId, MyVaadinUI myUi) {
        this.container = container;
        this.propertyId = propertyId;
        this.myUi = myUi;
    }

    @Override
    public Integer convertToModel(String value, Class<? extends Integer> targetType, Locale locale) throws ConversionException {
        return 0;
    }

    @Override
    public String convertToPresentation(Integer value, Class<? extends String> targetType, Locale locale) throws ConversionException {
        if (container.getContainerProperty(value, myUi.getMessage(SptMessages.Title)) != null &&
                container.getContainerProperty(value, myUi.getMessage(SptMessages.Title)).getValue() != null) {
            return container.getContainerProperty(value, myUi.getMessage(SptMessages.Title)).getValue().toString();
        } else if (value != null && propertyId != null && propertyId.equals(myUi.getMessage(SptMessages.Category))) {
            try {
                DbAccCategory dbCon = new DbAccCategory();
                dbCon.connect();
                String category = dbCon.exec_for_select_by_id(value);
                dbCon.close();
                if (category != null) {
                    return category;
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (value != null && propertyId != null && propertyId.equals(myUi.getMessage(SptMessages.ToEmployee))) {
            try {
                DbEmployee dbCon = new DbEmployee();
                dbCon.connect();
                String category = dbCon.execSQL_by_id(value);
                dbCon.close();
                if (category != null) {
                    return category;
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        return null;
    }

    @Override
    public Class<Integer> getModelType() {
        return Integer.class;
    }

    @Override
    public Class<String> getPresentationType() {
        return String.class;
    }
}
