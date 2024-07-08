package com.epay.ewallet.store.daesang.api;


import com.epay.ewallet.store.daesang.constant.EcodeConstant;
import com.epay.ewallet.store.daesang.controller.UserController;
import com.epay.ewallet.store.daesang.request.SupportRequest;
import com.epay.ewallet.store.daesang.response.CommonResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SupportCenterApi {

    private static final Logger log = LogManager.getLogger(UserController.class);

    private static final Gson gson = new Gson();

    @Value("${URL_SUPPORT_CENTER_SERVICE}")
    private String url;

    public CommonResponse<Object> createSupportRequest(SupportRequest request, String requestId, String token, Map<String, String> header) {

        String createSupportURL = url + "/support/createSupportRequest";
        String jsonObject = gson.toJson(request);
        log.info("{} |  createOrder | jsonPaymentRequest={} | urlPaymentCheckout={}", requestId, jsonObject,
                createSupportURL);
        CommonResponse<Object> response = getConnectSupportService(jsonObject, createSupportURL, requestId, token, header);
        log.info("{} |  createOrder | jsonPaymentRequest={} | urlPaymentCheckout={} | response={}", requestId,
                jsonObject, createSupportURL, response);
        return response;
    }

    private CommonResponse<Object> getConnectSupportService(String jsonObject, String endPoint, String requestId,
                                                           String token, Map<String, String> header) {

        String device = header.get("device");
        String language = header.get("language");
        CommonResponse<Object> res = new CommonResponse<Object>();
        log.info("{} | Connection support center Service start | jsonObject={} | endPoint={}  ", requestId, jsonObject,
                endPoint);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        Gson gson = new Gson();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HttpPost request = new HttpPost(endPoint);
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(jsonObject, "UTF-8"));
            request.addHeader("Authorization", "Bearer " + token);
            request.addHeader("requestId", requestId);
            request.addHeader("device", device);
            request.addHeader("language", language);
            HttpResponse response = httpClient.execute(request);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entityApi = response.getEntity();
            String rs = EntityUtils.toString(entityApi, "UTF-8");
            if (status == HttpStatus.SC_OK) {

                res = objectMapper.readValue(rs, CommonResponse.class);

            } else {
                log.info("{} | Connection support center Service | status={} | body={} ", requestId, status, rs);
                res.setData(EcodeConstant.PENDING);
                res.setEcode(EcodeConstant.PENDING);
            }
        } catch (Exception e) {
            log.fatal("{} | Connection support center Service | error={}", requestId, e);
            res.setEcode(EcodeConstant.EXCEPTION);
            res.setMessage(e.getMessage());
        }
        return res;
    }


}
