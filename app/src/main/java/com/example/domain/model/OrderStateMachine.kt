package com.example.domain.model

object OrderStateMachine {

  private val validOrderTransitions: Map<OrderStatus, Set<OrderStatus>> =
    mapOf(
      OrderStatus.ORDER_PLACED to setOf(OrderStatus.ACCEPTED, OrderStatus.CANCELLED),
      OrderStatus.ACCEPTED to setOf(OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED),
      OrderStatus.PREPARING to setOf(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED),
      OrderStatus.READY_FOR_PICKUP to setOf(OrderStatus.LOGISTICS_ASSIGNED, OrderStatus.CANCELLED),
      OrderStatus.LOGISTICS_ASSIGNED to setOf(OrderStatus.PICKED_UP, OrderStatus.CANCELLED),
      OrderStatus.PICKED_UP to setOf(OrderStatus.IN_TRANSIT),
      OrderStatus.IN_TRANSIT to setOf(OrderStatus.DELIVERED),
      OrderStatus.DELIVERED to emptySet(),
      OrderStatus.CANCELLED to emptySet(),
    )

  fun canTransitionOrder(from: OrderStatus, to: OrderStatus): Boolean =
    validOrderTransitions[from]?.contains(to) == true

  fun isTerminal(status: OrderStatus): Boolean =
    status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED

  fun nextLogisticsStep(current: LogisticsStatus): LogisticsStatus? =
    when (current) {
      LogisticsStatus.ASSIGNED -> LogisticsStatus.ACCEPTED
      LogisticsStatus.ACCEPTED -> LogisticsStatus.REACHING_FARMER
      LogisticsStatus.REACHING_FARMER -> LogisticsStatus.ARRIVED_AT_FARM
      LogisticsStatus.ARRIVED_AT_FARM -> LogisticsStatus.PICKUP_CONFIRMED
      LogisticsStatus.PICKUP_CONFIRMED -> LogisticsStatus.IN_TRANSIT
      LogisticsStatus.IN_TRANSIT -> LogisticsStatus.REACHING_CUSTOMER
      LogisticsStatus.REACHING_CUSTOMER -> LogisticsStatus.DELIVERED
      else -> null
    }

  val timelineSteps: List<OrderStatus> =
    listOf(
      OrderStatus.ORDER_PLACED,
      OrderStatus.ACCEPTED,
      OrderStatus.PREPARING,
      OrderStatus.READY_FOR_PICKUP,
      OrderStatus.LOGISTICS_ASSIGNED,
      OrderStatus.PICKED_UP,
      OrderStatus.IN_TRANSIT,
      OrderStatus.DELIVERED,
    )
}
