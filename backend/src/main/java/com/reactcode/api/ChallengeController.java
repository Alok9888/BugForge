package com.reactcode.api;

import com.reactcode.api.dto.ChallengeDetailDto;
import com.reactcode.api.dto.ChallengeSummaryDto;
import com.reactcode.domain.AppUser;
import com.reactcode.domain.Difficulty;
import com.reactcode.service.ChallengeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/problems", "/api/challenges"})
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public Page<ChallengeSummaryDto> list(
            HttpServletRequest request,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(defaultValue = "mvp") String catalog,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        AppUser user = (AppUser) request.getAttribute(CurrentUserFilter.USER_ATTR);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);
        return challengeService.list(user, q, difficulty, catalog, safePage, safeSize);
    }

    @GetMapping("/{slug}")
    public ChallengeDetailDto get(HttpServletRequest request, @PathVariable String slug) {
        AppUser user = (AppUser) request.getAttribute(CurrentUserFilter.USER_ATTR);
        return challengeService.getBySlug(user, slug);
    }
}
