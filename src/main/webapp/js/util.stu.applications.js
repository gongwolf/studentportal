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

$(document)
		.ready(
				function() {
					moment.tz
							.add("America/Denver|MST MDT MWT MPT|70 60 60 60|01010101023010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010|-261r0 1nX0 11B0 1nX0 11B0 1qL0 WN0 mn0 Ord0 8x20 ix0 LCN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1fz0 1cN0 1cL0 1cN0 1cL0 s10 1Vz0 LB0 1BX0 1cN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0|26e5");
					var table = $('#applications')
							.DataTable(
									{
										"aaData" : data,
										'columnDefs' : [ {
											'targets' : 4,
											'searchable' : false,
											'orderable' : false,
											'className' : 'dt-body-center',
											'render' : function(data, type,
													full, meta) {
												var edit_button_html = '<a href="application/resumeedit/'
														+ full.programNameAbbr
														+ '/'
														+ full.applicationID
														+ '" alt="Edit" style="margin-right: 10px;"><i title="Edit" class="fa fa-pencil-square-o fa-lg" aria-hidden="true"></i></a>';
												var str_fullname = full.programNameFull
														.replace(/\"/g, "");
												console.log(str_fullname);
												var withdraw_button_html = '<a href="javascript:void(0);" onclick=shown_withdrew('
														+ full.applicationID
														+ ',"withdrew"'
														+ ',"'
														+ full.programNameAbbr
														+ '","'
														+ full.schoolSemester
														+ '",'
														+ full.schoolYear
														+ ')><i title="Withdrew" class="fa fa-close fa-lg" aria-hidden="true"></i></a>';
												return edit_button_html
														+ withdraw_button_html;
											}
										} ],
										"aoColumns" : [
												{
													"mData" : "programNameFull"
												},

												{
													"mData" : "semester",
													"sClass" : "left",
													"mRender" : function(data,
															type, full) {
														return full.schoolSemester
																+ " "
																+ full.schoolYear;
													}
												},
												{
													"mData" : "startDate",
													"mRender" : function(data,
															type, full) {
														return moment
																.tz(
																		full.startDate,
																		"America/Denver")
																.format(
																		'YYYY-MM-DD');
													}
												}, {
													"mData" : "decision",
													'orderable' : false
												}, {
													"mData" : ""
												} ],
										"paging" : true,
										"pageLength" : 10,
										"ordering" : true,
										"order" : [ 2, "desc" ]
									});

				});

function shown_withdrew(applicationID, decision, fullname, semester, year) {
	if (decision == "withdrew") {
		var buttonName = "Withdrew"
		// alert(decision + " " + applicationID);
		var s_name;
		if (fullname == "URS") {
			s_name = "New Mexico AMP Undergraduate Research Scholars (URS)";
		} else if (fullname == "TRANS") {
			s_name = "New Mexico AMP Transfer Scholarship";
		}else if (fullname == "IREP") {
			s_name = "New Mexico AMP International Research and Education Participation";
		} else if (fullname == "MESA") {
			s_name = "New Mexico AMP MESA Scholarship";
		} else if (fullname == "CCCONF") {
			s_name = "New Mexico AMP Community College Professional Development Workshop Stipend";
		} else if (fullname == "SCCORE") {
			s_name = "New Mexico AMP Summer Community College Opportunity for Research Experience (SCCORE)";
		}

		var htmlData = '';
		htmlData += "<span style='font-size: 18px; margin-left:50px'>" + s_name
				+ " :  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;" + semester
				+ " &nbsp; &nbsp; &nbsp; - &nbsp; &nbsp; &nbsp;" + year
				+ "</span>"
		$("#withdrewDecisionApplication").html(htmlData);
		$("#StuDecisionButton").text("Withdrew");
		$("#withdrewDecisionModal #schoolYear").val(year);
		$("#withdrewDecisionModal #schoolSemester").val(semester);
		$("#withdrewDecisionModal #program").val(fullname);
		$("#withdrewDecisionModal #applicationID").val(applicationID);
		$("#withdrewDecisionModal #decision").val("Withdrew");
		$('#withdrewDecisionModal').modal('show');

	}
}

function escapeHTML(s) {
	return s.replace(/&/g, '&amp;').replace(/\"/g, '&quot;').replace(/</g,
			'&lt;').replace(/>/g, '&gt;');
}
