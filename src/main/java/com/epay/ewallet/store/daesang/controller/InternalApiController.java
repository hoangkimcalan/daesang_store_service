package com.epay.ewallet.store.daesang.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.epay.ewallet.store.daesang.constant.EcodeConstant;
import com.epay.ewallet.store.daesang.constant.LogCategory;
import com.epay.ewallet.store.daesang.entities.Order;
import com.epay.ewallet.store.daesang.model.Ecode;
import com.epay.ewallet.store.daesang.response.CommonResponse;
import com.epay.ewallet.store.daesang.service.OrderService;
import com.epay.ewallet.store.daesang.utility.CodeService;
import com.google.gson.Gson;

@Controller
@RequestMapping("/api")
public class InternalApiController {
	
	private Logger logger = LogManager.getLogger(InternalApiController.class);
	private Gson gson = new Gson();
	
	@Autowired private CodeService codeService;
	@Autowired private OrderService orderService;
	
	
	/**
	 * This API is used for payment-service to double-check before confirming payment
	 * @param header
	 * @param requestBody
	 * @param encrypted
	 * @return
	 */
	@PostMapping("/verify")
	@ResponseBody
	public CommonResponse<Object> verifyOrder(@RequestHeader Map<String, String> header,
			@RequestBody(required = true) String requestBody) {
		String logCategory = LogCategory.LOG_VERIFY_ORDER;
		String requestId = header.get("requestid");
		String language = header.get("language");
		CommonResponse<Object> response = new CommonResponse<Object>();

		try {
			Order order = gson.fromJson(requestBody, Order.class);

			logger.info("{} | {} | Header={}", logCategory, requestId, gson.toJson(header));
			logger.info("{} | {} | Order Request={}", logCategory, requestId, gson.toJson(order));

			response = orderService.verifyOrder(order, requestId, language);
			
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
			logger.info("{} | {} | Create raw response done | rawResponse={}", logCategory, requestId,
					gson.toJson(response));
		}
	}
	
}
