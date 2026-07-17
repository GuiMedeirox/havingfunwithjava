package com.havingfunwithjava.payment.infrastructure.persistence;

import com.havingfunwithjava.payment.domain.Money;
import com.havingfunwithjava.payment.domain.Payment;
import com.havingfunwithjava.payment.domain.PaymentId;
import com.havingfunwithjava.payment.domain.PaymentMethod;
import com.havingfunwithjava.payment.domain.PaymentStatus;

/**
 * Mapper (adaptador): traduz entre {@link Payment} (domínio) e {@link PaymentEntity} (JPA).
 */
final class PaymentMapper {

    private PaymentMapper() {
    }

    static PaymentEntity toEntity(Payment payment) {
        return new PaymentEntity(
                payment.id().value(),
                payment.orderId(),
                payment.method().name(),
                payment.amount().amount(),
                payment.amount().currency(),
                payment.status().name(),
                payment.attempts(),
                payment.createdAt(),
                payment.updatedAt()
        );
    }

    static Payment toDomain(PaymentEntity entity) {
        return new Payment(
                new PaymentId(entity.getId()),
                entity.getOrderId(),
                PaymentMethod.valueOf(entity.getMethod()),
                new Money(entity.getAmount(), entity.getCurrency()),
                PaymentStatus.valueOf(entity.getStatus()),
                entity.getAttempts(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
