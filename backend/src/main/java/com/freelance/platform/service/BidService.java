package com.freelance.platform.service;

import com.freelance.platform.common.enums.BidStatus;
import com.freelance.platform.common.enums.ProjectStatus;
import com.freelance.platform.common.enums.UserRole;
import com.freelance.platform.dto.request.CreateBidRequest;
import com.freelance.platform.dto.response.BidVO;
import com.freelance.platform.entity.Bid;
import com.freelance.platform.entity.Project;
import com.freelance.platform.entity.User;
import com.freelance.platform.exception.BusinessException;
import com.freelance.platform.repository.BidRepository;
import com.freelance.platform.repository.ProjectRepository;
import com.freelance.platform.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BidService {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public BidVO createBid(Integer projectId, CreateBidRequest request, Integer freelancerId) {
        User freelancer = userRepository.findById(freelancerId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (freelancer.getRole() != UserRole.FREELANCER) {
            throw new BusinessException(403, "只有接包方可以提交竞标");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        if (project.getStatus() != ProjectStatus.PUBLISHED) {
            throw new BusinessException("该项目不接受竞标");
        }

        if (bidRepository.existsByProjectIdAndFreelancerId(projectId, freelancerId)) {
            throw new BusinessException("您已对该项目提交过竞标");
        }

        Bid bid = new Bid();
        BeanUtils.copyProperties(request, bid);
        bid.setProjectId(projectId);
        bid.setFreelancerId(freelancerId);
        bid.setStatus(BidStatus.PENDING);

        bid = bidRepository.save(bid);
        return convertToVO(bid);
    }

    public List<BidVO> getBidsByProject(Integer projectId) {
        List<Bid> bids = bidRepository.findByProjectId(projectId);
        return bids.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public List<BidVO> getBidsByFreelancer(Integer freelancerId) {
        List<Bid> bids = bidRepository.findByProjectIdAndStatus(freelancerId, BidStatus.ACCEPTED);
        return bids.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BidVO acceptBid(Integer bidId, Integer clientId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new BusinessException(404, "竞标不存在"));

        Project project = projectRepository.findById(bid.getProjectId())
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        if (!project.getClientId().equals(clientId)) {
            throw new BusinessException(403, "只有项目发包方可以选择接包方");
        }

        bid.setStatus(BidStatus.ACCEPTED);
        bidRepository.save(bid);

        bidRepository.findByProjectId(bid.getProjectId()).stream()
                .filter(b -> !b.getId().equals(bidId))
                .forEach(b -> {
                    b.setStatus(BidStatus.REJECTED);
                    bidRepository.save(b);
                });

        project.setFreelancerId(bid.getFreelancerId());
        project.setAgreedPrice(bid.getPrice());
        project.setStatus(ProjectStatus.BIDDING);
        projectRepository.save(project);

        return convertToVO(bid);
    }

    private BidVO convertToVO(Bid bid) {
        BidVO vo = new BidVO();
        BeanUtils.copyProperties(bid, vo);

        User freelancer = userRepository.findById(bid.getFreelancerId()).orElse(null);
        if (freelancer != null) {
            vo.setFreelancerName(freelancer.getNickname());
            vo.setFreelancerAvatar(freelancer.getAvatar());
        }

        return vo;
    }
}
