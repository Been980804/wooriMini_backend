package com.woorifisa.backend.member.service;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.woorifisa.backend.common.entity.Member;
import com.woorifisa.backend.common.entity.Subscription;
import com.woorifisa.backend.common.exception.NoDataExsistException;
import com.woorifisa.backend.common.exception.SessionNotValidException;
import com.woorifisa.backend.common.repository.MemberRepository;
import com.woorifisa.backend.common.repository.ProductRepository;
import com.woorifisa.backend.common.repository.SubscriptionRepository;
import com.woorifisa.backend.member.dto.MemberInfoDTO;
import com.woorifisa.backend.member.dto.MemberInfoEditDTO;
import com.woorifisa.backend.member.dto.SubscriptionResponseDTO;
import com.woorifisa.backend.member.exception.NotValidPasswordException;



@Service
public class MemberInfoServiceImpl implements MemberInfoService{

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public MemberInfoDTO getMemberInfo(String memId) {
            
            Member member= memberRepository.findByMemId(memId);
            return MemberInfoDTO.builder()
                                .memAddr(member.getMemAddr())
                                .memBirth(member.getMemBirth())
                                .memEmail(member.getMemEmail())
                                .memId(member.getMemId())
                                .memName(member.getMemName())
                                .memPhone(member.getMemPhone())
                                .memSex(member.getMemSex())
                                .memType(member.getMemType())
                                .build();
    }
    
    @Transactional
    @Override
    public String updateMemberInfo(MemberInfoEditDTO memberInfoEditDTO, String memNum) throws NotValidPasswordException, SessionNotValidException {
        Member curMember = memberRepository.findByMemNum(memNum);
        if (!curMember.checkPassword(memberInfoEditDTO.getMemPw(), passwordEncoder)){
            throw new NotValidPasswordException("비밀번호가 올바르지 않습니다."); 
        }
        if (curMember.getMemId() != memberInfoEditDTO.getMemId()){
            throw new SessionNotValidException("로그인 정보와 수정하고자 하는 user의 id가 일치하지 않습니다!");
        }

        try {
            memberRepository.updateMemberInfo(memNum,memberInfoEditDTO.getMemId(),memberInfoEditDTO.getMemPw(),memberInfoEditDTO.getMemName(), 
                                              memberInfoEditDTO.getMemEmail(),memberInfoEditDTO.getMemPhone(),memberInfoEditDTO.getMemSex(),
                                              memberInfoEditDTO.getMemBirth(),memberInfoEditDTO.getMemAddr(),memberInfoEditDTO.getMemType());
            return "수정 성공";
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<SubscriptionResponseDTO> getSubList(String memNum) throws Exception {
        Member member = memberRepository.findById(memNum).orElse(null);
        if (member == null){
            throw new NoDataExsistException("로그인 세션에 해당하는 user가 존재하지 않습니다.");
        }
        List<Subscription> subList = subscriptionRepository.findByMemNum(member);
        
        if (subList == null){
            throw new NoDataExsistException("구독 정보가 존재하지 않습니다.");
        }
        return subList.stream()
            .map(sub -> new SubscriptionResponseDTO(sub.getSubNum(), sub.getSubPer(), sub.getSubStart(), sub.getSubDeli(), sub.getSubStat(), sub.getSubUpd(), sub.getSubCnt(), member.getMemNum(), sub.getProdNum().getProdNum(), sub.getPayNum().getPayNum(), sub.getProdNum().getProdImg(), sub.getProdNum().getProdName()))
            .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public String deleteSub(String subNum) throws Exception{
        try {
            System.out.println("전달 받은 번호" + subNum);
            subscriptionRepository.deleteSubById(subNum);
            return "삭제 완료";
        } catch (Exception e) {
            throw new Exception("구독을 삭제하는 중 오류가 발생했습니다.");
        }
    }
    
    
             
    
}
