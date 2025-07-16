package com.example.matching_fit.domain.score.repository;

import com.example.matching_fit.domain.score.entity.Competency;
import com.example.matching_fit.domain.score.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    //역량에 해당하는 키워드들 가져오기(역량에 어떤 키워드들이 있는지 보여주는 용도)
    List<Keyword> findByCompetency(Competency competency);
}
