package br.com.yonlero.apportionment.service.infrastructure.repository;

import br.com.yonlero.apportionment.service.infrastructure.entity.ApportionmentJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApportionmentRepository extends JpaRepository<ApportionmentJPA, UUID> {
}
