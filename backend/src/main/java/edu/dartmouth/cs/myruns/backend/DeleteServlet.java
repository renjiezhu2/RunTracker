package edu.dartmouth.cs.myruns.backend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.dartmouth.cs.myruns.backend.data.ExerciseEntryDatastore;

public class DeleteServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

    //delete the element from data store if id is not null or empty
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		String id = req.getParameter("_id");
		if (id != null){
            //delete exercise entry in the data store
			ExerciseEntryDatastore.delete(Long.parseLong(id));

            //send deleted exercise entry id back to app side
            MessagingEndpoint msg = new MessagingEndpoint();
            msg.sendMessage(id);

            //execute query
			resp.sendRedirect("/query.do");
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		doGet(req, resp);
	}
}
