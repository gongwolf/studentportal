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

public class ReportOthersBean implements Serializable {
 
	private static final long serialVersionUID = 1L;
	private Integer windowID; 
	private Integer userID; 
	private String semester; 
	
	public String getSemester() {
		return semester;
	}

	public Integer getWindowID() {
		return windowID;
	}

	public void setWindowID(Integer windowID) {
		this.windowID = windowID;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}

	public ReportOthersBean() {
		 
	}

	private String otherActivities;

	public String getOtherActivities() {
		return otherActivities;
	}

	public void setOtherActivities(String otherActivities) {
		this.otherActivities = otherActivities;
	} 
	 
}
