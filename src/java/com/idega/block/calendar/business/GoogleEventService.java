/**
 * @(#)GoogleEventService.java    1.0.0 16:22:21
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
package com.idega.block.calendar.business;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.idega.block.calendar.bean.Recurrence;
import com.idega.user.data.bean.User;

/**
 * <p>Manages {@link Event}s</p>
 * <p>You can report about problems to: 
 * <a href="mailto:martynas@idega.is">Martynas Stakė</a></p>
 *
 * @version 1.0.0 2015 gruod. 9
 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
 */
public interface GoogleEventService {

	/**
	 * 
	 * @param users to convert, not <code>null</code>;
	 * @return entities or {@link Collections#emptyList()} on failure;
	 */
	List<EventAttendee> getAttendees(Collection<User> users);

	/**
	 * 
	 * @param user to convert, not <code>null</code>;
	 * @return converted entity or <code>null</code> on failure;
	 */
	List<EventAttendee> getAttendee(User user);

	/**
	 * 
	 * @param user to convert, not <code>null</code>;
	 * @return converted entity or <code>null</code> on failure;
	 */
	List<EventAttendee> getAttendee(com.idega.user.data.User user);

	List<EventAttendee> getLegacyAttendees(
			Collection<com.idega.user.data.User> users);

	/**
	 * 
	 * <p>Removed event or whole recurrence, if exist.</p>
	 * @param calendarService 
	 * @param eventId is {@link Event#getId()}, not <code>null</code>;
	 * @param calendarId is {@link com.google.api.services.calendar.model.Calendar#getId()}, 
	 * not <code>null</code>;
	 * @return <code>true</code> on successful removal, 
	 * <code>false</code> otherwise;
	 */
	boolean remove(Calendar calendarService, String eventId, String calendarId);

	/**
	 *
	 * @param calendarId is Google Calendar id, not <code>null</code>;
	 * @param eventId is Google event id, not <code>null</code>;
	 * @return event created or <code>null</code> on failure;
	 */
	Event getEvent(Calendar calendarService, String calendarId, String eventId);

	/**
	 * 
	 * <p>Creates custom line to define recurrence in Google calendar</p>
	 * @param recurrence to define, not <code>null</code>;
	 * @return recurrence lines for adding to {@link Event#setRecurrence(List)}
	 * or {@link Collections#emptyList()} on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	List<String> getRecurrenceString(Recurrence recurrence);

	/**
	 * 
	 * <p>Creates/updates event in Google calendar</p>
	 * @param calendarService
	 * @param calendarId is Google Calendar id, not <code>null</code>;
	 * @param id is Google event id, new event created when <code>null</code>;
	 * @param name of Google event, not <code>null</code> for creation;
	 * @param description of Google event, skipped if <code>null</code>;
	 * @param location of Google event, skipped if <code>null</code>;
	 * @param type of Google event, skipped if <code>null</code>;
	 * @param from is start date of Google event, not <code>null</code> for creation;
	 * @param to is end date of Google event, not <code>null</code> for creation;
	 * @param recurrence of Google event, skipped if <code>null</code>;
	 * @param attendeeList of Google event, skipped if <code>null</code>;
	 * @return updated event or <code>null</code> on failure;
	 */
	Event update(
			Calendar calendarService, 
			String calendarId, 
			String id,
			String name, 
			String description, 
			String location, 
			String type,
			Date from, 
			Date to, 
			Recurrence recurrence,
			List<EventAttendee> attendeeList);
}
