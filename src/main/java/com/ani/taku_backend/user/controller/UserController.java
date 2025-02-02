package com.ani.taku_backend.user.controller;

import com.ani.taku_backend.auth.util.JwtUtil;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.common.exception.UserException;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.user.model.dto.OAuthUserInfo;
import com.ani.taku_backend.user.model.dto.RequestRegisterUser;
import com.ani.taku_backend.user.model.dto.UserDetailDTO;
import com.ani.taku_backend.user.model.dto.requestDto.UpdateProfileImgRequestDTO;
import com.ani.taku_backend.user.model.dto.requestDto.UserEditDTO;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.model.entity.UserStatus;
import com.ani.taku_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static com.ani.taku_backend.common.exception.ErrorCode.INVALID_INPUT_VALUE;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "User API", description = "유저 관련 API")
public class UserController {

	private final JwtUtil jwtUtil;
	private final UserService userService;
	private final FileService fileService;

	@PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(
		summary = "유저 등록",
		description = "유저를 등록합니다...",
		security = { @SecurityRequirement(name = "Bearer Auth") }
	)
	@Parameters({
		@Parameter(
			name = "X-Registration-Token",
			in = ParameterIn.HEADER,
			required = true,
			description = "OAuth 인증 후 발급받은 임시 토큰",
			example = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhZ2VfcmFuZ2..."
		)
	})
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		content = {
			@Content(
				mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
				encoding = {
					@Encoding(
						name = "user", 
						contentType = "application/json"
					),
					@Encoding(
						name = "profileImage",
						contentType = "image/png"
					)
				}
			)
		}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "유저 등록 성공"),
		@ApiResponse(responseCode = "409", description = "유저 이미 존재"),
		@ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
		@ApiResponse(responseCode = "503", description = "파일 업로드 실패")
	})
	public CommonResponse<String> registerUser(
		@RequestPart("user") @Parameter(
			description = "유저 정보 (<code>RequestRegisterUser</code> 스키마 참고) <code>Content-Type : application/json</code>",
			schema = @Schema(implementation = RequestRegisterUser.class)
		) RequestRegisterUser requestRegisterUser,
		@RequestPart(value = "profileImage", required = false) @Parameter(description = "프로필 이미지 파일 (png, jpg, jpeg만 가능)") MultipartFile profileImage,
		@RequestHeader("X-Registration-Token") String registrationToken
	) throws IllegalArgumentException {
		
		log.info("registrationToken : {}", registrationToken);
		log.info("requestRegisterUser : {}", requestRegisterUser);

		if(registrationToken.startsWith("Bearer ")) {
			registrationToken = registrationToken.substring(7);
		}else{
			throw new DuckwhoException(ErrorCode.EMPTY_TOKEN);
		}

		if (!this.jwtUtil.validateToken(registrationToken)) {
			throw new DuckwhoException(ErrorCode.INVALID_TOKEN);
		}

		OAuthUserInfo userInfo = OAuthUserInfo.of(requestRegisterUser.getProviderType(), this.jwtUtil.extractAllClaims(registrationToken));
		userInfo.setNickname(requestRegisterUser.getNickname());

		// 이미 가입된 유저인지 확인
		Optional<User> byDomesticId = this.userService.getUserByDomesticId(userInfo.getDomesticId());

		if (byDomesticId.isPresent()) {
			throw new DuckwhoException(ErrorCode.USER_ALREADY_EXISTS);
		}

		// 프로필 이미지 업로드
		if (profileImage != null) {	
			try {
				userInfo.setImageUrl(this.fileService.uploadVideoFile(profileImage));
			} catch (IOException e) {
				throw new DuckwhoException(ErrorCode.FILE_UPLOAD_ERROR);
			}
		}

		// 유저 등록
		User savedUser = this.userService.registerUser(userInfo);

		// TODO : 바로 로그인한다면 토큰을 던져주고 , 바로로그인 안한면 아무것도 던지지 않을 예정
		return CommonResponse.created(null);
	}

	
	@GetMapping("/nickname/{nickname}")
	@Operation(
		summary = "닉네임 중복여부 확인",
		description = "닉네임 중복여부 확인",
		security = { @SecurityRequirement(name = "Bearer Auth") }
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "닉네임 중복여부 확인 성공 <code>true : 중복, false : 중복X</code>"),
	})
	public CommonResponse<Boolean> checkNickname(@PathVariable("nickname") @Parameter(
		description = "닉네임",
		example = "looco"
	) String nickname) {
		return CommonResponse.ok(this.userService.isNicknameDuplication(nickname));
	}


	@DeleteMapping("/{userId}")
	@Operation(
		summary = "유저 삭제",
		description = "유저 삭제"
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "유저 삭제 성공"),
		@ApiResponse(responseCode = "404", description = "존재 하지 않거나,이미 삭제된 유저입니다."),
	})
	public CommonResponse<String> deleteUser(
		@PathVariable("userId") @Parameter(
			description = "유저 ID",
			example = "1"
		) Long userId
	) {

		// 유저 조회 
		Optional<User> user = this.userService.findByUserIdAndStatus(userId, UserStatus.ACTIVE);
		user.orElseThrow(() -> {
			log.info("여기니?");
			return new UserException.UserNotFoundException("존재 하지 않거나,이미 삭제된 유저입니다.");
		});

		// 유저 삭제
		int updateUserStatus = this.userService.updateUserStatus(user.get().getUserId(), UserStatus.INACTIVE);
		if(updateUserStatus == 0) {
			throw new UserException.UserAlreadyDeletedException("이미 삭제된 유저입니다.");
		}

		return CommonResponse.ok(null);
	}


	@GetMapping("/{userId}")
	@Operation(
			summary = "유저 정보 조회",
			description = "유저 프로필, 닉네임, 성별, 나이대 조회"
	)
	@Parameters({@Parameter(name="userId", description = "유저 개인 id")})
	public CommonResponse<UserDetailDTO>findUserDetail(@PathVariable Long userId){

		UserDetailDTO userDetail = userService.getUserDetail(userId);

		return CommonResponse.ok(userDetail);

	}

	@PatchMapping(value= "/{userId}")
	@Operation(
			summary = "유저 정보 수정",
			description = "유저 프로필, 닉네임 정보 수정"
	)
	@Parameters({@Parameter(name="userId", description = "유저 개인 id")
	})
	public CommonResponse<String>editUserDetail(@PathVariable Long userId
		 , @RequestPart(value = "image", required = false) MultipartFile multipartFile,  @RequestPart(value = "request") @Parameter(schema =@Schema(type = "string", format = "binary")) UserEditDTO request

	){

		if(request==null){
			return CommonResponse.fail(INVALID_INPUT_VALUE);
		}
		if(request.getNickname()!=null){
			String nickname = request.getNickname();
			if(userService.isNicknameDuplication(nickname)){ //이미 존재하는 닉네임일 경우
				System.out.println("이미 존재하는 닉네임 입니다. ");
				return CommonResponse.created("이미 존재하는 닉네임입니다. ");
			}else{ // 닉네임 vaildation 통과를 했을 경우
				System.out.println("이미 존재하는 닉네임이 아님으로, 업데이트를 시작합니다. ");
				userService.updateNickname(userId, nickname);

			}
		}

		if(multipartFile!=null){
			String fileUrl;
			//1번. martipart
			System.out.println("hello");
			System.out.println("multipart"+ multipartFile);
			try {
				fileUrl = fileService.uploadImageFile(multipartFile);
				UpdateProfileImgRequestDTO updateProfileImgRequestDTO = new UpdateProfileImgRequestDTO(userId, fileUrl,request.getFileSize(), request.getFileType(), request.getOriginalFileName());
				userService.updateProfileImg(updateProfileImgRequestDTO);

			}catch (Exception e){
				System.out.println(e);
				throw new FileException.FileUploadException();
			}

			return CommonResponse.ok(fileUrl);
			//2버.

		}

		return CommonResponse.ok(null);

	}



}

