package grid;

import edu.berkeley.boinc.rpc.BaseParser;
import edu.berkeley.boinc.rpc.Project;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * I couldn't find this functionality in the RPC
 * (even though I'm pretty sure the client can provide this),
 * so here it is.
 */
public class StatisticsParser extends BaseParser {

	private Statistic mStatistic = null;
	private Statistic.DailyStat mDailyStat = null;
	private boolean mInsideDailyStatistics = false;

	public static ArrayList<Statistic> getStatistics() {
		ArrayList<String> fileNames = new ArrayList<>(Arrays.asList(Grid.projectsFolder.getParentFile().list()));
		ArrayList<File> files = new ArrayList<>();
		ArrayList<Statistic> statistics = new ArrayList<>();
		for(String fileName : fileNames) {
			if(fileName.contains("statistics")) {
				files.add(new File(Grid.projectsFolder.getParent() + "\\" + fileName));
			}
		}
		for(File file : files) {
			StatisticsParser parser = new StatisticsParser();
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				saxParser.parse(file, parser);
				if(Grid.rpcClient != null) {
					for(Project project : Grid.rpcClient.getProjectStatus()) {
						if(project.master_url.equals(parser.mStatistic.master_url)) {
							parser.mStatistic.project_name = project.project_name;
						}
					}
				}
				statistics.add(parser.mStatistic);
			} catch(ParserConfigurationException e) {
				e.printStackTrace();
			} catch(SAXException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return statistics;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(qName.equalsIgnoreCase("project_statistics")) {
			mStatistic = new Statistic();
		} else if(qName.equalsIgnoreCase("daily_statistics")) {
			mDailyStat = new Statistic.DailyStat();
			mInsideDailyStatistics = true;
		} else {
			mElementStarted = true;
			mCurrentElement.setLength(0);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		try {
			if(mStatistic != null) {
				trimEnd();
				if(qName.equalsIgnoreCase("master_url")) {
					mStatistic.master_url = mCurrentElement.toString();
				} else if(mInsideDailyStatistics) {
					if(qName.equalsIgnoreCase("day")) {
						mDailyStat.day = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("user_total_credit")) {
						mDailyStat.user_total_credit = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("user_expavg_credit")) {
						mDailyStat.user_expavg_credit = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("host_total_credit")) {
						mDailyStat.host_total_credit = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("host_expavg_credit")) {
						mDailyStat.host_expavg_credit = Double.parseDouble(mCurrentElement.toString());
					} else if(qName.equalsIgnoreCase("daily_statistics")) {
						mStatistic.dailyStats.add(mDailyStat);
						mInsideDailyStatistics = false;
					}
				}
			}

		} catch(NumberFormatException e) {
		}
		mElementStarted = false;
	}
}
