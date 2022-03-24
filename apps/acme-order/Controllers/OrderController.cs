using acme_order.Models;
using acme_order.Services;
using Microsoft.AspNetCore.Mvc;
using System.Collections.Generic;
using acme_order.Auth;
using acme_order.Response;
using Microsoft.AspNetCore.Authorization;

namespace acme_order.Controllers
{

    [Route("api/[controller]")]
    [ApiController]
    public class OrderController : ControllerBase
    {
        private readonly OrderService _orderService;

        public OrderController(OrderService orderService)
        {
            _orderService = orderService;
        }

        [HttpPost("add/{userid:length(24)}")]
        [ServiceFilter(typeof(AuthorizeResource))]
        public ActionResult<OrderCreateResponse> Create(string userid, Order orderIn)
        {
            return _orderService.Create(userid, orderIn);
        }

        [HttpGet("all")]
        [ServiceFilter(typeof(AuthorizeResource))]
        public ActionResult<List<OrderResponse>> Get() =>
            _orderService.Get();

        [HttpGet("{userId:length(24)}", Name = "GetOrderByUser")]
        [ServiceFilter(typeof(AuthorizeResource))]
        public ActionResult<List<OrderResponse>> Get(string userId)
        {
            var orderList = _orderService.Get(userId);

            if (orderList == null || orderList.Count == 0)
            {
                return NotFound();
            }

            return orderList;
        }
    }
}