package com.woorifisa.backend.main.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.woorifisa.backend.common.dto.ProductDTO;
import com.woorifisa.backend.common.dto.ReviewDTO;
import com.woorifisa.backend.common.security.springsecurity.MemberDetail;
import com.woorifisa.backend.main.dto.ReviewPrintDTO;
import com.woorifisa.backend.main.exception.NoProductException;
import com.woorifisa.backend.main.service.MainService;
import com.woorifisa.backend.member.dto.LoginSessionDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/main")
@Tag(name = "main controller")
public class MainController {

    @Autowired
    private MainService mainService;

    // main 상품 미리보기
    @GetMapping("/productPreview")
    @Operation(summary = "main view 상품 미리보기 (개발 완료)",
               description = "product 최신 8개만 select")
    public List<ProductDTO> productPreview() {
        return mainService.productPreview();
    }

    // 상품 리스트 보기(카테고리별, 없을 시 전체)
    @GetMapping("/productAll")
    @Operation(summary = "상품 list view에서 전체 상품/ 카테고리별 상품 (개발 완료)",
               description = "category 컬럼으로 product list select (단, category null 일 경우 전체 출력)")
    public List<ProductDTO> productAllByCategory(
            @RequestParam(value = "category", defaultValue = "-99999") int category) {
        return mainService.productAllByCategory(category);
    }

    // 상품 상세보기
    @GetMapping("/productDetail/{prodNum}")
    @Operation(summary = "상품 상세 정보 가져오기 (개발완료)",
               description = "prod_num으로 product 정보 select / log기록")
    public ProductDTO productDetail(@PathVariable("prodNum") String prodNum, HttpServletRequest request)
            throws NoProductException {
        // 세션에서 로그인 정보 가져오기
        HttpSession session = request.getSession();
        LoginSessionDTO login = (LoginSessionDTO) session.getAttribute("login");

        // 조회수 log 남기기
        if (login != null) { // 로그인된 경우
            log.info(String.format("View - %s - %s", prodNum, login.getMemNum()));
        } else { // 로그인하지 않은 경우
            log.info(String.format("View - %s - mUNDEFINED", prodNum));
        }

        return mainService.productDetail(prodNum);
    }

    // 상품 리뷰list 출력
    @GetMapping("/reviewAllByProduct/{prodNum}")
    @Operation(summary = "상품에 대한 review 가져오기 (개발완료 - builder방식으로 변경할지 고민/토론)",
                description = "prod_num으로 review 정보 select")
    public List<ReviewPrintDTO> reviewAllByProdNum(@PathVariable("prodNum") String prodNum) {
        return mainService.reviewAllByProdNum(prodNum);
    }

    // 리뷰 등록
    @PostMapping("/reviewInsert")
    @Operation(summary = "상품에 대한 review insert (개발 완료 - 예외처리 고민중, builder방식으로 변경할지 고민/토론)",
                description = "prod_num과 session에 저장된 mem_num으로 review insert")
    public ReviewPrintDTO reviewInsert(@RequestBody ReviewDTO dto,  @AuthenticationPrincipal MemberDetail memberDetail) { 
        dto.setMemNum(memberDetail.getMemNum());
        return mainService.reviewInsert(dto);
    }
 
    // 리뷰 삭제
    @PostMapping("/reviewDelete")
    @Operation(summary = "리뷰 삭제 (개발 완료 )", description = "rev_num과 mem_num으로 리뷰 삭제")
    public String reviewDelete(@RequestBody Map<String, Object> reqMap, @AuthenticationPrincipal MemberDetail memberDetail) {
        reqMap.put("memNum", memberDetail.getMemNum());
        reqMap.put("memType",memberDetail.getMemType());
        return mainService.reviewDelete(reqMap);
    }

    @ExceptionHandler
    public String noProduct(NoProductException e){
        e.printStackTrace();
        return e.getMessage();
    }
}
