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

public class HostInfoParser extends BaseParser {
	private HostInfo mHostInfo = null;
	
	public final HostInfo getHostInfo() {
		return mHostInfo;
	}
	
	/**
	 * Parse the RPC result (host_info) and generate vector of projects info
	 * 
	 * @param rpcResult
	 *             String returned by RPC call of core client
	 * @return HostInfo
	 */
	public static HostInfo parse(String rpcResult) {
		HostInfoParser parser = new HostInfoParser();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new InputSource(new StringReader(rpcResult)), parser);
		} catch(SAXException e) {
			return null;
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return parser.getHostInfo();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(qName.equalsIgnoreCase("host_info")) {
			mHostInfo = new HostInfo();
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
		try {
			if(mHostInfo != null) {
				// we are inside <host_info>
				if(qName.equalsIgnoreCase("host_info")) {
					// Closing tag of <host_info> - nothing to do at the
					// moment
				} else {
					// Not the closing tag - we decode possible inner tags
					trimEnd();
					if(qName.equalsIgnoreCase("timezone")) {
						mHostInfo.timezone = Integer.parseInt(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("domain_name")) {
						mHostInfo.domain_name = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("ip_addr")) {
						mHostInfo.ip_addr = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("host_cpid")) {
						mHostInfo.host_cpid = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("p_ncpus")) {
						mHostInfo.p_ncpus = Integer.parseInt(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("p_vendor")) {
						mHostInfo.p_vendor = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("p_model")) {
						mHostInfo.p_model = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("p_features")) {
						mHostInfo.p_features = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("p_fpops")) {
						mHostInfo.p_fpops = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("p_iops")) {
						mHostInfo.p_iops = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("p_membw")) {
						mHostInfo.p_membw = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("p_calculated")) {
						mHostInfo.p_calculated = (long)Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("product_name")) {
						mHostInfo.product_name = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("m_nbytes")) {
						mHostInfo.m_nbytes = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("m_cache")) {
						mHostInfo.m_cache = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("m_swap")) {
						mHostInfo.m_swap = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("d_total")) {
						mHostInfo.d_total = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("d_free")) {
						mHostInfo.d_free = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("os_name")) {
						mHostInfo.os_name = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("os_version")) {
						mHostInfo.os_version = mCurrentElement.toString();
					} else if(qName.equalsIgnoreCase("virtualbox_version")) {
						mHostInfo.virtualbox_version = mCurrentElement.toString();
					}
				}
			}
		} catch(NumberFormatException e) {
		}
		mElementStarted = false;
	}
}