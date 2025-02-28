package com.ani.taku_backend.shorts.controller;

//@Controller
//@RequestMapping("/api/shorts")
//@RequiredArgsConstructor
//@Validated
//@Tag(name = "쇼츠 API", description = "파일 API")
//public class ShortsControllerCopy {
//
//    private final FileService fileUploadService;
//    private final ShortsService shortsService;
//    @Operation(summary = "파일 업로드", description = "파일을 스토리지에 업로드합니다.")
//    @ApiResponses(value = {
////            @ApiResponse(responseCode = "200", description = "File upload : SUCCESS"),
////            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
//    })
//    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    public String uploadFile(@AuthenticationPrincipal PrincipalUser principalUser, @Valid @ModelAttribute ShortsCreateReqDTO shortsCreateReqDTO) {
//        User user = principalUser.getUser();
//        shortsService.createShort(shortsCreateReqDTO, user);
//        return "파일이 스토리지에 업로드 되었습니다. UploadUrl: ";
//    }
//
//    @Operation(summary = "m3u8 PlayList url 반환", description = "m3u8 PlayList url 반환")
////    @ApiResponses(value = {
////            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File download : SUCCESS"),
////            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
////    })
//    @GetMapping("/{shortsId}")
//    public String findM3u8Url(@PathVariable(name = "shortsId") String shortsId, Model model) throws AmazonS3Exception {
//        ShortsResponseDTO shortsResponseDTO = shortsService.findShortsInfo(shortsId);
//        model.addAttribute("m3u8Url", shortsResponseDTO.getM3u8Url());
//        model.addAttribute("shortsId", shortsId);
//        return "video";
//    }
//
//
//    @GetMapping("/{shortsId}")
//    public ApiResponse<String> findM3u8Url(@PathVariable String shortsId) throws AmazonS3Exception {
//        String m3u8FileURL = shortsService.findM3u8FileURL(shortsId);
//
//        return ApiResponse.ok(m3u8FileURL);
//    }
//
//
//}
