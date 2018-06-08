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
import org.bitbucket.lvncnt.portal.service.FileValidator;
import org.bitbucket.lvncnt.portal.service.MailService;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.utilities.Parse;
import org.bitbucket.lvncnt.utilities.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.util.Date;

@Controller
@RequestMapping("/")
@PropertySource("classpath:mail.properties")
public class RecommendationController {
	
	private static final String VIEW_RECOMMENDATIONS_UPLOAD = "index/recommendations-upload"; 
	private static final String VIEW_PAGE_NOT_FOUND = "error/404"; 
	private static final String SECTION_REFERENCE = "reference.do"; 
	
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
    FileValidator fileValidator;
	
	@InitBinder("fileBucket")
	protected void initBinderFileBucket(WebDataBinder binder){
		binder.setValidator(fileValidator);
	}
	
	@PostMapping("/application/recommendations/add/{appcode}")
	public RedirectView addRecommendations(ModelMap model,
			@ModelAttribute("recommenderBean") RecommenderBean recommenderBean,
			BindingResult bindingResult, @AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status, 
			@ModelAttribute("section") String section,
			@ModelAttribute("schoolSemester") String schoolSemester, 
			@ModelAttribute("schoolYear") String schoolYear,
			@PathVariable String appcode,
			RedirectAttributes redirectAttributes){
		 
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			redirectView.setUrl("/application/edit/{appcode}");
			return redirectView; 
		} 
		String applicationID = _appcode[1];
		
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return RecommenderBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", 
						"NotBlank.recommenderBean.firstName", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", 
						"NotBlank.recommenderBean.lastName", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", 
						"NotBlank.recommenderBean.email", "Required");
			}
		}.validate(recommenderBean, bindingResult);
	 
		if(bindingResult.hasErrors()){
 			redirectAttributes.addFlashAttribute("section", SECTION_REFERENCE);
			String FORM_NAME = "recommenderBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("recommenderBean", recommenderBean);
		}else{
			// get application deadline 
			ProgramBean standard = adminDAO.getApplicationWindow(ProgramCode.TRANS,
					schoolSemester, String.valueOf(schoolYear)); 
			// check if application season exist or if exist, is within application window
			Date current = new Date();
			if(standard == null || 
					!(current.after(standard.getStartDate()) && current.before(standard.getEndDate()))){
				status = "Application season passed or not started!"; 
				redirectAttributes.addFlashAttribute("status", status); 
			}else{
				int result = studentDAO.updateRecommenderBean(user.getUserID(), Parse.tryParseInteger(applicationID), recommenderBean);
				if(result == -1){
					status = "You already submitted one recommender!"; 
				}else if(result == 0){
					status = "Error occurred during submission!"; 
				}else{
					// then send email to recommender 
					String recommendationKey = recommenderBean.getRecommendationKey(); 
					if(recommendationKey != null){
						String url = String.format("%s/recommendations/upload/%s", mynmampBaseURL, recommendationKey); 
						recommenderBean.setUrl(url);
						recommenderBean.setApplicantFirstName(user.getFirstName());
						recommenderBean.setApplicantLastName(user.getLastName());
						recommenderBean.setSchoolSemester(schoolSemester);
						recommenderBean.setSchoolYear(String.valueOf(schoolYear));
						recommenderBean.setProgramNameFull(ProgramCode.PROGRAMS.get(ProgramCode.TRANS));
						String deadline = Parse.FORMAT_DATE_YMD.format(standard.getEndDate());
						boolean success = mailService.sendMailRecommendationRequest(recommenderBean, deadline);
						if(!success){
							status = "Error occurred, please remove the registered recommender and try again."; 
						}else{
							status = "An email was sent to " + recommenderBean.getEmail();
						}
					}
				}
			}
		}
		  
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_REFERENCE); 
		redirectAttributes.addFlashAttribute("schoolSemester", schoolSemester);
		redirectAttributes.addFlashAttribute("schoolYear", schoolYear);
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}
	
	@GetMapping("/application/recommendations/remove/{appcode}/{schoolYear}/{schoolSemester}")
	public RedirectView removeRecommendations(ModelMap model, 
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status,  
			@PathVariable("appcode") String appcode, @PathVariable("schoolYear") int schoolYear,
			@PathVariable("schoolSemester") String schoolSemester,
			RedirectAttributes redirectAttributes){
		 
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			redirectView.setUrl("/application/edit/{appcode}");
			return redirectView; 
		} 
		String applicationID = _appcode[1];
		
		ProgramBean standard = adminDAO.getApplicationWindow(ProgramCode.TRANS,
				schoolSemester, String.valueOf(schoolYear)); 
		// check if application season exist or if exist, is within application window
		Date current = new Date();
		if(standard == null || 
				!(current.after(standard.getStartDate()) && current.before(standard.getEndDate()))){
			status = "Application season not started or passed!"; 
		}else{
			int result = studentDAO.removeRecommender(user.getUserID(), applicationID); 
			if(result == -1){
				status = "Recommendation already submitted!"; 
				redirectAttributes.addFlashAttribute("status", status); 
			}
		}
	  
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_REFERENCE); 
		redirectAttributes.addFlashAttribute("schoolSemester", schoolSemester);
		redirectAttributes.addFlashAttribute("schoolYear", schoolYear);
		
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}
	
	@GetMapping("/application/recommendations/resend/{appcode}/{schoolYear}/{schoolSemester}")
	public RedirectView resendRecommendations(ModelMap model, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status,  
			@PathVariable("appcode") String appcode, 
			@PathVariable("schoolYear") int schoolYear,
			@PathVariable("schoolSemester") String schoolSemester,
			RedirectAttributes redirectAttributes){
		 
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			redirectView.setUrl("/application/edit/{appcode}");
			return redirectView; 
		} 
		String applicationID = _appcode[1];
		
		// get application deadline 
		ProgramBean standard = adminDAO.getApplicationWindow(ProgramCode.TRANS,
				schoolSemester, String.valueOf(schoolYear)); 
		// check if application season exist or if exist, is within application window
		Date current = new Date();
		if(standard == null || 
				!(current.after(standard.getStartDate()) && current.before(standard.getEndDate()))){
			status = "Application season passed or not started!"; 
		}else{
			RecommenderBean recommenderBean = null; 
			recommenderBean = studentDAO.resendRecommender(user.getUserID(), applicationID); 
			
			if(recommenderBean == null){
				status = "Recommendation already submitted!"; 
				redirectAttributes.addFlashAttribute("status", status); 
			}else{
				// then send email to recommender 
				String recommendationKey = recommenderBean.getRecommendationKey(); 
				if(recommendationKey != null){
					String url = String.format("%s/recommendations/upload/%s", mynmampBaseURL, recommendationKey); 
					recommenderBean.setUrl(url);
					recommenderBean.setApplicantFirstName(user.getFirstName());
					recommenderBean.setApplicantLastName(user.getLastName());
					recommenderBean.setSchoolSemester(schoolSemester);
					recommenderBean.setSchoolYear(String.valueOf(schoolYear));
					recommenderBean.setProgramNameFull(ProgramCode.PROGRAMS.get(ProgramCode.TRANS));
					String deadline = Parse.FORMAT_DATE_YMD.format(standard.getEndDate());
					boolean success = mailService.sendMailRecommendationRequest(recommenderBean, deadline);
					if(!success){
						status = "Error occurred, please remove the registered recommender and try again."; 
					}else{
						status = "An email was sent to " + recommenderBean.getEmail();
					}
				}
			}
		}
					
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_REFERENCE); 
		redirectAttributes.addFlashAttribute("schoolSemester", schoolSemester);
		redirectAttributes.addFlashAttribute("schoolYear", schoolYear);
		
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}
	
	@GetMapping("/recommendations/upload/{key}")
	public String getUploadRecommendationsPage(ModelMap model, 
			@PathVariable("key") String key){
		 
		RecommenderBean recommenderBean = studentDAO.getRecommendationBean(key); 
		if(recommenderBean == null){
			return VIEW_PAGE_NOT_FOUND;
		}
		
		User user = ((UserDAOImpl)userDAO).getUserByUserID(recommenderBean.getUserID());
		recommenderBean.setApplicantFirstName(user.getFirstName());
		recommenderBean.setApplicantLastName(user.getLastName());
		model.addAttribute("recommenderBean", recommenderBean); 
		model.addAttribute("key", key); 
		if(!model.containsAttribute("fileBucket")){
			model.addAttribute("fileBucket", new FileBucket());
		}
		model.addAttribute("status", model.get("status")); 
		return VIEW_RECOMMENDATIONS_UPLOAD; 
	}
	
	@PostMapping("/recommendations/upload/{key}")
	public RedirectView uploadRecommendations(ModelMap model,
			@Valid FileBucket fileBucket, BindingResult bindingResult, 
			@PathVariable("key") String key, RedirectAttributes redirectAttributes){
		 
		if(bindingResult.hasErrors()){
			String FORM_NAME = "fileBucket"; 
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM_NAME, bindingResult);
			redirectAttributes.addFlashAttribute("fileBucket", fileBucket);
		}else{
			  
			int result = studentDAO.uploadRecommandation(key, fileBucket); 
			String status; 
			if(result != 0){
				status = "File uploaded successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
			redirectAttributes.addFlashAttribute("status", status); 
		}
		 
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		redirectView.setUrl("/recommendations/upload/{key}");
		return redirectView;
	}

}
