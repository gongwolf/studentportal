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

 
public class ProfileBean implements Serializable {
 
	private static final long serialVersionUID = 1L;
	
	private Integer userID; 
	private BiographyBean biographyBean; 
	private ContactBean contactBean; 
	private EthnicityBean ethnicityBean; 
	private FileBucket fileBucket;
	private MentorBean mentorBean; // Mentor
	public Integer getUserID() {
		return userID;
	}
	public void setUserID(Integer userID) {
		this.userID = userID;
	}
	

	public BiographyBean getBiographyBean() {
		return biographyBean;
	}
	public void setBiographyBean(BiographyBean biographyBean) {
		this.biographyBean = biographyBean;
	}
	
	public ContactBean getContactBean() {
		return contactBean;
	}
	public void setContactBean(ContactBean contactBean) {
		this.contactBean = contactBean;
	}
	
	public EthnicityBean getEthnicityBean() {
		return ethnicityBean;
	}
	public void setEthnicityBean(EthnicityBean ethnicityBean) {
		this.ethnicityBean = ethnicityBean;
	}
	public FileBucket getFileBucket() {
		return fileBucket;
	}
	public void setFileBucket(FileBucket fileBucket) {
		this.fileBucket = fileBucket;
	}
	public MentorBean getMentorBean() {
		return mentorBean;
	}
	public void setMentorBean(MentorBean mentorBean) {
		this.mentorBean = mentorBean;
	} 
 
}
