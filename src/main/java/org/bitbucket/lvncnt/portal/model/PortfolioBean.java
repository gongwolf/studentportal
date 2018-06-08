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
package org.bitbucket.lvncnt.portal.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PortfolioBean implements Serializable {
	 
	private static final long serialVersionUID = 1L;
	private String portfolioID; 
	private String title; 
	private String schoolYear; 
	private String schoolSemester; 
	private String description; 
	private String reflection; 
	
	private LocalDateTime createDate; 
	
	public LocalDateTime getCreateDate() {
		if(createDate == null) return null; 
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		if(createDate == null) return; 
		this.createDate = createDate;
	}

	public String getPortfolioID() {
		return portfolioID;
	}

	public void setPortfolioID(String portfolioID) {
		this.portfolioID = portfolioID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSchoolYear() {
		return schoolYear;
	}

	public void setSchoolYear(String schoolYear) {
		this.schoolYear = schoolYear;
	}

	public String getSchoolSemester() {
		return schoolSemester;
	}

	public void setSchoolSemester(String schoolSemester) {
		this.schoolSemester = schoolSemester;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReflection() {
		return reflection;
	}

	public void setReflection(String reflection) {
		this.reflection = reflection;
	}
	 
}
