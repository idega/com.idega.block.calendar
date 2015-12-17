/**
 * @(#)GoogleEventServiceImpl.java    1.0.0 16:23:01
 *
 * Idega Software hf. Source Code Licence Agreement x
 *
 * This agreement, made this 10th of February 2006 by and between 
 * Idega Software hf., a business formed and operating under laws 
 * of Iceland, having its principal place of business in Reykjavik, 
 * Iceland, hereinafter after referred to as "Manufacturer" and Agura 
 * IT hereinafter referred to as "Licensee".
 * 1.  License Grant: Upon completion of this agreement, the source 
 *     code that may be made available according to the documentation for 
 *     a particular software product (Software) from Manufacturer 
 *     (Source Code) shall be provided to Licensee, provided that 
 *     (1) funds have been received for payment of the License for Software and 
 *     (2) the appropriate License has been purchased as stated in the 
 *     documentation for Software. As used in this License Agreement, 
 *     Licensee shall also mean the individual using or installing 
 *     the source code together with any individual or entity, including 
 *     but not limited to your employer, on whose behalf you are acting 
 *     in using or installing the Source Code. By completing this agreement, 
 *     Licensee agrees to be bound by the terms and conditions of this Source 
 *     Code License Agreement. This Source Code License Agreement shall 
 *     be an extension of the Software License Agreement for the associated 
 *     product. No additional amendment or modification shall be made 
 *     to this Agreement except in writing signed by Licensee and 
 *     Manufacturer. This Agreement is effective indefinitely and once
 *     completed, cannot be terminated. Manufacturer hereby grants to 
 *     Licensee a non-transferable, worldwide license during the term of 
 *     this Agreement to use the Source Code for the associated product 
 *     purchased. In the event the Software License Agreement to the 
 *     associated product is terminated; (1) Licensee's rights to use 
 *     the Source Code are revoked and (2) Licensee shall destroy all 
 *     copies of the Source Code including any Source Code used in 
 *     Licensee's applications.
 * 2.  License Limitations
 *     2.1 Licensee may not resell, rent, lease or distribute the 
 *         Source Code alone, it shall only be distributed as a 
 *         compiled component of an application.
 *     2.2 Licensee shall protect and keep secure all Source Code 
 *         provided by this this Source Code License Agreement. 
 *         All Source Code provided by this Agreement that is used 
 *         with an application that is distributed or accessible outside
 *         Licensee's organization (including use from the Internet), 
 *         must be protected to the extent that it cannot be easily 
 *         extracted or decompiled.
 *     2.3 The Licensee shall not resell, rent, lease or distribute 
 *         the products created from the Source Code in any way that 
 *         would compete with Idega Software.
 *     2.4 Manufacturer's copyright notices may not be removed from 
 *         the Source Code.
 *     2.5 All modifications on the source code by Licencee must 
 *         be submitted to or provided to Manufacturer.
 * 3.  Copyright: Manufacturer's source code is copyrighted and contains 
 *     proprietary information. Licensee shall not distribute or 
 *     reveal the Source Code to anyone other than the software 
 *     developers of Licensee's organization. Licensee may be held 
 *     legally responsible for any infringement of intellectual property 
 *     rights that is caused or encouraged by Licensee's failure to abide 
 *     by the terms of this Agreement. Licensee may make copies of the 
 *     Source Code provided the copyright and trademark notices are 
 *     reproduced in their entirety on the copy. Manufacturer reserves 
 *     all rights not specifically granted to Licensee.
 *
 * 4.  Warranty & Risks: Although efforts have been made to assure that the 
 *     Source Code is correct, reliable, date compliant, and technically 
 *     accurate, the Source Code is licensed to Licensee as is and without 
 *     warranties as to performance of merchantability, fitness for a 
 *     particular purpose or use, or any other warranties whether 
 *     expressed or implied. Licensee's organization and all users 
 *     of the source code assume all risks when using it. The manufacturers, 
 *     distributors and resellers of the Source Code shall not be liable 
 *     for any consequential, incidental, punitive or special damages 
 *     arising out of the use of or inability to use the source code or 
 *     the provision of or failure to provide support services, even if we 
 *     have been advised of the possibility of such damages. In any case, 
 *     the entire liability under any provision of this agreement shall be 
 *     limited to the greater of the amount actually paid by Licensee for the 
 *     Software or 5.00 USD. No returns will be provided for the associated 
 *     License that was purchased to become eligible to receive the Source 
 *     Code after Licensee receives the source code. 
 */
package com.idega.block.calendar.business.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.idega.block.calendar.bean.ExcludedPeriod;
import com.idega.block.calendar.bean.Recurrence;
import com.idega.block.calendar.bean.Weekdays;
import com.idega.block.calendar.business.GoogleEventService;
import com.idega.user.dao.UserDAO;
import com.idega.user.data.bean.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.util.timer.DateUtil;

/**
 * <p>You can report about problems to: 
 * <a href="mailto:martynas@idega.is">Martynas Stakė</a></p>
 *
 * @version 1.0.0 2015 gruod. 9
 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
 */
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service
public class GoogleEventServiceImpl implements GoogleEventService {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	
	@Autowired
	private UserDAO userDAO;

	private UserDAO getUserDAO() {
		if (this.userDAO == null) {
			ELUtil.getInstance().autowire(this);
		}

		return this.userDAO;
	}

	@Override
	public List<String> getRecurrenceString(Recurrence recurrence) {
		List<String> recurrenceList = null;

		if (recurrence != null) {
			recurrenceList = new ArrayList<String>();

			/*
			 * None recurrence
			 */
			if (recurrence.getType().equalsIgnoreCase("NONE")) {
				return recurrenceList;
			}

			//*** Exclude dates list ***
			if (!ListUtil.isEmpty(recurrence.getExcludedPeriods())) {
				String datesExcludeString = "EXDATE:";
				for (ExcludedPeriod period : recurrence.getExcludedPeriods()) {
					LocalDate periodStart = DateUtil.getDate(period.getFrom());
					LocalDate periodEnd = DateUtil.getDate(period.getTo());

					/*
					 * All days in between
					 */
					List<LocalDate> excludedDates = new ArrayList<LocalDate>();
					while (!periodStart.isAfter(periodEnd)) {
						excludedDates.add(periodStart);
						periodStart = periodStart.plusDays(1);
					}

					for (LocalDate localDate : excludedDates) {
						if (!datesExcludeString.equalsIgnoreCase("EXDATE:")) {
							datesExcludeString += CoreConstants.COMMA;
						}

						datesExcludeString += localDate.format(FORMATTER);
						datesExcludeString += "T000000Z";
					}
				}

				recurrenceList.add(datesExcludeString);
			}

			StringBuilder recurrenceString = new StringBuilder();

			/*
			 * Daily recurrence
			 */
			if (recurrence.getType().equalsIgnoreCase("DAILY")) {
				recurrenceString.append("RRULE:FREQ=DAILY");
				if (recurrence.getRate() != null) {
					recurrenceString.append(";INTERVAL=").append(recurrence.getRate());
				}
			}

			/*
			 * Weekly recurrence
			 */
			if (recurrence.getType().equalsIgnoreCase("WEEKLY")) {
				recurrenceString.append("RRULE:FREQ=WEEKLY;WKST=MO");
				if (recurrence.getRate() != null) {
					recurrenceString.append(";INTERVAL=").append(recurrence.getRate());
				}

				Weekdays weekdays = recurrence.getWeekDays();
				if (weekdays != null && !ListUtil.isEmpty(weekdays.getSelectedDays())) {
					recurrenceString.append(";BYDAY=").append(weekdays.toString());
				}
			}

			/*
			 * Monthly recurrence
			 */
			if (recurrence.getType().equalsIgnoreCase("MONTHLY")) {
				recurrenceString.append("RRULE:FREQ=MONTHLY");
				if (recurrence.getRate() != null) {
					recurrenceString.append(";INTERVAL=").append(recurrence.getRate());
				}

				LocalDate date = DateUtil.getDate(recurrence.getFrom());
				if (date != null) {
					recurrenceString.append(";BYMONTHDAY=").append(date.getDayOfMonth());
				}
			}

			/*
			 * Yearly
			 */
			if (recurrence.getType().equalsIgnoreCase("YEARLY")) {
				recurrenceString.append("RRULE:FREQ=YEARLY");
				if (recurrence.getRate() != null) {
					recurrenceString.append(";INTERVAL=").append(recurrence.getRate());
				}

				LocalDate date = DateUtil.getDate(recurrence.getFrom());
				if (date != null) {
					recurrenceString.append(";BYMONTH=").append(date.getMonth());
					recurrenceString.append(";BYMONTHDAY=").append(date.getDayOfMonth());
				}
			}

			/*
			 * Adding the end of recurrence
			 */
			LocalDate endDate = DateUtil.getDate(recurrence.getTo());
			if (endDate != null) {
				recurrenceString.append(";UNTIL=").append(endDate.format(FORMATTER)).append("T000000Z");
			}

			recurrenceList.add(recurrenceString.toString());
		}

		return recurrenceList;
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.business.GoogleEventService#getEvent(com.google.api.services.calendar.Calendar, java.lang.String, java.lang.String)
	 */
	@Override
	public Event getEvent(Calendar calendarService, String calendarId, String eventId) {
		if (calendarService != null && !StringUtil.isEmpty(eventId) && !StringUtil.isEmpty(calendarId)) {
			try {
				return calendarService.events().get(calendarId, eventId).execute();
			} catch (IOException e) {
				java.util.logging.Logger.getLogger(getClass().getName()).log(
						Level.WARNING, "Failed to get event by event id: " + eventId +
						" and calendar id: " + calendarId +
						" cause of: " , e);
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.business.GoogleEventService#getAttendee(com.idega.user.data.bean.User)
	 */
	@Override
	public EventAttendee getAttendee(User user) {
		if (user != null) {
			EventAttendee attendee = new EventAttendee();
			attendee.setId(user.getPersonalID());
			attendee.setDisplayName(user.getName());
			attendee.setEmail(user.getEmailAddress());
			return attendee;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.business.GoogleEventService#getAttendee(com.idega.user.data.bean.User)
	 */
	@Override
	public EventAttendee getAttendee(com.idega.user.data.User user) {
		if (user != null) {
			User jpaUser = getUserDAO().getUser(user.getPersonalID());
			if (jpaUser != null) {
				return getAttendee(jpaUser);
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.business.GoogleEventService#getAttendees(java.util.Collection)
	 */
	@Override
	public List<EventAttendee> getAttendees(Collection<User> users) {
		ArrayList<EventAttendee> attendees = new ArrayList<EventAttendee>();
		
		for (User user : users) {
			EventAttendee attendee = getAttendee(user);
			if (attendee != null) {
				attendees.add(attendee);
			}
		}

		return attendees;
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.business.GoogleEventService#getLegacyAttendees(java.util.Collection)
	 */
	@Override
	public List<EventAttendee> getLegacyAttendees(Collection<com.idega.user.data.User> users) {
		ArrayList<EventAttendee> attendees = new ArrayList<EventAttendee>();
		
		if (!ListUtil.isEmpty(users)) {
			for (com.idega.user.data.User user : users) {
				EventAttendee attendee = getAttendee(user);
				if (attendee != null) {
					attendees.add(attendee);
				}
			}
		}

		return attendees;
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.business.GoogleEventService#remove(com.google.api.services.calendar.Calendar, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean remove(
			Calendar calendarService, 
			String eventId, 
			String calendarId) {
		if (	calendarService != null && 
				!StringUtil.isEmpty(eventId) && 
				!StringUtil.isEmpty(calendarId)) {
			try {
				calendarService.events().delete(calendarId, eventId).execute();
				return Boolean.TRUE;
			} catch (IOException e) {
				java.util.logging.Logger.getLogger(getClass().getName()).log(
						Level.WARNING, 
						"Failed to remove event by id: " + eventId + 
						" from calendar by id: " + calendarId + 
						" cause of: ", e);
			}
		}

		return Boolean.FALSE;
	}
}
