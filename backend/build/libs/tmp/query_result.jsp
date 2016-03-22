<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*"%>
<%@ page import="edu.dartmouth.cs.myruns.backend.data.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Query Result</title>
</head>
<body>
<center>
	<h1>Exercise Entries List</h1>
	<table border="1">
		<tr>
			<td>ID</td> 
			<td>Input Type</td>
			<td>Activity Type</td>
			<td>Date Time</td>
			<td>Duration</td>
			<td>Distance</td>
			<td>Average Speed</td>
			<td>Calories</td>
			<td>Climb</td>
			<td>Heart Rate</td>
			<td>Comment</td>
			<td>&nbsp&nbsp&nbsp</td>
		</tr>
		<%
			ArrayList<ExerciseEntry> resultList = (ArrayList<ExerciseEntry>) request
					.getAttribute("result");
			if (resultList != null) {
				for (ExerciseEntry exerciseEntry : resultList) {
					if (exerciseEntry != null) {
		%> 	
						<tr>
							<td><%=exerciseEntry.getId()%></td> 
							<td><%=exerciseEntry.getmInputTypeString()%></td>
							<td><%=exerciseEntry.getmActivityTypeString()%></td>
							<td><%=exerciseEntry.getmDateTime()%></td>
							<td><%=exerciseEntry.getmDurationString()%></td>
							<td><%=exerciseEntry.getmDistanceString()%></td>
							<td><%=exerciseEntry.getmAvgSpeedString()%></td>
							<td><%=exerciseEntry.getmCalorie()%></td>
							<td><%=exerciseEntry.getmClimbString()%></td>
							<td><%=exerciseEntry.getmHeartRate()%></td>
							<td><%=exerciseEntry.getmComment()%></td>
							<td><button type="button" onclick="location.href='/delete.do?_id=<%=exerciseEntry.getId()%>'">delete</button></td>
						</tr>
		<%
		 	    	}		 	    
 				}
 			}
 		%>
 	</table>
 </center>
</body>
</html>