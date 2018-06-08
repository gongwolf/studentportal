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
package org.bitbucket.lvncnt.portal.model.conf;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScheduleSession {
	private String timeStart;
	private String timeEnd;
	private String name;
	private String location;
	private String track;
	private String description; 
	private String[] speakers; 
	private String[] panelists;
	private String[] facilitators;
	
	public String getTimeStart() {
		return timeStart;
	}
	public void setTimeStart(String timeStart) {
		this.timeStart = timeStart;
	}
	public String getTimeEnd() {
		return timeEnd;
	}
	public void setTimeEnd(String timeEnd) {
		this.timeEnd = timeEnd;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getTrack() {
		return track;
	}
	public void setTrack(String track) {
		this.track = track;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String[] getSpeakers() {
		return speakers;
	}
	public void setSpeakers(String[] speakers) {
		this.speakers = speakers;
	}
	public String[] getPanelists() {
		return panelists;
	}
	public void setPanelists(String[] panelists) {
		this.panelists = panelists;
	}
	public String[] getFacilitators() {
		return facilitators;
	}
	public void setFacilitators(String[] facilitators) {
		this.facilitators = facilitators;
	}
	
	
}
