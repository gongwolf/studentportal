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
import java.util.ArrayList;
import java.util.List;

 
public class EthnicityBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Integer userID; 
	 
	public Integer getUserID() {
		return userID;
	}
	public void setUserID(Integer userID) {
		this.userID = userID;
	}
	
	// Ethnicity and Race
	private List<String> race;
	private Integer isHispanic;
	private String disability;
	 
	public List<String> getRace() {
		if(race == null) return null; 
		return new ArrayList<String>(race);
	}
	public void setRace(List<String> race) {
		if(race == null) return; 
		this.race = new ArrayList<>(race);
	}
	
	public Integer getIsHispanic() {
		return isHispanic;
	}

	public void setIsHispanic(Integer isHispanic) {
		this.isHispanic = isHispanic;
	}

	public String getDisability() {
		return disability;
	}
	public void setDisability(String disability) {
		this.disability = disability;
	}
	
}
