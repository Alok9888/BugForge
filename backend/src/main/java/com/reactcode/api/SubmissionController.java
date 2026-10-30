package com.reactcode.api;

import com.reactcode.api.dto.SubmissionRequest;
import com.reactcode.api.dto.SubmissionResponse;
import com.reactcode.domain.AppUser;
import com.reactcode.service.SubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse create(HttpServletRequest request, @Valid @RequestBody SubmissionRequest body) {
        AppUser user = (AppUser) request.getAttribute(CurrentUserFilter.USER_ATTR);
        return submissionService.submit(user, body);
    }

    @GetMapping("/{id}")
    public SubmissionResponse get(HttpServletRequest request, @PathVariable Long id) {
        AppUser user = (AppUser) request.getAttribute(CurrentUserFilter.USER_ATTR);
        return submissionService.get(user, id);
    }
}
