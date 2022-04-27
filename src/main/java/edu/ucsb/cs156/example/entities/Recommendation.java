package edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "recommendation")
public class Recommendation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  String requesterEmail;
  String professorEmail;
  String explanation;
  LocalDateTime dateRequested;
  LocalDateTime dateNeeded;
  boolean done;
}