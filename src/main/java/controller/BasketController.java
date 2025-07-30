@RestController
@RequestMapping("/api/baskets")
public class BasketController {

    @Autowired
    private BasketService basketService;

    // Add product to basket
    @PostMapping("/{id}/add")
    public ResponseEntity<String> addItemToBasket(
            @PathVariable int id,
            @RequestBody Map<String, Object> body) {
        basketService.addItemToBasket(id, (int) body.get("productId"),
                (int) body.get("quantity"), (double) body.get("price"));
        return ResponseEntity.ok("Item added to basket!");
    }

    // Check stock for a basket
    @GetMapping("/{id}/check-stock")
    public ResponseEntity<String> checkStock(@PathVariable int id) {
        return ResponseEntity.ok(basketService.checkBasketStock(id));
    }

    // Get total spending of a shopper
    @GetMapping("/shopper/{id}/total")
    public ResponseEntity<Double> getTotalSpending(@PathVariable int id) {
        return ResponseEntity.ok(basketService.getTotalSpending(id));
    }
}
