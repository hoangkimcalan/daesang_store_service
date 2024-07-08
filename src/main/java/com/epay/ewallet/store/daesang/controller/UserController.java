package com.epay.ewallet.store.daesang.controller;

import com.epay.ewallet.store.daesang.api.SupportCenterApi;
import com.epay.ewallet.store.daesang.authen.JwtTokenUtil;
import com.epay.ewallet.store.daesang.constant.EcodeConstant;
import com.epay.ewallet.store.daesang.constant.LogCategory;
import com.epay.ewallet.store.daesang.model.Ecode;
import com.epay.ewallet.store.daesang.request.SupportRequest;
import com.epay.ewallet.store.daesang.response.CommonResponse;
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
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);
    private static final Gson gson = new Gson();

    @Autowired
    private DecodeDataUtil decodeData;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CodeService codeService;

    @Autowired
    private SupportCenterApi supportCenterApi;



    @PostMapping("/requestSupport")
    @ResponseBody
    public CommonResponse<Object>  requestSupport(@RequestHeader Map<String, String> header,
                                                  @RequestBody(required = false) JsonNode requestBody,
                                                  @RequestParam(required = false, defaultValue = "true") boolean encrypted){
            String requestId = header.get("requestid");
            String language = header.get("language");
            String deviceId = Utils.getDeviceIdFromHeader(header);

            SupportRequest request = gson.fromJson(requestBody.toString(), SupportRequest.class);
            logger.info("{} | {} | Header={}", LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, requestId, gson.toJson(header));
            logger.info("{} | {} | Request={}", LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, requestId, gson.toJson(request));

            CommonResponse<Object> response = new CommonResponse<Object>();
            try {
//             get token from header, then get userId and phone from token
                String bearerToken = header.get("authorization");
                String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
                long onlineUserId = Long.parseLong(jwtTokenUtil.getAllClaimsFromToken(token).get("userId", String.class));
                String onlineUserphone = jwtTokenUtil.getUsernameFromToken(token);

                response = supportCenterApi.createSupportRequest(request, requestId, token, header);
                return response;
            } catch (Exception e) {
                logger.fatal("{} | {} | Exception | error={}", LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, requestId, e.getMessage(), e);
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
                    logger.info("{} | {} | Create raw response done | rawResponse={}", LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, requestId, gson.toJson(response));

                    String encryptedData = decodeData.encrypt(requestId, LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, deviceId, response.getData());
                    response.setData(encryptedData);
                }
                logger.info("{} | {} | End | response={}", LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS, requestId, gson.toJson(response));

            }
    }
}
