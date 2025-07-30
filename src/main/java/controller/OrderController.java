import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.OrderService;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Calculate tax for order
    @GetMapping("/tax")
    public ResponseEntity<Double> calculateTax(
            @RequestParam String state,
            @RequestParam double subtotal) {
        return ResponseEntity.ok(orderService.calculateTax(state, subtotal));
    }

    // Update order status with shipping info
    @PutMapping("/{id}/ship")
    public ResponseEntity<String> updateShipping(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {
        orderService.updateOrderShipping(
                id,
                body.get("shipper"),
                body.get("trackingNumber"),
                body.get("shipDate")
        );
        return ResponseEntity.ok("Order updated with shipping info!");
    }
}
