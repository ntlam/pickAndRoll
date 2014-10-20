/*
/*
/* 		This program uses the JSOUP HTML parsing API to retrieve data displayed on 
/*		the official NCAA standings page for the 2013-2014 college basketball season.
/*		The data is taken and populates a database for later computation.
/*
/**/

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.helper.*;
import org.jsoup.select.Selector;
import java.io.IOException;
import org.jsoup.select.Elements;
import java.util.Iterator;

import java.util.Properties;
import java.io.FileNotFoundException;
import java.io.FileInputStream;

import java.sql.*;
import javax.sql.*;
import oracle.jdbc.*;
import oracle.jdbc.pool.OracleDataSource;

import java.lang.Integer;
import java.lang.Float;
import java.sql.Types;

import java.util.HashMap;
import java.util.Map;

public class getAndStore
{
	public void getNewData(Connection conn) throws SQLException
	{
		// Properties props = new Properties();
		
		Document doc = null;
		try
		{
			doc = Jsoup.connect("http://www.ncaa.com/standings/basketball-men/d1/2013").get();
		}
		catch(Exception e)
		{
			System.out.println("Exception thrown" + e.getMessage());
		}

		int [] conSizes = new int [33];	//stores the sizes of each conference. Uses conference sizes to label teams within index with respective conference name.
		conSizes[0] = 9; //America East
		conSizes[1] = 10; //American Athletic Conference
		conSizes[2] = 13; //Atlantic 10
		conSizes[3] = 15; //Atlantic Coast
		conSizes[4] = 10; //Atlantic Sun
		conSizes[5] = 10; //Big 12
		conSizes[6] = 10;	//Big East
		conSizes[7] = 11; //Big Sky
		conSizes[8] = 12; //Big South North and South
		conSizes[9] = 12;	//Big Ten
		conSizes[10] = 9; //Big West
		conSizes[11] = 9; //Colonial Athletic
		conSizes[12] = 16; //Conference USA
		conSizes[13] = 9; //Horizon
		conSizes[14] = 1; //Independents
		conSizes[15] = 8; //Ivy League
		conSizes[16] = 11; //Metro Atlantic Athletic
		conSizes[17] = 12; //Mid-American East and West
		conSizes[18] = 13; //Mid-Eastern
		conSizes[19] = 10; //Missouri Valley
		conSizes[20] = 11; //Mountain West
		conSizes[21] = 10;	//Northeast
		conSizes[22] = 12;	//Ohio Valley East & West
		conSizes[23] = 12; //Pac-12
		conSizes[24] = 10; //Patriot
		conSizes[25] = 14; //Southeastern
		conSizes[26] = 11; //Southern
		conSizes[27] = 14; //Southland
		conSizes[28] = 10; //Southwestern
		conSizes[29] = 8; //Summit
		conSizes[30] = 10; //Sun Belt
		conSizes[31] = 10;	//West Coast
		conSizes[32] = 9; //Western Athletic   

		Elements conferences = doc.getElementsByClass("ncaa-standings-conference-name");
		Elements teams = doc.select("a.ncaa-standing-conference-team-link > span");
		Elements cwins = doc.select("td.ncaa-standing-conference-team + td");	//need to find a way to get the data that comes after this....
		Elements closses = doc.select("td.ncaa-standing-conference-team + td + td");
		Elements cpct = doc.select("td.ncaa-standing-conference-team + td + td + td");
		Elements owins = doc.select("td.ncaa-standing-conference-team + td + td + td + td");
		Elements olosses = doc.select("td.ncaa-standing-conference-team + td + td + td + td + td");
		Elements opct = doc.select("td.ncaa-standing-conference-team + td + td + td + td + td + td");
		Elements rpi = doc.select("td.ncaa-standing-conference-team + td + td + td + td + td + td + td + td");
		
		Iterator<Element> citr = conferences.iterator();
		Iterator<Element> titr = teams.iterator();
		Iterator<Element> cwitr = cwins.iterator();
		Iterator<Element> clitr = closses.iterator();
		Iterator<Element> cpctr = cpct.iterator();
		Iterator<Element> owitr = owins.iterator();
		Iterator<Element> olitr = olosses.iterator();
		Iterator<Element> opctr = opct.iterator();
		Iterator<Element> rpitr = rpi.iterator();

		Statement stmt = conn.createStatement();
		/*
		/* include code to update data in existing database
		/**/
		stmt.execute("DROP TABLE STANDINGS_2014"); //+ props.getProperty("table_name"));
		

		/*
		/* code to create entirely new database
		*/
		stmt.execute("CREATE TABLE STANDINGS_2014" 
					+ "("
						+ "TEAM varChar(255) NOT NULL PRIMARY KEY,"
						+ "CONFERENCE varChar(255), "
						+ "WINC int, "
						+ "LOSSC int, "
						+ "PCTC float, "
						+ "WINO int, "
						+ "LOSSO int, "
						+ "PCTO float, "
						+ "RPI int"
					+ ")");
		stmt.close();
		PreparedStatement stmt2 = conn.prepareStatement ("insert into STANDINGS_2014"	//STANDINGS_2014" 
														+ "(TEAM, CONFERENCE, WINC, "
                            							+ "LOSSC, PCTC, WINO, LOSSO, " 
                            							+ "PCTO, RPI)"
														+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?)");

		for(int x = 0; citr.hasNext(); x++)	//outermost loop keeps track of which conference team belongs to.
		{
			if(x != 8 && x != 14 && x != 17 && x != 22 && x != 27)
			{
				String conName = citr.next().ownText();		//stores conference name for multiple uses
				for(int i = 0; i < conSizes[x] && titr.hasNext(); i++)	//uses the size of each conference as a parameter to group teams into their respective conferences. 
				{
					// String teamName = titr.next().ownText();
					stmt2.setString(1, titr.next().ownText());
					stmt2.setString(2, conName);
					stmt2.setInt(3, Integer.parseInt(cwitr.next().ownText()));
					stmt2.setInt(4, Integer.parseInt(clitr.next().ownText()));
					stmt2.setFloat(5, Float.parseFloat(cpctr.next().ownText()));
					stmt2.setInt(6, Integer.parseInt(owitr.next().ownText()));
					stmt2.setInt(7, Integer.parseInt(olitr.next().ownText()));
					stmt2.setFloat(8, Float.parseFloat(opctr.next().ownText()));
					stmt2.setInt(9, Integer.parseInt(rpitr.next().ownText()));
					stmt2.execute();
					// System.out.println("\"" + teamName + "\", " );
				}
			}
			else 	//this conditional is not met when the Conference has a subconference (ie North/South, East/West)
			{
				if(x == 8)	//not elseif because it saves time for checks every iteration of the outermost for loop
				{
					String name = citr.next().ownText();
					String conName = name + " North";
					for(int n = 0; n < 2; n++)		
					{
						for(int i = 0; i < (conSizes[x] / 2) && titr.hasNext(); i++)
						{
							stmt2.setString(1, titr.next().ownText());
							stmt2.setString(2, conName);
							stmt2.setInt(3, Integer.parseInt(cwitr.next().ownText()));
							stmt2.setInt(4, Integer.parseInt(clitr.next().ownText()));
							stmt2.setFloat(5, Float.parseFloat(cpctr.next().ownText()));
							stmt2.setInt(6, Integer.parseInt(owitr.next().ownText()));
							stmt2.setInt(7, Integer.parseInt(olitr.next().ownText()));
							stmt2.setFloat(8, Float.parseFloat(opctr.next().ownText()));
							stmt2.setInt(9, Integer.parseInt(rpitr.next().ownText()));
							stmt2.execute();
							// System.out.println("\"" + teamName + "\", " );					
						}
						conName = name + " South";
					}
				}
				else if(x == 14)		//enters conditional if Independents to avoid exception thrown for attempting to convert "-" into an int/float.
				{
					cwitr.next();
					clitr.next();
					cpctr.next();
					stmt2.setString(1, titr.next().ownText());
					stmt2.setString(2, citr.next().ownText());
					stmt2.setNull(3, Types.INTEGER);
					stmt2.setNull(4, Types.INTEGER);
					stmt2.setNull(5, Types.FLOAT);
					stmt2.setInt(6, Integer.parseInt(owitr.next().ownText()));
					stmt2.setInt(7, Integer.parseInt(olitr.next().ownText()));
					stmt2.setFloat(8, Float.parseFloat(opctr.next().ownText()));
					stmt2.setInt(9, Integer.parseInt(rpitr.next().ownText()));
					stmt2.execute();
				}
				else if(x == 27)	//handles the case in which the team does not have an RPI. Isolated cases in one conference. 
				{
					String conName = citr.next().ownText();		
					for(int i = 0; i < conSizes[x] && titr.hasNext(); i++)	
					{
						String teamName = titr.next().ownText();
						stmt2.setString(1, teamName); //titr.next().ownText());
						stmt2.setString(2, conName);
						stmt2.setInt(3, Integer.parseInt(cwitr.next().ownText()));
						stmt2.setInt(4, Integer.parseInt(clitr.next().ownText()));
						stmt2.setFloat(5, Float.parseFloat(cpctr.next().ownText()));
						stmt2.setInt(6, Integer.parseInt(owitr.next().ownText()));
						stmt2.setInt(7, Integer.parseInt(olitr.next().ownText()));
						stmt2.setFloat(8, Float.parseFloat(opctr.next().ownText()));
						if(i != 4 && i != 12)
						{
							stmt2.setInt(9, Integer.parseInt(rpitr.next().ownText()));
						}
						else
						{
							stmt2.setNull(9, Types.INTEGER);
							rpitr.next();
						}
						stmt2.execute();
					}
				}
				else
				{
					String name = citr.next().ownText();
					String conName = name + " East";
					for(int n = 0; n < 2; n++)		//is there a more efficient way to do this other than a for loop.
					{
						for(int i = 0; i < (conSizes[x] / 2) && titr.hasNext(); i++)
						{
							stmt2.setString(1, titr.next().ownText());
							stmt2.setString(2, conName);
							stmt2.setInt(3, Integer.parseInt(cwitr.next().ownText()));
							stmt2.setInt(4, Integer.parseInt(clitr.next().ownText()));
							stmt2.setFloat(5, Float.parseFloat(cpctr.next().ownText()));
							stmt2.setInt(6, Integer.parseInt(owitr.next().ownText()));
							stmt2.setInt(7, Integer.parseInt(olitr.next().ownText()));
							stmt2.setFloat(8, Float.parseFloat(opctr.next().ownText()));
							stmt2.setInt(9, Integer.parseInt(rpitr.next().ownText()));
							stmt2.execute();
							// System.out.println("\"" + teamName + "\", " );
							}			
						}
						conName = name + " West";
				}
			}
		}
			stmt2.close();
			conn.commit();
			// conn.close();
	}

	public Connection getDBCon() throws SQLException
	{
		Properties props = new Properties();
		try
		{
			FileInputStream in = new FileInputStream("appProperties.properties");
			props.load(in);
			in.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception thrown" + e.getMessage());
		}

		OracleDataSource ods = new OracleDataSource();
	    ods.setUser(props.getProperty("user"));		
	    ods.setPassword(props.getProperty("pass"));
	    ods.setURL(props.getProperty("url"));

	    // Connect to the database
	    Connection con = ods.getConnection();
	    con.setAutoCommit(false);
	    return con;
	}

	public void getMargins(Connection conn) throws SQLException
	{
		HashMap<String, String> teamsMap = createMap();
		Statement stmt = conn.createStatement();
		stmt.execute("ALTER TABLE STANDINGS_2014 ADD (PPG float, OPPG float, MARGIN float)");

		for(int i = 0; i < 8; i++)
		{
			Document doc = null;
			try
			{
				if(i == 0)
					doc = Jsoup.connect("http://www.ncaa.com/stats/basketball-men/d1/current/team/147").get();
				else
				{
					String url = "http://www.ncaa.com/stats/basketball-men/d1/current/team/147/p" + Integer.toString(i + 1);
					doc = Jsoup.connect(url).get();
				}
			}
			catch(Exception e)
			{
				System.out.println("IO Exception: " + e.getMessage());
			}
			Elements teams = doc.select("tr > td + td > a");
			Elements ppg = doc.select("tr > td + td + td + td + td:contains(.)");
			Elements oppg = doc.select("tr > td + td + td + td + td + td + td:contains(.)");
			// Elements margin = doc.select("tr:last-child");

			Iterator<Element> titr = teams.iterator();
			Iterator<Element> pitr = ppg.iterator();
			Iterator<Element> opitr = oppg.iterator();
			// Iterator<Element> mitr = margin.iterator();

			while(titr.hasNext() && pitr.hasNext() && opitr.hasNext())
			{
				String teamName = titr.next().ownText();
				opitr.next();
				if(teamsMap.containsKey(teamName))
				{
					teamName = teamsMap.get(teamName);		//returns the appropriate value to the key-value pair. 
				}
				stmt.execute("UPDATE STANDINGS_2014 "
							+ "SET PPG = " + pitr.next().ownText() + ", "
							+ "OPPG = " + pitr.next().ownText() + ", "
							+ "MARGIN = " + opitr.next().ownText()
							+ "WHERE TEAM = '" + teamName + "'");
				pitr.next();
			}
		}
		stmt.close();
		conn.commit();
	}
	public void addPrevData(Connection conn) throws SQLException
	{
		HashMap<String, String> checker = createMap();
		int numYear;
		String[] teamURLs = new String [33];
		for(int i = 0; i < 12; i++)
		{
			numYear = 2002 + i;		
			String year = Integer.toString(numYear);
			
			teamURLs[0] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/1/year/" + year + "/america-east-conference";
			teamURLs[1] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/3/year/" + year + "/atlantic-10-conference";
			teamURLs[2] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/2/year/" + year + "/acc-conference";
			teamURLs[3] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/46/year/" + year + "/atlantic-sun-conference";
			teamURLs[4] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/8/year/" + year + "/big-12-conference";
			teamURLs[5] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/4/year/" + year + "/big-east-conference";
			teamURLs[6] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/5/year/" + year + "/big-sky-conference";
			teamURLs[7] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/6/year/" + year + "/big-south-conference";
			teamURLs[8] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/7/year/" + year + "/big-ten-conference";
			teamURLs[9] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/9/year/" + year + "/big-west-conference";
			teamURLs[10] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/10/year/" + year + "/colonial-conference";
			teamURLs[11] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/11/year/" + year + "/conference-usa-conference";
			teamURLs[12] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/43/year/" + year + "/independents-conference";
			teamURLs[13] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/57/year/" + year + "/great-west-conference";
			teamURLs[14] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/45/year/" + year + "/horizon-conference";
			teamURLs[15] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/12/year/" + year + "/ivy-conference";
			teamURLs[16] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/13/year/" + year + "/maac-conference";
			teamURLs[17] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/14/year/" + year + "/mid-american-conference";
			teamURLs[18] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/16/year/" + year + "/meac-conference";
			teamURLs[19] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/18/year/" + year + "/missouri-valley-conference";
			teamURLs[20] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/44/year/" + year + "/mountain-west-conference";
			teamURLs[21] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/19/year/" + year + "/northeast-conference";
			teamURLs[22] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/20/year/" + year + "/ohio-valley-conference";
			teamURLs[23] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/21/year/" + year + "/pac-12-conference";
			teamURLs[24] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/22/year/" + year + "/patriot-league-conference";
			teamURLs[25] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/23/year/" + year + "/sec-conference";
			teamURLs[26] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/24/year/" + year + "/southern-conference";
			teamURLs[27] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/25/year/" + year + "/southland-conference";
			teamURLs[28] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/26/year/" + year + "/swac-conference";
			teamURLs[29] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/49/year/" + year + "/summit-league-conference";
			teamURLs[30] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/27/year/" + year + "/sun-belt-conference";
			teamURLs[31] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/29/year/" + year + "/west-coast-conference";
			teamURLs[32] = "http://espn.go.com/mens-college-basketball/conferences/standings/_/id/30/year/" + year + "/wac-conference";
		
			Statement stmt = conn.createStatement();
			// stmt.execute("ALTER TABLE STANDINGS_2014 DROP COLUMN PCT" + year);
			stmt.execute("ALTER TABLE STANDINGS_2014 ADD PCT" + year + " float");

			for(int x = 0; x < 33; x++)
			{
				if((x == 7 && i != 11) || (x == 13 && i < 8) || (x == 22 && i != 11) || (x == 29 && i < 6))
				{
				}
				else
				{
					Document doc = null;
					try
					{
						doc = Jsoup.connect(teamURLs[x]).get();
					}
					catch(Exception e)
					{
						System.out.println("There is an issue..." + e.getMessage());
					}
					Elements teams = doc.select("tr > td > a");
					Elements pct = doc.select("tr > td:eq(5):containsOwn(.)");

					Iterator<Element> titr = teams.iterator();
					Iterator<Element> pctr = pct.iterator();

					
					String firstTeam = titr.next().ownText();
					if(checker.containsKey(firstTeam))			//consider hard coding in an if for year 2004 team saint joseph
						firstTeam = checker.get(firstTeam);
					String firstPCT = pctr.next().ownText();
					// System.out.println(firstTeam);
					stmt.execute("UPDATE STANDINGS_2014 "
										+ "SET PCT" + year +" = " + firstPCT
										+ "WHERE TEAM = '" + firstTeam + "'");
					while(titr.hasNext() && pctr.hasNext())
					{
						String teamCheck = titr.next().ownText();
						if(checker.containsKey(teamCheck))
							teamCheck = checker.get(teamCheck);
						if(teamCheck.equals(firstTeam))
							break;
						// System.out.println(teamCheck);
						// System.out.println(pctr.next().ownText());
						// System.out.println(teamCheck);
						stmt.execute("UPDATE STANDINGS_2014 "
										+ "SET PCT" + year +" = " + pctr.next().ownText() 
										+ "WHERE TEAM = '" + teamCheck + "'");
					}
				}
			}
		stmt.close();
		conn.commit();		//works without this being here...
		}
	}



	public HashMap<String, String> createMap()
	{
		HashMap<String, String> teamsMap = new HashMap<String, String>();		//accounts for discrepancies/addresses the discontinuity in naming schema and abbreviations among webpages.
		teamsMap.put("Wichita St.", "Wichita State");
		teamsMap.put("N.C. Central", "North Carolina Central");
		teamsMap.put("San Diego St.", "San Diego State");
		teamsMap.put("Stephen F. Austin", "Stephen F. Austin");
		teamsMap.put("Oklahoma St.", "Oklahoma State");
		teamsMap.put("Michigan St.", "Michigan State");
		teamsMap.put("North Dakota St.", "North Dakota State");
		teamsMap.put("New Mexico St.", "New Mexico State");
		teamsMap.put("Ohio St.", "Ohio State");
		teamsMap.put("Georgia St.", "Georgia State");
		teamsMap.put("Charleston So.", "Charleston Southern");
		teamsMap.put("Southern Miss.", "Southern Miss");		//should actually be southern mississippi....
		teamsMap.put("Iowa St.", "Iowa State");
		teamsMap.put("Southern U.", "Southern University");
		teamsMap.put("Middle Tenn.", "Middle Tennessee");
		teamsMap.put("Eastern Ky.", "Eastern Kentucky");
		teamsMap.put("Boise St.", "Boise State");
		teamsMap.put("Cleveland St.", "Cleveland State");
		teamsMap.put("Weber St.", "Weber State");
		teamsMap.put("Sam Houston St.", "Sam Houston State");
		teamsMap.put("Murray St.", "Murray State");
		teamsMap.put("Arizona St.", "Arizona State");
		teamsMap.put("La.-Lafayette", "Louisiana");
		teamsMap.put("Wright St.", "Wright State");
		teamsMap.put("St. John's (NY)", "St. John''s");
		teamsMap.put("St. John's", "St. John''s");
		teamsMap.put("Florida St.", "Florida State");
		teamsMap.put("Southeast Mo. St.", "Southeast Missouri State");
		teamsMap.put("Western Mich.", "Western Michigan");
		teamsMap.put("Boston U.", "Boston University");
		teamsMap.put("Arkansas St.", "Arkansas State");
		teamsMap.put("Eastern Mich.", "Eastern Michigan");
		teamsMap.put("Coastal Caro.", "Coastal Carolina");
		teamsMap.put("Kansas St.", "Kansas State");
		teamsMap.put("Colorado St.", "Colorado State");
		teamsMap.put("South Dakota St.", "South Dakota State");
		teamsMap.put("Northern Colo.", "Northern Colorado");
		teamsMap.put("Morehead St.", "Morehead State");
		teamsMap.put("St. Bonaventure", "St. Bonaventure");
		teamsMap.put("Youngstown St.", "Youngstown State");
		teamsMap.put("Northwestern St.", "Northwestern State");
		teamsMap.put("Utah St.", "Utah State");
		teamsMap.put("Indiana St.", "Indiana State");
		teamsMap.put("Fresno St.", "Fresno State");
		teamsMap.put("St. Francis Brooklyn", "St. Francis Brooklyn");
		teamsMap.put("Western Caro.", "Western Carolina");
		teamsMap.put("North Carolina St.", "North Carolina State");
		teamsMap.put("Alabama St.", "Alabama State");
		teamsMap.put("Kent St.", "Kent State");
		teamsMap.put("Eastern Wash.", "Eastern Washington");
		teamsMap.put("Western Ky.", "Western Kentucky");
		teamsMap.put("Norfolk St.", "Norfolk State");
		teamsMap.put("East Tenn. St.", "East Tenn. State");
		teamsMap.put("Idaho St.", "Idaho State");
		teamsMap.put("Penn St.", "Penn State");
		teamsMap.put("Missouri St.", "Missouri State");
		teamsMap.put("Ga. Southern", "Georgia Southern");
		teamsMap.put("Col. of Charleston", "Charleston");
		teamsMap.put("Portland St.", "Portland State");
		teamsMap.put("Oregon St.", "Oregon State");
		teamsMap.put("Southern Ill.", "Southern Illinois");
		teamsMap.put("Illinois St.", "Illinois State");
		teamsMap.put("Sacramento St.", "Sacramento State");
		teamsMap.put("Long Beach St.", "Long Beach State");
		teamsMap.put("Northern Ill.", "Northern Illinois");
		teamsMap.put("Morgan St.", "Morgan State");
		teamsMap.put("Central Mich.", "Central Michigan");
		teamsMap.put("Western Ill.", "Western Illinois");
		teamsMap.put("Northern Ariz.", "Northern Arizona");
		teamsMap.put("South Ala.", "South Alabama");
		teamsMap.put("Montana St.", "Montana State");
		teamsMap.put("Fla. Atlantic", "Florida Atlantic");
		teamsMap.put("Savannah St.", "Savannah State");
		teamsMap.put("Southeastern La.", "Southeastern Louisiana");
		teamsMap.put("Jackson St.", "Jackson State");
		teamsMap.put("Delaware St.", "Delaware State");
		teamsMap.put("Mississippi St.", "Mississippi State");
		teamsMap.put("Appalachian St.", "Appalachian State");
		teamsMap.put("Cal St. Fullerton", "Cal State Fullerton");
		teamsMap.put("La.-Monroe", "Louisiana-Monroe");
		teamsMap.put("Nicholls St.", "Nicholls State");
		teamsMap.put("Texas St.", "Texas State");
		teamsMap.put("Washington St.", "Washington State");
		teamsMap.put("Tex.-Pan American", "Texas-Pan American");
		teamsMap.put("South Fla.", "South Florida");
		teamsMap.put("Jacksonville St.", "Jacksonville State");
		teamsMap.put("Coppin St.", "Coppin State");
		teamsMap.put("Ill.-Chicago", "UIC");		//hashmap for autocomplete?
		teamsMap.put("Eastern Ill.", "Eastern Illinois");
		teamsMap.put("Chicago St.", "Chicago State");
		teamsMap.put("Central Conn. St.", "Central Connecticut State");
		teamsMap.put("Central Ark.", "Central Arkansas");
		teamsMap.put("Tennessee St.", "Tennessee State");
		teamsMap.put("Ark.-Pine Bluff", "UAPB");		//add to names?
		teamsMap.put("N.C. A&T", "North Carolina A&T");
		teamsMap.put("McNeese St.", "McNeese State");
		teamsMap.put("Ball St.", "Ball State");
		teamsMap.put("Mississippi Val.", "Mississippi Valley State");
		teamsMap.put("San Jose St.", "San Jose State");
		teamsMap.put("Kennesaw St.", "Kennesaw State");
		teamsMap.put("South Carolina St.", "South Carolina State");
		teamsMap.put("St. Mary's (CA)", "Saint Mary''s");
		teamsMap.put("Saint Mary's", "Saint Mary''s");
		teamsMap.put("Saint Joseph's", "Saint Joseph''s");
		teamsMap.put("Saint Peter's", "Saint Peter''s");
		teamsMap.put("St. Peter's", "Saint Peter''s");
		teamsMap.put("UNC Wilmington", "UNCW");		//not work
		teamsMap.put("UNC Greensboro", "UNCG");		//not work
		teamsMap.put("Texas-Arlington", "UT Arlington");
		teamsMap.put("Texas Arlington", "UT Arlington");
		teamsMap.put("UConn", "Connecticut");		//not work
		teamsMap.put("Saint Francis (PA)", "Saint Francis U"); //not work
		teamsMap.put("Mt. St. Mary's", "Mount St. Mary''s");
		teamsMap.put("Mount St. Mary's", "Mount St. Mary''s");
		teamsMap.put("Nicholls St.", "Nicholls");
		teamsMap.put("Bakersfield", "CSU Bakersfield");
		teamsMap.put("CSUN", "Cal State Northridge");
		teamsMap.put("Alcorn", "Alcorn State");

		teamsMap.put("Vmi", "VMI");
		teamsMap.put("Cal State Bakersfield", "CSU Bakersfield");
		teamsMap.put("SIU Edward", "SIUE");
		teamsMap.put("Arkansas-Little Rock", "UALR");
		teamsMap.put("Louisiana-Lafayette", "Louisiana");
		teamsMap.put("Loyola (IL)", "Loyola Chicago");
		teamsMap.put("Loyola (MD)", "Loyola Maryland");
		teamsMap.put("Nebraska-Omaha", "Nebraska Omaha");
		teamsMap.put("Ipfu", "IPFU");
		teamsMap.put("St. Francis (NY)", "St. Francis Brooklyn");
		teamsMap.put("St. Francis U", "Saint Francis U");
		teamsMap.put("Arkansas-Pine Bluff", "UAPB");
		teamsMap.put("Nicholls State", "Nicholls");
		teamsMap.put("Tennessee-Martin", "UT Martin");
		teamsMap.put("Bryant University", "Bryant");
		teamsMap.put("Loyola (IL)", "Loyola Chicago");
		teamsMap.put("Northern Iowa", "UNI");
		teamsMap.put("Pennsylvania", "Penn");
		teamsMap.put("Illinois-Chicago", "UIC");	
		teamsMap.put("Florida International", "FIU");
		teamsMap.put("North Carolina-Wilmington", "UNCW");
		teamsMap.put("Florida Gulf Coast", "FGCU");
		teamsMap.put("Virginia Commonwealth", "VCU");	
		teamsMap.put("SIU-Edwardsville", "SIUE");
		teamsMap.put("Texas A&M-CC", "Texas A&M-Corpus Christi");
		teamsMap.put("N.J.I.T.", "NJIT");
		return teamsMap;
	}
	public static void main (String [] args) 
	{
		getAndStore object = new getAndStore();
		Connection temp = null; 
		try
		{
			temp = object.getDBCon();
			object.getNewData(temp);
			object.getMargins(temp);
			object.addPrevData(temp);
			temp.close();
		}
		catch(Exception e)//(SQLException e)
		{
			System.out.println("SQL Exception: " + e.getMessage());
		}
	}
}

