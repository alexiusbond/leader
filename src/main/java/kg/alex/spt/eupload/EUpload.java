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

package kg.alex.spt.eupload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import kg.alex.spt.eupload.client.EUploadState;
import com.vaadin.server.Resource;
import com.vaadin.ui.Upload;

/**
 * {@link Upload} subclass which also allows to set the icon for the
 * contained button and control its style.
 *
 * @author Auke te Winkel
 *
 */
public class EUpload extends Upload {

    /**
     * SerialVersionUID
     */
    private static final long serialVersionUID = 5920393068578792849L;

    /**
     * Key for the button icon resource
     */
    public static final String BUTTON_ICON_KEY = "buttonicon";

    /**
     * Creates a new instance of EUpload.
     *
     * The receiver must be set before performing an upload.
     */
    public EUpload() {
        super();
    }

    /**
     * Constructor.
     *
     * @param caption
     *            Caption for the Upload element
     * @param uploadReceiver
     *            The {@link Receiver}
     */
    public EUpload(final String caption, final Receiver uploadReceiver) {
        super(caption, uploadReceiver);
    }

    /**
     * Add a CSS style name for the button that fires uploading.
     *
     * @param style
     *            The CSS style name to add
     */
    public final void addButtonStyleName(final String style) {
        if (style == null || "".equals(style)) {
            return;
        }
        if (style.contains(" ")) {
            // Split space separated style names and add them one by one.
            final StringTokenizer tokenizer = new StringTokenizer(style, " ");
            while (tokenizer.hasMoreTokens()) {
                this.addButtonStyleName(tokenizer.nextToken());
            }
            return;
        }

        if (this.getState().buttonstyles == null) {
            this.getState().buttonstyles = new ArrayList<String>();
        }
        final List<String> buttonstyles = this.getState().buttonstyles;
        if (!buttonstyles.contains(style)) {
            buttonstyles.add(style);
        }
    }

    /**
     * @return {@link Resource} to be rendered as icon for the button that fires
     *         uploading.
     */
    public final Resource getButtonIcon() {
        return this.getResource(BUTTON_ICON_KEY);
    }

    /**
     * Gets the primary style name for the button that fires uploading.
     *
     * @return The primary style name of the button
     */
    public final String getButtonPrimaryStyleName() {
        return this.getState(false).primaryButtonStyleName;
    }

    /**
     * Gets all user defined button style names for the button that fires
     * uploading.
     *
     * @return The user defined style names for the button
     */
    public final String getButtonStyleName() {
        String s = "";
        if (this.getState(false).buttonstyles != null && !this.getState(false).buttonstyles.isEmpty()) {
            for (final Iterator<String> it = this.getState(false).buttonstyles.iterator(); it.hasNext();) {
                s += it.next();
                if (it.hasNext()) {
                    s += " ";
                }
            }
        }
        return s;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.AbstractComponent#getState()
     */
    @Override
    protected final EUploadState getState() {
        return (EUploadState) super.getState();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.AbstractComponent#getState(boolean)
     */
    @Override
    protected final EUploadState getState(final boolean markAsDirty) {
        return (EUploadState) super.getState(markAsDirty);
    }

    /**
     * Set the caption for the button that fires uploading.
     *
     * @param buttonIcon
     *            icon for upload component button.
     */
    public final void setButtonIcon(final Resource buttonIcon) {
        this.setResource(BUTTON_ICON_KEY, buttonIcon);
    }

    /**
     * Sets the primary style name for the button that fires uploading. The
     * default "v-button" is replaced.
     *
     * @param style
     *            The primary style name to set
     */
    public final void setButtonPrimaryStyleName(final String style) {
        this.getState().primaryButtonStyleName = style;
    }

    /**
     * Set the CSS style name for the button that fires uploading. All previous
     * user-defined styles are cleared.
     *
     * @param style
     *            The CSS style name to set
     */
    public final void setButtonStyleName(final String style) {
        if (style == null || "".equals(style)) {
            this.getState().buttonstyles = null;
            return;
        }
        if (this.getState().buttonstyles == null) {
            this.getState().buttonstyles = new ArrayList<String>();
        }
        final List<String> styles = this.getState().buttonstyles;
        styles.clear();
        final StringTokenizer tokenizer = new StringTokenizer(style, " ");
        while (tokenizer.hasMoreTokens()) {
            styles.add(tokenizer.nextToken());
        }
    }
}
