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
import org.bitbucket.lvncnt.portal.dao.UserDAO;
import org.bitbucket.lvncnt.portal.dao.UserDAOImpl;
import org.bitbucket.lvncnt.portal.model.*;
import org.bitbucket.lvncnt.portal.service.ExcelXlsxView;
import org.bitbucket.lvncnt.portal.service.ITextPdf;
import org.bitbucket.lvncnt.portal.service.MailService;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.utilities.IOUtils;
import org.bitbucket.lvncnt.utilities.Parse;
import com.itextpdf.text.DocumentException;
import freemarker.template.utility.StringUtil;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdmApplicationController {

	private static final String VIEW_SEARCH_APPLICANT = "admin/search-applicant";
	private static final String VIEW_SEARCH_APPLICANT_RESULT = "admin/search-applicant-result";
	private static final String VIEW_NMSU_APPLICATION = "admin/manage-application";
	private static final String VIEW_NMSU_APPLICATION_RESULT = "admin/manage-application-result";

	@Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;

	@Autowired
	private ExcelXlsxView excelXlsxView;
	@Autowired
	private ITextPdf iTextPdf;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private AdminDAOImpl adminDAO;
	@Autowired
	private MailService mailService;
	@Autowired
	private ObjectMapper objectMapper;

	private static final Logger logger = LoggerFactory.getLogger(AdmApplicationController.class);

	/** Search Applicant **/
	@GetMapping("/search-applicant")
	public ModelAndView searchApplicant(Model model) {
		model.addAttribute("programs", ProgramCode.PROGRAMS);
		if (!model.containsAttribute("applicationBean")) {
			model.addAttribute("applicationBean", new ApplicationBean());
		}
		return new ModelAndView(VIEW_SEARCH_APPLICANT);
	}

	@PostMapping("/search-applicant/{criteria}")
	public ModelAndView searchApplicantByPersonProgram(ModelMap model, @PathVariable("criteria") String criteria,
			@ModelAttribute("applicationBean") ApplicationBean applicationBean, RedirectAttributes redirectAttributes) {

		List<ApplicationBean> applications = new ArrayList<>();

		if (criteria.equalsIgnoreCase("person")) {
			String firstName = applicationBean.getFirstName();
			String lastName = applicationBean.getLastName();
			String email = applicationBean.getEmail();
			String birthDate = "";
			if (applicationBean.getBirthDate() != null) {
				birthDate = Parse.FORMAT_DATE_MDY.format(applicationBean.getBirthDate());
			}
			applications = adminDAO.getApplicantsByPerson(firstName, lastName, birthDate, email);
		} else if (criteria.equalsIgnoreCase("program")) {
			String program = applicationBean.getProgramNameAbbr();
			applications = adminDAO.getApplicantsByProgram(program);
		}

		ModelAndView view = new ModelAndView();
		if (applications.isEmpty()) {
			redirectAttributes.addFlashAttribute("status", "No result found.");
			redirectAttributes.addFlashAttribute("applicationBean", applicationBean);
			view.setView(new RedirectView("/admin/search-applicant", true));
		} else {
			view.addObject("applicationList", applications);
			view.setViewName(VIEW_SEARCH_APPLICANT_RESULT);
		}
		return view;
	}

	@GetMapping("/search-applicant/download/student-agreement-and-disclosure-form")
	public void downloadClosure(HttpServletResponse response, @RequestParam("userID") String userID,
			@RequestParam("name") String name) {

		byte[] content = ((UserDAOImpl) userDAO).downloadDisclosureForm(Integer.parseInt(userID));
		if (content != null && content.length > 0) {
			String fileName = String.format("%s_disclosure_form.pdf", name);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.toLowerCase() + "\"");
			response.setContentType("application/pdf");
			try {
				FileCopyUtils.copy(content, response.getOutputStream());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		} else {
			try {
				response.sendError(HttpServletResponse.SC_NO_CONTENT);
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

	@GetMapping("/search-applicant/export-profile")
	public @ResponseBody String exportProfile(@RequestParam("userIDList") String[] userIDList) {
		ObjectNode json = objectMapper.createObjectNode();
		if (userIDList.length == 0) {
			json.set("status", objectMapper.convertValue("error", JsonNode.class));
		} else {
			json.set("status", objectMapper.convertValue("ok", JsonNode.class));
			json.set("id", objectMapper.convertValue(Arrays.asList(userIDList), JsonNode.class));
		}
		return json.toString();
	}

	@GetMapping("/search-applicant/export-profile/download")
	public @ResponseBody ModelAndView exportProfileDownload(@RequestParam("userIDList") String[] userIDList) {
		List<ProfileBean> profileList = adminDAO.getStudentProfile(userIDList);
		return new ModelAndView(excelXlsxView, "EXCEL_PROFILE_LIST", profileList);
	}

	/** Manage Applications **/
	@GetMapping("/manage-application")
	public ModelAndView manageApplication(ModelMap model, @AuthenticationPrincipal UserImpl user) {
		if (!model.containsAttribute("academicSchoolFrom")) {
			TreeMap<String, String> schools = new TreeMap<>();
			if (user.getRole().equals(Role.ADMIN)) {
				schools.putAll(ProgramCode.ACADEMIC_SCHOOL);
				schools.put("ALL", "All");
			} else {
				schools.put(user.getAffiliation(), ProgramCode.ACADEMIC_SCHOOL.get(user.getAffiliation()));
			}
			model.addAttribute("academicSchoolFrom", schools);
		}

		if (!model.containsAttribute("academicSchoolTo")) {
			TreeMap<String, String> schools = new TreeMap<>();
			schools.putAll(ProgramCode.ACADEMIC_SCHOOL_FOUR);
			schools.put("ALL", "All");
			model.addAttribute("academicSchoolTo", schools);
		}

		if (!model.containsAttribute("applicationBean")) {
			model.addAttribute("applicationBean", new ApplicationBean());
		}
		model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
		model.addAttribute("programs", ProgramCode.PROGRAMS);
		// model.addAttribute("status", model.get("status"));
		return new ModelAndView(VIEW_NMSU_APPLICATION);
	}

	@PostMapping("/manage-application")
	public View nmsuApplicationResult(ModelMap model,
			@ModelAttribute("applicationBean") ApplicationBean applicationBean,
			@RequestParam(value = "completed_only", required = false) String completed, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {

		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return ApplicationBean.class.isAssignableFrom(clazz);
			}

			@Override
			public void validate(Object target, Errors errors) {
				if (applicationBean.getSchoolYear() == 0) {
					errors.rejectValue("schoolYear", "NotBlank.applicationBean.schoolYear", "Please select a year.");
				}
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "programNameAbbr",
						"NotBlank.applicationBean.programNameAbbr", "Please select a program.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "schoolSemester",
						"NotBlank.applicationBean.schoolSemester", "Please select a semester.");
			}
		}.validate(applicationBean, bindingResult);

		RedirectView view = new RedirectView();
		view.setContextRelative(true);
		if (bindingResult.hasErrors()) {
			String FORM_NAME = "applicationBean";
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME;
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("applicationBean", applicationBean);
			view.setUrl("/admin/manage-application");
			return view;
		}

		boolean completedOnly = (completed != null);

		int year = applicationBean.getSchoolYear();
		String semester = applicationBean.getSchoolSemester();
		String program = applicationBean.getProgramNameAbbr();
		String schoolFrom = applicationBean.getAcademicSchool();
		String schoolTarget = applicationBean.getSchoolTarget();
		List<ApplicationBean> applications = adminDAO.getApplicationsBySchoolFromTo(schoolFrom, schoolTarget, year,
				semester, program, completedOnly);

		if (applications.isEmpty()) {
			redirectAttributes.addFlashAttribute("status", "No result found.");
			redirectAttributes.addFlashAttribute("applicationBean", applicationBean);
			view.setUrl("/admin/manage-application");
		} else {
			redirectAttributes.addFlashAttribute("program", program);
			redirectAttributes.addFlashAttribute("schoolSemester", semester);
			redirectAttributes.addFlashAttribute("schoolYear", year);
			redirectAttributes.addFlashAttribute("schoolTarget", schoolTarget);
			redirectAttributes.addFlashAttribute("programNameFull", ProgramCode.PROGRAMS.get(program));
			redirectAttributes.addFlashAttribute("completedOnly", completedOnly);
			view.setUrl(
					String.format("%s?year=%s&semester=%s&program=%s&schoolTarget=%s&completedOnly=%s&schoolFrom=%s",
							"/admin/manage-application-result", year, semester, program, schoolTarget, completedOnly,
							schoolFrom));
		}
		return view;
	}

	@GetMapping("/manage-application-result")
	public String nmsuApplicationDirect(ModelMap model, @RequestParam("program") String program,
			@RequestParam("year") int year, @RequestParam("semester") String semester,
			@RequestParam("schoolTarget") String schoolTarget, @RequestParam("schoolFrom") String schoolFrom,
			@RequestParam("completedOnly") boolean completedOnly, @AuthenticationPrincipal UserImpl ic) {

		// schoolTarget = ALL or others
		// schoolFrom = params if Role is Admin
		if (!ic.getRole().equals(Role.ADMIN)) {
			schoolFrom = ic.getAffiliation();
		}

		List<ApplicationBean> applicationList = adminDAO.getApplicationsBySchoolFromTo(schoolFrom, schoolTarget, year,
				semester, program, completedOnly);
		applicationList.stream().forEach((bean) -> {
			User user = ((UserDAOImpl) userDAO).getProfileStudentBasic(bean.getUserID());
			if (user != null) {
				bean.setFirstName(user.getFirstName());
				bean.setLastName(user.getLastName());
				bean.setEmail(user.getEmail());
				bean.setBirthDate(user.getBirthDate());
			}
		});
		try {
			model.addAttribute("applicationList", objectMapper.writeValueAsString(applicationList));
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		}
		model.addAttribute("program", program);
		model.addAttribute("programNameFull", ProgramCode.PROGRAMS.get(program));
		model.addAttribute("schoolSemester", semester);
		model.addAttribute("schoolYear", year);
		model.addAttribute("schoolTarget", schoolTarget);
		model.addAttribute("completedOnly", completedOnly);
		model.addAttribute("status", model.get("status"));
		return VIEW_NMSU_APPLICATION_RESULT;
	}

	@PostMapping(value = "/manage-application-result/update-decision/{decision}", params = { "year", "semester",
			"program", "appIDList", "schoolTarget" })
	public @ResponseBody String updateApplicationDecision(ModelMap model, @RequestParam("year") int year,
			@RequestParam("semester") String semester, @RequestParam("schoolTarget") String schoolTarget,
			@RequestParam("program") String program, @RequestParam("appIDList") String appIDList,
			@PathVariable("decision") String decision) {
		if ("reset".equals(decision))
			decision = "";
		decision = StringUtil.capitalize(decision);
		try {
			List<String> appIDs = objectMapper.readValue(appIDList, new TypeReference<List<String>>() {
			});
			adminDAO.batchUpdateApplicationDecision(program, appIDs, decision, schoolTarget);
			ObjectNode json = objectMapper.createObjectNode();
			json.set("status", objectMapper.convertValue("ok", JsonNode.class));
			return json.toString();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return "";
	}

	@GetMapping(value = "/manage-application-result/email-decision-resume", params = { "year", "semester", "program",
			"appIDList" })
	public @ResponseBody String emailApplicationDecisionResume(ModelMap model, @RequestParam("year") int year,
			@RequestParam("semester") String semester, @RequestParam("program") String program,
			@RequestParam("appIDList") String appIDList, RedirectAttributes redirectAttributes) {

		List<String> appIDs = new ArrayList<>();
		try {
			appIDs = objectMapper.readValue(appIDList, new TypeReference<List<String>>() {
			});
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		List<String> list = new ArrayList<>();
		List<String> admits = new ArrayList<>();
		List<String> denys = new ArrayList<>();
		List<String> nas = new ArrayList<>();
		List<ApplicationBean> appList = adminDAO.getSelectedApplicationDecision(year, semester, program, appIDs);
		for (ApplicationBean bean : appList) {
			String recipient = String.format("%s %s <%s>", bean.getFirstName(), bean.getLastName(), bean.getEmail());
			if (bean.getDecision() == null) {
				nas.add(recipient);
			} else if (bean.getDecision().equalsIgnoreCase("Admit")) {
				list.add(String.valueOf(bean.getApplicationID()));
				admits.add(recipient);
			} else if (bean.getDecision().equalsIgnoreCase("Deny")) {
				list.add(String.valueOf(bean.getApplicationID()));
				denys.add(recipient);
			} else {
				nas.add(recipient);
			}
		}

		ObjectNode json = objectMapper.createObjectNode();
		json.set("status", objectMapper.convertValue("ok", JsonNode.class));
		json.set("ids", objectMapper.convertValue(list, JsonNode.class));
		json.set("admits",
				objectMapper.convertValue(admits.stream().collect(Collectors.joining(", ")), JsonNode.class));
		json.set("denys", objectMapper.convertValue(denys.stream().collect(Collectors.joining(", ")), JsonNode.class));
		json.set("nas", objectMapper.convertValue(nas.stream().collect(Collectors.joining(", ")), JsonNode.class));
		return json.toString();
	}

	@PostMapping("/manage-application-result/email-decision-do")
	public View emailApplicationDecision(ModelMap model, @RequestParam("schoolTarget") String schoolTarget,
			@RequestParam("schoolYear") int year, @RequestParam("schoolSemester") String semester,
			@RequestParam("program") String program, @RequestParam("decisionIDList") String[] appIDList,
			@RequestParam("completedOnly") String completedOnly,
			@RequestParam("decisionfile") MultipartFile multipartFile, RedirectAttributes redirectAttributes) {

		String response = "";
		if (appIDList == null || appIDList.length == 0) {
			response = "No application is selected or No decision is set!";
		} else {
			List<ApplicationBean> recipients = adminDAO.getDecisionProfile(year, semester, program, appIDList);
			String emails = recipients.stream().map(bean -> bean.getEmail()).collect(Collectors.joining(", "));
			try {
				boolean success = mailService.emailApplicationDecision(program, recipients,
						multipartFile.getOriginalFilename(), multipartFile.getBytes());
				if (success) {
					adminDAO.batchUpdateDecisionNotifiedDate(year, semester, program, appIDList);
					response = "Email sent to " + emails;
				} else {
					response = "Error occurred!";
				}
			} catch (IOException e) {
				response = "Error occurred!";
				logger.error(e.getMessage());
			}
		}
		redirectAttributes.addFlashAttribute("status", response);
		RedirectView view = new RedirectView();
		view.setContextRelative(true);
		view.setUrl(String.format("%s?year=%s&semester=%s&program=%s&schoolTarget=%s&completedOnly=%s",
				"/admin/manage-application-result", year, semester, program, schoolTarget, completedOnly));
		return view;
	}

	@PostMapping("/manage-application-result/send-to-review-committee")
	public View sendApplicationsToReviewCommittee(ModelMap model, RedirectAttributes redirectAttributes,
			@RequestParam("schoolTarget") String schoolTarget, @RequestParam("schoolYear") int year,
			@RequestParam("schoolSemester") String semester, @RequestParam("program") String program,
			@RequestParam("completedOnly") String completedOnly,
			@RequestParam("applicationlist") String[] applicationlist, @RequestParam("emaillist") String[] emaillist,
			@RequestParam("subject") String subject, @RequestParam("message") String message,
			@RequestParam("file1") MultipartFile multipartFile1, @RequestParam("file2") MultipartFile multipartFile2) {

		RedirectView view = new RedirectView();
		view.setContextRelative(true);

		if (emaillist == null || emaillist.length == 0) {
			redirectAttributes.addFlashAttribute("status", "Missing email address!");
		} else {
			String response = "";
			List<String> recipients = Arrays.stream(emaillist).filter(s -> s.length() > 0).collect(Collectors.toList());
			try {
				byte[] zip = getZipApplication(program, Arrays.asList(applicationlist), true);
				boolean success = mailService.emailApplicationToReviewCommittee(program, recipients, subject, message,
						zip, multipartFile1.getOriginalFilename(), multipartFile1.getBytes(),
						multipartFile2.getOriginalFilename(), multipartFile2.getBytes());
				if (success) {
					response = recipients.stream().collect(Collectors.joining(", "));
					String decision = "In Committee Review";
					List<String> appIDs = new ArrayList<>(Arrays.asList(applicationlist));
					adminDAO.batchUpdateApplicationDecision(program, appIDs, decision, schoolTarget);
				} else {
					response = "Error occurred!";
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
				response = "Error occurred!";
			}
			redirectAttributes.addFlashAttribute("status", "Email sent to: " + response);
		}
		view.setUrl(String.format("%s?year=%s&semester=%s&program=%s&schoolTarget=%s&completedOnly=%s",
				"/admin/manage-application-result", year, semester, program, schoolTarget, completedOnly));
		return view;
	}

	@GetMapping(value = "/manage-application-result/download-review-zip", params = { "applicationlist", "emaillist",
			"subject", "message", "program" })
	public void downloadReviewZip(HttpServletResponse response,
			@RequestParam("applicationlist") String[] applicationlist, @RequestParam("emaillist") String[] emaillist,
			@RequestParam("program") String program, @RequestParam("subject") String subject,
			@RequestParam("message") String message) {
		String fileName = program + "-applications-for-review.zip";
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		System.out.println(" I am here !!!");
		try {
			byte[] zip = getZipApplication(program, Arrays.asList(applicationlist), true);
			FileCopyUtils.copy(zip, response.getOutputStream());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private byte[] getZipApplication(String program, List<String> appIDs, boolean forReview) {
		List<ApplicationBean> applications = null;
		if (forReview) {
			applications = adminDAO.getApplicationNMSUforCommitteeReview(program, appIDs);
		} else {
			applications = adminDAO.getApplicationForZip(program, appIDs);
		}

		Path tempDir = null;
		try {
			tempDir = Files.createTempDirectory("temp_nmamp_");
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		}
		String dirPath = tempDir.toString();

		for (ApplicationBean appBean : applications) {
			try {
				// fetch profile
				int userID = appBean.getUserID();
				ProfileBean profileBean = ((UserDAOImpl) userDAO).getProfileStudent(userID);
				if (profileBean == null) {
					profileBean = new ProfileBean();
					BiographyBean biographyBean = new BiographyBean();
					profileBean.setBiographyBean(biographyBean);
					ContactBean contactBean = new ContactBean();
					profileBean.setContactBean(contactBean);
				}

				// build application form
				byte[] pdfBytes = null;
				if (forReview) {
					pdfBytes = iTextPdf.buildPdfDocumentCommittyReview(appBean, profileBean);
				} else {
					// URS: fetch mentor profile
					if (program.equals(ProgramCode.URS) && appBean.getMentorID() != null) {
						MentorBean mentorBean = ((UserDAOImpl) userDAO).getProfileMentorBean(appBean.getMentorID());
						profileBean.setMentorBean(mentorBean);
					}

					pdfBytes = iTextPdf.buildPdfDocument(appBean, profileBean);
				}

				// merge with transcript
				List<byte[]> list = new ArrayList<>();
				list.add(pdfBytes);
				// handle if app not complete: i.e. missing transcript
				if (appBean.getFileBucket() != null) {
					list.add(appBean.getFileBucket().getFileContent());
				}
				if (program.equals(ProgramCode.MESA) && appBean.getReferenceBucket() != null) {
					list.add(appBean.getReferenceBucket().getFileContent());
				}
				if (program.equals(ProgramCode.TRANS) && appBean.getRecommenderBean() != null) {
					list.add(appBean.getRecommenderBean().getRecommendationContent());
				}
				if (program.equals(ProgramCode.SCCORE) && appBean.getReferenceBucket() != null) {
					list.add(appBean.getReferenceBucket().getFileContent()); // medical form
				}

				// school-current-in, school-apply-to
				String pdfFileName = String.format("%s_%s_%s_%s_%s_%d.pdf",
						profileBean.getBiographyBean().getLastName(), profileBean.getBiographyBean().getFirstName(),
						appBean.getAcademicBean() == null || appBean.getAcademicBean().getAcademicSchool() == null
								? "NA"
								: appBean.getAcademicBean().getAcademicSchool(),
						appBean.getSchoolTarget() == null ? "NA" : appBean.getSchoolTarget(), program,
						appBean.getApplicationID());
				File file = new File(dirPath, pdfFileName);
				FileOutputStream fileOutputStream = new FileOutputStream(file);

				iTextPdf.doMerge(list, fileOutputStream);
			} catch (IOException | DocumentException e) {
				e.printStackTrace();
			}
		}

		File[] files = tempDir.toFile().listFiles();
		try {
			byte[] zip = IOUtils.zipFiles(files);
			FileUtils.deleteDirectory(tempDir.toFile());
			return zip;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@GetMapping("/manage-application-result/export-application-excel")
	public @ResponseBody ModelAndView exportSelectedApplicationExcel(@RequestParam("year") int year,
			@RequestParam("semester") String semester, @RequestParam("program") String program,
			@RequestParam("appIDList") String appIDList) {
		ModelAndView modelView = new ModelAndView(excelXlsxView);
		try {
			List<String> appIDs = objectMapper.readValue(appIDList, new TypeReference<List<String>>() {
			});
			List<ApplicationBean> applicationList = adminDAO.getSelectedApplicationForExcel(year, semester, program,
					appIDs);
			modelView.addObject("program", program);
			modelView.addObject("EXCEL_APPLICATION_LIST", applicationList);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return modelView;
	}

	@RequestMapping(value = "/manage-application-result/export-application-zip")
	public void exportSelectedApplicationZip(@RequestParam("year") int year, @RequestParam("semester") String semester,
			@RequestParam("program") String program, @RequestParam("appIDList") String appIDList,
			HttpServletResponse response) {
		String fileName = "export_" + LocalDate.now().toString() + ".zip";
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		byte[] zip;
		try {
			List<String> appIDs = objectMapper.readValue(appIDList, new TypeReference<List<String>>() {
			});
			zip = getZipApplication(program, appIDs, false);
			FileCopyUtils.copy(zip, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@GetMapping("manage-application-result/download-application-transcript")
	public void downloadSelectedTranscript(HttpServletResponse response, @RequestParam("program") String program,
			@RequestParam("appID") String appID) {
		List<ApplicationBean> applications = adminDAO.getApplicationNMSUforCommitteeReview(program,
				Collections.singletonList(appID));
		if (applications.size() == 1 && applications.get(0).getFileBucket() != null) {
			byte[] content = applications.get(0).getFileBucket().getFileContent();
			if (content != null && content.length > 0) {
				String fileName = String.format("%s_transcript.pdf", appID);
				response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.toLowerCase() + "\"");
				response.setContentType("application/pdf");
				try {
					FileCopyUtils.copy(content, response.getOutputStream());
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			} else {
				try {
					response.sendError(HttpServletResponse.SC_NO_CONTENT);
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

	@GetMapping("manage-application-result/download-application-reference")
	public void downloadSelectedReference(HttpServletResponse response, @RequestParam("program") String program,
			@RequestParam("appID") String appID) {
		if (!ProgramCode.TRANS.equals(program) && !ProgramCode.MESA.equals(program)) {
			return;
		}

		List<ApplicationBean> applications = adminDAO.getApplicationNMSUforCommitteeReview(program,
				Collections.singletonList(appID));
		if (applications.size() != 1)
			return;
		byte[] content = null;
		if (ProgramCode.MESA.equals(program)) {
			if (applications.get(0).getReferenceBucket() != null) {
				content = applications.get(0).getReferenceBucket().getFileContent();
			}
		} else {
			if (applications.get(0).getRecommenderBean() != null) {
				content = applications.get(0).getRecommenderBean().getRecommendationContent();
			}
		}
		if (content != null && content.length > 0) {
			String fileName = String.format("%s_letter_of_recommendation.pdf", appID);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.toLowerCase() + "\"");
			response.setContentType("application/pdf");
			try {
				FileCopyUtils.copy(content, response.getOutputStream());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

	@GetMapping("manage-application-result/download-application-medical-form")
	public void downloadSelectedMedicalForm(HttpServletResponse response, @RequestParam("program") String program,
			@RequestParam("appID") String appID) {
		if (!ProgramCode.SCCORE.equals(program))
			return;

		List<ApplicationBean> applications = adminDAO.getApplicationNMSUforCommitteeReview(program,
				Collections.singletonList(appID));
		if (applications.size() != 1)
			return;
		byte[] content = null;
		if (applications.get(0).getReferenceBucket() != null) {
			content = applications.get(0).getReferenceBucket().getFileContent();
		}
		if (content != null && content.length > 0) {
			String fileName = String.format("%s_medical_form.pdf", appID);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.toLowerCase() + "\"");
			response.setContentType("application/pdf");
			try {
				FileCopyUtils.copy(content, response.getOutputStream());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

	@PostMapping("/manage-application-result/delete-application")
	public @ResponseBody String deleteApplication(@RequestParam("year") int year,
			@RequestParam("semester") String semester, @RequestParam("program") String program,
			@RequestParam("applicationID") String applicationID) {
		boolean status = adminDAO.deleteApplication(program, applicationID);
		ObjectNode json = objectMapper.createObjectNode();
		if (status) {
			json.set("status", objectMapper.convertValue("ok", JsonNode.class));
		} else {
			json.set("status", objectMapper.convertValue("error", JsonNode.class));
		}
		return json.toString();
	}

	/*
	 * Functions added by qixu
	 */
	@GetMapping(value = "/manage-application-result/application-preview", params = { "applicationID", "program" })
	public String PreviewApplicationDetailsByID(ModelMap model, @RequestParam("applicationID") String applicationID,
			@RequestParam("program") String program) {
		// System.out.println(applicationID);
		// System.out.println(program);

		ApplicationBean appBean = adminDAO.getApplicationByApplicationID(applicationID, program);
		// System.out.println(appBean.getApplicationID());

		int userID = appBean.getUserID();
		ProfileBean profileBean = ((UserDAOImpl) userDAO).getProfileStudent(userID);
		if (profileBean == null) {
			profileBean = new ProfileBean();
			BiographyBean biographyBean = new BiographyBean();
			profileBean.setBiographyBean(biographyBean);
			ContactBean contactBean = new ContactBean();
			profileBean.setContactBean(contactBean);
		} else {
			appBean.setFirstName(profileBean.getBiographyBean().getFirstName());
			appBean.setLastName(profileBean.getBiographyBean().getLastName());
			appBean.setEmail(profileBean.getContactBean().getEmailPref());
			appBean.setBirthDate(profileBean.getBiographyBean().getBirthDate());
			
			if (program.equals(ProgramCode.URS) && appBean.getMentorID() != null) {
				MentorBean mentorBean = ((UserDAOImpl) userDAO).getProfileMentorBean(appBean.getMentorID());
				profileBean.setMentorBean(mentorBean);
			}

		}

		// try {
		// model.addAttribute("application", objectMapper.writeValueAsString(appBean));
		// System.out.println(objectMapper.writeValueAsString(appBean));
		// } catch (JsonProcessingException e) {
		// logger.error(e.getMessage());
		// }
		// System.out.println(profileBean.getEthnicityBean().getRace());
		model.addAttribute("applicationBean", appBean.appBean());
		model.addAttribute("academicBean", appBean.getAcademicBean());
		model.addAttribute("profileBean", profileBean);
		// System.out.println(appBean.get);
		model.addAttribute("program", program);
		model.addAttribute("programNameFull", ProgramCode.PROGRAMS.get(program));
		// model.addAttribute("schoolTarget", schoolTarget);
		// model.addAttribute("completedOnly", completedOnly);
		model.addAttribute("status", model.get("status"));

		return "/admin/application-preview-" + program;
	}

}
