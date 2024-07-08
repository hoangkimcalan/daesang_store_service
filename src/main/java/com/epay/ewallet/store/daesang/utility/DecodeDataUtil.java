package com.epay.ewallet.store.daesang.utility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epay.ewallet.store.daesang.redis.RedisService;
import com.epay.ewallet.store.daesang.request.EncryptDataRequest;
import com.epay.ewallet.store.daesang.security.CryptAesServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service
public class DecodeDataUtil {
	
	private static final Logger log = LogManager.getLogger(DecodeDataUtil.class);
	
	@Autowired
	private RedisService redisService;

	public <T> T decrypt(String requestId, String logCategory, String deviceId, String encryptedData,
			Class<T> className) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		T data = null;
		try {
			log.info("{} | {} | Decrypt data start | deviceId={} | encryptedData={}",
					requestId, logCategory, deviceId, encryptedData);

			if (deviceId == null || deviceId.trim().isEmpty() == true) {
				log.error("{} | {} | Decrypt failed, deviceId is invalid",
						requestId, logCategory);
				return null;
			}

			if (encryptedData == null || encryptedData.trim().isEmpty() == true) {
				log.info("{} | {} | Encrypted data is empty, skip decryption",
						requestId, logCategory);
				return null;
			}

			String key = redisService.get("SETKEY" + deviceId);
			log.info("{} | {} | Get encrypt key done | deviceId={} | key={}", requestId, logCategory, deviceId, key);
			if (key == null || key.trim().isEmpty() == true) {
				log.error("{} | {} | Encrypt key is invalid | deviceId={} | key={}", requestId, logCategory, deviceId,
						key);
				return null;
			}

			CryptAesServiceImpl aes = new CryptAesServiceImpl();
			String decryptedData = aes.decrypt(encryptedData, key);
			log.info("{} | {} | Decrypt data done | decryptedData={}", requestId, logCategory, decryptedData);
			data = gson.fromJson(decryptedData, className);
			
			return data;

		} catch (Exception e) {
			log.fatal("{} | {} | Decrypt data fail | exception={}", requestId, logCategory, e);
			return null;

		}
	}

	public <T> String encrypt(String requestId, String logCategory, String deviceId, T data) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String encryptedData = "";

		try {
//			log.info("{} | {} | Encrypt data start | deviceId={} | data={}",
//					requestId, logCategory, deviceId, gson.toJson(data));

			if (deviceId == null || deviceId.trim().isEmpty() == true) {
				log.error("{} | {} | Encrypt failed, deviceId is invalid",
						requestId, logCategory);
				return null;
			}

			String key = redisService.get("SETKEY" + deviceId);
			log.info("{} | {} | Get encrypt key done | deviceId={} | key={}", requestId, logCategory, deviceId, key);
			if (key == null || key.trim().isEmpty() == true) {
				log.error("{} | {} | Encrypt key is invalid | deviceId={} | key={}", requestId, logCategory, deviceId,
						key);
				return "";
			}

			CryptAesServiceImpl aes = new CryptAesServiceImpl();
			encryptedData = aes.encrypt(gson.toJson(data), key);
			log.info("{} | {} | Encrypt data done | Encrypted={}", requestId, logCategory, encryptedData);

			return encryptedData;

		} catch (Exception e) {
			log.fatal("{} | {} | Encrypt response fail | exception={}", requestId, logCategory, e);
			return "";

		}
	}

	public <T> T getRequest(
			String requestId, String logCategory,
			JsonNode requestBody, Class<T> className,
			boolean encrypted, String deviceId) {
		T request = null;

		if (encrypted == true) {
			ObjectMapper objectMapper = new ObjectMapper();
			EncryptDataRequest enRequest = objectMapper.convertValue(requestBody, EncryptDataRequest.class);
			/**
			 * Decrypt request
			 */
			request = decrypt(requestId, logCategory, deviceId, enRequest.getData(), className);
		} else {
			ObjectMapper objectMapper = new ObjectMapper();
			request = objectMapper.convertValue(requestBody, className);
		}

		return request;
	}
	
}
