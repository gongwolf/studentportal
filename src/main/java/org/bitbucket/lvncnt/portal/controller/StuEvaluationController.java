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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bitbucket.lvncnt.portal.dao.StudentDAOImpl;
import org.bitbucket.lvncnt.portal.model.EvaluationBean;
import org.bitbucket.lvncnt.portal.model.MentorBean;
import org.bitbucket.lvncnt.portal.model.UserImpl;
import org.bitbucket.lvncnt.utilities.Parse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/evaluation")
public class StuEvaluationController {

	private static final String VIEW_EVALUATION = "student/evaluation"; 
	private static final String VIEW_STU_EVAL_MID = "student/mid-evaluation"; 
	private static final String VIEW_PAGE_NOT_FOUND = "error/404";
	
	@Autowired private StudentDAOImpl studentDAO;
	@Autowired private ObjectMapper objectMapper; 
 
	@GetMapping("")
	public String getEvaluationHome(Model model, @AuthenticationPrincipal UserImpl user){
		List<EvaluationBean> evalist = studentDAO.getEvaluations(user.getUserID());
		List<EvaluationBean> evalistPast = 
				evalist.stream()
				.filter(bean -> bean.getSubmitDate() != null || new Date().after(bean.getEvalDeadline()))
				.collect(Collectors.toList()); 
		List<EvaluationBean> evalistTodo = 
				evalist.stream()
				.filter(bean -> bean.getSubmitDate() == null && new Date().before(bean.getEvalDeadline()))
				.collect(Collectors.toList()); 
		model.addAttribute("evalistPast",  evalistPast);
		model.addAttribute("evalistTodo",  evalistTodo);
		return VIEW_EVALUATION ; 
	}
	
	@GetMapping("/{evalPoint}")
	public String getEvaluationForm(ModelMap model, 
			@PathVariable("evalPoint") String evalPoint, @RequestParam("applicationID") String applicationID, 
			@RequestParam("evalSemester") String evalSemester, @RequestParam("evalYear") String evalYear, 
			@RequestParam("name") String menteeName, 
			RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserImpl user){
		MentorBean mentorBean = studentDAO.getMentorBean(user.getUserID(), Parse.tryParseInteger(applicationID));
		if (mentorBean == null) {
			return VIEW_PAGE_NOT_FOUND; 
		}
		
		model.addAttribute("evalPoint",  evalPoint);
		model.addAttribute("evalSemester",  evalSemester);
		model.addAttribute("evalYear",  evalYear);
		model.addAttribute("applicationID",  applicationID);
		model.addAttribute("menteeName",  menteeName);
		model.addAttribute("mentorName",  mentorBean.getMentorFirstName() + " " + mentorBean.getMentorLastName());
		String url = ""; 
		if("Mid-Term".equalsIgnoreCase(evalPoint)){
			 url = VIEW_STU_EVAL_MID; 
		}else if("End-Of-Semester".equalsIgnoreCase(evalPoint)){
			// TODO
			// url = VIEW_MENTOR_EVAL_END;
		}
		return url; 
	}
	
	@PostMapping("/{evalPoint}")
	public @ResponseBody String submitEvaluationForm(@RequestParam ("json") String json,  
			 @RequestParam("applicationID") String applicationID, @PathVariable("evalPoint") String evalPoint,
			 @RequestParam("evalYear") String evalYear, @RequestParam("evalSemester") String evalSemester) {
		 
		TypeReference<HashMap<String, Object>> typeReference = new TypeReference<HashMap<String,Object>>() {
		};
		int result = 0;  
		if("Mid-Term".equalsIgnoreCase(evalPoint)){
			try {
				HashMap<String, Object> evalMap = objectMapper.readValue(json, typeReference);
				result = studentDAO.setEvaluationMidTerm(applicationID, evalYear, evalSemester, evalMap); 
			}  catch (IOException e) {
				e.printStackTrace();
			}  
		}else if("End-Of-Semester".equalsIgnoreCase(evalPoint)){
			// TODO
		}
		
		ObjectNode response = objectMapper.createObjectNode(); 
		if(result > 0){
			response.set("status", objectMapper.convertValue("ok", JsonNode.class));
		}else{
			response.set("status", objectMapper.convertValue("error", JsonNode.class));
		}
		return response.toString();
	}
 
}
