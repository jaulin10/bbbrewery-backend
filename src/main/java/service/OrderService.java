import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public double calculateTax(String state, double subtotal) {
        return orderRepository.calculateTax(state, subtotal);
    }

    public void updateOrderShipping(int orderId, String shipper, String trackingNumber, String shipDate) {
        orderRepository.updateOrderShipping(orderId, shipper, trackingNumber, shipDate);
    }
}
