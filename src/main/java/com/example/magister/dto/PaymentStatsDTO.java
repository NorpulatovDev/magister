package com.example.magister.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentStatsDTO {
      private Integer totalPayments;
      private BigDecimal totalAmount;
}