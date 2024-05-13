package com.room7.moneygement.controller;

import com.room7.moneygement.dto.PasswordChangeDTO;
import com.room7.moneygement.model.User;
import com.room7.moneygement.service.CustomUserDetails;
import com.room7.moneygement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserPageController {
    private final UserService userService;

    // 비밀번호 변경 api
    @PostMapping("/api/change-pw")
    public ResponseEntity<?> changePassword(HttpServletRequest request, HttpServletResponse response,
                                            @RequestBody PasswordChangeDTO passwordChangeDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        boolean success = userService.changePassword(user, passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());

        if (success) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
            return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인해주세요."));
        }
    }

    // 회원 탈퇴 api
    @PostMapping("/api/delete-account")
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, String> credentials,
                                           HttpServletRequest request, HttpServletResponse response) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        String password = credentials.get("password");

        boolean passwordMatch = userService.checkPassword(user, password);
        if(passwordMatch) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
            userService.deleteUser(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "계정이 성공적으로 삭제되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "비밀번호가 잘못되었습니다."));
        }
    }
}
