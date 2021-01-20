/*
 * Copyright 2014 Eijsink Afrekensystemen BV/Eijsink Software BV
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package kg.alex.spt.eupload.client;

import com.google.gwt.core.client.JsArrayString;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.StyleConstants;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.ui.Icon;
import com.vaadin.client.ui.upload.UploadConnector;
import com.vaadin.shared.ui.Connect;
import kg.alex.spt.eupload.EUpload;
import kg.alex.spt.eupload.client.EUploadState;

/**
 * Connector for {@link EUpload}
 *
 * @author Auke te Winkel
 *
 */
@Connect(EUpload.class)
public class EUploadConnector extends UploadConnector {

    /**
     * SerialVersionUID
     */
    private static final long serialVersionUID = -1123209461842738537L;
    /**
     * Style names for the embedded button
     */
    private final JsArrayString styleNames = JsArrayString.createArray().cast();

    /**
     * {@inheritDoc}
     *
     * @see com.vaadin.client.ui.AbstractComponentConnector#getState()
     */
    @Override
    public final EUploadState getState() {
        return (EUploadState) super.getState();
    }

    /**
     * Adds or removes a style name for the embedded button
     *
     * @param styleName
     *            The style name
     * @param add
     *            <code>true</code> if the style should be added, <code>false</code> if it should be removed
     */
    protected final void setButtonStyleName(final String styleName, final boolean add) {
        this.getWidget().submitButton.setStyleName(styleName, add);
    }

    /**
     * Adds or removes a style name with prefix for the embedded button
     *
     * @param prefix
     *            The prefix to apply
     * @param styleName
     *            The style name
     * @param add
     *            <code>true</code> if the style should be added, <code>false</code> if it should be removed
     */
    protected final void setButtonStyleNameWithPrefix(final String prefix, final String styleName, final boolean add) {
        String prefixToUse = prefix;
        if (!styleName.startsWith("-")) {
            if (!prefixToUse.endsWith("-")) {
                prefixToUse += "-";
            }
        } else {
            if (prefixToUse.endsWith("-")) {
                styleName.replaceFirst("-", "");
            }
        }
        this.getWidget().submitButton.setStyleName(prefixToUse + styleName, add);
    }

    /**
     * Updates the button style names
     *
     */
    @OnStateChange("buttonstyles")
    private void updateButtonStyleNames() {
        final EUploadState state = this.getState();

        final String primaryButtonStyleName = this.getWidget().submitButton.getStylePrimaryName();

        // Set the core 'v' style name for the widget
        // Not added to upload button currently
        this.setButtonStyleName(StyleConstants.UI_WIDGET, true);

        // add additional user defined style names as class names, prefixed with
        // component default class name. remove nonexistent style names.

        // Remove all old stylenames
        for (int i = 0; i < this.styleNames.length(); i++) {
            final String oldStyle = this.styleNames.get(i);
            this.setButtonStyleName(oldStyle, false);
            this.setButtonStyleNameWithPrefix(primaryButtonStyleName + "-", oldStyle, false);
        }
        this.styleNames.setLength(0);

        if (state.buttonstyles != null && !state.buttonstyles.isEmpty()) {
            // add new style names
            for (final String newStyle : state.buttonstyles) {
                this.setButtonStyleName(newStyle, true);
                this.setButtonStyleNameWithPrefix(primaryButtonStyleName + "-", newStyle, true);
                this.styleNames.push(newStyle);
            }

        }

        if (state.primaryButtonStyleName != null && !state.primaryButtonStyleName.equals(primaryButtonStyleName)) {
            /*
             * We overwrite the button's primary stylename if state defines a
             * primary stylename. This has to be done after updating other
             * styles to be sure the dependent styles are updated correctly.
             */
            this.getWidget().submitButton.setStylePrimaryName(state.primaryButtonStyleName);
        }
    }

    /**
     * Updates the button icon
     *
     */
    @OnStateChange("resources")
    private void updateIcon() {
        if (this.getWidget().submitButton.icon != null) {
            this.getWidget().submitButton.wrapper.removeChild(this.getWidget().submitButton.icon.getElement());
            this.getWidget().submitButton.icon = null;
        }

        final String iconUrl = this.getResourceUrl(EUpload.BUTTON_ICON_KEY);

        if (iconUrl != null) {
            final ApplicationConnection client = this.getConnection();
            final Icon icon = client.getIcon(iconUrl);

            this.getWidget().submitButton.icon = icon;
            this.getWidget().submitButton.wrapper.insertBefore(icon.getElement(), this.getWidget().submitButton.captionElement);
        }
    }

}
