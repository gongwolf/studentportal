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

public class ReportTravelBean implements Serializable {
 
	private static final long serialVersionUID = 1L;
	private String travelCity;
	private String travelState;
	private String travelStartDate;
	private String travelEndDate;
	private String travelPurpose;
 
	public String getTravelCity() {
		return travelCity;
	}

	public void setTravelCity(String travelCity) {
		this.travelCity = travelCity;
	}

	public String getTravelState() {
		return travelState;
	}

	public void setTravelState(String travelState) {
		this.travelState = travelState;
	}
  
	public String getTravelPurpose() {
		return travelPurpose;
	}

	public void setTravelPurpose(String travelPurpose) {
		this.travelPurpose = travelPurpose;
	}

	public String getTravelStartDate() {
		return travelStartDate;
	}

	public void setTravelStartDate(String travelStartDate) {
		this.travelStartDate = travelStartDate;
	}

	public String getTravelEndDate() {
		return travelEndDate;
	}

	public void setTravelEndDate(String travelEndDate) {
		this.travelEndDate = travelEndDate;
	}
	
	
}
