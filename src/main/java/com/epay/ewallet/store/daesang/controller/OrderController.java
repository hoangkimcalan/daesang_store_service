package com.epay.ewallet.store.daesang.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.epay.ewallet.store.daesang.authen.JwtTokenUtil;
import com.epay.ewallet.store.daesang.constant.EcodeConstant;
import com.epay.ewallet.store.daesang.constant.LogCategory;
import com.epay.ewallet.store.daesang.entities.Order;
import com.epay.ewallet.store.daesang.model.Ecode;
import com.epay.ewallet.store.daesang.request.GetAllOrderRequest;
import com.epay.ewallet.store.daesang.request.GetOrderDetailRequest;
import com.epay.ewallet.store.daesang.request.MobileBuyNowRequest;
import com.epay.ewallet.store.daesang.request.RetryPlaceOrderRequest;
import com.epay.ewallet.store.daesang.request.SelectDeliveryAddressRequest;
import com.epay.ewallet.store.daesang.response.CommonResponse;
import com.epay.ewallet.store.daesang.service.OrderService;
import com.epay.ewallet.store.daesang.utility.CodeService;
import com.epay.ewallet.store.daesang.utility.DecodeDataUtil;
import com.epay.ewallet.store.daesang.utility.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

@RestController
@RequestMapping("/order")
public class OrderController {

	private static final Logger logger = LogManager.getLogger(OrderController.class);
	private static final Gson gson = new Gson();

	@Autowired private DecodeDataUtil decodeData;
	@Autowired private JwtTokenUtil jwtTokenUtil;
	@Autowired private CodeService codeService;
	@Autowired private OrderService orderService;
	

	@PostMapping("/getAllOrders")
	@ResponseBody
	public CommonResponse<Object> getAllOrder(@RequestHeader Map<String, String> header,
			@RequestBody(required = true) JsonNode requestBody,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		CommonResponse<Object> response = new CommonResponse<Object>();
		String logCategory = LogCategory.LOG_GET_ALL_ORDER;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);

		try {
			GetAllOrderRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					GetAllOrderRequest.class, encrypted, deviceId);

			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);
			
			response = orderService.getAllOrders(onlineUserId, userCompanyId, request, logCategory, requestId);
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
				logger.info("{} | {} | Create raw response done | rawResponse={}", logCategory, requestId,
						gson.toJson(response));

				String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
				response.setData(encryptedData);
			}

			logger.info("{} | {} | End | response={}", logCategory, requestId, gson.toJson(response));
		}
	}

	@PostMapping("/getOrderDetails")
	@ResponseBody
	public CommonResponse<Object> getOrderDetail(@RequestHeader Map<String, String> header,
			@RequestBody(required = true) JsonNode requestBody,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		String logCategory = LogCategory.LOG_GET_ORDER_DETAIL;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);

		CommonResponse<Object> response = new CommonResponse<Object>();
		
		try {
			GetOrderDetailRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					GetOrderDetailRequest.class, encrypted, deviceId);

			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);

			response = orderService.getOrderDetail(onlineUserId, userCompanyId, request,
					logCategory, requestId, header);
			return response;

		} catch (Exception e) {
			logger.fatal("{} | {} | Exception | error={}", logCategory, requestId, e.getMessage(),
					e);

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

	@PostMapping("/selectDeliveryAddress")
	@ResponseBody
	public CommonResponse<Object> selectDeliveryAddress(@RequestHeader Map<String, String> header,
			@RequestBody(required = true) JsonNode requestBody,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		String logCategory = LogCategory.LOG_SELECT_DELIVERY_ADDRESS;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);
		CommonResponse<Object> response = new CommonResponse<Object>();

		try {
			SelectDeliveryAddressRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					SelectDeliveryAddressRequest.class, encrypted, deviceId);

			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);

			response = orderService.selectDeliveryAddress(request, onlineUserId, userCompanyId, requestId);
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
				logger.info("{} | {} | Create raw response done | rawResponse={}", logCategory, requestId,
						gson.toJson(response));

				String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
				response.setData(encryptedData);
			}
		}
	}

	
	@PostMapping("/createOrder")
	@ResponseBody
	public CommonResponse<Object> createOrder(@RequestHeader Map<String, String> header,
			@RequestBody(required = true) JsonNode requestBody,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		String logCategory = LogCategory.LOG_CREATE_ORDER;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);
		CommonResponse<Object> response = new CommonResponse<Object>();

		try {
			MobileBuyNowRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					MobileBuyNowRequest.class, encrypted, deviceId);

			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);

			response = orderService.createOrder(request, onlineUserId, userCompanyId, onlineUserphone, header,
					requestId);
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
				logger.info("{} | {} | Create raw response done | rawResponse={}", logCategory, requestId,
						gson.toJson(response));

				String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
				response.setData(encryptedData);
			}
		}
	}
	
	
	@PostMapping("/retryPlaceOrder")
	@ResponseBody
	public CommonResponse<Object> retryPlaceOrder(@RequestHeader Map<String, String> header,
			@RequestBody(required = true) JsonNode requestBody,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		String logCategory = LogCategory.LOG_RETRY_PLACE_ORDER;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);
		CommonResponse<Object> response = new CommonResponse<Object>();

		try {
			RetryPlaceOrderRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					RetryPlaceOrderRequest.class, encrypted, deviceId);

			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);

			response = orderService.retryPlaceOrder(request, onlineUserId, userCompanyId, onlineUserphone, header,
					requestId);
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
				logger.info("{} | {} | Create raw response done | rawResponse={}", logCategory, requestId,
						gson.toJson(response));

				String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
				response.setData(encryptedData);
			}
		}
	}
	
}
