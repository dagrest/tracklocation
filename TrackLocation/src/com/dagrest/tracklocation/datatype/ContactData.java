package com.dagrest.tracklocation.datatype;

import com.dagrest.tracklocation.Controller;

public class ContactData {
	private String nick; // free text
	private String email;
//	private String registration_id;
	private String firstName;
	private String lastName;
	
	public ContactData() {}
	
	public ContactData(String email) {
		this.email = email;
	}
	
	public String getNick() {
		if (nick == null || nick.isEmpty()){
			if (email == null || email.isEmpty()){
				nick = "unknown";
				email = "unknown@unknown.com";
			}
			else{
				nick = Controller.getNickNameFromEmail(email);	
			}
		}
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
//	public String getRegistration_id() {
//		return registration_id;
//	}
//	public void setRegistration_id(String registration_id) {
//		this.registration_id = registration_id;
//	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
