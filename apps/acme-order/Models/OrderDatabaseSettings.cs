namespace acme_order.Models
{
    public class OrderDatabaseSettings : IOrderDatabaseSettings
    {
        public string OrdersCollectionName { get; set; }
        public string DatabaseName { get; set; }
    }

    public interface IOrderDatabaseSettings
    {
        string OrdersCollectionName { get; set; }
        string DatabaseName { get; set; }
    }
}