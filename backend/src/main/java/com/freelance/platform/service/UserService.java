package com.freelance.platform.service;

import com.freelance.platform.common.enums.PaymentStatus;
import com.freelance.platform.common.enums.PaymentType;
import com.freelance.platform.common.enums.ProjectStatus;
import com.freelance.platform.common.enums.UserRole;
import com.freelance.platform.dto.response.DashboardVO;
import com.freelance.platform.dto.response.UserInfo;
import com.freelance.platform.entity.User;
import com.freelance.platform.exception.BusinessException;
import com.freelance.platform.repository.PaymentRepository;
import com.freelance.platform.repository.ProjectRepository;
import com.freelance.platform.repository.BidRepository;
import com.freelance.platform.repository.MessageRepository;
import com.freelance.platform.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public UserInfo getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return convertToUserInfo(user);
    }

    @Transactional
    public UserInfo updateUser(Integer id, UserInfo userInfo) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (userInfo.getNickname() != null) {
            user.setNickname(userInfo.getNickname());
        }
        if (userInfo.getAvatar() != null) {
            user.setAvatar(userInfo.getAvatar());
        }
        if (userInfo.getDescription() != null) {
            user.setDescription(userInfo.getDescription());
        }
        if (userInfo.getSkills() != null) {
            user.setSkills(userInfo.getSkills());
        }

        user = userRepository.save(user);
        return convertToUserInfo(user);
    }

    public DashboardVO getDashboard(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        DashboardVO vo = new DashboardVO();
        vo.setBalance(user.getBalance());

        if (user.getRole() == UserRole.CLIENT) {
            vo.setPublishedProjects((int) projectRepository.countByClientIdAndStatus(userId, ProjectStatus.PUBLISHED));
            vo.setInProgressProjects((int) projectRepository.countByClientIdAndStatus(userId, ProjectStatus.IN_PROGRESS));
            vo.setCompletedProjects((int) projectRepository.countByClientIdAndStatus(userId, ProjectStatus.COMPLETED));
            vo.setPendingBids(0);
            vo.setTotalSpent(paymentRepository.sumSpentByUserId(userId, PaymentType.RELEASE, PaymentStatus.COMPLETED));
            vo.setTotalEarnings(BigDecimal.ZERO);
        } else {
            vo.setPublishedProjects(0);
            vo.setInProgressProjects((int) projectRepository.countByFreelancerIdAndStatus(userId, ProjectStatus.IN_PROGRESS));
            vo.setCompletedProjects((int) projectRepository.countByFreelancerIdAndStatus(userId, ProjectStatus.COMPLETED));
            vo.setPendingBids((int) bidRepository.countPendingBidsByFreelancerId(userId));
            vo.setTotalEarnings(paymentRepository.sumEarningsByUserId(userId, PaymentType.RELEASE, PaymentStatus.COMPLETED));
            vo.setTotalSpent(BigDecimal.ZERO);
        }

        vo.setUnreadMessages((int) messageRepository.countUnreadByReceiverId(userId));

        return vo;
    }

    private UserInfo convertToUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        return userInfo;
    }
}
