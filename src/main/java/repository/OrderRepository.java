import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public double calculateTax(String state, double subtotal) {
        return jdbcTemplate.queryForObject(
                "CALL TAX_COST_SP(?, ?)",
                new Object[]{state, subtotal},
                Double.class
        );
    }

    public void updateOrderShipping(int orderId, String shipper, String trackingNumber, String shipDate) {
        jdbcTemplate.update("CALL STATUS_SHIP_SP(?, ?, ?, ?)", orderId, shipper, trackingNumber, shipDate);
    }
}
