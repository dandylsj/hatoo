package com.hatoo.domain.task;

import com.hatoo.domain.groups.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByGroupsId(UUID groupId);

    List<Task> findAllByGroupsContainingAndFinishedTrue(Group group);

}
