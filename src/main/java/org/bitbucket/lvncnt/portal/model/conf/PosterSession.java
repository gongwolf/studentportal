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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PosterSession {
	String title; 
	List<Presenter> presenter; 
	List<Mentor> mentor; 
	String[] sponsor; 
	String posterabstract;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<Presenter> getPresenter() {
		return presenter;
	}
	public void setPresenter(List<Presenter> presenter) {
		this.presenter = presenter;
	}
	public List<Mentor> getMentor() {
		return mentor;
	}
	public void setMentor(List<Mentor> mentor) {
		this.mentor = mentor;
	}
	public String[] getSponsor() {
		return sponsor;
	}
	public void setSponsor(String[] sponsor) {
		this.sponsor = sponsor;
	}
	public String getPosterabstract() {
		return posterabstract;
	}
	public void setPosterabstract(String posterabstract) {
		this.posterabstract = posterabstract;
	} 
	
	public void addPresenter(String name, String major, String school){
		if(this.presenter == null){
			this.presenter = new ArrayList<>(); 
		}
		this.presenter.add(new Presenter(name, major, school)); 
	}
	
	public void addMentor(String name, String school){
		if(this.mentor == null){
			this.mentor = new ArrayList<>(); 
		}
		this.mentor.add(new Mentor(name, school)); 
	}
	 
	private class Presenter{
		String name; 
		String major; 
		String school;
		public Presenter(String name, String major, String school) {
			this.name = name;
			this.major = major;
			this.school = school;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getMajor() {
			return major;
		}
		public void setMajor(String major) {
			this.major = major;
		}
		public String getSchool() {
			return school;
		}
		public void setSchool(String school) {
			this.school = school;
		} 
	}
	private class Mentor{
		String name; 
		String school;
		public Mentor(String name, String school) {
			super();
			this.name = name;
			this.school = school;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getSchool() {
			return school;
		}
		public void setSchool(String school) {
			this.school = school;
		} 
		
	}
	
}
