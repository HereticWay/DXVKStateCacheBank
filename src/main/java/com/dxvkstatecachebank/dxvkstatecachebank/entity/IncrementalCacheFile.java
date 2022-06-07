package com.dxvkstatecachebank.dxvkstatecachebank.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class IncrementalCacheFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "game_id")
    private Game game;

    private LocalDateTime lastUpdateTime;

    @Lob
    private byte[] data;
}
