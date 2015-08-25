/*******************************************************************************
 * This file is part of BOINC. http://boinc.berkeley.edu Copyright (C) 2012
 * University of California
 * 
 * BOINC is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * BOINC is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with BOINC. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package edu.berkeley.boinc.rpc;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ProjectInfoParser extends BaseParser {
	
	private ArrayList<ProjectInfo> mProjectInfos = new ArrayList<ProjectInfo>();
	private ProjectInfo mProjectInfo = null;
	private ArrayList<String> mPlatforms;
	Boolean withinPlatforms = false;
	
	public final ArrayList<ProjectInfo> getProjectInfos() {
		return mProjectInfos;
	}
	
	public static ArrayList<ProjectInfo> parse(String rpcResult) {
		ProjectInfoParser parser = new ProjectInfoParser();
		try {
			// report malformated XML to BOINC and remove String.replace
			// here...
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new InputSource(new StringReader(rpcResult.replace("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>", ""))), parser);
		} catch(SAXException e) {
			return null;
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return parser.getProjectInfos();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(qName.equalsIgnoreCase("project")) {
			mProjectInfo = new ProjectInfo();
		} else if(qName.equalsIgnoreCase("platforms")) {
			mPlatforms = new ArrayList<String>(); // initialize new list
											// (flushing old
											// elements)
			withinPlatforms = true;
		} else {
			// Another element, hopefully primitive and not constructor
			// (although unknown constructor does not hurt, because there
			// will be primitive start anyway)
			mElementStarted = true;
			mCurrentElement.setLength(0);
		}
	}
	
	// Method characters(char[] ch, int start, int length) is implemented by
	// BaseParser,
	// filling mCurrentElement (including stripping of leading whitespaces)
	// @Override
	// public void characters(char[] ch, int start, int length) throws
	// SAXException { }
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		if(mProjectInfo != null) {
			if(qName.equalsIgnoreCase("project")) {
				// Closing tag of <project> - add to vector and be ready for
				// next one
				if(!mProjectInfo.name.equals("")) {
					// name is a must
					mProjectInfos.add(mProjectInfo);
				}
				mProjectInfo = null;
			} else if(qName.equalsIgnoreCase("platforms")) { // closing tag
													// of platform
													// names
				mProjectInfo.platforms = mPlatforms;
				withinPlatforms = false;
			} else {
				// Not the closing tag - we decode possible inner tags
				trimEnd();
				if(qName.equalsIgnoreCase("name") && !withinPlatforms) { // project
																// name
					mProjectInfo.name = mCurrentElement.toString();
				} else if(qName.equalsIgnoreCase("url")) {
					mProjectInfo.url = mCurrentElement.toString();
				} else if(qName.equalsIgnoreCase("general_area")) {
					mProjectInfo.generalArea = mCurrentElement.toString();
				} else if(qName.equalsIgnoreCase("specific_area")) {
					mProjectInfo.specificArea = mCurrentElement.toString();
				} else if(qName.equalsIgnoreCase("description")) {
					mProjectInfo.description = mCurrentElement.toString();
				} else if(qName.equalsIgnoreCase("home")) {
					mProjectInfo.home = mCurrentElement.toString();
				} else if(qName.equalsIgnoreCase("name") && withinPlatforms) { // platform
																	// name
					mPlatforms.add(mCurrentElement.toString());
				} else if(qName.equalsIgnoreCase("image")) {
					mProjectInfo.imageUrl = mCurrentElement.toString();
				} else if(qName.equalsIgnoreCase("summary")) {
					mProjectInfo.summary = mCurrentElement.toString();
				}
			}
		}
		mElementStarted = false;
	}
}