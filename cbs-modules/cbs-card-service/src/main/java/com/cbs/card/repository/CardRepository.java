package com.cbs.card.repository;

import com.cbs.card.model.Card;
import com.cbs.card.model.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    boolean existsByCardNumber(String cardNumber);

    boolean existsByToken(String token);

    List<Card> findByCustomerIdOrderByIdDesc(Long customerId);

    List<Card> findByAccountIdOrderByIdDesc(Long accountId);

    List<Card> findByStatusOrderByIdDesc(CardStatus status);

    List<Card> findByCustomerIdAndStatusOrderByIdDesc(Long customerId, CardStatus status);
}
