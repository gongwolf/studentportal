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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bitbucket.lvncnt.portal.dao.AdminDAOImpl;
import org.bitbucket.lvncnt.portal.model.ApplicationBean;
import org.bitbucket.lvncnt.portal.model.EvaluationBean;
import org.bitbucket.lvncnt.portal.model.Role;
import org.bitbucket.lvncnt.portal.model.UserImpl;
import org.bitbucket.lvncnt.portal.service.ExcelXlsxView;
import org.bitbucket.lvncnt.portal.service.ITextPdf;
import org.bitbucket.lvncnt.portal.service.MailService;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.utilities.Parse;
import com.itextpdf.text.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/admin/urs-project")
public class AdmURSProjectController {
	
	private static final String VIEW_USR_PROJECT = "admin/urs-project"; 
	private static final String VIEW_USR_PROJECT_RESULT = "admin/urs-project-result"; 
	private static final String VIEW_USR_PROJECT_DETAIL = "admin/urs-project-detail"; 
 
	@Value("${URL.mynmamp.base}") private String mynmampBaseURL; 
	@Autowired @Qualifier("dataSource") private DataSource dataSource; 
	@Autowired private AdminDAOImpl adminDAO;
	@Autowired private MailService mailService;
	@Autowired private ExcelXlsxView excelXlsxView;
	@Autowired private ITextPdf iTextPdf;
	@Autowired private ObjectMapper objectMapper; 
	
	private static final Logger logger = LoggerFactory.getLogger(AdmURSProjectController.class); 
	
	@GetMapping("")
	public ModelAndView getURSProjects(ModelMap model, @AuthenticationPrincipal UserImpl user) {
		if(!model.containsAttribute("academicSchool")){
			TreeMap<String, String> schools = new TreeMap<String, String>(); 
			if(user.getRole().equals(Role.ADMIN)){
				schools.putAll(ProgramCode.ACADEMIC_SCHOOL_FOUR);
				schools.put("ALL", "All"); 
			}else{
				schools.put(user.getAffiliation(), ProgramCode.ACADEMIC_SCHOOL.get(user.getAffiliation()));
			}
			model.addAttribute("academicSchoolFour", schools); 
		}
		if(!model.containsAttribute("academicSemester")){
			model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
		}
		if(!model.containsAttribute("applicationBean")){
			ApplicationBean appBean = new ApplicationBean();
			appBean.setSchoolYear( Calendar.getInstance().get(Calendar.YEAR));
			model.addAttribute("applicationBean", appBean); 
		}
		model.addAttribute("status", model.get("status")); 
		return new ModelAndView(VIEW_USR_PROJECT); 
	}
	
	@PostMapping("")
	public View getURSProjectsResult(ModelMap model, 
			@ModelAttribute("applicationBean") ApplicationBean applicationBean, 
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
	 
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return ApplicationBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				if(applicationBean.getSchoolYear() == 0){
					errors.rejectValue("schoolYear", "NotBlank.applicationBean.schoolYear", 
							"Please select a year."); 
				}
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "schoolSemester", 
						"NotBlank.applicationBean.schoolSemester", "Please select a semester.");
			}
		}.validate(applicationBean, bindingResult);
		 
		RedirectView view = new RedirectView(); 
		view.setContextRelative(true);
		if(bindingResult.hasErrors()){
			String FORM_NAME = "applicationBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("applicationBean", applicationBean);
			view.setUrl("/admin/urs-project"); 
			return view; 
		}
		
		int schoolYear = applicationBean.getSchoolYear(); 
		String schoolSemester = applicationBean.getSchoolSemester(); 
		String schoolTarget = applicationBean.getSchoolTarget();
		redirectAttributes.addFlashAttribute("schoolSemester", schoolSemester);
        redirectAttributes.addFlashAttribute("schoolYear", schoolYear);
        redirectAttributes.addFlashAttribute("schoolTarget", schoolTarget);
        view.setUrl(String.format("%s?year=%s&semester=%s&schoolTarget=%s", 
        		"/admin/urs-project/result", schoolYear, schoolSemester,schoolTarget));
        return view;
	}
 
	@GetMapping(value="result")
	public ModelAndView ursProjectResultDirect(ModelMap model, 
			@RequestParam("year") int schoolYear, 
			@RequestParam("semester") String schoolSemester,
			@RequestParam("schoolTarget") String schoolTarget) {
		ModelAndView view = new ModelAndView(VIEW_USR_PROJECT_RESULT); 
		List<ApplicationBean> projectList = adminDAO.getAcceptedURSProject(schoolSemester, schoolYear,schoolTarget); 
		try {
			view.addObject("projectList", objectMapper.writeValueAsString(projectList));
		} catch (JsonProcessingException e) { 
			logger.error(e.getMessage());
		} 
		view.addObject("schoolSemester", schoolSemester); 
		view.addObject("schoolYear", schoolYear); 
		view.addObject("schoolTarget", schoolTarget); 
        view.addObject("status", model.get("status")); 
		return view; 
	} 
	 
	@GetMapping("detail/{schoolSemester}/{schoolYear}/{applicationID}/{schoolTarget}")
	public ModelAndView getProjectDetail(ModelMap model, 
			@PathVariable("applicationID") int applicationID, 
			@PathVariable("schoolSemester") String schoolSemester,
			@PathVariable("schoolYear") int schoolYear, 
			@PathVariable("schoolTarget") String schoolTarget) {
		ApplicationBean bean = adminDAO.getURSProjectByApplicationID(applicationID); 
		// get evaluation list 
		List<EvaluationBean> evaluationList = adminDAO.getEvaluations(applicationID);
		List<EvaluationBean> evaluationListStu = adminDAO.getEvaluationsStu(applicationID);
		model.addAttribute("applicationBean", bean); 
		model.addAttribute("evaluationList", evaluationList); 
		model.addAttribute("evaluationListStu", evaluationListStu);
		model.addAttribute("status", model.get("status")); 
		 
		if(!model.containsAttribute("evaluationBean")){
			model.addAttribute("evaluationBean", new EvaluationBean()); 
		}
		if(!model.containsAttribute("evaluationBeanStu")){
			model.addAttribute("evaluationBeanStu", new EvaluationBean()); 
		}
        return new ModelAndView(VIEW_USR_PROJECT_DETAIL); 
	}
	
	/** Evaluation by Mentor **/ 
	@PostMapping("/detail/add-evaluation")
	public View addMentorEvaluation(ModelMap model,
			@ModelAttribute("evaluationBean") EvaluationBean evaluationBean, 
			@RequestParam String applicationID, @RequestParam String acceptSemester, 
			@RequestParam String acceptYear, @RequestParam String schoolTarget, 
			BindingResult bindingResult, RedirectAttributes redirectAttributes){
 
		String status = ""; 
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return EvaluationBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "evalTerm", 
										"NotNull.evaluationBean.evalTerm", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "evalYear", 
						"NotNull.evaluationBean.evalYear", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "evalDeadline", 
						"NotNull.evaluationBean.evalDeadline", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "evalPoint", 
						"NotNull.evaluationBean.evalPoint", "Required.");
			}
		}.validate(evaluationBean, bindingResult);
		 
		RedirectView view = new RedirectView(String.format("%s/%s/%s/%s/%s", 
				"/admin/urs-project/detail", acceptSemester, 
				acceptYear, applicationID, schoolTarget), true); 
		if(bindingResult.hasErrors()){
			String FORM_NAME = "evaluationBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("evaluationBean", evaluationBean);
			return view; 
		}
		int result = adminDAO.addEvaluation(applicationID, evaluationBean); 
		if(result != 0){
			status = "New Evaluation Added!"; 
		}else{
			status = "Evaluation Already Existed!"; 
		}
		redirectAttributes.addFlashAttribute("status", status);
		return view;
	}
	
	// email/notify mentor 
	@PostMapping("/detail/email-evaluation/{acceptSemester}/{acceptYear}/{applicationID}")
	public @ResponseBody String sendEvaluationEmail(RedirectAttributes redirectAttributes, ModelMap model,
			@PathVariable("acceptYear") String acceptYear, @PathVariable("acceptSemester") String acceptSemester,
			@PathVariable("applicationID") String applicationID,
			@RequestParam("evalPoint") String evalPoint, 
			@RequestParam("evalTerm") String evalTerm, @RequestParam("evalYear") String evalYear) {
		// get deadline, send email to mentor, set notify date 
		EvaluationBean evaluationBean = adminDAO.getEvalutionBasic(applicationID, evalYear, evalTerm, evalPoint); 
		boolean success = mailService.sendMailMentorEvaluationRequest(evaluationBean);
		String response; 
		if(!success){
			response = "Error occurred, please try again."; 
		}else{
			response = "Email was sent to " + evaluationBean.getMentorEmail() + "!";
			// save notified date 
			adminDAO.setEvaluationNotifiedDate(applicationID, evalYear, evalTerm, evalPoint); 
		}
		ObjectNode json = objectMapper.createObjectNode(); 
		json.set("status", objectMapper.convertValue(response, JsonNode.class));
		return json.toString();
	}
 
	@PostMapping("/detail/update-deadline/{acceptSemester}/{acceptYear}/{applicationID}/{schoolTarget}")
	public View updateEvaluationDeadline(ModelMap model,RedirectAttributes redirectAttributes, 
			@PathVariable("acceptYear") String acceptYear, @PathVariable("acceptSemester") String acceptSemester,
			@PathVariable("applicationID") String applicationID, @PathVariable("schoolTarget") String schoolTarget,
			@RequestParam("evalPoint") String evalPoint, @RequestParam("evalDeadline") String evalDeadline,
			@RequestParam("evalSemester") String evalSemester, @RequestParam("evalYear") String evalYear) {
		String status; 
		Date deadline = null;
		try {
			String endDate;
			endDate = Parse.FORMAT_DATETIME.format(Parse.FORMAT_DATE_MDY.parse(evalDeadline));
			endDate = endDate.split("\\s+")[0] + " 23:59:59"; 
			deadline = Parse.FORMAT_DATETIME.parse(endDate);
		} catch (java.text.ParseException e) {
			deadline = new Date(); 
		} 
		
		int result = adminDAO.setEvaluationDeadline(applicationID, evalYear,
				evalSemester, evalPoint, deadline); 
		if(result != 0){
			status = "Deadline Updated!"; 
		}else{
			status = "Error occurred during submission!"; 
		}
		redirectAttributes.addFlashAttribute("status", status);
		RedirectView view = new RedirectView(); 
		view.setContextRelative(true);
		view.setUrl(String.format("%s/%s/%s/%s/%s", 
				"/admin/urs-project/detail", acceptSemester, acceptYear, applicationID, schoolTarget));
		return view;
	}
 
	@GetMapping("/export-evaluation-excel/{evalPoint}")
	public @ResponseBody ModelAndView exportEvaluationExcel(@PathVariable("evalPoint") String evalPoint, 
			@RequestParam("appIDList") String appIDList){
		List<String> appIDs;
		try {
			appIDs = objectMapper.readValue(appIDList, new TypeReference<List<String>>(){});
			List<List<String>> evaList = new ArrayList<>();  
			String eval = null; 
			if(evalPoint.equalsIgnoreCase(ProgramCode.EVALUATION_POINT.MIDTERM.toString())){
				evaList = adminDAO.getEvaluationsMidTerm(appIDs);
				eval = "EXCEL_EVAL_MID"; 
			}else if(evalPoint.equalsIgnoreCase(ProgramCode.EVALUATION_POINT.ENDOFSEMESTER.toString())){
				evaList = adminDAO.getEvaluationsEnd(appIDs);
				eval = "EXCEL_EVAL_END"; 
			}
			return new ModelAndView(excelXlsxView, eval, evaList); 
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null; 
	}
	 
	@GetMapping("/export-evaluation-pdf/{evalPoint}")
	public @ResponseBody String exportEvaluationPdf(ModelMap model, HttpServletResponse response, 
			@PathVariable("evalPoint") String evalPoint, @RequestParam("applicationID") String applicationID, 
			@RequestParam("evalSemester") String evalSemester, 
			@RequestParam("evalYear") String evalYear) {
		 
		List<String> evaluation = null;  
		byte[] pdfBytes = null; 
		String fileName = ""; 
		if(evalPoint.equalsIgnoreCase(ProgramCode.EVALUATION_POINT.MIDTERM.toString())){
			evaluation = adminDAO.getEvaluationMidTerm(applicationID, evalYear, evalSemester, evalPoint); 
		}else if(evalPoint.equalsIgnoreCase(ProgramCode.EVALUATION_POINT.ENDOFSEMESTER.toString())){
			evaluation = adminDAO.getEvalutionEnd(applicationID, evalYear, evalSemester, evalPoint); 
		}

		if(evaluation == null){
			return "Evaluation Not Submitted!";  
        }

		try {
			pdfBytes = iTextPdf.buildPdfDocumentEvaluation(evalPoint, evaluation);
			fileName = String.format("%s-%s-%s.pdf", evaluation.get(3), evaluation.get(4), evalPoint); 
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.toLowerCase() + "\"");
	        response.setContentType("application/pdf");
			FileCopyUtils.copy(pdfBytes, response.getOutputStream());
		} catch (DocumentException | IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
 
	/** Evaluation by Student **/ 
	@PostMapping("/detail/add-evaluation-stu")
	public View addStudentEvaluation(ModelMap model,
			@ModelAttribute("evaluationBeanStu") EvaluationBean evaluationBeanStu, 
			@RequestParam String applicationID, @RequestParam String acceptSemester, 
			@RequestParam String acceptYear, @RequestParam String schoolTarget, 
			BindingResult bindingResult, RedirectAttributes redirectAttributes){
 
		String status = ""; 
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return EvaluationBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "evalTerm", 
										"NotNull.evaluationBean.evalTerm", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "evalYear", 
						"NotNull.evaluationBean.evalYear", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "evalDeadline", 
						"NotNull.evaluationBean.evalDeadline", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "evalPoint", 
						"NotNull.evaluationBean.evalPoint", "Required.");
			}
		}.validate(evaluationBeanStu, bindingResult);
 
		RedirectView view = new RedirectView(String.format("%s/%s/%s/%s/%s", 
				"/admin/urs-project/detail", acceptSemester, 
				acceptYear, applicationID, schoolTarget), true); 
		if(bindingResult.hasErrors()){
			String FORM_NAME = "evaluationBeanStu"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("evaluationBeanStu", evaluationBeanStu);
			return view;
		}
		int result =  adminDAO.addStudentEvaluation(applicationID, evaluationBeanStu); 
		if(result != 0){
			status = "New Evaluation Added!"; 
		}else{
			status = "Evaluation Already Existed!"; 
		}
		redirectAttributes.addFlashAttribute("status", status);
		return view;
	}

	@PostMapping("/detail/update-deadline-stu/{acceptSemester}/{acceptYear}/{applicationID}/{schoolTarget}")
	public View updateEvaluationDeadlineStu(ModelMap model,RedirectAttributes redirectAttributes, 
			@PathVariable("acceptYear") String acceptYear, @PathVariable("acceptSemester") String acceptSemester,
			@PathVariable("applicationID") String applicationID, @PathVariable("schoolTarget") String schoolTarget,
			@RequestParam("evalPoint") String evalPoint, @RequestParam("evalDeadline") String evalDeadline,
			@RequestParam("evalSemester") String evalSemester, @RequestParam("evalYear") String evalYear) {
		String status; 
		Date deadline = null;
		try {
			String endDate;
			endDate = Parse.FORMAT_DATETIME.format(Parse.FORMAT_DATE_MDY.parse(evalDeadline));
			endDate = endDate.split("\\s+")[0] + " 23:59:59"; 
			deadline = Parse.FORMAT_DATETIME.parse(endDate);
		} catch (java.text.ParseException e) {
			deadline = new Date(); 
		} 
		
		int result = adminDAO.setEvaluationDeadlineStu(applicationID, evalYear,
				evalSemester, evalPoint, deadline); 
		if(result != 0){
			status = "Deadline Updated!"; 
		}else{
			status = "Error occurred during submission!"; 
		}
		redirectAttributes.addFlashAttribute("status", status);
		RedirectView view = new RedirectView(); 
		view.setContextRelative(true);
		view.setUrl(String.format("%s/%s/%s/%s/%s", 
				"/admin/urs-project/detail", acceptSemester, acceptYear, applicationID, schoolTarget));
		return view;
	}
	
	@PostMapping("/detail/email-evaluation-stu/{acceptSemester}/{acceptYear}/{applicationID}")
	public @ResponseBody String sendEvaluationEmailStu(RedirectAttributes redirectAttributes, ModelMap model,
			@PathVariable("acceptYear") String acceptYear, @PathVariable("acceptSemester") String acceptSemester,
			@PathVariable("applicationID") String applicationID,
			@RequestParam("evalPoint") String evalPoint, 
			@RequestParam("evalTerm") String evalTerm, @RequestParam("evalYear") String evalYear) {
		 
		// get deadline, send email to student, set notify date 
		EvaluationBean evaluationBean = adminDAO.getEvalutionBasicStu(applicationID, evalYear, evalTerm, evalPoint); 
		boolean success = mailService.sendMailStudentEvaluationRequest(evaluationBean);
		String response; 
		if(!success){
			response = "Error occurred, please try again."; 
		}else{
			response = "Email was sent to " + evaluationBean.getMenteeEmail() + "!";
			// save notified date 
			adminDAO.setEvaluationNotifiedDateStu(applicationID, evalYear, evalTerm, evalPoint); 
		}
		ObjectNode json = objectMapper.createObjectNode(); 
		json.set("status", objectMapper.convertValue(response, JsonNode.class));
		return json.toString();
	}
	
	@GetMapping("export-evaluation-pdf-stu/{evalPoint}")
	public @ResponseBody String exportEvaluationPdfStu(ModelMap model, HttpServletResponse response, 
			@PathVariable("evalPoint") String evalPoint, @RequestParam("applicationID") String applicationID, 
			@RequestParam("evalSemester") String evalSemester, 
			@RequestParam("evalYear") String evalYear) {
		 
		List<String> evaluation = null;  
		byte[] pdfBytes = null; 
		String fileName = ""; 
		if(ProgramCode.EVALUATION_POINT.MIDTERM.toString().equalsIgnoreCase(evalPoint)){
			evaluation = adminDAO.getEvaluationMidTermStu(applicationID, evalYear, evalSemester, evalPoint); 
		}else if(evalPoint.equalsIgnoreCase(ProgramCode.EVALUATION_POINT.ENDOFSEMESTER.toString())){
			// TODO
			// evaluation = adminDAO.getEvalutionEnd(applicationID, evalYear, evalSemester, evalPoint); 
		}

		if(evaluation == null){
			return "Evaluation Not Submitted!";  
        }

		try {
			pdfBytes = iTextPdf.buildPdfDocumentEvaluationStu(evalPoint, evaluation);
			fileName = String.format("%s-%s-%s.pdf", evaluation.get(3), evaluation.get(4), evalPoint); 
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.toLowerCase() + "\"");
	        response.setContentType("application/pdf");
			FileCopyUtils.copy(pdfBytes, response.getOutputStream());
		} catch (DocumentException | IOException e) {
			logger.error(e.getMessage());
		}
		return null;
		 
	}
	
	@GetMapping("export-evaluation-excel-stu/{evalPoint}")
	public @ResponseBody ModelAndView exportEvaluationExcelStu(@PathVariable("evalPoint") String evalPoint, 
			@RequestParam("appIDList") String appIDList){
		List<String> appIDs;
		try {
			appIDs = objectMapper.readValue(appIDList, new TypeReference<List<String>>(){});
			List<List<String>> evaList = new ArrayList<>();  
			String eval = null; 
			if(ProgramCode.EVALUATION_POINT.MIDTERM.toString().equalsIgnoreCase(evalPoint)){
				evaList = adminDAO.getEvaluationsMidTermStu(appIDs);
				eval = "EXCEL_EVAL_MID_STU"; 
			}else if(ProgramCode.EVALUATION_POINT.ENDOFSEMESTER.toString().equalsIgnoreCase(evalPoint)){
				// TODO
				//evaList = adminDAO.getEvaluationsEnd(appIDs);
				//eval = "EXCEL_EVAL_END"; 
			}
			return new ModelAndView(excelXlsxView, eval, evaList); 
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null; 
	}
	
}
 
