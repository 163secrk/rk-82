package com.freelance.platform.repository;

import com.freelance.platform.common.enums.PaymentStatus;
import com.freelance.platform.common.enums.PaymentType;
import com.freelance.platform.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByPayerId(Integer payerId);

    List<Payment> findByPayeeId(Integer payeeId);

    List<Payment> findByProjectId(Integer projectId);

    @Query("SELECT COALESCE(SUM(p.amount), 0.00) FROM Payment p WHERE p.payeeId = :userId " +
           "AND p.type = :type AND p.status = :status")
    BigDecimal sumEarningsByUserId(@Param("userId") Integer userId,
                                    @Param("type") PaymentType type,
                                    @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0.00) FROM Payment p WHERE p.payerId = :userId " +
           "AND p.type = :type AND p.status = :status")
    BigDecimal sumSpentByUserId(@Param("userId") Integer userId,
                                 @Param("type") PaymentType type,
                                 @Param("status") PaymentStatus status);

    boolean existsByProjectIdAndTypeAndStatus(Integer projectId, PaymentType type, PaymentStatus status);
}
