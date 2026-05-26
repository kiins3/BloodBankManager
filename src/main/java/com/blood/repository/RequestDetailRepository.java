package com.blood.repository;

import com.blood.model.RequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestDetailRepository extends JpaRepository<RequestDetail,Integer> {

    List<RequestDetail> findByBloodRequest_RequestId(Integer requestId);
}
