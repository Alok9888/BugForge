package com.reactcode.service;

import com.reactcode.ReactCodeApplication;
import com.reactcode.domain.AppUser;
import com.reactcode.domain.Difficulty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ReactCodeApplication.class)
class ChallengeServiceTest {

    @Autowired private ChallengeService challenges;
    @Autowired private UserService users;

    @Test
    void listsMvpChallengesAndFiltersByQueryAndDifficulty() {
        AppUser user = users.resolveOrCreate(UUID.randomUUID().toString());
        Page<?> page = challenges.list(user, "todo", Difficulty.MEDIUM, "mvp", 0, 10);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void returnsChallengeStarterCodeAndTests() {
        AppUser user = users.resolveOrCreate(UUID.randomUUID().toString());
        var detail = challenges.getBySlug(user, "counter");

        assertThat(detail.starterCode()).contains("data-testid=\"count\"");
        assertThat(detail.tests().path("cases")).hasSize(3);
    }
}
