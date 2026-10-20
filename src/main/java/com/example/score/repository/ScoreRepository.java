package com.example.score.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.score.entity.Score;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    List<Score> findByCno(String cno);

    List<Score> findBySno(String sno);

    List<Score> findByCnoIn(Collection<String> cnos);

    Optional<Score> findBySnoAndCno(String sno, String cno);

    void deleteByCnoAndSno(String cno, String sno);
}
