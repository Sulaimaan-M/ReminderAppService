package com.sulaimaan.ReminderApp.repository;

import com.sulaimaan.ReminderApp.dto.outgoing.DetailedReminderResponse;
import com.sulaimaan.ReminderApp.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for Reminder entity database operations
 */
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /**
     * Finds the latest incomplete reminder for each recurring task associated with a device
     */
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

    /**
     * Finds all reminders associated with a specific task ordered by reminded time
     */
    @Query("SELECT new com.sulaimaan.ReminderApp.dto.outgoing.DetailedReminderResponse(" +
            "r.id, r.remindedAt, r.isCompleted, t.id, t.taskTxt, t.recurrenceType) " +
            "FROM Reminder r JOIN r.task t " +
            "WHERE t.id = :taskId " +
            "ORDER BY r.remindedAt DESC")
    List<DetailedReminderResponse> findRemindersByTaskId(@Param("taskId") Long taskId);
}
