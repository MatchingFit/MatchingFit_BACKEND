package com.example.matching_fit.domain.manager.manager_competency_score.entity;
import com.example.matching_fit.domain.score.entity.Competency;
import com.example.matching_fit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "manager_competency_scores")
public class ManagerCompetencyScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;
    @Column(nullable = false)
    private Integer competencyScore;
}
