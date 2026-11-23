package io.miguelsouza.compliance.infrastructure.persistence.repository;

import io.miguelsouza.compliance.domain.Company;
import io.miguelsouza.compliance.domain.CompanyRepository;
import io.miguelsouza.compliance.infrastructure.persistence.entity.CompanyEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryCompanyRepository implements CompanyRepository {
    private final CompanyEntityRepository repository;

    public InMemoryCompanyRepository(CompanyEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Company company) {
        var entity = CompanyEntity.from(company);
        repository.save(entity);
    }
}
