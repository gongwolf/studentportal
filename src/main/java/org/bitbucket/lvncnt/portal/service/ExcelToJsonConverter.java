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

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.format.CellDateFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import org.bitbucket.lvncnt.portal.model.conf.PosterSession;
import org.bitbucket.lvncnt.portal.model.conf.ScheduleSession;
import org.bitbucket.lvncnt.portal.model.conf.Speaker;

public class ExcelToJsonConverter {
	
	private static final String SPLIT_COMMA = "\\s*,\\s*"; 

	public static List<Object> convertToJsonSchedule(InputStream inputStream) throws EncryptedDocumentException, InvalidFormatException, IOException {
		List<Object> schedules = new ArrayList<>(); 
		Workbook wb = WorkbookFactory.create(inputStream);
		Sheet sheet = wb.getSheetAt(0); 
		Map<String, List<ScheduleSession>> mapSchedule = new TreeMap<>(); 
		
		Iterator<Row> iterator = sheet.rowIterator();
		iterator.next();
		 
		String date = LocalDate.now().toString(); 
		
		while (iterator.hasNext()) {
			Row row = iterator.next();
			 
			if(!cellToObject(row.getCell(0)).isEmpty()) {
				date = cellToObject(row.getCell(0)); 
			}
			 
			ScheduleSession session = new ScheduleSession(); 
			session.setTimeStart(cellToObject(row.getCell(1)));
			session.setTimeEnd(cellToObject(row.getCell(2)));
			session.setName(cellToObject(row.getCell(3)));
			session.setLocation(cellToObject(row.getCell(4)));
			session.setTrack(cellToObject(row.getCell(5)));
			String speakers = cellToObject(row.getCell(6));
			if(!speakers.isEmpty()) {
				session.setSpeakers(speakers.split(SPLIT_COMMA));
			}
			String facilitators = cellToObject(row.getCell(7));
			if(!facilitators.isEmpty()) {
				session.setFacilitators(facilitators.split(SPLIT_COMMA));
			}
			String panelists = cellToObject(row.getCell(8));
			if(!panelists.isEmpty()) {
				session.setPanelists(panelists.split(SPLIT_COMMA));
			}
			String description = cellToObject(row.getCell(9));
			session.setDescription(description); 
			mapSchedule.computeIfAbsent(date, l -> new ArrayList<>()).add(session); 
		}
		 
		for (Entry<String, List<ScheduleSession>> entry : mapSchedule.entrySet()) {
			Map<String, Object> map = new HashMap<>(); 
			map.put("date", entry.getKey());  
			map.put("sessions", entry.getValue()); 
			schedules.add(map);  
		}
		wb.close();
		return schedules; 
	}
	
	public static List<Speaker> convertToJsonSpeaker(InputStream inputStream) throws EncryptedDocumentException, InvalidFormatException, IOException {
		List<Speaker> speakers = new ArrayList<>(); 
		Workbook wb = WorkbookFactory.create(inputStream);
		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> iterator = sheet.rowIterator();
		iterator.next();
		while (iterator.hasNext()) {
			Row row = iterator.next();
			Speaker speaker = new Speaker(); 
			speaker.setName(cellToObject(row.getCell(0)));
			speaker.setTitle(cellToObject(row.getCell(1)));
			speaker.setDepartment(cellToObject(row.getCell(2)));
			speaker.setInstitution(cellToObject(row.getCell(3)));
			speaker.setAbout(cellToObject(row.getCell(4)));
			speaker.setEmail(cellToObject(row.getCell(5)));
			speaker.setPhone(cellToObject(row.getCell(6)));
			speakers.add(speaker);
		}
		wb.close();
		return speakers; 
	}
	
	public static List<Object> convertToJsonPoster(InputStream inputStream) throws IOException, EncryptedDocumentException, InvalidFormatException {
		List<Object> posters = new ArrayList<>(); 
		Map<String, List<PosterSession>> mapPoster = new TreeMap<>(); 
		Workbook wb = WorkbookFactory.create(inputStream);
		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> iterator = sheet.rowIterator();
		iterator.next();
		while(iterator.hasNext()) {
			Row row = iterator.next(); 
			String category = cellToObject(row.getCell(8));
			PosterSession posterSession = new PosterSession(); 
			posterSession.addPresenter(
					cellToObject(row.getCell(1)) + " " + cellToObject(row.getCell(2)), 
					cellToObject(row.getCell(5)), 
					cellToObject(row.getCell(4)));
			posterSession.setSponsor(new String[] {cellToObject(row.getCell(3))});
			posterSession.setTitle(cellToObject(row.getCell(6)));
			posterSession.setPosterabstract(
					cellToObject(row.getCell(7)).replaceAll("&#39;", "'").replaceAll("&nbsp;", " ")); 
			posterSession.addMentor(
					cellToObject(row.getCell(9)), 
					cellToObject(row.getCell(10)) + ", " + cellToObject(row.getCell(11)));
			mapPoster.computeIfAbsent(category, List -> new ArrayList<>()).add(posterSession); 
		}
		 
		for (Entry<String, List<PosterSession>> entry : mapPoster.entrySet()) {
			Map<String, Object> map = new HashMap<>(); 
			map.put("field", entry.getKey());  
			List<PosterSession> sessions = entry.getValue(); 
			sessions.sort((s1, s2) -> s1.getTitle().compareTo(s2.getTitle()));
			map.put("groups", sessions); 
			posters.add(map);  
		}
		wb.close();
		return posters; 
	}
	 
	@SuppressWarnings({ "deprecation"})
	private static String cellToObject(Cell cell) {
		if(cell == null){
			return ""; 
		}
		
		int type = cell.getCellType();
		switch (type) {
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();
			
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				String formatstring = cell.getCellStyle().getDataFormatString();
				CellDateFormatter celldateformatter = new CellDateFormatter(
						formatstring);
				return celldateformatter.format(cell.getDateCellValue());
			} 
		}
		return "";
	}

}
