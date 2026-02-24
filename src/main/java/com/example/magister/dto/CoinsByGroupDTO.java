package com.example.magister.dto;

import lombok.Data;
import java.util.List;

@Data
public class CoinsByGroupDTO {
    private Long groupId;
    private String groupName;
    private Integer totalCoins;
    private List<CoinDTO> coins;
}
