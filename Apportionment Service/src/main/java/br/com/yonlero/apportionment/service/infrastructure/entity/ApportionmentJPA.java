package br.com.yonlero.apportionment.service.infrastructure.entity;

import br.com.yonlero.apportionment.service.domain.model.Apportionment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "apportionments")
public class ApportionmentJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String account;
    @Column(nullable = false)
    private String costCenter;
    @Column(nullable = false)
    private String businessUnit;
    @Column(nullable = false)
    private int year;
    @Column
    private UUID originId;
    @Column(nullable = false)
    private boolean isOrigin;

    @OneToMany(mappedBy = "apportionment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ValueDistributionJPA> valueDistributions;

    public ApportionmentJPA(Apportionment apportionment) {
        this.id = apportionment.getId();
        this.account = apportionment.getAccount();
        this.costCenter = apportionment.getCostCenter();
        this.businessUnit = apportionment.getBusinessUnit();
        this.year = apportionment.getYear();
        this.originId = apportionment.getOriginId();
        this.isOrigin = apportionment.isOrigin();

        this.valueDistributions = new ArrayList<>();
        if (apportionment.getValuesByMonth() != null) {
            apportionment.getValuesByMonth().forEach((month, distributions) -> {
                distributions.forEach(distribution -> {
                    ValueDistributionJPA distributionJPA = new ValueDistributionJPA();
                    distributionJPA.setMonth(month.getValue());
                    distributionJPA.setPercentage(distribution.getPercentage());
                    distributionJPA.setDestinationId(distribution.getDestinationId());
                    distributionJPA.setApportionment(this);
                    this.valueDistributions.add(distributionJPA);
                });
            });
        }
    }

    public Apportionment toDomain() {
        Apportionment apportionment = new Apportionment(
                this.getId(),
                this.getAccount(),
                this.getCostCenter(),
                this.getBusinessUnit(),
                this.getOriginId(),
                this.getYear(),
                this.isOrigin());

        if (this.getValueDistributions() != null) {
            this.getValueDistributions().forEach(distributionJPA -> {
                Month month = Month.of(distributionJPA.getMonth());
                apportionment.addNewValueDistribution(month, distributionJPA.getPercentage(), distributionJPA.getDestinationId());
            });
        }

        return apportionment;
    }

}
