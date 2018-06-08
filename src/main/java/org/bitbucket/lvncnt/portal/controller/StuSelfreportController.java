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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bitbucket.lvncnt.portal.dao.AdminDAOImpl;
import org.bitbucket.lvncnt.portal.dao.StudentDAOImpl;
import org.bitbucket.lvncnt.portal.model.UserImpl;
import org.bitbucket.lvncnt.portal.model.selfreport.ReportAcademicBean;
import org.bitbucket.lvncnt.portal.model.selfreport.ReportOthersBean;
import org.bitbucket.lvncnt.portal.model.selfreport.SelfReportBean;
import org.bitbucket.lvncnt.portal.service.DocxService;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.utilities.Parse;
import org.bitbucket.lvncnt.utilities.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/selfreport")
public class StuSelfreportController {
	
	private static final String VIEW_SELFREPORT = "student/selfreport"; 
	private static final String VIEW_SELFREPORT_SELECTSEMESTER = "student/selfreport-selectsemester"; 
	private static final String VIEW_SELFREPORT_SELECTVIEW = "student/selfreport-selectview";
	private static final String VIEW_SELFREPORT_SELECTEXPORT = "student/selfreport-selectexport";
	private static final String VIEW_SELFREPORT_SUBMIT = "student/selfreport-submit";
	private static final String SECTION_ACADEMIC = "academic.do"; 
	private static final String SECTION_INTERNSHIP = "internship.do"; 
	private static final String SECTION_TRAVEL = "travel.do"; 
	private static final String SECTION_CONFERENCE = "conference.do"; 
	private static final String SECTION_PUBLICATION = "publication.do"; 
	private static final String SECTION_AWARDS = "awards.do"; 
	private static final String SECTION_OTHERS = "others.do";
	private static final String VIEW_PAGE_NOT_FOUND = "error/404"; 
	
	private static final Logger logger = LoggerFactory.getLogger(StuSelfreportController.class); 
	
	@Autowired private AdminDAOImpl adminDAO;
	@Autowired private StudentDAOImpl studentDAO;
	@Autowired private DocxService docxService;
	@Autowired private ObjectMapper objectMapper; 
	
	@GetMapping("")
	public String getSelfreportHome(ModelMap model){
		return VIEW_SELFREPORT ; 
	}
	
	@GetMapping("/select")
	public String getSelfreportSeason(ModelMap model, 
			@AuthenticationPrincipal UserImpl user,
			@RequestParam("fuseaction") String fuseaction){
		switch (fuseaction) {
		case "app.resumecreate":
			if(!model.containsAttribute("selfreportWindows")){
				// fetch active windows 
				List<SelfReportBean> selfreportWindows = adminDAO.getSelfreportWindows(true);
				model.addAttribute("selfreportWindows",  selfreportWindows);
			} 
			if(!model.containsAttribute("selfReportBean")){
				model.addAttribute("selfReportBean", new SelfReportBean()); 
			}
			return VIEW_SELFREPORT_SELECTSEMESTER;
			
		case "app.resumeview":
			if(!model.containsAttribute("selfreportWindows")){
				List<SelfReportBean> selfreportWindows = studentDAO.getSelfreportSubmitDates(user.getUserID(), false); 
				model.addAttribute("selfreportWindows",  selfreportWindows);
			}
			return VIEW_SELFREPORT_SELECTVIEW;
			
		case "app.resumeexport":
			if(!model.containsAttribute("selfreportWindows")){
				// only reports having submit data is selected 
				List<SelfReportBean> selfreportWindows = studentDAO.getSelfreportSubmitDates(user.getUserID(), true); 
				model.addAttribute("selfreportWindows",  selfreportWindows);
			}
			return VIEW_SELFREPORT_SELECTEXPORT; 
			
		default:
			return VIEW_SELFREPORT;
		}
	}
	
	@GetMapping("/create")
	public String createSelfReport(ModelMap model, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("selfReportBean") SelfReportBean selfReportBean, 
			RedirectAttributes redirectAttributes){
		 
		if(selfReportBean.getSemester() == null || selfReportBean.getSemester().length() < 2){
			redirectAttributes.addFlashAttribute("status", "Please select semester!");
			return "redirect:/selfreport/select?fuseaction=app.resumecreate";
		}
		 
		int windowID = Integer.parseInt(selfReportBean.getSemester().split("\\*")[0]); 
		String semester = selfReportBean.getSemester().split("\\*")[1]; 
		// check if selfreport for the same semester exist
		int userID = user.getUserID(); 
		int result = studentDAO.createSelfReport(userID, windowID); 
		if(result != 0){
			redirectAttributes.addFlashAttribute("status", "You already created the self report for " + semester + "."); 
			return "redirect:/selfreport/select?fuseaction=app.resumecreate";
		}
	 
		redirectAttributes.addFlashAttribute("windowID", windowID);
		redirectAttributes.addFlashAttribute("semester", semester);
		return "redirect:/selfreport/create.do";
	}
	
	@GetMapping("/create.do")
	public String createSelfReportEdit(ModelMap model, 
			@AuthenticationPrincipal UserImpl user){
		ReportAcademicBean reportAcademicBean = new ReportAcademicBean();
		reportAcademicBean.setFirstName(user.getFirstName());
		reportAcademicBean.setLastName(user.getLastName());
		model.addAttribute("reportAcademicBean", reportAcademicBean);  
		model.addAttribute("reportOthersBean", new ReportOthersBean());
		
		model.addAttribute("states", ProgramCode.STATES);
		model.addAttribute("academicSchool", ProgramCode.ACADEMIC_SCHOOL);
		model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
		return VIEW_SELFREPORT_SUBMIT; 
	}
 
	@GetMapping("/resumeedit/{windowID}/{semester}")
	@ResponseBody public String resumeEditSelfReport(ModelMap model, 
			@PathVariable("semester") String semester, @PathVariable("windowID") int windowID){ 
		return String.format("selfreport/edit?windowID=%s&semester=%s",
				Security.encode(String.valueOf(windowID)), Security.encode(semester));
	}
	 
	@GetMapping("/edit")
	public String getEditSelfreport(ModelMap model, 
			@AuthenticationPrincipal UserImpl user, 
			@RequestParam("semester") String _semester, @RequestParam("windowID") String _windowID){
		 String semester = Security.decode(_semester);
		 int windowID = Parse.tryParseInteger(Security.decode(_windowID));
		SelfReportBean selfReportBean = studentDAO.getSelfreportBean(windowID, user.getUserID()); 
		 
		// check if window id and semester exist
		if(!adminDAO.isSelfreportWindowExist(windowID, semester)){
			return VIEW_PAGE_NOT_FOUND; 
		} 
		
		if(!model.containsAttribute("reportAcademicBean")){
			if(selfReportBean == null || selfReportBean.getReportAcademicBean() == null){
				ReportAcademicBean reportAcademicBean = new ReportAcademicBean(); 
				reportAcademicBean.setFirstName(user.getFirstName());
				reportAcademicBean.setLastName(user.getLastName());
				model.addAttribute("reportAcademicBean", reportAcademicBean); 
			}else{
				model.addAttribute("reportAcademicBean", selfReportBean.getReportAcademicBean());
			}
		}
		
		model.addAttribute("internJson", selfReportBean.getReportInternJson()); 
		model.addAttribute("publicationJson", selfReportBean.getReportPublicationJson()); 
		model.addAttribute("awardsJson", selfReportBean.getReportAwardsJson()); 
		model.addAttribute("conferenceJson", selfReportBean.getReportConferenceJson()); 
		model.addAttribute("travelJson", selfReportBean.getReportTravelJson()); 
		
		if(!model.containsAttribute("reportOthersBean")){
			if(selfReportBean == null || selfReportBean.getReportOthersBean() == null){
				model.addAttribute("reportOthersBean", new ReportOthersBean()); 
			}else{
				model.addAttribute("reportOthersBean", selfReportBean.getReportOthersBean());
			}
		}
		
		model.addAttribute("states", ProgramCode.STATES);
		model.addAttribute("academicSchool", ProgramCode.ACADEMIC_SCHOOL);
		model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
		
		model.addAttribute("status", model.get("status")); 
		model.addAttribute("section", model.get("section")); 
		model.addAttribute("semester", semester);
		model.addAttribute("windowID", windowID); 
		 
		return VIEW_SELFREPORT_SUBMIT; 
	}
	 
	@PostMapping("/edit/academic.do")
	public String setSelfreportAcademic(ModelMap model,
			@ModelAttribute("reportAcademicBean") ReportAcademicBean reportAcademicBean, 
			BindingResult bindingResult, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			@RequestParam("semester") String semester, @RequestParam("windowID") int windowID, 
			RedirectAttributes redirectAttributes){
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return ReportAcademicBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentAddressLine1", 
						"NotBlank.reportAcademicBean.currentAddressLine1", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentAddressCity", 
						"NotBlank.reportAcademicBean.currentAddressCity", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentAddressState", 
						"NotBlank.reportAcademicBean.currentAddressState", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentAddressZip", 
						"NotBlank.reportAcademicBean.currentAddressZip", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "selectSchool", 
						"NotBlank.reportAcademicBean.selectSchool", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "major", 
						"NotBlank.reportAcademicBean.major", "Required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gpa", 
						"NotBlank.reportAcademicBean.gpa", "Required");
			}
		}.validate(reportAcademicBean, bindingResult);
		 
		if(bindingResult.hasErrors()){
 			redirectAttributes.addFlashAttribute("section", SECTION_ACADEMIC);
			String FORM_NAME = "reportAcademicBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("reportAcademicBean", reportAcademicBean);
		}else{
			int result = studentDAO.updateReportAcademic(user.getUserID(), windowID, semester, reportAcademicBean); 
			if(result != 0){
				status = "Selfreport updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
		}
		  
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_ACADEMIC);
		return String.format("redirect:/selfreport/edit?windowID=%s&semester=%s",
				Security.encode(String.valueOf(windowID)), Security.encode(semester));
	}

	@PostMapping("/edit/internship.do")
	public String setSelfreportInternship(ModelMap model, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			@RequestParam("semester") String semester, @RequestParam("windowID") int windowID,
			@RequestParam("internJson") String internJson,  BindingResult bindingResult, 
			RedirectAttributes redirectAttributes){
		 
		if(internJson == null || internJson.isEmpty()){
 			redirectAttributes.addFlashAttribute("section", SECTION_INTERNSHIP);
			String FORM_NAME = "reportInternshipBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
		}else{
			int result = studentDAO.updateReportInternship(user.getUserID(), windowID, semester, internJson);
			if(result == 0){
				status = "Error occurred during submission!"; 
	 		}else{
	 			status = "New entry added!"; 
			}
		} 
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_INTERNSHIP); 
		redirectAttributes.addFlashAttribute("windowID", windowID);
		redirectAttributes.addFlashAttribute("semester", semester);
		return String.format("redirect:/selfreport/edit?windowID=%s&semester=%s",
				Security.encode(String.valueOf(windowID)), Security.encode(semester));
	}
	
	@PostMapping("/edit/travel.do")
	public String setSelfreportTravel(ModelMap model, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			@RequestParam("semester") String semester, @RequestParam("windowID") int windowID,
			@RequestParam("travelJson") String travelJson, 
			BindingResult bindingResult, RedirectAttributes redirectAttributes){
		 
		if(travelJson == null || travelJson.isEmpty()){
 			redirectAttributes.addFlashAttribute("section", SECTION_TRAVEL);
			String FORM_NAME = "reportTravelBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
		}else{
			int result = studentDAO.updateReportTravel(user.getUserID(), windowID, semester, travelJson);
			if(result == 0){
				status = "Error occurred during submission!"; 
	 		}else{
	 			status = "New entry added!"; 
			}
		}
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_TRAVEL); 
		redirectAttributes.addFlashAttribute("windowID", windowID);
		redirectAttributes.addFlashAttribute("semester", semester);
		return String.format("redirect:/selfreport/edit?windowID=%s&semester=%s",
				Security.encode(String.valueOf(windowID)), Security.encode(semester));
	}
	
	@PostMapping("/edit/conference.do")
	public String setSelfreportConference(ModelMap model, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			@RequestParam("semester") String semester, @RequestParam("windowID") int windowID,
			@RequestParam("conferenceJson") String conferenceJson, 
			BindingResult bindingResult, RedirectAttributes redirectAttributes){
		 
		if(conferenceJson == null || conferenceJson.isEmpty()){
 			redirectAttributes.addFlashAttribute("section", SECTION_CONFERENCE);
			String FORM_NAME = "reportConferenceBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
		}else{
			int result = studentDAO.updateReportConferenceBean(user.getUserID(), windowID, semester, conferenceJson);
			if(result == 0){
				status = "Error occurred during submission!"; 
	 		}else{
	 			status = "New entry added!"; 
			}
		}
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_CONFERENCE); 
		redirectAttributes.addFlashAttribute("windowID", windowID);
		redirectAttributes.addFlashAttribute("semester", semester);
		return String.format("redirect:/selfreport/edit?windowID=%s&semester=%s",
				Security.encode(String.valueOf(windowID)), Security.encode(semester));
	}
	
	@PostMapping("/edit/publication.do")
	public String setSelfreportPublication(ModelMap model, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			@RequestParam("semester") String semester, @RequestParam("windowID") int windowID,
			@RequestParam("publicationJson") String publicationJson, 
			BindingResult bindingResult, RedirectAttributes redirectAttributes){
		 
		if(publicationJson == null || publicationJson.isEmpty()){
 			redirectAttributes.addFlashAttribute("section", SECTION_PUBLICATION);
			String FORM_NAME = "reportPublicationBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
		}else{
			int result = studentDAO.updateReportPublicationBean(user.getUserID(), windowID, semester, publicationJson);
			if(result == 0){
				status = "Error occurred during submission!";  
	 		}else{
	 			status = "New entry added!"; 
			}
		} 
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_PUBLICATION); 
		redirectAttributes.addFlashAttribute("windowID", windowID);
		redirectAttributes.addFlashAttribute("semester", semester);
		return String.format("redirect:/selfreport/edit?windowID=%s&semester=%s",
				Security.encode(String.valueOf(windowID)), Security.encode(semester));
	}
	
	@PostMapping("/edit/awards.do")
	public String setSelfreportAwards(ModelMap model, 
			@AuthenticationPrincipal UserImpl user,  
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			@RequestParam("semester") String semester, @RequestParam("windowID") int windowID,
			@RequestParam("awardsJson") String awardsJson, 
			BindingResult bindingResult, RedirectAttributes redirectAttributes){
		if(awardsJson == null || awardsJson.isEmpty()){
 			redirectAttributes.addFlashAttribute("section", SECTION_AWARDS);
			String FORM_NAME = "reportAwardsBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
		}else{
			int result = studentDAO.updateReportAwardsBean(user.getUserID(), windowID, semester, awardsJson);
			if(result == 0){
				status = "Error occurred during submission!"; 
	 		}else{
	 			status = "New entry added!"; 
			}
		} 
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_AWARDS); 
		redirectAttributes.addFlashAttribute("windowID", windowID);
		redirectAttributes.addFlashAttribute("semester", semester);
		return String.format("redirect:/selfreport/edit?windowID=%s&semester=%s",
				Security.encode(String.valueOf(windowID)), Security.encode(semester));
	}
	
	@PostMapping("/edit/others.do")
	public String setSelfreportOthers(ModelMap model,
			@ModelAttribute("reportOthersBean") ReportOthersBean reportOthersBean, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			@RequestParam("semester") String semester, @RequestParam("windowID") int windowID,
			RedirectAttributes redirectAttributes){
		int result = studentDAO.updateReportOthersBean(user.getUserID(), windowID, semester, reportOthersBean);
		if(result == 0){
			status = "Error occurred during submission!"; 
 		}else{
 			status = "Selfreport updated successfully!"; 
		}
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_OTHERS); 
		redirectAttributes.addFlashAttribute("windowID", windowID);
		redirectAttributes.addFlashAttribute("semester", semester);
		return String.format("redirect:/selfreport/edit?windowID=%s&semester=%s",
				Security.encode(String.valueOf(windowID)), Security.encode(semester));
	}
	
	@PostMapping("/edit/submit.do")
	public String setSelfreportSubmit(ModelMap model, 
			@AuthenticationPrincipal UserImpl user,  
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			RedirectAttributes redirectAttributes,
			@RequestParam("semester") String semester, @RequestParam("windowID") int windowID){
		int result = studentDAO.updateReportSubmit(user.getUserID(), windowID, semester);
		if(result == 0){
			status = "Missing required information!"; 
			redirectAttributes.addFlashAttribute("status", status); 
			redirectAttributes.addFlashAttribute("section", SECTION_ACADEMIC);
			redirectAttributes.addFlashAttribute("windowID", windowID);
			redirectAttributes.addFlashAttribute("semester", semester);
			return String.format("redirect:/selfreport/edit?windowID=%s&semester=%s",
					Security.encode(String.valueOf(windowID)), Security.encode(semester));
 		}else{
 			status = "Selfreport submitted successfully!"; 
 			redirectAttributes.addFlashAttribute("status", status); 
 			return "redirect:/selfreport/select?fuseaction=app.resumeview";
		}
	}
	
	@GetMapping("/export/resume")
	public @ResponseBody String exportResumeSelfReport(@RequestParam("ids") String[] ids) {
		ObjectNode json = objectMapper.createObjectNode(); 
		if(ids.length == 0){
			json.set("status", objectMapper.convertValue("error", JsonNode.class));
		}else{
			json.set("status", objectMapper.convertValue("ok", JsonNode.class));
			json.set("ids", objectMapper.convertValue(Arrays.asList(ids), JsonNode.class));
		}
		return json.toString(); 
	}
	
	@GetMapping("/export/download")
	public @ResponseBody String exportDownloadSelfReport(@AuthenticationPrincipal UserImpl user, 
			HttpServletResponse response, @RequestParam("ids") String[] ids) {
		List<String> idList = new ArrayList<>(); 
		for(String id: ids){
			idList.add(id.split("\\*")[0]);
		}
		List<SelfReportBean> selfReportList = studentDAO.getSelectedSelfReport(user.getUserID(), idList); 
		
		String fileName = "export.docx"; 
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.toLowerCase() + "\"");
		byte[] content = docxService.getResume(selfReportList); 
		if(content != null){
			try {
				FileCopyUtils.copy(content, response.getOutputStream());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		return ""; 
	}
	
}
