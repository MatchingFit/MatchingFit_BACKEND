package com.example.matching_fit.domain.manager.manager.controller;

import com.example.matching_fit.domain.manager.manager.dto.ManagerDto;
import com.example.matching_fit.domain.manager.manager.service.ManagerService;
import com.example.matching_fit.global.rp.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createManager(@RequestBody ManagerDto managerDto) {
        managerService.createManager(managerDto);
        ApiResponse<?> response = ApiResponse.success(managerDto, "가입 성공!");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
