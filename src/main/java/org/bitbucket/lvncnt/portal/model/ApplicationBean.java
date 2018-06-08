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
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

public class ApplicationBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer userID; 
	private Integer applicationID;
	
	private String firstName;
	private String lastName;
	private String email;
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date birthDate; 
	
	private int schoolYear;
	private String schoolSemester;
	private String programNameAbbr; 
	private String programNameFull;

	private String schoolTarget;
	private String academicSchool;
	
	private Integer mentorID; 
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date startDate;
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date applicantSubmitDate;
	
	private String applicantSignature; 
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date transcriptDate;
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date mentorSubmitDate;
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date recommenderSubmitDate; 
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date mentorInfoDate; 
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date medicalSubmitDate; 
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date completeDate;
	
	private String decision; 
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date notifiedDate; 
	
	public ApplicationBean() {
	}
	
	public ApplicationBean(ApplicationBean from){
		this.userID=from.userID;
		this.applicationID=from.applicationID;
		
		this.firstName = from.firstName; 
		this.lastName = from.lastName; 
		this.email = from.email; 
		this.birthDate = from.birthDate; 
		
		this.schoolYear=from.schoolYear;
		this.schoolSemester=from.schoolSemester;
		this.programNameAbbr=from.programNameAbbr;
		this.programNameFull=from.programNameFull;
		this.startDate=from.startDate;
		this.applicantSubmitDate=from.applicantSubmitDate;
		this.applicantSignature = from.applicantSignature; 
		this.transcriptDate=from.transcriptDate;
		this.mentorSubmitDate=from.mentorSubmitDate;
		this.recommenderSubmitDate = from.recommenderSubmitDate; 
		this.mentorInfoDate = from.mentorInfoDate;
		this.medicalSubmitDate = from.medicalSubmitDate; 
		this.completeDate=from.completeDate;
		this.decision = from.decision;

		this.schoolTarget = from.schoolTarget;
		this.academicSchool = from.academicSchool;
	}

	private AcademicBean academicBean;
	private InvolvementBean involvementBean; 
	private EssayBean essayBean; 
	private MentorBean mentorBean; 
	private ProjectBean projectBean; 
	private FileBucket fileBucket; 
	private FileBucket referenceBucket; 
	
	private RecommenderBean recommenderBean; 
	private BudgetBean budgetBean; 
	private IREPProjectBean irepProjectBean; 
	
	List<EvaluationBean> evaList; 
	
	public String getSchoolTarget() {
		return schoolTarget;
	}

	public void setSchoolTarget(String schoolTarget) {
		this.schoolTarget = schoolTarget;
	}

    public String getAcademicSchool() {
        return academicSchool;
    }

    public void setAcademicSchool(String academicSchool) {
        this.academicSchool = academicSchool;
    }

    public Date getBirthDate() {
		if(birthDate == null) return null; 
		return (Date)birthDate.clone();
	}

	public void setBirthDate(Date birthDate) {
		if(birthDate == null)return; 
		this.birthDate = (Date)birthDate.clone();
	}

	public Date getRecommenderSubmitDate() {
		if(recommenderSubmitDate == null) return null; 
		return (Date)recommenderSubmitDate.clone();
	}

	public void setRecommenderSubmitDate(Date recommenderSubmitDate) {
		if(recommenderSubmitDate == null) return; 
		this.recommenderSubmitDate = (Date)recommenderSubmitDate.clone();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getUserID() {
		return userID;
	}

	public void setUserID(Integer userID) {
		this.userID = userID;
	}

	public Integer getApplicationID() {
		return applicationID;
	}
	public void setApplicationID(Integer applicationID) {
		this.applicationID = applicationID;
	}
	public int getSchoolYear() {
		return schoolYear;
	}
	public void setSchoolYear(int schoolYear) {
		this.schoolYear = schoolYear;
	}
	
	public String getSchoolSemester() {
		return schoolSemester;
	}
	public void setSchoolSemester(String schoolSemester) {
		this.schoolSemester = schoolSemester;
	}
	
	public String getProgramNameAbbr() {
		return programNameAbbr;
	}
	public void setProgramNameAbbr(String program) {
		this.programNameAbbr = program;
	}
	
	public String getProgramNameFull() {
		return programNameFull;
	}

	public void setProgramNameFull(String programName) {
		this.programNameFull = programName;
	}

	public Date getStartDate() {
		if(startDate == null) return null; 
		return (Date)startDate.clone();
	}
	public void setStartDate(Date startDate) {
		if(startDate == null) return; 
		this.startDate = (Date)startDate.clone();
	}
	public Date getApplicantSubmitDate() {
		if(applicantSubmitDate == null) return null; 
		return (Date)applicantSubmitDate.clone();
	}
	public void setApplicantSubmitDate(Date applicantSubmitDate) {
		if(applicantSubmitDate == null) return; 
		this.applicantSubmitDate = (Date)applicantSubmitDate.clone();
	}
	public Date getTranscriptDate() {
		if(transcriptDate == null) return null; 
		return (Date)transcriptDate.clone();
	}
	public void setTranscriptDate(Date transcriptDate) {
		if(transcriptDate == null) return; 
		this.transcriptDate = (Date)transcriptDate.clone();
	}
	public Date getMentorSubmitDate() {
		if(mentorSubmitDate == null) return null; 
		return (Date)mentorSubmitDate.clone();
	}
	public void setMentorSubmitDate(Date mentorSubmitDate) {
		if(mentorSubmitDate == null) return; 
		this.mentorSubmitDate = (Date)mentorSubmitDate.clone();
	}
	public Date getCompleteDate() {
		if(completeDate == null) return null; 
		return (Date)completeDate.clone();
	}
	public void setCompleteDate(Date completeDate) {
		if(completeDate == null) return; 
		this.completeDate = (Date)completeDate.clone();
	}

	public String getApplicantSignature() {
		return applicantSignature;
	}

	public void setApplicantSignature(String applicantSignature) {
		this.applicantSignature = applicantSignature;
	}

	public Integer getMentorID() {
		return mentorID;
	}

	public void setMentorID(Integer mentorID) {
		this.mentorID = mentorID;
	}

	public AcademicBean getAcademicBean() {
		if(this.academicBean == null) return null; 
		return new AcademicBean(this.academicBean);
	}

	public void setAcademicBean(AcademicBean academicBean) {
		this.academicBean = new AcademicBean(academicBean);
	} 
	
	public InvolvementBean getInvolvementBean() {
		if(this.involvementBean == null) return null;
		return new InvolvementBean(this.involvementBean);
	}

	public void setInvolvementBean(InvolvementBean involvementBean) {
		this.involvementBean = new InvolvementBean(involvementBean);
	}

	public EssayBean getEssayBean() {
		if(this.essayBean == null) return null;
		return new EssayBean(essayBean);
	}

	public void setEssayBean(EssayBean essayBean) {
		this.essayBean = new EssayBean(essayBean);
	}
  
	public MentorBean getMentorBean() {
		if(this.mentorBean == null) return null;
		return new MentorBean(mentorBean);
	}

	public void setMentorBean(MentorBean mentorBean) {
		this.mentorBean = new MentorBean(mentorBean);
	}
	
	public ProjectBean getProjectBean() {
		if(this.projectBean == null) return null; 
		return new ProjectBean(this.projectBean);
	}

	public void setProjectBean(ProjectBean projectBean) {
		this.projectBean = new ProjectBean(projectBean);
	}
	public FileBucket getFileBucket() {
		return fileBucket;
	}

	public void setFileBucket(FileBucket fileBucket) {
		this.fileBucket = fileBucket;
	}
 
	public FileBucket getReferenceBucket() {
		return referenceBucket;
	}

	public void setReferenceBucket(FileBucket referenceBucket) {
		this.referenceBucket = referenceBucket;
	}

	public RecommenderBean getRecommenderBean() {
		return recommenderBean;
	}

	public void setRecommenderBean(RecommenderBean recommenderBean) {
		this.recommenderBean = recommenderBean;
	}

	public BudgetBean getBudgetBean() {
		return budgetBean;
	}

	public void setBudgetBean(BudgetBean budgetBean) {
		this.budgetBean = budgetBean;
	}

	public IREPProjectBean getIrepProjectBean() {
		return irepProjectBean;
	}

	public void setIrepProjectBean(IREPProjectBean irepProjectBean) {
		this.irepProjectBean = irepProjectBean;
	}

	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}

	public Date getNotifiedDate() {
		if(notifiedDate == null) return null; 
		return (Date)notifiedDate.clone();
	}

	public void setNotifiedDate(Date notifiedDate) {
		if(notifiedDate == null) return; 
		this.notifiedDate = (Date)notifiedDate.clone();
	}

	public List<EvaluationBean> getEvaList() {
		return evaList;
	}

	public void setEvaList(List<EvaluationBean> evaList) {
		this.evaList = evaList;
	}
	
	private String addressLine1, addressLine2; 
	private String addressCity, addressState, addressZip;

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getAddressCity() {
		return addressCity;
	}

	public void setAddressCity(String addressCity) {
		this.addressCity = addressCity;
	}

	public String getAddressState() {
		return addressState;
	}

	public void setAddressState(String addressState) {
		this.addressState = addressState;
	}

	public String getAddressZip() {
		return addressZip;
	}

	public void setAddressZip(String addressZip) {
		this.addressZip = addressZip;
	}

	public Date getMentorInfoDate() {
		return mentorInfoDate;
	}

	public void setMentorInfoDate(Date mentorInfoDate) {
		this.mentorInfoDate = mentorInfoDate;
	}

	public Date getMedicalSubmitDate() {
		return medicalSubmitDate;
	}

	public void setMedicalSubmitDate(Date medicalSubmitDate) {
		this.medicalSubmitDate = medicalSubmitDate;
	}
	
	public ApplicationBean appBean()
	{
		return this;
	}
 
}
