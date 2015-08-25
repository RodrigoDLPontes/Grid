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

public class NoticesParser extends BaseParser {
	
	private Notice mNotice = null;
	private ArrayList<Notice> mNotices = new ArrayList<Notice>();
	
	public final ArrayList<Notice> getNotices() {
		return mNotices;
	}
	
	public static ArrayList<Notice> parse(String rpcResult) {
		NoticesParser parser = new NoticesParser();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new InputSource(new StringReader(rpcResult)), parser);
		} catch(SAXException e) {
			System.out.println("SAXException " + e.getMessage() + e.getException());
			return new ArrayList<Notice>();
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return parser.getNotices();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(qName.equalsIgnoreCase("notice")) {
			mNotice = new Notice();
		} else {
			// primitive
			mElementStarted = true;
			mCurrentElement.setLength(0);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		try {
			if(mNotice != null) {
				// inside <notice>
				if(qName.equalsIgnoreCase("notice")) {
					// Closing tag
					if(mNotice.seqno != -1) {
						// seqno is a must
						mNotices.add(mNotice);
					}
					mNotice = null;
				} else {
					// decode inner tags
					if(qName.equalsIgnoreCase("seqno")) {
						mNotice.seqno = Integer.parseInt(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("title")) {
						mNotice.title = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("description")) {
						mNotice.description = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("create_time")) {
						mNotice.create_time = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("arrival_time")) {
						mNotice.arrival_time = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("category")) {
						mNotice.category = mCurrentElement.toString();
						if(mNotice.category.equals("server")) mNotice.isServerNotice = true;
						if(mNotice.category.equals("scheduler")) mNotice.isServerNotice = true;
						if(mNotice.category.equals("client")) mNotice.isClientNotice = true;
					} else if(qName.equalsIgnoreCase("link")) {
						mNotice.link = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("project_name")) {
						mNotice.project_name = mCurrentElement.toString();
					}
				}
			}
			mElementStarted = false;
		} catch(NumberFormatException e) {
			System.out.println("NumberFormatException " + qName + " " + mCurrentElement.toString());
		}
	}
}