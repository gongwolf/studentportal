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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bitbucket.lvncnt.portal.dao.AdminDAOImpl;
import org.bitbucket.lvncnt.portal.dao.UserDAO;
import org.bitbucket.lvncnt.portal.dao.UserDAOImpl;
import org.bitbucket.lvncnt.portal.model.ApplicationBean;
import org.bitbucket.lvncnt.portal.model.Role;
import org.bitbucket.lvncnt.portal.model.User;
import org.bitbucket.lvncnt.portal.model.UserImpl;
import org.bitbucket.lvncnt.portal.model.selfreport.SelfReportBean;
import org.bitbucket.lvncnt.portal.service.ExcelXlsxView;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

@Controller
@RequestMapping("/admin/application-accept")
public class AdmAdmissionController {
	
	private static final String VIEW_APP_ACCEPT = "admin/application-accept"; 
	private static final String VIEW_APP_ACCEPT_RESULT = "admin/application-accept-result"; 
	private static final String VIEW_APP_ACCEPT_DETAIL = "admin/application-accept-detail"; 
	
	@Autowired private AdminDAOImpl adminDAO;
	@Autowired private UserDAO userDAO;
	@Autowired private ExcelXlsxView excelXlsxView;
	@Autowired private ObjectMapper objectMapper; 
	
	@GetMapping("")
	public ModelAndView getAcceptedApplication(ModelMap model, @AuthenticationPrincipal UserImpl user) {
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
		return new ModelAndView(VIEW_APP_ACCEPT); 
	}
	
	@PostMapping("")
	public View getAcceptedApplicationResult(ModelMap model, 
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
			view.setUrl("/admin/application-accept");
			return view; 
		}
		
		int schoolYear = applicationBean.getSchoolYear(); 
		String schoolSemester = applicationBean.getSchoolSemester(); 
		String schoolTarget = applicationBean.getSchoolTarget(); 
		redirectAttributes.addFlashAttribute("schoolSemester", schoolSemester);
        redirectAttributes.addFlashAttribute("schoolYear", schoolYear);
        redirectAttributes.addFlashAttribute("schoolTarget", schoolTarget);
        view.setUrl(String.format("/%s?year=%s&semester=%s&schoolTarget=%s", 
        		"admin/application-accept/result", schoolYear, schoolSemester,schoolTarget));
        return view;
	}
 
	@GetMapping("/result")
	public ModelAndView acceptedApplicationDirect(ModelMap model, @RequestParam("year") int schoolYear, 
			@RequestParam("semester") String schoolSemester,@RequestParam("schoolTarget") String schoolTarget) {
		List<ApplicationBean> studentList = adminDAO.getAcceptedStudents(schoolSemester, schoolYear,schoolTarget); 
		try {
			model.addAttribute("studentList", objectMapper.writeValueAsString(studentList));
		} catch (JsonProcessingException e) { } 
        model.addAttribute("schoolSemester", schoolSemester); 
        model.addAttribute("schoolYear", schoolYear); 
        model.addAttribute("schoolTarget", schoolTarget); 
        model.addAttribute("status", model.get("status")); 
		return new ModelAndView(VIEW_APP_ACCEPT_RESULT); 
	} 
	
	@GetMapping("/detail/{schoolSemester}/{schoolYear}/{userID}/{schoolTarget}")
	public ModelAndView getStudentDetail(ModelMap model, 
			@PathVariable("userID") int userID, @PathVariable("schoolSemester") String schoolSemester,
			@PathVariable("schoolYear") int schoolYear, @PathVariable("schoolTarget") String schoolTarget)
			throws JsonGenerationException, JsonMappingException, IOException{
		User user = ((UserDAOImpl)userDAO).getProfileStudentBasic(userID);
		List<ApplicationBean> applicationList = adminDAO.getApplicationsByUserID(userID); 
		model.addAttribute("user", user); 
		model.addAttribute("applicationList", applicationList); 
		model.addAttribute("status", model.get("status")); 
        return new ModelAndView(VIEW_APP_ACCEPT_DETAIL); 
	}
 
	/** Export Selfreport **/
	@GetMapping("/export-selfreport")
	public @ResponseBody String exportSelfreport(ModelMap model) {
		List<SelfReportBean> list = adminDAO.getSelfreportWindows(false);
		ArrayNode jsonArray = objectMapper.createArrayNode();
		
		list.stream().forEach((bean) -> {
			ObjectNode node = objectMapper.createObjectNode(); 
			node.set("windowID", objectMapper.convertValue(bean.getWindowID(), JsonNode.class));
			node.set("semester", objectMapper.convertValue(bean.getSemester(), JsonNode.class));
			jsonArray.add(node);
		});
		return jsonArray.toString();
	}
	
	@GetMapping("/export-selfreport/download")
	public @ResponseBody ModelAndView exportSelfreport(
			@RequestParam("semester") String semester, 
			@RequestParam("userIDList") String[] userIDList){
		List<SelfReportBean> selfreportList = adminDAO.getSubmittedSelfReport(semester, userIDList); 
		return new ModelAndView(excelXlsxView, "EXCEL_SELFREPORT_LIST", selfreportList);
	}

}
