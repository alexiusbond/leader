package kg.alex.spt.utils;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.converter.Converter;
import kg.alex.spt.MyVaadinUI;
import kg.alex.spt.i18n.SptMessages;

import java.util.Locale;

public class ValueFromContainerConverter implements Converter<String, Integer> {
    private IndexedContainer container;
    private MyVaadinUI myUi;

    public ValueFromContainerConverter(IndexedContainer container, MyVaadinUI myUi) {
        this.container = container;
        this.myUi = myUi;
    }

    @Override
    public Integer convertToModel(String value, Class<? extends Integer> targetType, Locale locale) throws ConversionException {
        return 0;
    }

    @Override
    public String convertToPresentation(Integer value, Class<? extends String> targetType, Locale locale) throws ConversionException {
        if (container.getContainerProperty(value, myUi.getMessage(SptMessages.Title)).getValue() != null) {
            return container.getContainerProperty(value, myUi.getMessage(SptMessages.Title)).getValue().toString();
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
