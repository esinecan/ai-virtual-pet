package com.cybercore.companion.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CorelingStateDto {
    private int dataIntegrity;
    private int processingLoad;
    private int emotionalCharge;
    private float[] memoryVector;

    public CorelingStateDto(int dataIntegrity, int processingLoad, int emotionalCharge, float[] memoryVector) {
        this.dataIntegrity = dataIntegrity;
        this.processingLoad = processingLoad;
        this.emotionalCharge = emotionalCharge;
        this.memoryVector = memoryVector;
    }
}
