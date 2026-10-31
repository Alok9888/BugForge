package com.reactcode.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactcode.domain.Challenge;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class HttpEvaluationClientTest {

    @Test
    void sendsPrivateTestsToRunnerAndReadsItsResults() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpEvaluationClient client = new HttpEvaluationClient(
                builder,
                new ObjectMapper(),
                "http://evaluator.test",
                "runner-secret"
        );
        Challenge challenge = Challenge.create();
        challenge.setSlug("counter");
        challenge.setTestsJson("{\"cases\":[{\"id\":\"increments\",\"name\":\"Increments\",\"steps\":[]}]}");

        server.expect(requestTo("http://evaluator.test/evaluate"))
                .andExpect(method(POST))
                .andExpect(header("X-Evaluator-Token", "runner-secret"))
                .andExpect(jsonPath("$.problemSlug").value("counter"))
                .andExpect(jsonPath("$.sourceCode").value("source"))
                .andExpect(jsonPath("$.tests.cases[0].id").value("increments"))
                .andRespond(withSuccess("""
                        {"passedCount":1,"failedCount":0,"totalCount":1,"results":[
                          {"id":"increments","name":"Increments","passed":true,"message":null}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        EvaluationResult result = client.evaluate(challenge, "source");

        assertThat(result.passed()).isTrue();
        assertThat(result.results()).hasSize(1);
        assertThat(result.results().getFirst().id()).isEqualTo("increments");
        server.verify();
    }
}
