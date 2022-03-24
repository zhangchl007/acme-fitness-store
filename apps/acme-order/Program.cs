using Microsoft.AspNetCore.Hosting;
using Microsoft.Azure.SpringCloud.Client;
using Microsoft.Extensions.Hosting;
using Steeltoe.Management.Endpoint;

namespace acme_order
{
    public static class Program
    {
        public static void Main(string[] args)
        {
            CreateHostBuilder(args).Build().Run();
        }

        private static IHostBuilder CreateHostBuilder(string[] args) =>
            Host.CreateDefaultBuilder(args)
                .UseAzureSpringCloudService()
                .AddAllActuators()
                .ConfigureWebHostDefaults(webBuilder => { webBuilder.UseStartup<Startup>(); });
    }
}