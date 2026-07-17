package com.havingfunwithjava.orders.interfaces;

import com.havingfunwithjava.orders.application.CreateOrderCommand;
import com.havingfunwithjava.orders.application.CreateOrderUseCase;
import com.havingfunwithjava.orders.application.GetOrderUseCase;
import com.havingfunwithjava.orders.application.ListOrdersUseCase;
import com.havingfunwithjava.orders.domain.CustomerId;
import com.havingfunwithjava.orders.domain.Order;
import com.havingfunwithjava.orders.domain.OrderId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para pedidos.
 *
 * - POST   /orders             → cria pedido (201)
 * - GET    /orders             → lista pedidos de um cliente (200)
 * - GET    /orders/{id}        → detalha um pedido (200 ou 404)
 *
 * <p>NOTA sobre auth: em produção, o customerId vem do claim do JWT (repassado
 * pelo gateway). Neste slice, {@code GET /orders} aceita {@code ?customerId=}
 * para não acoplar o serviço à camada de auth (que mora no gateway). O isolamento
 * (cliente não vê pedido alheio) é responsabilidade da borda, que conhece o
 * customerId autenticado.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderUseCase createOrder;
    private final ListOrdersUseCase listOrders;
    private final GetOrderUseCase getOrder;

    public OrderController(CreateOrderUseCase createOrder,
                           ListOrdersUseCase listOrders,
                           GetOrderUseCase getOrder) {
        this.createOrder = createOrder;
        this.listOrders = listOrders;
        this.getOrder = getOrder;
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

    /**
     * Lista pedidos de um cliente. Em produção, customerId virá do JWT;
     * aqui aceita via query param para desacoplar da auth.
     */
    @GetMapping
    public List<OrderResponse> list(@RequestParam UUID customerId) {
        return listOrders.execute(new CustomerId(customerId)).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable UUID id) {
        Order order = getOrder.execute(new OrderId(id));
        return OrderResponse.from(order);
    }
}
