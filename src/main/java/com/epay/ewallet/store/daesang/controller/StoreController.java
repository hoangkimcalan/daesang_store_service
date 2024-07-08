package com.epay.ewallet.store.daesang.controller;

import com.epay.ewallet.store.daesang.authen.JwtTokenUtil;
import com.epay.ewallet.store.daesang.constant.AppConstant;
import com.epay.ewallet.store.daesang.constant.EcodeConstant;
import com.epay.ewallet.store.daesang.constant.LogCategory;
import com.epay.ewallet.store.daesang.model.Ecode;
import com.epay.ewallet.store.daesang.request.GetProductDetailsRequest;
import com.epay.ewallet.store.daesang.request.GetStoreDeliverAddressRequest;
import com.epay.ewallet.store.daesang.request.SearchProductsRequest;
import com.epay.ewallet.store.daesang.response.CommonResponse;
import com.epay.ewallet.store.daesang.service.StoreService;
import com.epay.ewallet.store.daesang.utility.CodeService;
import com.epay.ewallet.store.daesang.utility.DecodeDataUtil;
import com.epay.ewallet.store.daesang.utility.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/store")
public class StoreController {

	private static final Logger logger = LogManager.getLogger(StoreController.class);
	private static final Gson gson = new Gson();

	@Autowired
	private DecodeDataUtil decodeData;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private CodeService codeService;

	@Autowired
	private StoreService storeService;

	@PostMapping("/visitDaesangStore")
	@ResponseBody
	public CommonResponse<Object> accessDaesangStore(@RequestHeader Map<String, String> header,
			@RequestBody(required = false) JsonNode requestBody,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		String logCategory = LogCategory.LOG_VISIT_STORE;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);

		CommonResponse<Object> response = new CommonResponse<Object>();
		
		try {
			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);

			response = storeService.visitDaesangStore(onlineUserId, userCompanyId, logCategory, requestId);
			return response;

		} catch (Exception e) {
			logger.fatal("{} | {} | Exception | error={}", logCategory, requestId, e.getMessage(), e);

			response.setEcode(EcodeConstant.EXCEPTION);
			return response;

		} finally {
			/**
			 * Actions before return
			 */
			if (response.getMessage() == null || response.getMessage().isEmpty()) {
				// Set ecode message, p_ecode, p_message
				Ecode ecode = codeService.getEcode(response.getEcode(), language);
				response.setMessage(ecode.getMessage());
				response.setP_ecode(ecode.getP_ecode());
				response.setP_message(ecode.getP_message());
			}

			/**
			 * Encrypt data
			 */
			if (encrypted == true) {
				logger.info("{} | {} | Create raw response done | rawResponse={}", logCategory,
						requestId, gson.toJson(response));
				String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId,
						response.getData());
				response.setData(encryptedData);
			}

		}
	}

	@PostMapping("/getStoreDeliveryAddress")
	@ResponseBody
	public CommonResponse<Object> getStoreDeliveryAddress(@RequestHeader Map<String, String> header,
			@RequestBody(required = true) JsonNode requestBody,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {

		CommonResponse<Object> response = new CommonResponse<Object>();
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);

		try {
			GetStoreDeliverAddressRequest request = decodeData.getRequest(requestId, LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, requestBody,
					GetStoreDeliverAddressRequest.class, encrypted, deviceId);
			
			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, requestId, onlineUserphone, onlineUserId);
			
			response = storeService.getStoreDeliveryAddress(request, onlineUserId, userCompanyId, requestId);
			return response;
		} catch (Exception e) {
			logger.fatal("{} | {} | Exception | error={}", LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, requestId,
					e.getMessage(), e);
			response.setEcode(EcodeConstant.EXCEPTION);
			return response;
		} finally {

			// action before return
			if (response.getMessage() == null || response.getMessage().isEmpty()) {
				// Set ecode message, p_ecode, p_message
				Ecode ecode = codeService.getEcode(response.getEcode(), language);
				response.setMessage(ecode.getMessage());
				response.setP_ecode(ecode.getP_ecode());
				response.setP_message(ecode.getP_message());
			}

			// encrypt data
			if (encrypted == true) {
				logger.info("{} | {} | Create raw response done | rawResponse={}",
						LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, requestId, gson.toJson(response));

				String encryptedData = decodeData.encrypt(requestId, LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS,
						deviceId, response.getData());
				response.setData(encryptedData);
			}

		}
	}

	@PostMapping("/getListProduct")
	@ResponseBody
	public CommonResponse<Object> searchProductsByName(@RequestHeader Map<String, String> header,
			@RequestBody(required = false) JsonNode requestBody,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		
		CommonResponse<Object> response = new CommonResponse<>();
		String logCategory = LogCategory.LOG_SEARCH_PRODUCTS_BY_NAME;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);

		try {
			SearchProductsRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					SearchProductsRequest.class, encrypted, deviceId);
			
			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);

			response = storeService.searchProducts(userCompanyId, request, logCategory, requestId);
			return response;
		} catch (Exception e) {
			logger.fatal("{} | {} | Exception | error={}", logCategory, requestId, e.getMessage(), e);

			response.setEcode(EcodeConstant.EXCEPTION);
			return response;
		} finally {
			if (response.getMessage() == null || response.getMessage().isEmpty()) {
				// Set ecode message, p_ecode, p_message
				Ecode ecode = codeService.getEcode(response.getEcode(), language);
				response.setMessage(ecode.getMessage());
				response.setP_ecode(ecode.getP_ecode());
				response.setP_message(ecode.getP_message());
			}

			if (encrypted == true) {
				logger.info("{} | {} | Create raw response done | rawResponse={}", logCategory, requestId,
						gson.toJson(response));

				String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
				response.setData(encryptedData);
			}
		}
	}

	@PostMapping("/getProductDetails")
	@ResponseBody
	public CommonResponse<Object> getProductDetails(@RequestHeader Map<String, String> header,
			@RequestBody(required = false) JsonNode requestBody,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		
		CommonResponse<Object> response = new CommonResponse<>();
		String logCategory = LogCategory.LOG_GET_PRODUCT_DETAILS;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);

		try {
			GetProductDetailsRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					GetProductDetailsRequest.class, encrypted, deviceId);
			
			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);

			response = storeService.getProductDetailsById(onlineUserId, userCompanyId, request, logCategory, requestId);
			return response;
		} catch (Exception e) {
			logger.fatal("{} | {} | Exception | error={}", logCategory, requestId, e.getMessage(), e);

			response.setEcode(EcodeConstant.EXCEPTION);
			return response;
		} finally {
			if (response.getMessage() == null || response.getMessage().isEmpty()) {
				// Set ecode message, p_ecode, p_message
				Ecode ecode = codeService.getEcode(response.getEcode(), language);
				response.setMessage(ecode.getMessage());
				response.setP_ecode(ecode.getP_ecode());
				response.setP_message(ecode.getP_message());
			}

			if (encrypted == true) {
				logger.info("{} | {} | Create raw response done | rawResponse={}", logCategory, requestId,
						gson.toJson(response));

				String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
				response.setData(encryptedData);
			}
		}
	}
}
