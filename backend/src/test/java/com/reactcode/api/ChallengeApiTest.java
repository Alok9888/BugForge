package com.reactcode.api;

import com.reactcode.ReactCodeApplication;
import com.reactcode.api.dto.TestCaseResultDto;
import com.reactcode.domain.Challenge;
import com.reactcode.service.EvaluationClient;
import com.reactcode.service.EvaluationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ReactCodeApplication.class)
@AutoConfigureMockMvc
class ChallengeApiTest {

    private static final String USER_ID = "aeb14af2-5d18-433a-8b5e-4ad9e85599c1";

    @Autowired private MockMvc mvc;
    @MockBean private EvaluationClient evaluator;

    @BeforeEach
    void configureTrustedEvaluationResult() {
        when(evaluator.evaluate(any(Challenge.class), anyString())).thenReturn(new EvaluationResult(3, 0, 3, List.of(
                new TestCaseResultDto("starts-at-zero", "Initially displays 0", true, null),
                new TestCaseResultDto("increment-once", "Clicking Increment changes the count to 1", true, null),
                new TestCaseResultDto("increment-twice", "Clicking Increment again changes the count to 2", true, null)
        )));
    }

    @Test
    void listsTenMvpProblems() throws Exception {
        mvc.perform(get("/api/problems").queryParam("catalog", "mvp").queryParam("page", "0").queryParam("size", "10")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.content[0].slug").value("counter"));
    }

    @Test
    void problemDetailsDoNotExposePrivateTests() throws Exception {
        mvc.perform(get("/api/problems/counter").header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.starterCode").exists())
                .andExpect(jsonPath("$.tests").doesNotExist());
    }

    @Test
    void sourceOnlySubmissionReturnsRunnerResults() throws Exception {
        mvc.perform(post("/api/submissions").header("X-User-Id", USER_ID)
                        .contentType("application/json")
                        .content("{\"challengeSlug\":\"counter\",\"code\":\"export default function App() { return null; }\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PASSED"))
                .andExpect(jsonPath("$.passedCount").value(3))
                .andExpect(jsonPath("$.totalCount").value(3))
                .andExpect(jsonPath("$.verificationSource").value("SERVER_RUNNER"));
    }
}
