import model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.ProductService;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Update product description
    @PutMapping("/{id}/description")
    public ResponseEntity<String> updateDescription(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {
        productService.updateProductDescription(id, body.get("description"));
        return ResponseEntity.ok("Product description updated!");
    }

    // Add new product
    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody Product product) {
        productService.addProduct(product);
        return ResponseEntity.ok("Product added successfully!");
    }

    // Check if product is on sale
    @GetMapping("/{id}/onsale")
    public ResponseEntity<String> checkSale(@PathVariable int id, @RequestParam String date) {
        return ResponseEntity.ok(productService.checkProductSale(id, date));
    }
}
