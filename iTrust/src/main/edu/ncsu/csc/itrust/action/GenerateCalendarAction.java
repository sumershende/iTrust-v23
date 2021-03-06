package edu.ncsu.csc.itrust.action;

import edu.ncsu.csc.itrust.exception.DBException;
import edu.ncsu.csc.itrust.logger.TransactionLogger;
import edu.ncsu.csc.itrust.model.old.beans.ApptBean;
import edu.ncsu.csc.itrust.model.old.dao.DAOFactory;
import edu.ncsu.csc.itrust.model.old.dao.mysql.ApptTypeDAO;
import edu.ncsu.csc.itrust.model.old.enums.TransactionType;
import edu.ncsu.csc.itrust.action.ViewMyApptsAction;

import java.util.List;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Calendar;

/**
 * Action class for calendar.jsp
 *
 */
public class GenerateCalendarAction {
	private ViewMyApptsAction a_action;
	private List<ApptBean> send;
	private ApptTypeDAO apptTypeDAO;
	
	/**
	 * Set up defaults
	 * 
	 * @param factory The DAOFactory used to create the DAOs used in this action.
	 * @param loggedInMID The MID of the user who is viewing the calendar
	 */
	public GenerateCalendarAction(DAOFactory factory, long loggedInMID) {
		a_action = new ViewMyApptsAction(factory, loggedInMID);
		send = new ArrayList<ApptBean>();
		apptTypeDAO = factory.getApptTypeDAO();
		TransactionLogger.getInstance().logTransaction(TransactionType.CALENDAR_VIEW, loggedInMID, 0L, "");
	}
	
	/**
	 * Return the send request for an AppointmentBean
	 * @return the send request for an AppointmentBean
	 */
	public List<ApptBean> getSend() {
		return send;
	}
	
	/**
	 * Check appointments appearing on the calendar for conflicts 
	 * with other appointments on the calendar. 
	 * 
	 * The array from this method is used to determine what appointments
	 * will appear in bold on the calendar.
	 * 
	 * @return An array of items that are in conflict with other items.
	 * @throws SQLException
	 * @throws DBException 
	 */
	public boolean[] getConflicts() throws SQLException, DBException {
		boolean conflicts[] = new boolean[send.size()];
		for(int i=0; i<send.size(); i++) {
			ApptBean ab = send.get(i);
			long t = ab.getDate().getTime();
			long m = apptTypeDAO.getApptType(ab.getApptType()).getDuration() * 60L * 1000L;
			Timestamp time = new Timestamp(t+m);
			for(int j=i+1; j<send.size(); j++) {
				if(send.get(j).getDate().before(time)) {
					conflicts[i] = true;
					conflicts[j] = true;
				}
			}
		}
		return conflicts;
	}
	
	/**
	 * Creates a hash table with all of the Appointments to be 
	 * displayed on the calendar for the month and year being viewed.
	 * 
	 * @param thisMonth The month of the calendar to be rendered
	 * @param thisYear The year of the calendar to be rendered
	 * @return A Hashtable containing the AppointmentBeans to be rendered
	 * @throws SQLException
	 * @throws DBException 
	 */
	public Hashtable<Integer, ArrayList<ApptBean>> getApptsTable(int thisMonth, int thisYear) throws SQLException, DBException {
		List<ApptBean> appts = a_action.getAllMyAppointments();
		Hashtable<Integer, ArrayList<ApptBean>> atable = new Hashtable<Integer, ArrayList<ApptBean>>();
		Calendar a = Calendar.getInstance();
		for(ApptBean b : appts) {
			a.setTimeInMillis(b.getDate().getTime());
			if(a.get(Calendar.MONTH) == thisMonth && a.get(Calendar.YEAR) == thisYear) {
				if(!atable.containsKey(a.get(Calendar.DAY_OF_MONTH)))
					atable.put(a.get(Calendar.DAY_OF_MONTH), new ArrayList<ApptBean>());
				ArrayList<ApptBean> l = atable.get(a.get(Calendar.DAY_OF_MONTH));
				l.add(b);
				send.add(b);
				atable.put(a.get(Calendar.DAY_OF_MONTH), l);
			}
		}
		return atable;
	}


}
