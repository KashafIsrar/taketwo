// entity/MovieCredit.java
package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "movie_credits", uniqueConstraints = @UniqueConstraint(columnNames = {"movie_id", "person_id", "role"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieCredit {

    public enum Role { DIRECTOR, ACTOR }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    // Cast billing order from TMDB - null for directors. Lets us cap how many
    // actors we store per film (top ~10) rather than entire cast lists.
    @Column(name = "billing_order")
    private Integer billingOrder;
}