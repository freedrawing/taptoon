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

    @Value("${SLACK_WEBHOOK_URL}")
    private final String slackWebhookUrk;

    public void sendSlackMessage(String message) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> request = new HashMap<>();
        request.put("text", message);

        restTemplate.postForObject(slackWebhookUrk, request, String.class);
    }
}
