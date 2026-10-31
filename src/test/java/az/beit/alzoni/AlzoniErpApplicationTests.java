package az.beit.alzoni;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import az.beit.alzoni.domain.Material;
import az.beit.alzoni.domain.MaterialType;
import az.beit.alzoni.domain.Order;
import az.beit.alzoni.domain.OrderStatus;
import az.beit.alzoni.domain.OrderType;
import az.beit.alzoni.domain.PurchaseRequest;
import az.beit.alzoni.domain.Requirement;
import az.beit.alzoni.domain.Role;
import az.beit.alzoni.domain.User;
import az.beit.alzoni.service.ErpService;
import az.beit.alzoni.service.OrderForm;
import az.beit.alzoni.service.UserForm;
import az.beit.alzoni.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AlzoniErpApplicationTests {

    private static final BigDecimal EXPENSE = BigDecimal.valueOf(25);
    private static final BigDecimal PROFIT = BigDecimal.valueOf(50);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ErpService erp;

    @Autowired
    private UserService users;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/orders", "/orders/new", "/design", "/costing", "/approval", "/supply",
            "/purchasing", "/warehouse", "/production", "/completion", "/recipes", "/recipes/HM-001",
            "/materials", "/warehouses", "/workshops", "/profile", "/users", "/users/new", "/users/1", "/settings"})
    void listPagesRender(String url) throws Exception {
        mvc.perform(get(url)).andExpect(status().isOk());
    }

    @Test
    void everyStagePageRendersForEverySeededOrder() throws Exception {
        for (Order order : erp.orders(null)) {
            mvc.perform(get("/orders/" + order.getNo())).andExpect(status().isOk());
        }
        mvc.perform(get("/design/" + erp.designQueue().getFirst().getNo())).andExpect(status().isOk());
        mvc.perform(get("/costing/" + erp.costingQueue().getFirst().getNo())).andExpect(status().isOk());
        mvc.perform(get("/approval/" + erp.approvalQueue().getFirst().getNo())).andExpect(status().isOk());
        mvc.perform(get("/supply/" + erp.supplyQueue().getFirst().getNo())).andExpect(status().isOk());
        mvc.perform(get("/production/" + erp.productionQueue().getFirst().getNo())).andExpect(status().isOk());
    }

    @Test
    void priceAddsExpenseThenProfitOnTop() {
        Order order = kitOrder(1);
        // 1 × 10 → +25% = 12.50 → +50% = 18.75
        assertThat(order.getMaterialCost()).isEqualByComparingTo("10.00");
        assertThat(order.getTotalCost()).isEqualByComparingTo("12.50");
        assertThat(order.getUnitPrice()).isEqualByComparingTo("18.75");
    }

    @Test
    void costingRequiresManualPriceWhenNoLastPurchase() {
        Order order = erp.costingQueue().getFirst();
        assertThatThrownBy(() -> erp.saveCosting(order.getNo(), Map.of(), EXPENSE, PROFIT, 5, LocalDate.now(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void customerRefusalStopsTheProcess() throws Exception {
        String no = erp.approvalQueue().getFirst().getNo();
        mvc.perform(post("/approval/" + no).param("approved", "false").param("reason", "Bahadır"))
                .andExpect(status().is3xxRedirection());
        assertThat(erp.order(no).getStatus()).isEqualTo(OrderStatus.DAYANDIRILDI);
    }

    @Test
    void onlyTheShortageIsPurchasedAndOrderEntersProductionAfterReceipt() {
        Material kit = kit("5");
        Order order = kitOrder(kit, 8);

        erp.openProductionOrder(order.getNo());
        Requirement requirement = order.getRequirements().getFirst();
        assertThat(requirement.getResult()).isEqualTo("Qismən var");
        PurchaseRequest purchase = erp.purchases(order).getFirst();
        assertThat(purchase.getQty()).isEqualByComparingTo("3");

        erp.issueMaterials(order.getNo());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.XAMMAL);
        assertThat(kit.getStock()).isEqualByComparingTo("0");

        erp.placePurchase(purchase.getNo(), "Təchizatçı", null);
        erp.receivePurchase(purchase.getNo(), "Q-1");
        erp.issueMaterials(order.getNo());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.ISTEHSALDA);
        assertThat(kit.getStock()).isEqualByComparingTo("0");
        assertThat(kit.getReserved()).isEqualByComparingTo("0");
    }

    @Test
    void stockHeldForOneOrderIsNotOfferedToTheNext() {
        Material kit = kit("5");
        Order first = kitOrder(kit, 4);
        Order second = kitOrder(kit, 4);

        erp.openProductionOrder(first.getNo());

        assertThat(erp.previewRequirements(second).getFirst().getFromStock()).isEqualByComparingTo("1");
    }

    @Test
    void switchedUserShowsInProfileWithTheirRoleTasks() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User designer = users.activeUsers().stream().filter(u -> u.getRole() == Role.DIZAYNER).findFirst().orElseThrow();

        mvc.perform(post("/profile/switch").session(session).param("userId", String.valueOf(designer.getId())))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/profile").session(session))
                .andExpect(content().string(containsString(designer.getName())))
                .andExpect(content().string(containsString("Dizayn gözləyir")))
                .andExpect(content().string(not(containsString("Satınalma tələbləri"))));
    }

    @Test
    void settingsApplyToNewOrdersOnly() {
        Order existing = erp.orders(OrderStatus.DIZAYN).getFirst();

        erp.saveSettings("Alzoni MMC", BigDecimal.valueOf(30), BigDecimal.valueOf(40), 7);
        Order created = erp.createOrder(new OrderForm("Test", null, "Menecer", false, OrderType.YENI,
                null, "Test məhsul", 1, null, null));

        assertThat(created.getExpensePct()).isEqualByComparingTo("30");
        assertThat(created.getProfitPct()).isEqualByComparingTo("40");
        assertThat(existing.getExpensePct()).isEqualByComparingTo("25");
    }

    @Test
    void currentUserAndLastAdminCannotBeDeactivated() {
        User admin = users.activeUsers().getFirst();
        UserForm inactive = new UserForm(admin.getName(), admin.getRole(), null, null, false);
        User other = users.activeUsers().get(1);

        assertThatThrownBy(() -> users.update(admin.getId(), inactive, admin)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> users.update(admin.getId(), inactive, other)).isInstanceOf(IllegalStateException.class);
    }

    private Order kitOrder(int qty) {
        return kitOrder(kit("100"), qty);
    }

    private Material kit(String stock) {
        Material kit = new Material("T-" + erp.materials().size(), "Test", MaterialType.XAMMAL, "ədəd",
                erp.warehouses().iterator().next(), BigDecimal.TEN, new BigDecimal(stock));
        erp.addMaterial(kit);
        return kit;
    }

    private Order kitOrder(Material kit, int qty) {
        Order order = erp.createOrder(new OrderForm("Test", null, "Menecer", false, OrderType.YENI,
                null, "Test məhsul", qty, null, null));
        erp.routeOrder(order.getNo());
        erp.saveDesign(order.getNo(), null, "t.dwg", null, null, List.of(kit.getCode()), List.of(BigDecimal.ONE), List.of("1-1"));
        erp.saveCosting(order.getNo(), Map.of(), EXPENSE, PROFIT, 5, LocalDate.now(), null);
        erp.decide(order.getNo(), true, null);
        return order;
    }
}
