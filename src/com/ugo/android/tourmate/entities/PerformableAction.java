package com.ugo.android.tourmate.entities;

public class PerformableAction {

	private String action;
	private String value;
	
	public PerformableAction(String action, String value) {
		this.action = action;
		this.value = value;
	}
	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
