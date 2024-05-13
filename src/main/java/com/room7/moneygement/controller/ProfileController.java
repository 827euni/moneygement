package com.room7.moneygement.controller;

import com.room7.moneygement.dto.CommentDTO;
import com.room7.moneygement.dto.ResponseDto;
import com.room7.moneygement.model.User;
import com.room7.moneygement.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final FollowService followService;
    private final CommentService commentService;
    private final S3Upload s3Upload;

    @GetMapping("/profile/{userId}")
    public String profile(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page, Model model) {
        User user = userService.findById(userId);

        if (user != null) {
            model.addAttribute("user", user);

            // 팔로워 수와 팔로잉 수
            int followersCount = followService.getFollowers(user.getUserId()).size();
            int followingCount = followService.getFollowing(user.getUserId()).size();
            model.addAttribute("followersCount", followersCount);
            model.addAttribute("followingCount", followingCount);

            // 방명록 목록 조회 및 모델에 추가
            Page<CommentDTO> comments = commentService.getCommentsByUserId(userId, page, 6);
            model.addAttribute("comments", comments.getContent());
            model.addAttribute("currentPage", comments.getNumber());
            model.addAttribute("totalPages", comments.getTotalPages());

            // 새로운 방명록 작성을 위한 DTO 객체 추가
            model.addAttribute("newComment", new CommentDTO());

            return "main/profile";
        } else {
            return "redirect:/";
        }
    }

    //-------------------------------------------------------------------------------------------------

    @PostMapping("/profile/{userId}/comments")
    public String createComment(@PathVariable Long userId, @ModelAttribute CommentDTO commentDTO) {
        commentService.createComment(commentDTO);
        return "redirect:/profile/" + userId;
    }

    @PostMapping("/profile/{userId}/comments/{commentId}/update")
    public String updateComment(@PathVariable Long userId, @PathVariable Long commentId, @ModelAttribute CommentDTO commentDTO) {
        commentService.updateComment(commentId, commentDTO);
        return "redirect:/profile/" + userId;
    }


    @PostMapping("/profile/{userId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return "redirect:/profile/" + userId;
    }

    @GetMapping("/profile/{userId}/comments/{commentId}/edit")
    public String editComment(@PathVariable Long userId, @PathVariable Long commentId, Model model) {
        CommentDTO comment = commentService.getCommentById(commentId);
        model.addAttribute("comment", comment);
        model.addAttribute("userId", userId);
        return "main/comment-edit";
    }

    //------------------------------------------------------------------------------------------------------


    @GetMapping("/profile-detail")
    public String getProfileDetailPage(@AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        User user = customUserDetails.getUser();
        model.addAttribute("user", user);
        return "main/profileDetail";
    }

    @PostMapping("/profileDetail/upload")
    public ResponseEntity<ResponseDto> imageUpload(@RequestPart(name = "profileImg", required = false) MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseDto("Invalid file. Please select a file to upload.", null));
        }
        try {
            String imageUrl = s3Upload.uploadFiles(multipartFile, "static");
            return ResponseEntity.ok(new ResponseDto("File uploaded successfully.", imageUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDto("Failed to upload file.", null));
        }
    }

    //------------------------------------------------------------------------------------------
    //프로필 이미지 업데이트

    @PostMapping("/updateProfileImage")
    public ResponseEntity<String> updateProfileImage(@RequestBody Map<String, String> request) {
        String imageUrl = request.get("imageUrl");

        // 현재 인증된 사용자 정보 가져오기 (Spring Security 사용 시)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user != null) {
            // User 객체의 프로필 이미지 URL 업데이트
            user.setProfileImg(imageUrl);
            userService.save(user); // 데이터베이스에 저장

            return ResponseEntity.ok("Profile image updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}
