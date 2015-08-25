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

public class AcctMgrRPCReplyParser extends BaseParser {
	
	private AcctMgrRPCReply mAcctMgrRPCReply;
	
	public AcctMgrRPCReply getAccountMgrRPCReply() {
		return mAcctMgrRPCReply;
	}
	
	public static AcctMgrRPCReply parse(String rpcResult) {
		AcctMgrRPCReplyParser parser = new AcctMgrRPCReplyParser();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new InputSource(new StringReader(rpcResult)), parser);
		} catch(SAXException e) {
			if(Logging.WARNING) System.out.println("AcctMgrRPCReplyParser: malformated XML" + e.getMessage());
			return null;
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return parser.getAccountMgrRPCReply();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(qName.equalsIgnoreCase("acct_mgr_rpc_reply")) {
			mAcctMgrRPCReply = new AcctMgrRPCReply();
		} else {
			mElementStarted = true;
			mCurrentElement.setLength(0);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		try {
			if(mAcctMgrRPCReply != null) {
				// inside <acct_mgr_rpc_reply>
				if(qName.equalsIgnoreCase("acct_mgr_rpc_reply")) {
					// closing tag
				} else {
					// decode inner tags
					if(qName.equalsIgnoreCase("error_num")) {
						mAcctMgrRPCReply.error_num = Integer.parseInt(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("message")) {
						mAcctMgrRPCReply.messages.add(mCurrentElement.toString());
					}
				}
			}
		} catch(Exception e) {
			if(Logging.WARNING) System.out.println("AcctMgrRPCReplyParser Exception: " + e.getMessage());
		}
		mElementStarted = false;
	}
	
}