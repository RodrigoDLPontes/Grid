/*******************************************************************************
 * This file is part of BOINC.
 * http://boinc.berkeley.edu
 * Copyright (C) 2012 University of California
 * 
 * BOINC is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * BOINC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with BOINC.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.berkeley.boinc.rpc;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ProjectAttachReplyParser extends BaseParser {

	private ProjectAttachReply mPAR;
	
	public ProjectAttachReply getProjectAttachReply() {
		return mPAR;
	}
	
	public static ProjectAttachReply parse(String rpcResult) {
		ProjectAttachReplyParser parser = new ProjectAttachReplyParser();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new InputSource(new StringReader(rpcResult)), parser);
		} catch (SAXException e) {
			return null;
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return parser.getProjectAttachReply();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (qName.equalsIgnoreCase("project_attach_reply")) {
			mPAR = new ProjectAttachReply();
		}
		else {
			// Another element, hopefully primitive and not constructor
			// (although unknown constructor does not hurt, because there will be primitive start anyway)
			mElementStarted = true;
			mCurrentElement.setLength(0);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		try {
			if (mPAR != null) {
				// we are inside <project_attach_reply>
				if (qName.equalsIgnoreCase("project_attach_reply")) {
					// Closing tag of <project_attach_reply> - nothing to do at the moment
				}
				else {
					// Not the closing tag - we decode possible inner tags
					trimEnd();
					if (qName.equalsIgnoreCase("error_num")) {
						mPAR.error_num = Integer.parseInt(mCurrentElement.toString());
					} else if (qName.equalsIgnoreCase("message")) {
						mPAR.messages.add(mCurrentElement.toString());
					}
				}
			}
		} catch (NumberFormatException e) {
		}
		mElementStarted = false;
	}
}