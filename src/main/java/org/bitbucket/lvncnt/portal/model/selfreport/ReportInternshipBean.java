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

public class ReportInternshipBean implements Serializable {
 
	private static final long serialVersionUID = 1L;
	private String jobCompany;
	private String jobCity;
	private String jobState;
	private String jobStartDate;
	private String jobEndDate;
	private String jobType;
	private String jobDuty;

	public String getJobCompany() {
		return jobCompany;
	}

	public void setJobCompany(String jobCompany) {
		this.jobCompany = jobCompany;
	}

	public String getJobCity() {
		return jobCity;
	}

	public void setJobCity(String jobCity) {
		this.jobCity = jobCity;
	}

	public String getJobState() {
		return jobState;
	}

	public void setJobState(String jobState) {
		this.jobState = jobState;
	}

	public String getJobStartDate() {
		return jobStartDate;
	}

	public void setJobStartDate(String jobStartDate) {
		this.jobStartDate = jobStartDate;
	}

	public String getJobEndDate() {
		return jobEndDate;
	}

	public void setJobEndDate(String jobEndDate) {
		this.jobEndDate = jobEndDate;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getJobDuty() {
		return jobDuty;
	}

	public void setJobDuty(String jobDuty) {
		this.jobDuty = jobDuty;
	}
 
}
