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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitbucket.lvncnt.portal.dao.AdminDAOImpl;
import org.bitbucket.lvncnt.portal.dao.StudentDAOImpl;
import org.bitbucket.lvncnt.portal.dao.UserDAO;
import org.bitbucket.lvncnt.portal.dao.UserDAOImpl;
import org.bitbucket.lvncnt.portal.model.*;
import org.bitbucket.lvncnt.portal.service.*;
import org.bitbucket.lvncnt.utilities.Parse;
import org.bitbucket.lvncnt.utilities.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/")
public class StuApplicationController {
	
	private static final String VIEW_APPLICATION = "student/application"; 
	private static final String VIEW_SELECT_PROGRAM = "student/select-program"; 
	private static final String VIEW_SELECT_TERM = "student/select-term";
	private static final String VIEW_APPLICATION_COMPLETE = "student/complete";
	private static final String SECTION_ACADEMIC = "academic.do"; 
	private static final String SECTION_INVOLVEMENT = "involvement.do"; 
	private static final String SECTION_ESSAY = "essay.do"; 
	private static final String SECTION_TRANSCRIPT = "transcript.do"; 
	private static final String SECTION_MENTOR = "mentor.do"; 
	private static final String SECTION_PROJECT = "project.do";
	private static final String SECTION_BUDGET = "budget.do";
	private static final String SECTION_REFERENCE = "reference.do"; 
	private static final String SECTION_SUBMIT = "submit.do"; 
	private static final String VIEW_PAGE_NOT_FOUND = "error/404"; 
	
	private static Map<String, String> VIEW_PROGRAMS = new HashMap<String, String>(){
		private static final long serialVersionUID = 1L;
		{
			put("CCCONF", "apply-ccconf"); 
			put("MESA", "apply-mesa");
			put("SCCORE", "apply-sccore");
			put("TRANS", "apply-trans");
			put("URS", "apply-urs");
			put("IREP", "apply-irep");
		}
	}; 
	
	@Autowired private UserDAO userDAO;
	@Autowired private StudentDAOImpl studentDAO;
	@Autowired private AdminDAOImpl adminDAO;
	@Autowired private MessageSource messageSource; 
	@Autowired private ApplicationAcademicValidator applicationAcademicValidator;
	@Autowired private ApplicationInvolvementValidator applicationInvolvementValidator;
	@Autowired private ApplicationEssayValidator applicationEssayValidator;
	@Autowired private ApplicationProjectValidator applicationProjectValidator;
	
	@Autowired private ApplicationBudgetValidator applicationBudgetValidator;
	
	@Autowired private SignatureValidator signatureValidator;
	
	@Autowired private FileValidator fileValidator;
	@Autowired private MailService mailService;
	@Autowired private ObjectMapper objectMapper; 
 
	@InitBinder("projectBean")
	protected void initBinderProjectBean(WebDataBinder binder){
		binder.setValidator(applicationProjectValidator);
	}
	
	@InitBinder("fileBucket")
	protected void initBinderFileBucket(WebDataBinder binder){
		binder.setValidator(fileValidator);
	}
	
	@GetMapping("/application")
	public String getApplications(ModelMap model, @AuthenticationPrincipal UserImpl user){
		List<ApplicationBean> applicationList = studentDAO.getApplicationList(user.getUserID());
		model.addAttribute("applicationList", applicationList); 
		model.addAttribute("status", model.get("status")); 
		return VIEW_APPLICATION; 
	}
	
	@GetMapping("/application/select")
	public View startApplicationSelectProgram(ModelMap model, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, 
			RedirectAttributes redirectAttributes) throws JsonProcessingException{
		boolean isProfileComplete = ((UserDAOImpl)userDAO).isProfileComplete(user.getUserID());
		String url = null; 
		if(!isProfileComplete){
			redirectAttributes.addFlashAttribute("status", "Please complete the Profile before starting any application!");
			url = "/profile";
		} else {
			url = "/application/select/program";
		}
		View redirectView = new RedirectView(url, true); 
		return redirectView; 
	}
	
	@GetMapping("/application/select/program")
	public String getSelectProgram(ModelMap model) throws JsonProcessingException{
		model.addAttribute("jSchoolsTwoYr", objectMapper.writeValueAsString(ProgramCode.ACADEMIC_SCHOOL_TWO));
		model.addAttribute("jSchoolsFourYr", objectMapper.writeValueAsString(ProgramCode.ACADEMIC_SCHOOL_FOUR));
		model.addAttribute("jProgramsTwoYr", objectMapper.writeValueAsString(ProgramCode.PROGRAMS_TWO_YR));
		model.addAttribute("jProgramsFourYr", objectMapper.writeValueAsString(ProgramCode.PROGRAMS_FOUR_YR));
		model.addAttribute("schools", ProgramCode.CURRENT_ACADEMIC_SCHOOL);
		return VIEW_SELECT_PROGRAM; 
	}
	 
	@PostMapping("/application/select/program")
	public View postSelectProgram(ModelMap model, 
			@RequestParam("school") String school ,  
			@RequestParam("program") String program,
			@ModelAttribute("status") String status, 
			RedirectAttributes redirectAttributes){
		String url = null; 
		if(Parse.isBlankOrNull(school) || Parse.isBlankOrNull(program)){
			redirectAttributes.addFlashAttribute("status", "Please select school or program!");
			url = "/application/select/program";
		}else {
			url = "/application/select/term/"+ Security.encode(String.format("%s$$%s", school, program));
		}
		View redirectView = new RedirectView(url, true); 
		return redirectView; 
	}
	
	@GetMapping("/application/select/term/{schoolprogram}")
	public String getSelectTerm(ModelMap model, @PathVariable String schoolprogram){
		model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
		if(!model.containsAttribute("programBean")){
			model.addAttribute("programBean", new ProgramBean());
		}
		return VIEW_SELECT_TERM;  
	}
	
	@PostMapping("/application/select/term/{schoolprogram}")
	public RedirectView postSelectTerm(ModelMap model, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("programBean") ProgramBean programBean, 
			BindingResult bindingResult, @ModelAttribute("status") String status, 
			@PathVariable("schoolprogram") String schoolprogram ,  
			RedirectAttributes redirectAttributes){
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _schoolprogram = Security.decode(schoolprogram).split("\\$\\$");
		if(_schoolprogram.length != 2 || !ProgramCode.PROGRAMS.containsKey(_schoolprogram[1])) {
			redirectAttributes.addFlashAttribute("programBean", programBean);
			redirectAttributes.addFlashAttribute("status", status); 
			redirectView.setUrl("/application/select/term/{schoolprogram}");
			return redirectView; 
		}
		
		String school = _schoolprogram[0], program = _schoolprogram[1]; 
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return ProgramBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "year", 
						"NotBlank.programBean.year", "Please select a year.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "semester", 
						"NotBlank.programBean.semester", "Please select a semester.");
				 
				ProgramBean standard = adminDAO.getApplicationWindow(program, 
						programBean.getSemester(), programBean.getYear()); 
				// check if application season exist or if exist, is within application window
				Date current = new Date();
				if(standard == null || 
						!(current.after(standard.getStartDate()) && current.before(standard.getEndDate()))){
					errors.rejectValue("semester", "NotStarted.programBean.semester", 
							"Application season passed or not started.");
					return; 
				} 
			}
		}.validate(programBean, bindingResult);
		
		if(bindingResult.hasErrors()){
 			redirectAttributes.addFlashAttribute("section", SECTION_ACADEMIC);
			String FORM_NAME = "programBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("programBean", programBean);
			redirectAttributes.addFlashAttribute("status", status); 
			redirectView.setUrl("/application/select/term/{schoolprogram}");
		} else {
			// check if application for the same program at the same season exist
			// if not, an application in the tableAppList, save school
			int applicationID[] = new int[1]; 
			int result = studentDAO.createApplication(user.getUserID(), program,
					programBean.getSemester(), programBean.getYear(), applicationID); 
			if(result != 0){
				status = "You already started the application for "
						+ programBean.getSemester() + " " + programBean.getYear() + ".";
				redirectAttributes.addFlashAttribute("programBean", programBean);
				redirectAttributes.addFlashAttribute("status", status); 
				redirectView.setUrl("/application/select/term/{schoolprogram}");
			}else {
				//save school to app detail table 
				studentDAO.saveAcademicSchool(user.getUserID(), applicationID[0], program, school);
				redirectView.setUrl("/application/edit/"+
						Security.encode(String.format("%s$$%s", program, String.valueOf(applicationID[0]))));
			}
		} 
		return redirectView; 
	}
	 
	@GetMapping("/application/resumeedit/{program}/{applicationID}")
	public RedirectView resumeEditApplication(ModelMap model, 
			@PathVariable("program") String program, 
			@PathVariable("applicationID") String applicationID ){
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		redirectView.setUrl("/application/edit/"+Security.encode(String.format("%s$$%s", program, applicationID)));
		return redirectView; 
	}
	
	@GetMapping("/application/edit/{appcode}")
	public String getApplication(ModelMap model, 
			@PathVariable String appcode, 
			@AuthenticationPrincipal UserImpl user, 
			RedirectAttributes redirectAttributes) throws JsonProcessingException{
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			return "redirect:/application"; 
		} 
		String program = _appcode[0], applicationID = _appcode[1]; 
		 
		ApplicationBean applicationBean = studentDAO.getApplicationBean(user.getUserID(),
				Parse.tryParseInteger(applicationID), program);
		// check if applicationID valid 
		if(applicationBean == null){
			return VIEW_PAGE_NOT_FOUND; 
		}
		 
		if(!model.containsAttribute("academicBean")){
			if(applicationBean == null || applicationBean.getAcademicBean() == null){
				model.addAttribute("academicBean", new AcademicBean());
			}else{
				model.addAttribute("academicBean", applicationBean.getAcademicBean());
			}
		}
		if(!model.containsAttribute("involvementBean")){
			if(applicationBean == null || applicationBean.getInvolvementBean() == null){
				model.addAttribute("involvementBean", new InvolvementBean());
			}else{
				model.addAttribute("involvementBean", applicationBean.getInvolvementBean());
			}
		}
		if(!model.containsAttribute("essayBean")){
			if(applicationBean == null || applicationBean.getEssayBean() == null){
				model.addAttribute("essayBean", new EssayBean());
			}else{
				model.addAttribute("essayBean", applicationBean.getEssayBean());
			}
		}
		 
		if(!model.containsAttribute("fileBucket")){
			if(applicationBean == null || applicationBean.getFileBucket() == null){
				model.addAttribute("fileBucket", new FileBucket());
			}else{
				model.addAttribute("fileBucket", applicationBean.getFileBucket());
			}
		}
		
		switch(program){
		case ProgramCode.CCCONF:
			model.addAttribute("academicSchoolTwo", ProgramCode.ACADEMIC_SCHOOL_TWO);
			model.addAttribute("academicStatus", ProgramCode.ACADEMIC_STATUS);
			model.addAttribute("academicCredit", ProgramCode.ACADEMIC_CREDIT);
			model.addAttribute("academicSchoolFour", ProgramCode.ACADEMIC_SCHOOL_FOUR);
			model.addAttribute("yesno", ProgramCode.YES_NO);
			model.addAttribute("scholarshipTypeCCCONF", ProgramCode.SCHOLARSHIP_TYPE_CCCONF);
			model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
			model.addAttribute("nmsuCampus", ProgramCode.NMSU_CAMPUS);
			break; 
		case ProgramCode.MESA:
			if(!model.containsAttribute("referenceBucket")){
				if(applicationBean == null || applicationBean.getReferenceBucket() == null){
					model.addAttribute("referenceBucket", new FileBucket()); 
				}else{
					model.addAttribute("referenceBucket", applicationBean.getReferenceBucket());
				}
			}
			model.addAttribute("academicYear", ProgramCode.ACADEMIC_YEAR);
			model.addAttribute("academicSchoolFour", ProgramCode.ACADEMIC_SCHOOL_FOUR);
			model.addAttribute("yesno", ProgramCode.YES_NO);
			model.addAttribute("scholarshipType", ProgramCode.SCHOLARSHIP_TYPE);
			model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
			break; 
		case ProgramCode.SCCORE:
			if(!model.containsAttribute("referenceBucket")){
				if(applicationBean == null || applicationBean.getReferenceBucket() == null){
					model.addAttribute("referenceBucket", new FileBucket()); 
				}else{
					model.addAttribute("referenceBucket", applicationBean.getReferenceBucket());
				}
			}
			model.addAttribute("academicSchoolSCCORE", ProgramCode.ACADEMIC_SCHOOL_TWO);
			model.addAttribute("academicYear", ProgramCode.ACADEMIC_YEAR);
			model.addAttribute("yesno", ProgramCode.YES_NO);
			model.addAttribute("scholarshipType", ProgramCode.SCHOLARSHIP_TYPE);
			model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
			model.addAttribute("academicSchoolFour", ProgramCode.ACADEMIC_SCHOOL_FOUR);
			
			model.addAttribute("jSchoolsFourYr", objectMapper.writeValueAsString(ProgramCode.ACADEMIC_SCHOOL_FOUR));
			break; 
		case ProgramCode.TRANS:
			model.addAttribute("academicSchoolTwo", ProgramCode.ACADEMIC_SCHOOL_TWO);
			model.addAttribute("academicYear", ProgramCode.ACADEMIC_YEAR);
			model.addAttribute("academicSchoolFour", ProgramCode.ACADEMIC_SCHOOL_FOUR);
			model.addAttribute("yesno", ProgramCode.YES_NO);
			model.addAttribute("scholarshipType", ProgramCode.SCHOLARSHIP_TYPE);
			model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
			
			// for recommendation 
			List<RecommenderBean> recommenderList = studentDAO.getRecommenderList(user.getUserID(), Parse.tryParseInteger(applicationID));
			model.addAttribute("recommenderList", recommenderList); 
			if(!model.containsAttribute("recommenderBean")){
				model.addAttribute("recommenderBean", new RecommenderBean());
			}
			model.addAttribute("mentorPrefix", ProgramCode.MENTOR_PREFIX);
			model.addAttribute("states", ProgramCode.STATES);
			break; 
		case ProgramCode.URS:
			if(!model.containsAttribute("mentorBean")){
				if(applicationBean == null || applicationBean.getMentorBean() == null){
					model.addAttribute("mentorBean", new MentorBean());
				}else{
					model.addAttribute("mentorBean", applicationBean.getMentorBean());
				}
			}
			if(!model.containsAttribute("projectBean")){
				if(applicationBean == null || applicationBean.getProjectBean() == null){
					model.addAttribute("projectBean", new ProjectBean());
				}else{
					model.addAttribute("projectBean", applicationBean.getProjectBean());
				}
			}
			model.addAttribute("academicYear", ProgramCode.ACADEMIC_YEAR);
			model.addAttribute("academicSchoolFour", ProgramCode.ACADEMIC_SCHOOL_FOUR);
			model.addAttribute("yesno", ProgramCode.YES_NO);
			model.addAttribute("scholarshipType", ProgramCode.SCHOLARSHIP_TYPE);
			model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
			model.addAttribute("mentorPrefix", ProgramCode.MENTOR_PREFIX);
			
			break; 
		case ProgramCode.IREP:
			model.addAttribute("academicYear", ProgramCode.ACADEMIC_YEAR);
			model.addAttribute("academicSchoolFour", ProgramCode.ACADEMIC_SCHOOL_FOUR);
			model.addAttribute("yesno", ProgramCode.YES_NO);
			
			if(!model.containsAttribute("irepProjectBean")){
				if(applicationBean == null || applicationBean.getIrepProjectBean() == null){
					model.addAttribute("irepProjectBean", new IREPProjectBean());
				}else{
					model.addAttribute("irepProjectBean", applicationBean.getIrepProjectBean());
				}
			}
			
			if(!model.containsAttribute("budgetBean")){
				if(applicationBean == null || applicationBean.getBudgetBean() == null){
					model.addAttribute("budgetBean", new BudgetBean());
				}else{
					model.addAttribute("budgetBean", applicationBean.getBudgetBean());
				}
			}
			
			if(!model.containsAttribute("mentorBean")){
				if(applicationBean == null || applicationBean.getMentorBean() == null){
					model.addAttribute("mentorBean", new MentorBean());
				}else{
					model.addAttribute("mentorBean", applicationBean.getMentorBean());
				}
			}
			break; 
		}
	 
		boolean disableTag = false; 
		SignatureBean signatureBean = new SignatureBean();
		ProgramBean standard = adminDAO.getApplicationWindow(program, 
				applicationBean.getSchoolSemester(), String.valueOf(applicationBean.getSchoolYear())); 
		// check if application season exist or if exist, is within application window
		// check if application already completed, ==> disable all buttons
		Date current = new Date();
		if(standard == null || 
			!(current.after(standard.getStartDate()) && current.before(standard.getEndDate()))){
			disableTag = true; 
		}
		if(applicationBean.getApplicantSignature() != null && 
				applicationBean.getApplicantSubmitDate() != null){
			disableTag = true; 
			signatureBean.setSignature(applicationBean.getApplicantSignature());
			signatureBean.setSignatureDate(applicationBean.getApplicantSubmitDate());
		} 
		
		model.addAttribute("tag", disableTag); 
		if(!model.containsAttribute("signatureBean")){
			model.addAttribute("signatureBean", signatureBean);
		}
		
		model.addAttribute("schoolSemester", applicationBean.getSchoolSemester()); 
		model.addAttribute("schoolYear", applicationBean.getSchoolYear());
		model.addAttribute("status", model.get("status")); 
		model.addAttribute("section", model.get("section")); 
		model.addAttribute("appcode", appcode);
		return "student/" + VIEW_PROGRAMS.get(program); 
	}
	
	@PostMapping("/application/edit/academic.do/{appcode}")
	public RedirectView setApplcationAcademic(ModelMap model,
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("academicBean") AcademicBean academicBean, 
			BindingResult bindingResult, 
			@ModelAttribute("status") String status, 
			@ModelAttribute("section") String section,
			@PathVariable String appcode,
			RedirectAttributes redirectAttributes){
		 
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			redirectView.setUrl("/application");
			return redirectView; 
		} 
		String program = _appcode[0], applicationID = _appcode[1]; 
		
		academicBean.setProgramNameAbbr(program);
		applicationAcademicValidator.validate(academicBean, bindingResult);
		if(bindingResult.hasErrors()){
 			redirectAttributes.addFlashAttribute("section", SECTION_ACADEMIC);
				 
			String FORM_NAME = "academicBean";
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("academicBean", academicBean);
		}else{
			int result = studentDAO.updateAcademicBean(user.getUserID(),
					Parse.tryParseInteger(applicationID), program, academicBean);
			if(result != 0){
				status = "Application updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
		}
		
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_ACADEMIC); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}
	
	@PostMapping("/application/edit/involvement.do/{appcode}")
	public RedirectView setApplcationInvlovement(ModelMap model,
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("academicBean") InvolvementBean involvementBean, 
			BindingResult bindingResult, 
			@ModelAttribute("status") String status, 
			@ModelAttribute("section") String section,
			@PathVariable String appcode, 
			RedirectAttributes redirectAttributes){
		 
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			redirectView.setUrl("/application");
			return redirectView; 
		} 
		String program = _appcode[0], applicationID = _appcode[1]; 
		
		involvementBean.setProgramNameAbbr(program);
		applicationInvolvementValidator.validate(involvementBean, bindingResult);
		if(bindingResult.hasErrors()){
			String FORM_NAME = "involvementBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("involvementBean", involvementBean);
		}else{
			int result = studentDAO.updateInvolvementBean(user.getUserID(),
					Parse.tryParseInteger(applicationID), program, involvementBean);
			if(result != 0){
				status = "Application updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
		}
		  
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_INVOLVEMENT); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}
	 
	@PostMapping("/application/edit/essay.do/{appcode}")
	public RedirectView setApplcationEssay(ModelMap model,
			@ModelAttribute("essayBean") EssayBean essayBean, 
			BindingResult bindingResult,  
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status, 
			@ModelAttribute("section") String section,
			@PathVariable String appcode, 
			RedirectAttributes redirectAttributes){
		 
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			redirectView.setUrl("/application");
			return redirectView; 
		} 
		String program = _appcode[0], applicationID = _appcode[1]; 
		essayBean.setProgramNameAbbr(program);
		applicationEssayValidator.validate(essayBean, bindingResult);
		if(bindingResult.hasErrors()){
			String FORM_NAME = "essayBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("essayBean", essayBean);
		}else{
			int result = studentDAO.updateEssayBean(user.getUserID(), Parse.tryParseInteger(applicationID), program, essayBean);
			if(result != 0){
				status = "Application updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
		}
		  
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_ESSAY); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}
	
	/* MESA: Reference */
	@PostMapping("/application/edit/reference.do/{appcode}")
	public RedirectView uploadReference(ModelMap model,
			@AuthenticationPrincipal UserImpl user,
			@Valid FileBucket referenceBucket, BindingResult bindingResult, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			@PathVariable String appcode, 
			RedirectAttributes redirectAttributes){
		
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			redirectView.setUrl("/application");
			return redirectView; 
		} 
		String applicationID = _appcode[1]; 
		
		if(bindingResult.hasErrors()){
			String FORM_NAME = "referenceBucket"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("referenceBucket", referenceBucket);
		}else{
			int result = studentDAO.uploadReference(user.getUserID(), 
					applicationID, ProgramCode.MESA, referenceBucket);
			if(result != 0){
				status = "File uploaded successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			} 
		}
		
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_REFERENCE); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView;  
	}
	
	@GetMapping("/application/download/reference-{appcode}")
	public String downloadReference(@AuthenticationPrincipal UserImpl user,
			@PathVariable String appcode, HttpServletResponse response) throws IOException{
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			return VIEW_PAGE_NOT_FOUND; 
		} 
		String applicationID = _appcode[1]; 
		String fileName = Security.digest(
				UUID.randomUUID().toString() + System.currentTimeMillis(), Security.MD5)+".pdf";
        byte[] content = studentDAO.downloadReference(user.getUserID(), applicationID, ProgramCode.MESA);
        if(content == null){
        		return VIEW_PAGE_NOT_FOUND; 
        }else{
        		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.toLowerCase() + "\"");
            response.setContentType("application/pdf");
    			FileCopyUtils.copy(content, response.getOutputStream());
    			return null;
        }
	}
	
	@GetMapping("/application/download/template/sccore-medical-authorization-form")
	public String downloadMedicalAuthorizationTemplate(ModelMap model,
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response){
	  
		String filename = messageSource.getMessage("Document.medical-form", null, Locale.ENGLISH); 
		String dir = request.getServletContext().getRealPath("resources/downloads/");
		Path path = Paths.get(dir, filename); 
		if(Files.exists(path)){
			response.setContentType("application/pdf");            
			response.setHeader("Content-disposition", "attachment; filename="+ filename);
			try {
				Files.copy(path, response.getOutputStream());
				response.getOutputStream().flush();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}else{
			redirectAttributes.addFlashAttribute("status", 
					messageSource.getMessage("NotExist.file", null, Locale.ENGLISH)); 
			redirectAttributes.addFlashAttribute("section", SECTION_REFERENCE); 

			model.clear();
			return "redirect:/profile"; 
		}
		return null; 
	}
	
	@PostMapping("/application/edit/medical.do/{appcode}")
	public RedirectView uploadMedicalAuthorization(ModelMap model,
			@Valid FileBucket referenceBucket, BindingResult bindingResult, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			@PathVariable String appcode,
			RedirectAttributes redirectAttributes){
		
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			redirectView.setUrl("/application");
			return redirectView; 
		} 
		String applicationID = _appcode[1]; 
		
		if(bindingResult.hasErrors()){
			String FORM_NAME = "referenceBucket"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("referenceBucket", referenceBucket);
		}else{
			int result = studentDAO.uploadMedicalAuthorization(user.getUserID(), 
					applicationID, ProgramCode.SCCORE, referenceBucket);
			if(result != 0){
				status = "File uploaded successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			} 
		}
		
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_REFERENCE); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView;  
	}
	
	@GetMapping("/application/download/medical-{appcode}")
	public String downloadMedicalAuthorization(@AuthenticationPrincipal UserImpl user,
			@PathVariable String appcode, HttpServletResponse response) throws IOException{
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			return VIEW_PAGE_NOT_FOUND; 
		} 
		String applicationID = _appcode[1]; 
		String fileName = Security.digest(
				UUID.randomUUID().toString() + System.currentTimeMillis(), Security.MD5)+".pdf";
        byte[] content = studentDAO.downloadMedicalAuthorization(user.getUserID(), applicationID, ProgramCode.SCCORE);
        if(content == null){
        		return VIEW_PAGE_NOT_FOUND; 
        }else{
        		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.toLowerCase() + "\"");
            response.setContentType("application/pdf");
    			FileCopyUtils.copy(content, response.getOutputStream());
    			return null;
        }
	}
	
	@PostMapping("/application/edit/transcript.do/{appcode}")
	public RedirectView uploadTranscript(ModelMap model,
			@Valid FileBucket fileBucket, 
			BindingResult bindingResult,  
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status, 
			@ModelAttribute("section") String section,
			@PathVariable String appcode, 
			RedirectAttributes redirectAttributes){
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			redirectView.setUrl("/application");
			return redirectView; 
		} 
		String program = _appcode[0], applicationID = _appcode[1]; 
		
		if(bindingResult.hasErrors()){
			String FORM_NAME = "fileBucket"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("fileBucket", fileBucket);
		}else{
			int result = studentDAO.uploadTranscript(user.getUserID(),
					Parse.tryParseInteger(applicationID), program, fileBucket);
			if(result != 0){
				status = "File uploaded successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
		} 
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_TRANSCRIPT); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}
	
	@RequestMapping(value="/application/download/transcript-{appcode}", method=RequestMethod.GET)
	public String downloadTranscript(@AuthenticationPrincipal UserImpl user,
			@PathVariable String appcode, HttpServletResponse response) throws IOException{
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			return VIEW_PAGE_NOT_FOUND; 
		} 
		String program = _appcode[0], applicationID = _appcode[1]; 
		String fileName = Security.digest(
				UUID.randomUUID().toString() + System.currentTimeMillis(), Security.MD5)+".pdf";
		byte[] content = studentDAO.downloadTranscript(user.getUserID(), applicationID, program);
        if(content == null){
        		return VIEW_PAGE_NOT_FOUND; 
        }else{
        		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.toLowerCase() + "\"");
            response.setContentType("application/pdf");
    			FileCopyUtils.copy(content, response.getOutputStream());
    			return null;
        }
	}

	@PostMapping("/application/edit/mentor.do/{appcode}")
	public RedirectView setApplcationMentor(ModelMap model,
			@ModelAttribute("mentorBean") MentorBean mentorBean, 
			BindingResult bindingResult,   
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status, 
			@ModelAttribute("section") String section,
			@PathVariable String appcode, 
			RedirectAttributes redirectAttributes){
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			redirectView.setUrl("/application");
			return redirectView; 
		} 
		String applicationID = _appcode[1]; 
		
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return MentorBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorFirstName", 
						"NotBlank.mentorBean.mentorFirstName", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorLastName", 
						"NotBlank.mentorBean.mentorLastName", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorPrefix", 
						"NotBlank.mentorBean.mentorPrefix", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorEmail", 
						"NotBlank.mentorBean.mentorEmail", "Required");
			}
		}.validate(mentorBean, bindingResult);
		if(bindingResult.hasErrors()){
			String FORM_NAME = "mentorBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("mentorBean", mentorBean);
		}else{
			int result = studentDAO.updateMentorBean(user.getUserID(),
					Parse.tryParseInteger(applicationID), mentorBean);
			if(result != 0){
				studentDAO.updateMentorInfoDate(user.getUserID(), applicationID);
				status = "Application updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
		}
		  
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_MENTOR); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}

	@PostMapping("/application/edit/project.do/{appcode}")
	public RedirectView setApplcationProject(ModelMap model,
			@ModelAttribute("projectBean") @Valid ProjectBean projectBean, 
			BindingResult bindingResult, Principal principal, 
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status, 
			@ModelAttribute("section") String section,
			@PathVariable String appcode, 
			RedirectAttributes redirectAttributes){
		  
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.PROGRAMS.containsKey(_appcode[0])){
			redirectView.setUrl("/application");
			return redirectView; 
		} 
		String applicationID = _appcode[1];
		
		if(bindingResult.hasErrors()){
			String FORM_NAME = "projectBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("projectBean", projectBean);
		}else{
			int result = studentDAO.updateProjectBean(user.getUserID(),
					Parse.tryParseInteger(applicationID), projectBean);
			if(result != 0){
				status = "Application updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
		}
		  
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_PROJECT); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}
	
	@PostMapping("/application/project/send.do/{appcode}")
	public RedirectView sendProjectProposal(ModelMap model,  
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status, 
			@ModelAttribute("schoolSemester") String schoolSemester, 
			@ModelAttribute("schoolYear") String schoolYear,
			@PathVariable String appcode, 
			RedirectAttributes redirectAttributes){
		
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		String[] _appcode = Security.decode(appcode).split("\\$\\$");
		if(_appcode.length != 2 || !ProgramCode.URS.equals(_appcode[0])){
			redirectView.setUrl("/application");
			return redirectView; 
		} 
		String applicationID = _appcode[1];

		// check if mentor info and project proposal are filled in 
		MentorBean mentorBean = studentDAO.isMentorAndProjectFilled(user.getUserID(),
				Parse.tryParseInteger(applicationID));
		if(mentorBean == null){
			status = "Missing mentor or project proposal information!"; 
		}else{
			// 1. send email to mentor (notify project proposal)
			// (first to do, avoid email send failure
			// (only include true password for the first time mentor)
			// 2. create account (if none exist) for mentor
			 
			// check if mentor profile already exist (by checking email)
			// one mentor can have more than one student, if exist, use user_id as mentor_id
			// not using existing profile, i.e., name, because not sure if error happens, which version is correct one. 
			int mentorID = ((UserDAOImpl)userDAO).isAccountExist(mentorBean.getMentorEmail()); 
			User mentor = new User();
			mentor.setFirstName(mentorBean.getMentorFirstName());
			mentor.setLastName(mentorBean.getMentorLastName());
			mentor.setEmail(mentorBean.getMentorEmail());
			String password = mentorID != 0 ? "******" : Security.getRandomUUID();
			mentor.setPassword(password); 
			
			ProgramBean standard = adminDAO.getApplicationWindow(ProgramCode.URS,
					schoolSemester, schoolYear); 
			String deadline = Parse.FORMAT_DATE_YMD.format(standard.getEndDate());
			boolean success = mailService.sendMailProjectProposalToMentor(
					user.getFirstName() + " " + user.getLastName(), 
					schoolSemester + " " + schoolYear, mentor, deadline); 
			if(!success){
				status = String.format("Error sending email to %s!", mentor.getEmail()); 
			}else{
				status = String.format("An email was sent to %s!", mentor.getEmail()); 
				// for new mentor: register mentor, create mentor profile 
				if(mentorID == 0){
					mentorID = ((UserDAOImpl)userDAO).registerMentor(mentorBean.getMentorFirstName(), 
							mentorBean.getMentorLastName(), mentorBean.getMentorEmail(), password);
					((UserDAOImpl)userDAO).createProfileMentor(mentorID); 
				}
				 
				// update mentor_id in application_list table
				studentDAO.setMentorID4URS(user.getUserID(),
						Parse.tryParseInteger(applicationID), mentorID);
			}
		}
		 
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_PROJECT);
		redirectAttributes.addFlashAttribute("schoolSemester", schoolSemester);
		redirectAttributes.addFlashAttribute("schoolYear", schoolYear);
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	} 

	@PostMapping("/application/edit/budget.do/{appcode}")
	public RedirectView setIREPBudget( 
			@ModelAttribute("budgetBean") BudgetBean budgetBean, 
			BindingResult bindingResult, 
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status, 
			@ModelAttribute("section") String section,
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
		
		applicationBudgetValidator.validate(budgetBean, bindingResult);
		if(bindingResult.hasErrors()){
 			redirectAttributes.addFlashAttribute("section", SECTION_BUDGET);
			String FORM_NAME = "budgetBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("budgetBean", budgetBean);
		}else{
			int result = studentDAO.updateBudgetBean(user.getUserID(), applicationID, budgetBean); 
			if(result != 0){
				status = "Application updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			} 
		}
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_BUDGET); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}
	
	@PostMapping(value="/application/edit/irep-project.do/{appcode}")
	public RedirectView setIREPProject( 
			@ModelAttribute("irepProjectBean") IREPProjectBean irepProjectBean, 
			BindingResult bindingResult, 
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
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
		
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return IREPProjectBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "researchDate", 
						"NotBlank.irepProjectBean.researchDate", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "leaveDate", 
						"NotBlank.irepProjectBean.leaveDate", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "returnDate", 
						"NotBlank.irepProjectBean.returnDate", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "everFundAmp", 
						"NotBlank.irepProjectBean.everFundAmp", "Required");
				IREPProjectBean bean = (IREPProjectBean)target; 
				if(bean.getEverFundAmp() != null && bean.getEverFundAmp() == 1){
					ValidationUtils.rejectIfEmptyOrWhitespace(errors, "listProgram", 
							"NotBlank.irepProjectBean.everFundAmp", "Required");
				}
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "projectAbstract", 
						"NotBlank.irepProjectBean.projectAbstract", "Required");
			}
		}.validate(irepProjectBean, bindingResult);
	 
		if(bindingResult.hasErrors()){
			String FORM_NAME = "irepProjectBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("irepProjectBean", irepProjectBean);
		}else{
			int result = studentDAO.updateIREPProjectBean(user.getUserID(), 
					Integer.parseInt(applicationID), irepProjectBean); 
			if(result != 0){
				status = "Application updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
		} 
		  
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_PROJECT); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}
	
	@PostMapping(value="/application/edit/irep-mentor.do/{appcode}")
	public RedirectView setApplcationIREPMentor( 
			@ModelAttribute("mentorBean") MentorBean mentorBean, 
			BindingResult bindingResult, 
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
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
		
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return MentorBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorFirstName", 
						"NotBlank.mentorBean.mentorFirstName", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorLastName", 
						"NotBlank.mentorBean.mentorLastName", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorEmail", 
						"NotBlank.mentorBean.mentorEmail", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorInstitution", 
						"NotBlank.mentorBean.mentorInstitution", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorPhone", 
						"NotBlank.mentorBean.mentorPhone", "Required");
			}
		}.validate(mentorBean, bindingResult);
		if(bindingResult.hasErrors()){
			String FORM_NAME = "mentorBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("mentorBean", mentorBean);
		}else{
			int result = studentDAO.updateIREPMentorBean(user.getUserID(),
					applicationID, mentorBean); 
			if(result != 0){
				status = "Application updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
		}
		  
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_MENTOR); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}

	@PostMapping("/application/edit/submit.do/{appcode}")
	public RedirectView setApplcationSubmit(ModelMap model,
			@ModelAttribute("signatureBean") SignatureBean signatureBean, 
			BindingResult bindingResult, 
			@AuthenticationPrincipal UserImpl user, 
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
			redirectView.setUrl("/application");
			return redirectView; 
		} 
		String applicationID = _appcode[1], program = _appcode[0];
		
		signatureValidator.validate(signatureBean, bindingResult);
		if(bindingResult.hasErrors()){
 			String FORM_NAME = "signatureBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("signatureBean", signatureBean);
		}else{
			ProgramBean standard = adminDAO.getApplicationWindow(program, 
					schoolSemester, schoolYear); 
			// check if application season exist or if exist, is within application window
			Date current = new Date();
			if(standard == null || 
					!(current.after(standard.getStartDate()) && current.before(standard.getEndDate()))){
				status = "Application season not started or passed!"; 
				redirectAttributes.addFlashAttribute("status", status); 
			}else if(!studentDAO.isApplicationIComplete(user.getUserID(), applicationID, program)){
				// check if all required info filled completely
				status = "Missing required information!"; 
			}else if(ProgramCode.URS.equalsIgnoreCase(program)
					&& !studentDAO.isApplicationIICompleteURS(user.getUserID(), applicationID)){
				status = "Project Proposal Not Completed!"; 
			}else {
				int result = studentDAO.submitApplication(user.getUserID(), applicationID, program, signatureBean); 
				if(result != 0){
					// email student for completing application 
					String name = user.getFirstName() + " " + user.getLastName(); 
					String email = user.getEmail(); 
					String term = schoolYear + " " + schoolSemester; 
					String programFull = ProgramCode.PROGRAMS.get(program);
					String deadline = Parse.FORMAT_DATE_YMD.format(standard.getEndDate());
					
					mailService.sendMailToStudentComplete(name, email, program, programFull, term, deadline); 
					if(ProgramCode.IREP.equalsIgnoreCase(program)){
						studentDAO.markIREPComplete(user.getUserID(), Integer.parseInt(applicationID)); 
					}
					
					// direct to complete page 
					redirectAttributes.addFlashAttribute("name", user.getFirstName()); 
					redirectAttributes.addFlashAttribute("program", programFull); 
					redirectAttributes.addFlashAttribute("term", term); 
					redirectView.setUrl("/application/complete/{appcode}");
					return redirectView; 
					 
				}else{
					status = "Error occurred during submission!"; 
				}
			}
		}
		  
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_SUBMIT); 
		redirectAttributes.addFlashAttribute("schoolSemester", schoolSemester);
		redirectAttributes.addFlashAttribute("schoolYear", schoolYear); 
		redirectView.setUrl("/application/edit/{appcode}");
		return redirectView; 
	}
	
	@GetMapping("/application/complete/{appcode}")
	public String complete(ModelMap model, @PathVariable String appcode){
		model.addAttribute("name", model.get("name")); 
		model.addAttribute("program", model.get("program")); 
		model.addAttribute("term", model.get("term")); 
		return VIEW_APPLICATION_COMPLETE;  
	}
	
	
	/**
	 * Add by qixu
	 */
	@PostMapping("/student/application/update-decision-do")
	public View DecisionUpdateApplication(ModelMap model,
			@RequestParam(value = "program") String program,
			@RequestParam(value = "applicationID") String appID,
			@RequestParam(value = "decision") String decision,
			@RequestParam(value = "targetSchool", required = false) String schoolTarget){
//		RedirectView redirectView = new RedirectView(); 
//		redirectView.setContextRelative(true);
//		redirectView.setUrl("/application/edit/"+Security.encode(String.format("%s$$%s", program, applicationID)));
//		System.out.println(" I am Here : "+program+" "+appID+" "+decision+" "+schoolTarget);
		RedirectView view = new RedirectView();
		studentDAO.UpdateSingleApplicationDecision(program, appID, decision, schoolTarget);
		view.setUrl("/portal/application");
		return 	view; 
	}

	
}