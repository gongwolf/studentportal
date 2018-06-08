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
package org.bitbucket.lvncnt.portal.service;

import org.bitbucket.lvncnt.portal.model.ApplicationBean;
import org.bitbucket.lvncnt.portal.model.EvaluationBean;
import org.bitbucket.lvncnt.portal.model.RecommenderBean;
import org.bitbucket.lvncnt.portal.model.User;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("mailService")
@PropertySource("classpath:mail.properties")
public class MailService{

	@Autowired
	JavaMailSender mailSender; 
	
	@Autowired
	Configuration freemarkerConfiguration; 
	
	@Autowired
	private MessageSource messageSource; 
	
	@Value("${javamail.bcc}")
	private String bcc; 
	
	@Value("${javamail.from.address}")
	private String addressFrom; 
	
	@Value("${javamail.from.personal}")
	private String addressPersonal; 
	
	@Value("${javamail.subject.recommendation.request}")
	private String subjectRecommendationRequest; 
	
	@Value("${javamail.subject.resetpassword}")
	private String subjectResetpassword; 
	
	@Value("${javamail.subject.urs.project}")
	private String subjectURSProject; 
	
	@Value("${javamail.subject.irep.project}")
	private String subjectIREPProject; 
	
	@Value("${javamail.subject.application.decision}")
	private String subjectApplicationDecision; 
	
	@Value("${javamail.subject.urs.eval.mid}")
	private String subjectURSEvalMid;  
	
	@Value("${javamail.subject.urs.eval.end}")
	private String subjectURSEvalEnd;  
	
	
	private static SimpleDateFormat FORMAT_DATE_MMDY = new SimpleDateFormat("MMMM dd, yyyy");
	private static SimpleDateFormat FORMAT_DATE_YYYY = new SimpleDateFormat("yyyy");
	
	/**
	 * Send reference request to recommender 
	 */
	public boolean sendMailRecommendationRequest(RecommenderBean bean, String deadline) {
		String ftlTemplate = "recommendation-template.ftl"; 
		String to = bean.getEmail(); 
		
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
				helper.setSubject(subjectRecommendationRequest);
				helper.setFrom(new InternetAddress(addressFrom, addressPersonal));
				helper.setTo(to);
				helper.setBcc(bcc);
				
				Map<String, Object> model = new HashMap<>();
				model.put("bean", bean);
				model.put("deadline", deadline); 
				String text = getFreeMarkerTemplateContent(ftlTemplate, model); 
				helper.setText(text, true);
			}
		};
		
		try{
			mailSender.send(preparator);
			return true; 
		}catch(MailException e){
			return false; 
		}
	}
	
	private MimeMessagePreparator getMessagePreparator(String to, String subject, 
			String ftlTemplate, Object bean){
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
				helper.setSubject(subject);
				helper.setFrom(new InternetAddress(addressFrom, addressPersonal));
				helper.setTo(to);
				helper.setBcc(bcc);
				
				Map<String, Object> model = new HashMap<>(); 
				model.put("bean", bean); 
			
				String text = getFreeMarkerTemplateContent(ftlTemplate, model);
				helper.setText(text, true);
				
			}
		};
		return preparator; 
	}
	
	private String getFreeMarkerTemplateContent(String ftlTemplate, Map<String, Object> model){
		StringBuilder content = new StringBuilder(); 
		try {
			content.append(FreeMarkerTemplateUtils.processTemplateIntoString(
					freemarkerConfiguration.getTemplate(ftlTemplate), model));
			return content.toString(); 
		} catch (IOException | TemplateException e) {
			e.printStackTrace();
		}
		return ""; 
	}

	/**
	 * Resetpassword
	 */
	public boolean sendMailResetpassword(String to, String url) {
		String ftlTemplate = "resetpassword-template.ftl"; 
		MimeMessagePreparator preparator = getMessagePreparator(to, subjectResetpassword, 
				ftlTemplate, url); 
		try{
			mailSender.send(preparator);
			return true; 
		}catch(MailException e){
			return false; 
		}
	}
	
	public boolean sendMailProjectProposalToMentor(String menteeName, String term, 
			User bean, String deadline) {
		String ftlTemplate = "urs-project-to-mentor-template.ftl"; 
		String to = bean.getEmail(); 
		MimeMessagePreparator preparator = getMessagePreparatorURS(to, subjectURSProject, 
				ftlTemplate, menteeName, term, bean, deadline); 
		try{
			mailSender.send(preparator);
			return true; 
		}catch(MailException e){
			return false; 
		}
	}
	private MimeMessagePreparator getMessagePreparatorURS(String to, String subject, 
			String ftlTemplate, String menteeName, String term, Object bean, String deadline){
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
				helper.setSubject(subject);
				helper.setFrom(new InternetAddress(addressFrom, addressPersonal));
				helper.setTo(to);
				helper.setBcc(bcc);
				
				Map<String, Object> model = new HashMap<>(); 
				model.put("bean", bean); 
				model.put("menteeName", menteeName); 
				model.put("term", term); 
				model.put("deadline", deadline); 
				
				String text = getFreeMarkerTemplateContent(ftlTemplate, model);
				helper.setText(text, true);
				
			}
		};
		return preparator; 
	}
	
	public boolean sendMailProjectProposalToMentee(ApplicationBean bean, String deadline) {
		String ftlTemplate = "urs-project-to-mentee-template.ftl"; 
		String to = bean.getEmail(); 
		MimeMessagePreparator preparator = getMessagePreparatorProjectProposalToMentee(to, subjectURSProject, 
				ftlTemplate, bean, deadline); 
		try{
			mailSender.send(preparator);
			return true; 
		}catch(MailException e){
			return false; 
		}
	}
	private MimeMessagePreparator getMessagePreparatorProjectProposalToMentee(String to, String subject, 
			String ftlTemplate, Object bean, String deadline){
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
				helper.setSubject(subject);
				helper.setFrom(new InternetAddress(addressFrom, addressPersonal));
				helper.setTo(to);
				helper.setBcc(bcc);
				
				Map<String, Object> model = new HashMap<>(); 
				model.put("bean", bean); 
				model.put("deadline", deadline); 
			
				String text = getFreeMarkerTemplateContent(ftlTemplate, model);
				helper.setText(text, true);
			}
		};
		return preparator; 
	}
	/**
	 * Email application decision to students in batch 
	 * Send the given array of simple mail messages in batch.
	 */
	public boolean emailApplicationDecision(String program, List<ApplicationBean> beanList, 
			String attachmentName, byte[] attachmentBytes){
		TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));
		List<MimeMessagePreparator> mimeMessages = new ArrayList<>(); 
		MimeMessagePreparator preparator = null; 
		for(ApplicationBean bean: beanList){
			String decision = bean.getDecision(); 
			final String template = messageSource.getMessage(ProgramCode.getDecisionMessage(program, decision)
					, null, Locale.ENGLISH); 
			preparator = new MimeMessagePreparator() {
				
				@Override
				public void prepare(MimeMessage mimeMessage) throws Exception {
					MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
					helper.setSubject(subjectApplicationDecision);
					helper.setFrom(new InternetAddress(addressFrom, addressPersonal));
					helper.setTo(bean.getEmail());
					helper.setBcc(bcc);
					
					Map<String, Object> model = new HashMap<>(); 
					model.put("bean", bean); 
					String date = FORMAT_DATE_MMDY.format(new Date());
					String year = FORMAT_DATE_YYYY.format(new Date());
					model.put("date", date); 
					model.put("year", year); 
					
					String htmlText = getFreeMarkerTemplateContent(template, model);
					helper.setText(htmlText, true);
				 
					if(attachmentBytes.length > 0){
						helper.addAttachment(attachmentName, new ByteArrayResource(attachmentBytes));
					}

				}
			};
			mimeMessages.add(preparator); 
		}
		 
		try{
			mailSender.send(mimeMessages.toArray(
					new MimeMessagePreparator[mimeMessages.size()]));
			return true; 
		}catch(MailException e){
			e.printStackTrace();
			return false; 
		}
	}
	
	/**
	 * Email application to review committee.
	 */
	public boolean emailApplicationToReviewCommittee(String program, List<String> recipients, 
			String subject, String message, byte[] attachmentBytes, 
			String attachment2Name, byte[] attachment2Bytes, 
			String attachment3Name, byte[] attachment3Bytes){
		String[] emailList = new String[recipients.size()]; 
		for(int i = 0; i < recipients.size(); i++){
			emailList[i] = recipients.get(i); 
		}
		
		StringBuffer html = new StringBuffer();
		html.append("<html>");
		html.append("<head></head>");
		html.append("<body><div><br/>");
		html.append(message.replaceAll("\n", "<br/>"));
		html.append("</div></body></html>");
 
			MimeMessagePreparator preparator = new MimeMessagePreparator() {
				
				@Override
				public void prepare(MimeMessage mimeMessage) throws Exception {
					MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
					helper.setSubject(subject);
					helper.setFrom(new InternetAddress(addressFrom, addressPersonal));
					helper.setTo(emailList);
					helper.setBcc(bcc);
					helper.setText(html.toString(), true);
				 
					InputStreamSource attachmentSource = new ByteArrayResource(attachmentBytes);
					String attachmentFilename = program + "-applications-for-review.zip"; 
					helper.addAttachment(attachmentFilename, attachmentSource);
					if(attachment2Bytes.length > 0){
						helper.addAttachment(attachment2Name, new ByteArrayResource(attachment2Bytes));
					}
					if(attachment3Bytes.length > 0){
						helper.addAttachment(attachment3Name, new ByteArrayResource(attachment3Bytes));
					}
				}
			};
		try{
			mailSender.send(preparator);  
			return true; 
		}catch(MailException e){
			return false; 
		}
	}
	 
	/**
	 * Send evaluation request to mentor 
	 */
	public boolean sendMailMentorEvaluationRequest(EvaluationBean bean) {
		String ftlTemplate = "mentor-evaluation-template.ftl"; 
		String to = bean.getMentorEmail(); 
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
				String evalPoint = ""; 
				if(bean.getEvalPoint().equalsIgnoreCase(ProgramCode.EVALUATION_POINT.MIDTERM.toString())){
					helper.setSubject(subjectURSEvalMid);
					evalPoint = "Mid Term"; 
				}else if(bean.getEvalPoint().equalsIgnoreCase(ProgramCode.EVALUATION_POINT.ENDOFSEMESTER.toString())){
					helper.setSubject(subjectURSEvalEnd);
					evalPoint = "End Of Semester"; 
				}
				
				helper.setFrom(new InternetAddress(addressFrom, addressPersonal));
				helper.setTo(to);
				helper.setBcc(bcc);
				
				Map<String, Object> model = new HashMap<>();
				model.put("bean", bean); 
				model.put("evalPoint", evalPoint); 
				
				String text = getFreeMarkerTemplateContent(ftlTemplate, model);
				helper.setText(text, true);
			}
		};
	 
		try{
			mailSender.send(preparator);
			return true; 
		}catch(MailException e){
			return false; 
		}
	}
	
	/**
	 * Send evaluation request to Student 
	 */
	public boolean sendMailStudentEvaluationRequest(EvaluationBean bean) {
		String ftlTemplate = "student-evaluation-template.ftl"; 
		String to = bean.getMenteeEmail(); 
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
				String evalPoint = ""; 
				if(bean.getEvalPoint().equalsIgnoreCase(ProgramCode.EVALUATION_POINT.MIDTERM.toString())){
					helper.setSubject(subjectURSEvalMid);
					evalPoint = "Mid Term"; 
				}else if(bean.getEvalPoint().equalsIgnoreCase(ProgramCode.EVALUATION_POINT.ENDOFSEMESTER.toString())){
					helper.setSubject(subjectURSEvalEnd);
					evalPoint = "End Of Semester"; 
				}
				
				helper.setFrom(new InternetAddress(addressFrom, addressPersonal));
				helper.setTo(to);
				helper.setBcc(bcc);
				
				Map<String, Object> model = new HashMap<>();
				model.put("bean", bean); 
				model.put("evalPoint", evalPoint); 
				
				String text = getFreeMarkerTemplateContent(ftlTemplate, model);
				helper.setText(text, true);
			}
		};
	 
		try{
			mailSender.send(preparator);
			return true; 
		}catch(MailException e){
			return false; 
		}
	}
	
	/**
	 * Send email to admin after sccore application is complete 
	 */
	public boolean sendMailToAdminSCCOREComplete(String name, String email, String program, 
			String term, String date, String size) {
		String ftlTemplate = "application-complete-to-admin-template.ftl"; 
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
				helper.setSubject(String.format("Submitted Application: %s - %s", program, term));
				helper.setFrom(new InternetAddress(addressFrom, addressPersonal));
				helper.setTo(bcc);
		 
				Map<String, Object> model = new HashMap<>();
				model.put("name", name);
				model.put("email", email); 
				model.put("program", program); 
				model.put("term", term); 
				model.put("date", date); 
				model.put("size", size); 
				String text = getFreeMarkerTemplateContent(ftlTemplate, model); 
				helper.setText(text, true);
			}
		};
	 
		try{
			mailSender.send(preparator);
			return true; 
		}catch(MailException e){
			return false; 
		}
	}
	
	/**
	 * Send email to student after application (for program other than tran and urs) is complete 
	 */
	public boolean sendMailToStudentComplete(String name, String email, String program, 
			String programFull, String term, String deadline) {
		String ftlTemplate; 
		switch (program) {
		case ProgramCode.TRANS:
			ftlTemplate = "application-complete-to-student-trans.ftl"; 
			break; 
		case ProgramCode.URS:
			ftlTemplate = "application-complete-to-student-urs.ftl"; 
			break; 
		case ProgramCode.CCCONF:
		case ProgramCode.MESA:
		case ProgramCode.SCCORE:
		default:
			ftlTemplate = "application-complete-to-student-template.ftl"; 
			break; 
		}
		 
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
				helper.setSubject(String.format("Your Application to the  %s %s program", term, programFull));
				helper.setFrom(new InternetAddress(addressFrom, addressPersonal));
				helper.setTo(email);
				helper.setBcc(bcc);
				
				Map<String, Object> model = new HashMap<>();
				model.put("name", name);
				model.put("programFull", programFull);
				model.put("term", term); 
				model.put("deadline", deadline); 
				String text = getFreeMarkerTemplateContent(ftlTemplate, model); 
				helper.setText(text, true);
			}
		};
	 
		try{
			mailSender.send(preparator);
			return true; 
		}catch(MailException e){
			return false; 
		}
	}
	
	public boolean sendMailProjectAbstractToMentor(String term, String url, String deadline, 
			String menteeName, String mentorName, String mentorEmail) {
		String ftlTemplate = "irep-project-to-mentor-template.ftl"; 
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
				helper.setSubject(subjectIREPProject);
				helper.setFrom(new InternetAddress(addressFrom, addressPersonal));
				helper.setTo(mentorEmail);
				helper.setBcc(bcc);
				
				Map<String, Object> model = new HashMap<>(); 
				model.put("mentorName", mentorName); 
				model.put("menteeName", menteeName); 
				model.put("term", term); 
				model.put("url", url); 
				model.put("deadline", deadline); 
				
				String text = getFreeMarkerTemplateContent(ftlTemplate, model);  
				helper.setText(text, true);
			}
		};
		try{
			mailSender.send(preparator);
			return true; 
		}catch(MailException e){
			return false; 
		}
	}
 
 
}
