package edu.dartmouth.cs.myruns.backend;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.dartmouth.cs.myruns.backend.data.ExerciseEntry;
import edu.dartmouth.cs.myruns.backend.data.ExerciseEntryDatastore;

public class AddServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		String json = req.getParameter("Key");
		ArrayList<ExerciseEntry> result = new ArrayList<>();
		try{
			JSONArray resultSet = new JSONArray(json);
            //delete all the elements in the data store
			ExerciseEntryDatastore.deleteAll();
			int length = resultSet.length();
			for (int i = 0; i < length; i++){
                //obtain attribute value from json object
				JSONObject exerciseElement = resultSet.getJSONObject(i);

				String id = exerciseElement.getString("_id");
				String inputType = exerciseElement.getString("input_type");
				String activity_type = exerciseElement.getString("activity_type");
				String dateTime = exerciseElement.getString("date_time");
				String duration = exerciseElement.getString("duration");
				String distance = exerciseElement.getString("distance");
				String avg_speed = exerciseElement.getString("avg_speed");
				String calories = exerciseElement.getString("calories");
				String climb = exerciseElement.getString("calories");
				String heartRate = exerciseElement.getString("heart_rate");
				String comment = exerciseElement.getString("comment");
				String is_metric = exerciseElement.getString("is_metric");

                //store the exercise entry into data store if id is not null
				if (id != null && !id.isEmpty()){
					ExerciseEntry exerciseEntry = new ExerciseEntry(Long.parseLong(id),
							Integer.parseInt(inputType), Integer.parseInt(activity_type),
							dateTime, Integer.parseInt(duration), Double.parseDouble(distance),
							Double.parseDouble(avg_speed), calories, Double.parseDouble(climb),
							heartRate, comment, Integer.parseInt(is_metric));
					boolean ret = ExerciseEntryDatastore.add(exerciseEntry);
					if (ret) {
						req.setAttribute("_retStr", "Add exercise " + id + " success");
						result.add(exerciseEntry);
						req.setAttribute("result", result);
					}else{
						req.setAttribute("_retStr", id + " exists");
					}
				}
			}

		}catch (JSONException e){

		}
		getServletContext().getRequestDispatcher("/query_result.jsp").forward(
				req, resp);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		doGet(req, resp);
	}

}
