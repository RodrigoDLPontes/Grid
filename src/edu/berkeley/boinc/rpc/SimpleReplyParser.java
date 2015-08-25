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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SimpleReplyParser extends BaseParser {
	private boolean mParsed = false;
	private boolean mInReply = false;
	private boolean mSuccess = false;
	
	private String errorMessage = null;
	
	public final String getErrorMessage() {
		return errorMessage;
	}
	
	// Disable direct instantiation of this class
	private SimpleReplyParser() {
	}
	
	public final boolean result() {
		return mSuccess;
	}
	
	public static SimpleReplyParser parse(String reply) {
		SimpleReplyParser parser = new SimpleReplyParser();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new InputSource(new StringReader(reply)), parser);
		} catch(SAXException e) {
			return null;
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return parser;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(qName.equalsIgnoreCase("boinc_gui_rpc_reply")) {
			mInReply = true;
		} else {
			mElementStarted = true;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		
		if(qName.equalsIgnoreCase("boinc_gui_rpc_reply")) {
			mInReply = false;
		} else if(mInReply && !mParsed) {
			if(qName.equalsIgnoreCase("success")) {
				mSuccess = true;
				mParsed = true;
			} else if(qName.equalsIgnoreCase("failure")) {
				mSuccess = false;
				mParsed = true;
			} else if(qName.equalsIgnoreCase("error")) {
				trimEnd();
				errorMessage = mCurrentElement.toString();
				mSuccess = false;
				mParsed = true;
			}
		}
		mElementStarted = false;
	}
}