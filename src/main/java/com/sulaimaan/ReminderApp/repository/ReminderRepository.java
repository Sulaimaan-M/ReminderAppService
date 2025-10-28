package com.sulaimaan.ReminderApp.repository;

import com.sulaimaan.ReminderApp.dto.outgoing.DetailedReminderResponse;
import com.sulaimaan.ReminderApp.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    // ✅ Direct DTO mapping for detailed reminders (To-Do tab reminder section)
    @Query("SELECT new com.sulaimaan.ReminderApp.dto.outgoing.DetailedReminderResponse(" +
            "r.id, r.remindedAt, r.isCompleted, t.id, t.taskTxt, t.recurrenceType) " +
            "FROM Reminder r JOIN r.task t " +
            "WHERE t.deviceToken.id = :deviceId " +
            "AND t.recurrenceType <> 'SIMPLE' " +
            "AND r.isCompleted = false " +
            "AND r.remindedAt = (" +
            "    SELECT MAX(r2.remindedAt) " +
            "    FROM Reminder r2 " +
            "    WHERE r2.task = r.task AND r2.isCompleted = false" +
            ") " +
            "ORDER BY r.remindedAt DESC")
    List<DetailedReminderResponse> findLatestIncompleteRemindersByDeviceId(@Param("deviceId") Long deviceId);

    // 🔄 Legacy method (keep for backward compatibility)
    @Query("""
           SELECT r
           FROM Reminder r
           WHERE r.task.deviceToken.id = :deviceId
             AND r.task.recurrenceType <> 'SIMPLE'
             AND r.isCompleted = false
             AND r.remindedAt = (
                 SELECT MAX(r2.remindedAt)
                 FROM Reminder r2
                 WHERE r2.task = r.task
                   AND r2.isCompleted = false
             )
           ORDER BY r.remindedAt DESC
           """)
    List<Reminder> findLatestIncompleteByDevice(@Param("deviceId") Long deviceId);

    // Existing methods
    List<Reminder> findByTaskIdOrderByRemindedAtDesc(Long taskId);
    List<Reminder> findByTaskIdAndIsCompletedOrderByRemindedAtDesc(Long taskId, Boolean isCompleted);
}
