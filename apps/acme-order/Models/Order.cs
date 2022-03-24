using System.Collections.Generic;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace acme_order.Models
{
    public class Order
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }

        public string Date { get; set; }
        public string Paid { get; set; }
        public string Userid { get; set; }
        public string Firstname { get; set; }
        public string Lastname { get; set; }
        public string Total { get; set; }
        public Address Address { get; set; }
        public string Email { get; set; }
        public string Delivery { get; set; }
        public Card Card { get; set; }
        public List<Cart> Cart { get; set; }
    }
}