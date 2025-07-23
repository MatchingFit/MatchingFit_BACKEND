package com.example.matching_fit.domain.manager.manager.service;

import com.example.matching_fit.domain.manager.manager.dto.ManagerDto;
import com.example.matching_fit.domain.manager.manager.entity.Manager;
import com.example.matching_fit.domain.manager.manager.repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;

    public void createManager(ManagerDto managerDto) {
        Manager manager = Manager.builder()
                .managerName(managerDto.getManagerName())
                .companyName(managerDto.getCompanyName())
                .build();
        managerRepository.save(manager);
    }
}
