package com.havingfunwithjava.orders.interfaces;

import com.havingfunwithjava.orders.application.CreateOrderCommand;
import com.havingfunwithjava.orders.application.CreateOrderUseCase;
import com.havingfunwithjava.orders.domain.Order;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller REST para pedidos.
 *
 * - POST /orders → cria pedido (201)
 *
 * (Endpoints de consulta e status entram nas issues #16 e #17.)
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderUseCase createOrder;

    public OrderController(CreateOrderUseCase createOrder) {
        this.createOrder = createOrder;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        List<CreateOrderCommand.ItemRequest> items = request.items().stream()
                .map(i -> new CreateOrderCommand.ItemRequest(
                        i.productId(), i.quantity(), i.expectedUnitPrice(), i.currency()))
                .toList();
        CreateOrderCommand command = new CreateOrderCommand(request.customerId(), items);

        Order created = createOrder.execute(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id().value())
                .toUri();

        return ResponseEntity.created(location).body(OrderResponse.from(created));
    }
}
