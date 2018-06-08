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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bitbucket.lvncnt.portal.service.ExcelToJsonConverter;
import org.bitbucket.lvncnt.portal.service.MessageService;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.lightcouch.CouchDbClient;
import org.lightcouch.NoDocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
@RequestMapping("/admin/manage-ampconf")
public class AdmConfController {
	
	@Autowired private MessageSource messageSource; 
	@Autowired private MessageService messageService;
	@Autowired private CouchDbClient couchDbClient;
	@Autowired private ObjectMapper objectMapper; 
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdmConfController.class); 
 
	private static final String VIEW_MANAGE_IONIC = "admin/manage-ampconf"; 
	 
	@GetMapping("")
	public ModelAndView getManageIonic(ModelMap model) {
		model.addAttribute("status", model.get("status")); 
		return new ModelAndView(VIEW_MANAGE_IONIC); 
	}
	
	@GetMapping("download/template/{template}")
	public View downloadExcelTemplate(ModelMap model,
			@ModelAttribute("status") String status, @PathVariable("template") String template,
			RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response){
	  
		String filename = "";
		switch(template){
		case "template-schedule":
			filename = messageSource.getMessage("Document.template.schedule", null, Locale.ENGLISH); 
			break; 
		case "template-speaker":
			filename = messageSource.getMessage("Document.template.speaker", null, Locale.ENGLISH); 
			break; 
		case "template-poster":
			filename = messageSource.getMessage("Document.template.poster", null, Locale.ENGLISH); 
			break; 
		}
		
		String dir = request.getServletContext().getRealPath("resources/downloads/");
		Path path = Paths.get(dir, filename); 
		if(Files.exists(path)){
			response.setContentType("application/vnd.ms-excel");          
			response.setHeader("Content-disposition", "attachment; filename="+ filename);
			try {
				Files.copy(path, response.getOutputStream());
				response.getOutputStream().flush();
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			} 
		}else{
			redirectAttributes.addFlashAttribute("status", 
					messageSource.getMessage("NotExist.file", null, Locale.ENGLISH)); 
			model.clear();
			RedirectView view = new RedirectView("/admin/manage-ampconf", true); 
			return view; 
		}
		return null; 
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping("view-data")
	public @ResponseBody String processScheduleData() {
		boolean isNew = false, isDown = false; 
		String docID = messageService.getCouchDocumentID(); 
		Map<String, Object> conference = new HashMap<>(); 
		try { 
			conference = couchDbClient.find(Map.class, docID); 
		} catch(NoDocumentException e){ 
			isNew = true; 
		} catch (NullPointerException e){
			isDown = true; 
		}
		
		ObjectNode json = objectMapper.createObjectNode(); 
		if(isNew || isDown){
			json.set("status", objectMapper.convertValue("error", JsonNode.class));
		}else{
			json.set("status", objectMapper.convertValue("ok", JsonNode.class));
			try {
				json.set("value", objectMapper.convertValue(
						objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(conference),
						JsonNode.class)); 
			} catch (JsonProcessingException e) {
				LOGGER.error(e.getMessage());
			}
		}
		return json.toString(); 
	}
	
	@PostMapping("upload-{section}")
	public View uploadScheduleData(ModelMap model, 
			@PathVariable("section") String section, 
			@RequestParam("start_date") Optional<String> startDate, 
			@RequestParam("end_date") Optional<String> endDate,
			@RequestParam("location") Optional<String> location,
			@RequestParam("schedule_file") Optional<MultipartFile> scheduleFile,
			@RequestParam("speaker_file") Optional<MultipartFile> speakerFile,
			@RequestParam("poster_file") Optional<MultipartFile> posterFile, 
			RedirectAttributes redirectAttributes) {
		RedirectView view = new RedirectView("/admin/manage-ampconf", true); 
		 
		Map<String, Object> conference = new HashMap<>(); 
		boolean isNew = false, isDown = false; 
		String docID = messageService.getCouchDocumentID(); 
		try {  
			conference = couchDbClient.find(Map.class, docID);  
		} catch(NoDocumentException e){
			conference.put("_id", docID); 
			isNew = true; 
		} catch (NullPointerException e){
			isDown = true; 
		}

		if(isDown){
			redirectAttributes.addFlashAttribute("status", "Error: CouchDB service is not started!");
			return view; 
		}
		 
		boolean valid = false; 
		String status = ""; 
		switch (section) {
		case "start":
			if(startDate.isPresent() && !startDate.get().isEmpty()) {
				valid = true; 
				conference.put("start", startDate.get()); 
			}
			break;
		case "end": 
			if(endDate.isPresent() && !endDate.get().isEmpty()) {
				valid = true; 
				conference.put("end", endDate.get()); 
			}
			break; 
		case "location": 
			if(location.isPresent() && !location.get().isEmpty()) {
				valid = true;  
				conference.put("map", messageService.getConferenceAddress(location.get())); 
			}
			break; 
		case "schedule": 
			if(scheduleFile.isPresent() && !scheduleFile.get().isEmpty()) {
				valid = true;  
				try {
					conference.put("schedule", ExcelToJsonConverter.convertToJsonSchedule(scheduleFile.get().getInputStream()));
				} catch (IOException | EncryptedDocumentException | InvalidFormatException e) {
					LOGGER.error(e.getMessage());
				} 
			}
			break; 
		case "speaker": 
			if(speakerFile.isPresent() && !speakerFile.get().isEmpty()) {
				valid = true;  
				try {
					conference.put("speaker", ExcelToJsonConverter.convertToJsonSpeaker(speakerFile.get().getInputStream()));
				} catch (IOException | EncryptedDocumentException | InvalidFormatException e) {
					LOGGER.error(e.getMessage());
				} 
			}
			break; 
		case "poster": 
			if(posterFile.isPresent() && !posterFile.get().isEmpty()) {
				valid = true;  
				try {
					conference.put("poster", ExcelToJsonConverter.convertToJsonPoster(posterFile.get().getInputStream()));
				} catch (IOException | EncryptedDocumentException | InvalidFormatException e) {
					LOGGER.error(e.getMessage());
				} 
			}
			break;  
		}
		
		if(!valid) {
			status = "Error: Missing " + section; 
			redirectAttributes.addFlashAttribute("status", status);
			return view; 
		} 
		
		if(isNew){
			couchDbClient.save(conference); 
			status = "Data saved: " + section; 
		}else{
			status = "Data updated: " + section; 
			couchDbClient.update(conference); 
		}
		redirectAttributes.addFlashAttribute("status", status);
		return view; 
	} 
 
}
