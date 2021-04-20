package com.bculinary.ltiautoevaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bculinary.ltiautoevaluation.entity.Juegos;

@Repository
public interface JuegosRepository extends JpaRepository<Juegos, Long> {

		
	
}
