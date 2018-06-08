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
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.bitbucket.lvncnt.portal.model.selfreport.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service("docxService")
public class DocxService {
	private static final String BULLET = "• "; 
	
	public byte[] getResume(List<SelfReportBean> selfReportList){
		
		XWPFDocument docx = null;
	    ByteArrayOutputStream out = null;
	    ObjectMapper objectMapper = new ObjectMapper(); 
	    
		try {
			
			if(selfReportList == null || selfReportList.size() == 0) {
				return null; 
			}

			ReportAcademicBean latestBean = selfReportList.get(0).getReportAcademicBean();
			List<ReportInternshipBean> jobList = new ArrayList<>();
			List<ReportTravelBean> travelList = new ArrayList<>();
			List<ReportConferenceBean> conferenceList = new ArrayList<>();
			List<ReportAwardsBean> awardsList = new ArrayList<>();
			List<ReportPublicationBean> publicationList = new ArrayList<>();
			 
			for(SelfReportBean bean: selfReportList){
				String jsonIntern = bean.getReportInternJson(); 
				if(jsonIntern != null) {
					TypeReference<List<ReportInternshipBean>> typeIntern = new TypeReference<List<ReportInternshipBean>>() {
					};
					jobList.addAll(objectMapper.readValue(jsonIntern, typeIntern));
				}
				
				String jsonTravel = bean.getReportTravelJson(); 
				if(jsonTravel != null) {
					TypeReference<List<ReportTravelBean>> typeTravel = new TypeReference<List<ReportTravelBean>>() {
					};
					travelList.addAll(objectMapper.readValue(jsonTravel, typeTravel));
				} 
				
				String jsonConf = bean.getReportConferenceJson(); 
				if(jsonConf != null) {
					TypeReference<List<ReportConferenceBean>> typeConf = new TypeReference<List<ReportConferenceBean>>() {
					};
					List<ReportConferenceBean> listConf = objectMapper.readValue(jsonConf, typeConf);
					conferenceList.addAll(listConf);
				}
				
				String jsonAwards = bean.getReportAwardsJson(); 
				if(jsonAwards != null) {
					TypeReference<List<ReportAwardsBean>> typeAwards = new TypeReference<List<ReportAwardsBean>>() {
					};
					awardsList.addAll(objectMapper.readValue(jsonAwards, typeAwards));
				}
				
				String jsonPub = bean.getReportPublicationJson(); 
				if(jsonPub != null) {
					TypeReference<List<ReportPublicationBean>> typePub = new TypeReference<List<ReportPublicationBean>>() {
					};
					List<ReportPublicationBean> listPub = objectMapper.readValue(jsonPub, typePub);
					publicationList.addAll(listPub); 
				}
			}
		
			docx = new XWPFDocument();

			// Profile 
			XWPFParagraph paragraph = docx.createParagraph();

			paragraph.setAlignment(ParagraphAlignment.LEFT);
			XWPFRun run = paragraph.createRun();
			String name = latestBean.getFirstName() + " " + latestBean.getLastName(); 
			String addressL1 = latestBean.getCurrentAddressLine1();
			String addressL2 = latestBean.getCurrentAddressLine2(); 
			String city = latestBean.getCurrentAddressCity(); 
			String zip = latestBean.getCurrentAddressZip(); 
			run.setText(name); run.addBreak();
			
			StringBuilder sb = new StringBuilder(); 
			sb.append(addressL1).append('\n'); 
			if(addressL2 != null && addressL2.length() > 1){
				sb.append(addressL1).append('\n'); 
			}
			run.setText(sb.toString()); run.addBreak(); 
			run.setText(city + " " + zip); run.addBreak(); 

			// Academic
			paragraph = docx.createParagraph();
			run = paragraph.createRun();
			run.setText("Education"); run.setBold(true); run.setCapitalized(true);
			paragraph = docx.createParagraph();
			run = paragraph.createRun();
			sb = new StringBuilder(); 
			String college = ProgramCode.ACADEMIC_SCHOOL.get(latestBean.getSelectSchool());
			String major = latestBean.getMajor(), gpa = latestBean.getGpa(); 
			run.setText(college); run.addBreak();
			run.setText(BULLET + "Major: " + major); run.addBreak();
			run.setText(BULLET + "GPA: " + gpa); run.addBreak();
			
			// Experience
			paragraph = docx.createParagraph();
			run = paragraph.createRun();
			run.setText("Experience"); run.setBold(true); run.setCapitalized(true);
			for(ReportInternshipBean internshipBean: jobList){
				paragraph = docx.createParagraph();
				run = paragraph.createRun();
				run.setText(internshipBean.getJobCompany()); run.setBold(true);
				run.addBreak();
				run = paragraph.createRun(); 
				run.setText(String.format("%s, %s, %s - %s", 
						internshipBean.getJobCity(), internshipBean.getJobState(), 
						internshipBean.getJobStartDate(), internshipBean.getJobEndDate()));
				paragraph = docx.createParagraph();
	        		paragraph.setStyle("ListParagraph");
	            run = paragraph.createRun();
	            run.setText(BULLET + internshipBean.getJobDuty());
	            run.addBreak();
			}
			 
			// Travel Research 
			paragraph = docx.createParagraph();
			run = paragraph.createRun();
			run.setText("Travel Research"); run.setBold(true); run.setCapitalized(true);
			for(ReportTravelBean travelBean: travelList){
				paragraph = docx.createParagraph();
				run = paragraph.createRun();
				run.setText(String.format("%s, %s", travelBean.getTravelCity(), travelBean.getTravelState()));
				run.setBold(true);
				run = paragraph.createRun();
				run.setText(String.format(", %s - %s", 
						travelBean.getTravelStartDate(), travelBean.getTravelEndDate()));
				paragraph = docx.createParagraph();
	        		paragraph.setStyle("ListParagraph");
	            run = paragraph.createRun();
	            run.setText(BULLET + travelBean.getTravelPurpose());
	            run.addBreak();
			}
			
			// Conference
			paragraph = docx.createParagraph();
			run = paragraph.createRun();
			run.setText("Conference"); run.setBold(true); run.setCapitalized(true);
			for(ReportConferenceBean conferenceBean: conferenceList){
				paragraph = docx.createParagraph();
				run = paragraph.createRun();
				run.setText(String.format("%s", conferenceBean.getConferenceName()));
				run.setBold(true);
				run = paragraph.createRun();
				run.setText(String.format(", %s, %s", conferenceBean.getConferencePresentationType(), conferenceBean.getConferenceDate()));
				paragraph = docx.createParagraph();
	        	paragraph.setStyle("ListParagraph");
	            run = paragraph.createRun();
	            run.setText(BULLET + conferenceBean.getConferencePresentationTitle());
	            run.addBreak();
			}
			 	        
			// Awards and Accomplishments 
			paragraph = docx.createParagraph();
			run = paragraph.createRun();
			run.setText("Awards and Accomplishments"); run.setBold(true); run.setCapitalized(true);
			for(ReportAwardsBean awardsBean: awardsList){
				paragraph = docx.createParagraph();
	            run = paragraph.createRun();
	            run.setText(BULLET + awardsBean.getAwardsName() + ", " + awardsBean.getAwardsYear());
			}
			run.addBreak();

			// Publication
			paragraph = docx.createParagraph();
			run = paragraph.createRun();
			run.setText("Publication"); run.setBold(true); run.setCapitalized(true);
			for(ReportPublicationBean publicationBean: publicationList){
				paragraph = docx.createParagraph();
	            run = paragraph.createRun();
	            run.setText(BULLET + publicationBean.getPublication());
			} 

			out = new ByteArrayOutputStream();  
			docx.write(out);
			out.close();
			return out.toByteArray(); 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	      if(out != null) {
	          try {
	            out.close();
	          } catch (IOException e) {
	            e.printStackTrace();
	          }
	        }
	        if(docx != null){
	          try {
	            docx.close();
	          } catch (IOException e) {
	            e.printStackTrace();
	          }
	        }
	        
	    }
		return null; 
	}
 
}
