using acme_order.Auth;
using acme_order.Configuration;
using acme_order.Models;
using acme_order.Services;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Options;
using Steeltoe.Connector.MongoDb;

namespace acme_order
{
    public class Startup
    {
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            // services.AddDiscoveryClient(Configuration);
            services.AddMongoClient(Configuration);

            services.Configure<OrderDatabaseSettings>(
                Configuration.GetSection(nameof(OrderDatabaseSettings)));

            services.AddSingleton<IOrderDatabaseSettings>(sp =>
                sp.GetRequiredService<IOptions<OrderDatabaseSettings>>().Value);

            services.Configure<AcmeServiceSettings>(
                Configuration.GetSection(nameof(AcmeServiceSettings)));

            services.AddSingleton<IAcmeServiceSettings>(sp =>
                sp.GetRequiredService<IOptions<AcmeServiceSettings>>().Value);

            services.AddSingleton<OrderService>();
            services.AddControllers();
            services.AddScoped<AuthorizeResource>();
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }

            app.UseHttpsRedirection();
            app.UseRouting();
            app.UseEndpoints(endpoints => { endpoints.MapControllers(); });
        }
    }
}