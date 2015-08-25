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

public class AccountOutParser extends BaseParser {
	
	private AccountOut mAccountOut = null;
	
	public AccountOut getAccountOut() {
		return mAccountOut;
	}
	
	public static AccountOut parse(String rpcResult) {
		AccountOutParser parser = new AccountOutParser();
		String outResult;
		try {
			int xmlHeaderStart = rpcResult.indexOf("<?xml");
			if(xmlHeaderStart != -1) {
				int xmlHeaderEnd = rpcResult.indexOf("?>");
				outResult = rpcResult.substring(0, xmlHeaderStart);
				outResult += rpcResult.substring(xmlHeaderEnd + 2);
			} else outResult = rpcResult;		
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new InputSource(new StringReader(outResult)), parser);
		} catch(SAXException e) {
			return null;
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return parser.getAccountOut();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(qName.equalsIgnoreCase("error_num") || qName.equalsIgnoreCase("error_msg") || qName.equalsIgnoreCase("authenticator")) {
			if(mAccountOut == null) mAccountOut = new AccountOut();
		} else {
			mElementStarted = true;
			mCurrentElement.setLength(0);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		try {
			if(mAccountOut != null) {
				trimEnd();
				if(qName.equalsIgnoreCase("error_num")) {
					mAccountOut.error_num = Integer.parseInt(mCurrentElement.toString());
				} else if(qName.equalsIgnoreCase("error_msg")) {
					mAccountOut.error_msg = mCurrentElement.toString();
				} else if(qName.equalsIgnoreCase("authenticator")) {
					mAccountOut.authenticator = mCurrentElement.toString();
				}
			}
		} catch(NumberFormatException e) {
		}
		mElementStarted = false;
	}
}