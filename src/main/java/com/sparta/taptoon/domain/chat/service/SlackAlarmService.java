package com.sparta.taptoon.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SlackAlarmService {

    @Value("${slack.webhook-url}")
//    private final String slackWebhookUrl = "https://hooks.slack.com/services/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";
    private final String slackWebhookUrl;

    public void sendSlackMessage(String message) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> request = new HashMap<>();
        request.put("text", message);

        restTemplate.postForObject(slackWebhookUrl, request, String.class);
    }
}
