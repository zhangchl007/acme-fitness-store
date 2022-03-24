using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using acme_order.Configuration;
using acme_order.Models;
using acme_order.Request;
using acme_order.Response;
using MongoDB.Driver;
using Newtonsoft.Json;

namespace acme_order.Services
{
    public class OrderService
    {
        private readonly IMongoCollection<Order> _orders;
        private static IAcmeServiceSettings _acmeServiceSettings;

        public OrderService(IMongoClient mongoClient, IOrderDatabaseSettings dbSettings, IAcmeServiceSettings acmeServiceSettings)
        {
            var database = mongoClient.GetDatabase(dbSettings.DatabaseName);
            _orders = database.GetCollection<Order>(dbSettings.OrdersCollectionName);
            _acmeServiceSettings = acmeServiceSettings;
        }

        public OrderCreateResponse Create(string userid, Order orderIn)
        {
            var order = new Order
            {
                Date = DateTime.UtcNow.ToString(CultureInfo.CurrentCulture),
                Paid = "pending",
                Userid = userid,
                Firstname = orderIn.Firstname,
                Lastname = orderIn.Lastname,
                Total = orderIn.Total,
                Address = orderIn.Address,
                Email = orderIn.Email,
                Delivery = orderIn.Delivery,
                Card = orderIn.Card,
                Cart = orderIn.Cart
            };

            _orders.InsertOne(order);

            const string transactionId = "pending";

            var paymentResult = MakePayment(orderIn.Total, order.Card);
            var payment = paymentResult.Result;

            var response = new OrderCreateResponse();
            if (string.IsNullOrEmpty(order.Id)) return response;
            var orderFound = _orders.Find(orderDb => orderDb.Id == order.Id).FirstOrDefault();
            if (string.Equals(transactionId, payment.TransactionId)) return response;
            orderFound.Paid = payment.TransactionId;
            Update(orderFound.Id, orderFound);
            response.UserId = userid;
            response.OrderId = orderFound.Id;
            response.Payment = payment;

            return response;
        }

        public List<OrderResponse> Get()
        {
            var orderList = _orders.Find(order => true).ToList();

            return FromOrderToOrderResponse(orderList);
        }

        public List<OrderResponse> Get(string userId)
        {
            var orderList = _orders.Find(order => order.Userid == userId).ToList();

            return FromOrderToOrderResponse(orderList);
        }

        private void Update(string id, Order orderIn) =>
            _orders.ReplaceOne(order => order.Id == id, orderIn);

        private static async Task<Payment> MakePayment(string total, Card card)
        {
            var paymentRequest = new PaymentRequest()
                {
                    Card = new CardRequest()
                    {
                        Number = card.Number,
                        ExpMonth = card.ExpMonth,
                        ExpYear = card.ExpYear,
                        Ccv = card.Ccv
                    },
                    Total = total
                }
                ;

            var json = JsonConvert.SerializeObject(paymentRequest);
            var data = new StringContent(json, Encoding.UTF8, "application/json");
            var url = $"{_acmeServiceSettings.PaymentServiceUrl}/pay";
            
            using var client = new HttpClient();

            var response = await client.PostAsync(url, data);

            if (response.StatusCode != HttpStatusCode.OK && response.StatusCode != HttpStatusCode.Unauthorized &&
                response.StatusCode != HttpStatusCode.BadRequest &&
                response.StatusCode != HttpStatusCode.PaymentRequired) return new Payment();

            var result = response.Content.ReadAsStringAsync().Result;
            var obj = JsonConvert.DeserializeObject<Payment>(result);

            if (obj != null)
                return new Payment
                {
                    Success = obj.Success,
                    Message = obj.Message,
                    Amount = obj.Amount,
                    TransactionId = obj.TransactionId
                };
            return new Payment();
        }

        private static List<OrderResponse> FromOrderToOrderResponse(IEnumerable<Order> orderList)
        {
            return orderList.Select(order =>
                new OrderResponse
                {
                    Userid = order.Userid,
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