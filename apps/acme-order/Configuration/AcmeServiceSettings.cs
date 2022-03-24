using System;

namespace acme_order.Configuration
{
    public class AcmeServiceSettings : IAcmeServiceSettings
    {
        public string UserServiceUrl { get; set; }
        public string PaymentServiceUrl { get; set; }
    }
    
    public interface IAcmeServiceSettings
    {
        public string UserServiceUrl { get; set; }
        public string PaymentServiceUrl { get; set; }
    }
}