package br.com.yonlero.apportionment.service.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "value_distributions")
public class ValueDistributionJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "apportionment_id", nullable = false)
    private ApportionmentJPA apportionment;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false, precision = 3, scale = 16)
    private BigDecimal percentage;

    @Column(nullable = false)
    private UUID destinationId;
}