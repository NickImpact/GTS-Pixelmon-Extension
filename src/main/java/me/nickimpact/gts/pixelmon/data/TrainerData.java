package me.nickimpact.gts.pixelmon.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class TrainerData {

    private UUID uuid;

    private String name;

}
