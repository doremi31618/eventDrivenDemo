package com.logic.eventdrivendemo.repository;

import com.logic.eventdrivendemo.model.EventRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRecordRepository extends JpaRepository<EventRecord, Long> {
}
