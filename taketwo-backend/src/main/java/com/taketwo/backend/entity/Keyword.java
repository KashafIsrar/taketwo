// entity/Keyword.java
package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "keywords", uniqueConstraints = @UniqueConstraint(columnNames = "tmdb_keyword_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tmdb_keyword_id", nullable = false, unique = true)
    private Integer tmdbKeywordId;

    @Column(nullable = false)
    private String name;
}