package db.workshift;

import com.fasterxml.jackson.databind.ObjectMapper;
import db.workshift.users.User;
import db.workshift.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShopRepository shopRepository;

    private User user1;
    private User user2;
    private Shop shop1;
    private Shift shift;

    @BeforeEach
    public void initData() throws Exception {
        user1 = userRepository.save(User.create("Bent Jørgensen"));
        user2 = userRepository.save(User.create("Bente Sørgensen"));
        shop1 = createShopWithAShift();
        shift = shop1.getAvailableShifts().get(0);
        Shop shop2 = createShopWithAShift();
        shop2.assignUserToShift(user2, shop2.getAvailableShifts().get(0));
        shopRepository.save(shop2);
    }

    private Shop createShopWithAShift() {
        Shop shop = Shop.create();
        shop.addShift(LocalDateTime.now().plusHours(2), Duration.ofHours(6));
        return shopRepository.save(shop);
    }

    @Test
    public void createShop() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/shops")
                                .content("{\"name\":\"Kalles Kiosk\"}")
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());

        String respBody = resultActions.andReturn().getResponse().getContentAsString();
        Shop shop = objectMapper.readValue(respBody, Shop.class);
        assertNotNull(shop);
        assertNotNull(shop.getId());
    }

    @Test
    public void addShift() throws Exception {
        Shift shift = Shift.create(LocalDateTime.now().plusHours(10), Duration.ofHours(8));
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/shops/%s/shift".formatted(shop1.getId()))
                                .content(objectMapper.writeValueAsString(shift))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void addUser() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/shops/%s/user/%s".formatted(shop1.getId(), user1.getId()))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void assignUserToShift() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.put(
                                        "/api/shops/%s/shift/%s/assign/%s"
                                                .formatted(
                                                        shop1.getId(),
                                                        shift.getId(),
                                                        user1.getId()))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());


    }

    @Test
    public void assignUserToShiftConflicting() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.put(
                                        "/api/shops/%s/shift/%s/assign/%s"
                                                .formatted(
                                                        shop1.getId(),
                                                        shift.getId(),
                                                        user2.getId()))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(result -> assertEquals("Conflicting shift detected", result.getResponse().getErrorMessage()))
        ;
    }

}
