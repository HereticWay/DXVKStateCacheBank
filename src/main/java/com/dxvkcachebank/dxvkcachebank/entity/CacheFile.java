package com.dxvkcachebank.dxvkcachebank.entity;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class CacheFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "upload_date",
            nullable = false
    )
    private LocalDateTime uploadDateTime;

    @ManyToOne
    @JoinColumn(
            foreignKey =
                @ForeignKey(name = "uploader_id"),
            nullable = false
    )
    private User uploader;

    @ManyToOne
    @JoinColumn(
            foreignKey =
                @ForeignKey(name = "game_id"),
            nullable = false
    )
    private Game game;

    @Lob
    private byte[] data;
}
