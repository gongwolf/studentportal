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
package org.bitbucket.lvncnt.portal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitbucket.lvncnt.portal.model.*;
import org.bitbucket.lvncnt.portal.model.selfreport.*;
import org.bitbucket.lvncnt.utilities.Parse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service("excelXlsxView")
public class ExcelXlsxView extends AbstractXlsxView{
	
	private static final String EXCEL_SELFREPORT_LIST = "EXCEL_SELFREPORT_LIST"; 
	private static final String EXCEL_PROFILE_LIST = "EXCEL_PROFILE_LIST"; 
	private static final String EXCEL_APPLICATION_LIST = "EXCEL_APPLICATION_LIST"; 
	private static final String EXCEL_EVAL_MID = "EXCEL_EVAL_MID"; 
	private static final String EXCEL_EVAL_END = "EXCEL_EVAL_END"; 
	private static final String EXCEL_EVAL_MID_STU = "EXCEL_EVAL_MID_STU";
 
	@Override
	@SuppressWarnings("unchecked")
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		 
		for(String key: model.keySet()){
			switch (key) {
			case EXCEL_SELFREPORT_LIST:
				List<SelfReportBean> selfreportList = (List<SelfReportBean>)model.get(EXCEL_SELFREPORT_LIST);
				buildExcelDocumentSelfReport(model, workbook, request, response, selfreportList);
				break;

			case EXCEL_PROFILE_LIST:
				List<ProfileBean> profileList = (List<ProfileBean>)model.get(EXCEL_PROFILE_LIST);
				buildExcelDocumentProfile(model, workbook, request, response, profileList);
				break;
				
			case EXCEL_APPLICATION_LIST:
				List<ApplicationBean> appList = (List<ApplicationBean>)model.get(EXCEL_APPLICATION_LIST);
				String program = (String)model.get("program");
				switch (program) {
				case ProgramCode.CCCONF:
					buildExcelDocumentAplicationCCCONF(model, workbook, request, response, appList);
					break;
					
				case ProgramCode.URS:
					buildExcelDocumentAplicationURS(model, workbook, request, response, appList);
					break;
					
				case ProgramCode.MESA:
					buildExcelDocumentAplicationMESA(model, workbook, request, response, appList);
					break; 

				case ProgramCode.TRANS:
					buildExcelDocumentAplicationTRANS(model, workbook, request, response, appList);
					break; 
					
				case ProgramCode.SCCORE:
					buildExcelDocumentAplicationSCCORE(model, workbook, request, response, appList);
					break;
					
				case ProgramCode.IREP:
					buildExcelDocumentAplicationIREP(model, workbook, request, response, appList);
					break;
				}
				break;
				
			case EXCEL_EVAL_MID: 
				List<List<String>> evaList = (List<List<String>>)model.get(EXCEL_EVAL_MID);
				buildExcelDocumentEvalMid(model, workbook, request, response, evaList);
				break; 
				
			case EXCEL_EVAL_END: 
				buildExcelDocumentEvalEnd(model, workbook, request, response, 
						(List<List<String>>)model.get(EXCEL_EVAL_END));
				break; 
				
			case EXCEL_EVAL_MID_STU: 
				buildExcelDocumentEvalMidStu(model, workbook, request, response, 
						 (List<List<String>>)model.get(EXCEL_EVAL_MID_STU));
				break; 
			}
		}
	}
	 
	private void buildExcelDocumentProfile(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response, 
			List<ProfileBean> profileList) throws Exception {
		String filename = LocalDate.now().toString(); 
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx"); 
		
		String sheetname = "result";
		Sheet sheet = workbook.createSheet(sheetname); 
		 
		int rowCount = 0, colCount = 0;
		String[] profileHeader = {"User ID", 
			"First Name", "Last Name", "Date of Birth", "Email", "MI", "SSN last 4 digits", "Gender",
			"New Mexico Resident?", "Citizenship", "Parent has bachelor degree?", 
			"Email Alternate", "Phone Num-1", "Phone type-1", "Phone Num-2", "Phone type-2", "Receive SMS?", 
			"permanent address line1", "permanent address line2", "permanent address city",
			"permanent address county", "permanent address state", "permanent address zip",
			"current address line1","current address line2","current address city",
			"current address county","current address state","current address zip",
			"Name of person who will always know how to reach you", "Cell Phone", "Phone Type", "Alternate Phone", "Phone Type", "Relationship to Applicant",
			"Address of Above Individual", "City", "County", "State", "Zip",
			"Hispanic or Latino?", "Race", "Disability?"
		}; 
		Row header = sheet.createRow(rowCount); 
		for(colCount = 0; colCount < profileHeader.length; colCount ++){
			header.createCell(colCount).setCellValue(profileHeader[colCount]);
		}
		
		rowCount ++;
		for(ProfileBean bean: profileList){
			colCount = 0;
			Row row = sheet.createRow(rowCount ++);
			BiographyBean bioBean = bean.getBiographyBean();
			ContactBean conBean = bean.getContactBean();
			EthnicityBean etBean = bean.getEthnicityBean();
			
			row.createCell(colCount++).setCellValue(bean.getUserID());
			row.createCell(colCount++).setCellValue(bioBean.getFirstName());
			row.createCell(colCount++).setCellValue(bioBean.getLastName());
			if(bioBean.getBirthDate() != null){
				row.createCell(colCount++).setCellValue(
						Parse.FORMAT_DATE_MDY.format(bioBean.getBirthDate()));
			}else{
				row.createCell(colCount++).setCellValue("");
			}
			row.createCell(colCount++).setCellValue(conBean.getEmailPref());
			row.createCell(colCount++).setCellValue(bioBean.getMiddleName());
			row.createCell(colCount++).setCellValue(bioBean.getSsnLastFour());
			row.createCell(colCount++).setCellValue(bioBean.getGender());
			if(bioBean.getIsNMResident() != null){
				row.createCell(colCount++).setCellValue(
						ProgramCode.YES_NO.get(String.valueOf(bioBean.getIsNMResident())));
			}else{
				row.createCell(colCount++).setCellValue("");
			}
			
			if(bioBean.getCitizenship() != null){
				row.createCell(colCount++).setCellValue(
						ProgramCode.CITIZENSHIP.get(String.valueOf(bioBean.getCitizenship())));
			}else{
				row.createCell(colCount++).setCellValue("");
			}
			
			if(bioBean.getParentHasDegree() != null){
				row.createCell(colCount++).setCellValue(
						ProgramCode.YES_NO.get(String.valueOf(bioBean.getParentHasDegree())));
			}else{
				row.createCell(colCount++).setCellValue("");
			}
			
			row.createCell(colCount++).setCellValue(conBean.getEmailAltn());
			row.createCell(colCount++).setCellValue(conBean.getPhoneNum1());
			row.createCell(colCount++).setCellValue(conBean.getPhoneType1());
			row.createCell(colCount++).setCellValue(conBean.getPhoneNum2());
			row.createCell(colCount++).setCellValue(conBean.getPhoneType2());
			if(conBean.getReceiveSMS() != null){
				row.createCell(colCount++).setCellValue(
						ProgramCode.YES_NO.get(String.valueOf(conBean.getReceiveSMS())));
			}else{
				row.createCell(colCount++).setCellValue("");
			}
			
			row.createCell(colCount++).setCellValue(conBean.getPermanentAddressLine1());
			row.createCell(colCount++).setCellValue(conBean.getPermanentAddressLine2());
			row.createCell(colCount++).setCellValue(conBean.getPermanentAddressCity());
			row.createCell(colCount++).setCellValue(conBean.getPermanentAddressCounty());
			row.createCell(colCount++).setCellValue(conBean.getPermanentAddressState());
			row.createCell(colCount++).setCellValue(conBean.getPermanentAddressZip());
			row.createCell(colCount++).setCellValue(conBean.getCurrentAddressLine1());
			row.createCell(colCount++).setCellValue(conBean.getCurrentAddressLine2());
			row.createCell(colCount++).setCellValue(conBean.getCurrentAddressCity());
			row.createCell(colCount++).setCellValue(conBean.getCurrentAddressCounty());
			row.createCell(colCount++).setCellValue(conBean.getCurrentAddressState());
			row.createCell(colCount++).setCellValue(conBean.getCurrentAddressZip());
			
			if(conBean.getEcFirstName() != null && conBean.getEcLastName() != null){
				row.createCell(colCount++).setCellValue(conBean.getEcFirstName()+" "+conBean.getEcLastName());
			}else{
				row.createCell(colCount++).setCellValue("");
			}
			
			row.createCell(colCount++).setCellValue(conBean.getEcPhoneNum1());
			row.createCell(colCount++).setCellValue(conBean.getEcPhoneType1());
			row.createCell(colCount++).setCellValue(conBean.getEcPhoneNum2());
			row.createCell(colCount++).setCellValue(conBean.getEcPhoneType2());
			row.createCell(colCount++).setCellValue(conBean.getEcRelationship());
			
			if(conBean.getEcAddressLine1() != null && conBean.getEcAddressLine2() != null){
				row.createCell(colCount++).setCellValue(conBean.getEcAddressLine1()+" "+conBean.getEcAddressLine2());
			}else{
				row.createCell(colCount++).setCellValue("");
			}
			
			row.createCell(colCount++).setCellValue(conBean.getEcAddressCity());
			row.createCell(colCount++).setCellValue(conBean.getEcAddressCounty());
			row.createCell(colCount++).setCellValue(conBean.getEcAddressState());
			row.createCell(colCount++).setCellValue(conBean.getEcAddressZip());
			
			if(etBean != null && etBean.getIsHispanic() != null){
				row.createCell(colCount++).setCellValue(
						ProgramCode.YES_NO.get(String.valueOf(etBean.getIsHispanic())));
			}else{
				row.createCell(colCount++).setCellValue("");
			}
			
			if(etBean != null && etBean.getRace() != null){
				row.createCell(colCount++).setCellValue(etBean.getRace().toString());
			}else{
				row.createCell(colCount++).setCellValue("");
			}

			if(etBean != null && etBean.getDisability() != null){
				row.createCell(colCount++).setCellValue(
						ProgramCode.DISABILITY_STATUS.get(etBean.getDisability()));
			}else{
				row.createCell(colCount++).setCellValue("");
			}
			
		}
	}

	private void buildExcelDocumentAplicationCCCONF(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response, 
			List<ApplicationBean> applicationList) throws Exception {
		String filename = LocalDate.now().toString(); 
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx"); 
		
		String sheetname = "result";
		Sheet sheet = workbook.createSheet(sheetname); 
		 
		int rowCount = 0, colCount = 0;
		String[] applicantHeader = {"", "Student Name", "Date of Birth", "Student Email", "Institution currently enrolled", "Banner ID", 
				"Major", "Status", "Credits Completed", "Cum. GPA", "Institution where you are transferring to", 
				"Transfer Date", "Intended Major", "Who referred you to NM AMP", 
				"Recount a significant experience that influenced you to decide you wanted a college degree", 
				"What semester/year do you plan to transfer to a four-year university, and to which institution do think you will transfer", 
				"What do you plan to do when you finish your B.S. degree", 
				"How do you think this Professional Development Workshop will help you reach your goals, and how else could New Mexico AMP assist you"}; 
		Row header = sheet.createRow(rowCount); 
		for(colCount = 0; colCount < applicantHeader.length; colCount ++){
			header.createCell(colCount).setCellValue(applicantHeader[colCount]);
		}
		
		rowCount ++;
		for(ApplicationBean bean:applicationList){
			colCount = 0;
			Row row = sheet.createRow(rowCount ++);
			row.createCell(colCount++).setCellValue(rowCount - 1);
			row.createCell(colCount++).setCellValue(bean.getFirstName() + " " + bean.getLastName());
			row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getBirthDate()));
			row.createCell(colCount++).setCellValue(bean.getEmail());
			
			if(bean.getAcademicBean() != null){
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_SCHOOL.get(bean.getAcademicBean().getAcademicSchool()));
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicBannerID());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicMajor());
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_STATUS.get(bean.getAcademicBean().getAcademicStatus()));
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicCredit());
				 
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicGPA());
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_SCHOOL.get(bean.getAcademicBean().getAcademicTransferSchool()));
				row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getAcademicBean().getAcademicTransferDate()));
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicIntendedMajor());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicReferrer());
			}else{
				for(int i = 0; i < 10; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
			if(bean.getEssayBean() != null){
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayCriticalEvent());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayEducationalGoal());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayProfesionalGoal());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayAmpGain());
			}else{
				for(int i = 0; i < 4; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
		}
		
	}
	
	private void buildExcelDocumentAplicationMESA(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response, 
			List<ApplicationBean> applicationList) throws Exception {
		String filename = LocalDate.now().toString(); 
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx"); 
		
		String sheetname = "result";
		Sheet sheet = workbook.createSheet(sheetname); 
		 
		int rowCount = 0, colCount = 0;
		String[] applicantHeader = {"", "Student Name", "Date of Birth", "Student Email", "High School you currently attend", "Academic Year", 
				"Graduation Date", "Cum. GPA", 
				"Institution you will be attending", 
				"Starting Date", "Intended Major", "How did you hear about scholarship", 
				"What attracted you to your intended field of study and what are your professional goals", 
				"What is the academic pathway that you envision for yourself, and how will this particular pathway prepare you to accomplish your professional goals", 
				"What events and individuals have been critical in influencing your academic and career decisions, and how specifically have these events individuals shaped your decisions"
				}; 
		Row header = sheet.createRow(rowCount); 
		for(colCount = 0; colCount < applicantHeader.length; colCount ++){
			header.createCell(colCount).setCellValue(applicantHeader[colCount]);
		}
		
		rowCount ++;
		for(ApplicationBean bean:applicationList){
			colCount = 0;
			Row row = sheet.createRow(rowCount ++);
			row.createCell(colCount++).setCellValue(rowCount - 1);
			row.createCell(colCount++).setCellValue(bean.getFirstName() + " " + bean.getLastName());
			row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getBirthDate()));
			row.createCell(colCount++).setCellValue(bean.getEmail());
			
			if(bean.getAcademicBean() != null){
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicSchool());
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_YEAR.get(bean.getAcademicBean().getAcademicYear()));
				row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getAcademicBean().getAcademicGradDate()));
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicGPA());
				 
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_SCHOOL.get(bean.getAcademicBean().getAcademicTransferSchool()));
				
				row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getAcademicBean().getAcademicTransferDate()));
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicIntendedMajor());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicReferrer());
			}else{
				for(int i = 0; i < 8; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
			if(bean.getEssayBean() != null){
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayProfesionalGoal());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayAcademicPathway());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayCriticalEvent());
			}else{
				for(int i = 0; i < 3; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
		}
	}
	
	private void buildExcelDocumentAplicationTRANS(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response, 
			List<ApplicationBean> applicationList) throws Exception {
		String filename = LocalDate.now().toString();  
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx"); 
		
		String sheetname = "result";
		Sheet sheet = workbook.createSheet(sheetname); 
		 
		int rowCount = 0, colCount = 0;
		String[] applicantHeader = {"", "Student Name", "Date of Birth", "Student Email", 
				"Institution currently enrolled", "Academic Year", "Graduation Date", "Banner ID", 
				"Major", "Minior", "Cum. GPA", "Institution where you are transferring to", 
				"Transfer Date", "Intended Major", "Who referred you to NM AMP",  
				"What are your profesional goals and what have you done toward reaching those goals",
				"Did you discover your field of interest on your own, or did you receive counseling from a teacher or other individual",
				"As a community college student, what has been the most important factor in your decision to transfer to a university to pursue a BS degree", 
				"Do you have a mentor at your community college? Who is that person and why do you consider them your mentor",
				"Aside from financial support, what are the most important ways in which New Mexico AMP and others can support your transfer intentions"
		, "Recommender Name", "Recommender Email"
		}; 
		Row header = sheet.createRow(rowCount); 
		for(colCount = 0; colCount < applicantHeader.length; colCount ++){
			header.createCell(colCount).setCellValue(applicantHeader[colCount]);
		}
		
		rowCount ++;
		for(ApplicationBean bean:applicationList){
			colCount = 0;
			Row row = sheet.createRow(rowCount ++);
			row.createCell(colCount++).setCellValue(rowCount - 1);
			row.createCell(colCount++).setCellValue(bean.getFirstName() + " " + bean.getLastName());
			row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getBirthDate()));
			row.createCell(colCount++).setCellValue(bean.getEmail());
			
			if(bean.getAcademicBean() != null){
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_SCHOOL.get(bean.getAcademicBean().getAcademicSchool()));
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_YEAR.get(bean.getAcademicBean().getAcademicYear()));
				row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getAcademicBean().getAcademicGradDate()));
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicBannerID());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicMajor());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicMinor());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicGPA());
				
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_SCHOOL.get(bean.getAcademicBean().getAcademicTransferSchool()));
				row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getAcademicBean().getAcademicTransferDate()));
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicIntendedMajor());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicReferrer());
			}else{
				for(int i = 0; i < 11; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
			if(bean.getEssayBean() != null){
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayProfesionalGoal());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayFieldOfInterest());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayCriticalEvent());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayMentor());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayAmpGain());
			}else{
				for(int i = 0; i < 5; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
			if(bean.getRecommenderBean() != null){
				row.createCell(colCount++).setCellValue(bean.getRecommenderBean().getFirstName()+" "+bean.getRecommenderBean().getLastName());
				row.createCell(colCount++).setCellValue(bean.getRecommenderBean().getEmail());
			}else{
				for(int i = 0; i < 2; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
		}
	}
	
	private void buildExcelDocumentAplicationSCCORE(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response, 
			List<ApplicationBean> applicationList) throws Exception {
		String filename = LocalDate.now().toString(); 
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx"); 
		
		String sheetname = "result";
		Sheet sheet = workbook.createSheet(sheetname); 
		 
		int rowCount = 0, colCount = 0;
		String[] applicantHeader = {"", "Student Name", "Date of Birth", "Student Email", 
				"Institution currently enrolled", "Academic Year", "Graduation Date", "Banner ID", 
				"Major", "Minior", "Cum. GPA", "1st Choice", "2nd Choice", 
				"Identify your major and field of study, and explain why you chose that field and how it fits your personal and professional interests and goals", 
				"What are your educational goals (B.S., Masters, Ph.D.) and what are the reasons for these goals", 
				"Describe the type of research project you would like to work on", 
				"Describe the strengths and skills that you will bring to your research project",
				"Describe what you hope to gain from the SCCORE experience"
		}; 
		Row header = sheet.createRow(rowCount); 
		for(colCount = 0; colCount < applicantHeader.length; colCount ++){
			header.createCell(colCount).setCellValue(applicantHeader[colCount]);
		}
		
		rowCount ++;
		for(ApplicationBean bean:applicationList){
			colCount = 0;
			Row row = sheet.createRow(rowCount ++);
			row.createCell(colCount++).setCellValue(rowCount - 1);
			row.createCell(colCount++).setCellValue(bean.getFirstName() + " " + bean.getLastName());
			row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getBirthDate()));
			row.createCell(colCount++).setCellValue(bean.getEmail());
			
			if(bean.getAcademicBean() != null){
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_SCHOOL_TWO.get(bean.getAcademicBean().getAcademicSchool()));
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_YEAR.get(bean.getAcademicBean().getAcademicYear()));
				row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getAcademicBean().getAcademicGradDate()));
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicBannerID());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicMajor());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicMinor());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicGPA());
				
				if(bean.getEssayBean() == null){
					row.createCell(colCount++).setCellValue("");
					row.createCell(colCount++).setCellValue("");
				}else{
					row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_SCHOOL.get(bean.getEssayBean().getSccoreSchoolAttendPref()));
					row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_SCHOOL.get(bean.getEssayBean().getSccoreSchoolAttendAltn()));
				}
				
			}else{
				for(int i = 0; i < 9; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
			if(bean.getEssayBean() != null){
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayFieldOfStudy());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayEducationalGoal());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayPreferredResearch());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayStrengthBring());
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayAmpGain());
			}else{
				for(int i = 0; i < 5; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
		}
	}
	
	private void buildExcelDocumentAplicationURS(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response, 
			List<ApplicationBean> applicationList) throws Exception {
		String filename = LocalDate.now().toString(); 
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx"); 
		
		String sheetname = "result";
		Sheet sheet = workbook.createSheet(sheetname); 
		 
		int rowCount = 0, colCount = 0;
		String[] applicantHeader = {"", "Student Name", "Date of Birth", "Student Email", "Academic School", "Academic Year", "Graduation Date", "Banner ID", 
				"Major", "Minor", "Cum. GPA", "Faculty Mentor", "Mentor Email", 
				"Project Title", "Is External Project", "External Agency", "External Title", "External Duration", 
				"Project Goal", "Project Method", "Project Result", "Project Task", "Essay"}; 
		Row header = sheet.createRow(rowCount); 
		for(colCount = 0; colCount < applicantHeader.length; colCount ++){
			header.createCell(colCount).setCellValue(applicantHeader[colCount]);
		}
		
		rowCount ++;
		for(ApplicationBean bean:applicationList){
			colCount = 0;
			Row row = sheet.createRow(rowCount ++);
			row.createCell(colCount++).setCellValue(rowCount - 1);
			row.createCell(colCount++).setCellValue(bean.getFirstName() + " " + bean.getLastName());
			row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getBirthDate()));
			row.createCell(colCount++).setCellValue(bean.getEmail());
			if(bean.getSchoolTarget() == null){
				row.createCell(colCount++).setCellValue("");
			}else{
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_SCHOOL.get(bean.getSchoolTarget()));
			}
			
			if(bean.getAcademicBean() != null){
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicYear());
				row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getAcademicBean().getAcademicGradDate()));
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicBannerID());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicMajor());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicMinor());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicGPA());
			}else{
				for(int i = 0; i < 6; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
			if(bean.getMentorBean() != null){
				row.createCell(colCount++).setCellValue(bean.getMentorBean().getMentorFirstName() + " " + bean.getMentorBean().getMentorLastName());
				row.createCell(colCount++).setCellValue(bean.getMentorBean().getMentorEmail());
			}else{
				for(int i = 0; i < 2; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
			if(bean.getProjectBean() != null){
				row.createCell(colCount++).setCellValue(bean.getProjectBean().getProjectTitle());
				row.createCell(colCount++).setCellValue(bean.getProjectBean().getExternalProject());
				row.createCell(colCount++).setCellValue(bean.getProjectBean().getExternalAgency());
				row.createCell(colCount++).setCellValue(bean.getProjectBean().getExternalTitle());
				row.createCell(colCount++).setCellValue(bean.getProjectBean().getExternalDuration());
				row.createCell(colCount++).setCellValue(bean.getProjectBean().getProjectGoal());
				row.createCell(colCount++).setCellValue(bean.getProjectBean().getProjectMethod());
				row.createCell(colCount++).setCellValue(bean.getProjectBean().getProjectResult());
				row.createCell(colCount++).setCellValue(bean.getProjectBean().getProjectTask());
			}else{
				for(int i = 0; i < 9; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
			if(bean.getEssayBean() != null){
				row.createCell(colCount++).setCellValue(bean.getEssayBean().getEssayEducationalGoal());
			}else{
				row.createCell(colCount++).setCellValue("");
			}
		}
	}

	private void buildExcelDocumentAplicationIREP(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response, 
			List<ApplicationBean> applicationList) throws Exception {
		String filename = LocalDate.now().toString(); 
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx"); 
		
		String sheetname = "result";
		Sheet sheet = workbook.createSheet(sheetname); 
		 
		int rowCount = 0, colCount = 0;
		String[] applicantHeader = {"", "Student Name", "Date of Birth", "Student Email", "Academic School", "Academic Year", "Graduation Date", "Banner ID", 
				"Major", "Minor", "Cum. GPA", 
				"Current Mentor Name", "Current Mentor Institution", "Current Mentor Phone", "Current Mentor Email", 
				"International Mentor Name", "International Mentor Institution", "International Mentor Country", "International Mentor Phone", "International Mentor Email",
				"Dates of Scheduled Research", "Expected Date Leaving", "Expected Date of Return", 
				"Have you been funded by New Mexico AMP Scholarships in past", "List the program", "Project Abstract",  
				"Total Domestic Travel","Total Round Trip Airfare","Total Visa","Total Passport and Photos","Total Immunizations","Total Housing","Total Communication","Total Meals","Total Miscellaneous", 
				"Current Domestic Travel","Current Round Trip Airfare","Current Visa","Current Passport and Photos","Current Immunizations","Current Housing","Current Communication","Current Meals","Current Miscellaneous", 
				"Describe Miscellaneous Expenses","List Your Current Funding Sources"
		}; 
		Row header = sheet.createRow(rowCount); 
		for(colCount = 0; colCount < applicantHeader.length; colCount ++){
			header.createCell(colCount).setCellValue(applicantHeader[colCount]);
		}
		
		rowCount ++;
		for(ApplicationBean bean:applicationList){
			colCount = 0;
			Row row = sheet.createRow(rowCount ++);
			row.createCell(colCount++).setCellValue(rowCount - 1);
			row.createCell(colCount++).setCellValue(bean.getFirstName() + " " + bean.getLastName());
			row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getBirthDate()));
			row.createCell(colCount++).setCellValue(bean.getEmail());
			if(bean.getSchoolTarget() == null){
				row.createCell(colCount++).setCellValue("");
			}else{
				row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_SCHOOL.get(bean.getSchoolTarget()));
			}
			
			if(bean.getAcademicBean() != null){
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicYear());
				row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getAcademicBean().getAcademicGradDate()));
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicBannerID());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicMajor());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicMinor());
				row.createCell(colCount++).setCellValue(bean.getAcademicBean().getAcademicGPA());
			}else{
				for(int i = 0; i < 6; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
			if(bean.getMentorBean() != null){
				row.createCell(colCount++).setCellValue(bean.getMentorBean().getMentorFirstName() + " " + bean.getMentorBean().getMentorLastName());
				row.createCell(colCount++).setCellValue(bean.getMentorBean().getMentorInstitution());
				row.createCell(colCount++).setCellValue(bean.getMentorBean().getMentorPhone());
				row.createCell(colCount++).setCellValue(bean.getMentorBean().getMentorEmail());
				
				row.createCell(colCount++).setCellValue(bean.getMentorBean().getIntlMentorFirstName() + " " + bean.getMentorBean().getIntlMentorLastName());
				row.createCell(colCount++).setCellValue(bean.getMentorBean().getIntlMentorInstitution());
				row.createCell(colCount++).setCellValue(bean.getMentorBean().getIntlMentorCountry());
				row.createCell(colCount++).setCellValue(bean.getMentorBean().getIntlMentorPhone());
				row.createCell(colCount++).setCellValue(bean.getMentorBean().getIntlMentorEmail());
			}else{
				for(int i = 0; i < 9; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
			if(bean.getIrepProjectBean() != null){
				row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getIrepProjectBean().getResearchDate()));
				row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getIrepProjectBean().getLeaveDate()));
				row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getIrepProjectBean().getReturnDate()));
				row.createCell(colCount++).setCellValue(bean.getIrepProjectBean().getEverFundAmp());
				row.createCell(colCount++).setCellValue(bean.getIrepProjectBean().getListProgram());
				row.createCell(colCount++).setCellValue(bean.getIrepProjectBean().getProjectAbstract());
			}else{
				for(int i = 0; i < 6; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			}
			
			if(bean.getBudgetBean() != null){
				row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getTotalDomesticTravel()));
				row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getTotalRoundTrip()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getTotalVisa()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getTotalPassport()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getTotalImmunization()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getTotalHousing()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getTotalCommunication()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getTotalMeal()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getTotalMiscellaneous()));
                
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getCurrentDomesticTravel()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getCurrentRoundTrip()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getCurrentVisa()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getCurrentPassport()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getCurrentImmunization()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getCurrentHousing()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getCurrentCommunication()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getCurrentMeal()));
                row.createCell(colCount++).setCellValue(String.valueOf(bean.getBudgetBean().getCurrentMiscellaneous()));
				
				row.createCell(colCount++).setCellValue(bean.getBudgetBean().getMiscellaneousDescribe());
				row.createCell(colCount++).setCellValue(bean.getBudgetBean().getFundingSource());
			}else{
				for(int i = 0; i < 20; i ++){
					row.createCell(colCount++).setCellValue("");
				}
			} 
		} 
	}
	
	private void buildExcelDocumentEvalEnd(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response, 
			List<List<String>> evaList) throws Exception {
		String filename = LocalDate.now().toString();
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + "-endofsemester.xlsx"); // inline
		 
		String sheetname = "result";
		Sheet sheet = workbook.createSheet(sheetname); 
		 
		Row label0 = sheet.createRow(0);
		
		createLabelCell(workbook, sheet, label0, "Rating Scale: 4= Strongly Agree 3= Mostly Agree 2= Agree 1= Mostly Disagree 0= Strongly Disagree", 0, 0, 0, 24);
		
		int rowCount = 1, colCount = 0;
		Row label = sheet.createRow(rowCount); 
		createLabelCell(workbook, sheet, label, "Lab/Field skills", rowCount, rowCount, 9, 11);
		createLabelCell(workbook, sheet, label, "Organization/Time Management skills", rowCount, rowCount, 12, 15);
		createLabelCell(workbook, sheet, label, "Independent thinking skills to solve research problems", rowCount, rowCount, 16, 17);
		createLabelCell(workbook, sheet, label, "Writing/presentation skills", rowCount, rowCount, 18, 19);
		
		CellStyle style = workbook.createCellStyle(); 
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setBorderBottom(BorderStyle.THIN); 
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN); 
		style.setBorderTop(BorderStyle.THIN);
		Cell cell = label.createCell(14); cell.setCellValue("Choose a response"); cell.setCellStyle(style);
		
		createLabelCell(workbook, sheet, label, "Part 2", rowCount, rowCount, 20, 23);
		cell = label.createCell(24); cell.setCellValue("Submit Date"); cell.setCellStyle(style);
		
		rowCount ++; 
		
		Row header = sheet.createRow(rowCount);
		for(colCount = 0; colCount < evalEndHeader.length; colCount ++){
			header.createCell(colCount).setCellValue(evalEndHeader[colCount]);
		}
        
		rowCount ++;
		for(List<String> list: evaList){
			colCount = 0;
			Row row = sheet.createRow(rowCount ++);
			for(int i = 0; i < list.size(); i ++){
				row.createCell(colCount++).setCellValue(list.get(i));
			}
		}
	}
	
	private void buildExcelDocumentEvalMid(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response, 
			List<List<String>> evaList) throws Exception {
		String filename = LocalDate.now().toString(); 
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + "-midterm.xlsx"); // inline
		 
		String sheetname = "result";
		Sheet sheet = workbook.createSheet(sheetname); 
		 
		Row label = sheet.createRow(0);
		int rowCount = 0, colCount = 0;
		createLabelCell(workbook, sheet, label, "Rating Scale (0 = Strongly Disagree)", rowCount, rowCount, 9, 13);
		
		CellStyle style = workbook.createCellStyle(); 
		style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setBorderBottom(BorderStyle.THIN); 
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN); 
		style.setBorderTop(BorderStyle.THIN);
		Cell cell = label.createCell(14); cell.setCellValue("Choose a response"); cell.setCellStyle(style);
		
		createLabelCell(workbook, sheet, label, "Questions", rowCount, rowCount, 15, 18);
		cell = label.createCell(19); cell.setCellValue("Submit Date"); cell.setCellStyle(style);
		
		rowCount ++; 
		
		Row header = sheet.createRow(rowCount);
		for(colCount = 0; colCount < evalMidHeader.length; colCount ++){
			header.createCell(colCount).setCellValue(evalMidHeader[colCount]);
		}
        
		rowCount ++;
		for(List<String> list: evaList){
			colCount = 0;
			Row row = sheet.createRow(rowCount ++);
			for(int i = 0; i < list.size(); i ++){
				row.createCell(colCount++).setCellValue(list.get(i));
			}
		}
	}
	
	private void buildExcelDocumentEvalMidStu(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response, 
			List<List<String>> evaList) throws Exception {
		String filename = LocalDate.now().toString(); 
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + "-midterm.xlsx"); // inline
		 
		String sheetname = "result";
		Sheet sheet = workbook.createSheet(sheetname); 
		int rowCount = 0, colCount = 0;
		Row header = sheet.createRow(rowCount);
		for(colCount = 0; colCount < evalMidHeaderStu.length; colCount ++){
			header.createCell(colCount).setCellValue(evalMidHeaderStu[colCount]);
		}
        
		rowCount ++;
		for(List<String> list: evaList){
			colCount = 0;
			Row row = sheet.createRow(rowCount ++);
			for(int i = 0; i < list.size(); i ++){
				row.createCell(colCount++).setCellValue(list.get(i));
			}
		}
	}
	
	private void buildExcelDocumentSelfReport(Map<String, Object> model, Workbook workbook,
			HttpServletRequest request, HttpServletResponse response, 
			List<SelfReportBean> selfreportList) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper(); 
		String filename = LocalDate.now().toString(); 
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx");
		String sheetname = "result"; 
		Sheet sheet = workbook.createSheet(sheetname); 
		 
		Row label = sheet.createRow(0);
		int rowCount = 0, colCount = 0;
		createLabelCell(workbook, sheet, label, "Academic Profile", rowCount, rowCount, 3, 13);
		createLabelCell(workbook, sheet, label, "Internship/Co-ops", rowCount, rowCount, 14, 23);
		createLabelCell(workbook, sheet, label, "Travel Research", rowCount, rowCount, 24, 33);
		createLabelCell(workbook, sheet, label, "Conferences", rowCount, rowCount, 34, 43);
		createLabelCell(workbook, sheet, label, "Publications", rowCount, rowCount, 44, 53);
		createLabelCell(workbook, sheet, label, "Awards and Accomplishments", rowCount, rowCount, 54, 63);
		CellStyle style = workbook.createCellStyle(); 
		style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setBorderBottom(BorderStyle.THIN); 
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN); 
		style.setBorderTop(BorderStyle.THIN);
		Cell cell = label.createCell(64); cell.setCellValue("Other Activities"); cell.setCellStyle(style);
		cell = label.createCell(65); cell.setCellValue("Submit Date"); cell.setCellStyle(style);
		rowCount ++; 
		
		Row header = sheet.createRow(rowCount);
		for(colCount = 0; colCount < selfreportHeader.length; colCount ++){
			header.createCell(colCount).setCellValue(selfreportHeader[colCount]);
		}
		rowCount ++;
		for(SelfReportBean bean: selfreportList){
			if(bean == null || bean.getReportAcademicBean() == null){
				continue; 
			}
			colCount = 0;
			Row row = sheet.createRow(rowCount ++);
			row.createCell(colCount++).setCellValue(bean.getSemester());
	        row.createCell(colCount++).setCellValue(bean.getUserID());;
	        row.createCell(colCount++).setCellValue(bean.getEmail());
	        // Academic Profile
	        ReportAcademicBean reportAcademicBean = bean.getReportAcademicBean();
	        row.createCell(colCount++).setCellValue(reportAcademicBean.getFirstName());
	        row.createCell(colCount++).setCellValue(reportAcademicBean.getLastName());
	        row.createCell(colCount++).setCellValue(reportAcademicBean.getCurrentAddressLine1());
	        row.createCell(colCount++).setCellValue(reportAcademicBean.getCurrentAddressLine2());
	        row.createCell(colCount++).setCellValue(reportAcademicBean.getCurrentAddressCity());
	        row.createCell(colCount++).setCellValue(reportAcademicBean.getCurrentAddressState());
	        row.createCell(colCount++).setCellValue(reportAcademicBean.getCurrentAddressZip());
	        row.createCell(colCount++).setCellValue(reportAcademicBean.getCurrentAddressCountry());
	        if(reportAcademicBean.getSelectSchool() == null){
	        	 row.createCell(colCount++).setCellValue("");
	        }else{
	        	 row.createCell(colCount++).setCellValue(ProgramCode.ACADEMIC_SCHOOL.get(reportAcademicBean.getSelectSchool()));
	        }
	        row.createCell(colCount++).setCellValue(reportAcademicBean.getMajor());
	        row.createCell(colCount++).setCellValue(reportAcademicBean.getGpa());
	        
	        // Internship/Co-ops
	        String jsonIntern = bean.getReportInternJson(); 
	        int k = 0; 
			if(jsonIntern != null) {
				TypeReference<List<ReportInternshipBean>> typeIntern = new TypeReference<List<ReportInternshipBean>>() {
				};
				List<ReportInternshipBean> jobList = objectMapper.readValue(jsonIntern, typeIntern);
				if(jobList != null) {
					k = jobList.size(); 
					for(ReportInternshipBean internshipBean: jobList) {
						String val = String.format("%s, %s, %s, %s - %s, %s, %s", 
								internshipBean.getJobCompany(), 
								internshipBean.getJobCity(), internshipBean.getJobState(),
								internshipBean.getJobStartDate(), internshipBean.getJobEndDate(), 
								internshipBean.getJobType(), internshipBean.getJobDuty());
						row.createCell(colCount++).setCellValue(val);
					}
				}
			} 
			for(int i = k; i < 10; i ++){
				row.createCell(colCount++).setCellValue("");
			}
			
			//Travel Research
			k = 0; 
			String jsonTravel = bean.getReportTravelJson(); 
			if(jsonTravel != null) {
				TypeReference<List<ReportTravelBean>> typeTravel = new TypeReference<List<ReportTravelBean>>() {
				};
				List<ReportTravelBean> travelList = objectMapper.readValue(jsonTravel, typeTravel);
				if(travelList != null) {
					k = travelList.size(); 
					for(ReportTravelBean travelBean: travelList) {
						String val = String.format("%s, %s, %s - %s, %s", 
								travelBean.getTravelCity(), travelBean.getTravelState(), 
								travelBean.getTravelStartDate(), travelBean.getTravelEndDate(), 
								travelBean.getTravelPurpose());
						row.createCell(colCount++).setCellValue(val);
					}
				}
			} 
			for(int i = k; i < 10; i ++){
				row.createCell(colCount++).setCellValue("");
			}
			
			//Conferences
			k = 0; 
			String jsonConf = bean.getReportConferenceJson(); 
			if(jsonConf != null) {
				TypeReference<List<ReportConferenceBean>> typeConf = new TypeReference<List<ReportConferenceBean>>() {
				};
				List<ReportConferenceBean> confList = objectMapper.readValue(jsonConf, typeConf);
				if(confList != null) {
					k = confList.size(); 
					for(ReportConferenceBean confBean: confList) {
						String val = String.format("%s, %s, %s, %s", 
								confBean.getConferenceName(), confBean.getConferenceDate(),
								confBean.getConferencePresentationTitle(), confBean.getConferencePresentationType());
						row.createCell(colCount++).setCellValue(val);
					}
				}
			} 
			for(int i = k; i < 10; i ++){
				row.createCell(colCount++).setCellValue("");
			}
			 
			// Publications
			k = 0;
			String jsonPub = bean.getReportPublicationJson(); 
			if(jsonPub != null) {
				TypeReference<List<ReportPublicationBean>> typePub = new TypeReference<List<ReportPublicationBean>>() {
				};
				List<ReportPublicationBean> pubList = objectMapper.readValue(jsonPub, typePub);
				if(pubList != null) {
					k = pubList.size(); 
					for(ReportPublicationBean pBean: pubList) {
						row.createCell(colCount++).setCellValue(pBean.getPublication());
					}
				}
			} 
			for(int i = k; i < 10; i ++){
				row.createCell(colCount++).setCellValue("");
			}
			
			// Awards and Accomplishments
			k = 0;
			String jsonAwards = bean.getReportAwardsJson(); 
			if(jsonAwards != null) {
				TypeReference<List<ReportAwardsBean>> typeAwards = new TypeReference<List<ReportAwardsBean>>() {
				};
				List<ReportAwardsBean> awardsList = objectMapper.readValue(jsonAwards, typeAwards);
				if(awardsList != null) {
					k = awardsList.size(); 
					for(ReportAwardsBean awardsBean: awardsList) {
						row.createCell(colCount++).setCellValue(awardsBean.getAwardsName() + ", " + awardsBean.getAwardsSemester() + ", " + awardsBean.getAwardsYear());
					}
				}
			} 
			for(int i = k; i < 10; i ++){
				row.createCell(colCount++).setCellValue("");
			}
			 
    			row.createCell(colCount++).setCellValue(bean.getReportOthersBean().getOtherActivities());
	    		if(bean.getSubmitDate() == null){
	    			row.createCell(colCount++).setCellValue("");
	    		}else{
	    			row.createCell(colCount++).setCellValue(Parse.FORMAT_DATE_MDY.format(bean.getSubmitDate()));
	    		}
    		
		}
	}
	private void createLabelCell(Workbook workbook, Sheet sheet, Row label, 
			String content, int firstRow, int lastRow, int firstCol, int lastCol){
		CellStyle style = workbook.createCellStyle(); 
		style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		 
		Cell cell = label.createCell(firstCol);
	    cell.setCellValue(content);
	    cell.setCellStyle(style);
		
		CellRangeAddress cellRangeAddress = new CellRangeAddress(
				firstRow,
	            lastRow,
	            firstCol,
	            lastCol
	    ); 
		sheet.addMergedRegion(cellRangeAddress);
		RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);;
		RegionUtil.setBorderLeft(BorderStyle.THIN, cellRangeAddress, sheet);
		RegionUtil.setBorderRight(BorderStyle.THIN, cellRangeAddress, sheet);
		RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress, sheet); 
	}
 
	private static String[] selfreportHeader = {"Semester", "User ID", "Email", "First Name", "Last Name", 
			"Address Line 1", "Address Line 2", "City", "State", "Zip", "Country", "College", "Major", "GPA", 
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"Other Activities", "Submit Date"
	}; 
 
	private static String[] evalMidHeader = {
		"Semester", "Application ID", "Project Title", 
		"Mentor First Name", "Mentor Last Name", "Mentor Email", "Mentor Department",  
		"Student First Name", "Student Last Name", 
		"1.Student reports to work on time and as scheduled", "2.Student has worked steadily to this point of the project period",
		"3.Student completes his/her assignments in a timely fashion", "4.Student demonstrates motivation and willingness to take initiative and responsibility for the project",
		"5.In my opinion, the student is gaining new knowledge and research skills", "6.Student is meeting with me", 
		"7.Please provide a brief assessment of your URS student’s successes from the start of the semester to now", 
		"8.Please list any conferences attended and/or presentations the student has participated in (or plans to participate in)", 
		"9.Please comment on any disappointments/and/or concerns with the student and/or program you wish to include here",
		"10.New Mexico AMP can assist my URS student by doing the following", 
		"Submit Date"
	}; 
	
	private static String[] evalEndHeader = {
		"Semester", "Application ID", "Project Title", 
		"Mentor First Name", "Mentor Last Name", "Mentor Email", "Mentor Department",  
		"Student First Name", "Student Last Name",
		"1.Skill with using lab/field equipment increased?",
		"2.Greater ability to design and conduct tests increased?",
		"3.Ability to analyze testing results increased?",
		"1.Satisfactory achievement of goals set at the beginning of semester was made?",
		"2.Academic workload with URS responsibilities was balanced?",
		"3.Record keeping and data gathering was properly maintained?",
		"4.Satisfactory time was spent on the research project?",
		"1.Creative thinking increased?",
		"2.Student gained improved decision making skills?",
		"1.Writing skills enhanced?",
		"2.Presentation skills enhanced?", 
		"1.What techniques/tools are you using to motivate your student to consider graduate school",
		"2.Please list any co-authored publications your student is working on including the date it should be published, journal name/volume, and title",
		"3. Please list any conferences or presentations the student has participated in this semester",
		"4.Please provide a summary assessment of your URS student's progress for this semester",	
		"Submit Date"
	}; 
	
	private static String[] evalMidHeaderStu = {
			"Semester", "Application ID", "Project Title", 
			"Mentor First Name", "Mentor Last Name", "Mentor Email", "Mentor Department",  
			"Student First Name", "Student Last Name", 
			"Question 1. Please provide a summary of the progress you have done on your project till now.", 
			"Question 2. Please describe the working relationship with your mentor.",
			"Question 3. As the result of participation on the research project (to this point), I plan to ... or I have learned ...", 
			"Submit Date"
		}; 

}
