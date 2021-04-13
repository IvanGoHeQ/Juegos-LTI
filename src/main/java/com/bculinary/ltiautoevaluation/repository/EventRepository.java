package com.bculinary.ltiautoevaluation.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bculinary.ltiautoevaluation.entity.Event;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {
}
