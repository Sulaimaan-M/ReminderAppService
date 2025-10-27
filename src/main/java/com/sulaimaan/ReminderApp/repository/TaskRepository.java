package com.sulaimaan.ReminderApp.repository;

import com.sulaimaan.ReminderApp.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.deviceToken.id = :deviceId AND t.recurrenceType <> 'SIMPLE' ORDER BY t.nextReminderAt ASC")
    List<Task> findNonSimpleTasksByDeviceId(@Param("deviceId") Long deviceId);
}
