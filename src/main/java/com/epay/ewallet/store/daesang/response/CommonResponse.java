package com.epay.ewallet.store.daesang.response;

public class CommonResponse<T> {

	private String ecode;
	private T data;
	private String message;
	private String p_ecode;
	private String p_message;

	public String getEcode() {
		return ecode;
	}

	public void setEcode(String ecode) {
		this.ecode = ecode;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getP_ecode() {
		return p_ecode;
	}

	public void setP_ecode(String p_ecode) {
		this.p_ecode = p_ecode;
	}

	public String getP_message() {
		return p_message;
	}

	public void setP_message(String p_message) {
		this.p_message = p_message;
	}

	@Override
	public String toString() {
		return "CommonResponse [ecode=" + ecode + ", data=" + data + ", message=" + message + ", p_ecode=" + p_ecode
				+ ", p_message=" + p_message + "]";
	}

}