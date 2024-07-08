package com.epay.ewallet.store.daesang.utility;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epay.ewallet.store.daesang.mapperOne.IEcode;
import com.epay.ewallet.store.daesang.model.Ecode;

@Service
public class CodeService {

	private static final Logger log = LogManager.getLogger(CodeService.class);

	@Autowired
	private IEcode iEcode;

	public String getMessageByCode(String lang, String code) {
		HashMap<String, String> map = new HashMap<>();
		if (lang == null || lang.trim().isEmpty() == true) {
			lang = "EN";
		}
		map.put("language", lang);
		map.put("ERROR_CODE", code);

		String message = "";
		try {
			map = iEcode.getMessageByCode(map);
			message = (String) map.get("ERROR_MESSAGE");
		} catch (Exception e) {
			log.fatal("getMessageByCode | Could not get error message | code={} | language={}", code, lang);
		}

		if (message == null || message.trim().isEmpty() == true) {
			log.warn("getMessageByCode | Error message is empty or does not exist | code={} | language={}", code, lang);
			message = getMessagePlaceholder(code, lang);
		}

		return message;
	}

	public Ecode getEcode(String code, String language) {
		HashMap<String, String> map = new HashMap<>();
		if (language == null || language.trim().isEmpty() == true) {
			language = "EN";
		}
		map.put("ERROR_LANG", language);
		map.put("ERROR_CODE", code);

		Ecode ecode = iEcode.getEcode(map);

		if (ecode == null) {
			log.error("CodeService | GET_ECODE | Ecode is not exist | ecode={} | language={}", code, language);
			ecode = new Ecode();
			ecode.setEcode(code);
			ecode.setLanguage(language);
			ecode.setMessage("");
			ecode.setP_ecode("");
			ecode.setP_message("");
		}

		if (ecode.getMessage() == null || ecode.getMessage().trim().isEmpty() == true) {
			log.warn("CodeService | GET_ECODE | Ecode message is empty | ecode={} | language={}", code, language);
			ecode.setMessage(getMessagePlaceholder(code, language));
		}

		if (ecode.getP_message() == null || ecode.getP_message().trim().isEmpty() == true) {
			log.warn("CodeService | GET_ECODE | p_ecode is empty | ecode={} | language={}", code, language);
			ecode.setP_ecode("");
		}

		if (ecode.getP_message() == null || ecode.getP_message().trim().isEmpty() == true) {
			log.warn("CodeService | GET_ECODE | p_message is empty | ecode={} | language={}", code, language);
			ecode.setP_message(getMessagePlaceholder(code, language));
		}

		return ecode;
	}

	public String getMessagePlaceholder(String code, String language) {
		return code + "_" + language;
	}
}
