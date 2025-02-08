package com.cybercore.companion.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil = new JwtUtil();
    
    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "57921b3ef4aec88149b95865dd2ff25bcd73ce28c6da25ab83177f57ed5df7a4f7ca3de7352455e68a127309012dbc43fa428ed7874a859cc3177f5904f7ee097ef00a6b81f8a73a03bb9c9a9c56fd27592159fe45230bd4c35e77f1fa92c3fce6b24a1072d841fadda180d119bddcf23c530a33b4f741fc058f0725891b9f698e0b1b70efe16d3a952b5501684f74317b89ad8bd102d1af2e1874a50bfeb2d0f1b5541f0a01db17f36d4db1329974986e39e725d0ed6de47736155f49948a9dae01e8c430d1fdec3ba578afc50732231131393575cc77fdffbab795c2dd1d717fca0c7f51b69ae7b6f5503fc5a972704c1bac432b5959d1ee982a849a6c21ee14d1b0b97fefe6d294bf628d81c8fc716f19ed71cc51928dafbe3244b00c8bbd6e6310beefe65eca17e20fe37e7dae1d30b2fb75cb461e123e26e35b736a06278eba53712c1629900d02871aba90881014da6ade149414191d1d10429e0bc3ada64a4d9490e7ea7a842822203e2d7d1a3d81acd69d1dea22e6ae0120c129e8e372cbbfdf303ba8952f7b5d3cb30597e4810f20c053eb618ccf136f7983674e3192542eb6a1b469f146d4aa1f9abc0a5aa8e4d916b75d42ef0b710da2d3d03cec514c902e722da9a74622fd146c66b209805e879972676dadcc85128f376a403b0d07e953f90eb89c6fa66b66542b62f0e3d366e46251c9b83676c7a2288f8bfc");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken("testuser");
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("testuser", jwtUtil.extractUsername(token));
    }
}
