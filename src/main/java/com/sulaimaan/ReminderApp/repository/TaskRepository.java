package com.sulaimaan.ReminderApp.repository;

import com.sulaimaan.ReminderApp.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
