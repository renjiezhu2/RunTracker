package edu.dartmouth.cs.myruns.backend.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExerciseEntryDatastore {

	private static final Logger mLogger = Logger
			.getLogger(ExerciseEntryDatastore.class.getName());
	private static final DatastoreService mDatastore = DatastoreServiceFactory
			.getDatastoreService();

	private static Key getKey() {
		return KeyFactory.createKey(ExerciseEntry.EXERCISE_PARENT_ENTITY_NAME,
				ExerciseEntry.EXERCISE_PARENT_KEY_NAME);
	}


    //add elements into data store
	public static boolean add(ExerciseEntry exerciseEntry) {
		if (getExerciseEntryById(exerciseEntry.getId(), null) != null) {
			mLogger.log(Level.INFO, "contact exists");
			return false;
		}

		Key parentKey = getKey();

		Entity entity = new Entity(ExerciseEntry.EXERCISE_ENTRY_ENTITY_NAME, exerciseEntry.getId(),
				parentKey);
		entity.setProperty(ExerciseEntry.FIELD_NAME_ID, exerciseEntry.getId());
		entity.setProperty(ExerciseEntry.FIELD_NAME_INPUT_TYPE, exerciseEntry.getmInputType());
		entity.setProperty(ExerciseEntry.FIELD_NAME_ACTIVITY_TYPE, exerciseEntry.getmActivityType());
		entity.setProperty(ExerciseEntry.FIELD_NAME_DATE_AND_TIME, exerciseEntry.getmDateTime());
		entity.setProperty(ExerciseEntry.FIELD_NAME_DURATION, exerciseEntry.getmDuration());
		entity.setProperty(ExerciseEntry.FIELD_NAME_DISTANCE, exerciseEntry.getmDistance());
		entity.setProperty(ExerciseEntry.FIELD_NAME_AVG_SPEED, exerciseEntry.getmAvgSpeed());
		entity.setProperty(ExerciseEntry.FIELD_NAME_CALORIE, exerciseEntry.getmCalorie());
		entity.setProperty(ExerciseEntry.FIELD_NAME_CLIMB, exerciseEntry.getmClimb());
        entity.setProperty(ExerciseEntry.FIELD_NAME_HEARTRATE, exerciseEntry.getmHeartRate());
		entity.setProperty(ExerciseEntry.FIELD_NAME_COMMENT, exerciseEntry.getmComment());
		entity.setProperty(ExerciseEntry.FIELD_NAME_IS_METRIC, exerciseEntry.getmIsMetric());
		mDatastore.put(entity);

		return true;
	}

    //delete elements in the data store based on element id
	public static boolean delete(long id) {
		// query
		Filter filter = new FilterPredicate(ExerciseEntry.FIELD_NAME_ID,
				FilterOperator.EQUAL, id);

		Query query = new Query(ExerciseEntry.EXERCISE_ENTRY_ENTITY_NAME);
		query.setFilter(filter);

		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = mDatastore.prepare(query);

		Entity result = pq.asSingleEntity();
		boolean ret = false;
		if (result != null) {
			// delete
			mDatastore.delete(result.getKey());
			ret = true;
		}

		return ret;
	}

    //delete all the elements in the data store
	public static void deleteAll(){
		Query query = new Query(ExerciseEntry.EXERCISE_ENTRY_ENTITY_NAME);

		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = mDatastore.prepare(query);

		Iterator iterator = pq.asIterator();
		while (iterator.hasNext()) {
			// delete
			Entity result = (Entity)iterator.next();
			mDatastore.delete(result.getKey());
		}
	}

    //query elements in the data store
	public static ArrayList<ExerciseEntry> query(String id) {
		ArrayList<ExerciseEntry> resultList = new ArrayList<ExerciseEntry>();
		if (id != null && !id.isEmpty()) {
			ExerciseEntry exerciseEntry = getExerciseEntryById(Long.parseLong(id), null);
			if (exerciseEntry != null) {
				resultList.add(exerciseEntry);
			}
		} else {
			Query query = new Query(ExerciseEntry.EXERCISE_ENTRY_ENTITY_NAME);
			// get every record from datastore, no filter
			query.setFilter(null);
			// set query's ancestor to get strong consistency
			query.setAncestor(getKey());

			PreparedQuery pq = mDatastore.prepare(query);

			for (Entity entity : pq.asIterable()) {
				ExerciseEntry exerciseEntry = getExerciseEntryFromEntity(entity);
				if (exerciseEntry != null) {
					resultList.add(exerciseEntry);
				}
			}
		}
		return resultList;
	}

    //get exercise element from data store by id
	public static ExerciseEntry getExerciseEntryById(Long id, Transaction txn) {
		Entity result = null;
		try {
			result = mDatastore.get(KeyFactory.createKey(getKey(),
					ExerciseEntry.EXERCISE_ENTRY_ENTITY_NAME, id));
		} catch (Exception ex) {

		}

		return getExerciseEntryFromEntity(result);
	}

    //transform entity into exercise entry
	private static ExerciseEntry getExerciseEntryFromEntity(Entity entity) {
		if (entity == null) {
			return null;
		}

		return new ExerciseEntry(
				(Long) entity.getProperty(ExerciseEntry.FIELD_NAME_ID),
                (int)(long) entity.getProperty(ExerciseEntry.FIELD_NAME_INPUT_TYPE),
				(int)(long) entity.getProperty(ExerciseEntry.FIELD_NAME_ACTIVITY_TYPE),
				(String) entity.getProperty(ExerciseEntry.FIELD_NAME_DATE_AND_TIME),
				(int)(long) entity.getProperty(ExerciseEntry.FIELD_NAME_DURATION),
				(double) entity.getProperty(ExerciseEntry.FIELD_NAME_DISTANCE),
				(double) entity.getProperty(ExerciseEntry.FIELD_NAME_AVG_SPEED),
				(String) entity.getProperty(ExerciseEntry.FIELD_NAME_CALORIE),
				(double) entity.getProperty(ExerciseEntry.FIELD_NAME_CLIMB),
				(String) entity.getProperty(ExerciseEntry.FIELD_NAME_HEARTRATE),
				(String) entity.getProperty(ExerciseEntry.FIELD_NAME_COMMENT),
				(int)(long) entity.getProperty(ExerciseEntry.FIELD_NAME_IS_METRIC));
	}
}
