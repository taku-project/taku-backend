package com.ani.taku_backend.user.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ani.taku_backend.auth.util.JwtUtil;
import com.ani.taku_backend.common.exception.JwtException;
import com.ani.taku_backend.common.exception.UserException;
import com.ani.taku_backend.common.model.MainResponse;
import com.ani.taku_backend.user.model.dto.OAuthUserInfo;
import com.ani.taku_backend.user.model.dto.RequestRegisterUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "User API", description = "유저 관련 API")
public class UserController {

	private final JwtUtil jwtUtil;
	private final UserService userService;

	@PostMapping("/register")
	@Operation(summary = "유저 등록", description = "유저를 등록합니다. 반환 값(SUCCESS/FAIL)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "유저 등록 성공"),
		@ApiResponse(responseCode = "409", description = "유저 이미 존재"),
		@ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
	})
	public ResponseEntity<MainResponse<String>> registerUser(
		@Parameter(description = "등록할 유저 정보", required = true) @RequestBody RequestRegisterUser user,
		@Parameter(description = "등록 토큰", required = true , example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") @RequestHeader("X-Registration-Token") String registrationToken
	) throws IllegalArgumentException {
		
		log.info("registrationToken : {}", registrationToken);
		log.info("user : {}", user);

		if (!this.jwtUtil.validateToken(registrationToken)) {
			throw new JwtException.InvalidTokenException("유효하지 않은 토큰입니다.");
		}

		OAuthUserInfo userInfo = OAuthUserInfo.of(user.getProviderType(), this.jwtUtil.extractAllClaims(registrationToken));
		log.info("userInfo : {}", userInfo);

		// 이미 가입된 유저인지 확인
		Optional<User> byDomesticId = this.userService.getUser(userInfo.getEmail());

		if (byDomesticId.isPresent()) {
			throw new UserException.UserAlreadyExistsException("이미 가입된 유저입니다.");
		}

		User savedUser = this.userService.registerUser(userInfo);

		// TODO : 바로 로그인한다면 토큰을 던져주고 , 바로로그인 안한다면 아무것도 던지지 않을 예정
		return ResponseEntity.status(HttpStatus.CREATED).body(MainResponse.getSuccessResponse(null));
	}
	
}

