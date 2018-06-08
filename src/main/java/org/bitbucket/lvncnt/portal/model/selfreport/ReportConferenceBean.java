/*
 * Copyright (c) 2018 Feng Liu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitbucket.lvncnt.portal.model.selfreport;

import java.io.Serializable;

public class ReportConferenceBean implements Serializable {
 
	private static final long serialVersionUID = 1L;
	private String conferenceName;
	private String conferenceDate;
	private String conferencePresentationTitle;
	private String conferencePresentationType;

	public String getConferenceName() {
		return conferenceName;
	}

	public void setConferenceName(String conferenceName) {
		this.conferenceName = conferenceName;
	}

	public String getConferenceDate() {
		return conferenceDate;
	}

	public void setConferenceDate(String conferenceDate) {
		this.conferenceDate = conferenceDate;
	}

	public String getConferencePresentationTitle() {
		return conferencePresentationTitle;
	}

	public void setConferencePresentationTitle(String conferencePresentationTitle) {
		this.conferencePresentationTitle = conferencePresentationTitle;
	}

	public String getConferencePresentationType() {
		return conferencePresentationType;
	}

	public void setConferencePresentationType(String conferencePresentationType) {
		this.conferencePresentationType = conferencePresentationType;
	}
}
