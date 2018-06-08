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

import org.bitbucket.lvncnt.portal.dao.AdminDAOImpl;
import org.bitbucket.lvncnt.portal.dao.StudentDAOImpl;
import org.bitbucket.lvncnt.portal.dao.UserDAO;
import org.bitbucket.lvncnt.portal.dao.UserDAOImpl;
import org.bitbucket.lvncnt.portal.model.*;
import org.bitbucket.lvncnt.portal.service.MailService;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.portal.service.SignatureValidator;
import org.bitbucket.lvncnt.utilities.Parse;
import org.bitbucket.lvncnt.utilities.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Controller
@RequestMapping("/")
@PropertySource("classpath:mail.properties") 
public class ProjectController {
	
	private static final String SECTION_PROJECT = "project.do";
	private static final String VIEW_PAGE_NOT_FOUND = "error/404"; 
	private static final String VIEW_PROJECT_REVIEW = "index/projects-review"; 
	
	@Value("${URL.mynmamp.base}")
	private String mynmampBaseURL; 
	
	@Autowired
	private MailService mailService;
 
	@Autowired
    StudentDAOImpl studentDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private AdminDAOImpl adminDAO;
	
	@Autowired
    SignatureValidator signatureValidator;
 
	@PostMapping("/application/project-irep/send.do/{appcode}")
	public RedirectView sendProjectAbstractIREP(ModelMap model, 
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status, 
			@ModelAttribute("schoolSemester") String schoolSemester, 
			@ModelAttribute("schoolYear") String schoolYear,
			@PathVariable String appcode, 
			RedirectAttributes redirectAttributes){
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.IREP.equals(_appcode[0])){
			redirectView.setUrl("/application");
			return redirectView; 
		} 
		String applicationID = _appcode[1];
		
		// check if mentor info and project abstract are filled in 
		MentorBean mentorBean = studentDAO.isMentorAndProjectFilledIREP(user.getUserID(), applicationID);
		if(mentorBean == null){
			status = "Missing mentor information or project abstract!"; 
		}else{
			ProgramBean standard = adminDAO.getApplicationWindow(ProgramCode.IREP,
					schoolSemester, String.valueOf(schoolYear)); 
			// check if application season exist or if exist, is within application window
			Date current = new Date();
			if(standard == null || 
				!(current.after(standard.getStartDate()) && current.before(standard.getEndDate()))){
				status = "Application season passed or not started !"; 
			}else{
				// set project-key, send email to mentor (notify project abstract)
				String projectKey = studentDAO.updateProjectKeyIREP(user.getUserID(), applicationID); 
				String url = String.format("%s/projects/review/%s", mynmampBaseURL, projectKey); 
				String deadline = Parse.FORMAT_DATE_YMD.format(standard.getEndDate());
				boolean success = mailService.sendMailProjectAbstractToMentor(schoolSemester + " " + schoolYear, 
						url, deadline, 
						user.getFirstName() + " " + user.getLastName(), 
						mentorBean.getMentorFirstName() + " " + mentorBean.getMentorLastName(), 
						mentorBean.getMentorEmail()); 
				if(!success){
					status = String.format("Error sending email to %s!", mentorBean.getMentorEmail()); 
				}else{
					status = String.format("An email was sent to %s!", mentorBean.getMentorEmail()); 
				}
			}
		}
	 
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_PROJECT); 
		redirectAttributes.addFlashAttribute("schoolSemester", schoolSemester);
		redirectAttributes.addFlashAttribute("schoolYear", schoolYear);
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	} 
	
	@GetMapping("/projects/review/{key}")
	public String getProjectReviewPage(ModelMap model, @PathVariable("key") String key){
		IREPProjectBean projectBean = studentDAO.getProjectAbstractIREP(key);
		if(projectBean == null){
			return VIEW_PAGE_NOT_FOUND;
		}
		
		ProgramBean standard = adminDAO.getApplicationWindow(ProgramCode.IREP,
				projectBean.getSchoolSemester(), projectBean.getSchoolYear()); 
		// check if application season exist or if exist, is within application window
		Date current = new Date();
		if(standard == null || 
			!(current.after(standard.getStartDate()) && current.before(standard.getEndDate()))){
			model.addAttribute("status", "Application season passed or not started !"); 
			model.addAttribute("tag", true); 
			return VIEW_PROJECT_REVIEW; 
		}
		
		User applicantBean = ((UserDAOImpl)userDAO).getUserByUserID(projectBean.getUserID());
		model.addAttribute("applicantBean", applicantBean); 
		model.addAttribute("projectBean", projectBean); 
		model.addAttribute("key", key); 
		if(!model.containsAttribute("signatureBean")){
			model.addAttribute("signatureBean", new SignatureBean());
		}
		model.addAttribute("status", model.get("status")); 
		return VIEW_PROJECT_REVIEW; 
	}

	@GetMapping(value="/LICENSE")
	public RedirectView getLicense(){
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(false);
		redirectView.setUrl(Security.LICENSE);
		return redirectView; 
	}
	
	@PostMapping(value="/projects/review/{key}")
	public String submitProjectReview(ModelMap model,
			@ModelAttribute("signatureBean") SignatureBean signatureBean,  BindingResult bindingResult, 
			@PathVariable("key") String key, @ModelAttribute("applicationID") int applicationID, 
			@ModelAttribute("userID") int userID, RedirectAttributes redirectAttributes){
		 
		signatureValidator.validate(signatureBean, bindingResult);
		if(bindingResult.hasErrors()){
			String FORM_NAME = "signatureBean"; 
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM_NAME, bindingResult);
			redirectAttributes.addFlashAttribute("signatureBean", signatureBean);
		}else{
			int result = studentDAO.submitProjectIREP(userID, applicationID, signatureBean); 
			String status; 
			if(result != 0){
				studentDAO.markIREPComplete(userID, applicationID); 
				status = "Submitted successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
			redirectAttributes.addFlashAttribute("status", status); 
		}
		return "redirect:/projects/review/"+key;
	}

}
