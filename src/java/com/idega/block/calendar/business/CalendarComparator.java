package com.idega.block.calendar.business;

import java.util.Comparator;

import com.idega.block.calendar.data.CalendarEntry;
import com.idega.util.IWTimestamp;

public class CalendarComparator implements Comparator<CalendarEntry> {

	@Override
	public int compare(CalendarEntry o1, CalendarEntry o2) {
		IWTimestamp stamp1 = new IWTimestamp(o1.getDate());
		IWTimestamp stamp2 = new IWTimestamp(o2.getDate());
		IWTimestamp now = new IWTimestamp();
		
		if (stamp1.getYear() > now.getYear() && stamp2.getYear() <= now.getYear()) {
			return 1;
		}
		else if (stamp1.getYear() <= now.getYear() && stamp2.getYear() > now.getYear()) {
			return -1;
		}
		else if (stamp1.getYear() > now.getYear() && stamp2.getYear() > now.getYear()) {
			if (stamp1.getYear() > stamp2.getYear()) {
				return 1;
			}
			else if (stamp1.getYear() < stamp2.getYear()) {
				return -1;
			}
		}

		if (stamp1.getMonth() > stamp2.getMonth()) {
			return 1;
		}
		else if (stamp1.getMonth() < stamp2.getMonth()) {
			return -1;
		}
		else {
			if (stamp1.getDay() > stamp2.getDay()) {
				return 1;
			}
			else if (stamp1.getDay() < stamp2.getDay()) {
				return -1;
			}
			else {
				if (stamp1.getYear() > stamp2.getYear()) {
					return 1;
				}
				else if (stamp1.getYear() < stamp2.getYear()) {
					return -1;
				}
				else {
					if (stamp1.getHour() > stamp2.getHour()) {
						return 1;
					}
					else if (stamp1.getHour() < stamp2.getHour()) {
						return -1;
					}
					else {
						if (stamp1.getMinute() > stamp2.getMinute()) {
							return 1;
						}
						else if (stamp1.getMinute() < stamp2.getMinute()) {
							return -1;
						}
					}
				}
			}
		}
		
		return 0;
	}
}