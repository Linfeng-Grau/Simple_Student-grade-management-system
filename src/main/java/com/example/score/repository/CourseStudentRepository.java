package com.example.score.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.score.entity.CourseStudent;

public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {

    Optional<CourseStudent> findByCnoAndSno(String cno, String sno);

    long countByCno(String cno);

    @Query("select cs.sno from CourseStudent cs where cs.cno = :cno order by cs.sno")
    List<String> findSnoByCno(@Param("cno") String cno);

    @Query("select cs.cno from CourseStudent cs where cs.sno = :sno order by cs.cno")
    List<String> findCnoBySno(@Param("sno") String sno);

    List<CourseStudent> findBySno(String sno);

    void deleteByCnoAndSno(String cno, String sno);
}
