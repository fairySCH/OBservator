package com.happy.observator.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.happy.observator.service.UserAssetService;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class UserAssetController {

    @Autowired
    private UserAssetService userAssetService;

    @GetMapping("/{userId}")
    public List<UserAsset> getUserAssets(@PathVariable int userId) {
        return userAssetService.getUserAssets(userId);
    }

    @PostMapping
    public UserAsset createUserAsset(@RequestBody UserAsset userAsset) {
        return userAssetService.saveUserAsset(userAsset);
    }
}

