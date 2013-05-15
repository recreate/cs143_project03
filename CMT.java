import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.net.URLEncoder;
import java.sql.Timestamp;

public class CMT extends HttpServlet {

	public static int getSesskey(HttpSession s, HttpServletResponse r) 
			throws IOException, ServletException {
		Object vs = s.getValue("sesskey");
		if (vs == null) {
			if (s != null) {
				s.invalidate();
			}
			r.sendRedirect("./../Login.html");
			return 0;
		}
		return (int)vs;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		// Register MySQL JDBC driver

		try {
			// register the MySQL JDBC driver with DriverManager
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// get the output stream for result page
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		try {
			// Connect to the database
			Connection con = null;

			// URL is jdbc:mysql:dbname
			String url = "jdbc:mysql://localhost/cs143s54";
			String userName = "cs143s54";
			String password = "meta90york";

			// connect to the database, user name and password can be specified
			// through this method
			con = DriverManager.getConnection(url, userName, password);
			HttpSession session = request.getSession(true);
			Statement stmt = con.createStatement();

			String opid = request.getParameter("opid");
			if (opid.equals("0")) {			// LOGIN
				out.println("<html>");
				out.println("<head>");
				String title = "Login";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				String email = request.getParameter("email");
				String last_name = request.getParameter("last_name");
				
				String sql = "SELECT * FROM Users WHERE " + 
					"email=\'" + email + "\' AND last_name=\'" + last_name + "\'";

				ResultSet rs = stmt.executeQuery(sql);
				if (rs.next()) {
					int sesskey = rs.getInt(1);
					session.putValue("sesskey", sesskey);
					out.println("<br>" + sesskey);
					out.println("<br>Login Successful<br>");
					out.println("<a href=\"./../CMT.html\">Continue to CMT</a>");
				} else {
					out.println("Invalid E-mail and/or last name.<br>");
					out.println("<a href=\"./../Login.html\">Return</a>");
				}

				rs.close();
				
			} else if (opid.equals("1")) {		// CREATE USER
				out.println("<html>");
				out.println("<head>");
				String title = "User created";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");
				
				String first_name = request.getParameter("first_name");
				String middle_name = request.getParameter("middle_name");
				String last_name = request.getParameter("last_name");
				String email = request.getParameter("email");
				String affiliation = request.getParameter("affiliation");

				if (first_name.isEmpty() || middle_name.isEmpty() || last_name.isEmpty() ||
					email.isEmpty() || affiliation.isEmpty()) {
					out.println("Missing user information.<br>");
				} else {
					int nid = 0;
					ResultSet rs = stmt.executeQuery("SELECT U_ID FROM Users " + 
						"ORDER BY U_ID DESC LIMIT 1");
					if (rs.next()) { 
						nid = rs.getInt(1) + 1;
					}
					rs.close();

					String params = "(" + nid + ",\'" + email + "\',\'" + 
						affiliation + "\',\'" + first_name + "\',\'" + 
						middle_name + "\',\'" + last_name + "\',0)";
					String sql = "INSERT INTO Users " + 
						"(U_ID, email, affiliation, first_name, "+ 
						"middle_name, last_name, is_chair)" + 
						" VALUES " + params;
					stmt.executeUpdate(sql);

					out.println("<p>Created User " + params + "</p>");
				}

				out.println("<a href=\"./../Login.html\">Return</a>");

			} else if (opid.equals("2")) {		// MODIFY ACC INFO
				out.println("<html>");
				out.println("<head>");
				String title = "User Account Information";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				Object vs = session.getValue("sesskey");
				if (vs == null) {
					if (session != null) {
						session.invalidate();
					}
					response.sendRedirect("./../Login.html");
					return;
				}
				int sesskey = (int)vs;

				String first_name = request.getParameter("first_name");
				String middle_name = request.getParameter("middle_name");
				String last_name = request.getParameter("last_name");
				String affiliation = request.getParameter("affiliation");

				int nid = 0;
				ResultSet rs = stmt.executeQuery("SELECT U_ID FROM Users " +
					"ORDER BY U_ID DESC LIMIT 1");
				if (rs.next()) { 
					nid = rs.getInt(1);
				}

				String params = "";
				if (!affiliation.isEmpty()) {
					params += "affiliation=\'" + affiliation + "\'";
				}
				if (!first_name.isEmpty()) {
					params += ",first_name=\'" + first_name + "\'";
				}
				if (!middle_name.isEmpty()) {
					params += ",middle_name=\'" + middle_name + "\'";
				}
				if (!last_name.isEmpty()) {
					params += ",last_name=\'" + last_name + "\'";
				}

				if (!params.isEmpty()) {
					params = params.charAt(0) == ',' ? params.substring(1) : params;
					String sql = "UPDATE Users SET " + params  + " WHERE U_ID=" + sesskey;
					stmt.executeUpdate(sql);
					out.println("<p>Sucessfully updated user information.</p>");
				} else {
					out.println("<p>Nothing to update.</p>");
				}

				ResultSet rs1 = stmt.executeQuery("SELECT * FROM Users WHERE U_ID=" + sesskey);
				if (rs1.next()) {
					String r1 = rs1.getString(2);
					String r2 = rs1.getString(3);
					String r3 = rs1.getString(4);
					String r4 = rs1.getString(5);
					String r5 = rs1.getString(6);

					out.println("First name:\t" + r3 + "<br>");
					out.println("Middle name:\t" + r4 + "<br>");
					out.println("Last name:\t" + r5 + "<br>");
					out.println("Affiliation:\t" + r2 + "<br>");
					out.println("E-mail:\t" + r1 + "<br>");
				}

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\"> Back to CMT</a>");
				rs.close();

			} else if (opid.equals("3")) {		// CREATE PAPERS
				out.println("<html>");
				out.println("<head>");
				String title = "Create papers";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int sesskey = CMT.getSesskey(session, response);

				String p_title = request.getParameter("title");
				String p_abstract = request.getParameter("abstract");
				String content = request.getParameter("content");

				int nid = 0;
				ResultSet rs =stmt.executeQuery("SELECT PID FROM Paper ORDER BY PID DESC LIMIT 1");
				if (rs.next()) { 
					nid = rs.getInt(1) + 1;
				}

				String params = "(" + nid + "," + sesskey + ",\'" + p_title + "\',\'" + 
					p_abstract + "\',\'" + content + "\')";
				String sql = "INSERT INTO Paper VALUES " + params;
				stmt.executeUpdate(sql);

				out.println("<p>Added Paper " + p_title + "</p>");

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\"> Back to CMT</a>");
				rs.close();

			} else if (opid.equals("4")) {		// MOD/SUBMIT PAPERS
				out.println("<html>");
				out.println("<head>");
				String title = "View/Modify/Submit Papers";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");
				
				int sesskey = CMT.getSesskey(session, response);

				String sql = "SELECT p.PID, p.title, p.abstract, p.content, p.author_ID " + 
					"FROM Paper AS p WHERE " + 
					"p.author_ID=" + sesskey;
				ResultSet rs = stmt.executeQuery(sql);
				
				out.println("<table border=\"1\">");
				out.println("<tr><th>Title</th><th>Abstract</th><th>Content</th>" +
					"<th>Update Paper</th><th>Submit Paper</th><th>"+
					"Add CoAuthor</th><th>Score</th></tr>");
				for(; rs.next();) {
					int p_id = rs.getInt(1);
					String p_title = rs.getString(2);
					String p_abstract = rs.getString(3);
					String p_content = rs.getString(4);
					int aid = rs.getInt(5);

					sql = "SELECT status FROM Submitted WHERE paper_ID="+p_id;
					Statement stmt1 = con.createStatement();
					ResultSet rs3 = stmt1.executeQuery(sql);
					String score = "";
					if (rs3.next()){
						score = rs3.getString(1);
					}

					out.println("<tr><th>" + p_title + "</th><th>" + 
						p_abstract + "</th><th>" + p_content + "</th><th>" + 
						"<a href=\'CMT?opid=5&PID=" + p_id + "\'>Update</a></th><th>" +
						"<a href=\'CMT?opid=18&pid=" + p_id + "\'>Sumbit</a></th><th>" + 
						"<a href=\'CMT?opid=14&pid="+ p_id + 
						"&aid=" + aid + "\'>Add</a></th><th>"+
						score+"</th></tr>");

				}
				out.println("</table>");

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">CMT</a>");
				rs.close();

			} else if (opid.equals("5")) {		// UPDATE PAPERSE FORM
				out.println("<html>");
				out.println("<head>");
				String title = "Update Paper information";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int pid = Integer.parseInt(request.getParameter("PID"));

				String sql = "SELECT * FROM Paper WHERE PID=" + pid;
				ResultSet rs = stmt.executeQuery(sql);
				if (rs.next()) {
					String p_title = rs.getString(3);
					String p_abstract = rs.getString(4);
					String p_content = rs.getString(5);

					out.println("<h3>Update Paper</h3>");
					out.println("<form method=get action=CMT>" + 
						"Title: <input type=text name=title size=20 maxlength=50 " +
						"value=\'" + p_title + "\'><br>Abstract: <textarea " + 
						"name=abstract cols=50 rows=10 maxlength=200>" + p_abstract + 
						"</textarea><br><br>Content:<textarea name=content cols=50 " + 
						"rows=10 maxlength=1000>" + p_content + "</textarea><br>" + 
						"<input type=hidden name=opid value=6>" + 
						"<input type=hidden name=pid value=" + pid + ">" +
						"<input type=submit value=submit></form>");
				}
				
				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs.close();

			} else if (opid.equals("6")) {		// UPDATE PAPERS SUBMIT
				out.println("<html>");
				out.println("<head>");
				String title = "Update Papers";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");
				
				String p_title = request.getParameter("title");
				String p_abstract = request.getParameter("abstract");
				String p_content = request.getParameter("content");
				int pid = Integer.parseInt(request.getParameter("pid"));

				String sql = "UPDATE Paper SET title=\'" + p_title + "\',abstract=\'" + 
					p_abstract + "\',content=\'" + p_content + "\' " + 
					"WHERE PID=" + pid;

				stmt.executeUpdate(sql);

				out.println("Updated Paper " + p_title);

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");

			} else if (opid.equals("7")) {	
				long curr_time = System.currentTimeMillis();

				out.println("<html>");
				out.println("<head>");
				String title = "Create a Conference";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int sesskey = CMT.getSesskey(session, response);
				ResultSet rs=stmt.executeQuery("SELECT is_chair FROM Users WHERE U_ID="+sesskey);
				if (rs.next()) {
					int is_chair = rs.getInt(1);
					if (is_chair == 0) {
						out.println("<p>You must be a chair to create a conference</p>");
						out.println("<a href=\'./../CMT.html\'>Back to CMT</a>");
						out.println("</body></html>");
						return;
					}
				}

				String c_name = request.getParameter("c_name");

				String s_month = request.getParameter("s_month");
				String s_day = request.getParameter("s_day");
				String s_year = request.getParameter("s_year");
				String s_hour = request.getParameter("s_hour");
				String s_minute = request.getParameter("s_minute");
				String e_month = request.getParameter("e_month");
				String e_day = request.getParameter("e_day");
				String e_year = request.getParameter("e_year");
				String e_hour = request.getParameter("e_hour");
				String e_minute = request.getParameter("e_minute");
				try {
					Integer.parseInt(s_month);
					Integer.parseInt(s_day);
					Integer.parseInt(s_year);
					Integer.parseInt(s_hour);
					Integer.parseInt(s_minute);
					Integer.parseInt(e_month);
					Integer.parseInt(e_day);
					Integer.parseInt(e_year);
					Integer.parseInt(e_hour);
					Integer.parseInt(e_minute);
				} catch (Exception e) {
					out.println("<p>You must enter valid start/end dates.</p>");
					out.println("<a href=\'./../CMT.html\'>Back to CMT</a>");
					out.println("</body></html>");
					return;
				}
				
				String s_date = s_year+"-"+s_month+"-"+s_day+"-"+s_hour+"-"+s_minute+"-00";
				String e_date = e_year+"-"+e_month+"-"+e_day+"-"+e_hour+"-"+e_minute+"-00";
				DateFormat dformat = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss");
				long s_time = 0, e_time = 0;
				try {
					s_time = (dformat.parse(s_date)).getTime();
					e_time = (dformat.parse(e_date)).getTime();
				} catch (Exception e) {
					out.println("<p>Invalid date.</p>");
					out.println("<a href=\'./../CMT.html\'>Back to CMT</a>");
					out.println("</body></html>");
					return;
				}
				String n_r = request.getParameter("num_reviewers");
				String m_p = request.getParameter("max_papers");
				int num_reviewers = 0, max_papers = 0;
				try {
					num_reviewers = Integer.parseInt(n_r);
					max_papers = Integer.parseInt(m_p);
				} catch (Exception e) {
					out.println("<p>You must enter valid number of reviewers/papers.</p>");
					out.println("<a href=\'./../CMT.html\'>Back to CMT</a>");
					out.println("</body></html>");
					return;
				}

				//out.println("<br>" + s_time);
				//out.println("<br>" + e_time);
				//out.println("<br>" + curr_time);

				if (e_time <= s_time || e_time <= curr_time) {
					out.println("<p>End time is earlier than the start/current time.</p>");
					out.println("<a href=\'./../CMT.html\'>Back to CMT</a>");
					out.println("</body></html>");
					return;
				}

				int nid = 0;
				ResultSet rs1 = stmt.executeQuery("SELECT CID FROM Conference " + 
					"ORDER BY CID DESC LIMIT 1");
				if (rs1.next()) { 
					nid = rs1.getInt(1) + 1;
				}

				String params ="("+nid+",\'" + c_name + "\'," + s_time + "," + e_time + 
					"," + num_reviewers + "," + max_papers + ")";
				String sql = "INSERT INTO Conference VALUES " + params;
				stmt.executeUpdate(sql);
	
				out.println("<br>Successfully created Conference " + params);
				
				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs.close();
				rs1.close();

			} else if (opid.equals("8")) { 		// VIEW/MOD CONFERENCES
				out.println("<html>");
				out.println("<head>");
				String title = "Conferences";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int sesskey = CMT.getSesskey(session, response);
				ResultSet rs=stmt.executeQuery("SELECT is_chair FROM Users WHERE U_ID="+sesskey);
				if (rs.next()) {
					int is_chair = rs.getInt(1);
					if (is_chair == 0) {
						out.println("<p>You must be a chair to create a conference</p>");
						out.println("<a href=\'./../CMT.html\'>Back to CMT</a>");
						out.println("</body></html>");
						return;
					}
				}

				out.println("<table border=\"1\">");
				out.println("<tr><th>Name</th><th>Start time</th><th>End time</th>" +
					"<th>Minimum Reviewers per paper</th><th>Maximum papers per reviewer" + 
					"</th><th>Update</th><th>View Papers</th><th>" + 
					"Evaluate</th></tr>");

				String sql = "SELECT * FROM Conference";
				ResultSet rs1 = stmt.executeQuery(sql);
				for(; rs1.next();) {
					int cid = rs1.getInt(1);
					String c_name = rs1.getString(2);
					long s_time = rs1.getLong(3);
					long e_time = rs1.getLong(4);
					int num_reviewers = rs1.getInt(5);
					int max_papers = rs1.getInt(6);
					
					java.util.Date sd = new java.util.Date((long)s_time*1000);
					java.util.Date ed = new java.util.Date((long)e_time*1000);
					DateFormat df = new SimpleDateFormat("MM-dd-yyyy kk:mm");
					
					out.println("<tr><th>" + c_name + "</th><th>" + sd.toString() + 
						"</th><th>" + ed.toString() + "</th><th>" + num_reviewers + 
						"</th><th>" + max_papers + "</th><th>" + 
						"<a href=\'CMT?opid=90&cid="+cid+"\'>Update</a></th><th>" + 
						"<a href=\'CMT?opid=10&cid="+cid+"\'>Papers</a></th><th>" +
						"<a href=\'CMT?opid=20&cid="+cid+"\'>Eval</a></th></tr>");

				}
				out.println("</table>");
				
				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs.close();

			} else if (opid.equals("90")) {
				out.println("<html>");
				out.println("<head>");
				String title = "Update Conference";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int cid = Integer.parseInt(request.getParameter("cid"));
				String sql = "SELECT * FROM Conference WHERE CID="+cid;
				ResultSet rs = stmt.executeQuery(sql);
				String c_name = ""; long start_time=0,end_time=0;int num_reviewers=0,max_papers=0;
				if (rs.next()) {
					c_name = rs.getString(2);
					start_time = rs.getLong(3);
					end_time = rs.getLong(4);
					num_reviewers = rs.getInt(5);
					max_papers = rs.getInt(6);
				}
				
				out.println("Blank fields will remain unchanged.<br>");
				out.println("<form method=get action=CMT><br>");
				out.println("Conference name:<input type=text name=c_name size=40 maxlength=100>"+
					"<br>");
				out.println("Start time (MM-DD-YYYY-HH-mm):"+
		"Month<input type=text name=s_month size=2 maxlength=2>"+
		"Day<input type=text name=s_day size=2 maxlength=2>"+
		"Year<input type=text name=s_year size=4 maxlength=4>"+
		"Hour<input type=text name=s_hour size=2 maxlength=2>"+
		"Minute<input type=text name=s_minute size=2 maxlength=2><br>");
				out.println("End time (MM-DD-YYYY-HH-mm):" +
		"Month<input type=text name=e_month size=2 maxlength=2>"+
		"Day<input type=text name=e_day size=2 maxlength=2>"+
		"Year<input type=text name=e_year size=4 maxlength=4>"+
		"Hour<input type=text name=e_hour size=2 maxlength=2>"+
		"Minute<input type=text name=e_minute size=2 maxlength=2>"+
		"<br>"+
	"Required number of reviewers:<input type=text name=num_reviewers size=5 maxlength=5><br>"+
	"Maximum papers per reviewer:<input type=text name=max_papers size=5 maxlength=5><br>"+
	"<input type=hidden name=opid value=9>"+
	"<input type=hidden name=cid value="+cid+">" +
	"<input type=submit value=suubmit><br><br></form>");
					
				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs.close();

			} else if (opid.equals("9")) {		// UPDATE CONFERENCE
				long curr_time = System.currentTimeMillis();

				out.println("<html>");
				out.println("<head>");
				String title = "Update Conference";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int cid = Integer.parseInt(request.getParameter("cid"));

				int sesskey = CMT.getSesskey(session, response);
				ResultSet rs=stmt.executeQuery("SELECT is_chair FROM Users WHERE U_ID="+sesskey);
				if (rs.next()) {
					int is_chair = rs.getInt(1);
					if (is_chair == 0) {
						out.println("<p>You must be a chair to create a conference</p>");
						out.println("<a href=\'./../CMT.html\'>Back to CMT</a>");
						out.println("</body></html>");
						return;
					}
				}

				String c_name = request.getParameter("c_name");

				String s_month = request.getParameter("s_month");
				String s_day = request.getParameter("s_day");
				String s_year = request.getParameter("s_year");
				String s_hour = request.getParameter("s_hour");
				String s_minute = request.getParameter("s_minute");
				String e_month = request.getParameter("e_month");
				String e_day = request.getParameter("e_day");
				String e_year = request.getParameter("e_year");
				String e_hour = request.getParameter("e_hour");
				String e_minute = request.getParameter("e_minute");
				
				boolean ss = true;

				String s_date = s_year+"-"+s_month+"-"+s_day+"-"+s_hour+"-"+s_minute+"-00";
				String e_date = e_year+"-"+e_month+"-"+e_day+"-"+e_hour+"-"+e_minute+"-00";
				DateFormat dformat = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss");
				long s_time = 0, e_time = 0;
				try {
					s_time = (dformat.parse(s_date)).getTime();
				} catch (Exception e) {
					ss = false;
				}
				
				boolean es = true;
				try { 
					e_time = (dformat.parse(e_date)).getTime();
				} catch (Exception e) {
					es = false;
				}

				boolean rss = true;
				boolean pss = true;
				String n_r = request.getParameter("num_reviewers");
				String m_p = request.getParameter("max_papers");
				int num_reviewers = 0, max_papers = 0;
				try {
					num_reviewers = Integer.parseInt(n_r);
				} catch (Exception e) {
					rss = false;
				}
				try {
					max_papers = Integer.parseInt(m_p);
				} catch (Exception e) {
					pss = false;
				}

				//out.println("<br>" + s_time);
				//out.println("<br>" + e_time);
				//out.println("<br>" + curr_time);

				if (ss && es && (e_time <= s_time || e_time <= curr_time)) {
					out.println("<p>End time is earlier than the start/current time.</p>");
					out.println("<a href=\'./../CMT.html\'>Back to CMT</a>");
					out.println("</body></html>");
					return;
				}

				String params = "";
				if (c_name != null) {
					params += "c_name=\'" + c_name + "\',";
				}
				if (ss) {
					params += "start_time=" + s_time + ",";
				}
				if (es) {
					params += "end_time=" + e_time + ",";
				}  
				if (rss) {
					params += "num_reviewers=" + num_reviewers + ",";
				}
				if (pss) {
					params += "max_papers=" + max_papers + ",";
				}
				String sql = "UPDATE Conference SET " + params.substring(0,params.length()-1) + 
					" WHERE CID="+cid;
				out.println(sql);
				stmt.executeUpdate(sql);
	
				out.println("<br>Successfully updated Conference " + params);
				
				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs.close();

			} else if (opid.equals("10")) {		//CONFERENCE PAPERS
				out.println("<html>");
				out.println("<head>");
				String title = "Conferences Papers";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int sesskey = CMT.getSesskey(session, response);
				ResultSet rs=stmt.executeQuery("SELECT is_chair FROM Users WHERE U_ID="+sesskey);
				if (rs.next()) {
					int is_chair = rs.getInt(1);
					if (is_chair == 0) {
						out.println("<p>You must be a chair to create a conference</p>");
						out.println("<a href=\'./../CMT.html\'>Back to CMT</a>");
						out.println("</body></html>");
						return;
					}
				}

				int cid = Integer.parseInt(request.getParameter("cid"));
				
				out.println("<table border=\"1\">");
				out.println("<tr><th>Title</th><th>Author</th><th>Add Reviewer</th></tr>");

				String sql = "SELECT s.SID, p.PID, p.title, u.last_name, u.first_name "+
					"FROM Submitted AS s, Paper AS p, Users AS u " + 
					"WHERE s.conference_ID=" + cid + " AND s.paper_ID=p.PID AND " + 
					"p.author_ID=u.U_ID";
				ResultSet rs1 = stmt.executeQuery(sql);
				for(; rs1.next();) {
					int pid = rs1.getInt(2);
					String p_title = rs1.getString(3);
					String l_name = rs1.getString(4);
					String f_name = rs1.getString(5);
					String name = f_name + " " + l_name;
					
					out.println("<tr><th>" + p_title + "</th><th>" + name + "</th><th>" +
						"<a href=\'CMT?opid=11&cid="+cid+"&pid="+pid+"\'>" + 
						"Add Reviewers</a></th></tr>");
				}
				out.println("</table>");

				boolean none = true;
				sql = "SELECT * FROM Paper WHERE PID NOT IN " + 
					"(SELECT s.paper_ID FROM Submitted AS s " + 
					"WHERE s.conference_ID="+cid+")";
				ResultSet rs2 = stmt.executeQuery(sql);
				String options = "<select name=pid>";
				for(; rs2.next();) {
					none = false;
					int pid = rs2.getInt(1);
					String p_title = rs2.getString(3);
					options += "<option value="+pid+">" + p_title + "</option>";
				}
				options += "</select>";

				out.println("<p>Add a paper</p>");
				out.println("<form method=get action=CMT>");
				out.println("<input type=hidden name=opid value=13>");
				out.println("<input type=hidden name=cid value=" + cid + ">");
				if (!none) {
					out.println(options);
					out.println("<br><input type=submit value=submit>");
				}
				out.println("</form>");

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs.close();
			
			} else if (opid.equals("11")) {		// ADD REVIEWERS
				out.println("<html>");
				out.println("<head>");
				String title = "Add Reviewers";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int cid = Integer.parseInt(request.getParameter("cid"));
				int pid = Integer.parseInt(request.getParameter("pid"));

				String sql = "SELECT * FROM Users WHERE is_chair=0 AND U_ID NOT IN " + 
					"(SELECT author_ID FROM Paper as p WHERE p.PID="+pid+")";

				boolean none = true;
				ResultSet rs1 = stmt.executeQuery(sql);
				out.println("<form method=get action=CMT>");
				String options = "<select name=u_id>";
				for(; rs1.next();) {
					none = false;
					int u_id = rs1.getInt(1);
					String f_name = rs1.getString(4);
					String l_name = rs1.getString(6);
					String name = f_name + " " + l_name;
					options += "<option value=" + u_id + ">" + name + "</option>";
				}
				options += "</select>";
				out.println(options);
				out.println("<br><input type=hidden name=opid value=12>");
				out.println("<input type=hidden name=cid value="+cid+">");
				out.println("<input type=hidden name=pid value="+pid+">");
				if (!none) {
					out.println("<input type=submit value=submit>");
				}
				out.println("</form>");

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs1.close();

			} else if (opid.equals("12")) { 	// PROCESS REVIEWESR
				out.println("<html>");
				out.println("<head>");
				String title = "Added Reviewer";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				
				int u_id = Integer.parseInt(request.getParameter("u_id"));
				int cid = Integer.parseInt(request.getParameter("cid"));
				int pid = Integer.parseInt(request.getParameter("pid"));
				
				int nid = 0;
				ResultSet rs = stmt.executeQuery("SELECT RID FROM Reviews " +
					"ORDER BY RID DESC LIMIT 1");
				if (rs.next()) { 
					nid = rs.getInt(1) + 1;
				}
				String params = "(" + nid + "," + u_id + "," + cid + "," + pid + "," + 
					0 + ",\'\')";
				String sql = "INSERT INTO Reviews VALUES " + params;

				Statement stmt1 = con.createStatement();
				stmt1.executeUpdate(sql);
			
				out.println("<p>Added reviewer</p>");

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs.close();

			} else if (opid.equals("13")) { 	// ADDED PAPERS
				out.println("<html>");
				out.println("<head>");
				String title = "Added Paper";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int cid = Integer.parseInt(request.getParameter("cid"));
				int pid = Integer.parseInt(request.getParameter("pid"));

				int nid = 0;
				ResultSet rs = stmt.executeQuery("SELECT SID FROM Submitted " +
					"ORDER BY SID DESC LIMIT 1");
				if (rs.next()) { 
					nid = rs.getInt(1) + 1;
				}

				String sql = "SELECT author_ID FROM Paper WHERE PID=" + pid;
				ResultSet rs1 = stmt.executeQuery(sql);
				if (rs1.next()) {
					int aid = rs1.getInt(1);

					Statement stmt1 = con.createStatement();
					String params = "("+nid+","+cid+","+pid+",\'\'"+")";
					sql = "INSERT INTO Submitted VALUES " + params;
					stmt1.executeUpdate(sql);
				}

				out.println("<p>Update successful</p>");

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs.close();

			} else if (opid.equals("14")) { 	// COAUTHOR
				out.println("<html>");
				out.println("<head>");
				String title = "Add CoAuthor";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int pid = Integer.parseInt(request.getParameter("pid"));
				int aid = Integer.parseInt(request.getParameter("aid"));

				String sql = "SELECT * FROM Users WHERE U_ID NOT IN " + 
					"(SELECT u.U_ID FROM Users AS u WHERE is_chair=1 " + 
					"OR u.U_ID=" + aid + ")";

				ResultSet rs = stmt.executeQuery(sql);
				boolean none = true;
				String options = "<select name=uid>";
				for (; rs.next();) {
					none = false;
					int uid = rs.getInt(1);
					String f_name = rs.getString(4);
					String l_name = rs.getString(6);
					String name = f_name + " " + l_name;

					options += "<option value="+uid+">" + name + "</option>";
					
				}
				options += "</select>";

				if (!none) {
					out.println("<form method=get action=CMT>");
					out.println(options);
					out.println("<input type=hidden name=opid value=15>");
					out.println("<input type=hidden name=pid value="+pid+">");
					out.println("<input type=submit value=submit>");
					out.println("</form>");
				}
				
				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs.close();

			} else if (opid.equals("15")) {
				out.println("<html>");
				out.println("<head>");
				String title = "Added CoAuthor";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int pid = Integer.parseInt(request.getParameter("pid"));
				int uid = Integer.parseInt(request.getParameter("uid"));

				int nid = 0;
				ResultSet rs = stmt.executeQuery("SELECT CAID FROM CoAuthored " +
					"ORDER BY CAID DESC LIMIT 1");
				if (rs.next()) { 
					nid = rs.getInt(1) + 1;
				}

				String params = "("+nid+","+pid+","+uid+")";
				String sql = "INSERT INTO CoAuthored VALUES " + params;
				stmt.executeUpdate(sql);

				out.println("Successfully added CoAuthor " + params);

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs.close();

			} else if (opid.equals("16")) { 	// Review Papers
				out.println("<html>");
				out.println("<head>");
				String title = "Review Assigned Papers";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int sesskey = CMT.getSesskey(session, response);

				String sql = "SELECT r.RID, p.title, r.score, r.review " + 
					"FROM Reviews AS r, Paper AS p " + 
					"WHERE r.reviewer_ID=" + sesskey + " AND p.PID=r.paper_ID";
				ResultSet rs = stmt.executeQuery(sql);
				for (; rs.next();) {
					int rid = rs.getInt(1);
					String ptitle = rs.getString(2);
					int score = rs.getInt(3);
					String review = rs.getString(4);

					out.println(ptitle + "<br>");
					out.println("<form method=get action=CMT>");
					out.println("Score:<input type=text name=score value=" + score + "><br>");
					out.println("Review:<input type=text name=review value=" + review + 
						"><br>");
					out.println("<input type=hidden name=opid value=17>");
					out.println("<input type=hidden name=rid value=" +rid+">" );
					out.println("<input type=submit value=submit>");
					out.println("</form><br>");

				}

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
				rs.close();

			} else if (opid.equals("17")) {
				out.println("<html>");
				out.println("<head>");
				String title = "Paper Reviewed";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int rid = Integer.parseInt(request.getParameter("rid"));
				int score = Integer.parseInt(request.getParameter("score"));
				String review = request.getParameter("review");

				String sql = "UPDATE Reviews SET score="+score+",review=\'"+review+"\' " + 
					"WHERE RID=" + rid;
				stmt.executeUpdate(sql);

				out.println("<p>Successfully updated review</p>");
			
				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");

			} else if (opid.equals("18")) {		// SUBMIT PAPERS
				long curr_time = System.currentTimeMillis();

				out.println("<html>");
				out.println("<head>");
				String title = "Paper Submit Form";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int pid = Integer.parseInt(request.getParameter("pid"));

				String sql = "SELECT * FROM Conference WHERE end_time>"+curr_time;

				ResultSet rs = stmt.executeQuery(sql);
				boolean none = true;
				String options = "<select name=cid>";
				for (; rs.next();) {
					none = false;
					int cid = rs.getInt(1);
					String cname = rs.getString(2);

					options += "<option value="+cid+">"+cname+"</option>";
				}
				options += "</select>";

				if (!none) {
					out.println("<form method=get action=CMT>");
					out.println(options);
					out.println("<input type=hidden name=opid value=19>");
					out.println("<input type=hidden name=pid value="+pid+">");
					out.println("<input type=submit value=submit>");
					out.println("</form>");
				}

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");

			} else if (opid.equals("19")) { 	//PAPER SUBMITTION
				out.println("<html>");
				out.println("<head>");
				String title = "Paper Submittion";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int pid = Integer.parseInt(request.getParameter("pid"));
				int cid = Integer.parseInt(request.getParameter("cid"));

				int nid = 0;
				ResultSet rs = stmt.executeQuery("SELECT SID FROM Submitted " +
					"ORDER BY SID DESC LIMIT 1");
				if (rs.next()) { 
					nid = rs.getInt(1) + 1;
				}

				String params = "("+nid+","+cid+","+pid+",\'\')";
				String sql = "INSERT INTO Submitted VALUES " + params;

				stmt.executeUpdate(sql);

				out.println("<p>Submitted Paper</p>");
				
				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");

			} else if (opid.equals("20")) {
				out.println("<html>");
				out.println("<head>");
				String title = "Paper Evaluations";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");

				int cid = Integer.parseInt(request.getParameter("cid"));

				String sql = "SELECT COUNT(paper_ID), paper_ID FROM Reviews"+
					" WHERE conference_ID="+cid+
					" GROUP BY paper_ID";

				ResultSet rs = stmt.executeQuery(sql);
				for(; rs.next();) {
					int pid = rs.getInt(2);
					int tot = rs.getInt(1);
					int sum = 0;
					float scr = 0;
					Statement stmt1 = con.createStatement();
					sql = "SELECT score FROM Reviews WHERE paper_ID="+pid;
					ResultSet rs2 = stmt1.executeQuery(sql);
					for(; rs2.next();){
						int s = rs2.getInt(1);
						sum += s;
					}
					scr = sum/tot;

					sql = "UPDATE Submitted SET status="+scr+
						" WHERE conference_ID="+cid+
						" AND paper_id="+pid;
					stmt1.executeUpdate(sql);
				}

				out.println("Evaluation done.");

				out.println("<br>");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");

			} else if (opid.equals("999")) {
				if (session != null) {
					session.invalidate();
				}
				response.sendRedirect("./../Login.html");
			} else {
				out.println("This page does not exist");
				out.println("<a href = \"./../CMT.html\">Back to CMT</a>");
			}

			out.println("</body>");
			out.println("</html>");

			stmt.close();
			con.close();
		} catch (SQLException ex) {
			out.println("SQLException caught<br>");
			out.println("---<br>");
			while (ex != null) {
				out.println("Message   : " + ex.getMessage() + "<br>");
				out.println("SQLState  : " + ex.getSQLState() + "<br>");
				out.println("ErrorCode : " + ex.getErrorCode() + "<br>");
				out.println("---<br>");
				ex = ex.getNextException();
			}

			out.println("<br>");
			out.println("<a href = \"./../Login.html\">Return</a>");
		}
	}
}

