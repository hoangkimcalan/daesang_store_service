package com.epay.ewallet.store.daesang.authen;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.epay.ewallet.store.daesang.response.CommonResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

	private static final long serialVersionUID = -7858869558953243875L;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		String ecode = (String) request.getAttribute("ecode");
		String message = (String) request.getAttribute("message");
		CommonResponse<Object> apiRes = new CommonResponse<Object>();
		apiRes.setMessage(message);
		apiRes.setEcode(ecode);
		OutputStream out = response.getOutputStream();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, apiRes);
		out.flush();
	}
}
