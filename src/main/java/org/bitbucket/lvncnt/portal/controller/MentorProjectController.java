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
package org.bitbucket.lvncnt.portal.controller;

import org.bitbucket.lvncnt.portal.model.*;
import org.bitbucket.lvncnt.portal.service.ApplicationProjectValidator;
import org.bitbucket.lvncnt.portal.service.MailService;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.portal.service.SignatureValidator;
import org.bitbucket.lvncnt.portal.dao.*;
import org.bitbucket.lvncnt.utilities.Parse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Date;

@Controller
@RequestMapping("/mentor")
public class MentorProjectController {
	
	private static final String VIEW_PROJECT = "mentor/project-manage"; 
	private static final String VIEW_PROJECT_EDIT = "mentor/project-edit";
	private static final String SECTION_PROJECT = "project.do"; 
	private static final String SECTION_SUBMIT = "submit.do"; 
	
	@Value("${URL.mynmamp.base}")
	private String mynmampBaseURL; 
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	StudentDAOImpl studentDAO;
	
	@Autowired
	MentorDAOImpl mentorDAO;
	
	@Autowired
	AdminDAOImpl adminDAO;
	
	@Autowired
    ApplicationProjectValidator applicationProjectValidator;
	
	@Autowired
    SignatureValidator signatureValidator;
	
	@InitBinder("projectBean")
	protected void initBinderProjectBean(WebDataBinder binder){
		binder.setValidator(applicationProjectValidator);
	}
	
	@GetMapping("/project")
	public String getProjects(ModelMap model, 
			@AuthenticationPrincipal UserImpl mentor){
		if(!model.containsAttribute("menteeList")){
			model.addAttribute("menteeList",  mentorDAO.getMenteeList(mentor.getUserID()));
		} 
		model.addAttribute("status", model.get("status"));
		return VIEW_PROJECT; 
	}
	 
	@GetMapping("/project/edit/{schoolSemester}/{schoolYear}/{applicationID}")
	public String getEditProject(ModelMap model, @AuthenticationPrincipal UserImpl mentor, 
			@PathVariable("schoolSemester") String schoolSemester, 
			@PathVariable("schoolYear") int schoolYear, 
			@PathVariable("applicationID") int applicationID, 
			RedirectAttributes redirectAttributes){
		boolean isProfileComplete = ((UserDAOImpl)userDAO).isMentorProfileComplete(mentor.getUserID());
		if(!isProfileComplete){
			redirectAttributes.addFlashAttribute("status", "Please complete the Profile!");
			return "redirect:/mentor/project";
		}
		
		ApplicationBean applicationBean = mentorDAO.getMenteeBean(mentor.getUserID(), applicationID);
		if(!model.containsAttribute("applicationBean")){
			model.addAttribute("applicationBean", applicationBean); 
		}
		 
		int menteeID = applicationBean.getUserID(); 
		// check if application already completed, ==> disable all buttons 
		// project proposal cannot be changed after the application has been completed by student
		
		ProgramBean standard = adminDAO.getApplicationWindow(ProgramCode.URS,
				applicationBean.getSchoolSemester(), String.valueOf(applicationBean.getSchoolYear())); 
		// check if application season exist or if exist, is within application window
		// check if application already completed, ==> disable all buttons
		Date current = new Date();
		if(standard == null || 
			!(current.after(standard.getStartDate()) && current.before(standard.getEndDate()))){
			model.addAttribute("tag", true); 
		}else if(studentDAO.isApplicationCompleteByCompleteDate(menteeID, applicationID)) {
			model.addAttribute("tag", true); 
		}else {
			model.addAttribute("tag", false); 
		}
		 
		SignatureBean signatureBean = new SignatureBean();
		if(!model.containsAttribute("projectBean")){
			ProjectBean projectBean = mentorDAO.getProjectBean(menteeID, applicationID);
			if(projectBean == null){
				projectBean = new ProjectBean(); 
			}else if(projectBean.getMentorSignature() != null){
				signatureBean.setSignature(projectBean.getMentorSignature());
				signatureBean.setSignatureDate(projectBean.getMentorSignatureDate());
			}else{
				signatureBean.setSignatureDate(Parse.tryParseDateMDY(
						Parse.FORMAT_DATE_MDY.format(new Date())));
			}
			model.addAttribute("projectBean", projectBean); 
		}
		
		if(!model.containsAttribute("signatureBean")){
			model.addAttribute("signatureBean", signatureBean);
		}
	 
		model.addAttribute("yesno", ProgramCode.YES_NO);
		
		model.addAttribute("status", model.get("status")); 
		model.addAttribute("section", model.get("section")); 
		 
		return VIEW_PROJECT_EDIT; 
	}
	 
	@PostMapping("/project/edit/{schoolSemester}/{schoolYear}/{applicationID}/project.do")
	public String setProjectProposal(ModelMap model,
			@ModelAttribute("projectBean") @Valid ProjectBean projectBean,  
			BindingResult bindingResult, Principal principal, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			@PathVariable("schoolSemester") String schoolSemester, @PathVariable("schoolYear") int schoolYear,
			@PathVariable("applicationID") int applicationID, RedirectAttributes redirectAttributes){
		  
		if(bindingResult.hasErrors()){
 			redirectAttributes.addFlashAttribute("section", SECTION_PROJECT);
			String FORM_NAME = "projectBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("projectBean", projectBean);
		}else{
			UserImpl mentor = (UserImpl) ((Authentication) principal).getPrincipal();
			ApplicationBean applicationBean = mentorDAO.getMenteeBean(mentor.getUserID(), applicationID);
			int result = studentDAO.updateProjectBean(applicationBean.getUserID(), applicationID, projectBean); 
			if(result != 0){
				status = "Project proposal updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
		}
		  
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_PROJECT); 
		return String.format("redirect:/mentor/project/edit/%s/%s/%s",
				schoolSemester,schoolYear,applicationID); 
	}
 
	@PostMapping("/project/edit/{schoolSemester}/{schoolYear}/{applicationID}/submit.do")
	public String submitProjectProposal(ModelMap model, @AuthenticationPrincipal UserImpl mentor, 
			@ModelAttribute("signatureBean") SignatureBean signatureBean,  
			BindingResult bindingResult,  
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			@PathVariable("schoolSemester") String schoolSemester, @PathVariable("schoolYear") int schoolYear,
			@PathVariable("applicationID") int applicationID, RedirectAttributes redirectAttributes){
		
		signatureValidator.validate(signatureBean, bindingResult);
		if(bindingResult.hasErrors()){
 			redirectAttributes.addFlashAttribute("section", SECTION_SUBMIT);
			String FORM_NAME = "signatureBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("signatureBean", signatureBean);
			return String.format("redirect:/mentor/project/edit/%s/%s/%s",
					schoolSemester,schoolYear,applicationID); 
		}
		
		// check if project filled completely
		// save mentor submit date in table_applist, 
		// save mentor submit sign and date in table_urs,
		// send email to applicant 
		if(!mentorDAO.isProjectComplete(applicationID)){
			status = "Missing required information!"; 
			redirectAttributes.addFlashAttribute("status", status); 
			redirectAttributes.addFlashAttribute("section", SECTION_PROJECT);
			return String.format("redirect:/mentor/project/edit/%s/%s/%s",
					schoolSemester,schoolYear,applicationID);
		}
		
		int result = mentorDAO.submitProject(applicationID, signatureBean); 
		if(result == 0){
			redirectAttributes.addFlashAttribute("status", "Error occurred during submission!"); 
			return String.format("redirect:/mentor/project/edit/%s/%s/%s",
					schoolSemester,schoolYear,applicationID);
		}else{
			ApplicationBean applicationBean = mentorDAO.getMenteeBean(mentor.getUserID(), applicationID);
			MentorBean mentorBean = new MentorBean();
			mentorBean.setMentorFirstName(mentor.getFirstName());
			mentorBean.setMentorLastName(mentor.getLastName());
			applicationBean.setMentorBean(mentorBean);
			String menteeEmail = applicationBean.getEmail(); 
			
			ProgramBean standard = adminDAO.getApplicationWindow(ProgramCode.URS,
					schoolSemester, String.valueOf(schoolYear)); 
			String deadline = Parse.FORMAT_DATE_YMD.format(standard.getEndDate());
			boolean success = mailService.sendMailProjectProposalToMentee(applicationBean, deadline);
			if(success){
				status = "Project proposal submitted successfully! An email was sent to " + menteeEmail; 
			}
			
 			redirectAttributes.addFlashAttribute("status", status); 
 			return "redirect:/mentor/project"; 
		}
		  
	} 
	
	
}
