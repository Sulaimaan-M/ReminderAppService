package com.sulaimaan.ReminderApp.repository;

import com.sulaimaan.ReminderApp.dto.outgoing.RecurringTaskResponse;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.projection.SimpleTaskProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // ✅ Direct DTO mapping for recurring tasks
    @Query("SELECT new com.sulaimaan.ReminderApp.dto.outgoing.RecurringTaskResponse(" +
            "t.id, t.taskTxt, t.recurrenceType, t.nextReminderAt) " +
            "FROM Task t " +
            "WHERE t.deviceToken.id = :deviceId AND t.recurrenceType <> 'SIMPLE' " +
            "ORDER BY t.nextReminderAt ASC")
    List<RecurringTaskResponse> findRecurringTasksByDeviceId(@Param("deviceId") Long deviceId);

    // ✅ Projection interface for simple tasks with optional reminder
    @Query("SELECT t.id AS taskId, t.taskTxt AS taskTxt, t.nextReminderAt AS nextReminderAt, " +
            "r.id AS reminderId, r.remindedAt AS remindedAt, r.isCompleted AS isCompleted " +
            "FROM Task t LEFT JOIN Reminder r ON r.task.id = t.id " +
            "WHERE t.deviceToken.id = :deviceId AND t.recurrenceType = 'SIMPLE' " +
            "ORDER BY t.nextReminderAt ASC")
    List<SimpleTaskProjection> findSimpleTaskProjectionsByDeviceId(@Param("deviceId") Long deviceId);

    // 🔄 Legacy method (keep for backward compatibility if needed)
    @Query("SELECT t FROM Task t WHERE t.deviceToken.id = :deviceId AND t.recurrenceType <> 'SIMPLE' ORDER BY t.nextReminderAt ASC")
    List<Task> findNonSimpleTasksByDeviceId(@Param("deviceId") Long deviceId);
}
