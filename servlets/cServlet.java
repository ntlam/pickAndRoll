import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.io.*;
import java.util.*;

public class cServlet  extends HttpServlet 
{
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{ 
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		Properties props = new Properties();
		try
		{
			FileInputStream in = new FileInputStream("/Users/nathanlam/Downloads/apache-tomcat-8.0.11/webapps/pickAndRoll/WEB-INF/classes/servlet.properties");
			props.load(in);
			in.close();
		}
		catch(Exception e)
		{
			response.getWriter().print(e.getMessage());
		}

		PoolProperties p = new PoolProperties();
		p.setUrl(props.getProperty("url"));
		p.setDriverClassName("oracle.jdbc.OracleDriver");
		p.setUsername(props.getProperty("user"));
		p.setPassword(props.getProperty("pass"));

		DataSource datasource = new DataSource();
		datasource.setPoolProperties(p);

		Connection con = null;
		Statement st = null;
		Statement st2 = null;		//was unable to reuse st and rs
		ResultSet rs = null;
		ResultSet rs2 = null;

		String team1 = request.getParameter("Team1").toLowerCase();
		String team2 = request.getParameter("Team2").toLowerCase();

		try 
		{
            con = datasource.getConnection();
            // if(con != null)
            // {
            // 	out.println("<h1>It Works!</h1>");
            // }
            st = con.createStatement();
            st2 = con.createStatement();
        	rs = st.executeQuery("SELECT * FROM STANDINGS_2014 WHERE lower(TEAM) = '" + team1 + "'");
            rs2 = st2.executeQuery("SELECT * FROM STANDINGS_2014 WHERE lower(TEAM) = '" + team2 + "'");

            if(rs.next())
            { 
            	if(rs2.next())
	            {
	            	out.println("<h1>" + rs.getString("TEAM") + " vs. " + rs2.getString("TEAM") + " (2013 - 2014 Season)</h1>");
	            	out.println("<table>");
	            	// out.println("<thead>Comparing " + rs.getString("TEAM") + " vs. " + rs2.getString("TEAM") + "</thead>");
			        out.println("<tr><td class=\"label\"><b>Team</b></td><td><b>" + rs.getString("TEAM") + "</b></td><td><b>" + rs2.getString("TEAM") + "</b></td></tr>");            	
			        out.println("<tr><td class=\"label\"><b>Conference</b></td><td> " + rs.getString("CONFERENCE") + "</td><td>" + rs2.getString("CONFERENCE") + "</td></tr>");
			        // if(rs.getInt("WINC") > rs2.getInt("WINC"))
				       //  out.println("<tr><td><b>Conference Wins</b></td><td><b>" + rs.getString("WINC") + "</b></td><td> " + rs2.getString("WINC") + "</td></tr>");
			        // else
				       //  out.println("<tr><td><b>Conference Wins</b></td><td> " + rs.getString("WINC") + "</td><td><b>" + rs2.getString("WINC") + "</b></td></tr>");		        	
			      out.println("<tr><td class=\"label\"><b>Conference Wins</b></td><td> " + rs.getString("WINC") + "</td><td>" + rs2.getString("WINC") + "</td></tr>");		        	
				        out.println("<tr><td class=\"label\"><b>Conference Losses</b></td><td>" + rs.getString("LOSSC") + "</td><td>" + rs2.getString("LOSSC") + "</td></tr>");
			       // if(rs.getInt("LOSSC") < rs2.getInt("LOSSC"))
				      //   out.println("<tr><td><b>Conference Losses</b></td><td><b>" + rs.getString("LOSSC") + "</b></td><td>" + rs2.getString("LOSSC") + "</td></tr>");
			       //  else
				      //   out.println("<tr><td><b>Conference Losses</b></td><td>" + rs.getString("LOSSC") + "</td><td><b>" + rs2.getString("LOSSC") + "</b></td></tr>");
			        if(rs.getFloat("PCTC") > rs2.getFloat("PCTC"))
			        	out.println("<tr><td class=\"label\"><b>Conference Percentage</b></td><td><b>" + rs.getString("PCTC") + "</b></td><td>" + rs2.getString("PCTC") + "</td></tr>");
			        else
			        	out.println("<tr><td class=\"label\"><b>Conference Percentage</b></td><td>" + rs.getString("PCTC") + "</td><td><b>" + rs2.getString("PCTC") + "</b></td></tr>");
					 out.println("<tr><td class=\"label\"><b>Overall Wins</b></td><td>" + rs.getString("WINO") + "</td><td>" + rs2.getString("WINO") + "</td></tr>");
			        out.println("<tr><td class=\"label\"><b>Overall Losses</b></td><td>" + rs.getString("LOSSO") + "</td><td>" + rs2.getString("LOSSO") + "</td></tr>");

			        // if(rs.getInt("WINO") > rs2.getInt("WINO"))
				       //  out.println("<tr><td><b>Overall Wins</b></td><td><b>" + rs.getString("WINO") + "</b></td><td>" + rs2.getString("WINO") + "</td></tr>");
			        // else
				       //  out.println("<tr><td><b>Overall Wins</b></td><td>" + rs.getString("WINO") + "</td><td><b>" + rs2.getString("WINO") + "</b></td></tr>");
				    // if(rs.getInt("LOSSO") < rs2.getInt("LOSSO"))
			     //    out.println("<tr><td><b>Overall Losses</b></td><td><b>" + rs.getString("LOSSO") + "</b></td><td>" + rs2.getString("LOSSO") + "</td></tr>");
			     //    else
			     //    out.println("<tr><td><b>Overall Losses</b></td><td>" + rs.getString("LOSSO") + "</td><td><b>" + rs2.getString("LOSSO") + "</b></td></tr>");
			        if(rs.getFloat("PCTO") > rs2.getFloat("PCTO"))
				        out.println("<tr><td class=\"label\"><b>Overall Percentage</b></td><td><b>" + rs.getString("PCTO") + "</b></td><td>" + rs2.getString("PCTO") + "</td></tr>");
				    else
				        out.println("<tr><td class=\"label\"><b>Overall Percentage</b></td><td>" + rs.getString("PCTO") + "</td><td><b>" + rs2.getString("PCTO") + "</b></td></tr>");
				    if(rs.getInt("RPI") < rs2.getInt("RPI"))
				        out.println("<tr><td class=\"label\"><b>RPI</b></td><td><b>" + rs.getString("RPI") + "</b></td><td>" + rs2.getString("RPI") + "</td></tr>");
			    	else
				        out.println("<tr><td class=\"label\"><b>RPI</b></td><td>" + rs.getString("RPI") + "</td><td><b>" + rs2.getString("RPI") + "</b></td></tr>");
					if(rs.getString("PPG") != null && rs2.getString("PPG") != null)
					{
						if(rs.getFloat("PPG") > rs2.getFloat("PPG"))
					        out.println("<tr><td class=\"label\"><b>PPG</b></td><td><b>" + rs.getString("PPG") + "</b></td><td>" + rs2.getString("PPG") + "</td></tr>");
				    	else
					        out.println("<tr><td class=\"label\"><b>PPG</b></td><td>" + rs.getString("PPG") + "</td><td><b>" + rs2.getString("PPG") + "</b></td></tr>");
					    if(rs.getFloat("OPPG") < rs2.getFloat("OPPG"))
					        out.println("<tr><td class=\"label\"><b>OPPG</b></td><td><b>" + rs.getString("OPPG") + "</b></td><td>" + rs2.getString("OPPG") + "</td></tr>");
				    	else
					        out.println("<tr><td class=\"label\"><b>OPPG</b></td><td>" + rs.getString("OPPG") + "</td><td><b>" + rs2.getString("OPPG") + "</b></td></tr>");
					    if(rs.getInt("MARGIN") > rs2.getInt("MARGIN"))
					        out.println("<tr><td class=\"label\"><b>Scoring Margin</b></td><td><b>" + rs.getString("MARGIN") + "</b></td><td>" + rs2.getString("MARGIN") + "</td></tr>");
				    	else
					        out.println("<tr><td class=\"label\"><b>Scoring Margin</b></td><td>" + rs.getString("MARGIN") + "</td><td><b>" + rs2.getString("MARGIN") + "</b></td></tr>");
			        }
			        else
			        {
			        	if(rs.getString("PPG") == null)
			        	{
					        out.println("<tr><td class=\"label\"><b>PPG</b></td><td>N/A</td><td>" + rs2.getString("PPG") + "</td></tr>");
					        out.println("<tr><td class=\"label\"><b>OPPG</b></td><td>N/A</td><td>" + rs2.getString("OPPG") + "</td></tr>");
					        out.println("<tr><td class=\"label\"><b>Scoring Margin</b></td><td>N/A</td><td>" + rs2.getString("MARGIN") + "</td></tr>");
			        	}
			        	else
			        	{
					        out.println("<tr><td class=\"label\"><b>PPG</b></td><td>" + rs.getString("PPG") + "</td><td>N/A</td></tr>");
					        out.println("<tr><td class=\"label\"><b>OPPG</b></td><td>" + rs.getString("OPPG") + "</td><td>N/A</td></tr>");
					        out.println("<tr><td class=\"label\"><b>Scoring Margin</b></td><td>" + rs.getString("MARGIN") + "</td><td>N/A</td></tr>");
			        	}
			        }
			        out.println("</table>");
			    }
			    else
			    {
			    	out.print("Team 2 does not exist!");
			    }
			}
			else
			{
				if(rs2.next())
				{
					out.print("Team 1 does not exist!");
				}
				else
				{
					out.print("Team 1 &amp; Team 2 do not exist!");
				}
			}

	    	out.close();
	        rs.close();
	        rs2.close();
			st.close();
			st2.close();
			con.close();
		}
        catch(Exception e)
        {
        	out.println("<h1>Exception: " + e + "</h1>");
        }
	}

	@Override
    public void doPost (HttpServletRequest request, HttpServletResponse response)
    {
    	try
    	{
	    	doGet(request, response);
	    }
	    catch(Exception e)
	    {
	    }
    }
}
