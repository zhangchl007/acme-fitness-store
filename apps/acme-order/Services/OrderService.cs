using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using acme_order.Configuration;
using acme_order.Db;
using acme_order.Models;
using acme_order.Request;
using acme_order.Response;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;

namespace acme_order.Services
{
    public class OrderService
    {
        private readonly OrderContext _context;
        private readonly ILogger _logger;
        private static IAcmeServiceSettings _acmeServiceSettings;
        private const string PendingTransactionId = "pending";

        public OrderService(OrderContext context, IAcmeServiceSettings acmeServiceSettings, ILogger<OrderService> logger)
        {
            _context = context;
            _acmeServiceSettings = acmeServiceSettings;
            _logger = logger;
        }
        
        public async Task<OrderCreateResponse> Create(string userid, Order orderIn, string authorization)
        {
            var order = new Order
            {
                Paid = "pending",
                UserId = userid,
                Firstname = orderIn.Firstname,
                Lastname = orderIn.Lastname,
                Total = orderIn.Total,
                Address = orderIn.Address,
                Email = orderIn.Email,
                Delivery = orderIn.Delivery,
                Card = orderIn.Card,
                Cart = orderIn.Cart
            };

            var payment = await MakePayment(orderIn.Total, order.Card, authorization);
            _logger.LogDebug("Received payment response transactionId {transactionId}", payment.TransactionId);
            
            if (string.Equals(PendingTransactionId, payment.TransactionId)) return new OrderCreateResponse();

            order.Paid = payment.TransactionId;
            
            var savedOrder = SaveOrder(order);

            return new OrderCreateResponse
            {
                UserId = userid,
                OrderId = savedOrder.Id.ToString(),
                Payment = payment
            };
        }

        public List<OrderResponse> Get() => 
            FromOrderToOrderResponse(_context.Orders.ToList());

        public List<OrderResponse> Get(string userId) => 
            FromOrderToOrderResponse(_context.Orders.Where(o => o.UserId == userId).ToList());

        private Order SaveOrder(Order order)
        {
            _logger.LogDebug("Attempting to Save Order {order}", order);
            var saved = _context.Orders.Add(order).Entity;
            _context.SaveChanges();
            _logger.LogDebug("Saved Order {saved}", saved);
            return saved;
        }

        private async Task<Payment> MakePayment(string total, Card card, string authorization)
        {
            var paymentRequest = new PaymentRequest()
                {
                    Card = new CardRequest()
                    {
                        Number = card.Number,
                        ExpMonth = card.ExpMonth,
                        ExpYear = card.ExpYear,
                        Ccv = card.Ccv,
                        Type = card.Type
                    },
                    Total = total
                }
                ;

            var json = JsonConvert.SerializeObject(paymentRequest);
            var data = new StringContent(json, Encoding.UTF8, "application/json");
            var url = $"{_acmeServiceSettings.PaymentServiceUrl}/pay";
            
            _logger.LogDebug("Making Payment Request for {total} to {url}", total, url);
            var request = new HttpRequestMessage(HttpMethod.Post, url);
            request.Content = data;
            request.Headers.Authorization = AuthenticationHeaderValue.Parse(authorization);

            using var client = new HttpClient();
            client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

            var response = await client.SendAsync(request);

            if (response.StatusCode != HttpStatusCode.OK && response.StatusCode != HttpStatusCode.Unauthorized &&
                response.StatusCode != HttpStatusCode.BadRequest &&
                response.StatusCode != HttpStatusCode.PaymentRequired) return new Payment();

            var result = response.Content.ReadAsStringAsync().Result;
            var obj = JsonConvert.DeserializeObject<Payment>(result);

            return obj ?? new Payment();
        }

        private static List<OrderResponse> FromOrderToOrderResponse(IEnumerable<Order> orderList)
        {
            return orderList.Select(order =>
                new OrderResponse
                {
                    Userid = order.UserId,
                    Firstname = order.Firstname,
                    Lastname = order.Lastname,
                    Address = order.Address,
                    Email = order.Email,
                    Delivery = order.Delivery,
                    Card = order.Card,
                    Cart = order.Cart,
                    Total = order.Total
                }).ToList();
        }
    }
}