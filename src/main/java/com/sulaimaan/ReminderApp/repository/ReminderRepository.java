package com.sulaimaan.ReminderApp.repository;

import com.sulaimaan.ReminderApp.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
}
