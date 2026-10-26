package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByMotherId(Integer motherId);

    List<Conversation> findByPhmAreaCodeOrderByUpdatedAtDesc(String phmAreaCode);

    List<Conversation> findByPhmAreaIdOrderByUpdatedAtDesc(Integer phmAreaId);

    @Query("SELECT c FROM Conversation c WHERE (c.phmAreaId = :areaIdOrCodeNum OR c.phmAreaCode = :areaCode) ORDER BY c.updatedAt DESC")
    List<Conversation> findByAreaIdOrCode(@Param("areaIdOrCodeNum") Integer areaIdOrCodeNum, @Param("areaCode") String areaCode);
}
