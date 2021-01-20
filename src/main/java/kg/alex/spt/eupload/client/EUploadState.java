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

import java.util.List;
import com.vaadin.shared.ui.upload.UploadState;

/**
 * State for the EUPload widget
 *
 * @author Auke te Winkel
 *
 */
public class EUploadState extends UploadState {

    /**
     * SerialVersionUID
     */
    private static final long serialVersionUID = -5210434821065120622L;

    /**
     * Styles of the embedded button which initiates upload
     */
    public List<String> buttonstyles = null;

    /**
     * Default primary style name for the embedded button
     */
    public String primaryButtonStyleName = "v-button";

}
