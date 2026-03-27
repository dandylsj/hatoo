package com.hatoo.domain.groups;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final GroupRepository groupRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (groupRepository.findByName("DEFAULT").isEmpty()) {
            groupRepository.save(new Group("DEFAULT", "기본 그룹"));
        }
    }
}