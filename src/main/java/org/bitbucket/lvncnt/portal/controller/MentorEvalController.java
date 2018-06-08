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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bitbucket.lvncnt.portal.dao.MentorDAOImpl;
import org.bitbucket.lvncnt.portal.model.EvaluationBean;
import org.bitbucket.lvncnt.portal.model.UserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
@RequestMapping("/mentor/evaluation")
public class MentorEvalController {
	
	private static final String VIEW_EVALUATION = "mentor/evaluation-manage"; 
	private static final String VIEW_MENTOR_EVAL_MID = "mentor/mid-evaluation"; 
	private static final String VIEW_MENTOR_EVAL_END = "mentor/end-evaluation"; 
	
	@Value("${URL.mynmamp.base}") private String mynmampBaseURL; 
	@Autowired private MentorDAOImpl mentorDAO;
	@Autowired private ObjectMapper objectMapper; 
	
	private static final Logger logger = LoggerFactory.getLogger(MentorEvalController.class); 
  
	@GetMapping("")
	public ModelAndView getEvaluationHome(ModelMap model, @AuthenticationPrincipal UserImpl mentor){
		if(!model.containsAttribute("menteeList")){
			model.addAttribute("menteeList",  mentorDAO.getMenteeList(mentor.getUserID()));
		} 
		// get evaluations to be filled and evaluations previously submitted 
		List<EvaluationBean> evalist = mentorDAO.getEvaluations(mentor.getUserID());
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
		model.addAttribute("status", model.get("status")); 
		return new ModelAndView(VIEW_EVALUATION); 
	}
	
	@GetMapping("/{evalPoint}")
	public String getEvaluationForm(ModelMap model, 
			@PathVariable("evalPoint") String evalPoint, @RequestParam("applicationID") String applicationID, 
			@RequestParam("evalSemester") String evalSemester, @RequestParam("evalYear") String evalYear, 
			@RequestParam("name") String menteeName, 
			RedirectAttributes redirectAttributes){
		model.addAttribute("evalPoint",  evalPoint);
		model.addAttribute("evalSemester",  evalSemester);
		model.addAttribute("evalYear",  evalYear);
		model.addAttribute("applicationID",  applicationID);
		model.addAttribute("menteeName",  menteeName);
		String url = ""; 
		if(evalPoint.equalsIgnoreCase("Mid-Term")){
			 url = VIEW_MENTOR_EVAL_MID; 
		}else if(evalPoint.equalsIgnoreCase("End-Of-Semester")){
			 url = VIEW_MENTOR_EVAL_END;
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
		if(evalPoint.equalsIgnoreCase("Mid-Term")){
			try {
				HashMap<String, Object> evalMap = objectMapper.readValue(json, typeReference);
				result = mentorDAO.setEvaluationMidTerm(applicationID, evalYear, evalSemester, evalMap); 
			}  catch (IOException e) {
				logger.error(e.getMessage());
			}  
		}else if(evalPoint.equalsIgnoreCase("End-Of-Semester")){
			try {
				HashMap<String, Object> evalMap = objectMapper.readValue(json, typeReference);
				result = mentorDAO.setEvaluationEnd(applicationID, evalYear, evalSemester, evalMap);
			}  catch (IOException e) {
				logger.error(e.getMessage());
			} 
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
