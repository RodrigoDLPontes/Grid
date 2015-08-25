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

import edu.berkeley.boinc.utils.Logging;

public class AcctMgrInfoParser extends BaseParser {
	
	private AcctMgrInfo mAcctMgrInfo = null;
	
	public AcctMgrInfo getAccountMgrInfo() {
		return mAcctMgrInfo;
	}
	
	public static AcctMgrInfo parse(String rpcResult) {
		AcctMgrInfoParser parser = new AcctMgrInfoParser();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new InputSource(new StringReader(rpcResult)), parser);
		} catch(SAXException e) {
			if(Logging.WARNING) System.out.println("AcctMgrRPCReplyParser: malformated XML");
			return null;
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return parser.getAccountMgrInfo();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(qName.equalsIgnoreCase("acct_mgr_info")) {
			mAcctMgrInfo = new AcctMgrInfo();
		} else {
			mElementStarted = true;
			mCurrentElement.setLength(0);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		try {
			if(mAcctMgrInfo != null) {
				// inside <acct_mgr_info>
				if(qName.equalsIgnoreCase("acct_mgr_info")) {
					// closing tag
					if(!mAcctMgrInfo.acct_mgr_name.isEmpty() && !mAcctMgrInfo.acct_mgr_url.isEmpty() && mAcctMgrInfo.have_credentials) mAcctMgrInfo.present = true;
				} else {
					// decode inner tags
					if(qName.equalsIgnoreCase("acct_mgr_name")) {
						mAcctMgrInfo.acct_mgr_name = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("acct_mgr_url")) {
						mAcctMgrInfo.acct_mgr_url = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("have_credentials")) {
						mAcctMgrInfo.have_credentials = true;
					} else if(qName.equalsIgnoreCase("cookie_required")) {
						mAcctMgrInfo.cookie_required = true;
					} else if(qName.equalsIgnoreCase("cookie_failure_url")) mAcctMgrInfo.cookie_failure_url = mCurrentElement.toString();
				}
			}
		} catch(Exception e) {
		}
		mElementStarted = false;
	}
}