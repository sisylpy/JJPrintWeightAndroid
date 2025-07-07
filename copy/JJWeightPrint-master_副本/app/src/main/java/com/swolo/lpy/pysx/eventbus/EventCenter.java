package com.swolo.lpy.pysx.eventbus;

public class EventCenter<T> {

	// 关闭所有Activity

	// 关闭所有Activity
	public static final int EVENTCODE_CLOSE_ALL_ACTIVITY = -1;
	public static final int EVENT_ADDNEWPURCH = 1000;
	public static final int EVENT_ORDERCONFIRM = 1001;
	public static final int EVENT_ONRESUM = 1002;
	public static final int EVENT_ADDMAN = 1003;
	public static final int EVENT_ADDGUIGE= 1004;


	/**
	 * reserved data
	 */
	private T data;

	/**
	 * this code distinguish between diffEVENT_ADDNEWPURCHerent events
	 */
	private int eventCode = -1;

	public EventCenter(int eventCode) {
		this(eventCode, null);
	}

	public EventCenter(int eventCode, T data) {
		this.eventCode = eventCode;
		this.data = data;
	}

	/**
	 * get event code
	 * 
	 * @return
	 */
	public int getEventCode() {
		return this.eventCode;
	}

	/**
	 * get event reserved data
	 * 
	 * @return
	 */
	public T getData() {
		return this.data;
	}
}
