package com.happy.observator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.happy.observator.model.UserAsset;
import com.happy.observator.repository.UserAssetRepository;

import java.util.List;

@Service
public class UserAssetService {

    @Autowired
    private UserAssetRepository userAssetRepository;

    public List<UserAsset> getUserAssets(int userId) {
        return userAssetRepository.findAssetsByUserId(userId);
    }

    public UserAsset saveUserAsset(UserAsset userAsset) {
        return userAssetRepository.save(userAsset);
    }
}
