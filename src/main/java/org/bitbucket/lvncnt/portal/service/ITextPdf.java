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

import org.bitbucket.lvncnt.portal.model.*;
import org.bitbucket.lvncnt.utilities.Parse;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.PdfCopy.PageStamp;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
 
@Service("iTextPdf")
public class ITextPdf {
	
	private static final char BOX_UNCHECK ='\u2610';
	private static final char BOX_CHECK ='\u2611';
	private static final char CHECK_MARK ='\u2713';
	
	private static final float LINE_WIDTH = 0.25f;
	private static final float LINE_LEADING = 10f;

	private static final String SEGOE_UI_FONT = "segoe-ui-symbol.ttf";
	 
	private static Font segoeUIFont;
	private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
	private static Font regularFontBold = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD);
	private static Font regularFontItalic = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.ITALIC);	
	private static Font regularFont = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
	private static Font mediumFontBold = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
	
	private LineDash solid = new SolidLine(); 
	 
	private Rectangle rectangle = PageSize.LETTER; 
	
	public ITextPdf() {
  
	}
	 
	/* Program Pdf */
	public byte[] buildPdfDocument(ApplicationBean appBean, ProfileBean profileBean)
			throws DocumentException, IOException{
		String path = String.format("%1$sfonts%1$s%2$s", System.getProperty("file.separator"), SEGOE_UI_FONT);
		BaseFont baseSegoeUIFont = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED); 
		segoeUIFont = new Font(baseSegoeUIFont, 8); 
		
		String program = appBean.getProgramNameAbbr(); 
		String semester = appBean.getSchoolSemester(); 
		int year = appBean.getSchoolYear(); 
		
		Document document = new Document(rectangle); 
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, byteStream);
		document.open();
		document.add(createHeader(program, semester, year)); 
		
		document.add(createCategory("Part I: Application Form")); 
		// common profile: biography
		document.add(createProfileBiography(profileBean.getBiographyBean())); 
		// common profile: contact info = address+phone+email
		document.add(createProfileContact(profileBean.getContactBean())); 
		
		// program-specific: academic info
		AcademicBean academicBean = appBean.getAcademicBean(); 
		switch (program) {
		case ProgramCode.CCCONF:
			document.add(createTableAcademicCCCONF(academicBean));
			break;
		case ProgramCode.MESA:
			document.add(createTableAcademicMESA(academicBean)); 
			break;
		case ProgramCode.SCCORE:
			document.add(createTableAcademicSCCORE(academicBean));
			break;
		case ProgramCode.TRANS:
			document.add(createTableAcademicTRANS(academicBean));
			break;
		case ProgramCode.URS:
		case ProgramCode.IREP:
			document.add(createTableAcademicURS(academicBean));
			break;
		}
        
        document.add(createProfileResidentStatus(profileBean.getBiographyBean().getCitizenship(),
        		profileBean.getBiographyBean().getIsNMResident()));
        document.add(createProfileParentDegree(profileBean.getBiographyBean().getParentHasDegree()));
	    document.add(new Paragraph("Ethnicity & Race", regularFontBold));
		document.add(createProfileEthnicity(profileBean.getEthnicityBean().getIsHispanic(), profileBean.getEthnicityBean().getRace()));
	    document.add(new Paragraph("Disability Status: ", regularFontBold));
        document.add(createProfileDisability(profileBean.getEthnicityBean().getDisability()));
        document.add(createProfileEmergencyContact(profileBean.getContactBean()));
     		
        document.newPage(); 
        
        InvolvementBean involvementBean = null;
        EssayBean essayBean = null; 
        switch (program) {
        case ProgramCode.CCCONF:
        	// program-specific: involvement info
        	involvementBean = appBean.getInvolvementBean(); 
        	document.add(createTableInvolvementCCCONF(involvementBean));
        	// program-specific: short essay
	        document.add(createCategory("Part II: Short Essay"));
	        String essayCriticalEvent = "", essayEducationalGoal = "" ; 
	        String essayProfesionalGoal = "", essayAmpGain = ""; 
	        essayBean = appBean.getEssayBean(); 
	        if(essayBean != null){
	        	essayCriticalEvent = essayBean.getEssayCriticalEvent(); 
	        	essayEducationalGoal = essayBean.getEssayEducationalGoal() ; 
		        essayProfesionalGoal = essayBean.getEssayProfesionalGoal(); 
		        essayAmpGain = essayBean.getEssayAmpGain(); 
	        }
	        document.add(createTableEssay(ESSAY_CCCONF_HEADER, 
	        		new String[]{essayCriticalEvent, essayEducationalGoal,
	        			essayProfesionalGoal,essayAmpGain}));
	        document.newPage();
	        document.add(addLineBreak());
	        document.add(createCategory("For Official Use Only"));
	        // For Official Use Only
	        String dateReceived; 
	        if(appBean.getCompleteDate() != null){
				dateReceived = Parse.FORMAT_DATE_MDY.format(appBean.getCompleteDate());
			}else{
				dateReceived = ""; 
			}
	        document.add(createTableOfficialUseCCCONF(dateReceived)); 
        	break ;
        	
        case ProgramCode.MESA:
        	involvementBean = appBean.getInvolvementBean(); 
        	document.add(createTableInvolvementMESA(involvementBean));
        	document.add(createCategory("Part II: Essay Questions"));
        	essayProfesionalGoal = ""; 
	        essayCriticalEvent =  ""; 
	        String essayAcademicPathway = ""; 
	        essayBean = appBean.getEssayBean(); 
	        if(essayBean != null){
	        	essayProfesionalGoal = essayBean.getEssayProfesionalGoal(); 
	        	 essayAcademicPathway = essayBean.getEssayAcademicPathway();
		        essayCriticalEvent = essayBean.getEssayCriticalEvent(); 
	        }
			document.add(createTableEssay(ESSAY_MESA_HEADER, 
					new String[]{essayProfesionalGoal, essayAcademicPathway, essayCriticalEvent}));
        	
			document.newPage();
	        document.add(addLineBreak());
			document.add(createCategory("For Official Use Only"));
			if(appBean.getCompleteDate() != null){
				dateReceived = Parse.FORMAT_DATE_MDY.format(appBean.getCompleteDate());
			}else{
				dateReceived = ""; 
			}
			document.add(createTableOfficialUseMESA(dateReceived));
        	break; 
        	
        case ProgramCode.SCCORE:
        	involvementBean = appBean.getInvolvementBean(); 
        	document.add(createTableInvolvementSCCORE(involvementBean));
        	
			document.add(createCategory("Part II: Application Questionnaire"));
			essayBean = appBean.getEssayBean(); 
			String essayFieldOfStudy = ""; 
	        essayEducationalGoal = ""; 
	        String essayPreferredResearch = "", essayStrengthBring = ""; 
	        essayAmpGain = ""; 
	        String firstChoice = "", secondChoice = ""; 
	        if(essayBean != null){
	        	essayFieldOfStudy = essayBean.getEssayFieldOfStudy(); 
	        	essayEducationalGoal = essayBean.getEssayEducationalGoal(); 
	        	essayPreferredResearch = essayBean.getEssayPreferredResearch();
	        	essayStrengthBring = essayBean.getEssayStrengthBring(); 
	        	essayAmpGain = essayBean.getEssayAmpGain(); 
	        	firstChoice = essayBean.getSccoreSchoolAttendPref(); 
	        	secondChoice = essayBean.getSccoreSchoolAttendAltn(); 
	        }
			document.add(createTableEssaySchoolSCCORE(ProgramCode.ACADEMIC_SCHOOL.get(firstChoice),
					ProgramCode.ACADEMIC_SCHOOL.get(secondChoice)));
			document.add(createTableEssay(ESSAY_SCCORE_HEADER, 
					new String[]{essayFieldOfStudy,essayEducationalGoal,essayPreferredResearch,essayStrengthBring,essayAmpGain}));
			
			document.newPage();
	        document.add(addLineBreak());
			document.add(createCategory("For Official Use Only"));
			if(appBean.getCompleteDate() != null){
				dateReceived = Parse.FORMAT_DATE_MDY.format(appBean.getCompleteDate());
			}else{
				dateReceived = ""; 
			}
			document.add(createTableOfficialUseMESA(dateReceived));
        	break; 
        	
        case ProgramCode.TRANS:
        	involvementBean = appBean.getInvolvementBean(); 
        	document.add(createTableInvolvementTRANS(involvementBean));
        	
			document.add(createCategory("Part II: Essay Questions"));
			essayProfesionalGoal = ""; 
	        String essayFieldOfInterest = "", essayMentor = "";
	        essayCriticalEvent = "";  essayAmpGain = ""; 
	        essayBean = appBean.getEssayBean(); 
	        if(essayBean != null){
	        	essayProfesionalGoal = essayBean.getEssayProfesionalGoal(); 
	        	essayFieldOfInterest = essayBean.getEssayFieldOfInterest(); 
	        	essayCriticalEvent = essayBean.getEssayCriticalEvent(); 
	        	essayMentor = essayBean.getEssayMentor();
	        	essayAmpGain = essayBean.getEssayAmpGain(); 
	        }
			document.add(createTableEssay(ESSAY_TRANS_HEADER, 
					new String[]{essayProfesionalGoal,essayFieldOfInterest,essayCriticalEvent,essayMentor,essayAmpGain}));
			
			document.newPage();
			
			document.add(createCategory("Part III: Recommender Information"));
			document.add(createTableRecommender(appBean.getRecommenderBean()));
			
	        document.add(addLineBreak());
			document.add(createCategory("For Official Use Only"));
			dateReceived = appBean.getCompleteDate() == null ? 
					"" : Parse.FORMAT_DATE_MDY.format(appBean.getCompleteDate());
			document.add(createTableOfficialUseMESA(dateReceived));
        	break; 
        	
        case ProgramCode.URS:
        	involvementBean = appBean.getInvolvementBean(); 
        	document.add(createTableInvolvementURS(involvementBean));
        	
        	document.add(createCategory("Part II: Essay Questions"));
        	essayEducationalGoal = "" ; 
	        essayBean = appBean.getEssayBean(); 
	        if(essayBean != null){
	        	essayEducationalGoal = essayBean.getEssayEducationalGoal() ; 
	        }
	        document.add(createTableEssay(ESSAY_URS_HEADER, 
	        		new String[]{essayEducationalGoal}));
	        
        	document.add(createCategory("Part III: Faculty Mentor Information"));
        	document.add(createTableMentorURS(profileBean.getMentorBean()));
        	
			document.add(createCategory("Part IV: Project Proposal"));
			String projectGoal = "", projectMethod = ""; 
	        String projectResult = "", projectTask = ""; 
	        ProjectBean projectBean = appBean.getProjectBean(); 
	        document.add(createTableProjectProposalTitleURS(projectBean)); 
	        if(projectBean != null){
	        	projectGoal = projectBean.getProjectGoal(); 
	        	projectMethod = projectBean.getProjectMethod(); 
	        	projectResult = projectBean.getProjectResult(); 
	        	projectTask = projectBean.getProjectTask(); 
	        }
			document.add(createTableEssay(PROJECT_URS_HEADER, 
					new String[]{projectGoal,projectMethod,projectResult,projectTask}));
			document.add(addLineBreak());
        	break; 
        	
        case ProgramCode.IREP:
        	document.add(createCategory("Part II: Mentor Information"));
        	document.add(createTableMentorIREP(appBean.getMentorBean()));
        	
			document.add(createCategory("Part III: Project Abstract"));
			String projectAbstract = "", miscDescribe = "", fundingSource = ""; 
	        IREPProjectBean irepProjectBean = appBean.getIrepProjectBean();
	        document.add(createTableProjectProposalTitleIREP(irepProjectBean)); 
	        if(irepProjectBean != null){
	        	projectAbstract = irepProjectBean.getProjectAbstract(); 
	        }
			document.add(createTableEssay(new String[]{"Project Abstract"}, 
					new String[]{projectAbstract})); 
			
			document.newPage();
			document.add(createCategory("Part IV: Budget Worksheet"));
			document.add(createTableBudgetIREP(appBean.getBudgetBean()));
			if(appBean.getBudgetBean() != null){
				miscDescribe = appBean.getBudgetBean().getMiscellaneousDescribe();
	        	fundingSource = appBean.getBudgetBean().getFundingSource();
			}
			document.add(createTableEssay(new String[]{"Describe Miscellaneous Expenses", "List Your Current Funding Sources"}, 
					new String[]{miscDescribe, fundingSource}));
			document.add(addLineBreak());
        	break; 
        }
		document.close();
	    return byteStream.toByteArray();
	}
	
	/* URS: create application pdf for committee review */
	public byte[] buildPdfDocumentCommittyReview(ApplicationBean appBean, ProfileBean profileBean)
			throws DocumentException, IOException{
		String path = String.format("%1$sfonts%1$s%2$s", System.getProperty("file.separator"), SEGOE_UI_FONT); 
		BaseFont baseSegoeUIFont = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED); 
		segoeUIFont = new Font(baseSegoeUIFont, 8); 
		
		String program = appBean.getProgramNameAbbr(); 
		String semester = appBean.getSchoolSemester(); 
		int year = appBean.getSchoolYear(); 
		
		Document document = new Document(rectangle); 
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, byteStream);
		document.open();
		document.add(createHeader(program, semester, year)); 
		
		document.add(createCategory("Part I: Student Information")); 
		PdfPTable table;
        String lastName = profileBean.getBiographyBean().getLastName();
		String firstName = profileBean.getBiographyBean().getFirstName();
		 
        int colspan = 1; 
        
        table = new PdfPTable(3);
        table.setWidthPercentage(100);
        String[] header1 = {"Last Name", "First Name", ""};
        String[] content1 = {lastName, firstName, ""}; 
        for(String content: content1){
            table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header1){
            table.addCell(createCellBorderNone(colspan, content));
        }
        
       String[] header2 = {"Major", "Cum. GPA", "Expected Grad Date (M/Y)"}; 
        String[] content2 = {
        		appBean.getAcademicBean() == null ? " ":appBean.getAcademicBean().getAcademicMajor(), 
        		appBean.getAcademicBean() == null ? " " : String.valueOf(appBean.getAcademicBean().getAcademicGPA()), 
        		appBean.getAcademicBean() == null || appBean.getAcademicBean().getAcademicGradDate() == null ? " " 
        	    		   : Parse.FORMAT_DATE_MDY.format(appBean.getAcademicBean().getAcademicGradDate())};
       
        for(String content: content2){
        	table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header2){
        	table.addCell(createCellBorderNone(colspan, content));
        } 
        document.add(table); 

        EssayBean essayBean = null; 
        String[] header3 = {"Institution you will be attending", "Date Starting"}; 
    	String[] header4 = {"Intended Major", "How did you hear about scholarship?"}; 
        switch (program) {
        case ProgramCode.CCCONF:
        	table = new PdfPTable(2);
            table.setWidthPercentage(100);
        	
            String[] content3c = {
            	appBean.getAcademicBean() == null || appBean.getAcademicBean().getAcademicTransferSchool() == null? " " : 
            		ProgramCode.ACADEMIC_SCHOOL.get(appBean.getAcademicBean().getAcademicTransferSchool()),
            	appBean.getAcademicBean() == null || appBean.getAcademicBean().getAcademicTransferDate() == null ? " " :
						Parse.FORMAT_DATE_MDY.format(appBean.getAcademicBean().getAcademicTransferDate())
            }; 
            for(String content: content3c){
            	table.addCell(createCellBorderBottom(colspan, content));
            }
            for(String content: header3){
            	table.addCell(createCellBorderNone(colspan, content));
            }
            
            String[] content4c = {
            	appBean.getAcademicBean() == null ? " " : appBean.getAcademicBean().getAcademicIntendedMajor(), 
            	appBean.getAcademicBean() == null ? " " : appBean.getAcademicBean().getAcademicReferrer()
            }; 
            for(String content: content4c){
            	table.addCell(createCellBorderBottom(colspan, content));
            }
            for(String content: header4){
            	table.addCell(createCellBorderNone(colspan, content));
            }
            document.add(table); 
            
        	document.add(createCategory("Part II: Short Essay"));
	        String essayCriticalEvent = "", essayEducationalGoal = "" ; 
	        String essayProfesionalGoal = "", essayAmpGain = ""; 
	        essayBean = appBean.getEssayBean(); 
	        if(essayBean != null){
	        	essayCriticalEvent = essayBean.getEssayCriticalEvent(); 
	        	essayEducationalGoal = essayBean.getEssayEducationalGoal() ; 
		        essayProfesionalGoal = essayBean.getEssayProfesionalGoal(); 
		        essayAmpGain = essayBean.getEssayAmpGain(); 
	        }
	        document.add(createTableEssay(ESSAY_CCCONF_HEADER, 
	        		new String[]{essayCriticalEvent, essayEducationalGoal,
	        			essayProfesionalGoal,essayAmpGain}));
        	break; 
        	
        case ProgramCode.MESA:
        	table = new PdfPTable(2);
            table.setWidthPercentage(100);
            String[] content3m = {
        		appBean.getAcademicBean() == null || appBean.getAcademicBean().getAcademicTransferSchool() == null? " " : 
            		ProgramCode.ACADEMIC_SCHOOL.get(appBean.getAcademicBean().getAcademicTransferSchool()),
            	appBean.getAcademicBean() == null || appBean.getAcademicBean().getAcademicTransferDate() == null ? " " :
						Parse.FORMAT_DATE_MDY.format(appBean.getAcademicBean().getAcademicTransferDate())
            }; 
            for(String content: content3m){
            	table.addCell(createCellBorderBottom(colspan, content));
            }
            for(String content: header3){
            	table.addCell(createCellBorderNone(colspan, content));
            }
            String[] content4m = {
                	appBean.getAcademicBean() == null ? " " : appBean.getAcademicBean().getAcademicIntendedMajor(), 
                	appBean.getAcademicBean() == null ? " " : appBean.getAcademicBean().getAcademicReferrer()
            };
            for(String content: content4m){
            	table.addCell(createCellBorderBottom(colspan, content));
            }
            for(String content: header4){
            	table.addCell(createCellBorderNone(colspan, content));
            }
            document.add(table); 
            
        	document.add(createCategory("Part II: Essay Questions"));
        	essayProfesionalGoal = ""; 
	        essayCriticalEvent =  ""; 
	        String essayAcademicPathway = ""; 
	        essayBean = appBean.getEssayBean(); 
	        if(essayBean != null){
	        	essayProfesionalGoal = essayBean.getEssayProfesionalGoal(); 
	        	 essayAcademicPathway = essayBean.getEssayAcademicPathway();
		        essayCriticalEvent = essayBean.getEssayCriticalEvent(); 
	        }
			document.add(createTableEssay(ESSAY_MESA_HEADER, 
					new String[]{essayProfesionalGoal, essayAcademicPathway, essayCriticalEvent}));
			break; 
        	
        case ProgramCode.URS:
        	document.add(createCategory("Part II: Essay Questions"));
        	essayEducationalGoal = "" ; 
	        essayBean = appBean.getEssayBean(); 
	        if(essayBean != null){
	        	essayEducationalGoal = essayBean.getEssayEducationalGoal() ; 
	        }
	        document.add(createTableEssay(ESSAY_URS_HEADER, 
	        		new String[]{essayEducationalGoal}));
	        
        	
			document.add(createCategory("Part III: Project Proposal"));
			String projectGoal = "", projectMethod = ""; 
	        String projectResult = "", projectTask = ""; 
	        ProjectBean projectBean = appBean.getProjectBean(); 
	        document.add(createTableProjectProposalTitleURS(projectBean)); 
	        if(projectBean != null){
	        	projectGoal = projectBean.getProjectGoal(); 
	        	projectMethod = projectBean.getProjectMethod(); 
	        	projectResult = projectBean.getProjectResult(); 
	        	projectTask = projectBean.getProjectTask(); 
	        }
			document.add(createTableEssay(PROJECT_URS_HEADER, 
					new String[]{projectGoal,projectMethod,projectResult,projectTask}));
			document.add(addLineBreak());
        	break; 
        	
        case ProgramCode.SCCORE:
	        document.add(createCategory("Part II: Application Questionnaire"));
			essayBean = appBean.getEssayBean(); 
			String essayFieldOfStudy = ""; 
	        essayEducationalGoal = ""; 
	        String essayPreferredResearch = "", essayStrengthBring = ""; 
	        essayAmpGain = ""; 
	        String firstChoice = "", secondChoice = ""; 
	        if(essayBean != null){
	        	essayFieldOfStudy = essayBean.getEssayFieldOfStudy(); 
	        	essayEducationalGoal = essayBean.getEssayEducationalGoal(); 
	        	essayPreferredResearch = essayBean.getEssayPreferredResearch();
	        	essayStrengthBring = essayBean.getEssayStrengthBring(); 
	        	essayAmpGain = essayBean.getEssayAmpGain(); 
	        	firstChoice = essayBean.getSccoreSchoolAttendPref(); 
	        	secondChoice = essayBean.getSccoreSchoolAttendAltn(); 
	        }
			document.add(createTableEssaySchoolSCCORE(ProgramCode.ACADEMIC_SCHOOL.get(firstChoice),
					ProgramCode.ACADEMIC_SCHOOL.get(secondChoice)));
			document.add(createTableEssay(ESSAY_SCCORE_HEADER, 
					new String[]{essayFieldOfStudy,essayEducationalGoal,essayPreferredResearch,essayStrengthBring,essayAmpGain}));
        	break; 
        
        case ProgramCode.TRANS:
        	table = new PdfPTable(2);
            table.setWidthPercentage(100);
            String[] content3t = {
        		appBean.getAcademicBean() == null || appBean.getAcademicBean().getAcademicTransferSchool() == null? " " : 
            		ProgramCode.ACADEMIC_SCHOOL.get(appBean.getAcademicBean().getAcademicTransferSchool()),
            	appBean.getAcademicBean() == null || appBean.getAcademicBean().getAcademicTransferDate() == null ? " " :
						Parse.FORMAT_DATE_MDY.format(appBean.getAcademicBean().getAcademicTransferDate())
            }; 
            for(String content: content3t){
            	table.addCell(createCellBorderBottom(colspan, content));
            }
            for(String content: header3){
            	table.addCell(createCellBorderNone(colspan, content));
            }
            String[] content4t = {
            	appBean.getAcademicBean() == null ? " " : appBean.getAcademicBean().getAcademicIntendedMajor(), 
            	appBean.getAcademicBean() == null ? " " : appBean.getAcademicBean().getAcademicReferrer()
            }; 
            for(String content: content4t){
            	table.addCell(createCellBorderBottom(colspan, content));
            }
            for(String content: header4){
            	table.addCell(createCellBorderNone(colspan, content));
            }
            document.add(table); 
            
            document.add(createCategory("Part II: Essay Questions"));
			essayProfesionalGoal = ""; 
	        String essayFieldOfInterest = "", essayMentor = "";
	        essayCriticalEvent = "";  essayAmpGain = ""; 
	        essayBean = appBean.getEssayBean(); 
	        if(essayBean != null){
	        	essayProfesionalGoal = essayBean.getEssayProfesionalGoal(); 
	        	essayFieldOfInterest = essayBean.getEssayFieldOfInterest(); 
	        	essayCriticalEvent = essayBean.getEssayCriticalEvent(); 
	        	essayMentor = essayBean.getEssayMentor();
	        	essayAmpGain = essayBean.getEssayAmpGain(); 
	        }
			document.add(createTableEssay(ESSAY_TRANS_HEADER, 
					new String[]{essayProfesionalGoal,essayFieldOfInterest,essayCriticalEvent,essayMentor,essayAmpGain}));
			
			document.add(createCategory("Part III: Recommender Information"));
			document.add(createTableRecommender(appBean.getRecommenderBean()));
			break; 
			
        case ProgramCode.IREP:
        	document.add(createCategory("Part II: Mentor Information"));
        	document.add(createTableMentorIREP(appBean.getMentorBean()));
        	
			document.add(createCategory("Part III: Project Abstract"));
			String projectAbstract = "", miscDescribe = "", fundingSource = ""; 
	        IREPProjectBean irepProjectBean = appBean.getIrepProjectBean(); 
	        document.add(createTableProjectProposalTitleIREP(irepProjectBean)); 
	        if(irepProjectBean != null){
	        	projectAbstract = irepProjectBean.getProjectAbstract(); 
	        }
			document.add(createTableEssay(new String[]{"Project Abstract"}, 
					new String[]{projectAbstract})); 
			
			document.newPage();
			document.add(createCategory("Part IV: Budget Worksheet"));
			document.add(createTableBudgetIREP(appBean.getBudgetBean()));
			if(appBean.getBudgetBean() != null){
				miscDescribe = appBean.getBudgetBean().getMiscellaneousDescribe();
	        	fundingSource = appBean.getBudgetBean().getFundingSource();
			}
			document.add(createTableEssay(new String[]{"Describe Miscellaneous Expenses", "List Your Current Funding Sources"}, 
					new String[]{miscDescribe, fundingSource}));
			document.add(addLineBreak());
        	break; 
        }
		document.close();
	    return byteStream.toByteArray();
	}

	private Paragraph createHeader(String program, String semester, int year){
		Paragraph preface = new Paragraph(); 
		preface.add(new Chunk("NEW MEXICO AMP", subFont)); 
		preface.add(Chunk.NEWLINE); 
		preface.add(new Chunk(ProgramCode.PROGRAMS_SUB.get(program), subFont));
		preface.add(Chunk.NEWLINE); 
		 
		preface.add(new Chunk(semester + " " + year, subFont));
		preface.setAlignment(Element.ALIGN_CENTER);
		 
		return preface; 
	}
	
	private PdfPTable createCategory(String header){
		PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        float spacing = 5f; 
        table.setSpacingBefore(spacing);
        table.setSpacingAfter(spacing);
        PdfPCell cell = new PdfPCell(new Phrase(header, mediumFontBold));
        cell.setColspan(1);
        cell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell);
        return table;
	}
	
	private PdfPTable createProfileBiography(BiographyBean profileBean){
	 
		String lastName = profileBean.getLastName();
		String firstName = profileBean.getFirstName();
		String middleName = profileBean.getMiddleName();
		String birthDate = "";
		if(profileBean.getBirthDate() != null){
			birthDate = Parse.FORMAT_DATE_MDY.format(profileBean.getBirthDate());
		}
		String ssnLastFour = profileBean.getSsnLastFour();
		String gender = profileBean.getGender();
		PdfPTable table;
        int colspan = 1; 
        
        table = new PdfPTable(6);
        table.setWidthPercentage(100);

        table.addCell(createCellBorderBottom(colspan, lastName));
        table.addCell(createCellBorderBottom(colspan, firstName));
        table.addCell(createCellBorderBottom(colspan, middleName));
        table.addCell(createCellBorderBottom(colspan, ssnLastFour));
        
        PdfPCell cell = createCellBorderBottom(colspan, null); 
        cell.addElement(addCheckBox(gender, 1, ProgramCode.GENDER));
        table.addCell(cell);
       
        table.addCell(createCellBorderBottom(1, birthDate));
        
        String[] headers = {"Last Name", "First Name", "MI", "SSN last 4 digits", "Gender", "Date of Birth"}; 
        for(String header: headers){
        	table.addCell(createCellBorderNone(colspan, header));
        }
        return table; 
	}
	
	private PdfPTable createProfileContact(ContactBean bean){
		 
		String[] contentCurrentAddress = {bean.getCurrentAddressLine1() + " " + bean.getCurrentAddressLine2(),
				bean.getCurrentAddressCity(), bean.getCurrentAddressCounty(), 
				bean.getCurrentAddressState(), bean.getCurrentAddressZip()}; 
        String[] contentPermanentAddress = {bean.getPermanentAddressLine1() + " " + bean.getPermanentAddressLine2(), 
        		bean.getPermanentAddressCity(), bean.getPermanentAddressCounty(), 
        		bean.getPermanentAddressState(), bean.getPermanentAddressZip()}; 
        String[] phoneEmail = {bean.getPhoneNum1(),bean.getPhoneType1(),bean.getPhoneNum2(),bean.getPhoneType2(),
        		bean.getEmailPref(), bean.getEmailAltn()}; 
        
		PdfPTable table;
        table = new PdfPTable(30);
        table.setWidthPercentage(100);
        
        int colspan; 
        for(int i = 0; i < contentCurrentAddress.length; i++){
        	switch (i) {
			case 0:
				colspan = 10; break;
			case 1:
				colspan = 6; break;
			case 2:
				colspan = 6; break;
			case 3:
				colspan = 5; break;
			case 4:
				colspan = 4; break;
			default:
				colspan = 6; break; 	
			}
            table.addCell(createCellBorderBottom(colspan, contentCurrentAddress[i]));
        }
        
        String[] headerCurrent = {"Local Address", "City", "County", "State", "Zip"}; 
        for(int i = 0; i < headerCurrent.length; i++){
        	switch (i) {
			case 0:
				colspan = 10; break;
			case 1:
				colspan = 6; break;
			case 2:
				colspan = 6; break;
			case 3:
				colspan = 5; break;
			case 4:
				colspan = 4; break;
			default:
				colspan = 6; break; 	
			}
        	table.addCell(createCellBorderNone(colspan, headerCurrent[i]));
        }
          
        for(int i = 0; i < contentPermanentAddress.length; i ++){
        	switch (i) {
			case 0:
				colspan = 10; break;
			case 1:
				colspan = 6; break;
			case 2:
				colspan = 6; break;
			case 3:
				colspan = 5; break;
			case 4:
				colspan = 4; break;
			default:
				colspan = 6; break; 	
			}
            table.addCell(createCellBorderBottom(colspan, contentPermanentAddress[i]));
        }
        
        String[] headerPermanent = {"Permanent Address", "City", "County", "State", "Zip"}; 
        for(int i = 0; i < headerPermanent.length; i ++){
        	switch (i) {
			case 0:
				colspan = 10; break;
			case 1:
				colspan = 6; break;
			case 2:
				colspan = 6; break;
			case 3:
				colspan = 5; break;
			case 4:
				colspan = 4; break;
			default:
				colspan = 6; break; 	
			}
        	table.addCell(createCellBorderNone(colspan, headerPermanent[i]));
        }
         
        for(int i = 0; i < phoneEmail.length; i ++){
        	switch (i) {
			case 0:
				colspan = 5; break;
			case 1:
				colspan = 3; break;
			case 2:
				colspan = 5; break;
			case 3:
				colspan = 3; break;
			case 4:
				colspan = 7; break;
			case 5:
				colspan = 7; break;
			default:
				colspan = 6; break; 	
			}
            table.addCell(createCellBorderBottom(colspan, phoneEmail[i]));
        }
        
        String[] headerPhoneEmail = {"Cell Phone", "Phone Type", "Alternate Phone", "Phone Type", "Preferred Email", "Alternate Email"}; 
        for(int i = 0; i < headerPhoneEmail.length; i ++){
        	switch (i) {
			case 0:
				colspan = 5; break;
			case 1:
				colspan = 3; break;
			case 2:
				colspan = 5; break;
			case 3:
				colspan = 3; break;
			case 4:
				colspan = 7; break;
			case 5:
				colspan = 7; break;
			default:
				colspan = 6; break; 	
			}
        	table.addCell(createCellBorderNone(colspan, headerPhoneEmail[i]));
        }
        return table; 
	}
	
	private PdfPTable createProfileResidentStatus(String citizenship, Integer isNMResident){
		PdfPTable table;
        PdfPCell cell;
        
        table = new PdfPTable(2);
        table.setWidthPercentage(100);
        cell = createCellBorderBottom(1, null); 
        cell.addElement(addCheckBox(citizenship, 1, ProgramCode.CITIZENSHIP));
        table.addCell(cell);
         
        Phrase phrase = new Phrase(); 
        phrase.add(new Paragraph("New Mexico Resident? ", regularFont));
        phrase.add(addCheckBox(String.valueOf(isNMResident), 1, ProgramCode.YES_NO));
        cell = createCellBorderBottom(1, null); 
        cell.addElement(phrase);
        table.addCell(cell);
        return table; 
	}
	
	private PdfPTable createProfileParentDegree(Integer parentHasDegree){
		PdfPTable table;
        PdfPCell cell;
        
        table = new PdfPTable(1);
        table.setWidthPercentage(100); 
        
        Phrase phrase = new Phrase(); 
        phrase.add(new Paragraph("Do either of your parents have a college or University Bachelor's degree? ", regularFont));
        phrase.add(addCheckBox(String.valueOf(parentHasDegree), 1, ProgramCode.YES_NO));
        cell = createCellBorderBottom(1, null); 
        cell.addElement(phrase);
        table.addCell(cell);
        return table; 
	}
	
	private PdfPTable createProfileEthnicity(int isHispanic, List<String> race){
		int isAfricanAmerican = 0, isAmericanIndian = 0, isAlaskaNative = 0;
		int isAsian = 0, isNativeHawaiian = 0, isWhite = 0; 
		if(race != null){
			for(String s: race){
				switch (s) {
				case ProgramCode.RACE_AFRICANAMERICAN:
					isAfricanAmerican = 1; break;
				case ProgramCode.RACE_AMERICANINDIAN:
					isAmericanIndian = 1; break; 
				case ProgramCode.RACE_ALASKANATIVE:
					isAlaskaNative = 1; break; 
				case ProgramCode.RACE_ASIAN:
					isAsian = 1; break; 
				case ProgramCode.RACE_NATIVEHAWAIIAN:
					isNativeHawaiian = 1; break; 
				case ProgramCode.RACE_WHITE:
					isWhite = 1; break;
				}
			}
		}
		
		PdfPTable tableEthnicity = new PdfPTable(2);
        tableEthnicity.setPaddingTop(0);
        tableEthnicity.setSpacingBefore(5);
        tableEthnicity.setWidthPercentage(100);
        PdfPCell cellEthnicity = new PdfPCell();
        cellEthnicity.setBorderWidth(LINE_WIDTH);
        cellEthnicity.setColspan(2);cellEthnicity.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEthnicity.setPaddingBottom(5);
        Paragraph pEthnicity = new Paragraph();
        pEthnicity.setLeading(LINE_LEADING);
        pEthnicity.add(new Chunk("Do you consider yourself Hispanic or Latino? ", regularFont)); 
        if(isHispanic == 1){
        	pEthnicity.add(new Chunk(BOX_CHECK, segoeUIFont));
        }else{
        	pEthnicity.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        }
        pEthnicity.add(new Chunk("Yes  ", regularFont)); 
        if(isHispanic == 0){
        	pEthnicity.add(new Chunk(BOX_CHECK, segoeUIFont));
        }else{
        	pEthnicity.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        }
	    pEthnicity.add(new Chunk("No", regularFont)); 
	    pEthnicity.add(Chunk.NEWLINE); 
	    pEthnicity.add(new Chunk("Includes persons of Cuban, Mexican, Puerto Rican, South or Central American or other Spanish culture or origin.", regularFontItalic));
	    pEthnicity.add(Chunk.NEWLINE); 
	    pEthnicity.add(new Chunk("In addition, select one or more of the following racial categories as appropriate for you ", regularFont)); 
	    pEthnicity.add(new Chunk("(if applicable)", regularFontItalic)); 
	    pEthnicity.add(new Chunk(": ", regularFont)); 
	    pEthnicity.add(Chunk.NEWLINE); 
	    
	    if(isAfricanAmerican == 1){
        	pEthnicity.add(new Chunk(BOX_CHECK, segoeUIFont));
        }else{
        	pEthnicity.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        }
	    pEthnicity.add(new Chunk(" African American or Black ", regularFont)); 
	    pEthnicity.add(new Chunk("(A person having origins in any of the black racial groups of Africa)", regularFontItalic)); 
	    pEthnicity.add(Chunk.NEWLINE); 
	    
	    if(isAmericanIndian == 1){
        	pEthnicity.add(new Chunk(BOX_CHECK, segoeUIFont));
        }else{
        	pEthnicity.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        }
	    pEthnicity.add(new Chunk(" American Indian ", regularFont)); 
	    pEthnicity.add(new Chunk("(A person having origins in any of the original peoples of North & South America, including Central America, and who maintains a tribal affiliation or community attachment)", regularFontItalic)); 
	    pEthnicity.add(Chunk.NEWLINE);

	    if(isAlaskaNative == 1){
        	pEthnicity.add(new Chunk(BOX_CHECK, segoeUIFont));
        }else{
        	pEthnicity.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        }
	    pEthnicity.add(new Chunk(" Alaska Native ", regularFont)); 
	    pEthnicity.add(new Chunk("(A person having origins in any of the original peoples of North & South America, including Central America, and who maintains a tribal affiliation or community attachment)", regularFontItalic)); 
	    pEthnicity.add(Chunk.NEWLINE);

	    if(isAsian == 1){
        	pEthnicity.add(new Chunk(BOX_CHECK, segoeUIFont));
        }else{
        	pEthnicity.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        }
	    pEthnicity.add(new Chunk(" Asian ", regularFont)); 
	    pEthnicity.add(new Chunk("(A person having origins in any of the original peoples of the Far East, Southeast Asia, or the Indian subcontinent including, for example, Cambodia, China, India, Japan, Korea, Malaysia, Pakistan, the Philippine Islands, Thailand, and Vietnam)", regularFontItalic)); 
	    pEthnicity.add(Chunk.NEWLINE);

	    if(isNativeHawaiian == 1){
        	pEthnicity.add(new Chunk(BOX_CHECK, segoeUIFont));
        }else{
        	pEthnicity.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        }
	    pEthnicity.add(new Chunk(" Native Hawaiian or Other Pacific Islander ", regularFont)); 
	    pEthnicity.add(new Chunk("(A person having origins in any of the original peoples of Hawaii, Guam, Samoa, or other Pacific Islands)", regularFontItalic)); 
	    pEthnicity.add(Chunk.NEWLINE);

	    if(isWhite == 1){
        	pEthnicity.add(new Chunk(BOX_CHECK, segoeUIFont));
        }else{
        	pEthnicity.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        }
	    pEthnicity.add(new Chunk(" White ", regularFont)); 
	    pEthnicity.add(new Chunk("(A person having origins in any of the original peoples of Europe, the Middle East, or North Africa)", regularFontItalic)); 
	    pEthnicity.add(Chunk.NEWLINE);
	    
	    cellEthnicity.addElement(pEthnicity);
        tableEthnicity.addCell(cellEthnicity);
        return tableEthnicity; 
	}
	
	private PdfPTable createProfileDisability(String disability){
		PdfPTable tableDisability = new PdfPTable(2);
        tableDisability.setPaddingTop(0);
        tableDisability.setSpacingBefore(5);
        tableDisability.setWidthPercentage(100);
        PdfPCell cellDisability = new PdfPCell();
        cellDisability.setBorderWidth(LINE_WIDTH);
        cellDisability.setColspan(2);cellDisability.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellDisability.setPaddingBottom(5);
        Paragraph pDisability = new Paragraph();
        pDisability.setLeading(10);
        if(disability.equalsIgnoreCase("YES")){
        	pDisability.add(new Chunk(BOX_CHECK, segoeUIFont));
        }else{
        	pDisability.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        }
        pDisability.add(new Chunk(" Yes (Check yes if any of the following apply):", regularFont)); 
        pDisability.add(Chunk.NEWLINE);
        
        com.itextpdf.text.List listDisability = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
        listDisability.setIndentationLeft(10);
        listDisability.setListSymbol(new Chunk('\u2022', segoeUIFont));
        listDisability.add(new ListItem(" Deaf or serious difficulty hearing", regularFont)); 
        listDisability.add(new ListItem(" Blind or serious difficulty seeing even when wearing glasses", regularFont)); 
        listDisability.add(new ListItem(" Serious difficulty walking or climbing stairs", regularFont)); 
        listDisability.add(new ListItem(" Other serious difficulty related to a physical, mental, or emotional condition", regularFont)); 
        pDisability.add(listDisability); 
         
        if(disability.equalsIgnoreCase("NO")){
        	pDisability.add(new Chunk(BOX_CHECK, segoeUIFont));
        }else{
        	pDisability.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        }
        pDisability.add(new Chunk(" No", regularFont)); 
        pDisability.add(Chunk.NEWLINE);
        if(disability.equalsIgnoreCase("OT")){
        	pDisability.add(new Chunk(BOX_CHECK, segoeUIFont));
        }else{
        	pDisability.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        }
        pDisability.add(new Chunk(" Do not wish to provide", regularFont));  
        cellDisability.addElement(pDisability);
	    tableDisability.addCell(cellDisability); 
	    return tableDisability; 
	}
	
	private PdfPTable createProfileEmergencyContact(ContactBean bean){
        String[] contentPhone = {bean.getEcFirstName()+" "+bean.getEcLastName(),
        		bean.getEcPhoneNum1(), bean.getEcPhoneType1(),
        		bean.getEcPhoneNum2(), bean.getEcPhoneType2(), bean.getEcRelationship()}; 
        String[] contentAddress = {bean.getEcAddressLine1()+" "+bean.getEcAddressLine2(), 
        		bean.getEcAddressCity(), bean.getEcAddressCounty(), 
        		bean.getEcAddressState(), bean.getEcAddressZip(),""}; 
        
		PdfPTable table;
        int colspan = 1; 
        float[] columnWidths = {34f, 12f, 8f, 12f, 8f, 16f};
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        for(String address: contentPhone){
            table.addCell(createCellBorderBottom(colspan, address));
        }
        
        String[] headerPhone = {"Name of person who will always know how to reach you", "Cell Phone", "Phone Type", "Alternate Phone", "Phone Type", "Relationship to Applicant"}; 
        for(String header: headerPhone){
        	table.addCell(createCellBorderNone(colspan, header));
        }
         
        for(String address: contentAddress){
            table.addCell(createCellBorderBottom(colspan, address));
        }
        
        String[] headerAddress = {"Address of Above Individual", "City", "County", "State", "Zip",""}; 
        for(String header: headerAddress){
            table.addCell(createCellBorderNone(colspan, header));
        } 
        return table; 
	}
	
	
	private PdfPTable createTableAcademicCCCONF(AcademicBean bean){
		PdfPTable table; 
        int colspan = 1; 
        
        float[] columnWidths = {33f, 12f, 18f, 15f, 22f};
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        String[] header1 = {"Institution currently enrolled", "Banner ID", "Major", "Status", "Cum. GPA"}; 
        String[] content1 = {
        	(bean == null||bean.getAcademicSchool()==null) ? " " : ProgramCode.ACADEMIC_SCHOOL.get(bean.getAcademicSchool()),
        	(bean == null||bean.getAcademicBannerID()==null) ? " " : bean.getAcademicBannerID(),
        	(bean == null||bean.getAcademicMajor()==null) ? " " : bean.getAcademicMajor(),
        	(bean == null||bean.getAcademicStatus()==null) ? " " : ProgramCode.ACADEMIC_STATUS.get(bean.getAcademicStatus()),
        	(bean == null||bean.getAcademicGPA()==null) ? " " : String.valueOf(bean.getAcademicGPA())
        }; 
        for(String content: content1){
            table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header1){
            table.addCell(createCellBorderNone(colspan, content));
        }

        String[] header2 = {"Institution where you are transferring to", "Transfer Date", "Intended Major", "Credits Completed", "Who referred you to NM AMP?"}; 
        String[] content2 = {
        	(bean == null||bean.getAcademicTransferSchool()==null) ? " " : ProgramCode.ACADEMIC_SCHOOL.get(bean.getAcademicTransferSchool()),
        	(bean == null||bean.getAcademicTransferDate()==null) ? " " : Parse.FORMAT_DATE_MDY.format(bean.getAcademicTransferDate()),
        	(bean == null||bean.getAcademicIntendedMajor()==null) ? " " : bean.getAcademicIntendedMajor(),
        	(bean == null||bean.getAcademicCredit()==null) ? " " : bean.getAcademicCredit(),
        	(bean == null||bean.getAcademicReferrer()==null) ? " " : bean.getAcademicReferrer()
        }; 
        
        for(String content: content2){
        	table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header2){
        	table.addCell(createCellBorderNone(colspan, content));
        }
		return table; 
	}
	private PdfPTable createTableAcademicMESA(AcademicBean bean){
		PdfPTable table; 
        int colspan = 1; 
        
        float[] columnWidths = {33f, 12f, 30f, 25f};
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        String[] header1 = {"High School you currently attend", "Academic Year", "Cum. GPA", "High School Graduation Date"}; 
        String[] content1 = {
        	(bean == null||bean.getAcademicSchool()==null) ? " " : bean.getAcademicSchool(), 
        	(bean == null||bean.getAcademicYear()==null) ? " " : ProgramCode.ACADEMIC_YEAR.get(bean.getAcademicYear()),
        	(bean == null||bean.getAcademicGPA()==null) ? " " : String.valueOf(bean.getAcademicGPA()),
        	(bean == null||bean.getAcademicGradDate()==null) ? " " : Parse.FORMAT_DATE_MDY.format(bean.getAcademicGradDate())
        };
        for(String content: content1){
            table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header1){
            table.addCell(createCellBorderNone(colspan, content));
        }

        String[] header2 = {"Institution you will be attending", "Date Starting", "Intended Major", "How did you hear about scholarship?"}; 
        String[] content2 = {
        	(bean == null||bean.getAcademicTransferSchool()==null) ? " " : ProgramCode.ACADEMIC_SCHOOL.get(bean.getAcademicTransferSchool()),
        	(bean == null||bean.getAcademicTransferDate()==null) ? " " : Parse.FORMAT_DATE_MDY.format(bean.getAcademicTransferDate()),
        	(bean == null||bean.getAcademicIntendedMajor()==null) ? " " : bean.getAcademicIntendedMajor(),
        	(bean == null||bean.getAcademicReferrer()==null) ? " " : bean.getAcademicReferrer()
        }; 
        for(String content: content2){
        	table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header2){
        	table.addCell(createCellBorderNone(colspan, content));
        }
		return table; 
	}
	private PdfPTable createTableAcademicSCCORE(AcademicBean bean){
		PdfPTable table; 
        int colspan = 1; 
        
        float[] columnWidths = {35f, 35f, 20f, 10f};
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        
        String[] header1 = {"Institution currently enrolled", "Academic Year", "Banner ID", "Cum. GPA"};
        String[] content1 = {
        	(bean == null||bean.getAcademicSchool()==null) ? " " : ProgramCode.ACADEMIC_SCHOOL_TWO.get(bean.getAcademicSchool()),
        	(bean == null||bean.getAcademicYear()==null) ? " " : ProgramCode.ACADEMIC_YEAR.get(bean.getAcademicYear()),
        	(bean == null||bean.getAcademicBannerID()==null) ? " " : bean.getAcademicBannerID(), 
        	(bean == null||bean.getAcademicGPA()==null) ? " " : String.valueOf(bean.getAcademicGPA())
        }; 
        for(String content: content1){
            table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header1){
            table.addCell(createCellBorderNone(colspan, content));
        }
        
        String[] header2 = {"Major", "Minor", "Expected Grad Date (M/Y)",""};
        String[] content2 = {
        	(bean == null||bean.getAcademicMajor()==null) ? " " : bean.getAcademicMajor(), 
        	(bean == null||bean.getAcademicMinor()==null) ? " " : bean.getAcademicMinor(), 
        	(bean == null||bean.getAcademicGradDate()==null) ? " " : Parse.FORMAT_DATE_MDY.format(bean.getAcademicGradDate()), ""
        }; 
        for(String content: content2){
        	table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header2){
        	table.addCell(createCellBorderNone(colspan, content));
        }
		return table; 
	}
	private PdfPTable createTableAcademicTRANS(AcademicBean bean){
		PdfPTable table; 
        int colspan = 1; 
        
        float[] columnWidths = {34f, 34f, 22f, 10f};
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        
        String[] header1 = {"Institution currently enrolled", "Academic Year", "Banner ID (NMSU)", "Cum. GPA"};
        String[] content1 = {
        	(bean == null||bean.getAcademicSchool()==null) ? " " : ProgramCode.ACADEMIC_SCHOOL.get(bean.getAcademicSchool()),
        	(bean == null||bean.getAcademicYear()==null) ? " " : ProgramCode.ACADEMIC_YEAR.get(bean.getAcademicYear()),
            (bean == null||bean.getAcademicBannerID()==null) ? " " :bean.getAcademicBannerID(), 
            (bean == null||bean.getAcademicGPA()==null) ? " " :String.valueOf(bean.getAcademicGPA())
        }; 
        for(String content: content1){
            table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header1){
            table.addCell(createCellBorderNone(colspan, content));
        }
        
        String[] header2 = {"Major", "Minor", "Expected Grad Date (M/Y)",""};
        String[] content2 = {
        	(bean == null||bean.getAcademicMajor()==null) ? " " :bean.getAcademicMajor(), 
        	(bean == null||bean.getAcademicMinor()==null) ? " " :bean.getAcademicMinor(), 
        	(bean == null||bean.getAcademicGradDate()==null) ? " " :Parse.FORMAT_DATE_MDY.format(bean.getAcademicGradDate()),""
        }; 
        for(String content: content2){
        	table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header2){
        	table.addCell(createCellBorderNone(colspan, content));
        }
        
        String[] header3 = {"Institution where you are transferring to", "Intended Major", "Who referred you to NM AMP?","Transfer Date"};
        String[] content3 = {
        	(bean == null||bean.getAcademicTransferSchool()==null) ? " " : ProgramCode.ACADEMIC_SCHOOL.get(bean.getAcademicTransferSchool()),
        	(bean == null||bean.getAcademicIntendedMajor()==null) ? " " :bean.getAcademicIntendedMajor(), 
        	(bean == null||bean.getAcademicReferrer()==null) ? " " :bean.getAcademicReferrer(), 
        	(bean == null||bean.getAcademicTransferDate()==null) ? " " :Parse.FORMAT_DATE_MDY.format(bean.getAcademicTransferDate())
        }; 
        for(String content: content3){
        	table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header3){
        	table.addCell(createCellBorderNone(colspan, content));
        }
		return table; 
	}
	private PdfPTable createTableAcademicURS(AcademicBean bean){
		PdfPTable table; 
        int colspan = 1; 
        
        float[] columnWidths = {34f, 34f, 22f, 10f};
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        
        String[] header1 = {"Institution currently enrolled", "Academic Year", "Banner ID", "Cum. GPA"};
        String[] content1 = {(bean == null||bean.getAcademicSchool()==null) ? " " : ProgramCode.ACADEMIC_SCHOOL.get(bean.getAcademicSchool()),
    			(bean == null||bean.getAcademicYear()==null) ? " " : ProgramCode.ACADEMIC_YEAR.get(bean.getAcademicYear()),
    			(bean == null||bean.getAcademicBannerID()==null) ? " " : bean.getAcademicBannerID(), 
    			(bean == null||bean.getAcademicGPA()==null) ? " " : String.valueOf(bean.getAcademicGPA())
        }; 
        
        for(String content: content1){
            table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header1){
            table.addCell(createCellBorderNone(colspan, content));
        }
        
        String[] header2 = {"Major", "Minor", "Expected Grad Date (M/Y)",""};
        String[] content2 = {
        	(bean == null || bean.getAcademicMajor()==null) ? " " : bean.getAcademicMajor(), 
        	(bean == null || bean.getAcademicMinor()==null) ? " " : bean.getAcademicMinor(), 
        	(bean == null || bean.getAcademicGradDate()==null) ? " " : Parse.FORMAT_DATE_MDY.format(bean.getAcademicGradDate()), ""
        }; 
        for(String content: content2){
        	table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header2){
        	table.addCell(createCellBorderNone(colspan, content));
        }
        
		return table; 
	}
	
	private PdfPTable createTableInvolvementCCCONF(InvolvementBean bean){
		Integer ampScholarship = (bean == null) ? null : bean.getAmpScholarship();  
		String ampScholarshipSchool = "";
		String ampScholarshipType = "";
		String ampScholarshipTerm = "", ampScholarshipAmount = ""; 
		if(bean != null && ampScholarship == 1){
			ampScholarshipSchool = bean.getAmpScholarshipSchool(); 
			ampScholarshipTerm = bean.getAmpScholarshipSemester() + "/" + bean.getAmpScholarshipYear(); 
			ampScholarshipType = bean.getAmpScholarshipType();
			ampScholarshipAmount = String.valueOf(bean.getAmpScholarshipAmount()); 
		}
	 
		Integer otherScholarship = (bean == null) ? null : bean.getOtherScholarship();
		String listOtherScholarship = (bean == null) ? null : bean.getListOtherScholarship();
		
		Integer isCurrentEmploy = (bean == null) ? null : bean.getIsCurrentEmploy();
    	String listEmployCampus = "", listEmployDept = "", listEmploySupervisor = ""; 
    	String listEmployStart = "", listEmployEnd = ""; 
    	if(bean != null && isCurrentEmploy == 1){
    		listEmployCampus = ProgramCode.NMSU_CAMPUS.get(bean.getListEmployCampus());
    		listEmployDept = bean.getListEmployDept();
    		listEmploySupervisor = bean.getListEmploySupervisor(); 
    		listEmployStart = Parse.FORMAT_DATE_MDY.format(bean.getListEmployStart());
    		listEmployEnd = Parse.FORMAT_DATE_MDY.format(bean.getListEmployEnd());
    	}
    	 
    	Integer everInResearch = (bean == null) ? null : bean.getEverInResearch();
    	String describeResearch = (bean == null) ? null : bean.getDescribeResearch();
    	
    	Integer everAttendConference = (bean == null) ? null : bean.getEverAttendConference();
    	
    	Integer programEverIn = (bean == null) ? null : bean.getProgramEverIn(); 
    	String programEverInYear = bean == null || programEverIn == 0 ? "" :
    		String.valueOf(bean.getProgramEverInYear());  
    	 
		PdfPTable table; 
		PdfPCell cell;
        Paragraph paragraph; 
        int colspan = 13; 
        table = new PdfPTable(colspan);
        table.setWidthPercentage(100);
        
        /* Other AMP Scholarship */ 
        colspan = 7;
        cell = createCellBorderNone(colspan, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Have you ever received an AMP scholarship from one of our programs?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 6; 
        cell = createCellBorderNone(colspan, null);  
        cell.addElement(addCheckBox(String.valueOf(ampScholarship), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        colspan = 3;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, at which school?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 10; 
        table.addCell(createCellBorderBottom(colspan, ampScholarshipSchool));
        
        colspan = 5;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, what type of New Mexico AMP scholarship?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 8; 
        cell = createCellBorderNone(colspan, null); 
        cell.addElement(addCheckBox(ampScholarshipType, ampScholarship, ProgramCode.SCHOLARSHIP_TYPE_CCCONF));
        table.addCell(cell);
        
        colspan = 6;
        cell = createCellBorderNone(colspan, null); 
    	paragraph = new Paragraph(); 
        paragraph.add(new Chunk("What Semester/Year was the New Mexico AMP Scholarship awarded?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
    	colspan = 2; 
        table.addCell(createCellBorderBottom(colspan, ampScholarshipTerm));
        
        colspan = 4; 
        cell = createCellBorderNone(colspan, null); 
    	paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Amount of New Mexico AMP Scholarship?", regularFont));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 1; 
        table.addCell(createCellBorderBottom(colspan, ampScholarshipAmount));
        
        /* Other Scholarship */
        cell = createCellBorderNone(7, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Are you currently receiving other scholarships or research support?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        cell = createCellBorderNone(6, null); 
        cell.addElement(addCheckBox(String.valueOf(otherScholarship), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        // if yes 
        cell = createCellBorderNone(2, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, list:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        if(bean != null && otherScholarship == 1){
        	table.addCell(createCellBorderBottom(11, listOtherScholarship));
    	}else{
    		table.addCell(createCellBorderBottom(11, ""));
    	} 
        
        /* Employment and Research */ 
        cell = createCellBorderNone(7, null);
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Are you currently employed by NMSU or an NMSU branch campus?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        cell = createCellBorderNone(6, null);
        cell.addElement(addCheckBox(String.valueOf(isCurrentEmploy), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        cell = createCellBorderNone(13, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, please indicate the following:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        cell = createCellBorderNone(1, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Campus:", regularFont)); 
        cell.addElement(paragraph);
        table.addCell(cell);
        
        table.addCell(createCellBorderBottom(4, listEmployCampus));
        
        cell = createCellBorderNone(2, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Department:", regularFont)); 
        cell.addElement(paragraph);
        table.addCell(cell);
        
        table.addCell(createCellBorderBottom(6, listEmployDept));
         
        cell = createCellBorderNone(1, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Supervisor:", regularFont)); 
        cell.addElement(paragraph);
        table.addCell(cell);
        
        table.addCell(createCellBorderBottom(4, listEmploySupervisor));
         
        cell = createCellBorderNone(3, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Dates of Employment: From", regularFont)); 
        cell.addElement(paragraph);
        table.addCell(cell);
        
        table.addCell(createCellBorderBottom(2, listEmployStart));
        
        cell = createCellBorderNone(1, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("to", regularFont)); 
        paragraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(paragraph);
        table.addCell(cell);

        table.addCell(createCellBorderBottom(2, listEmployEnd));
        
        cell = createCellBorderNone(7, null);
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Have you ever participated in undergraduate research?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        cell = createCellBorderNone(6, null);
        cell.addElement(addCheckBox(String.valueOf(everInResearch), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        cell = createCellBorderNone(13, "If Yes, please describe your experience in the space below:");
        table.addCell(cell);
        if(bean != null&& everInResearch == 1 && describeResearch != null){
        	cell = createCellFixed(70f, new StringBuilder(describeResearch)); 
        }else{
        	cell = createCellFixed(70f, new StringBuilder(""));  
        }
        cell.setColspan(13);
        table.addCell(cell);
        
        cell = createCellBorderNone(7, null);
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Have you previously attended the New Mexico AMP Undergraduate Conference?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        cell = createCellBorderNone(6, null);
        cell.addElement(addCheckBox(String.valueOf(everAttendConference), 1, ProgramCode.YES_NO));
        table.addCell(cell);

        cell = createCellBorderNone(7, null);
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Have you received a New Mexico AMP conference stipend before?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        cell = createCellBorderNone(2, null);
        cell.addElement(addCheckBox(String.valueOf(programEverIn), 1, ProgramCode.YES_NO));
        table.addCell(cell);
      
        cell = createCellBorderNone(2, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, what year?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
       
        table.addCell(createCellBorderBottom(3, programEverInYear));
       
        return table; 
	}
	private PdfPTable createTableInvolvementMESA(InvolvementBean bean){
		
		Integer ampScholarship = (bean == null) ? null : bean.getAmpScholarship(); //1; 
		String ampScholarshipSchool = "";//"New Mexico Inst of Mining and Technology";
		String ampScholarshipType = ""; // "CCSTIPEND"; 
		String ampScholarshipTerm = "", ampScholarshipAmount = ""; 
		if(bean != null && ampScholarship == 1){
			ampScholarshipSchool = bean.getAmpScholarshipSchool(); 
			ampScholarshipTerm = bean.getAmpScholarshipSemester() + "/" + bean.getAmpScholarshipYear(); 
			ampScholarshipType = bean.getAmpScholarshipType();
			ampScholarshipAmount = String.valueOf(bean.getAmpScholarshipAmount()); 
		}
		 
		Integer otherScholarship = (bean == null) ? null : bean.getOtherScholarship(); //1; 
		String listOtherScholarship = (bean == null) ? null : bean.getListOtherScholarship(); //"XX, YY";
    	
		PdfPTable table; 
		PdfPCell cell;
        Paragraph paragraph; 
        int colspan = 15; 
        table = new PdfPTable(colspan);
        table.setWidthPercentage(100);
        
        /* Other AMP Scholarship */ 
        colspan = 8;
        cell = createCellBorderNone(colspan, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Have you ever received an AMP scholarship from one of our programs?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 7; 
        cell = createCellBorderNone(colspan, null);  
        cell.addElement(addCheckBox(String.valueOf(ampScholarship), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        colspan = 3;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, at which school?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 12; 
        table.addCell(createCellBorderBottom(colspan, ampScholarshipSchool));
        
        colspan = 5;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, what type of New Mexico AMP scholarship?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 10; 
        cell = createCellBorderNone(colspan, null); 
        cell.addElement(addCheckBox(ampScholarshipType, ampScholarship, ProgramCode.SCHOLARSHIP_TYPE));
        table.addCell(cell);
        
        colspan = 7;
        cell = createCellBorderNone(colspan, null); 
    	paragraph = new Paragraph(); 
        paragraph.add(new Chunk("What Semester/Year was the New Mexico AMP Scholarship awarded?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
    	colspan = 2; 
    	table.addCell(createCellBorderBottom(colspan, ampScholarshipTerm));
        
        colspan = 5; 
        cell = createCellBorderNone(colspan, null); 
    	paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Amount of New Mexico AMP Scholarship?", regularFont));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 1; 
        table.addCell(createCellBorderBottom(colspan, ampScholarshipAmount)); 
        /* Other Scholarship */
        cell = createCellBorderNone(8, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Are you currently receiving other scholarships or research support?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        cell = createCellBorderNone(7, null); 
        cell.addElement(addCheckBox(String.valueOf(otherScholarship), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        cell = createCellBorderNone(2, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, list:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        if(bean != null && otherScholarship == 1){
        	table.addCell(createCellBorderBottom(13, listOtherScholarship));
    	}else{
    		table.addCell(createCellBorderBottom(13, ""));
    	} 
       
        return table; 
	}
	private PdfPTable createTableInvolvementSCCORE(InvolvementBean bean){
		Integer ampScholarship = (bean == null) ? null : bean.getAmpScholarship();
		String ampScholarshipSchool = "";
		String ampScholarshipType = "";
		String ampScholarshipTerm = "", ampScholarshipAmount = ""; 
		if(bean != null && ampScholarship == 1){
			ampScholarshipSchool = bean.getAmpScholarshipSchool(); 
			ampScholarshipTerm = bean.getAmpScholarshipSemester() + "/" + bean.getAmpScholarshipYear(); 
			ampScholarshipType = bean.getAmpScholarshipType();
			ampScholarshipAmount = String.valueOf(bean.getAmpScholarshipAmount()); 
		}
		
		Integer otherScholarship = (bean == null) ? null : bean.getOtherScholarship();  
		String listOtherScholarship = (bean == null) ? null : bean.getListOtherScholarship();
		 
		Integer programEverIn = (bean == null) ? null : bean.getProgramEverIn(); 
    	String programEverInYear = bean == null || programEverIn == 0 ? "" :
    		String.valueOf(bean.getProgramEverInYear()); 
    	
		PdfPTable table; 
		PdfPCell cell;
        Paragraph paragraph; 
        int colspan = 15; 
        table = new PdfPTable(colspan);
        table.setWidthPercentage(100);

        cell = createCellBorderNone(8, null);
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Have you previously been in the SCCORE Program?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        cell = createCellBorderNone(2, null);
        cell.addElement(addCheckBox(String.valueOf(programEverIn), 1, ProgramCode.YES_NO));
        table.addCell(cell);
      
        cell = createCellBorderNone(2, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, what year?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
       
        table.addCell(createCellBorderBottom(3, programEverInYear));
        
        /* Other AMP Scholarship */ 
        colspan = 8;
        cell = createCellBorderNone(colspan, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Have you ever received an AMP scholarship from one of our programs?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 7; 
        cell = createCellBorderNone(colspan, null);  
        cell.addElement(addCheckBox(String.valueOf(ampScholarship), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        colspan = 3;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, at which school?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 12; 
        table.addCell(createCellBorderBottom(colspan, ampScholarshipSchool));
        
        colspan = 5;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, what type of New Mexico AMP scholarship?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 10; 
        cell = createCellBorderNone(colspan, null); 
        cell.addElement(addCheckBox(ampScholarshipType, ampScholarship, ProgramCode.SCHOLARSHIP_TYPE));
        table.addCell(cell);
        
        colspan = 7;
        cell = createCellBorderNone(colspan, null); 
    	paragraph = new Paragraph(); 
        paragraph.add(new Chunk("What Semester/Year was the New Mexico AMP Scholarship awarded?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
    	colspan = 2; 
    	table.addCell(createCellBorderBottom(colspan, ampScholarshipTerm));
        
        colspan = 5; 
        cell = createCellBorderNone(colspan, null); 
    	paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Amount of New Mexico AMP Scholarship?", regularFont));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 1; 
        table.addCell(createCellBorderBottom(colspan, ampScholarshipAmount));
        
        /* Other Scholarship */
        cell = createCellBorderNone(8, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Are you currently receiving other scholarships or research support?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        cell = createCellBorderNone(7, null); 
        cell.addElement(addCheckBox(String.valueOf(otherScholarship), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        cell = createCellBorderNone(2, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, list:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        if(bean != null && otherScholarship == 1){
        	table.addCell(createCellBorderBottom(13, listOtherScholarship));
    	}else{
    		table.addCell(createCellBorderBottom(13, ""));
    	} 
       
        return table; 
	}
	private PdfPTable createTableInvolvementTRANS(InvolvementBean bean){
		Integer ampScholarship = (bean == null) ? null : bean.getAmpScholarship();
		String ampScholarshipSchool = "";
		String ampScholarshipType = "";
		String ampScholarshipTerm = "", ampScholarshipAmount = ""; 
		if(bean != null && ampScholarship == 1){
			ampScholarshipSchool = bean.getAmpScholarshipSchool(); 
			ampScholarshipTerm = bean.getAmpScholarshipSemester() + "/" + bean.getAmpScholarshipYear(); 
			ampScholarshipType = bean.getAmpScholarshipType();
			ampScholarshipAmount = String.valueOf(bean.getAmpScholarshipAmount()); 
		}
		
		Integer otherScholarship = (bean == null) ? null : bean.getOtherScholarship(); //1; 
		String listOtherScholarship = (bean == null) ? null : bean.getListOtherScholarship();
	 
		PdfPTable table; 
		PdfPCell cell;
        Paragraph paragraph; 
        int colspan = 15; 
        table = new PdfPTable(colspan);
        table.setWidthPercentage(100);
        
        /* Other AMP Scholarship */ 
        colspan = 8;
        cell = createCellBorderNone(colspan, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Have you ever received an AMP scholarship from one of our programs?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 7; 
        cell = createCellBorderNone(colspan, null);  
        cell.addElement(addCheckBox(String.valueOf(ampScholarship), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        colspan = 3;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, at which school?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 12; 
        table.addCell(createCellBorderBottom(colspan, ampScholarshipSchool));
        
        colspan = 5;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, what type of New Mexico AMP scholarship?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 10; 
        cell = createCellBorderNone(colspan, null); 
        cell.addElement(addCheckBox(ampScholarshipType, ampScholarship, ProgramCode.SCHOLARSHIP_TYPE));
        table.addCell(cell);
        
        colspan = 7;
        cell = createCellBorderNone(colspan, null); 
    	paragraph = new Paragraph(); 
        paragraph.add(new Chunk("What Semester/Year was the New Mexico AMP Scholarship awarded?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
    	colspan = 2; 
    	table.addCell(createCellBorderBottom(colspan, ampScholarshipTerm));
        
        colspan = 5; 
        cell = createCellBorderNone(colspan, null); 
    	paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Amount of New Mexico AMP Scholarship?", regularFont));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 1; 
        table.addCell(createCellBorderBottom(colspan, ampScholarshipAmount));
        
        /* Other Scholarship */
        cell = createCellBorderNone(8, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Are you currently receiving other scholarships or research support?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        cell = createCellBorderNone(7, null); 
        cell.addElement(addCheckBox(String.valueOf(otherScholarship), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        cell = createCellBorderNone(2, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, list:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        if(bean != null && otherScholarship == 1){
        	table.addCell(createCellBorderBottom(13, listOtherScholarship));
    	}else{
    		table.addCell(createCellBorderBottom(13, ""));
    	} 
    
        return table; 
	}
	private PdfPTable createTableInvolvementURS(InvolvementBean bean){
		Integer programEverIn = (bean == null) ? null : bean.getProgramEverIn(); 
    	String programEverInSemesters = (bean == null) ? null : bean.getProgramEverInSemesters(); 
    	
    	Integer ampScholarship = (bean == null) ? null : bean.getAmpScholarship();  
		String ampScholarshipSchool = "";
		String ampScholarshipType = "";
		String ampScholarshipTerm = "", ampScholarshipAmount = ""; 
		if(bean != null && ampScholarship == 1){
			ampScholarshipSchool = bean.getAmpScholarshipSchool(); 
			ampScholarshipTerm = bean.getAmpScholarshipSemester() + "/" + bean.getAmpScholarshipYear(); 
			ampScholarshipType = bean.getAmpScholarshipType();
			ampScholarshipAmount = String.valueOf(bean.getAmpScholarshipAmount()); 
		}
		
		Integer otherScholarship = (bean == null) ? null : bean.getOtherScholarship();
		String listOtherScholarship = (bean == null) ? null : bean.getListOtherScholarship();
    	
		PdfPTable table; 
		PdfPCell cell;
        Paragraph paragraph; 
        int colspan = 15; 
        table = new PdfPTable(colspan);
        table.setWidthPercentage(100);
        
        cell = createCellBorderNone(8, null);
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Have you previously been in the URA Program?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        cell = createCellBorderNone(7, null);
        cell.addElement(addCheckBox(String.valueOf(programEverIn), 1, ProgramCode.YES_NO));
        table.addCell(cell);
      
        cell = createCellBorderNone(8, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, list the semester(s) you have participated", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
       
        table.addCell(createCellBorderBottom(7, programEverInSemesters));
       
        colspan = 8;
        cell = createCellBorderNone(colspan, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Have you ever received an AMP scholarship from one of our programs?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 7; 
        cell = createCellBorderNone(colspan, null);  
        cell.addElement(addCheckBox(String.valueOf(ampScholarship), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        colspan = 3;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, at which school?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 12; 
        table.addCell(createCellBorderBottom(colspan, ampScholarshipSchool));
        
        colspan = 5;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, what type of New Mexico AMP scholarship?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 10; 
        cell = createCellBorderNone(colspan, null); 
        cell.addElement(addCheckBox(ampScholarshipType, ampScholarship, ProgramCode.SCHOLARSHIP_TYPE));
        table.addCell(cell);
        
        colspan = 7;
        cell = createCellBorderNone(colspan, null); 
    	paragraph = new Paragraph(); 
        paragraph.add(new Chunk("What Semester/Year was the New Mexico AMP Scholarship awarded?", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
    	colspan = 2; 
    	table.addCell(createCellBorderBottom(colspan, ampScholarshipTerm));
        
        colspan = 5; 
        cell = createCellBorderNone(colspan, null); 
    	paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Amount of New Mexico AMP Scholarship?", regularFont));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 1; 
        table.addCell(createCellBorderBottom(colspan, ampScholarshipAmount));
        
        cell = createCellBorderNone(8, null);
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Are you currently receiving other scholarships or research support?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        cell = createCellBorderNone(7, null); 
        cell.addElement(addCheckBox(String.valueOf(otherScholarship), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        cell = createCellBorderNone(2, null);
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, list:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        if(bean != null && otherScholarship == 1){
        	table.addCell(createCellBorderBottom(13, listOtherScholarship));
    	}else{
    		table.addCell(createCellBorderBottom(13, ""));
    	} 
       
        return table; 
	}
	
	
	/* Essay for CCCONF MESA SCCORE TRANS URS(Project Proposal) */ 
	private PdfPTable createTableEssay(String[] headers, String[] contents){
		PdfPTable table = new PdfPTable(1); 
        PdfPCell cell; 
        table.setWidthPercentage(100);
        
        for(int i = 0; i < headers.length; i ++){
        	cell = createCellBorderNone(1, headers[i]); 
            cell.setPaddingTop(5f);cell.setPaddingBottom(5f);
            table.addCell(cell);
            table.addCell(createCellBorder(contents[i]));
        }
        
        return table; 
	}
	private PdfPTable createTableEssaySchoolSCCORE(String sccoreSchoolAttendPref, 
			String sccoreSchoolAttendAltn){
    	
		PdfPTable table; 
        float[] columnWidths = {10f, 90f};
        int colspan= columnWidths.length; 
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
         
        table.addCell(createCellBorderNone(colspan, "Select the New Mexico institution at which you want to attend the SCCORE program"));
      
        PdfPCell cell = createCellBorderNone(1%colspan, null);  
        Paragraph paragraph = new Paragraph(); 
        paragraph.add(new Chunk("1st Choice:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        table.addCell(createCellBorderBottom(1%colspan, sccoreSchoolAttendPref));
        
        cell = createCellBorderNone(1%colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("2nd Choice:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        table.addCell(createCellBorderBottom(1%colspan, sccoreSchoolAttendAltn));
        return table; 
	}
	private PdfPTable createTableProjectProposalTitleURS(ProjectBean bean){
		
		String projectTitle = ""; 
		Integer isExternal = null; 
		
		String externalAgency = "", externalTitle = "", externalDuration = ""; 
		if(bean != null){
			projectTitle = bean.getProjectTitle(); 
			isExternal = bean.getExternalProject(); 
			if(isExternal == 1){
				externalAgency = bean.getExternalAgency(); 
				externalTitle = bean.getExternalTitle(); 
				externalDuration = bean.getExternalDuration(); 
			}
		}
		 
		PdfPTable table; 
		PdfPCell cell;
        Paragraph paragraph; 
        int colspan = 17; 
        table = new PdfPTable(colspan);
        table.setWidthPercentage(100);

        colspan = 3;
        cell = createCellBorderNone(colspan, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Research Project Title:", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 14; 
        table.addCell(createCellBorderBottom(colspan, projectTitle));
        
        colspan = 5;
        cell = createCellBorderNone(colspan, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Is this an externally funded project?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 12; 
        cell = createCellBorderNone(colspan, null);  
        cell.addElement(addCheckBox(String.valueOf(isExternal), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        colspan = 3;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, title of Agency:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 14; 
        table.addCell(createCellBorderBottom(colspan, externalAgency));
        colspan = 3;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Federal Project Title:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 14; 
        table.addCell(createCellBorderBottom(colspan, externalTitle));
        colspan = 3;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Duration of Project:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 14; 
        table.addCell(createCellBorderBottom(colspan, externalDuration));
         
        return table; 
	}
	
private PdfPTable createTableProjectProposalTitleIREP(IREPProjectBean bean){
		
		Integer everFundAmp = null; 
		String researchDate = " ", leaveDate = " ", returnDate = " ", listPrograms = " "; 
		if(bean != null){
			researchDate = bean.getResearchDate() == null ? " " 
		    		   : Parse.FORMAT_DATE_MDY.format(bean.getResearchDate());
			leaveDate = bean.getLeaveDate() == null ? " " 
		    		   : Parse.FORMAT_DATE_MDY.format(bean.getLeaveDate());
			returnDate = bean.getReturnDate() == null ? " " 
		    		   : Parse.FORMAT_DATE_MDY.format(bean.getReturnDate());
			everFundAmp = bean.getEverFundAmp(); 
			if(everFundAmp != null && everFundAmp == 1){
				listPrograms = bean.getListProgram(); 
			}
		}
	    		   
		PdfPTable table; 
		PdfPCell cell;
        Paragraph paragraph; 
        int colspan = 17; 
        table = new PdfPTable(colspan);
        table.setWidthPercentage(100);

        colspan = 6;
        cell = createCellBorderNone(colspan, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Dates of Scheduled Research:", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 11; 
        table.addCell(createCellBorderBottom(colspan, researchDate));
        colspan = 6;
        cell = createCellBorderNone(colspan, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Expected Date Leaving:", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 11; 
        table.addCell(createCellBorderBottom(colspan, leaveDate));
        colspan = 6;
        cell = createCellBorderNone(colspan, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Expected Date of Return:", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 11; 
        table.addCell(createCellBorderBottom(colspan, returnDate));
        
        
        colspan = 11;
        cell = createCellBorderNone(colspan, null); 
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("Have you been funded by New Mexico AMP Scholarships in past?", regularFontBold));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 6; 
        cell = createCellBorderNone(colspan, null);  
        cell.addElement(addCheckBox(String.valueOf(everFundAmp), 1, ProgramCode.YES_NO));
        table.addCell(cell);
        
        colspan = 6;
        cell = createCellBorderNone(colspan, null);  
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If Yes, list the program:", regularFont));
        cell.addElement(paragraph);
        table.addCell(cell);
        
        colspan = 11; 
        table.addCell(createCellBorderBottom(colspan, listPrograms));
        return table; 
	}
	
	private PdfPTable createTableRecommender(RecommenderBean bean){
		String address = (bean == null || bean.getAddressLine1() == null || bean.getAddressLine1().length() == 0)
				? " " : bean.getAddressLine1(); 
		String city = bean == null || bean.getAddressCity() == null || bean.getAddressCity().length() == 0 
				? " " : bean.getAddressCity(); 
		String state = bean == null || bean.getAddressState() == null || bean.getAddressState().length() == 0 
				? " " : bean.getAddressState(); 
		String zip = bean == null || bean.getAddressZip() == null || bean.getAddressZip().length() == 0 
				? " " : bean.getAddressZip(); 
		String country = bean == null || bean.getAddressCountry() == null || bean.getAddressCountry().length() == 0 
				? " " : bean.getAddressCountry(); 
		String[] contentAddress = {address, city, state, zip, country};
        String[] phoneEmail = {bean == null ? " " : bean.getFirstName(),
        		bean == null ? " " : bean.getLastName(), 
        		bean == null ? " " : bean.getPrefix(),
        		bean == null ? " " : bean.getEmail(), 
        		bean == null ? " " : bean.getPhone(), 
        		bean == null ? " " : bean.getInstitution()}; 
        
		PdfPTable table;
        table = new PdfPTable(30);
        table.setWidthPercentage(100);
        
        int colspan; 
        for(int i = 0; i < phoneEmail.length; i ++){
        	switch (i) {
			case 0:
				colspan = 4; break;
			case 1:
				colspan = 4; break;
			case 2:
				colspan = 2; break;
			case 3:
				colspan = 7; break;
			case 4:
				colspan = 4; break;
			case 5:
				colspan = 9; break;
			default:
				colspan = 6; break; 	
			}
            table.addCell(createCellBorderBottom(colspan, phoneEmail[i]));
        }
        
        String[] headerPhoneEmail = {"First Name", "Last Name", "Prefix", "Email", "Phone Number", "Institution/Employer"}; 
        for(int i = 0; i < headerPhoneEmail.length; i ++){
        	switch (i) {
			case 0:
				colspan = 4; break;
			case 1:
				colspan = 4; break;
			case 2:
				colspan = 2; break;
			case 3:
				colspan = 7; break;
			case 4:
				colspan = 4; break;
			case 5:
				colspan = 9; break;
			default:
				colspan = 6; break; 	
			}
        	table.addCell(createCellBorderNone(colspan, headerPhoneEmail[i]));
        }
        
        for(int i = 0; i < contentAddress.length; i++){
        	switch (i) {
			case 0:
				colspan = 10; break;
			case 1:
				colspan = 6; break;
			case 2:
				colspan = 6; break;
			case 3:
				colspan = 5; break;
			case 4:
				colspan = 4; break;
			default:
				colspan = 6; break; 	
			}
            table.addCell(createCellBorderBottom(colspan, contentAddress[i]));
        }
        
        String[] headerCurrent = {"Address", "City", "State", "Zip", "Country"}; 
        for(int i = 0; i < headerCurrent.length; i++){
        	switch (i) {
			case 0:
				colspan = 10; break;
			case 1:
				colspan = 6; break;
			case 2:
				colspan = 6; break;
			case 3:
				colspan = 5; break;
			case 4:
				colspan = 4; break;
			default:
				colspan = 6; break; 	
			}
        	table.addCell(createCellBorderNone(colspan, headerCurrent[i]));
        }
         
        return table; 
	}
	
	private PdfPTable createTableMentorURS(MentorBean bean){
		 
		PdfPTable table;
        int colspan = 1; 
        float[] columnWidths = {34f, 17f, 33f, 17f};
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        String[] header1 = {"First Name", "MI", "Last Name", "Prefix"}; 
        String[] content1 = null; 
        if(bean == null){
        	content1 = new String[]{" "," "," "," "}; 
        }else{
        	content1 = new String[]{bean.getMentorFirstName(), bean.getMentorMiddleName(), 
                bean.getMentorLastName(), bean.getMentorPrefix()}; 
        }
        for(String address: content1){
            table.addCell(createCellBorderBottom(colspan, address));
        }
        for(String header: header1){
        	table.addCell(createCellBorderNone(colspan, header));
        }
         
        table.addCell(createCellBorderBottom(4, bean == null ? " " : bean.getMentorTitle()));
        table.addCell(createCellBorderNone(4, "Title"));
         
        table.addCell(createCellBorderBottom(2, bean == null ? " " : bean.getMentorInstitution()));
        table.addCell(createCellBorderBottom(2, bean == null ? " " : bean.getMentorDept()));
        table.addCell(createCellBorderNone(2, "Name of Institution or Organization"));
        table.addCell(createCellBorderNone(2, "Name of Department"));
        
        String[] header3 = {"Building/MSC", "", "Preferred Phone Number", "Fax Number"}; 
        String[] content3 = {
        		bean == null ? " " : bean.getMentorBuilding(), "", 
        		bean == null ? " " : bean.getMentorPhone(), 
        		bean == null ? " " : bean.getMentorFax()
        }; 
        for(String content: content3){
        	table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header3){
        	table.addCell(createCellBorderNone(colspan, content));
        }
        
        table.addCell(createCellBorderBottom(4, bean == null ? " " : bean.getMentorEmail()));
        table.addCell(createCellBorderNone(4, "Preferred Email"));
		return table; 
	}
	
	private PdfPTable createTableMentorIREP(MentorBean bean){
		 
		PdfPTable table;
        int colspan = 1; 
        float[] columnWidths = {33f, 33f, 33f, 1f};
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        String[] headerC = {"Current Mentor"," "," "," "}; 
        for(String header: headerC){
        	table.addCell(createCellBorderNone(colspan, header));
        }
         
        String[] header1 = {"First Name", "Last Name", "Institution", ""}; 
        String[] content1 = null; 
        if(bean == null){
        	content1 = new String[]{" "," "," "," "}; 
        }else{
        	content1 = new String[]{bean.getMentorFirstName(),bean.getMentorLastName(), 
        			bean.getMentorInstitution(), " "}; 
        }
        for(String address: content1){
            table.addCell(createCellBorderBottom(colspan, address));
        }
        for(String header: header1){
        	table.addCell(createCellBorderNone(colspan, header));
        }
        
        String[] header2 = {"Phone", "Email", "", ""}; 
        String[] content2 = {
        		bean == null ? " " : bean.getMentorPhone(),  
        		bean == null ? " " : bean.getMentorEmail(), " ", " "
        }; 
        for(String content: content2){
        	table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header2){
        	table.addCell(createCellBorderNone(colspan, content));
        }
         
        String[] headerI = {"International Mentor"," "," "," "}; 
        for(String header: headerI){
        	table.addCell(createCellBorderNone(colspan, header));
        }
        
        String[] content3 = {
        		bean == null ? " " : bean.getIntlMentorFirstName(), 
        		bean == null ? " " : bean.getIntlMentorLastName(), 
        		bean == null ? " " : bean.getIntlMentorInstitution(), " "
        }; 
        for(String address: content3){
            table.addCell(createCellBorderBottom(colspan, address));
        }
        for(String header: header1){
        	table.addCell(createCellBorderNone(colspan, header));
        }
         
        String[] header3 = {"Country", "Phone", "Email", ""}; 
        String[] content4 = {
        		bean == null ? " " : bean.getIntlMentorCountry(), 
        		bean == null ? " " : bean.getIntlMentorPhone(), 
        		bean == null ? " " : bean.getIntlMentorEmail(), " "
        }; 
        for(String content: content4){
        	table.addCell(createCellBorderBottom(colspan, content));
        }
        for(String content: header3){
        	table.addCell(createCellBorderNone(colspan, content));
        }
		return table; 
	}
	
	private PdfPTable createTableBudgetIREP(BudgetBean bean){
		 
		PdfPTable table;
        int colspan = 1; 
        float[] columnWidths = {25f, 25f, 25f, 25f};
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        
        String[] header = {"", "Total Expenses", "Current Funding", "Remaining Need"}; 
        for(String h: header){
        	table.addCell(createCellBorderNone(colspan, h));
        }
        
        String[] content1 = null; 
        if(bean == null){
        	content1 = new String[]{"Domestic Travel"," "," "," "}; 
        }else{
        	content1 = new String[]{"Domestic Travel", 
        			String.valueOf(bean.getTotalDomesticTravel()), 
        			String.valueOf(bean.getCurrentDomesticTravel()), 
        			String.valueOf(budgetSubstract(bean.getTotalDomesticTravel(), 
        					bean.getCurrentDomesticTravel()))}; 
        }
        for(String c: content1){
            table.addCell(createCellBorderBottom(colspan, c));
        }  
        
        String[] content2 = null; 
        if(bean == null){
        	content2 = new String[]{"Round Trip Airfare"," "," "," "}; 
        }else{
        	content2 = new String[]{"Round Trip Airfare", 
        			String.valueOf(bean.getTotalRoundTrip()), 
        			String.valueOf(bean.getCurrentRoundTrip()), 
        			String.valueOf(budgetSubstract(bean.getTotalRoundTrip(), 
        					bean.getCurrentRoundTrip()))}; 
        }
        for(String c: content2){
            table.addCell(createCellBorderBottom(colspan, c));
        }
        
        String[] content3 = null; 
        if(bean == null){
        	content3 = new String[]{"Visa"," "," "," "}; 
        }else{
        	content3 = new String[]{"Visa", 
        			String.valueOf(bean.getTotalVisa()), 
        			String.valueOf(bean.getCurrentVisa()), 
        			String.valueOf(budgetSubstract(bean.getTotalVisa(), 
        					bean.getCurrentVisa()))}; 
        }
        for(String c: content3){
            table.addCell(createCellBorderBottom(colspan, c));
        }
        
        String[] content4 = null; 
        if(bean == null){
        	content4 = new String[]{"Passport and Photos"," "," "," "}; 
        }else{
        	content4 = new String[]{"Passport and Photos", 
        			String.valueOf(bean.getTotalPassport()), 
        			String.valueOf(bean.getCurrentPassport()), 
        			String.valueOf(budgetSubstract(bean.getTotalPassport(), 
        					bean.getCurrentPassport()))}; 
        }
        for(String c: content4){
            table.addCell(createCellBorderBottom(colspan, c));
        }
        
        String[] content5 = null; 
        if(bean == null){
        	content5 = new String[]{"Immunizations"," "," "," "}; 
        }else{
        	content5 = new String[]{"Immunizations", 
        			String.valueOf(bean.getTotalImmunization()), 
        			String.valueOf(bean.getCurrentImmunization()), 
        			String.valueOf(budgetSubstract(bean.getTotalImmunization(), 
        					bean.getCurrentImmunization()))}; 
        }
        for(String c: content5){
            table.addCell(createCellBorderBottom(colspan, c));
        }
        
        String[] content6 = null; 
        if(bean == null){
        	content6 = new String[]{"Housing"," "," "," "}; 
        }else{
        	content6 = new String[]{"Housing", 
        			String.valueOf(bean.getTotalHousing()), 
        			String.valueOf(bean.getCurrentHousing()), 
        			String.valueOf(budgetSubstract(bean.getTotalHousing(), 
        					bean.getCurrentHousing()))}; 
        }
        for(String c: content6){
            table.addCell(createCellBorderBottom(colspan, c));
        }
        
        String[] content7 = null; 
        if(bean == null){
        	content7 = new String[]{"Communication"," "," "," "}; 
        }else{
        	content7 = new String[]{"Communication", 
        			String.valueOf(bean.getTotalCommunication()), 
        			String.valueOf(bean.getCurrentCommunication()), 
        			String.valueOf(budgetSubstract(bean.getTotalCommunication(), 
        					bean.getCurrentCommunication()))}; 
        }
        for(String c: content7){
            table.addCell(createCellBorderBottom(colspan, c));
        }
        
        String[] content8 = null; 
        if(bean == null){
        	content8 = new String[]{"Meals"," "," "," "}; 
        }else{
        	content8 = new String[]{"Meals", 
        			String.valueOf(bean.getTotalMeal()), 
        			String.valueOf(bean.getCurrentMeal()), 
        			String.valueOf(budgetSubstract(bean.getTotalMeal(), 
        					bean.getCurrentMeal()))}; 
        }
        for(String c: content8){
            table.addCell(createCellBorderBottom(colspan, c));
        }
        
        String[] content9 = null; 
        if(bean == null){
        	content9 = new String[]{"Miscellaneous (describe below)"," "," "," "}; 
        }else{
        	content9 = new String[]{"Miscellaneous (describe below)", 
        			String.valueOf(bean.getTotalMiscellaneous()), 
        			String.valueOf(bean.getCurrentMiscellaneous()), 
        			String.valueOf(budgetSubstract(bean.getTotalMiscellaneous(), 
        					bean.getCurrentMiscellaneous()))}; 
        }
        for(String c: content9){
            table.addCell(createCellBorderBottom(colspan, c));
        }
         
        String[] content10 = null; 
        if(bean == null){
        	content10 = new String[]{"Total"," "," "," "}; 
        }else{
        	BigDecimal t1 = null, t2 = null; 
        	if(bean.getTotalDomesticTravel() != null && 
        		bean.getTotalRoundTrip()!= null &&
				bean.getTotalVisa()!= null &&
				bean.getTotalPassport()!= null &&
				bean.getTotalImmunization()!= null && 
				bean.getTotalHousing()!= null && 
				bean.getTotalCommunication()!= null && 
				bean.getTotalMeal()!= null && 
				bean.getTotalMiscellaneous()!= null){
        		t1 = bean.getTotalDomesticTravel().add(bean.getTotalRoundTrip())
        				.add(bean.getTotalVisa())
        				.add(bean.getTotalPassport())
        				.add(bean.getTotalImmunization())
        				.add(bean.getTotalHousing())
        				.add(bean.getTotalCommunication())
        				.add(bean.getTotalMeal())
        				.add(bean.getTotalMiscellaneous()); 
        	}
        	if(bean.getCurrentDomesticTravel() != null && 
                bean.getCurrentRoundTrip()!= null &&
                        bean.getCurrentVisa()!= null &&
                        bean.getCurrentPassport()!= null &&
                        bean.getCurrentImmunization()!= null && 
                        bean.getCurrentHousing()!= null && 
                        bean.getCurrentCommunication()!= null && 
                        bean.getCurrentMeal()!= null && 
                        bean.getCurrentMiscellaneous()!= null){
                t2 = bean.getCurrentDomesticTravel().add(bean.getCurrentRoundTrip())
                        .add(bean.getCurrentVisa())
                        .add(bean.getCurrentPassport())
                        .add(bean.getCurrentImmunization())
                        .add(bean.getCurrentHousing())
                        .add(bean.getCurrentCommunication())
                        .add(bean.getCurrentMeal())
                        .add(bean.getCurrentMiscellaneous()); 
            }
        	content10 = new String[]{"Total", 
        			String.valueOf(t1), String.valueOf(t2), 
        			String.valueOf(budgetSubstract(t1, t2))}; 
        }
        for(String c: content10){
            table.addCell(createCellBorderBottom(colspan, c));
        }
		return table; 
	}
	
	private BigDecimal budgetSubstract(BigDecimal total, BigDecimal current){
		if(total == null || current == null)return null; 
		return total.subtract(current); 
	}
	
	private PdfPTable createTableOfficialUseCCCONF(String dateReceived){
		
		PdfPTable outerTable, innerTable; 
		PdfPCell outerCell, innerCell;
        Paragraph paragraph; 
        int colspan = 5; 
        float[] columnWidths = {30f, 3f, 24f, 3f, 40};
        outerTable = new PdfPTable(columnWidths);
        outerTable.setWidthPercentage(100);
        
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Date Received: ", regularFontBold)); 
        Chunk underline = new Chunk(dateReceived, regularFontBold);
        underline.setUnderline(0.1f, -2f);
        paragraph.add(underline);
        
        outerCell = new PdfPCell(new Phrase(paragraph));
        outerCell.setBorder(PdfPCell.NO_BORDER);
        outerCell.setColspan(colspan);
        outerCell.setPaddingBottom(10f);
        outerTable.addCell(outerCell);
        
        outerCell = new PdfPCell();
        outerCell.setPadding(0);
        outerCell.setBorder(PdfPCell.NO_BORDER);
        colspan = 2;
	    innerTable = new PdfPTable(colspan); 
	    innerTable.setWidthPercentage(100);
	    
	    innerCell = new PdfPCell(new Phrase("Item", regularFontBold));
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        innerTable.addCell(innerCell);
        
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Complete (", regularFontBold));
        paragraph.add(new Chunk(CHECK_MARK, segoeUIFont));
        paragraph.add(new Chunk(")", regularFontBold));
        innerCell = new PdfPCell(new Phrase(paragraph));
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        innerTable.addCell(innerCell);
        
        innerCell = new PdfPCell(new Phrase("Ethnicity", regularFontBold));
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
        innerCell = new PdfPCell();
        innerTable.addCell(innerCell);
        
        innerCell = new PdfPCell(new Phrase("STEM major", regularFontBold));
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
        innerCell = new PdfPCell();
        innerTable.addCell(innerCell);
        
        innerCell = new PdfPCell(new Phrase("Transcript", regularFontBold));
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
        innerCell = new PdfPCell();
        innerTable.addCell(innerCell);
        
        innerCell = new PdfPCell(new Phrase("30 hours completed", regularFontBold));
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
        innerCell = new PdfPCell();
        innerTable.addCell(innerCell);
         
        innerCell = new PdfPCell(new Phrase("GPA", regularFontBold));
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
        innerCell = new PdfPCell();
        innerTable.addCell(innerCell); 
        paragraph = new Paragraph();
        paragraph.add(new Chunk("Part time ", regularFontBold));
        paragraph.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        paragraph.add(new Chunk(Chunk.TABBING));
        paragraph.add(new Chunk("Full time ", regularFontBold));
        paragraph.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        innerCell = new PdfPCell(new Phrase(paragraph));
        innerCell.setColspan(colspan);
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
        
        paragraph = new Paragraph();
        paragraph.add(new Chunk("Stipend eligible:", regularFontBold));
        paragraph.add(new Chunk(Chunk.TABBING));
        paragraph.add(new Chunk("Yes ", regularFontBold));
        paragraph.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        
        paragraph.add(new Chunk("    No ", regularFontBold));
        paragraph.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        paragraph.add(new Chunk(Chunk.NEWLINE));
        paragraph.add(new Chunk("Authorized by:", regularFontBold));
        paragraph.add(new Chunk(Chunk.TABBING));
        paragraph.add(new Chunk("____________________", regularFontBold));
        innerCell = new PdfPCell(new Phrase(paragraph));
        innerCell.setPaddingBottom(5);
        innerCell.setColspan(colspan);
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
         
        outerCell.addElement(innerTable);
        outerTable.addCell(outerCell);
         
        
        outerCell = new PdfPCell();
        outerCell.setBorder(PdfPCell.NO_BORDER);
	    outerTable.addCell(outerCell);
  
	    outerCell = new PdfPCell();
        outerCell.setPadding(0);
        outerCell.setBorder(PdfPCell.NO_BORDER);
        colspan = 1; 
	    innerTable = new PdfPTable(colspan); 
	    innerTable.setWidthPercentage(100);
        
        innerTable.addCell(new PdfPCell(new Phrase(" ", regularFontBold)));
        
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Citizenship: ", regularFontBold));
        paragraph.add(new Chunk("Yes ", regularFontBold));
        paragraph.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        paragraph.add(new Chunk("    No ", regularFontBold));
        paragraph.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        innerCell = new PdfPCell(new Phrase(paragraph));
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
        
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("If no, is a copy of US Permanent Resident Card attached? ", regularFontBold));
        paragraph.add(new Chunk("Yes ", regularFontBold));
        paragraph.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        paragraph.add(new Chunk("    No ", regularFontBold));
        paragraph.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        innerCell = new PdfPCell(new Phrase(paragraph));
        innerCell.setLeading(0f, 1.5f);
        innerCell.setPaddingBottom(10f);
        innerTable.addCell(innerCell);
        
        innerCell = new PdfPCell(new Phrase(Chunk.NEWLINE));
        innerTable.addCell(innerCell);
        
        innerCell = new PdfPCell(new Phrase(Chunk.NEWLINE));
        innerCell.setPaddingBottom(17f);
        innerTable.addCell(innerCell);
      
	    outerCell.addElement(innerTable);
	    outerTable.addCell(outerCell);

	    outerCell = new PdfPCell();
        outerCell.setBorder(PdfPCell.NO_BORDER);
	    outerTable.addCell(outerCell);
	    
        outerTable.addCell(new Phrase(" Comments: ", regularFontBold));
         
        return outerTable; 
	}
	private PdfPTable createTableOfficialUseMESA(String dateReceived){
		
		PdfPTable outerTable, innerTable; 
		PdfPCell outerCell, innerCell;
        Paragraph paragraph; 
        int colspan = 5; 
        float[] columnWidths = {35f, 3f, 45f, 17f};
        outerTable = new PdfPTable(columnWidths);
        outerTable.setWidthPercentage(100);
 
        outerCell = new PdfPCell();
        outerCell.setPadding(0);
        outerCell.setBorder(PdfPCell.NO_BORDER);
        colspan = 2;
	    innerTable = new PdfPTable(colspan); 
	    innerTable.setWidthPercentage(100);
        
        innerCell = new PdfPCell(new Phrase("Date Application Received", regularFont));
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
        innerCell = new PdfPCell(new Phrase(dateReceived, regularFont));
        innerTable.addCell(innerCell);
        
        innerCell = new PdfPCell(new Phrase("Questionnaire Completed", regularFont));
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
        innerCell = new PdfPCell(new Phrase(dateReceived, regularFont));
        innerTable.addCell(innerCell);
        
        innerCell = new PdfPCell(new Phrase("Transcript Attached", regularFont));
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
        innerCell = new PdfPCell(new Phrase(dateReceived, regularFont));
        innerTable.addCell(innerCell);
        //
        paragraph = new Paragraph(); 
        paragraph.add(new Chunk("Award Offered?", regularFontBold));
        paragraph.add(new Chunk(Chunk.TABBING));
        paragraph.add(new Chunk("Yes ", regularFontBold));
        paragraph.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        
        paragraph.add(new Chunk("    No ", regularFontBold));
        paragraph.add(new Chunk(BOX_UNCHECK, segoeUIFont));
        paragraph.add(new Chunk(Chunk.NEWLINE));
        paragraph.add(new Chunk("Authorized by:", regularFontBold));
        paragraph.add(new Chunk(Chunk.TABBING));
        paragraph.add(new Chunk("____________________", regularFontBold));
        innerCell = new PdfPCell(new Phrase(paragraph));
        innerCell.setPaddingBottom(5);
        innerCell.setColspan(colspan);
	    innerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        innerTable.addCell(innerCell);
         
        outerCell.addElement(innerTable);
        outerTable.addCell(outerCell);
         
        outerCell = new PdfPCell();
        outerCell.setBorder(PdfPCell.NO_BORDER);
	    outerTable.addCell(outerCell);
  
        outerTable.addCell(new Phrase(" Math Courses Completed and Grades: ", regularFontBold));
         
		outerCell = new PdfPCell();
		outerCell.setBorder(PdfPCell.NO_BORDER);
		outerTable.addCell(outerCell);
        return outerTable; 
	}
	
	
	/* Utilities */ 
	private Chunk addLineBreak(){
		LineSeparator separator = new LineSeparator();
        separator.setOffset(-3);
        Chunk linebreak = new Chunk(separator);
        return linebreak; 
	}
	
	private PdfPCell createCellFixed(float height, StringBuilder content){
		PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(height);
        cell.setPaddingLeft(5);
        cell.setPaddingRight(5);
        cell.setPaddingTop(0);
        cell.setPaddingBottom(0); 
        cell.addElement(new Paragraph(content.toString(), regularFont));
        return cell; 
	}
	
	private PdfPCell createCellBorder(String content){
		PdfPCell cell = new PdfPCell();
		cell.setBorder(PdfPCell.NO_BORDER);
        cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
        Phrase phrase = new Phrase(); 
        if(Parse.isBlankOrNull(content)){
        		phrase.add(new Chunk(" ", regularFont)); 
		}else {
			cell.setPaddingLeft(5);
            cell.setPaddingRight(5);
            cell.setPaddingTop(5);
            cell.setPaddingBottom(5); 
			phrase.add(new Chunk(content, regularFont)); 
		}
        cell.addElement(phrase);
		return cell; 
	}
	
	private PdfPCell createCellBorderBottom(int colspan, String content){
		PdfPCell cell = new PdfPCell();
		cell.setColspan(colspan);
		cell.setBorder(PdfPCell.NO_BORDER);
        cell.setCellEvent(new CustomBorder(null, null, null, solid));
		if(content != null && !content.isEmpty()){
			Phrase phrase = new Phrase(); 
	        phrase.add(new Chunk(content, regularFont)); 
	        cell.addElement(phrase);
		}
		return cell; 
	}
	
	private PdfPCell createCellBorderNone(int colspan, String content){
		PdfPCell cell;
		
        cell = new PdfPCell(new Phrase(content, regularFontBold));
        cell.setColspan(colspan);
        cell.setBorder(PdfPCell.NO_BORDER);
		return cell; 
	}
	
	private Paragraph addCheckBox(String checked, Integer precondition, HashMap<String, String> map){
		Paragraph phrase = new Paragraph(); 
		if(checked == null || precondition == null){
			for(String choice: map.keySet()){
				phrase.add(new Chunk(BOX_UNCHECK, segoeUIFont));
				phrase.add(new Chunk(" " + map.get(choice) + "    ", regularFont)); 
			}
		}else{
			for(String choice: map.keySet()){
				if(precondition == 1 && choice.equalsIgnoreCase(checked)){
		        	phrase.add(new Chunk(BOX_CHECK, segoeUIFont));
		        }else{
		        	phrase.add(new Chunk(BOX_UNCHECK, segoeUIFont));
		        }
				phrase.add(new Chunk(" " + map.get(choice) + "    ", regularFont)); 
			}
		}
		return phrase; 
	}

	 
	/**
	 * merge/combine application form and transcript with PdfCopy
	 */
	public void doMerge(List<byte[]> list, OutputStream outputStream)
            throws DocumentException, IOException {
        Document document = new Document();
        PdfCopy pdfCopy = new PdfCopy(document, outputStream); 
	    pdfCopy.setCloseStream(false);
	    document.open();
 
	   int n = 0; 
	   for (byte[] in : list) {
		   PdfReader reader = new PdfReader(in); 
		   PdfReader.unethicalreading = true; 
		  
		   n += reader.getNumberOfPages();
	   }
	   int prev = 0; 
	   
	   for (byte[] in : list) {
		   PdfReader reader = new PdfReader(in); 
		   PdfReader.unethicalreading = true; 
		   int i = 0; 
		   for(; i < reader.getNumberOfPages();){
				PdfImportedPage page = pdfCopy.getImportedPage(reader, ++i); 
				PageStamp stamp = pdfCopy.createPageStamp(page); 
				ColumnText.showTextAligned(stamp.getUnderContent(), 
						Element.ALIGN_CENTER, 
						new Phrase(String.format("Page %d of %d", i+prev, n), regularFontBold), 
						rectangle.getRight() - 90.5f, rectangle.getBottom() + 18, 0); // 297.5f, 28
				stamp.alterContents();
				pdfCopy.addPage(page);
			}
		   prev = i; 
	   }

        outputStream.flush();
        document.close();
        outputStream.close();
    }

	private static final String[] ESSAY_CCCONF_HEADER = {
		"1) Recount a significant experience that influenced you to decide you wanted a college degree.",
		"2) What semester/year do you plan to transfer to a four-year university, and to which institution do think you will transfer",
		"3) What do you plan to do when you finish your B.S. degree?",
		"4) How do you think this Professional Development Workshop will help you reach your goals, and how else could New Mexico AMP assist you?"
	}; 
	
	private static final String[] ESSAY_MESA_HEADER = {
		"1) What attracted you to your intended field of study and what are your professional goals?",
		"2) What is the academic pathway that you envision for yourself, and how will this particular pathway prepare you to accomplish your professional goals?", 
		"3) What events and individuals have been critical in influencing your academic and career decisions, and how specifically have these events individuals shaped your decisions?"
	}; 
	
	private static final String[] ESSAY_SCCORE_HEADER = {
		"1) Identify your major and field of study, and explain why you chose that field and how it fits your personal and professional interests and goals.",
		"2) What are your educational goals (B.S., Masters, Ph.D.) and what are the reasons for these goals?",
		"3) As part of the SCCORE program, you will be working on a research project with a faculty mentor. "
		+"Describe the type of research project you would like to work on, if available, as your first choice. "
		+"Also, list an alternative research interest (2nd choice) if we cannot locate a project in your preferred interest area. "
		+"(Keep in mind that we try very hard to identify a mentor in your major/interest; "
		+"however, if one is not available, we will place you with a mentor/project that is as close to your interest as possible.)",
		"4) Describe the strengths and skills that you will bring to your research project.",
		"5) Describe what you hope to gain from the SCCORE experience. Consider both academic and professional development."
	}; 
	
	private static final String[] ESSAY_TRANS_HEADER = {
		"1) What are your profesional goals and what have you done toward reaching those goals?",
		"2) Did you discover your field of interest on your own, or did you receive counseling from a teacher or other individual?",
		"3) As a community college student, what has been the most important factor in your decision to transfer to a university to pursue a BS degree?",
		"4) Do you have a mentor at your community college? Who is that person and why do you consider them your mentor?",
		"5) Aside from financial support, what are the most important ways in which New Mexico AMP and others can support your transfer intentions?"
	}; 
	
	private static final String[] ESSAY_URS_HEADER = {
		"Please describe why you are applying for the scholarship, your qualifications, and your educational and career goals. Please limit your statement to less than 500 words."
	};
	
	private static final String[] PROJECT_URS_HEADER = {
		"1) State briefly your project's goals and objectives:",
		"2) Provide a review of the methods to be used:",
		"3) Indicate the results, benefits, or information expected to be gained from the project:",
		"4) Identify three primary tasks/outcomes the student will complete or accomplish during the semester:"
	}; 
	
	/* URS: create application PDF for mentor evaluation */
	public byte[] buildPdfDocumentEvaluation(String evalPoint, List<String> eval) 
			throws DocumentException, IOException{
		if(evalPoint.equalsIgnoreCase(ProgramCode.EVALUATION_POINT.MIDTERM.toString())){
			return buildPdfDocumentEvaluationMidTerm(eval); 
		}else if(evalPoint.equalsIgnoreCase(ProgramCode.EVALUATION_POINT.ENDOFSEMESTER.toString())){
			return buildPdfDocumentEvaluationEnd(eval); 
		}
		return null; 
	}
	private byte[] buildPdfDocumentEvaluationMidTerm(List<String> eval)
			throws DocumentException, IOException{
		 
		String semester = eval.get(0); 
		
		Document document = new Document(rectangle); 
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, byteStream);
		document.open();
		document.add(createHeaderEvaluation("Mid Term Evaluation Form", semester, "Mentor"));
		 
        document.add(createTableAEvaluation(new String[]{
        		eval.get(eval.size()-1), eval.get(3)+" "+eval.get(4), eval.get(6), 
        	eval.get(7)+" "+eval.get(8), eval.get(2)
        })); 
        
        document.add(addLineBreak());
         
        document.add(createCategory("Part I. Please evaluate your Undergraduate Research Scholars' (URS) performance relative to each question. Please answer using the below rating scale:"));
		 
     
        Paragraph paragraph = new Paragraph() ;
        paragraph.setLeading(LINE_LEADING); 
        paragraph.add(new Chunk("Rating Scale: ", regularFont)); 
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk("4 = Strongly Agree", regularFont));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk("3 = Mostly Agree", regularFont));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk("2 = Agree", regularFont));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk("1 = Mostly Disagree", regularFont));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk("0 = Strongly Disagree", regularFont));
        paragraph.add(Chunk.NEWLINE); paragraph.add(Chunk.NEWLINE);
        document.add(paragraph); 
        
        document.add(createTableMidEvaluationRating( 
        new String[] {"1. Student reports to work on time and as scheduled",
        		"2. Student has worked steadily to this point of the project period",
        		"3. Student completes his/her assignments in a timely fashion", 
        		"4. Student demonstrates motivation and willingness to take initiative and responsibility for the project",
        		"5. In my opinion, the student is gaining new knowledge and research skills", 
        		"6. Student is meeting with me:"}, 
        		new String[]{
        	eval.get(9)==null?"NA":eval.get(9),eval.get(10)==null?"NA":eval.get(10),
        	eval.get(11)==null?"NA":eval.get(11),eval.get(12)==null?"NA":eval.get(12) ,
        			eval.get(13)==null?"NA":eval.get(13), eval.get(14)
        })); 
        
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("7. Please provide a brief assessment of your URS student’s successes from the start of the semester to now.", regularFontBold));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk(eval.get(15), regularFont)); paragraph.add(Chunk.NEWLINE);
       
        paragraph.add(new Chunk("8. Please list any conferences attended and/or presentations the student has participated in (or plans to participate in).", regularFontBold));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk(eval.get(16), regularFont));paragraph.add(Chunk.NEWLINE);
        
        paragraph.add(new Chunk("9. Please comment on any disappointments/and/or concerns with the student and/or program you wish to include here.", regularFontBold));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk(eval.get(17), regularFont));paragraph.add(Chunk.NEWLINE);
        
        paragraph.add(new Chunk("10.New Mexico AMP can assist my URS student by doing the following: ", regularFontBold));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk(eval.get(18), regularFont));paragraph.add(Chunk.NEWLINE);
        document.add(paragraph); 
       
		document.close();
	    return byteStream.toByteArray();
	}
	public byte[] buildPdfDocumentEvaluationEnd(List<String> eval)
			throws DocumentException, IOException{
		String semester = eval.get(0); 
		
		Document document = new Document(rectangle); 
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, byteStream);
		document.open();
		document.add(createHeaderEvaluation("End Of Semester Evaluation Form", semester, "Mentor"));
		 
        document.add(createTableAEvaluation(new String[]{
        		eval.get(eval.size()-1), eval.get(3)+" "+eval.get(4), eval.get(6), 
        	eval.get(7)+" "+eval.get(8), eval.get(2)
        })); 
        
        document.add(addLineBreak());
        
        document.add(createCategory("Part I. Please evaluate your Undergraduate Research Scholars' (URS) performance compared to the beginning of the semester. Please answer using the below rating scale:"));
		 
        Paragraph paragraph = new Paragraph() ;
        paragraph.setLeading(LINE_LEADING); 
        paragraph.add(new Chunk("Rating Scale: ", regularFont)); 
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk("4 = Strongly Agree", regularFont));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk("3 = Mostly Agree", regularFont));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk("2 = Agree", regularFont));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk("1 = Mostly Disagree", regularFont));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk("0 = Strongly Disagree", regularFont));
        paragraph.add(Chunk.NEWLINE);
        document.add(paragraph); 
      
        document.add(new Paragraph("Lab/Field skills", regularFontBold));
        document.add(createTableMidEvaluationRating( 
        new String[] {"1. Skill with using lab/field equipment increased?",
        		"2. Greater ability to design and conduct tests increased?",
        		"3. Ability to analyze testing results increased?"}, 
        		new String[]{ eval.get(9)==null?"NA":eval.get(9),
        		eval.get(10)==null?"NA":eval.get(10), eval.get(11)==null?"NA":eval.get(11) 
        })); 
        
        document.add(new Paragraph("Organization/Time Management skills", regularFontBold));
        document.add(createTableMidEvaluationRating( 
        new String[] {"1. Satisfactory achievement of goals set at the beginning of semester was made?",
        		"2. Academic workload with URS responsibilities was balanced?",
        		"3. Record keeping and data gathering was properly maintained?",
        		"4. Satisfactory time was spent on the research project?"}, 
        		new String[]{ eval.get(12)==null?"NA":eval.get(12),
        		eval.get(13)==null?"NA":eval.get(13), eval.get(14)==null?"NA":eval.get(14), 
        				eval.get(15)==null?"NA":eval.get(15)
        })); 
        
        document.add(new Paragraph("Independent thinking skills to solve research problems", regularFontBold));
        document.add(createTableMidEvaluationRating( 
        new String[] {"1. Creative thinking increased?",
        		"2. Student gained improved decision making skills?"}, 
        		new String[]{ eval.get(16)==null?"NA":eval.get(16),
        		eval.get(17)==null?"NA":eval.get(17) 
        })); 
        
        document.add(new Paragraph("Writing/presentation skills", regularFontBold));
        document.add(createTableMidEvaluationRating( 
        new String[] {"1. Writing skills enhanced?", "2. Presentation skills enhanced?"}, 
        		new String[]{ eval.get(18)==null?"NA":eval.get(18),
        		eval.get(19)==null?"NA":eval.get(19) 
        }));
        
        document.add(createCategory("Part II. Please answer the following questions concerning your URS student: "));
        
        paragraph = new Paragraph() ;
        paragraph.add(new Chunk("1. What techniques/tools are you using to motivate your student to consider graduate school (if not already stated in Midterm Review or has changed)?", regularFontBold));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk(eval.get(20), regularFont)); 
        paragraph.add(Chunk.NEWLINE);
       
        paragraph.add(new Chunk("2. Please list any co-authored publications your student is working on including the date it should be published, journal name/volume, and title (if known at this time):", regularFontBold));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk(eval.get(21), regularFont));
        paragraph.add(Chunk.NEWLINE);
        
        paragraph.add(new Chunk("3. Please list any conferences or presentations the student has participated in this semester:", regularFontBold));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk(eval.get(22), regularFont));
        paragraph.add(Chunk.NEWLINE);
        
        paragraph.add(new Chunk("4. Please provide a summary assessment of your URS student’s progress for this semester:", regularFontBold));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk(eval.get(23), regularFont));
        paragraph.add(Chunk.NEWLINE);
        document.add(paragraph); 
        
		document.close();
	    return byteStream.toByteArray();
	}
	private Paragraph createHeaderEvaluation(String evalPoint, String semester, String who){
		Paragraph preface = new Paragraph(); 
		preface.add(new Chunk("NEW MEXICO AMP", subFont)); 
		preface.add(Chunk.NEWLINE); 
		preface.add(new Chunk("Undergraduate Research Scholars (URS)", mediumFontBold)); 
		preface.add(Chunk.NEWLINE); 
		preface.add(new Chunk(evalPoint, mediumFontBold)); 
		preface.add(Chunk.NEWLINE);
		preface.add(new Chunk("Semester: " + semester, mediumFontBold));
		preface.add(Chunk.NEWLINE);
		preface.add(new Chunk("(Completed by " + who + ")", regularFont));
		preface.setAlignment(Element.ALIGN_CENTER);
		 
		return preface; 
	}
	private PdfPTable createTableAEvaluation(String[] rowContent){
    	
		PdfPTable table; 
		PdfPCell cell;
        float[] columnWidths = {20f, 80f};
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        
        String[] rowHeader = {"Submit Date: ", "Mentor Name: ", "Department: ", "Name of URS Student: ", "Project Title: "}; 
        for(int i = 0; i < rowHeader.length; i ++){
        	Phrase phrase = new Phrase(rowHeader[i], regularFontBold); 
        	cell = new PdfPCell(phrase);
        	cell.setBorder(PdfPCell.NO_BORDER);
        	cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
            
            phrase = new Phrase(rowContent[i], regularFont); 
            cell = new PdfPCell(phrase);
        	cell.setBorder(PdfPCell.NO_BORDER);
        	cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
        return table; 
	}
	private PdfPTable createTableMidEvaluationRating(String[] rowHeader, String[] rowContent){
    	
		PdfPTable table; 
		PdfPCell cell;
        float[] columnWidths = {60f, 40f};
        table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        
        for(int i = 0; i < rowHeader.length; i ++){
        	Phrase phrase = new Phrase(rowHeader[i], regularFont); 
        	cell = new PdfPCell(phrase);
        	//cell.setPadding(0);
        	cell.setBorder(PdfPCell.NO_BORDER);
        	cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
            
            phrase = new Phrase(rowContent[i], regularFont); 
            cell = new PdfPCell(phrase);
        	cell.setBorder(PdfPCell.NO_BORDER);
        	cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
        
        return table; 
	}

	/* URS: create application PDF for mentor evaluation */
	public byte[] buildPdfDocumentEvaluationStu(String evalPoint, List<String> eval) 
			throws DocumentException, IOException{
		if(ProgramCode.EVALUATION_POINT.MIDTERM.toString().equalsIgnoreCase(evalPoint)){
			return buildPdfDocumentEvaluationMidTermStu(eval); 
		}else if(evalPoint.equalsIgnoreCase(ProgramCode.EVALUATION_POINT.ENDOFSEMESTER.toString())){
			//return buildPdfDocumentEvaluationEnd(eval); 
		}
		return null; 
	}
	private byte[] buildPdfDocumentEvaluationMidTermStu(List<String> eval)
			throws DocumentException, IOException{
		String semester = eval.get(0); 
		Document document = new Document(rectangle); 
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, byteStream);
		document.open();
		document.add(createHeaderEvaluation("Mid Term Evaluation Form", semester, "Student"));
		
	    document.add(createTableAEvaluation(new String[]{
	    		eval.get(eval.size()-1), eval.get(3)+" "+eval.get(4), eval.get(6), 
	    	eval.get(7)+" "+eval.get(8), eval.get(2)
	    })); 
	    
	    document.add(addLineBreak());
	    
	    Paragraph paragraph = new Paragraph();
	    paragraph.add(new Chunk("Question 1. Please provide a summary of the progress you have made on your project through now.", regularFontBold));
	    paragraph.add(Chunk.NEWLINE);
	    paragraph.add(new Chunk(eval.get(9), regularFont)); 
	    paragraph.add(Chunk.NEWLINE); paragraph.add(Chunk.NEWLINE);
	   
	    paragraph.add(new Chunk("Question 2. Please describe the working relationship with your mentor.", regularFontBold));
	    paragraph.add(Chunk.NEWLINE);
	    paragraph.add(new Chunk(eval.get(10), regularFont));
	    paragraph.add(Chunk.NEWLINE); paragraph.add(Chunk.NEWLINE);
	    
	    paragraph.add(new Chunk("Question 3. As the result of participation on the research project (to this point), I plan to ... or I have learned ...", regularFontBold));
	    paragraph.add(Chunk.NEWLINE);
	    paragraph.add(new Chunk(eval.get(11), regularFont));
	    paragraph.add(Chunk.NEWLINE); paragraph.add(Chunk.NEWLINE);
	    document.add(paragraph);  
		document.close(); 
	    return byteStream.toByteArray();
	}
	 
}
