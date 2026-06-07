package com.freelance.platform.service;

import com.freelance.platform.common.enums.PaymentStatus;
import com.freelance.platform.common.enums.PaymentType;
import com.freelance.platform.common.enums.ProjectStatus;
import com.freelance.platform.dto.request.EscrowRequest;
import com.freelance.platform.dto.request.ReleaseRequest;
import com.freelance.platform.dto.response.PaymentVO;
import com.freelance.platform.entity.Payment;
import com.freelance.platform.entity.Project;
import com.freelance.platform.entity.User;
import com.freelance.platform.exception.BusinessException;
import com.freelance.platform.repository.PaymentRepository;
import com.freelance.platform.repository.ProjectRepository;
import com.freelance.platform.repository.ReviewRepository;
import com.freelance.platform.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public PaymentVO escrow(EscrowRequest request, Integer clientId) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        if (!project.getClientId().equals(clientId)) {
            throw new BusinessException(403, "只有项目发包方可以托管资金");
        }

        if (project.getFreelancerId() == null) {
            throw new BusinessException("请先选择接包方");
        }

        if (project.getStatus() != ProjectStatus.BIDDING && project.getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new BusinessException("项目状态不允许托管资金");
        }

        if (project.getStatus() == ProjectStatus.DISPUTED) {
            throw new BusinessException("项目存在争议，暂无法进行资金操作");
        }

        if (paymentRepository.existsByProjectIdAndTypeAndStatus(
                request.getProjectId(), PaymentType.ESCROW, PaymentStatus.COMPLETED)) {
            throw new BusinessException("该项目已托管资金");
        }

        User client = userRepository.findById(clientId).orElseThrow();
        if (client.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("余额不足");
        }

        client.setBalance(client.getBalance().subtract(request.getAmount()));
        userRepository.save(client);

        Payment payment = new Payment();
        payment.setProjectId(request.getProjectId());
        payment.setPayerId(clientId);
        payment.setPayeeId(project.getFreelancerId());
        payment.setAmount(request.getAmount());
        payment.setType(PaymentType.ESCROW);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setRemark("项目资金托管");

        payment = paymentRepository.save(payment);

        project.setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(project);

        return convertToVO(payment);
    }

    @Transactional
    public PaymentVO release(ReleaseRequest request, Integer clientId) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        if (!project.getClientId().equals(clientId)) {
            throw new BusinessException(403, "只有项目发包方可以结算资金");
        }

        if (project.getStatus() != ProjectStatus.DELIVERED && project.getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new BusinessException("项目状态不允许结算资金");
        }

        if (project.getStatus() == ProjectStatus.DISPUTED) {
            throw new BusinessException("项目存在争议，暂无法进行资金操作");
        }

        if (!paymentRepository.existsByProjectIdAndTypeAndStatus(
                request.getProjectId(), PaymentType.ESCROW, PaymentStatus.COMPLETED)) {
            throw new BusinessException("该项目尚未托管资金");
        }

        User freelancer = userRepository.findById(project.getFreelancerId()).orElseThrow();
        freelancer.setBalance(freelancer.getBalance().add(request.getAmount()));
        userRepository.save(freelancer);

        Payment payment = new Payment();
        payment.setProjectId(request.getProjectId());
        payment.setPayerId(clientId);
        payment.setPayeeId(project.getFreelancerId());
        payment.setAmount(request.getAmount());
        payment.setType(PaymentType.RELEASE);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setRemark("项目资金结算");

        payment = paymentRepository.save(payment);

        project.setStatus(ProjectStatus.COMPLETED);
        projectRepository.save(project);

        updateUserRating(project.getFreelancerId());

        return convertToVO(payment);
    }

    @Transactional
    public PaymentVO deposit(BigDecimal amount, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        Payment payment = new Payment();
        payment.setPayerId(userId);
        payment.setPayeeId(userId);
        payment.setAmount(amount);
        payment.setType(PaymentType.DEPOSIT);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setRemark("账户充值");

        payment = paymentRepository.save(payment);
        return convertToVO(payment);
    }

    @Transactional
    public PaymentVO withdraw(BigDecimal amount, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (user.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }

        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);

        Payment payment = new Payment();
        payment.setPayerId(userId);
        payment.setPayeeId(userId);
        payment.setAmount(amount);
        payment.setType(PaymentType.WITHDRAW);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setRemark("账户提现");

        payment = paymentRepository.save(payment);
        return convertToVO(payment);
    }

    public List<PaymentVO> getPaymentHistory(Integer userId) {
        List<Payment> payments = paymentRepository.findByPayerId(userId);
        payments.addAll(paymentRepository.findByPayeeId(userId));
        return payments.stream()
                .distinct()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private void updateUserRating(Integer userId) {
        Double avgRating = reviewRepository.calculateAverageRating(userId);
        if (avgRating != null) {
            User user = userRepository.findById(userId).orElseThrow();
            user.setRating(BigDecimal.valueOf(avgRating));
            userRepository.save(user);
        }
    }

    private PaymentVO convertToVO(Payment payment) {
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(payment, vo);

        User payer = userRepository.findById(payment.getPayerId()).orElse(null);
        if (payer != null) {
            vo.setPayerName(payer.getNickname());
        }

        User payee = userRepository.findById(payment.getPayeeId()).orElse(null);
        if (payee != null) {
            vo.setPayeeName(payee.getNickname());
        }

        if (payment.getProjectId() != null) {
            Project project = projectRepository.findById(payment.getProjectId()).orElse(null);
            if (project != null) {
                vo.setProjectTitle(project.getTitle());
            }
        }

        return vo;
    }
}
