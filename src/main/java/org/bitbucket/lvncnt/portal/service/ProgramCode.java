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

import java.util.*;

public class ProgramCode {

	// AMP program name
	public static final String CCCONF = "CCCONF";
	public static final String SCCORE = "SCCORE";
	public static final String TRANS = "TRANS";
	public static final String MESA = "MESA";
	public static final String URS = "URS";
	public static final String IREP = "IREP";

	public static final HashMap<String, String> TABLE_APPLICATION_DETAIL = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(CCCONF, "application_ccconf");
			put(MESA, "application_mesa");
			put(SCCORE, "application_sccore");
			put(TRANS, "application_trans");
			put(URS, "application_urs");
			put(IREP, "application_irep");
		}
	};
	
	public static final HashMap<String, String> TABLE_EVALUATION = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{ 
			put(EVALUATION_POINT.MIDTERM.toString(), "evaluation_midterm");
			put(EVALUATION_POINT.ENDOFSEMESTER.toString(), "evaluation_endofsemester");
		}
	};
	
	public static final TreeMap<String, String> PROGRAMS = new TreeMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(MESA, "New Mexico AMP MESA Scholarship");
			put(URS, "New Mexico AMP Undergraduate Research Scholars (URS)"); 
			put(IREP, "New Mexico AMP International Research and Education Participation"); 
			put(CCCONF, "New Mexico AMP Community College Professional Development Workshop Stipend");
			put(SCCORE, "New Mexico AMP Summer Community College Opportunity for Research Experience (SCCORE)"); 
			put(TRANS, "New Mexico AMP Transfer Scholarship");
		}
	};
	
	public static final HashMap<String, String> PROGRAMS_SUB = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(CCCONF, "Community College Professional Development Workshop Stipend");
			put(SCCORE, "Summer Community College Opportunity for Research Experience (SCCORE)"); 
			put(TRANS, "Transfer Scholarship");
			put(MESA, "MESA Scholarship");
			put(URS, "Undergraduate Research Scholars (URS)"); 
			put(IREP, "International Research and Education Participation"); 
		}
	};
	
	public static final TreeMap<String, String> PROGRAMS_TWO_YR = new TreeMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(CCCONF, "New Mexico AMP Community College Professional Development Workshop Stipend");
			put(SCCORE, "New Mexico AMP Summer Community College Opportunity for Research Experience (SCCORE)"); 
			put(TRANS, "New Mexico AMP Transfer Scholarship");
		}
	};
	
	public static final TreeMap<String, String> PROGRAMS_FOUR_YR = new TreeMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(IREP, "New Mexico AMP International Research and Education Participation"); 
			put(MESA, "New Mexico AMP MESA Scholarship");
			put(URS, "New Mexico AMP Undergraduate Research Scholars (URS)"); 
		}
	};
 
	// academic school
	/*
	public static final TreeMap<String, String> ACADEMIC_SCHOOL_SCCORE = new TreeMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("CTLNMC", "Central New Mexico College");
			put("LUNACC", "Luna Community College");
			put("NMSUAM", "New Mexico State University - Alamogordo");
			put("NMSUCB", "New Mexico State University - Carlsbad");
			put("NMSUDA", "New Mexico State University - Dona Ana");
			put("NMSUGS", "New Mexico State University - Grants");
            put("SFCC", "Santa Fe Community College");
			put("SWIPTI", "Southwestern Indian Polytechnic Institute");
		}
	};*/
	
	public static final TreeMap<String, String> ACADEMIC_SCHOOL = new TreeMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("CNMCC", "Central New Mexico Community College");
			put("ENMUR", "Eastern New Mexico University - Roswell");
			put("LUNACC", "Luna Community College");
			put("NMSUA", "New Mexico State University - Alamogordo"); 
			put("NMSUC", "New Mexico State University - Carlsbad Branch"); 
			put("NMSUD", "New Mexico State University - Dona Ana Branch"); 
			put("NMSUG", "New Mexico State University - Grants Campus"); 
			put("SFCC", "Santa Fe Community College");
			put("SIPI", "Southwestern Indian Polytechnic Institute");
            put("UNMTAOS", "University of New Mexico - Taos");

			put("ENMU", "Eastern New Mexico University");
			put("NMHU", "New Mexico Highlands University");
			put("NMTECH", "New Mexico Inst of Mining and Technology");
			put("NMSU", "New Mexico State University");
			put("NNMC", "Northern New Mexico College");
			put("UNM", "University of New Mexico");
			put("WNMU", "Western New Mexico University");
		}
	};
 		  
	public static final TreeMap<String, String> ACADEMIC_SCHOOL_TWO = new TreeMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("CNMCC", "Central New Mexico Community College");
				put("ENMUR", "Eastern New Mexico University - Roswell");
				put("LUNACC", "Luna Community College");
				put("NMSUA", "New Mexico State University - Alamogordo"); 
				put("NMSUC", "New Mexico State University - Carlsbad Branch"); 
				put("NMSUD", "New Mexico State University - Dona Ana Branch"); 
				put("NMSUG", "New Mexico State University - Grants Campus"); 
				put("SFCC", "Santa Fe Community College");
				put("SIPI", "Southwestern Indian Polytechnic Institute");
				put("UNMTAOS", "University of New Mexico - Taos");
			}
		};
	  
	public static final TreeMap<String, String> ACADEMIC_SCHOOL_FOUR = new TreeMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("ENMU", "Eastern New Mexico University");
			put("NMHU", "New Mexico Highlands University");
			put("NMTECH", "New Mexico Inst of Mining and Technology");
			put("NMSU", "New Mexico State University");
			put("NNMC", "Northern New Mexico College");
			put("UNM", "University of New Mexico");
			put("WNMU", "Western New Mexico University");
		}
	};

	 
	// academic term
	public static final String[] ACADEMIC_SEMESTER = { "Fall", "Spring",
			"Summer" };

	// academic year
	public static final HashMap<String, String> ACADEMIC_YEAR = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("FRESHMAN", "Freshman");
			put("SOPHOMORE", "Sophomore");
			put("JUNIOR", "Junior");
			put("SENIOR", "Senior");
		}
	};

	// academic status for CCCONF program
	public static final HashMap<String, String> ACADEMIC_STATUS = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("FULLTIME", "Full Time");
			put("PARTTIME", "Part Time");
		}
	};

	// academic credit for CCCONF program
	public static final HashMap<String, String> ACADEMIC_CREDIT = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("0-30", "0 - 30");
			put(">30", "> 30");
		}
	};

	// ScholarshipType
	public static final HashMap<String, String> SCHOLARSHIP_TYPE = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("SSTEM", "S-STEM");
			put("TRANS", "Transfer Scholarship");
			put("SCCORE", "SCCORE");
			put("MESA", "MESA");
			put("CCCONF", "CC Conference Stipend");
			put("IREP", "IREP");
		}
	};
	
	public static final HashMap<String, String> SCHOLARSHIP_TYPE_CCCONF = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("CCSTIPEND", "Community College Stipend");
			put("MESA", "MESA");
			put("SCCORE", "SCCORE");
			put("OTHER", "Other");
		}
	};

	// NMSU or an NMSU branch campus for CCCORE program
	public static final TreeMap<String, String> NMSU_CAMPUS = new TreeMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("LAS_CRUCES", "Las Cruces (Main)");
			put("ALAMOGORDO", "Alamogordo (Comm College)");
			put("CARLSBAD", "Carlsbad (Comm College)");
			put("DONA_ANA", "Dona Ana (Comm College)");
		}
	};

	// DISABILITY STATUS
	public static final HashMap<String, String> DISABILITY_STATUS = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("YES", "Yes");
			put("NO", "No");
			put("OT", "Do not wish to provide");
		}
	};
	
	public static final HashMap<String, String> CITIZENSHIP = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("US", "U.S. Citizen");
			put("PR", "Permanent Resident");
			put("OT", "Other Non-Citizen");
		}
	};
	
	public static final HashMap<String, String> YES_NO = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("1", "Yes");
			put("0", "No");
		}
	};
	
	public static final HashMap<String, String> GENDER = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("F", "Female");
			put("M", "Male");
		}
	};
	
	public static final String RACE_AFRICANAMERICAN = "AFRICANAMERICAN"; 
	public static final String RACE_AMERICANINDIAN = "AMERICANINDIAN"; 
	public static final String RACE_ALASKANATIVE = "ALASKANATIVE"; 
	public static final String RACE_ASIAN = "ASIAN"; 
	public static final String RACE_NATIVEHAWAIIAN = "NATIVEHAWAIIAN"; 
	public static final String RACE_WHITE = "WHITE"; 
	public static final HashMap<String, String> CHECKBOX_RACE = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(RACE_AFRICANAMERICAN, "African American or Black");
			put(RACE_AMERICANINDIAN, "American Indian");
			put(RACE_ALASKANATIVE, "Alaska Native");
			put(RACE_ASIAN, "Asian");
			put(RACE_NATIVEHAWAIIAN, "Native Hawaiian or Other Pacific Islander");
			put(RACE_WHITE, "White");
		}
	};
 
	// phone types
	public static final String[] PHONE_TYPES = { "Cell", "Home", "Work", "Other" };

	// states
	public static final String[] STATES = { "Alabama", "Alaska",
			"American Samoa", "Arizona", "Arkansas", "California", "Colorado",
			"Connecticut", "Delaware", "Dist of Columbia", "Florida",
			"Georgia", "Guam", "Hawaii", "Idaho", "Illinois", "Indiana",
			"Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland",
			"Massachusetts", "Michigan", "Minnesota", "Mississippi",
			"Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire",
			"New Jersey", "New Mexico", "New York", "North Carolina",
			"North Dakota", "Northern Mariana Islands", "Ohio", "Oklahoma",
			"Oregon", "Pennsylvania", "Puerto Rico", "Rhode Island",
			"South Carolina", "South Dakota", "Tennessee", "Texas", "Utah",
			"Vermont", "Virgin Islands", "Virginia", "Washington",
			"West Virginia", "Wisconsin", "Wyoming" };
	
	// application status
	public static final String[] APPLICATION_STATUS = { "Started", "Completed",
			"Review", "Admitted", "Denied" };

	// mentor prefix
	public static final HashMap<String, String> MENTOR_PREFIX = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("Dr.", "Dr.");
			put("Mr.", "Mr.");
			put("Ms.", "Ms.");
		}
	};

	//Todo: Need to add withdraw massage
	public static String getDecisionMessage(String program, String decision){
		switch (program.toUpperCase()) {
		case CCCONF:
			return decision.equalsIgnoreCase("admit")?"Document.ccconf.admit":"Document.ccconf.deny"; 
		case MESA:
			return decision.equalsIgnoreCase("admit")?"Document.mesa.admit":"Document.mesa.deny"; 
		case TRANS:
			return decision.equalsIgnoreCase("admit")?"Document.trans.admit":"Document.trans.deny";  
		case SCCORE:
			return decision.equalsIgnoreCase("admit")?"Document.sccore.admit":"Document.sccore.deny"; 
		case URS:
			return decision.equalsIgnoreCase("admit")?"Document.urs.admit":"Document.urs.deny"; 
		case IREP:
			return decision.equalsIgnoreCase("admit")?"Document.irep.admit":"Document.irep.deny"; 
		default:
			return null;
		}
	}
	
	public static enum EVALUATION_POINT {
		MIDTERM("Mid-Term"), ENDOFSEMESTER("End-Of-Semester");

	    private final String value;
 
	    private EVALUATION_POINT(final String value) {
	        this.value = value;
	    }
 
	    @Override
	    public String toString() {
	        return this.value;
	    }
	}

	public static int newApplicationID(String userID, String schoolYear) {
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		int millisecond = calendar.get(Calendar.MILLISECOND);
		return Integer.parseInt(schoolYear + "" + userID.substring(userID.length() - 2) + ""
				+ String.valueOf(millisecond + 1000).substring(1));
		 
	}
}
