package com.idega.block.calendar.bean;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "excludedPeriod")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExcludedPeriod implements Serializable {
	private static final long serialVersionUID = 2765217299124265273L;

	private Date from; //Date format yyyy-MM-dd
	private Date to; //Date format yyyy-MM-dd

	public ExcludedPeriod() {}

	public ExcludedPeriod(Date from, Date to) {
		this.from = from;
		this.to = to;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}
}
