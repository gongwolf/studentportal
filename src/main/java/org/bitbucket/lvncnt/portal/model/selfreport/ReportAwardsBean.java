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

public class ReportAwardsBean implements Serializable {
 
	private static final long serialVersionUID = 1L;

	private String awardsName;
	private String awardsSemester;
	private String awardsYear;

	public String getAwardsName() {
		return awardsName;
	}

	public void setAwardsName(String awardsName) {
		this.awardsName = awardsName;
	}

	public String getAwardsSemester() {
		return awardsSemester;
	}

	public void setAwardsSemester(String awardsSemester) {
		this.awardsSemester = awardsSemester;
	}

	public String getAwardsYear() {
		return awardsYear;
	}

	public void setAwardsYear(String awardsYear) {
		this.awardsYear = awardsYear;
	}
	
}
