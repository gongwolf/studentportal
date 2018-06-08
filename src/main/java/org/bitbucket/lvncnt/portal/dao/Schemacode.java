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
package org.bitbucket.lvncnt.portal.dao;

public interface Schemacode {
	
	static final String TABLE_APPLICATION_LIST = "application_list";
	
	static final String TABLE_EVAL_MIDTERM = "evaluation_midterm";
	static final String TABLE_EVAL_END = "evaluation_endofsemester";
	
	static final String TABLE_EVAL_MIDTERM_STU = "evaluation_midterm_stu";
	static final String TABLE_EVAL_END_STU = "evaluation_endofsemester_stu";
	
	static final String TABLE_PROFILE_STUDENT = "profile_student"; 
	static final String TABLE_PROFILE_MENTOR = "profile_mentor"; 
	static final String TABLE_PROFILE_STAFF = "profile_staff"; 
	
	
	static final String TABLE_PROGRAM = "programs"; 
	static final String TABLE_USER = "user"; 
	
	static final String TABLE_SELFREPORT_MANAGE = "selfreport_manage"; 
	static final String TABLE_SELFREPORT_DATA = "selfreport_data";

}
