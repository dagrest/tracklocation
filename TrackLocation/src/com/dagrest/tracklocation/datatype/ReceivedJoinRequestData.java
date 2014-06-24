package com.dagrest.tracklocation.datatype;

public class ReceivedJoinRequestData {
	private String phoneNumber;
	private String mutualId;
	private String RegId;
	private String account;
	private String timestamp;
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getMutualId() {
		return mutualId;
	}
	public void setMutualId(String mutualId) {
		this.mutualId = mutualId;
	}
	public String getRegId() {
		return RegId;
	}
	public void setRegId(String regId) {
		RegId = regId;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}