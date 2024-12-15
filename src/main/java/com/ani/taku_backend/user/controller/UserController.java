package com.ani.taku_backend.user.controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ani.taku_backend.auth.util.JwtUtil;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.common.exception.JwtException;
import com.ani.taku_backend.common.exception.UserException;
import com.ani.taku_backend.common.model.MainResponse;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.user.model.dto.OAuthUserInfo;
import com.ani.taku_backend.user.model.dto.RequestRegisterUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "User API", description = "유저 관련 API")
public class UserController {

	private final JwtUtil jwtUtil;
	private final UserService userService;
	private final FileService fileService;

	@PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(
		summary = "유저 등록",
		description = "유저를 등록합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "유저 등록 성공"),
		@ApiResponse(responseCode = "409", description = "유저 이미 존재"),
		@ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
		@ApiResponse(responseCode = "503", description = "파일 업로드 실패")
	})
	public ResponseEntity<MainResponse<String>> registerUser(
		@RequestPart("user") @Parameter(
			description = "유저 정보 <code>RequestRegisterUser</code>스키마 참고 <code>Content-Type: application/json</code>",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = RequestRegisterUser.class)
			)
		) RequestRegisterUser requestRegisterUser,
	
		@RequestPart(value = "profileImage", required = false) @Parameter(
			description = "프로필 이미지 파일 <code>png, jpg, jpeg만 가능</code>",
			content = @Content(
				mediaType = "image/*"
			)
		) MultipartFile profileImage,
	
		@RequestHeader("X-Registration-Token") @Parameter(
			name = "X-Registration-Token",
			in = ParameterIn.HEADER,
			required = true,
			description = "OAuth 인증 후 발급받은 임시 토큰",
			example = "Bearer eyJhbGciOiJIUzI1NiIs..."
		) String registrationToken
	) throws IllegalArgumentException {
		
		log.info("registrationToken : {}", registrationToken);
		log.info("requestRegisterUser : {}", requestRegisterUser);

		if (!this.jwtUtil.validateToken(registrationToken)) {
			throw new JwtException.InvalidTokenException("유효하지 않은 토큰입니다.");
		}

		OAuthUserInfo userInfo = OAuthUserInfo.of(requestRegisterUser.getProviderType(), this.jwtUtil.extractAllClaims(registrationToken));
		userInfo.setNickname(requestRegisterUser.getNickname());

		// 이미 가입된 유저인지 확인
		Optional<User> byDomesticId = this.userService.getUser(userInfo.getEmail());

		if (byDomesticId.isPresent()) {
			throw new UserException.UserAlreadyExistsException("이미 가입된 유저입니다.");
		}

		// 프로필 이미지 업로드
		if (profileImage != null) {	
			try {
				userInfo.setImageUrl(this.fileService.uploadFile(profileImage));
			} catch (IOException e) {
				throw new FileException.FileUploadException("파일 업로드 실패");
			}
		}

		// 유저 등록
		User savedUser = this.userService.registerUser(userInfo);

		// TODO : 바로 로그인한다면 토큰을 던져주고 , 바로로그인 안한면 아무것도 던지지 않을 예정
		return ResponseEntity.status(HttpStatus.CREATED).body(MainResponse.getSuccessResponse(null));
	}

	
	@GetMapping("/nickname/{nickname}")
	@Operation(
		summary = "닉네임 중복여부 확인",
		description = "닉네임 중복여부 확인"
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "닉네임 중복여부 확인 성공 <code>true : 중복, false : 중복X</code>"),
	})
	public ResponseEntity<MainResponse<Boolean>> checkNickname(@PathVariable("nickname") @Parameter(
		description = "닉네임",
		example = "looco"
	) String nickname) {
		return ResponseEntity.ok(MainResponse.getSuccessResponse(this.userService.checkNickname(nickname)));
	}
}

