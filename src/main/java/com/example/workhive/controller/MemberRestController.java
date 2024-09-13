package com.example.workhive.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.workhive.domain.dto.MemberDTO;
import com.example.workhive.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberRestController {
	
	private final MemberService service;

    // 현재 로그인된 사용자의 memberId를 반환하는 API
    @GetMapping("/getCurrentUserMemberId")
    public String getCurrentUserMemberId() {
        // Spring Security를 사용하여 현재 인증된 사용자의 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String memberId = authentication.getName(); // 로그인한 사용자의 ID를 가져옴
            log.info("Current logged in user memberId: {}", memberId);
            return memberId;
        }
        return "No user is currently logged in.";
    }
    
    // 모든 회원 목록을 반환하는 API
    @GetMapping("/getmembers")
    public List<MemberDTO> getAllMembers() {
        return service.getAllMembers();
    }
}

