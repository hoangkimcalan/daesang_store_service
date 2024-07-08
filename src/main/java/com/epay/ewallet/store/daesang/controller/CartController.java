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
import com.epay.ewallet.store.daesang.model.Ecode;
import com.epay.ewallet.store.daesang.request.AddProductToCartRequest;
import com.epay.ewallet.store.daesang.request.ChangeCartProductQuantityRequest;
import com.epay.ewallet.store.daesang.request.ChooseCartProductRequest;
import com.epay.ewallet.store.daesang.request.MobileCartCheckoutRequest;
import com.epay.ewallet.store.daesang.request.ViewCartRequest;
import com.epay.ewallet.store.daesang.response.CommonResponse;
import com.epay.ewallet.store.daesang.service.CartService;
import com.epay.ewallet.store.daesang.utility.CodeService;
import com.epay.ewallet.store.daesang.utility.DecodeDataUtil;
import com.epay.ewallet.store.daesang.utility.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	private static final Logger logger = LogManager.getLogger(CartController.class);
	private static final Gson gson = new Gson();

	@Autowired private DecodeDataUtil decodeData;
	@Autowired private JwtTokenUtil jwtTokenUtil;
	@Autowired private CodeService codeService;
	
	@Autowired private CartService cartService;
	
	
	@PostMapping("/addProduct")
	@ResponseBody
	public CommonResponse<Object> addProductToCart(@RequestBody(required = true) JsonNode requestBody,
			@RequestHeader(required = true) Map<String, String> header,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		CommonResponse<Object> response = new CommonResponse<Object>();
		String logCategory = LogCategory.LOG_ADD_PRODUCT_TO_CART;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);
		
		try {
			AddProductToCartRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					AddProductToCartRequest.class, encrypted, deviceId);
			
			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);

			response = cartService.addProductToCart(request, userCompanyId, onlineUserId, requestId);
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
	
	
	@PostMapping("/view")
	@ResponseBody
	public CommonResponse<Object> viewCart(@RequestBody(required = true) JsonNode requestBody,
			@RequestHeader(required = true) Map<String, String> header,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		CommonResponse<Object> response = new CommonResponse<Object>();
		String logCategory = LogCategory.LOG_VIEW_CART;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);
		
		try {
			ViewCartRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					ViewCartRequest.class, encrypted, deviceId);
			
			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);

			response = cartService.viewCart(request, userCompanyId, onlineUserId, language, requestId);
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
	
	@PostMapping("/changeProductQuantity")
	@ResponseBody
	public CommonResponse<Object> changeProductQuantity(@RequestBody(required = true) JsonNode requestBody,
			@RequestHeader(required = true) Map<String, String> header,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		CommonResponse<Object> response = new CommonResponse<Object>();
		String logCategory = LogCategory.LOG_CHANGE_PRODUCT_QUANTITY;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);
		
		try {
			ChangeCartProductQuantityRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					ChangeCartProductQuantityRequest.class, encrypted, deviceId);
			
			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);

			response = cartService.changeProductQuantity(request, userCompanyId, onlineUserId, requestId);
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
	
	
	@PostMapping("/chooseProduct")
	@ResponseBody
	public CommonResponse<Object> chooseProductInCart(@RequestBody(required = true) JsonNode requestBody,
			@RequestHeader(required = true) Map<String, String> header,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		CommonResponse<Object> response = new CommonResponse<Object>();
		String logCategory = LogCategory.LOG_CHOOSE_PRODUCT_IN_CART;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);
		
		try {
			ChooseCartProductRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					ChooseCartProductRequest.class, encrypted, deviceId);
			
			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);

			response = cartService.chooseProductInCart(request, onlineUserId, requestId);
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
	
	
	@PostMapping("/checkout")
	@ResponseBody
	public CommonResponse<Object> checkout(@RequestBody(required = true) JsonNode requestBody,
			@RequestHeader(required = true) Map<String, String> header,
			@RequestParam(required = false, defaultValue = "true") boolean encrypted) {
		CommonResponse<Object> response = new CommonResponse<Object>();
		String logCategory = LogCategory.LOG_CART_CHECKOUT;
		String requestId = header.get("requestid");
		String language = header.get("language");
		String deviceId = Utils.getDeviceIdFromHeader(header);
		
		try {
			MobileCartCheckoutRequest request = decodeData.getRequest(requestId, logCategory, requestBody,
					MobileCartCheckoutRequest.class, encrypted, deviceId);
			
			String bearerToken = header.get("authorization");
			String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
			String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);
			long onlineUserId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
			long userCompanyId = Long.valueOf(jwtTokenUtil.getAllClaimsFromToken(token).get("companyId", String.class));

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Request={}", logCategory, requestId, gson.toJson(request));
			logger.info("{} | {} | UserInfo: phone={} | userId={}", logCategory, requestId, onlineUserphone, onlineUserId);
			
			response = cartService.cartCheckout(request, onlineUserId, userCompanyId, requestId, header);
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
	
}
