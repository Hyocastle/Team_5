	package com.example.workhive.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.workhive.domain.dto.ChatRoomDTO;
import com.example.workhive.domain.entity.ChatRoomEntity;
import com.example.workhive.domain.entity.CompanyEntity;
import com.example.workhive.domain.entity.MemberEntity;
import com.example.workhive.domain.entity.ProjectMemberEntity;
import com.example.workhive.domain.entity.ChatRoomKindEntity;
import com.example.workhive.repository.ChatRoomKindRepository;
import com.example.workhive.repository.ChatRoomRepository;
import com.example.workhive.repository.MemberRepository;
import com.example.workhive.repository.ProjectMemberRepository;
import com.example.workhive.repository.CompanyRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // 채팅방 목록 불러오기 (채팅방 이름과 ID 함께 반환)
    public List<ChatRoomDTO> getChatRoomsByUser(String memberId) {
        List<ProjectMemberEntity> projectMembers = projectMemberRepository.findByMember_MemberId(memberId);
        List<ChatRoomDTO> chatRoomDTOs = new ArrayList<>();

        for (ProjectMemberEntity projectMember : projectMembers) {
            ChatRoomEntity chatRoom = projectMember.getChatRoom();
            if (chatRoom != null) {
                ChatRoomDTO chatRoomDTO = ChatRoomDTO.builder()
                    .chatRoomId(chatRoom.getChatRoomId())
                    .chatRoomName(chatRoom.getChatRoomName())
                    .build();
                chatRoomDTOs.add(chatRoomDTO);
            }
        }

        return chatRoomDTOs;
    }


 // 채팅방 생성하기
    public void createChatRoom(ChatRoomDTO chatRoomDTO) {
        // DTO에서 필요한 엔티티를 조회
        // MemberEntity는 항상 유효한 memberId로 존재한다고 가정 (Optional 사용)
        MemberEntity member = memberRepository.findByMemberId(chatRoomDTO.getCreatedByMemberId());

        // CompanyEntity도 항상 유효한 companyId로 존재한다고 가정 (Optional에서 값을 꺼냄)
        CompanyEntity company = companyRepository.findByCompanyId(chatRoomDTO.getCompanyId());

        ChatRoomKindEntity chatRoomKind = new ChatRoomKindEntity();
        chatRoomKind.setChatroomKindId(4L);  // 이미 생성된 '프로젝트' 채팅방 종류 사용
        chatRoomKind.setKind("프로젝트");

        // ChatRoomEntity 생성
        ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                .chatRoomName(chatRoomDTO.getChatRoomName())
                .createdByMember(member) // 생성자 MemberEntity 설정
                .company(company) // 회사 CompanyEntity 설정
                .chatRoomKind(chatRoomKind) // 채팅방 종류 ChatRoomKindEntity 설정
                .build();

        // 채팅방 저장
        ChatRoomEntity savedChatRoom = chatRoomRepository.save(chatRoom); // 저장된 ChatRoomEntity 반환받음

        // 생성된 채팅방의 chatRoomId와 생성자의 memberId로 project_member 테이블에 추가
        ProjectMemberEntity projectMember = new ProjectMemberEntity();
        projectMember.setChatRoom(savedChatRoom);  // 저장된 채팅방의 chatRoomId 사용
        projectMember.setMember(member);  // 생성자의 memberId 사용
        projectMember.setRole("방장");  // 생성자는 방장으로 설정

        // project_member 테이블에 데이터 저장
        projectMemberRepository.save(projectMember);
    }
    public List<String> getChatRoomParticipants(Long chatRoomId) {
        // chatRoomId에 맞는 projectMember 목록을 가져옴
        List<ProjectMemberEntity> projectMembers = projectMemberRepository.findByChatRoom_ChatRoomId(chatRoomId);
        
        List<String> participantNames = new ArrayList<>();

        for (ProjectMemberEntity memberEntity : projectMembers) {
            // 각 멤버의 이름을 리스트에 추가
            participantNames.add(memberEntity.getMember().getMemberName());
        }

        return participantNames;
    }
    public boolean inviteUserToChatRoom(Long chatRoomId, String memberId) {
        try {
            // 멤버와 채팅방을 찾아서 ProjectMemberEntity에 저장
            MemberEntity member = memberRepository.findByMemberId(memberId);
            ChatRoomEntity chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 없습니다."));

            ProjectMemberEntity projectMember = ProjectMemberEntity.builder()
                .chatRoom(chatRoom)
                .member(member)
                .role("일반") // 기본적으로 초대된 멤버의 역할 설정
                .build();

            projectMemberRepository.save(projectMember);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
