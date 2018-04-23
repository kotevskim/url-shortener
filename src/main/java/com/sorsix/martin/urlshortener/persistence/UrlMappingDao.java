package com.sorsix.martin.urlshortener.persistence;

import com.sorsix.martin.urlshortener.domain.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlMappingDao extends JpaRepository<UrlMapping, String> {
}
