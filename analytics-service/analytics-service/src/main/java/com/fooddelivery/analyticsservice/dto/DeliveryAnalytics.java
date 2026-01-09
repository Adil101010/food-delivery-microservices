package com.fooddelivery.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAnalytics {

    private Long totalDeliveries;
    private Long activePartners;
    private Long totalPartners;

    private Map<String, Long> deliveriesByStatus;

    private Double averageDeliveryTime;
    private Double averagePartnerRating;

    private Long topPartnerId;
    private Integer topPartnerDeliveries;
    private Double topPartnerRating;
}
