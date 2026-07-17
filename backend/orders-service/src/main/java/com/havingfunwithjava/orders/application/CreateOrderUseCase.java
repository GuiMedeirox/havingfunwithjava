package com.havingfunwithjava.orders.application;

import com.havingfunwithjava.orders.domain.CatalogClient;
import com.havingfunwithjava.orders.domain.CatalogItem;
import com.havingfunwithjava.orders.domain.CustomerId;
import com.havingfunwithjava.orders.domain.InvalidOrderException;
import com.havingfunwithjava.orders.domain.Money;
import com.havingfunwithjava.orders.domain.Order;
import com.havingfunwithjava.orders.domain.OrderItem;
import com.havingfunwithjava.orders.domain.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: criar um pedido.
 *
 * <p>Orquestra o domínio e a integração com o catalog-service:
 * <ol>
 *   <li>Valida itens via {@link CatalogClient}: cada produto deve existir e estar ativo.</li>
 *   <li>Valida preços: o preço esperado pelo cliente deve bater com o preço atual do catalog.
 *       Isso evita que o cliente pague preço antigo (stale) ou manipulado.</li>
 *   <li>Constrói os {@link OrderItem} (snapshots de nome/preço no momento da compra).</li>
 *   <li>Cria o {@link Order} (em PENDING_PAYMENT) via factory do domínio.</li>
 *   <li>Persiste e retorna.</li>
 * </ol>
 *
 * <p>NOTA sobre estoque: a issue #15 menciona "deduz estoque no catalog após criação".
 * Como o catalog-service atual não tem campo de estoque (só active flag), a "dedução"
 * neste slice é uma chamada ao catalog para confirmar a existência dos itens — a
 * reserva/dedução real de estoque fica para quando o catalog ganhar controle de
 * estoque (futuro). O contrato de validação (itens existem + preços conferem) é o
 * que este slice entrega e já protege contra pedidos inconsistentes.
 */
@Service
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;

    public CreateOrderUseCase(OrderRepository orderRepository, CatalogClient catalogClient) {
        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
    }

    @Transactional
    public Order execute(CreateOrderCommand command) {
        // 1. Busca produtos no catalog (valida existência + active)
        List<UUID> productIds = command.items().stream()
                .map(CreateOrderCommand.ItemRequest::productId)
                .toList();
        List<CatalogItem> catalogItems = catalogClient.findProductsByIds(productIds);

        // Indexa por productId p/ lookup O(1) durante a validação
        Map<UUID, CatalogItem> catalogById = new HashMap<>();
        for (CatalogItem ci : catalogItems) {
            catalogById.put(ci.productId(), ci);
        }

        // 2. Valida cada item: existe + ativo + preço esperado == preço atual
        List<OrderItem> orderItems = new ArrayList<>();
        for (CreateOrderCommand.ItemRequest requested : command.items()) {
            CatalogItem catalogItem = catalogById.get(requested.productId());
            if (catalogItem == null) {
                throw new InvalidOrderException(
                        "Produto não encontrado ou inativo: " + requested.productId());
            }
            Money expectedPrice = Money.of(requested.expectedUnitPrice(), requested.currency());
            if (!samePrice(catalogItem.price(), expectedPrice)) {
                throw new InvalidOrderException(
                        "Preço divergente para o produto " + catalogItem.name()
                                + ": esperado " + requested.expectedUnitPrice()
                                + ", atual " + catalogItem.price().amount().toPlainString());
            }
            // Snapshot do item no momento da compra (nome + preço travados)
            orderItems.add(new OrderItem(
                    catalogItem.productId(),
                    catalogItem.name(),
                    requested.quantity(),
                    catalogItem.price().amount(),
                    catalogItem.price().currency()
            ));
        }

        // 3. Cria o pedido (factory valida invariantes: >=1 item, mesma moeda)
        Order order = Order.createNew(new CustomerId(command.customerId()), orderItems);

        // 4. Persiste em PENDING_PAYMENT e retorna
        return orderRepository.save(order);
    }

    /**
     * Compara preços por valor (BigDecimal usa compareTo, não equals — 1.0 != 1.00 com equals).
     */
    private boolean samePrice(Money a, Money b) {
        if (!a.currency().equals(b.currency())) {
            return false;
        }
        return a.amount().compareTo(b.amount()) == 0;
    }
}
