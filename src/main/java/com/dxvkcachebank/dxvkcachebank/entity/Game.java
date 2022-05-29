package com.dxvkcachebank.dxvkcachebank.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

@Entity
@Data
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private List<User> contributors;

    private CacheFile latestCache;

    private List<CacheFile> contributions;

    private Long steamId;
}
