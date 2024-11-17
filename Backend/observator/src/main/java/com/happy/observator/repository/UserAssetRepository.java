package com.happy.observator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.happy.observator.model.UserAsset;

import java.util.List;

@Repository
public interface UserAssetRepository extends JpaRepository<UserAsset, Integer> {

    @Query("SELECT a FROM UserAsset a WHERE a.userId = :userId")
    List<UserAsset> findAssetsByUserId(int userId);
}
