package com.freelance.platform.service;

import com.freelance.platform.common.enums.DisputeStatus;
import com.freelance.platform.common.enums.PaymentStatus;
import com.freelance.platform.common.enums.PaymentType;
import com.freelance.platform.common.enums.ProjectStatus;
import com.freelance.platform.common.enums.UserRole;
import com.freelance.platform.dto.request.CreateDisputeRequest;
import com.freelance.platform.dto.request.DisputeResolutionRequest;
import com.freelance.platform.dto.response.DisputeVO;
import com.freelance.platform.entity.Dispute;
import com.freelance.platform.entity.Payment;
import com.freelance.platform.entity.Project;
import com.freelance.platform.entity.User;
import com.freelance.platform.exception.BusinessException;
import com.freelance.platform.repository.DisputeRepository;
import com.freelance.platform.repository.PaymentRepository;
import com.freelance.platform.repository.ProjectRepository;
import com.freelance.platform.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DisputeService {

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public DisputeVO createDispute(CreateDisputeRequest request, Integer userId) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        if (project.getStatus() != ProjectStatus.IN_PROGRESS && project.getStatus() != ProjectStatus.DELIVERED) {
            throw new BusinessException("项目状态不允许发起争议");
        }

        if (disputeRepository.existsByProjectIdAndStatus(request.getProjectId(), DisputeStatus.PENDING)) {
            throw new BusinessException("该项目已有未处理的争议");
        }

        if (!project.getClientId().equals(userId) && !project.getFreelancerId().equals(userId)) {
            throw new BusinessException(403, "只有项目参与方可以发起争议");
        }

        BigDecimal escrowedAmount = paymentRepository.sumEarningsByUserId(
                project.getFreelancerId(), PaymentType.ESCROW, PaymentStatus.COMPLETED);
        if (escrowedAmount == null || escrowedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("该项目暂无托管资金，无法发起争议");
        }

        if (request.getClaimedAmount().compareTo(escrowedAmount) > 0) {
            throw new BusinessException("诉求金额不能超过托管资金总额");
        }

        Integer respondentId = project.getClientId().equals(userId)
                ? project.getFreelancerId()
                : project.getClientId();

        Dispute dispute = new Dispute();
        dispute.setProjectId(request.getProjectId());
        dispute.setInitiatorId(userId);
        dispute.setRespondentId(respondentId);
        dispute.setReason(request.getReason());
        dispute.setClaimedAmount(request.getClaimedAmount());
        dispute.setStatus(DisputeStatus.PENDING);

        dispute = disputeRepository.save(dispute);

        project.setStatus(ProjectStatus.DISPUTED);
        projectRepository.save(project);

        return convertToVO(dispute);
    }

    public List<DisputeVO> getUserDisputes(Integer userId) {
        List<Dispute> disputes = disputeRepository.findByUserId(userId);
        return disputes.stream()
                .sorted((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()))
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public DisputeVO getDisputeDetail(Integer id, Integer userId) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "争议不存在"));

        User user = userRepository.findById(userId).orElseThrow();
        if (user.getRole() != UserRole.ADMIN
                && !dispute.getInitiatorId().equals(userId)
                && !dispute.getRespondentId().equals(userId)) {
            throw new BusinessException(403, "无权查看该争议");
        }

        return convertToVO(dispute);
    }

    public Page<DisputeVO> getAllDisputes(DisputeStatus status, Pageable pageable, Integer adminId) {
        User admin = userRepository.findById(adminId).orElseThrow();
        if (admin.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "只有管理员可以查看所有争议");
        }

        Page<Dispute> disputes = disputeRepository.findByFilters(status, pageable);
        return disputes.map(this::convertToVO);
    }

    @Transactional
    public DisputeVO resolveDisputeRefund(Integer id, DisputeResolutionRequest request, Integer adminId) {
        User admin = userRepository.findById(adminId).orElseThrow();
        if (admin.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "只有管理员可以裁决争议");
        }

        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "争议不存在"));

        if (dispute.getStatus() != DisputeStatus.PENDING) {
            throw new BusinessException("该争议已处理，无法重复裁决");
        }

        Project project = projectRepository.findById(dispute.getProjectId()).orElseThrow();

        List<Payment> escrowPayments = paymentRepository.findByProjectId(project.getId()).stream()
                .filter(p -> p.getType() == PaymentType.ESCROW && p.getStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.toList());

        if (escrowPayments.isEmpty()) {
            throw new BusinessException("未找到托管资金记录");
        }

        BigDecimal refundAmount = request.getResolvedAmount();
        BigDecimal totalEscrow = escrowPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (refundAmount.compareTo(totalEscrow) > 0) {
            throw new BusinessException("退款金额不能超过托管资金总额");
        }

        User client = userRepository.findById(project.getClientId()).orElseThrow();
        client.setBalance(client.getBalance().add(refundAmount));
        userRepository.save(client);

        Payment refundPayment = new Payment();
        refundPayment.setProjectId(project.getId());
        refundPayment.setPayerId(project.getFreelancerId());
        refundPayment.setPayeeId(project.getClientId());
        refundPayment.setAmount(refundAmount);
        refundPayment.setType(PaymentType.REFUND);
        refundPayment.setStatus(PaymentStatus.COMPLETED);
        refundPayment.setTransactionId(UUID.randomUUID().toString());
        refundPayment.setRemark("争议裁决退款：" + request.getResolution());
        paymentRepository.save(refundPayment);

        BigDecimal remainingAmount = totalEscrow.subtract(refundAmount);
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            User freelancer = userRepository.findById(project.getFreelancerId()).orElseThrow();
            freelancer.setBalance(freelancer.getBalance().add(remainingAmount));
            userRepository.save(freelancer);

            Payment releasePayment = new Payment();
            releasePayment.setProjectId(project.getId());
            releasePayment.setPayerId(project.getClientId());
            releasePayment.setPayeeId(project.getFreelancerId());
            releasePayment.setAmount(remainingAmount);
            releasePayment.setType(PaymentType.RELEASE);
            releasePayment.setStatus(PaymentStatus.COMPLETED);
            releasePayment.setTransactionId(UUID.randomUUID().toString());
            releasePayment.setRemark("争议裁决部分放款");
            paymentRepository.save(releasePayment);
        }

        dispute.setStatus(DisputeStatus.RESOLVED_REFUND);
        dispute.setResolution(request.getResolution());
        dispute.setResolvedAmount(refundAmount);
        dispute.setResolvedAt(LocalDateTime.now());
        dispute.setAdminId(adminId);
        disputeRepository.save(dispute);

        project.setStatus(ProjectStatus.CANCELLED);
        projectRepository.save(project);

        return convertToVO(dispute);
    }

    @Transactional
    public DisputeVO resolveDisputeRelease(Integer id, DisputeResolutionRequest request, Integer adminId) {
        User admin = userRepository.findById(adminId).orElseThrow();
        if (admin.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "只有管理员可以裁决争议");
        }

        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "争议不存在"));

        if (dispute.getStatus() != DisputeStatus.PENDING) {
            throw new BusinessException("该争议已处理，无法重复裁决");
        }

        Project project = projectRepository.findById(dispute.getProjectId()).orElseThrow();

        List<Payment> escrowPayments = paymentRepository.findByProjectId(project.getId()).stream()
                .filter(p -> p.getType() == PaymentType.ESCROW && p.getStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.toList());

        if (escrowPayments.isEmpty()) {
            throw new BusinessException("未找到托管资金记录");
        }

        BigDecimal releaseAmount = request.getResolvedAmount();
        BigDecimal totalEscrow = escrowPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (releaseAmount.compareTo(totalEscrow) > 0) {
            throw new BusinessException("放款金额不能超过托管资金总额");
        }

        User freelancer = userRepository.findById(project.getFreelancerId()).orElseThrow();
        freelancer.setBalance(freelancer.getBalance().add(releaseAmount));
        userRepository.save(freelancer);

        Payment releasePayment = new Payment();
        releasePayment.setProjectId(project.getId());
        releasePayment.setPayerId(project.getClientId());
        releasePayment.setPayeeId(project.getFreelancerId());
        releasePayment.setAmount(releaseAmount);
        releasePayment.setType(PaymentType.RELEASE);
        releasePayment.setStatus(PaymentStatus.COMPLETED);
        releasePayment.setTransactionId(UUID.randomUUID().toString());
        releasePayment.setRemark("争议裁决放款：" + request.getResolution());
        paymentRepository.save(releasePayment);

        BigDecimal remainingAmount = totalEscrow.subtract(releaseAmount);
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            User client = userRepository.findById(project.getClientId()).orElseThrow();
            client.setBalance(client.getBalance().add(remainingAmount));
            userRepository.save(client);

            Payment refundPayment = new Payment();
            refundPayment.setProjectId(project.getId());
            refundPayment.setPayerId(project.getFreelancerId());
            refundPayment.setPayeeId(project.getClientId());
            refundPayment.setAmount(remainingAmount);
            refundPayment.setType(PaymentType.REFUND);
            refundPayment.setStatus(PaymentStatus.COMPLETED);
            refundPayment.setTransactionId(UUID.randomUUID().toString());
            refundPayment.setRemark("争议裁决部分退款");
            paymentRepository.save(refundPayment);
        }

        dispute.setStatus(DisputeStatus.RESOLVED_RELEASE);
        dispute.setResolution(request.getResolution());
        dispute.setResolvedAmount(releaseAmount);
        dispute.setResolvedAt(LocalDateTime.now());
        dispute.setAdminId(adminId);
        disputeRepository.save(dispute);

        project.setStatus(ProjectStatus.COMPLETED);
        projectRepository.save(project);

        return convertToVO(dispute);
    }

    @Transactional
    public DisputeVO cancelDispute(Integer id, Integer userId) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "争议不存在"));

        if (!dispute.getInitiatorId().equals(userId)) {
            throw new BusinessException(403, "只有争议发起方可以取消争议");
        }

        if (dispute.getStatus() != DisputeStatus.PENDING) {
            throw new BusinessException("该争议已处理，无法取消");
        }

        dispute.setStatus(DisputeStatus.CANCELLED);
        dispute.setResolvedAt(LocalDateTime.now());
        disputeRepository.save(dispute);

        Project project = projectRepository.findById(dispute.getProjectId()).orElseThrow();
        project.setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(project);

        return convertToVO(dispute);
    }

    private DisputeVO convertToVO(Dispute dispute) {
        DisputeVO vo = new DisputeVO();
        BeanUtils.copyProperties(dispute, vo);

        Project project = projectRepository.findById(dispute.getProjectId()).orElse(null);
        if (project != null) {
            vo.setProjectTitle(project.getTitle());
        }

        User initiator = userRepository.findById(dispute.getInitiatorId()).orElse(null);
        if (initiator != null) {
            vo.setInitiatorName(initiator.getNickname());
        }

        User respondent = userRepository.findById(dispute.getRespondentId()).orElse(null);
        if (respondent != null) {
            vo.setRespondentName(respondent.getNickname());
        }

        if (dispute.getAdminId() != null) {
            User admin = userRepository.findById(dispute.getAdminId()).orElse(null);
            if (admin != null) {
                vo.setAdminName(admin.getNickname());
            }
        }

        return vo;
    }
}
