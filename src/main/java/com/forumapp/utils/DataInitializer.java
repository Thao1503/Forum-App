package com.forumapp.utils;

import com.forumapp.entity.RoleEntity;
import com.forumapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(RoleEntity.builder().name("ADMIN").build());
        }

        if (roleRepository.findByName("MEMBER").isEmpty()) {
            roleRepository.save(RoleEntity.builder().name("MEMBER").build());
        }
    }
}