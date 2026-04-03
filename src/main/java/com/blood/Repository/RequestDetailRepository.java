package com.blood.Repository;

import com.blood.Model.RequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestDetailRepository extends JpaRepository<RequestDetail,Integer> {

}
