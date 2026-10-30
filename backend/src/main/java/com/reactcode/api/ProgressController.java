package com.reactcode.api;

import com.reactcode.api.dto.ChallengeProgressDto;
import com.reactcode.api.dto.ProgressDto;
import com.reactcode.domain.AppUser;
import com.reactcode.service.ProgressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public ProgressDto get(HttpServletRequest request) {
        AppUser user = (AppUser) request.getAttribute(CurrentUserFilter.USER_ATTR);
        return progressService.getProgress(user);
    }

    @GetMapping("/{challengeSlug}")
    public ChallengeProgressDto getOne(HttpServletRequest request, @PathVariable String challengeSlug) {
        AppUser user = (AppUser) request.getAttribute(CurrentUserFilter.USER_ATTR);
        return progressService.getChallengeProgress(user, challengeSlug);
    }
}
